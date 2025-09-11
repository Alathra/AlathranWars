package com.github.alathra.alathranwars.command;

import com.github.alathra.alathranwars.AlathranWars;
import com.github.alathra.alathranwars.command.towny.TownyCommandHandler;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;

public class CommandHandler {
    private final AlathranWars plugin;
    private final TownyCommandHandler townyHandler;

    /**
     * Instantiates the Command handler.
     *
     * @param plugin the plugin
     */
    public CommandHandler(AlathranWars plugin) {
        this.plugin = plugin;
        this.townyHandler = new TownyCommandHandler();
    }

    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(plugin).shouldHookPaperReload(true).silentLogs(true));
        townyHandler.onLoad(plugin);
    }

    public void onEnable() {
        CommandAPI.onEnable();

        // Register commands
        new WarCommands();
        new SiegeCommands();
        new AdminCommands();
        townyHandler.onEnable(plugin);
    }

    public void onDisable() {
        CommandAPI.onDisable();
        townyHandler.onDisable(plugin);
    }
}
