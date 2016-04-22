package application;

import application.util.queuePolicy;
import application.util.queueSelectionPolicy;
import domain.Queue;
import domain.Server;
import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;
import it.unifi.oris.sirio.petrinet.Transition;

public class MeanDelayModel extends PetriNetModel{

    
    
    private queueSelectionPolicy serverType;
    private queuePolicy queueType;
    private int Tokens;
    private Queue queue;
    private Server server;
    private int K;

    public MeanDelayModel(util.queueSelectionPolicy qsp, util.queuePolicy qp, int K) {
        // TODO Auto-generated constructor stub
        super();
        this.serverType = qsp;
        this.queueType = qp; 
        this.Tokens = 0;
        this.queue = null;
        this.server = null;
        this.K = K;
        build();
    }
    
    @Override
    protected void build() {
        // TODO Auto-generated method stub
        this.server = getServerType(serverType,1);
        this.server.create(this.Net, this.Marking);
        this.server.addService(this.Net, this.Marking, 0, "MD");
        this.queue = getQueue(this.queueType, "MD",0.1,this.K);
        this.queue.addMeanTime(this.Net, this.Marking);
        this.queue.linkToService(this.Net, this.server.getLast());
        this.server.addAbsorbent(this.Net, this.server.getLast());
        
        //inizializzo tutti i token
        this.Marking.setTokens(this.server.getLast().getService(), 0);
        this.Marking.setTokens(this.queue.getWaiting(), 0);
        this.Marking.setTokens(this.server.getLast().getPolling(), 1);
    }
    
    public void setMeanDelayTokens(int tokens){
        
        this.queue.setTokens(tokens);
        this.Marking.setTokens(this.queue.getWaiting(),tokens);
    }
    
    public double getMeanTimeDelay(){
        return this.queue.getMeanDelay();
    }


}