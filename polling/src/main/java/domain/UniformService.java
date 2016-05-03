package domain;

import java.math.BigDecimal;

import it.unifi.oris.sirio.models.gspn.RateExpressionFeature;
import it.unifi.oris.sirio.models.gspn.WeightExpressionFeature;
import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;
import it.unifi.oris.sirio.models.tpn.Priority;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Place;
import it.unifi.oris.sirio.petrinet.Transition;

public class UniformService extends Service{

    
    private Place service;
    private Transition select;
    private Transition complete;
    private Place polling;
    
    public UniformService(String name){
        
        super(name,1);
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
        select.addFeature(new WeightExpressionFeature("1"));
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
        return this.polling;
    }

    @Override
    public void setGamma(double gamma) {
        // TODO Auto-generated method stub
        
        select.removeFeature(StochasticTransitionFeature.class);
        select.removeFeature(WeightExpressionFeature.class);
        select.removeFeature(Priority.class);
           
        select.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        select.addFeature(new RateExpressionFeature(Double.toString(gamma)));
        
    }

    @Override
    public void setPolling(Place polling) {
        // TODO Auto-generated method stub
        this.polling = polling;
    }
    
    

}
