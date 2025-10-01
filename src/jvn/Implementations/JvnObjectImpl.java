package jvn.Implementations;

import jvn.Exceptions.JvnException;
import jvn.Models.JvnObject;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {

    private static final long serialVersionUID = 1L;
    private Serializable object;
    private int objectId;
    private boolean isReadLocked = false;
    private boolean isWriteLocked = false;

    public JvnObjectImpl(Serializable o) {
        this.object = o;
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
        return 0;
    }

    @Override
    public Serializable jvnGetSharedObject() throws JvnException {
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
