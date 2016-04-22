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
import it.unifi.oris.sirio.petrinet.TransitionFeature;

public class SequentialService extends Service{

    private Transition select;
    private Transition complete;
    private Place service;
    private Place polling;
    
    public SequentialService(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void add(PetriNet pn, Marking m) {
        // TODO Auto-generated method stub
        select = pn.addTransition("Select"+ServiceName);
        complete = pn.addTransition("Complete"+ServiceName);
        service = pn.addPlace("Service"+ServiceName);
        polling = pn.addPlace("Polling"+ServiceName);
        
        pn.addPostcondition(select, service);
        pn.addPrecondition(service, complete);
        pn.addPostcondition(complete, polling);
        
        
        complete.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        complete.addFeature(new WeightExpressionFeature("1"));
        complete.addFeature(new Priority(new Integer("0")));
     
        select.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        select.addFeature(new RateExpressionFeature("294.12"));
    }

    @Override
    public Place getService() {
        // TODO Auto-generated method stub
        return service;
    }

    @Override
    public Transition getComplete() {
        // TODO Auto-generated method stub
        return complete;
    }
    
    public Transition getSelect(){
        return select;
    }
    
    public Place getPolling(){
        return polling;
    }

    @Override
    public void setGamma(PetriNet net, double gamma) {
        // TODO Auto-generated method stub
        Transition t = net.getTransition("Select"+this.ServiceName);
        if (t.hasFeature(StochasticTransitionFeature.class)){
            t.removeFeature(StochasticTransitionFeature.class);
            t.removeFeature(RateExpressionFeature.class);
            
        }
        t.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        t.addFeature(new RateExpressionFeature(gamma+""));
    }

}
