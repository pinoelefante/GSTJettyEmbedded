package gst.sottotitoli.opensubtitles;

import gst.programma.Settings;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
	
	public OpenSubtitlesAPI(){
		setts = Settings.getInstance();
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
		api.loginAnon();
		try {
			File file = new File("C:\\Multimedia\\SerieTV\\Gotham\\Gotham.S01E06.720p.HDTV.X264-DIMENSION.mkv");
			String hash = OpenSubtitlesHasher.computeHash(file);
			int bytes = (int) file.length();
			System.out.println("file hash: "+hash);
			System.out.println("file size: "+bytes);
			api.searchSubtitles(hash, bytes, "it");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
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
	public void searchSubtitles(String hash, int filesizeByte, String lang){
		List<Object> params = new ArrayList<Object>();
		params.add(token);
		List<Object> research_params = new ArrayList<Object>();
		Map<Object,Object> research = new HashMap<Object, Object>();
		research.put("moviehash", hash);
		research.put("moviebytesize",filesizeByte);
		research.put("sublanguageid", "ita");
		research_params.add(research);
		params.add(research_params);
		List<Object> limit = new ArrayList<Object>();
		limit.add("50");
		//params.add(limit);
		
		try {
			Map res = execute("SearchSubtitles", params);
			printMap(res);
			printMap(research);
			Object[] data = (Object[]) res.get("data");
			for(int i=0;i<data.length;i++){
				printMap((Map) data[i]);
				System.out.println();
			}
		}
		catch (XmlRpcException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	@SuppressWarnings("unused")
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
	public void serverInfo(){
		List<Object> p = new ArrayList<>();
		try {
			Map res = execute("ServerInfo", p);
			printMap(res);
		}
		catch (XmlRpcException e) {
			e.printStackTrace();
		}
	}
}
