package crawler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import db.RecipePersister;
import recipe.Recipe;

public class CategoryCrawler implements Runnable {
	private String category;

	public CategoryCrawler(String category) {
		this.category = category;
	}

	@Override
	public void run() {
		RecipesCrawler crawler = new RecipesCrawler();
		RecipePersister rp = new RecipePersister("jdbc:mysql://localhost:3306/recipes?useSSL=false", "root", "123456");
		try {
			List<String> pages = crawler.getAllPagesAtCategory(category);
			for (String page : pages) {
				List<String> recipesAtPage = crawler.getAllRecipesAtPage(page);
				for (String recipe : recipesAtPage) {
					rp.persistRecipe(crawler.getSingleRecipe(recipe));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
