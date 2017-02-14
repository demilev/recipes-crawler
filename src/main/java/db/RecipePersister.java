package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import recipe.Product;
import recipe.Recipe;

public class RecipePersister {
	private static final String MAPING_INSERT_QUERY = "INSERT INTO recipes_to_products (recipe_id,product_id) "
			+ "VALUES (?,?)";
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

	public void persistRecipe(Recipe recipe) throws SQLException {
		Connection conn = DriverManager.getConnection(dbURL, username, password);
		PreparedStatement stm = conn.prepareStatement(PRODUCT_INSERT_QUERY);
		for (Product product : recipe.getProducts()) {
			persistProduct(product, stm);
		}
		stm = conn.prepareStatement(RECIPE_INSERT_QUERY);
		persistRecipeInfo(recipe, stm);
		stm.close();
		persistRecipeToMapTable(recipe, conn);
		conn.close();

	}

	private void persistRecipeToMapTable(Recipe recipe, Connection conn) throws SQLException {
		int recipeID = getRecipeID(recipe, conn);
		List<Integer> productsIDs = getProductsIDs(recipe, conn);
		PreparedStatement stm = conn.prepareStatement(MAPING_INSERT_QUERY);
		stm.setInt(1, recipeID);
		for (Integer productID : productsIDs) {
			stm.setInt(2, productID);
			stm.executeUpdate();
		}
		stm.close();
	}

	private List<Integer> getProductsIDs(Recipe recipe, Connection conn) throws SQLException {
		List<Integer> productsIDs = new LinkedList<>();
		PreparedStatement stm = conn.prepareStatement("SELECT * FROM products WHERE name = ? ");
		ResultSet rs = null;
		for (Product product : recipe.getProducts()) {
			stm.setString(1, product.getName());
			rs = stm.executeQuery();
			if (rs.next())
				productsIDs.add(rs.getInt("product_id"));
		}
		rs.close();
		stm.close();
		return productsIDs;
	}

	private int getRecipeID(Recipe recipe, Connection conn) throws SQLException {
		int result = 0;
		Statement stm = conn.createStatement();
		ResultSet rs = stm.executeQuery("SELECT * FROM recipes WHERE name = '" + recipe.getName() + "'");
		if (rs.next()) {
			result = rs.getInt("recipe_id");
		}
		rs.close();
		stm.close();
		return result;
	}

	private void persistRecipeInfo(Recipe recipe, PreparedStatement stm) throws SQLException {
		stm.setString(1, recipe.getName());
		stm.setString(2, recipe.getDescription());
		stm.executeUpdate();
	}

	private void persistProduct(Product product, PreparedStatement stm) throws SQLException {
		stm.setString(1, product.getName());
		try {
			stm.executeUpdate();
		} catch (SQLIntegrityConstraintViolationException e) {

		}
	}

}
