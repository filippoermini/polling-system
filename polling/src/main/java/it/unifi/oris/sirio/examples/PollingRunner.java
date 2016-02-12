package it.unifi.oris.sirio.examples;

import java.math.BigDecimal;

import it.unifi.oris.sirio.analyzer.policy.FIFOPolicy;
import it.unifi.oris.sirio.math.OmegaBigDecimal;
import it.unifi.oris.sirio.models.gspn.RateExpressionFeature;
import it.unifi.oris.sirio.models.gspn.WeightExpressionFeature;
import it.unifi.oris.sirio.models.stpn.DeterministicEnablingState;
import it.unifi.oris.sirio.models.stpn.DeterministicEnablingStateBuilder;
import it.unifi.oris.sirio.models.stpn.EnablingSyncsEvaluator;
import it.unifi.oris.sirio.models.stpn.RewardRate;
import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;
import it.unifi.oris.sirio.models.stpn.steadystate.RegenerativeSteadyStateAnalysis;
import it.unifi.oris.sirio.models.stpn.steadystate.SteadyStateInitialStateBuilder;
import it.unifi.oris.sirio.models.stpn.steadystate.SteadyStatePostProcessor;
import it.unifi.oris.sirio.models.stpn.steadystate.SteadyStateSolution;
import it.unifi.oris.sirio.models.tpn.Priority;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.MarkingCondition;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Place;
import it.unifi.oris.sirio.petrinet.Transition;

public class PollingRunner {
    
      public static void build(PetriNet net, Marking marking) {

        //Generating Nodes
        Place p0 = net.addPlace("p0");
        Place p1 = net.addPlace("p1");
        Place p11 = net.addPlace("p11");
        Place p2 = net.addPlace("p2");
        Transition t0 = net.addTransition("t0");
        Transition t11 = net.addTransition("t11");
        Transition t3 = net.addTransition("t3");
        Transition t4 = net.addTransition("t4");

        //Generating Connectors
        net.addInhibitorArc(p1, t4);
        net.addPrecondition(p2, t0);
        net.addPostcondition(t4, p11);
        net.addPrecondition(p11, t11);
        net.addPostcondition(t3, p0);
        net.addPostcondition(t0, p1);
        net.addPrecondition(p2, t0);
        net.addPrecondition(p0, t4);
        net.addPostcondition(t3, p2);
        net.addPrecondition(p1, t3);
        net.addPrecondition(p0, t3);
        net.addPostcondition(t3, p2);
        net.addPostcondition(t11, p0);
        net.addPostcondition(t11, p0);

        //Generating Properties
        marking.setTokens(p0, 1);
        marking.setTokens(p1, 0);
        marking.setTokens(p11, 0);
        marking.setTokens(p2, 3);
        t0.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        t0.addFeature(new RateExpressionFeature("0.1*p2"));
        t11.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        t11.addFeature(new RateExpressionFeature("0.106960951 "));
        t3.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("1"), new OmegaBigDecimal("3")));
        t4.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        t4.addFeature(new WeightExpressionFeature("1"));
        t4.addFeature(new Priority(new Integer("0")));
    }

    public static void main(String[] args) {
        
        PetriNet pn = new PetriNet();
        Marking m = new Marking();
        
        build(pn, m);
        
        MarkingCondition stopCondition = MarkingCondition.NONE;

        
        SteadyStateInitialStateBuilder<DeterministicEnablingState> sb = 
                new SteadyStateInitialStateBuilder<>(new DeterministicEnablingStateBuilder(pn, false));
        
        RegenerativeSteadyStateAnalysis<DeterministicEnablingState> analysis = 
                    RegenerativeSteadyStateAnalysis.compute(
                            pn, 
                            new DeterministicEnablingState(m, pn), 
                            sb, new SteadyStatePostProcessor(new EnablingSyncsEvaluator()), 
                            new FIFOPolicy(),
                            stopCondition, 
                            false, 
                            false, 
                            null, 
                            null, 
                            true);
            
        SteadyStateSolution<Marking> steadyStateProbs = analysis.getSteadyState();
        SteadyStateSolution<RewardRate> steadyStateRewards = SteadyStateSolution.computeRewards(steadyStateProbs, "p0");
        System.out.println(steadyStateRewards.getSteadyState());
    }
}
