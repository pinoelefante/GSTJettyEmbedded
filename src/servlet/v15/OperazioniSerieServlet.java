package servlet.v15;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import gst.serietv.Episodio;
import gst.serietv.SerieTV;
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
				VideoProviderController.getInstance().setFavourite(id, true);
				break;
			}
			case "rimuoviFavorita":
			{
				int id = Integer.parseInt(req.getParameter("id"));
				VideoProviderController.getInstance().setFavourite(id, false);
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
				VideoProviderController.getInstance().rename(id, newName);
				break;
			}
			case "unisciSerie":
			{
				int id1 = Integer.parseInt(req.getParameter("id1"));
				int id2 = Integer.parseInt(req.getParameter("id2"));
				VideoProviderController.getInstance().associateComposers(id1, id2);
				break;
			}
			case "aggiornaElencoEpisodi":
			{
				VideoProviderController.getInstance().aggiornaEpisodi();
				break;
			}
			case "aggiornaElencoEpisodiSerie":
			{
				int serie = Integer.parseInt(req.getParameter("id"));
				VideoProviderController.getInstance().aggiornaEpisodi(serie);
				break;
			}
			case "elencoEpisodi":
			{
				
				break;
			}
			case "elencoEpisodiSerie":
			{
				int serie = Integer.parseInt(req.getParameter("id"));
				Set<Episodio> episodi = VideoProviderController.getInstance().getElencoEpisodi(serie);
				jsonContent = ResponseSender.createCollectionJson(episodi);
				break;
			}
		}
		ResponseSender.sendJSONResponse(resp, jsonContent);
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		
	}
}
