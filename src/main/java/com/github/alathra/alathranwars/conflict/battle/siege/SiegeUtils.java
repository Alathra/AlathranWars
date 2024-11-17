package com.github.alathra.alathranwars.conflict.battle.siege;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public abstract class SiegeUtils {
    public static double SIEGE_LASER_HEIGHT = 350D;

    public static Location getLaserStartLocation(Location location) {
        return location;
    }

    public static Location getLaserToLocation(Location location) {
        return location.clone().add(new Vector(0, SIEGE_LASER_HEIGHT, 0));
    }
}
