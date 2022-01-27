import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Class that is an order, existing of its ID and list of items
 * @author Geuze at al.
 *
 */
public class Order {
	private final List<Item> items;
	private final Double orderId;
	
	/**
	 * Constructor to make an order
	 * @param items
	 * @param orderId
	 */
	public Order(List<Item> items, double orderId) {
		this.items = new ArrayList<>();
		this.items.addAll(items);
		this.orderId = orderId;
	}
	
	/**
	 * Get the list of items in the order
	 * @return items
	 */
	public List<Item> getItems() {
		return this.items;
	}
	
	/**
	 * Method to get the ID of the order
	 * @return orderId
	 */
	public double getOrderId() {
		return this.orderId;
	}
	
	/**
	 * Method to read the order file
	 * @param orderFile
	 * @param items
	 * @return
	 * @throws FileNotFoundException
	 */
	public static List<Order> readOrder(File orderFile, Map<Double, Item> items) throws FileNotFoundException {
		try(Scanner s = new Scanner(orderFile)) {
			List<Order> orders = new ArrayList<>();
			List<Item> itemsInOrder = new ArrayList<>();
			s.useDelimiter(",");
			s.nextLine(); // Skip the description line
			double ordernumber = 0.0;	
			double orderId = 0;
			double itemId = 0;
			double quantity = 0;
			while(s.hasNextLine()) {
				String line = s.nextLine();
				String[] parts = line.split(",");
				orderId = Double.parseDouble(parts[0]);
				itemId = Double.parseDouble(parts[1]);
				quantity = Double.parseDouble(parts[2]);
				if(orderId == ordernumber) { // Same order
					for(int i = 0; i < quantity; i++){
						itemsInOrder.add(items.get(itemId));
					}
				}
				else { // First rule of new order
					// First close previous order
					Order o = new Order(itemsInOrder,orderId - 1.0);
					orders.add(o);
					itemsInOrder.clear();
					// Now start with new order
					ordernumber = orderId;
					for(int i = 0; i < quantity; i++){
						itemsInOrder.add(items.get(itemId));
					}		
				}			
			}
			Order o = new Order(itemsInOrder, orderId);
			orders.add(o);
			return orders;	
		}
	}
	
	
		
	
	
}
