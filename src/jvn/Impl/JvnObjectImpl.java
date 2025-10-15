package jvn.Impl;

import java.io.Serializable;

import jvn.Enums.ConsoleColor;
import jvn.Enums.JvnObjectStatus;
import jvn.Exceptions.JvnException;
import jvn.Interfaces.JvnLocalServer;
import jvn.Interfaces.JvnObject;

public class JvnObjectImpl implements JvnObject {
    
    private final int id;
    
    private Serializable o;
    
    private enum WaitingCoordStatus {
        NOT_WAITING, WAIT_FOR_READ, WAIT_FOR_WRITE
    }
    
    private transient WaitingCoordStatus waitingForCoordAuto = WaitingCoordStatus.NOT_WAITING;
    
    private final transient Object lockLockStatus = new Object();
    private JvnObjectStatus lockStatus;
    // transient = n'est pas serialisé et est mis a "null" de l'autre coté. Merci les dev Java
    private final transient JvnLocalServer server;
    
    public JvnObjectImpl(Serializable o, int id, JvnLocalServer server) {
        this.o = o;
        this.id = id;
        this.lockStatus = JvnObjectStatus.NL;
        this.server = server;
    }
    
    @Override
    public void jvnLockRead() throws JvnException {
        boolean needLockRead = false;
        String bf = "jvnLockRead : [ " + id + " ] lockStatus( ";
        synchronized (lockLockStatus) {
            bf += lockStatus;
            /*sysout*/ // ConsoleColor.magicLog(bf);
            switch (lockStatus) {
                case JvnObjectStatus.WC -> lockStatus = JvnObjectStatus.RWC;
                case JvnObjectStatus.RC -> lockStatus = JvnObjectStatus.R;
                case JvnObjectStatus.NL -> {
                    waitingForCoordAuto = WaitingCoordStatus.WAIT_FOR_READ;
                    needLockRead = true;
                }
                case JvnObjectStatus.W -> {}
                default -> throw new JvnException("jvnLockRead -> lockStatus non attendu : " + lockStatus);
            }
        }
        if (!needLockRead) {
            /*sysout*/ // ConsoleColor.magicLog(bf + ">" + lockStatus + " )");
            return;
        }
        
        this.updateSerializable(server.jvnLockRead(id));
        
        synchronized (lockLockStatus) {
            waitingForCoordAuto = WaitingCoordStatus.NOT_WAITING;
            bf += (">" + lockStatus);
            lockStatus = JvnObjectStatus.R;
            /*sysout*/ // ConsoleColor.magicLog(bf + ">" + lockStatus + " )");
        }
    }
    
    @Override
    public void jvnLockWrite() throws JvnException {
        boolean needLockWrite = false;
        String bf = "jvnLockWrite : [ " + id + " ] lockStatus( ";
        synchronized (lockLockStatus) {
            bf += lockStatus;
            /*sysout*/ // ConsoleColor.magicLog(bf);
            switch (lockStatus) {
                case JvnObjectStatus.WC -> lockStatus = JvnObjectStatus.W;
                case JvnObjectStatus.R, JvnObjectStatus.RC, JvnObjectStatus.NL -> {
                    waitingForCoordAuto = WaitingCoordStatus.WAIT_FOR_WRITE;
                    needLockWrite = true;
                }
                default -> throw new JvnException("jvnLockWrite -> lockStatus non attendu : " + lockStatus);
            }
            
        }
        if (!needLockWrite) {
            /*sysout*/ // ConsoleColor.magicLog(bf + ">" + lockStatus + " )");
            return;
        }
        
        this.updateSerializable(server.jvnLockWrite(id));
        
        synchronized (lockLockStatus) {
            waitingForCoordAuto = WaitingCoordStatus.NOT_WAITING;
            bf += (">" + lockStatus);
            lockStatus = JvnObjectStatus.W;
            /*sysout*/ // ConsoleColor.magicLog(bf + ">" + lockStatus + " )");
        }
    }
    
    @Override
    public void jvnUnLock() throws JvnException {
        synchronized (lockLockStatus) {
            String bf = "jvnUnLock : [ " + id + " ] lockStatus( " + lockStatus;
            /*sysout*/ // ConsoleColor.magicLog(bf);
            switch (lockStatus) {
                case JvnObjectStatus.R, JvnObjectStatus.RC -> lockStatus = JvnObjectStatus.RC;
                case JvnObjectStatus.W, JvnObjectStatus.RWC -> lockStatus = JvnObjectStatus.WC;
                default -> throw new AssertionError("Unknown case on jvnUnLock : lockStatus = " + lockStatus);
            }
            /*sysout*/ // ConsoleColor.magicLog(bf + ">" + lockStatus + " )  & notifyAll");
            lockLockStatus.notifyAll(); // Réveille les threads qui attendent sur un lock pour le Coord
        }
    }
    
    @Override
    public void updateSerializable(Serializable s) {
        // TODO : Besoin de sync ?
        this.o = s;
    }
    
    @Override
    public int jvnGetObjectId() throws JvnException {
        return id;
    }
    
    @Override
    public Serializable jvnGetSharedObject() throws JvnException {
        // TODO : Besoin de sync ?
        return o;
    }
    
    @Override
    public void jvnInvalidateReader() throws JvnException {
        synchronized (lockLockStatus) {
            if (waitingForCoordAuto == WaitingCoordStatus.WAIT_FOR_READ) {
                // même principe que dans jvnInvalidateWriter
                lockStatus = JvnObjectStatus.R;
            } else if (waitingForCoordAuto == WaitingCoordStatus.WAIT_FOR_WRITE) {
                // pas besoin d'attendre la fin de lecture : Utilisation de l'enum pour fix interbloquage possible
                return;
            }
            String bf = "InvalidateReader : [ " + id + " ] lockStatus( " + lockStatus;
            /*sysout*/ // ConsoleColor.magicLog(bf);
            
            while (lockStatus == JvnObjectStatus.R) {
                try {
                    /*sysout*/ // ConsoleColor.magicLog("WAITING FOR UNLOCK [ " + id + " ] lockStatus( " + lockStatus + " )");
                    lockLockStatus.wait();
                    bf += (">" + lockStatus);
                    /*sysout*/ // ConsoleColor.magicLog("WOKEN UP [ " + id + " ] lockStatus( " + lockStatus + " )");
                } catch (InterruptedException e) {
                    throw new JvnException("Erreur en attendant le notify dans jvnInvalidateReader: " + e.getMessage());
                }
            }
            
            if (lockStatus != JvnObjectStatus.RC && lockStatus != JvnObjectStatus.R) throw new JvnException("jvnInvalidateReader -> lockStatus non attendu : " + lockStatus);
            lockStatus = JvnObjectStatus.NL;
            /*sysout*/ // ConsoleColor.magicLog(bf + ">" + lockStatus + " )");
        }
    }
    
    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        String bf = "";
        synchronized (lockLockStatus) {
            if (waitingForCoordAuto == WaitingCoordStatus.WAIT_FOR_WRITE) {
                lockStatus = JvnObjectStatus.W;
            } else if (waitingForCoordAuto == WaitingCoordStatus.WAIT_FOR_READ) {
                // pas possible ?
                throw new JvnException("Cas impossible ? waitingForCoordAuto == WaitingCoordStatus.WAIT_FOR_READ dans un jvnInvalidateWriter");
            }
            bf = "InvalidateWriter : [ " + id + " ] lockStatus( " + lockStatus;
            /*sysout*/ // ConsoleColor.magicLog(bf);
            
            // Aussi RWC car invalidateWriter = un autre serv dmd de Writer donc il ne doit plus y avoir de WL et de RL dans les autres server
            while (lockStatus == JvnObjectStatus.W || lockStatus == JvnObjectStatus.RWC) {
                try {
                    /*sysout*/ // ConsoleColor.magicLog("WAITING FOR UNLOCK [ " + id + " ] lockStatus( " + lockStatus + " )");
                    lockLockStatus.wait();
                    bf += (">" + lockStatus);
                    /*sysout*/ // ConsoleColor.magicLog("WOKEN UP [ " + id + " ] lockStatus( " + lockStatus + " )");
                } catch (InterruptedException e) {
                    throw new JvnException("Erreur en attendant le notify dans jvnInvalidateWriter: " + e.getMessage());
                }
            }
            
            if (lockStatus == JvnObjectStatus.WC) {
                lockStatus = JvnObjectStatus.NL;
            } else throw new JvnException("jvnInvalidateWriter -> lockStatus non attendu : " + lockStatus);
            
            /*sysout*/ // ConsoleColor.magicLog(bf + ">" + lockStatus + " )");
        }
        return jvnGetSharedObject();
    }
    
    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        synchronized (lockLockStatus) {
            if (waitingForCoordAuto == WaitingCoordStatus.WAIT_FOR_WRITE) {
                lockStatus = JvnObjectStatus.W;
            } else if (waitingForCoordAuto == WaitingCoordStatus.WAIT_FOR_READ) {
                // pas possible ?
                throw new JvnException("Cas impossible ? waitingForCoordAuto == WaitingCoordStatus.WAIT_FOR_READ dans un jvnInvalidateWriter");
            }
            String bf = "jvnInvalidateWriterForReader : [ " + id + " ] lockStatus( " + lockStatus;
            /*sysout*/ // ConsoleColor.magicLog(bf);
            
            while (lockStatus == JvnObjectStatus.W) {
                try {
                    /*sysout*/ // ConsoleColor.magicLog("WAITING FOR UNLOCK [ " + id + " ] lockStatus( " + lockStatus + " )");
                    lockLockStatus.wait();
                    bf += (">" + lockStatus);
                    /*sysout*/ // ConsoleColor.magicLog("WOKEN UP [ " + id + " ] lockStatus( " + lockStatus + " )");
                } catch (InterruptedException e) {
                    throw new JvnException("Erreur en attendant le notify dans jvnInvalidateWriterForReader: " + e.getMessage());
                }
            }
            switch (lockStatus) {
                case JvnObjectStatus.RWC -> lockStatus = JvnObjectStatus.R;
                case JvnObjectStatus.WC -> lockStatus = JvnObjectStatus.RC;
                case JvnObjectStatus.R, JvnObjectStatus.RC -> {
                    // ok, rien a faire 
                }
                default -> throw new JvnException("jvnInvalidateWriterForReader -> lockStatus non attendu : " + lockStatus);
            }
            /*sysout*/ // ConsoleColor.magicLog(bf + ">" + lockStatus + " )");
        }
        return jvnGetSharedObject();
    }
    
    @Override
    public String toString() {
        try {
            JvnObjectStatus gls = null;
            if (lockLockStatus != null) synchronized (lockLockStatus) {
                gls = lockStatus;
            }
            return
                "Class: " + jvnGetSharedObject().getClass().getName() +
                ", Obj : { " + jvnGetSharedObject().toString() + " }" +
                ", server: " + (server == null ? "null" : "ok") +
                ", id: " +
                ", lock-status: " + gls;
        } catch (JvnException e) {
            return "ERROR";
        }
    }
}