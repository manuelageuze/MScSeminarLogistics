import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

public class Main {

	public static void main(String[] args) throws FileNotFoundException {
		
		Map<Double, Item> items = readItems();
		List<Order> orders = readOrders(items);

	}

	private static Map readItems() throws FileNotFoundException {
		File itemFile = new File("items.csv");
		Map<Double,Item> items = Item.readItem(itemFile);
		return items;	
	}
	
	private static List<Order> readOrders(Map<Double,Item> items) throws FileNotFoundException {
		File orderFile = new File("orders.csv");
		List<Order> orders = Order.readOrder(orderFile, items);
		return orders;	
		
	}
		
}
