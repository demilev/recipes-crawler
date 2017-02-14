package recipe;

import java.util.List;

public class Recipe {

	private List<Product> products;
	private String description;
	private String name;

	public Recipe(String recepieName, List<Product> recepieProducts, String description) {
		products = recepieProducts;
		this.description = description;
		name = recepieName;
	}

	public List<Product> getProducts() {
		return products;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}
