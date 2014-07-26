package servlet;

import gst.serieTV.GestioneSerieTV;
import gst.serieTV.SerieTV;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;

public class OperazioniSerieServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private GestioneSerieTV manager;
	
	@Override
	public void init() throws ServletException {
		super.init();
		manager = GestioneSerieTV.getInstance();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)	throws ServletException, IOException {
		String action = req.getParameter("action");
		if(action == null || action.isEmpty()){
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "action not sended");
			return;
		}
		Document xml = null;
		switch(action){
			case "add": {
				int provider = Integer.parseInt(checkParameter("provider", resp, req));
				int serie = Integer.parseInt(checkParameter("serie", resp, req));
				boolean res = manager.aggiungiSerieAPreferiti(provider, serie);
				xml = ResponseSender.createResponseBoolean(res);
				break;
			}
			case "getSeriePreferite":
				xml = ResponseSender.createResponseSerie(manager.getElencoSeriePreferite());
				break;
			case "getProviders":
				xml = ResponseSender.createResponseProviders(manager.getProviders());
				break;
			case "getSerieFromProvider": {
				try {
					int provider = Integer.parseInt(checkParameter("provider", resp, req));
					ArrayList<SerieTV> serie = manager.getSerieFromProvider(provider);
					xml = ResponseSender.createResponseSerie(serie);
				}
				catch(Exception e){
					xml = ResponseSender.createResponseBoolean(false);
				}
				break;
			}
			case "getSerieNuoveFromProvider": {
				try {
					int provider = Integer.parseInt(checkParameter("provider", resp, req));
					ArrayList<SerieTV> serie = manager.getSerieNuoveByProvider(provider);
					xml = ResponseSender.createResponseSerie(serie);
				}
				catch(Exception e){
					xml = ResponseSender.createResponseBoolean(false);
				}
				break;
			}
			case "updateListSeries":{
				int provider = Integer.parseInt(checkParameter("provider", resp, req));
				boolean res = manager.aggiornaListaSerie(provider);
				xml = ResponseSender.createResponseBoolean(res);
				break;
			}
		}
		
		ResponseSender.sendResponse(resp, xml);
	}
	private String checkParameter(String parametro, HttpServletResponse resp, HttpServletRequest req) throws IOException{
		if(req.getParameter(parametro)==null)
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,parametro+" not sended");
		return req.getParameter(parametro);
	}

}
