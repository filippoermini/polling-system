package domain;

import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Place;

public abstract class Queue {
    
    protected double lambda;
    protected double mu;
    protected double gamma;
    protected String QueueName;
    protected int Tokens;
    
    public Queue(String name,int Tokens,double lambda){
        
        this.QueueName = name;
        this.Tokens = Tokens;
        this.lambda = lambda;
        this.mu = 1.25;
        this.gamma = 294.12;
        
    }
    
    public Queue(String name,int Tokens,double lambda, double mu, double gamma){
        
        this.QueueName = name;
        this.Tokens = Tokens;
        this.lambda = lambda;
        this.mu = mu;
        this.gamma = gamma; 
        
    }
   
  
    public abstract void add(PetriNet pn, Marking m);
    public abstract void addMeanTime(PetriNet pn,Marking m);
    public abstract void linkToService(PetriNet pn,Service s);
    public abstract double getMeanDelay();
    
    public abstract Place getWaiting();
    
    public double getGamma(){
        return this.gamma;
    }
    
    public double getLabda(){
        return this.lambda;
    }
    
    public double getMu(){
        return this.mu;
    }
    public void setTokens(int tokens){
        this.Tokens = tokens;
    }
    

}
