package domain;

import java.math.BigDecimal;
import it.unifi.oris.sirio.math.OmegaBigDecimal;
import it.unifi.oris.sirio.models.gspn.RateExpressionFeature;
import it.unifi.oris.sirio.models.pn.PostUpdater;
import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Place;
import it.unifi.oris.sirio.petrinet.Transition;

public class OnlyPresentAtArrivalQueue extends Queue{
    
    
    private Place waiting;
    private Place idle;
    private Place arrived;
    private Transition serviceQ;
    private Transition arrival;
    
    public OnlyPresentAtArrivalQueue(String queueName, Integer tokens, Double mu, Double lambda){
        
        super(queueName,tokens,mu,lambda);
   
    }
    @Override
    public void add(PetriNet pn, Marking m){
        
        //Generating Nodes
        this.waiting = pn.addPlace("Waiting"+QueueName);
        this.idle = pn.addPlace("Idle"+QueueName);
        this.arrived = pn.addPlace("Arrived"+QueueName);
        this.serviceQ = pn.addTransition("ServiceQ"+QueueName);
        this.arrival = pn.addTransition("Arrival"+QueueName);
        

        pn.addPrecondition(waiting, serviceQ);
        pn.addPostcondition(serviceQ, idle);
        pn.addPrecondition(idle, arrival);
        pn.addPostcondition(arrival, arrived);
       
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
       String condition = "Waiting"+QueueName+"=Arrived"+QueueName+",Arrived"+QueueName+"=0";
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
    public double getMeanTime(Server server) {
        // TODO Auto-generated method stub
        double gamma = server.getLast().getGamma();
        return 1/gamma+(this.Tokens*1/this.getMu());
    }

}
