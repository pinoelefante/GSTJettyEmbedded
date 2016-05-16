package gst.programma;

public class Update {
	public static void start() {
		if(Settings.getInstance().getVersioneSoftware()>Settings.getInstance().getLastVersion()){
			switch(Settings.getInstance().getLastVersion()){
				case 0:
				default:
					Settings.getInstance().setLastVersion(Settings.getInstance().getVersioneSoftware());
					Settings.getInstance().salvaSettings();
			}
		}
	}
}
