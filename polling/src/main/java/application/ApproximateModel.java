package application;

import java.math.BigDecimal;

import application.util.queuePolicy;
import application.util.queueSelectionPolicy;
import domain.Approximate;
import domain.Queue;
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
   
    
    public ApproximateModel(String name, int k, queueSelectionPolicy qsp, queuePolicy qp ) {
        // TODO Auto-generated constructor stub
        super();
        
        this.K = k;
        this.serverType = qsp;
        this.queueType = qp;
        this.queueName = name;
        
        this.build();
    }
    
    public ApproximateModel(String name, queueSelectionPolicy qsp, queuePolicy qp) {
        // TODO Auto-generated constructor stub
        super();
        
        this.K = 0;
        this.serverType = qsp;
        this.queueType = qp;
        this.queueName = name;
        
        this.build();
    }
    
    @Override
    protected void build() {
        // TODO Auto-generated method stub
        
        TransitionFeature initialDelta = StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1"));
        
        Server sp = getServerType(this.serverType,1);
        Approximate apx = new Approximate();
        sp.create(this.Net, this.Marking);
        sp.addService(this.Net, this.Marking, 0, this.queueName);
        apx.insertApproximation(this.Net, this.Marking);
        apx.setFeatures(initialDelta);
        Queue q = getQueue(this.queueType,this.queueName,1,0.1);
        q.add(this.Net, this.Marking);
        q.linkToService(this.Net, sp.getLast()); 
        sp.linkApproximate(this.Net, sp.getLast(), apx);
        
        //set Tokens
       
        this.Marking.setTokens(sp.getLast().getPolling(), 1);
        this.Marking.setTokens(sp.getLast().getService(), 0);
        
    }
    public void setTokens(int tokens){
        this.Marking.setTokens("Idle"+this.queueName, tokens);
    }
    public void setDelta(double delta){
        Transition t = this.Net.getTransition("Delta");
        if (t.hasFeature(StochasticTransitionFeature.class)){
            t.removeFeature(StochasticTransitionFeature.class);
            t.removeFeature(RateExpressionFeature.class);
            
        }
        t.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        t.addFeature(new RateExpressionFeature(delta+""));
    }
    public void setLambda(double lambda) {
        // TODO Auto-generated method stub
        Transition t = this.Net.getTransition("ArrivalAPX");
        if (t.hasFeature(StochasticTransitionFeature.class)){
            t.removeFeature(StochasticTransitionFeature.class);
            t.removeFeature(RateExpressionFeature.class);
        }
        
        t.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        t.addFeature(new RateExpressionFeature(lambda+"*IdleAPX"));

        
    }

}
