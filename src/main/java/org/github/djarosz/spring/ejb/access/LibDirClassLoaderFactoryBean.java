package org.github.djarosz.spring.ejb.access;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Creates {@link java.lang.ClassLoader} bean with all classes in directory given as <var>directory</var> property.
 * Also includes a *.jar files in specified directory (optionally searching for then recursively)
 */
public class LibDirClassLoaderFactoryBean implements BeanNameAware, BeanClassLoaderAware, FactoryBean, InitializingBean {

	private static final Logger LOGGER = Logger.getLogger(LibDirClassLoaderFactoryBean.class);

	private File directory;

	private ClassLoader contextClassLoader;

	private boolean recursive;

	private String beanName;

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.contextClassLoader = classLoader;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (directory == null) {
			throw new BeanCreationException("Loader[" + beanName + "] : directory is required.");
		}

		if (!directory.exists() || !directory.canRead()) {
			throw new BeanCreationException("Loader[" + beanName + "]: directory'" + directory + "' does not exist or is not readable");
		}
	}

	@Override
	public Object getObject() throws Exception {
		LinkedList<URL> urls = new LinkedList<URL>();
		URL dirURL = directory.toURI().toURL();

		log("Adding url: " + dirURL);
		urls.add(dirURL);
		addJars(urls, directory);

		return new URLClassLoader(urls.toArray(new URL[urls.size()]), contextClassLoader);
	}

	private void addJars(LinkedList<URL> urls, File dir) throws MalformedURLException {
		File[] jars = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return (recursive && pathname.isDirectory())
						|| (!pathname.isDirectory() && pathname.getName().endsWith(".jar"));
			}
		});

		for (File jar : jars) {
			if (jar.isDirectory()) {
				addJars(urls, jar);
			} else {
				URL jarURL = jar.toURI().toURL();
				log("Adding url: " + jarURL);
				urls.add(jar.toURI().toURL());
			}
		}
	}

	@Override
	public Class getObjectType() {
		return ClassLoader.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}

	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	private void log(String message) {
		LOGGER.info("Loader[" + beanName + "]: " + message);
	}

}
