/***
* JAVANAISE Implementation
* JvnServerImpl class
* Implementation of a Jvn server
* Contact: 
*
* Authors: 
*/

package jvn.Impl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import jvn.Enums.ConsoleColor;
import jvn.Exceptions.JvnException;
import jvn.Interfaces.JvnLocalServer;
import jvn.Interfaces.JvnObject;
import jvn.Interfaces.JvnRemoteCoord;
import jvn.Interfaces.JvnRemoteServer;

public class JvnServerImpl 	
extends UnicastRemoteObject 
implements JvnLocalServer, JvnRemoteServer {
	
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	// A JVN server is managed as a singleton 
	private static JvnServerImpl js = null;
	
	private static JvnRemoteCoord coord;
	
	// TODO : probablment pas utile
	private final Map<Integer, JvnObject> jvnObjectsMap;
	/**
	* Default constructor
	* @throws JvnException
	**/
	private JvnServerImpl() throws Exception {
		super();
		System.setProperty("java.rmi.server.hostname", "127.0.0.1");
		
		Registry registry = LocateRegistry.getRegistry("127.0.0.1", 5000);
		coord = (JvnRemoteCoord) registry.lookup("coord");
		
		jvnObjectsMap = new HashMap<>();
	}
	
	/**
	* Static method allowing an application to get a reference to 
	* a JVN server instance
	* @throws JvnException
	**/
	public static JvnServerImpl jvnGetServer() {
		if (js == null){
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				return null;
			}
		}
		return js;
	}
	
	@Override
	/**
	* The JVN service is not used anymore
	* @throws JvnException
	**/
	public void jvnTerminate()
	throws JvnException {
		try {
			coord.jvnTerminate(js);
		} catch (RemoteException e) {
			e.printStackTrace();
			// Coord ko ?
		}
	} 
	
	@Override
	/**
	* creation of a JVN object
	* @param o : the JVN object state
	* @throws JvnException
	* @throws RemoteException 
	**/
	public JvnObject jvnCreateObject(Serializable o) throws JvnException {
		try {
			if(o instanceof JvnObject jo) {
				return jvnLocalCreaObject(jo.jvnGetSharedObject(), jo.jvnGetObjectId());
			}
			int id = coord.jvnGetObjectId();
			JvnObject jo = new JvnObjectImpl(o, id, js);
			jvnObjectsMap.put(id, jo);
			return jo;
		} catch (RemoteException | JvnException e) {
			throw new JvnException(e.getMessage());
		} 
	}
	private JvnObject jvnLocalCreaObject(Serializable o, int id) throws JvnException {
		JvnObject jo = new JvnObjectImpl(o, id, js);
		jvnObjectsMap.put(id, jo);
		return jo; 
	}
	
	@Override
	/**
	* Associate a symbolic name with a JVN object
	* @param jon : the JVN object name
	* @param jo : the JVN object 
	* @throws JvnException
	**/
	public void jvnRegisterObject(String jon, JvnObject jo)
	throws JvnException {
		try {
			coord.jvnRegisterObject(jon, jo, (JvnRemoteServer)js);
			ConsoleColor.magicLog("New JVN Object Registered : { jon: "+jon+", jos.toString(): { "+jo.jvnGetSharedObject().toString()+" } }");
		} catch (JvnException e) {
			throw e;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	/**
	* Provide the reference of a JVN object beeing given its symbolic name
	* @param jon : the JVN object name
	* @return the JVN object 
	* @throws JvnException
	**/
	public JvnObject jvnLookupObject(String jon)
	throws JvnException {
		// TODO : probable probleme de sync coté coord
		try {
			// TODO : le lookup du coord doit donner une version a jour de l'objet : invalidate le writer si il y en a un et donner le bon
			JvnObject jo = coord.jvnLookupObject(jon, js);
			if(jo == null) return null;
			
			int joi = jo.jvnGetObjectId();
			JvnObject ljo = jvnObjectsMap.get(joi);
			

			
			// Si j'ai pas le JO en local, le créer pour l'interceptor
			if(ljo == null) return jo;

			// Sinon juste mettre a jour l'objet
			ljo.updateSerializable(jo.jvnGetSharedObject());
			// Problème : si l'objet en local est plus récent que l'objet distant ?
			
			return ljo;
		} catch (RemoteException | JvnException e) {
			throw new JvnException(e.getMessage());
		}
	}	
	
	@Override
	/**
	* Get a Read lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws JvnException
	**/
	public Serializable jvnLockRead(int joi)
	throws JvnException {
		try {
			return coord.jvnLockRead(joi, js);
		} catch (RemoteException e) {
			throw new JvnException("Erreur lors de la récupération du LockRead vers le Coord : "+e.getMessage());
		}
		// to be completed 
		
	}	
	
	@Override
	/**
	* Get a Write lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws JvnException
	**/
	public Serializable jvnLockWrite(int joi)
	throws JvnException {
		try {
			return coord.jvnLockWrite(joi, js);
		} catch (RemoteException e) {
			throw new JvnException("Erreur lors de la récupération du LockWrite vers le Coord : "+e.getMessage());
		}
	}	
	
	@Override
	/**
	* Invalidate the Read lock of the JVN object identified by id 
	* called by the JvnCoord
	* @param joi : the JVN object id
	* @return void
	* @throws java.rmi.RemoteException,JvnException
	**/
	public void jvnInvalidateReader(int joi)
	throws java.rmi.RemoteException, JvnException {
		JvnObject jo = jvnObjectsMap.get(joi);
		if(jo == null) throw new JvnException("Le server ne contiend pas le JvnObject avec l'id "+joi);
		jo.jvnInvalidateReader();
	};
	
	@Override
	/**
	* Invalidate the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
	public Serializable jvnInvalidateWriter(int joi)
	throws java.rmi.RemoteException, JvnException {
		JvnObject jo = jvnObjectsMap.get(joi);
		if(jo == null) throw new JvnException("Le server ne contiend pas le JvnObject avec l'id "+joi);
		return jo.jvnInvalidateWriter();
	};
	
	@Override
	/**
	* Reduce the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
	public Serializable jvnInvalidateWriterForReader(int joi)
	throws java.rmi.RemoteException, JvnException {
		JvnObject jo = jvnObjectsMap.get(joi);
		if(jo == null) throw new JvnException("Le server ne contiend pas le JvnObject avec l'id "+joi);
		return jo.jvnInvalidateWriterForReader();
	};
	
}