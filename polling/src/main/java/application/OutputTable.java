package application;

import java.util.ArrayList;
import java.util.Formatter;

import org.apache.commons.math3.analysis.function.Max;

public class OutputTable {
    
    private String[] Header;
    private ArrayList<String[]> Record;
    private String[] Format;
    private String Title;
    private int[] maxDim;
    private int col;
    
    public OutputTable(int numCol,String title){
        this.col = numCol;
        this.Header = new String[col];
        this.Format = new String[col];
        this.maxDim = new int[col];
        for(int i=0;i<col;i++)
            maxDim[i] = Integer.MIN_VALUE;
        this.Record = new ArrayList<>(); 
        this.Title = title;
    }
    public void setFormat(String...format){
        if(format.length==col) 
            Format = format;
    }
    public void setHeader(String...format){
        if(format.length==col) 
            Header = format;
    }
    public void setTitle(String title){
        this.Title = title;
    }
    public void addRecord(Object...objects){
        
        if(objects.length == col){
            String[] rec = new String[col];
            for(int i=0;i<col;i++){
                String s = objects[i].toString();
                Formatter formatter = new Formatter();
                if(Format[i] != "")
                    s = formatter.format(Format[i], objects[i]).toString();
                rec[i] = s;
                maxDim[i] = Math.max(maxDim[i],rec[i].length());
                formatter.close();
            }
            this.Record.add(rec);
        }
    }
    
    public String printTable(){
        String out = "";
        int[] colDim = new int[col];
        int lineDim = col+1+(col*2);
        for(int i=0;i<col;i++){
            colDim[i] = Math.max(Header[i].length(), maxDim[i]);
            lineDim+=colDim[i];
        }
        String separator = "";
        for(int i=0;i<lineDim;i++)
            separator+= "-";
        //System.out.println(this.Title);
        out += this.Title +"\n";
        //System.out.println(separator);
        out += separator +"\n";
        //costruzione header
        if (!this.headerIsEmpty()){
            String head = "|";
            for(int i=0;i<col;i++){
                String pre = getOffset((colDim[i]+2-Header[i].length()) / 2);
                String suff = getOffset((int)Math.round((double)(colDim[i]+2-Header[i].length()) / 2));
                head += pre+Header[i]+suff+"|";
            }
            //System.out.println(head);
            out += head +"\n";
            //costruzione body
            //System.out.println(separator);
            out += separator + "\n";
        }
        for(String[] record:Record){
            String row = "|";
            for(int i=0;i<col;i++){
                String pre = getOffset((colDim[i]+2-record[i].length()) / 2);
                String suff = getOffset((int)Math.round((double)(colDim[i]+2-record[i].length()) / 2));
                row += pre+record[i]+suff+"|";
            }
            //System.out.println(row);
            out += row +"\n";
        }
        //System.out.println(separator);
        out += separator +"\n";
        return out;
         
    }
    private String getOffset(int off){
        String offset = "";
        for(int i=0;i<off;i++)
            offset += " ";
        return offset;
    }
    private boolean headerIsEmpty(){
        boolean empty = true;
        for(String s:Header)
            if(!s.isEmpty()) return false;
        return empty;
    }

}
