package util.os;

public class Os {
	private static String sistemaOperativo = "";

	public static boolean is32bit() {
		String arch_vm = System.getProperty("os.arch");
		boolean x86 = arch_vm.contains("x86") || arch_vm.contains("i386");
		return x86;
	}

	public static boolean is64bit() {
		return !is32bit();
	}

	public static String getVMArch() {
		if (is32bit())
			return "i386";
		else
			return "amd64";
	}

	public static String getSistemaOperativo() {
		initOS();
		return sistemaOperativo;
	}

	public static boolean isMacOS() {
		return getSistemaOperativo().toLowerCase().contains("mac");
	}

	public static boolean isWindows() {
		return getSistemaOperativo().toLowerCase().contains("windows");
	}

	public static boolean isLinux() {
		return getSistemaOperativo().toLowerCase().contains("linux");
	}

	public static String getOSName() {
		String name = "";

		if (isWindows())
			name = "Win32";
		else if (isLinux())
			name = "Linux";
		else if (isMacOS())
			name = "MacOS";

		return name;
	}

	private static void initOS() {
		if (sistemaOperativo.isEmpty()) {
			sistemaOperativo = System.getProperty("os.name");
		}
	}
}
