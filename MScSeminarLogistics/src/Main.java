import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import ilog.concert.IloException;

public class Main {

	public static void main(String[] args) throws FileNotFoundException, IloException {
		
		Map<Double, Item> items = readItems();
		List<Order> orders = readOrders(items);
		int choice_D2_VBO = 2; // TODO: 1 if DFTRC-2^2 is used, 2 if DFTRC-2-VBO is used
		
		// Compute lower bound and write file
		//File sol = new File("lowerBound_solution.txt");
		//PrintWriter out = new PrintWriter(sol);
		//out.println("instance minimum_number_of_crates");
		//for(int i = 0; i < orders.size(); i++) {
			//double lowerbound = LowerBoundModel.setCoveringLB(orders.get(i), items);	
			//out.print(i + " ");
			//out.println(lowerbound);
		//}	
		//out.close();
		
		
		//for(int i = 0; i < orders.size();i++) {
			BF bf = new BF(orders.get(0)); //31
			bf.computeBF();
		//}
		BRKGA.solve(orders.get(0), choice_D2_VBO);
		
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
