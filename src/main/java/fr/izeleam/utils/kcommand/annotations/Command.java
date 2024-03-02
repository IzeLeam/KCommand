package fr.izeleam.utils.kcommand.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Command {

  String[] name();
  String description() default "";
  String usage() default "";
  String[] permissions() default {};
  SenderType sender() default SenderType.DEFAULT;

  public enum SenderType {
    PLAYER_ONLY(Player.class),
    CONSOLE_ONLY(ConsoleCommandSender.class),
    DEFAULT(CommandSender.class);

    final Class<?> associated;

    SenderType(final Class<?> associated) {
      this.associated = associated;
    }

    public Class<?> getAssociatedType() {
      return this.associated;
    }
  }
}
