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

	public static void main(String[] args) throws IloException, IOException {

		Map<Double, Item> items = readItems();
		List<Order> orders = readOrders(items);
		Graph g = Graph.createGraph();

		//computeLowerBound(orders,items); // Computes lowerbound

		// Solve FF
		//List<Crate> crates = solveFF(orders);
		// Solve BF
		List<Crate> crates = solveBF(orders);

		// Voor alle 2000 kratten individueel
		// Make a shortest path class
		ShortestPath sp = new ShortestPath(crates, g);
		// Compute the number of aisles
		List<ArrayList<Integer>> aisles = sp.computeAisles();
		// Compute the shortest path per crate. Displays for each crate the amount of aisles needed to traversed for the shortest path, but not which aisles
		//List<Integer> shortestPaths = sp.computeShortestPathSize();
		// Geeft lijst met integers die de lengte van het korste pad aangeven
		List<ArrayList<Integer>> shortestPathList = sp.computeShortestPathList();
		// Geeft lijst van het korste pad per krat (dus de nummers van de aisles)
		List<Integer> shortestPaths = sp.computeShortestPathListLength();

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
		// alles van 8 is er nu uit + 2 zesjes
				
		
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
				else {
					// ga naar volgende crate en check deze
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
		
		/*
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
		}*/
		
		
		//Restlist with cplex
		Model model = new Model();
		List<ArrayList<Crate>> crat = model.solveModel(restList);
		for(int i = 0; i < crat.size(); i++) {
			boolean[] testaisles = new boolean[8];
			for(Crate crate : crat.get(i)) {
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
			OrderPicker picker = new OrderPicker(orderpickers.size(), crat.get(i), aislesToVisit.size(), aislesToVisit);
			orderpickers.add(picker);
		}
			
		double total = 0.0;
		for(int i = 0; i < orderpickers.size(); i++) {
			ShortestPath spath = new ShortestPath(orderpickers.get(i).getCrates(), g);
			int value = spath.computeShortestPathOneCrate(orderpickers.get(i).getAislesToVisist());
			orderpickers.get(i).setShortestPath(value);
			total = total + value;
			System.out.println(value);
		}
		System.out.println("Total number of aisles needed: " + total);
		
		
		
		


		// Create list of all orderpickers who need to visit all 8 aisles anyway
		//List<OrderPicker> orderpickers = fullOrderPickers(counter, crates);
		//int numCrates = orderpickers.size()*8; // index start of which crates are not yet added to an orderpicker

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



		// Solve 100 times for average time
		//		double totalTime = 0;
		//		for(int i = 0 ; i < 100 ; i++)
		//		{
		//			Long start = System.currentTimeMillis();
		//			solveFFPaper(orders);
		//			double timeLB = System.currentTimeMillis()-start;
		//			totalTime += timeLB/1000;
		//			System.out.println("Solution time: "+timeLB/1000);
		//		}
		//		System.out.println(totalTime);

		// List<ArrayList<Integer>> aisles = solveFFFiles(orders);

	}
	public static List<OrderPicker> fullOrderPickers(double counter, List<Crate> crates){
		List<OrderPicker> orderpickers = new ArrayList<>();
		int numOrderPickers8Aisles = (int) Math.floor(counter/8.0);
		int index = 0;
		int orderPickerIndex = 0;
		for(int i = 0; i < numOrderPickers8Aisles; i++) { // Voor alle kratten met shortest path 8, stop ze bij elkaar
			List<Crate> partCrates = new ArrayList<>();
			for(int j = index; j < index + 8; j++) {
				if(j >= crates.size()) {
					break;
				}
				partCrates.add(crates.get(j));
			}
			index = (i+1)*8;
			boolean[] visited = new boolean[8];
			for(Crate crate : partCrates) {
				for(int j = 0; j < 8; j++) {
					if(crate.getAisles()[j] == true) {
						visited[j] = true;
					}
				}
			}
			int co = 0;
			List<Integer> aislesToVisit = new ArrayList<>();
			for(int j = 0; j < 8; j++) {
				if(visited[j] == true) {
					co++;
					aislesToVisit.add(j);
				}
			}	
			orderPickerIndex = i;
			OrderPicker test = new OrderPicker(i, partCrates, co, aislesToVisit);
			orderpickers.add(test);
		}
		return orderpickers;
	}

	// Dit is voor random 8 kratten toewijzen
	public static List<OrderPicker> orderPicking(List<Crate> crates) {
		double numPickers = Math.ceil((double) crates.size()/8.0);
		List<OrderPicker> orderpickers = new ArrayList<>();
		int index = 0;
		for(int i = 0; i < numPickers; i++) {
			List<Crate> partCrates = new ArrayList<>();
			for(int j = index; j < index + 8; j++) {
				if(j >= crates.size()) {
					break;
				}
				partCrates.add(crates.get(j));
			}
			index = (i+1)*8;
			boolean[] visited = new boolean[8];
			for(Crate crate : partCrates) {
				for(int j = 0; j < 8; j++) {
					if(crate.getAisles()[j] == true) {
						visited[j] = true;
					}
				}
			}
			int counter = 0;
			List<Integer> aislesToVisit = new ArrayList<>();
			for(int j = 0; j < 8; j++) {
				if(visited[j] == true) {
					counter++;
					aislesToVisit.add(j);
				}
			}	
			OrderPicker test = new OrderPicker(i, partCrates, counter, aislesToVisit);
			orderpickers.add(test);
		}
		return orderpickers;
	}

	private static void computeLowerBound(List<Order> orders, Map<Double,Item> items) throws IloException, IOException {
		// Compute lower bound and write file
		Long start = System.currentTimeMillis();
		solveLP(orders, items);
		double timeLB = System.currentTimeMillis()-start;
		System.out.println("Solution time: "+timeLB/1000);
		start = System.currentTimeMillis();
		compair(new File("LB_solution_value.txt"),new File("BF_solution_value.txt"),orders);
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
	private static void compair(File sol1, File sol2, List<Order> orders) throws FileNotFoundException
	{
		Scanner s1 = new Scanner(sol1);
		Scanner s2 = new Scanner(sol2);
		s1.nextLine();s2.nextLine();
		double small = 23;
		double large = 101;

		double dif = 0.0;
		double amountSmall = 0.0; double amountLarge = 0.0;double amountMedium = 0.0;
		double difSmall = 0.0;double difMedium = 0.0;double difLarge = 0.0;

		for(int i = 0 ; i < 1000 ; i++)
		{
			int order1 = s1.nextInt();
			int order2 = s2.nextInt();
			double difBin = s2.nextInt()-s1.nextInt();
			if(order1==order2&& difBin >= 0)
			{
				dif += difBin;
				int size = orders.get(order1).getItems().size();
				if(size <= small)
				{
					difSmall += difBin;
					amountSmall++;
				}
				else if(small < size && size < large)
				{
					difMedium += difBin;
					amountMedium++;
				}
				else
				{
					difLarge += difBin;
					amountLarge++;
				}
			}
			else
			{
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
	private static void solveLP(List<Order> orders, Map<Double, Item> items) throws IloException, IOException {
		FileWriter myWriter = new FileWriter("LB_solution_value.txt");
		myWriter.write("Order\tCrates\n");
		double totalBins = 0.0;
		double totalVolume = 0.0;
		double totalWeight = 0.0;
		for(Order order : orders)
		{
			for(Item i : order.getItems())
			{
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
		//		for(int i = 900 ; i < 1000 ; i++)
		//		{
		//			int LB = (int)LowerBoundModel.setCoveringLB(orders.get(i), items);
		//		}
		//		int LB = (int)LowerBoundModel.setCoveringLB(orders.get(27), items);
	}
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
		System.out.println("Total Bins: "+(int)totalBins);
		return crates;
	}
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
		for(int i = 0 ; i < orders.size() ; i++)
		{
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
			for(Crate crate : crates)
			{
				crateCounter++;
				List<Item> items = crate.getItemList();
				double volume = 0.0;
				double weight = 0.0;
				for(Item item : items)
				{
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
				for(Item item : items)
				{
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
	private static void solveFFPaper(List<Order> orders) throws IOException {
		FileWriter myWriter = new FileWriter("FF_Paper_solution_value.txt");
		FileWriter myWriter2 = new FileWriter("FF_Paper_solution.txt");
		double totalBins = 0.0;
		double totalVolume = 0.0;
		double totalWeight = 0.0;
		myWriter.write("Order\tamountCrates\n");
		for(int i = 0 ; i < orders.size() ; i++)
		{
			FF_paper ff = new FF_paper(orders.get(i));
			List<Crate> crates = ff.computeFF();
			//			myWriter.write(i+"\t"+crates.size()+"\n");
			int test = checkSolution(crates,i);
			if(test==0)myWriter.write(i+"\t"+crates.size()+"\n");
			else myWriter.write(i+"\t"+crates.size()+"\t"+test+"\n");
			myWriter2.write("Order: "+i+"\nCrates: "+crates.size()+"\n");
			int counter = 1;
			totalBins += crates.size();
			for(Crate crate : crates)
			{
				List<Item> items = crate.getItemList();
				double volume = 0.0;
				double weight = 0.0;
				for(Item item : items)
				{
					volume += item.getVolume();
					weight += item.getWeight();
				}
				totalVolume += volume;
				totalWeight += weight;
				double fillRate = Math.round(volume/crate.getVolume()*10000)/100;
				myWriter2.write(counter+"\t"+fillRate+"\t"+weight+"\t");
				for(Item item : items)
				{
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



	public static void getPlotOutput(List<Crate> crates, int crateNumber) throws IOException
	{
		if(crateNumber > crates.size())
		{
			System.out.println("Crate number not feasible");
			return;
		}
		else
		{
			FileWriter myWriter = new FileWriter("outputPlot_FF.txt");
			Crate crate = crates.get(crateNumber-1);
			List<Item> items  =crate.getItemList();
			myWriter.write("id\tx\ty\tz\tw\tl\th\n");
			for(int j = 6 ; j < items.size() ; j++)
			{
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

	public static int checkSolution(List<Crate> crates, int order) {
		int counter = 1;
		for(Crate c : crates)
		{
			List<Item> items = c.getItemList();
			for(int i = 0 ; i < items.size() ; i++)
			{
				Item itemi = items.get(i);
				for(int j = 0 ; j < items.size() ; j++)
				{
					Item itemj = items.get(j);
					if(i==j) continue;
					if(isOverlapping(itemi,itemj))
					{
						return counter;
					}
				}
			}
			counter++;
		}
		return 0;
	}
	
	private static boolean isOverlapping(Item item1, Item item2)
	{
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

		if(x1min < x2max && x2min < x1max 
				&& y1min < y2max && y2min < y1max
				&& z1min < z2max && z2min < z1max)return true;
		else return false;
	}
}