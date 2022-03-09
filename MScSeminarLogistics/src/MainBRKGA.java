import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ilog.concert.IloException;

public class MainBRKGA {

	static final int MAX_T = 8; 

	public static void main(String[] args) throws IloException, IOException, InterruptedException {
		// Variables, parameters and results
		Map<Double, Item> items = readItems();
		List<Order> orders = readOrders(items);
		Crate crate = new Crate();
		int choiceSplit = 1; // Choice for order splitting or not: 1 for no splitting, 2 for splitting
		int choiceAlgorithm = 3; // Choice for original algorithm: 1 for BRKGA, 2 for BF, 3 for read file
		int choiceAisles = 1; // Choice for incorporating number of aisles or not: 1 for not incorporating, 2 for only incorporating aisles, 3 for incorporating aisles and fill rate
		// Results
		double totalNumCrates = 0.0;
		int totalNumAislesBefore = 0;
		int totalNumAislesAfter = 0;
		double avVolume = 0.0;
		double avWeight = 0.0;
		double avFillRate = 0.0;
		double avWeightRate = 0.0;
		long totalRunTime = 0;
		List<Chromosome> chromosomes = new ArrayList<Chromosome>();

		double[] lowerBound = new double[orders.size()];
		for(int i=0; i < orders.size(); i++) {
			double lowerbound = LowerBoundModel.setCoveringLB(orders.get(i), items);
			lowerBound[i] = lowerbound;
		}

		// Total volume and weight of all orders
		double totalVolume = 0.0;
		double totalWeight = 0.0;
		for (int i=0; i < orders.size(); i++) {
			List<Item> order = orders.get(i).getItems();
			for (int j=0; j < order.size(); j++) {
				totalVolume += order.get(j).getVolume();
				totalWeight += order.get(j).getWeight();	
			}
		}

		// Order splitting
		if (choiceSplit == 2) {
			splitOrders(orders, lowerBound);
			lowerBound = new double[orders.size()];
			for(int i=0; i < orders.size(); i++) {
				double lowerbound = LowerBoundModel.setCoveringLB(orders.get(i), items);
				lowerBound[i] = lowerbound;
			}
		}		

		// Variables
		int[] numCrates = new int[orders.size()];
		int[] numAisles = new int[orders.size()];
		long startTime = System.nanoTime();
		long endTime = 0;
		long totalTime = 0; // seconds
		switch(choiceAlgorithm) {
		case 1: 
			// Create tasks
			List<Runnable> runnables = new ArrayList<>();
			for(int i=0; i < orders.size(); i++) {
				Runnable runnable = new MultiThread(i, orders.get(i), lowerBound[i], 1, 0);
				runnables.add(runnable);
			}
			// Creates a thread pool with MAX_T nr of threads as the fixed pool size
			ExecutorService pool = Executors.newFixedThreadPool(MAX_T); 
			// Passes the MultiThread objects to the pool to execute
			for(Runnable runnable: runnables) {
				pool.execute(runnable);
			}
			awaitTerminationAfterShutdown(pool);
			//pool.shutdown();
			endTime = System.nanoTime();
			totalTime = (endTime - startTime)/1000000000; // seconds

			for (int i=0; i < runnables.size(); i++) {
				Chromosome chrom = ((MultiThread) runnables.get(i)).getChromosome();
				chromosomes.add(chrom);
				numCrates[i] = chrom.getNumCrates();
				totalNumCrates += numCrates[i];
				List<Crate> crates = chrom.getCrates();
				for (Crate cr : crates) {
					numAisles[i] += cr.getShortestPathLength();
					totalNumAislesBefore += cr.getShortestPathLength();
				}
			}
			break;
		case 2: 
			for (int i=0; i < orders.size(); i++) {
				// Get original solution
				List<Order> order = new ArrayList<Order>();
				order.add(orders.get(i));
				FF ff = new FF(orders.get(i));
				List<Crate> crates = ff.computeFF();
				// Save original solution
				numCrates[i] = crates.size();
				totalNumCrates += numCrates[i];
				for (Crate cr : crates) {
					numAisles[i] += cr.getShortestPathLength();
					totalNumAislesBefore += cr.getShortestPathLength();
				}
				chromosomes.add(new Chromosome(new double[0], new double[0], crates, orders.get(i).getItems(), BRKGA.getAdjustedNumberBins(crates), numCrates[i], orders.get(i).getOrderId()));
			}
			endTime = System.nanoTime();
			totalTime = (endTime - startTime)/1000000000; // seconds
			break;
		case 3:
			chromosomes = readFileOriginalGAChrom(new File("GA_aisle.csv"), items);
			for (int i=0; i < chromosomes.size(); i++) {
				List<Crate> crates = chromosomes.get(i).getCrates();
				numCrates[i] = crates.size();
				for (Crate cr : crates) {
					numAisles[i] += cr.getShortestPathLength();
					totalNumAislesBefore += cr.getShortestPathLength();
				}
			}
			endTime = System.nanoTime();
			totalTime = (endTime - startTime)/1000000000; // seconds
			break;
		}

		if (choiceAisles == 2) {
			optAisles(orders, chromosomes, lowerBound, numCrates, numAisles);
			endTime = System.nanoTime();
			totalTime = (endTime - startTime)/1000000000; // seconds
			for (int i=0; i < chromosomes.size(); i++) {
				List<Crate> crates = chromosomes.get(i).getCrates();
				for (Crate cr : crates) {
					numAisles[i] += cr.getShortestPathLength();
					totalNumAislesAfter += cr.getShortestPathLength();
				}
			}
		}

		// Write solution
		//		switch (choiceSplit) {
		//		case 1: totalNumCrates = writeSolNoSplit(orders, lowerBound, chromosomes); // zit nog een fout in
		//		break;
		//		case 2: totalNumCrates = writeSolSplit(orders, lowerBound, chromosomes);
		//		break;
		//		}
		avVolume = totalVolume/totalNumCrates;
		avWeight = totalWeight/totalNumCrates;
		avFillRate = avVolume/crate.getVolume();
		avWeightRate = avWeight/crate.getMaxWeight();
		totalRunTime += totalTime;
		double gap = (totalNumCrates-1748)/1748;

		System.out.println("Total number of crates: " + totalNumCrates + ", gap: " + gap);
		System.out.println("Total num aisles original: " + totalNumAislesBefore);
		System.out.println("Total num aisles after optimizing: " + totalNumAislesAfter);
		System.out.println("Average volume: " + avVolume + ", average fill rate: " + avFillRate);
		System.out.println("Average weight: " + avWeight + ", average weight rate: " + avWeightRate);
		System.out.println("Runtime: " + totalRunTime);

		writeFileCompetition(chromosomes);

		// 		int instance = 88;
		//		double lowerbound = LowerBoundModel.setCoveringLB(orders.get(instance), items);
		//		System.out.println(lowerbound);
		//		Chromosome chrom = BRKGA.solve(orders.get(instance), lowerbound, choice_D2_VBO);
		//		printCrate(orders.get(instance), chrom);

		//		return chromosomes;
	}

	private static void optAisles(List<Order> orders, List<Chromosome> chromosomes, double[] lowerBound, int[] numCrates, int[] numAislesOriginal) throws FileNotFoundException {
		// Create tasks
		List<Runnable> runnables = new ArrayList<>();
		for(int i=0; i < orders.size(); i++) {
			Runnable runnable = new MultiThread(i, orders.get(i), lowerBound[i], 2, numCrates[i]);
			runnables.add(runnable);
		}
		// Creates a thread pool with MAX_T nr of threads as the fixed pool size
		ExecutorService pool = Executors.newFixedThreadPool(MAX_T); 
		// Passes the MultiThread objects to the pool to execute
		for(Runnable runnable: runnables) {
			pool.execute(runnable);
		}
		awaitTerminationAfterShutdown(pool);

		// Check if number of crates is equal to original. If so, take new solution. If not, keep original solution
		int numOrdersImproved = 0;
		for (int i=0; i < runnables.size(); i++) {
			Chromosome chrom = ((MultiThread) runnables.get(i)).getChromosome();
			List<Crate> crates = chrom.getCrates();
			int numAisles = 0; // Number of aisles in chromosome: in 1 order (or order split)
			for (Crate cr : crates) {
				numAisles += cr.getShortestPathLength();
			} 
			if (chrom.getNumCrates() <= numCrates[i] && numAisles < numAislesOriginal[i]) {
				chromosomes.set(i, chrom);
				numOrdersImproved++;
			}
		}
		System.out.println("Number of orders improved: " + numOrdersImproved);
	}

	private static void splitOrders(List<Order> orders, double[] lowerBound) {
		double LBBound = 3.0;
		int numItemsBound = 50;
		int j=0;
		for (int i=0; i < orders.size(); i++) {
			Order order = orders.get(i);
			if (lowerBound[j] >= LBBound && order.getItems().size() >= numItemsBound) {
				// Make 2 new orders with each half of the items
				int numItemsSplit = order.getItems().size()/2;
				Order newOrder1 = new Order(order.getItems().subList(0, numItemsSplit), order.getOrderId()+0.1); // Order x.1
				Order newOrder2 = new Order(order.getItems().subList(numItemsSplit, order.getItems().size()), order.getOrderId()+0.2); // Order x.2
				orders.set(i, newOrder1);
				orders.add(i+1, newOrder2);
				i++;
			}
			j++;
		}
	}

	@SuppressWarnings("unused")
	private static int writeSolNoSplit(List<Order> orders, double[] lowerBound, List<Chromosome> chromosomes) throws FileNotFoundException {
		File info = new File("GA_solution_info_multi.txt"); 
		File sol = new File("GA_solution_multi.txt");
		PrintWriter outInfo = new PrintWriter(info);
		PrintWriter outSol = new PrintWriter(sol);
		outInfo.println("instance\tnumCrates\tadjNumCrates\tnumCratesLB\tavFillRate\tavWeight");
		int totalNumBins = 0;
		Crate crate = new Crate();
		for (int i=0; i < orders.size(); i++) {
			Chromosome chrom = chromosomes.get(i);
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
			for (int k=0; k < chrom.getCrates().size(); k++) {
				String output = k + "\t";
				for (int j=0; j < chrom.getCrates().size(); j++) {
					output += (int) chrom.getCrates().get(k).getItemList().get(j).getItemId() + " ";
				}
				outSol.println(output);
			}
		}
		outInfo.close();
		outSol.close();
		System.out.println("Total Number Of Bins MULTITHREAD: " + totalNumBins);
		return totalNumBins;
	}

	@SuppressWarnings("unused")
	private static int writeSolSplit(List<Order> orders, double[] lowerBound, List<Chromosome> chromosomes) throws FileNotFoundException {
		File info = new File("GA_solution_info_multi.txt"); 
		File sol = new File("GA_solution_multi.txt");
		PrintWriter outInfo = new PrintWriter(info);
		PrintWriter outSol = new PrintWriter(sol);
		outInfo.println("instance\tnumCrates\tadjNumCrates\tnumCratesLB\tavFillRate\tavWeight");
		int totalNumBins = 0;
		Crate crate = new Crate();
		for (int i=0; i < orders.size(); i++) {
			Chromosome chrom = chromosomes.get(i);
			Chromosome chrom2 = null;
			int numCrates = chrom.getNumCrates();
			double fitness = chrom.getFitness();
			if (chrom.getOrderId() - (int) chrom.getOrderId() > 0) { // Check if order has been split
				chrom2 = chromosomes.get(i+1);
				numCrates += chrom2.getNumCrates();
				fitness = fitness - (int) fitness > chrom2.getFitness() - (int) chrom2.getFitness() ? chrom2.getFitness()+chrom.getNumCrates() : fitness+chrom2.getNumCrates();
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
			for (int k=0; k < chrom.getCrates().size(); k++) {
				String output = k + "\t";
				for (int j=0; j < chrom.getCrates().size(); j++) {
					output += (int) chrom.getCrates().get(k).getItemList().get(j).getItemId() + " ";
				}
				outSol.println(output);
			}
			if (chrom2 != null) {
				for (int k=0; k < chrom2.getCrates().size(); k++) {
					String output = k+chrom.getNumCrates() + "\t";
					for (int j=0; j < chrom.getCrates().size(); j++) {
						output += (int) chrom2.getCrates().get(k).getItemList().get(j).getItemId() + " ";
					}
					outSol.println(output);
				}
				i++;
			}
		}
		outInfo.close();
		outSol.close();
		System.out.println("Total Number Of Bins MULTITHREAD: " + totalNumBins);
		return totalNumBins;
	}

	private static void awaitTerminationAfterShutdown(ExecutorService threadPool) {
		threadPool.shutdown();
		try {
			if (!threadPool.awaitTermination(1, TimeUnit.DAYS)) {
				threadPool.shutdownNow();
			}
		} catch (InterruptedException ex) {
			threadPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	public static List<Chromosome> readFileOriginalGAChrom(File file, Map<Double, Item> items) throws FileNotFoundException {
		try(Scanner s = new Scanner(file)) {
			s.nextLine();
			List<Chromosome> chromosomes = new ArrayList<Chromosome>();
			List<Crate> cratesOrder = new ArrayList<Crate>();
			List<Item> itemsCrate = new ArrayList<Item>();
			List<Item> itemsOrder = new ArrayList<Item>();
			int crateNumber = 0;
			int orderNumber = 0;
			Chromosome chrom = new Chromosome(new double[0], new double[0], cratesOrder, itemsOrder, Double.POSITIVE_INFINITY, 0, orderNumber);
			while (s.hasNextLine()) {
				String line = s.nextLine();
				String[] lineArray = line.split(",");
				int curCrateNumber = Integer.parseInt(lineArray[0]);
				int curOrderNumber = Integer.parseInt(lineArray[1]);
				int itemId = Integer.parseInt(lineArray[2]);
				int insertedY = Integer.parseInt(lineArray[3]);
				int insertedX = Integer.parseInt(lineArray[5]);
				int insertedZ = Integer.parseInt(lineArray[7]);
				Item item = items.get((double) itemId);
				item.setInsertedX(insertedX);
				item.setInsertedY(insertedY);
				item.setInsertedZ(insertedZ);
				if (curCrateNumber == crateNumber) { // Same crate, same order
					itemsCrate.add(item);
					itemsOrder.add(item);
				}
				else if (curOrderNumber == orderNumber) { // Different crate, same order
					crateNumber++;
					Crate crate = new Crate(itemsCrate);
					crate.setOrderIndex(curOrderNumber);
					cratesOrder.add(crate);
					itemsCrate = new ArrayList<Item>();
					itemsCrate.add(item);
					itemsOrder.add(item);
				}
				else { // Different order
					crateNumber++;
					orderNumber++;
					Crate crate = new Crate(itemsCrate);
					crate.setOrderIndex(curOrderNumber);
					cratesOrder.add(crate);
					chrom.setFitness(BRKGA.getAdjustedNumberBins(cratesOrder));
					chrom.setNumCrates(cratesOrder.size());
					chromosomes.add(chrom);
					cratesOrder = new ArrayList<Crate>();
					itemsCrate = new ArrayList<Item>();
					itemsOrder = new ArrayList<Item>();
					chrom = new Chromosome(new double[0], new double[0], cratesOrder, itemsOrder, Double.POSITIVE_INFINITY, 0, orderNumber);
					itemsCrate.add(item);
					itemsOrder.add(item);
				}
			}
			return chromosomes;
		}
	}

	public static List<Crate> readFileOriginalGACrates(File file, Map<Double, Item> items) throws FileNotFoundException {
		try(Scanner s = new Scanner(file)) {
			s.nextLine();
			List<Crate> crates = new ArrayList<Crate>();
			List<Item> itemsCrate = new ArrayList<Item>();
			int crateNumber = 0;
			while (s.hasNextLine()) {
				String line = s.nextLine();
				String[] lineArray = line.split(",");
				int curCrateNumber = Integer.parseInt(lineArray[0]);
				int curOrderNumber = Integer.parseInt(lineArray[1]);
				int itemId = Integer.parseInt(lineArray[2]);
				int insertedY = Integer.parseInt(lineArray[3]);
				int insertedX = Integer.parseInt(lineArray[5]);
				int insertedZ = Integer.parseInt(lineArray[7]);
				Item item = items.get((double) itemId);
				item.setInsertedX(insertedX);
				item.setInsertedY(insertedY);
				item.setInsertedZ(insertedZ);
				if (curCrateNumber == crateNumber) { // Same crate, same order
					itemsCrate.add(item);
				}
				else { // Different crate, same order
					crateNumber++;
					Crate crate = new Crate(itemsCrate);
					crate.setOrderIndex(curOrderNumber);
					crates.add(crate);
					itemsCrate = new ArrayList<Item>();
					itemsCrate.add(item);
				}
			}
			return crates;
		}
	}

	@SuppressWarnings("unused")
	private static void writeFileCompetition(List<Chromosome> chromosomes) throws FileNotFoundException {
		File competition = new File("GA_aisle_after_original.csv");
		PrintWriter out = new PrintWriter(competition);
		out.println("crate_id,order_id,item_id,x_start,x_end,y_start,y_end,z_start,z_end");
		int crate_id = 0;
		for (int i=0; i < chromosomes.size(); i++) {
			List<Crate> crates = chromosomes.get(i).getCrates();
			for (int k=0; k < crates.size(); k++) {
				List<Item> items = crates.get(k).getItemList();
				for (int l=0; l < items.size(); l++) {
					Item item = items.get(l);
					int x_end = (int) (item.getInsertedY() + item.getLength());
					int y_end = (int) (item.getInsertedX() + item.getWidth());
					int z_end = (int) (item.getInsertedZ() + item.getHeight());
					String line = crate_id + "," + (int) chromosomes.get(i).getOrderId() + "," + (int) item.getItemId() + ",";
					line += (int) item.getInsertedY() + "," + x_end + "," + (int) item.getInsertedX() + "," + y_end + "," + (int) item.getInsertedZ() + "," + z_end;
					out.println(line);
				}
				crate_id++;
			}
		}
		out.close();
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
