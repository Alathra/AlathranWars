package com.github.alathra.alathranwars.events.battle;

import com.github.alathra.alathranwars.conflict.battle.Battle;
import com.github.alathra.alathranwars.conflict.war.War;
import com.github.alathra.alathranwars.enums.battle.BattleType;
import com.github.alathra.alathranwars.enums.battle.BattleVictor;
import com.github.alathra.alathranwars.enums.battle.BattleVictoryReason;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BattleResultEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final War war;
    private final Battle battle;
    private final BattleType battleType;
    private final BattleVictor battleVictor;
    private final BattleVictoryReason battleVictoryReason;

    public BattleResultEvent(War war, Battle battle, BattleType battleType, BattleVictor battleVictor, BattleVictoryReason battleVictoryReason) {
        this.war = war;
        this.battle = battle;
        this.battleType = battleType;
        this.battleVictor = battleVictor;
        this.battleVictoryReason = battleVictoryReason;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public War getWar() {
        return war;
    }

    public Battle getBattle() {
        return battle;
    }

    public BattleType getBattleType() {
        return battleType;
    }

    public BattleVictor getBattleVictor() {
        return battleVictor;
    }

    public BattleVictoryReason getBattleVictoryReason() {
        return battleVictoryReason;
    }
}
