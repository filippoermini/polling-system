package feature_transition;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;

public abstract class TransitionManager {

    private String Type;
   
    public TransitionManager(String type){
        this.Type = type;
    }
    public abstract StochasticTransitionFeature getFeatureTransition();
    public abstract double getTransitionValue();
    public abstract void setParams(String... params);
    
    public String getType(){
        return this.Type;
    }
    
    @SuppressWarnings("unchecked")
    public static TransitionManager getIstance(String... params){
        Class<TransitionManager> c;
        try {
             c = (Class<TransitionManager>) Class.forName("feature_transition."+params[0].toLowerCase());
             Constructor<TransitionManager> cons = c.getConstructor(params.getClass());
             return (TransitionManager) cons.newInstance(new Object[]{params});
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
