package com.uroria.backend.impl.communication.response;

import com.uroria.backend.impl.communication.TopicHolder;
import lombok.NonNull;

import java.util.function.Supplier;

public interface Request extends TopicHolder {

    byte[] getData();

    void respondSync(byte @NonNull [] data);

    void respondSync(@NonNull Supplier<byte @NonNull []> data);

    void respondAsync(byte @NonNull [] data);

    void respondAsync(@NonNull Supplier<byte @NonNull []> data);
}
