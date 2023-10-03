package com.uroria.backend.clan;

import com.uroria.annotations.safety.TimeConsuming;
import com.uroria.backend.Deletable;
import com.uroria.backend.user.User;
import lombok.NonNull;

import java.util.Collection;
import java.util.UUID;

public interface Clan extends Deletable {

    String getTag();

    String getName();

    void setTag(@NonNull String tag);

    long getFoundingDate();

    @TimeConsuming
    boolean hasMember(@NonNull User user);

    void addMember(@NonNull User user);

    void addOperator(@NonNull User user);

    void addModerator(@NonNull User user);

    @TimeConsuming
    void removeMember(User user);

    void removeModerator(User user);

    void removeOperator(User user);

    void removeMember(UUID uuid);

    void removeModerator(UUID uuid);

    void removeOperator(UUID uuid);

    @TimeConsuming
    Collection<User> getOperators();

    @TimeConsuming
    Collection<User> getMembers();

    @TimeConsuming
    Collection<User> getModerators();
}
