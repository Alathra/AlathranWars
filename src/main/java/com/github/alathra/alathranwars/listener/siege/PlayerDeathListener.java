package com.github.alathra.alathranwars.listener.siege;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.github.alathra.alathranwars.AlathranWars;
import com.github.alathra.alathranwars.conflict.battle.siege.Siege;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.github.alathra.alathranwars.utility.Utils;
import com.palmergames.bukkit.towny.event.deathprice.NationPaysDeathPriceEvent;
import com.palmergames.bukkit.towny.event.deathprice.PlayerPaysDeathPriceEvent;
import com.palmergames.bukkit.towny.event.deathprice.TownPaysDeathPriceEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerDeathListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPaysDeathPrice(PlayerPaysDeathPriceEvent e) {
        Player p = e.getDeadResident().getPlayer();
        if (p == null) return;

        if (AlathranWars.getInstance().isWarTime() && WarController.getInstance().isInAnyWars(p)) {
            e.setCancelled(true);
            return;
        }

        Siege siege = Utils.getClosestSiege(p, true);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(p, siege)) return;

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPaysDeathPrice(TownPaysDeathPriceEvent e) {
        Player p = e.getDeadResident().getPlayer();
        if (p == null) return;

        if (AlathranWars.getInstance().isWarTime() && WarController.getInstance().isInAnyWars(p)) {
            e.setCancelled(true);
            return;
        }

        Siege siege = Utils.getClosestSiege(p, true);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(p, siege)) return;

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPaysDeathPrice(NationPaysDeathPriceEvent e) {
        Player p = e.getDeadResident().getPlayer();
        if (p == null) return;

        if (AlathranWars.getInstance().isWarTime() && WarController.getInstance().isInAnyWars(p)) {
            e.setCancelled(true);
            return;
        }

        Siege siege = Utils.getClosestSiege(p, true);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(p, siege)) return;

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player victim = e.getPlayer();
        Player attacker = e.getPlayer().getKiller();
        if (attacker == null) return;

        if (AlathranWars.getInstance().isWarTime() && WarController.getInstance().isInAnyWars(victim)) {
            siegeKill(e);
            return;
        }

        Siege siege = Utils.getClosestSiege(victim, false);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(victim, siege)) return;

        if (siege.isInBattle(victim) && siege.isInBattle(attacker)) {
            siegeKill(e);
        } else {
            oocKill(e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSuicide(PlayerDeathEvent e) {
        Player victim = e.getPlayer();
        Player attacker = e.getPlayer().getKiller();
        if (attacker != null) return;

        if (AlathranWars.getInstance().isWarTime() && WarController.getInstance().isInAnyWars(victim)) {
            siegeKill(e);
            return;
        }

        Siege siege = Utils.getClosestSiege(victim, false);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(victim, siege)) return;

        if (siege.isInBattle(victim)) {
            siegeKill(e);
        } else {
            oocKill(e);
        }
    }

    /**
     * Damage all gear held by the player (and then send them to spawn?)
     * They don't lose items from death.
     *
     * @param e event
     */
    private void siegeKill(PlayerDeathEvent e) {
//        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + victim.getName()); // TODO Re enable?
//        Utils.damageAllGear(e.getPlayer());
        e.setKeepInventory(true);
        e.getDrops().clear();
        e.setKeepLevel(true);
        e.setDroppedExp(0);

        Player p = e.getPlayer();

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void respawnEvent(PlayerPostRespawnEvent e) { // TODO test
        Player p = e.getPlayer();

        Siege siege = Utils.getClosestSiege(p, false);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(p, siege)) return;

        if (siege.getDefenderSide().isOnSide(p) && siege.getTownSpawn() != null) {
            Location wrongLoc = e.getRespawnedLocation();

            // TODO Use spawn system
            // Is the new spawn location close enough to not use custom spawn
            if (wrongLoc.getWorld().equals(siege.getTownSpawn().getWorld()) && wrongLoc.distance(siege.getTownSpawn()) < 5)
                return;

            p.teleportAsync(siege.getTownSpawn(), PlayerTeleportEvent.TeleportCause.PLUGIN);
//            e.setRespawnLocation(siege.getTownSpawn());
        }
    }

    /**
     * Dont damage items held by the player
     * They don't lose items from death.
     *
     * @param e event
     */
    private void oocKill(PlayerDeathEvent e) {
//        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "spawn " + victim.getName()); // TODO Re enable?
        e.setKeepInventory(true);
        e.getDrops().clear();
        e.setKeepLevel(true);
        e.setDroppedExp(0);
    }
}
