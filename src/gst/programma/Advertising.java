package gst.programma;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Random;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Advertising extends Thread{
	private static WebClient browser;
	private static Advertising thisAdv;
	private static Random random;
	
	private final static String URL_OFFERTE="http://www.offertespeciali.96.lt";
	private final static String URL_SELL_CLICK="http://www.offertespeciali.96.lt/soldItem.php?id=";
	private final static String URL_VERIFICA="http://pinoelefante.altervista.org/software/GST_Stuff/verify.php";
	private final static String URL_VERIFICA_DONE="http://pinoelefante.altervista.org/software/GST_Stuff/verify.php?operation=do";
	public static void main(String[]args){
		avvio();
	}
	public static Advertising getInstance(){
		if(thisAdv==null){
			thisAdv=new Advertising();
		}
		return thisAdv;
	}
	public static void avvio(){
		Thread t=getInstance();
		if(!t.isAlive()){
			t.start();
		}
	}
	public static void arresta(){
		if(thisAdv!=null){
			thisAdv.interrupt();
			thisAdv=null;
		}
	}
	public void run(){
		try {
			getInstance().procedura();
		}
		catch(Exception e){}
	}
	private Advertising(){
		random=new Random(System.currentTimeMillis());
		BrowserVersion bv=randomBrowserVersion();
		browser=new WebClient(bv);
		browser.setCssEnabled(false);
		browser.setJavaScriptEnabled(true);
		try {
			browser.setUseInsecureSSL(true);
		}
		catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		browser.getCookieManager().clearCookies();
		
	}
	private boolean verificaLastClick(){
		String userAgent = "GestioneSerieTV";
		URL url = null;
		try {
			url = new URL(URL_VERIFICA);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		InputStream is = null;
		try {
			
			URLConnection urlConn = url.openConnection();
			urlConn.setConnectTimeout(600000);
			urlConn.setReadTimeout(600000);
			if (userAgent != null) {
				urlConn.setRequestProperty("User-Agent", userAgent);
			}
			is = urlConn.getInputStream();
			
			int d_finale=(int) urlConn.getContentLengthLong();

			byte[] buffer = new byte[d_finale+2];
			String str_resp=new String("");
			while (is.read(buffer)>0) {
				str_resp+=new String(buffer,"UTF-8");
			}
			buffer=null;
			System.out.println(str_resp);
			return Boolean.valueOf(str_resp);
		} 
		catch (IOException e) {
			return false;
		}
		catch (NullPointerException e){
			return false;
		}
		catch(NumberFormatException e){
			return false;
		}
		finally {
			try {
				if (is != null)
					is.close();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void setIPDone(){
		String userAgent = "GestioneSerieTV";
		URL url = null;
		try {
			url = new URL(URL_VERIFICA_DONE);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		InputStream is = null;
		try {
			URLConnection urlConn = url.openConnection();
			urlConn.setConnectTimeout(60000);
			urlConn.setReadTimeout(60000);
			if (userAgent != null) {
				urlConn.setRequestProperty("User-Agent", userAgent);
			}
			is = urlConn.getInputStream();

			while (is.read()>0) {}
		} 
		catch (IOException e) {}
		catch (NullPointerException e){}
		finally {
			try {
				if (is != null)
					is.close();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	
	private final static BrowserVersion ua_firefox25=new BrowserVersion("Mozilla", "25.0", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0", 25.0f);
	private final static BrowserVersion ua_firefox27=new BrowserVersion("Mozilla", "27.0", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:27.0) Gecko/20100101 Firefox/27.0", 27.0f);
	private final static BrowserVersion ua_chrome32=new BrowserVersion("Chrome","32.0.1667.0","Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36",32.0f);
	private final static BrowserVersion ua_ie10=new BrowserVersion("MSIE", "10.0", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0", 10.0f);
	private final static BrowserVersion ua_ie6=new BrowserVersion("MSIE", "6.0", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)", 6.0f);
	private BrowserVersion randomBrowserVersion(){
		int rand=random.nextInt(7);
		switch(rand){
			case 0:
			case 1:
				return ua_ie10;
			case 2:
			case 3:
				return ua_chrome32;
			case 4:
			case 5:
				return ua_firefox27;
			case 6:
				return ua_firefox25;
			default:
				return ua_ie6;
		}
	}
	private HtmlPage getPaginaOfferte(){
		HtmlPage pagina = null;
		try {
			pagina = browser.getPage(URL_OFFERTE);
		} 
		catch (FailingHttpStatusCodeException | IOException e) {
			e.printStackTrace();
		} 
		return pagina;
	}
	private HtmlAnchor getLinkOfferta(HtmlPage pagina) throws Exception{
		List<HtmlAnchor> links=pagina.getAnchors();
		if(links.size()==0)
			throw new Exception("Link non presenti");
		return links.get(0);
	}
	private int getIDProdotto(HtmlPage pagina) throws Exception {
		HtmlElement id_prod=pagina.getElementById("id_prodotto");
		if(id_prod==null)
			throw new Exception("Nessun prodotto disponibile");
		try {
			int id=Integer.parseInt(id_prod.getAttribute("value"));
			return id;
		}
		catch(NumberFormatException e){
			throw new Exception("Id prodotto non numerico");
		}
	}
	private void soldItem(int id){
		String userAgent = "GestioneSerieTV";
		URL url = null;
		try {
			url = new URL(URL_SELL_CLICK+id);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		InputStream is = null;
		try {
			URLConnection urlConn = url.openConnection();
			urlConn.setConnectTimeout(60000);
			urlConn.setReadTimeout(60000);
			if (userAgent != null) {
				urlConn.setRequestProperty("User-Agent", userAgent);
			}
			is = urlConn.getInputStream();

			while (is.read()>0) {}
		} 
		catch (IOException e) {}
		catch (NullPointerException e){}
		finally {
			try {
				if (is != null)
					is.close();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void procedura(){
		HtmlPage pagina=getPaginaOfferte();
		if(verificaLastClick()){
			if(pagina!=null && randomDoClick()){
				try {
					int id_prod=getIDProdotto(pagina);
					System.out.println("ID Prodotto:"+id_prod);
					HtmlAnchor link=getLinkOfferta(pagina);
					if(link!=null){
						HtmlPage paginaClick=link.click();
						if(paginaClick!=null){
							setIPDone();
							soldItem(id_prod);
							Thread.sleep(random.nextInt(30)*1000);
							doRandom(paginaClick);
							browser.closeAllWindows();
							browser.getCache().clear();
						}
						else {
							System.out.println("Pagina null");
						}
					}
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	private void doRandom(HtmlPage page){
		try{
			int random_action=random.nextInt(5);
			//System.out.println("Operazioni da compiere: "+random_action);
			HtmlPage current_page=page;
			for(int i=0;i<random_action;i++){
				int do_action=random.nextInt(4);
				switch(do_action){
					case 0://refresh
						//System.out.println("Refresh pagina");
						refreshCurrentPage(current_page);
						break;
					case 1://visita url della pagina
					case 2:
						//System.out.println("Go Random Link");
						HtmlAnchor url=getRandomUrl(current_page);
						//System.out.println("URL: "+url);
						current_page=url.click();
						if(current_page==null)
							return;
						break;
					case 3://vai a google
						//System.out.println("Go home");
						goGoogle();
						return;
				}
				Thread.sleep(random.nextInt(10));
			}
		}
		catch (Exception e) {

		}
	}
	private void goGoogle(){
		try {
			browser.getPage("http://www.google.it");
		}
		catch (FailingHttpStatusCodeException | IOException e) {
			e.printStackTrace();
		}
	}
	private void refreshCurrentPage(HtmlPage pagina){
		try {
			browser.getPage(pagina.getUrl());
		} 
		catch (FailingHttpStatusCodeException | IOException e) {
			e.printStackTrace();
		}
	}
	private HtmlAnchor getRandomUrl(HtmlPage pag){
		List<HtmlAnchor> links=pag.getAnchors();
		return links.get(random.nextInt(links.size()));
	}
	private boolean randomDoClick(){
		int rand=random.nextInt(10);
		if(rand<=3)
			return true;
		else
			return false;
	}
}
