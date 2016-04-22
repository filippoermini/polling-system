package domain;

import java.math.BigDecimal;
import it.unifi.oris.sirio.math.OmegaBigDecimal;
import it.unifi.oris.sirio.models.gspn.RateExpressionFeature;
import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Place;
import it.unifi.oris.sirio.petrinet.Transition;

public class ExaustiveQueue extends Queue{
    
    
    private Place waiting;
    private Place idle;
    private Transition serviceQ;
    private Transition arrival;
    
    public ExaustiveQueue(String queueName,int tokens, double lambda){
        
        super(queueName,tokens,lambda);
        
    }
    
    public ExaustiveQueue(String queueName,Integer tokens, Double lambda){
        
        super(queueName,tokens,(double)lambda);
        
    }
    
    public ExaustiveQueue(String queueName,Integer tokens, Double lambda, double mu, double gamma){
        
        super(queueName,tokens,(double)lambda,mu,gamma);
        
    }
    
    @Override
    public void linkToService(PetriNet pn,Service s) {
       pn.addInhibitorArc(waiting, s.getComplete());
       pn.addPrecondition(s.getService(), serviceQ);
       pn.addPostcondition(serviceQ, s.getService());
        
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
    @Override
    public Place getWaiting() {
        // TODO Auto-generated method stub
        return this.waiting;
    }
    @Override
    public void add(PetriNet pn, Marking m) {
        // TODO Auto-generated method stub
        
            
        //Generating Nodes
        this.waiting = pn.addPlace("Waiting"+QueueName);
        this.idle = pn.addPlace("Idle"+QueueName);
        this.serviceQ = pn.addTransition("ServiceQueue"+QueueName);
        this.arrival = pn.addTransition("Arrival"+QueueName);
        
        //Generating Connectors
        pn.addPrecondition(waiting, serviceQ);
        pn.addPostcondition(serviceQ, idle);
        pn.addPrecondition(idle, arrival);
        pn.addPostcondition(arrival, waiting);
        
        //Generating Properties
        arrival.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        arrival.addFeature(new RateExpressionFeature(lambda+"*Idle"+QueueName));
        
        //serviceQ.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("1"), new OmegaBigDecimal("3")));
        serviceQ.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        serviceQ.addFeature(new RateExpressionFeature(this.mu+""));
        
        m.setTokens(idle, this.Tokens);
        m.setTokens(waiting, 0);
        
    }

    @Override
    public double getMeanDelay() {
        // TODO Auto-generated method stub
        return 1/this.getGamma()+(this.Tokens*1/this.getMu());
    }
    
    


}
