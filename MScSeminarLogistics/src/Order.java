import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
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
	public Order(List<Item> item, double orderId) {
		this.items = new ArrayList<>();
		this.items.addAll(item);
		this.orderId = orderId;
	}
	
	/**
	 * Get the list of items in the order
	 * @return itemlist
	 */
	public List<Item> getItems() {
		return this.items;
	}
	
	/**
	 * method to get the id of the order
	 * @return id
	 */
	public double getId() {
		return this.orderId;
	}
	
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
				if(orderId == ordernumber) { // Als je nogsteeds in dezelfde order zit
					for(int i = 0; i < quantity; i++){
						itemsInOrder.add(items.get(itemId));
					}
				}
				else { // Als de eerste regel van een nieuwe order
					// Dan eerst de oude order afsluiten
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
