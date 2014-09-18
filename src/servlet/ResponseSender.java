package servlet;

import gst.serieTV.Episodio;
import gst.serieTV.ProviderSerieTV;
import gst.serieTV.SerieTV;
import gst.sottotitoli.ProviderSottotitoli;
import gst.sottotitoli.SerieSub;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class ResponseSender {
	public static void sendResponse(HttpServletResponse response, Document doc){
		XMLOutputter xml_out = new XMLOutputter();
		xml_out.setFormat(Format.getPrettyFormat());
		response.setContentType("text/xml");
		response.setHeader("Cache-Control",	"no-store, no-cache, must-revalidate");
		PrintWriter out;
		try {
			out = response.getWriter();
			xml_out.output(doc, out);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static Document createResponseBoolean(boolean b){
		Element root = new Element("response");
		Element valore = new Element("booleanResponse");
		valore.addContent(b+"");
		root.addContent(valore);
		Document doc = new Document(root);
		return doc;
	}
	public static Document createResponseProviders(ArrayList<ProviderSerieTV> p){
		Element root = new Element("response");
		Element providers = new Element("providers");
		root.addContent(providers);
		for(int i=0;i<p.size();i++){
			Element provider = new Element("provider");
			Element nome = new Element("name");
			nome.addContent(p.get(i).getProviderName());
			Element id = new Element("id");
			id.addContent(p.get(i).getProviderID()+"");
			provider.addContent(nome);
			provider.addContent(id);
			providers.addContent(provider);
		}
		Document doc = new Document(root);
		return doc;
	}
	public static Document createResponseSerie(ArrayList<SerieTV> serie){
		if(serie == null)
			return createResponseBoolean(false);
		Element root = new Element("response");
		Element ok = new Element("booleanResponse");
		ok.addContent(true+"");
		root.addContent(ok);
		
		Element elenco=new Element("listSerie");
		root.addContent(elenco);
		for(int i=0;i<serie.size();i++){
			Element serie_tag=new Element("serie");
			Element nome = new Element("name");
			Element provider_name = new Element("provider_name");
			nome.addContent(serie.get(i).getNomeSerie());
			Element id = new Element("id");
			id.addContent(serie.get(i).getIDDb()+"");
			Element provider = new Element("provider");
			provider.addContent(serie.get(i).getProviderID()+"");
			provider_name.addContent(ProviderSerieTV.getProviderNameByID(serie.get(i).getProviderID()));
			serie_tag.addContent(nome);
			serie_tag.addContent(id);
			serie_tag.addContent(provider);
			serie_tag.addContent(provider_name);
			elenco.addContent(serie_tag);
		}
		Document doc=new Document(root);
		return doc;
	}
	public static Document createResponseEpisodi(int idSerie, ArrayList<Episodio> episodi) {
		Element root = new Element("response");
		Element serie = new Element("serie");
		Element id_serie = new Element("id_serie");
		id_serie.addContent(""+idSerie);
		serie.addContent(id_serie);
		Element episodiList = new Element("elenco_episodi");
		serie.addContent(episodiList);
		for(int i=0;episodi!=null && i<episodi.size();i++){
			Episodio ep = episodi.get(i);
			Element episodio = new Element("ep");
			Element stagione=new Element("stagione");
			stagione.addContent(ep.getStagione()+"");
			episodio.addContent(stagione);
			Element episodioP = new Element("episodio");
			episodioP.addContent(""+ep.getEpisodio());
			episodio.addContent(episodioP);
			Element idEpisodio = new Element("id_episodio");
			idEpisodio.addContent(""+ep.getId());
			episodio.addContent(idEpisodio);
			Element stato_visualizzazione = new Element("stato");
			stato_visualizzazione.addContent(""+ep.getStatoVisualizzazione());
			episodio.addContent(stato_visualizzazione);
			//Element links = new Element("links");
			episodiList.addContent(episodio);
		}
		root.addContent(serie);
		Document doc = new Document(root);
		return doc;
	}
	public static Document createResponseDownload(int idEpisodio, boolean d) {
		Element root = new Element("response");
		Element valore = new Element("booleanResponse");
		valore.addContent(d+"");
		Element episodio = new Element("episodio");
		episodio.addContent(""+idEpisodio);
		root.addContent(valore);
		root.addContent(episodio);
		Document doc = new Document(root);
		return doc;
	}
	public static Document createProviderSottotitoli(Map<ProviderSottotitoli, ArrayList<SerieSub>> map) {
		Element root = new Element("response");
		Element ok = new Element("booleanResponse");
		ok.addContent(true+"");
		root.addContent(ok);
		
		Set<Entry<ProviderSottotitoli, ArrayList<SerieSub>>> list=map.entrySet();
		for(Entry<ProviderSottotitoli, ArrayList<SerieSub>> provider : list){
			ProviderSottotitoli p = provider.getKey();
			ArrayList<SerieSub> elenco = provider.getValue();
			
			Element e_provider = new Element("provider");
			e_provider=e_provider.setAttribute("nome", p.getProviderName());
			root.addContent(e_provider);
			Element e_elenco = new Element("series");
			e_provider.addContent(e_elenco);
			for(int i=0;i<elenco.size();i++){
				Element e_serie = new Element("serie");
				e_elenco.addContent(e_serie);
				Element e_nome = new Element("nome");
				e_nome.addContent(elenco.get(i).getNomeSerie());
				e_serie.addContent(e_nome);
				Element e_id=new Element("id");
				e_id.addContent(elenco.get(i).getIDDB()+"");
				e_serie.addContent(e_id);
			}
 		}
		return new Document(root);
	}
}
