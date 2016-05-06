package domain;

import java.math.BigDecimal;

import it.unifi.oris.sirio.analyzer.SuccessionProcessor;
import it.unifi.oris.sirio.math.OmegaBigDecimal;
import it.unifi.oris.sirio.models.gspn.RateExpressionFeature;
import it.unifi.oris.sirio.models.stpn.DeterministicEnablingState;
import it.unifi.oris.sirio.models.stpn.DeterministicEnablingStateBuilder;
import it.unifi.oris.sirio.models.stpn.EnablingSyncsEvaluator;
import it.unifi.oris.sirio.models.stpn.RegenerativeTransientAnalysis;
import it.unifi.oris.sirio.models.stpn.RewardRate;
import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;
import it.unifi.oris.sirio.models.stpn.TransientSolution;
import it.unifi.oris.sirio.models.stpn.policy.TruncationPolicy;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.MarkingCondition;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Place;
import it.unifi.oris.sirio.petrinet.Transition;

public class SojournsTime {
    public static BigDecimal compute(int K, int token, double lambda, double mu, double threshold){
        
        PetriNet pn = new PetriNet();
        Marking m = new Marking();
        
        //Generating Nodes
        Place service = pn.addPlace("Service");
        Transition complete = pn.addTransition("Complete");
        Place finish = pn.addPlace("Finish");
        Place waiting = pn.addPlace("Waiting");
        Transition serviceQ = pn.addTransition("ServiceQ");
        Place idle = pn.addPlace("Idle");
        Transition arrival = pn.addTransition("Arrival");
         
        //Generating Connectors
        pn.addPrecondition(service, complete);
        pn.addPostcondition(complete, finish);
        pn.addPrecondition(service, serviceQ);
        pn.addInhibitorArc(waiting, complete);
        pn.addPostcondition(serviceQ, service);
        pn.addPrecondition(waiting, serviceQ);
        pn.addPostcondition(serviceQ, idle);
        pn.addPrecondition(idle, arrival);
        pn.addPostcondition(arrival, waiting);
        
        //Generating Properties
        complete.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        serviceQ.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        serviceQ.addFeature(new RateExpressionFeature(Double.toString(mu)));
        arrival.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        arrival.addFeature(new RateExpressionFeature("Idle*"+lambda));
        
        m.addTokens(service, 1);
        m.addTokens(waiting, K);
        m.addTokens(idle, token-K);
        
        BigDecimal absorptionTime = null;
        OmegaBigDecimal timeBound = new OmegaBigDecimal(BigDecimal.valueOf(token*(1/mu)).multiply(new BigDecimal(5)));
        BigDecimal step = new BigDecimal("0.1");
        
        while (absorptionTime == null) {
            
            BigDecimal error = BigDecimal.ZERO;
            SuccessionProcessor postProc = new EnablingSyncsEvaluator();
            DeterministicEnablingState initialReg = new DeterministicEnablingState(m, pn);
            
            TransientSolution<DeterministicEnablingState, Marking> solution = RegenerativeTransientAnalysis
                    .compute(pn, initialReg, new DeterministicEnablingStateBuilder(pn, true),
                            postProc, new TruncationPolicy(error, new OmegaBigDecimal(timeBound)), false, false, null, null)
                    .solveDiscretizedMarkovRenewal(timeBound.bigDecimalValue(), step,
                            MarkingCondition.ANY, false, null, null);

            TransientSolution<DeterministicEnablingState, RewardRate> reward = TransientSolution.computeRewards(false, solution, "Finish");
            
            BigDecimal time = BigDecimal.ZERO;
            for (int t=0; t < reward.getSamplesNumber(); t++) {
                //System.out.printf("%.2f %.5f\n", time, reward.getSolution()[t][0][0]);
                if (reward.getSolution()[t][0][0] >= threshold) {
                    absorptionTime = time;
                } else {
                    time = time.add(reward.getStep());
                }
            }
            
            timeBound = timeBound.multiply(new OmegaBigDecimal(2));
        }
        
        return absorptionTime;
        
    }
}
