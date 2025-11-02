package io.github.alathra.alathranwars.command;

import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.gui.SpawnSelectMenu;
import io.github.alathra.alathranwars.hook.NameColorHandler;
import io.github.alathra.alathranwars.utility.UtilsChat;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;

import static io.github.alathra.alathranwars.enums.CommandArgsWar.ALL_WARS;

public class AdminCommands {
    public AdminCommands() {
        new CommandAPICommand("alathranwarsadmin")
            .withAliases("awa", "wa", "waradmin")
            .withPermission("alathranwars.admin")
            .withSubcommands(
                commandWar(),
                commandSiege(),
                commandNames(),
                commandWartime(),
                commandRespawn()
            )
            .executes((sender, args) -> {
                if (args.count() == 0)
                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "Invalid Arguments.").build());
            })
            .register();
    }

    private static CommandAPICommand commandWar() {
        return new CommandAPICommand("war")
            .withSubcommands(
                WarCommands.commandCreate(true),
                WarCommands.commandDelete(true),
                WarCommands.commandJoin(true),
                WarCommands.commandJoinNear(),
                WarCommands.commandSurrender(true),
                WarCommands.commandList(),
                WarCommands.commandInfo(),
                WarCommands.commandInfoSide(),
                WarCommands.commandKick()
            );
    }

    private static CommandAPICommand commandSiege() {
        return new CommandAPICommand("siege")
            .withSubcommands(
                SiegeCommands.commandStart(true),
                SiegeCommands.commandStop(true),
                SiegeCommands.commandAbandon(true),
                SiegeCommands.commandSurrender(true),
                SiegeCommands.commandList()
            );
    }

    private static CommandAPICommand commandNames() {
        return new CommandAPICommand("updatenames")
            .executesPlayer((player, commandArguments) -> {
                Bukkit.getOnlinePlayers().forEach(p -> NameColorHandler.getInstance().calculatePlayerColors(p));
            });
    }

    private static CommandAPICommand commandWartime() {
        return new CommandAPICommand("wartime")
            .withArguments(
                CommandUtil.warWarArgument("war", true, ALL_WARS, ""),
                new IntegerArgument("delay_minutes", 0).setOptional(true)
            )
            .executes((sender, args) -> {
                War war = args.getByClass("war", War.class);
                Duration delayMinutes = args.getOptionalByClass("delay_minutes", Integer.class).map(Duration::ofMinutes).orElse(Duration.ZERO); // The duration to wait in minutes before the war time starts
                war.setScheduledWarTime(Instant.now().plus(delayMinutes));
                sender.sendMessage(
                    ColorParser.of(
                        "<green>War time for war starting in <time> minutes."
                    )
                        .with("time", String.valueOf(delayMinutes.toMinutes()))
                        .build()
                );
            });
    }

    protected static CommandAPICommand commandRespawn() {
        return new CommandAPICommand("respawn")
            .executes((sender, args) -> {
                if (sender instanceof Player p && !WarController.getInstance().isInActiveWar(p))
                    throw CommandAPIBukkit.failWithAdventureComponent(
                        ColorParser.of("<red>This command can only be used while in an active war.").build()
                    );

                if (sender instanceof Player p)
                    SpawnSelectMenu.open(p);
            });
    }
}
