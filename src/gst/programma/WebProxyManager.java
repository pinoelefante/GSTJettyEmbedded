package gst.programma;

import java.util.ArrayList;

public class WebProxyManager {
	private static ArrayList<WebProxy> proxies;
	private static boolean instanced;
	private static int last_proxy=0;
	
	public static void instance(){
		proxies=new ArrayList<WebProxy>();
		proxies.add(new WebProxy("Anonymouse", "http://anonymouse.org/anonwww.html", "http://anonymouse.org/cgi-bin/anon-www.cgi/"));
		//proxies.add(new WebProxy("Stealthy", "http://webproxy.stealthy.co/", "http://webproxy.stealthy.co/browse.php?u="));
		//proxies.add(new WebProxy("Webproxy.net", "http://webproxy.net/", "http://webproxy.net/view?q="));
		
		/*
		proxies.add(new WebProxy("", "", ""));
		proxies.add(new WebProxy("", "", ""));
		proxies.add(new WebProxy("", "", ""));
		proxies.add(new WebProxy("", "", ""));
		*/
	}
	public static String getUrlProxy(){
		if(!instanced)
			instance();
		int c=0;
		while(c<proxies.size()){
			if(last_proxy==proxies.size())
				last_proxy=0;
			WebProxy p=proxies.get(last_proxy++);
			if(p.isOnline())
				return p.getProxyURL();
			c++;
		}
		return "";
	}
}
