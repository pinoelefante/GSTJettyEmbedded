package util.plist;

import java.io.IOException;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;

public class PlistReader {
	public static void main(String[] args){
		try {
			NSObject root = PropertyListParser.parse("com.qbittorrent.qBittorrent.plist");
			if(root instanceof NSDictionary){
				System.out.println("L'elemento principale del file è NSDictionary");
				NSDictionary root_d = (NSDictionary)root;
				NSObject item = root_d.objectForKey("Preferences.WebUI.Enabled");
				//System.out.println(item.getClass().getName());
				stampaItem(item);
				root_d.put("Preferences.WebUI.Enabled", new NSNumber(true));
				stampaItem(root_d.objectForKey("Preferences.WebUI.Enabled"));
			}
		}
		catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (PropertyListFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void stampaItem(NSObject i){
		if(i instanceof NSNumber){
			NSNumber n=(NSNumber)i;
			if(n.isBoolean())
				System.out.println(n.boolValue());
			else if(n.isInteger())
				System.out.println(n.intValue());
			else if(n.isReal())
				System.out.println(n.doubleValue());
		}
		else if(i instanceof NSString){
			NSString s=(NSString)i;
			System.out.println(s);
		}
	}
}
