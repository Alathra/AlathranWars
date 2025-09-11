package com.github.alathra.alathranwars;

/**
 * Implemented in classes that should support being reloaded IE executing the methods during runtime after startup.
 */
public interface Reloadable {
    /**
     * On plugin load.
     */
    void onLoad(AlathranWars plugin);

    /**
     * On plugin enable.
     */
    void onEnable(AlathranWars plugin);

    /**
     * On plugin disable.
     */
    void onDisable(AlathranWars plugin);
}
