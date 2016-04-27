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

public class SingleServiceQueue extends Queue{
    
    
    
    
    private Place waiting;
    private Place idle;
    private Place arrived;
    private Transition serviceQ;
    private Transition arrival;
    
    public SingleServiceQueue(String queueName, Integer tokens, Double mu, Double lambda){
        
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
        
        serviceQ.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        serviceQ.addFeature(new RateExpressionFeature(this.mu+""));
        
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
        serviceQ.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        serviceQ.addFeature(new RateExpressionFeature(this.mu+""));
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
        if (this.Tokens >= 1)
            return (1/gamma)+(1/this.getMu());
        else return (1/gamma)+(this.Tokens*(1/this.getMu()));
    }

}