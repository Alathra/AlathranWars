package com.github.alathra.alathranwars.listeners.siege;

import com.github.alathra.alathranwars.conflict.battle.siege.Siege;
import com.github.alathra.alathranwars.utility.Utils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Arrays;
import java.util.List;

public class PlayerDamageEntityListener implements Listener {
    private final List<EntityType> typeWhitelist = Arrays.asList(
        EntityType.PLAYER,
        EntityType.HORSE,
        EntityType.LLAMA,
        EntityType.TRADER_LLAMA,
        EntityType.CAMEL,
        EntityType.ZOMBIE_HORSE,
        EntityType.WOLF,
        EntityType.DONKEY,
        EntityType.PIG,
        EntityType.BOAT,
        EntityType.MINECART,
        EntityType.MINECART_TNT,
        EntityType.BEE
    );

    /**
     * Always allow damaging certain entities withing the siege zone (mounts and dogs)
     *
     * @param e the e
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        Entity attacker = e.getDamager();
        Entity target = e.getEntity();

        if (!(attacker instanceof Player p))
            return;

        Siege siege = Utils.getClosestSiege(p, true);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(p, siege)) return;

        if (typeWhitelist.contains(target.getType())) {
            e.setCancelled(false); // Force un-cancel event
        }
    }
}
