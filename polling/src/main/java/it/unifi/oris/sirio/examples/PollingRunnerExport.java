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
import it.unifi.oris.sirio.models.stpn.RewardRate;
import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;
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
          Place Approximate = net.addPlace("Approximate");
          Place Idle = net.addPlace("Idle");
          Place Polling = net.addPlace("Polling");
          Place Service = net.addPlace("Service");
          Place Waiting = net.addPlace("Waiting");
          Transition Arrival = net.addTransition("Arrival");
          Transition Complete = net.addTransition("Complete");
          Transition Delta = net.addTransition("Delta");
          Transition Select = net.addTransition("Select");
          Transition ServiceQ = net.addTransition("ServiceQ");

          //Generating Connectors
          net.addPrecondition(Idle, Arrival);
          net.addPostcondition(Arrival, Waiting);
          net.addPrecondition(Waiting, ServiceQ);
          net.addPostcondition(ServiceQ, Idle);
          net.addPostcondition(ServiceQ, Idle);
          net.addPostcondition(ServiceQ, Idle);
          net.addPostcondition(ServiceQ, Idle);
          net.addInhibitorArc(Waiting, Complete);
          net.addPrecondition(Service, Complete);
          net.addPrecondition(Service, ServiceQ);
          net.addPostcondition(ServiceQ, Approximate);
          net.addPostcondition(Complete, Approximate);
          net.addPrecondition(Approximate, Delta);
          net.addPrecondition(Polling, Select);
          net.addPostcondition(Select, Service);
          net.addPostcondition(Select, Service);
          net.addPostcondition(Select, Service);
          net.addPostcondition(Select, Service);
          net.addPostcondition(Delta, Polling);

          //Generating Properties
          marking.setTokens(Approximate, 0);
          marking.setTokens(Idle, 3);
          marking.setTokens(Polling, 1);
          marking.setTokens(Service, 0);
          marking.setTokens(Waiting, 0);
          Arrival.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
          Arrival.addFeature(new RateExpressionFeature("0.023809523809523808"));
          Complete.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
          Complete.addFeature(new WeightExpressionFeature("1"));
          Complete.addFeature(new Priority(new Integer("0")));
          Delta.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
          Delta.addFeature(new RateExpressionFeature("1.2447"));
          Select.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
          Select.addFeature(new RateExpressionFeature("294.12"));
          ServiceQ.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
          ServiceQ.addFeature(new RateExpressionFeature("1.25"));
        }

    public static void main(String[] args) {
        
        PetriNet pn = new PetriNet();
        Marking m = new Marking();
        
        build(pn, m);
        System.out.println(pn);
        for (Transition t: pn.getTransitions()) {
            System.out.println(t.getName());
            StochasticTransitionFeature f = t.getFeature(StochasticTransitionFeature.class);
            System.out.println(f.getFiringTimeDensity().getDomain());
            System.out.println(f.getFiringTimeDensity().getDensity());
            RateExpressionFeature rf = t.getFeature(RateExpressionFeature.class);
            if(rf!=null)System.out.println(rf.getExpression());
            System.out.println("-----------");
        }
        //util.showGraph(util.nonDeterministicAnalysis(pn, m, true, MarkingCondition.NONE));
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
        SteadyStateSolution<RewardRate> steadyStateRewards = SteadyStateSolution.computeRewards(steadyStateProbs, "Waiting");
        System.out.println(steadyStateRewards.getSteadyState());
        
       
    }
    
    public static void smartProbabilisticOnlyPresentAtArrival(PetriNet net, Marking marking){
      //Generating Nodes
        Place Arrived1 = net.addPlace("Arrived1");
        Place Arrived2 = net.addPlace("Arrived2");
        Place Arrived3 = net.addPlace("Arrived3");
        Place Idle1 = net.addPlace("Idle1");
        Place Idle2 = net.addPlace("Idle2");
        Place Idle3 = net.addPlace("Idle3");
        Place Ready = net.addPlace("Ready");
        Place SelectNext = net.addPlace("SelectNext");
        Place Service1 = net.addPlace("Service1");
        Place Service2 = net.addPlace("Service2");
        Place Service3 = net.addPlace("Service3");
        Place Waiting1 = net.addPlace("Waiting1");
        Place Waiting2 = net.addPlace("Waiting2");
        Place Waiting3 = net.addPlace("Waiting3");
        Transition arrival1 = net.addTransition("arrival1");
        Transition arrival2 = net.addTransition("arrival2");
        Transition arrival3 = net.addTransition("arrival3");
        Transition complete1 = net.addTransition("complete1");
        Transition complete2 = net.addTransition("complete2");
        Transition complete3 = net.addTransition("complete3");
        Transition move = net.addTransition("move");
        Transition select1 = net.addTransition("select1");
        Transition select2 = net.addTransition("select2");
        Transition select3 = net.addTransition("select3");
        Transition service1 = net.addTransition("service1");
        Transition service2 = net.addTransition("service2");
        Transition service3 = net.addTransition("service3");

        //Generating Connectors
        net.addInhibitorArc(Waiting1, complete1);
        net.addInhibitorArc(Waiting2, complete2);
        net.addInhibitorArc(Waiting3, complete3);
        net.addPrecondition(Service1, service1);
        net.addPrecondition(Ready, select2);
        net.addPostcondition(move, Ready);
        net.addPrecondition(Ready, select3);
        net.addPostcondition(complete1, SelectNext);
        net.addPrecondition(Ready, select2);
        net.addPostcondition(service1, Service1);
        net.addPrecondition(Idle3, arrival3);
        net.addPostcondition(service1, Idle1);
        net.addPostcondition(service3, Service3);
        net.addPrecondition(Service2, service2);
        net.addPostcondition(complete3, SelectNext);
        net.addPostcondition(service2, Idle2);
        net.addPrecondition(Service2, complete2);
        net.addPostcondition(complete1, SelectNext);
        net.addPostcondition(complete1, SelectNext);
        net.addPrecondition(Ready, select3);
        net.addPrecondition(Waiting2, service2);
        net.addPrecondition(Idle1, arrival1);
        net.addPostcondition(service2, Idle2);
        net.addPostcondition(service3, Idle3);
        net.addPostcondition(select2, Service2);
        net.addPostcondition(complete2, SelectNext);
        net.addPrecondition(Service3, complete3);
        net.addPrecondition(SelectNext, move);
        net.addPostcondition(service2, Service2);
        net.addPrecondition(Service3, service3);
        net.addPrecondition(Ready, select1);
        net.addPostcondition(complete2, SelectNext);
        net.addPostcondition(select1, Service1);
        net.addPrecondition(Service1, complete1);
        net.addPrecondition(Waiting1, service1);
        net.addPostcondition(complete2, SelectNext);
        net.addPostcondition(select3, Service3);
        net.addPostcondition(service1, Idle1);
        net.addPrecondition(Idle2, arrival2);
        net.addPostcondition(complete3, SelectNext);
        net.addPostcondition(service3, Idle3);
        net.addPrecondition(Ready, select2);
        net.addPrecondition(Waiting3, service3);
        net.addPostcondition(arrival1, Arrived1);
        net.addPostcondition(arrival2, Arrived2);
        net.addPostcondition(arrival3, Arrived3);

        //Generating Properties
        marking.setTokens(Arrived1, 0);
        marking.setTokens(Arrived2, 0);
        marking.setTokens(Arrived3, 0);
        marking.setTokens(Idle1, 2);
        marking.setTokens(Idle2, 2);
        marking.setTokens(Idle3, 2);
        marking.setTokens(Ready, 0);
        marking.setTokens(SelectNext, 1);
        marking.setTokens(Service1, 0);
        marking.setTokens(Service2, 0);
        marking.setTokens(Service3, 0);
        marking.setTokens(Waiting1, 0);
        marking.setTokens(Waiting2, 0);
        marking.setTokens(Waiting3, 0);
        arrival1.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        arrival1.addFeature(new RateExpressionFeature("0.1*Idle1"));
        arrival2.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        arrival2.addFeature(new RateExpressionFeature("0.1*Idle2"));
        arrival3.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        arrival3.addFeature(new RateExpressionFeature("0.1*Idle3"));
        complete1.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        complete1.addFeature(new WeightExpressionFeature("1"));
        complete1.addFeature(new Priority(new Integer("0")));
        complete2.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        complete2.addFeature(new WeightExpressionFeature("1"));
        complete2.addFeature(new Priority(new Integer("0")));
        complete3.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        complete3.addFeature(new WeightExpressionFeature("1"));
        complete3.addFeature(new Priority(new Integer("0")));
        move.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("0"), new OmegaBigDecimal("1")));
        select1.addFeature(new PostUpdater("Waiting1=Arrived1,Arrived1=0", net));
        select1.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        select1.addFeature(new WeightExpressionFeature("1+Waiting1"));
        select1.addFeature(new Priority(new Integer("0")));
        select2.addFeature(new PostUpdater("Waiting2=Arrived2,Arrived2=0", net));
        select2.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        select2.addFeature(new WeightExpressionFeature("1+Waiting2"));
        select2.addFeature(new Priority(new Integer("0")));
        select3.addFeature(new PostUpdater("Waiting3=Arrived3,Arrived3=0", net));
        select3.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        select3.addFeature(new WeightExpressionFeature("1+Waiting3"));
        select3.addFeature(new Priority(new Integer("0")));
        service1.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("1"), new OmegaBigDecimal("3")));
        service2.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("1"), new OmegaBigDecimal("3")));
        service3.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("1"), new OmegaBigDecimal("3")));
        
    }
    public static void smartProbabilisticKShots(PetriNet net, Marking marking,int K){
      //Generating Nodes
        Place Arrived1 = net.addPlace("Arrived1");
        Place Arrived2 = net.addPlace("Arrived2");
        Place Arrived3 = net.addPlace("Arrived3");
        Place Idle1 = net.addPlace("Idle1");
        Place Idle2 = net.addPlace("Idle2");
        Place Idle3 = net.addPlace("Idle3");
        Place Ready = net.addPlace("Ready");
        Place SelectNext = net.addPlace("SelectNext");
        Place Service1 = net.addPlace("Service1");
        Place Service2 = net.addPlace("Service2");
        Place Service3 = net.addPlace("Service3");
        Place Waiting1 = net.addPlace("Waiting1");
        Place Waiting2 = net.addPlace("Waiting2");
        Place Waiting3 = net.addPlace("Waiting3");
        Transition arrival1 = net.addTransition("arrival1");
        Transition arrival2 = net.addTransition("arrival2");
        Transition arrival3 = net.addTransition("arrival3");
        Transition complete1 = net.addTransition("complete1");
        Transition complete2 = net.addTransition("complete2");
        Transition complete3 = net.addTransition("complete3");
        Transition move = net.addTransition("move");
        Transition select1 = net.addTransition("select1");
        Transition select2 = net.addTransition("select2");
        Transition select3 = net.addTransition("select3");
        Transition service1 = net.addTransition("service1");
        Transition service2 = net.addTransition("service2");
        Transition service3 = net.addTransition("service3");

        //Generating Connectors
        net.addInhibitorArc(Waiting1, complete1);
        net.addInhibitorArc(Waiting2, complete2);
        net.addInhibitorArc(Waiting3, complete3);
        net.addPrecondition(Service1, service1);
        net.addPrecondition(Ready, select2);
        net.addPostcondition(move, Ready);
        net.addPrecondition(Ready, select3);
        net.addPostcondition(complete1, SelectNext);
        net.addPrecondition(Ready, select2);
        net.addPostcondition(service1, Service1);
        net.addPrecondition(Idle3, arrival3);
        net.addPostcondition(service1, Idle1);
        net.addPostcondition(service3, Service3);
        net.addPrecondition(Service2, service2);
        net.addPostcondition(complete3, SelectNext);
        net.addPostcondition(service2, Idle2);
        net.addPrecondition(Service2, complete2);
        net.addPostcondition(complete1, SelectNext);
        net.addPostcondition(complete1, SelectNext);
        net.addPrecondition(Ready, select3);
        net.addPrecondition(Waiting2, service2);
        net.addPrecondition(Idle1, arrival1);
        net.addPostcondition(service2, Idle2);
        net.addPostcondition(service3, Idle3);
        net.addPostcondition(select2, Service2);
        net.addPostcondition(complete2, SelectNext);
        net.addPrecondition(Service3, complete3);
        net.addPrecondition(SelectNext, move);
        net.addPostcondition(service2, Service2);
        net.addPrecondition(Service3, service3);
        net.addPrecondition(Ready, select1);
        net.addPostcondition(complete2, SelectNext);
        net.addPostcondition(select1, Service1);
        net.addPrecondition(Service1, complete1);
        net.addPrecondition(Waiting1, service1);
        net.addPostcondition(complete2, SelectNext);
        net.addPostcondition(select3, Service3);
        net.addPostcondition(service1, Idle1);
        net.addPrecondition(Idle2, arrival2);
        net.addPostcondition(complete3, SelectNext);
        net.addPostcondition(service3, Idle3);
        net.addPrecondition(Ready, select2);
        net.addPrecondition(Waiting3, service3);
        net.addPostcondition(arrival1, Arrived1);
        net.addPostcondition(arrival2, Arrived2);
        net.addPostcondition(arrival3, Arrived3);

        //Generating Properties
        marking.setTokens(Arrived1, 0);
        marking.setTokens(Arrived2, 0);
        marking.setTokens(Arrived3, 0);
        marking.setTokens(Idle1, 2);
        marking.setTokens(Idle2, 2);
        marking.setTokens(Idle3, 2);
        marking.setTokens(Ready, 0);
        marking.setTokens(SelectNext, 1);
        marking.setTokens(Service1, 0);
        marking.setTokens(Service2, 0);
        marking.setTokens(Service3, 0);
        marking.setTokens(Waiting1, 0);
        marking.setTokens(Waiting2, 0);
        marking.setTokens(Waiting3, 0);
        arrival1.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        arrival1.addFeature(new RateExpressionFeature("0.1*Idle1"));
        arrival2.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        arrival2.addFeature(new RateExpressionFeature("0.1*Idle2"));
        arrival3.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        arrival3.addFeature(new RateExpressionFeature("0.1*Idle3"));
        complete1.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        complete1.addFeature(new WeightExpressionFeature("1"));
        complete1.addFeature(new Priority(new Integer("0")));
        complete2.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        complete2.addFeature(new WeightExpressionFeature("1"));
        complete2.addFeature(new Priority(new Integer("0")));
        complete3.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        complete3.addFeature(new WeightExpressionFeature("1"));
        complete3.addFeature(new Priority(new Integer("0")));
        move.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("0"), new OmegaBigDecimal("1")));
        String condition = "Waiting1=If(Arrived1<"+K+",Arrived1,"+K+");Arrived1=Arrived1-If(Arrived1<"+K+",Arrived1,"+K+");";
        System.out.println(condition);
        select1.addFeature(new PostUpdater(condition , net));
        select1.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        select1.addFeature(new WeightExpressionFeature("1+Waiting1"));
        select1.addFeature(new Priority(new Integer("0")));
        select2.addFeature(new PostUpdater("Waiting2=If(Arrived2<"+K+",Arrived2,"+K+");Arrived2=Arrived2-If(Arrived2<"+K+",Arrived2,"+K+");", net));
        select2.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        select2.addFeature(new WeightExpressionFeature("1+Waiting2"));
        select2.addFeature(new Priority(new Integer("0")));
        select3.addFeature(new PostUpdater("Waiting3=If(Arrived3<"+K+",Arrived3,"+K+");Arrived3=Arrived3-If(Arrived3<"+K+",Arrived3,"+K+");", net));
        select3.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        select3.addFeature(new WeightExpressionFeature("1+Waiting3"));
        select3.addFeature(new Priority(new Integer("0")));
        service1.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("1"), new OmegaBigDecimal("3")));
        service2.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("1"), new OmegaBigDecimal("3")));
        service3.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("1"), new OmegaBigDecimal("3")));
    }
    public static void smartProbabilisticExhaustive(PetriNet net, Marking marking){
      //Generating Nodes
        Place Idle1 = net.addPlace("Idle1");
        Place Idle2 = net.addPlace("Idle2");
        Place Idle3 = net.addPlace("Idle3");
        Place Ready = net.addPlace("Ready");
        Place SelectNext = net.addPlace("SelectNext");
        Place Service1 = net.addPlace("Service1");
        Place Service2 = net.addPlace("Service2");
        Place Service3 = net.addPlace("Service3");
        Place Waiting1 = net.addPlace("Waiting1");
        Place Waiting2 = net.addPlace("Waiting2");
        Place Waiting3 = net.addPlace("Waiting3");
        Transition arrival1 = net.addTransition("arrival1");
        Transition arrival2 = net.addTransition("arrival2");
        Transition arrival3 = net.addTransition("arrival3");
        Transition complete1 = net.addTransition("complete1");
        Transition complete2 = net.addTransition("complete2");
        Transition complete3 = net.addTransition("complete3");
        Transition move = net.addTransition("move");
        Transition select1 = net.addTransition("select1");
        Transition select2 = net.addTransition("select2");
        Transition select3 = net.addTransition("select3");
        Transition service1 = net.addTransition("service1");
        Transition service2 = net.addTransition("service2");
        Transition service3 = net.addTransition("service3");

        //Generating Connectors
        net.addInhibitorArc(Waiting1, complete1);
        net.addInhibitorArc(Waiting2, complete2);
        net.addInhibitorArc(Waiting3, complete3);
        net.addPrecondition(Service1, service1);
        net.addPrecondition(Ready, select2);
        net.addPostcondition(move, Ready);
        net.addPrecondition(Idle3, arrival3);
        net.addPrecondition(Ready, select3);
        net.addPostcondition(complete1, SelectNext);
        net.addPrecondition(Ready, select2);
        net.addPostcondition(service1, Service1);
        net.addPrecondition(Idle3, arrival3);
        net.addPostcondition(service1, Idle1);
        net.addPostcondition(service3, Service3);
        net.addPrecondition(Service2, service2);
        net.addPostcondition(complete3, SelectNext);
        net.addPostcondition(service2, Idle2);
        net.addPrecondition(Service2, complete2);
        net.addPostcondition(complete1, SelectNext);
        net.addPostcondition(complete1, SelectNext);
        net.addPrecondition(Idle2, arrival2);
        net.addPrecondition(Ready, select3);
        net.addPrecondition(Waiting2, service2);
        net.addPrecondition(Idle1, arrival1);
        net.addPostcondition(service2, Idle2);
        net.addPostcondition(service3, Idle3);
        net.addPostcondition(select2, Service2);
        net.addPostcondition(complete2, SelectNext);
        net.addPostcondition(arrival1, Waiting1);
        net.addPrecondition(Service3, complete3);
        net.addPrecondition(SelectNext, move);
        net.addPostcondition(service2, Service2);
        net.addPrecondition(Service3, service3);
        net.addPrecondition(Ready, select1);
        net.addPostcondition(complete2, SelectNext);
        net.addPostcondition(select1, Service1);
        net.addPrecondition(Service1, complete1);
        net.addPrecondition(Waiting1, service1);
        net.addPostcondition(complete2, SelectNext);
        net.addPostcondition(select3, Service3);
        net.addPostcondition(service1, Idle1);
        net.addPostcondition(arrival2, Waiting2);
        net.addPrecondition(Idle2, arrival2);
        net.addPostcondition(complete3, SelectNext);
        net.addPostcondition(service3, Idle3);
        net.addPrecondition(Idle1, arrival1);
        net.addPostcondition(arrival3, Waiting3);
        net.addPrecondition(Ready, select2);
        net.addPrecondition(Waiting3, service3);

        //Generating Properties
        marking.setTokens(Idle1, 2);
        marking.setTokens(Idle2, 2);
        marking.setTokens(Idle3, 2);
        marking.setTokens(Ready, 0);
        marking.setTokens(SelectNext, 1);
        marking.setTokens(Service1, 0);
        marking.setTokens(Service2, 0);
        marking.setTokens(Service3, 0);
        marking.setTokens(Waiting1, 0);
        marking.setTokens(Waiting2, 0);
        marking.setTokens(Waiting3, 0);
        arrival1.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        arrival1.addFeature(new RateExpressionFeature("1*Idle1"));
        arrival2.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        arrival2.addFeature(new RateExpressionFeature("0.5*Idle2"));
        arrival3.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        arrival3.addFeature(new RateExpressionFeature("0.25*Idle3"));
        complete1.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        complete1.addFeature(new WeightExpressionFeature("1"));
        complete1.addFeature(new Priority(new Integer("0")));
        complete2.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        complete2.addFeature(new WeightExpressionFeature("1"));
        complete2.addFeature(new Priority(new Integer("0")));
        complete3.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        complete3.addFeature(new WeightExpressionFeature("1"));
        complete3.addFeature(new Priority(new Integer("0")));
        move.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("0"), new OmegaBigDecimal("1")));
        select1.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        select1.addFeature(new WeightExpressionFeature("1+Waiting1"));
        select1.addFeature(new Priority(new Integer("0")));
        select2.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        select2.addFeature(new WeightExpressionFeature("1+Waiting2"));
        select2.addFeature(new Priority(new Integer("0")));
        select3.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        select3.addFeature(new WeightExpressionFeature("1+Waiting3"));
        select3.addFeature(new Priority(new Integer("0")));
        service1.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("1"), new OmegaBigDecimal("3")));
        service2.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("1"), new OmegaBigDecimal("3")));
        service3.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("1"), new OmegaBigDecimal("3")));
    }
    public static void sequentialOnlyPresentAtArrival(PetriNet net, Marking marking){
        //Generating Nodes
        Place arrived0 = net.addPlace("arrived0");
        Place arrived1 = net.addPlace("arrived1");
        Place arrived2 = net.addPlace("arrived2");
        Place finish0 = net.addPlace("finish0");
        Place finish1 = net.addPlace("finish1");
        Place finish2 = net.addPlace("finish2");
        Place idle0 = net.addPlace("idle0");
        Place idle1 = net.addPlace("idle1");
        Place idle2 = net.addPlace("idle2");
        Place service0 = net.addPlace("service0");
        Place service1 = net.addPlace("service1");
        Place service2 = net.addPlace("service2");
        Place waiting0 = net.addPlace("waiting0");
        Place waiting1 = net.addPlace("waiting1");
        Place waiting2 = net.addPlace("waiting2");
        Transition arrival0 = net.addTransition("arrival0");
        Transition arrival1 = net.addTransition("arrival1");
        Transition arrival2 = net.addTransition("arrival2");
        Transition complete0 = net.addTransition("complete0");
        Transition complete1 = net.addTransition("complete1");
        Transition complete2 = net.addTransition("complete2");
        Transition select0 = net.addTransition("select0");
        Transition select1 = net.addTransition("select1");
        Transition select2 = net.addTransition("select2");
        Transition serviceq0 = net.addTransition("serviceq0");
        Transition serviceq1 = net.addTransition("serviceq1");
        Transition serviceq2 = net.addTransition("serviceq2");

        //Generating Connectors
        net.addPostcondition(select1, service1);
        net.addPostcondition(complete2, finish2);
        net.addPrecondition(finish2, select0);
        net.addPostcondition(complete1, finish1);
        net.addPrecondition(service2, complete2);
        net.addPrecondition(service0, complete0);
        net.addPrecondition(finish1, select2);
        net.addPostcondition(select2, service2);
        net.addPrecondition(finish0, select1);
        net.addPostcondition(complete0, finish0);
        net.addPostcondition(select0, service0);
        net.addPrecondition(service1, complete1);
        net.addPrecondition(finish2, select0);
        net.addPrecondition(finish2, select0);
        net.addPrecondition(finish2, select0);
        net.addPrecondition(waiting0, serviceq0);
        net.addPostcondition(serviceq0, idle0);
        net.addPostcondition(serviceq0, idle0);
        net.addPrecondition(idle0, arrival0);
        net.addPostcondition(arrival0, arrived0);
        net.addPostcondition(arrival0, arrived0);
        net.addInhibitorArc(waiting0, complete0);
        net.addPrecondition(waiting1, serviceq1);
        net.addPostcondition(serviceq1, idle1);
        net.addPrecondition(idle1, arrival1);
        net.addPostcondition(arrival1, arrived1);
        net.addInhibitorArc(waiting1, complete1);
        net.addPostcondition(serviceq1, service1);
        net.addPrecondition(service1, serviceq1);
        net.addPostcondition(serviceq1, idle1);
        net.addPostcondition(serviceq1, idle1);
        net.addPrecondition(service0, serviceq0);
        net.addPostcondition(serviceq0, service0);
        net.addPrecondition(idle2, arrival2);
        net.addPostcondition(arrival2, arrived2);
        net.addPrecondition(waiting2, serviceq2);
        net.addPostcondition(serviceq2, idle2);
        net.addPostcondition(serviceq2, service2);
        net.addPrecondition(service2, serviceq2);
        net.addInhibitorArc(waiting2, complete2);
    }
    public static void sequentialKShots(PetriNet net, Marking marking,int K,int T){
      //Generating Nodes
        Place arrived0 = net.addPlace("arrived0");
        Place arrived1 = net.addPlace("arrived1");
        Place arrived2 = net.addPlace("arrived2");
        Place finish0 = net.addPlace("finish0");
        Place finish1 = net.addPlace("finish1");
        Place finish2 = net.addPlace("finish2");
        Place idle0 = net.addPlace("idle0");
        Place idle1 = net.addPlace("idle1");
        Place idle2 = net.addPlace("idle2");
        Place service0 = net.addPlace("service0");
        Place service1 = net.addPlace("service1");
        Place service2 = net.addPlace("service2");
        Place waiting0 = net.addPlace("waiting0");
        Place waiting1 = net.addPlace("waiting1");
        Place waiting2 = net.addPlace("waiting2");
        Transition arrival0 = net.addTransition("arrival0");
        Transition arrival1 = net.addTransition("arrival1");
        Transition arrival2 = net.addTransition("arrival2");
        Transition complete0 = net.addTransition("complete0");
        Transition complete1 = net.addTransition("complete1");
        Transition complete2 = net.addTransition("complete2");
        Transition select0 = net.addTransition("select0");
        Transition select1 = net.addTransition("select1");
        Transition select2 = net.addTransition("select2");
        Transition serviceq0 = net.addTransition("serviceq0");
        Transition serviceq1 = net.addTransition("serviceq1");
        Transition serviceq2 = net.addTransition("serviceq2");

        //Generating Connectors
        net.addInhibitorArc(waiting2, complete2);
        net.addInhibitorArc(waiting0, complete0);
        net.addInhibitorArc(waiting1, complete1);
        net.addPrecondition(finish1, select2);
        net.addPostcondition(serviceq1, idle1);
        net.addPrecondition(service2, complete2);
        net.addPrecondition(idle2, arrival2);
        net.addPostcondition(complete0, finish0);
        net.addPrecondition(service0, serviceq0);
        net.addPostcondition(serviceq0, idle0);
        net.addPrecondition(waiting0, serviceq0);
        net.addPrecondition(idle0, arrival0);
        net.addPostcondition(select0, service0);
        net.addPostcondition(serviceq2, idle2);
        net.addPrecondition(finish0, select1);
        net.addPostcondition(arrival2, arrived2);
        net.addPrecondition(service1, complete1);
        net.addPostcondition(serviceq2, service2);
        net.addPostcondition(arrival0, arrived0);
        net.addPrecondition(idle1, arrival1);
        net.addPostcondition(complete1, finish1);
        net.addPrecondition(service2, serviceq2);
        net.addPrecondition(waiting1, serviceq1);
        net.addPrecondition(service1, serviceq1);
        net.addPostcondition(serviceq1, idle1);
        net.addPrecondition(finish2, select0);
        net.addPostcondition(select1, service1);
        net.addPostcondition(serviceq0, idle0);
        net.addPrecondition(service0, complete0);
        net.addPrecondition(finish2, select0);
        net.addPrecondition(finish2, select0);
        net.addPostcondition(arrival1, arrived1);
        net.addPostcondition(serviceq1, service1);
        net.addPostcondition(select2, service2);
        net.addPostcondition(serviceq0, service0);
        net.addPostcondition(arrival0, arrived0);
        net.addPrecondition(waiting2, serviceq2);
        net.addPostcondition(complete2, finish2);
        net.addPrecondition(finish2, select0);
        net.addPostcondition(serviceq1, idle1);

        //Generating Properties
        marking.setTokens(arrived0, 0);
        marking.setTokens(arrived1, 0);
        marking.setTokens(arrived2, 0);
        marking.setTokens(finish0, 0);
        marking.setTokens(finish1, 0);
        marking.setTokens(finish2, 0);
        marking.setTokens(idle0, T);
        marking.setTokens(idle1, T);
        marking.setTokens(idle2, T);
        marking.setTokens(service0, 1);
        marking.setTokens(service1, 0);
        marking.setTokens(service2, 0);
        marking.setTokens(waiting0, 0);
        marking.setTokens(waiting1, 0);
        marking.setTokens(waiting2, 0);
        arrival0.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        arrival0.addFeature(new RateExpressionFeature("0.1*idle0"));
        arrival1.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        arrival1.addFeature(new RateExpressionFeature("0.1*idle1"));
        arrival2.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        arrival2.addFeature(new RateExpressionFeature("0.1*idle2"));
        complete0.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        complete0.addFeature(new WeightExpressionFeature("1"));
        complete0.addFeature(new Priority(new Integer("0")));
        complete1.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        complete1.addFeature(new WeightExpressionFeature("1"));
        complete1.addFeature(new Priority(new Integer("0")));
        complete2.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        complete2.addFeature(new WeightExpressionFeature("1"));
        complete2.addFeature(new Priority(new Integer("0")));
        select0.addFeature(new PostUpdater("waiting0=If(arrived0<"+K+",arrived0,"+K+");arrived0=arrived0-If(arrive0<"+K+",arrived0,"+K+")", net));
        select0.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        select0.addFeature(new RateExpressionFeature("1"));
        select1.addFeature(new PostUpdater("waiting1=If(arrived1<"+K+",arrived1,"+K+");arrived1=arrived1-If(arrived1<"+K+",arrived1,"+K+")", net));
        select1.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        select1.addFeature(new RateExpressionFeature("1"));
        select2.addFeature(new PostUpdater("waiting2=If(arrived2<"+K+",arrived2,"+K+");arrived2=arrived2-If(arrived2<"+K+",arrived2,"+K+")", net));
        select2.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        select2.addFeature(new RateExpressionFeature("1"));
        serviceq0.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("1"), new OmegaBigDecimal("3")));
        serviceq1.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("1"), new OmegaBigDecimal("3")));
        serviceq2.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("1"), new OmegaBigDecimal("3")));
      }
    public static void sequentiaExhaustive(PetriNet net, Marking marking){
      //Generating Nodes
        Place p0 = net.addPlace("p0");
        Place p1 = net.addPlace("p1");
        Place p10 = net.addPlace("p10");
        Place p11 = net.addPlace("p11");
        Place p12 = net.addPlace("p12");
        Place p13 = net.addPlace("p13");
        Place p2 = net.addPlace("p2");
        Place p4 = net.addPlace("p4");
        Place p5 = net.addPlace("p5");
        Place p6 = net.addPlace("p6");
        Place p8 = net.addPlace("p8");
        Place p9 = net.addPlace("p9");
        Transition t0 = net.addTransition("t0");
        Transition t1 = net.addTransition("t1");
        Transition t10 = net.addTransition("t10");
        Transition t11 = net.addTransition("t11");
        Transition t12 = net.addTransition("t12");
        Transition t13 = net.addTransition("t13");
        Transition t2 = net.addTransition("t2");
        Transition t3 = net.addTransition("t3");
        Transition t4 = net.addTransition("t4");
        Transition t6 = net.addTransition("t6");
        Transition t7 = net.addTransition("t7");
        Transition t9 = net.addTransition("t9");

        //Generating Connectors
        net.addInhibitorArc(p5, t7);
        net.addInhibitorArc(p1, t4);
        net.addPrecondition(p2, t0);
        net.addPostcondition(t4, p11);
        net.addPostcondition(t6, p4);
        net.addPostcondition(t1, p5);
        net.addPrecondition(p5, t6);
        net.addPrecondition(p6, t1);
        net.addPrecondition(p11, t11);
        net.addPostcondition(t3, p0);
        net.addPostcondition(t0, p1);
        net.addPrecondition(p2, t0);
        net.addPrecondition(p0, t4);
        net.addPostcondition(t3, p2);
        net.addPrecondition(p1, t3);
        net.addPrecondition(p0, t3);
        net.addPostcondition(t6, p6);
        net.addPrecondition(p4, t7);
        net.addPostcondition(t7, p12);
        net.addPostcondition(t3, p2);
        net.addPostcondition(t6, p6);
        net.addPrecondition(p6, t1);
        net.addPrecondition(p4, t6);
        net.addPostcondition(t11, p4);
        net.addPrecondition(p12, t12);
        net.addPrecondition(p10, t2);
        net.addPrecondition(p10, t2);
        net.addPostcondition(t9, p10);
        net.addPostcondition(t9, p10);
        net.addPrecondition(p13, t13);
        net.addPostcondition(t10, p13);
        net.addPrecondition(p9, t10);
        net.addPrecondition(p9, t9);
        net.addPostcondition(t9, p9);
        net.addPrecondition(p8, t9);
        net.addInhibitorArc(p8, t10);
        net.addPostcondition(t2, p8);
        net.addPostcondition(t13, p0);
        net.addPostcondition(t13, p0);
        net.addPostcondition(t13, p0);
        net.addPostcondition(t13, p0);
        net.addPostcondition(t12, p9);

        //Generating Properties
        marking.setTokens(p0, 1);
        marking.setTokens(p1, 0);
        marking.setTokens(p10, 3);
        marking.setTokens(p11, 0);
        marking.setTokens(p12, 0);
        marking.setTokens(p13, 0);
        marking.setTokens(p2, 3);
        marking.setTokens(p4, 0);
        marking.setTokens(p5, 0);
        marking.setTokens(p6, 3);
        marking.setTokens(p8, 0);
        marking.setTokens(p9, 0);
        t0.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        t0.addFeature(new RateExpressionFeature("0.1*p2"));
        t1.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        t1.addFeature(new RateExpressionFeature("0.1*p6"));
        t10.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        t10.addFeature(new WeightExpressionFeature("1"));
        t10.addFeature(new Priority(new Integer("0")));
        t11.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        t11.addFeature(new RateExpressionFeature("1"));
        t12.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        t12.addFeature(new RateExpressionFeature("1"));
        t13.addFeature(new PostUpdater("p1=min(p0,1)", net));
        t13.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        t13.addFeature(new RateExpressionFeature("1"));
        t2.addFeature(StochasticTransitionFeature.newExponentialInstance(new BigDecimal("1")));
        t2.addFeature(new RateExpressionFeature("0.1*p10"));
        t3.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("1"), new OmegaBigDecimal("3")));
        t4.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        t4.addFeature(new WeightExpressionFeature("1"));
        t4.addFeature(new Priority(new Integer("0")));
        t6.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("1"), new OmegaBigDecimal("3")));
        t7.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), new BigDecimal("1")));
        t7.addFeature(new WeightExpressionFeature("1"));
        t7.addFeature(new Priority(new Integer("0")));
        t9.addFeature(StochasticTransitionFeature.newUniformInstance(new OmegaBigDecimal("1"), new OmegaBigDecimal("3")));
    }
}   
