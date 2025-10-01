package jvn.Implementations;

import jvn.Exceptions.JvnException;
import jvn.Models.JvnObject;
import jvn.Models.JvnLocalServer;

import java.io.Serializable;
import java.rmi.RemoteException;

public class JvnObjectImpl implements JvnObject {

    private static final long serialVersionUID = 1L;
    private Serializable object;
    private int objectId;
    private boolean isReadLocked = false;
    private boolean isWriteLocked = false;
    //jvn server proxy
    private transient JvnLocalServer jvnServer;

    public JvnObjectImpl(Serializable o) {
        this.object = o;
    }

    // Get server reference
    private JvnLocalServer getJvnServer() {
        if (jvnServer == null) {
            jvnServer = JvnServerImpl.jvnGetServer();
        }
        return jvnServer;
    }

    @Override
    public void jvnLockRead() throws JvnException {
        if (isWriteLocked) {
            throw new JvnException("Object is write-locked");
        }
        isReadLocked = true;
    }

    @Override
    public void jvnLockWrite() throws JvnException {
        if (isReadLocked || isWriteLocked) {
            throw new JvnException("Object is already locked");
        }
        isWriteLocked = true;
    }

    @Override
    public void jvnUnLock() throws JvnException {
        isReadLocked = false;
        isWriteLocked = false;
    }

    @Override
    public int jvnGetObjectId() throws JvnException {
        return objectId;
    }

    @Override
    public Serializable jvnGetSharedObject() throws JvnException, RemoteException {
        return getJvnServer().jvnLockRead(objectId);
    }

    @Override
    public void jvnInvalidateReader() throws JvnException {

    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        return null;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        return null;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }
}
