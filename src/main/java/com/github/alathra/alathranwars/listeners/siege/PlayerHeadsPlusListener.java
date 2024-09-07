package com.github.alathra.alathranwars.listeners.siege;

import com.github.alathra.alathranwars.AlathranWars;
import com.github.alathra.alathranwars.conflict.battle.siege.Siege;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.github.alathra.alathranwars.utility.Utils;
import io.github.thatsmusic99.headsplus.api.events.PlayerHeadDropEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerHeadsPlusListener implements Listener {
    @EventHandler
    public void onHeadDrop(PlayerHeadDropEvent e) {
        if (AlathranWars.getInstance().isWarTime() && WarController.getInstance().isInAnyWars(e.getDeadPlayer())) {
            e.setCancelled(true);
            return;
        }

        Siege siege = Utils.getClosestSiege(e.getDeadPlayer(), false);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(e.getDeadPlayer(), siege)) return;

        e.setCancelled(true);
    }
}
