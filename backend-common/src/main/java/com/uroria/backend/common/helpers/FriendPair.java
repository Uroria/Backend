package com.uroria.backend.common.helpers;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public record FriendPair(UUID friend, long friendshipDate) implements Serializable {
    @Serial private static final long serialVersionUID = 1;
}
