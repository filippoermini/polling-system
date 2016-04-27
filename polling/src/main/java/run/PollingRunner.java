package run;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Properties;
import java.util.Scanner;
import application.ApproximateModel;
import application.FixedPointModel;
import application.util;
import it.unifi.oris.sirio.models.stpn.RewardRate;

public class PollingRunner {

    public static void printUsage() {
        System.out.println("Usage:\n PollingMoldelAnalysis [-sPolicy <serverType>] [-qPolicy <queueType>] [-numQueue <num>] [-showInfo] [-debug] [-ini] [-o <OutputFile>]");
        System.out.println("\t -sPolicy: Server type: [SQ | PR | FP] - Sequential, Proportional to queue length, Fixed priority (need additional priority values)");
        System.out.println("\t -qPolicy: Queue type: [EX | KS | OP] - Exaustive, K-Shots (need additional value of K), Only present at arrival");
        System.out.println("\t -numQueue: numbers of queue");
        System.out.println("\t -showInfo: Show info during iteration");
        System.out.println("\t -debug: Enable debug mode (the system show info step by step)");
        System.out.println("\t -o: Print results in file");
        System.out.println("\t Additional values will be required later...");
    }
    
    public static void main(String[] args) throws IOException {
        
        int i=0;
        int numQ = 0;
        int K = 0;
        double mu = 1;
        double gamma = 1;
        String arg = "";
        util.queuePolicy qp = null;
        util.queueSelectionPolicy sp = null;
        String iniPath = "./model.ini"; 
        String outFile = "";
        String ext = "";
        
        boolean showInfo = false;
        boolean step = false;
        boolean ini = false;
        boolean output = false;
        
        Scanner scan = new Scanner(System.in);
        if(args.length==0){
            System.err.println("Arguments requires\n");
            
            printUsage();
            System.out.println("Load params from .ini file (y/n) ?");
            String res = scan.nextLine();
            if (res.contentEquals("y")) ini = true;
            else System.exit(-1);
        }
        while (i < args.length && args[i].startsWith("-")) {
            arg = args[i++];
            if (arg.equals("-sPolicy")) {
                if (i < args.length){
                    sp = util.queueSelectionPolicy.getPolicyFromPrefix(args[i++]);
                    if(sp==null) System.err.println("-sPolicy: requires only [EX | KS | OP]");
                }
                else{
                    System.err.println("-sPolicy: requires a value [EX | KS | OP]");
                    System.exit(-1);
                }
            } else if(arg.equals("-qPolicy")) {
                if (i < args.length){
                    qp = util.queuePolicy.getPolicyFromPrefix(args[i++]);
                    if(sp==null) System.err.println("-sPolicy: requires only [SQ | PR | FP]");
                }
                else{
                    System.err.println("-qPolicy: requires a value [SQ | PR | FP]");
                    System.exit(-1);
                }
            } else if(arg.equals("-numQueue")) {
                if (i < args.length){
                    numQ = Integer.parseInt(args[i++]);
                }else{
                    System.err.println("-numQueue: requires a integer value");
                    System.exit(-1);
                }
            }else if(arg.equals("-o")) {
                if (i < args.length){
                    String filename = args[i++];
                    if (filename.replace(".", ",").split(",").length==2){
                        outFile = filename.replace(".", ",").split(",")[0];
                        ext = filename.replace(".", ",").split(",")[1];
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
            }else{
                System.err.println("Unknown param: "+arg);
                printUsage();
                System.exit(-1);
            }
        }
        int[] tokens = null;
        int[] prio = null;
        double lambda[] = null;
        double[] ro = null;
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(iniPath));
        } catch (FileNotFoundException e1) {
            System.out.println("ERROR: can not find INI file: "+iniPath);
        } catch (IOException e1) {
            System.out.println("ERROR: can not find INI file: "+iniPath);
        }
        if(ini){
            System.out.println("Load value from "+iniPath);
            
            if (props.getProperty("Server") != null) sp = util.queueSelectionPolicy.getPolicyFromPrefix(props.getProperty("Server"));
            if (props.getProperty("Queue")  != null) qp = util.queuePolicy.getPolicyFromPrefix(props.getProperty("Queue"));
            if (props.getProperty("outFile")!= null) outFile = props.getProperty("outFile");
            if (props.getProperty("Tokens") != null) {
                String[] tok = props.getProperty("Tokens").replace("[", "").replace("]", "").split(",");
                tokens = new int[tok.length];
                tokens = Arrays.asList(tok).stream().mapToInt(Integer::parseInt).toArray();
            }
            if (props.getProperty("Prio") != null) {
                String[] pri = props.getProperty("Prio").replace("[", "").replace("]", "").split(",");
                prio = new int[pri.length];
                prio = Arrays.asList(pri).stream().mapToInt(Integer::parseInt).toArray();
            }
            if (props.getProperty("Lambda") != null) {
                String[] l = props.getProperty("Lambda").replace("[", "").replace("]", "").split(",");
                lambda = new double[l.length];
                lambda = Arrays.asList(l).stream().mapToDouble(Double::parseDouble).toArray();
            }
            if (props.getProperty("ro") != null) {
                String[] r = props.getProperty("ro").replace("[", "").replace("]", "").split(",");
                ro = new double[r.length];
                ro = Arrays.asList(r).stream().mapToDouble(Double::parseDouble).toArray();
            }
            if (props.getProperty("numQueue") != null) numQ = Integer.parseInt(props.getProperty("numQueue"));
            if (props.getProperty("K") != null) K = Integer.parseInt(props.getProperty("K"));
            if (props.getProperty("mu") != null) mu = Double.parseDouble(props.getProperty("mu"));
            if (props.getProperty("gamma") != null) gamma = Double.parseDouble(props.getProperty("gamma"));
            
        }
        else{
            
            System.out.println("Insert a vector of "+numQ+" tokens (use comma to split)");
            tokens = new int[numQ];
            boolean error = false;
            do{
                String line = scan.nextLine();
                error = false;
                try{
                    tokens = Arrays.asList(line.split(",")).stream().mapToInt(Integer::parseInt).toArray();
                    if(tokens.length<numQ) {
                        error = true;
                        System.err.println("Insert "+numQ+ " values");
                    }
                    if(Arrays.asList(tokens).contains(0)){
                        error = true;
                        System.err.println("Values must be > 0");
                    }
                }catch(NumberFormatException ex){
                    System.err.println("Token values must be integer");
                    error = true;
                }
            }while(error);
            if(qp==util.queuePolicy.KSHOTS){
                error = false;
                System.out.println("Insert value of K");
                do{
                    try{
                        K = scan.nextInt();
                    }catch(NumberFormatException ex){
                        error = true;
                        System.err.println("K must be integer");
                    }
                }while(error);
            }
            prio = new int[numQ];
            if(sp==util.queueSelectionPolicy.FIXED_PRIORITY){
                error = false;
                System.out.println("Insert a priority vector of "+numQ+" values  (use comma to split)");
                do{
                    String line = scan.nextLine();
                    try{
                        prio = Arrays.asList(line.split(",")).stream().mapToInt(Integer::parseInt).toArray();
                        if(prio.length<numQ) {
                            error = true;
                            System.err.println("Insert "+numQ+ " values");
                        }
                    }catch(NumberFormatException ex){
                        System.err.println("Priority values must be integer");
                        error = true;
                    }
                }while(error);
            }
            
        }
        FixedPointModel fp;
        double[][] results = new double[ro.length][2]; 
        Formatter formatter = new Formatter();
        String s = "";
        String res = "----------------------------------------------------------\n";
        res+="| ro  |\tModel Si(N)\t|\tPolling System\t| error\t|\n";
        res+= "----------------------------------------------------------\n";
        
        for(int j=0;j<ro.length;j++){
            
            fp = new FixedPointModel(sp, qp, numQ, tokens, K, prio, showInfo, step, mu ,gamma,lambda,ro[j], outFile+j+"."+ext);
            results[j] = fp.fixedPointIteration();
            double error = (Math.abs(results[j][0] - results[j][1]))*100/results[j][1];
            s = formatter.format("| %.1f |\t%.7f\t|\t%.7f\t| %.3f\t|\n",ro[j],results[j][0],results[j][1],error).toString();
            
        }
        res += s;
        res+= "----------------------------------------------------------\n";
        System.out.println(res);
        formatter.close();
        if(outFile!= ""){
            File file = new File(outFile+"Res."+ext);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(res);
            fileWriter.flush();
            fileWriter.close();
        }
        
        
    }
        
    
}
