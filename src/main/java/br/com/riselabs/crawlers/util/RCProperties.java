/**
 * 
 */
package br.com.riselabs.crawlers.util;

import java.io.File;

/**
 * @author alcemir
 *
 */
public class RCProperties {

	private static final String USER_HOME = System.getProperty("user.home");
	private static String WORKING_DIR = USER_HOME;

	public static String getUserHome() {
		return USER_HOME;
	}

	/**
	 * Set the working directory to the User Home directory.
	 */
	public static void setWorkingDir(){
		setWorkingDir("");
	}
	/**
	 * Set the working directory appending the "suffix" to the User Home directory.
	 */
	public static void setWorkingDir(String suffix) {
		WORKING_DIR = USER_HOME + suffix;
	}

	public static String getWorkingDir() {
		return WORKING_DIR;
	}

	public static String getCodefaceDir() {
		return getWorkingDir() + File.separator + "codeface/";
	}

	public static String getReposDir() {
		return getCodefaceDir() + "repos/";
	}

	public static String getConfigDir() {
		return getCodefaceDir() + "conf/";
	}

	public static String getLogDir() {
		return getWorkingDir() + File.separator + "rc_logs/";
	}

}
