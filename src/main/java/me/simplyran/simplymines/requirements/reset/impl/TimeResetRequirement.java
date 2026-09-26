package me.simplyran.simplymines.requirements.reset.impl;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import lombok.Setter;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.requirements.reset.IResetRequirement;

import java.util.List;

public class TimeResetRequirement implements IResetRequirement {

    public final static String NAME = "time_reset_requirement";


    @Getter private long lastReset;
    @Getter @Setter private int resetTime;

    public TimeResetRequirement(int resetTime){
        this.resetTime = resetTime;
    }

    @Override
    public boolean isEnabled(){
        return true;
    }

    @Override
    public void setEnabled(boolean enabled){}




    /** Seconds until this requirement is satisfied, given the current time in epoch seconds. */
    public long secondsUntilReset(long nowSeconds) {
        return resetTime - (nowSeconds - lastReset);
    }

    @Override
    public boolean isSatisfied() {
        return secondsUntilReset(System.currentTimeMillis() / 1000) <= 0;
    }

    @Override
    public void update() {
        lastReset = System.currentTimeMillis() / 1000;
    }

    @Override
    public String getSerializationKey() {
        return NAME;
    }

    @Override
    public List<Pair<String, Object>> serialize() {
        return List.of(Pair.of("reset_time", resetTime));
    }

    public static IResetRequirement deserialize(BasicMine mine, JsonObject json) {
        return new TimeResetRequirement(json.get("reset_time").getAsInt());
    }




}
