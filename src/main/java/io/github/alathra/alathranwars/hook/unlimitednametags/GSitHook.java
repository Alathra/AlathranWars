package io.github.alathra.alathranwars.hook.unlimitednametags;

import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.hook.AbstractHook;
import io.github.alathra.alathranwars.hook.Hook;

public class GSitHook extends AbstractHook {
    public GSitHook(AlathranWars plugin) {
        super(plugin);
    }

    @Override
    public void onLoad(AlathranWars plugin) {
        if (!isPluginPresent(Hook.GSit.getPluginName()))
            return;
    }

    @Override
    public void onEnable(AlathranWars plugin) {
        if (!isPluginEnabled(Hook.GSit.getPluginName()))
            return;
    }

    @Override
    public void onDisable(AlathranWars plugin) {
        if (!isPluginEnabled(Hook.GSit.getPluginName()))
            return;
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.GSit.getPluginName());
    }
}
