import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import ilog.concert.IloException;

public class Test {

	public static void main(String[] args) throws IloException, FileNotFoundException {
		System.out.println("Test");
		// Variables, parameters and results
		Map<Double, Item> items = MainBRKGA.readItems();
		List<Order> orders = MainBRKGA.readOrders(items);
		Graph graph = Graph.createGraph();
//		Crate crate = new Crate();
//		int choiceSplit = 2; // Choice for order splitting or not: 1 for no splitting, 2 for splitting
	//	int choiceAisles = 2; // Choice for incorporating number of aisles or not: 1 for not incorporating, 2 for only incorporating aisles, 3 for incorporating aisles and fill rate
		
		int instance = 124;
		double lowerbound = LowerBoundModel.setCoveringLB(orders.get(instance), items);
		System.out.println("Lower bound = " + lowerbound);
		Chromosome chrom = BRKGA.solve(orders.get(instance), lowerbound, 1, 0);
		int thisNumCrates = chrom.getNumCrates();
		System.out.println("Num crates original GA: " + thisNumCrates);
		List<Crate> crates = chrom.getCrates();
//		
//		Chromosome chrom2 = BRKGA.solve(orders.get(instance), lowerbound, choiceAisles, thisNumCrates);
//		int thisNumCrates2 = chrom2.getNumCrates();
//		List<Crate> crates2 = chrom2.getCrates();
//		System.out.println("Num crates including aisles: " + thisNumCrates2);
//		ShortestPath shortestPath2 = new ShortestPath(crates2, graph);
//		int totalNumAisles2 = shortestPath2.computeTotalPathLength(crates2, graph);
//		System.out.println("Num aisles including aisles: " + totalNumAisles2);
		
		// List<Chromosome> chromosomes = MainBRKGA.readFileOriginalGAChrom(new File("competitionAnswersGroup5.csv"), items);
	}

	@SuppressWarnings({ "unused", "resource"})
	private static void printCrate(Order order, Chromosome chrom) throws FileNotFoundException {
		List<Item> items = order.getItems();
		File crate = new File("items_crate.txt");
		PrintWriter out = new PrintWriter(crate);
		out.println("id\tx\ty\tz\tw\tl\th");
		int crateNumber = 0;
		for (Item item : items) {
			if (item.getCrateIndex() == crateNumber) {
				String s = item.getItemId() + "\t" + item.getInsertedX() + "\t" + item.getInsertedY() + "\t" + item.getInsertedZ() + "\t" + item.getWidth() + "\t" + item.getLength() + "\t" + item.getHeight();
				System.out.println(s);
			}
		}
	}

}
