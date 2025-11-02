package io.github.alathra.alathranwars.listener.battle.siege;

import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathranwars.conflict.battle.siege.Siege;
import io.github.alathra.alathranwars.conflict.war.War;
import io.github.alathra.alathranwars.conflict.war.WarController;
import io.github.alathra.alathranwars.conflict.war.side.Side;
import io.github.alathra.alathranwars.enums.battle.BattleSide;
import io.github.alathra.alathranwars.enums.battle.BattleType;
import io.github.alathra.alathranwars.enums.battle.BattleVictoryReason;
import io.github.alathra.alathranwars.event.battle.*;
import io.github.alathra.alathranwars.utility.Cfg;
import io.github.alathra.alathranwars.utility.UtilsChat;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import io.github.milkdrinkers.wordweaver.Translation;
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
import java.util.Objects;
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
            ColorParser.of(Translation.of("battle.siege.event.started.attacker.title"))
                .with("town", siege.getTown().getName())
                .with("attacker", siege.getAttackerSide().getName())
                .with("defender", siege.getDefenderSide().getName())
                .build(),
            ColorParser.of("battle.siege.event.started.attacker.subtitle")
                .with("town", siege.getTown().getName())
                .with("attacker", siege.getAttackerSide().getName())
                .with("defender", siege.getDefenderSide().getName())
                .build(),
            TITLE_TIMES
        );
        final Title attTitle = Title.title(
            ColorParser.of("battle.siege.event.started.defender.title")
                .with("town", siege.getTown().getName())
                .with("attacker", siege.getAttackerSide().getName())
                .with("defender", siege.getDefenderSide().getName())
                .build(),
            ColorParser.of("battle.siege.event.started.defender.subtitle")
                .with("town", siege.getTown().getName())
                .with("attacker", siege.getAttackerSide().getName())
                .with("defender", siege.getDefenderSide().getName())
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
                        ColorParser.of(Translation.of("battle.siege.event.ended.attacker-victory.global.surrender"))
                            .with("prefix", UtilsChat.getPrefix())
                            .with("town", siege.getTown().getName())
                            .with("attacker", siege.getAttackerSide().getName())
                            .with("defender", siege.getDefenderSide().getName())
                            .build()
                    );
                } else {
                    Bukkit.broadcast(ColorParser.of(Translation.of("battle.siege.event.ended.attacker-victory.global.forced"))
                        .with("prefix", UtilsChat.getPrefix())
                        .with("town", siege.getTown().getName())
                        .with("attacker", siege.getAttackerSide().getName())
                        .with("defender", siege.getDefenderSide().getName())
                        .build());
                }

                final Title vicAttackTitle = Title.title(
                    ColorParser.of(Translation.of("battle.siege.event.ended.attacker-victory.attacker.title"))
                        .with("town", siege.getTown().getName())
                        .with("attacker", siege.getAttackerSide().getName())
                        .with("defender", siege.getDefenderSide().getName())
                        .build(),
                    ColorParser.of(Translation.of("battle.siege.event.ended.attacker-victory.attacker.subtitle"))
                        .with("town", siege.getTown().getName())
                        .with("attacker", siege.getAttackerSide().getName())
                        .with("defender", siege.getDefenderSide().getName())
                        .build(),
                    TITLE_TIMES
                );
                final Title losAttackTitle = Title.title(
                    ColorParser.of(Translation.of("battle.siege.event.ended.attacker-victory.defender.title"))
                        .with("town", siege.getTown().getName())
                        .with("attacker", siege.getAttackerSide().getName())
                        .with("defender", siege.getDefenderSide().getName())
                        .build(),
                    ColorParser.of(Translation.of("battle.siege.event.ended.attacker-victory.defender.subtitle"))
                        .with("town", siege.getTown().getName())
                        .with("attacker", siege.getAttackerSide().getName())
                        .with("defender", siege.getDefenderSide().getName())
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
                        ColorParser.of(Translation.of("battle.siege.event.ended.defender-victory.global.surrender"))
                            .with("prefix", UtilsChat.getPrefix())
                            .with("town", siege.getTown().getName())
                            .with("attacker", siege.getAttackerSide().getName())
                            .with("defender", siege.getDefenderSide().getName())
                            .build()
                    );
                } else {
                    Bukkit.broadcast(
                        ColorParser.of(Translation.of("battle.siege.event.ended.defender-victory.global.forced"))
                            .with("prefix", UtilsChat.getPrefix())
                            .with("town", siege.getTown().getName())
                            .with("attacker", siege.getAttackerSide().getName())
                            .with("defender", siege.getDefenderSide().getName())
                            .build()
                    );
                }

                final Title vicDefendTitle = Title.title(
                    ColorParser.of(Translation.of("battle.siege.event.ended.defender-victory.attacker.title"))
                        .with("town", siege.getTown().getName())
                        .with("attacker", siege.getAttackerSide().getName())
                        .with("defender", siege.getDefenderSide().getName())
                        .build(),
                    ColorParser.of(Translation.of("battle.siege.event.ended.defender-victory.attacker.subtitle"))
                        .with("town", siege.getTown().getName())
                        .with("attacker", siege.getAttackerSide().getName())
                        .with("defender", siege.getDefenderSide().getName())
                        .build(),
                    TITLE_TIMES
                );
                final Title losDefendTitle = Title.title(
                    ColorParser.of(Translation.of("battle.siege.event.ended.defender-victory.defender.title"))
                        .with("town", siege.getTown().getName())
                        .with("attacker", siege.getAttackerSide().getName())
                        .with("defender", siege.getDefenderSide().getName())
                        .build(),
                    ColorParser.of(Translation.of("battle.siege.event.ended.defender-victory.defender.subtitle"))
                        .with("town", siege.getTown().getName())
                        .with("attacker", siege.getAttackerSide().getName())
                        .with("defender", siege.getDefenderSide().getName())
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
                    ColorParser.of(Translation.of("battle.siege.event.ended.draw.global"))
                        .with("prefix", UtilsChat.getPrefix())
                        .with("town", siege.getTown().getName())
                        .with("attacker", siege.getAttackerSide().getName())
                        .with("defender", siege.getDefenderSide().getName())
                        .build()
                );

                final Title drawTitle = Title.title(
                    ColorParser.of(Translation.of("battle.siege.event.ended.draw.title"))
                        .with("town", siege.getTown().getName())
                        .with("attacker", siege.getAttackerSide().getName())
                        .with("defender", siege.getDefenderSide().getName())
                        .build(),
                    ColorParser.of(Translation.of("battle.siege.event.ended.draw.subtitle"))
                        .with("town", siege.getTown().getName())
                        .with("attacker", siege.getAttackerSide().getName())
                        .with("defender", siege.getDefenderSide().getName())
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

                Objects.requireNonNull(town, "Town should not be null here");

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
                siege.getAttackerSide().addScore(Cfg.get().getOrDefault("battles.sieges.points.attacker-victory", 50));
                siege.getDefenderSide().addScore(Cfg.get().getOrDefault("battles.sieges.points.defender-loss", 5));
            }
            case DEFENDER -> {
                siege.getAttackerSide().addScore(Cfg.get().getOrDefault("battles.sieges.points.attacker-loss", 1));
                siege.getDefenderSide().addScore(Cfg.get().getOrDefault("battles.sieges.points.defender-victory", 10));
            }
            case DRAW -> {
            }
        }
    }

    /**
     * On player entering a battlefield.
     *
     * @param e event
     */
    @EventHandler
    public void onBattleEnter(PlayerEnteredBattlefieldEvent e) {
        if (!e.getBattle().getBattleType().equals(BattleType.SIEGE)) return;

        if (!(e.getBattle() instanceof Siege siege)) return;

        final Player p = e.getPlayer();

        final Title defTitle = Title.title(
            ColorParser.of(Translation.of("battle.battlefield.entered.title"))
                .with("town", siege.getTown().getName())
                .with("attacker", siege.getAttackerSide().getName())
                .with("defender", siege.getDefenderSide().getName())
                .build(),
            ColorParser.of(Translation.of("battle.battlefield.entered.subtitle"))
                .with("town", siege.getTown().getName())
                .with("attacker", siege.getAttackerSide().getName())
                .with("defender", siege.getDefenderSide().getName())
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
    }

    /**
     * On player leaving a battlefield.
     *
     * @param e event
     */
    @EventHandler
    public void onBattleLeave(PlayerLeftBattlefieldEvent e) {
        if (!e.getBattle().getBattleType().equals(BattleType.SIEGE)) return;

        if (!(e.getBattle() instanceof Siege siege)) return;

        final Player p = e.getPlayer();

        final Title defTitle = Title.title(
            ColorParser.of(Translation.of("battle.battlefield.left.title"))
                .with("town", siege.getTown().getName())
                .with("attacker", siege.getAttackerSide().getName())
                .with("defender", siege.getDefenderSide().getName())
                .build(),
            ColorParser.of(Translation.of("battle.battlefield.left.subtitle"))
                .with("town", siege.getTown().getName())
                .with("attacker", siege.getAttackerSide().getName())
                .with("defender", siege.getDefenderSide().getName())
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
        }
    }
}
