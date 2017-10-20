package com.geostax.admin;

import java.io.File;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.Tomcat.FixContextListener;

public class AdminServer {
	static final int port = 9080;
	static final String docBase = "src/main/webapp/";

	public static void main(String[] args) throws Exception {
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(port);
		tomcat.setBaseDir(docBase);
		tomcat.addWebapp("/gx-scheduler-admin", new File(docBase).getAbsolutePath());
		//tomcat.getHost().setAutoDeploy(false);

		String contextPath = "/";
		StandardContext context = new StandardContext();
		context.setPath(contextPath);
		context.addLifecycleListener(new FixContextListener());
		
		tomcat.getHost().addChild(context);
		tomcat.start();
		tomcat.getServer().await();
	}
}
