package io.github.alathra.alathranwars.conflict.war.side.spawn;

import io.github.alathra.alathranwars.conflict.IUpdateable;
import io.github.alathra.alathranwars.conflict.war.side.Side;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Contains a cached list of all spawns related to a {@link Side}.
 */
public class SpawnCache implements IUpdateable {
    private final Side side;
    private final List<Spawn> spawns;
    private final List<SoftReference<RallyPoint>> rallies;

    public SpawnCache(Side side) {
        this(side, new ArrayList<>());
    }

    public SpawnCache(Side side, List<RallyPoint> rallyPoints) {
        this.side = side;
        this.spawns = new ArrayList<>();
        this.rallies = new ArrayList<>();
        spawns.addAll(rallyPoints);
        getSpawns().forEach(spawn -> {
            if (spawn instanceof RallyPoint rally)
                this.rallies.add(new SoftReference<>(rally));
            spawn.setSide(side);
        });
    }

    /**
     * Get the {@link Side} associated with this spawn cache
     * @return side
     */
    public Side getSide() {
        return side;
    }

    /**
     * Get a list of all spawns
     * @return list
     */
    public List<Spawn> getSpawns() {
        return spawns;
    }


    /**
     * Get a list of all rallies
     * Convenience method for getting a cached list of rallies so the user doesn't have to loop
     * @return list
     */
    public List<RallyPoint> getRallies() {
        return rallies.stream()
            .map(SoftReference::get)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * Runs update logic for all spawns held by this spawn cache
     */
    @Override
    public void update() {
        getSpawns().forEach(Spawn::update);
    }

    /**
     * Add a spawn
     * @param spawn spawn
     * @return whether successful or not
     */
    public boolean add(Spawn spawn) {
        final boolean res = spawns.add(spawn);
        if (spawn instanceof RallyPoint rally)
            rallies.add(new SoftReference<>(rally));
        return res;
    }

    /**
     * Remove a spawn
     * @param spawn spawn
     * @return whether successful or not
     */
    public boolean remove(Spawn spawn) {
        if (spawn instanceof RallyPoint rally)
            rallies.remove(new SoftReference<>(rally));
        return spawns.remove(spawn);
    }
}
