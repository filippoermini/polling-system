package domain;

import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Place;
import it.unifi.oris.sirio.petrinet.Precondition;
import it.unifi.oris.sirio.petrinet.Transition;

public class Sequential extends Server{

    private Transition Head;
    private Place Tail;
    private int numServices;
    private Place absorbent;
   
    public Sequential(int Services) {
        // TODO Auto-generated constructor stub
        super();
        Head = null;
        Tail = null;
        numServices = Services;
    }
    @Override
    public void create(PetriNet pn, Marking m) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addService(PetriNet pn, Marking m,int index, String serviceName) {
        // TODO Auto-generated method stub
        
        SequentialService service = new SequentialService(serviceName);
        serviceList.add(service);
        service.add(pn,m);
        
        
        if (serviceList.size() == 1) {
            this.Head = service.getSelect();
            m.setTokens(service.getService(),1);
            m.setTokens(service.getPolling(), 0);
        }
        else {
            pn.addPrecondition(Tail, service.getSelect());
            m.setTokens(service.getService(),0);
            m.setTokens(service.getPolling(), 0);
        }
        this.Tail = service.getPolling();
        if (serviceList.size() == this.numServices)
            pn.addPrecondition(Tail, Head);
        
   
        
    }
    @Override
    public void linkApproximate(PetriNet pn,Service s, Approximate a) {
        // TODO Auto-generated method stub
        pn.removePostcondition(pn.getPostcondition(s.getComplete(), s.getPolling()));
        pn.addPostcondition(s.getComplete(), a.getPlace());
        pn.addPostcondition(a.getTransition(), s.getPolling());
        
        if(pn.getPostcondition(pn.getTransition("ServiceQAPX"),s.getPolling()) != null){
            pn.removePostcondition(pn.getPostcondition(pn.getTransition("ServiceQAPX"),s.getPolling()));
            pn.addPostcondition(pn.getTransition("ServiceQAPX"), a.getPlace());
        }
        //pn.addPrecondition(s.getPolling(), s.getSelect());
        
    }
    @Override
    public void addAbsorbent(PetriNet pn, Service s) {
        // TODO Auto-generated method stub
        this.absorbent = pn.addPlace("Absorbent");
        pn.removePostcondition(pn.getPostcondition(s.getComplete(), s.getPolling()));
        pn.addPostcondition(s.getComplete(), absorbent);
        
    }

}
