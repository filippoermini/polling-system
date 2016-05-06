package application;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;

import it.unifi.oris.sirio.analyzer.Analyzer;
import it.unifi.oris.sirio.analyzer.graph.SuccessionGraph;
import it.unifi.oris.sirio.analyzer.graph.SuccessionGraphViewer;
import it.unifi.oris.sirio.analyzer.log.AnalysisMonitor;
import it.unifi.oris.sirio.analyzer.policy.FIFOPolicy;
import it.unifi.oris.sirio.models.tpn.TimedComponentsFactory;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.MarkingCondition;
import it.unifi.oris.sirio.petrinet.PetriNet;
import it.unifi.oris.sirio.petrinet.Transition;

public class util {
    
   
    public enum queuePolicy {
        EXHAUSTIVE("ExaustiveQueue","EX",new int[]{0,1,2,3}),
        ONLY_PRESENT_AT_ARRIVAL("OnlyPresentAtArrivalQueue","OP",new int[]{0,1,2,3}),
        KSHOTS("KShotsQueue","KS",new int[]{0,1,2,3,4}),
        SINGLESERVICE("SingleServiceQueue","SS",new int[]{0,1,2,3});
        
        private String className;
        private String prefix;
        private int[] params;
        private queuePolicy(String name,String pre,int[] param){
            className = name;
            prefix = pre;
            params = param;
        }
        
        public int[] getParams(){
            return params;
        }
        public String getClassName(){
            return className;
        }
        public static queuePolicy getPolicyFromPrefix(String pre){
            for(queuePolicy qp:queuePolicy.values()){
                if(pre.contentEquals(qp.prefix)){
                    return qp;
                }
            }
            return null;
            
        }
    }
    public enum queueSelectionPolicy{
        SEQUENTIAL("Sequential","SQ",new int[]{0,2}),
        FIXED_PRIORITY("FixedPriority","FP",new int[]{1,2}),
        PROBABILISTIC_PROPOTIONAL_TO_QUEUE_LENGTH("SmartProbabilistic","PR",new int[]{2}),
        UNIFORM("Uniform","UF",new int[]{2});
        
        private String className;
        private String prefix; 
        private int[] params;
        private queueSelectionPolicy(String name,String pre,int[] param){
            className = name;
            prefix = pre;
            params = param;
        }
        public int[] getParams(){
            return params;
        }
        public String getClassName(){
            return className;
        }
        public static queueSelectionPolicy getPolicyFromPrefix(String pre){
            for(queueSelectionPolicy qsp:queueSelectionPolicy.values()){
                if(pre.contentEquals(qsp.prefix)){
                    return qsp;
                }
            }
            return null;
        }
    }
    
    
    
    public static SuccessionGraph nonDeterministicAnalysis(PetriNet petriNet,
            Marking initialMarking, boolean extended, MarkingCondition absorbingCondition) {
        TimedComponentsFactory f = new TimedComponentsFactory(false, false, true, true, extended,
                new FIFOPolicy(), absorbingCondition, new AnalysisMonitor() {

                    @Override
                    public void notifyMessage(String message) {
                        System.out.println(message);
                    }

                    @Override
                    public boolean interruptRequested() {
                        return false;
                    }
                });

        Analyzer<PetriNet, Transition> analyzer = new Analyzer<PetriNet, Transition>(f, petriNet,
                f.buildInitialState(petriNet, initialMarking));

        SuccessionGraph graph = analyzer.analyze();

        return graph;
    }
    public static void showGraph(SuccessionGraph graph) {
        System.out.println("The graph contains " + graph.getStates().size() + " states and "
                + graph.getSuccessions().size() + " transitions");

        final JPanel viewer = new SuccessionGraphViewer(graph);
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                JFrame frame = new JFrame("State graph");
                frame.add(viewer);
                frame.setDefaultCloseOperation(3);
                frame.setExtendedState(6);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

}
