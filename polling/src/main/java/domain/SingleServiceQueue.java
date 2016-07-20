package domain;

import java.math.BigDecimal;
import java.util.ArrayList;

import application.ApproximateModel;
import application.PetriNetModel;
import application.PollingModel;
import application.util;
import feature_transition.TransitionManager;
import it.unifi.oris.sirio.math.OmegaBigDecimal;
import it.unifi.oris.sirio.models.gspn.RateExpressionFeature;
import it.unifi.oris.sirio.models.gspn.WeightExpressionFeature;
import it.unifi.oris.sirio.models.pn.PostUpdater;
import it.unifi.oris.sirio.models.stpn.RewardRate;
import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;
import it.unifi.oris.sirio.models.tpn.Priority;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Place;
import it.unifi.oris.sirio.petrinet.Transition;

public class SingleServiceQueue extends Queue{
    
    private Place waiting;
    private Place idle;
    private Transition serviceQ;
    private Transition arrival;
    
    public SingleServiceQueue(String queueName,Integer tokens, TransitionManager mu, Double lambda, Integer K){
        
        super(queueName,tokens,mu,lambda);
    }
        
    
    @Override
    public void add(PetriNet pn, Marking m){
        
        //Generating Nodes
        this.waiting = pn.addPlace("Waiting"+QueueName);
        this.idle = pn.addPlace("Idle"+QueueName);
        this.serviceQ = pn.addTransition("ServiceQ"+QueueName);
        this.arrival = pn.addTransition("Arrival"+QueueName);
        
        //Generating Connectors
        pn.addPrecondition(waiting, serviceQ);
        pn.addPostcondition(serviceQ, idle);
        pn.addPrecondition(idle, arrival);
        pn.addPostcondition(arrival, waiting);
        
        //Generating Properties
        arrival.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        arrival.addFeature(new RateExpressionFeature(lambda+"*Idle"+QueueName));
        
        serviceQ.addFeature(mu.getFeatureTransition());
        
        m.setTokens(idle, this.Tokens);
        m.setTokens(waiting, 0);
    }
    @Override
    public void linkToService(PetriNet pn,Service s) {
       pn.addInhibitorArc(waiting, s.getComplete());
       pn.addPrecondition(s.getService(), serviceQ);
       if(s.getPolling() != null) pn.addPostcondition(serviceQ, s.getPolling());
       else if(pn.getPlace("SelectNext") != null ) pn.addPostcondition(serviceQ, pn.getPlace("SelectNext"));
    }
    @Override
    public void addMeanTime(PetriNet pn, Marking m) {
        // TODO Auto-generated method stub
        this.waiting = pn.addPlace("Waiting"+QueueName);
        this.serviceQ = pn.addTransition("ServiceQ"+QueueName);
        
        pn.addPrecondition(waiting, serviceQ);
        serviceQ.addFeature(mu.getFeatureTransition());
        m.setTokens(waiting, this.Tokens); 
    }
    public Place getWaiting() {
        // TODO Auto-generated method stub
        return this.waiting;
    }

    @Override
    public double getMeanTime(double gamma) {
        // TODO Auto-generated method stub
        if (this.Tokens >= 1)
            return (1/gamma)+(1/this.getMu().getTransitionValue());
        else return (1/gamma)+(this.Tokens*(1/this.getMu().getTransitionValue()));
    }


    @Override
    public BigDecimal[] getMeanSojourns(ApproximateModel.ApproximateNet pm, ArrayList<Results> res, TransitionManager gamma, int numQueue) {
        BigDecimal[] di = new BigDecimal[numQueue];
        for(int i=0;i<numQueue;i++){
            BigDecimal d = BigDecimal.ZERO;
            pm.setParams(Tokens, res.get(i).lambda_i, res.get(i).delta.doubleValue());
            for(int j=0;j<this.Tokens+1;j++){
                RewardRate rw = RewardRate.fromString("If(Waiting"+this.QueueName+"=="+j+",1,0)");
                BigDecimal p = pm.RegenerativeSteadyStateAnalysis(rw).get(rw);
                d = d.add(BigDecimal.valueOf(this.getMeanTime(gamma.getTransitionValue())).multiply(p));
            }
            di[i] = d;
        }
        return di;
    }


    @Override
    public BigDecimal getSojournTime(BigDecimal di, BigDecimal Ni) {
        // TODO Auto-generated method stub
        return di;
    }

}