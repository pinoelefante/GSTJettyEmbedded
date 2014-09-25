package servlet;

import gst.serieTV.Episodio;
import gst.serieTV.SerieTV;
import gst.sottotitoli.GestoreSottotitoli;
import gst.sottotitoli.ProviderSottotitoli;
import gst.sottotitoli.SerieSub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;

import util.Object3Value;

public class SottotitoliServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");
		if(action == null || action.isEmpty()){
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "action not sended");
			return;
		}
		Document xml = null;
		switch(action){
			case "getProviders": {
				Map<ProviderSottotitoli, ArrayList<SerieSub>> map = GestoreSottotitoli.getInstance().getProviders();
				xml = ResponseSender.createProviderSottotitoli(map);
				break;
			}
			case "getSottotitoliDaScaricare": {
				Map<SerieTV, ArrayList<Episodio>> map = GestoreSottotitoli.getInstance().sottotitoliDaScaricare();
				xml = ResponseSender.createResponseSubDownload(map);
				break;
			}
			case "scaricaSubByID": {
				Integer idEPisodio = Integer.parseInt(checkParameter("id", resp, req, false));
				boolean r = GestoreSottotitoli.getInstance().scaricaSottotitolo(idEPisodio);
				xml = ResponseSender.createResponseBoolean(r);
				break;
			}
			case "removeSubByID": {
				int idEpisodio = Integer.parseInt(checkParameter("id", resp, req, false));
				GestoreSottotitoli.setSottotitoloDownload(idEpisodio, false);
				xml = ResponseSender.createResponseBoolean(true);
				break;
			}
			case "associa": {
				int idSerie = Integer.parseInt(checkParameter("idSerie", resp, req, false));
				int idProvider = Integer.parseInt(checkParameter("idProvider", resp, req, false));
				int idSerieSub = Integer.parseInt(checkParameter("idSerieSub", resp, req, false));
				System.out.println("idSerie="+idSerie+" idSerieSub="+idSerieSub+" idProvider="+idProvider);
				boolean r=GestoreSottotitoli.getInstance().associaSerie(idSerie, idProvider, idSerieSub);
				xml=ResponseSender.createResponseBoolean(r);
				break;
			}
			case "disassocia": {
				int idSerie = Integer.parseInt(checkParameter("idSerie", resp, req, false));
				int idProvider = Integer.parseInt(checkParameter("idProvider", resp, req, false));
				boolean r=GestoreSottotitoli.getInstance().disassociaSerie(idSerie, idProvider);
				System.out.println("idSerie="+idSerie+" idProvider="+idProvider);
				xml=ResponseSender.createResponseBoolean(r);
				break;
			}
			case "getLogSub": {
				ArrayList<Object3Value<ProviderSottotitoli, SerieTV, Episodio>> list = GestoreSottotitoli.getInstance().getLast50LogSub();
				xml = ResponseSender.createResponseLogSub(list);
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
