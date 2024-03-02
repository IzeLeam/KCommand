package fr.izeleam.utils.kcommand;

import fr.izeleam.utils.kcommand.annotations.Command.SenderType;
import fr.izeleam.utils.kcommand.annotations.TabComplete;
import fr.izeleam.utils.kcommand.utils.SingleMap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang.Validate;
import java.lang.reflect.ParameterizedType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.command.Command;

public class CommandManager implements TabExecutor {

  private static CommandManager instance;
  private CommandMap commandMap;
  private final Map<String, SingleMap<Object, Method>> commands;
  private final Map<String, SingleMap<Object, Method>> tabCompletes;

  public static CommandManager getInstance() {
    if (instance == null) {
      instance = new CommandManager();
    }
    return instance;
  }

  public CommandManager() {
    this.commands = new HashMap<>();
    this.tabCompletes = new HashMap<>();
    try {
      this.commandMap = (CommandMap) SimplePluginManager.class.getDeclaredField("commandMap").get(Bukkit.getPluginManager());
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  public void registerCommands(final Plugin plugin, final Object object) {
    for (final Method method : object.getClass().getDeclaredMethods()) {
      if (method.isAnnotationPresent(fr.izeleam.utils.kcommand.annotations.Command.class)) {

        final fr.izeleam.utils.kcommand.annotations.Command command = method.getAnnotation(fr.izeleam.utils.kcommand.annotations.Command.class);

        Validate.notNull(command.name());
        Validate.notEmpty(command.name());
        Validate.notNull(command.sender());

        final Class<?> clazz = command.sender().getAssociatedType();

        Validate.isTrue(method.getParameterTypes().length > 0 && method.getParameterTypes()[0].equals(clazz),
            "The first argument of the command " + Arrays.toString(
                command.name()) + "  must be of the type " + clazz.getSimpleName());
        Validate.isTrue(method.getParameterTypes().length > 1 && method.getParameterTypes()[1].equals(String[].class),
            "The second argument of the command " + Arrays.toString(
                command.name()) + "  must be of the type String[]");

        for (final String cmd : command.name()) {
          this.commands.put(cmd, new SingleMap<>(object, method));
          final String label = cmd.contains(".") ? cmd.split("[.]")[0] : cmd;

          if (this.commandMap.getCommand(label) == null) {
            final Command bukkitCmd = new KCommand(label, this);
            this.commandMap.register(plugin.getName(), bukkitCmd);
          }
        }
      }

      if (method.isAnnotationPresent(TabComplete.class)) {
        final TabComplete tabComplete = method.getAnnotation(TabComplete.class);

        Validate.notNull(tabComplete.name());
        Validate.notEmpty(tabComplete.name());

        final Type genericReturnType = method.getGenericReturnType();

        Validate.isTrue(genericReturnType instanceof ParameterizedType,
            "The method " + method.getName() + " must return a List<String> for the " + tabComplete.name()[0] + " command");

        final ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;

        Validate.isTrue(parameterizedType.getRawType() == List.class,
            "The method " + method.getName() + " must return a List<String> for the " + tabComplete.name()[0] + " command");
        Validate.isTrue(parameterizedType.getActualTypeArguments().length == 1,
            "The method " + method.getName() + " must return a List<String> for the " + tabComplete.name()[0] + " command");
        Validate.isTrue(parameterizedType.getActualTypeArguments()[0] == String.class,
            "The method " + method.getName() + " must return a List<String> for the " + tabComplete.name()[0] + " command");

        Class<?> clazz = method.getParameterTypes()[0];

        Validate.isTrue(clazz.equals(Player.class), "The first argument of the method " + method.getName() + " must be of the type Player");

        for (final String cmd : tabComplete.name()) {
          this.tabCompletes.put(cmd, new SingleMap<>(object, method));
        }
      }
    }
  }

  public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
    for (int i = args.length; i >= 0; --i) {
      final StringBuilder sb = new StringBuilder();
      sb.append(label.toLowerCase());

      for (int x = 0; x < i; ++x) {
        sb.append(".").append(args[x].toLowerCase());
      }

      final String cmd = sb.toString();
      if (this.commands.containsKey(cmd)) {
        final Method method = this.commands.get(cmd).getValue();
        final Object methodObject = this.commands.get(cmd).getKey();
        return this.executeCommand(i, sender, method, methodObject, args);
      }
    }
    return false;
  }

  private boolean executeCommand(final int i, final CommandSender commandSender, final Method method, final Object methodObject,
      final String[] args) {
    final fr.izeleam.utils.kcommand.annotations.Command command = method.getAnnotation(fr.izeleam.utils.kcommand.annotations.Command.class);
    if (command.sender().equals(SenderType.PLAYER_ONLY) && !(commandSender instanceof Player)) {
      commandSender.sendMessage("§cOnly players can use this Command.");
      return false;
    }

    final Object[] params = new Object[method.getParameterTypes().length];
    if (params.length >= 1) {
      if (!command.sender().getAssociatedType().isInstance(commandSender)) {
        commandSender.sendMessage("§cOnly the CONSOLE can use this Command.");
        return true;
      }

      params[0] = command.sender().getAssociatedType().cast(commandSender);
      if (params.length >= 2) {
        params[1] = this.getSubArgs(i, args);
      }
    }
    if (this.hasPerm(command, commandSender)) {
      try {
        method.invoke(methodObject, params);
      } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | ClassCastException e) {
        e.printStackTrace();
      }
    }
    return true;
  }

  private boolean hasPerm(final fr.izeleam.utils.kcommand.annotations.Command command, final CommandSender commandSender) {
    if (commandSender instanceof Player) {
      for (final String perm : command.permissions()) {
        if (!commandSender.hasPermission(perm)) {
          return false;
        }
      }
    }
    return true;
  }

  private String[] getSubArgs(final int i, final String[] args) {
    final String[] newArgs = new String[Math.max(0, args.length - i)];
    if (newArgs.length != 0) {
      System.arraycopy(args, i, newArgs, 0, newArgs.length);
    }
    return newArgs;
  }

  public List<String> onTabComplete(final CommandSender commandSender, final Command command, final String label, final String[] args) {
    final List<String> completeList = new ArrayList<>();
    if (!(commandSender instanceof Player)) {
      return completeList;
    }

    for (int i = args.length; i >= 0; --i) {
      final StringBuilder sb = new StringBuilder();
      sb.append(label.toLowerCase());

      for (int x = 0; x < i; ++x) {
        sb.append(".").append(args[x].toLowerCase());
      }

      final String cmd = sb.toString();
      if (this.tabCompletes.containsKey(cmd)) {
        final Method method = this.tabCompletes.get(cmd).getValue();
        final Object methodObject = this.tabCompletes.get(cmd).getKey();
        return this.getTabComplete(i, (Player) commandSender, method, methodObject, args);
      }
    }
    return completeList;
  }

  private List<String> getTabComplete(final int i, final Player player, final Method method, final Object methodObject, final String[] args) {
    List<String> tabComplete = new ArrayList<>();
    final Object[] params = new Object[method.getParameterTypes().length];
    params[0] = player;

    if (params.length >= 2) {
      final String[] newArgs = this.getSubArgs(i, args);
      params[1] = newArgs;
    }

    try {
      tabComplete = (List<String>) method.invoke(methodObject, params);
    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | ClassCastException e) {
      e.printStackTrace();
    }

    if (tabComplete.isEmpty()) {
      return tabComplete;
    }

    tabComplete = tabComplete.stream().filter(arg -> arg.toLowerCase().contains(args[args.length - 1].toLowerCase()))
        .collect(Collectors.toList());
    return tabComplete;
  }
}