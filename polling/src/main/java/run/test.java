package run;

import application.ApproximateModel;
import application.FixedPointModel;
import application.PollingModel;
import application.util;

public class test {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        int tokens[] = {3,3,3};
        double[] lambda = {2,1,0.5};
        int[] prio = {1,2,3};
        double mu = 1.25;
        double ro = 0.1;
        double gamma = 294.12;
        PollingModel pm = new PollingModel(3, tokens,1,prio, util.queuePolicy.ONLY_PRESENT_AT_ARRIVAL, util.queueSelectionPolicy.PROBABILISTIC_PROPOTIONAL_TO_QUEUE_LENGTH,lambda,mu,gamma,ro);
        //pm.showInfo();
        ApproximateModel ap = new ApproximateModel("APX", util.queueSelectionPolicy.PROBABILISTIC_PROPOTIONAL_TO_QUEUE_LENGTH, util.queuePolicy.SINGLESERVICE, 0, prio, mu, gamma);
        ap.setDelta(0.4160772286415953);
        ap.setLambda(0.023809523809523808);
        ap.setTokens(3);
        ap.showInfo();
        
        System.out.println(ap.RegenerativeSteadyStateAnalysis("If(WaitingAPX==0,1,0)"));
        //System.out.println(pm.RegenerativeSteadyStateAnalysis("Waiting1"));
        //System.out.println(pm.RegenerativeSteadyStateAnalysis("Waiting2"));
        
    }

}
