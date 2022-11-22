package me.ShermansWorld.AlathraWar.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.War;
import me.ShermansWorld.AlathraWar.commands.WarCommands;
import me.ShermansWorld.AlathraWar.hooks.LuckPermsHook;
import me.ShermansWorld.AlathraWar.hooks.TABHook;

import org.bukkit.event.Listener;

public class JoinListener implements Listener {
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player p = event.getPlayer();
		Main.rolesData.getData(p.getUniqueId());
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				boolean inWar = false;
				boolean colorRemoved = false;
				for (final War war : WarCommands.wars) {
				    if (war.getSide1Players() == null) {
				        Main.warLogger.log("ERROR: Unable to retrieve side 1 of war " + war.getName());
				    } else if (war.getSide1Players().contains(p.getName())) {
						TABHook.assignSide1WarSuffix(p, war);
						if (!colorRemoved) {
							TABHook.removeColorPrefix(p, LuckPermsHook.getPrefix(p.getName()));
							colorRemoved = true;
						}
						inWar = true;
					} else if (war.getSide2Players() == null) {
					    Main.warLogger.log("ERROR: Unable to retrieve side 2 of war " + war.getName());
					} else if (war.getSide2Players().contains(p.getName())) {
						TABHook.assignSide2WarSuffix(p, war);
						if (!colorRemoved) {
							TABHook.removeColorPrefix(p, LuckPermsHook.getPrefix(p.getName()));
							colorRemoved = true;
						}
						inWar = true;
					}
					if (!inWar) {
						LuckPermsHook.resetPrefix(p.getName());
					}
				}
			}
		}, 60L); // 20 Tick (1 Second) delay before run() is called
	}
}
