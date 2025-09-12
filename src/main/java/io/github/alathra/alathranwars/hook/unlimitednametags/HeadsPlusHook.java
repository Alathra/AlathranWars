package io.github.alathra.alathranwars.hook.unlimitednametags;

import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.hook.AbstractHook;
import io.github.alathra.alathranwars.hook.Hook;

public class HeadsPlusHook extends AbstractHook {
    public HeadsPlusHook(AlathranWars plugin) {
        super(plugin);
    }

    @Override
    public void onLoad(AlathranWars plugin) {
        if (!isPluginPresent(Hook.HeadsPlus.getPluginName()))
            return;
    }

    @Override
    public void onEnable(AlathranWars plugin) {
        if (!isPluginEnabled(Hook.HeadsPlus.getPluginName()))
            return;
    }

    @Override
    public void onDisable(AlathranWars plugin) {
        if (!isPluginEnabled(Hook.HeadsPlus.getPluginName()))
            return;
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.HeadsPlus.getPluginName());
    }
}
