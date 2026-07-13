package me.simplyran.simplymines.objects;

import org.bukkit.Location;

public interface IMine {


    BoxedRegion getRegion();


    String getName();


    int getResetTime();


    void setResetTime(int time);


    long getLastReset();

    void setLastReset(long lastReset);


    void reset();

    boolean isEnabled();

    void setEnabled(boolean enabled);


    boolean isInsideMine(Location location);



}
