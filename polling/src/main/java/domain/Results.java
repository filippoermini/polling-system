package domain;

import java.math.BigDecimal;

public class Results {

    public double[] MeanDelayResults;
    public BigDecimal[] SteadyStateProbability;
    public BigDecimal d_i;
    public BigDecimal D;
    public BigDecimal delta;
    
    public Results(int Tokens){
        this.MeanDelayResults = new double[Tokens];
        this.SteadyStateProbability = new BigDecimal[Tokens];
        this.d_i = BigDecimal.ZERO;
        this.D = BigDecimal.ZERO;
        this.delta = BigDecimal.ZERO;
    }
    
   
    
    
        
}
