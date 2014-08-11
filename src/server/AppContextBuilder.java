package server;

import org.eclipse.jetty.webapp.WebAppContext;

import servlet.OperazioniSerieServlet;
import servlet.OperazioniSistema;

public class AppContextBuilder {
	private WebAppContext webAppContext;

	public WebAppContext buildWebAppContext() {
		webAppContext = new WebAppContext();
		webAppContext.setDescriptor(webAppContext + "/WEB-INF/web.xml");
		webAppContext.setResourceBase("./page");
		webAppContext.setContextPath("/");
		
		webAppContext.addServlet(OperazioniSerieServlet.class, "/OperazioniSerieServlet");
		webAppContext.addServlet(OperazioniSistema.class, "/OperazioniSistemaServlet");
		
		return webAppContext;
	}
}
