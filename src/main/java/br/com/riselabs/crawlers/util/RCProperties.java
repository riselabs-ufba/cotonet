/**
 * 
 */
package br.com.riselabs.crawlers.util;

/**
 * @author alcemir
 *
 */
public final class RCProperties {

	public static final String USER_HOME = System.getProperty("user.home");
	public static String WORKING_DIR = USER_HOME;
	public static final String CODEFACE_DIR = WORKING_DIR+"/codeface/";
	public static final String REPOS_DIR = CODEFACE_DIR	+ "repos/";
	public static final String CONFIG_DIR = CODEFACE_DIR + "conf/";
	
	
	public static void setWorkingDir(String suffix) {
		WORKING_DIR = USER_HOME+suffix;
	}
	
}
