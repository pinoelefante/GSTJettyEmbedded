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
		//String webDir = AppContextBuilder.class.getResource("./page").toExternalForm();
		//System.out.println("Path ResFiles = "+webDir);
		//webAppContext.setResourceBase(webDir);
		webAppContext.setContextPath("/");
		
		webAppContext.addServlet(OperazioniSerieServlet.class, "/OperazioniSerieServlet");
		webAppContext.addServlet(OperazioniSistema.class, "/OperazioniSistemaServlet");
		
		return webAppContext;
	}
}
