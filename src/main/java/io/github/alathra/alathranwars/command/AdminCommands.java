package io.github.alathra.alathranwars.command;

import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.hook.NameColorHandler;
import io.github.alathra.alathranwars.utility.UtilsChat;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;

import java.util.Optional;

import static io.github.alathra.alathranwars.enums.CommandArgsWar.ALL_WARS;

public class AdminCommands {
    public AdminCommands() {
        new CommandAPICommand("alathranwarsadmin")
            .withAliases("awa", "wa", "waradmin")
            .withPermission("AlathranWars.admin")
            .withSubcommands(
                commandWar(),
                commandSiege(),
                commandNames(),
                commandWartime()
            )
            .executes((sender, args) -> {
                if (args.count() == 0)
                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "Invalid Arguments.").build());
            })
            .register();
    }

    private CommandAPICommand commandWar() {
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

    private CommandAPICommand commandSiege() {
        return new CommandAPICommand("siege")
            .withSubcommands(
                SiegeCommands.commandStart(true),
                SiegeCommands.commandStop(true),
                SiegeCommands.commandAbandon(true),
                SiegeCommands.commandSurrender(true),
                SiegeCommands.commandList()
            );
    }

    private CommandAPICommand commandNames() {
        return new CommandAPICommand("updatenames")
            .executesPlayer((player, commandArguments) -> {
                Bukkit.getOnlinePlayers().forEach(p -> NameColorHandler.getInstance().calculatePlayerColors(p));
            });
    }

    private CommandAPICommand commandWartime() {
        return new CommandAPICommand("wartime")
            .withArguments(
                CommandUtil.warWarArgument("war", true, ALL_WARS, "")
            )
            .executes((sender, commandArguments) -> {
                War war = commandArguments.getByClass("war", War.class);
                // TODO Allow scheduling of war time
//                AlathranWars.getInstance().setWarTime(!AlathranWars.getInstance().isWarTime());

//                if (AlathranWars.getInstance().isWarTime()) {
//                    sender.sendMessage(
//                        ColorParser.of("<green>War time has started!").build()
//                    );
//                } else {
//                    sender.sendMessage(
//                        ColorParser.of("<red>War time has ended!").build()
//                    );
//                }
            });
    }
}
