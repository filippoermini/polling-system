package application;

import java.util.stream.DoubleStream;

import application.util.queuePolicy;
import application.util.queueSelectionPolicy;
import domain.Queue;
import domain.Server;

public class PollingModel extends PetriNetModel{

    private int numQueue;
    private int[] Tokens;
    private int K;
    private int[] prio={1,2,3};
    private queueSelectionPolicy serverType;
    private queuePolicy queueType;
    private double[] lambda;
    private double ro;
    private double mu;
    private double gamma;
    
    
    public PollingModel(int numq, int[] tokens, int k, int[] prio, queuePolicy qp, queueSelectionPolicy qsp, double[] lambda, double mu, double ro, double gamma) {
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
        this.ro = ro;
        
        this.build();
    }
    public PollingModel(int numq, int[] tokens, queuePolicy qp, queueSelectionPolicy qsp, double[] lambda) {
        // TODO Auto-generated constructor stub
        this.numQueue = numq;
        this.Tokens = tokens;
        this.K = 0;
        this.prio = null;
        this.serverType = qsp;
        this.queueType = qp;
        this.lambda = lambda;
        this.mu = 1;
        this.ro = 0;
        
        this.build();
    }
    @Override
    protected void build() {
        // TODO Auto-generated method stub
        double lambdaSum = DoubleStream.of(lambda).sum();
        double l = 1;
        Server sp = getServerType(this.serverType, numQueue);
        sp.create(this.Net, this.Marking);
        for (int i=0;i<numQueue;i++){
            //aggiungo un service
            sp.addService(this.Net, this.Marking,i, i+"");
            //aggiungo una coda
            if (ro != 0) l = (this.mu * this.ro)/(this.Tokens[i]*lambdaSum);
            Queue q = getQueue(this.queueType,i+"", this.Tokens[i],l*this.lambda[i],this.K);
            q.add(this.Net, this.Marking);
            q.linkToService(this.Net, sp.getLast());
        }    
    }
    
    
}
