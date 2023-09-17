package com.uroria.backend.user.punishment;

import com.uroria.backend.Deletable;
import com.uroria.base.lang.Language;
import net.kyori.adventure.text.Component;

public interface Punishment extends Deletable {
    
    Component getReason(Language language);

    int getReasonId();

    boolean isPermanent();
}
