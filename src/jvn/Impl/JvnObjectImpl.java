package jvn.Impl;

import jvn.Enums.ConsoleColor;
import jvn.Enums.JvnObjectStatus;
import jvn.Exceptions.JvnException;
import jvn.Interfaces.JvnLocalServer;
import jvn.Interfaces.JvnObject;
import jvn.JvnInterceptor;

import java.io.Serializable;
import java.lang.reflect.Proxy;

public class JvnObjectImpl implements JvnObject {

    private final Object proxy;
    private final int id;

    private enum WaitingCoordStatus {
        NOT_WAITING, WAIT_FOR_READ, WAIT_FOR_WRITE
    }

    private transient WaitingCoordStatus waitingForCoordAuto = WaitingCoordStatus.NOT_WAITING;

    private final transient Object lockLockStatus = new Object();
    private JvnObjectStatus lockStatus;
    // transient = n'est pas serialisé et est mis a "null" de l'autre coté. Merci les dev Java
    private final transient JvnLocalServer server;

    public JvnObjectImpl(Serializable o, int id, JvnLocalServer server) {
        this.proxy = JvnInterceptor.createInterceptor(o);
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
            System.out.println(bf);
            switch (lockStatus) {
                case JvnObjectStatus.WC -> lockStatus = JvnObjectStatus.RWC;
                case JvnObjectStatus.RC -> lockStatus = JvnObjectStatus.R;
                case JvnObjectStatus.NL -> {
                    waitingForCoordAuto = WaitingCoordStatus.WAIT_FOR_READ;
                    needLockRead = true;
                }
                default -> throw new JvnException("jvnLockRead -> lockStatus non attendu : " + lockStatus);
            }
        }
        if (!needLockRead) {
            System.out.println("[ " + System.currentTimeMillis() + " ] " + bf + ">" + lockStatus + " )");
            return;
        }

        this.updateSerializable
                (server.jvnLockRead(id));

        synchronized (lockLockStatus) {
            waitingForCoordAuto = WaitingCoordStatus.NOT_WAITING;
            bf += (">" + lockStatus);
            lockStatus = JvnObjectStatus.R;
            System.out.println("[ " + System.currentTimeMillis() + " ] " + bf + ">" + lockStatus + " )");
        }
    }

    @Override
    public void jvnLockWrite() throws JvnException {
        boolean needLockWrite = false;
        String bf = "jvnLockWrite : [ " + id + " ] lockStatus( ";
        synchronized (lockLockStatus) {
            bf += lockStatus;
            System.out.println(bf);
            switch (lockStatus) {
                case JvnObjectStatus.WC -> lockStatus = JvnObjectStatus.W;
                case JvnObjectStatus.RC, JvnObjectStatus.NL -> {
                    // TODO : ^^^^^^^^ C'est ici le début de la merde avec l'interbloquage
                    // Récupère "fictivement" le WriteLock avant l'accord du server
                    /*
                     * Appel des jvnLockRead et jvnLockWrite en dehors du synchronized pour éviter des deadlock
                     * MAIS du coup si je fais un sync après la réponse du server, je peux recevoir un invalidate avant que j'ai mis a jour mon lockStatus et avant que j'ai pu réellement faire mes action avec le lock>grosses incohérence
                     * les invalidates sont obligé d'attendre le unlock = pas d'incohérence + pas de deadlock
                     */
                    waitingForCoordAuto = WaitingCoordStatus.WAIT_FOR_WRITE;
                    needLockWrite = true;
                    //lockStatus = JvnObjectStatus.W;
                }
                default -> throw new JvnException("jvnLockWrite -> lockStatus non attendu : " + lockStatus);
            }

        }
        if (!needLockWrite) {
            System.out.println("[ " + System.currentTimeMillis() + " ] " + bf + ">" + lockStatus + " )");
            return;
        }

        this.updateSerializable
                (server.jvnLockWrite(id));

        synchronized (lockLockStatus) {
            waitingForCoordAuto = WaitingCoordStatus.NOT_WAITING;
            bf += (">" + lockStatus);
            lockStatus = JvnObjectStatus.W;
            System.out.println("[ " + System.currentTimeMillis() + " ] " + bf + ">" + lockStatus + " )");
        }
    }

    @Override
    public void jvnUnLock() throws JvnException {
        synchronized (lockLockStatus) {
            String bf = "jvnUnLock : [ " + id + " ] lockStatus( " + lockStatus;
            System.out.println(bf);
            switch (lockStatus) {
                //  
                /*
                 * case JvnObjectStatus.RC : possible si
                 *
                 * lockStatus = W
                 *          --------------------------------------> temps
                 * COORD  : invWforR
                 * SERV_1 :           invWforR
                 * SERV_0 :                     Unlock
                 *
                 * SERV_0 = main thrad
                 * SERV_1 = thread by coord (RMI)
                 */
                case JvnObjectStatus.R, JvnObjectStatus.RC -> lockStatus = JvnObjectStatus.RC;
                case JvnObjectStatus.W, JvnObjectStatus.RWC -> lockStatus = JvnObjectStatus.WC;
                default -> throw new AssertionError("Unknown case on jvnUnLock : lockStatus = " + lockStatus);
            }
            System.out.println("[ " + System.currentTimeMillis() + " ] " + bf + ">" + lockStatus + " )  & notifyAll");
            lockLockStatus.notifyAll(); // Réveille les threads qui attendent sur un lock pour le Coord
        }
    }

    @Override
    public void updateSerializable(Serializable s) {
        ((JvnInterceptor) Proxy.getInvocationHandler(proxy)).updateObject(s);
    }

    @Override
    public int jvnGetObjectId() throws JvnException {
        return id;
    }

    @Override
    public Serializable jvnGetSharedObject() throws JvnException {
        return ((JvnInterceptor) Proxy.getInvocationHandler(proxy)).getSerializable();
    }

    @Override
    public void jvnInvalidateReader() throws JvnException {
        synchronized (lockLockStatus) {
            if (waitingForCoordAuto == WaitingCoordStatus.WAIT_FOR_READ) {
                // même principe que dans jvnInvalidateWriter
                lockStatus = JvnObjectStatus.R;
            } else if (waitingForCoordAuto == WaitingCoordStatus.WAIT_FOR_WRITE) {
                // pas besoin d'attendre la fin de lecture : Utilisation de l'enum pour fix interbloquage possible
                System.out.println(ConsoleColor.toRed("EZ DODGE"));
                return;
            }
            String bf = "InvalidateReader : [ " + id + " ] lockStatus( " + lockStatus;
            System.out.println(bf);

            while (lockStatus == JvnObjectStatus.R) {
                try {
                    System.out.println("[ " + System.currentTimeMillis() + " ] " + "WAITING FOR UNLOCK [ " + id + " ] lockStatus( " + lockStatus + " )");
                    lockLockStatus.wait();
                    bf += (">" + lockStatus);
                    System.out.println("[ " + System.currentTimeMillis() + " ] " + "WOKEN UP [ " + id + " ] lockStatus( " + lockStatus + " )");
                } catch (InterruptedException e) {
                    throw new JvnException("Erreur en attendant le notify dans jvnInvalidateReader: " + e.getMessage());
                }
            }

            if (lockStatus != JvnObjectStatus.RC && lockStatus != JvnObjectStatus.R)
                throw new JvnException("jvnInvalidateReader -> lockStatus non attendu : " + lockStatus);
            lockStatus = JvnObjectStatus.NL;
            System.out.println("[ " + System.currentTimeMillis() + " ] " + bf + ">" + lockStatus + " )");
        }
    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        String bf = "";
        synchronized (lockLockStatus) {
            if (waitingForCoordAuto == WaitingCoordStatus.WAIT_FOR_WRITE) {
                // ici, cela veut dire que ce serv sur cette objet attend d'avoir un nouveau lock (R ou W) donc il n'a plus de lock -> plus besoin d'invalidate (dépassé)

                // C FO (le return ci dessous) car ici, il attend le W dans jvnLockWriter (ne l'a pas encore eu et utilisé) -> donc il doit attendre qu'il le relache donc lockStatus = W ici mais pas encore a jour donc je l'update
                // return jvnGetSharedObject();
                lockStatus = JvnObjectStatus.W;
            } else if (waitingForCoordAuto == WaitingCoordStatus.WAIT_FOR_READ) {
                // pas possible ?
                throw new JvnException("Cas impossible ? waitingForCoordAuto == WaitingCoordStatus.WAIT_FOR_READ dans un jvnInvalidateWriter");
            }
            bf = "InvalidateWriter : [ " + id + " ] lockStatus( " + lockStatus;
            System.out.println(bf);

            // Aussi RWC car invalidateWriter = un autre serv dmd de Writer donc il ne doit plus y avoir de WL et de RL dans les autres server
            while (lockStatus == JvnObjectStatus.W || lockStatus == JvnObjectStatus.RWC) {
                try {
                    System.out.println("[ " + System.currentTimeMillis() + " ] " + "WAITING FOR UNLOCK [ " + id + " ] lockStatus( " + lockStatus + " )");
                    lockLockStatus.wait();
                    bf += (">" + lockStatus);
                    System.out.println("[ " + System.currentTimeMillis() + " ] " + "WOKEN UP [ " + id + " ] lockStatus( " + lockStatus + " )");
                } catch (InterruptedException e) {
                    throw new JvnException("Erreur en attendant le notify dans jvnInvalidateWriter: " + e.getMessage());
                }
            }

            if (lockStatus == JvnObjectStatus.WC) {
                lockStatus = JvnObjectStatus.NL;
            } else throw new JvnException("jvnInvalidateWriter -> lockStatus non attendu : " + lockStatus);

            System.out.println("[ " + System.currentTimeMillis() + " ] " + bf + ">" + lockStatus + " )");
        }
        return jvnGetSharedObject();
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        String bf = "";
        synchronized (lockLockStatus) {
            if (waitingForCoordAuto == WaitingCoordStatus.WAIT_FOR_WRITE) {
                // ici, cela veut dire que ce serv sur cette objet attend d'avoir un nouveau lock (R ou W) donc il n'a plus de lock -> plus besoin d'invalidate (dépassé)

                // C FO (le return ci dessous) car ici, il attend le W dans jvnLockWriter (ne l'a pas encore eu et utilisé) -> donc il doit attendre qu'il le relache donc lockStatus = W ici mais pas encore a jour donc je l'update
                // return jvnGetSharedObject();
                lockStatus = JvnObjectStatus.W;
            } else if (waitingForCoordAuto == WaitingCoordStatus.WAIT_FOR_READ) {
                // pas possible ?
                throw new JvnException("Cas impossible ? waitingForCoordAuto == WaitingCoordStatus.WAIT_FOR_READ dans un jvnInvalidateWriter");
            }
            /*
            if(waitingForCoordAuto) {
            // même chose que dans jvnInvalidateWriter
            lockStatus = JvnObjectStatus.W;
            }*/
            bf = "jvnInvalidateWriterForReader : [ " + id + " ] lockStatus( " + lockStatus;
            System.out.println(bf);

            while (lockStatus == JvnObjectStatus.W) {
                try {
                    System.out.println("[ " + System.currentTimeMillis() + " ] " + "WAITING FOR UNLOCK [ " + id + " ] lockStatus( " + lockStatus + " )");
                    lockLockStatus.wait();
                    bf += (">" + lockStatus);
                    System.out.println("[ " + System.currentTimeMillis() + " ] " + "WOKEN UP [ " + id + " ] lockStatus( " + lockStatus + " )");
                } catch (InterruptedException e) {
                    throw new JvnException("Erreur en attendant le notify dans jvnInvalidateWriterForReader: " + e.getMessage());
                }
            }

            switch (lockStatus) {
                case JvnObjectStatus.RWC -> lockStatus = JvnObjectStatus.R;
                case JvnObjectStatus.WC -> lockStatus = JvnObjectStatus.RC;
                case JvnObjectStatus.R, JvnObjectStatus.RC -> {/* ok, rien a faire */}
                default ->
                        throw new JvnException("jvnInvalidateWriterForReader -> lockStatus non attendu : " + lockStatus);
            }
            System.out.println("[ " + System.currentTimeMillis() + " ] " + bf + ">" + lockStatus + " )");
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