package domain;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;

import application.ApproximateModel;
import application.PetriNetModel;
import application.PollingModel;
import application.util;
import feature_transition.TransitionManager;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Place;

public abstract class Queue {
    
    protected double lambda;
    protected TransitionManager mu;
    protected double gamma;
    protected String QueueName;
    protected int Tokens;
    
    public Queue(String name,int Tokens,double lambda){
        
        this.QueueName = name;
        this.Tokens = Tokens;
        this.lambda = lambda;
        this.mu = TransitionManager.getIstance(new String[]{"EXP","1.25"});
        
        
    }
    
    public Queue(String name,int Tokens,TransitionManager mu, double lambda){
        
        this.QueueName = name;
        this.Tokens = Tokens;
        this.lambda = lambda;
        this.mu = mu;
        
    }
   
  
    public abstract void add(PetriNet pn, Marking m);
    public abstract void addMeanTime(PetriNet pn,Marking m);
    public abstract void linkToService(PetriNet pn,Service s);
    public abstract double getMeanTime(double gamma);
    public abstract BigDecimal getSojournTime(BigDecimal di, BigDecimal Ni);
    public abstract BigDecimal[] getMeanSojourns(ApproximateModel.ApproximateNet pm, ArrayList<Results> res, TransitionManager gamma, int numQueue);
    
    public abstract Place getWaiting();
    
    
    public double getLabda(){
        return this.lambda;
    }
    
    public TransitionManager getMu(){
        return mu;
    }
    public void setTokens(int tokens){
        this.Tokens = tokens;
    }
    public void setLambda(double lambda){
        this.lambda = lambda;
    }
    
    public static Queue getQueue(util.queuePolicy queueType, Object... params){
        Class c;
        try {
            String className = queueType.getClassName();
            Class[] paramType = new Class[params.length];
            Object[] param = new Object[params.length];
            int index = 0;
            for (Object p: params){
                paramType[index] = p.getClass().toString().contains("feature_transition")?p.getClass().getSuperclass():p.getClass();
                param[index] = p;
                index++;
            }
            c = Class.forName("domain."+className);
            Constructor cons = c.getConstructor(paramType);
            return (Queue) cons.newInstance(param);
        } catch (ClassNotFoundException  | 
                NoSuchMethodException    | 
                SecurityException        | 
                InstantiationException   | 
                IllegalAccessException   | 
                IllegalArgumentException | 
                InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
        
    }
    

}
