package gst.serieTV;

import gst.programma.ManagerException;
import gst.programma.Settings;

import java.util.ArrayList;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Node;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Karmorra extends ProviderSerieTV {

	@Override
	public String getProviderName() {
		return "Karmorra";
	}

	@Override
	public String getBaseURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void aggiornaElencoSerie() {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<Episodio> nuoviEpisodi(SerieTV serie) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void caricaEpisodiDB(SerieTV serie) {
		// TODO Auto-generated method stub
	}

	@Override
	public void caricaSerieDB() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void salvaSerieInDB(SerieTV s) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void salvaEpisodioInDB(Torrent t) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getProviderID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void caricaEpisodiOnline(SerieTV serie) {
		// TODO Auto-generated method stub

	}

	public Karmorra() {
		instance();
	}

	private WebClient webClient;

	private void instance() {
		if (webClient == null) {
			webClient = new WebClient(new BrowserVersion("GestioneSerieTV", "", "GestioneSerieTV_" + Settings.getVersioneSoftware(), (float) Settings.getVersioneSoftware()));
			webClient.setActiveXNative(false);
			webClient.setAppletEnabled(false);
			webClient.setCssEnabled(false);
			webClient.setJavaScriptEnabled(false);
		}
	}

	public void browserToShow(int id) {
		String url = "http://showrss.karmorra.info/?cs=browse&show=" + id + "&magnets=1";
		try {
			HtmlPage pagina_serie = (HtmlPage) webClient.getPage(url);
			List<HtmlAnchor> links = pagina_serie.getAnchors();
			for (int i = 0; i < links.size(); i++) {
				HtmlAnchor link = links.get(i);
				String link_s = link.getAttribute("href");
				if (link_s.toLowerCase().startsWith("magnet:")) {
					// TODO salvare il link
					System.out.println(link_s);
				}
			}
		}
		catch (FailingHttpStatusCodeException | IOException e) {
			e.printStackTrace();
			ManagerException.registraEccezione(e);
		}
	}

	public void caricaElencoSerie() {
		String url = "http://showrss.karmorra.info/?cs=browse";
		try {
			HtmlPage pagina_elenco = webClient.getPage(url);
			HtmlElement elenco_serie = pagina_elenco.getHtmlElementById("browse_show");
			Iterator<HtmlElement> opzioni = elenco_serie.getChildElements().iterator();
			while (opzioni.hasNext()) {
				HtmlElement el = opzioni.next();
				String id = el.getAttribute("value");
				String nome_serie = ((Node) el).getTextContent();
				System.out.println(nome_serie + " " + id);
				// TODO aggiungere lo show all'elenco
			}
			pagina_elenco.cleanUp();
		}
		catch (FailingHttpStatusCodeException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Karmorra k = new Karmorra();
		k.browserToShow(2);
	}
	/*
	 * private void logga(){ instance(); try {
	 * webClient.getCookieManager().clearCookies(); HtmlPage page_login =
	 * (HtmlPage) webClient.getPage("http://showrss.karmorra.info/?cs=login");
	 * HtmlForm form = page_login.getForms().get(0); HtmlSubmitInput
	 * button_submit = (HtmlSubmitInput) form.getInputByValue("Log in");
	 * HtmlTextInput textField_username = (HtmlTextInput)
	 * form.getInputByName("username"); HtmlPasswordInput textField_password =
	 * (HtmlPasswordInput) form.getInputByName("password");
	 * textField_username.setValueAttribute("gst_username");//TODO inserire qui
	 * username
	 * textField_password.setValueAttribute("gestioneserietvpass");//TODO
	 * inserire qui password button_submit.click();
	 * webClient.getPage("http://showrss.karmorra.info/?cs=browse&magnets=1");
	 * webClient.closeAllWindows(); webClient.getCache().clear();
	 * page_login.cleanUp(); } catch (FailingHttpStatusCodeException |
	 * IOException e) { e.printStackTrace();
	 * ManagerException.registraEccezione(e); } 
	 * }
	 */

	@Override
	protected boolean rimuoviSerieDaDB(SerieTV serie) {
		// TODO Auto-generated method stub
		return false;
	}
}
