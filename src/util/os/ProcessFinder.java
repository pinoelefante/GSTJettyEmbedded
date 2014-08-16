package util.os;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

public class ProcessFinder {
	public static int getPid(String processName){
		int pid = -1;
		if (Os.isWindows()) {
			try {
				String line;
				String[] cmd = { System.getenv("windir") + File.separator + "system32" + File.separator + "tasklist.exe", "/fo", "csv", "/nh" };
				Process p = Runtime.getRuntime().exec(cmd);
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while ((line = input.readLine()) != null) {
					String[] list = line.split(",");
					if (list[0].replace("\"", "").toLowerCase().compareTo(processName.toLowerCase()) == 0) {
						pid = Integer.parseInt(list[1].replace("\"", "").trim());
						break;
					}
				}
				input.close();
			}
			catch (Exception err) {
				err.printStackTrace();
			}
		}
		else if(Os.isLinux() || Os.isMacOS()){
			try {
				String line;
				String[] cmd = {"ps", "-e", "-c"};
				Process p = Runtime.getRuntime().exec(cmd);
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while ((line = input.readLine()) != null) {
					line=line.replace("\t"," ");
					Scanner scanner = new Scanner(line);
					ArrayList<String> list=new ArrayList<String>();
					while(scanner.hasNext()){
						list.add(scanner.next().trim());	
					}
					scanner.close();
					if(!list.isEmpty()){
						if(list.get(list.size()-1).toLowerCase().compareTo(processName.toLowerCase())==0){
							pid=Integer.parseInt(list.get(0));
							list.clear();
							break;					
						}
					}
					list.clear();
				}
				input.close();
			}
			catch (Exception err) {
				err.printStackTrace();
			}
		}
		return pid;
	}
	public static void closeProcessByPID(int pid){
		if (Os.isWindows()) {
			String[] cmd = { System.getenv("windir") + File.separator + "system32" + File.separator + "taskkill.exe", "/PID", "" + pid, "/F" };
			try {
				Runtime.getRuntime().exec(cmd).waitFor();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if (Os.isLinux() || Os.isMacOS()) {
			String[] cmd = { "kill", /*"-9",*/ "" + pid };
			try {
				Runtime.getRuntime().exec(cmd).waitFor();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
