package jvn.test_custom_seria;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import jvn.Enums.ConsoleColor;
import jvn.Exceptions.JvnException;
import jvn.Interfaces.JvnLocalServer;
import jvn.Interfaces.JvnObject;
import jvn.JvnInterceptor;

public class S2_Impl implements S2 {
    
    private final Map<String, Serializable> map = new HashMap<>();
    
    private transient JvnLocalServer server;
    public S2_Impl(JvnLocalServer server) {
        this.server = server;
    }
    public void setServer(JvnLocalServer server) {
        this.server = server;
    }
    
    @Override
    public boolean add(String name, Serializable s) {
        if(map.containsKey(name)) return false;
        map.put(name, s);
        return true;
    }
    
    @Override
    public Serializable get(String name) {
        return map.get(name);
    }
    
    @Override
    public Serializable size() {
        return map.size();
    }
    
    @Override
    public Serializable remove(String name) {
        return map.remove(name);
    }
    
    @Override
    public String toSting() {
        StringBuilder sb = new StringBuilder();
        map.forEach((k,v) -> {
            sb.append("{ ").append(k).append(" => ").append(v.toString()).append(" }\n");
        });
        int lg = sb.length();
        if(lg > 0) sb.deleteCharAt(lg-1);
        return sb.toString();
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException {
        // default serialization 
        oos.defaultWriteObject();
        // write the object
        
        for (Map.Entry<String, Serializable> en : map.entrySet()) {
            String k = en.getKey();
            Serializable v = en.getValue();
            boolean isJvnObject = v instanceof java.lang.reflect.Proxy;
            
            // System.out.println(interceptors.get("a") instanceof java.lang.reflect.Proxy);
            
            oos.writeBoolean(isJvnObject);
            oos.writeObject(k);
            oos.writeObject(isJvnObject?null:v);
        }
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        // default deserialization
        ois.defaultReadObject();
        try {
            while(true) {
                boolean isJvnObject = ois.readBoolean();
                String name = (String) ois.readObject();
                
                if(!isJvnObject) {
                    map.put(name, (Serializable) ois.readObject());
                } else {
                    try {
                        JvnObject jo = server.jvnLookupObject(name);
                        map.put(name, jo==null?null:JvnInterceptor.createInterceptor(jo, name, server));
                    } catch (JvnException e) {
                        ConsoleColor.magicError("ERROR lors de la récupération de l'objet "+name+" : "+e.getMessage());
                    }
                    ois.readObject(); // null
                }
            }
        } catch(OptionalDataException e) {/* END */}
        
    }
}