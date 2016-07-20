package feature_transition;

import java.math.BigDecimal;

import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;

public class exp extends TransitionManager{

    private BigDecimal lambda;
    public exp(String... params) {
        super(params[0]);
        this.lambda = new BigDecimal(params[1]);
    }
    @Override
    public StochasticTransitionFeature getFeatureTransition() {
        return StochasticTransitionFeature.newExponentialInstance(lambda);
    }
    @Override
    public double getTransitionValue() {
        return lambda.doubleValue();
    }
    @Override
    public void setParams(String... params) {
        this.lambda = new BigDecimal(params[0]);
        
    }

}
