package domain;

import java.math.BigDecimal;

import it.unifi.oris.sirio.models.gspn.WeightExpressionFeature;
import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;
import it.unifi.oris.sirio.models.tpn.Priority;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Place;
import it.unifi.oris.sirio.petrinet.Transition;

public class SmartProbabilisticService extends Service{

    
    private Place service;
    private Transition select;
    private Transition complete;
    
    public SmartProbabilisticService(String name){
        
        super(name);
    }
    
    public void add(PetriNet pn, Marking m){
        
        //nodi
        service = pn.addPlace("Service"+ServiceName);
        select = pn.addTransition("Select"+ServiceName);
        complete = pn.addTransition("Complete"+ServiceName);
        
        //transizioni
        pn.addPostcondition(select, service);
        pn.addPrecondition(service, complete);
        
        select.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        select.addFeature(new WeightExpressionFeature("1+Waiting"+ServiceName));
        select.addFeature(new Priority(new Integer("0")));
        
        complete.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        complete.addFeature(new WeightExpressionFeature("1"));
        complete.addFeature(new Priority(new Integer("0")));
        
        m.setTokens(service, 0);
    }


    public Place getService() {
        return service;
    }

    public Transition getSelect() {
        return select;
    }

    public Transition getComplete() {
        return complete;
    }

    @Override
    public Place getPolling() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setGamma(PetriNet net, double gamma) {
        // TODO Auto-generated method stub
        
    }
    
    

}
