package gst.sottotitoli.italiansubs;

import gst.naming.CaratteristicheFile;
import gst.serieTV.SerieTV;
import gst.sottotitoli.SerieSub;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import util.UserAgent;

public class ItasaAPI {
	private String AUTHCODE		 = "";
	private String APIKEY		 = "87c9d52fba19ba856a883b1d3ddb14dd";

	private String API_SHOWLIST  = "https://api.italiansubs.net/api/rest/shows?apikey=" + APIKEY;
	private String API_SUB_GETID = "https://api.italiansubs.net/api/rest/subtitles/search?q=<QUERY>&show_id=<SHOW_ID>&version=<VERSIONE>&apikey=" + APIKEY;
	private String API_LOGIN	 = "https://api.italiansubs.net/api/rest/users/login?username=<USERNAME>&password=<PASSWORD>&apikey=" + APIKEY;
	private String API_DOWNLOAD  = "https://api.italiansubs.net/api/rest/subtitles/download?subtitle_id=<ID_SUB>&authcode=<AUTHCODE>&apikey=" + APIKEY;

	private CloseableHttpClient httpclient;
	private BasicCookieStore	cookieStore;

	public ItasaAPI() throws Exception {
		SSLContextBuilder builder = SSLContexts.custom();
		builder.loadTrustMaterial(null, new TrustStrategy() {
		    @Override
		    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		        return true;
		    }
		});
		SSLContext sslContext = builder.build();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
		        .<ConnectionSocketFactory> create().register("https", sslsf)
		        .register("http", new PlainConnectionSocketFactory())
		        .build();

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		
		ArrayList<Header> headers = new ArrayList<Header>();
		headers.add(new BasicHeader(HttpHeaders.USER_AGENT, UserAgent.get()));
		
		cookieStore = new BasicCookieStore();
		httpclient = HttpClients.custom()
				.setDefaultCookieStore(cookieStore)
				.setConnectionManager(cm)
				.setDefaultHeaders(headers)
				.build();
	}

	public boolean login(String username, String password) {
		String auth = verificaLogin(username, password);
		if (auth != null) {
			AUTHCODE = auth;
			try {
				loginWeb(username, password);
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

	private void loginWeb(String username, String password) throws Exception {
        org.jsoup.nodes.Document homepage = Jsoup.parse(new URL("https://www.italiansubs.net/index.php"), 5000);
        org.jsoup.select.Elements inputs = homepage.select("form#form-login input[type=hidden]");
        List<NameValuePair> input_fields = new ArrayList<NameValuePair>();
        for(int i=0;i<inputs.size();i++)
        {
            org.jsoup.nodes.Element inp = inputs.get(i);
            input_fields.add(new BasicNameValuePair(inp.attr("name"), inp.attr("value")));
        }
        input_fields.add(new BasicNameValuePair("username", username));
        input_fields.add(new BasicNameValuePair("passwd", password));
        
        HttpPost request = new HttpPost("https://www.italiansubs.net/index.php");
        request.setHeader(new BasicHeader(HttpHeaders.REFERER, "https://www.italiansubs.net/index.php"));
        request.setHeader(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded"));
        
        UrlEncodedFormEntity entity_request = new UrlEncodedFormEntity(input_fields, Consts.UTF_8);
        request.setEntity(entity_request);
        
        try {
            CloseableHttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String body_content = ReadResponse(entity);
                EntityUtils.consume(entity);
                System.out.println(body_content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	private String ReadResponse(HttpEntity entity) {
        try {
            InputStream in = entity.getContent();
            InputStreamReader in_reader = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(in_reader);
            StringBuilder str_builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                str_builder.append(line);
            }
            reader.close();
            in_reader.close();
            in.close();
            return str_builder.toString();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
	private boolean tm_instanced = false;

	private HttpsURLConnection getConnection(String url) throws IOException {
		if (!tm_instanced) {
			tm_instanced = true;
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				public void checkClientTrusted(X509Certificate[] certs, String authType) {}
				public void checkServerTrusted(X509Certificate[] certs, String authType) {}
			} };

			// Install the all-trusting trust manager
			SSLContext sc = null;
			try {
				sc = SSLContext.getInstance("SSL");
			}
			catch (NoSuchAlgorithmException e2) {
				e2.printStackTrace();
			}
			try {
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
			}
			catch (KeyManagementException e1) {
				e1.printStackTrace();
			}
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		}
		try {
			URL uri = new URL(url.replace(" ", "%20"));
			HttpsURLConnection connection = (HttpsURLConnection) uri.openConnection();
			connection.setRequestProperty("User-Agent", UserAgent.get());
			connection.setDoInput(true);
			connection.setDoOutput(true);
			return connection;
		}
		catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public String verificaLogin(String username, String password) {
		String url_login = API_LOGIN.replace("<USERNAME>", username).replace("<PASSWORD>", password);
		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder domparser = null;
		HttpsURLConnection connection = null;
		try {
			connection = getConnection(url_login);
			domparser = dbfactory.newDocumentBuilder();
			Document doc = domparser.parse(connection.getInputStream());

			NodeList elenco_shows = doc.getElementsByTagName("authcode");
			if (elenco_shows.getLength() > 0)
				return ((Element) elenco_shows.item(0)).getTextContent();
		}
		catch (SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return null;
	}
/*
	private List<NameValuePair> getParameters(String html) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		org.jsoup.nodes.Document doc = Jsoup.parse(html);
		org.jsoup.nodes.Element form = doc.getElementById("form-login");
		Elements input = form.getElementsByTag("input");
		for (int i = 0; i < input.size(); i++) {
			org.jsoup.nodes.Element el = input.get(i);
			if (el.attr("name").compareTo("username") == 0 || el.attr("name").compareTo("passwd") == 0)
				continue;
			params.add(new BasicNameValuePair(el.attr("name"), el.attr("value")));
		}
		return params;
	}
*/
	public String download(int idSub, String folder) throws Exception {
		if (AUTHCODE.isEmpty())
			throw new Exception("Not logged in - Check your credentials");

		String url = API_DOWNLOAD.replace("<ID_SUB>", "" + idSub).replace("<AUTHCODE>", AUTHCODE);
		HttpGet req = new HttpGet(url);
		CloseableHttpResponse response = httpclient.execute(req);
		HttpEntity entity = null;;
		InputStream content = null;;
		FileOutputStream fos = null;;
		try {
			entity = response.getEntity();
			content = entity.getContent();
			new File(folder).mkdirs();
			String pathDown = folder + File.separator + idSub + ".zip";
			fos = new FileOutputStream(pathDown);
			byte[] buff = new byte[1024];
			int read = 0;
			while ((read = content.read(buff)) > 0) {
				fos.write(buff, 0, read);
			}
			return pathDown;
		}
		finally {
			if (response != null)
				response.close();
			EntityUtils.consume(entity);
			if (content != null)
				content.close();
			if (fos != null)
				fos.close();
		}
	}

	private String getVersione(CaratteristicheFile c) {
		if (c.isDVDRip())
			return ItalianSubs.DVDRIP;
		if (c.is720p())
			return ItalianSubs.HD720p;

		return ItalianSubs.HDTV;
	}

	public int cercaSottotitolo(SerieTV serie, CaratteristicheFile stat) {
		return cercaSottotitolo(serie.getIDItasa(), stat.getStagione(), stat.getEpisodio(), getVersione(stat));
	}

	public int cercaSottotitolo(SerieSub serie, CaratteristicheFile stat) {
		return cercaSottotitolo(serie.getIDDB(), stat.getStagione(), stat.getEpisodio(), getVersione(stat));
	}

	public int cercaSottotitolo(int idSerie, int stagione, int episodio, String qualita) {
		String query = stagione + "x" + (episodio < 10 ? "0" + episodio : episodio);
		String url_query = API_SUB_GETID.replace("<QUERY>", query).replace("<VERSIONE>", qualita).replace("<SHOW_ID>", idSerie + "");
		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder domparser = null;
		HttpsURLConnection connection = null;
		try {
			connection = getConnection(url_query);
			domparser = dbfactory.newDocumentBuilder();
			Document doc = domparser.parse(connection.getInputStream());

			NodeList countlist = doc.getElementsByTagName("count");
			if (countlist.getLength() == 1) {
				Element count = (Element) countlist.item(0);
				int num_sub = Integer.parseInt(count.getTextContent());
				if (num_sub > 0) {
					NodeList idlist = doc.getElementsByTagName("id");
					int id = 0;
					for (int i = 0; i < idlist.getLength(); i++) {
						Node value = idlist.item(i);
						if (value instanceof Element) {
							Element v = (Element) value;
							int id_v = Integer.parseInt(v.getTextContent());
							if (id_v > id)
								id = id_v;
						}
					}
					return id;
				}
			}
		}
		catch (ParserConfigurationException e) {}
		catch (SAXException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return 0;
	}

	public ArrayList<SerieSub> caricaElencoSerieOnlineXML() {
		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder domparser = null;
		ArrayList<SerieSub> serie = new ArrayList<SerieSub>();
		HttpsURLConnection connection = null;
		try {
			connection = getConnection(API_SHOWLIST);
			domparser = dbfactory.newDocumentBuilder();
			Document doc = domparser.parse(connection.getInputStream());

			NodeList elenco_shows = doc.getElementsByTagName("show");
			for (int i = 0; i < elenco_shows.getLength(); i++) {
				Node show = elenco_shows.item(i);
				NodeList show_attributi = show.getChildNodes();
				String nome = "";
				int id = 0;
				for (int j = 0; j < show_attributi.getLength(); j++) {
					Node attr = show_attributi.item(j);
					if (attr instanceof Element) {
						Element attributo = (Element) attr;
						switch (attributo.getTagName()) {
							case "id":
								id = Integer.parseInt(attributo.getTextContent().trim());
								break;
							case "name":
								nome = attributo.getTextContent().trim();
								break;
						}
					}
				}
				SerieSub ssub = new SerieSub(nome, id);
				serie.add(ssub);
			}
		}
		catch (SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return serie;
	}
}
