import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import ilog.concert.IloException;

public class Main {

	public static void main(String[] args) throws IloException, IOException {

		Map<Double, Item> items = readItems();
		List<Order> orders = readOrders(items);

		Graph g = createGraph(items,orders);

		//		FileWriter w = new FileWriter("ItemsAisles.txt");
		//		for(Map.Entry<Double, Item> entry: items.entrySet()) {
		//			w.write(entry.getValue().getItemId()+" "+entry.getValue().getAisle()+ "\n");
		//		}
		//		w.close();


		//		BF bf = new BF(orders.get(0));
		//		List<Crate> crates = bf.computeBF();
		//		checkSolution(crates,0);
		//		System.out.println(checkSolution(crates));
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
		//		getPlotOutput((new FF(orders.get(0))).computeFF(),1);

		List<ArrayList<Integer>> aisles = solveFF(orders);
	
		List<Integer> numAisleTraversed = new ArrayList<>();
		FileWriter myWriter = new FileWriter("FF_shortestpath.txt");
		for(int i = 0; i < aisles.size();i++) { // for all crates
			int length = 0;
			// Add length from s to first aisle
			length = length + (int) shortestPath(g, 0, aisles.get(i).get(0)+1);
			for(int j = 0; j < aisles.get(i).size() - 1; j++) {
				length = (int) (length + shortestPath(g, aisles.get(i).get(j)+1, aisles.get(i).get(j+1)+1));
			}
			// add length form last aisle to t
			length = length + (int) shortestPath(g, aisles.get(i).get(aisles.get(i).size() - 1) + 1, 9);
			numAisleTraversed.add(length);
			myWriter.write(i + " " + length + "\n");

		}
		myWriter.close();
		int k = 0;
		k = k + 1;

		//		solveFFPaper(orders);

		// Compute lower bound and write file
		//out.println("instance minimum_number_of_crates");
		//		Long start = System.currentTimeMillis();
		//		solveLP(orders, items);
		//		double timeLB = System.currentTimeMillis()-start;
		//		System.out.println("Solution time: "+timeLB/1000);
		//		start = System.currentTimeMillis();
		//		getPlotOutput(orders.get(457),2);
		//		solveBF(orders);

		//		double timeBF = System.currentTimeMillis()-start;
		//		System.out.println("Solution time: "+timeBF/1000);
		//		System.out.println("Time:\nLB\tBF\n"+timeLB+"\t"+timeBF);
		//		compair(new File("LB_solution_value.txt"),new File("BF_solution_value.txt"),orders);
	}

	private static Graph createGraph(Map<Double, Item> items, List<Order> orders) {
		Graph g = new Graph();

		// Add nodes
		Node S = new Node('S', -1); // Source heeft -1 als index
		Node A = new Node('A', 0);
		Node B = new Node('B', 1);
		Node C = new Node('C', 2);
		Node D = new Node('D', 3);
		Node E = new Node('E', 4);
		Node F = new Node('F', 5);
		Node G = new Node('G', 6);
		Node H = new Node('H', 7);
		Node T = new Node('T', -2); // Sink heeft -2 als index
		g.addNode(S);
		g.addNode(A);
		g.addNode(B);
		g.addNode(C);
		g.addNode(D);
		g.addNode(E);
		g.addNode(F);
		g.addNode(G);
		g.addNode(H);
		g.addNode(T);

		// Add arcs
		g.addArc(S, A, 1);
		g.addArc(S, C, 1);
		g.addArc(S, E, 1);
		g.addArc(S, G, 1);
		g.addArc(A, B, 1);
		g.addArc(A, D, 1);
		g.addArc(A, F, 1);
		g.addArc(A, H, 1);
		g.addArc(B, E, 1);
		g.addArc(B, G, 1);
		g.addArc(B, C, 1);
		g.addArc(B, T, 0);
		g.addArc(C, D, 1);
		g.addArc(C, F, 1);
		g.addArc(C, H, 1);
		g.addArc(D, E, 1);
		g.addArc(D, G, 1);
		g.addArc(D, T, 0);
		g.addArc(E, H, 1);
		g.addArc(E, F, 1);
		g.addArc(F, G, 1);
		g.addArc(F, T, 0);
		g.addArc(G, H, 1);
		g.addArc(H, T, 0);

		return g;	
	}

	/**
	 * Computes shortest path from one node to another
	 * @param g
	 * @param beginIndex index of beginnode in list of nodes, So node A has index 1.
	 * @param endIndex Node T has index 9
	 * @return
	 */
	private static double shortestPath(Graph g, int beginIndex, int endIndex) {
		List<Node> nodes = g.getNodes();
		for(int i = 0; i < nodes.size(); i++) { // Set all things to zero or null after beginIndex
			nodes.get(i).setDijkstra(Double.POSITIVE_INFINITY);
			nodes.get(i).setPredecessor(null);
		}
		nodes.get(beginIndex).setDijkstra(0.0); // Set start distance to zero

		for(int i = beginIndex + 1; i <= endIndex;i++) { // For all nodes starting from beginnode
			// Go over all predecessors
			List<Arc> inarcs = g.getInArcs(nodes.get(i));// Get the inarcs of the node
			double minCost = Double.POSITIVE_INFINITY;
			Node predecessor = null;
			for(int j = 0; j < inarcs.size(); j++) {
				double cost = inarcs.get(j).getFrom().getDijkstra() + inarcs.get(j).getWeight();
				if(cost < minCost) { // If better, remember it
					minCost = cost;
					predecessor = inarcs.get(j).getFrom();
				}
			}
			nodes.get(i).setDijkstra(minCost);
			nodes.get(i).setPredecessor(predecessor);
		}
		double shortestPathCost = nodes.get(endIndex).getDijkstra();
		return shortestPathCost;	
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

	private static List<ArrayList<Integer>> solveFF(List<Order> orders) throws IOException {
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
			//			myWriter.write(i+"\t"+crates.size()+"\n");
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

	private static void solveBF(List<Order> orders) throws IOException {
		FileWriter myWriter = new FileWriter("BF_solution_value.txt");
		FileWriter myWriter2 = new FileWriter("BF_solution.txt");
		double totalBins = 0.0;
		double totalVolume = 0.0;
		double totalWeight = 0.0;
		myWriter.write("Order\tamountCrates\n");
		for(int i = 0 ; i < orders.size() ; i++)
		{
			BF bf = new BF(orders.get(i));
			List<Crate> crates = bf.computeBF();
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

	public static void getPlotOutput_BF(Order order, int crateNumber) throws IOException
	{
		BF bf = new BF(order);
		List<Crate> crates = bf.computeBF();
		if(crateNumber > crates.size())
		{
			System.out.println("Crate number not feasible");
			return;
		}
		else
		{
			FileWriter myWriter = new FileWriter("outputPlot.txt");
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