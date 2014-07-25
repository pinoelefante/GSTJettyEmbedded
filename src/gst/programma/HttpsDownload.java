package gst.programma;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class HttpsDownload {
	private final static WebClient webClient=new WebClient(new BrowserVersion("GST",
			"5.0 ("+System.getProperty("os.name")+" "+System.getProperty("os.arch")+") GST/"+Settings.getVersioneSoftware(), 
			"Mozilla/5.0 ("+System.getProperty("os.name")+" "+System.getProperty("os.arch")+") GST/"+Settings.getVersioneSoftware(),
			Settings.getVersioneSoftware()));
	public static HtmlPage downloadPageFromHttpsUrl(String url_download) {
		try {
			webClient.setUseInsecureSSL(true);
			webClient.setActiveXNative(false);
			webClient.setAppletEnabled(false);
			webClient.setJavaScriptEnabled(false);
			HtmlPage pagina=(HtmlPage)webClient.getPage(url_download);
			return pagina;
		}
		catch (GeneralSecurityException e) {
			//webClient.closeAllWindows();
			e.printStackTrace();
		}
		catch (FailingHttpStatusCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static boolean isHttpsRaggiungibile(String url_s){
		try {
			webClient.setUseInsecureSSL(true);
			HtmlPage page=webClient.getPage(url_s);
			if(page!=null){
				return true;
			}
		}
		catch (GeneralSecurityException | FailingHttpStatusCodeException | IOException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
}
