package gst.sottotitoli.italiansubs;

import gst.sottotitoli.SerieSub;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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

            /*
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
            */
            parametri = getParameters(EntityUtils.toString(entity));
            parametri.add(new BasicNameValuePair("username",username));
            parametri.add(new BasicNameValuePair("passwd",password));
            EntityUtils.consume(entity);
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

            //System.out.println("Login form get: " + response2.getStatusLine());
            EntityUtils.consume(entity);

            /*
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
            */
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
	public String download(int idSub, String folder) throws Exception {
		if(AUTHCODE.isEmpty())
			throw new Exception("Not logged in");
		
		String url=API_DOWNLOAD.replace("<ID_SUB>", ""+idSub).replace("<AUTHCODE>", AUTHCODE);
		HttpGet req=new HttpGet(url);
		CloseableHttpResponse response=httpclient.execute(req);
		HttpEntity entity=null;;
		InputStream content=null;;
		FileOutputStream fos=null;;
		try {
			entity = response.getEntity();
			content=entity.getContent();
			new File(folder).mkdirs();
			String pathDown = folder+File.separator+idSub+".zip";
			fos=new FileOutputStream(pathDown);
			byte[] buff=new byte[1024];
			int read=0;
			while((read=content.read(buff))>0){
				fos.write(buff,0, read);
			}
			return pathDown;
		}
		finally {
			if(response!=null)
				response.close();
			EntityUtils.consume(entity);
			if(content!=null)
				content.close();
			if(fos!=null)
				fos.close();
		}
	}
	public int cercaSottotitolo(int idSerie, int stagione, int episodio, String qualita){
		String query = stagione + "x"	+ (episodio < 10 ? "0" + episodio : episodio); 
		String url_query = API_SUB_GETID.replace("<QUERY>", query).replace("<VERSIONE>", qualita).replace("<SHOW_ID>", idSerie	+ "");
		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder domparser = null;
		try {
			domparser = dbfactory.newDocumentBuilder();
			Document doc = domparser.parse(url_query);
			
			NodeList countlist=doc.getElementsByTagName("count");
			if(countlist.getLength()==1){
				Element count=(Element) countlist.item(0);
				int num_sub=Integer.parseInt(count.getTextContent());
				if(num_sub>0){
					NodeList idlist=doc.getElementsByTagName("id");
					int id=0;
					for(int i=0;i<idlist.getLength();i++){
						Node value=idlist.item(i);
						if(value instanceof Element){
							Element v=(Element)value;
							int id_v=Integer.parseInt(v.getTextContent());
							if(id_v>id)
								id=id_v;
						}
					}
					return id;
				}
			}
		}
		catch(ParserConfigurationException e){}
		catch (SAXException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return 0; 
	}
	public ArrayList<SerieSub> caricaElencoSerieOnlineXML() {
		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder domparser = null;
	    ArrayList<SerieSub> serie = new ArrayList<SerieSub>();
		try {
			domparser = dbfactory.newDocumentBuilder();
			Document doc = domparser.parse(API_SHOWLIST);
			
			NodeList elenco_shows=doc.getElementsByTagName("show");
			for(int i=0;i<elenco_shows.getLength();i++){
				Node show=elenco_shows.item(i);
				NodeList show_attributi=show.getChildNodes();
				String nome="";
				int id=0;
				for(int j=0;j<show_attributi.getLength();j++){
					Node attr=show_attributi.item(j);
					if(attr instanceof Element){
						Element attributo=(Element)attr;
						switch(attributo.getTagName()){
							case "id":
								id=Integer.parseInt(attributo.getTextContent().trim());
								break;
							case "name":
								nome=attributo.getTextContent().trim();
								break;
						}
					}
				}
				SerieSub ssub=new SerieSub(nome, id);
				serie.add(ssub);
			}
		} 
		catch (SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return serie;
	}
}
