package me.simplyran.simplymines.requirements.reset.impl;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import lombok.Setter;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.requirements.reset.IResetRequirement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TimeResetRequirement implements IResetRequirement {

    public final static String NAME = "time_reset_requirement";


    private final BasicMine mine;
    private long lastReset;
    @Getter @Setter private int resetTime;

    public TimeResetRequirement(@NotNull BasicMine mine,
                                int resetTime){
        this.mine = mine;
        this.resetTime = resetTime;
    }

    @Override
    public boolean isEnabled(){
        return true;
    }

    @Override
    public void setEnabled(boolean enabled){}




    @Override
    public boolean isSatisfied() {
        long now = System.currentTimeMillis() / 1000;
        long secondsUntilReset = resetTime - (now - lastReset);

        return secondsUntilReset <= 0;
    }

    @Override
    public void update() {
        lastReset = System.currentTimeMillis();
    }

    @Override
    public List<Pair<String, Object>> serialize() {
        return List.of(Pair.of("reset_time", resetTime));
    }

    public static IResetRequirement deserialize(BasicMine mine, JsonObject json) {
        return new TimeResetRequirement(
                mine,
                json.get("reset_time").getAsInt()
        );
    }




}
