package crawler;

public class QuantityTrimer {
	public static String trimQuantity(String productName) {
		String result = removeNumbers(productName);
		result = removeParanthesis(result);
		result = removeUnit(result);

		return result;
	}

	private static String removeParanthesis(String productName) {
		return productName.replaceAll("\\([^\\)]*\\)\\s*", "").replaceAll("\\s*\\([^\\)]*\\)", "");
	}

	private static String removeUnit(String productName) {
		if (productName.matches(
				"(бр.|глава|глави|връзка|кг|с.л.|стрък|стръка|ч.л.|ч.ч.|шепа|шепи|кубчета|г|кг|скилидка|скилидки|л|мл|г.|кг.|л.|мл.|сл|чл|чч)\\s+.*"))
			return productName.replaceFirst(
					"(бр.|глава|глави|връзка|кг|с.л.|стрък|стръка|ч.л.|ч.ч.|шепа|шепи|кубчета|г|кг|скилидка|скилидки|л|мл|г.|кг.|л.|мл.|сл|чл|чч)\\s+",
					"");
		return productName;
	}

	private static String removeNumbers(String productName) {

		return productName
				.replaceAll("\\d+\\s*(-|" + ((char) 8211) + ")\\s*\\d+\\s*|\\d+\\.\\d+\\s*|\\d+/\\d+\\s*|\\d+\\s*", "");
	}

	public static void main(String[] args) {
		System.out.println(trimQuantity("3 с.л. кисела сметана"));
	}
}
