import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import ilog.concert.IloException;

public class MainBRKGA {

	public static void main(String[] args) throws IloException, IOException {
		Map<Double, Item> items = readItems();
		List<Order> orders = readOrders(items);
		int choice_D2_VBO = 1; // TODO: 1 if DFTRC-2^2 is used, 2 if DFTRC-2-VBO is used

//		double[] lowerBound = new double[orders.size()];
//		for(int i = 0; i < orders.size(); i++) {
//			double lowerbound = LowerBoundModel.setCoveringLB(orders.get(i), items);
//			lowerBound[i] = lowerbound;
//		}
//		
//		File info = new File("GA_solution_info.txt"); 
//		File sol = new File("GA_solution.txt");
//		PrintWriter outInfo = new PrintWriter(info);
//		PrintWriter outSol = new PrintWriter(sol);
//		outInfo.println("instance\tnumCrates\tadjNumCrates\tnumCratesLB\tavFillRate\tavWeight\trunTime");
//		int totalNumBins = 0;
//		Crate crate = new Crate();
//		for (int i=0; i < orders.size(); i++) { // TODO: orders.size
//			System.out.println("Order nr " + i);
//			long startTime = System.nanoTime();
//			Chromosome chrom = BRKGA.solve(orders.get(i), lowerBound[i], choice_D2_VBO);
//			long endTime   = System.nanoTime();
//			long totalTime = (endTime - startTime)/1000000;
//			totalNumBins += chrom.getNumCrates();
//			double avFillRate = 0.0;
//			double avWeight = 0.0;
//			for (Item it : orders.get(i).getItems()) {
//				avFillRate += it.getVolume();
//				avWeight += it.getWeight();
//			}
//			avFillRate = avFillRate/(chrom.getNumCrates()*crate.getVolume());
//			avWeight = avWeight/chrom.getNumCrates();
//			outInfo.println(i + "\t" + chrom.getNumCrates() + "\t" + chrom.getFitness() + "\t" + lowerBound[i] + "\t" + avFillRate + "\t" + avWeight + "\t" + totalTime);
//			outSol.println("Order: " + i);
//			outSol.println("Crates: " + chrom.getNumCrates());
//			for (int k=0; k < chrom.getOpenCrates().size(); k++) {
//				List<Integer> openCrate = chrom.getOpenCrates().get(k);
//				String output = k + "\t";
//				for (int j=0; j < openCrate.size(); j++) {
//					if (openCrate.get(j) == 1)
//						output += (int) chrom.getItems().get(j).getItemId() + " ";
//				}
//				outSol.println(output);
//			}
//		}
//		outInfo.close();
//		outSol.close();
//		System.out.println("Total Number of bins bitchessszs " + totalNumBins);
		
		int instance = 88;
		double lowerbound = LowerBoundModel.setCoveringLB(orders.get(instance), items);
		System.out.println(lowerbound);
		Chromosome chrom = BRKGA.solve(orders.get(instance), lowerbound, choice_D2_VBO);
		printCrate(orders.get(instance), chrom);
		
//		double avWeight = 0.0;
//		double avVolume = 0.0;
//		for (int i=0; i < orders.size(); i++) {
//			List<Item> order = orders.get(i).getItems();
//			for (int j=0; j < order.size(); j++) {
//				avWeight += order.get(j).getWeight();
//				avVolume += order.get(j).getVolume();
//			}
//		}
//		avWeight = avWeight/1907;
//		avVolume = avVolume/1907;
//		System.out.println("Average volume = " + avVolume + ", average weight = " + avWeight);
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
