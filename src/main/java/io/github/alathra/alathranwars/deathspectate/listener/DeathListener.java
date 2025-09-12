package io.github.alathra.alathranwars.deathspectate.listener;

import io.github.alathra.alathranwars.deathspectate.DeathConfig;
import io.github.alathra.alathranwars.deathspectate.DeathUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {
    public DeathListener() {
    }

    /**
     * Start death spectating on player death
     * @param e event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    @SuppressWarnings("unused")
    private void onPlayerDies(PlayerDeathEvent e) { // TODO Listen for battle death
        final Player p = e.getEntity();

        if (DeathUtil.isSpectating(p)) {
            e.setCancelled(true);
            return;
        }

        if (p.getLastDamageCause() == null)
            return;

        if (!DeathConfig.canSpectate(p, p.getLastDamageCause().getCause()))
            return;

        // Check for citizens npc's
        if (e.getEntity().hasMetadata("NPC"))
            return;

        if (DeathUtil.startDeathSpectating(p, e))
            e.setCancelled(true);
    }
}
