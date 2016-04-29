/**
 * 
 */
package br.com.riselabs.cotonet.util;

import java.io.File;

/**
 * @author alcemir
 *
 */
public class Directories {

	private static final String APP_DIR = System.getProperty("user.dir");

	public static String getAppHome() {
		return APP_DIR;
	}

	public static File getReposDir() {
		return getDir("repos");
	}

	public static File getConfigDir(){
		return getDir("confs");
	}

	public static File getLogDir()  {
		return getDir("logs");
	}

	public static File getMappingsDir(){
		return getDir("mappings");
	}

	private static File getDir(String child) {
		File dir;
		if ((dir = new File(APP_DIR, child)).exists()) {
			dir.mkdir();
		}
		return dir;
	}
}
