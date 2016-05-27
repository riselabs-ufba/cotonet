/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Alcemir R. Santos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package br.com.riselabs.cotonet.util;

import java.io.File;

/**
 * @author Alcemir R. Santos
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

	public static File getScriptsDir() {
		return getDir("scripts");
	}
	private static File getDir(String child) {
		File dir;
		if ((dir = new File(new File(APP_DIR, "cotonet-files"), child)).exists()) {
			dir.mkdirs();
		}
		return dir;
	}

}
