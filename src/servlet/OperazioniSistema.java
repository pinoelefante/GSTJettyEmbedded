package servlet;

import gst.gui.InterfacciaGrafica;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;

import server.ServerStart;

public class OperazioniSistema extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");
		boolean close = false;
		Document xml = null;
		switch(action){
			case "isOpen":
				xml = ResponseSender.createResponseBoolean(true);
				break;
			case "show":
				InterfacciaGrafica.getInstance().apriInterfaccia();
				xml = ResponseSender.createResponseBoolean(true);
				break;
			case "showOpzioni":
				InterfacciaGrafica.getInstance().mostraFinestraOpzioni();
				xml = ResponseSender.createResponseBoolean(true);
				break;
			case "isAskOnClose":
				xml = ResponseSender.createResponseBoolean(true);
				break;
			case "closeGST":
				xml = ResponseSender.createResponseBoolean(true);
				close = true;
				break;
		}
		ResponseSender.sendResponse(resp, xml);
		if(close){
			ServerStart.close();
		}
	}
}
