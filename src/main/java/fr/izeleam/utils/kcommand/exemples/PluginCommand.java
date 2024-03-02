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

public class PluginCommand extends JavaPlugin {

    @Override
    public void onEnable() {
      final CommandManager manager = CommandManager.getInstance();

      manager.registerCommands(this, this);
    }

  @Command(name = {"hello", "hi"},
      description = "Say hello to the player",
      usage = "/hello",
      permissions = {"commands.hello"},
      sender = Command.SenderType.PLAYER_ONLY)
  public void sayHelloCommand(final Player player) {
    player.sendMessage("Hello, " + player.getName() + "!");
  }

  @Command(name = {"hello.to"},
      description = "Say hello to another player",
      usage = "/hello to <player>",
      permissions = {"commands.helloto"},
      sender = Command.SenderType.PLAYER_ONLY)
  public void sayHelloToEveryBody(final Player player, String[] args) {
    Player target = Bukkit.getPlayer(args[0]);
    if (target == null) {
      player.sendMessage("The player " + args[0] + " is not online.");
      return;
    }

    player.sendMessage("§8You said hello to " + target.getName() + "!");
    target.sendMessage("Hello from " + player.getName() + "!");
  }

  @TabComplete(name = {"hello"})
  public List<String> onTabComplete(final Player player, String[] args) {
    return Collections.singletonList("to");
  }

  @TabComplete(name = {"hello.to"})
  public List<String> onTabCompleteTo(final Player player, String[] args) {
    List<String> list = new ArrayList<>();
    for (Player p : Bukkit.getOnlinePlayers()) {
      list.add(p.getName());
    }
    return list;
  }
}
