package com.github.alathra.alathranwars.listener.war;

import com.github.alathra.alathranwars.conflict.war.WarController;
import com.github.alathra.alathranwars.hook.NameColorHandler;
import com.github.milkdrinkers.colorparser.ColorParser;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Duration;

public class PlayerJoinListener implements Listener {
    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        NameColorHandler.getInstance().calculatePlayerColors(p);

        if (WarController.getInstance().isInAnyWars(p)) {
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
