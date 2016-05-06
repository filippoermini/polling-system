package it.unifi.oris.sirio.examples;

import java.awt.EventQueue;
import application.*;
import java.math.BigDecimal;

import javax.swing.JFrame;
import javax.swing.JPanel;

import application.util;
import domain.ExaustiveQueue;
import domain.Sequential;
import domain.SequentialService;
import it.unifi.oris.sirio.analyzer.Analyzer;
import it.unifi.oris.sirio.analyzer.SuccessionProcessor;
import it.unifi.oris.sirio.analyzer.graph.SuccessionGraph;
import it.unifi.oris.sirio.analyzer.graph.SuccessionGraphViewer;
import it.unifi.oris.sirio.analyzer.log.AnalysisMonitor;
import it.unifi.oris.sirio.analyzer.policy.FIFOPolicy;
import it.unifi.oris.sirio.math.OmegaBigDecimal;
import it.unifi.oris.sirio.models.gspn.RateExpressionFeature;
import it.unifi.oris.sirio.models.gspn.WeightExpressionFeature;
import it.unifi.oris.sirio.models.pn.PostUpdater;
import it.unifi.oris.sirio.models.stpn.DeterministicEnablingState;
import it.unifi.oris.sirio.models.stpn.DeterministicEnablingStateBuilder;
import it.unifi.oris.sirio.models.stpn.EnablingSyncsEvaluator;
import it.unifi.oris.sirio.models.stpn.RegenerativeTransientAnalysis;
import it.unifi.oris.sirio.models.stpn.RewardRate;
import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;
import it.unifi.oris.sirio.models.stpn.TransientSolution;
import it.unifi.oris.sirio.models.stpn.policy.TruncationPolicy;
import it.unifi.oris.sirio.models.stpn.steadystate.RegenerativeSteadyStateAnalysis;
import it.unifi.oris.sirio.models.stpn.steadystate.SteadyStateInitialStateBuilder;
import it.unifi.oris.sirio.models.stpn.steadystate.SteadyStatePostProcessor;
import it.unifi.oris.sirio.models.stpn.steadystate.SteadyStateSolution;
import it.unifi.oris.sirio.models.tpn.Priority;
import it.unifi.oris.sirio.models.tpn.TimedComponentsFactory;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.MarkingCondition;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Place;
import it.unifi.oris.sirio.petrinet.Transition;

public class PollingRunnerExport {
    
      public static void build(PetriNet net, Marking marking) {


          //Generating Nodes
          Place start = net.addPlace("start");
          Place p1 = net.addPlace("p1");
          Place p2 = net.addPlace("p2");
          Place p3 = net.addPlace("p3");
          Transition select1 = net.addTransition("select1");
          Transition select2 = net.addTransition("select2");
          Transition select3 = net.addTransition("select3");
          Transition sojourn2 = net.addTransition("sojourn2");
          Transition sojourn3 = net.addTransition("sojourn3");

          //Generating Connectors
          net.addPrecondition(start, select1);
          net.addPostcondition(select1, p1);
          net.addPrecondition(start, select2);
          net.addPostcondition(select2, p2);
          net.addPrecondition(p2, sojourn2);
          net.addPostcondition(sojourn2, start);
          net.addPrecondition(start, select3);
          net.addPostcondition(select3, p3);
          net.addPrecondition(p3, sojourn3);
          net.addPostcondition(sojourn3, start);

          //Generating Properties
          marking.setTokens(start, 1);
          marking.setTokens(p1, 0);
          marking.setTokens(p2, 0);
          marking.setTokens(p3, 0);
          select1.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1.11647")));
          select2.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1.17426")));
          select3.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1.20302")));
          sojourn2.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("048332993704313174931908037024186342023313045501708984375"), new BigDecimal("1")));
          sojourn3.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0260871630592369556367327021462187985889613628387451171875"), new BigDecimal("1")));
        }

    public static void main(String[] args) {
        
        PetriNet pn = new PetriNet();
        Marking m = new Marking();
        
        build(pn, m);
        
        
        
        BigDecimal absorptionTime = null;
        OmegaBigDecimal timeBound = new OmegaBigDecimal("17");
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

            TransientSolution<DeterministicEnablingState, RewardRate> reward = TransientSolution.computeRewards(false, solution, "p1");
            
            BigDecimal time = BigDecimal.ZERO;
            for (int t=0; t < reward.getSamplesNumber(); t++) {
                System.out.printf("%.2f %.5f\n", time, reward.getSolution()[t][0][0]);
                if (reward.getSolution()[t][0][0] >= 0.999) {
                    absorptionTime = time;
                } else {
                    time = time.add(reward.getStep());
                }
            }
            
            timeBound = timeBound.multiply(new OmegaBigDecimal(2));
        }
        
        System.out.println(absorptionTime);;
       
    }
    
}