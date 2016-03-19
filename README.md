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

## Example bean definitions required to connect to WAS stateless session bean

Use this to connect to Websphere Application Server secured StatelessSession beans.
You need to set these to be set as system properties (witch -D jvm params):
- -Dorg.omg.CORBA.ORBClass=com.ibm.CORBA.iiop.ORB"
- -Dorg.omg.CORBA.ORBSingletonClass=com.ibm.rmi.corba.ORBSingleton"
- -Djavax.rmi.CORBA.StubClass=com.ibm.rmi.javax.rmi.CORBA.StubDelegateImpl"
- -Djavax.rmi.CORBA.PortableRemoteObjectClass=com.ibm.rmi.javax.rmi.PortableRemoteObject"
- -Djavax.rmi.CORBA.UtilClass=com.github.djarosz.spring.ejb.access.ClassLoaderUtilDelegate"
- -Djavax.rmi.CORBA.ClassLoaderUtilDelegate.UtilClass=com.ibm.ws.orb.WSUtilDelegateImpl"

To configure in spring use some thing like this

```xml
<bean id="ibmCorbaClassloader" class="LibDirClassLoaderFactoryBean"
  scope="prototype">
  <property name="directory" value="/some/dir//lib/websphere"/>
  <property name="recursive" value="true"/>
</bean>

<bean id="commonCorbaProperties" class="org.springframework.beans.factory.config.PropertiesFactoryBean">
  <property name="properties">
    <props>
      <prop key="com.ibm.CORBA.enableLocateRequest">${com.ibm.CORBA.enableLocateRequest}</prop>
      <prop key="com.ibm.CORBA.AllowUserInterrupt">${com.ibm.CORBA.AllowUserInterrupt}</prop>
      <prop key="com.ibm.CORBA.LocateRequestTimeout">${com.ibm.CORBA.LocateRequestTimeout}</prop>
      <prop key="com.ibm.CORBA.ConnectTimeout">${com.ibm.CORBA.ConnectTimeout}</prop>
      <prop key="com.ibm.CORBA.RequestTimeout">${com.ibm.CORBA.RequestTimeout}</prop>
      <prop key="com.ibm.CORBA.FragmentTimeout">${com.ibm.CORBA.FragmentTimeout}</prop>
      <prop key="com.ibm.CORBA.ORBCharEncoding">UTF8</prop>
      <prop key="com.ibm.ssl.contextProvider">IBMJSSE</prop>
      <prop key="com.ibm.ssl.protocol">SSL</prop>
      <prop key="com.ibm.ssl.keyStoreType">JKS</prop>
      <prop key="com.ibm.ssl.keyStore">${websphere.ssl.keyStore}</prop>
      <prop key="com.ibm.ssl.keyStorePassword">WebAS</prop>
      <prop key="com.ibm.ssl.trustStoreType">JKS</prop>
      <prop key="com.ibm.ssl.trustStore">${websphere.ssl.trustStore}</prop>
      <prop key="com.ibm.ssl.trustStorePassword">WebAS</prop>
      <prop key="java.naming.factory.initial">com.ibm.websphere.naming.WsnInitialContextFactory</prop>
      <prop key="java.naming.factory.url.pkgs">com.ibm.ws.naming</prop>
      <prop key="javax.rmi.CORBA.StubClass">com.ibm.rmi.javax.rmi.CORBA.StubDelegateImpl</prop>
      <prop key="javax.rmi.CORBA.PortableRemoteObjectClass">com.ibm.rmi.javax.rmi.PortableRemoteObject</prop>
      <prop key="org.omg.PortableInterceptor.ORBInitializerClass.com.ibm.ejs.ras.RasContextSupport"></prop>
      <prop key="org.omg.PortableInterceptor.ORBInitializerClass.com.ibm.ISecurityLocalObjectBaseL13Impl.CSIClientRI"></prop>
      <prop key="com.ibm.ws.orb.transport.ConnectionInterceptorName">com.ibm.ISecurityLocalObjectBaseL13Impl.SecurityConnectionInterceptor</prop>
      <prop key="com.ibm.ws.orb.transport.WSSSLClientSocketFactoryName">com.ibm.ws.security.orbssl.WSSSLClientSocketFactoryImpl</prop>
      <prop key="com.ibm.CORBA.TransportMode">Pluggable</prop>
      <prop key="com.ibm.CORBA.ForceTunnel">never</prop>
      <prop key="org.omg.CORBA.ORBClass">com.ibm.rmi.iiop.ORB</prop>
      <prop key="org.omg.CORBA.ORBSingletonClass">com.ibm.rmi.corba.ORBSingleton</prop>
      <prop key="com.ibm.CORBA.iiop.SubcontractInit">com.ibm.ws.orb.WSSubcontractInitImpl</prop>
      <prop key="com.ibm.CORBA.ORBPluginClass.com.ibm.ws.orbimpl.transport.WSTransport"></prop>
      <prop key="com.ibm.CORBA.ORBPluginClass.com.ibm.ws.orbimpl.WSORBPropertyManager"></prop>
      <prop key="com.ibm.CORBA.ORBPluginClass.com.ibm.ISecurityUtilityImpl.SecurityPropertyManager"></prop>
      <prop key="com.ibm.CORBA.AllowUserInterrupt">true</prop>
      <prop key="com.ibm.CORBA.securityEnabled">true</prop>
      <prop key="com.ibm.CSI.protocol">csiv2</prop>
      <prop key="com.ibm.CORBA.authenticationTarget">BasicAuth</prop>
      <prop key="com.ibm.CORBA.authenticationRetryEnabled">true</prop>
      <prop key="com.ibm.CORBA.authenticationRetryCount">3</prop>
      <prop key="com.ibm.CORBA.validateBasicAuth">false</prop>
      <prop key="com.ibm.CORBA.loginTimeout">300</prop>
      <prop key="com.ibm.CORBA.loginSource">properties</prop>
      <prop key="com.ibm.CSI.performStateful">true</prop>
      <prop key="com.ibm.CSI.performClientAuthenticationRequired">true</prop>
      <prop key="com.ibm.CSI.performClientAuthenticationSupported">true</prop>
      <prop key="com.ibm.CSI.performTLClientAuthenticationRequired">true</prop>
      <prop key="com.ibm.CSI.performTLClientAuthenticationSupported">true</prop>
      <prop key="com.ibm.CSI.performTransportAssocSSLTLSRequired">true</prop>
      <prop key="com.ibm.CSI.performTransportAssocSSLTLSSupported">true</prop>
      <prop key="com.ibm.CSI.performMessageIntegrityRequired">true</prop>
      <prop key="com.ibm.CSI.performMessageIntegritySupported">true</prop>
      <prop key="com.ibm.CSI.performMessageConfidentialityRequired">true</prop>
      <prop key="com.ibm.CSI.performMessageConfidentialitySupported">true</prop>
    </props>
  </property>
</bean>

<bean id="remoteEJBOnWAS" class="WASRemoteStatelessSessionProxyFactoryBean"
  lazy-init="true">
  <property name="classLoader" ref="ibmCorbaClassloader"/>
  <property name="lookupHomeOnStartup" value="false"/>
  <property name="refreshHomeOnConnectFailure" value="true"/>
  <property name="resourceRef" value="false"/>
  <property name="businessInterface" value="com.example.SessionBean"/>
  <property name="timeout" value="${CORBA.methodInvocationTimeout}"/>
  <property name="jndiName" value="${was.target.session.bean.jndi.name}"/>
  <property name="providerUrl" value="${was.target.session.bean.provider.url}"/>
  <property name="user" value="${was.target.session.bean.login}"/>
  <property name="password" value="${was.target.session.bean.password}"/>
  <property name="jndiEnvironment" ref="commonCorbaProperties"/>
</bean>
```

### External libraries needed to connect to remote WebSphere Application Server

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
