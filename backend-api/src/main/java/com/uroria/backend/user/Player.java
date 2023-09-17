package com.uroria.backend.user;

import com.uroria.backend.Deletable;
import com.uroria.backend.PropertyHolder;
import com.uroria.backend.permission.PermHolder;
import com.uroria.backend.stats.StatHolder;

import java.util.UUID;

public interface Player extends PermHolder, PropertyHolder, Deletable, StatHolder {

    UUID getUniqueId();

}
