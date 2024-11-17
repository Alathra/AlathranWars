package com.github.alathra.alathranwars.event;

import com.github.alathra.alathranwars.conflict.war.War;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WarCreateEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final War war;

    public WarCreateEvent(War war) {
        this.war = war;
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
}
