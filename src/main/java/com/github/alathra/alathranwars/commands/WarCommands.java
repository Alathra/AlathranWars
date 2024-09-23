package com.github.alathra.alathranwars.commands;

import com.github.alathra.alathranwars.conflict.war.War;
import com.github.alathra.alathranwars.conflict.war.WarBuilder;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.github.alathra.alathranwars.conflict.war.side.Side;
import com.github.alathra.alathranwars.conflict.war.side.SideCreationException;
import com.github.alathra.alathranwars.hook.NameColorHandler;
import com.github.alathra.alathranwars.utility.UtilsChat;
import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.github.alathra.alathranwars.enums.CommandArgsWar.*;

public class WarCommands {
    public WarCommands() {
        new CommandAPICommand("war")
            .withSubcommands(
                commandCreate(false),
                commandDelete(false),
                commandJoin(false),
                commandSurrender(false),
                commandList(),
                commandInfo()
            )
            .executesPlayer((sender, args) -> {
                if (args.count() == 0)
                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "Invalid Arguments. /war help").build());
            })
            .register();
    }

    public static CommandAPICommand commandCreate(boolean asAdmin) {
        return new CommandAPICommand("create")
            .withPermission("AlathranWars.admin")
            .withArguments(
                CommandUtil.customTownAndNationArgument("side1", "war", false),
                CommandUtil.customTownAndNationArgument("side2", "war", false),
                new BooleanArgument("event"),
                new GreedyStringArgument("warlabel")
                    .replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info -> List.of(
                            "Nations War of Conquest",
                            "Nations War for Survival",
                            "Nations War of Liberation"
                        ))
                    )
            )
            .executesPlayer(WarCommands::warCreate);
    }

    public static CommandAPICommand commandDelete(boolean asAdmin) {
        return new CommandAPICommand("delete")
            .withPermission("AlathranWars.admin")
            .withArguments(
                CommandUtil.warWarArgument("war", asAdmin, ALL_WARS, "")
            )
            .executesPlayer(WarCommands::warDelete);
    }

    public static CommandAPICommand commandJoin(boolean asAdmin) {
        return new CommandAPICommand("join")
            .withPermission("AlathranWars.war.join")
            .withArguments(
                CommandUtil.warWarArgument("war", asAdmin, asAdmin ? ALL_WARS : OUT_WAR, ""),
                CommandUtil.warSideCreateArgument("side", "war", asAdmin, !asAdmin, ""),
                CommandUtil.warTargetCreateArgument("target", "war", asAdmin).setOptional(!asAdmin)
            )
            .executesPlayer((Player p, CommandArguments args) -> warJoin(p, args, asAdmin));
    }

    public static CommandAPICommand commandJoinNear() {
        return new CommandAPICommand("joinnear")
            .withPermission("AlathranWars.admin")
            .withArguments(
                CommandUtil.warWarArgument("war", true, ALL_WARS, ""),
                CommandUtil.warSideCreateArgument("side", "war", true, false, "")
            )
            .executesPlayer((Player p, CommandArguments args) -> warJoinNear(p, args, true));
    }

    public static CommandAPICommand commandSurrender(boolean asAdmin) {
        return new CommandAPICommand("surrender")
            .withArguments(
                CommandUtil.warWarArgument("war", asAdmin, asAdmin ? ALL_WARS : IN_WAR, "player"),
                new PlayerArgument("player")
                    .setOptional(true)
                    .withPermission("AlathranWars.admin")
            )
            .executesPlayer((Player p, CommandArguments args) -> warSurrender(p, args, false));
    }

    public static CommandAPICommand commandList() {
        return new CommandAPICommand("list")
            .executesPlayer(WarCommands::warList);
    }

    public static CommandAPICommand commandInfo() {
        return new CommandAPICommand("info")
            .withArguments(
                CommandUtil.warWarArgument("war", false, ALL_WARS, "player")
            )
            .executesPlayer(WarCommands::warInfo);
    }

    public static CommandAPICommand commandInfoSide() {
        return new CommandAPICommand("infoside")
            .withArguments(
                CommandUtil.warWarArgument("war", false, ALL_WARS, "player"),
                CommandUtil.warSideArgument("side", "war", true, false, "")
            )
            .executesPlayer(WarCommands::warInfoSide);
    }

    public static CommandAPICommand commandKick() {
        return new CommandAPICommand("kick")
            .withPermission("AlathranWars.admin")
            .withArguments(
                CommandUtil.warWarArgument("war", true, ALL_WARS, "player"),
                CommandUtil.playerOrTownOrNationInWar("target", "war")
            )
            .executesPlayer(WarCommands::warKick);
    }

    protected static void warCreate(CommandSender p, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        if (!(args.get("warlabel") instanceof final String argLabel))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You need to specify a war label.").build());

        if (Objects.equals(args.get("side1"), args.get("side2")))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>Cannot declare war on oneself.").build());

        final boolean event = (boolean) args.getOrDefault("event", false);

        final UUID uuid1 = (UUID) args.get("side1");
        final UUID uuid2 = (UUID) args.get("side2");

        // Create new war depending on what argument was specified
        @Nullable Town town1 = TownyAPI.getInstance().getTown(uuid1);
        @Nullable Town town2 = TownyAPI.getInstance().getTown(uuid2);

        // Automatically escalate to nation level if possible
        @Nullable Nation nation1 = town1 == null ? TownyAPI.getInstance().getNation(uuid1) : town1.getNationOrNull();
        @Nullable Nation nation2 = town2 == null ? TownyAPI.getInstance().getNation(uuid2) : town2.getNationOrNull();

        // Implement permission checking here if this is ever going to be exposed to players

        // Create war depending on what args have been passed, attempt to create nation wars first
        WarBuilder builder = new WarBuilder()
            .setUuid(UUID.randomUUID())
            .setLabel(argLabel)
            .setEvent(event);
        if (nation1 != null) {
            if (nation2 != null) { // Nation vs Nation
                // Allow civil wars
                if (nation2.getUUID() != nation1.getUUID()) {
                    builder
                        .setAggressor(nation1)
                        .setVictim(nation2);
                } else {
                    builder
                        .setAggressor(town1)
                        .setVictim(town2);
                }
            } else if (town2 != null) { // Nation vs Town
                builder
                    .setAggressor(nation1)
                    .setVictim(town2);
            }
        } else if (town1 != null) {
            if (town2 != null) { // Town vs Town
                builder
                    .setAggressor(town1)
                    .setVictim(town2);
            } else if (nation2 != null) { // Nation vs Town
                builder
                    .setAggressor(town1)
                    .setVictim(nation2);
            }
        }
        try {
            builder.create(); // TODO Add war to list
        } catch (SideCreationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void warDelete(Player p, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");
        if (war == null) return;

        war.draw();
    }

    protected static void warJoin(Player p, @NotNull CommandArguments args, boolean asAdmin) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");
        if (war == null) return;

        if (!(args.get("side") instanceof final Side side))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You need to specify a side.").build());

        if (!(args.getOptional("target").orElse(new CommandUtil.TownyIdentifierArgument(CommandUtil.TownyIdentifierArgument.TownyIdentifierArgumentType.PLAYER, p.getUniqueId())) instanceof CommandUtil.TownyIdentifierArgument target))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Failed to yeet from the war because the target is invalid.").build());

        CommandUtil.TownyIdentifierArgument.TownyIdentifierArgumentType targetType = target.getType();
        UUID targetUuid = target.getUUID();

        @Nullable Resident res = TownyAPI.getInstance().getResident(p);
        if (res == null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Resident invalid.").build());

        switch (targetType) {
            case NATION -> {
                Nation nation = TownyAPI.getInstance().getNation(targetUuid);

                if (nation == null)
                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>No nation of that name exists.").build());

                if (war.isInWar(nation))
                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Nation is already in that war.").build());

                final boolean canKingJoin = (res.hasNation() && res.getNationOrNull().equals(nation) && res.isKing() && nation.isKing(res));

                if (!asAdmin && !canKingJoin)
                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>You are not of sufficient rank to join your nation into the war.").build());

                side.add(nation);
                nation.getResidents().stream().filter(Resident::isOnline).map(Resident::getPlayer).toList().forEach(player -> NameColorHandler.getInstance().calculatePlayerColors(player));
                Bukkit.broadcast(
                    ColorParser.of(
                            "<prefix>The nation of <nation> joined the war of <war> on the side of <side>."
                        )
                        .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                        .parseMinimessagePlaceholder("nation", nation.getName())
                        .parseMinimessagePlaceholder("war", war.getLabel())
                        .parseMinimessagePlaceholder("side", side.getName())
                        .build()
                );
                final Title warTitle = Title.title(
                    ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War")
                        .build(),
                    ColorParser.of("<gray><i>You entered the war of <war>!")
                        .parseMinimessagePlaceholder("war", war.getLabel())
                        .build(),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                );
                final Sound warSound = Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.VOICE, 0.5f, 1.0F);

                nation.getResidents().stream().filter(Resident::isOnline).map(Resident::getPlayer).toList().forEach(player -> {
                    player.showTitle(warTitle);
                    player.playSound(warSound);
                });
            }
            case TOWN -> {
                Town town = TownyAPI.getInstance().getTown(targetUuid);

                if (town == null)
                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>No town of that name exists.").build());

                if (war.isInWar(town))
                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Town is already in that war.").build());

                final boolean canMayorJoin = (res.hasTown() && res.getTownOrNull().equals(town) && res.isMayor() && town.isMayor(res));

                if (!asAdmin && !canMayorJoin)
                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>You are not of sufficient rank to join your town into the war.").build());

                side.add(town);
                town.getResidents().stream().filter(Resident::isOnline).map(Resident::getPlayer).toList().forEach(player -> NameColorHandler.getInstance().getPlayerNameColor(player));
                Bukkit.broadcast(
                    ColorParser.of(
                            "<prefix>The town of <town> joined the war of <war> on the side of <side>."
                        )
                        .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                        .parseMinimessagePlaceholder("town", town.getName())
                        .parseMinimessagePlaceholder("war", war.getLabel())
                        .parseMinimessagePlaceholder("side", side.getName())
                        .build()
                );
                final Title warTitle = Title.title(
                    ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War")
                        .build(),
                    ColorParser.of("<gray><i>You entered the war of <war>!")
                        .parseMinimessagePlaceholder("war", war.getLabel())
                        .build(),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                );
                final Sound warSound = Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.VOICE, 0.5f, 1.0F);

                town.getResidents().stream().filter(Resident::isOnline).map(Resident::getPlayer).toList().forEach(player -> {
                    player.showTitle(warTitle);
                    player.playSound(warSound);
                });
            }
            case PLAYER -> {
                Player player = Bukkit.getPlayer(targetUuid);

                if (player == null)
                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>No player of that name exists.").build());

                if (war.isInWar(player))
                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Player is already in that war.").build());

                if (!asAdmin)
                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Players cannot individually join wars.").build());

                player.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "You have joined the war.").build());
                side.add(player);
                NameColorHandler.getInstance().calculatePlayerColors(player);
                final Title warTitle = Title.title(
                    ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War")
                        .build(),
                    ColorParser.of("<gray><i>You entered the war of <war>!")
                        .parseMinimessagePlaceholder("war", war.getLabel())
                        .build(),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                );
                final Sound warSound = Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.VOICE, 0.5f, 1.0F);

                player.showTitle(warTitle);
                player.playSound(warSound);
            }
            default -> {
                throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>The target is invalid.").build());
            }
        }

        /*for (@NotNull Siege siege : war.getSieges()) {
            if (siege.getAttackerSide().equals(side)) {
                siege.addPlayer(argPlayer, BattleSide.ATTACKER);
            } else {
                siege.addPlayer(argPlayer, BattleSide.DEFENDER);
            }
        }*/
    }

    protected static void warJoinNear(@NotNull Player p, @NotNull CommandArguments args, boolean asAdmin) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");
        if (war == null) return;

        if (!(args.get("side") instanceof final Side side))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You need to specify a side.").build());

        final Location location = p.getLocation();

        for (Player targetPlayer : Bukkit.getOnlinePlayers()) {
            if (p.equals(targetPlayer)) continue;
            final Location targetLocation = targetPlayer.getLocation();
            if (!location.getWorld().equals(targetLocation.getWorld())) continue;
            if (location.distance(targetLocation) > 25D) continue;
            if (war.isInWar(targetPlayer)) continue;

            targetPlayer.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "You have joined the war.").build());
            side.add(targetPlayer);
            NameColorHandler.getInstance().calculatePlayerColors(targetPlayer);
            final Title warTitle = Title.title(
                ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War")
                    .build(),
                ColorParser.of("<gray><i>You entered the war of <war>!")
                    .parseMinimessagePlaceholder("war", war.getLabel())
                    .build(),
                Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
            );
            final Sound warSound = Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.VOICE, 0.5f, 1.0F);

            targetPlayer.showTitle(warTitle);
            targetPlayer.playSound(warSound);
        }
    }

    protected static void warSurrender(@NotNull Player p, @NotNull CommandArguments args, boolean asAdmin) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");
        if (war == null) return;

        if (!(args.getOptional("player").orElse(p) instanceof Player argPlayer))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Player not found.").build());

        // Towny Resident Object
        @Nullable Resident res = TownyAPI.getInstance().getResident(argPlayer);
        if (res == null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Resident invalid.").build());

        // Town check
        @Nullable Town town = res.getTownOrNull();
        if (town == null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>You are not in a town.").build());

        @Nullable Side side = war.getSide(town);
        if (side == null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>You are not in that war.").build());

        final @Nullable Nation resNation = res.getNationOrNull();
        final @Nullable Town resTown = res.getTownOrNull();

        if (resNation != null && (p.hasPermission("AlathranWars.nationsurrender") || res.isKing() || asAdmin)) {
            // Has nation surrender permission
            argPlayer.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "You have surrendered the war for " + resNation.getName() + ".").build());
            Bukkit.broadcast(ColorParser.of(UtilsChat.getPrefix() + "The nation of " + resNation.getName() + " has surrendered!").build());
            war.surrender(resNation);
        } else if (resTown == null) {
            // Cannot surrender nation involvement
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>You cannot surrender for your nation.").build());
        }

        if (resTown != null && (p.hasPermission("AlathranWars.townsurrender") || res.isMayor() || asAdmin)) {
            // Is in indepdenent town & has surrender perms
            argPlayer.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "You have surrendered the war for " + resTown.getName() + ".").build());
            Bukkit.broadcast(ColorParser.of(UtilsChat.getPrefix() + "The town of " + resTown.getName() + " has surrendered!").build());
            war.cancelSieges(resTown);
            war.surrender(resTown);
        } else {
            // No perms
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>You cannot surrender war for your town.").build());
        }
    }

    protected static void warKick(@NotNull Player p, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");
        if (war == null) return;

        if (!(args.get("target") instanceof CommandUtil.TownyIdentifierArgument target))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Failed to yeet from the war because the target is invalid.").build());

        CommandUtil.TownyIdentifierArgument.TownyIdentifierArgumentType targetType = target.getType();
        UUID targetUuid = target.getUUID();

        switch (targetType) {
            case PLAYER -> {
                OfflinePlayer player = Bukkit.getOfflinePlayer(targetUuid);
                Side sidePlayer = war.getPlayerSide(targetUuid);
                sidePlayer.kick(player.getUniqueId());
                p.sendMessage(ColorParser.of("Target yeeted.").build());
                if (player.getPlayer() != null)
                    NameColorHandler.getInstance().calculatePlayerColors(player.getPlayer());
            }
            case TOWN -> {
                Town town = TownyAPI.getInstance().getTown(targetUuid);
                Side sideTown = war.getSide(town);
                sideTown.kick(town);
                p.sendMessage(ColorParser.of("Target yeeted.").build());
                town.getResidents().stream().filter(Resident::isOnline).map(Resident::getPlayer).forEach(player1 -> NameColorHandler.getInstance().calculatePlayerColors(player1));
            }
            case NATION -> {
                Nation nation = TownyAPI.getInstance().getNation(targetUuid);
                Side sideNation = war.getSide(nation);
                sideNation.kick(nation);
                p.sendMessage(ColorParser.of("Target yeeted.").build());
                nation.getResidents().stream().filter(Resident::isOnline).map(Resident::getPlayer).forEach(player1 -> NameColorHandler.getInstance().calculatePlayerColors(player1));
            }
            default ->
                throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Failed to yeet from the war because the target is not in it.").build());
        }
    }

    private static void warList(@NotNull Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        Set<War> wars = WarController.getInstance().getWars();

        if (wars.isEmpty()) {
            p.sendMessage(ColorParser.of("<red>There are no wars at the moment, the world is at peace.").build());
            return;
        }

        final StringBuilder msg = new StringBuilder();

        for (War war : wars) {
            msg.append("\n<white><bold>%s<reset>".formatted(war.getLabel()));
            msg.append("\n <grey>Sides: <white>%s <grey>vs <white>%s".formatted(war.getSide1().getName(), war.getSide2().getName()));
            msg.append("\n <grey>Score: <white>%s <grey>vs <white>%s".formatted(war.getSide1().getScore(), war.getSide2().getScore()));
            msg.append("\n");
        }

        p.sendMessage(
            ColorParser.of(
                msg.toString()
            ).build()
        );
    }

    private static void warInfo(Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");
        if (war == null) return;

        final StringBuilder msg = new StringBuilder();

        for (Side side : war.getSides()) {
            msg.append("\n<white><bold>%s<reset>".formatted(side.getName()));
            msg.append("\n <grey>Score: <green>%s".formatted(side.getScore()));
            msg.append("\n <grey>Nations: <green>%s<grey>/<red>%s".formatted(side.getNations().size(), side.getNationsAll().size()));
            msg.append("\n <grey>Towns: <green>%s<grey>/<red>%s".formatted(side.getTowns().size(), side.getTownsAll().size()));
            msg.append("\n <grey>Players: <green>%s<grey>/<red>%s".formatted(side.getPlayers().size(), side.getPlayersAll().size()));
            msg.append("\n");
        }

        p.sendMessage(
            ColorParser.of(
                msg.toString()
            ).build()
        );
    }

    private static void warInfoSide(Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");
        if (war == null) return;

        if (!(args.get("side") instanceof final Side side))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You need to specify a side.").build());


        final StringBuilder msg = new StringBuilder();

        msg.append("\n<white><bold>Nations:<reset>");
        for (Nation nation : side.getNations()) {
            msg.append("\n <grey>%s".formatted(nation.getName()));
        }
        msg.append("\n<white><bold>Towns:<reset>");
        for (Town town : side.getTowns()) {
            msg.append("\n <grey>%s".formatted(town.getName()));
        }
        msg.append("\n");

        p.sendMessage(
            ColorParser.of(
                msg.toString()
            ).build()
        );
    }
}