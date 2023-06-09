package com.uroria.backend.pluginapi.plugins;

import org.apache.pulsar.shade.org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class PluginConfiguration {
    private final Map<String, Object> mapping;
    private final String pluginName, version, main;
    public PluginConfiguration(InputStream inputStream) {
        Yaml yml = new Yaml();
        this.mapping = yml.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        this.pluginName = getString("name");
        this.version = getString("version");
        this.main = getString("main");
    }

    public String getString(String key) {
        String[] tree = key.split("\\.");
        String string = null;
        for (String s : tree) {
            string = (String) mapping.get(s);
        }
        return string;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getVersion() {
        return version;
    }

    public String getMain() {
        return main;
    }
}
