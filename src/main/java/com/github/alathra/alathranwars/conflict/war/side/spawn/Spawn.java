package com.github.alathra.alathranwars.conflict.war.side.spawn;

import com.github.alathra.alathranwars.conflict.IUpdateable;
import com.github.alathra.alathranwars.conflict.war.side.Side;
import com.github.alathra.alathranwars.utility.SideUtils;
import org.bukkit.Location;
import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;
import java.time.Instant;

/**
 * Represents a spawn location.
 */
public class Spawn implements IUpdateable {
    public static final int PLAYER_PROXY_COUNT = 2; // Required players to proxy
    public static final int PLAYER_PROXY_RANGE = 8; // Range for proxying
    public static final Duration PLAYER_PROXY_REENABLE = Duration.ofSeconds(10);

    private final String name;
    private final Location location;
    private final SpawnType type;
    private final Side side;
    private boolean isProxied = false;
    private Instant lastProxied = Instant.now();
    private Instant startProxied = Instant.now();

    @ApiStatus.Internal
    Spawn(String name, Location location, SpawnType type, Side side) {
        this.name = name;
        this.location = location;
        this.type = type;
        this.side = side;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public SpawnType getType() {
        return type;
    }

    public Side getSide() {
        return side;
    }

    /**
     * Check if this spawn is proxied, also checks if some time has passed since it was last proxied
     *
     * @return boolean
     */
    public boolean isProxied() {
        return !isProxied && getLastProxied().plus(PLAYER_PROXY_REENABLE).isBefore(Instant.now());
    }

    /**
     * Set the spawn as proxied (disabling it)
     *
     * @param proxied boolean
     */
    public void setProxied(boolean proxied) {
        if (!isProxied && proxied)
            startProxied = Instant.now();
        isProxied = proxied;
        if (proxied)
            lastProxied = Instant.now();
    }

    /**
     * Returns the last time this spawn was proxied
     *
     * @return instant
     */
    public Instant getLastProxied() {
        return lastProxied;
    }

    /**
     * Returns the time this spawn has been proxied since
     *
     * @return instant
     */
    public Instant getStartProxied() {
        return startProxied;
    }

    /**
     * Runs updates on the spawn calculating whether it should be proxied or not
     */
    @Override
    public void update() {
        setProxied(shouldProxy());
    }

    /**
     * Returns whether this spawn should be proxied or not
     *
     * @return boolean
     */
    private boolean shouldProxy() {
        try {
            final Side oppSide = SideUtils.getOpponent(getSide());

            final int oppsInRange = SideUtils.getPlayersFromSideAtCoord(getLocation(), PLAYER_PROXY_RANGE, oppSide).size();

            return oppsInRange >= PLAYER_PROXY_COUNT;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Spawn spawn)) return false;
        return equals(spawn);
    }

    public boolean equals(Spawn spawn) {
        return spawn.getLocation().getWorld().equals(getLocation().getWorld()) && spawn.getLocation().equals(getLocation()) && spawn.getSide().equals(getSide()) && spawn.getType().equals(getType()) && spawn.getName().equals(getName());
    }
}
