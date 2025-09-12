package io.github.alathra.alathranwars.command.towny;

import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.Reloadable;

public class TownyCommandHandler implements Reloadable {
    @Override
    public void onLoad(AlathranWars plugin) {

    }

    @Override
    public void onEnable(AlathranWars plugin) {
        new TownAdminCommands();
        new TownCommands();
    }

    @Override
    public void onDisable(AlathranWars plugin) {

    }
}
