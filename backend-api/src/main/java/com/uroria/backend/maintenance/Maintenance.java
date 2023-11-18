package com.uroria.backend.maintenance;

import com.uroria.backend.app.ApplicationTarget;
import lombok.NonNull;

public interface Maintenance {

    void setMessage(@NonNull String message);

    String getMessage();

    ApplicationTarget getTarget();

    void setTarget(@NonNull ApplicationTarget target);

    long getStartMs();

    void setEndMs(long startMs);

    long getEstimatedEndMs();

    void setEstimatedEndMs(long endMs);
}
