package server.settings;

import java.nio.file.Paths;

public class SettingsController
{
	private final static String
		DOWNLOAD_DIR = "DownloadDir",
		VLC_PATH = "VlcPath",
		UTORRENT_PATH = "UtorrentPath",
		AUTOSTART = "Autostart",
		OPENSUBTITLES_USER = "OpenSubtitles_Username",
		OPENSUBTITLES_PASSWORD = "OpenSubtitles_Password",
		PODNAPISI_USER = "Podnapisi_Username",
		PODNAPISI_PASSWORD = "Podnapisi_Password";
	
	private SettingsManager settings;
	
	private static class SingletonHelper{
        private static final SettingsController INSTANCE = new SettingsController();
    }
	private SettingsController()
	{
		settings = SettingsManager.getInstance();
	}
	public static SettingsController getInstance() {
		return SingletonHelper.INSTANCE;
	}
	
	public String getDownloadDir()
	{
		return settings.getSetting(DOWNLOAD_DIR, getDefaultDownloadDir());
	}
	public void setDownloadDir(String dir)
	{
		settings.setSetting(DOWNLOAD_DIR, dir);
	}
	public String getVLCPath() {
		return settings.getSetting(VLC_PATH, null);
	}
	public void setVLCPath(String path)
	{
		settings.setSetting(VLC_PATH, path);
	}
	public String getUTorrentPath() {
		return settings.getSetting(UTORRENT_PATH, null);
	}
	public void setUTorrentPath(String path) {
		settings.setSetting(UTORRENT_PATH, path);
	}
	public boolean isAutostart() {
		return settings.getSetting(AUTOSTART, false);
	}
	public void setAutostart(boolean b)
	{
		settings.setSetting(AUTOSTART, b);
	}
	public String getOpenSubtitlesUsername() {
		return settings.getSetting(OPENSUBTITLES_USER, "");
	}
	public void setOpenSubtitlesUsername(String user)
	{
		settings.setSetting(OPENSUBTITLES_USER, user);
	}
	public String getOpenSubtitlesPassword() {
		return settings.getSetting(OPENSUBTITLES_PASSWORD, "");
	}
	public void setOpenSubtitlesPassword(String p) {
		settings.setSetting(OPENSUBTITLES_PASSWORD, p);
	}
	public String getPodnapisiUsername() {
		return settings.getSetting(PODNAPISI_USER, "");
	}
	public void setPodnapisiUsername(String u) {
		settings.setSetting(PODNAPISI_USER, u);
	}
	public String getPodnapisiPassword() {
		return settings.getSetting(PODNAPISI_PASSWORD, "");
	}
	public void setPodnapisiPassword(String p) {
		settings.setSetting(PODNAPISI_PASSWORD, p);
	}
	public boolean saveSettings() {
		return settings.saveSettings();
	}
	public boolean loadSettings() {
		return settings.loadSettings();
	}
	private String getDefaultDownloadDir()
	{
		String currentDir = Paths.get("").toAbsolutePath().toString();
		return Paths.get(currentDir, "Download").toAbsolutePath().toString();
	}
}
