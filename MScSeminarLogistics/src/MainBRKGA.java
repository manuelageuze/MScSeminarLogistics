import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import ilog.concert.IloException;

public class MainBRKGA {

	public static void main(String[] args) throws IloException, IOException {
		Map<Double, Item> items = readItems();
		List<Order> orders = readOrders(items);
		int choice_D2_VBO = 1; // TODO: 1 if DFTRC-2^2 is used, 2 if DFTRC-2-VBO is used

		double[] lowerBound = new double[orders.size()];
		for(int i = 0; i < orders.size(); i++) {
			double lowerbound = LowerBoundModel.setCoveringLB(orders.get(i), items);
			lowerBound[i] = lowerbound;
		}
		
//		File sol = new File("GA_solution.txt");
//		PrintWriter out = new PrintWriter(sol);
//		out.println("instance min_num_crates_GA min_num_crates_LB running_time");
//		for (int i=0; i < orders.size(); i++) {
//			long startTime = System.nanoTime();
//			int numBins = BRKGA.solve(orders.get(i), lowerBound[i], choice_D2_VBO);
//			long endTime   = System.nanoTime();
//			long totalTime = (endTime - startTime)/1000000;
//			out.print(i + " " + numBins + " " + lowerBound[i] + " ");
//			out.println(totalTime);
//		}
//		out.close();
		int instance = 0;
		double lowerbound = LowerBoundModel.setCoveringLB(orders.get(instance), items);
		System.out.println(lowerbound);
		BRKGA.solve(orders.get(instance), lowerbound, choice_D2_VBO);
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
