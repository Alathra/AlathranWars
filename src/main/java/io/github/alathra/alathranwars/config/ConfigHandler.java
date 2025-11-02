package io.github.alathra.alathranwars.config;

import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.Reloadable;
import io.github.milkdrinkers.crate.Config;
import io.github.milkdrinkers.crate.internal.settings.ReloadSetting;

import javax.inject.Singleton;

/**
 * A class that generates/loads {@literal &} provides access to a configuration file.
 */
@Singleton
public class ConfigHandler implements Reloadable {
    private final AlathranWars plugin;
    private Config cfg;
    private Config databaseCfg;

    /**
     * Instantiates a new Config handler.
     *
     * @param plugin the plugin instance
     */
    public ConfigHandler(AlathranWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad(AlathranWars plugin) {
        cfg = Config.builderConfig()
            .path(plugin.getDataFolder().toPath().resolve("config.yml"))
            .defaults(plugin.getResource("config.yml"))
            .reload(ReloadSetting.MANUALLY)
            .build();
        databaseCfg = Config.builderConfig()
            .path(plugin.getDataFolder().toPath().resolve("database.yml"))
            .defaults(plugin.getResource("database.yml"))
            .reload(ReloadSetting.MANUALLY)
            .build();
    }

    @Override
    public void onEnable(AlathranWars plugin) {
    }

    @Override
    public void onDisable(AlathranWars plugin) {
    }

    /**
     * Gets main config object.
     *
     * @return the config object
     */
    public Config getConfig() {
        return cfg;
    }

    /**
     * Gets database config object.
     *
     * @return the config object
     */
    public Config getDatabaseConfig() {
        return databaseCfg;
    }
}
