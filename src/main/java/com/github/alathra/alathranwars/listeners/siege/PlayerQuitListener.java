package com.github.alathra.alathranwars.listeners.siege;

import com.github.alathra.alathranwars.conflict.battle.siege.Siege;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.github.alathra.alathranwars.enums.battle.BattleSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerQuitListener implements Listener {
    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent e) {
        final @NotNull Player p = e.getPlayer();
        if (WarController.getInstance().isPlayerInAnySiege(p)) {
            for (@NotNull Siege siege : WarController.getInstance().getPlayerSieges(p)) {
                final @NotNull BattleSide side = siege.getPlayerSideInSiege(p);
                siege.removeOnlinePlayer(p, side);
            }
        }
    }
}
