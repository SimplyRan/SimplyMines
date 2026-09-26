package me.simplyran.simplymines.managers;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.objects.ConfigData;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final SimplyMines plugin;
    private final List<ConfigData<?>> registry = new ArrayList<>();
    private final LangManager langManager;
    private final List<Runnable> reloadListeners = new ArrayList<>();
    private boolean initialized = false;

    public ConfigManager(@NotNull SimplyMines plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();

        ConfigData<String> langKey = ConfigFactory.newConfigData("lang", "en_us");
        registry.add(langKey);
        loadConfigData();

        this.langManager = new LangManager(plugin, langKey);
    }

    public void register(@NotNull ConfigData<?> data) {
        registry.add(data);
        if (initialized) {
            applyAndPersist(data);
        }
    }

    public void registerLang(@NotNull ConfigData<String> data) {
        langManager.register(data);
    }

    /** Runs after config.yml and the lang file were reloaded, so listeners can re-read their values. */
    public void addReloadListener(@NotNull Runnable listener) {
        reloadListeners.add(listener);
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        loadConfigData();
        langManager.reloadLang();
        for (Runnable listener : reloadListeners) {
            listener.run();
        }
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
