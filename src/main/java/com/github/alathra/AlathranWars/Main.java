package com.github.alathra.AlathranWars;

import com.github.alathra.AlathranWars.commands.CommandManager;
import com.github.alathra.AlathranWars.data.ConfigManager;
import com.github.alathra.AlathranWars.data.DataManager;
import com.github.alathra.AlathranWars.conflict.WarManager;
import com.github.alathra.AlathranWars.hooks.HookManager;
import com.github.alathra.AlathranWars.items.WarItemRegistry;
import com.github.alathra.AlathranWars.listeners.ListenerHandler;
import com.github.alathra.AlathranWars.utility.SQLQueries;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.arim.morepaperlib.MorePaperLib;

public class Main extends JavaPlugin {
    public static @Nullable Economy econ = null;
    private static Main instance = null;
    private static MorePaperLib paperLib;

    private ConfigManager configManager;
    private DataManager dataManager;
    private CommandManager commandManager;
    private ListenerHandler listenerHandler;
    private HookManager hookManager;

    @NotNull
    public static Main getInstance() {
        return instance;
    }
    public static MorePaperLib getPaperLib() {
        return paperLib;
    }

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

    @Override
    public void onLoad() {
        instance = this;
        paperLib = new MorePaperLib(getInstance());
        WarManager.getInstance();
        configManager = new ConfigManager(getInstance());
        dataManager = new DataManager(getInstance());
        commandManager = new CommandManager(getInstance());
        listenerHandler = new ListenerHandler(getInstance());
        hookManager = new HookManager();

        configManager.onLoad();
        dataManager.onLoad();
        commandManager.onLoad();
        listenerHandler.onLoad();
        hookManager.onLoad();
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        new WarItemRegistry();
        setupEconomy();

        configManager.onEnable();
        dataManager.onEnable();
        commandManager.onEnable();
        listenerHandler.onEnable();
        hookManager.onEnable();
        WarManager.getInstance().loadAll();
    }

    @Override
    public void onDisable() {
        getPaperLib().scheduling().cancelGlobalTasks();
        SQLQueries.saveAll();
        configManager.onDisable();
        commandManager.onDisable();
        listenerHandler.onDisable();
        hookManager.onDisable();
        dataManager.onDisable();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
