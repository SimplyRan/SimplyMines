package me.simplyran.simplymines.objects;

import org.bukkit.Location;

public interface IMine {


    BoxedRegion getRegion();


    String getName();


    int getResetTime();


    long getLastReset();

    void setLastReset(long lastReset);


    void reset();


    boolean isInsideMine(Location location);



}
