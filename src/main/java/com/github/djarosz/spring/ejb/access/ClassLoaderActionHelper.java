package com.github.djarosz.spring.ejb.access;

import org.apache.log4j.Logger;

public abstract class ClassLoaderActionHelper {

	private static final Logger log = Logger.getLogger(ClassLoaderActionHelper.class);

	public static <T> T doInClassLoader(ClassLoader classLoader, ClassLoaderActionCallback<T> callback) throws Throwable {
		Thread currentThread = Thread.currentThread();
		ClassLoader callerClassLoader  = currentThread.getContextClassLoader();

		if (callerClassLoader == classLoader) { // Same class loader. No need to switch
			return callback.execute();
		}

		log.trace("Calling class loader: " + currentThread.getContextClassLoader());
		try {
			currentThread.setContextClassLoader(classLoader);
			log.trace("Switched class loader to: " + currentThread.getContextClassLoader());
			return callback.execute();
		} finally {
			currentThread.setContextClassLoader(callerClassLoader);
			log.trace("Class loader switched back to: " + currentThread.getContextClassLoader());
		}
	}

}
