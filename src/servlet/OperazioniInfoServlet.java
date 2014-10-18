package servlet;

import gst.infoManager.thetvdb.EpisodioTVDB;
import gst.infoManager.thetvdb.SerieTVDB;
import gst.infoManager.thetvdb.SerieTVDBFull;
import gst.infoManager.thetvdb.TheTVDB;
import gst.serieTV.Episodio;
import gst.serieTV.GestioneSerieTV;
import gst.serieTV.ProviderSerieTV;
import gst.serieTV.SerieTV;

import java.io.IOException;
import java.util.ArrayList;

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
			case "associa": {
				int idSerie = Integer.parseInt(checkParameter("idSerie", resp, req, false));
				int idTvdb = Integer.parseInt(checkParameter("id_tvdb", resp, req, false));
				boolean r=ProviderSerieTV.associaSerieTVDB(idSerie, idTvdb);
				xml = ResponseSender.createResponseBoolean(r);
				break;
			}
			case "cercaSerieAssociabili": {
				int idSerie = Integer.parseInt(checkParameter("id", resp, req, false));
				SerieTV serie = ProviderSerieTV.getSerieByID(idSerie);
				if(serie==null)
					xml=ResponseSender.createResponseBoolean(false);
				else {
					ArrayList<SerieTVDB> list=TheTVDB.getInstance().cercaSerie(serie);
					System.out.println(list.size()+" serie trovate");
					xml=ResponseSender.createResponseTVDBList(list);
				}
				break;
			}
			case "getIdTVDB": {
				int idSerie = Integer.parseInt(checkParameter("idSerie", resp, req, false));
				SerieTV s = ProviderSerieTV.getSerieByID(idSerie);
				if(s==null)
					xml = ResponseSender.createResponseBoolean(false);
				else
					xml = ResponseSender.createResponseInteger(s.getIDTvdb());
				break;
			}
			case "getInfoEpisodio": {
				int idEpisodio = Integer.parseInt(checkParameter("id", resp, req, false));
				Episodio episodio = ProviderSerieTV.getEpisodio(idEpisodio);
				SerieTV serie = ProviderSerieTV.getSerieByID(episodio.getSerie());
				EpisodioTVDB ep = TheTVDB.getInstance().getEpisodio(serie.getIDTvdb(), episodio.getStagione(), episodio.getEpisodio());
				if(episodio.getIdTvDB()<=0){
					GestioneSerieTV.getInstance().associaEpisodioTVDB(idEpisodio, ep.getIdEpisodio());
				}
				xml = ResponseSender.createResponseTVDBEpisodio(ep);
				break;
			}
			case "getInfoSerie": {
				int idSerie = Integer.parseInt(checkParameter("id", resp, req, false));
				String f = checkParameter("force", resp, req, true);
				boolean force = Boolean.parseBoolean(f==null?"false":f);
				SerieTVDBFull serie = TheTVDB.getInstance().getSerie(idSerie, force);
				xml = (serie!=null?ResponseSender.createResponseInfoSerie(serie):ResponseSender.createResponseBoolean(false));
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
