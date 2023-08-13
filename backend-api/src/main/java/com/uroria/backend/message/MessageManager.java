package com.uroria.backend.message;

import lombok.NonNull;

public interface MessageManager {
    void sendMessage(@NonNull Message message);
}
