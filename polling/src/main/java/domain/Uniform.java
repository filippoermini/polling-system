package domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Formatter;

import application.ApproximateModel;
import application.PetriNetModel;
import it.unifi.oris.sirio.math.OmegaBigDecimal;
import it.unifi.oris.sirio.models.gspn.RateExpressionFeature;
import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Place;
import it.unifi.oris.sirio.petrinet.Transition;

public class Uniform extends Server{

    
    private Place selectNext;
    private Place ready;
    private Transition move;
    private Place absorbent;
    
    
    public Uniform(Double gamma){
     
        super(gamma);
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
        
        move.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        move.addFeature(new RateExpressionFeature(Double.toString(gamma)));
    }
    @Override
    public void addService(PetriNet pn, Marking m,int index, String serviceName){
        
        SmartProbabilisticService service = new SmartProbabilisticService(serviceName);
        serviceList.add(service);
        service.add(pn, m);
        
        //connessione alla struttura principale
        if(ready != null) pn.addPrecondition(ready, service.getSelect());
        if(selectNext != null) pn.addPostcondition(service.getComplete(), selectNext);
  
     
    }



    @Override
    public void linkApproximate(PetriNet pn, Service s, Approximate a) {
        // TODO Auto-generated method stub
        Place polling = pn.addPlace("PollingAPX");
        s.setPolling(polling);
        pn.addPostcondition(s.getComplete(), a.getPlace());
        pn.addPostcondition(a.getTransition(), polling);
        pn.addPrecondition(polling, s.getSelect());
        if(pn.getPostcondition(pn.getTransition("ServiceQAPX"), s.getService()) == null)
            pn.addPostcondition(pn.getTransition("ServiceQAPX"), a.getPlace());
    }

    @Override
    public void addAbsorbent(PetriNet pn, Service s) {
     // TODO Auto-generated method stub
        if(s.getPolling() == null){
            Place polling = pn.addPlace("PollingMD");
            s.setPolling(polling);
            pn.addPrecondition(polling, s.getSelect());
        }
        this.absorbent = pn.addPlace("Absorbent");
        pn.addPostcondition(s.getComplete(), absorbent);
        if(pn.getPostcondition(pn.getTransition("ServiceQMD"), s.getService()) == null){
            pn.addPostcondition(pn.getTransition("ServiceQMD"), absorbent);
        }
        
    }

    @Override
    public BigDecimal getMeanDelay(ArrayList<Results> res, int index, int k, BigDecimal P) {
        // TODO Auto-generated method stub
    
        return BigDecimal.valueOf(res.get(index).MeanDelayResults[k]).multiply(P);
    }

    @Override
    public String getOutpuString(int index) {
        // TODO Auto-generated method stub
        return "w_"+index;
    }

    @Override
    public BigDecimal getDi(ApproximateModel pm, ArrayList<Results> res, int index, int numQueue) {
        // TODO Auto-generated method stub
        BigDecimal[] weights = new BigDecimal[numQueue];
        BigDecimal[] meanSojourns = new BigDecimal[numQueue];
        int i=0;
        
        for(Results r: res){
            weights[i] = BigDecimal.ONE;
            i++;
        }
        meanSojourns = this.getLast().getQueue().getMeanSojourns(pm, res, i, numQueue);
        return BigDecimal.valueOf(1/AbsorptionTime.compute(index, 0.999, weights, meanSojourns).doubleValue());
    }

    @Override
    public BigDecimal getWeights(int k, BigDecimal P) {
        // TODO Auto-generated method stub
        return BigDecimal.valueOf(k).multiply(P);
    }
  
    
   
}

