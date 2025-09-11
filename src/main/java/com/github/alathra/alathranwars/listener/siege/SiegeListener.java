package com.github.alathra.alathranwars.listener.siege;

import com.github.alathra.alathranwars.conflict.battle.siege.Siege;
import com.github.alathra.alathranwars.conflict.battle.siege.SiegeUtils;
import com.github.alathra.alathranwars.conflict.war.War;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.github.alathra.alathranwars.conflict.war.side.Side;
import com.github.alathra.alathranwars.enums.battle.BattleSide;
import com.github.alathra.alathranwars.enums.battle.BattleType;
import com.github.alathra.alathranwars.enums.battle.BattleVictoryReason;
import com.github.alathra.alathranwars.event.battle.*;
import com.github.alathra.alathranwars.packet.CustomLaser;
import com.github.alathra.alathranwars.utility.UtilsChat;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SiegeListener implements Listener {
    private final static Title.Times TITLE_TIMES = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500));
    private final static Sound SOUND_VICTORY = Sound.sound(Key.key("item.goat_horn.sound.0"), Sound.Source.VOICE, 0.5f, 1.0F);
    private final static Sound SOUND_DEFEAT = Sound.sound(Key.key("entity.wither.death"), Sound.Source.VOICE, 0.5f, 1.0F);
    private final static List<Sound> SOUND_GOATHORNS = List.of(
        Sound.sound(Key.key("item.goat_horn.sound.0"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F)),
        Sound.sound(Key.key("item.goat_horn.sound.2"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F)),
        Sound.sound(Key.key("item.goat_horn.sound.3"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F)),
        Sound.sound(Key.key("item.goat_horn.sound.7"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F))
    );

    /**
     * On battle start UI handling.
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onBattleStart(BattleStartEvent e) {
        if (!e.getBattle().getBattleType().equals(BattleType.SIEGE)) return;

        if (!(e.getBattle() instanceof Siege siege)) return;

        final Title defTitle = Title.title(
            ColorParser.of("<red><u><b><town>")
                .with("town", siege.getTown().getName())
                .build(),
            ColorParser.of("<gray><i>Is under siege, defend!")
                .build(),
            TITLE_TIMES
        );
        final Title attTitle = Title.title(
            ColorParser.of("<red><u><b><town>")
                .with("town", siege.getTown().getName())
                .build(),
            ColorParser.of("<gray><i>Has been put to siege, attack!")
                .build(),
            TITLE_TIMES
        );

        
        siege.getPlayersInZone(BattleSide.DEFENDER).forEach(player -> {
            player.showTitle(defTitle);
            player.playSound(SOUND_GOATHORNS.get(new Random().nextInt(1, SOUND_GOATHORNS.size())));
        });

        siege.getPlayersInZone(BattleSide.ATTACKER).forEach(player -> {
            player.showTitle(attTitle);
            player.playSound(SOUND_GOATHORNS.get(new Random().nextInt(1, SOUND_GOATHORNS.size())));
        });
    }

    /**
     * On battle end UI handling.
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onBattleResult(BattleResultEvent e) {
        if (!e.getBattle().getBattleType().equals(BattleType.SIEGE)) return;

        if (!(e.getBattle() instanceof Siege siege)) return;

        switch (e.getBattleVictor()) {
            case ATTACKER -> {
                if (e.getBattleVictoryReason().equals(BattleVictoryReason.OPPONENT_RETREAT)) {
                    Bukkit.broadcast(
                        ColorParser.of("<prefix>The town of <town> has surrendered.")
                            .with("prefix", UtilsChat.getPrefix())
                            .with("town", siege.getTown().getName())
                            .build()
                    );
                } else {
                    Bukkit.broadcast(ColorParser.of("<prefix>The town of <town> has been sacked and placed under occupation by the armies of <attacker>!")
                        .with("prefix", UtilsChat.getPrefix())
                        .with("town", siege.getTown().getName())
                        .with("attacker", siege.getAttackerSide().getName())
                        .build());
                }

                final Title vicAttackTitle = Title.title(
                    ColorParser.of("<green><u><b>Victory")
                        .build(),
                    ColorParser.of("<gray><i><town> has been captured!")
                        .with("town", siege.getTown().getName())
                        .build(),
                    TITLE_TIMES
                );
                final Title losAttackTitle = Title.title(
                    ColorParser.of("<red><u><b>Defeat")
                        .build(),
                    ColorParser.of("<gray><i><town> has been lost!")
                        .with("town", siege.getTown().getName())
                        .build(),
                    TITLE_TIMES
                );
                siege.getPlayersInZone(BattleSide.ATTACKER).forEach(player -> {
                    player.showTitle(vicAttackTitle);
                    player.playSound(SOUND_VICTORY);
                });
                siege.getPlayersInZone(BattleSide.DEFENDER).forEach(player -> {
                    player.showTitle(losAttackTitle);
                    player.playSound(SOUND_DEFEAT);
                });
            }
            case DEFENDER -> {
                if (e.getBattleVictoryReason().equals(BattleVictoryReason.OPPONENT_RETREAT)) {
                    Bukkit.broadcast(
                        ColorParser.of("<prefix>The siege at <town> has been abandoned by the attackers.")
                            .with("prefix", UtilsChat.getPrefix())
                            .with("town", siege.getTown().getName())
                            .build()
                    );
                } else {
                    Bukkit.broadcast(
                        ColorParser.of("<prefix>The siege of <town> has been lifted by the defenders!")
                            .with("prefix", UtilsChat.getPrefix())
                            .with("town", siege.getTown().getName())
                            .build()
                    );
                }

                final Title vicDefendTitle = Title.title(
                    ColorParser.of("<red><u><b>Defeat")
                        .build(),
                    ColorParser.of("<gray><i>We failed to capture <town>!")
                        .with("town", siege.getTown().getName())
                        .build(),
                    TITLE_TIMES
                );
                final Title losDefendTitle = Title.title(
                    ColorParser.of("<green><u><b>Victory")
                        .build(),
                    ColorParser.of("<gray><i><town> has been made safe!")
                        .with("town", siege.getTown().getName())
                        .build(),
                    TITLE_TIMES
                );
                siege.getPlayersInZone(BattleSide.ATTACKER).forEach(player -> {
                    player.showTitle(vicDefendTitle);
                    player.playSound(SOUND_DEFEAT);
                });
                siege.getPlayersInZone(BattleSide.DEFENDER).forEach(player -> {
                    player.showTitle(losDefendTitle);
                    player.playSound(SOUND_VICTORY);
                });
            }
            case DRAW -> {
                Bukkit.broadcast(
                    ColorParser.of("<prefix>The siege of <town> has ended in a draw!")
                        .with("prefix", UtilsChat.getPrefix())
                        .with("town", siege.getTown().getName())
                        .build()
                );

                final Title drawTitle = Title.title(
                    ColorParser.of("<yellow><u><b>Draw")
                        .build(),
                    ColorParser.of("<gray><i>The siege at <town> has ended!")
                        .with("town", siege.getTown().getName())
                        .build(),
                    TITLE_TIMES
                );
                siege.getPlayersInZone(BattleSide.ATTACKER).forEach(player -> {
                    player.showTitle(drawTitle);
                    player.playSound(SOUND_DEFEAT);
                });
                siege.getPlayersInZone(BattleSide.DEFENDER).forEach(player -> {
                    player.showTitle(drawTitle);
                    player.playSound(SOUND_DEFEAT);
                });
            }
        }
    }

    /**
     * On battle end handle occupation results.
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBattleResultOccupy(BattleResultEvent e) {
        if (!e.getBattle().getBattleType().equals(BattleType.SIEGE)) return;

        if (!(e.getBattle() instanceof Siege siege)) return;

        switch (e.getBattleVictor()) {
            case ATTACKER -> {
                War war = e.getWar();

                if (war.isEventWar()) return;

                Town town = siege.getTown();
                Side townSide = war.getSide(town);

                if (townSide == null) return;

                final boolean isLiberation = siege.getAttackerSide().equals(townSide); // Is the town being liberated or occupied

                if (isLiberation) { // Liberation siege
                    if (townSide.isSurrendered(town)) {
                        war.unsurrender(town);
                    }
                } else { // Occupation siege
                    if (!townSide.isSurrendered(town)) {
                        war.surrender(town);
                    }
                }
            }
            case DEFENDER, DRAW -> {
            }
        }
    }

    /**
     * On battle end handle war score distribution.
     *
     * @param e event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBattleResultWarScore(BattleResultEvent e) {
        if (!e.getBattle().getBattleType().equals(BattleType.SIEGE)) return;

        if (!(e.getBattle() instanceof Siege siege)) return;

        switch (e.getBattleVictor()) {
            case ATTACKER -> {
                siege.getAttackerSide().addScore(50);
                siege.getDefenderSide().addScore(5);
            }
            case DEFENDER -> {
                siege.getDefenderSide().addScore(10);
            }
            case DRAW -> {
            }
        }
    }

    /**
     * On player entering a battlefield.
     * @param e event
     */
    @EventHandler
    public void onBattleEnter(PlayerEnteredBattlefieldEvent e) {
        if (!e.getBattle().getBattleType().equals(BattleType.SIEGE)) return;

        if (!(e.getBattle() instanceof Siege siege)) return;

        final Player p = e.getPlayer();

        final Title defTitle = Title.title(
            ColorParser.of("<red><u><b>Battle")
                .with("town", siege.getTown().getName())
                .build(),
            ColorParser.of("<gray><i>You entered a battlefield!")
                .build(),
            TITLE_TIMES
        );

        p.showTitle(defTitle);

        // Boss bar
        final BattleSide battleSide = siege.getPlayerBattleSide(p);
        switch (battleSide) {
            case ATTACKER -> siege.getBossBarManager().addAttackerBar(p);
            case DEFENDER -> siege.getBossBarManager().addDefenderBar(p);
            case SPECTATOR -> siege.getBossBarManager().addSpectatorBar(p);
        }

        // End Crystal laser
        CustomLaser laser = siege.getLaserManager().getLaser();
        if (laser != null)
            laser.addViewer(p);
    }

    /**
     * On player leaving a battlefield.
     * @param e event
     */
    @EventHandler
    public void onBattleLeave(PlayerLeftBattlefieldEvent e) {
        if (!e.getBattle().getBattleType().equals(BattleType.SIEGE)) return;

        if (!(e.getBattle() instanceof Siege siege)) return;

        final Player p = e.getPlayer();

        final Title defTitle = Title.title(
            ColorParser.of("<red><u><b>Battle")
                .with("town", siege.getTown().getName())
                .build(),
            ColorParser.of("<gray><i>You left the battlefield!")
                .build(),
            TITLE_TIMES
        );

        p.showTitle(defTitle);

        // Boss bar remove player from audience
        final BattleSide battleSide = siege.getPlayerBattleSide(p);
        switch (battleSide) {
            case ATTACKER -> siege.getBossBarManager().removeAttackerBar(p);
            case DEFENDER -> siege.getBossBarManager().removeDefenderBar(p);
            case SPECTATOR -> siege.getBossBarManager().removeSpectatorBar(p);
        }

        // End Crystal laser
        CustomLaser laser = siege.getLaserManager().getLaser();
        if (laser != null)
            laser.removeViewer(p);
    }

    @EventHandler
    public void onPlayerServerLeave(PlayerQuitEvent e) {
        final Player p = e.getPlayer();

        for (Siege siege : WarController.getInstance().getSieges()) {
            // Boss bar remove player from audience
            final BattleSide battleSide = siege.getPlayerBattleSide(p);
            switch (battleSide) {
                case ATTACKER -> siege.getBossBarManager().removeAttackerBar(p);
                case DEFENDER -> siege.getBossBarManager().removeDefenderBar(p);
                case SPECTATOR -> siege.getBossBarManager().removeSpectatorBar(p);
            }

            // End Crystal laser
            CustomLaser laser = siege.getLaserManager().getLaser();
            if (laser != null)
                laser.removeViewer(p);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMoveControlPoint(SetControlPointEvent e) {
        if (!WarController.getInstance().isInAnySieges(e.getTown()))
            return;

        Set<Siege> sieges = WarController.getInstance().getSieges();

        for (Siege siege : sieges) {
            if (!siege.getTown().equals(e.getTown()))
                continue;

            siege.setControlPoint(e.getNewLocation());

            // End Crystal laser position update
            CustomLaser laser = siege.getLaserManager().getLaser();
            if (laser != null && siege.getControlPoint() != null)
                laser.move(SiegeUtils.getLaserFromLocation(siege.getControlPoint()), SiegeUtils.getLaserToLocation(siege.getControlPoint()));
        }
    }
}
