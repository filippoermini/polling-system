package domain;

import java.math.BigDecimal;
import java.util.ArrayList;

import application.ApproximateModel;
import application.PetriNetModel;
import application.PollingModel;
import it.unifi.oris.sirio.math.OmegaBigDecimal;
import it.unifi.oris.sirio.models.gspn.RateExpressionFeature;
import it.unifi.oris.sirio.models.stpn.RewardRate;
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
      
    public ExaustiveQueue(String queueName,Integer tokens, Double mu, Double lambda){
        
        super(queueName,tokens,mu,lambda);   
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
        
        //serviceQ.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("1"), new OmegaBigDecimal("3")));
        serviceQ.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        serviceQ.addFeature(new RateExpressionFeature(this.mu+""));
        
        m.setTokens(idle, this.Tokens);
        m.setTokens(waiting, 0);
        
    }

    @Override
    public double getMeanTime(double gamma) {
        
        return 1/gamma+(this.Tokens*1/this.getMu());
    }

    @Override
    public BigDecimal[] getMeanSojourns(ApproximateModel pm, ArrayList<Results> res, double gamma, int numQueue) {
        // TODO Auto-generated method stub
        BigDecimal[] di = new BigDecimal[numQueue];
        for(int i=0;i<numQueue;i++){
            BigDecimal d = BigDecimal.ZERO;
            pm.setParams(Tokens, res.get(i).lambda_i, res.get(i).delta.doubleValue());
            for(int j=0;j<this.Tokens+1;j++){
                RewardRate rw = RewardRate.fromString("If(Waiting"+this.QueueName+"=="+j+",1,0)");
                BigDecimal p = pm.RegenerativeSteadyStateAnalysis(rw).get(rw);
                d = d.add(SojournsTime.compute(j, this.Tokens, res.get(i).lambda_i, mu, 0.999).multiply(p));
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
