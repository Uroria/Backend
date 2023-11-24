package com.uroria.backend.impl.utils;

import com.uroria.base.lang.Language;
import com.uroria.base.lang.Translation;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;

@UtilityClass
public class TranslationUtils {

    public Component disconnect(Component component) {
        return Translation.component(Language.DEFAULT, "kick.default")
                .replaceText(builder -> builder.matchLiteral("<msg>")
                .replacement(component));
    }

    public Component disconnect(Component component, String error) {
        return Translation.component(Language.DEFAULT, "kick.error")
                .replaceText(builder -> builder.matchLiteral("<msg>")
                        .replacement(component)
                        .matchLiteral("<error>")
                        .replacement(error));
    }
}
