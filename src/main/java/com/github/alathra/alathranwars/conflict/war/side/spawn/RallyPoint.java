package com.github.alathra.alathranwars.conflict.war.side.spawn;

import com.github.alathra.alathranwars.conflict.war.side.Side;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;

import java.time.Duration;
import java.time.Instant;

public class RallyPoint extends Spawn {
    public static final Duration PLAYER_PROXY_REENABLE = Duration.ofSeconds(60);
    private final Block rallyBanner;
    private final OfflinePlayer creator;

    public RallyPoint(String name, Location location, SpawnType type, Side side, Block rallyBanner, OfflinePlayer creator) {
        super(name, location, type, side);
        this.rallyBanner = rallyBanner;
        this.creator = creator;
    }

    public Block getRallyBanner() {
        return rallyBanner;
    }

    public OfflinePlayer getCreator() {
        return creator;
    }

    @Override
    public void update() {
        super.update();
        if (getStartProxied().plus(PLAYER_PROXY_REENABLE).isBefore(Instant.now()) && isProxied()) {
            delete();
        }
    }

    /**
     * Deletes this rally point
     */
    public void delete() {
        final Chunk chunk = getRallyBanner().getChunk();
        final boolean wasLoaded = chunk.isLoaded();
        if (!wasLoaded) {
            chunk.load();
        }

        getRallyBanner().setType(Material.AIR);

        if (!wasLoaded) {
            chunk.unload();
        }
    }
}
