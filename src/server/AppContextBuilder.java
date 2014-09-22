package server;

import org.eclipse.jetty.webapp.WebAppContext;

import servlet.OperazioniInfoServlet;
import servlet.OperazioniSerieServlet;
import servlet.OperazioniSistema;
import servlet.SottotitoliServlet;

public class AppContextBuilder {
	private WebAppContext webAppContext;

	public WebAppContext buildWebAppContext() {
		webAppContext = new WebAppContext();
		webAppContext.setDescriptor(webAppContext + "/WEB-INF/web.xml");
		webAppContext.setResourceBase("./page");
		webAppContext.setContextPath("/");
		
		webAppContext.addServlet(OperazioniSerieServlet.class, "/OperazioniSerieServlet");
		webAppContext.addServlet(OperazioniSistema.class, "/OperazioniSistemaServlet");
		webAppContext.addServlet(SottotitoliServlet.class, "/OperazioniSottotitoliServlet");
		webAppContext.addServlet(OperazioniInfoServlet.class, "/OperazioniInfoServlet");
		
		return webAppContext;
	}
}
