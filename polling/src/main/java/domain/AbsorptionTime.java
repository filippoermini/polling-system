package domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.stream.Stream;

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
import it.unifi.oris.sirio.models.tpn.Priority;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.MarkingCondition;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Place;
import it.unifi.oris.sirio.petrinet.Transition;

public class AbsorptionTime {
    public static BigDecimal compute(int target, double threshold, BigDecimal[] weights, BigDecimal[] meanSojourns) {
        if (weights.length != meanSojourns.length)
            throw new IllegalArgumentException("Non-matching lengths for weights and sojourns");
        
        PetriNet pn = new PetriNet();
        
        Place start = pn.addPlace("start");
        for (int i = 0; i < weights.length; i++) {
            Transition select = pn.addTransition("select"+i);
            select.addFeature(StochasticTransitionFeature.newDeterministicInstance(BigDecimal.ZERO, weights[i]));
            Place p = pn.addPlace("p"+i);
            pn.addPrecondition(start, select);
            pn.addPostcondition(select, p);
            
            if (i != target) {
                Transition sojourn = pn.addTransition("sojourn"+i);
                Double time = (meanSojourns[i].doubleValue());
                sojourn.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal(time), new BigDecimal("1")));
                sojourn.addFeature(new Priority(new Integer("0")));
                pn.addPrecondition(p, sojourn);
                pn.addPostcondition(sojourn, start);
            }
        }

        Marking m = new Marking();
        m.addTokens(start, 1);
       
        
        BigDecimal sojournsSum = BigDecimal.ZERO;
        // BigDecimal sojournsMin = null;
        for (int i = 0; i < meanSojourns.length; i ++) {
            if (i != target) {
                sojournsSum = sojournsSum.add(meanSojourns[i]);
            }
            // sojournsMin = sojournsMin == null ? x : sojournsMin.min(x);
        }
        
        BigDecimal absorptionTime = null;
        OmegaBigDecimal timeBound = new OmegaBigDecimal(sojournsSum.multiply(new BigDecimal(20)));
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

            TransientSolution<DeterministicEnablingState, RewardRate> reward = TransientSolution.computeRewards(false, solution, "p"+target);
            
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
    
    //public static void main(String[] args) {
    //    BigDecimal[] weights = new BigDecimal[] { new BigDecimal("0.9"), new BigDecimal("0.1") };
    //    BigDecimal[] meanSojourns = new BigDecimal[] { new BigDecimal("10"), new BigDecimal("10") };
    //    System.out.println(compute(1, 0.999, weights, meanSojourns));
    //}
}
