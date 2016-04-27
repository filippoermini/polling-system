package domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Formatter;

import it.unifi.oris.sirio.math.OmegaBigDecimal;
import it.unifi.oris.sirio.models.gspn.RateExpressionFeature;
import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Place;
import it.unifi.oris.sirio.petrinet.Transition;

public class FixedPriority extends Server{

    
    private Place selectNext;
    private Place ready;
    private Transition move;
    private Place absorbent; 
    
    private int[] prio;
    
    public FixedPriority(int[] prio, Double gamma){
     
        super(gamma);
        this.prio = prio;
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
        //move.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("0"), new OmegaBigDecimal("1"))); 
        
        move.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        move.addFeature(new RateExpressionFeature(Double.toString(gamma)));
    }
    @Override
    public void addService(PetriNet pn, Marking m,int index, String serviceName){
        
        FixedPriorityService service = new FixedPriorityService(serviceName,prio[index]+"");
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
        s.setPolling(polling);;
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
        int Token = res.get(index).MeanDelayResults.length;
        return BigDecimal.valueOf((double) prio[index]/(double)Token);
    }

    @Override
    public String getOutpuString(int index, double delta, BigDecimal md) {
        // TODO Auto-generated method stub
        Formatter formatter = new Formatter();
        String s =  formatter.format("Calcolo di S"+index+"(N) | delta = "+delta+"\n-----------------------------------------\n|\t\tw_%d = %.4f\t\t|\n-----------------------------------------\n",index,md).toString();
        formatter.close();
        return s;
    }

    @Override
    public BigDecimal getDi(ArrayList<Results> res, int index, int numQueue) {
     // TODO Auto-generated method stub
        BigDecimal[] weights = new BigDecimal[numQueue];
        BigDecimal[] meanSojourns = new BigDecimal[numQueue];
        int i=0;
        for(Results r: res){
            weights[i] = r.d_i;
            meanSojourns[i] = BigDecimal.valueOf(r.MeanDelayResults[r.MeanDelayResults.length-1]);
            i++;
        }
        return AbsorptionTime.compute(index, 0.999, weights, meanSojourns);
    }
  
    
   
}