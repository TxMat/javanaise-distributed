package Objects;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import jvn.Enums.ConsoleColor;
import jvn.Exceptions.JvnException;
import jvn.Impl.JvnServerImpl;
import jvn.Interfaces.JvnLocalServer;
import jvn.Interfaces.JvnObject;
import jvn.JvnInterceptor;

public class SerializedInterceptor implements Serializable, InvocationHandler /* Pour éviter une java.lang.ClassCastException */ {
    
    private final String jon;
    private static int cpt = 0;
    private int id=-1;
    public static final Map<String, Serializable> deserializedObject = new HashMap<>();
    
    public SerializedInterceptor(String jon) {
        this.jon = jon;
        this.id = cpt++;
        ConsoleColor.magicLog(ConsoleColor.toCyan("SerializedInterceptor créé avec jon="+jon+" [id:"+id+"]"));
    }
    
    private Object readResolve() throws ObjectStreamException {
        try {
            ConsoleColor.magicLog(this);
            JvnLocalServer server = JvnServerImpl.jvnGetServer(null);
            if (server == null) {
                // Cas coordinateur : on ne le transfo pas en JvnInterceptor et on le laisse en SerializedInterceptor 
                // (théoriquement on pourrait faire en sotre de récréer un JvnInterceptor mais c'est totalement inutile + compliqué)
                ConsoleColor.magicLog(ConsoleColor.toBlue("SerializedInterceptor non changer lors de readResolve (appel sur coordinateur)"));
                return this;
            }
            
            if(deserializedObject.containsKey(jon)) {
                ConsoleColor.magicLog(ConsoleColor.toPurple("Objet déjà deserialisé : "+jon));
                Object o = deserializedObject.get(jon);
                return o==null?this:o;
            }
            
            deserializedObject.put(jon, null);
            ConsoleColor.magicLog(ConsoleColor.toPurple(deserializedObject.keySet()));

            ConsoleColor.magicLog("Try JO lookup "+jon+" "+id+" "+cpt);
            JvnObject jo = server.jvnLookupObject(jon);
            ConsoleColor.magicLog("JO lookup ("+jon+") "+jo);

            deserializedObject.put(jon, jo);
            if (jo == null) throw new InvalidObjectException("Objet partagé '" + jon + "' non trouvé via lookup.");
            
            ConsoleColor.magicLog(ConsoleColor.toBlue("SerializedInterceptor changer lors de readResolve (appel sur un server)"));
            Object proxy = JvnInterceptor.createInterceptor(jo, jon, server);
            ConsoleColor.magicLog("Interseptor "+proxy);
            return Proxy.getInvocationHandler(proxy);
        } catch (JvnException e) {
            throw new InvalidObjectException("Erreur lors de la désérialisation de l'intercepteur pour " + jon + ": " + e.getMessage());
        }
    }
    
    @Override
    public String toString() {
        return "SerializedInterceptor:{ jon: "+jon+" }";
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("toString") && method.getParameterCount() == 0) {
            return this.toString();
        }
        throw new UnsupportedOperationException("Impossible d'invoke sur un proxy en mode serializé");
    }
}
