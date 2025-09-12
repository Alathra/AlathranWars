package io.github.alathra.alathranwars.deathspectate.listener;

import io.github.alathra.alathranwars.AlathranWars;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import static io.github.alathra.alathranwars.deathspectate.DeathUtil.META_AWAITING_RESPAWN;
import static io.github.alathra.alathranwars.deathspectate.DeathUtil.META_DEAD;

public class RespawnListener implements Listener {
    private final AlathranWars plugin;

    public RespawnListener() {
        this.plugin = AlathranWars.getInstance();
    }

    /**
     * Handle cleaning up metadata for API
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onPlayerRespawns(PlayerRespawnEvent e) {
        final Player player = e.getPlayer();

        // Check if already death spectating or being respawned
        if (!player.hasMetadata(META_DEAD) || player.hasMetadata(META_AWAITING_RESPAWN))
            return;

        // Cleanup metadata
        player.removeMetadata(META_DEAD, plugin);
    }
}
