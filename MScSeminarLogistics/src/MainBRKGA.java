import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
		Graph graph = Graph.createGraph();
		Crate crate = new Crate();
		int choiceSplit = 2; // Choice for order splitting or not: 1 for no splitting, 2 for splitting
		int choiceAisles = 2; // Choice for incorporating number of aisles or not: 1 for not incorporating, 2 for incorporating
		int numExecutions = 1;
		double totalNumBins = 0.0;
		int[] numCrates = new int[numExecutions];
		int[] numAisles = new int[numExecutions];
		double[] avVolume = new double[numExecutions];
		double[] avWeight = new double[numExecutions];
		double[] avFillRate = new double[numExecutions];
		double[] avWeightRate = new double[numExecutions];
		double totalAvVolume = 0.0;
		double totalAvWeight = 0.0;
		double totalAvFillRate = 0.0;
		double totalAvWeightRate = 0.0;
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

		for (int j=0; j < numExecutions; j++) {
			int[] thisNumCrates = new int[orders.size()];
			int[] thisNumAisles = new int[orders.size()];
			long startTime = System.nanoTime();

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
			long endTime = System.nanoTime();
			long totalTime = (endTime - startTime)/1000000000; // seconds

			int numAislesJ = 0;	
			for (int i=0; i < runnables.size(); i++) {
				Chromosome chrom = ((MultiThread) runnables.get(i)).getChromosome();
				chromosomes.add(chrom);
				thisNumCrates[i] = chrom.getNumCrates();
				List<Crate> crates = chrom.getCrates();
				ShortestPath shortestPath = new ShortestPath(crates, graph);
				List<Integer> pathSizes = shortestPath.getPathSizes();
				for (Integer integer : pathSizes) {
					thisNumAisles[i] = integer;
					numAislesJ += integer;
				}	
			}
			numAisles[j] = numAislesJ;
			System.out.println("Num aisles original: ");
			System.out.println(Arrays.toString(thisNumAisles));
			System.out.println("Total num aisles original: " + numAisles[j]);
			
			if (choiceAisles == 2) {
				optAisles(orders, chromosomes, lowerBound, thisNumCrates);
				endTime = System.nanoTime();
				totalTime = (endTime - startTime)/1000000000; // seconds
			}
			
			numAislesJ = 0;
			for (int i=0; i < chromosomes.size(); i++) {
				List<Crate> crates = chromosomes.get(i).getCrates();
				ShortestPath shortestPath = new ShortestPath(crates, graph);
				List<Integer> pathSizes = shortestPath.getPathSizes();
				for (Integer integer : pathSizes) {
					thisNumAisles[i] = integer;
					numAislesJ += integer;
				}
			}
			numAisles[j] = numAislesJ;
			System.out.println("Num aisles after optimizing: ");
			System.out.println(Arrays.toString(thisNumAisles));
			System.out.println("Total num aisles after optimizing: " + numAisles[j]);
			
			// Write solution
			int numCratesJ = 0;
			switch (choiceSplit) {
			case 1: numCratesJ = writeSolNoSplit(orders, lowerBound, chromosomes);
			break;
			case 2: numCratesJ = writeSolSplit(orders, lowerBound, chromosomes);
			break;
			}
			totalNumBins += numCratesJ;
			numCrates[j] = numCratesJ;
			avVolume[j] = totalVolume/numCratesJ;
			avWeight[j] = totalWeight/numCratesJ;
			avFillRate[j] = avVolume[j]/crate.getVolume();
			avWeightRate[j] = avWeight[j]/crate.getMaxWeight();
			totalAvVolume += avVolume[j];
			totalAvWeight += avWeight[j];
			totalAvFillRate += avFillRate[j];
			totalAvWeightRate += avWeightRate[j];
			totalRunTime += totalTime;
			System.out.println("Total runtime MULTITHREAD execution " + j + ": " + totalTime + " s");
		}

		double avTotalNumBins = totalNumBins/numExecutions;
		double gap = (avTotalNumBins-1748)/1748;
		double avTotalVolume = totalAvVolume/numExecutions;
		double avTotalWeight = totalAvWeight/numExecutions;
		double avTotalFillRate = totalAvFillRate/numExecutions;
		double avTotalWeightRate = totalAvWeightRate/numExecutions;
		double avRunTime = (double) totalRunTime/numExecutions;
		System.out.println("Average total number of bins over " + numExecutions + " executions: " + avTotalNumBins + ", gap: " + gap);
		System.out.println("Average volume: " + avTotalVolume + ", average fill rate: " + avTotalFillRate);
		System.out.println("Average weight: " + avTotalWeight + ", average weight rate: " + avTotalWeightRate);
		System.out.println("Average runtime: " + avRunTime);

		// 		int instance = 88;
		//		double lowerbound = LowerBoundModel.setCoveringLB(orders.get(instance), items);
		//		System.out.println(lowerbound);
		//		Chromosome chrom = BRKGA.solve(orders.get(instance), lowerbound, choice_D2_VBO);
		//		printCrate(orders.get(instance), chrom);

	}

	private static void optAisles(List<Order> orders, List<Chromosome> chromosomes, double[] lowerBound, int[] thisNumCrates) throws FileNotFoundException {
		// Create tasks
		List<Runnable> runnables = new ArrayList<>();
		for(int i=0; i < orders.size(); i++) {
			Runnable runnable = new MultiThread(i, orders.get(i), lowerBound[i], 2, thisNumCrates[i]);
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
			if (chrom.getNumCrates() <= thisNumCrates[i]) {
				System.out.println("lekker bezig in thread " + ((MultiThread) runnables.get(i)).getThreadNumber());
				numOrdersImproved++;
				chromosomes.set(i, chrom);
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
		return totalNumBins;
	}

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
