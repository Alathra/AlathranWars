package com.github.alathra.alathranwars.conflict.battle.siege;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public abstract class SiegeUtils {
    public static double SIEGE_LASER_LENGHT = 350D;
    public static double SIEGE_LASER_HEIGHT_ABOVE_GROUND = 2.5D;

    public static Location getLaserFromLocation(Location location) {
        return location.clone().add(new Vector(0, SIEGE_LASER_HEIGHT_ABOVE_GROUND, 0));
    }

    public static Location getLaserToLocation(Location location) {
        return location.clone().add(new Vector(0, SIEGE_LASER_LENGHT, 0));
    }
}
