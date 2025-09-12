package io.github.alathra.alathranwars.listener;

import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.Reloadable;
import io.github.alathra.alathranwars.listener.siege.*;
import io.github.alathra.alathranwars.listener.war.*;
import io.github.alathra.alathranwars.listener.war.PlayerJoinListener;
import io.github.alathra.alathranwars.listener.war.PlayerQuitListener;
import io.github.alathra.alathranwars.listener.war.TownListener;
import org.bukkit.Bukkit;

/**
 * A class to handle registration of event listeners.
 */
public class ListenerHandler implements Reloadable {
    private final AlathranWars plugin;

    /**
     * Instantiates a the Listener handler.
     *
     * @param plugin the plugin instance
     */
    public ListenerHandler(AlathranWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad(AlathranWars plugin) {
    }

    @Override
    public void onEnable(AlathranWars plugin) {
        // Sieges
        plugin.getServer().getPluginManager().registerEvents(new BlockBreakPlaceListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerDeathListener(), plugin);
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("Graves"))
            plugin.getServer().getPluginManager().registerEvents(new PlayerGraveListener(), plugin);
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("HeadsPlus"))
            plugin.getServer().getPluginManager().registerEvents(new PlayerHeadsPlusListener(), plugin);
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("GSit"))
            plugin.getServer().getPluginManager().registerEvents(new PlayerSitListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ItemUseListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerDamageEntityListener(), plugin);

        // Battles
        plugin.getServer().getPluginManager().registerEvents(new SiegeListener(), plugin);

        // Wars
        plugin.getServer().getPluginManager().registerEvents(new StatusScreenListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new NationListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new TownListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerTeleportListener(), plugin);
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("Essentials"))
            plugin.getServer().getPluginManager().registerEvents(new EssentialsListener(), plugin);
    }

    @Override
    public void onDisable(AlathranWars plugin) {
    }
}