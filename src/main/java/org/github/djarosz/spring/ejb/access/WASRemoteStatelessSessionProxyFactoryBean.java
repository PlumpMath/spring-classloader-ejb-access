package org.github.djarosz.spring.ejb.access;

import java.util.Timer;
import java.util.TimerTask;
import javax.naming.NamingException;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;

/**
 * Use this to connect to Websphere Application Server secured StatelessSession Beans
 *
 * To properly work this needs at least these properties to be set as system properties (witch -D jvm params):
 * <ul>
 *     <li>-Dorg.omg.CORBA.ORBClass=com.ibm.CORBA.iiop.ORB"
 *     <li>-Dorg.omg.CORBA.ORBSingletonClass=com.ibm.rmi.corba.ORBSingleton"
 *     <li>-Djavax.rmi.CORBA.StubClass=com.ibm.rmi.javax.rmi.CORBA.StubDelegateImpl"
 *     <li>-Djavax.rmi.CORBA.PortableRemoteObjectClass=com.ibm.rmi.javax.rmi.PortableRemoteObject"
 *     <li>-Djavax.rmi.CORBA.UtilClass=ClassLoaderUtilDelegate"
 *     <li>-Djavax.rmi.CORBA.ClassLoaderUtilDelegate.UtilClass=com.ibm.ws.orb.WSUtilDelegateImpl"
 * </ul>
 *
 * To configure in spring use some thing like this
 * <pre>
 *     	&lt;bean id="ibmCorbaClassloader" class="LibDirClassLoaderFactoryBean" scope="prototype"&gt;
 *			&lt;property name="directory" value="${catalina.home}/lib-nsp/websphere"/&gt;
 *			&lt;property name="recursive" value="true"/&gt;
 *		&lt;/bean&gt;
 *
 *		&lt;bean id="commonCorbaProperties" class="org.springframework.beans.factory.config.PropertiesFactoryBean"&gt;
 *			&lt;property name="properties"&gt;
 *				&lt;props&gt;
 *					&lt;prop key="com.ibm.CORBA.enableLocateRequest"&gt;${com.ibm.CORBA.enableLocateRequest}&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.AllowUserInterrupt"&gt;${com.ibm.CORBA.AllowUserInterrupt}&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.LocateRequestTimeout"&gt;${com.ibm.CORBA.LocateRequestTimeout}&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.ConnectTimeout"&gt;${com.ibm.CORBA.ConnectTimeout}&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.RequestTimeout"&gt;${com.ibm.CORBA.RequestTimeout}&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.FragmentTimeout"&gt;${com.ibm.CORBA.FragmentTimeout}&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.ORBCharEncoding"&gt;UTF8&lt;/prop&gt;
 *					&lt;prop key="com.ibm.ssl.contextProvider"&gt;IBMJSSE&lt;/prop&gt;
 *					&lt;prop key="com.ibm.ssl.protocol"&gt;SSL&lt;/prop&gt;
 *					&lt;prop key="com.ibm.ssl.keyStoreType"&gt;JKS&lt;/prop&gt;
 *					&lt;prop key="com.ibm.ssl.keyStore"&gt;${websphere.ssl.keyStore}&lt;/prop&gt;
 *					&lt;prop key="com.ibm.ssl.keyStorePassword"&gt;WebAS&lt;/prop&gt;
 *					&lt;prop key="com.ibm.ssl.trustStoreType"&gt;JKS&lt;/prop&gt;
 *					&lt;prop key="com.ibm.ssl.trustStore"&gt;${websphere.ssl.trustStore}&lt;/prop&gt;
 *					&lt;prop key="com.ibm.ssl.trustStorePassword"&gt;WebAS&lt;/prop&gt;
 *					&lt;prop key="java.naming.factory.initial"&gt;com.ibm.websphere.naming.WsnInitialContextFactory&lt;/prop&gt;
 *					&lt;prop key="java.naming.factory.url.pkgs"&gt;com.ibm.ws.naming&lt;/prop&gt;
 *					&lt;prop key="javax.rmi.CORBA.StubClass"&gt;com.ibm.rmi.javax.rmi.CORBA.StubDelegateImpl&lt;/prop&gt;
 *					&lt;prop key="javax.rmi.CORBA.PortableRemoteObjectClass"&gt;com.ibm.rmi.javax.rmi.PortableRemoteObject&lt;/prop&gt;
 *					&lt;prop key="org.omg.PortableInterceptor.ORBInitializerClass.com.ibm.ejs.ras.RasContextSupport"&gt;&lt;/prop&gt;
 *					&lt;prop key="org.omg.PortableInterceptor.ORBInitializerClass.com.ibm.ISecurityLocalObjectBaseL13Impl.CSIClientRI"&gt;&lt;/prop&gt;
 *					&lt;prop key="com.ibm.ws.orb.transport.ConnectionInterceptorName"&gt;com.ibm.ISecurityLocalObjectBaseL13Impl.SecurityConnectionInterceptor&lt;/prop&gt;
 *					&lt;prop key="com.ibm.ws.orb.transport.WSSSLClientSocketFactoryName"&gt;com.ibm.ws.security.orbssl.WSSSLClientSocketFactoryImpl&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.TransportMode"&gt;Pluggable&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.ForceTunnel"&gt;never&lt;/prop&gt;
 *					&lt;prop key="org.omg.CORBA.ORBClass"&gt;com.ibm.rmi.iiop.ORB&lt;/prop&gt;
 *					&lt;prop key="org.omg.CORBA.ORBSingletonClass"&gt;com.ibm.rmi.corba.ORBSingleton&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.iiop.SubcontractInit"&gt;com.ibm.ws.orb.WSSubcontractInitImpl&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.ORBPluginClass.com.ibm.ws.orbimpl.transport.WSTransport"&gt;&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.ORBPluginClass.com.ibm.ws.orbimpl.WSORBPropertyManager"&gt;&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.ORBPluginClass.com.ibm.ISecurityUtilityImpl.SecurityPropertyManager"&gt;&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.AllowUserInterrupt"&gt;true&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.securityEnabled"&gt;true&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CSI.protocol"&gt;csiv2&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.authenticationTarget"&gt;BasicAuth&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.authenticationRetryEnabled"&gt;true&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.authenticationRetryCount"&gt;3&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.validateBasicAuth"&gt;false&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.loginTimeout"&gt;300&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CORBA.loginSource"&gt;properties&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CSI.performStateful"&gt;true&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CSI.performClientAuthenticationRequired"&gt;true&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CSI.performClientAuthenticationSupported"&gt;true&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CSI.performTLClientAuthenticationRequired"&gt;true&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CSI.performTLClientAuthenticationSupported"&gt;true&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CSI.performTransportAssocSSLTLSRequired"&gt;true&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CSI.performTransportAssocSSLTLSSupported"&gt;true&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CSI.performMessageIntegrityRequired"&gt;true&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CSI.performMessageIntegritySupported"&gt;true&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CSI.performMessageConfidentialityRequired"&gt;true&lt;/prop&gt;
 *					&lt;prop key="com.ibm.CSI.performMessageConfidentialitySupported"&gt;true&lt;/prop&gt;
 *			&lt;/props&gt;
 *		&lt;/property&gt;
 *	&lt;/bean&gt;
 *
 *	&lt;!-- CCFacade proxy --&gt;
 *		&lt;bean id="ccFacade"
 *		class="WASRemoteStatelessSessionProxyFactoryBean"
 *		lazy-init="true"&gt;
 *		&lt;property name="classLoader" ref="ibmCorbaClassloader"/&gt;
 *		&lt;property name="lookupHomeOnStartup" value="false"/&gt;
 *		&lt;property name="refreshHomeOnConnectFailure" value="true"/&gt;
 *		&lt;property name="resourceRef" value="false"/&gt;
 *		&lt;property name="businessInterface" value="com.example.SessionBean"/&gt;
 *		&lt;property name="timeout" value="${CORBA.methodInvocationTimeout}"/&gt;
 *		&lt;property name="jndiName" value="${ccfacade.jndi.name}"/&gt;
 *		&lt;property name="providerUrl" value="${ccfacade.provider.url}"/&gt;
 *		&lt;property name="user" value="${ccfacade.login}"/&gt;
 *		&lt;property name="password" value="${ccfacade.password}"/&gt;
 *		&lt;property name="jndiEnvironment" ref="commonCorbaProperties"/&gt;
 *	&lt;/bean&gt;
 * </pre>
 */
public class WASRemoteStatelessSessionProxyFactoryBean extends ClassLoaderAwareRemoteStatelessSessionProxyFactoryBean {

	private static final Logger log = Logger.getLogger(WASRemoteStatelessSessionProxyFactoryBean.class);

	public static final int NO_TIMEOUT = -1;

	private long timeout = NO_TIMEOUT;

	private Timer timer;

	private String providerUrl;

	private String user;

	private String password;

	@Override
	public void afterPropertiesSet() throws NamingException {
		super.afterPropertiesSet();
		if (hasTimeout() && timer == null) {
			timer = new Timer();
		}

		getJndiTemplate().getEnvironment().setProperty("java.naming.provider.url", providerUrl);
		getJndiTemplate().getEnvironment().setProperty("com.ibm.CORBA.loginUserid", user);
		getJndiTemplate().getEnvironment().setProperty("com.ibm.CORBA.loginPassword", password);
	}

	protected Object doInvoke(MethodInvocation invocation) throws Throwable {
		TimerTask interruptTask = null;

		try {
			if (hasTimeout()) {
				interruptTask = interruptAfter(Thread.currentThread(), timeout);
			}
			return super.doInvoke(invocation);
		} finally {
			if (hasTimeout() && interruptTask != null) {
				interruptTask.cancel();
			}
			Thread.interrupted();
		}
	}

	private TimerTask interruptAfter(final Thread thread, long timeout) {
		TimerTask timerTask = new TimerTask() {
			public void run() {
				log.debug("Before interrupt thread: " + thread.getName());
				thread.interrupt();
				log.debug("After interrupt thread");
			}
		};
		timer.schedule(timerTask, timeout);

		return timerTask;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public long getTimeout() {
		return timeout;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public String getProviderUrl() {
		return providerUrl;
	}

	public void setProviderUrl(String providerUrl) {
		this.providerUrl = providerUrl;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	private boolean hasTimeout() {
		return timeout > 0;
	}

}
