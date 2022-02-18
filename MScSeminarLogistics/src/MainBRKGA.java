import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ilog.concert.IloException;

public class MainBRKGA {

	static final int MAX_T = 4; 

	public static void main(String[] args) throws IloException, IOException, InterruptedException {
		Map<Double, Item> items = readItems();
		List<Order> orders = readOrders(items);
		int choice_D2_VBO = 1; // TODO: 1 if DFTRC-2^2 is used, 2 if DFTRC-2-VBO is used

		double[] lowerBound = new double[orders.size()];
		for(int i = 0; i < orders.size(); i++) {
			double lowerbound = LowerBoundModel.setCoveringLB(orders.get(i), items);
			lowerBound[i] = lowerbound;
		}


		// create tasks
		List<Runnable> runnables = new ArrayList<>();
		for(int i=0; i < orders.size(); i++) {
			Runnable runnable = new MultiThread(orders.get(i), 1, lowerBound[i], i);
			runnables.add(runnable);

		}

		// creates a thread pool with MAX_T no. of 
		// threads as the fixed pool size(Step 2)
		ExecutorService pool = Executors.newFixedThreadPool(MAX_T); 

		// passes the MultiThread objects to the pool to execute (Step 3)
		for(Runnable runnable: runnables) {
			pool.execute(runnable);
		}

		awaitTerminationAfterShutdown(pool);
		// pool shutdown ( Step 4)
		//pool.shutdown();

		File info = new File("GA_solution_info_multi.txt"); 
		File sol = new File("GA_solution_multi.txt");
		PrintWriter outInfo = new PrintWriter(info);
		PrintWriter outSol = new PrintWriter(sol);
		outInfo.println("instance\tnumCrates\tadjNumCrates\tnumCratesLB\tavFillRate\tavWeight\trunTime");
		int totalNumBins = 0;
		Crate crate = new Crate();
		for (int i=0; i < orders.size(); i++) { // TODO: orders.size
			System.out.println("Order nr " + i);
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
		System.out.println("Total Number Of Bins MULTITHREAD " + totalNumBins);


	    // int instance = 88;
    	//		double lowerbound = LowerBoundModel.setCoveringLB(orders.get(instance), items);
		//		System.out.println(lowerbound);
		//		Chromosome chrom = BRKGA.solve(orders.get(instance), lowerbound, choice_D2_VBO);
		//		printCrate(orders.get(instance), chrom);

		/*
		 * File info = new File("GA_solution_info_multi.txt"); File sol = new
		 * File("GA_solution_multi.txt"); PrintWriter outInfo = new PrintWriter(info);
		 * PrintWriter outSol = new PrintWriter(sol); outInfo.println(
		 * "instance\tnumCrates\tadjNumCrates\tnumCratesLB\tavFillRate\tavWeight"); int
		 * totalNumBins = 0; Crate crate = new Crate(); List<Thread> threads = new
		 * ArrayList<Thread>(); long startTime = System.nanoTime(); ExecutorService
		 * executorService = Executors.newSingleThreadExecutor();
		 * executorService.execute(new Runnable() { public void run() {
		 * System.out.println("Asynchronous task"); } });
		 * 
		 * executorService.shutdown(); for(int i = 0; i < orders.size(); i++) {
		 * MultiThread multiThread = new MultiThread(orders.get(i), choice_D2_VBO,
		 * lowerBound[i], i); Thread thread = new Thread(multiThread);
		 * threads.add(thread); thread.start(); } List<Thread> threadsCopy = new
		 * ArrayList<Thread>(threads); while (!threadsCopy.isEmpty()) {
		 * System.out.println("Number of alive threads: " + threadsCopy.size());
		 * Iterator<Thread> iter = threadsCopy.iterator(); while(iter.hasNext()){
		 * MultiThread threadCopy = iter.next(); if(!threadCopy.isAlive()){
		 * iter.remove(); } } } long endTime = System.nanoTime(); long totalTime =
		 * (endTime - startTime)/1000000; System.out.println("Total time = " +
		 * totalTime); for(int i=0; i < orders.size(); i++) { Chromosome chrom =
		 * threads.get(i).getChromosome(); totalNumBins += chrom.getNumCrates(); double
		 * avFillRate = 0.0; double avWeight = 0.0; for (Item it :
		 * orders.get(i).getItems()) { avFillRate += it.getVolume(); avWeight +=
		 * it.getWeight(); } avFillRate =
		 * avFillRate/(chrom.getNumCrates()*crate.getVolume()); avWeight =
		 * avWeight/chrom.getNumCrates(); outInfo.println(i + "\t" +
		 * chrom.getNumCrates() + "\t" + chrom.getFitness() + "\t" + lowerBound[i] +
		 * "\t" + avFillRate + "\t" + avWeight); outSol.println("Order: " + i);
		 * outSol.println("Crates: " + chrom.getNumCrates()); for (int k=0; k <
		 * chrom.getOpenCrates().size(); k++) { List<Integer> openCrate =
		 * chrom.getOpenCrates().get(k); String output = k + "\t"; for (int j=0; j <
		 * openCrate.size(); j++) { if (openCrate.get(j) == 1) output += (int)
		 * chrom.getItems().get(j).getItemId() + " "; } outSol.println(output); } }
		 * outInfo.close(); outSol.close();
		 * System.out.println("MULTI THREAD TOTAL NUMBER OF BINS: " + totalNumBins);
		 */

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
