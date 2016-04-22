package domain;

import java.util.ArrayList;

import it.unifi.oris.sirio.math.OmegaBigDecimal;
import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Place;
import it.unifi.oris.sirio.petrinet.Transition;

public class SmartProbabilistic extends Server{

    
    private Place selectNext;
    private Place ready;
    private Transition move;
    
    
    public SmartProbabilistic(){
     
        super();
    }
    
    @Override
    public void create(PetriNet pn,Marking m){
        
        //Generating Nodes
        selectNext = pn.addPlace("SelectNext");
        ready = pn.addPlace("Ready");
        move = pn.addTransition("Move");
        
        //Generating Transition
        pn.addPrecondition(selectNext, move);
        pn.addPostcondition(move, ready);
        
        //Generating Properties
        m.setTokens(selectNext, 1);
        m.setTokens(ready, 0);
        move.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("0"), new OmegaBigDecimal("1"))); 
    }
    @Override
    public void addService(PetriNet pn, Marking m,int index, String serviceName){
        
        SmartProbabilisticService service = new SmartProbabilisticService(serviceName);
        serviceList.add(service);
        service.add(pn, m);
        
        //connessione alla struttura principale
        pn.addPrecondition(ready, service.getSelect());
        pn.addPostcondition(service.getComplete(), selectNext);
  
     
    }



    @Override
    public void linkApproximate(PetriNet pn, Service s, Approximate a) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addAbsorbent(PetriNet pn, Service s) {
        // TODO Auto-generated method stub
        
    }
  
    
   
}