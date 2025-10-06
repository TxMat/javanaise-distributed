/***
 * JAVANAISE Implementation
 * JvnCoordImpl class
 * This class implements the Javanaise central coordinator
 * Contact:  
 *
 * Authors: 
 */ 

package jvn.Implementations;

import jvn.Enums.LockState;
import jvn.Exceptions.JvnException;
import jvn.Models.JvnObject;
import jvn.Models.JvnRemoteCoord;
import jvn.Models.JvnRemoteServer;

import java.rmi.server.UnicastRemoteObject;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;


public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {
	
    private static final long serialVersionUID = 1L;

    // ID generator for new objects
    private AtomicInteger nextObjectId = new AtomicInteger(1);

    // Registry of named objects
    private Map<String, ObjectInfo> namedObjects = new ConcurrentHashMap<>();

    // Object state and lock management
    private Map<Integer, ObjectInfo> objects = new ConcurrentHashMap<>();

    // Information about each object
    private static class ObjectInfo {
        JvnObject object;
        Serializable currentState;
        // uses only NL, R, W here
        LockState lockState = LockState.NL;
        Set<JvnRemoteServer> readers = new HashSet<>();
        JvnRemoteServer writer = null;
        JvnRemoteServer owner; // The server that created/owns this object

        ObjectInfo(JvnObject obj, JvnRemoteServer owner) {
            this.object = obj;
            this.owner = owner;
            try {
                this.currentState = obj.jvnGetSharedObject();
            } catch (Exception e) {
                this.currentState = null;
            }
        }
    }

    /**
     * Default constructor
     * @throws JvnException
     **/
    private JvnCoordImpl() throws Exception {
        super();
        System.out.println("JVN Coordinator started");
    }

    /**
     * Static method to create coordinator instance
     */
    public static JvnCoordImpl createCoordinator() throws Exception {
        return new JvnCoordImpl();
    }

    /**
     *  Allocate a NEW JVN object id (usually allocated to a
     *  newly created JVN object)
     * @throws java.rmi.RemoteException,JvnException
     **/
    public synchronized int jvnGetObjectId()
    throws java.rmi.RemoteException, JvnException {
        int id = nextObjectId.getAndIncrement();
        System.out.println("Coordinator: Allocated new object ID: " + id);
        return id;
    }

    /**
     * Associate a symbolic name with a JVN object
     * @param jon : the JVN object name
     * @param jo  : the JVN object
     * @param js  : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     **/
    public synchronized void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
    throws java.rmi.RemoteException, JvnException {
        if (jon == null || jo == null || js == null) {
            throw new JvnException("Coordinator: Cannot register with null parameters");
        }

        try {
            int objectId = jo.jvnGetObjectId();
            ObjectInfo info = new ObjectInfo(jo, js);

            // Register by name and ID
            namedObjects.put(jon, info);
            objects.put(objectId, info);

            System.out.println("Coordinator: Registered object '" + jon + "' with ID " + objectId);
        } catch (Exception e) {
            throw new JvnException("Coordinator: Error registering object: " + e.getMessage());
        }
    }

    /**
     * Get the reference of a JVN object managed by a given JVN server
     * @param jon : the JVN object name
     * @param js : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     **/
    public synchronized JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
    throws java.rmi.RemoteException, JvnException {
        if (jon == null) {
            throw new JvnException("Coordinator: Cannot lookup null object name");
        }

        ObjectInfo info = namedObjects.get(jon);
        if (info == null) {
            System.out.println("Coordinator: Object '" + jon + "' not found");
            return null;
        }

        System.out.println("Coordinator: Found object '" + jon + "'");
        return info.object;
    }

    /**
     * Get a Read lock on a JVN object managed by a given JVN server
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     **/
    public synchronized Serializable jvnLockRead(int joi, JvnRemoteServer js)
    throws java.rmi.RemoteException, JvnException{
        ObjectInfo info = objects.get(joi);
        if (info == null) {
            throw new JvnException("Coordinator: Object " + joi + " not found");
        }

        try {
            // If there's a writer, we need to invalidate it first
            if ((info.lockState == LockState.W || info.lockState == LockState.WC) && info.writer != null) {
                System.out.println("Coordinator: Invalidating writer for read lock on object " + joi);
                Serializable newState = info.writer.jvnInvalidateWriterForReader(joi);
                if (newState != null) {
                    info.currentState = newState;
                }
                info.writer = null;
            }

            // Add this server as a reader
            info.readers.add(js);
            info.lockState = LockState.R;

            System.out.println("Coordinator: Granted read lock on object " + joi + " to server");
            return info.currentState;

        } catch (Exception e) {
            throw new JvnException("Coordinator: Error acquiring read lock: " + e.getMessage());
        }
    }

    /**
     * Get a Write lock on a JVN object managed by a given JVN server
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     **/
    public synchronized Serializable jvnLockWrite(int joi, JvnRemoteServer js)
    throws java.rmi.RemoteException, JvnException{
        ObjectInfo info = objects.get(joi);
        if (info == null) {
            throw new JvnException("Coordinator: Object " + joi + " not found");
        }

        try {
            // Invalidate all readers
            for (JvnRemoteServer reader : info.readers) {
                if (!reader.equals(js)) { // Don't invalidate the requesting server
                    System.out.println("Coordinator: Invalidating reader for write lock on object " + joi);
                    reader.jvnInvalidateReader(joi);
                }
            }
            info.readers.clear();

            // Invalidate writer if there is one
            if ((info.lockState == LockState.W || info.lockState == LockState.WC) && info.writer != null && !info.writer.equals(js)) {
                System.out.println("Coordinator: Invalidating writer for write lock on object " + joi);
                Serializable newState = info.writer.jvnInvalidateWriter(joi);
                if (newState != null) {
                    info.currentState = newState;
                }
            }

            // Grant write lock
            info.writer = js;
            info.lockState = LockState.W;

            System.out.println("Coordinator: Granted write lock on object " + joi + " to server");
            return info.currentState;

        } catch (Exception e) {
            throw new JvnException("Coordinator: Error acquiring write lock: " + e.getMessage());
        }
    }

    public synchronized void jvnUpdateObject(int joi, Serializable newState, JvnRemoteServer js)
    throws java.rmi.RemoteException, JvnException {
        ObjectInfo info = objects.get(joi);
        if (info == null) {
            throw new JvnException("Coordinator: Object " + joi + " not found for update");
        }

        // Update the object state in the coordinator
        info.currentState = newState;

        // The writer is still cached at the client but no longer holds an active write lock
        if (info.writer != null && info.writer.equals(js) && info.lockState == LockState.W) {
            info.lockState = LockState.WC;
        }
        System.out.println("Coordinator: Updated object " + joi + " state from server");
    }

    /**
     * A JVN server terminates
     * @param js  : the remote reference of the server
     * @throws java.rmi.RemoteException, JvnException
     **/
    public synchronized void jvnTerminate(JvnRemoteServer js)
    throws java.rmi.RemoteException, JvnException {
        System.out.println("Coordinator: Server terminating, cleaning up locks");

        // Clean up all locks held by this server
        for (ObjectInfo info : objects.values()) {
            info.readers.remove(js);
            if (info.writer != null && info.writer.equals(js)) {
                info.writer = null;
                info.lockState = LockState.NL;
            }
        }
    }
}
