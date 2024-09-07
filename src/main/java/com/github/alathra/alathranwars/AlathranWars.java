package com.github.alathra.alathranwars;

import com.github.alathra.alathranwars.commands.CommandManager;
import com.github.alathra.alathranwars.config.ConfigHandler;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.github.alathra.alathranwars.db.DatabaseHandler;
import com.github.alathra.alathranwars.db.DatabaseQueries;
import com.github.alathra.alathranwars.hooks.HookManager;
import com.github.alathra.alathranwars.items.WarItemRegistry;
import com.github.alathra.alathranwars.listeners.ListenerHandler;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.arim.morepaperlib.MorePaperLib;

public class AlathranWars extends JavaPlugin {
    public static @Nullable Economy econ = null;
    private static MorePaperLib paperLib;

    private static AlathranWars instance;
    private ConfigHandler configHandler;
    private DatabaseHandler databaseHandler;
    private ListenerHandler listenerHandler;

    private CommandManager commandManager;
    private HookManager hookManager;
    private boolean isWarTime = false;

    @SuppressWarnings("rawtypes")
    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        @SuppressWarnings("unchecked") final @Nullable RegisteredServiceProvider<Economy> rsp = (RegisteredServiceProvider<Economy>) this.getServer()
            .getServicesManager().getRegistration((Class) Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public void onLoad() {
        instance = this;
        paperLib = new MorePaperLib(instance);
        WarController.getInstance();
        configHandler = new ConfigHandler(instance);
        databaseHandler = new DatabaseHandler(instance);
        commandManager = new CommandManager(instance);
        listenerHandler = new ListenerHandler(instance);
        hookManager = new HookManager();

        configHandler.onLoad();
        databaseHandler.onLoad();
        commandManager.onLoad();
        listenerHandler.onLoad();
        hookManager.onLoad();
    }

    public void onEnable() {
        this.saveDefaultConfig();
        new WarItemRegistry();
        setupEconomy();

        configHandler.onEnable();
        databaseHandler.onEnable();
        commandManager.onEnable();
        listenerHandler.onEnable();
        hookManager.onEnable();
        WarController.getInstance().loadAll();
    }

    public void onDisable() {
        getPaperLib().scheduling().cancelGlobalTasks();
        DatabaseQueries.saveAll();
        commandManager.onDisable();
        listenerHandler.onDisable();
        hookManager.onDisable();
        databaseHandler.onDisable();
        configHandler.onDisable();
    }

    @NotNull
    public static AlathranWars getInstance() {
        return instance;
    }

    public static MorePaperLib getPaperLib() {
        return paperLib;
    }

    @NotNull
    public DatabaseHandler getDataHandler() {
        return databaseHandler;
    }

    @NotNull
    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public boolean isWarTime() {
        return isWarTime;
    }

    public void setWarTime(boolean warTime) {
        isWarTime = warTime;
    }
}
