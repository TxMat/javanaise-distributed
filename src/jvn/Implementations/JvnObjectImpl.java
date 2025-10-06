package jvn.Implementations;

import java.io.Serializable;

import jvn.Enums.LockState;
import jvn.Exceptions.JvnException;
import jvn.Models.JvnLocalServer;
import jvn.Models.JvnObject;

public class JvnObjectImpl implements JvnObject {

    private static final long serialVersionUID = 1L;
    private Serializable object;
    private int objectId;
    // TODO: Do we really need objectName ?
    private String objectName; // track object name here I suppose
    private LockState lock = LockState.NL;
    private transient JvnLocalServer localServer;

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
        if (objectId == -1) {
            throw new JvnException("Object not registered with coordinator");
        }

        Serializable latestObject;

        switch (lock) {
            case NL:
                // NL -> R : Ask coordinator
                latestObject = localServer.jvnLockRead(objectId);
                if (latestObject != null) {
                    this.object = latestObject;
                }
                lock = LockState.R;
                break;
            case RC:
                // RC -> R : Need to get latest from coordinator since we only have cached read
                latestObject = localServer.jvnLockRead(objectId);
                if (latestObject != null) {
                    this.object = latestObject;
                }
                lock = LockState.R;
                break;
            case WC:
                // WC -> RWC : Need to get latest from coordinator since another writer may have updated
                latestObject = localServer.jvnLockRead(objectId);
                if (latestObject != null) {
                    this.object = latestObject;
                }
                lock = LockState.RWC;
                break;
            case W:
                throw new JvnException("Can't lock read: already write locked");
            case R:
            case RWC:
                break;
        }
    }

    @Override
    public void jvnLockWrite() throws JvnException {
        if (objectId == -1) {
            throw new JvnException("Object not registered with coordinator");
        }

        Serializable latestObject;

        switch (lock) {
            case NL:
                // NL -> W : Ask coordinator
                latestObject = localServer.jvnLockWrite(objectId);
                if (latestObject != null) {
                    this.object = latestObject;
                }
                lock = LockState.W;
                break;
            case RC:
                // RC -> W : Ask coordinator
                latestObject = localServer.jvnLockWrite(objectId);
                if (latestObject != null) {
                    this.object = latestObject;
                }
                lock = LockState.W;
                break;
            case WC:
                // WC -> W
                lock = LockState.W;
                break;
            case R:
                throw new JvnException("Can't lock write: already read locked");
            case W:
                break;
            case RWC:
                throw new JvnException("Can't lock write: already read locked");
        }
    }

    @Override
    public void jvnUnLock() throws JvnException {
        try {
            switch (lock) {
                case W:
                    // W -> WC : Notify changes for coordinator
                    if (localServer != null && objectId != -1) {
                        localServer.jvnUpdateObject(objectId, object);
                    }
                    lock = LockState.WC;
                    break;
                case R:
                    // R -> RC
                    lock = LockState.RC;
                    break;
                case RWC:
                    // RWC -> WC
                    lock = LockState.WC;
                    break;
                default:
                    // NL, RC, WC: nothing to do
                    break;
            }
        } catch (Exception e) {
            throw new JvnException("Unlock failed: " + e.getMessage());
        }
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
        switch (lock) {
            case R:
                // R -> NL
            case RC:
                // RC -> NL
                lock = LockState.NL;
                break;
            case RWC:
                // RWC -> WC
                lock = LockState.WC;
                break;
            default:
                break;
        }
    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        Serializable result = null;
        switch (lock) {
            case W:
                // W -> NL
                result = object;
                lock = LockState.NL;
                break;
            case WC:
                // WC -> NL
                result = object;
                lock = LockState.NL;
                break;
            case RWC:
                // RWC -> RC
                result = object;
                lock = LockState.RC;
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        Serializable result = null;
        switch (lock) {
            case W:
                // W -> R
                result = object;
                lock = LockState.R;
                break;
            case WC:
                // WC -> RC
                result = object;
                lock = LockState.RC;
                break;
            case RWC:
                // RWC -> R
                result = object;
                lock = LockState.R;
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    public void initializeTransientFields() {
        this.localServer = JvnServerImpl.jvnGetServer();
    }
}
