package jvn;

import jvn.Implementations.JvnRemoteCoordImpl;
import jvn.Models.JvnRemoteCoord;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Coordinator {

    public static void main(String[] args) throws RemoteException {
        System.out.println("Coordinator");

        JvnRemoteCoordImpl jrci = new JvnRemoteCoordImpl();
        JvnRemoteCoord coordinator_stub = (JvnRemoteCoord) UnicastRemoteObject.exportObject(jrci, 0);

        Registry registry = LocateRegistry.createRegistry(1099);
        registry.rebind("Coordinator", coordinator_stub);

        System.out.println("Coordinator ready");

    }
}
