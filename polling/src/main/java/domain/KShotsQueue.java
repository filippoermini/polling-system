package domain;

import java.math.BigDecimal;

import it.unifi.oris.sirio.math.OmegaBigDecimal;
import it.unifi.oris.sirio.models.gspn.RateExpressionFeature;
import it.unifi.oris.sirio.models.gspn.WeightExpressionFeature;
import it.unifi.oris.sirio.models.pn.PostUpdater;
import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;
import it.unifi.oris.sirio.models.tpn.Priority;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Place;
import it.unifi.oris.sirio.petrinet.Transition;

public class KShotsQueue extends Queue{
    
    
    private int K;
    
    private Place waiting;
    private Place idle;
    private Place arrived;
    private Transition serviceQ;
    private Transition arrival;
    
    public KShotsQueue(String queueName,int tokens, double lambda, int K){
        
        super(queueName,tokens,lambda);
        this.K = K;
        
    }
    
    public KShotsQueue(String queueName,Integer tokens, Double lambda, Integer K){
        
        super(queueName,tokens,(double)lambda);
        this.K = K;
        
    }
    
    public KShotsQueue(String queueName,Integer tokens, Double lambda, Integer K, double mu, double gamma){
        
        super(queueName,tokens,(double)lambda,mu,gamma);
        this.K = K;
        
    }

    public KShotsQueue(String queueName, Double lambda){
        
        super(queueName,0,(double)lambda);
        this.K = 0;
        
    }
    public KShotsQueue(String queueName, Integer K, Double lambda){
        
        super(queueName,0,(double)lambda);
        this.K = K;
        
    }
    public KShotsQueue(String queueName, Double lambda, Integer K){
        
        super(queueName,0,(double)lambda);
        this.K = K;
        
    }
    @Override
    public void add(PetriNet pn, Marking m){
        
        //Generating Nodes
        this.waiting = pn.addPlace("Waiting"+QueueName);
        this.idle = pn.addPlace("Idle"+QueueName);
        this.arrived = pn.addPlace("Arrived"+QueueName);
        this.serviceQ = pn.addTransition("ServiceQ"+QueueName);
        this.arrival = pn.addTransition("Arrival"+QueueName);
        
        //Generating Connectors
        pn.addPrecondition(waiting, serviceQ);
        pn.addPostcondition(serviceQ, idle);
        pn.addPrecondition(idle, arrival);
        pn.addPostcondition(arrival, arrived);
        
        //Generating Properties
        arrival.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        arrival.addFeature(new RateExpressionFeature(lambda+"*Idle"+QueueName));
        
        serviceQ.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        serviceQ.addFeature(new RateExpressionFeature(this.mu+""));
        
        m.setTokens(idle, this.Tokens);
        m.setTokens(waiting, 0);
    }
    @Override
    public void linkToService(PetriNet pn,Service s) {
       pn.addInhibitorArc(waiting, s.getComplete());
       pn.addPrecondition(s.getService(), serviceQ);
       pn.addPostcondition(serviceQ, s.getService());
       String condition = "Waiting"+QueueName+"=If(Arrived"+QueueName+"<"+K+",Arrived"+QueueName+","+K+");Arrived"+QueueName+"=Arrived"+QueueName+"-If(Arrived"+QueueName+"<"+K+",Arrived"+QueueName+","+K+");";
       //System.out.println(condition);
       try{
           s.getSelect().addFeature(new PostUpdater(condition, pn));
       }catch(IllegalArgumentException ex){}
       try{
           s.getSelect().addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
           s.getSelect().addFeature(new RateExpressionFeature("1"));
       }catch(IllegalArgumentException ex){}
    }
    @Override
    public void addMeanTime(PetriNet pn, Marking m) {
        // TODO Auto-generated method stub
        this.waiting = pn.addPlace("Waiting"+QueueName);
        this.serviceQ = pn.addTransition("ServiceQ"+QueueName);
        
        pn.addPrecondition(waiting, serviceQ);
        serviceQ.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("1"), new OmegaBigDecimal("3")));
        m.setTokens(waiting, this.Tokens); 
    }
    public Place getWaiting() {
        // TODO Auto-generated method stub
        return this.waiting;
    }

    @Override
    public double getMeanDelay() {
        // TODO Auto-generated method stub
        if (this.Tokens >= this.K)
            return (1/this.getGamma())+(this.K*(1/this.getMu()));
        else return (1/this.getGamma())+(this.Tokens*(1/this.getMu()));
    }

}
