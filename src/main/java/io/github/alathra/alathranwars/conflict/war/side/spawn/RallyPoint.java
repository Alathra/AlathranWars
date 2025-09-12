package io.github.alathra.alathranwars.conflict.war.side.spawn;

import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.database.QueryUtils;
import io.github.alathra.alathranwars.database.Storable;
import io.github.alathra.alathranwars.database.schema.tables.records.SidesSpawnsRecord;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;

import java.time.Duration;
import java.time.Instant;

public class RallyPoint extends Spawn implements Storable<SidesSpawnsRecord> {
    public static final Duration PLAYER_PROXY_REENABLE = Duration.ofSeconds(60);
    private final Block rallyBanner;
    private final OfflinePlayer creator;

    public RallyPoint(String name, Location location, SpawnType type, Block rallyBanner, OfflinePlayer creator) {
        super(name, location, type);
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

    @Override
    public SidesSpawnsRecord deserialize() {
        return new SidesSpawnsRecord(
            QueryUtils.UUIDUtil.toBytes(getSide().getUUID()),
            getName(),
            QueryUtils.InstantUtil.toDateTime(getLastProxied()),
            QueryUtils.InstantUtil.toDateTime(getStartProxied()),
            QueryUtils.BooleanUtil.toByte(isProxied()),
            QueryUtils.UUIDUtil.toBytes(getCreator().getUniqueId()),
            QueryUtils.UUIDUtil.toBytes(getLocation().getWorld().getUID()),
            getLocation().getBlockX(),
            getLocation().getBlockY(),
            getLocation().getBlockZ(),
            (double) getLocation().getYaw(),
            (double) getLocation().getPitch(),
            QueryUtils.UUIDUtil.toBytes(getRallyBanner().getWorld().getUID()),
            getRallyBanner().getLocation().getBlockX(),
            getRallyBanner().getLocation().getBlockY(),
            getRallyBanner().getLocation().getBlockZ()
        );
    }
}
