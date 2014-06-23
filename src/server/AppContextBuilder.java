package server;

import org.eclipse.jetty.webapp.WebAppContext;

import servlet.TestServlet;

public class AppContextBuilder {
	private WebAppContext webAppContext;

	public WebAppContext buildWebAppContext() {
		webAppContext = new WebAppContext();
		webAppContext.setDescriptor(webAppContext + "/WEB-INF/web.xml");
		webAppContext.setResourceBase(".");
		webAppContext.setContextPath("/gst");
		
		webAppContext.addServlet(TestServlet.class, "/TestServlet");
		
		return webAppContext;
	}
}
