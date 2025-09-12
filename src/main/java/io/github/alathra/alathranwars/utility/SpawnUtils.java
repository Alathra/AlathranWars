package io.github.alathra.alathranwars.utility;

import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.conflict.war.side.spawn.Spawn;
import io.github.alathra.alathranwars.conflict.war.side.spawn.SpawnBuilder;
import io.github.alathra.alathranwars.conflict.war.side.spawn.SpawnCreationException;
import io.github.alathra.alathranwars.conflict.war.side.spawn.SpawnType;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Contains utility methods related to the spawn system
 */
public final class SpawnUtils {
    // Internal

    /**
     * Get all spawns for this side
     * @param side side
     * @return list of spawns
     * @implNote this computes a list of spawns
     */
    @ApiStatus.Internal
    public static List<Spawn> getSpawnPoints(Side side) { // TODO Execute
        List<Spawn> locations = new ArrayList<>();

        // Include occupied town spawns & outposts
        final List<Town> occupiedTowns = SideUtils.getOpponent(side).getTownsSurrendered()
            .stream()
            .filter(Town::hasSpawn).
            toList();

        occupiedTowns.forEach(t -> locations.addAll(getTownSpawns(t, side)));

        // Include town spawns & outposts
        final List<Town> towns = side.getTowns()
            .stream()
            .filter(Town::hasSpawn)
            .toList();

        towns.forEach(t -> locations.addAll(getTownSpawns(t, side)));

        return locations;
    }

    /**
     * Get all spawns for this town
     * @param town town
     * @return list of spawns
     * @implNote this computes a list of spawns
     */
    @ApiStatus.Internal
    public static List<Spawn> getTownSpawns(Town town, Side side) {
        List<Spawn> spawns = new ArrayList<>();

        if (town.getSpawnOrNull() != null)
            try {
                spawns.add(
                    new SpawnBuilder()
                        .setName(town.getName()).setLocation(town.getSpawnOrNull())
                        .setType(SpawnType.TOWN)
                        .setSide(side)
                        .build()
                );
            } catch (SpawnCreationException e) {
                Logger.get().debug("Failed to create spawn: ", e);
            }

        spawns.addAll(getOutpostSpawns(town, side));

        return spawns;
    }

    /**
     * Get a map of outpost spawns mapped by outpost name to location
     * @param town town
     * @return list of spawns
     * @implNote this computes a list of spawns
     */
    @ApiStatus.Internal
    public static List<Spawn> getOutpostSpawns(Town town, Side side) {
        List<Spawn> spawns = new ArrayList<>();

        int i = 0;
        for (Location location : town.getAllOutpostSpawns()) {
            i++;

            final TownBlock outpost = TownyAPI.getInstance().getTownBlock(location);
            if (outpost == null)
                continue;

            try {
                final String outpostName = outpost.hasPlotObjectGroup() ? outpost.getPlotObjectGroup().getName() : outpost.getName();
                final Spawn spawn = new SpawnBuilder()
                    .setName(outpostName.isEmpty() ? String.valueOf(i) : outpostName)
                    .setLocation(location)
                    .setType(SpawnType.OUTPOST)
                    .setSide(side)
                    .build();

                spawns.add(spawn);
            } catch (SpawnCreationException e) {
                Logger.get().debug("Failed to create spawn: ", e);
            }
        }

        return spawns;
    }

    // Public

    /**
     * Get a list of all spawns in the plugin
     * @return list of spawns
     * @implNote this retrieves a cached list
     */
    public static List<Spawn> getSpawns() {
        return WarController.getInstance().getWars().stream()
            .flatMap(war -> getSpawns(war).stream())
            .toList();
    }

    /**
     * Get a list of all war spawns in this war
     * @param war war
     * @return list of spawns
     * @implNote this retrieves a cached list
     */
    public static List<Spawn> getSpawns(War war) {
        return war.getSides().stream().flatMap(side -> getSpawns(side).stream()).toList();
    }

    /**
     * Get a list of all war spawns for this side
     * @param side side
     * @return list of spawns
     * @implNote this retrieves a cached list
     */
    public static List<Spawn> getSpawns(Side side) {
        return side.getSpawnManager().getSpawns();
    }

    /**
     * Get a list of all war spawns for this government
     * @param government nation or town
     * @return list of spawns
     * @implNote this retrieves a cached list
     */
    public static List<Spawn> getSpawns(Government government) {
        if (government instanceof Nation n) {
            return WarController.getInstance().getWars(n).stream()
                .map(war -> war.getSide(n))
                .filter(Objects::nonNull)
                .filter(side -> !side.isSurrendered(n))
                .flatMap(side -> SpawnUtils.getSpawns(side).stream()).toList();
        } else if (government instanceof Town t) {
            return WarController.getInstance().getWars(t).stream()
                .map(war -> war.getSide(t))
                .filter(Objects::nonNull)
                .filter(side -> !side.isSurrendered(t))
                .flatMap(side -> SpawnUtils.getSpawns(side).stream()).toList();
        }
        return Collections.emptyList();
    }

    /**
     * Get a list of all war spawns for this player
     * @param p player
     * @return list of spawns
     * @implNote this retrieves a cached list
     */
    public static List<Spawn> getSpawns(Player p) {
        return WarController.getInstance().getWars(p).stream()
            .map(war -> war.getPlayerSide(p))
            .filter(Objects::nonNull)
            .filter(side -> !side.isSurrendered(p))
            .flatMap(side -> SpawnUtils.getSpawns(side).stream()).toList();
    }

    /**
     * Sort spawns alphabetically and after category
     * @param spawns list of spawns
     * @return sorted list of spawns
     */
    public static List<Spawn> sortSpawns(List<Spawn> spawns) {
        // Sorts by enum type
        // Sorts by name
        spawns.sort(Comparator.comparing(Spawn::getType)
            .thenComparing(Spawn::getName));
        return spawns;
    }

    /**
     * Get the closest spawn from a list of spawns, (excludes proxied spawns)
     * @param location the location to compare against
     * @param spawns a collection of spawns
     * @param checkProxied should we check if spawns are proxied
     * @return a spawn or null
     * @implNote uses {@link #getClosestSpawn(Location, Collection, double, boolean)} internally
     */
    @Nullable
    public static Spawn getClosestSpawn(Location location, Collection<Spawn> spawns, boolean checkProxied) {
        return getClosestSpawn(location, spawns, 1000000000, checkProxied);
    }

    /**
     * Get the closest spawn from a list of spawns, (excludes proxied spawns)
     * @param location the location to compare against
     * @param spawns a collection of spawns
     * @param range a maximum range (spawns outside this range will be ignored)
     * @param checkProxied should we check if spawns are proxied
     * @return a spawn or null
     */
    @Nullable
    public static Spawn getClosestSpawn(Location location, Collection<Spawn> spawns, double range, boolean checkProxied) {
        Spawn spawnResult = null; // The value to return
        double closestSpawn = 1000000000;

        for (Spawn spawn : spawns) {
            final Location spawnLoc = spawn.getLocation();

            if (checkProxied && spawn.isProxied()) continue;
            if (!location.getWorld().equals(spawnLoc.getWorld())) continue;

            final double distance = location.distance(spawnLoc);
            if (distance < closestSpawn && distance <= range) {
                spawnResult = spawn;
                closestSpawn = distance;
            }
        }

        return spawnResult;
    }

    public static final double SPAWN_FRIENDLY_MIN_TOWN_RANGE = 300;
    public static final double SPAWN_FRIENDLY_MIN_OUTPOST_RANGE = 300;
    public static final double SPAWN_FRIENDLY_MIN_RALLY_RANGE = 120;

    public static final double SPAWN_HOSTILE_MIN_TOWN_RANGE = 150;
    public static final double SPAWN_HOSTILE_MIN_OUTPOST_RANGE = 150;
    public static final double SPAWN_HOSTILE_MIN_RALLY_RANGE = 40;

    /**
     * Check if a new spawn location is allowed
     * @param location location
     * @param side the side this spawn will be associated with
     * @throws SpawnCreationException thrown when spawn is illegal
     */
    @ApiStatus.Internal
    public static void isValidSpawnPlacement(Location location, Side side) throws SpawnCreationException {
        // Check if 3x3x3 is clear around banner
        final List<Vector> locationList = List.of(
            new Vector(1D, 2D, 0D),
            new Vector(1D, 2D, 0D),
            new Vector(0D, 2D, 1D),
            new Vector(0D, 2D, 1D)
        );

        for (var vec : locationList) {
            final RayTraceResult res = location.getBlock().rayTrace(location, vec, 3, FluidCollisionMode.ALWAYS);
            if (res == null)
                continue;

            if (res.getHitBlock() != null)
                throw new SpawnCreationException(
                    ColorParser.of("<red>A rally must have a clear 3x3 spawning area around it!").build()
                );
        }

        // Check friendly spawns
        final List<Spawn> friendlySpawns = getSpawns(side);

        for (Spawn spawn : friendlySpawns.stream().filter(spawn -> spawn.getType().equals(SpawnType.TOWN)).toList()) {
            if (location.distance(spawn.getLocation()) < SPAWN_FRIENDLY_MIN_TOWN_RANGE) // Check if too close to any spawns
                throw new SpawnCreationException(
                    ColorParser.of("<red>A rally must be further than <range> blocks away from a friendly town!")
                        .with("range", String.valueOf(SPAWN_FRIENDLY_MIN_TOWN_RANGE))
                        .build()
                );
        }

        for (Spawn spawn : friendlySpawns.stream().filter(spawn -> spawn.getType().equals(SpawnType.OUTPOST)).toList()) {
            if (location.distance(spawn.getLocation()) < SPAWN_FRIENDLY_MIN_OUTPOST_RANGE) // Check if too close to any spawns
                throw new SpawnCreationException(
                    ColorParser.of("<red>A rally must be further than <range> blocks away from a friendly outpost!")
                        .with("range", String.valueOf(SPAWN_FRIENDLY_MIN_OUTPOST_RANGE))
                        .build()
                );
        }

        for (Spawn spawn : friendlySpawns.stream().filter(spawn -> spawn.getType().equals(SpawnType.RALLY)).toList()) {
            if (location.distance(spawn.getLocation()) < SPAWN_FRIENDLY_MIN_RALLY_RANGE) // Check if too close to any spawns
                throw new SpawnCreationException(
                    ColorParser.of("<red>A rally must be further than <range> blocks away from a friendly rally!")
                        .with("range", String.valueOf(SPAWN_FRIENDLY_MIN_RALLY_RANGE))
                        .build()
                );
        }

        // Check hostile spawns
        final List<Spawn> hostileSpawns = getSpawns(SideUtils.getOpponent(side));

        for (Spawn spawn : hostileSpawns.stream().filter(spawn -> spawn.getType().equals(SpawnType.TOWN)).toList()) {
            if (location.distance(spawn.getLocation()) < SPAWN_HOSTILE_MIN_TOWN_RANGE) // Check if too close to enemy spawns
                throw new SpawnCreationException(
                    ColorParser.of("<red>A rally must be further than <range> blocks away from a hostile town!")
                        .with("range", String.valueOf(SPAWN_HOSTILE_MIN_TOWN_RANGE))
                        .build()
                );
        }

        for (Spawn spawn : hostileSpawns.stream().filter(spawn -> spawn.getType().equals(SpawnType.OUTPOST)).toList()) {
            if (location.distance(spawn.getLocation()) < SPAWN_HOSTILE_MIN_OUTPOST_RANGE) // Check if too close to enemy spawns
                throw new SpawnCreationException(
                    ColorParser.of("<red>A rally must be further than <range> blocks away from a hostile outpost!")
                        .with("range", String.valueOf(SPAWN_HOSTILE_MIN_OUTPOST_RANGE))
                        .build()
                );
        }

        for (Spawn spawn : hostileSpawns.stream().filter(spawn -> spawn.getType().equals(SpawnType.RALLY)).toList()) {
            if (location.distance(spawn.getLocation()) < SPAWN_HOSTILE_MIN_RALLY_RANGE) // Check if too close to enemy spawns
                throw new SpawnCreationException(
                    ColorParser.of("<red>A rally must be further than <range> blocks away from a friendly rally!")
                        .with("range", String.valueOf(SPAWN_HOSTILE_MIN_RALLY_RANGE))
                        .build()
                );
        }
    }

    /**
     * Calculates which {@link Side} a spawn should belong to if placed at the specified location
     *
     * @param location spawn location
     * @param p        the placer of the spawn
     * @return optional with side if one could be determined
     */
    @ApiStatus.Internal
    public static Optional<Side> calculateSpawnSide(Location location, Player p) {
        final List<Side> playerSides = SideUtils.getPlayerSides(p);

        @Nullable Side closestSide = null;
        double closestSideRange = 1000000000;

        // Scan through enemy locations to figure out which side owns the closest one
        for (Side side : playerSides) {
            // Get list of all opposing sides (sides we are at war with)
            final List<Spawn> hostileSpawns = getSpawns(SideUtils.getOpponent(side));

            // Scan for closest town
            for (Spawn spawn : hostileSpawns.stream().filter(spawn -> spawn.getType().equals(SpawnType.TOWN)).toList()) {
                if (!location.getWorld().equals(spawn.getLocation().getWorld())) continue;

                final double distance = location.distance(spawn.getLocation());
                if (distance < closestSideRange) {
                    closestSide = side;
                    closestSideRange = distance;
                }
            }

            // Scan for closest outpost
            for (Spawn spawn : hostileSpawns.stream().filter(spawn -> spawn.getType().equals(SpawnType.OUTPOST)).toList()) {
                if (!location.getWorld().equals(spawn.getLocation().getWorld())) continue;

                final double distance = location.distance(spawn.getLocation());
                if (distance < closestSideRange) {
                    closestSide = side;
                    closestSideRange = distance;
                }
            }
        }

        if (closestSide == null)
            return playerSides.stream().findAny();

        return Optional.of(closestSide);
    }
}
