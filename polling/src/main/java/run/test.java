package run;

import application.ApproximateModel;
import application.FixedPointModel;
import application.PollingModel;
import application.util;

public class test {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        int tokens[] = {3,3,3};
        double[] lambda = {0.1,0.1,0.1};
        PollingModel pm = new PollingModel(3, tokens, util.queuePolicy.SINGLESERVICE, util.queueSelectionPolicy.SEQUENTIAL,lambda);
        ApproximateModel apx = new ApproximateModel("APX", 0, util.queueSelectionPolicy.SEQUENTIAL, util.queuePolicy.SINGLESERVICE);
        System.out.println(pm.RegenerativeSteadyStateAnalysis("Waiting0"));
    }

}
