package application;

import java.util.ArrayList;
import java.util.stream.DoubleStream;

import application.util.queuePolicy;
import application.util.queueSelectionPolicy;
import domain.Queue;
import domain.Server;

public class PollingModel extends PetriNetModel{

    private queueSelectionPolicy serverType;
    private queuePolicy queueType;
    private int numQueue;
    private int[] Tokens;
    private int K;
    private int[] prio={1,2,3};
    private double[] lambda;
    private double ro;
    private double mu;
    private double gamma;
    private Server server;
    private ArrayList<Queue> queueList;
    
    
    
    public PollingModel(int numq, int[] tokens, int k, int[] prio, queuePolicy qp, queueSelectionPolicy qsp, double[] lambda, double mu, double gamma, double ro) {
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
        this.ro = ro;
        this.queueList = new ArrayList<>();
        this.build();
    }
    
    @Override
    protected void build() {
        // TODO Auto-generated method stub
        double lambdaSum = DoubleStream.of(lambda).sum();
        double l = 1;
        server = getServerType(this.serverType,numQueue,prio,gamma);
        server.create(this.Net, this.Marking);
        for (int i=0;i<numQueue;i++){
            //aggiungo un service
            server.addService(this.Net, this.Marking,i, i+"");
            //aggiungo una coda
            if (ro != 0) l = (this.mu * this.ro)/(this.Tokens[i]*lambdaSum);
            Queue q = getQueue(this.queueType,i+"", this.Tokens[i],mu,l*this.lambda[i],this.K);
            q.add(this.Net, this.Marking);
            queueList.add(q);
            server.getLast().setQueue(q);
            q.linkToService(this.Net, server.getLast());
        }    
    }
    
    
}
