package me.simplyran.simplymines.objects;

import org.bukkit.Location;

public interface IMine {


    BoxedRegion getRegion();
    void setRegion(BoxedRegion region);


    String getName();


    int getResetTime();


    void setResetTime(int time);


    long getLastReset();

    void setLastReset(long lastReset);


    void reset();

    boolean isEnabled();

    void setEnabled(boolean enabled);


    boolean isInsideMine(Location location);

    Location getTeleportLocation();
    void setTeleportLocation(Location location);



}
