import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import ilog.concert.IloException;

public class Main {

	public static void main(String[] args) throws IloException, IOException, InterruptedException {
		Map<Double, Item> items = readItems();
		List<Order> orders = readOrders(items);
		int choiceOriginal = 1; // Choice for initial algorithm: 1 for FF, 2 for BF, 3 for BRKGA

		long startTime = System.nanoTime();
		List<Crate> crates = new ArrayList<Crate>();
		switch(choiceOriginal) {
		case 1:
			crates = solveFF(orders);
			break;
		case 2:
			crates = solveBF(orders);
			break;
		case 3:
			crates = MainBRKGA.readFileOriginalGACrates(new File("GA_aisle_after_original.csv"), items);
			break;
		default: break;
		}

		long endTime = System.nanoTime();
		int numAislesOriginal = 0;
		for (Crate cr : crates) {
			List<Crate> crate = new ArrayList<Crate>();
			crate.add(cr);
			ShortestPath sp = new ShortestPath(crate);
			numAislesOriginal += sp.getPathSize(0);
		}
		System.out.println("Total aisles before optimizing is:\t" + numAislesOriginal);
		System.out.println("Time:\t" + (endTime - startTime)/1000000);
		
		// Create order pickers sequential
		startTime = System.nanoTime();
		List<OrderPicker> sequentialPickers = sequentialPickers(crates);

		// Compute shortest path for all pickers
		int numAislesSequential = shortestPathPickers(sequentialPickers);
		System.out.println("Total aisles sequential is:\t" + numAislesSequential);
		System.out.println("Num pickers is:\t" + sequentialPickers.size());
		System.out.println("Time:\t" + (System.nanoTime() - startTime)/1000000);

		// Find order pickers with greedy
		startTime = System.nanoTime();
		List<OrderPicker> intuitivePickers = solveExtension2Heuristic(crates); // The simple heuristic, with the last ones random

		// Compute shortest path for all pickers
		int numAislesIntuitive = shortestPathPickers(intuitivePickers);		
		System.out.println("Total aisles after intuitive :\t" + numAislesIntuitive);
		System.out.println("Num pickers is:\t" + intuitivePickers.size());
		System.out.println("Time:\t"+ (System.nanoTime()-startTime)/1000000);


		// Find order pickers with greedy
		startTime = System.nanoTime();
		List<OrderPicker> greedyPickers = greedyHeuristic(crates);

		// Compute shortest path for all pickers
		int numAislesGreedy = shortestPathPickers(greedyPickers);		
		System.out.println("Total aisles after greedy :\t" + numAislesGreedy);
		System.out.println("Num pickers is:\t" + greedyPickers.size());
		System.out.println("Time:\t"+ (System.nanoTime()-startTime)/1000000);

		// Perform Local search
		startTime = System.nanoTime();
		List<OrderPicker> ls = (new LocalSearch()).performLocalSearch(greedyPickers);
		int totalNumAisle2 = 0;
		for(int i = 0 ; i < ls.size() ; i++)
		{
			totalNumAisle2 += ls.get(i).getShortestPath();
		}

		System.out.println("Total pickers:\t" + greedyPickers.size());
		System.out.println("Total aisle after LS:\t" + totalNumAisle2);	
		System.out.println("Time:\t"+ (System.nanoTime()-startTime)/1000000000);
	}

	/**
	 * computes for all order pickers the shortest path, and returns the lenght of all shortestpaths together
	 * @param orderPickers input is list of orderpickers
	 * @param graph
	 * @return
	 */
	private static int shortestPathPickers(List<OrderPicker> orderPickers) {
		int totalLength = 0;
		for(int i = 0; i < orderPickers.size(); i++) {			
			ShortestPath sp = new ShortestPath(orderPickers.get(i).getCrates());
			int total = sp.computeTotalPathLength(orderPickers.get(i).getCrates());
			orderPickers.get(i).setShortestPath(total);
			totalLength = totalLength + total;
		}
		return totalLength;
	}

	/**
	 * Computes a list of order pickers when setcovering all crates 'Random' (a.k.a group first 8 etc)
	 * @param crates
	 * @return
	 */
	private static List<OrderPicker> sequentialPickers(List<Crate> crat){
		List<Crate> crates = new ArrayList<Crate>(crat);		
		double numPickers = Math.ceil(crates.size()/8.0);
		Iterator<Crate> iterator = crates.iterator();
		List<OrderPicker> orderpickers = new ArrayList<OrderPicker>();
		for(int j = 0; j < numPickers; j++) {
			List<Crate> cr = new ArrayList<Crate>();
			for(int i = 0; i < 8; i++) {
				if(iterator.hasNext() == false) {
					break;
				}
				Crate c = iterator.next();
				cr.add(c);
				iterator.remove();
			}
			boolean[] testaisles = new boolean[8];
			for(Crate crate : cr) {
				for(int z = 0; z < 8; z++) {
					if(crate.getAisles()[z] == true) {
						testaisles[z] = true;;
					}
				}	
			}
			List<Integer> aislesToVisit = new ArrayList<Integer>();
			for(int z = 0; z < 8 ; z++) {
				if(testaisles[z] == true) {
					aislesToVisit.add(z);
				}
			}
			OrderPicker picker = new OrderPicker(j, cr, aislesToVisit.size(), aislesToVisit);
			orderpickers.add(picker);
		}
		return orderpickers;
	}

	private static List<OrderPicker> greedyHeuristic(List<Crate> crates) {
		Collections.sort(crates);
		List<OrderPicker> pickers = new ArrayList<>();
		Crate[] bins = new Crate[crates.size()];
		boolean[] visited = new boolean[crates.size()];
		for(int i = 0 ; i < crates.size() ; i++)
		{
			bins[i] = crates.get(i);
			visited[i] = false;
		}
		for(int b = 0 ; b < crates.size() ; b++)
		{
			if(visited[b])continue;
			List<Crate> pickerBoy = new ArrayList<>();
			pickerBoy.add(bins[b]);
			visited[b] = true;
			while(pickerBoy.size() < 8)
			{
				int best = -1;
				int costBest = 10000000;
				int numAisleBest=0;
				for(int i = b+1 ; i < crates.size() ; i++)
				{
					if(visited[i])continue;
					pickerBoy.add(bins[i]);
					int cost = computeTotalPathLength(pickerBoy);
					int num = bins[i].getAisleList().size();
					if(cost < costBest)
					{
						best = i;
						costBest = cost;
						numAisleBest = num;
					}
					else if(cost == costBest && num > numAisleBest)
					{
						best = i;
						costBest = cost;
						numAisleBest = num;
					}
					pickerBoy.remove(pickerBoy.size()-1);
				}
				if(best == -1)
				{
					break;
				}
				pickerBoy.add(bins[best]);
				visited[best] = true;
			}
			pickers.add(new OrderPicker(pickers.size(),pickerBoy));
		}
		return pickers;
	}

	@SuppressWarnings("unused")
	private static List<OrderPicker> solveExtension2Heuristic(List<Crate> crates) {
		// Maak nieuwe lijst
		List<Crate> cratesToPick = new ArrayList<>(crates);
		Collections.sort(cratesToPick); // Sorteer je lijst met kratten gebaseerd op shortestpath length
		// Compute number of crates with a shortest path of 8
		double eightcounter = 0.0;
		for(int i = 0; i < crates.size();i++) {
			if(crates.get(i).getShortestPathLength() == 8) {
				eightcounter++;
			}
		}
		double counter = Math.ceil(eightcounter/8.0);
		List<OrderPicker> orderpickers = new ArrayList<OrderPicker>();

		Iterator<Crate> iter = cratesToPick.iterator();
		for(int j = 0; j < counter; j++) {
			List<Crate> cr = new ArrayList<Crate>();
			for(int i = 0; i < 8; i++) {
				Crate c = iter.next();
				cr.add(c);
				iter.remove();
			}
			OrderPicker picker = new OrderPicker(j, cr, 8, cr.get(0).getAisleList());
			orderpickers.add(picker);
		}
		// alles van 8 is er nu uit + een paar zesjes


		List<Crate> restList = new ArrayList<Crate>();
		while(cratesToPick.isEmpty() == false) {
			Iterator<Crate> iterate = cratesToPick.iterator();
			List<Crate> cr = new ArrayList<Crate>();
			Crate first = cratesToPick.get(0);
			while(iterate.hasNext() && cr.size() < 8) {
				Crate c = iterate.next();
				if(first.getShortestPathList().equals(c.getShortestPathList())) {
					cr.add(c);
					iterate.remove();
				}
				else { // ga naar volgende crate en check deze
				}
			}
			if(cr.size() == 8) {
				OrderPicker picker = new OrderPicker(orderpickers.size(), cr, cr.get(0).getNumAisles(), cr.get(0).getAisleList());
				orderpickers.add(picker);
			}
			else { // size is kleiner
				restList.addAll(cr);
			}
		}
		// Restlist with heuristic
		double restcounter = Math.ceil(restList.size()/8.0);
		Iterator<Crate> iterator = restList.iterator();
		for(int j = 0; j < restcounter; j++) {
			List<Crate> cr = new ArrayList<Crate>();
			for(int i = 0; i < 8; i++) {
				if(iterator.hasNext() == false) {
					break;
				}
				Crate c = iterator.next();
				cr.add(c);
				iterator.remove();
			}
			boolean[] testaisles = new boolean[8];
			for(Crate crate : cr) {
				for(int z = 0; z < 8; z++) {
					if(crate.getAisles()[z] == true) {
						testaisles[z] = true;;
					}
				}	
			}
			List<Integer> aislesToVisit = new ArrayList<Integer>();
			for(int z = 0; z < 8 ; z++) {
				if(testaisles[z] == true) {
					aislesToVisit.add(z);
				}
			}
			OrderPicker picker = new OrderPicker(j, cr, aislesToVisit.size(), aislesToVisit);
			orderpickers.add(picker);
		}

		double total = 0.0;
		for(int i = 0; i < orderpickers.size(); i++) {
			ShortestPath spath = new ShortestPath(orderpickers.get(i).getCrates());
			int value = spath.computeShortestPathOneCrate(orderpickers.get(i).getAislesToVisit());
			orderpickers.get(i).setShortestPath(value);
			total = total + value;
			//			System.out.println(value);
		}
		//		System.out.println("Total number of aisles needed: " + total);
		return orderpickers;

		/*
		// Voor set van 8 kratten
		List<OrderPicker> orderpickers = orderPicking(crates);
		for(int i = 0; i < orderpickers.size(); i++) {
			ShortestPath spath = new ShortestPath(orderpickers.get(i).getCrates(), g);
			int value = spath.computeShortestPathOneCrate(orderpickers.get(i).getAislesToVisist());
			orderpickers.get(i).setShortestPath(value);
			System.out.println(value);
		}
		 */
	}


	/**
	 * Method that solves LP problem for the lower bound
	 * @param orders
	 * @param items
	 * @throws IloException
	 * @throws IOException
	 */
	private static void solveLP(List<Order> orders, Map<Double, Item> items) throws IloException, IOException {
		FileWriter myWriter = new FileWriter("LB_solution_value.txt");
		myWriter.write("Order\tCrates\n");
		double totalBins = 0.0;
		double totalVolume = 0.0;
		double totalWeight = 0.0;
		for(Order order : orders) {
			for(Item i : order.getItems()) {
				totalVolume += i.getVolume();
				totalWeight += i.getWeight();
			}
			int LB = (int)LowerBoundModel.setCoveringLB(order, items);
			int id = (int) order.getOrderId();
			myWriter.write(id+"\t"+LB+"\n");
			totalBins += LB;
		}
		double averageVolume = Math.round(totalVolume/totalBins/1000);
		double averageWeight = Math.round(totalWeight/totalBins);
		System.out.println("Total bins: "+totalBins);
		System.out.println("Average volume: "+averageVolume/1000);
		System.out.println("Average weight: "+averageWeight/1000);
		myWriter.close();
	}

	/**
	 * Method that performs BF
	 * @param orders
	 * @return
	 */
	private static List<Crate> solveBF(List<Order> orders) {
		List<Crate> crates = new ArrayList<>();
		double totalBins = 0.0;
		for(int i = 0; i < orders.size(); i++) {
			BF bf = new BF(orders.get(i));
			List<Crate> c = bf.computeBF();
			totalBins += c.size();
			for(Crate crate : c) {
				crates.add(crate);
			}
		}
		System.out.println("Total Bins: "+(int) totalBins);
		return crates;
	}

	/**
	 * Method that performs FF
	 * @param orders
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private static List<Crate> solveFF(List<Order> orders) throws IOException {
		List<Crate> crates = new ArrayList<>();
		double totalBins = 0.0;		
		for(int i = 0; i < orders.size(); i++) {
			FF ff = new FF(orders.get(i));
			List<Crate> c = ff.computeFF();
			totalBins += c.size();
			for(Crate crate : c) {
				crates.add(crate);
			}	
		}
		System.out.println("Total Bins: " + (int) totalBins);
		return crates;
	}

	/**
	 * Method that performs FF and writes solution files
	 * @param orders
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private static List<ArrayList<Integer>> solveFFFiles(List<Order> orders) throws IOException {
		List<ArrayList<Integer>> aisles = new ArrayList<>();
		FileWriter myWriter = new FileWriter("FF_solution_value.txt");
		FileWriter myWriter2 = new FileWriter("FF_solution.txt");
		FileWriter myWriter3 = new FileWriter("FF_aisles.txt");
		FileWriter myWriter4 = new FileWriter("FF_numAisles.txt");
		FileWriter myWriter5 = new FileWriter("FF_CrateAisleList.txt");
		double totalBins = 0.0;
		double totalVolume = 0.0;
		double totalWeight = 0.0;
		int crateCounter = 0;
		int oneCounter = 0;
		int twoCounter = 0;
		int threeCounter = 0;
		int fourCounter = 0;
		int fiveCounter = 0;
		int sixCounter = 0;
		int sevenCounter = 0;
		int eightCounter = 0;
		myWriter.write("Order\tamountCrates\n");
		for(int i = 0 ; i < orders.size() ; i++) {
			FF ff = new FF(orders.get(i));
			List<Crate> crates = ff.computeFF();
			myWriter.write(i+"\t"+crates.size()+"\n");
			int test = checkSolution(crates,i);
			if(test==0) {
				myWriter.write(i+"\t"+crates.size()+"\n");
			}
			else {
				myWriter.write(i+"\t"+crates.size()+"\t"+test+"\n");
			}
			myWriter2.write("Order: "+i+"\nCrates: "+crates.size()+"\n");
			myWriter3.write("Order: "+i+"\nCrates: "+crates.size()+ "\n");
			int counter = 1;
			totalBins += crates.size();
			for(Crate crate : crates) {
				crateCounter++;
				List<Item> items = crate.getItemList();
				double volume = 0.0;
				double weight = 0.0;
				for(Item item : items) {
					volume += item.getVolume();
					weight += item.getWeight();
				}
				totalVolume += volume;
				totalWeight += weight;
				double fillRate = Math.round(volume/crate.getVolume()*10000)/100;
				myWriter2.write(counter+"\t"+fillRate+"\t"+weight+"\t");
				myWriter4.write(crateCounter + "\t");
				myWriter5.write(crateCounter + "\t");
				int aisleCounter = 0;
				for(Item item : items) {
					int id = (int) item.getItemId();
					if(id>=0)myWriter2.write(id+" ");
				}
				ArrayList<Integer> ail = new ArrayList<Integer>(); 
				for(int j = 0; j < 8; j++) {
					if(crate.getAisles()[j] == true) {
						aisleCounter++;
						myWriter3.write(j+ " ");
						ail.add(j);
						myWriter5.write(j + " ");
					}
				}
				aisles.add(ail);
				if(aisleCounter == 1) {oneCounter++;}
				if(aisleCounter == 2) {twoCounter++;}
				if(aisleCounter == 3) {threeCounter++;}
				if(aisleCounter == 4) {fourCounter++;}
				if(aisleCounter == 5) {fiveCounter++;}
				if(aisleCounter == 6) {sixCounter++;}
				if(aisleCounter == 7) {sevenCounter++;}
				if(aisleCounter == 6) {eightCounter++;}
				myWriter4.write(aisleCounter + "\n");
				myWriter2.write("\n");
				myWriter3.write("\n");
				myWriter5.write("\n");
				counter++;
			}
			myWriter2.write("\n");
			myWriter3.write("\n");
		}
		myWriter.close();myWriter2.close();myWriter3.close();myWriter4.close();myWriter5.close();
		System.out.println("Total Bins: "+(int)totalBins);
		double averageVolume = Math.round(totalVolume/totalBins/1000);
		double averageWeight = Math.round(totalWeight/totalBins);
		System.out.println("Average volume: "+averageVolume/1000);
		System.out.println("Average weight: "+averageWeight/1000);
		System.out.println("Times 1 aisle used: " + oneCounter);
		System.out.println("Times 2 aisles used: " + twoCounter);
		System.out.println("Times 3 aisles used: " + threeCounter);
		System.out.println("Times 4 aisles used: " + fourCounter);
		System.out.println("Times 5 aisles used: " + fiveCounter);
		System.out.println("Times 6 aisles used: " + sixCounter);
		System.out.println("Times 7 aisles used: " + sevenCounter);
		System.out.println("Times 8 aisles used: " + eightCounter);
		return aisles;
	}

	/**
	 * Method that performs FF according to the paper
	 * @param orders
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private static void solveFFPaper(List<Order> orders) throws IOException {
		FileWriter myWriter = new FileWriter("FF_Paper_solution_value.txt");
		FileWriter myWriter2 = new FileWriter("FF_Paper_solution.txt");
		double totalBins = 0.0;
		double totalVolume = 0.0;
		double totalWeight = 0.0;
		myWriter.write("Order\tamountCrates\n");
		for(int i = 0 ; i < orders.size() ; i++) {
			FF_paper ff = new FF_paper(orders.get(i));
			List<Crate> crates = ff.computeFF();
			//			myWriter.write(i+"\t"+crates.size()+"\n");
			int test = checkSolution(crates,i);
			if(test==0)myWriter.write(i+"\t"+crates.size()+"\n");
			else myWriter.write(i+"\t"+crates.size()+"\t"+test+"\n");
			myWriter2.write("Order: "+i+"\nCrates: "+crates.size()+"\n");
			int counter = 1;
			totalBins += crates.size();
			for(Crate crate : crates) {
				List<Item> items = crate.getItemList();
				double volume = 0.0;
				double weight = 0.0;
				for(Item item : items) {
					volume += item.getVolume();
					weight += item.getWeight();
				}
				totalVolume += volume;
				totalWeight += weight;
				double fillRate = Math.round(volume/crate.getVolume()*10000)/100;
				myWriter2.write(counter+"\t"+fillRate+"\t"+weight+"\t");
				for(Item item : items) {
					int id = (int) item.getItemId();
					if(id>=0)myWriter2.write(id+" ");
				}
				myWriter2.write("\n");
				counter++;
			}
			myWriter2.write("\n");
		}
		myWriter.close();myWriter2.close();
		System.out.println("Total Bins: "+(int)totalBins);
		double averageVolume = Math.round(totalVolume/totalBins/1000);
		double averageWeight = Math.round(totalWeight/totalBins);
		System.out.println("Average volume: "+averageVolume/1000);
		System.out.println("Average weight: "+averageWeight/1000);
	}

	/**
	 * Method that computes lower bound and compares with BF solution
	 * @param orders
	 * @param items
	 * @throws IloException
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private static void computeLowerBound(List<Order> orders, Map<Double,Item> items) throws IloException, IOException {
		// Compute lower bound and write file
		Long start = System.currentTimeMillis();
		solveLP(orders, items);
		double timeLB = System.currentTimeMillis()-start;
		System.out.println("Solution time: "+timeLB/1000);
		start = System.currentTimeMillis();
		compare(new File("LB_solution_value.txt"), new File("BF_solution_value.txt"),orders);
	}

	/**
	 * Method that compares lower bound with BF solution
	 * @param sol1
	 * @param sol2
	 * @param orders
	 * @throws FileNotFoundException
	 */
	private static void compare(File sol1, File sol2, List<Order> orders) throws FileNotFoundException {
		Scanner s1 = new Scanner(sol1);
		Scanner s2 = new Scanner(sol2);
		s1.nextLine();
		s2.nextLine();
		double small = 23;
		double large = 101;
		double dif = 0.0;
		double amountSmall = 0.0; 
		double amountLarge = 0.0;
		double amountMedium = 0.0;
		double difSmall = 0.0;
		double difMedium = 0.0;
		double difLarge = 0.0;
		for(int i = 0 ; i < 1000 ; i++) {
			int order1 = s1.nextInt();
			int order2 = s2.nextInt();
			double difBin = s2.nextInt()-s1.nextInt();
			if(order1 == order2 && difBin >= 0) {
				dif += difBin;
				int size = orders.get(order1).getItems().size();
				if(size <= small) {
					difSmall += difBin;
					amountSmall++;
				}
				else if(small < size && size < large) {
					difMedium += difBin;
					amountMedium++;
				}
				else {
					difLarge += difBin;
					amountLarge++;
				}
			}
			else {
				System.out.println("fout opgetreden");
				s1.close();s2.close();
				return;
			}
		}
		s1.close();s2.close();
		System.out.println("Total:\t"+(int)dif+"\t"+dif/1000.0);
		System.out.println("Small:\t"+(int)difSmall +"\t"+difSmall/amountSmall);
		System.out.println("Medium:\t"+(int)difMedium+"\t"+difMedium/amountMedium);
		System.out.println("Large:\t"+(int)difLarge+"\t"+difLarge/amountLarge);
	}

	/**
	 * Method that writes a file in order to make a visualisation
	 * @param crates
	 * @param crateNumber
	 * @throws IOException
	 */
	public static void getPlotOutput(List<Crate> crates, int crateNumber) throws IOException {
		if(crateNumber > crates.size()) {
			System.out.println("Crate number not feasible");
			return;
		}
		else {
			FileWriter myWriter = new FileWriter("outputPlot_FF.txt");
			Crate crate = crates.get(crateNumber-1);
			List<Item> items  =crate.getItemList();
			myWriter.write("id\tx\ty\tz\tw\tl\th\n");
			for(int j = 6 ; j < items.size() ; j++) {
				Item i = items.get(j);
				int id = (int) i.getItemId();
				int x = (int) i.getInsertedX();
				int y = (int) i.getInsertedY();
				int z = (int) i.getInsertedZ();
				int w = (int) i.getWidth();
				int l = (int) i.getLength();
				int h = (int) i.getHeight();
				myWriter.write(id+"\t"+x+"\t"+y+"\t"+z+"\t"+w+"\t"+l+"\t"+h+"\n");
			}
			myWriter.close();
		}
	}

	/**
	 * Method that checks whether items in a crate overlap
	 * @param crates
	 * @param order
	 * @return
	 */
	public static int checkSolution(List<Crate> crates, int order) {
		int counter = 1;
		for(Crate c : crates) {
			List<Item> items = c.getItemList();
			for(int i = 0 ; i < items.size() ; i++) {
				Item itemi = items.get(i);
				for(int j = 0 ; j < items.size() ; j++) {
					Item itemj = items.get(j);
					if(i==j) continue;
					if(isOverlapping(itemi,itemj)) {
						return counter;
					}
				}
			}
			counter++;
		}
		return 0;
	}

	/**
	 * Method that checks whether 2 items overlap
	 * @param item1
	 * @param item2
	 * @return True if items overlap, false otherwise
	 */
	private static boolean isOverlapping(Item item1, Item item2) {
		double x1min = item1.getInsertedX();
		double x1max = x1min + item1.getWidth();
		double y1min = item1.getInsertedY();
		double y1max = y1min + item1.getLength();
		double z1min = item1.getInsertedZ();
		double z1max = z1min + item1.getHeight();
		double x2min = item2.getInsertedX();
		double x2max = x2min + item2.getWidth();
		double y2min = item2.getInsertedY();
		double y2max = y2min + item2.getLength();
		double z2min = item2.getInsertedZ();
		double z2max = z2min + item2.getHeight();
		if(x1min < x2max && x2min < x1max && y1min < y2max && y2min < y1max && z1min < z2max && z2min < z1max)
			return true;
		return false;
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

	/**
	 * Compute total number of paths passed over all crates
	 * @return
	 */
	public static int computeTotalPathLength(List<Crate> crates) {

		// Compute all aisles that are visited in this list of crates
		List<Integer> aislesToVisit = new ArrayList<Integer>();
		boolean[] testaisles = new boolean[8];	
		for(Crate crate : crates) {
			for(int z = 0; z < 8; z++) {
				if(crate.getAisles()[z] == true) {
					testaisles[z] = true;;
				}
			}	
		}
		for(int z = 0; z < 8 ; z++) {
			if(testaisles[z] == true) {
				aislesToVisit.add(z);
			}
		}

		ArrayList<Integer> a = new ArrayList<Integer>();

		for(int j = 0; j < aislesToVisit.size(); j++) {
			int index = aislesToVisit.get(j);
			if(j == 0) {
				if(index % 2 == 0) {
					// Dan is het goed
					a.add(index);
				} 
				else {
					a.add(index - 1);
					a.add(index);
				}
			}
			else {
				int prevIndex = aislesToVisit.get(j - 1);
				int space = index - prevIndex;
				if(space % 2 != 0) { // dan is het goed
					a.add(index);
				}
				else {
					a.add(index - 1);
					a.add(index);
				}
			}
			if(j == aislesToVisit.size() - 1) {
				// Als laatste Isle oneven is, kan je meteen naar eindpunt. Doe niets. Laatste ail al toegevoegd in vorige if statement
				if(index % 2 == 0) { 
					// Als laatste aisle even is, moet je nog terug door een aisle
					a.add(index + 1);
				}
			}		
		}
		return a.size();
	}
}