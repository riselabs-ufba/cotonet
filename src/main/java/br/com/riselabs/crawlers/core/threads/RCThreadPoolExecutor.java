/**
 * 
 */
package br.com.riselabs.crawlers.core;

import java.util.concurrent.ArrayBlockingQueue;
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

	final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(
			12);

	public RCThreadPoolExecutor() {
		threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize,
				keepAliveTime, TimeUnit.SECONDS, queue);

	}

	public void runTask(Runnable task) {
		// System.out.println("Task count.."+threadPool.getTaskCount() );
		// System.out.println("Queue Size before assigning the
		// task.."+queue.size() );
		threadPool.execute(task);
		// System.out.println("Queue Size after assigning the
		// task.."+queue.size() );
		// System.out.println("Pool Size after assigning the
		// task.."+threadPool.getActiveCount() );
		// System.out.println("Task count.."+threadPool.getTaskCount() );
		System.out.println("Task count.." + queue.size());
	}

	public void shutDown() {
		threadPool.shutdown();
	}


}