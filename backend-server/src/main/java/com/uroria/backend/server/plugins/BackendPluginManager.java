package com.uroria.backend.server.plugins;

import com.uroria.backend.api.plugins.BackendPlugin;
import com.uroria.backend.api.plugins.PluginConfiguration;
import com.uroria.backend.api.plugins.PluginManager;
import com.uroria.backend.server.Uroria;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class BackendPluginManager implements PluginManager {
    private final Map<String, BackendPlugin> plugins;
    private final File pluginFolder;
    private final PluginClassLoader classLoader;
    private final Uroria uroria;

    public BackendPluginManager(Uroria uroria) {
        this.pluginFolder = new File("./plugins");
        this.plugins = new HashMap<>();
        this.classLoader = new PluginClassLoader(new URL[0]);
        this.uroria = uroria;
        if (!pluginFolder.exists()) {
            if (!pluginFolder.mkdir()) {
                Uroria.getLogger().error("Cannot create plugin directory");
            }
        }
    }

    public void startPlugins() {
        if (!this.plugins.isEmpty()) return;
        File[] files = this.pluginFolder.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.isFile() && !file.getName().endsWith(".jar")) continue;
            loadPlugin(file.getName()).ifPresent(BackendPlugin::start);
        }
    }

    public void stopPlugins() {
        this.plugins.forEach((name, plugin) -> plugin.stop());
    }

    private synchronized Optional<BackendPlugin> loadPlugin(String pluginFileName) {
        if (pluginFileName == null || pluginFileName.isEmpty()) return Optional.empty();
        File jarFile = new File(pluginFolder.getPath() + "/" + pluginFileName);
        if (!jarFile.isFile() || !jarFile.getName().endsWith(".jar")) return Optional.empty();
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(jarFile))) {
            while (true) {
                ZipEntry entry = zip.getNextEntry();
                if (entry == null) break;
                String zipName = entry.getName();
                if (zipName.endsWith("module.yml")) {
                    PluginConfiguration pluginConfiguration = new PluginConfiguration(zip);
                    String name = pluginConfiguration.getPluginName();
                    String version = pluginConfiguration.getVersion();
                    String main = pluginConfiguration.getMain();
                    if (name == null) {
                        Uroria.getLogger().error("Name in " + jarFile.getName() + " is null");
                        break;
                    }
                    if (version == null) {
                        Uroria.getLogger().error("Version in " + jarFile.getName() + " is null");
                        break;
                    }
                    if (main == null) {
                        Uroria.getLogger().error("Main in " + jarFile.getName() + " is null");
                        break;
                    }
                    if (plugins.containsKey(name)) {
                        Uroria.getLogger().warn("Ambiguous plugin name in " + jarFile.getName() + " with the plugin " + plugins.get(name).getClass().getName());
                        break;
                    }

                    this.classLoader.addPath(jarFile.toPath());

                    Class<?> clazz = this.classLoader.loadClass(main);
                    Constructor<?> declaredConstructor = clazz.getDeclaredConstructor();
                    declaredConstructor.setAccessible(true);
                    BackendPlugin plugin = (BackendPlugin) declaredConstructor.newInstance(this.uroria, pluginConfiguration);
                    declaredConstructor.setAccessible(false);
                    this.plugins.put(plugin.getPluginName(), plugin);
                    return Optional.of(plugin);
                }
            }
        } catch (Exception exception) {
            Uroria.getLogger().error("Cannot load plugin " + jarFile.getName(), exception);
        }
        return Optional.empty();
    }

    @Override
    public Collection<BackendPlugin> getPlugins() {
        return null;
    }

    @Override
    public Optional<BackendPlugin> getPlugin(String name) {
        return Optional.empty();
    }
}
