package jvn.Implementations;

import jvn.Exceptions.JvnException;
import jvn.Models.JvnRemoteServer;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.io.Serializable;

/**
 * Remote implementation of JvnRemoteServer
 * This class extends JvnServerImpl to provide remote access
 */
public class JvnRemoteServerImpl implements JvnRemoteServer {

    @Override
    public void jvnInvalidateReader(int joi) throws RemoteException, JvnException {

    }

    @Override
    public Serializable jvnInvalidateWriter(int joi) throws RemoteException, JvnException {
        return null;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader(int joi) throws RemoteException, JvnException {
        return null;
    }
}
