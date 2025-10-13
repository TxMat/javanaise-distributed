package jvn;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import jvn.Exceptions.JvnException;
import jvn.Interfaces.JvnLocalServer;
import jvn.Interfaces.JvnObject;

public class JvnInterceptor implements InvocationHandler, Serializable {
    
    public static Object createInterceptor(Serializable s, String jon, JvnLocalServer server) throws JvnException {
        JvnObject jo = server.jvnCreateObject(s);
        server.jvnRegisterObject(jon, jo);
        return Proxy.newProxyInstance(
            s.getClass().getClassLoader(),
            s.getClass().getInterfaces(),
            new JvnInterceptor(jo, server)
        );
    }
    
    private JvnObject jo;
    private JvnLocalServer server;
    
    private JvnInterceptor(JvnObject jo, JvnLocalServer server) {
        this.jo = jo;
        this.server = server;
    }
    
    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        return m.invoke(jo.jvnGetSharedObject(), args);
    }
}