package io.github.alathra.alathranwars.deathspectate.listener;

import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.deathspectate.DeathConfig;
import io.github.alathra.alathranwars.deathspectate.DeathUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.*;

import static io.github.alathra.alathranwars.deathspectate.DeathUtil.META_TELEPORTING;

public class AbuseListener  implements Listener {
    private final AlathranWars plugin;

    public AbuseListener() {
        this.plugin = AlathranWars.getInstance();
    }

    /**
     * Prevent other plugins from teleporting a dead player
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("unused")
    void onPlayerTryToTeleportWhenDead(PlayerTeleportEvent e) {
        final Player p = e.getPlayer();

        if (!DeathUtil.isSpectating(p))
            return;

        // Only allow us to teleport players while dead
        if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.PLUGIN) && p.hasMetadata(META_TELEPORTING)) {
            p.removeMetadata(META_TELEPORTING, plugin);
            return;
        }

        e.setCancelled(true);
    }

    /**
     * Prevent player sneaking while spectating (Fixes issues with ability plugins like MCMMO)
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("unused")
    void onPlayerTryToSneakWhenDead(PlayerToggleSneakEvent e) {
        if (DeathUtil.isSpectating(e.getPlayer()) && e.isSneaking())
            e.setCancelled(true);
    }

    /**
     * Prevent players from running commands while dead
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    @SuppressWarnings("unused")
    void onPlayerTryToRunCommandWhenDead(PlayerCommandPreprocessEvent e) {
        if (DeathUtil.isSpectating(e.getPlayer()) && !DeathConfig.isAllowedToUseAnyCommand(e.getPlayer())) {
            String cmd = e.getMessage().split(" ")[0]; // Got a more efficient/better way? Let me know/PR it!
            cmd = cmd.substring(1); // Remove slash
            if (!DeathConfig.isWhitelistedCommand(cmd)) {
                e.setCancelled(true);
                if (!DeathConfig.getCommandDeniedMessage().isEmpty())
                    e.getPlayer().sendMessage(DeathConfig.getCommandDeniedMessage());
            }
        }
    }


    /**
     * Dead players do not persist, instantly respawn a dead player that leaves
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOW)
    @SuppressWarnings("unused")
    void onPlayerQuitWhileSpectatingOrDead(PlayerQuitEvent e) {
        DeathUtil.respawnPlayer(e.getPlayer());
    }

    /**
     * Prevent spectators from taking damage (from the void for example)
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("unused")
    void onPlayerTakeDamageWhileSpectating(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p))
            return;

        if (DeathUtil.isSpectating(p))
            e.setCancelled(true);
    }

    /**
     * Prevent healing while spectating
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("unused")
    void onPlayerRegainingHealthWhileSpectating(EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof Player p))
            return;

        if (DeathUtil.isSpectating(p))
            e.setCancelled(true);
    }

    /**
     * Prevent triggering animations while dead
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("unused")
    private void onPlayerSwingHand(PlayerAnimationEvent e) {
        if (DeathUtil.isSpectating(e.getPlayer()))
            e.setCancelled(true);
    }

    /**
     * Prevent player interactions while dead
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("unused")
    private void onPlayerTryToInteractWhenDead(PlayerInteractEvent e) {
        if (DeathUtil.isSpectating(e.getPlayer()))
            e.setCancelled(true);
    }

    /**
     * Prevent picking up items while dead
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("unused")
    private void onPlayerPickupWhenDead(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player p))
            return;

        if (DeathUtil.isSpectating(p))
            e.setCancelled(true);
    }
}
