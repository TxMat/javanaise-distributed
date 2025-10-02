package jvn.Implementations;

import jvn.Exceptions.JvnException;
import jvn.Models.JvnLocalServer;
import jvn.Models.JvnObject;
import jvn.Models.JvnRemoteCoord;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {

    private static final long serialVersionUID = 1L;
    private Serializable object;
    private int objectId;
    private String objectName; // track object name here i suppose
    private boolean isReadLocked = false;
    private boolean isWriteLocked = false;
    private static JvnLocalServer localServer;

    public JvnObjectImpl(Serializable o) {
        this.object = o;
        this.objectId = -1; // Initialize to invalid ID
        this.objectName = null; // Will be set when registered
        localServer = JvnServerImpl.jvnGetServer();
    }

    // Constructor with object ID (used when retrieving from coordinator)
    public JvnObjectImpl(Serializable o, int objectId) {
        this.object = o;
        this.objectId = objectId;
        this.objectName = null;
        localServer = JvnServerImpl.jvnGetServer();
    }

    // Constructor with both ID and name (used when creating/registering)
    public JvnObjectImpl(Serializable o, int objectId, String objectName) {
        this.object = o;
        this.objectId = objectId;
        this.objectName = objectName;
        localServer = JvnServerImpl.jvnGetServer();
    }

    // Setter for object ID (called when object is registered with coordinator)
    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    // Setter for object name (called when object is registered)
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    // Getter for object name
    public String getObjectName() {
        return this.objectName;
    }

    @Override
    public void jvnLockRead() throws JvnException {
        if (isWriteLocked) {
            throw new JvnException("Object is write-locked");
        }
        if (objectId == -1) {
            throw new JvnException("Object not registered with coordinator");
        }

        // Get the latest object state from the coordinator
        Serializable latestObject = localServer.jvnLockRead(objectId);
        if (latestObject != null) {
            this.object = latestObject;
        }
        isReadLocked = true;
    }

    @Override
    public void jvnLockWrite() throws JvnException {
        if (isReadLocked || isWriteLocked) {
            throw new JvnException("Object is already locked");
        }
        if (objectId == -1) {
            throw new JvnException("Object not registered with coordinator");
        }

        // Get the latest object state from the coordinator
        Serializable latestObject = localServer.jvnLockWrite(objectId);
        if (latestObject != null) {
            this.object = latestObject;
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
        return this.objectId;
    }

    @Override
    public Serializable jvnGetSharedObject() throws JvnException {
        // The object should be up-to-date if proper locking was used I SUPPOSE
        if (object == null) {
            throw new JvnException("No object data available");
        }
        return object;
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
}
