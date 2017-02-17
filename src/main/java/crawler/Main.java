package crawler;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
	private static final int NUMBER_OF_THREADS = 20;

	public static void main(String[] args) throws InterruptedException, IOException {
		long start = System.currentTimeMillis();
		
		ExecutorService crawler = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
		
		new RecipesCrawler().getCategories().forEach(category -> crawler.execute(new CategoryCrawler(category)));
		crawler.shutdown();
		crawler.awaitTermination(5, TimeUnit.HOURS);
		
		long end = System.currentTimeMillis();
		System.out.println("Finished for " + (end - start)/10000 + " minutes.");
	}
}
