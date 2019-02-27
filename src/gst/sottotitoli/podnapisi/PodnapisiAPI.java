package gst.sottotitoli.podnapisi;

import gst.sottotitoli.opensubtitles.OpenSubtitlesHasher;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

@SuppressWarnings({"rawtypes","unchecked"})
public class PodnapisiAPI {
	private final static Integer RESPONSE_OK = 200/*,
			RESPONSE_INVALIDCREDENTIALS = 300, RESPONSE_NOAUTHORISATION = 301,
			RESPONSE_INVALIDSESSION = 302, RESPONSE_MOVIENOTFOUND = 400,
			RESPONSE_INVALIDFORMAT = 401, RESPONSE_INVALIDLANGUAGE = 402,
			RESPONSE_INVALIDHASH = 403, RESPONSE_INVALIDARCHIVE = 404*/;
	private final static String  EMPTY	   = "";

	private XmlRpcClient server;
	private String	   session, nonce;
	private boolean initialized, authenticated, anonymouse;

	public PodnapisiAPI() {
		try {
			server = new XmlRpcClient();
			XmlRpcClientConfigImpl conf = new XmlRpcClientConfigImpl();
			conf.setServerURL(new URL("http://ssp.podnapisi.net:8000/RPC2/"));
			conf.setUserAgent("GSTJ");
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

	public boolean initiate() {
		List<Object> p = new ArrayList<Object>();
		p.add("GSTJ");
		try {
			Map m = execute("initiate", p);
			//printMap(m);
			if (isValid(m)) {
				initialized = true;
				session = m.get("session").toString();
				nonce = m.get("nonce").toString();
				return true;
			}
		}
		catch(XmlRpcException e){
			e.printStackTrace();
		}
		return false;
	}

	public boolean authenticate() {
		boolean a = authenticate(EMPTY, EMPTY);
		if(a)
			anonymouse = true;
		return a;
	}

	public boolean authenticate(String username, String pass) {
		if(!isInizialized())
			return false;
		
		String f_pass = pass.isEmpty()?pass:(generaHash(generaHash(pass, "MD5") + nonce, "SHA-256"));
		ArrayList params = new ArrayList<>();
		params.add(session);
		params.add(username);
		params.add(f_pass);

		try {
			Map m = execute("authenticate", params);
			boolean ok = isValid(m);
			if(!ok){
				return authenticate();
			}
			else {
				if(username.isEmpty() || pass.isEmpty())
					anonymouse = true;
			}
			authenticated = ok;
			return ok;
		}
		catch(XmlRpcException e){
			e.printStackTrace();
		}
		return false;
	}
	public List<Entry<String, Integer>> getSupportedLanguages(){
		ArrayList<Entry<String,Integer>> list = new ArrayList<Map.Entry<String,Integer>>();
		if(!isInizialized())
			return list;
		
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(session);
		
		try {
			Map res = execute("supportedLanguages", params);
			boolean ok = isValid(res);
			System.out.println("supported langs: "+ok);
			Object[] langs = (Object[]) res.get("languages");
			for(int i=0;i<langs.length;i++){
				Object[] lang = (Object[])langs[i];
				Entry<String, Integer> e = new AbstractMap.SimpleEntry(lang[1].toString(), lang[0]);
				list.add(e);
			}
		}
		catch (XmlRpcException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	public ArrayList<String> search(File file, boolean hd){
		ArrayList<String> ids = new ArrayList<String>();
		if(!isInizialized())
			return ids;
		
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(session);
		ArrayList<Object> hashes = new ArrayList<Object>();
		String file_hash = null;
		try {
			file_hash=OpenSubtitlesHasher.computeHash(file);
			System.out.println(file_hash+"   filename="+file.getName());
			hashes.add(file_hash);
		}
		catch (IOException e) {
			e.printStackTrace();
			return ids;
		}
		
		params.add(hashes);
		
		try {
			Map resp = execute("search", params);
			if(isValid(resp)){
				if(resp.containsKey("results")){
					Map results = (Map) resp.get("results");
					if(results.containsKey("hash") || results.containsKey(file_hash)){
						Map hash = (Map) results.get(results.containsKey("hash")?"hash":file_hash);
						if(hash.containsKey("subtitles")){
							System.out.println(hash.get("tvSeason")+"x"+hash.get("tvEpisode"));
							Object[] matches = (Object[]) hash.get("subtitles");
							System.out.println("Sub trovati = "+matches.length);
							for(int i=0;i<matches.length;i++){
								Map m = (Map) matches[i];
								if(hd && m.get("flags").toString().contains("h")){
									ids.add(m.get("id").toString());
								}
								else if(hd==false && !m.get("flags").toString().contains("h")){
									ids.add(m.get("id").toString());
								}
							}
						}
					}
				}
			}
		}
		catch (XmlRpcException e) {
			e.printStackTrace();
		}
		
		return ids;
	}
	public boolean setFilters(Integer lang){
		ArrayList<Integer> p = new ArrayList<Integer>();
		p.add(lang);
		return setFilters(p);
	}
	public boolean setFilters(List<Integer> langs){
		if(!isInizialized()){
			return false;
		}
		
		ArrayList<Object> parameters = new ArrayList<Object>();
		parameters.add(session);
		parameters.add(Boolean.TRUE);
		parameters.add(langs);
		parameters.add(Boolean.FALSE);
		
		try {
			Map resp = execute("setFilters", parameters);
			boolean ok = isValid(resp);
			System.out.println("Applicazione filtro lingue: "+ok);
			return ok;
		}
		catch (XmlRpcException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	public String download(String sub){
		ArrayList<String> s = new ArrayList<String>();
		s.add(sub);
		ArrayList<String> list = download(s);
		if(list.size()>0)
			return list.get(0);
		return "";
	}
	public ArrayList<String> download(List<String> subs){
		ArrayList<String> res = new ArrayList<String>();
		if(!isInizialized() || anonymouse)
			return res;
		
		ArrayList<Object> parameters = new ArrayList<Object>();
		parameters.add(session);
		parameters.add(subs);
		
		try {
			Map m = execute("download", parameters);
			if(isValid(m)){
				Object[] list = (Object[]) m.get("names");
				for(int i=0;i<list.length;i++){
					Map s = (Map) list[i];
					res.add("http://www.podnapisi.net/static/podnapisi/"+s.get("filename").toString());
				}
			}
		}
		catch (XmlRpcException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	private boolean isValid(Map m) {
		Integer code = (Integer) m.get("status");
		return (code.equals(RESPONSE_OK));
	}
	public boolean isInizialized(){
		return initialized;
	}
	public boolean isAuthenticated(){
		return authenticated;
	}

	@SuppressWarnings("unused")
	private void printMap(Map m) {
		Set<Entry> i = m.entrySet();
		for (Entry e : i) {
			System.out.println(e.getKey() + " = " + e.getValue());
		}
	}

	private String generaHash(String word, String alg) {
		try {
			MessageDigest msg_dig = MessageDigest.getInstance(alg);
			byte[] hash = msg_dig.digest(word.getBytes());

			StringBuilder hexString = new StringBuilder();

			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
}
