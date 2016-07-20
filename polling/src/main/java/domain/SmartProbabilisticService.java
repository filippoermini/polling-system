package domain;

import java.math.BigDecimal;

import application.util;
import feature_transition.TransitionManager;
import it.unifi.oris.sirio.models.gspn.RateExpressionFeature;
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
    private Place polling;
    
    public SmartProbabilisticService(String name){
        
        super(name,TransitionManager.getIstance(new String[]{"EXP","1"}));
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
        return this.polling;
    }

    @Override
    public void setGamma(TransitionManager gamma) {
        // TODO Auto-generated method stub
        
        select.removeFeature(StochasticTransitionFeature.class);
        select.removeFeature(WeightExpressionFeature.class);
        select.removeFeature(Priority.class);
           
        select.addFeature(gamma.getFeatureTransition());
    }

    @Override
    public void setPolling(Place polling) {
        // TODO Auto-generated method stub
        this.polling = polling;
    }
    
    

}
