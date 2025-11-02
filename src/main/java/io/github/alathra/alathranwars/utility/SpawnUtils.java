package io.github.alathra.alathranwars.utility;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.conflict.war.side.spawn.Spawn;
import io.github.alathra.alathranwars.conflict.war.side.spawn.SpawnBuilder;
import io.github.alathra.alathranwars.conflict.war.side.spawn.SpawnCreationException;
import io.github.alathra.alathranwars.conflict.war.side.spawn.SpawnType;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import io.github.milkdrinkers.wordweaver.Translation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Contains utility methods related to the spawn system
 */
public final class SpawnUtils {
    // Internal

    /**
     * Get all spawns for this side
     *
     * @param side side
     * @return list of spawns
     * @implNote this computes a list of spawns
     */
    @ApiStatus.Internal
    public static List<Spawn> computeSpawnPoints(Side side) {
        List<Spawn> locations = new ArrayList<>();

        // Include occupied town spawns & outposts
        final List<Town> occupiedTowns = SideUtils.getOpponent(side).getTownsSurrendered()
            .stream()
            .filter(Town::hasSpawn).
            toList();

        occupiedTowns.forEach(t -> locations.addAll(computeTownSpawns(t, side)));

        // Include town spawns & outposts
        final List<Town> towns = side.getTowns()
            .stream()
            .filter(Town::hasSpawn)
            .toList();

        towns.forEach(t -> locations.addAll(computeTownSpawns(t, side)));

        return locations;
    }

    /**
     * Get all spawns for this town
     *
     * @param town town
     * @return list of spawns
     * @implNote this computes a list of spawns
     */
    @ApiStatus.Internal
    public static List<Spawn> computeTownSpawns(Town town, Side side) {
        List<Spawn> spawns = new ArrayList<>();

        if (town.getSpawnOrNull() != null)
            try {
                spawns.add(
                    new SpawnBuilder()
                        .setName(town.getName()).setLocation(town.getSpawnOrNull())
                        .setType(SpawnType.TOWN)
                        .setSide(side)
                        .setTown(town)
                        .build()
                );
            } catch (SpawnCreationException e) {
                Logger.get().debug("Failed to create spawn: ", e);
            }

        spawns.addAll(computeOutpostSpawns(town, side));

        return spawns;
    }

    /**
     * Get a map of outpost spawns mapped by outpost name to location
     *
     * @param town town
     * @return list of spawns
     * @implNote this computes a list of spawns
     */
    @ApiStatus.Internal
    public static List<Spawn> computeOutpostSpawns(Town town, Side side) {
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
                    .setTown(town)
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
     *
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
     *
     * @param war war
     * @return list of spawns
     * @implNote this retrieves a cached list
     */
    public static List<Spawn> getSpawns(War war) {
        return war.getSides().stream().flatMap(side -> getSpawns(side).stream()).toList();
    }

    /**
     * Get a list of all war spawns for this side
     *
     * @param side side
     * @return set of spawns
     * @implNote this retrieves a cached list
     */
    public static Set<Spawn> getSpawns(Side side) {
        return side.getSpawnManager().getSpawns();
    }

    /**
     * Get a list of all war spawns for this government
     *
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
     *
     * @param p player
     * @return list of spawns
     * @implNote this retrieves a cached list
     */
    public static List<Spawn> getSpawns(Player p) {
        return WarController.getInstance().getWars(p).stream()
            .map(war -> war.getPlayerSide(p))
            .filter(Objects::nonNull)
            .filter(side -> !side.isSurrendered(p))
            .flatMap(side -> side.getSpawns().stream())
            .collect(Collectors.toList());
    }

    // TODO Add sort modes for GUI
    public enum SortType {
        ALPHABETICALLY_A_Z,
        ALPHABETICALLY_Z_A,
    }

    /**
     * Sort spawns alphabetically and after category
     *
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
     *
     * @param location     the location to compare against
     * @param spawns       a collection of spawns
     * @param checkProxied should we check if spawns are proxied
     * @return a spawn or empty optional
     * @implNote uses {@link #getClosestSpawn(Location, Collection, double, boolean)} internally
     */
    public static Optional<Spawn> getClosestSpawn(Location location, Collection<Spawn> spawns, boolean checkProxied) {
        return getClosestSpawn(location, spawns, Double.MAX_VALUE, checkProxied);
    }

    /**
     * Get the closest spawn from a list of spawns
     *
     * @param location     the location to compare against
     * @param spawns       a collection of spawns
     * @param range        a maximum range (spawns outside this range will be ignored)
     * @param checkProxied should we check if spawns are proxied
     * @return a spawn or empty optional
     */
    public static Optional<Spawn> getClosestSpawn(Location location, Collection<Spawn> spawns, double range, boolean checkProxied) {
        return spawns.stream()
            .filter(s -> !checkProxied || !s.isProxied())
            .filter(s -> location.getWorld().equals(s.getLocation().getWorld()))
            .filter(s -> s.getLocation().distanceSquared(location) <= Math.pow(range, 2))
            .reduce((spawn1, spawn2) -> { // Returns the nearest spawn
                final Location loc1 = spawn1.getLocation();
                final double dist1 = loc1.distanceSquared(location);

                final Location loc2 = spawn2.getLocation();
                final double dist2 = loc2.distanceSquared(location);

                return dist1 < dist2 ? spawn1 : spawn2;
            });
    }

    public static final double SPAWN_FRIENDLY_MIN_TOWN_RANGE = Math.pow(Cfg.get().getOrDefault("respawns.rallies.min-range-town-friendly", 150), 2);
    public static final double SPAWN_FRIENDLY_MIN_OUTPOST_RANGE = Math.pow(Cfg.get().getOrDefault("respawns.rallies.min-range-outpost-friendly", 100), 2);
    public static final double SPAWN_FRIENDLY_MIN_RALLY_RANGE = Math.pow(Cfg.get().getOrDefault("respawns.rallies.min-range-rally-friendly", 120), 2);

    public static final double SPAWN_HOSTILE_MIN_TOWN_RANGE = Math.pow(Cfg.get().getOrDefault("respawns.rallies.min-range-town-hostile", 100), 2);
    public static final double SPAWN_HOSTILE_MIN_OUTPOST_RANGE = Math.pow(Cfg.get().getOrDefault("respawns.rallies.min-range-outpost-hostile", 50), 2);
    public static final double SPAWN_HOSTILE_MIN_RALLY_RANGE = Math.pow(Cfg.get().getOrDefault("respawns.rallies.min-range-rally-hostile", 25), 2);

    // 3x2x3 Rectangle (Spawn location is always 0, 0, 0)
    final static Vector BOUNDING_BOX_MIN = new Vector(-1, 0, -1);
    final static Vector BOUNDING_BOX_MAX = new Vector(1, 1, 1);

    /**
     * Check if a new spawn location is allowed
     *
     * @param location location
     * @param side     the side this spawn will be associated with
     * @throws SpawnCreationException thrown when spawn is illegal
     */
    @ApiStatus.Internal
    public static void isValidSpawnPlacement(Location location, Side side) throws SpawnCreationException {
        // Check if 3x2x3 is clear around banner
        final Vector min = BOUNDING_BOX_MIN;
        final Vector max = BOUNDING_BOX_MAX;

        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    if (x == 0 || z == 0) // The banner is being placed here :)
                        continue;

                    final Location boxPos = location.clone().add(x, y, z);
                    if (!isAllowedMaterialNearSpawn(boxPos.getBlock().getType()))
                        throw new SpawnCreationException(
                            Translation.as("rally.placement.bounding-box")
                        );
                }
            }
        }

        // Check friendly spawns
        final Set<Spawn> friendlySpawns = getSpawns(side);

        for (Spawn spawn : friendlySpawns.stream().filter(spawn -> spawn.getType().equals(SpawnType.TOWN)).toList()) {
            if (location.distanceSquared(spawn.getLocation()) < SPAWN_FRIENDLY_MIN_TOWN_RANGE) // Check if too close to any spawns
                throw new SpawnCreationException(
                    ColorParser.of(Translation.of("rally.placement.min-range-town-friendly"))
                        .with("range", String.valueOf(Math.sqrt(SPAWN_FRIENDLY_MIN_TOWN_RANGE)))
                        .build()
                );
        }

        for (Spawn spawn : friendlySpawns.stream().filter(spawn -> spawn.getType().equals(SpawnType.OUTPOST)).toList()) {
            if (location.distanceSquared(spawn.getLocation()) < SPAWN_FRIENDLY_MIN_OUTPOST_RANGE) // Check if too close to any spawns
                throw new SpawnCreationException(
                    ColorParser.of(Translation.of("rally.placement.min-range-outpost-friendly"))
                        .with("range", String.valueOf(Math.sqrt(SPAWN_FRIENDLY_MIN_OUTPOST_RANGE)))
                        .build()
                );
        }

        for (Spawn spawn : friendlySpawns.stream().filter(spawn -> spawn.getType().equals(SpawnType.RALLY)).toList()) {
            if (location.distanceSquared(spawn.getLocation()) < SPAWN_FRIENDLY_MIN_RALLY_RANGE) // Check if too close to any spawns
                throw new SpawnCreationException(
                    ColorParser.of(Translation.of("rally.placement.min-range-rally-friendly"))
                        .with("range", String.valueOf(Math.sqrt(SPAWN_FRIENDLY_MIN_RALLY_RANGE)))
                        .build()
                );
        }

        // Check hostile spawns
        final Set<Spawn> hostileSpawns = getSpawns(SideUtils.getOpponent(side));

        for (Spawn spawn : hostileSpawns.stream().filter(spawn -> spawn.getType().equals(SpawnType.TOWN)).toList()) {
            if (location.distanceSquared(spawn.getLocation()) < SPAWN_HOSTILE_MIN_TOWN_RANGE) // Check if too close to enemy spawns
                throw new SpawnCreationException(
                    ColorParser.of(Translation.of("rally.placement.min-range-town-hostile"))
                        .with("range", String.valueOf(Math.sqrt(SPAWN_HOSTILE_MIN_TOWN_RANGE)))
                        .build()
                );
        }

        for (Spawn spawn : hostileSpawns.stream().filter(spawn -> spawn.getType().equals(SpawnType.OUTPOST)).toList()) {
            if (location.distanceSquared(spawn.getLocation()) < SPAWN_HOSTILE_MIN_OUTPOST_RANGE) // Check if too close to enemy spawns
                throw new SpawnCreationException(
                    ColorParser.of(Translation.of("rally.placement.min-range-outpost-hostile"))
                        .with("range", String.valueOf(Math.sqrt(SPAWN_HOSTILE_MIN_OUTPOST_RANGE)))
                        .build()
                );
        }

        for (Spawn spawn : hostileSpawns.stream().filter(spawn -> spawn.getType().equals(SpawnType.RALLY)).toList()) {
            if (location.distanceSquared(spawn.getLocation()) < SPAWN_HOSTILE_MIN_RALLY_RANGE) // Check if too close to enemy spawns
                throw new SpawnCreationException(
                    ColorParser.of(Translation.of("rally.placement.min-range-rally-hostile"))
                        .with("range", String.valueOf(Math.sqrt(SPAWN_HOSTILE_MIN_RALLY_RANGE)))
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
        // Scan through enemy locations to figure out which side owns the closest one
        return SideUtils.getPlayerSides(p).stream()
            .filter(side -> side.getWar().isWarTime())
            .map(side -> SideUtils.getOpponent(side).getSpawns()) // Get list of all opposing/hostile sides' spawns (sides we are at war with)
            .flatMap(Collection::stream)
            .filter(spawn -> spawn.getType().equals(SpawnType.TOWN) || spawn.getType().equals(SpawnType.OUTPOST)) // Only keep town and outpost spawns
            .filter(spawn -> spawn.getLocation().getWorld().equals(location.getWorld())) // Check same world
            .reduce((spawn1, spawn2) -> { // Get closest spawn
                final Location loc1 = spawn1.getLocation();
                final double dist1 = loc1.distanceSquared(location);

                final Location loc2 = spawn2.getLocation();
                final double dist2 = loc2.distanceSquared(location);

                return dist1 < dist2 ? spawn1 : spawn2;
            })
            .map(Spawn::getSide); // Return optional side
    }

    public static boolean isAllowedMaterialNearSpawn(Material material) {
        if (material.isAir())
            return true;

        if (material.equals(Material.WATER))
            return false;

        if (material.equals(Material.LAVA))
            return false;

        if (!material.isSolid())
            return true;

        return Tag.SMALL_FLOWERS.isTagged(material) ||
            Tag.FLOWERS.isTagged(material) ||
            Tag.SAPLINGS.isTagged(material) ||
            Tag.CROPS.isTagged(material) ||
            Tag.CAVE_VINES.isTagged(material) ||
            Tag.CLIMBABLE.isTagged(material) ||
            Tag.CORAL_PLANTS.isTagged(material) ||
            Tag.UNDERWATER_BONEMEALS.isTagged(material);
    }
}
