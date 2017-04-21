package crawler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import recipe.Product;
import recipe.Recipe;

public class RecipesCrawler {
	public static final String RECIPES_LINK = "http://www.receptite.com/%D0%BA%D0%B0%D1%82%D0%B0%D0%BB%D0%BE%D0%B7%D0%B8-%D1%81-%D1%80%D0%B5%D1%86%D0%B5%D0%BF%D1%82%D0%B8";
	private static final CharSequence RECEPTI_ZA = "%D1%80%D0%B5%D1%86%D0%B5%D0%BF%D1%82%D0%B8-%D0%B7%D0%B0";

	public List<String> getCategories() throws IOException {

		return getDocument(RECIPES_LINK)
				.getElementsByAttributeValue("class", "menucolumn recepticol2")
					.stream()
					.map(element -> element.select("a[href]"))
					.map(links -> links.stream()
								   	   .filter(link -> link.getElementsByAttributeValue("class", "notlink").size() == 0)
								   	   .map(link -> link.attr("href"))
								   	   .filter(link -> !link.contains(RECEPTI_ZA))
								   	   .collect(Collectors.toList()))
					.flatMap(List::stream)
					.collect(Collectors.toList());
		
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
		
		return getDocument(link)
				.getElementsByAttributeValue("class", "pages_bar")
					.select("a[href]")
					.stream()
					.filter(page -> !page.text().isEmpty())
					.mapToInt(page -> Integer.parseInt(page.text()))
					.max().getAsInt(); 
	}

	private String getNextPage(String currentPage) throws IOException {
		Document doc = getDocument(currentPage);

		Elements pageBars = doc.getElementsByAttributeValue("class", "pages_bar");
		Elements pages = pageBars.select("a[href]");
		return pages.get(pages.size() - 1).attr("href");
	}

	public List<String> getAllRecipesAtPage(String page) throws IOException {
		
		return getDocument(page)
				.getElementsByAttributeValue("class", "zagS")
					.stream()
					.map(recipe->recipe.getElementsByAttribute("href").attr("href"))
					.collect(Collectors.toList());
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
				products.add(new Product(QuantityTrimer.trimQuantity(product.text())));
		}

		Elements instructionsElements = doc.getElementsByAttributeValue("itemprop", "recipeInstructions");
		recipeInstructions.append("\nНачин на приготвяне:\n\n" + instructionsElements.text());

		return new Recipe(recipeName, products, recipeInstructions.toString());
	}

	private Document getDocument(String link) throws IOException {
		Document doc = Jsoup.connect(link)
				.userAgent(
						"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
				.timeout(10000)
				.get();
		return doc;
	}

	public static void main(String[] args) throws IOException {
		new RecipesCrawler().getAllPagesAtCategory("http://www.receptite.com/%D1%8F%D1%81%D1%82%D0%B8%D1%8F-%D1%81-%D0%BC%D0%B5%D1%81%D0%BE/%D0%B0%D0%B3%D0%BD%D0%B5%D1%88%D0%BA%D0%BE").forEach(System.out::println);
	}

}
