package com.github.alathra.alathranwars.command.towny;

import com.github.alathra.alathranwars.Reloadable;

public class TownyCommandHandler implements Reloadable {
    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        new TownAdminCommands();
        new TownCommands();
    }

    @Override
    public void onDisable() {

    }
}
