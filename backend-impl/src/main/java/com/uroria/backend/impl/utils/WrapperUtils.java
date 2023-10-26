package com.uroria.backend.impl.utils;

import com.uroria.are.Application;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@UtilityClass
public class WrapperUtils {

    public Optional<String> getGroupName() {
        if (Application.isOffline()) return Optional.empty();
        String property = Application.getVariables().get("groupName");
        return Optional.ofNullable(property);
    }

    public Optional<Integer> getTemplateId(Logger logger) {
        if (Application.isOffline()) return Optional.empty();
        String property = getTemplateIdFromVariables();
        if (property == null) {
            Properties properties = System.getProperties();
            property = properties.getProperty("server.tid");
        }
        return parseTemplateId(property, logger);
    }

    public Optional<Long> getServerId(Logger logger) {
        if (Application.isOffline()) return Optional.empty();
        String property = getServerIdFromVariables();
        if (property == null) {
            Properties properties = System.getProperties();
            property = properties.getProperty("server.id");
        }
        return parseServerId(property, logger);
    }

    private Optional<Long> parseServerId(String property, Logger logger) {
        if (property == null) return Optional.empty();
        try {
            long id = Long.parseLong(property);
            return Optional.of(id);
        } catch (Exception exception) {
            logger.error("Cannot parse server-id of string " + property, exception);
            return Optional.empty();
        }
    }

    private Optional<Integer> parseTemplateId(String property, Logger logger) {
        if (property == null) return Optional.empty();
        try {
            int id = Integer.parseInt(property);
            return Optional.of(id);
        } catch (Exception exception) {
            logger.error("Cannot parse template-id of string " + property, exception);
            return Optional.empty();
        }
    }

    private @Nullable String getServerIdFromVariables() {
        Map<String, String> variables = Application.getVariables();
        return variables.get("serverId");
    }

    private @Nullable String getTemplateIdFromVariables() {
        Map<String, String> variables = Application.getVariables();
        return variables.get("templateId");
    }
}
