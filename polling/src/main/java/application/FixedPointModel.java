package application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import java.util.List;
import application.util.queuePolicy;
import application.util.queueSelectionPolicy;
import domain.Results;
import it.unifi.oris.sirio.models.gspn.RateExpressionFeature;
import it.unifi.oris.sirio.models.stpn.RewardRate;
import it.unifi.oris.sirio.models.stpn.StochasticTransitionFeature;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.Transition;
import it.unifi.oris.sirio.petrinet.TransitionFeature;

public class FixedPointModel {

    private PollingModel PollingModel;
    private ApproximateModel ApproximateModel;
    private MeanDelayModel MeanDelayModel;
    
    private int numQueue;
    private int K;
    private int[] Tokens;
    private int[] prio;
    private boolean showInfo;
    private boolean debug;
    private double[] lambda;
    private double ro;
    private final double epsilon = 0.00001;
    private double mu;
    private double gamma;
    private String outFile;
    private boolean printOut = false;
    
    private FileWriter fileWriter = null;
    
    
    
    private ArrayList<Results> FPResult;
    
    
    public FixedPointModel(queueSelectionPolicy qsp, queuePolicy qp, int numQueue, int[] Token, int K,int[] prio,boolean info, boolean debug,double mu, double gamma, double[] labda, double ro, String outfile){
        
        this.numQueue = numQueue;
        this.K = K;
        this.Tokens = Token;
        this.prio = prio;
        this.showInfo = info;
        this.debug = debug;
        this.lambda = labda;
        this.ro = ro;
        this.mu = mu;
        this.gamma = gamma;
        this.outFile = outfile;
        
        if(!outfile.isEmpty()){
            printOut = true;
            File file = new File(outFile);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String execution_info = "Queue Selectrion Policy:"+qsp.toString()+" Queue Type:"+qp.toString()+(qp==util.queuePolicy.KSHOTS?" K:"+K:"")+" num Queue:"+numQueue+" Token:"+Arrays.toString(Tokens)+(qsp==util.queueSelectionPolicy.FIXED_PRIORITY?" Prio:"+Arrays.toString(prio):""+" ro:"+ro);
            try {
                fileWriter = new FileWriter(file);
                fileWriter.write(sdf.format(new Date())+"\n");
                fileWriter.write(execution_info+"\n");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        //inizializzazione modelli
        this.PollingModel = new PollingModel(numQueue,Token,K,prio,qp,qsp,labda,mu,gamma,ro);
        this.ApproximateModel = new ApproximateModel("APX",qsp,qp,K,prio,mu,gamma);
        this.MeanDelayModel = new MeanDelayModel(qsp, qp,mu,gamma,K,prio);
        
        this.FPResult = new ArrayList<>();    
    }
    
    public double[] fixedPointIteration() throws IOException{
        double l = 0;
        int iteration = 0;
        Formatter formatter = new Formatter();
        String s = "";
        String ress = "Inizializzazione valori tramite il modello Mean Delay\n";
        //inizializzazione valori tramite il modello Mean Delay
        int index = 0;
        
        
            ress += "-------------------------------------------\n";
            ress += "|    Coda    |    #Token    |   di(k)     |\n";
            ress += "-------------------------------------------\n";
        
        for(int i=0;i<this.numQueue;i++){ //per ogni coda 
            Results res = new Results(this.Tokens[i]+1); 
            for(int j=0;j<this.Tokens[i]+1;j++){ //per ogni token
                this.MeanDelayModel.setMeanDelayTokens(j);
                res.MeanDelayResults[j] = this.MeanDelayModel.getMeanTimeToAbsorption();
                s = formatter.format("|      %d     |      %d       |  %.7f  |\n", i,j,this.MeanDelayModel.getMeanTimeToAbsorption()).toString();
                
            }
            this.FPResult.add(res);
        }
        ress+= s;
        ress+= "-------------------------------------------\n";
        if(showInfo) System.out.println(ress);
        if(printOut) fileWriter.write(ress);
        if(showInfo && debug)
            System.in.read();
        
        
        //Mean response time computation
        boolean convergence = false;
        double lambdaSum = DoubleStream.of(lambda).sum();
        do{
            ress = "";
            formatter = new Formatter();
            ress +="Iterazione "+iteration+"\n\n";
            BigDecimal di = BigDecimal.ZERO;
            BigDecimal oldDelta = BigDecimal.ZERO;
            for(int i=0;i<this.numQueue;i++){
                //calcolo lambda_i
                l = 0;
                l = (this.mu * this.ro)/(this.Tokens[i]*lambdaSum);
                //calcolo d_i
                double delta = 1;
                if(this.FPResult.get(i).delta == BigDecimal.ZERO)
                    //caso iniziale 
                    delta = 1 / this.FPResult.get(i).MeanDelayResults[this.Tokens[i]-1];
                else
                    delta = 1 / this.FPResult.get(i).D.doubleValue();
                this.FPResult.get(i).delta = BigDecimal.valueOf(delta);
                
                this.ApproximateModel.setTokens(this.Tokens[i]);
                this.ApproximateModel.setDelta(delta);
                this.ApproximateModel.setLambda(l*lambda[i]);
                
                
                
                for(int j=0;j<this.Tokens[i]+1;j++){

                    RewardRate rw = RewardRate.fromString("If(WaitingAPX=="+j+",1,0)");
                    BigDecimal p = this.ApproximateModel.RegenerativeSteadyStateAnalysis(rw).get(rw);
                    this.FPResult.get(i).SteadyStateProbability[j] = p;  
                    
                    di = di.add(this.ApproximateModel.getServer().getMeanDelay(FPResult, i, j, p));
                }
                
                s = this.ApproximateModel.getServer().getOutpuString(i, delta, di);
                
                this.FPResult.get(i).d_i = di;
                di = BigDecimal.ZERO;
            }
            ress += s;
            //fine calcolo d_i
            if(showInfo)System.out.println(ress);
            if(printOut) fileWriter.write(ress);
            if(showInfo && debug)
                System.in.read();
            
            formatter = new Formatter();
            ress = "\nCalcolo Di\n";
            //Calcolo Di//
            for(int i=0;i<this.numQueue;i++){
                BigDecimal Di = BigDecimal.ZERO;
                
                
                Di = this.ApproximateModel.getServer().getDi(FPResult,i,numQueue);
                
                this.FPResult.get(i).D = Di;
                oldDelta = this.FPResult.get(i).delta;
                this.FPResult.get(i).delta = BigDecimal.valueOf(1/Di.doubleValue());//BigDecimal.ONE.divide(Di);
                s = formatter.format("---------------------------------------------------------\n|\tD_%d = %.4f\t|\t(%.5f,%.5f)\t|\n",i,Di,oldDelta,this.FPResult.get(i).delta).toString();
                
            }
            ress += s;
            ress += "---------------------------------------------------------\n";
            //fine colcolo Di//
            if(showInfo)System.out.println(ress);
            if(printOut) fileWriter.write(ress);
            for(int i=0;i<this.numQueue;i++){
                if(this.FPResult.get(i).delta.subtract(oldDelta).abs().doubleValue() < epsilon) convergence = true;
            }
            if(showInfo && debug)
                System.in.read();
            iteration++;
        }while(!convergence);
        
        //calcolo di E[Ri]
        formatter = new Formatter();
        
        double[] E_A = new double[numQueue];
        ress = "\nCalcolo E[Ri] di Si(N)\n";
        for(int i=0;i<numQueue;i++){
            
            this.ApproximateModel.setDelta(this.FPResult.get(i).delta.doubleValue());
            this.ApproximateModel.setLambda(l*lambda[i]);
            RewardRate rw = RewardRate.fromString(l*lambda[i]+"*IdleAPX");
            
            double Tput = this.ApproximateModel.RegenerativeSteadyStateAnalysis(rw).get(rw).doubleValue();
            double a = Tokens[i] / Tput;
            double b = l*lambda[i];
            double c = 1/b;
            E_A[i] = a - c; 
            if(showInfo){
                
                s = formatter.format("-------------------------------------------------\n|\t\tE[R%d] = %.7f\t\t|\n",i,E_A[i]).toString();
            }
            
        }
        ress += s;
        ress += "---------------------------------------------------------\n";
        if(showInfo) System.out.println(ress);
        if(printOut) fileWriter.write(ress);
        
        formatter = new Formatter();
        ress ="\nCalcolo E[Ri] Sul sistema di polling\n";
        
        double[] E_P = new double[numQueue];
        for(int i=0;i<numQueue;i++){
            
            RewardRate rw = RewardRate.fromString(l*lambda[i]+"*Idle"+i);
            
            double Tput = this.PollingModel.RegenerativeSteadyStateAnalysis(rw).get(rw).doubleValue();
            double a = Tokens[i] / Tput;
            double b = l*lambda[i];
            double c = 1/b;
            E_P[i] = a - c; 
            s = formatter.format("-------------------------------------------------\n|\t\tE[R%d] = %.7f\t\t|\n",i,E_P[i]).toString();
        }
        ress += s;
        ress += "-------------------------------------------------\n";
        if(showInfo) System.out.println(ress);
        if(printOut) {
            fileWriter.write(ress);
            fileWriter.flush();
            fileWriter.close();
        }
        return new double[]{E_A[0],E_P[0]};
        
    }
    
    
    
    
}
