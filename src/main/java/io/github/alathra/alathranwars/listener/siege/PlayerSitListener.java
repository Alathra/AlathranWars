package io.github.alathra.alathranwars.listener.siege;

import io.github.alathra.alathranwars.conflict.battle.siege.Siege;
import io.github.alathra.alathranwars.utility.Utils;
import dev.geco.gsit.api.event.PreEntitySitEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerSitListener implements Listener {
    @EventHandler
    public void onSit(PreEntitySitEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;

        Siege siege = Utils.getClosestSiege(p, false);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(p, siege)) return;

        e.setCancelled(true);
    }
}
