package feature_transition;

import java.math.BigDecimal;

import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;

public class det extends TransitionManager{

    private BigDecimal value;
    private BigDecimal weight;
    
    public det(String... params){
        super(params[0]);
        this.value = new BigDecimal(params[1]);
        this.weight = new BigDecimal(params[2]);
    }

    @Override
    public StochasticTransitionFeature getFeatureTransition() {
        // TODO Auto-generated method stub
        return StochasticTransitionFeature.newDeterministicInstance(value,weight);
    }

    @Override
    public double getTransitionValue() {
        // TODO Auto-generated method stub
        return value.doubleValue();
    }

    @Override
    public void setParams(String... params) {
        this.value = new BigDecimal(params[0]);
        this.weight = new BigDecimal(params[1]);
        
    }
    
}
