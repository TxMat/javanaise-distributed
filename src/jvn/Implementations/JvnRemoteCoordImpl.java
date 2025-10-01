package jvn.Implementations;

import jvn.Exceptions.JvnException;
import jvn.Models.JvnObject;
import jvn.Models.JvnRemoteCoord;
import jvn.Models.JvnRemoteServer;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Remote implementation of JvnRemoteCoord
 * This class extends JvnCoordImpl to provide remote access
 */
public class JvnRemoteCoordImpl implements JvnRemoteCoord {

    private AtomicInteger objectId = new AtomicInteger(0);

    private Map<Integer, Map<String, JvnObject>> objectMap = new HashMap<>();

    @Override
    public int jvnGetObjectId() throws RemoteException, JvnException {
        return 0;
    }

    @Override
    public int jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws RemoteException, JvnException {
        int id = objectId.incrementAndGet();
        objectMap.putIfAbsent(id, new HashMap<>());
        objectMap.get(id).put(jon, jo);
        jo.setObjectId(id);
        return id;
    }

    @Override
    public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws RemoteException, JvnException {
        for (Map<String, JvnObject> map : objectMap.values()) {
            if (map.containsKey(jon)) {
                return map.get(jon);
            }
        }
        return null;
    }

    @Override
    public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
        JvnObject jo = objectMap.get(joi).values().stream().findFirst().orElse(null);
        if (jo == null) {
            throw new JvnException("Object not found");
        }
        jo.jvnLockRead();
        return jo;
    }

    @Override
    public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
        JvnObject jo = jvnLookupObject(String.valueOf(joi), js);
        if (jo == null) {
            throw new JvnException("Object not found");
        }
        jo.jvnLockWrite();
        return jo;
    }

    @Override
    public void jvnTerminate(JvnRemoteServer js) throws RemoteException, JvnException {
        // to be completed
    }
}