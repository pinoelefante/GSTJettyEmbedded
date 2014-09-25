package servlet;

import gst.infoManager.thetvdb.ActorTVDB;
import gst.infoManager.thetvdb.SerieTVDB;
import gst.infoManager.thetvdb.SerieTVDBFull;
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

import util.Object3Value;

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
	public static Document createResponseInteger(int n){
		Element root = new Element("response");
		Element valore = new Element("booleanResponse");
		valore.addContent(true+"");
		root.addContent(valore);
		Element integer = new Element("Integer");
		integer.addContent(n+"");
		root.addContent(integer);
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
			Element id_itasa = new Element("id_itasa");
			Element id_subspedia = new Element("id_subspedia");
			Element id_subsfactory = new Element("id_subsfactory");
			Element id_tvdb = new Element("id_tvdb");
			nome.addContent(serie.get(i).getNomeSerie());
			Element id = new Element("id");
			id.addContent(serie.get(i).getIDDb()+"");
			Element provider = new Element("provider");
			provider.addContent(serie.get(i).getProviderID()+"");
			provider_name.addContent(ProviderSerieTV.getProviderNameByID(serie.get(i).getProviderID()));
			id_itasa.addContent(serie.get(i).getIDItasa()+"");
			id_subsfactory.addContent(""+serie.get(i).getIDDBSubsfactory());
			id_subspedia.addContent(serie.get(i).getIDSubspedia()+"");
			id_tvdb.addContent(serie.get(i).getIDTvdb()+"");
			serie_tag.addContent(nome);
			serie_tag.addContent(id);
			serie_tag.addContent(provider);
			serie_tag.addContent(provider_name);
			serie_tag.addContent(id_itasa);
			serie_tag.addContent(id_subsfactory);
			serie_tag.addContent(id_subspedia);
			serie_tag.addContent(id_tvdb);
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
			e_provider=e_provider.setAttribute("id_provider", p.getProviderID()+"");
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
	public static Document createResponseSubDownload(Map<SerieTV, ArrayList<Episodio>> map) {
		Element root = new Element("response");
		Element ok = new Element("booleanResponse");
		ok.addContent(true+"");
		root.addContent(ok);
		
		for(Entry<SerieTV, ArrayList<Episodio>> entry: map.entrySet()){
			SerieTV s = entry.getKey();
			Element serie = new Element("serie");
			Element nome=new Element("nome");
			nome.addContent(s.getNomeSerie());
			Element id_s = new Element("id_serie");
			id_s.addContent(s.getIDDb()+"");
			Element episodi = new Element("episodi");
			serie.addContent(nome);
			serie.addContent(id_s);
			serie.addContent(episodi);
			ArrayList<Episodio> eps = entry.getValue();
			for(int i=0;i<eps.size();i++){
				Episodio ep = eps.get(i);
				Element episodio = new Element("episodio");
				Element id_e = new Element("id_episodio");
				id_e.addContent(""+ep.getId());
				Element stagione = new Element("season");
				stagione.addContent(""+ep.getStagione());
				Element episode = new Element("episode");
				episode.addContent(""+ep.getEpisodio());
				episodio.addContent(id_e);
				episodio.addContent(stagione);
				episodio.addContent(episode);
				episodi.addContent(episodio);
			}
			root.addContent(serie);
		}
		
		return new Document(root);
	}
	public static Document createResponseEpisodiVedere(ArrayList<Entry<SerieTV, ArrayList<Episodio>>> map) {
		Element root = new Element("response");
		Element ok = new Element("booleanResponse");
		ok.addContent(true+"");
		root.addContent(ok);
		
		for(Entry<SerieTV, ArrayList<Episodio>> entry : map){
			SerieTV st = entry.getKey();
			ArrayList<Episodio> eps = entry.getValue();
			for(int i=0;i<eps.size();i++){
				Episodio ep = eps.get(i);
				Element episodio = new Element("episodio");
				Element titolo=new Element("titolo");
				Element id=new Element("id");
				titolo.addContent(st.getNomeSerie()+" "+ep.getStagione()+"x"+(ep.getEpisodio()<10?"0"+ep.getEpisodio():ep.getEpisodio()));
				id.addContent(""+ep.getId());
				episodio.addContent(titolo);
				episodio.addContent(id);
				root.addContent(episodio);
			}
		}
		return new Document(root);
	}
	public static Document createResponseInfoSerie(SerieTVDBFull serie) {
		Element root = new Element("response");
		Element ok = new Element("booleanResponse");
		ok.addContent(true+"");
		root.addContent(ok);
		Element id_serie = new Element("id_serie");
		id_serie.addContent(serie.getId()+"");
		root.addContent(id_serie);
		Element nome_serie = new Element("nome_serie");
		nome_serie.addContent(serie.getNomeSerie());
		root.addContent(nome_serie);
		Element first_air = new Element("first_air");
		first_air.addContent(serie.getDataInizioITA());
		root.addContent(first_air);
		Element rating = new Element("rating");
		rating.addContent(serie.getRating()+"");
		root.addContent(rating);
		Element network = new Element("network");
		network.addContent(serie.getNetwork());
		root.addContent(network);
		Element air_day = new Element("air_day");
		air_day.addContent(serie.getGiornoSettimana());
		root.addContent(air_day);
		Element air_hour = new Element("air_hour");
		air_hour.addContent(serie.getOraTrasmissione());
		root.addContent(air_hour);
		Element durata = new Element("durata_episodi");
		durata.addContent(serie.getDurataEpisodi()+"");
		root.addContent(durata);
		Element stato_serie = new Element("stato_serie");
		stato_serie.addContent(serie.getStatoSerie());
		root.addContent(stato_serie);
		Element banner_url=new Element("banner_url");
		banner_url.addContent(serie.getUrlBanner());
		root.addContent(banner_url);
		Element descrizione = new Element("descrizione");
		descrizione.addContent(serie.getDescrizione());
		root.addContent(descrizione);
	
		Element generi = new Element("generi");
		for(String genere: serie.getGeneri()){
			Element g = new Element("genere");
			g.addContent(genere);
			generi.addContent(g);
		}
		root.addContent(generi);
		Element attori = new Element("attori");
		ArrayList<ActorTVDB> elenco_attori = serie.getAttori().size()>0?serie.getAttori():serie.getAttoriString();
		for(ActorTVDB a:elenco_attori){
			Element attore = new Element("attore");
			Element nome = new Element("nome_attore");
			nome.addContent(a.getNome());
			Element ruolo = new Element("ruolo_attore");
			ruolo.addContent(a.getRuolo());
			Element img_attore=new Element("img_attore");
			img_attore.addContent(a.getUrlImage());
			attore.addContent(nome);
			attore.addContent(ruolo);
			attore.addContent(img_attore);
			attori.addContent(attore);
		}
		root.addContent(attori);
		Element poster_url = new Element("posters");
		for(String p: serie.getPoster()){
			Element poster = new Element("poster");
			poster.addContent(p);
			poster_url.addContent(poster);
		}
		root.addContent(poster_url);
		Element banners = new Element("banners");
		for(String b : serie.getBanners()){
			Element banner = new Element("banner");
			banner.addContent(b);
			banners.addContent(banner);
		}
		root.addContent(banners);
		
		return new Document(root);
	}
	public static Document createResponseTVDBList(ArrayList<SerieTVDB> list) {
		Element root = new Element("response");
		Element ok = new Element("booleanResponse");
		ok.addContent(true+"");
		root.addContent(ok);
		Element series = new Element("series");
		root.addContent(series);
		for(int i=0;i<list.size();i++){
			SerieTVDB serie = list.get(i);
			Element s = new Element("serie");
			Element id_serie = new Element("id_serie");
			id_serie.addContent(serie.getId()+"");
			s.addContent(id_serie);
			Element nome_serie = new Element("nome_serie");
			nome_serie.addContent(serie.getNomeSerie());
			s.addContent(nome_serie);
			Element first_air = new Element("first_air");
			first_air.addContent(serie.getDataInizioITA());
			s.addContent(first_air);
			Element anno=new Element("anno_inizio");
			anno.addContent(""+serie.getAnnoInizio());
			s.addContent(anno);
			Element banner_url=new Element("banner_url");
			banner_url.addContent(serie.getUrlBanner());
			s.addContent(banner_url);
			Element descrizione = new Element("descrizione");
			descrizione.addContent(serie.getDescrizione());
			s.addContent(descrizione);
			series.addContent(s);
		}
		
		return new Document(root);
	}
	public static Document createResponseLogSub(ArrayList<Object3Value<ProviderSottotitoli, SerieTV, Episodio>> list) {
		Element root = new Element("response");
		Element ok = new Element("booleanResponse");
		ok.addContent(true+"");
		root.addContent(ok);
		Element subs = new Element("subs");
		for(Object3Value<ProviderSottotitoli, SerieTV, Episodio> s : list){
			Element sub = new Element("sub");
			Element nomeSerie = new Element("nomeSerie");
			nomeSerie.addContent(s.getV().getNomeSerie());
			Element id_episodio = new Element("id_episodio");
			id_episodio.addContent(s.getT().getId()+"");
			Element stagione = new Element("stagione");
			stagione.addContent(s.getT().getStagione()+"");
			Element episodio = new Element("episodio");
			episodio.addContent(s.getT().getEpisodio()+"");
			Element provider = new Element("provider");
			provider.addContent(s.getK().getProviderName());
			sub.addContent(nomeSerie);
			sub.addContent(id_episodio);
			sub.addContent(stagione);
			sub.addContent(episodio);
			sub.addContent(provider);
			subs.addContent(sub);
		}
		root.addContent(subs);
		return new Document(root);
	}
}
