package crawler;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import db.RecipePersister;
import recipe.Recipe;

public class CategoryCrawler implements Runnable {
	private String category;
	private RecipesCrawler crawler = new RecipesCrawler();

	public CategoryCrawler(String category) {
		this.category = category;
	}

	@Override
	public void run() {
		RecipePersister rp = new RecipePersister("jdbc:mysql://localhost:3306/recipes?useSSL=false", "root", "123456");
		try {
			rp.persistListOfRecipes(getAllPagesAtCategory(category)
										.stream()
										.map(page -> getAllRecipesAtPage(page))
										.map(recipesAtPage -> recipesAtPage.stream()
																		   .map(recipeLink -> getSingleRecipe(recipeLink))
																		   .collect(Collectors.toList()))
										.flatMap(listOfRecipes -> listOfRecipes.stream())
										.collect(Collectors.toList()));
		} catch (SQLException e) {
			e.printStackTrace();
		}			
	}
	
	private List<String> getAllPagesAtCategory(String category) {
		try {
			return crawler.getAllPagesAtCategory(category);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private Recipe getSingleRecipe(String recipeLink) {
		try {
			return crawler.getSingleRecipe(recipeLink);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private List<String> getAllRecipesAtPage(String page) {
		try {
			return crawler.getAllRecipesAtPage(page);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
