package me.simplyran.simplymines.objects;

public class ConfigFactory {

    private ConfigFactory() {}

    public static <T> ConfigData<T> newConfigData(String path, T defaultValue) {
        return new ConfigData<>(path, defaultValue);
    }

}
