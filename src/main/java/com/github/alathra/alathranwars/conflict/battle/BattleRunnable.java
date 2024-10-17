package com.github.alathra.alathranwars.conflict.battle;

public abstract class BattleRunnable implements Runnable {
    private final long tickRate;

    public BattleRunnable(long tickRate) {
        this.tickRate = tickRate;
    }

    public long getTickRate() {
        return tickRate;
    }

    public void stop() {}
}
