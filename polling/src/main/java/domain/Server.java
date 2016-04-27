package domain;

import java.math.BigDecimal;
import java.util.ArrayList;

import application.ApproximateModel;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.PetriNet;

public abstract class Server {

    protected ArrayList<Service> serviceList;
    protected double gamma;
   
    
    public Server(double gamma){
        
        this.serviceList = new ArrayList<Service>();
        this.gamma = gamma;
        
    }
    
    public abstract void create(PetriNet pn, Marking m);
    public abstract void addService(PetriNet pn, Marking m, int index, String serviceName);
    public abstract void linkApproximate(PetriNet pn,Service s, Approximate a);
    public abstract void addAbsorbent(PetriNet pn,Service s);
    public abstract BigDecimal getMeanDelay(ArrayList<Results> res, int index, int k, BigDecimal P);
    public abstract String getOutpuString(int index, double delta, BigDecimal md);
    public abstract BigDecimal getDi(ArrayList<Results> res, int index, int numQueue);
    
    public Service getServiceAtIndex(int index){
        if (!serviceList.isEmpty())
            return serviceList.get(index);
        return null;
    }
    public Service getLast(){
        if(!serviceList.isEmpty())
            return serviceList.get(serviceList.size()-1);
        return null;
    }
}
