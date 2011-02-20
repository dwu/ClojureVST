package de.flupp.clojurevst;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utility class (from opaz-plugdk; https://github.com/thbar/opaz-plugdk)
 */
public class ProxyTools {
	// Although I wanted to use VSTPluginAdapter.RUNNING_MAC_X instead of this, it raises AWT thread errors.
	// Sticking with this one for the moment.
	public static boolean useMacOSX() {
		String lcOSName = System.getProperty("os.name").toLowerCase();
		return lcOSName.startsWith("mac os x");
	}

	public static String getResourcesFolder(String logBasePath) {
		String resourcesFolder = logBasePath;
		if (useMacOSX()) // mac os x tweak :o
			resourcesFolder += "/../Resources";
		return resourcesFolder;
	}

	public static String getIniFileName(String resourcesFolder, String logFileName) {
		String iniFileName = logFileName.replaceAll("_java_stdout.txt","");
		if (useMacOSX())
			iniFileName += ".jnilib";
		return resourcesFolder + "/" + iniFileName + ".ini";
	}
	
	/**
	 * Get stack trace of an exception as a string for logging purposes.
	 * 
	 * @param e Exception
	 * @return
	 */
	public static String getStackTraceText(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.getBuffer().toString();
	}
}
