package servlet.v15;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;

import gst.serieTV.SerieTV;
import gst.serieTV.SerieTVController;

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
		Document xml = null;
		switch(action)
		{
			case "elencoSerie":
			{
				List<SerieTV> listSerie = SerieTVController.getInstance().getElencoSerieTV();
				System.out.println("Serie Trovate: "+listSerie.size());
				xml = ResponseSender.createCollectionXml(listSerie);
				break;
			}
		}
		ResponseSender.sendResponse(resp, xml);
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		
	}
}
