package me.simplyran.simplymines.managers;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.objects.ConfigData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LangManager {

    private final SimplyMines plugin;
    private final ConfigData<String> langKey;
    private final List<ConfigData<String>> registry = new ArrayList<>();
    private final Set<String> warnedMissingKeys = new HashSet<>();
    private YamlConfiguration active;
    private File activeFile;
    private boolean initialized = false;

    LangManager(@NotNull SimplyMines plugin, @NotNull ConfigData<String> langKey) {
        this.plugin = plugin;
        this.langKey = langKey;
        plugin.saveResource("lang/en_us.yml", false);
        reloadLang();
    }

    public void register(@NotNull ConfigData<String> data) {
        registry.add(data);
        if (initialized) {
            applyValue(data, true);
        }
    }

    public void reloadLang() {
        File file = resolveLangFile(langKey.getValue());
        if (file == null || !file.exists()) {
            plugin.getLogger().warning("Configured lang file '" + langKey.getValue()
                    + ".yml' was not found in plugins/SimplyMines/lang/, falling back to en_us.yml.");
            file = new File(plugin.getDataFolder(), "lang" + File.separator + "en_us.yml");
            if (!file.exists()) {
                plugin.saveResource("lang/en_us.yml", true);
            }
        }

        this.activeFile = file;
        active = YamlConfiguration.loadConfiguration(file);
        warnedMissingKeys.clear();

        boolean anyMissing = false;
        for (ConfigData<String> data : registry) {
            anyMissing |= applyValue(data, false);
        }

        if (anyMissing) {
            persistMissingKeys();
        }

        initialized = true;
    }

    private boolean applyValue(@NotNull ConfigData<String> data, boolean persistIfMissing) {
        String value = active.getString(data.getPath());
        if (value == null) {
            if (warnedMissingKeys.add(data.getPath())) {
                plugin.getLogger().warning("Lang key '" + data.getPath() + "' is missing from "
                        + langKey.getValue() + ".yml, using the built-in default.");
            }
            data.setValue(data.getDefaultValue());
            active.set(data.getPath(), data.getDefaultValue());
            if (persistIfMissing) {
                persistMissingKeys();
            }
            return true;
        }

        data.setValue(value);
        return false;
    }

    private void persistMissingKeys() {
        if (activeFile == null) return;
        try {
            active.save(activeFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not write missing lang keys back to "
                    + langKey.getValue() + ".yml: " + e.getMessage());
        }
    }

    @Nullable
    private File resolveLangFile(@NotNull String localeName) {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        File target = new File(langFolder, localeName + ".yml");

        Path folder = langFolder.toPath().toAbsolutePath().normalize();
        Path file = target.toPath().toAbsolutePath().normalize();

        if (!file.startsWith(folder)) {
            plugin.getLogger().severe("Refusing to use unsafe lang file name: " + localeName);
            return null;
        }
        return target;
    }
}
