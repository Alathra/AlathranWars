package com.github.alathra.alathranwars.listeners.siege;

import com.github.alathra.alathranwars.AlathranWars;
import com.github.alathra.alathranwars.conflict.battle.siege.Siege;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.github.alathra.alathranwars.utility.Utils;
import com.ranull.graves.event.GraveCreateEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PlayerGraveListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGraveCreate(GraveCreateEvent e) {
        if (!e.getEntityType().equals(EntityType.PLAYER)) return;
        if (!(e.getEntity() instanceof Player p)) return;

        if (AlathranWars.getInstance().isWarTime() && WarController.getInstance().isInAnyWars(p)) {
            e.setCancelled(true);
            return;
        }

        Siege siege = Utils.getClosestSiege(p, false);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(p, siege)) return;

        e.setCancelled(true);
    }
}

