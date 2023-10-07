package com.uroria.backend.user;

import com.uroria.annotations.safety.TimeConsuming;
import com.uroria.backend.clan.Clan;
import com.uroria.backend.user.crew.CrewHolder;
import com.uroria.backend.user.punishment.Punishable;
import com.uroria.base.lang.Language;
import com.uroria.base.user.UserStatus;
import com.uroria.problemo.result.Result;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Represents a User. Who could've guest this.
 * Everything you need from database is in here.
 * Note: Every method here may trigger a database query. This could cost some time.
 */
public interface User extends Punishable, CrewHolder, Player {

    @NotNull String getUsername();

    @NotNull Language getLanguage();

    boolean isOnline();

    UserStatus getStatus();

    UserStatus getRealStatus();

    long getPlaytime();

    void setPlaytime(long playtime);

    long getLastJoin();

    void setLastJoin(long lastJoin);

    long getFirstJoin();

    void setStatus(@NonNull UserStatus status);

    void setLanguage(@NonNull Language language);

    void setUsername(@NonNull String username);

    @TimeConsuming
    List<User> getFriends();

    @TimeConsuming
    List<User> getFriendRequests();

    void addFriendRequest(@NonNull User user);

    void removeFriendRequest(User user);

    void removeFriendRequest(UUID uuid);

    void addFriend(@NonNull User user);

    void removeFriend(User user);

    void removeFriend(UUID uuid);

    @TimeConsuming
    Result<Clan> getClan();

    void joinClan(@NonNull Clan clan);

    void leaveClan();
}
