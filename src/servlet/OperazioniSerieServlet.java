package servlet;

import gst.gui.InterfacciaGrafica;
import gst.programma.Settings;
import gst.serieTV.Episodio;
import gst.serieTV.GestioneSerieTV;
import gst.serieTV.ProviderSerieTV;
import gst.serieTV.SerieTV;
import gst.serieTV.ShowRSS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

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
		long init = System.currentTimeMillis();
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
				xml = manager.GetEpisodiDaVedere();
				break;
			}
			case "getEpisodiDaScaricare":{
				xml = manager.GetEpisodiDaScaricare();
				break;
			}
			case "getNumEpisodiDaScaricare":{
				int count = manager.GetNumEpisodiDaScaricare();
				xml = ResponseSender.createResponseInteger(count);
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
			case "stream":
			{
				int idEpisodio = Integer.parseInt(checkParameter("episodio", resp, req, false));
				String filename = manager.getVideoFile(idEpisodio);
				if(filename == null)
				{
					resp.sendError(404);
					return;
				}
				
				String range = req.getHeader("Range");
				System.out.println("Range="+(range==null?"null":range));
				long start = 0;
				long end = 0;
				if(range !=null)
				{
					String[] ranges = range.replace("bytes=", "").trim().split("-");
					start = Long.parseLong(ranges[0]);
					end = Long.parseLong(ranges.length>1 ? ranges[1] : "0");
					
					System.out.println(String.format("Range request: %d - %d", start, end));
				}
				
				File file_info = new File(filename);
				String file_ext = filename.substring(filename.lastIndexOf(".")+1);
				/*
				if(start==0 && end==0) // bytes=0-
				{
					System.out.println("Begin");
					resp.setStatus(200);
					resp.setHeader("Accept-Ranges", "bytes");
					resp.setHeader("Content-Type", "video/x-matroska");
					resp.setHeader("Content-Length", file_info.length()+"");
					//resp.setHeader("Content-Length", 1048576+"");
					resp.setHeader("Content-Range", String.format("bytes 0-1048575/%d", file_info.length()));
					byte[] fileContent = ReadFile(filename, 0, 1048576);
					resp.getOutputStream().write(fileContent);
					resp.getOutputStream().close();
				}
				else if(start > 0)
				{
					resp.setStatus(206);
					resp.setHeader("Accept-Ranges", "bytes");
					resp.setHeader("Content-Type", "video/x-matroska");
					resp.setHeader("Content-Length", file_info.length()+"");
					resp.setHeader("Content-Range", String.format("bytes %d-%d/883912350", start, (start + 1048575)));
					
					byte[] fileContent = ReadFile(filename, (int)start, (int)start + 1048575);
					resp.getOutputStream().write(fileContent);
				}
				/*
				else if(end > start) // es: bytes=12-1024
				{
					System.out.println("Range play");
				}
				*/
				
				//else 
				{
					System.out.println("Direct play");
					resp.setStatus(200);
					resp.setHeader("Accept-Ranges", "bytes");
					resp.setHeader("Content-Type", GetMimeType(file_ext));
					resp.setHeader("Content-Length", file_info.length()+"");
					//resp.setHeader("Content-Range", "bytes 0-1048575/883912350");
					
					byte[] buffer = new byte[1048576];
					int read = 0;
					try(FileInputStream reader = new FileInputStream(file_info))
					{
						while((read = reader.read(buffer)) > 0)
						{
							resp.getOutputStream().write(buffer, 0, read);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				
				return;
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
			case "setDefaultVideoQualityForAll":
				int quality = Integer.parseInt(checkParameter("quality", resp, req, false));
				manager.changeDefaultVideoQualityForAll(quality);
				xml = ResponseSender.createResponseBoolean(true);
				break;
		}
		long finish = System.currentTimeMillis();
		System.out.println("Tempo esecuzione "+action+" = "+ (finish-init));
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
	private String GetMimeType(String ext)
	{
		ext = ext.toLowerCase();
		switch(ext)
		{
			case "mkv":
				return "video/x-matroska";
			case "mp4":
				return "video/mp4";
		}
		return "text/html";
	}
}
