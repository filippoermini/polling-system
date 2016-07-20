package domain;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;

import application.ApproximateModel;
import application.PetriNetModel;
import application.util;
import feature_transition.TransitionManager;
import it.unifi.oris.sirio.petrinet.Marking;
import it.unifi.oris.sirio.petrinet.PetriNet;

public abstract class Server {

    protected ArrayList<Service> serviceList;
    protected TransitionManager gamma;
   
    
    public Server(TransitionManager gamma){
        
        this.serviceList = new ArrayList<Service>();
        this.gamma = gamma;
        
    }
    
    public abstract void create(PetriNet pn, Marking m);
    public abstract void addService(PetriNet pn, Marking m, int index, String serviceName);
    public abstract void linkApproximate(PetriNet pn,Service s, Approximate a);
    public abstract void addAbsorbentPlace(PetriNet pn,Service s);
    public abstract BigDecimal getMeanDelay(ArrayList<Results> res, int index, int k, BigDecimal P);
    public abstract BigDecimal getWeights(int k,int indexQ, BigDecimal P);
    public abstract String getOutpuString(int index);
    public abstract BigDecimal getDi(ApproximateModel.ApproximateNet pm, ArrayList<Results> res, int index, int numQueue);
    
    public TransitionManager getGamma(){
        return gamma;
    }
    
    public Service getServiceAtIndex(int index){
        if (!serviceList.isEmpty())
            return serviceList.get(index);
        return null;
    }
    public Service getLast(){
        if(!serviceList.isEmpty())
            return serviceList.get(serviceList.size()-1);
        return null;
    }
    
    
    public static Server getServer(util.queueSelectionPolicy serverType, Object... params) {
        Class c;
        try {
            String className = serverType.getClassName();
            c = Class.forName("domain."+className);
            Class[] paramType = new Class[params.length];
            Object[] param = new Object[params.length];
            int index = 0;
            if (paramType.length !=0){
                for (Object p: params){
                    paramType[index] = p.getClass().toString().contains("feature_transition")?p.getClass().getSuperclass():p.getClass();
                    param[index] = p;
                    index++;
                }
                Constructor cons = c.getConstructor(paramType);
                return (Server) cons.newInstance(param);
            }else{
                Constructor cons = c.getConstructor();
                return (Server) cons.newInstance();
            }
        } catch (ClassNotFoundException     | 
                InstantiationException      | 
                IllegalAccessException      | 
                NoSuchMethodException       | 
                SecurityException           | 
                IllegalArgumentException    | 
                InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
