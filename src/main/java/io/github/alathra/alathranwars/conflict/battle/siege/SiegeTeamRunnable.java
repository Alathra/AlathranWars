package io.github.alathra.alathranwars.conflict.battle.siege;

import io.github.alathra.alathranwars.conflict.battle.BattleRunnable;
import org.bukkit.Location;

import static io.github.alathra.alathranwars.conflict.battle.siege.Siege.BATTLEFIELD_RANGE;

public class SiegeTeamRunnable extends BattleRunnable {
    private final Siege siege;

    public SiegeTeamRunnable(Siege siege) {
        super(1);
        this.siege = siege;
    }

    @Override
    public void run() {
        final Location controlPoint = siege.getControlPoint();

        if (controlPoint == null)
            return;

        // Calculate battlefield players
        siege.calculateBattlefieldPlayers(controlPoint, BATTLEFIELD_RANGE, siege.getWar(), siege);
    }
}
