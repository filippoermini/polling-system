package domain;

import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Place;
import it.unifi.oris.sirio.petrinet.Transition;
import it.unifi.oris.sirio.petrinet.TransitionFeature;

public class Approximate {

    private Place approximate;
    private Transition delta;
    
    public Approximate(){
        
    }
    public void insertApproximation(PetriNet pn, Marking marking){
        
        this.approximate = pn.addPlace("Approximate");
        this.delta = pn.addTransition("Delta");
       
        pn.addPrecondition(approximate, delta);
    }
    
    public Place getPlace(){
        return this.approximate;
    }
    public Transition getTransition(){
        return this.delta;
    }
    public void setFeatures(TransitionFeature feature){
        this.delta.addFeature(feature);
    }
}
