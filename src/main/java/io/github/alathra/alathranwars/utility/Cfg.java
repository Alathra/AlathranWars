package io.github.alathra.alathranwars.utility;

import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.config.ConfigHandler;
import io.github.milkdrinkers.crate.Config;
import org.jetbrains.annotations.NotNull;

/**
 * Convenience class for accessing {@link ConfigHandler#getConfig}
 */
public final class Cfg {
    /**
     * Convenience method for {@link ConfigHandler#getConfig} to getConnection {@link Config}
     *
     * @return the config
     */
    @NotNull
    public static Config get() {
        return AlathranWars.getInstance().getConfigHandler().getConfig();
    }
}
