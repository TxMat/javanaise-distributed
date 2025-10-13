package jvn;

import jvn.Annotations.JvnAnnotate;
import jvn.Exceptions.JvnException;
import jvn.Interfaces.JvnLocalServer;
import jvn.Interfaces.JvnObject;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JvnInterceptor implements InvocationHandler, Serializable {

    public static Object createInterceptor(Serializable s, String jon, JvnLocalServer server) throws JvnException {
        JvnObject jo = server.jvnCreateObject(s);
        server.jvnRegisterObject(jon, jo);
        return Proxy.newProxyInstance(
                s.getClass().getClassLoader(),
                s.getClass().getInterfaces(),
                new JvnInterceptor(jo)
        );
    }

    private JvnObject jo;

    private JvnInterceptor(JvnObject jo) {
        this.jo = jo;
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        if (m.isAnnotationPresent(JvnAnnotate.class)) {
            JvnAnnotate annotate = m.getAnnotation(jvn.Annotations.JvnAnnotate.class);
            if (annotate.value() == JvnAnnotate.LockType.READ) {
                jo.jvnLockRead();
            } else if (annotate.value() == JvnAnnotate.LockType.WRITE) {
                jo.jvnLockWrite();
            }
        }
        Object res = m.invoke(jo.jvnGetSharedObject(), args);
        if (m.isAnnotationPresent(JvnAnnotate.class)) {
            jo.jvnUnLock();
        }
        return res;
    }
}