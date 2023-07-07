package com.uroria.backend.server.plugins;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public final class PluginClassLoader extends URLClassLoader {
    public PluginClassLoader(URL[] urls) {
        super(urls);
    }

    void addPath(Path path) {
        try {
            addURL(path.toUri().toURL());
        } catch (MalformedURLException exception) {
            throw new AssertionError(exception);
        }
    }
}
