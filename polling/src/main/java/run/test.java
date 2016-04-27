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
        PollingModel pm = new PollingModel(3, tokens,1,prio, util.queuePolicy.EXHAUSTIVE, util.queueSelectionPolicy.PROBABILISTIC_PROPOTIONAL_TO_QUEUE_LENGTH,lambda,mu,gamma,ro);
        //pm.showInfo();
        System.out.println(pm.RegenerativeSteadyStateAnalysis("Waiting0"));
    }

}
