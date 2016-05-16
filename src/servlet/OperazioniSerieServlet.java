package servlet;

import gst.gui.InterfacciaGrafica;
import gst.programma.Settings;
import gst.serieTV.Episodio;
import gst.serieTV.GestioneSerieTV;
import gst.serieTV.ProviderSerieTV;
import gst.serieTV.SerieTV;
import gst.serieTV.ShowRSS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;

import util.os.DirectoryManager;
import util.os.DirectoryNotAvailableException;

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
			case "deleteFolder": {
				int idSerie = Integer.parseInt(checkParameter("idSerie", resp, req, false));
				boolean r = manager.deleteFolderSerie(idSerie);
				xml = ResponseSender.createResponseBoolean(r);
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
			case "getEpisodiDaScaricare":{
				ArrayList<Entry<SerieTV, ArrayList<Episodio>>> map = manager.getEpisodiDaScaricare();
				xml = ResponseSender.createResponseEpisodiScaricare(map);
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
			case "ignora": {
				int id = Integer.parseInt(checkParameter("id", resp, req, false));
				boolean r = manager.ignoraEpisodio(id);
				xml = ResponseSender.createResponseBoolean(r);
				break;
			}
			case "modificaPreferenzeSerie": {
				int id = Integer.parseInt(checkParameter("id", resp, req, false));
				String lingue = checkParameter("lingue_sub", resp, req, false);
				int pref_down = Integer.parseInt(checkParameter("pref_down", resp, req, false));
				boolean escludi = Boolean.parseBoolean(checkParameter("escludi", resp, req, false));
				if(lingue.compareTo("null")==0)
					lingue = Settings.getInstance().getLingua();
				boolean b1=manager.setSerieNonSelezionabile(id, escludi);
				boolean b2=manager.setLingueSub(id, lingue);
				boolean b3=manager.setPreferenzeDownload(id, pref_down);
				xml = ResponseSender.createResponseBoolean(b1 && b2 && b3);
				break;
			}
			case "openFolder": {
				int id = Integer.parseInt(checkParameter("id", resp, req, false));
				SerieTV serie = ProviderSerieTV.getSerieByID(id);
				String directory;
				try {
					directory = DirectoryManager.getInstance().getFolderSerie(serie.getFolderSerie());
					boolean r = InterfacciaGrafica.getInstance().openFolder(directory);
					xml = ResponseSender.createResponseBoolean(r);
				}
				catch (DirectoryNotAvailableException e) {
					e.printStackTrace();
					xml = ResponseSender.createResponseBoolean(false);
				}
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
				boolean rem = (rimuoviEpisodi==null||rimuoviEpisodi.compareTo("false")==0)?false:true;
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
			case "associaAShowRss":{
				int idSerie = Integer.parseInt(checkParameter("serie", resp, req, false));
				int idRss = Integer.parseInt(checkParameter("showrssid", resp, req, false));
				//TODO
			}
			break;
			case "forzaAggiornamentoSerie":
				//TODO
				break;
			case "updateShowrssAssociation":{
				int isPreferiti = Integer.parseInt(checkParameter("preferiti", resp, req, false));
				ArrayList<SerieTV> listSerie = null;
				ShowRSS p_showrss = (ShowRSS)GestioneSerieTV.getInstance().checkProvider(ProviderSerieTV.PROVIDER_SHOWRSS);
				
				if(isPreferiti == 0)
					listSerie = p_showrss.getElencoSerieCompleto();
				else
					listSerie = GestioneSerieTV.getInstance().getElencoSeriePreferite();
				
				for(SerieTV serie : listSerie){
					if(serie.getProviderID() == ProviderSerieTV.PROVIDER_SHOWRSS){
						int eztv = p_showrss.associaToEztv(serie);
						if(isPreferiti > 0){
    						if(eztv > 0){
    							int oldPrefId = serie.getIDDb();
    							GestioneSerieTV.getInstance().rimuoviSeriePreferita(oldPrefId, false);
    							GestioneSerieTV.getInstance().aggiungiSerieAPreferiti(eztv);
    						}
						}
					}
				}
				xml = ResponseSender.createResponseBoolean(true);
			}
			break;
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
