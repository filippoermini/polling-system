package domain;

import java.util.ArrayList;

import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.PetriNet;

public abstract class Server {

    protected ArrayList<Service> serviceList;
   
    
    public Server(){
        
        this.serviceList = new ArrayList<Service>();
        
    }
    
    public abstract void create(PetriNet pn, Marking m);
    public abstract void addService(PetriNet pn, Marking m, int index,String serviceName);
    public abstract void linkApproximate(PetriNet pn,Service s, Approximate a);
    public abstract void addAbsorbent(PetriNet pn,Service s);
    
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
