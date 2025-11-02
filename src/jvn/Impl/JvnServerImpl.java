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

import Objects.SerializedInterceptor;
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
	private final Map<String, JvnObjectCapsule> jvnObjectsNameMap;
	private final Map<Integer, JvnObjectCapsule> jvnObjectsIdMap;
	/**
	* Default constructor
	* @throws JvnException
	**/
	private JvnServerImpl() throws Exception {
		this("127.0.0.1");
	}
	private JvnServerImpl(String host) throws Exception {
		System.setProperty("java.rmi.server.hostname", host);
		
		Registry registry = LocateRegistry.getRegistry(host, 5000);
		coord = (JvnRemoteCoord) registry.lookup("coord");
		
		jvnObjectsNameMap = new HashMap<>();
		jvnObjectsIdMap = new HashMap<>();
	}
	
	/**
	* Static method allowing an application to get a reference to 
	* a JVN server instance
	* @throws JvnException
	**/
	public static JvnServerImpl jvnGetServer(String host) {
		ConsoleColor.magicLog(ConsoleColor.toYellow("host: "+host+", js: "+(js==null?"null":js.toString())));
		if(js!=null) return js;
		if(host==null) return null;
		
		try {
			js = new JvnServerImpl(host);
		} catch (Exception e) {
			ConsoleColor.magicError("ERROR : impossible de créer le JvnServerImpl : "+e.getMessage()+"\n"+ConsoleColor.toYellow("null"));
			return null;
		}
		ConsoleColor.magicLog(ConsoleColor.toYellow("OK : "+js.toString()));
		return js;
	}	
	public static JvnServerImpl jvnGetServer() {
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
			int id;
			if(o instanceof JvnObject ojo) {
				id = ojo.jvnGetObjectId();
				o = ojo.jvnGetSharedObject();
			} else {
				id = coord.jvnGetObjectId();
			}
			return jvnLocalCreaObject(o, id);
		} catch (RemoteException | JvnException e) {
			throw new JvnException(e.getMessage());
		} 
	}
	private JvnObject jvnLocalCreaObject(Serializable o, int id) throws JvnException {
		JvnObject jo = new JvnObjectImpl(o, id);
		jvnObjectsIdMap.put(id, new JvnObjectCapsule(jo));
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
			int id = jo.jvnGetObjectId();
			if(jvnObjectsIdMap.get(id)!=null) {
				jvnObjectsNameMap.put(jon, jvnObjectsIdMap.get(id));
			} else {
				JvnObjectCapsule joc = new JvnObjectCapsule(jo);
				jvnObjectsNameMap.put(jon, joc);
				jvnObjectsIdMap.put(jo.jvnGetObjectId(), joc);
			}
			
			coord.jvnRegisterObject(jon, jo, js);
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
		try {
			JvnObject jo = coord.jvnLookupObject(jon, js);
			SerializedInterceptor.deserializedObject.clear(); // Sert a éviter les cycles, mais il faut le clear après chauqe readResolve() complet 
			if(jo == null) return null;
			
			JvnObjectCapsule joc = jvnObjectsNameMap.get(jon);
			
			// Si j'ai pas le JO en local, le créer pour l'interceptor
			if(joc == null) {
				joc = new JvnObjectCapsule(jo);
				jvnObjectsIdMap.put(jo.jvnGetObjectId(), joc);
				jvnObjectsNameMap.put(jon, joc);
				return jo;
			}
			JvnObject ljo = joc.get();
			
			// Sinon juste mettre a jour l'objet
			ljo.updateSerializable(jo.jvnGetSharedObject());
			
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
			Serializable s = coord.jvnLockRead(joi, js);
			SerializedInterceptor.deserializedObject.clear(); // Sert a éviter les cycles, mais il faut le clear après chauqe readResolve() complet 
			return s;
		} catch (RemoteException e) {
			throw new JvnException("Erreur lors de la récupération du LockRead vers le Coord : "+e.getMessage());
		}
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
			Serializable s = coord.jvnLockWrite(joi, js);
			SerializedInterceptor.deserializedObject.clear(); // Sert a éviter les cycles, mais il faut le clear après chauqe readResolve() complet 
			return s;
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
		JvnObjectCapsule joc = jvnObjectsIdMap.get(joi);
		if(joc == null) throw new JvnException("Le server ne contiend pas le JvnObject avec l'id "+joi);
		joc.get().jvnInvalidateReader();
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
		JvnObjectCapsule joc = jvnObjectsIdMap.get(joi);
		if(joc == null) throw new JvnException("Le server ne contiend pas le JvnObject avec l'id "+joi);
		return joc.get().jvnInvalidateWriter();
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
		JvnObjectCapsule joc = jvnObjectsIdMap.get(joi);
		if(joc == null) throw new JvnException("Le server ne contiend pas le JvnObject avec l'id "+joi);
		return joc.get().jvnInvalidateWriterForReader();
	};
	
	private class JvnObjectCapsule {
		JvnObject jo;
		JvnObjectCapsule(JvnObject jo) {
			this.jo = jo;
		}
		JvnObject get() { return jo; };

		@Override
		public String toString(){
			return jo==null?"null":jo.toString();
		}
	}
}