package com.github.alathra.alathranwars.listener.war;

import com.github.alathra.alathranwars.AlathranWars;
import com.github.alathra.alathranwars.conflict.war.WarController;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import com.palmergames.bukkit.towny.event.NationSpawnEvent;
import com.palmergames.bukkit.towny.event.TownSpawnEvent;
import com.palmergames.bukkit.towny.event.teleport.ResidentSpawnEvent;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class PlayerTeleportListener implements Listener {
    @EventHandler
    public void onResidentSpawn(@NotNull ResidentSpawnEvent e) {
        Player p = e.getPlayer();

        if (AlathranWars.getInstance().isWarTime() && WarController.getInstance().isInAnyWars(p)) {
            p.showTitle(
                Title.title(
                    ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War").build(),
                    ColorParser.of("<gray><i>You cannot teleport during war time!").build(),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
            e.setCancelMessage("");
        }
    }

    @EventHandler
    public void onTownSpawn(@NotNull TownSpawnEvent e) {
        Player p = e.getPlayer();

        if (!AlathranWars.getInstance().isWarTime())
            return;

        if (!WarController.getInstance().isInAnyWars(p))
            return;

        if (!isInSpawn(p)) {
            p.showTitle(
                Title.title(
                    ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War").build(),
                    ColorParser.of("<gray><i>You cannot teleport during war time!").build(),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
            e.setCancelMessage("");
            return;
        }

        Town town = e.getToTown();

        if (WarController.getInstance().isInAnySieges(town)) {
            p.showTitle(
                Title.title(
                    ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War").build(),
                    ColorParser.of("<gray><i>You cannot teleport a town that's besieged!").build(),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(5500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
            e.setCancelMessage("");
        }
    }

    @EventHandler
    public void onNationSpawn(@NotNull NationSpawnEvent e) {
        Player p = e.getPlayer();

        if (!AlathranWars.getInstance().isWarTime())
            return;

        if (!WarController.getInstance().isInAnyWars(p))
            return;

        if (!isInSpawn(p)) {
            p.showTitle(
                Title.title(
                    ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War").build(),
                    ColorParser.of("<gray><i>You cannot teleport during war time!").build(),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
            e.setCancelMessage("");
            return;
        }

        Town town = e.getToNation().getCapital();

        if (WarController.getInstance().isInAnySieges(town)) {
            p.showTitle(
                Title.title(
                    ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War").build(),
                    ColorParser.of("<gray><i>You cannot teleport a town that's besieged!").build(),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(5500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
            e.setCancelMessage("");
        }
    }

    private boolean isInSpawn(Player p) {
        return p.getWorld().getName().equalsIgnoreCase("world");
    }
}
