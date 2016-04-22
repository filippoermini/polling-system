package application;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Map;

import org.junit.internal.runners.model.EachTestNotifier;

import domain.Queue;
import domain.Server;
import it.unifi.oris.sirio.analyzer.policy.FIFOPolicy;
import it.unifi.oris.sirio.models.gspn.RateExpressionFeature;
import it.unifi.oris.sirio.models.stpn.DeterministicEnablingState;
import it.unifi.oris.sirio.models.stpn.DeterministicEnablingStateBuilder;
import it.unifi.oris.sirio.models.stpn.EnablingSyncsEvaluator;
import it.unifi.oris.sirio.models.stpn.RewardRate;
import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;
import it.unifi.oris.sirio.models.stpn.steadystate.RegenerativeSteadyStateAnalysis;
import it.unifi.oris.sirio.models.stpn.steadystate.SteadyStateInitialStateBuilder;
import it.unifi.oris.sirio.models.stpn.steadystate.SteadyStatePostProcessor;
import it.unifi.oris.sirio.models.stpn.steadystate.SteadyStateSolution;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.MarkingCondition;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Transition;

public abstract class PetriNetModel {

    protected PetriNet Net;
    protected Marking Marking;
    
    public PetriNetModel(){
        this.Net = new PetriNet();
        this.Marking = new Marking();
        
    }
    
    protected abstract void build();
    
    protected Server getServerType(util.queueSelectionPolicy serverType, int numQueue) {
        Class c;
        try {
            String className = serverType.getClassName();
            c = Class.forName("domain."+className);
            Constructor<?> cons = c.getConstructor(int.class);
            Object[] obj = {numQueue};
            return (Server) cons.newInstance(obj);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
    protected Queue getQueue(util.queuePolicy queueType, Object... params){
        Class c;
        try {
            String className = queueType.getClassName();
            Class[] paramType = new Class[params.length];
            int index = 0;
            for (Object p: params){
                paramType[index++] = p.getClass();
            }
            c = Class.forName("domain."+className);
            Constructor cons = c.getConstructor(paramType);
            return (Queue) cons.newInstance(params);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
        
    }
    
    public void showInfo(){
        System.out.println(Net);
        for (Transition t: Net.getTransitions()) {
            System.out.println(t.getName());
            StochasticTransitionFeature f = t.getFeature(StochasticTransitionFeature.class);
            System.out.println(f.getFiringTimeDensity().getDomain());
            System.out.println(f.getFiringTimeDensity().getDensity());
            RateExpressionFeature rf = t.getFeature(RateExpressionFeature.class);
            if(rf!=null)System.out.println(rf.getExpression());
            System.out.println("-----------");
        }
    }
    
    public void ShowGraph(){
        util.showGraph(util.nonDeterministicAnalysis(Net, Marking, true, MarkingCondition.NONE));
    }
    
    public Map<RewardRate, BigDecimal> RegenerativeSteadyStateAnalysis(RewardRate reward){
        
        MarkingCondition stopCondition = MarkingCondition.NONE;
        SteadyStateInitialStateBuilder<DeterministicEnablingState> sb = 
            new SteadyStateInitialStateBuilder<>(new DeterministicEnablingStateBuilder(Net, false));
    
        RegenerativeSteadyStateAnalysis<DeterministicEnablingState> analysis = 
                RegenerativeSteadyStateAnalysis.compute(
                        Net, 
                        new DeterministicEnablingState(Marking, Net), 
                        sb, new SteadyStatePostProcessor(new EnablingSyncsEvaluator()), 
                        new FIFOPolicy(),
                        stopCondition, 
                        false, 
                        false, 
                        null, 
                        null, 
                        true);
        
        SteadyStateSolution<Marking> steadyStateProbs = analysis.getSteadyState();
        SteadyStateSolution<RewardRate> steadyStateRewards = SteadyStateSolution.computeRewards(steadyStateProbs, reward);
        return steadyStateRewards.getSteadyState();
    }
    
    public Map<RewardRate, BigDecimal> RegenerativeSteadyStateAnalysis(String reward){
        
        MarkingCondition stopCondition = MarkingCondition.NONE;
        SteadyStateInitialStateBuilder<DeterministicEnablingState> sb = 
            new SteadyStateInitialStateBuilder<>(new DeterministicEnablingStateBuilder(Net, false));
    
        RegenerativeSteadyStateAnalysis<DeterministicEnablingState> analysis = 
                RegenerativeSteadyStateAnalysis.compute(
                        Net, 
                        new DeterministicEnablingState(Marking, Net), 
                        sb, new SteadyStatePostProcessor(new EnablingSyncsEvaluator()), 
                        new FIFOPolicy(),
                        stopCondition, 
                        false, 
                        false, 
                        null, 
                        null, 
                        true);
        
        SteadyStateSolution<Marking> steadyStateProbs = analysis.getSteadyState();
        SteadyStateSolution<RewardRate> steadyStateRewards = SteadyStateSolution.computeRewards(steadyStateProbs, reward);
        return steadyStateRewards.getSteadyState();
    }
}
