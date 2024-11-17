package com.github.alathra.alathranwars.listener.rallypoint;

import com.github.alathra.alathranwars.AlathranWars;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.github.alathra.alathranwars.conflict.war.side.Side;
import com.github.alathra.alathranwars.conflict.war.side.spawn.SpawnCreationException;
import com.github.alathra.alathranwars.utility.SpawnUtils;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.event.actions.TownyDestroyEvent;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.List;
import java.util.Optional;

public class RallyBuildListener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(TownyDestroyEvent e) {
        // TODO Prevent building around rally point as people can block spawn blocks/break the rally banner
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(TownyBuildEvent e) {
        if (!AlathranWars.getInstance().isWarTime())
            return;

        // Check if placing rally
        if (!isValidBanner(e.getBlock())) {
            return;
        }

        final Player p = e.getPlayer();
        final Resident res = TownyAPI.getInstance().getResident(p);
        final Location location = e.getLocation();

        if (res == null)
            return;

        if (!res.isMayor() && !res.isKing() && !res.isAdmin()) {
            e.setCancelled(true);
            e.setCancelMessage("<red>Only town/nation leaders can place down rallies!");
            return;
        }

        if (!WarController.getInstance().isInAnyWars(p))
            return;


        // TODO Check for rally cooldown

        // Get side that this rally will be placed for
        final Optional<Side> sideOptional = SpawnUtils.calculateSpawnSide(location, p);
        if (sideOptional.isEmpty()) {
            e.setCancelled(true);
            e.setCancelMessage("");
            return;
        }

        final Side side = sideOptional.get();

        // Check if valid placement position
        try {
            SpawnUtils.isValidSpawnPlacement(location, side);
        } catch (SpawnCreationException exception) {
            e.setCancelled(true);
            e.setCancelMessage(exception.getMessage());
            return;
        }

        // TODO Create rally

        // TODO Message team and play sound
    }

    /**
     * Check if this is a valid banner placement
     * @param block banner block
     * @return boolean
     */
    private static boolean isValidBanner(Block block) {
        return  isCustomizedBanner(block) && canBuildRally(block);
    }

    private static final List<Material> VALID_MATERIAL = List.of(
        Material.BLACK_BANNER,
        Material.BLUE_BANNER,
        Material.BROWN_BANNER,
        Material.CYAN_BANNER,
        Material.GRAY_BANNER,
        Material.GREEN_BANNER,
        Material.LIGHT_BLUE_BANNER,
        Material.LIGHT_GRAY_BANNER,
        Material.LIME_BANNER,
        Material.MAGENTA_BANNER,
        Material.ORANGE_BANNER,
        Material.PINK_BANNER,
        Material.PURPLE_BANNER,
        Material.RED_BANNER,
        Material.WHITE_BANNER,
        Material.YELLOW_BANNER
    );

    /**
     * Check if block is a banner and is not a default white one
     * @param block block
     * @return true if customized
     */
    private static boolean isCustomizedBanner(Block block) {
        if (!Tag.BANNERS.isTagged(block.getType()))
            return false;

        // Check if standing banner
        if (!VALID_MATERIAL.contains(block.getType()))
            return false;

        // Check if customized
        if (block instanceof Banner banner)
            return banner.numberOfPatterns() > 0 || banner.getBaseColor() != DyeColor.WHITE;

        return false;
    }

    /**
     * Check if this block can be placed on the one under it
     * @param block the block being placed
     * @return boolean
     */
    private static boolean canBuildRally(Block block) {
        return block.getRelative(BlockFace.DOWN).canPlace(block.getBlockData());
    }
}
