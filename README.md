# spring-classloader-ejb-access

This project was created because I needed to connect to SessionBeasn hosted on WebSphere Application Server from java application
runing on non IBM JVM (Oraccle/openjdk). Spring already has RemoteStatelessSessionProxyFactoryBean which allows you to connect to remote server
but in my configuration i had many such remote stateless session beans each had to use different user credentials.
Since authentication code is hardcoded into IBM libraries (which are obviously not open source) and uses some kind of static class variables the solution
is to load and invoke each such stateless session bean in different classloader. It is not optimal in context of memory usage but it works.

This project extends spring RemoteStatelessSessionProxyFactoryBean and allows you to create and invoke remote session bean using
classloader given as property. This mechanism is rathre generic so you can probably use this module for other purposes (not just connecting to WAS).

Please look and javadoc of classes listed below to find out how to configure and use this module.

## Main classes

If you wat to use this code you should probably look an these classes and their javadoc. Three first classes is wath you probably want
if you need to connect to remote stateless session bean on WAS

* [LibDirClassLoaderFactoryBean](./src/main/java/com/github/djarosz/spring/ejb/access/LibDirClassLoaderFactoryBean.java)
  use this to define class loader which will include all classes in all jars in specified directory
* [WASRemoteStatelessSessionProxyFactoryBean](./src/main/java/com/github/djarosz/spring/ejb/access/WASRemoteStatelessSessionProxyFactoryBean.java)
  This one is used to connect to WebSphere application server from non IBM JVM. **javadoc includes spring configuration example**
* [ClassLoaderUtilDelegate](./src/main/java/com/github/djarosz/spring/ejb/access/ClassLoaderUtilDelegate.java)
  Read javadoc as it gives cutial part of how to setup system wide java properties so WAS connections are actualy handled by appropriate classloader
* [ClassLoaderAwareRemoteStatelessSessionProxyFactoryBean](./src/main/java/com/github/djarosz/spring/ejb/access/ClassLoaderAwareRemoteStatelessSessionProxyFactoryBean.java)
  crates proxy to remote stateless session bean and ensures all calls are made in specified classloader
* [ClassLoaderAwareJndiTemplate](./src/main/java/com/github/djarosz/spring/ejb/access/ClassLoaderAwareJndiTemplate.java)
  used to look up remote session beans in context of given classloader

## External libraries needed to connect to remote WebSphre Application Server

For use witch WAS you still need some jar's from IBM (it's probably WAS version dependent). Usually these will suffice:
- bootstrap.jar
- ecutils.jar
- ffdc.jar
- ibmcfw.jar
- ibmext.jar
- ibmjsseprovider2.jar
- ibmkeycert.jar
- ibmorb.jar
- idl.jar
- iwsorb.jar
- iwsorbutil.jar
- log4j.jar
- namingclient.jar
- naming.jar
- ras.jar
- sas.jar
- security.jar
- utils.jar
- was-wssecurity.jar
- wsexception.jar

## License

spring-classloader-ejb-access is licensed under the [MIT](./LICENSE).
