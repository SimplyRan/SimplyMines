package me.simplyran.simplymines.actions;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ActionFactory {
    IAction create(@NotNull JsonObject json);
}
