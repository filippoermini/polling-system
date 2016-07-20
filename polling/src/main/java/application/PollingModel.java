package application;

import java.util.ArrayList;
import java.util.stream.DoubleStream;

import application.util.queuePolicy;
import application.util.queueSelectionPolicy;
import domain.Queue;
import domain.Server;
import domain.TransitionParams;
import feature_transition.TransitionManager;

public class PollingModel extends PetriNetModel{

    private queueSelectionPolicy serverType;
    private queuePolicy[] queueType;
    private int numQueue;
    private int[] Tokens;
    private int[] K;
    private int[] prio={1,2,3};
    private double[] lambda;
    private double rho;
    private TransitionManager mu;
    private TransitionManager gamma;
    private Server server;
    private ArrayList<Queue> queueList;
    
    
    
    public PollingModel(int numq, int[] tokens, int[] k, int[] prio, queuePolicy[] qp, queueSelectionPolicy qsp, double[] lambda, TransitionManager mu, TransitionManager gamma, double ro) {
        // TODO Auto-generated constructor stub
        super();
        
        this.numQueue = numq;
        this.Tokens = tokens;
        this.K = k;
        this.prio = prio;
        this.serverType = qsp;
        this.queueType = qp;
        this.lambda = lambda;
        this.mu = mu;
        this.gamma = gamma;
        this.rho = ro;
        this.queueList = new ArrayList<>();
        this.build();
    }
    
    @Override
    protected void build() {
        // TODO Auto-generated method stub
        double den = 0;
        for(int h=0;h<lambda.length;h++)
            den += lambda[h]*Tokens[h];
        double l;
        server = Server.getServer(this.serverType,numQueue,prio,gamma);
        server.create(this.Net, this.Marking);
        for (int i=0;i<numQueue;i++){
            //aggiungo un service
            server.addService(this.Net, this.Marking,i, i+"");
            //aggiungo una coda
            l = (this.mu.getTransitionValue() * this.rho)/(den);
            double Lambda = l*this.lambda[i];
            Queue q = Queue.getQueue(this.queueType[i],i+"", this.Tokens[i],mu,Lambda,this.K[i]);
            q.add(this.Net, this.Marking);
            queueList.add(q);
            server.getLast().setQueue(q);
            q.linkToService(this.Net, server.getLast());
        }    
    }
    
    
}
