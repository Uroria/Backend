package com.uroria.backend;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface PropertyHolder {

    Map<String, Object> getProperties();

    void setProperty(@NonNull String key, @NonNull String value);

    void setProperty(@NonNull String key, int value);

    void setProperty(@NonNull String key, long value);

    void setProperty(@NonNull String key, double value);

    void setProperty(@NonNull String key, float value);

    void setProperty(@NonNull String key, boolean value);

    default @Nullable String getPropertyString(@NonNull String key) {
        return getPropertyStringOrElse(key, null);
    }

    String getPropertyStringOrElse(@NonNull String key, @Nullable String defValue);

    default int getPropertyInt(@NonNull String key) {
        return getPropertyIntOrElse(key, 0);
    }

    int getPropertyIntOrElse(@NonNull String key, int defValue);

    default long getPropertyLong(@NonNull String key) {
        return getPropertyLongOrElse(key, 0L);
    }

    long getPropertyLongOrElse(@NonNull String key, long defValue);

    default double getPropertyDouble(@NonNull String key) {
        return getPropertyDoubleOrElse(key, 0d);
    }

    double getPropertyDoubleOrElse(@NonNull String key, double defValue);

    default float getPropertyFloat(@NonNull String key) {
        return getPropertyFloatOrElse(key, 0f);
    }

    float getPropertyFloatOrElse(@NonNull String key, float defValue);

    default boolean getPropertyBoolean(@NonNull String key) {
        return getPropertyBooleanOrElse(key, false);
    }

    boolean getPropertyBooleanOrElse(@NonNull String key, boolean defValue);
}
