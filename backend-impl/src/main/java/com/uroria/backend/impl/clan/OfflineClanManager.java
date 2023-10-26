package com.uroria.backend.impl.clan;

import com.uroria.backend.user.User;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public final class OfflineClanManager {
    private final ObjectSet<OfflineClan> clans;

    public OfflineClanManager() {
        this.clans = new ObjectArraySet<>();
    }

    public OfflineClan getClan(String tag) {
        for (OfflineClan clan : this.clans) {
            if (clan.getTag().equals(tag)) return clan;
        }
        return null;
    }

    public OfflineClan getClanByName(String name) {
        for (OfflineClan clan : this.clans) {
            if (clan.getName().equals(name)) return clan;
        }
        return null;
    }

    public OfflineClan createClan(String tag, String name, User operator, long foundingDate) {
        OfflineClan clan = new OfflineClan(name, foundingDate);
        clan.addOperator(operator);
        clan.setTag(tag);
        this.clans.add(clan);
        return clan;
    }
}
