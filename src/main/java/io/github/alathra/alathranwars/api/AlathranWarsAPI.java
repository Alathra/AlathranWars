package io.github.alathra.alathranwars.api;

import org.jetbrains.annotations.ApiStatus;

/**
 * The API class for AlathranWars.
 * This class provides access to the AlathranWars API.
 *
 * @see #getInstance()
 * @since 4.0.0
 */
public class AlathranWarsAPI implements WarAPI, MercenaryAPI, WarCooldownAPI, EnlistAPI, DeathAPI, ColorAPI {
    private static AlathranWarsAPI INSTANCE = null;

    /**
     * Gets the instance of the AlathranWarsAPI.
     *
     * @return the instance of AlathranWarsAPI
     * @since 4.0.0
     */
    public static AlathranWarsAPI getInstance() {
        if (!isLoaded())
            throw new RuntimeException("AlathranWars API was accessed before being initialized!");
        return INSTANCE;
    }

    /**
     * Sets the instance of the AlathranWarsAPI.
     * This method is intended for internal use by the api provider only.
     *
     * @param api the instance of AlathranWarsAPI to set
     * @since 4.0.0
     */
    @ApiStatus.Internal
    protected static void setInstance(AlathranWarsAPI api) {
        INSTANCE = api;
    }

    /**
     * Checks if the AlathranWarsAPI is available
     *
     * @return true if API is loaded and available
     * @since 4.0.0
     */
    public static boolean isLoaded() {
        return INSTANCE != null;
    }
}
