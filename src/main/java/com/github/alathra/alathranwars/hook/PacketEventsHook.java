package com.github.alathra.alathranwars.hook;

import com.github.alathra.alathranwars.AlathranWars;
import com.github.alathra.alathranwars.Reloadable;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.tofaa.entitylib.APIConfig;
import me.tofaa.entitylib.EntityLib;
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform;
import org.bukkit.Bukkit;

/**
 * A hook that enables API for PacketEvents and EntityLib.
 */
public class PacketEventsHook implements Reloadable {
    private final AlathranWars plugin;
    private final static String pluginName = "PacketEvents";

    /**
     * Instantiates a new PacketEvents hook.
     *
     * @param plugin the plugin instance
     */
    public PacketEventsHook(AlathranWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad(AlathranWars plugin) {
        if (!Bukkit.getPluginManager().isPluginEnabled(pluginName))
            return;

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(plugin));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable(AlathranWars plugin) {
        if (!Bukkit.getPluginManager().isPluginEnabled(pluginName))
            return;

        PacketEvents.getAPI().init();

        SpigotEntityLibPlatform entityLib = new SpigotEntityLibPlatform(plugin);
        final APIConfig entityLibConfig = new APIConfig(PacketEvents.getAPI())
            .usePlatformLogger();

        EntityLib.init(entityLib, entityLibConfig);
    }

    @Override
    public void onDisable(AlathranWars plugin) {
        if (!Bukkit.getPluginManager().isPluginEnabled(pluginName))
            return;

        PacketEvents.getAPI().terminate();
    }

    /**
     * Check if the PacketEvents hook is loaded and ready for use.
     * @return whether the PacketEvents hook is loaded or not
     */
    public boolean isHookLoaded() {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName) && PacketEvents.getAPI().isLoaded();
    }
}
