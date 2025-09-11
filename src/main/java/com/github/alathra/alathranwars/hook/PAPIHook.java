package com.github.alathra.alathranwars.hook;

import com.github.alathra.alathranwars.AlathranWars;
import com.github.alathra.alathranwars.Reloadable;
import org.bukkit.Bukkit;

/**
 * A hook to interface with <a href="https://wiki.placeholderapi.com/">PlaceholderAPI</a>.
 */
public class PAPIHook implements Reloadable {
    private final AlathranWars plugin;
    private final static String pluginName = "PlaceholderAPI";
    private PAPIExpansion PAPIExpansion;

    /**
     * Instantiates a new PlaceholderAPI hook.
     *
     * @param plugin the plugin instance
     */
    public PAPIHook(AlathranWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad(AlathranWars plugin) {
    }

    @Override
    public void onEnable(AlathranWars plugin) {
        if (!Bukkit.getPluginManager().isPluginEnabled(pluginName))
            return;

        PAPIExpansion = new PAPIExpansion(plugin, NameColorHandler.getInstance());
        PAPIExpansion.register();
    }

    @Override
    public void onDisable(AlathranWars plugin) {
        if (!Bukkit.getPluginManager().isPluginEnabled(pluginName))
            return;

        PAPIExpansion.unregister();
        PAPIExpansion = null;
    }
}
