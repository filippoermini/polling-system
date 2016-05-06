package application;

import java.math.BigDecimal;

import application.util.queuePolicy;
import application.util.queueSelectionPolicy;
import domain.Approximate;
import domain.Queue;
import domain.Results;
import domain.Server;
import it.unifi.oris.sirio.models.gspn.RateExpressionFeature;
import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Transition;
import it.unifi.oris.sirio.petrinet.TransitionFeature;

public class ApproximateModel extends PetriNetModel{

   
    private int K;
    private queueSelectionPolicy serverType;
    private queuePolicy queueType;
    private String queueName;
    private Server server;
    private Queue queue;
    private Approximate apx;
    private double delta; 
    private int tokens;
    private double mu;
    private double gamma;
    private int[] prio;
   
    
    public ApproximateModel(String name, queueSelectionPolicy qsp, queuePolicy qp, int k, int[] prio, double mu, double gamma) {
        // TODO Auto-generated constructor stub
        super();
        
        this.K = k;
        this.serverType = qsp;
        this.queueType = qp;
        this.queueName = name;
        this.tokens = 0;
        this.gamma = gamma;
        this.mu = mu;
        this.prio = prio;
        this.build();
    }
    
    
    @Override
    protected void build() {
        // TODO Auto-generated method stub
        
        TransitionFeature initialDelta = StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1"));
        
        server = getServerType(this.serverType,1,prio,gamma);
        apx = new Approximate();
        //server.create(this.Net, this.Marking);
        server.addService(this.Net, this.Marking, 0, this.queueName);
        server.getLast().setGamma(gamma);
        apx.insertApproximation(this.Net, this.Marking);
        apx.setFeatures(initialDelta);
        queue = getQueue(this.queueType,this.queueName,1,this.mu,1.0,1);
        queue.add(this.Net, this.Marking);
        queue.linkToService(this.Net, server.getLast()); 
        server.linkApproximate(this.Net, server.getLast(), apx);
        server.getLast().setQueue(queue);;
        
        //set Tokens
       
        this.Marking.setTokens(server.getLast().getPolling(), 1);
        this.Marking.setTokens(server.getLast().getService(), 0);
        
    }
    public void setParams(int tokens, double lambda, double delta){
        this.setTokens(tokens);
        this.setDelta(delta);
        this.setLambda(lambda);
    }
    public void setTokens(int tokens){
        this.Marking.setTokens("Idle"+this.queueName, tokens);
        this.queue.setTokens(tokens);
        this.tokens = tokens;
    }
    public void setDelta(double delta){
        Transition t = this.Net.getTransition("Delta");
        if (t.hasFeature(StochasticTransitionFeature.class)){
            t.removeFeature(StochasticTransitionFeature.class);
            t.removeFeature(RateExpressionFeature.class);
            
        }
        t.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        t.addFeature(new RateExpressionFeature(delta+""));
        this.delta = delta;
    }
    public void setLambda(double lambda) {
        // TODO Auto-generated method stub
        Transition t = this.Net.getTransition("Arrival"+this.queueName);
        if (t.hasFeature(StochasticTransitionFeature.class)){
            t.removeFeature(StochasticTransitionFeature.class);
            t.removeFeature(RateExpressionFeature.class);
        }
        
        t.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        t.addFeature(new RateExpressionFeature(lambda+"*Idle"+this.queueName));
        queue.setLambda(lambda);
    }
    
    public Server getServer(){
        return this.server;

    }

}
