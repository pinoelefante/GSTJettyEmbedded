package server;

import org.eclipse.jetty.webapp.WebAppContext;

import servlet.OperazioniSerieServlet;
import servlet.TestServlet;

public class AppContextBuilder {
	private WebAppContext webAppContext;

	public WebAppContext buildWebAppContext() {
		webAppContext = new WebAppContext();
		webAppContext.setDescriptor(webAppContext + "/WEB-INF/web.xml");
		webAppContext.setResourceBase("./page");
		webAppContext.setContextPath("/");
		
		webAppContext.addServlet(TestServlet.class, "/TestServlet");
		webAppContext.addServlet(OperazioniSerieServlet.class, "/OperazioniSerieServlet");
		
		return webAppContext;
	}
}
