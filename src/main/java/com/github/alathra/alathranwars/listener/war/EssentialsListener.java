package com.github.alathra.alathranwars.listener.war;

import com.github.alathra.alathranwars.AlathranWars;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.github.milkdrinkers.colorparser.ColorParser;
import net.ess3.api.events.TPARequestEvent;
import net.ess3.api.events.UserRandomTeleportEvent;
import net.ess3.api.events.UserTeleportHomeEvent;
import net.ess3.api.events.UserWarpEvent;
import net.essentialsx.api.v2.events.UserTeleportSpawnEvent;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class EssentialsListener implements Listener {
    @EventHandler
    public void onTPACommand(@NotNull TPARequestEvent e) {
        if (!AlathranWars.getInstance().isWarTime())
            return;

        if (!e.getRequester().isPlayer())
            return;

        final boolean isTeleportHere = e.isTeleportHere();

        final Player origin = !isTeleportHere ? e.getRequester().getPlayer() : e.getTarget().getBase(); // The teleported
        final Player target = !isTeleportHere ? e.getTarget().getBase() : e.getRequester().getPlayer(); // The destination

        if (WarController.getInstance().isInAnyWars(origin)) {
            origin.showTitle(
                Title.title(
                    ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War").build(),
                    ColorParser.of("<gray><i>You cannot teleport during war time!").build(),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
        }
        if (WarController.getInstance().isInAnyWars(target)) {
            target.showTitle(
                Title.title(
                    ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War").build(),
                    ColorParser.of("<gray><i>You cannot teleport during war time!").build(),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHomeCommand(@NotNull UserTeleportHomeEvent e) {
        Player p = e.getUser().getBase();

        if (AlathranWars.getInstance().isWarTime() && WarController.getInstance().isInAnyWars(p)) {
            p.showTitle(
                Title.title(
                    ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War").build(),
                    ColorParser.of("<gray><i>You cannot teleport during war time!").build(),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onWarpCommand(@NotNull UserWarpEvent e) {
        Player p = e.getUser().getBase();

        if (AlathranWars.getInstance().isWarTime() && WarController.getInstance().isInAnyWars(p)) {
            p.showTitle(
                Title.title(
                    ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War").build(),
                    ColorParser.of("<gray><i>You cannot teleport during war time!").build(),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpawnCommand(@NotNull UserTeleportSpawnEvent e) {
        Player p = e.getUser().getBase();

        if (AlathranWars.getInstance().isWarTime() && WarController.getInstance().isInAnyWars(p)) {
            p.showTitle(
                Title.title(
                    ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War").build(),
                    ColorParser.of("<gray><i>You cannot teleport during war time!").build(),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onRTPCommand(@NotNull UserRandomTeleportEvent e) {
        Player p = e.getUser().getBase();

        if (AlathranWars.getInstance().isWarTime() && WarController.getInstance().isInAnyWars(p)) {
            p.showTitle(
                Title.title(
                    ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War").build(),
                    ColorParser.of("<gray><i>You cannot teleport during war time!").build(),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
                )
            );
            e.setCancelled(true);
        }
    }
}
