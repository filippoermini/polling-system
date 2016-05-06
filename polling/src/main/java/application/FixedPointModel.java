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
    private int[] Tokens;
    private boolean showInfo;
    private boolean debug;
    private double[] lambda;
    private double ro;
    private final double epsilon = 0.01;
    private double mu;
    private String outFile;
    private boolean printOut = false;
    
    private FileWriter fileWriter = null;
    
    
    
    private ArrayList<Results> FPResult;
    
    
    public FixedPointModel(queueSelectionPolicy qsp, queuePolicy qp, int numQueue, int[] Token, int K,int[] prio,boolean info, boolean debug,double mu, double gamma, double[] labda, double ro, String outfile){
        
        this.numQueue = numQueue;
        this.Tokens = Token;
        this.showInfo = info;
        this.debug = debug;
        this.lambda = labda;
        this.ro = ro;
        this.mu = mu;
        this.outFile = outfile;
        
        if(!outfile.isEmpty()){
            printOut = true;
            File file = new File(outFile);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String execution_info = "Queue Selection Policy:"+qsp.toString()+" Queue Type:"+qp.toString()+(qp==util.queuePolicy.KSHOTS?" K:"+K:"")+" num Queue:"+numQueue+" Token:"+Arrays.toString(Tokens)+(qsp==util.queueSelectionPolicy.FIXED_PRIORITY?" Prio:"+Arrays.toString(prio):""+" ro:"+ro);
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
        OutputTable MeanTime = new OutputTable(3, "Inizializzazione valori tramite il modello Mean Delay");
        //inizializzazione valori tramite il modello Mean Delay
        int index = 0;
        MeanTime.setHeader("Coda","#Token","di(k)");
        MeanTime.setFormat("","","%.7f");
        for(int i=0;i<this.numQueue;i++){ //per ogni coda 
            Results res = new Results(this.Tokens[i]+1); 
            //calcolo lambda_i
            double lambdaSum = DoubleStream.of(lambda).sum();
            l = (this.mu * this.ro)/(this.Tokens[i]*lambdaSum);
            res.lambda_i = l*lambda[i];
            
            for(int j=0;j<this.Tokens[i]+1;j++){ //per ogni token
                this.MeanDelayModel.setMeanDelayTokens(j);
                res.MeanDelayResults[j] = this.MeanDelayModel.getMeanTimeToAbsorption();
                MeanTime.addRecord(i,j,this.MeanDelayModel.getMeanTimeToAbsorption());
            }
            this.FPResult.add(res);
        }
        
        String ress = MeanTime.printTable();
        if(showInfo) System.out.println(ress);
        if(printOut) fileWriter.write(ress);
        if(showInfo && debug)
            System.in.read();
        
        
        //Mean response time computation
        boolean convergence = false;
        do{
            BigDecimal di = BigDecimal.ZERO;
            BigDecimal Ni = BigDecimal.ZERO;
            BigDecimal oldDelta = BigDecimal.ZERO;
            for(int i=0;i<this.numQueue;i++){
                OutputTable MeanDelay = new OutputTable(2,"Mean Delay");
                //calcolo d_i
                double delta = 1;
                if(this.FPResult.get(i).delta == BigDecimal.ZERO){
                    //caso iniziale 
                    delta = 1 / this.FPResult.get(i).MeanDelayResults[this.Tokens[i]];
                    this.FPResult.get(i).delta = BigDecimal.valueOf(delta);
                }
                this.ApproximateModel.setParams(Tokens[i], this.FPResult.get(i).lambda_i, this.FPResult.get(i).delta.doubleValue());
                for(int j=0;j<this.Tokens[i]+1;j++){

                    RewardRate rw = RewardRate.fromString("If(WaitingAPX=="+j+",1,0)");
                    BigDecimal p = this.ApproximateModel.RegenerativeSteadyStateAnalysis(rw).get(rw);
                    this.FPResult.get(i).SteadyStateProbability[j] = p;  
                    
                    di = di.add(this.ApproximateModel.getServer().getMeanDelay(FPResult, i, j, p));
                    Ni = Ni.add(this.ApproximateModel.getServer().getWeights(j, p));
                }
                MeanDelay.setTitle("Calcolo di S"+i+"(N) | delta = "+this.FPResult.get(i).delta);
                MeanDelay.setHeader("di","Ni");
                MeanDelay.setFormat("%.5f","%.5f");
                MeanDelay.addRecord(di,Ni );
                
                ress = MeanDelay.printTable();
                if(showInfo)System.out.println(ress);
                if(printOut) fileWriter.write(ress);
                if(showInfo && debug)
                    System.in.read();
                
                this.FPResult.get(i).d_i = this.ApproximateModel.getServer().getLast().getQueue().getSojournTime(di, Ni);
                this.FPResult.get(i).w_i = Ni.add(BigDecimal.ONE);
                di = BigDecimal.ZERO;
            }
            
            //fine calcolo d_i
            
            
            //Calcolo Di//
            OutputTable DelaySum = new OutputTable(4, "Calcolo Di");
            DelaySum.setHeader("#","Di","Old Delta","New Delta");
            DelaySum.setFormat("","%.4f","%.5f","%.5f");
            for(int i=0;i<this.numQueue;i++){
                
                BigDecimal Di = BigDecimal.ZERO;
                Di = this.ApproximateModel.getServer().getDi(this.ApproximateModel,FPResult,i,numQueue);
                
                this.FPResult.get(i).D = Di;
                oldDelta = this.FPResult.get(i).delta;
                this.FPResult.get(i).delta = BigDecimal.valueOf(1/Di.doubleValue());
                DelaySum.addRecord(i,Di,oldDelta,this.FPResult.get(i).delta);
            }
            
            //fine colcolo Di//
            ress = DelaySum.printTable();
            if(showInfo)System.out.println(ress);
            if(printOut) fileWriter.write(ress);
            for(int i=0;i<this.numQueue;i++){
                if(this.FPResult.get(i).delta.subtract(oldDelta).abs().doubleValue() < epsilon) convergence = true;
            }
            if(showInfo && debug)
                System.in.read();
        }while(!convergence);
        
        //calcolo di E[Ri]
        
        
        double[] E_A = new double[numQueue];
        OutputTable AppResults = new OutputTable(2, "Calcolo E[Ri] di Si(N)"); 
        AppResults.setHeader("#","E[Ri]");
        AppResults.setFormat("","%.7f");
        for(int i=0;i<numQueue;i++){
            
            
            this.ApproximateModel.setDelta(this.FPResult.get(i).delta.doubleValue());
            this.ApproximateModel.setLambda(this.FPResult.get(i).lambda_i);
            this.ApproximateModel.setTokens(this.Tokens[i]);
            RewardRate rw = RewardRate.fromString(l*lambda[i]+"*IdleAPX");
            double Tput = this.ApproximateModel.RegenerativeSteadyStateAnalysis(rw).get(rw).doubleValue();
            double a = Tokens[i] / Tput;
            double b = l*lambda[i];
            double c = 1/b;
            E_A[i] = a - c; 
            AppResults.addRecord(i,E_A[i]);
          
            
        }
        ress = AppResults.printTable();
        if(showInfo) System.out.println(ress);
        if(printOut) fileWriter.write(ress);
        
        
        
        
        OutputTable PollingResults = new OutputTable(2, "Calcolo E[Ri] Sul sistema di polling"); 
        PollingResults.setHeader("#","E[Ri]");
        PollingResults.setFormat("","%.7f");
        double[] E_P = new double[numQueue];
        for(int i=0;i<numQueue;i++){
            
            RewardRate rw = RewardRate.fromString(this.FPResult.get(i).lambda_i+"*Idle"+i);
            double Tput = this.PollingModel.RegenerativeSteadyStateAnalysis(rw).get(rw).doubleValue();
            double a = Tokens[i] / Tput;
            double b = l*lambda[i];
            double c = 1/b;
            E_P[i] = a - c; 
            PollingResults.addRecord(i,E_P[i]);
            }
        ress = PollingResults.printTable();
        if(showInfo) System.out.println(ress);
        if(printOut) {
            fileWriter.write(ress);
            fileWriter.flush();
            fileWriter.close();
        }
//        RewardRate rw1 = RewardRate.fromString("Waiting2");
//        RewardRate rw2 = RewardRate.fromString("WaitingAPX");
//        System.out.println(this.PollingModel.RegenerativeSteadyStateAnalysis(rw1).get(rw1));
//        System.out.println(this.ApproximateModel.RegenerativeSteadyStateAnalysis(rw2).get(rw2));
//        System.in.read();
        
        return new double[]{E_A[0],E_P[0]};
        
    }
    
    
    
    
}
