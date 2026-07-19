package me.simplyran.simplymines.objects;


import lombok.Getter;
import lombok.Setter;

@Getter
public class ConfigData<T> {

    @Setter private T value;

    private final T defaultValue;

    private final String path;

    public ConfigData(String path, T defaultValue){
        this.defaultValue = defaultValue;
        this.path = path;
        this.value = defaultValue;
    }

    @Override
    public String toString(){
        if (value != null) return value.toString();
        else return defaultValue.toString();
    }

}
