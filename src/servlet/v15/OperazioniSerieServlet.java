package servlet.v15;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import gst.serietv.EpisodeWrapper;
import gst.serietv.SerieTV;
import gst.serietv.Torrent;
import gst.serietv.VideoProviderController;

public class OperazioniSerieServlet extends HttpServlet
{
	private static final long serialVersionUID = -6853024448104090242L;
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String action = req.getParameter("action");
		if(action == null || action.isEmpty()){
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "action not sended");
			return;
		}
		JSONObject jsonContent = null;
		switch(action)
		{
			case "aggiungiFavorita":
			{
				int id = Integer.parseInt(req.getParameter("id"));
				jsonContent = changeFavourite(id, true);
				break;
			}
			case "rimuoviFavorita":
			{
				int id = Integer.parseInt(req.getParameter("id"));
				jsonContent = changeFavourite(id, false);
				break;
			}
			case "aggiornaElencoSerie":
			{
				List<SerieTV> list = VideoProviderController.getInstance().aggiornaSerie();
				jsonContent = ResponseSender.createCollectionJson(list);
				break;
			}
			case "elencoSerie":
			{
				List<SerieTV> listSerie = VideoProviderController.getInstance().getElencoSerieTV();
				jsonContent = ResponseSender.createCollectionJson(listSerie);
				break;
			}
			case "elencoSerieFavorite":
			{
				List<SerieTV> listSerie = VideoProviderController.getInstance().getFavouriteList();
				jsonContent = ResponseSender.createCollectionJson(listSerie);
				break;
			}
			case "rinominaSerie":
			{
				int id = Integer.parseInt(req.getParameter("id"));
				String newName = req.getParameter("name");
				boolean r = VideoProviderController.getInstance().rename(id, newName);
				jsonContent = ResponseSender.createBooleanJson(r);
				break;
			}
			case "unisciSerie":
			{
				int id1 = Integer.parseInt(req.getParameter("id1"));
				int id2 = Integer.parseInt(req.getParameter("id2"));
				boolean r = VideoProviderController.getInstance().associateComposers(id1, id2);
				jsonContent = ResponseSender.createBooleanJson(r);
				break;
			}
			case "aggiornaElencoEpisodi":
			{
				Map<SerieTV, Set<EpisodeWrapper>> map = VideoProviderController.getInstance().aggiornaEpisodi();
				jsonContent = ResponseSender.createMapJson(map);
				break;
			}
			case "aggiornaElencoEpisodiSerie":
			{
				int serie = Integer.parseInt(req.getParameter("id"));
				Map<SerieTV, Set<EpisodeWrapper>> episodi = VideoProviderController.getInstance().aggiornaEpisodi(serie);
				jsonContent = ResponseSender.createMapJson(episodi);
				break;
			}
			case "elencoEpisodi":
			{
				Map<SerieTV, Set<EpisodeWrapper>> serie = VideoProviderController.getInstance().getElencoEpisodi();
				jsonContent = ResponseSender.createMapJson(serie);
				break;
			}
			case "elencoEpisodiSerie":
			{
				int serie = Integer.parseInt(req.getParameter("id"));
				Set<EpisodeWrapper> episodi = VideoProviderController.getInstance().getElencoEpisodi(serie);
				jsonContent = ResponseSender.createCollectionJson(episodi);
				break;
			}
			case "getLinks":
			{
				int showId = Integer.parseInt(req.getParameter("id"));
				int season = Integer.parseInt(req.getParameter("stagione"));
				int episode = Integer.parseInt(req.getParameter("episodio"));
				Set<Torrent> torrents = VideoProviderController.getInstance().getTorrentsForEpisode(showId, season, episode);
				jsonContent = ResponseSender.createCollectionJson(torrents);
				break;
			}
			case "impostaRisoluzione":
			{
				int showId = Integer.parseInt(req.getParameter("id"));
				int risoluzione = Integer.parseInt(req.getParameter("risoluzione"));
				boolean r = VideoProviderController.getInstance().changeFavouriteResolution(showId, risoluzione);
				jsonContent = ResponseSender.createBooleanJson(r);
				break;
			}
			case "download":
			{
				int showId = Integer.parseInt(req.getParameter("id"));
				int season = Integer.parseInt(req.getParameter("stagione"));
				int episode = Integer.parseInt(req.getParameter("episodio"));
				Torrent t = VideoProviderController.getInstance().downloadEpisode(showId, season, episode);
				jsonContent = ResponseSender.createItemJson(t);
				break;
			}
		}
		ResponseSender.sendJSONResponse(resp, jsonContent);
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		
	}
	
	private JSONObject changeFavourite(int serieId, boolean status)
	{
		boolean b = VideoProviderController.getInstance().setFavourite(serieId, status);
		return ResponseSender.createBooleanJson(b);
	}
}
