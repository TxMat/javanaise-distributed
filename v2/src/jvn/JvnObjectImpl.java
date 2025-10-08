package jvn;

import java.io.Serializable;
import java.lang.reflect.Proxy;

public class JvnObjectImpl implements JvnObject {
    
    private final Object proxy;
    private final int id;
    
    private final transient Object lockLockStatus = new Object();
    private int lockStatus;
    // transient = n'est pas serialisé et est mis a "null" de l'autre coté. Merci les dev Java
    private final transient JvnLocalServer server; 

    private final transient Object antiDL = new Object();
    private boolean antiDL_RC_to_W = false;
    
    public JvnObjectImpl(Serializable o, int id, JvnLocalServer server) {
        this.proxy = JvnInterseptor.createInterseptor(o);
        this.id = id;
        this.lockStatus = JvnObject.NL;
        this.server = server;
    }
    
    @Override
    public void jvnLockRead() throws JvnException {
        synchronized (lockLockStatus) {
            String bf = "jvnLockRead : [ "+id+" ] lockStatus( "+ gls(lockStatus);
            System.out.println(bf);
            switch (lockStatus) {
                case JvnObject.WC -> lockStatus = JvnObject.RWC;
                case JvnObject.RC -> lockStatus = JvnObject.R;
                case JvnObject.NL -> {
                    server.jvnLockRead(id);
                    lockStatus = JvnObject.R;
                }
                default -> throw new JvnException("jvnLockRead -> lockStatus non attendu : " + gls(lockStatus));
            }
            System.out.println("[ "+System.currentTimeMillis()+" ] "+bf+" => "+ gls(lockStatus)+" )");
        }
    }
    
    @Override
    public void jvnLockWrite() throws JvnException {
        synchronized (lockLockStatus) {
            String bf = "jvnLockWrite : [ "+id+" ] lockStatus( "+ gls(lockStatus);
            System.out.println(bf);
            switch (lockStatus) {
                case JvnObject.WC -> lockStatus = JvnObject.W;
                case JvnObject.RC ->
                // TODO : ^^^^^^^^ C'est ici le début de la merde avec l'interbloquage
                {
                    // throw new AssertionError("INTERBLOQUAGE POSSIBLE");
                    /*
                    synchronized (antiDL) {
                        antiDL_RC_to_W = true;
                    }*/
                    server.jvnLockWrite(id);
                    lockStatus = JvnObject.W;
                } 
                    
                case JvnObject.NL -> {
                    server.jvnLockWrite(id);
                    lockStatus = JvnObject.W;
                }
                default -> throw new JvnException("jvnLockWrite -> lockStatus non attendu : " + gls(lockStatus));
            }
            System.out.println("[ "+System.currentTimeMillis()+" ] "+bf+" => "+ gls(lockStatus)+" )");
        }
    }
    
    @Override
    public void jvnUnLock() throws JvnException {
        synchronized (lockLockStatus) {
            String bf = "jvnUnLock : [ "+id+" ] lockStatus( "+ gls(lockStatus);
            System.out.println(bf);
            switch(lockStatus) {
                case JvnObject.R -> lockStatus = JvnObject.RC;
                case JvnObject.W, JvnObject.RWC -> lockStatus = JvnObject.WC;
                // default -> throw new AssertionError("Unknown case on jvnUnLock : lockStatus = " + gls(lockStatus));
                /*

                LockW
                ...
                Unlock

                PROBLEME : 

                LockW
                ...
                    <- InvalidateWriteForRead
                    -> lock go from W to RC
                Unlock
                */
            }
            System.out.println("[ "+System.currentTimeMillis()+" ] "+bf+" => "+ gls(lockStatus)+" )  & notifyAll");
            lockLockStatus.notifyAll(); // Réveille les threads qui attendent sur un lock pour le Coord
        }
    }
    
    @Override
    public void updateSerializable(Serializable s) {
        ((JvnInterseptor) Proxy.getInvocationHandler(proxy)).updateObject(s);
    }
    
    @Override
    public int jvnGetObjectId() throws JvnException {
        return id;
    }
    
    @Override
    public Serializable jvnGetSharedObject() throws JvnException {
        return ((JvnInterseptor) Proxy.getInvocationHandler(proxy)).getSerializable();
    }
    
    @Override
    public void jvnInvalidateReader() throws JvnException {/*
        synchronized (antiDL) {
            if(antiDL_RC_to_W) return;
        }*/
        synchronized (lockLockStatus) { 
            String bf = "InvalidateReader : [ "+id+" ] lockStatus( "+ gls(lockStatus);
            System.out.println(bf);
            switch (lockStatus) {
                case JvnObject.RC -> lockStatus = JvnObject.NL;
                case JvnObject.R -> {
                    while (lockStatus == JvnObject.R) {
                        try {
                            System.out.println("[ "+System.currentTimeMillis()+" ] "+"WAITING FOR UNLOCK [ "+id+" ] lockStatus( "+gls(lockStatus)+" )");
                            lockLockStatus.wait();
                            System.out.println("[ "+System.currentTimeMillis()+" ] "+"WOKEN UP [ "+id+" ] lockStatus( "+gls(lockStatus)+" )");
                        } catch (InterruptedException e) {
                            throw new JvnException("Erreur en attendant le notify dans jvnInvalidateReader: " + e.getMessage());
                        }
                    }
                    if(lockStatus != JvnObject.RC) throw new JvnException("jvnInvalidateReader -> lockStatus non attendu après WAIT : " + gls(lockStatus)+" au lieu de RC.");
                    lockStatus = JvnObject.NL;
                }
                default -> throw new JvnException("jvnInvalidateReader -> lockStatus non attendu : " + gls(lockStatus));
            }
            System.out.println("[ "+System.currentTimeMillis()+" ] "+bf+" => "+ gls(lockStatus)+" )");
        }
    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        synchronized (lockLockStatus) {
            String bf = "InvalidateWriter : [ "+id+" ] lockStatus( "+ gls(lockStatus);
            System.out.println(bf);
            switch (lockStatus) {
                case JvnObject.WC, JvnObject.RWC -> { break; }
                case JvnObject.W -> {
                    while(lockStatus == JvnObject.W) {
                        try {
                            System.out.println("[ "+System.currentTimeMillis()+" ] "+"WAITING FOR UNLOCK [ "+id+" ] lockStatus( "+gls(lockStatus)+" )");
                            lockLockStatus.wait();
                            System.out.println("[ "+System.currentTimeMillis()+" ] "+"WOKEN UP [ "+id+" ] lockStatus( "+gls(lockStatus)+" )");
                        } catch (InterruptedException e) {
                            throw new JvnException("Erreur en attendant le notify dans jvnInvalidateWriter: " + e.getMessage());
                        }
                    }
                    if(lockStatus != JvnObject.WC && lockStatus != JvnObject.RWC) throw new JvnException("jvnInvalidateWriter -> lockStatus non attendu après WAIT : " + gls(lockStatus)+" au lieu de WC.");
                }
                default -> throw new JvnException("jvnInvalidateWriter -> lockStatus non attendu : " + gls(lockStatus));
            }
            lockStatus = JvnObject.NL;
            System.out.println("[ "+System.currentTimeMillis()+" ] "+bf+" => "+gls(lockStatus)+" )");
        }
        return jvnGetSharedObject();
    }
    
    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        synchronized (lockLockStatus) {
            String bf = "jvnInvalidateWriterForReader : [ "+id+" ] lockStatus( "+ gls(lockStatus);
            System.out.println(bf);
            switch (lockStatus) {
                case JvnObject.RWC -> lockStatus = JvnObject.R;
                case JvnObject.WC -> lockStatus = JvnObject.RC;
                case JvnObject.W -> {
                    while (lockStatus == JvnObject.W) {
                        try {
                            System.out.println("[ "+System.currentTimeMillis()+" ] "+"WAITING FOR UNLOCK [ "+id+" ] lockStatus( "+gls(lockStatus)+" )");
                            lockLockStatus.wait();
                            System.out.println("[ "+System.currentTimeMillis()+" ] "+"WOKEN UP [ "+id+" ] lockStatus( "+gls(lockStatus)+" )");
                        } catch (InterruptedException e) {
                            throw new JvnException("Erreur en attendant le notify dans jvnInvalidateWriterForReader: " + e.getMessage());
                        }
                    }
                    lockStatus = JvnObject.RC;
                }
                default -> throw new JvnException("jvnInvalidateWriterForReader -> lockStatus non attendu : " + gls(lockStatus));
            }
            System.out.println("[ "+System.currentTimeMillis()+" ] "+bf+" => "+gls(lockStatus)+" )");
        }
        return jvnGetSharedObject();
    }
    
    @Override
    public String toString() {
        try{
            String gls = "NL";
            if(lockLockStatus != null)synchronized (lockLockStatus) {
                gls = gls(lockStatus);
            }
            return 
            "Class: "+jvnGetSharedObject().getClass().getName()+
            ", Obj : { "+jvnGetSharedObject().toString()+" }"+
            ", server: "+(server==null?"null":"ok")+
            ", id: "+
            ", lock-status: " + gls;
        } catch (JvnException e) { return "ERROR"; }
    }
    
    private String gls(int lockStatus) {
        String status;
        switch (lockStatus) {
            case JvnObject.NL -> status = "NL";
            case JvnObject.R -> status = "R";
            case JvnObject.RC -> status = "RC";
            case JvnObject.W -> status = "W";
            case JvnObject.WC -> status = "WC";
            case JvnObject.RWC -> status = "RWC";
            default -> status = "??";
        }
        return status;
    }
}