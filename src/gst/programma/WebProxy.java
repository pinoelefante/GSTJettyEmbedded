package gst.programma;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class WebProxy {
	private String baseURL;
	private String proxyURL;
	private String nomeProxy;
	private boolean isOnline;
	private long lastOnline=0L;
	
	public WebProxy(String nome, String base, String url){
		this.nomeProxy=nome;
		this.baseURL=base;
		this.proxyURL=url;
	}
	public boolean isOnline(){
		long curtime=System.currentTimeMillis();
		if(curtime-lastOnline>=3*60*1000){
			checkOnline();
		}
		return isOnline;
	}
	public String getNomeProxy(){
		return nomeProxy;
	}
	public String getProxyURL(){
		return proxyURL;
	}
	private void checkOnline(){
		//System.out.println("Verifica online proxy");
		try {
			URL url=new URL(baseURL);
			HttpURLConnection conn=(HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(60000);
			conn.setRequestProperty("User-Agent", "GestioneSerieTV/rel."+Settings.getVersioneSoftware()+" ("+System.getProperty("os.name")+")");
			if(conn.getResponseCode()==200)
				isOnline=true;
			else
				isOnline=false;
			lastOnline=System.currentTimeMillis();
			
			conn.disconnect();
		}
		catch (MalformedURLException e) {
			isOnline=false;
		}
		catch (IOException e) {
			isOnline=false;
		}
	}
}
