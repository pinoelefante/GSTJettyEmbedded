package gst.download;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("deprecation")
public class UTorrentAPI {

	private final String	 baseURL;
	private final String	 token;

	private final HttpClient client;

	public UTorrentAPI(InetSocketAddress address, String username, String password) {
		client = new DefaultHttpClient();

		// Use HTTP 1.1
		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

		// Authentication
		((DefaultHttpClient) client).getCredentialsProvider().setCredentials(new AuthScope(address.getAddress().getHostAddress(), address.getPort(), AuthScope.ANY_REALM), new UsernamePasswordCredentials(username, password));

		baseURL = "http://" + address.getAddress().getHostAddress() + ":" + address.getPort() + "/gui/";
		token = this.getToken();

	}

	String get(String query) {
		final HttpRequestBase request = new HttpGet(baseURL + "?token=" + token + "&" + query);
		return this.call(request);
	}

	private synchronized String call(HttpRequestBase request) {
		try {
			final HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
				throw new IOException("Receied non-OK response code for URI: " + request.getURI());

			return EntityUtils.toString(response.getEntity());
		}
		catch (IOException e) {

			return null;
		}
	}

	private String getToken() {
		final String result = this.call(new HttpGet(baseURL + "token.html"));

		final Pattern regex = Pattern.compile(">([^<]+)<");
		final Matcher matcher = regex.matcher(result);

		if (!matcher.find()) {

			return null;
		}
		return matcher.group(1);
	}

	@SuppressWarnings("unchecked")
	Map<String, Object> getMap(String query) {
		final String result = this.get(query);
		final JSONParser parser = new JSONParser();

		try {
			return (Map<String, Object>) parser.parse(result);
		}
		catch (ParseException e) {

			return new HashMap<String, Object>();
		}
	}

	// TODO: Make into a ServerVersion object
	@SuppressWarnings("unchecked")
	public Map<String, Object> getVersion() {
		final Map<String, Object> settings = this.getMap("action=getversion");
		return (Map<String, Object>) settings.get("version");
	}
}
