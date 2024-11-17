package com.github.alathra.alathranwars;

import com.github.alathra.alathranwars.command.CommandHandler;
import com.github.alathra.alathranwars.config.ConfigHandler;
import com.github.alathra.alathranwars.conflict.war.SpawnController;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.github.alathra.alathranwars.database.DatabaseQueries;
import com.github.alathra.alathranwars.database.handler.DatabaseHandler;
import com.github.alathra.alathranwars.hook.*;
import com.github.alathra.alathranwars.listener.ListenerHandler;
import com.github.alathra.alathranwars.translation.TranslationManager;
import com.github.alathra.alathranwars.updatechecker.UpdateChecker;
import com.github.alathra.alathranwars.utility.Logger;
import com.github.milkdrinkers.colorparser.ColorParser;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import space.arim.morepaperlib.MorePaperLib;

public class AlathranWars extends JavaPlugin {
    private static MorePaperLib paperLib;

    private static AlathranWars instance;
    private ConfigHandler configHandler;
    private TranslationManager translationManager;
    private DatabaseHandler databaseHandler;
    private CommandHandler commandHandler;
    private ListenerHandler listenerHandler;
    private UpdateChecker updateChecker;
    public static PlainTextComponentSerializer plainTextComponentSerializer = PlainTextComponentSerializer.plainText();

    // Hooks
    private static BStatsHook bStatsHook;
    private static VaultHook vaultHook;
    private static PacketEventsHook packetEventsHook;
    private static PAPIHook papiHook;

    // Data
    private boolean isWarTime = false;

    public void onLoad() {
        instance = this;
        paperLib = new MorePaperLib(instance);
        WarController.getInstance();
        configHandler = new ConfigHandler(instance);
        translationManager = new TranslationManager(instance);
        databaseHandler = new DatabaseHandler(configHandler, getComponentLogger());
        commandHandler = new CommandHandler(instance);
        listenerHandler = new ListenerHandler(instance);
        updateChecker = new UpdateChecker();
        bStatsHook = new BStatsHook(instance);
        vaultHook = new VaultHook(instance);
        packetEventsHook = new PacketEventsHook(instance);
        papiHook = new PAPIHook(instance);

        configHandler.onLoad();
        translationManager.onLoad();
        databaseHandler.onLoad();
        commandHandler.onLoad();
        listenerHandler.onLoad();
        updateChecker.onLoad();
        bStatsHook.onLoad();
        vaultHook.onLoad();
        packetEventsHook.onLoad();
        papiHook.onLoad();
        SpawnController.getInstance().onLoad();
    }

    public void onEnable() {
        configHandler.onEnable();
        translationManager.onEnable();
        databaseHandler.onEnable();
        commandHandler.onEnable();
        listenerHandler.onEnable();
        updateChecker.onEnable();
        bStatsHook.onEnable();
        vaultHook.onEnable();
        packetEventsHook.onEnable();
        papiHook.onEnable();

        if (!databaseHandler.isRunning()) {
            Logger.get().warn(ColorParser.of("<yellow>Database handler failed to start. Database support has been disabled.").build());
        }

        if (vaultHook.isVaultLoaded()) {
            Logger.get().info(ColorParser.of("<green>Vault has been found on this server. Vault support enabled.").build());
        } else {
            Logger.get().warn(ColorParser.of("<yellow>Vault is not installed on this server. Vault support has been disabled.").build());
        }

        if (packetEventsHook.isHookLoaded()) {
            Logger.get().info(ColorParser.of("<green>PacketEvents has been found on this server. PacketEvents support enabled.").build());
        } else {
            Logger.get().warn(ColorParser.of("<yellow>PacketEvents is not installed on this server. PacketEvents support has been disabled.").build());
        }

        WarController.getInstance().loadAll();
        SpawnController.getInstance().onEnable();
    }

    public void onDisable() {
        getPaperLib().scheduling().cancelGlobalTasks();
        DatabaseQueries.saveAll();

        SpawnController.getInstance().onDisable();
        configHandler.onDisable();
        translationManager.onDisable();
        databaseHandler.onDisable();
        commandHandler.onDisable();
        listenerHandler.onDisable();
        updateChecker.onDisable();
        bStatsHook.onDisable();
        vaultHook.onDisable();
        packetEventsHook.onDisable();
        papiHook.onDisable();
    }

    /**
     * Gets plugin instance.
     *
     * @return the plugin instance
     */
    @NotNull
    public static AlathranWars getInstance() {
        return instance;
    }

    /**
     * Gets more paper lib instance.
     *
     * @return the more paper lib instance
     */
    @NotNull
    public static MorePaperLib getPaperLib() {
        return paperLib;
    }

    /**
     * Gets data handler.
     *
     * @return the data handler
     */
    @NotNull
    public DatabaseHandler getDataHandler() {
        return databaseHandler;
    }

    /**
     * Gets config handler.
     *
     * @return the config handler
     */
    @NotNull
    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    /**
     * Gets config handler.
     *
     * @return the translation handler
     */
    @NotNull
    public TranslationManager getTranslationManager() {
        return translationManager;
    }

    /**
     * Gets update checker.
     *
     * @return the update checker
     */
    @NotNull
    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    /**
     * Gets bStats hook.
     *
     * @return the bStats hook
     */
    @NotNull
    public static BStatsHook getBStatsHook() {
        return bStatsHook;
    }

    /**
     * Gets vault hook.
     *
     * @return the vault hook
     */
    @NotNull
    public static VaultHook getVaultHook() {
        return vaultHook;
    }

    /**
     * Gets PacketEvents hook.
     *
     * @return the PacketEvents hook
     */
    @NotNull
    public static PacketEventsHook getPacketEventsHook() {
        return packetEventsHook;
    }

    public boolean isWarTime() {
        return isWarTime;
    }

    public void setWarTime(boolean warTime) {
        isWarTime = warTime;
    }
}
