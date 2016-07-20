package run;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

import application.ApproximateModel;
import application.FixedPointModel;
import application.OutputTable;
import application.util;
import application.util.queuePolicy;
import domain.TransitionParams;
import feature_transition.TransitionManager;
import it.unifi.oris.sirio.models.stpn.RewardRate;

public class PollingRunner {

    public static void printUsage() {
        System.out.println("Usage:\nPolling Moldel Analysis by Fixed Point iteration \n"
                + "\t[-sPolicy <serverType>] [-qPolicy <queueType>...] [-numQueue <num>] [-t <num>...] [-l <num>...] [-p <num>...] [-K <num>...] [-ro <num>...]\n"
                + "\t[-mu <transitionType>,<params>...] [-gamma <transitionType>,<params>...] [-delta <transitionType>,<params>...] [-showInfo] [-debug]\n"
                + "\t[-ini <inifile>] [-o <OutputFile>]");
        System.out.println("\t -sPolicy: Server type: [SQ | PR | FP | UN]  SQ=Sequential, PR=Proportional to queue length, FP=Fixed priority, UF=Uniform weight");
        System.out.println("\t -qPolicy: Array of queue type (separated by commas): [EX | SS | KS | OP]  EX=Exhaustive, SS=Single service, KS=K-Shots, OP=Only present at arrival");
        System.out.println("\t -t: Array of token for each queue");
        System.out.println("\t -p: Array of value of simulated priority");
        System.out.println("\t -l: Array of value firing rate lambda_i (l_i*lambda)");
        System.out.println("\t -rho: Array of offered load");
        System.out.println("\t -mu: set type of mu. <transitionType> = [UNI | EXP | DET], <params> = list of the indicated transition parameter (defalut value: EXP,1.25)");
        System.out.println("\t -gamma: set type of mu. <transitionType> = [UNI | EXP | DET], <params> = list of the indicated transition parameter (default value: EXP,294.12)");
        System.out.println("\t -delta: set type of mu. <transitionType> = [UNI | EXP | DET], <params> = list of the indicated transition parameter (default value: EXP,1)");
        System.out.println("\t -numQueue: Numbers of queue");
        System.out.println("\t -showInfo: Show info during iteration");
        System.out.println("\t -debug: Enable debug mode (the system show info step by step)");
        System.out.println("\t -o: Print results in external file");
        System.out.println("\t -K: Array of K value");
        System.out.println("\t -ini <inifile>: Read params from INI file. Default INI file read is: ./model.ini");
        System.out.println("\tEach array should have the size of the number of queues");
    }
    
    public static void main(String[] args) throws IOException {
        
        int i=0;
        int numQ = 0;
        TransitionManager mu = TransitionManager.getIstance("EXP","1.25"); 
        TransitionManager gamma = TransitionManager.getIstance("EXP","294.12"); ;
        TransitionManager delta = TransitionManager.getIstance("EXP","1"); 
        String arg = "";
        util.queuePolicy[] qp = null;
        util.queueSelectionPolicy sp = null;
        String iniPath = "./model.ini"; 
        String outFile = "";
        String ext = "";
        
        boolean showInfo = false;
        boolean step = false;
        boolean ini = false;
        boolean output = false;
        boolean latex = false;
        boolean matlab = false;
        
        int[] K = null;
        int[] tokens = null;
        int[] prio = null;
        double lambda[] = null;
        double[] rho = null;
        
        Scanner scan = new Scanner(System.in);
        if(args.length==0){
            System.out.println("Arguments requires\n");
            printUsage();
            System.out.println("Load params from .ini file (y/n) ?");
            String res = scan.nextLine();
            if (res.contentEquals("y")) ini = true;
            else System.exit(-1);
        }
        scan.close();
        while (i < args.length && args[i].startsWith("-")) {
            arg = args[i++];
            if (arg.equals("-qPolicy")) {
                if (i < args.length){
                    String[] qplist = args[i++].split(",");
                    int k=0;
                    qp = new util.queuePolicy[qplist.length];
                    for(String q:qplist){
                        queuePolicy qpl = util.queuePolicy.getPolicyFromPrefix(q);
                        if(qpl==null) {
                            System.err.println("-qPolicy: requires only [EX | SS | KS | OP] separated by comma");
                            System.exit(-1);
                        }
                        qp[k] = qpl; 
                        k++;
                    }
                }
                else{
                    System.err.println("-qPolicy: requires an array of value [EX | SS | KS | OP]");
                    System.exit(-1);
                }
            } else if(arg.equals("-K")) {
                if (i < args.length){
                    String[] list = args[i++].split(",");
                    K = new int[list.length];
                    for(int j=0;j<list.length;j++){
                        try{
                            K[j] = Integer.parseInt(list[j]);
                        }catch(NumberFormatException ex){
                            System.err.println("-k: value must be integer");
                            System.exit(-1);
                        }
                    }
                }
                else{
                    System.err.println("-k: requires an array of integer values");
                    System.exit(-1);
                }
            } else if(arg.equals("-sPolicy")) {
                if (i < args.length){
                    sp = util.queueSelectionPolicy.getPolicyFromPrefix(args[i++]);
                    if(sp==null) System.err.println("-sPolicy: requires only [SQ | PR | FP | UF]");
                }
                else{
                    System.err.println("-sPolicy: requires a value [SQ | PR | FP | UF]");
                    System.exit(-1);
                }
            } else if(arg.equals("-p")) {
                if (i < args.length){
                    String[] list = args[i++].split(",");
                    prio = new int[list.length];
                    for(int j=0;j<list.length;j++){
                        try{
                            prio[j] = Integer.parseInt(list[j]);
                        }catch(NumberFormatException ex){
                            System.err.println("-p: value must be integer");
                            System.exit(-1);
                        }
                    }
                }
                else{
                    System.err.println("-p: requires an array of integer values");
                    System.exit(-1);
                }
            } else if(arg.equals("-t")) {
                if (i < args.length){
                    String[] list = args[i++].split(",");
                    tokens = new int[list.length];
                    for(int j=0;j<list.length;j++){
                        try{
                            tokens[j] = Integer.parseInt(list[j]);
                        }catch(NumberFormatException ex){
                            System.err.println("-t: value must be integer");
                            System.exit(-1);
                        }
                    }
                }
                else{
                    System.err.println("-t: requires an array of integer values");
                    System.exit(-1);
                }
            } else if(arg.equals("-l")) {
                if (i < args.length){
                    String[] list = args[i++].split(",");
                    lambda = new double[list.length];
                    for(int j=0;j<list.length;j++){
                        try{
                            lambda[j] = Double.parseDouble(list[j]);
                        }catch(NumberFormatException ex){
                            System.err.println("-l: value must be float");
                            System.exit(-1);
                        }
                    }
                }
                else{
                    System.err.println("-l: requires an array of float values");
                    System.exit(-1);
                }
            }else if(arg.equals("-rho")) {
                if (i < args.length){
                    String[] list = args[i++].split(",");
                    rho = new double[list.length];
                    for(int j=0;j<list.length;j++){
                        try{
                            rho[j] = Double.parseDouble(list[j]);
                        }catch(NumberFormatException ex){
                            System.err.println("-ro: value must be float");
                            System.exit(-1);
                        }
                    }
                }
                else{
                    System.err.println("-ro: requires an array of float values");
                    System.exit(-1);
                }
            } else if(arg.equals("-numQueue")) {
                if (i < args.length){
                    numQ = Integer.parseInt(args[i++]);
                }else{
                    System.err.println("-numQueue: requires a integer value");
                    System.exit(-1);
                }
            } else if(arg.equals("-mu")) {
                if (i < args.length){
                    String[] list = args[i++].split(",");
                    if (list.length<1)
                        throw new IllegalArgumentException("the parameters must be more than one");
                    mu = TransitionManager.getIstance(list);  
                }else{
                    System.err.println("-mu: requires a transition type and some parameters");
                    System.exit(-1);
                }
            } else if(arg.equals("-gamma")) {
                if (i < args.length){
                    String[] list = args[i++].split(",");
                    if (list.length<1)
                        throw new IllegalArgumentException("the parameters must be more than one");
                    gamma = TransitionManager.getIstance(list);  
                }else{
                    System.err.println("-gamma: requires a transition type and some parameters");
                    System.exit(-1);
                }
            } else if(arg.equals("-delta")) {
                if (i < args.length){
                    String[] list = args[i++].split(",");
                    if (list.length<1)
                        throw new IllegalArgumentException("the parameters must be more than one");
                    delta = TransitionManager.getIstance(list);  
                }else{
                    System.err.println("-delta: requires a transition type and some parameters");
                    System.exit(-1);
                }
            }else if(arg.equals("-o")) {
                if (i < args.length){
                    String filename = args[i++];
                    if (filename.replace(".", ",").split(",").length==2){
                        outFile = filename.replace(".", ",").split(",")[0];
                        ext = filename.replace(".", ",").split(",")[1];
                        output = true;
                    }else outFile = filename;    
                }else{
                    System.err.println("-o: requires a file name");
                    System.exit(-1);
                }
            }else if(arg.equals("-showInfo")) {
               showInfo = true;
            }else if(arg.equals("-debug")){
                step = true;
            }else if(arg.contains("-ini")){
                ini = true;
            }else if(arg.contains("-latex")){
                latex = true;
            }else if(arg.contains("-matlab")){
                matlab = true;
            }else{
                System.err.println("Unknown param: "+arg);
                printUsage();
                System.exit(-1);
            }
        }
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(iniPath));
        } catch (FileNotFoundException e1) {
            System.out.println("ERROR: can not find INI file: "+iniPath);
        } catch (IOException e1) {
            System.out.println("ERROR: can not find INI file: "+iniPath);
        }
        if(ini){
            boolean error = false;
            System.out.println("Load value from "+iniPath);
            
            if (props.getProperty("Server") != null) sp = util.queueSelectionPolicy.getPolicyFromPrefix(props.getProperty("Server"));
            else {  
                System.out.println("Missing or incorrect server value");
                error = true;
            }
            if (props.getProperty("outFile")!= null) outFile = props.getProperty("outFile");
            if (props.getProperty("Tokens") != null) {
                String[] tok = props.getProperty("Tokens").replace("[", "").replace("]", "").split(",");
                tokens = new int[tok.length];
                tokens = Arrays.asList(tok).stream().mapToInt(Integer::parseInt).toArray();
            }else{
                
            }
            if (props.getProperty("Queue") != null) {
                String[] q = props.getProperty("Queue").replace("[", "").replace("]", "").split(",");
                qp = new util.queuePolicy[q.length];
                for(int k=0;k<q.length;k++)
                    qp[k] = util.queuePolicy.getPolicyFromPrefix(q[k]);
            }
            if (props.getProperty("Prio") != null) {
                String[] pri = props.getProperty("Prio").replace("[", "").replace("]", "").split(",");
                prio = new int[pri.length];
                prio = Arrays.asList(pri).stream().mapToInt(Integer::parseInt).toArray();
            }
            if (props.getProperty("K") != null) {
                String[] k = props.getProperty("K").replace("[", "").replace("]", "").split(",");
                K = new int[k.length];
                K = Arrays.asList(k).stream().mapToInt(Integer::parseInt).toArray();
            }
            if (props.getProperty("Lambda") != null) {
                String[] l = props.getProperty("Lambda").replace("[", "").replace("]", "").split(",");
                lambda = new double[l.length];
                lambda = Arrays.asList(l).stream().mapToDouble(Double::parseDouble).toArray();
            }
            if (props.getProperty("rho") != null) {
                String[] r = props.getProperty("rho").replace("[", "").replace("]", "").split(",");
                rho = new double[r.length];
                rho = Arrays.asList(r).stream().mapToDouble(Double::parseDouble).toArray();
            }
            if (props.getProperty("numQueue") != null) numQ = Integer.parseInt(props.getProperty("numQueue"));
            if (props.getProperty("mu") != null){
                String[] m = props.getProperty("mu").replace("[", "").replace("]", "").split(",");
                if (m.length<1)
                    throw new IllegalArgumentException("the parameters must be more than one");
                mu = TransitionManager.getIstance(m);  
            }
            if (props.getProperty("gamma") != null){
                String[] g = props.getProperty("gamma").replace("[", "").replace("]", "").split(",");
                if (g.length<1)
                    throw new IllegalArgumentException("the parameters must be more than one");
                gamma = TransitionManager.getIstance(g);
            }
            if (props.getProperty("delta") != null){
                String[] d = props.getProperty("delta").replace("[", "").replace("]", "").split(",");
                if (d.length<1)
                    throw new IllegalArgumentException("the parameters must be more than one");
                delta = TransitionManager.getIstance(d);
            }
        }
        FixedPointModel fp;
        String Reward = "Waiting";
        OutputTable table = new OutputTable(5,"Risultati Mean Response Time",latex);
        HashMap<String, HashMap<String,Double>> results;
        table.setHeader("rho","Q","Si(N)","Polling System","error");
        table.setFormat("","","%.7f","%.7f","%.2f%%");
        String matFile = "";
        for(int j=0;j<rho.length;j++){
            
            fp = new FixedPointModel(sp, qp, numQ, tokens, K, prio, showInfo, step, mu ,gamma, delta, lambda,rho[j], outFile+"_"+j+"."+ext);
            results = fp.fixedPointIteration(Reward);
            HashMap<String,Double> appResults = results.get(fp.getApproximateModel().getClass().getName());
            HashMap<String,Double> pollingResults = results.get(fp.getPollingModel().getClass().getName());
            String row = "";
            for (int k=0;k<numQ;k++){
                row+=rho[j]+",";
                double erAPX = appResults.get("er"+k);
                double erPolling = pollingResults.get("er"+k);
                double error = (Math.abs(erAPX - erPolling))*100/erPolling;
                row+=erPolling+","+erAPX+","+error+"\n";
                String rewardString = Reward+k;
                double rewAPX = appResults.get(rewardString);
                double rewPolling = pollingResults.get(rewardString);
                double errorRew = (Math.abs(rewAPX - rewPolling))*100/rewPolling;
                table.addRecord(rho[j],qp[k].getPrefix(),erAPX,erPolling,error);//,rewAPX,rewPolling,errorRew);
            }
            matFile+=row;
            table.addSeparator();
            
            
            
        }
        String res = table.printTable();
        String qlist = "[";
        for(queuePolicy q:qp)
            qlist += q.getPrefix()+",";
        qlist = qlist.substring(0, qlist.length()-1)+"]";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String execution_info = "Queue Selection Policy:"+sp.toString()+" Queue Type:"+qlist.toString()+" K:"+Arrays.toString(K)+" num Queue:"+numQ+" Token:"+Arrays.toString(tokens)+(sp==util.queueSelectionPolicy.SIMULATED_PRIORITY?" Weights:"+Arrays.toString(prio):"");
        
        System.out.println(res);
        if(outFile!= ""){
            File file = new File(outFile+"Res."+ext);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(sdf.format(new Date())+"\n"+execution_info+"\n"+res);
            fileWriter.flush();
            fileWriter.close();
            if(matlab){
                File fileMAt = new File(outFile+"MAT."+ext);
                FileWriter fileWriterMat = new FileWriter(fileMAt);
                fileWriterMat.write(matFile);
                fileWriterMat.flush();
                fileWriterMat.close();
            }
        }
        
        
    }
    private static boolean containsQueue(util.queuePolicy[] qpList, util.queuePolicy qp){
        for(util.queuePolicy q:qpList)
            if (q==qp) return true; 
        return false;
    }
        
    
}
