package com.github.alathra.alathranwars.conflict.war.side.spawn;

import com.github.alathra.alathranwars.conflict.war.side.Side;
import org.bukkit.Location;

public class SpawnBuilder {
    private String name = "";
    private Location location;
    private SpawnType type = SpawnType.TOWN;
    private Side side;

    public SpawnBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public SpawnBuilder setLocation(Location location) {
        this.location = location;
        return this;
    }

    public SpawnBuilder setType(SpawnType type) {
        this.type = type;
        return this;
    }

    public SpawnBuilder setSide(Side side) {
        this.side = side;
        return this;
    }

    /**
     * Build a new Spawn
     * @return a new spawn
     * @throws SpawnCreationException exception
     */
    public Spawn build() throws SpawnCreationException {
        if (name == null)
            throw new SpawnCreationException("Missing state name required to create Spawn!");

        if (location == null)
            throw new SpawnCreationException("Missing state location required to create Spawn!");

        if (type == null)
            throw new SpawnCreationException("Missing state type required to create Spawn!");

        if (side == null)
            throw new SpawnCreationException("Missing state side required to create Spawn!");

        return new Spawn(name, location, type, side);
    }
}