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

import util.pythonLists.PythonMap;

@SuppressWarnings({"rawtypes","unchecked"})
public class PodnapisiAPI {
	private final static Integer RESPONSE_OK = 200,
			RESPONSE_INVALIDCREDENTIALS = 300, RESPONSE_NOAUTHORISATION = 301,
			RESPONSE_INVALIDSESSION = 302, RESPONSE_MOVIENOTFOUND = 400,
			RESPONSE_INVALIDFORMAT = 401, RESPONSE_INVALIDLANGUAGE = 402,
			RESPONSE_INVALIDHASH = 403, RESPONSE_INVALIDARCHIVE = 404;
	private final static String  EMPTY	   = "";

	public static void main(String[] args) throws Exception {

		PodnapisiAPI p = new PodnapisiAPI();
		p.initiate("GestioneSerieTV/1.0");
		p.authenticate();
		p.search(new File("H:\\SerieTV\\The Big Bang Theory\\The.Big.Bang.Theory.S08E01.HDTV.x264-LOL.mp4"));
		/*
		ArrayList<Entry<String, String>> langs = p.getSupportedLanguages();
		for(Entry<String, String> e:langs){
			System.out.println(e.getKey()+" - "+e.getValue());
		}
		*/
		
	}

	private XmlRpcClient server;
	private String	   session, nonce;
	private boolean initialized, authenticated;

	public PodnapisiAPI() {
		try {
			server = new XmlRpcClient();
			XmlRpcClientConfigImpl conf = new XmlRpcClientConfigImpl();
			conf.setServerURL(new URL("http://ssp.podnapisi.net:8000/RPC2/"));
			conf.setUserAgent("GestioneSerieTVJ/1.0");
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
		return PythonMap.parseMap(res.toString());
	}
	private Map execute(String method, Object[] params) throws XmlRpcException {
		Object res = server.execute(method, params);
		if (res instanceof HashMap)
			return (HashMap) res;
		return PythonMap.parseMap(res.toString());
	}

	public boolean initiate(String userAgent) {
		List<Object> p = new ArrayList<Object>();
		p.add(userAgent);
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
		return authenticate(EMPTY, EMPTY);
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
			printArray((Object[])m.get("search_langs"));
			boolean ok = isValid(m);
			authenticated = ok;
			return ok;
		}
		catch(XmlRpcException e){
			e.printStackTrace();
		}
		return false;
	}
	public ArrayList<Entry<String, String>> getSupportedLanguages(){
		ArrayList<Entry<String,String>> list = new ArrayList<Map.Entry<String,String>>();
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
				Entry<String, String> e = new AbstractMap.SimpleEntry(lang[1].toString(), lang[0].toString());
				list.add(e);
			}
		}
		catch (XmlRpcException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	public ArrayList<String> search(File file){
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
							Object[] matches = (Object[]) hash.get("subtitles");
							System.out.println("Sub trovati = "+matches.length);
							for(int i=0;i<matches.length;i++){
								Map m = (Map) matches[i];
								System.out.println(m.get("id")+" "+m.get("lang"));
							}
						}
						else {
							System.out.println("subtitles non presente");
						}
					}
					else {
						System.out.println("hash non presente");
					}
				}
				else {
					System.out.println("results non presente");
				}
			}
		}
		catch (XmlRpcException e) {
			e.printStackTrace();
		}
		
		return ids;
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

	private void printMap(Map m) {
		Set<Entry> i = m.entrySet();
		for (Entry e : i) {
			System.out.println(e.getKey() + " = " + e.getValue());
		}
	}
	private void printList(List l){
		for(Object i:l){
			System.out.println(i.toString());
		}
	}
	private void printArray(Object[] a){
		for(int i=0;i<a.length;i++)
			System.out.println(a[i]);
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
