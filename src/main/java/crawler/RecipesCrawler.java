package crawler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

import recipe.Product;
import recipe.Recipe;

public class RecipesCrawler {
	private static final int NUMBER_OF_THREADS = 5;
	public static final String RECIPES_LINK = "http://www.receptite.com/%D0%BA%D0%B0%D1%82%D0%B0%D0%BB%D0%BE%D0%B7%D0%B8-%D1%81-%D1%80%D0%B5%D1%86%D0%B5%D0%BF%D1%82%D0%B8";

	public List<String> getCategories() throws IOException {
		List<String> categoriesLinks = new LinkedList<>();

		Document doc = getDocument(RECIPES_LINK);

		Elements categories = doc.getElementsByAttributeValue("class", "menucolumn recepticol2");
		for (Element element : categories) {
			Elements links = element.select("a[href]");
			for (Element link : links) {
				if (link.getElementsByAttributeValue("class", "notlink").size() == 0)
					categoriesLinks.add(link.attr("href"));
			}
		}
		return categoriesLinks;
	}

	public List<String> getAllPagesAtCategory(String category) throws IOException {
		List<String> pages = new LinkedList<>();
		if (isSinglePage(category)) {
			pages.add(category);
			return pages;
		}
		Integer maxPageNumber = getMaxPage(category);
		Integer currentPageNumber = 1;
		String currentPage = category;
		while (currentPageNumber < maxPageNumber) {
			pages.add(currentPage);
			currentPage = getNextPage(currentPage);
			currentPageNumber++;
		}
		pages.add(currentPage);
		return pages;
	}

	private boolean isSinglePage(String page) throws IOException {
		Document doc = getDocument(page);

		Elements pageBars = doc.getElementsByAttributeValue("class", "pages_bar");
		if (pageBars.get(0).text().isEmpty())
			return true;

		return false;

	}

	private Integer getMaxPage(String link) throws IOException {
		Document doc = getDocument(link);

		Elements pageBars = doc.getElementsByAttributeValue("class", "pages_bar");
		Elements pages = pageBars.select("a[href]");
		Integer max = 0;
		for (Element page : pages) {
			if (!page.text().isEmpty() && Integer.parseInt(page.text()) > max)
				max = Integer.parseInt(page.text());
		}
		return max;
	}

	private String getNextPage(String currentPage) throws IOException {
		Document doc = getDocument(currentPage);

		Elements pageBars = doc.getElementsByAttributeValue("class", "pages_bar");
		Elements pages = pageBars.select("a[href]");
		return pages.get(pages.size() - 1).attr("href");
	}

	public List<String> getAllRecipesAtPage(String page) throws IOException {
		List<String> recipes = new LinkedList<>();
		Document doc = getDocument(page);
		Elements recipesElements = doc.getElementsByAttributeValue("class", "zagS");
		for (Element recipe : recipesElements) {
			recipes.add(recipe.getElementsByAttribute("href").attr("href"));
		}
		return recipes;
	}

	public Recipe getSingleRecipe(String recipeLink) throws IOException {
		Document doc = getDocument(recipeLink);
		String recipeName = doc.title().substring(0, doc.title().indexOf('|') - 1);

		List<Product> products = new LinkedList<>();

		Elements productElements = doc.getElementsByAttributeValue("itemprop", "ingredients");

		StringBuilder recipeInstructions = new StringBuilder();
		recipeInstructions.append("Необходими продукти :\n\n");

		for (Element product : productElements) {
			recipeInstructions.append(product.text()).append("\n");
			if (product.getElementsByAttributeValue("class", "prod_za").size() == 0)
				products.add(new Product(product.text()));
		}

		Elements instructionsElements = doc.getElementsByAttributeValue("itemprop", "recipeInstructions");
		recipeInstructions.append("\nНачин на приготвяне:\n\n" + instructionsElements.text());

		return new Recipe(recipeName, products, recipeInstructions.toString());
	}

	private Document getDocument(String link) throws IOException {
		Document doc = Jsoup.connect(link)
				.userAgent(
						"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
				.get();
		return doc;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		long start = System.currentTimeMillis();
		// Thread[] threads = new Thread[5];
		String[] categories = {
				"http://www.receptite.com/%D1%8F%D1%81%D1%82%D0%B8%D1%8F/%D1%81%D0%BF%D0%B0%D0%BD%D0%B0%D0%BA",
				"http://www.receptite.com/%D1%81%D0%B0%D0%BB%D0%B0%D1%82%D0%B8/%D1%81%D0%B0%D0%BB%D0%B0%D1%82%D0%B8-%D1%81-%D0%BA%D0%BE%D0%BB%D0%B1%D0%B0%D1%81%D0%B8",
				"http://www.receptite.com/%D0%BF%D1%80%D0%B5%D0%B4%D1%8F%D1%81%D1%82%D0%B8%D1%8F/%D1%81%D0%BE%D0%BB%D0%B5%D0%BD%D0%B8-%D0%BF%D0%B0%D0%BB%D0%B0%D1%87%D0%B8%D0%BD%D0%BA%D0%B8",
				"http://www.receptite.com/%D1%8F%D1%81%D1%82%D0%B8%D1%8F/%D0%BF%D0%B0%D1%82%D0%BB%D0%B0%D0%B4%D0%B6%D0%B0%D0%BD%D0%B8",
				"http://www.receptite.com/%D1%8F%D1%81%D1%82%D0%B8%D1%8F-%D0%B1%D0%B5%D0%B7-%D0%BC%D0%B5%D1%81%D0%BE/%D1%82%D0%B8%D0%BA%D0%B2%D0%B8%D1%87%D0%BA%D0%B8" };
		ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

		for (int i = 0; i < 5; i++) {
			executor.execute(new CategoryCrawler(categories[i]));
		}
		executor.shutdown();
		executor.awaitTermination(100, TimeUnit.SECONDS);

		// for (int i = 0; i < 5; i++) {
		// threads[i].join();
		// }
		//
		long end = System.currentTimeMillis();
		System.out.println("Finished for : " + (end - start));
		// getSingleRecipe(
		// "http://www.receptite.com/%D1%80%D0%B5%D1%86%D0%B5%D0%BF%D1%82%D0%B0/%D1%81%D0%BC%D0%BE%D0%BA%D0%B8%D0%BD%D0%BE%D0%B2-%D0%BA%D0%B5%D0%BA%D1%81-%D1%81-%D0%B2%D0%B8%D0%BD%D0%B5%D0%BD-%D1%81%D0%B8%D1%80%D0%BE%D0%BF");
		// getAllRecipesAtPage(
		// "http://www.receptite.com/%D1%8F%D1%81%D1%82%D0%B8%D1%8F-%D1%81-%D0%BC%D0%B5%D1%81%D0%BE/%D0%BF%D0%B8%D0%BB%D0%B5%D1%88%D0%BA%D0%BE/3");
		// getNextPage("http://www.receptite.com/%D1%8F%D1%81%D1%82%D0%B8%D1%8F-%D1%81-%D0%BC%D0%B5%D1%81%D0%BE/%D0%BF%D0%B8%D0%BB%D0%B5%D1%88%D0%BA%D0%BE/72");
		// System.out.println(getMaxPage(
		// "http://www.receptite.com/%D0%B7%D0%B8%D0%BC%D0%BD%D0%B8%D0%BD%D0%B0/%D0%BA%D0%BE%D0%BD%D1%84%D0%B8%D1%82%D1%8E%D1%80"));
		// // getAllPagesAtCategory(
		// "http://www.receptite.com/%D0%B7%D0%B8%D0%BC%D0%BD%D0%B8%D0%BD%D0%B0/%D0%BA%D0%BE%D0%BD%D1%84%D0%B8%D1%82%D1%8E%D1%80")
		// .forEach(System.out::println);
		// List<String> categoriesLinks = new LinkedList<>();
		//
		// Document doc = Jsoup.connect(RECIPES_LINK)
		// .userAgent(
		// "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like
		// Gecko) Chrome/19.0.1042.0 Safari/535.21")
		// .timeout(10000).get();
		//
		// Elements elementsByAttributeValue =
		// doc.getElementsByAttributeValue("class", "menucolumn recepticol2");
		// for (Element element : elementsByAttributeValue) {
		// Elements links = element.select("a[href]");
		// for (Element link : links) {
		// if (link.getElementsByAttributeValue("class", "notlink").size() == 0)
		// categoriesLinks.add(link.attr("href"));
		// }
		// }
		// categoriesLinks.forEach(System.out::println);
	}

}
