import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import ilog.concert.IloException;

public class MultiThreadThing extends Thread {

	private int threadNumber;
	private Order order;
	private Map<Double, Item> items;
	public MultiThreadThing(int threadNumber, Order order) throws FileNotFoundException {
		this.threadNumber = threadNumber;
		this.order = order;
	}
	
	
	@Override
	public void run(){
		Map<Double, Item> items = null;
		try {
			items = MainBRKGA.readItems();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int choice_D2_VBO = 1;
		double lowerbound = 0;
		try {
			lowerbound = LowerBoundModel.setCoveringLB(this.order, items);
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(lowerbound);
		Chromosome chrom = BRKGA.solve(this.order, lowerbound, choice_D2_VBO);
	}
	@SuppressWarnings("unused")
	private static void printCrate(Order order, Chromosome chrom) throws FileNotFoundException {
		List<Item> items = chrom.getItems();
		File crate = new File("items_crate.txt");
		PrintWriter out = new PrintWriter(crate);
		out.println("id\tx\ty\tz\tw\tl\th");
		int crateNumber = 0;
		for (Item item : items) {
			if (item.getCrateIndex() == crateNumber) {
				String s = (int) item.getItemId() + "\t" + (int) item.getInsertedX() + "\t" + (int) item.getInsertedY() + "\t" + (int) item.getInsertedZ() + "\t" + (int) item.getWidth() + "\t" + (int) item.getLength() + "\t" + (int) item.getHeight();
				out.println(s);
			}
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


