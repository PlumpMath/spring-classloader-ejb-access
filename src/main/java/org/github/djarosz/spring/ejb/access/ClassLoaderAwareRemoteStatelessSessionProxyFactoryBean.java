package org.github.djarosz.spring.ejb.access;

import static org.github.djarosz.spring.ejb.access.ClassLoaderActionHelper.doInClassLoader;

import java.util.Properties;
import javax.naming.NamingException;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.ejb.access.SimpleRemoteStatelessSessionProxyFactoryBean;
import org.springframework.jndi.JndiTemplate;

/**
 * Creates proxy to remote SessionBean using specified class loader. Usually different then then one used
 * by ApplicationContext creating this bean.
 */
public class ClassLoaderAwareRemoteStatelessSessionProxyFactoryBean extends SimpleRemoteStatelessSessionProxyFactoryBean {

	protected ClassLoader classLoader;

	private Properties jndiEnvironment;

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	@Override
	public void setJndiTemplate(JndiTemplate jndiTemplate) {
		this.jndiEnvironment = jndiTemplate.getEnvironment();
	}

	@Override
	public void setJndiEnvironment(Properties jndiEnvironment) {
		this.jndiEnvironment = jndiEnvironment;
	}

	@Override
	public void afterPropertiesSet() throws NamingException {
		if (classLoader == null) {
			throw new BeanCreationException("Required property 'classLoader' not set");
		}

		setBeanClassLoader(classLoader); // This should called before call to supper
		super.setJndiTemplate(new ClassLoaderAwareJndiTemplate(classLoader, (Properties) jndiEnvironment.clone()));
		super.afterPropertiesSet();
	}

	@Override
	protected Object doInvoke(final MethodInvocation invocation) throws Throwable {
		return doInClassLoader(classLoader, new ClassLoaderActionCallback<Object>() {
			@Override
			public Object execute() throws Throwable {
				return ClassLoaderAwareRemoteStatelessSessionProxyFactoryBean.super.doInvoke(invocation);
			}
		});
	}

}
