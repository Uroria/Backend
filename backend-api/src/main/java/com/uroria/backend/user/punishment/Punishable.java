package com.uroria.backend.user.punishment;

import com.uroria.annotations.safety.TimeConsuming;
import com.uroria.backend.user.punishment.mute.Mute;

import java.util.List;

public interface Punishable {

    @TimeConsuming
    List<Punishment> getActivePunishments();

    @TimeConsuming
    List<Punishment> getExpiredPunishments();

    @TimeConsuming
    List<Mute> getActiveMutes();

    @TimeConsuming
    List<Mute> getExpiredMutes();
}
