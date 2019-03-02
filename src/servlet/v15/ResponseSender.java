package servlet.v15;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import gst.serietv.JSONSerializable;
import gst.serietv.XMLSerializable;

public class ResponseSender
{
	public static void sendXMLResponse(HttpServletResponse response, Document doc){
		XMLOutputter xml_out = new XMLOutputter();
		xml_out.setFormat(Format.getPrettyFormat());
		response.setContentType("text/xml");
		response.setHeader("Cache-Control",	"no-store, no-cache, must-revalidate");
		PrintWriter out;
		try {
			out = response.getWriter();
			xml_out.output(doc, out);
			out.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void sendJSONResponse(HttpServletResponse response, JSONObject obj)
	{
		if(obj == null)
			obj = new JSONObject();
		response.setContentType("application/json");
		response.setHeader("Cache-Control",	"no-store, no-cache, must-revalidate");
		PrintWriter out;
		try {
			out = response.getWriter();
			obj.writeJSONString(out);
			out.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	public static <E extends XMLSerializable> Document createItemXml(E item)
	{
		Document doc = new Document(item.getXml());
		return doc;
	}
	public static <E extends XMLSerializable> Document createCollectionXml(Collection<E> collection)
	{
		Element col = new Element("collection");
		System.out.println("collectionSize: "+collection.size());
		for(E item : collection)
		{
			System.out.println(item.toString());
			col.addContent(item.getXml());
		}
		Document doc = new Document(col);
		return doc;
	}
	public static <E extends JSONSerializable> JSONObject createItemJson(E item)
	{
		return item.getJson();
	}
	@SuppressWarnings("unchecked")
	public static <E extends JSONSerializable> JSONObject createCollectionJson(Collection<E> collection)
	{
		JSONObject obj = new JSONObject();
		JSONArray array = new JSONArray();
		collection.forEach((E item) -> array.add(item.getJson()));
		obj.put("collection", array);
		return obj;
	}
}
