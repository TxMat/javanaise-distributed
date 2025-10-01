/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Implementation of a Jvn server
 * Contact:
 *
 * Authors: 
 */

package jvn.Implementations;

import jvn.Exceptions.JvnException;
import jvn.Models.JvnLocalServer;
import jvn.Models.JvnObject;
import jvn.Models.JvnRemoteCoord;
import jvn.Models.JvnRemoteServer;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class JvnServerImpl
        extends UnicastRemoteObject
        implements JvnLocalServer, JvnRemoteServer {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // A JVN server is managed as a singleton
    private static JvnServerImpl js = null;

    private static JvnRemoteCoord coordinator;

    // store for JVN objects
    private Map<Integer, Map<String, JvnObject>> objectMap = new HashMap<>();

    /**
     * Default constructor
     *
     * @throws JvnException
     **/
    private JvnServerImpl() throws Exception {
        super();
        // to be completed
    }

    /**
     * Static method allowing an application to get a reference to
     * a JVN server instance
     *
     * @throws JvnException
     **/
    public static JvnServerImpl jvnGetServer() {
        if (js == null) {
            try {
                js = new JvnServerImpl();
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                coordinator = (JvnRemoteCoord) registry.lookup("Coordinator");
            } catch (ConnectException e) {
                System.out.println("JVN server not running");
                System.exit(1);
            } catch (Exception e) {
                System.out.println("JVN server problem : " + e.getMessage());
                return null;
            }
        }
        return js;
    }

    /**
     * The JVN service is not used anymore
     *
     * @throws JvnException
     **/
    public void jvnTerminate() throws JvnException {
        coordinator = null;
        js = null;
//        objectMap.clear();
        System.gc();
    }

    /**
     * creation of a JVN object
     *
     * @param o : the JVN object state
     * @throws JvnException
     **/
    public JvnObject jvnCreateObject(Serializable o) throws JvnException {
        if (o == null)
            throw new JvnException("jvnCreateObject: null object");
        return new JvnObjectImpl(o);
    }

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jon : the JVN object name
     * @param jo  : the JVN object
     * @throws JvnException
     **/
    public void jvnRegisterObject(String jon, JvnObject jo) throws JvnException, RemoteException {
        if (jon == null || jon.isEmpty())
            throw new JvnException("jvnRegisterObject: null name");
        if (jo == null)
            throw new JvnException("jvnRegisterObject: null object");
        int id = coordinator.jvnRegisterObject(jon, jo, this);
        objectMap.putIfAbsent(id, new HashMap<>());
        objectMap.get(id).put(jon, jo);
        jo.setObjectId(id);
    }

    /**
     * Provide the reference of a JVN object beeing given its symbolic name
     * If the JVN object is not found, returns null
     * @param jon : the JVN object name
     * @return the JVN object
     * @throws JvnException
     **/
    public JvnObject jvnLookupObject(String jon) throws JvnException, RemoteException {
        if (jon == null || jon.isEmpty())
            throw new JvnException("jvnLookupObject: null name");
        for (Map<String, JvnObject> map : objectMap.values()) {
            if (map.containsKey(jon)) {
                return map.get(jon);
            }
        }
        // cache miss
        return coordinator.jvnLookupObject(jon, this);
    }

    /**
     * Get a Read lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     **/
    public Serializable jvnLockRead(int joi) throws JvnException, RemoteException {
        return coordinator.jvnLockRead(joi, this);
    }

    /**
     * Get a Write lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     **/
    public Serializable jvnLockWrite(int joi)
            throws JvnException, RemoteException {
        return coordinator.jvnLockWrite(joi, this);
    }


    /**
     * Invalidate the Read lock of the JVN object identified by id
     * called by the JvnCoord
     *
     * @param joi : the JVN object id
     * @return void
     * @throws java.rmi.RemoteException,JvnException
     **/
    public void jvnInvalidateReader(int joi)
            throws java.rmi.RemoteException, JvnException {
        // to be completed
    }

    ;

    /**
     * Invalidate the Write lock of the JVN object identified by id
     *
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException,JvnException
     **/
    public Serializable jvnInvalidateWriter(int joi)
            throws java.rmi.RemoteException, JvnException {
        // to be completed
        return null;
    }

    ;

    /**
     * Reduce the Write lock of the JVN object identified by id
     *
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException,JvnException
     **/
    public Serializable jvnInvalidateWriterForReader(int joi)
            throws java.rmi.RemoteException, JvnException {
        // to be completed
        return null;
    }

    ;

}


