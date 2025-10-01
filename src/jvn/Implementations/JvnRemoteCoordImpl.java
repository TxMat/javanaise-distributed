package jvn.Implementations;

import jvn.Exceptions.JvnException;
import jvn.Implementations.JvnCoordImpl;
import jvn.Models.JvnObject;
import jvn.Models.JvnRemoteCoord;
import jvn.Models.JvnRemoteServer;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * Remote implementation of JvnRemoteCoord
 * This class extends JvnCoordImpl to provide remote access
 */
public class JvnRemoteCoordImpl implements JvnRemoteCoord {

    @Override
    public int jvnGetObjectId() throws RemoteException, JvnException {
        return 0;
    }

    @Override
    public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws RemoteException, JvnException {

    }

    @Override
    public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws RemoteException, JvnException {
        return null;
    }

    @Override
    public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
        return null;
    }

    @Override
    public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
        return null;
    }

    @Override
    public void jvnTerminate(JvnRemoteServer js) throws RemoteException, JvnException {

    }
}