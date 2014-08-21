package gst.gui;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class ClientFilter extends FileFilter {
	private String	description;
	private String	client;

	public ClientFilter(String descr, String client) {
		this.description = descr;
		this.client = client.toLowerCase();
	}

	public boolean accept(File f) {
		if (f.isDirectory())
			return true;
		String path = f.getAbsolutePath().toLowerCase();

		return path.endsWith(this.client);
	}

	public String getDescription() {
		return this.description;
	}
}
