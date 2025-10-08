package jvn;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JvnInterseptor implements InvocationHandler, Serializable {
    
    public static Object createInterseptor(Serializable o) {
        return Proxy.newProxyInstance(
            o.getClass().getClassLoader(),
            o.getClass().getInterfaces(),
            new JvnInterseptor(o)
        );
    }
    
    private Serializable o;
    
    private JvnInterseptor(Serializable o) {
        this.o = o;
    }
    
    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        return m.invoke(o, args);
    }

    public Serializable getSerializable() {
        return o;
    }

    public void updateObject(Serializable o) {
        this.o = o;
    }

}