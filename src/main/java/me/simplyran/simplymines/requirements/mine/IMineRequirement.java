package me.simplyran.simplymines.requirements.mine;

import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface IMineRequirement {

    @Getter List<Class<? extends IMineRequirement>> allMineMineRequirement = new ArrayList<>();

    static void addNewRequirement(Class<? extends IMineRequirement> clazz){
        allMineMineRequirement.add(clazz);
    }

    boolean isSatisfied(@NotNull Player player);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    //List of values.
    //left is Property
    //right is value
    List<Pair<String, Object>> serialize();


}
