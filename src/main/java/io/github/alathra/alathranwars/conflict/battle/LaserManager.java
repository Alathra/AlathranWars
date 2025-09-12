package io.github.alathra.alathranwars.conflict.battle;

import io.github.alathra.alathranwars.packet.CustomLaser;
import org.jetbrains.annotations.Nullable;

/**
 * Manages an end crystal laser for battles.
 */
public class LaserManager implements BattleManager {
    @Nullable
    private CustomLaser laser;

    public LaserManager() {
    }

    public LaserManager(@Nullable CustomLaser laser) {
        this.laser = laser;
    }

    @Override
    public void start() {
        if (laser == null)
            return;

        laser.spawn();
    }

    @Override
    public void stop() {
        if (laser == null)
            return;

        laser.despawn();
    }

    @Nullable
    public CustomLaser getLaser() {
        return laser;
    }

    public void setLaser(CustomLaser laser) {
        if (laser != null)
            laser.despawn();

        this.laser = laser;
    }
}
