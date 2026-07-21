package me.simplyran.simplymines.factories;

import me.simplyran.simplymines.objects.ConfigData;

public class ConfigFactory {

    private ConfigFactory() {}

    public static <T> ConfigData<T> newConfigData(String path, T defaultValue) {
        return new ConfigData<>(path, defaultValue);
    }

}
