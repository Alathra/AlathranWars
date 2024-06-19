package com.github.alathra.alathranwars.listeners.war;

import com.github.alathra.alathranwars.conflict.war.War;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.github.alathra.alathranwars.conflict.war.side.Side;
import com.github.alathra.alathranwars.hooks.NameColorHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerQuitListener implements Listener {
    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent e) {
        final @NotNull Player p = e.getPlayer();
        if (WarController.getInstance().isInAnyWars(p)) {
            for (@NotNull War war : WarController.getInstance().getWars()) {
                if (war.isInWar(p)) {
                    @Nullable Side side = war.getSideOf(p);
                    if (side != null) {
                        side.removeOnlinePlayer(p);
                    }
                }
            }
        }
        NameColorHandler.getInstance().calculatePlayerColors(p);
    }
}
