package com.uroria.backend.user.punishment;

import com.uroria.backend.user.punishment.mute.Mute;

import java.util.List;

public interface Punishable {

    List<Punishment> getActivePunishments();

    List<Punishment> getExpiredPunishments();

    List<Mute> getActiveMutes();

    List<Mute> getExpiredMutes();
}
