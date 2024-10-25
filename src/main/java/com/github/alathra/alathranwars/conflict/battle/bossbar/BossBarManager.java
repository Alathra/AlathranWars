package com.github.alathra.alathranwars.conflict.battle.bossbar;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

/**
 * Holds three boss bars for three different audiences.
 * Exposes methods for performantly updating the boss bars and audiences.
 */
public class BossBarManager {
    private final WrappedBossBar attackerBar;
    private final WrappedBossBar defenderBar;
    private final WrappedBossBar spectatorBar;

    public BossBarManager() {
        attackerBar = new WrappedBossBar(BossBar.bossBar(Component.text(""), 0, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS));
        defenderBar = new WrappedBossBar(BossBar.bossBar(Component.text(""), 0, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS));
        spectatorBar = new WrappedBossBar(BossBar.bossBar(Component.text(""), 0, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS));
    }

    public BossBarManager(BossBar bar1, BossBar bar2, BossBar bar3) {
        attackerBar = new WrappedBossBar(bar1);
        defenderBar = new WrappedBossBar(bar2);
        spectatorBar = new WrappedBossBar(bar3);
    }

    public WrappedBossBar getAttackerBar() {
        return attackerBar;
    }

    public WrappedBossBar getDefenderBar() {
        return defenderBar;
    }

    public WrappedBossBar getSpectatorBar() {
        return spectatorBar;
    }

    public void addAttackerBar(Audience viewer) {
        attackerBar.addViewer(viewer);
    }

    public void addDefenderBar(Audience viewer) {
        defenderBar.addViewer(viewer);
    }

    public void addSpectatorBar(Audience viewer) {
        spectatorBar.addViewer(viewer);
    }

    public void removeAttackerBar(Audience viewer) {
        attackerBar.removeViewer(viewer);
    }

    public void removeDefenderBar(Audience viewer) {
        defenderBar.removeViewer(viewer);
    }

    public void removeSpectatorBar(Audience viewer) {
        spectatorBar.removeViewer(viewer);
    }
}
