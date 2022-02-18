import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ilog.concert.IloException;

public class MainBRKGA {

	static final int MAX_T = 8; 

	public static void main(String[] args) throws IloException, IOException, InterruptedException {
		Map<Double, Item> items = readItems();
		List<Order> orders = readOrders(items);
		int choiceSplit = 1; // Choice for order splitting or not: 1 for no splitting, 2 for splitting

		double[] lowerBound = new double[orders.size()];
		for(int i=0; i < orders.size(); i++) {
			double lowerbound = LowerBoundModel.setCoveringLB(orders.get(i), items);
			lowerBound[i] = lowerbound;
		}
		// Order splitting
		if (choiceSplit == 2) splitOrders(orders, lowerBound);

		long startTime = System.nanoTime();
		// Create tasks
		List<Runnable> runnables = new ArrayList<>();
		for(int i=0; i < orders.size(); i++) {
			Runnable runnable = new MultiThread(orders.get(i), lowerBound[i], i);
			runnables.add(runnable);
		}
		// Creates a thread pool with MAX_T no. of threads as the fixed pool size(Step 2)
		ExecutorService pool = Executors.newFixedThreadPool(MAX_T); 
		// Passes the MultiThread objects to the pool to execute (Step 3)
		for(Runnable runnable: runnables) {
			pool.execute(runnable);
		}
		awaitTerminationAfterShutdown(pool);
		//pool.shutdown();
		long endTime = System.nanoTime();
		long totalTime = (endTime - startTime)/1000000000; // seconds

		// Merge orders, if split
		switch (choiceSplit) {
		case 1: writeSolNoSplit(orders, lowerBound, runnables);
		break;
		case 2: writeSolSplit(orders, lowerBound, runnables);
		break;
		}

		System.out.println("Total runtime MULTITHREAD: " + totalTime + " s");

		// 		int instance = 88;
		//		double lowerbound = LowerBoundModel.setCoveringLB(orders.get(instance), items);
		//		System.out.println(lowerbound);
		//		Chromosome chrom = BRKGA.solve(orders.get(instance), lowerbound, choice_D2_VBO);
		//		printCrate(orders.get(instance), chrom);

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

	private static void splitOrders(List<Order> orders, double[] lowerBound) {
		double LBBound = 3.0;
		int numItemsBound = 50;
		for (int i=0; i < orders.size(); i++) {
			Order order = orders.get(i);
			if (lowerBound[i] >= LBBound && order.getItems().size() >= numItemsBound) {
				// Make 2 new orders with each half of the items
				int numItemsSplit = order.getItems().size()/2;
				Order newOrder1 = new Order(order.getItems().subList(0, numItemsSplit), order.getOrderId()+0.1); // Order x.1
				Order newOrder2 = new Order(order.getItems().subList(numItemsSplit, order.getItems().size()), order.getOrderId()+0.2); // Order x.2
				orders.set(i, newOrder1);
				orders.add(i+1, newOrder2);
				i++;
			}
		}
	}

	private static void writeSolNoSplit(List<Order> orders, double[] lowerBound, List<Runnable> runnables) throws FileNotFoundException {
		File info = new File("GA_solution_info_multi.txt"); 
		File sol = new File("GA_solution_multi.txt");
		PrintWriter outInfo = new PrintWriter(info);
		PrintWriter outSol = new PrintWriter(sol);
		outInfo.println("instance\tnumCrates\tadjNumCrates\tnumCratesLB\tavFillRate\tavWeight");
		int totalNumBins = 0;
		Crate crate = new Crate();
		for (int i=0; i < orders.size(); i++) {
			Chromosome chrom = ((MultiThread) runnables.get(i)).getChromosome();
			totalNumBins += chrom.getNumCrates();
			double avFillRate = 0.0;
			double avWeight = 0.0;
			for (Item it : orders.get(i).getItems()) {
				avFillRate += it.getVolume();
				avWeight += it.getWeight();
			}
			avFillRate = avFillRate/(chrom.getNumCrates()*crate.getVolume());
			avWeight = avWeight/chrom.getNumCrates();
			outInfo.println(i + "\t" + chrom.getNumCrates() + "\t" + chrom.getFitness() + "\t" + lowerBound[i] + "\t" + avFillRate + "\t" + avWeight + "\t");
			outSol.println("Order: " + i);
			outSol.println("Crates: " + chrom.getNumCrates());
			for (int k=0; k < chrom.getOpenCrates().size(); k++) {
				List<Integer> openCrate = chrom.getOpenCrates().get(k);
				String output = k + "\t";
				for (int j=0; j < openCrate.size(); j++) {
					if (openCrate.get(j) == 1)
						output += (int) chrom.getItems().get(j).getItemId() + " ";
				}
				outSol.println(output);
			}
		}
		outInfo.close();
		outSol.close();
		System.out.println("Total Number Of Bins MULTITHREAD: " + totalNumBins);
	}

	private static void writeSolSplit(List<Order> orders, double[] lowerBound, List<Runnable> runnables) throws FileNotFoundException {
		File info = new File("GA_solution_info_multi.txt"); 
		File sol = new File("GA_solution_multi.txt");
		PrintWriter outInfo = new PrintWriter(info);
		PrintWriter outSol = new PrintWriter(sol);
		outInfo.println("instance\tnumCrates\tadjNumCrates\tnumCratesLB\tavFillRate\tavWeight");
		int totalNumBins = 0;
		Crate crate = new Crate();
		for (int i=0; i < orders.size(); i++) {
			Chromosome chrom = ((MultiThread) runnables.get(i)).getChromosome();
			Chromosome chrom2 = null;
			int numCrates = chrom.getNumCrates();
			double fitness = chrom.getFitness();
			if (chrom.getOrderId() - (int) chrom.getOrderId() > 0) { // Check if order has been split
				chrom2 = ((MultiThread) runnables.get(i+1)).getChromosome();
				numCrates += chrom2.getNumCrates();
				fitness = fitness - (int) fitness > chrom2.getFitness() - (int) chrom2.getFitness() ? chrom2.getFitness() : fitness;
			}
			totalNumBins += numCrates;
			double avFillRate = 0.0;
			double avWeight = 0.0;
			for (Item it : orders.get(i).getItems()) {
				avFillRate += it.getVolume();
				avWeight += it.getWeight();
			}
			if (chrom2 != null) {
				for (Item it : orders.get(i+1).getItems()) {
					avFillRate += it.getVolume();
					avWeight += it.getWeight();
				}
			}
			avFillRate = avFillRate/(numCrates*crate.getVolume());
			avWeight = avWeight/numCrates;
			outInfo.println(i + "\t" + numCrates + "\t" + fitness + "\t" + lowerBound[i] + "\t" + avFillRate + "\t" + avWeight + "\t");
			outSol.println("Order: " + (double) ((int) chrom.getOrderId()));
			outSol.println("Crates: " + numCrates);
			for (int k=0; k < chrom.getOpenCrates().size(); k++) {
				List<Integer> openCrate = chrom.getOpenCrates().get(k);
				String output = k + "\t";
				for (int j=0; j < openCrate.size(); j++) {
					if (openCrate.get(j) == 1)
						output += (int) chrom.getItems().get(j).getItemId() + " ";
				}
				outSol.println(output);
			}
			if (chrom2 != null) {
				for (int k=0; k < chrom2.getOpenCrates().size(); k++) {
					List<Integer> openCrate = chrom2.getOpenCrates().get(k);
					String output = k+chrom.getNumCrates() + "\t";
					for (int j=0; j < openCrate.size(); j++) {
						if (openCrate.get(j) == 1)
							output += (int) chrom2.getItems().get(j).getItemId() + " ";
					}
					outSol.println(output);
				}
			}
		}
		outInfo.close();
		outSol.close();
		System.out.println("Total Number Of Bins MULTITHREAD: " + totalNumBins);
	}

	public static void awaitTerminationAfterShutdown(ExecutorService threadPool) {
		threadPool.shutdown();
		try {
			if (!threadPool.awaitTermination(1, TimeUnit.HOURS)) {
				threadPool.shutdownNow();
			}
		} catch (InterruptedException ex) {
			threadPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
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
	public static Map<Double, Item> readItems() throws FileNotFoundException {
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
	public static List<Order> readOrders(Map<Double, Item> items) throws FileNotFoundException {
		File orderFile = new File("orders.csv");
		List<Order> orders = Order.readOrder(orderFile, items);
		return orders;
	}

}
