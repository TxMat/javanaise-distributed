/***
 * JAVANAISE Implementation
 * JvnCoordImpl class
 * This class implements the Javanaise central coordinator
 * Contact:
 *
 * Authors:
 */

package jvn.Impl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import jvn.Exceptions.JvnException;
import jvn.Interfaces.JvnObject;
import jvn.Interfaces.JvnRemoteCoord;
import jvn.Interfaces.JvnRemoteServer;



public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {

    // ========== ========== ========== ========== ==========
    // ========== ========== ========== ========== ==========
    // 
    //                      MAIN DU COORD 
    // 
    // ========== ========== ========== ========== ==========
    // ========== ========== ========== ========== ==========

    private static JvnCoordImpl coord;

    public static void main(String[] args) {
        try {
            // Important : forcer RMI à utiliser l'adresse locale explicite => Obligatoir a cause de WSL
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");

            coord = new JvnCoordImpl();

            Registry registry = LocateRegistry.createRegistry(5000);
            registry.rebind("coord", (JvnRemoteCoord) coord);

            System.out.println("[ " + System.currentTimeMillis() + " ] > Coordinateur RMI prêt !");
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Thread(JvnCoordImpl::runConsole).start();
    }

    public static void runConsole() {
        Scanner scanner = new Scanner(System.in);
        origin:
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            String[] args = line.trim().split("\\s+");

            if (args.length == 0 || args[0].isEmpty()) continue;

            try {
                switch (args[0]) {
                    case "exit", "q" -> {
                        System.out.println("[ " + System.currentTimeMillis() + " ] EXIT...");
                        scanner.close();
                        break origin;
                    }
                    case "list", "ls" -> {
                        StringBuilder sb = new StringBuilder();

                        sb.append("Links ID - Name : \n");
                        coord.linkIdName.forEach((k, v) -> {
                            sb.append(k).append(" : ").append(v).append("\n");
                        });
                        sb.append("\nJvnObject Info : \n");
                        coord.jvnObjects.forEach((k, v) -> {
                            sb.append(k).append(" : ").append(v).append("\n");
                        });
                        System.out.println(sb.toString());
                    }
                    default -> System.out.println("[ " + System.currentTimeMillis() + " ] Commande inconnue.");
                }
            } catch (Exception e) {
                System.out.println("[ " + System.currentTimeMillis() + " ] Erreur : " + e.getMessage());
            }
        }
        scanner.close();
        System.exit(0);
    }

    // ========== ========== ========== ========== ==========
    // ========== ========== ========== ========== ==========
    // 
    //                   REEL CODE DU COORD 
    // 
    // ========== ========== ========== ========== ==========
    // ========== ========== ========== ========== ==========

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final Map<Integer, String> linkIdName;
    private final Map<String, JvnObjectInfo> jvnObjects;
    private int nextObjectID;

    /**
     * Default constructor
     *
     * @throws JvnException
     **/
    private JvnCoordImpl() throws Exception {
        linkIdName = new HashMap<>(); // Useless ?
        jvnObjects = new HashMap<>();
        nextObjectID = 0;
    }

    @Override
    /**
     *  Allocate a NEW JVN object id (usually allocated to a
     *  newly created JVN object)
     * @throws java.rmi.RemoteException,JvnException
     **/ public int jvnGetObjectId() throws java.rmi.RemoteException, JvnException {
        return nextObjectID++;
    }

    @Override
    /**
     * Associate a symbolic name with a JVN object
     * @param jon : the JVN object name
     * @param jo  : the JVN object
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     **/ public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
        if (jvnObjects.containsKey(jon))
            throw new RuntimeException("Impossible de REGISTER un objet avec un nom déjà utilisé");
        jvnObjects.put(jon, new JvnObjectInfo(jo));
        linkIdName.put(jo.jvnGetObjectId(), jon);
    }

    @Override
    /**
     * Get the reference of a JVN object managed by a given JVN server
     * @param jon : the JVN object name
     * @param js : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     **/ public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
        // TODO : utilité de js ?
        JvnObjectInfo info = jvnObjects.get(jon);
        if (info == null) return null;
        return info.getLatestObject();
    }

    @Override
    /**
     * Get a Read lock on a JVN object managed by a given JVN server
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     **/ public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
        String jon = linkIdName.get(joi);
        JvnObjectInfo info = jvnObjects.get(jon);
        info.addReader(js);
        return info.jo.jvnGetSharedObject();
    }

    @Override
    /**
     * Get a Write lock on a JVN object managed by a given JVN server
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     **/ public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
        String name = linkIdName.get(joi);
        JvnObjectInfo info = jvnObjects.get(name);
        info.switchWriter(js, joi);
        return info.jo.jvnGetSharedObject();
    }

    @Override
    /**
     * A JVN server terminates
     * @param js  : the remote reference of the server
     * @throws java.rmi.RemoteException, JvnException
     **/ public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
        // TODO : idk lol
        jvnObjects.forEach((k, v) -> {
            v.removeServer(js);
        });
    }

    private class JvnObjectInfo {

        JvnObject jo;

        final Object lock = new Object();

        Set<JvnRemoteServer> readLock;
        JvnRemoteServer writeLock;


        @Override
        public String toString() {
            return jo + ", RL servers : " + readLock.size() + ", WL take : " + (writeLock == null ? "no" : "yes");
        }

        public JvnObjectInfo(JvnObject jo) {
            this.jo = jo;
            this.readLock = new HashSet<>();
        }

        void addReader(JvnRemoteServer server) throws RemoteException, JvnException {
            System.out.println("[ " + System.currentTimeMillis() + " ] Waiting lock on addReader");
            synchronized (lock) {
                System.out.println("[ " + System.currentTimeMillis() + " ] Lock take on addReader");

                if (writeLock != null) {
                    Serializable s = writeLock.jvnInvalidateWriterForReader(jo.jvnGetObjectId());
                    System.out.println(s.getClass());
                    jo.updateSerializable(s);
                    readLock.add(writeLock);
                    writeLock = null;
                }
                readLock.add(server);
                System.out.println(this + "\n[ " + System.currentTimeMillis() + " ] Lock unlock :iq: on addReader");
            }
        }

        void switchWriter(JvnRemoteServer server, int joi) throws RemoteException, JvnException {
            System.out.println("[ " + System.currentTimeMillis() + " ] Waiting lock on switchWriter");
            synchronized (lock) {
                System.out.println("[ " + System.currentTimeMillis() + " ] Lock take on switchWriter");

                // TODO : possibel comme en C de lancer tout les truc en paralelle et de faire un waitBarrier ?

                for (JvnRemoteServer jrs : readLock) {
                    if (jrs.equals(server)) {
                        System.out.println("[ " + System.currentTimeMillis() + " ] J'ai 3 IQ");
                        continue;
                    }
                    jrs.jvnInvalidateReader(joi);
                }
                readLock.clear();

                String hash = "    Server : " + server.hashCode() + "\nold writer : " + (writeLock == null ? null : writeLock.hashCode()) + "\nNew Server : ";
                if (writeLock != null) {
                    Serializable s = writeLock.jvnInvalidateWriter(joi);
                    System.out.println(s.getClass());
                    jo.updateSerializable(s);
                }
                writeLock = server;
                System.out.println(hash + writeLock.hashCode() + "\n" + this + "\n[ " + System.currentTimeMillis() + " ] Lock unlock :iq: on switchWriter");
            }
        }
        
        public JvnObject getLatestObject() throws RemoteException, JvnException {
            System.out.println("[ " + System.currentTimeMillis() + " ] Waiting lock on getLatestObject");
            synchronized (lock) {
                System.out.println("[ " + System.currentTimeMillis() + " ] Lock take on getLatestObject");
                
                if (writeLock != null) {
                    Serializable s = writeLock.jvnInvalidateWriter(jo.jvnGetObjectId());
                    jo.updateSerializable(s);
                }
                writeLock = null;
                System.out.println("[ " + System.currentTimeMillis() + " ] Lock unlock :iq: on switchWriter");
                return jo;
            }
        }
        
        
        
        void removeServer(JvnRemoteServer js) {
            System.out.println("[ " + System.currentTimeMillis() + " ] Waiting lock on removeServer");
            synchronized (lock) {
                System.out.println("[ " + System.currentTimeMillis() + " ] Lock take on removeServer");

                if (writeLock == js) writeLock = null;
                readLock.remove(js);
                System.out.println(this + "\n[ " + System.currentTimeMillis() + " ] Lock unlock :iq: on removeServer");
            }
        }
    }

}