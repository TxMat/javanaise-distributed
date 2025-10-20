/***
* JAVANAISE Implementation
* JvnServerImpl class
* Implementation of a Jvn Memory Efficient server
* Contact: 
*
* Authors: 
*/

package jvn.Impl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Function;

import Objects.Debug;
import jvn.Enums.ConsoleColor;
import jvn.Exceptions.JvnException;
import jvn.Interfaces.JvnLocalServer;
import jvn.Interfaces.JvnObject;
import jvn.Interfaces.JvnRemoteServer;

public class JvnMemoryEfficientServerImpl implements JvnLocalServer, JvnRemoteServer {
    /**
     * 
     */
    private static JvnServerImpl jvnServer;

    private static final Queue<SimpleEntry<Function<Integer, Object>, Integer>> evictionCallbacks = new LinkedList<>();

    private final static int MAX_OBJECTS = 2; // Maximum number of objects to keep in memory

    /**
     * Static method allowing an application to get a reference to
     * a JVN server instance
     * 
     * @throws JvnException
     **/
    public static JvnMemoryEfficientServerImpl jvnGetServer() {
        if (jvnServer == null) {
            try {
                jvnServer = JvnServerImpl.jvnGetServer();
            } catch (Exception e) {
                return null;
            }
        }
        return new JvnMemoryEfficientServerImpl();
    }

    @Override
    /**
     * The JVN service is not used anymore
     * 
     * @throws JvnException
     **/
    public void jvnTerminate()
            throws JvnException {
        jvnServer.jvnTerminate();
    }

    @Override
    /**
     * creation of a JVN object
     * 
     * @param o : the JVN object state
     * @throws JvnException
     * @throws RemoteException
     **/
    public JvnObject jvnCreateObject(Serializable o) throws JvnException {
        try {
            if (o instanceof JvnObject jo) {
                return jvnLocalCreaObject(jo.jvnGetSharedObject(), jo.jvnGetObjectId());
            }
            int id = JvnServerImpl.getCoord().jvnGetObjectId();
            JvnObject jo = new JvnObjectImpl(o, id, jvnServer);
            RegisterObject(id, jo);

            return jo;
        } catch (RemoteException | JvnException e) {
            throw new JvnException(e.getMessage());
        }
    }

    private JvnObject jvnLocalCreaObject(Serializable o, int id) throws JvnException {
        JvnObject jo = new JvnObjectImpl(o, id, jvnServer);
        RegisterObject(id, jo);

        return jo;
    }

    @Override
    /**
     * Associate a symbolic name with a JVN object
     * 
     * @param jon : the JVN object name
     * @param jo  : the JVN object
     * @throws JvnException
     **/
    public void jvnRegisterObject(String jon, JvnObject jo)
            throws JvnException {
        try {
            JvnServerImpl.getCoord().jvnRegisterObject(jon, jo, (JvnRemoteServer) jvnServer);
        } catch (JvnException e) {
            throw e;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    /**
     * Provide the reference of a JVN object beeing given its symbolic name
     * 
     * @param jon : the JVN object name
     * @return the JVN object
     * @throws JvnException
     **/
    public JvnObject jvnLookupObject(String jon)
            throws JvnException {
        return jvnServer.jvnLookupObject(jon);
    }

    @Override
    /**
     * Get a Read lock on a JVN object
     * 
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     **/
    public Serializable jvnLockRead(int joi)
            throws JvnException {
        return jvnServer.jvnLockRead(joi);
    }

    @Override
    /**
     * Get a Write lock on a JVN object
     * 
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     **/
    public Serializable jvnLockWrite(int joi)
            throws JvnException {
        return jvnServer.jvnLockWrite(joi);
    }

    @Override
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
        jvnServer.jvnInvalidateReader(joi);
    };

    @Override
    /**
     * Invalidate the Write lock of the JVN object identified by id
     * 
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException,JvnException
     **/
    public Serializable jvnInvalidateWriter(int joi)
            throws java.rmi.RemoteException, JvnException {
        return jvnServer.jvnInvalidateWriter(joi);
    };

    @Override
    /**
     * Reduce the Write lock of the JVN object identified by id
     * 
     * @param joi : the JVN object id
     * @return the current JVN object state
     * @throws java.rmi.RemoteException,JvnException
     **/
    public Serializable jvnInvalidateWriterForReader(int joi)
            throws java.rmi.RemoteException, JvnException {
        return jvnServer.jvnInvalidateWriterForReader(joi);
    };

    // --- MEMORY EFFICIENCY METHODS ---

    private void RegisterObject(int id, JvnObject jo) throws JvnException {
        while (evictionCallbacks.size() >= MAX_OBJECTS) {
            EvictOldestObject();
        }
        if (Debug.DEBUG) ConsoleColor.magicLog("Registering object ID: " + id);
        JvnServerImpl.putObjectInMap(id, jo);
        RegisterEvictionCallback(JvnServerImpl::removeObjectFromMap, id);

        if (Debug.DEBUG) ConsoleColor.magicLog("Map content: " + JvnServerImpl.showObjectsInMap());
    }

    private void RegisterEvictionCallback(Function<Integer, Object> callback, Integer id) {
        if (Debug.DEBUG) ConsoleColor.magicLog("Registered eviction callback for object ID: " + id);
        evictionCallbacks.add(new SimpleEntry<>(callback, id));
    }

    private void EvictOldestObject() {
        var callbackEntry = evictionCallbacks.poll();
        if (callbackEntry != null) {
            Function<Integer, Object> callback = callbackEntry.getKey();
            Integer id = callbackEntry.getValue();
            if (Debug.DEBUG) ConsoleColor.magicLog("Evicting object ID: " + id);
            callback.apply(id);
        }
    }
}
