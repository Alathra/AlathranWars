package io.github.alathra.alathranwars.listener.siege;

import io.github.alathra.alathranwars.conflict.battle.siege.Siege;
import io.github.alathra.alathranwars.utility.Utils;
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
        EntityType.BIRCH_BOAT,
        EntityType.BIRCH_CHEST_BOAT,
        EntityType.OAK_BOAT,
        EntityType.OAK_CHEST_BOAT,
        EntityType.SPRUCE_BOAT,
        EntityType.SPRUCE_CHEST_BOAT,
        EntityType.JUNGLE_BOAT,
        EntityType.JUNGLE_CHEST_BOAT,
        EntityType.ACACIA_BOAT,
        EntityType.ACACIA_CHEST_BOAT,
        EntityType.DARK_OAK_BOAT,
        EntityType.DARK_OAK_CHEST_BOAT,
        EntityType.CHERRY_BOAT,
        EntityType.CHERRY_CHEST_BOAT,
        EntityType.MANGROVE_BOAT,
        EntityType.MANGROVE_CHEST_BOAT,
        EntityType.PALE_OAK_BOAT,
        EntityType.PALE_OAK_CHEST_BOAT,
        EntityType.MINECART,
        EntityType.TNT_MINECART,
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
