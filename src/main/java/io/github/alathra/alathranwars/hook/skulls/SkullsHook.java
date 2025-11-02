package io.github.alathra.alathranwars.hook.skulls;

import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.hook.AbstractHook;
import io.github.alathra.alathranwars.hook.Hook;

public class SkullsHook extends AbstractHook {
    public SkullsHook(AlathranWars plugin) {
        super(plugin);
    }

    @Override
    public void onLoad(AlathranWars plugin) {
        if (!isPluginPresent(Hook.Skulls.getPluginName())) {
        }
    }

    @Override
    public void onEnable(AlathranWars plugin) {
        if (!isPluginEnabled(Hook.Skulls.getPluginName())) {
        }
    }

    @Override
    public void onDisable(AlathranWars plugin) {
        if (!isPluginEnabled(Hook.Skulls.getPluginName())) {
        }
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.Skulls.getPluginName());
    }
}
