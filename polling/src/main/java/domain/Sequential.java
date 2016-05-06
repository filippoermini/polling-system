package domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Formatter;

import application.ApproximateModel;
import application.PetriNetModel;
import it.unifi.oris.sirio.models.stpn.RewardRate;
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
   
    public Sequential(Integer Services, Double gamma) {
        // TODO Auto-generated constructor stub
        super(gamma);
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
        
        SequentialService service = new SequentialService(serviceName,gamma);
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
    @Override
    public BigDecimal getMeanDelay(ArrayList<Results> res, int index, int k, BigDecimal P) {
     // TODO Auto-generated method stub
        BigDecimal di = BigDecimal.ZERO;
        BigDecimal dik = BigDecimal.valueOf(res.get(index).MeanDelayResults[k]);
        di = di.add(dik.multiply(P));
        return di;
    }
    @Override
    public String getOutpuString(int index) {
        // TODO Auto-generated method stub
        return "d_"+index;
    }
    @Override
    public BigDecimal getDi(ApproximateModel pm, ArrayList<Results> res, int index, int numQueue) {
        BigDecimal Di = BigDecimal.ZERO;
        for(int j=0;j<numQueue;j++){
            if(index!=j) 
                Di = Di.add(res.get(j).d_i);
        }
        return Di;
    }
    @Override
    public BigDecimal getWeights(int k, BigDecimal P) {
        // TODO Auto-generated method stub
        return BigDecimal.ZERO;
    }

}
