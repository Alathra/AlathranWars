package com.github.alathra.alathranwars.listeners.war;

import com.github.alathra.alathranwars.conflict.war.War;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.github.alathra.alathranwars.conflict.war.side.Side;
import com.github.alathra.alathranwars.hooks.NameColorHandler;
import com.github.milkdrinkers.colorparser.ColorParser;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        final @NotNull Player p = e.getPlayer();
        if (WarController.getInstance().isPlayerInAnyWars(p)) {
            for (@NotNull War war : WarController.getInstance().getWars()) {
                if (war.isPlayerInWar(p)) {
                    @Nullable Side side = war.getPlayerSide(p);
                    if (side != null) {
                        side.addOnlinePlayer(p);
                    }
                }
            }
        }
        NameColorHandler.getInstance().calculatePlayerColors(p);

        if (WarController.getInstance().isPlayerInAnyWars(p)) {
            p.showTitle(
                Title.title(
                    ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War").build(),
                    ColorParser.of("<gray><i>You are in at least one war!").build(),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                )
            );
        }
    }
}
