package com.geostax.scheduler.core.rpc.netcom;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import com.geostax.scheduler.core.rpc.codec.RpcRequest;
import com.geostax.scheduler.core.rpc.codec.RpcResponse;
import com.geostax.scheduler.core.rpc.jetty.JettyClient;

/**
 * rpc proxy
 * 
 * @author xuxueli 2015-10-29 20:18:32
 */
public class NetComClientProxy implements FactoryBean<Object> {
	private static final Logger logger = LoggerFactory.getLogger(NetComClientProxy.class);

	// ---------------------- config ----------------------
	private Class<?> iface;
	String serverAddress;
	JettyClient client = new JettyClient();

	public NetComClientProxy(Class<?> iface, String serverAddress) {
		this.iface = iface;
		this.serverAddress = serverAddress;
	}

	@Override
	public Object getObject() throws Exception {
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { iface },
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

						// request
						RpcRequest request = new RpcRequest();
						request.setServerAddress(serverAddress);
						request.setCreateMillisTime(System.currentTimeMillis());
						request.setClassName(method.getDeclaringClass().getName());
						request.setMethodName(method.getName());
						request.setParameterTypes(method.getParameterTypes());
						request.setParameters(args);

						// send
						RpcResponse response = client.send(request);

						// valid response
						if (response == null) {
							logger.error(">>>>>>>>>>> xxl-rpc netty response not found.");
							throw new Exception(">>>>>>>>>>> xxl-rpc netty response not found.");
						}
						if (response.isError()) {
							throw new RuntimeException(response.getError());
						} else {
							return response.getResult();
						}

					}
				});
	}

	@Override
	public Class<?> getObjectType() {
		return iface;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

}
