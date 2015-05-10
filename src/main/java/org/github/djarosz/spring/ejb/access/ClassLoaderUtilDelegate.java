package org.github.djarosz.spring.ejb.access;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIClassLoader;
import java.util.Map;
import java.util.WeakHashMap;
import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.UtilDelegate;
import javax.rmi.CORBA.ValueHandler;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

/**
 * If you want to use many ORB's in the same JVM but want them to be loaded using different classloader's
 * simply setting javax.rmi.CORBA.ClassLoaderUtilDelegate.UtilClass property does not work as it is
 * shared because {@link javax.rmi.CORBA.Util} is in system class loader and thus is shared in all child class loaders.
 * <p>
 * To solve this problem set these <b>system</b> properties:
 * <pre>
 *     javax.rmi.CORBA.UtilClass=org.github.djarosz.spring.ejb.access.ClassLoaderUtilDelegate
 *	   javax.rmi.CORBA.ClassLoaderUtilDelegate.UtilClass=to the util real UtilDelegate iimplementation
 * </pre>
 * eg
 * <pre>
 *     javax.rmi.CORBA.UtilClass=org.github.djarosz.spring.ejb.accesslassLoaderUtilDelegate
 *	   javax.rmi.CORBA.ClassLoaderUtilDelegate.UtilClass=com.ibm.ws.orb.WSUtilDelegateImpl
 * </pre>
 *
 * This ensures that any call to {@link javax.rmi.CORBA.Util} method will result in calling real UtilDelegate class
 * (specified as javax.rmi.CORBA.ClassLoaderUtilDelegate.UtilClass system property) but in context on class loader
 * of current running thread and not in context of system class loader.
 */
public class ClassLoaderUtilDelegate implements UtilDelegate {

	private static final String REAL_UTIL_CLASS_KEY = "javax.rmi.CORBA.ClassLoaderUtilDelegate.UtilClass";

	private Map<ClassLoader, UtilDelegate> utilDelegates = new WeakHashMap<ClassLoader, UtilDelegate>();

	public ClassLoaderUtilDelegate() {
	}

	private UtilDelegate getClassLoaderUtilDelegate() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		UtilDelegate delegate = utilDelegates.get(cl);

		if (delegate == null) {
			String realUtilClass = System.getProperty(REAL_UTIL_CLASS_KEY);
			if (realUtilClass == null) {
				throw new RuntimeException(REAL_UTIL_CLASS_KEY + " system property not set");
			}
			delegate = createDelegate(realUtilClass);
			utilDelegates.put(cl, delegate);
		}

		return delegate;
	}

	private UtilDelegate createDelegate(String className) {
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			return (UtilDelegate) Class.forName(className, false, loader).newInstance();
		} catch (Exception e) {
			// ignore, then try RMIClassLoader
		}

		try {
			return (UtilDelegate) RMIClassLoader.loadClass((String) null, className).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Could not load " + className + ": " + e.toString());
		}
	}

	@Override
	public RemoteException mapSystemException(SystemException ex) {
		return getClassLoaderUtilDelegate().mapSystemException(ex);
	}

	@Override
	public void writeAny(OutputStream out, Object obj) {
		getClassLoaderUtilDelegate().writeAny(out, obj);
	}

	@Override
	public Object readAny(InputStream in) {
		return getClassLoaderUtilDelegate().readAny(in);
	}

	@Override
	public void writeRemoteObject(OutputStream out, Object obj) {
		getClassLoaderUtilDelegate().writeRemoteObject(out, obj);
	}

	@Override
	public void writeAbstractObject(OutputStream out, Object obj) {
		getClassLoaderUtilDelegate().writeAbstractObject(out, obj);
	}

	@Override
	public void registerTarget(Tie tie, Remote target) {
		getClassLoaderUtilDelegate().registerTarget(tie, target);
	}

	@Override
	public void unexportObject(Remote target) throws NoSuchObjectException {
		getClassLoaderUtilDelegate().unexportObject(target);
	}

	@Override
	public Tie getTie(Remote target) {
		return getClassLoaderUtilDelegate().getTie(target);
	}

	@Override
	public ValueHandler createValueHandler() {
		return getClassLoaderUtilDelegate().createValueHandler();
	}

	@Override
	public String getCodebase(Class clz) {
		return getClassLoaderUtilDelegate().getCodebase(clz);
	}

	@Override
	public Class loadClass(String className, String remoteCodebase, ClassLoader loader) throws ClassNotFoundException {
		return getClassLoaderUtilDelegate().loadClass(className, remoteCodebase, loader);
	}

	@Override
	public boolean isLocal(Stub stub) throws RemoteException {
		return getClassLoaderUtilDelegate().isLocal(stub);
	}

	@Override
	public RemoteException wrapException(Throwable obj) {
		return getClassLoaderUtilDelegate().wrapException(obj);
	}

	@Override
	public Object copyObject(Object obj, ORB orb) throws RemoteException {
		return getClassLoaderUtilDelegate().copyObject(obj, orb);
	}

	@Override
	public Object[] copyObjects(Object[] obj, ORB orb) throws RemoteException {
		return getClassLoaderUtilDelegate().copyObjects(obj, orb);
	}
}
