/**
 * 
 */
package br.com.riselabs.cotonet.crawler.threads;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author alcemirsantos
 *
 */
public class RCThreadPoolExecutor {
	int poolSize = 12;

	int maxPoolSize = 24;

	long keepAliveTime = 60;

	ThreadPoolExecutor threadPool = null;

	final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();

	public RCThreadPoolExecutor() {
		threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize,
				keepAliveTime, TimeUnit.SECONDS, queue);
	}

	public void runTask(Runnable task) {
		threadPool.execute(task);
	}

	public void shutDown() {
		threadPool.shutdown();
	}


}