package util.httpOperations;

import java.net.URI;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpOperations {
	public static String GET_withResponse(String url) throws Exception{
		HttpGet getter=new HttpGet(new URI(url));
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response =  httpclient.execute(getter);
		HttpEntity entity = response.getEntity();
		String ret = EntityUtils.toString(entity);
		response.close();
		httpclient.close();
		return ret;
	}
	public static boolean GET_withBoolean(String url) throws Exception{
		HttpGet getter=new HttpGet(new URI(url));
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response =  httpclient.execute(getter);
		int statusCode = response.getStatusLine().getStatusCode();
		response.close();
		httpclient.close();
		return statusCode==200;
	}
	
	public static String POST_withResponse(String url, List<NameValuePair> parametri) throws Exception{
		HttpPost post = new HttpPost(new URI("http://localhost:8080/command/download"));
		if(parametri!=null){
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parametri, Consts.UTF_8);
			post.setEntity(entity);
		}
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response =  httpclient.execute(post);
		String ret = EntityUtils.toString(response.getEntity()); 
		response.close();
		httpclient.close();
		return ret;
	}
	public static boolean POST_withBoolean(String url, List<NameValuePair> parametri) throws Exception{
		HttpPost post = new HttpPost(new URI(url));
		if(parametri!=null){
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parametri, "UTF-8");
			System.out.println(EntityUtils.toString(entity));
			post.setEntity(entity);
		}
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response =  httpclient.execute(post);
		int statusCode = response.getStatusLine().getStatusCode();
		response.close();
		httpclient.close();
		return statusCode==200;
	}
	public static boolean POST_withBooleanNotEncoded(String url, StringEntity parametri) throws Exception{
		HttpPost post = new HttpPost(new URI(url));
		if(parametri!=null){
			System.out.println(EntityUtils.toString(parametri));
			post.setEntity(parametri);
		}
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response =  httpclient.execute(post);
		int statusCode = response.getStatusLine().getStatusCode();
		response.close();
		httpclient.close();
		return statusCode==200;
	}
	
	public static boolean GET_withBoolean_AuthBasic(String address, String port, String user, String pass, String cmd) throws Exception {
		/*CredentialsProvider credsProvider = new BasicCredentialsProvider();
		AuthScope authScope = new AuthScope(address, Integer.parseInt(port));
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, pass);
		credsProvider.setCredentials(authScope, credentials);
		*/
		
		String encoding = new String(Base64.encodeBase64((user + ":" + pass).getBytes()));
		HttpGet getter=new HttpGet(new URI(cmd));
		getter.addHeader("Authorization", "Base "+encoding);
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response =  httpclient.execute(getter);
		int statusCode = response.getStatusLine().getStatusCode();
		response.close();
		httpclient.close();
		return statusCode==200;
	}
}
