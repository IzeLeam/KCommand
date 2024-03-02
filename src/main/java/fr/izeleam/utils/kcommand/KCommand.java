package fr.izeleam.utils.kcommand;

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public class KCommand extends Command {

  private final TabExecutor handle;

  public KCommand(final String name, final TabExecutor handle) {
    super(name);
    this.handle = handle;
  }

  public boolean execute(final CommandSender commandSender, final String label, final String[] args) {
    return this.handle.onCommand(commandSender, this, label, args);
  }

  public List<String> tabComplete(final CommandSender commandSender, final String label, final String[] args) {
    return this.handle.onTabComplete(commandSender, this, label, args);
  }
}
