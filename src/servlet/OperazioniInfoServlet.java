package servlet;

import gst.infoManager.thetvdb.SerieTVDBFull;
import gst.infoManager.thetvdb.TheTVDB;
import gst.serieTV.GestioneSerieTV;
import gst.serieTV.ProviderSerieTV;
import gst.serieTV.SerieTV;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;

public class OperazioniInfoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = checkParameter("action", resp, req, false);
		Document xml = null;
		switch(action){
			case "getInfoSerie": {
				int idSerie = Integer.parseInt(checkParameter("id", resp, req, false));
				SerieTVDBFull serie = TheTVDB.getInstance().getSerie(idSerie);
				xml = ResponseSender.createResponseInfoSerie(serie);
				break;
			}
			case "getIdTVDB": {
				int idSerie = Integer.parseInt(checkParameter("idSerie", resp, req, false));
				SerieTV s = ProviderSerieTV.getSerieByID(idSerie);
				xml = ResponseSender.createResponseInteger(s==null?0:s.getIDTvdb());
				break;
			}
			case "cercaInfoSerie": {
				
				break;
			}
			case "associa": {
				
				break;
			}
		}
		ResponseSender.sendResponse(resp, xml);
	}
	private String checkParameter(String parametro, HttpServletResponse resp, HttpServletRequest req, boolean paramOpzionale) throws IOException{
		if(req.getParameter(parametro)==null){
			if(paramOpzionale)
				return null;
			else
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST,parametro+" not sended");
		}
		return req.getParameter(parametro);
	}
}
