/**
 * 
 */
package br.com.riselabs.crawlers.util;

/**
 * @author alcemir
 *
 */
public class RCProperties {

	private static final String USER_HOME = System.getProperty("user.home");
	private static  String WORKING_DIR = USER_HOME;
	
	public static String getUserHome() {
		return USER_HOME;
	}
	
	public static void setWorkingDir(String suffix) {
		WORKING_DIR = USER_HOME+suffix;
	}
	
	public static String getWorkingDir() {
		return WORKING_DIR;
	}

	public static String getCodefaceDir() {
		return WORKING_DIR+"/codeface/";
	}

	public static String getReposDir() {
		return getCodefaceDir() + "repos/";
	}

	public static String getConfigDir() {
		return getCodefaceDir() + "conf/";
	}
	
}
