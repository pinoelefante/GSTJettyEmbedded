package gst.sottotitoli.italiansubs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ItasaAPI {
	private String AUTHCODE="";
	private String APIKEY="87c9d52fba19ba856a883b1d3ddb14dd";
	
	private String API_SHOWLIST="https://api.italiansubs.net/api/rest/shows?apikey="+APIKEY;
	private String API_SUB_GETID = "https://api.italiansubs.net/api/rest/subtitles/search?q=<QUERY>&show_id=<SHOW_ID>&version=<VERSIONE>&apikey="+APIKEY;
	private String API_LOGIN="https://api.italiansubs.net/api/rest/users/login?username=<USERNAME>&password=<PASSWORD>&apikey="+APIKEY;
	private String API_DOWNLOAD="https://api.italiansubs.net/api/rest/subtitles/download?subtitle_id=<ID_SUB>&authcode=<AUTHCODE>&apikey="+APIKEY;
	
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
			AUTHCODE=auth;
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
		List<NameValuePair> parametri=null;
		CloseableHttpResponse response1 = httpclient.execute(httpget);
        try {
            HttpEntity entity = response1.getEntity();

            System.out.println("Login form get: " + response1.getStatusLine());
            
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
            parametri = getParameters(EntityUtils.toString(entity));
            parametri.add(new BasicNameValuePair("username",username));
            parametri.add(new BasicNameValuePair("passwd",password));
        } 
        finally {
            response1.close();
        }
        
        RequestBuilder rq=RequestBuilder.post();
        for(int i=0;parametri!=null && i<parametri.size();i++){
        	rq.addParameter(parametri.get(i));
        }
        
        HttpUriRequest login = rq.setUri("http://www.italiansubs.net/index.php").build();
        CloseableHttpResponse response2 = httpclient.execute(login);
        try {
            HttpEntity entity = response2.getEntity();

            System.out.println("Login form get: " + response2.getStatusLine());
            EntityUtils.consume(entity);

            System.out.println("Post logon cookies:");
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
            response2.close();
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
	private List<NameValuePair> getParameters(String html){
		List<NameValuePair> params=new ArrayList<NameValuePair>();
		org.jsoup.nodes.Document doc=Jsoup.parse(html);
		org.jsoup.nodes.Element form=doc.getElementById("form-login");
		Elements input=form.getElementsByTag("input");
		for(int i=0;i<input.size();i++){
			org.jsoup.nodes.Element el=input.get(i);
			if(el.attr("name").compareTo("username")==0 || el.attr("name").compareTo("passwd")==0)
				continue;
			params.add(new BasicNameValuePair(el.attr("name"), el.attr("value")));
		}
		return params;
	}
	public boolean download(int idSub, String folder) throws Exception{
		String url=API_DOWNLOAD.replace("<ID_SUB>", ""+idSub).replace("<AUTHCODE>", AUTHCODE);
		HttpGet req=new HttpGet(url);
		CloseableHttpResponse response=httpclient.execute(req);
		HttpEntity entity = response.getEntity();
		InputStream content=entity.getContent();
		new File(folder).mkdirs();
		FileOutputStream fos=new FileOutputStream(folder+File.separator+idSub+".zip");
		byte[] buff=new byte[1024];
		int read=0;
		while((read=content.read(buff))>0){
			fos.write(buff,0, read);
		}
		content.close();
		fos.close();
		return true;
	}
	public static void main(String[] args){
		ItasaAPI api=new ItasaAPI();
		api.login("pinoelefante", "elefante");
		try {
			api.download(20738,"C:\\download");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
