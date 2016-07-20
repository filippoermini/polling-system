package domain;

import java.util.HashMap;

public class TransitionParams {

    private HashMap<String,String> params;
    
    public TransitionParams(){
        params = new HashMap<>();
    }
    public TransitionParams(String[] param){
        params = new HashMap<>();
        if (param.length%2!=0)
            throw new IllegalArgumentException("the parameters must be couples <key,value>");
        for(int k=0;k<param.length;k=k+2){
            this.setParmas(param[k], param[k+1]);
        }
    }
    public void setParmas(String key,String value){
        if(!params.containsKey(key))
            params.put(key, value);
    }
    public String getParams(String key){
        return this.params.get(key);
    }
    public double getValueTransition(){
        String type = getParams("TYPE");
        switch(type){
        case "EXP":
            return Double.parseDouble(getParams("LAMBDA"));
        case "DET":
            return Double.parseDouble(getParams("VALUE"));
        case "UNI":
            return (Double.parseDouble(getParams("LFT"))+Double.parseDouble(getParams("EFT")))/2;
        }
        return 1;
    }
}
