package com.github.alathra.alathranwars.listener.war;

import com.github.alathra.alathranwars.conflict.war.War;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.palmergames.bukkit.towny.event.NationPreAddTownEvent;
import com.github.alathra.alathranwars.conflict.war.side.Side;
import com.github.alathra.alathranwars.hook.NameColorHandler;
import com.palmergames.bukkit.towny.event.NationPreRenameEvent;
import com.palmergames.bukkit.towny.event.nation.NationPreInviteTownEvent;
import com.palmergames.bukkit.towny.event.nation.NationPreMergeEvent;
import com.palmergames.bukkit.towny.event.nation.NationPreTownLeaveEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NationListener implements Listener {
    /*@EventHandler
    public void onNationKingChange(NationKingChangeEvent e) {
        e.setCancelMessage("");
        e.setCancelled(true);
    }*/

    @EventHandler
    public void onRename(NationPreRenameEvent e) {
        Nation nation = e.getNation();

        if (WarController.getInstance().isInAnyWars(nation)) {
            e.setCancelMessage("You can't rename a nation while it's in a war.");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onTownPreInvite(NationPreInviteTownEvent e) {
        Nation nation = e.getNation();
        Town town = e.getInvitedTown();

        boolean isNationAtWar = WarController.getInstance().isInAnyWars(nation);
        if (isNationAtWar) {
            e.setCancelMessage("You cannot invite that town because your nation is in a war.");
            e.setCancelled(true);
            return;
        }

        boolean isTownAtWar = WarController.getInstance().isInAnyWars(town);
        if (isTownAtWar) {
            e.setCancelMessage("You cannot invite that town because it is in a war.");
            e.setCancelled(true);
            return;
        }

        // Add town to all nation wars
        for (War war : WarController.getInstance().getWars(nation)) {
            Side side = war.getSide(nation);
            if (side == null) continue;

            side.add(town);
        }

        // TODO Add nation to towns' wars
        for (War war : WarController.getInstance().getWars(town)) {
            Side side = war.getSide(town);
            if (side == null) continue;

            side.add(nation);
        }

        for (War war : WarController.getInstance().getWars(nation)) {
            Side side = war.getSide(nation);
            if (side == null) continue;

            side.getPlayersOnline().forEach(p -> NameColorHandler.getInstance().calculatePlayerColors(p));
        }
    }

    @EventHandler
    public void onTownPreAdd(NationPreAddTownEvent e) {
        Nation nation = e.getNation();
        Town town = e.getTown();

        boolean isNationAtWar = WarController.getInstance().isInAnyWars(nation);
        if (isNationAtWar) {
            e.setCancelMessage("Your town cannot join the nation because it is in a war.");
            e.setCancelled(true);
            return;
        }

        boolean isTownAtWar = WarController.getInstance().isInAnyWars(town);
        if (isTownAtWar) {
            e.setCancelMessage("Your town cannot join the nation because the town is in a war.");
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onTownPreLeave(NationPreTownLeaveEvent e) {
        Nation nation = e.getNation();
        Town town = e.getTown();

        boolean isNationAtWar = WarController.getInstance().isInAnyWars(nation);
        if (isNationAtWar) {
            e.setCancelMessage("A town cannot abandon its nation while at war!");
            e.setCancelled(true);
            return;
        }

        boolean isTownAtWar = WarController.getInstance().isInAnyWars(town);
        if (isTownAtWar) {
            e.setCancelMessage("Your nation cannot abandon a town while at war!");
            e.setCancelled(true);
            return;
        }
    }

//    @EventHandler
//    public void onTownJoin(NationAddTownEvent e) {
//        Nation nation = e.getNation();
//        Town town = e.getTown();
//
//        // Add town to all nation wars
//        for (War war : WarController.getInstance().getNationWars(nation)) {
//            Side side = war.getSide(nation);
//            if (side == null) continue;
//
//            side.addTown(town);
//        }
//
//        // TODO Add nation to towns' wars
//        for (War war : WarController.getInstance().getTownWars(town)) {
//            Side side = war.getSide(town);
//            if (side == null) continue;
//
//            side.addNation(nation);
//        }
//
//        for (War war : WarController.getInstance().getNationWars(nation)) {
//            Side side = war.getSide(nation);
//            if (side == null) continue;
//
//            side.getPlayers().forEach(p -> NameColorHandler.getInstance().calculatePlayerColors(p));
//        }
//    }

    /*@EventHandler // Do nothing, leaving a nation should not let you escape nation wars
    public void onTownLeave(NationTownLeaveEvent e) {

    }*/

    @EventHandler
    public void onMerge(NationPreMergeEvent e) {
        Nation nation = e.getRemainingNation();
        Nation nation1 = e.getNation();

        if (WarController.getInstance().isInAnyWars(nation) || WarController.getInstance().isInAnyWars(nation1)) {
            e.setCancelMessage("You can't merge nations while they are in a war.");
            e.setCancelled(true);
        }

        // TODO Allow merging nations in war, literally just ensure that towns are added to sieges and sides
    }
}
