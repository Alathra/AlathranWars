package io.github.alathra.alathranwars.command.towny;

import io.github.alathra.alathranwars.event.battle.PreSetControlPointEvent;
import io.github.alathra.alathranwars.event.battle.SetControlPointEvent;
import io.github.alathra.alathranwars.meta.ControlPoint;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.command.BaseCommand;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.AddonCommand;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

class TownAdminCommands extends BaseCommand implements TabExecutor {
    private static final String PERM_ADMIN = "alathranwars.command.admin.capturepoint";

    public TownAdminCommands() {
        AddonCommand adminCapturePointCommand = new AddonCommand(TownyCommandAddonAPI.CommandType.TOWNYADMIN_TOWN, "capturepoint", this);

        TownyCommandAddonAPI.addSubCommand(adminCapturePointCommand);
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0 && sender instanceof Player p) {
            try {
                set(p, args);
            } catch (TownyException e) {
                p.sendMessage(
                    ColorParser.of(
                        "<red>" + e.getMessage()
                    ).build()
                );
            }
        } else {
            help(sender);
        }

        return true;
    }

    public void help(@NotNull CommandSender sender) {
        sender.sendMessage(
            ColorParser.of(
                "/ta set capturepoint [town]"
            ).build()
        );
    }

    public void set(Player p, String[] args) throws TownyException {
        checkPermOrThrow(p, PERM_ADMIN);

        Town town = getTownOrThrow(args[0]);

        final @Nullable Location oldLoc = ControlPoint.get(town);

        PreSetControlPointEvent preEvent = new PreSetControlPointEvent(town, oldLoc, p.getLocation(), p);
        if (!preEvent.callEvent())
            throw new TownyException("The capture point could not be set.");

        final Location loc = preEvent.getNewLocation();

        if (!town.getWorld().equals(loc.getWorld()))
            throw new TownyException("The capture point is not in the same world as the town.");

        ControlPoint.set(town, loc);

        new SetControlPointEvent(town, oldLoc, loc, p).callEvent();
    }
}
