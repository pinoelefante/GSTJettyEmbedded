package servlet;

import gst.gui.InterfacciaGrafica;
import gst.programma.Settings;
import gst.serieTV.Episodio;
import gst.serieTV.GestioneSerieTV;
import gst.serieTV.ProviderSerieTV;
import gst.serieTV.SerieTV;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;

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
				int serie = Integer.parseInt(checkParameter("serie", resp, req, false));
				boolean res = manager.aggiungiSerieAPreferiti(serie);
				xml = ResponseSender.createResponseBoolean(res);
				break;
			}
			case "deleteFile": {
				int idEpisodio = Integer.parseInt(checkParameter("episodio", resp, req, false));
				xml = ResponseSender.createResponseBoolean(manager.deleteEpisodio(idEpisodio));
				break;
			}
			case "download": {
				int idEpisodio = Integer.parseInt(checkParameter("episodio", resp, req, false));
				boolean d = false;
				try {
					d = manager.downloadEpisodio(idEpisodio);
				}
				catch(Exception e){
					d=false;
				}
				xml = ResponseSender.createResponseDownload(idEpisodio, d);
				break;
			}
			case "getElencoSerie": {
				ArrayList<SerieTV> serie = manager.getSerie();
				xml = ResponseSender.createResponseSerie(serie);
				break;
			}
			case "getEpisodiBySerie": {
				int idSerie = Integer.parseInt(checkParameter("serie", resp, req, false));
				ArrayList<Episodio> episodi = manager.getEpisodiSerie(idSerie);
				xml = ResponseSender.createResponseEpisodi(idSerie, episodi);
				break;
			}
			case "getEpisodiDaVedere": {
				ArrayList<Entry<SerieTV, ArrayList<Episodio>>> map = manager.getEpisodiDaVedere();
				xml = ResponseSender.createResponseEpisodiVedere(map);
				break;
			}
			case "getEpisodiDaScaricareBySerie": {
				int idSerie = Integer.parseInt(checkParameter("serie", resp, req, false));
				ArrayList<Episodio> episodi = manager.getEpisodiDaScaricareBySerie(idSerie);
				xml = ResponseSender.createResponseEpisodi(idSerie, episodi);
				break;
			}
			case "getSeriePreferite":
				xml = ResponseSender.createResponseSerie(manager.getElencoSeriePreferite());
				break;
			case "getSerieNuove": {
				ArrayList<SerieTV> serie = manager.getSerieNuove();
				xml = ResponseSender.createResponseSerie(serie);
				break;
			}
			case "openFolder": {
				int id = Integer.parseInt(checkParameter("id", resp, req, false));
				SerieTV serie = ProviderSerieTV.getSerieByID(id);
				boolean r = InterfacciaGrafica.getInstance().openFolder(Settings.getInstance().getDirectoryDownload()+serie.getFolderSerie());
				xml = ResponseSender.createResponseBoolean(r);
				break;
			}
			case "play": {
				try {
					int idEpisodio = Integer.parseInt(checkParameter("episodio", resp, req, false));
					boolean r=manager.playVideo(idEpisodio);
					xml=ResponseSender.createResponseBoolean(r);
				}
				catch (Exception e) {
					xml = ResponseSender.createResponseBoolean(false);
					e.printStackTrace();
				}
				break;
			}
			case "remove": {
				int idSerie=Integer.parseInt(checkParameter("id", resp, req, false));
				String rimuoviEpisodi = checkParameter("removeEp", resp, req, true);
				boolean rem = rimuoviEpisodi==null||rimuoviEpisodi.compareTo("false")==0?false:true;
				boolean r = manager.rimuoviSeriePreferita(idSerie, rem);
				xml = ResponseSender.createResponseBoolean(r);
				break;
			}
			case "updateListSeries":{
				boolean res = manager.aggiornaListeSerie();
				xml = ResponseSender.createResponseBoolean(res);
				break;
			}
			case "updateTorrents": {
				int idSerie = Integer.parseInt(checkParameter("serie", resp, req, false));
				int idProvider = Integer.parseInt(checkParameter("provider", resp, req, false));
				try {
					manager.aggiornaEpisodiSerie(idSerie, idProvider);
					xml = ResponseSender.createResponseBoolean(true);
				}
				catch(Exception e){
					xml = ResponseSender.createResponseBoolean(false);
				}
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
