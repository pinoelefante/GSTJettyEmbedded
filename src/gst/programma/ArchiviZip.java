package gst.programma;

import java.io.IOException;
import java.io.RandomAccessFile;

import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class ArchiviZip {
	private static boolean init;

	private static void instance() {
		if (!init) {
			try {
				SevenZip.initSevenZipFromPlatformJAR();
				init = true;
			}
			catch (SevenZipNativeInitializationException e) {
				ManagerException.registraEccezione(e);
			}
		}
	}

	public static void main(String[] args) {
		try {
			SevenZip.initSevenZipFromPlatformJAR();
			System.out.println("7-Zip-JBinding library was initialized");
			estrai("D:\\SerieTV\\Baby Daddy\\Baby.Daddy.S02E07.HDTV.x264-ASAP.zip", ".srt", "ciao_pino", "D:\\SerieTV\\Baby Daddy\\");
			// estrai_tutto("C:\\Users\\Pino\\Desktop\\vlc_win64.7z","D:\\SerieTV\\VLC");
		}
		catch (SevenZipNativeInitializationException e) {
			e.printStackTrace();
		}
	}

	public static void estrai_tutto(String archivio, String dir_dest) {
		instance();
		RandomAccessFile randomAccessFile = null;
		ISevenZipInArchive inArchive = null;
		try {
			randomAccessFile = new RandomAccessFile(archivio, "r");
			inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));

			int[] in = new int[inArchive.getNumberOfItems()];
			for (int i = 0; i < in.length; i++) {
				in[i] = i;
			}
			inArchive.extract(in, false, new MySevenZipCallBack(inArchive, dir_dest, ""));
		}
		catch (Exception e) {
			System.err.println("Error occurs: " + e);
			System.exit(1);
		}
		finally {
			if (inArchive != null) {
				try {
					inArchive.close();
				}
				catch (SevenZipException e) {
					System.err.println("Error closing archive: " + e);
				}
			}
			if (randomAccessFile != null) {
				try {
					randomAccessFile.close();
				}
				catch (IOException e) {
					System.err.println("Error closing file: " + e);
				}
			}
		}
	}

	public static void estrai(String archivio, final String estensione, final String rename, final String cartella_destinazione) {
		instance();
		RandomAccessFile randomAccessFile = null;
		ISevenZipInArchive inArchive = null;
		try {
			randomAccessFile = new RandomAccessFile(archivio, "r");
			inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));

			int[] in = new int[inArchive.getNumberOfItems()];
			int i;
			for (i = 0; i < in.length; i++) {
				String path = (String) inArchive.getProperty(i, PropID.PATH);
				if (path.toLowerCase().endsWith(estensione.toLowerCase()))
					in[i] = i;
			}
			System.arraycopy(in, 0, in, 0, i);
			inArchive.extract(in, false, new MySevenZipCallBack(inArchive, cartella_destinazione, rename));
		}
		catch (Exception e) {
			System.err.println("Error occurs: " + e);
		}
		finally {
			if (inArchive != null) {
				try {
					inArchive.close();
				}
				catch (SevenZipException e) {
					System.err.println("Error closing archive: " + e);
				}
			}
			if (randomAccessFile != null) {
				try {
					randomAccessFile.close();
				}
				catch (IOException e) {
					System.err.println("Error closing file: " + e);
				}
			}
		}
	}
}
