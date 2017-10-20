package com.geostax.scheduler.core.rpc.netcom;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import com.geostax.scheduler.core.model.ReturnT;
import com.geostax.scheduler.core.rpc.codec.RpcRequest;
import com.geostax.scheduler.core.rpc.codec.RpcResponse;
import com.geostax.scheduler.core.rpc.jetty.JettyServer;

/**
 * netcom init
 * 
 * @author xuxueli 2015-10-31 22:54:27
 */
public class NetComServerFactory  {
	private static final Logger logger = LoggerFactory.getLogger(NetComServerFactory.class);

	// ---------------------- server start ----------------------
	JettyServer server = new JettyServer();
	public void start(int port, String ip, String appName) throws Exception {
		server.start(port, ip, appName);
	}

	// ---------------------- server destroy ----------------------
	public void destroy(){
		server.destroy();
	}

	// ---------------------- server init ----------------------
	/**
	 * init local rpc service map
	 */
	private static Map<String, Object> serviceMap = new HashMap<String, Object>();
	public static void putService(Class<?> iface, Object serviceBean){
		serviceMap.put(iface.getName(), serviceBean);
	}
	public static RpcResponse invokeService(RpcRequest request, Object serviceBean) {
		if (serviceBean==null) {
			serviceBean = serviceMap.get(request.getClassName());
		}
		if (serviceBean == null) {
			// TODO
		}

		RpcResponse response = new RpcResponse();

		if (System.currentTimeMillis() - request.getCreateMillisTime() > 180000) {
			response.setResult(new ReturnT<String>(ReturnT.FAIL_CODE, "the timestamp difference between admin and executor exceeds the limit."));
			return response;
		}

		try {
			Class<?> serviceClass = serviceBean.getClass();
			String methodName = request.getMethodName();
			Class<?>[] parameterTypes = request.getParameterTypes();
			Object[] parameters = request.getParameters();

			FastClass serviceFastClass = FastClass.create(serviceClass);
			FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);

			Object result = serviceFastMethod.invoke(serviceBean, parameters);

			response.setResult(result);
		} catch (Throwable t) {
			t.printStackTrace();
			response.setError(t.getMessage());
		}

		return response;
	}

}
