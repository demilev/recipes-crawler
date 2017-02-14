package crawler;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		Thread crawler = new Thread(new CategoryCrawler(
				"http://www.receptite.com/%D1%81%D1%83%D0%BF%D0%B8/%D1%81%D1%83%D0%BF%D0%B8-%D1%81-%D0%BA%D0%BE%D0%BB%D0%B1%D0%B0%D1%81%D0%B8"));
		long start = System.currentTimeMillis();
		crawler.start();
		crawler.join();
		long end = System.currentTimeMillis();
		System.out.println("Finished for " + (end - start));
	}
}
