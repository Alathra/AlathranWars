package io.github.alathra.alathranwars.deathspectate.task;

import io.github.alathra.alathranwars.AlathranWars;
import io.github.alathra.alathranwars.deathspectate.DeathConfig;
import io.github.alathra.alathranwars.deathspectate.DeathUtil;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static io.github.alathra.alathranwars.deathspectate.DeathUtil.META_TELEPORTING;

public class SpectateTask extends BukkitRunnable {
    private final Plugin plugin;
    private final Instant respawnEndTime;
    private final Duration respawnTime;
    private final Player p;
    private final Location deathLocation;

    public SpectateTask(Player p, Duration respawnTime) {
        this.plugin = AlathranWars.getInstance();
        this.respawnEndTime = Instant.now().plus(respawnTime);
        this.respawnTime = respawnTime;
        this.p = p;
        this.deathLocation = p.getLocation();

        final long secondsRemaining = ChronoUnit.SECONDS.between(Instant.now(), respawnEndTime);
        final Duration titleFadeOutTime = Duration.ofSeconds(1);

        final Component mainTitle = getTitle(secondsRemaining);
        final Component subTitle = getSubTitle(secondsRemaining);
        final Title.Times times = Title.Times.times(
            Duration.ofSeconds(0),
            this.respawnTime.minus(titleFadeOutTime),
            titleFadeOutTime
        );
        p.showTitle(Title.title(mainTitle, subTitle, times));
    }

    private Component getTitle(long secondsRemaining) {
        return ColorParser.of(DeathConfig.getDeathTitle())
            .papi(p)
            .with("time", String.valueOf(secondsRemaining))
            .legacy()
            .build();
    }

    private Component getSubTitle(long secondsRemaining) {
        return ColorParser.of(DeathConfig.getDeathSubTitle())
            .papi(p)
            .with("time", String.valueOf(secondsRemaining))
            .legacy()
            .build();
    }

    public void run() {
        if (p.isDead() && DeathUtil.isSpectating(p)) {
            DeathUtil.removeSpectator(p, null);
            this.cancel();
            return;
        }

        if (!DeathUtil.isSpectating(p)) {
            this.cancel();
            return;
        }

        final Instant now = Instant.now();

        final long secondsRemaining = ChronoUnit.SECONDS.between(now, respawnEndTime);
        p.sendTitlePart(TitlePart.TITLE, getTitle(secondsRemaining));
        p.sendTitlePart(TitlePart.SUBTITLE, getSubTitle(secondsRemaining));

        if (now.isAfter(respawnEndTime)) {
            DeathUtil.respawnPlayer(p);
            p.clearTitle();
            this.cancel();
            return;
        }

        if (deathLocation.distanceSquared(p.getLocation()) > 0)
            teleportPlayer(deathLocation.setDirection(p.getLocation().getDirection()));

        p.setFlySpeed(0F);
    }

    public void teleportPlayer(Location location) {
        p.setMetadata(META_TELEPORTING, new FixedMetadataValue(plugin, true));
        p.teleport(location);
    }
}
