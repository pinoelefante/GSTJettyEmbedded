package servlet;

import gst.gui.InterfacciaGrafica;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OperazioniSistema extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");
		switch(action){
			case "isOpen":
				break;
			case "show":
				InterfacciaGrafica.getInstance().apriInterfaccia();
				break;
			case "showOpzioni":
				InterfacciaGrafica.getInstance().mostraFinestraOpzioni();
				break;
		}
		resp.getOutputStream().write("OK".getBytes());
	}
}
