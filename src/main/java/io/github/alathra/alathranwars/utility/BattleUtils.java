package io.github.alathra.alathranwars.utility;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathranwars.conflict.battle.siege.Siege;
import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.meta.ControlPoint;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class BattleUtils {
    public record SidedTown(War war, Side side, Town town) {
    }

    /**
     * Finds the nearest siegable town for a player, excluding allies and active sieges.
     *
     * @param player The player to find the nearest siegable town for.
     * @param ignoreAllies Whether to ignore allied towns.
     * @param ignoreActiveSieges Whether to ignore towns under active sieges.
     * @return An Optional containing the nearest SidedTown, or empty if none found.
     */
    public static Optional<SidedTown> getNearestSiegableTown(
        final Player player,
        final boolean ignoreAllies,
        final boolean ignoreActiveSieges
    ) {
        final Location playerLoc = player.getLocation();

        return WarController.getInstance().getWars(player).stream()
            .filter(War::isWarTime)
            .flatMap(war -> war.getSides().stream())
            .filter(side -> !side.isOnSide(player) && !side.isSiegeGraceActive()) // Excludes allies and sides with grace time
            .flatMap(side -> side.getTowns().stream().map(t -> new SidedTown(side.getWar(), side, t))) // Excludes surrendered towns
            .filter(t -> !WarController.getInstance().isInAnySieges(t.town())) // Excludes towns under siege
            .filter(t -> {
                // Excludes towns without a control point
                final Location l = ControlPoint.get(t.town);
                if (l == null)
                    return false;

                // Excludes towns in other worlds
                if (!l.getWorld().equals(playerLoc.getWorld()))
                    return false;

                // Excludes towns that are too far away
                return l.distanceSquared(playerLoc) <= Cfg.get().getOrDefault("battles.sieges.trigger.range", 250L);
            })
            .reduce((t1, t2) -> { // Returns the nearest town
                final Location loc1 = ControlPoint.get(t1.town);
                final double dist1 = loc1.distanceSquared(playerLoc);

                final Location loc2 = ControlPoint.get(t2.town);
                final double dist2 = loc2.distanceSquared(playerLoc);

                return dist1 < dist2 ? t1 : t2;
            });
    }

    /**
     * Checks if a player is a captain of their side in the current war.
     * A captain is a mayor, co-mayor, king, co-king or war leader in a war.
     * @param player The player to check.
     * @return True if the player is a captain, false otherwise.
     */
    public static boolean isCaptain(final Player player) {
         final Resident res = TownyAPI.getInstance().getResident(player);
        if (res == null)
            return false;

        return res.isMayor() || res.hasTownRank("co-mayor") || res.isKing() || res.hasNationRank("co-king");
    }

    public static boolean isSurrendered(
        final Player player,
        final Side side
    ) {
        return side.isSurrendered(player);
    }

    /**
     * Attempts to dynamically start a siege at the player's current location.
     * @param player The player attempting to start the siege.
     */
    public static void attemptSiegeAtLocation(
        final Player player
    ) {
        final Optional<SidedTown> nearestSiegableTown = getNearestSiegableTown(player, true, true);
        if (nearestSiegableTown.isEmpty()) {
            player.sendRichMessage("<red>You cannot start a siege as there are no attackable towns near you.");
            return;
        }

        final SidedTown sidedTown = nearestSiegableTown.get();

        if (!isCaptain(player)) {
            player.sendRichMessage("<red>You cannot start a siege as you are not a captain.");
            return;
        }

        if (isSurrendered(player, sidedTown.side())) {
            player.sendRichMessage("<red>You cannot start a siege as you have surrendered.");
            return;
        }

        startSiege(player, sidedTown.town(), sidedTown.war(), sidedTown.side());
    }

    public static void startSiege(
        Player siegeLeader,
        Town town,
        War war,
        Side side
    ) {
        side.setSiegeGrace();
        Siege siege = new Siege(war, town, siegeLeader);

        war.addSiege(siege);

        Bukkit.broadcast(
            ColorParser.of(
                    "<prefix>The town of <town> has been put to siege by <side>!"
                )
                .with("prefix", UtilsChat.getPrefix())
                .with("town", town.getName())
                .with("side", side.getName())
                .build()
        );

        siege.start();
    }
}
