package me.simplyran.simplymines.managers;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.objects.ConfigData;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final SimplyMines plugin;
    private final List<ConfigData<?>> registry = new ArrayList<>();
    private boolean initialized = false;

    public ConfigManager(@NotNull SimplyMines plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        reloadConfig();
    }

    public void register(@NotNull ConfigData<?> data) {
        registry.add(data);
        if (initialized) {
            applyAndPersist(data);
        }
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        loadConfigData();
    }

    private void loadConfigData() {
        FileConfiguration config = plugin.getConfig();

        for (ConfigData<?> data : registry) {
            config.addDefault(data.getPath(), data.getDefaultValue());
        }

        config.options().copyDefaults(true);
        plugin.saveConfig();

        for (ConfigData<?> data : registry) {
            applyValue(data, config);
        }

        initialized = true;
    }

    private void applyAndPersist(ConfigData<?> data) {
        FileConfiguration config = plugin.getConfig();
        config.addDefault(data.getPath(), data.getDefaultValue());
        config.options().copyDefaults(true);
        applyValue(data, config);
        plugin.saveConfig();
    }

    @SuppressWarnings("unchecked")
    private <T> void applyValue(ConfigData<T> data, FileConfiguration config) {
        data.setValue((T) config.get(data.getPath(), data.getDefaultValue()));
    }
}
