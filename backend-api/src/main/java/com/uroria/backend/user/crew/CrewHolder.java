package com.uroria.backend.user.crew;

import com.uroria.backend.user.User;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

public interface CrewHolder {

    List<User> getCrew();

    void addCrewMember(@NonNull User user);

    void removeCrewMember(User user);

    void removeCrewMember(UUID uuid);
}
