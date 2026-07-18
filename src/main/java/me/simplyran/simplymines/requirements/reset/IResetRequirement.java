package me.simplyran.simplymines.requirements.reset;

import it.unimi.dsi.fastutil.Pair;

import java.util.List;

public interface IResetRequirement {


     boolean isSatisfied();

     void update();

     boolean isEnabled();

     void setEnabled(boolean enabled);

     //List of values.
     //left is Property
     //right is value
     List<Pair<String, Object>> serialize();

}
