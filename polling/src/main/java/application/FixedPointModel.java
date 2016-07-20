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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import java.util.List;
import application.util.queuePolicy;
import application.util.queueSelectionPolicy;
import domain.Results;
import domain.TransitionParams;
import feature_transition.TransitionManager;
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
    private final double epsilon = 0.001;
    private TransitionManager mu;
    private TransitionManager delta;
    private String outFile;
    private boolean printOut = false;
    
    
    private FileWriter fileWriter = null;
    
    
    
    private ArrayList<Results> FPResult;
    
    
    public FixedPointModel(queueSelectionPolicy qsp, queuePolicy[] qpList, int numQueue, int[] Token, int[] K,int[] prio,boolean info, boolean debug,TransitionManager mu, TransitionManager gamma, TransitionManager delta, double[] labda, double ro, String outfile){
        
        this.numQueue = numQueue;
        this.Tokens = Token;
        this.showInfo = info;
        this.debug = debug;
        this.lambda = labda;
        this.ro = ro;
        this.mu = mu;
        this.delta = delta;
        this.outFile = outfile;
        
        
        if(!outfile.isEmpty()){
            printOut = true;
            File file = new File(outFile);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String qlist = "[";
            for(queuePolicy q:qpList)
                qlist += q.getPrefix()+",";
            qlist = qlist.substring(0, qlist.length()-1)+"]";
            String execution_info = "Queue Selection Policy:"+qsp.toString()+" Queue Type:"+qlist+" K:"+Arrays.toString(K)+" num Queue:"+numQueue+" Token:"+Arrays.toString(Tokens)+(qsp==util.queueSelectionPolicy.SIMULATED_PRIORITY?" Weights:"+Arrays.toString(prio):""+" rho:"+ro);
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
        this.PollingModel = new PollingModel(numQueue,Token,K,prio,qpList,qsp,labda,mu,gamma,ro);
        this.ApproximateModel = new ApproximateModel("APX",qsp,qpList,K,prio,mu,gamma,delta);
        this.MeanDelayModel = new MeanDelayModel(qsp, qpList,mu,gamma,K,prio);
        
        this.FPResult = new ArrayList<>();    
    }
    public PollingModel getPollingModel(){
        return this.PollingModel;
    }
    public ApproximateModel getApproximateModel(){
        return this.ApproximateModel;
    }
    public HashMap<String,HashMap<String,Double>> fixedPointIteration(String reward) throws IOException{
        
        double l = 0;
        OutputTable MeanTime = new OutputTable(3, "Inizializzazione valori tramite il modello Mean Delay",false);
        //inizializzazione valori tramite il modello Mean Delay
        int index = 0;
        MeanTime.setHeader("Coda","#Token","di(k)");
        MeanTime.setFormat("","","%.7f");
        for(int i=0;i<this.numQueue;i++){ //per ogni coda 
            Results res = new Results(this.Tokens[i]+1); 
            //calcolo lambda_i
            double den = 0;
            for(int h=0;h<lambda.length;h++){
                den += lambda[h]*Tokens[h];
            }
            l = (mu.getTransitionValue() * this.ro)/(den);
            res.lambda_i = l*lambda[i];
            
            for(int j=0;j<this.Tokens[i]+1;j++){ //per ogni token
                this.MeanDelayModel.getMeanDelayNet(i).setMeanDelayTokens(j);
                res.MeanDelayResults[j] = this.MeanDelayModel.getMeanDelayNet(i).getMeanTimeToAbsorption();
                MeanTime.addRecord(i,j,this.MeanDelayModel.getMeanDelayNet(i).getMeanTimeToAbsorption());
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
            BigDecimal[] oldDelta = new BigDecimal[this.numQueue];
            for(int i=0;i<this.numQueue;i++){
                OutputTable MeanDelay = new OutputTable(2,"Mean Delay",false);
                //calcolo d_i
                double delta = 1;
                if(this.FPResult.get(i).delta == BigDecimal.ZERO){
                    //caso iniziale 
                    delta = 1 / this.FPResult.get(i).MeanDelayResults[this.Tokens[i]];
                    this.FPResult.get(i).delta = BigDecimal.valueOf(delta);
                }
                
                this.ApproximateModel.getApxModel(i).setParams(Tokens[i], this.FPResult.get(i).lambda_i, this.FPResult.get(i).delta.doubleValue());
                for(int j=0;j<this.Tokens[i]+1;j++){
                    
                    RewardRate rw = RewardRate.fromString("If(WaitingAPX=="+j+",1,0)");
                    BigDecimal p = this.ApproximateModel.getApxModel(i).RegenerativeSteadyStateAnalysis(rw).get(rw);
                    this.FPResult.get(i).SteadyStateProbability[j] = p;  
                    
                    di = di.add(this.ApproximateModel.getApxModel(i).getServer().getMeanDelay(FPResult, i, j, p));
                    Ni = Ni.add(this.ApproximateModel.getApxModel(i).getServer().getWeights(j, i, p));
                }
                MeanDelay.setTitle("Calcolo di S"+i+"(N) | delta = "+this.FPResult.get(i).delta);
                MeanDelay.setHeader("d"+i,"N"+i);
                MeanDelay.setFormat("%.5f","%.5f");
                MeanDelay.addRecord(di,Ni );
                
                ress = MeanDelay.printTable();
                if(showInfo)System.out.println(ress);
                if(printOut) fileWriter.write(ress);
                if(showInfo && debug)
                    System.in.read();
                
                this.FPResult.get(i).d_i = this.ApproximateModel.getApxModel(i).getServer().getLast().getQueue().getSojournTime(di, Ni);
                this.FPResult.get(i).w_i = Ni;
                di = BigDecimal.ZERO;
                Ni = BigDecimal.ZERO;
            }
            
            //fine calcolo d_i
            
            
            //Calcolo Di//
            OutputTable DelaySum = new OutputTable(4, "Calcolo Di",false);
            DelaySum.setHeader("#","Di","Old Delta","New Delta");
            DelaySum.setFormat("","%.4f","%.5f","%.5f");
            for(int i=0;i<this.numQueue;i++){
                
                BigDecimal Di = BigDecimal.ZERO;
                Di = this.ApproximateModel.getApxModel(i).getServer().getDi(this.ApproximateModel.getApxModel(i),FPResult,i,numQueue);
                
                this.FPResult.get(i).D = Di;
                oldDelta[i] = this.FPResult.get(i).delta;
                this.FPResult.get(i).delta = BigDecimal.valueOf(1/Di.doubleValue());
                DelaySum.addRecord(i,Di,oldDelta[i],this.FPResult.get(i).delta);
            }
            
            //fine colcolo Di//
            ress = DelaySum.printTable();
            if(showInfo)System.out.println(ress);
            if(printOut) fileWriter.write(ress);
            for(int i=0;i<this.numQueue;i++){
                if(this.FPResult.get(i).delta.subtract(oldDelta[i]).abs().doubleValue() < epsilon) 
                    convergence = true;
            }
            if(showInfo && debug)
                System.in.read();
        }while(!convergence);
        
        //calcolo di E[Ri]
        
        
        double[] E_A = new double[numQueue];
        double[] steadyStateSolutionAPX = new double[numQueue];
        HashMap<String, Double> AppResultsMap = new HashMap<>();
        OutputTable AppResults = new OutputTable(2, "Calcolo E[Ri] di Si(N)",false); 
        AppResults.setHeader("#","E[Ri]");
        AppResults.setFormat("","%.7f");
        for(int i=0;i<numQueue;i++){
            
           
            this.ApproximateModel.getApxModel(i).setParams(this.Tokens[i], this.FPResult.get(i).lambda_i, this.FPResult.get(i).delta.doubleValue());
            RewardRate rw = RewardRate.fromString(this.FPResult.get(i).lambda_i+"*IdleAPX");
            RewardRate rw2 = RewardRate.fromString(reward+this.ApproximateModel.getApxModel(i).getName());
            double sstReward = this.ApproximateModel.getApxModel(i).RegenerativeSteadyStateAnalysis(rw2).get(rw2).doubleValue();
            steadyStateSolutionAPX[i] = sstReward;
            long inizio = System.currentTimeMillis();
            double Tput = this.ApproximateModel.getApxModel(i).RegenerativeSteadyStateAnalysis(rw).get(rw).doubleValue();
            long fine = System.currentTimeMillis();
            double a = Tokens[i] / Tput;
            double b = this.FPResult.get(i).lambda_i;
            double c = 1/b;
            E_A[i] = a - c; 
            AppResultsMap.put("er"+i, a-c);
            AppResultsMap.put(reward+i, sstReward);
            AppResults.addRecord(i,E_A[i]);
            System.out.println(fine +"-"+ inizio+"="+(fine-inizio)/1000.+"ms");
          
            
        }
        ress = AppResults.printTable();
        if(showInfo) System.out.println(ress);
        if(printOut) fileWriter.write(ress);
        
        
        
        
        OutputTable PollingResults = new OutputTable(2, "Calcolo E[Ri] Sul sistema di polling",false); 
        PollingResults.setHeader("#","E[Ri]");
        PollingResults.setFormat("","%.7f");
        HashMap<String, Double> PollingReesultsMap = new HashMap<>();
        double[] E_P = new double[numQueue];
        double[] steadyStateSolution = new double[numQueue];
        for(int i=0;i<numQueue;i++){
            int indexQ = (i==0?2:i-1);
            RewardRate rw = RewardRate.fromString(this.FPResult.get(i).lambda_i+"*Idle"+i);
            RewardRate rw2 = RewardRate.fromString(reward+indexQ);
            double sstReward = this.PollingModel.RegenerativeSteadyStateAnalysis(rw2).get(rw2).doubleValue();
            steadyStateSolution[i] = sstReward;
            long inizio  = System.currentTimeMillis();
            double Tput =  this.PollingModel.RegenerativeSteadyStateAnalysis(rw).get(rw).doubleValue();
            long fine = System.currentTimeMillis();
            double a = Tokens[i] / Tput;
            double b = this.FPResult.get(i).lambda_i;
            double c = 1/b;
            E_P[i] = a - c; 
            PollingReesultsMap.put("er"+i, a-c);
            PollingReesultsMap.put(reward+i, sstReward);
            PollingResults.addRecord(i,E_P[i]);
            System.out.println(fine +"-"+ inizio+"="+(fine-inizio)/1000.+"ms");
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
        HashMap<String,HashMap<String,Double>> results = new HashMap<>();
        results.put(ApproximateModel.getClass().getName(), AppResultsMap);
        results.put(PollingModel.getClass().getName(), PollingReesultsMap);
        return results;
        
    }
    
    
    
    
}
