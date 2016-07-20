package feature_transition;

import java.math.BigDecimal;

import it.unifi.oris.sirio.math.OmegaBigDecimal;
import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;

public class uni extends TransitionManager{

    private double eft;
    private double lft;
    
    public uni(String...params){
        super(params[0]);
        this.eft = Double.parseDouble(params[1]);
        this.lft = Double.parseDouble(params[2]);
    }

    @Override
    public StochasticTransitionFeature getFeatureTransition() {
        return StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal(eft+""), new OmegaBigDecimal(lft+""));
    }

    @Override
    public double getTransitionValue() {
        // TODO Auto-generated method stub
        return (eft + lft)/2;
    }

    @Override
    public void setParams(String... params) {
        this.eft = Double.parseDouble(params[0]);
        this.lft = Double.parseDouble(params[1]);
    }
}
