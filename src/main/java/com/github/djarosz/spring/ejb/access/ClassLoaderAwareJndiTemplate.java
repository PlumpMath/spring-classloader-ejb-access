package com.github.djarosz.spring.ejb.access;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.NamingException;
import org.apache.log4j.Logger;
import org.springframework.jndi.JndiCallback;
import org.springframework.jndi.JndiTemplate;

/**
 * JndiTemplate Performs all remote calls in specified class loader.
 */
public class ClassLoaderAwareJndiTemplate extends JndiTemplate {

	private static final Logger LOGGER = Logger.getLogger(ClassLoaderAwareJndiTemplate.class);

	private ClassLoader classLoader;

	public ClassLoaderAwareJndiTemplate() {
	}

	public ClassLoaderAwareJndiTemplate(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public ClassLoaderAwareJndiTemplate(ClassLoader classLoader, Properties environment) {
		super(environment);
		this.classLoader = classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public Object execute(final JndiCallback contextCallback) throws NamingException {
		return doInClassLoader(new ClassLoaderActionCallback<Object>() {
			@Override
			public Object execute() throws Throwable {
				return ClassLoaderAwareJndiTemplate.super.execute(contextCallback);
			}
		});
	}

	@Override
	protected Context createInitialContext() throws NamingException  {
		return doInClassLoader(new ClassLoaderActionCallback<Context>() {
			@Override
			public Context execute() throws Throwable {
				return ClassLoaderAwareJndiTemplate.super.createInitialContext();
			}
		});
	}

	@Override
	public void releaseContext(final Context ctx) {
		try {
			ClassLoaderActionHelper.doInClassLoader(classLoader, new ClassLoaderActionCallback<Object>() {
				@Override
				public Object execute() throws Throwable {
					ClassLoaderAwareJndiTemplate.super.releaseContext(ctx);
					return null;
				}
			});
		} catch (Throwable e) {
			LOGGER.error("Error while performing operation witch switched class loader", e);
			throw new ClassLoaderActionRuntimeException(e);
		}
	}

	private <T> T doInClassLoader(ClassLoaderActionCallback<T> action) throws NamingException {
		try {
			return ClassLoaderActionHelper.doInClassLoader(classLoader, action);
		} catch (NamingException e) {
			throw e;
		} catch (Throwable e) {
			LOGGER.error("Error while performing operation witch switched class loader", e);
			throw new ClassLoaderActionRuntimeException(e);
		}
	}

}
