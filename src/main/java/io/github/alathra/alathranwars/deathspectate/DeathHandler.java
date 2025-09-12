package io.github.alathra.alathranwars.deathspectate;

import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.Reloadable;
import io.github.alathra.alathranwars.deathspectate.listener.AbuseListener;
import io.github.alathra.alathranwars.deathspectate.listener.DeathListener;
import io.github.alathra.alathranwars.deathspectate.listener.RespawnListener;
import org.bukkit.Bukkit;

public class DeathHandler implements Reloadable {
    /**
     * On plugin load.
     */
    @Override
    public void onLoad(AlathranWars plugin) {
    }

    /**
     * On plugin enable.
     */
    @Override
    public void onEnable(AlathranWars plugin) {
        Bukkit.getPluginManager().registerEvents(new AbuseListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new DeathListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new RespawnListener(), plugin);
    }

    /**
     * On plugin disable.
     */
    @Override
    public void onDisable(AlathranWars plugin) {

    }
}
