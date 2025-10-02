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
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class JvnServerImpl
        extends UnicastRemoteObject
        implements JvnLocalServer, JvnRemoteServer {

    private static final long serialVersionUID = 1L;
    // A JVN server is managed as a singleton
    private static JvnServerImpl js = null;

    private static JvnRemoteCoord coordinator;

    // ain't no way this is the right way to do it

    // store for JVN objects by name
    private Map<String, JvnObject> objectMap = new HashMap<>();
    // store for JVN objects by ID
    private Map<Integer, JvnObject> objectIdMap = new HashMap<>();

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
    public void jvnTerminate()
            throws JvnException {
        // to be completed
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
        
        try {
            // Get a new object ID from the coordinator
            int objectId = coordinator.jvnGetObjectId();
            JvnObjectImpl jvnObject = new JvnObjectImpl(o, objectId);
            
            // Store the object by ID
            objectIdMap.put(objectId, jvnObject);
            
            return jvnObject;
        } catch (Exception e) {
            throw new JvnException("Error creating JVN object: " + e.getMessage());
        }
    }

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jon : the JVN object name
     * @param jo  : the JVN object
     * @throws JvnException
     **/
    public void jvnRegisterObject(String jon, JvnObject jo) throws JvnException {
        if (jon == null || jon.isEmpty())
            throw new JvnException("jvnRegisterObject: null name");
        if (jo == null)
            throw new JvnException("jvnRegisterObject: null object");
        
        try {
            // Cast to access internal methods
            JvnObjectImpl jvnObj = (JvnObjectImpl) jo;
            jvnObj.setObjectName(jon);
            
            // Register with coordinator
            coordinator.jvnRegisterObject(jon, jo, this);
            
            // Store locally by name
            objectMap.put(jon, jo);
            
        } catch (Exception e) {
            throw new JvnException("Error registering object: " + e.getMessage());
        }
    }

    /**
     * Provide the reference of a JVN object beeing given its symbolic name
     * If the JVN object is not found, returns null
     * @param jon : the JVN object name
     * @return the JVN object
     * @throws JvnException
     **/
    public JvnObject jvnLookupObject(String jon) throws JvnException {
        if (jon == null || jon.isEmpty())
            throw new JvnException("jvnLookupObject: null name");
        
        // First check local cache
        JvnObject localObject = objectMap.get(jon);
        if (localObject != null) {
            return localObject;
        }
        
        // If not found locally, ask the coordinator
        try {
            JvnObject remoteObject = coordinator.jvnLookupObject(jon, this);
            if (remoteObject != null) {
                // Cache the object locally
                objectMap.put(jon, remoteObject);
                objectIdMap.put(remoteObject.jvnGetObjectId(), remoteObject);
            }
            return remoteObject;
        } catch (Exception e) {
            throw new JvnException("Error looking up object: " + e.getMessage());
        }
    }

    /**
     * Get a Read lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     **/
    public Serializable jvnLockRead(int joi)
            throws JvnException {
        try {
            return coordinator.jvnLockRead(joi, this);
        } catch (Exception e) {
            throw new JvnException("Error acquiring read lock: " + e.getMessage());
        }
    }

    /**
     * Get a Write lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     **/
    public Serializable jvnLockWrite(int joi)
            throws JvnException {
        try {
            return coordinator.jvnLockWrite(joi, this);
        } catch (Exception e) {
            throw new JvnException("Error acquiring write lock: " + e.getMessage());
        }
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
        JvnObject obj = objectIdMap.get(joi);
        if (obj != null) {
            obj.jvnInvalidateReader();
        }
    }

    /**
     * Invalidate the Write lock of the JVN object identified by id
     *
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException,JvnException
     **/
    public Serializable jvnInvalidateWriter(int joi)
            throws java.rmi.RemoteException, JvnException {
        JvnObject obj = objectIdMap.get(joi);
        if (obj != null) {
            return obj.jvnInvalidateWriter();
        }
        return null;
    }

    /**
     * Reduce the Write lock of the JVN object identified by id
     *
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException,JvnException
     **/
    public Serializable jvnInvalidateWriterForReader(int joi)
            throws java.rmi.RemoteException, JvnException {
        JvnObject obj = objectIdMap.get(joi);
        if (obj != null) {
            return obj.jvnInvalidateWriterForReader();
        }
        return null;
    }

    public void jvnUpdateObject(int joi, Serializable newState) throws JvnException {
        try {
            // Update the coordinator with the new object state
            coordinator.jvnUpdateObject(joi, newState, this);
            System.out.println("Updated object " + joi + " state in coordinator");
        } catch (Exception e) {
            throw new JvnException("Error updating object state: " + e.getMessage());
        }
    }
}
