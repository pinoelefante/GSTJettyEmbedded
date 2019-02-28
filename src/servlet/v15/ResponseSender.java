package servlet.v15;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import gst.serietv.XMLSerializable;

public class ResponseSender
{
	public static void sendResponse(HttpServletResponse response, Document doc){
		XMLOutputter xml_out = new XMLOutputter();
		xml_out.setFormat(Format.getPrettyFormat());
		response.setContentType("text/xml");
		response.setHeader("Cache-Control",	"no-store, no-cache, must-revalidate");
		PrintWriter out;
		try {
			out = response.getWriter();
			xml_out.output(doc, out);
		} 
		catch (IOException e) {
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
}
