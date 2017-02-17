package db;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import recipe.Product;
import recipe.Recipe;

public class RecipePersister {
	private static final String MAPING_INSERT_QUERY = "INSERT INTO recipes_to_products (recipe_id,product_id) VALUES (?,?)";
	private static final String RECIPE_INSERT_QUERY = "INSERT INTO recipes (name,description) VALUES (?,?)";
	private static final String PRODUCT_INSERT_QUERY = "INSERT INTO products (name) VALUES (?)";
	private String dbURL;
	private String username;
	private String password;

	public RecipePersister(String dbURL, String username, String password) {
		this.dbURL = dbURL;
		this.username = username;
		this.password = password;
	}

	public void persistListOfRecipes(List<Recipe> listOfRecipes) throws SQLException {
		Connection conn = DriverManager.getConnection(dbURL, username, password);
		PreparedStatement productInsertStm = conn.prepareStatement(PRODUCT_INSERT_QUERY);
		PreparedStatement recipeInsertStm = conn.prepareStatement(RECIPE_INSERT_QUERY);
		PreparedStatement mapingInsertStm = conn.prepareStatement(MAPING_INSERT_QUERY);

		for (Recipe recipe : listOfRecipes) {
			addRecipeBatch(recipeInsertStm, recipe);
			addProductsBatch(productInsertStm, recipe.getProducts());
		}

		try {
			recipeInsertStm.executeBatch();
			productInsertStm.executeBatch();
		} catch (BatchUpdateException e) {
		}

		createMapingBatch(listOfRecipes, conn, mapingInsertStm);
		try {
			mapingInsertStm.executeBatch();
		} catch (BatchUpdateException e) {
		}
		mapingInsertStm.close();
		productInsertStm.close();
		recipeInsertStm.close();
		conn.close();

	}

	private void createMapingBatch(List<Recipe> listOfRecipes, Connection conn, PreparedStatement mapingInsertStm)
			throws SQLException {
		PreparedStatement selectRecipeStm = conn.prepareStatement("SELECT * FROM recipes WHERE name = ?");
		PreparedStatement selectProudctsStm = conn.prepareStatement("SELECT * FROM products WHERE name = ?");

		for (Recipe recipe : listOfRecipes) {
			int recipeID = getRecipeID(recipe, selectRecipeStm);
			List<Integer> productsIDs = getProductsIDs(recipe, selectProudctsStm);
			for (Integer productID : productsIDs) {
				mapingInsertStm.setInt(1, recipeID);
				mapingInsertStm.setInt(2, productID);
				mapingInsertStm.addBatch();
			}

		}
		selectRecipeStm.close();
		selectProudctsStm.close();
	}

	private void addProductsBatch(PreparedStatement productInsertStm, List<Product> products) throws SQLException {
		for (Product product : products) {
			productInsertStm.setString(1, product.getName());
			productInsertStm.addBatch();
		}
	}

	private void addRecipeBatch(PreparedStatement recipeInsertStm, Recipe recipe) throws SQLException {
		recipeInsertStm.setString(1, recipe.getName());
		recipeInsertStm.setString(2, recipe.getDescription());
		recipeInsertStm.addBatch();
	}

	private List<Integer> getProductsIDs(Recipe recipe, PreparedStatement selectProudctsStm) throws SQLException {
		List<Integer> productsIDs = new LinkedList<>();
		ResultSet rs = null;
		for (Product product : recipe.getProducts()) {
			selectProudctsStm.setString(1, product.getName());
			rs = selectProudctsStm.executeQuery();
			if (rs.next())
				productsIDs.add(rs.getInt("product_id"));
		}
		rs.close();
		return productsIDs;
	}

	private int getRecipeID(Recipe recipe, PreparedStatement selectRecipeStm) throws SQLException {
		int result = 0;
		selectRecipeStm.setString(1, recipe.getName());
		ResultSet rs = selectRecipeStm.executeQuery();
		if (rs.next()) {
			result = rs.getInt("recipe_id");
		}
		rs.close();
		return result;
	}
}
