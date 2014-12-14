package gst.sottotitoli.opensubtitles;

import gst.programma.Settings;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import util.UserAgent;

@SuppressWarnings({"rawtypes","unchecked"})
public class OpenSubtitlesAPI {
	private boolean testing = true;
	private XmlRpcClient server;
	private final static String TestUserAgent = "OSTestUserAgent";
	private String userAgent, token;
	private Settings setts;
	private int download_count, download_limit;
	private Timer timer;
	
	public OpenSubtitlesAPI(){
		setts = Settings.getInstance();
		timer = new Timer();
		userAgent = testing?TestUserAgent:UserAgent.getOpenSubtitles();
		server = new XmlRpcClient();
		XmlRpcClientConfigImpl conf = new XmlRpcClientConfigImpl();
		try {
			conf.setServerURL(new URL("http://api.opensubtitles.org/xml-rpc"));
			conf.setUserAgent(userAgent);
			conf.setGzipCompressing(true);
			conf.setGzipRequesting(true);
			server.setConfig(conf);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	private Map execute(String method, List<Object> params) throws XmlRpcException {
		Object res = server.execute(method, params);
		if (res instanceof HashMap)
			return (HashMap) res;
		throw new XmlRpcException("formato dati inaspettato");
	}
	public boolean login(String user, String pass){
		List<Object> params = new ArrayList<Object>();
		params.add(user);
		params.add(pass);
		params.add(setts.getLingua());
		params.add(userAgent);
		try {
			Map login = execute("LogIn", params);
			printMap(login);
			if(isResponseAccepted(login)){
				token = login.get("token").toString();
				verifyDownloadCount();
				return true;
			}
			else
				return false;
		}
		catch (XmlRpcException e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean loginAnon(){
		return login("","");
	}
	public boolean logga(){
		return login(setts.getOpenSubtitlesUsername(),setts.getOpenSubtitlesPassword());
	}
	public boolean logout(){
		List<Object> p = new ArrayList<>();
		p.add(token);
		try {
			Map res = execute("LogOut", p);
			return isResponseAccepted(res);
		}
		catch (XmlRpcException e) {
			e.printStackTrace();
		}
		return false;
	}
	public void noOperation(){
		List<Object> p = new ArrayList<>();
		p.add(token);
		try {
			Map res = execute("NoOperation", p);
			if(!isResponseAccepted(res))
				token="";
		}
		catch (XmlRpcException e) {
			e.printStackTrace();
		}
	}
	public boolean isLoggedIn(){
		if(token==null || token.isEmpty())
			return false;
		return true;
	}
	public static void main(String[] args){
		OpenSubtitlesAPI api = new OpenSubtitlesAPI();
		api.logga();
		api.verifyDownloadCount();
	}
	public void checkMovieHash(String hash){
		List<Object>p = new ArrayList<>();
		p.add(token);
		List<Object> hashes = new ArrayList<Object>();
		hashes.add(hash);
		p.add(hashes);
		try {
			Map res = execute("CheckMovieHash",p);
			printMap(res);
		}
		catch (XmlRpcException e) {
			e.printStackTrace();
		}
	}
	public Map<String, String> searchSubtitles(String hash, int filesizeByte, String lang){
		Map<String, String> sottotitoli = new HashMap<String, String>();
		if(!isLoggedIn()){
			if(!logga())
				return sottotitoli;
		}
		List<Object> params = new ArrayList<Object>();
		params.add(token);
		List<Object> research_params = new ArrayList<Object>();
		Map<Object,Object> research = new HashMap<Object, Object>();
		research.put("moviehash", hash);
		research.put("moviebytesize",filesizeByte);
		research.put("sublanguageid", "ita");
		research_params.add(research);
		params.add(research_params);
		
		try {
			Map res = execute("SearchSubtitles", params);
			printMap(res);
			try {
    			Object[] data = (Object[]) res.get("data");
    			for(int i=0;i<data.length;i++){
    				Map l = (Map) data[i];
    				String filename = l.get("MovieReleaseName").toString();
    				String link = l.get("ZipDownloadLink").toString();
    				sottotitoli.put(filename, link);
    			}
			}
			catch(ClassCastException e){}
		}
		catch (XmlRpcException e) {
			e.printStackTrace();
		}
		return sottotitoli;
	}
	
	private void printMap(Map m) {
		Set<Entry> i = m.entrySet();
		for (Entry e : i) {
			System.out.println(e.getKey() + " = " + e.getValue());
		}
	}
	private boolean isResponseAccepted(Map req){
		if(req!=null){
			Object stat=req.get("status");
			if(stat!=null){
				return(stat.toString().startsWith("20"));
			}
		}
		return false;
	}
	public Map serverInfo(){
		List<Object> p = new ArrayList<>();
		Map res = null;
		try {
			res = execute("ServerInfo", p);
			//printMap(res);
		}
		catch (XmlRpcException e) {
			e.printStackTrace();
		}
		return res;
	}
	public boolean canDownload(){
		return download_count<download_limit;
	}
	private final static long time_verify_download = 30*60*1000;
	private TimerTask task;
	public void increaseDownloadCount(){
		download_count++;
		if(download_count>=download_limit){
			timer.schedule(task=new TimerTask() {
				public void run() {
					verifyDownloadCount();
				}
			}, 0, time_verify_download);
		}
	}
	public void verifyDownloadCount(){
		Map res = serverInfo();
		if(res!=null){
			Map limits = (Map) res.get("download_limits");
			if(limits!=null){
				try {
					download_count = Integer.parseInt(limits.get("client_24h_download_count").toString());
					download_limit = Integer.parseInt(limits.get("client_24h_download_limit").toString());
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		if(download_count<download_limit){
			if(task!=null)
				task.cancel();
			timer.purge();
			task = null;
		}
	}
}
