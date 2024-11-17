package com.github.alathra.alathranwars.conflict.battle.siege;

import com.github.alathra.alathranwars.conflict.battle.BattleRunnable;
import com.github.alathra.alathranwars.packet.ParticleCircle;
import com.github.retrooper.packetevents.protocol.color.Color;
import org.bukkit.Location;

/**
 * A runnable which sends a packets to players on the battlefield drawing a circle around the capture point.
 */
public class SiegeParticleRunnable extends BattleRunnable {
    private final Siege siege;

    public SiegeParticleRunnable(Siege siege) {
        super(1);
        this.siege = siege;
    }

    @Override
    public void run() {
        final Location controlPoint = siege.getControlPoint();

        if (controlPoint == null)
            return;

        // TODO Fade between side colors depending on who has more people on point
        ParticleCircle.sendCircle(siege.getPlayersOnBattlefield(), controlPoint, SiegeRunnable.CAPTURE_RANGE, 90, new Color(255, 255, 255));
    }
}
