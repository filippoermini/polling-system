package domain;

import java.math.BigDecimal;

import it.unifi.oris.sirio.models.gspn.RateExpressionFeature;
import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Place;
import it.unifi.oris.sirio.petrinet.Transition;

public abstract class Service {

    protected String ServiceName;
    protected double gamma;
    protected Queue queue;
    
    public Service(String name, double gamma){
        this.ServiceName = name;
        this.gamma = gamma;
    }
    public void setQueue(Queue q){
        this.queue = q;
    }
    public Queue getQueue(){
        return this.queue;
    }
    public abstract void add(PetriNet pn, Marking m);
    public abstract Place getService();
    public abstract Transition getComplete();
    public abstract Transition getSelect();
    public abstract Place getPolling();
    public abstract void setGamma(double gamma);
    public abstract void setPolling(Place polling);
    
       
}
