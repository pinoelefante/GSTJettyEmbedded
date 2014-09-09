package gst.sottotitoli.italiansubs;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ItasaAPI {
	private String AUTHCODE;
	private String APIKEY;
	
	private String API_SHOWLIST="https://api.italiansubs.net/api/rest/shows?apikey="+APIKEY;
	private String API_SUB_GETID = "https://api.italiansubs.net/api/rest/subtitles/search?q=<QUERY>&show_id=<SHOW_ID>&version=<VERSIONE>&apikey="+APIKEY;
	private String API_LOGIN="https://api.italiansubs.net/api/rest/users/login?username=<USERNAME>&password=<PASSWORD>&apikey="+APIKEY;
	
	private CloseableHttpClient httpclient;
	private BasicCookieStore cookieStore;
	
	public ItasaAPI(){
		cookieStore = new BasicCookieStore();
        httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();
	}
	
	public boolean login(String username, String password){
		String auth=verificaLogin(username, password);
		if(auth!=null){
			try {
				loginWeb(username,password);
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		else
			return false;
	}
	private void loginWeb(String username, String password) throws Exception{
		HttpGet httpget = new HttpGet("http://www.italiansubs.net/index.php");
		CloseableHttpResponse response1 = httpclient.execute(httpget);
        try {
            HttpEntity entity = response1.getEntity();

            System.out.println("Login form get: " + response1.getStatusLine());
            EntityUtils.consume(entity);
            

            System.out.println("Initial set of cookies:");
            List<Cookie> cookies = cookieStore.getCookies();
            if (cookies.isEmpty()) {
                System.out.println("None");
            }
            else {
                for (int i = 0; i < cookies.size(); i++) {
                    System.out.println("- " + cookies.get(i).toString());
                }
            }
        } 
        finally {
            response1.close();
        }
	}
	public String verificaLogin(String username, String password){
		String url_login=API_LOGIN.replace("<USERNAME>", username).replace("<PASSWORD>", password);
		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder domparser = null;
		try {
			domparser = dbfactory.newDocumentBuilder();
			Document doc = domparser.parse(url_login);
			
			NodeList elenco_shows=doc.getElementsByTagName("authcode");
			if(elenco_shows.getLength()>0)
				return ((Element)elenco_shows.item(0)).getTextContent();
		} 
		catch (SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
