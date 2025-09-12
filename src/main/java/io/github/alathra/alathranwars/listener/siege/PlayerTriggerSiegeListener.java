package io.github.alathra.alathranwars.listener.siege;

import io.github.alathra.alathranwars.event.spawn.RallyCreateEvent;
import io.github.alathra.alathranwars.event.spawn.RallyPlaceEvent;
import io.github.alathra.alathranwars.utility.BattleUtils;
import io.github.alathra.alathranwars.utility.Cfg;
import io.github.milkdrinkers.itemutil.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@SuppressWarnings("unused")
public class PlayerTriggerSiegeListener implements Listener {
    private List<String> allowedItems = null; // Lazy-loaded list of allowed items

    private List<String> getAllowedItems() {
        if (allowedItems == null)
            allowedItems = Cfg.get().getStringList("battles.sieges.trigger.items");
        return allowedItems;
    }

    @EventHandler
    public void onTrigger(PlayerInteractEvent e) {
        final Player p = e.getPlayer();

        if (!e.getAction().isRightClick())
            return;

        final ItemStack i = e.getItem();
        if (i == null || i.getType().isAir())
            return;

        final boolean hasValidItem = getAllowedItems().stream()
            .anyMatch(itemId -> ItemUtils.match(i, itemId));

        if (!hasValidItem)
            return;

        BattleUtils.attemptSiegeAtLocation(p);
    }

    @EventHandler
    public void onTrigger(RallyPlaceEvent e) {
        final Player p = e.getPlayer(); // TODO Implement

        BattleUtils.attemptSiegeAtLocation(p);
    }
}
