package io.github.alathra.alathranwars.listener.war;

import io.github.alathra.alathranwars.event.WarTimeStartedEvent;
import io.github.alathra.alathranwars.utility.UtilsChat;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;

public class WarListener implements Listener {
    @EventHandler
    public void onWarTimeStart(WarTimeStartedEvent e) {
        final Audience audience = Audience.audience(e.getWar().getPlayersOnlineAll());
        final Title warTitle = Title.title(
            ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War")
                .build(),
            ColorParser.of("<gray><i>War time has started for <name>!")
                .with("name", e.getWar().getLabel())
                .build(),
            Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
        );
        final Sound warSound = Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.VOICE, 0.5f, 1.0F);
        audience.showTitle(warTitle);
        audience.playSound(warSound);

        Audience.audience(Audience.audience(Bukkit.getServer().getOnlinePlayers().stream().filter(p -> !e.getWar().getPlayersOnline().contains(p)).toList()), Bukkit.getServer().getConsoleSender())
            .sendMessage(ColorParser.of("<prefix><white>War time has started for <name>.")
                .with("prefix", UtilsChat.getPrefix())
                .with("name", e.getWar().getLabel())
                .build());
    }

    //@EventHandler
    //public void onWarTimeEnd(WarTimeEndedEvent e) {
    // TODO Small notification about end of war time, battles will play out
    //}
}
