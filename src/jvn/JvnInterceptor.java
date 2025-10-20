package jvn;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import jvn.Annotations.JvnAnnotate;
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
        
        return (T) createProxy(s, jo);
    }
    private static <U> U createProxy(U s, JvnObject jo){
         return (U) Proxy.newProxyInstance(
            s.getClass().getClassLoader(),
            s.getClass().getInterfaces(),
            new JvnInterceptor(jo)
        );
    }
    
    private final JvnObject jo;
    
    private JvnInterceptor(JvnObject jo) {
        this.jo = jo;
    }
    
    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        boolean needLock = m.isAnnotationPresent(JvnAnnotate.class);

        if (needLock) {
            boolean isReader = m.getAnnotation(jvn.Annotations.JvnAnnotate.class).value() == JvnAnnotate.LockType.READ;
            if (isReader) {
                jo.jvnLockRead();
            } else {
                jo.jvnLockWrite();
            }
        }

        Object res = m.invoke(jo.jvnGetSharedObject(), args);
        if (needLock) jo.jvnUnLock();
        
        return res;
    }
}