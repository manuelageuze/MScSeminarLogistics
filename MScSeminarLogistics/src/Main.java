import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import ilog.concert.IloException;

public class Main {

	public static void main(String[] args) throws FileNotFoundException, IloException {
		
		Map<Double, Item> items = readItems();
		List<Order> orders = readOrders(items);
		
		// Compute lower bound and write file
		File sol = new File("lowerBound_solution.txt");
		PrintWriter out = new PrintWriter(sol);
		out.println("instance minimum_number_of_crates");
		for(int i = 0; i < orders.size(); i++) {
			double lowerbound = LowerBoundModel.setCoveringLB(orders.get(i), items);	
			out.print(i + " ");
			out.println(lowerbound);
		}	
		out.close();	
	}
	
	/**
	 * Read items
	 * @return Map containing items
	 * @throws FileNotFoundException
	 */
	private static Map<Double, Item> readItems() throws FileNotFoundException {
		File itemFile = new File("items.csv");
		Map<Double, Item> items = Item.readItems(itemFile);
		return items;	
	}
	
	/**
	 * Read orders
	 * @param items
	 * @return List of orders
	 * @throws FileNotFoundException
	 */
	private static List<Order> readOrders(Map<Double, Item> items) throws FileNotFoundException {
		File orderFile = new File("orders.csv");
		List<Order> orders = Order.readOrder(orderFile, items);
		return orders;
	}
		
}
