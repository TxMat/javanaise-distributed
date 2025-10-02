package jvn;

import jvn.Implementations.JvnCoordImpl;
import jvn.Models.JvnRemoteCoord;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Coordinator {

    public static void main(String[] args) throws RemoteException, Exception {
        System.out.println("Coordinator");

        JvnCoordImpl jrci = JvnCoordImpl.createCoordinator();

        Registry registry = LocateRegistry.createRegistry(1099);
        registry.rebind("Coordinator", jrci);

        System.out.println("Coordinator ready");

    }
}
