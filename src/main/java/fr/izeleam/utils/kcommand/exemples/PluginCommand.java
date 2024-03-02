package fr.izeleam.utils.kcommand.exemples;

import fr.izeleam.utils.kcommand.annotations.Command;
import fr.izeleam.utils.kcommand.CommandManager;
import fr.izeleam.utils.kcommand.annotations.TabComplete;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * This is an example of how to use the KCommand library.
 *
 * @version 1.0
 */
public class PluginCommand extends JavaPlugin {

  /**
   * You have to register commands for the CommandManager to execute them.
   */
  @Override
  public void onEnable() {
    final CommandManager manager = CommandManager.getInstance();

    manager.registerCommands(this, this);
  }

  /**
   * This is an example of a command that says hello to the player.
   * All of your methods must associate the first parameter with the SenderType enum.
   * The first parameter is the sender of the command.
   *
   * @param player The player who executed the command.
   * @param args The arguments of the command.
   */
  @Command(name = {"hello", "hi"},
      description = "Say hello to the player",
      usage = "/hello",
      permissions = {"commands.hello"},
      sender = Command.SenderType.PLAYER_ONLY)
  public void sayHelloCommand(final Player player, String[] args) {
    player.sendMessage("Hello, " + player.getName() + "!");
  }

  @Command(name = {"hello.to"},
      description = "Say hello to another player",
      usage = "/hello to <player>",
      permissions = {"commands.helloto"},
      sender = Command.SenderType.PLAYER_ONLY)
  public void sayHelloTo(final Player player, String[] args) {
    Player target = Bukkit.getPlayer(args[0]);
    if (target == null) {
      player.sendMessage("The player " + args[0] + " is not online.");
      return;
    }

    player.sendMessage("§8You said hello to " + target.getName() + "!");
    target.sendMessage("Hello from " + player.getName() + "!");
  }

  /**
   * This is an example of a tab completion method.
   * The method must return a list of strings.
   * The first parameter is the player who's tabing.
   *
   * @param player The player who executed the command.
   * @return A list of strings.
   */
  @TabComplete(name = {"hello"})
  public List<String> onTabComplete(final Player player) {
    return Collections.singletonList("to");
  }

  @TabComplete(name = {"hello.to"})
  public List<String> onTabCompleteTo(final Player player) {
    List<String> list = new ArrayList<>();
    for (Player p : Bukkit.getOnlinePlayers()) {
      list.add(p.getName());
    }
    return list;
  }
}
