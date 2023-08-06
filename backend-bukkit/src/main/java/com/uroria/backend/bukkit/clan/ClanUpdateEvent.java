package com.uroria.backend.bukkit.clan;

import com.uroria.backend.clan.Clan;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ClanUpdateEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private @Getter final Clan clan;

    public ClanUpdateEvent(Clan clan) {
        super(true);
        this.clan = clan;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
