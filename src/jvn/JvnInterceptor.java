package jvn;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import Objects.SerializedInterceptor;
import jvn.Annotations.JvnAnnotate;
import jvn.Enums.ConsoleColor;
import jvn.Exceptions.JvnException;
import jvn.Interfaces.JvnLocalServer;
import jvn.Interfaces.JvnObject;

public class JvnInterceptor implements InvocationHandler, Serializable {
    
    public static <T> T createInterceptor(Serializable s, String jon, JvnLocalServer server) throws JvnException {
        if(s == null || jon == null || server == null || jon.isEmpty()) throw new JvnException("Impossible de créer un intersepteur : parametre null ou nom d'objet vide");
        
        JvnObject jo;
        // 2 cas : on créé un objet de 0 ou on le récupère du coord
        if(s instanceof JvnObject joS) {
            // Si s est un JvnObject => déjà register sur le coord => juste créer l'interseptor local
            jo = server.jvnCreateObject(s);
            s = joS.jvnGetSharedObject();
        } else {
            // Sinon on créer le JvnObject + Register
            jo = server.jvnCreateObject(s);
            server.jvnRegisterObject(jon, jo);
        }
        
        return (T) createProxy(s, jo, jon);
    }
    private static <U> U createProxy(U s, JvnObject jo, String jon){
        return (U) Proxy.newProxyInstance(
        s.getClass().getClassLoader(),
        s.getClass().getInterfaces(),
        new JvnInterceptor(jo, jon)
        );
    }
    
    private JvnObject jo;
    private String jon;
    
    private JvnInterceptor(JvnObject jo, String jon) {
        this.jo = jo;
        this.jon = jon;
    }
    public JvnObject getJvnObejct() {
        return jo;
    }
    
    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        /*sysout*/ // ConsoleColor.magicLog(ConsoleColor.toGreen(m.getName()+" "+Arrays.toString(args)), false);
        boolean needLock = m.isAnnotationPresent(JvnAnnotate.class);
        /*sysout*/ // System.out.print(ConsoleColor.toGreen(", needLock : "+needLock));
        
        if (needLock) {
            boolean isReader = m.getAnnotation(jvn.Annotations.JvnAnnotate.class).value() == JvnAnnotate.LockType.READ;
            /*sysout*/ // System.out.print(ConsoleColor.toGreen(", isReader : "+isReader));
            if (isReader) {
                jo.jvnLockRead();
            } else {
                jo.jvnLockWrite();
            }
        }
        /*sysout*/ // System.out.println();
        
        // il y avais une erreur mais je sais plus pk j'avais mis ca, donc je remet tant que j'ai pas d'erreur, elle a peut etre été corrigé "naturlmement" depuis avec mes modif de code
        // if(m.getName().equals("toString") && jo == null) return "null";
        
        Object res = m.invoke(jo.jvnGetSharedObject(), args);
        if (needLock) jo.jvnUnLock();
        
        return res;
    }
    /*
    private void writeObject(ObjectOutputStream oos) throws IOException {
        // default serialization 
        oos.defaultWriteObject();
        
        // oos.writeObject(jo==null?null:jon);
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        String jon = (String) ois.readObject();
        if(jon != null) {
            try {
                JvnLocalServer server = JvnServerImpl.jvnGetServer(null);
                this.jo = server==null?null:server.jvnLookupObject(jon);
                this.jon = jon;
            } catch (JvnException e) {
                ConsoleColor.magicError("ERROR reading a JvnInterceptor : " + e.getMessage());
            }
        }
        ConsoleColor.magicLog(ConsoleColor.toYellow(this.toString()));
    }
    */
    
    private Object writeReplace() throws ObjectStreamException {
        // Ne jamais serialiser le JvnInterceptor (pas besoin de readObejct car il est jamais réelement serialisé en tant que JvnInterceptor)
        // On garde que le nom pour le recréer quand il est sur un server
        ConsoleColor.magicLog("writeReplace : "+jon);
        SerializedInterceptor si = new SerializedInterceptor(jon);
        ConsoleColor.magicLog(ConsoleColor.toYellow(si.toString()));
        return si;
    }
    
    
    @Override
    public String toString(){
        return "JvnInterseptor{ jon: "+jon+", jo:"+(jo==null?"null":jo.toString())+" }";
    }
}