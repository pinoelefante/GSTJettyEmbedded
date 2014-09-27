package util;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class MD5Hash {
	public static String hashFile(String file) {
		return hashFile(new File(file));
	}
	public static String hashFile(File file) {
	    try (FileInputStream inputStream = new FileInputStream(file)) {
	        MessageDigest digest = MessageDigest.getInstance("MD5");
	 
	        byte[] bytesBuffer = new byte[1024];
	        int bytesRead = -1;
	 
	        while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
	            digest.update(bytesBuffer, 0, bytesRead);
	        }
	 
	        byte[] hashedBytes = digest.digest();
	 
	        return convertByteArrayToHexString(hashedBytes);
	    } 
	    catch (Exception ex) {
	       ex.printStackTrace();
	    }
	    return null;
	}
	private static String convertByteArrayToHexString(byte[] arrayBytes) {
	    StringBuffer stringBuffer = new StringBuffer();
	    for (int i = 0; i < arrayBytes.length; i++) {
	        stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16).substring(1));
	    }
	    return stringBuffer.toString();
	}
}
