import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import ilog.concert.IloException;

public class Main {

	public static void main(String[] args) throws IloException, IOException {

		Map<Double, Item> items = readItems();
		List<Order> orders = readOrders(items);
		
//		BF bf = new BF(orders.get(0));
//		List<Crate> crates = bf.computeBF();
//		checkSolution(crates,0);
//		System.out.println(checkSolution(crates));
		double totalTime = 0;
		for(int i = 0 ; i < 100 ; i++)
		{
			Long start = System.currentTimeMillis();
			solveFF(orders);
			double timeLB = System.currentTimeMillis()-start;
			totalTime += timeLB/1000;
			System.out.println("Solution time: "+timeLB/1000);
		}
		System.out.println(totalTime);
		getPlotOutput((new FF(orders.get(0))).computeFF(),1);
//		solveFF(orders);
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
	
	private static void solveFF(List<Order> orders) throws IOException {
		FileWriter myWriter = new FileWriter("FF_solution_value.txt");
		FileWriter myWriter2 = new FileWriter("FF_solution.txt");
		double totalBins = 0.0;
		double totalVolume = 0.0;
		double totalWeight = 0.0;
		myWriter.write("Order\tamountCrates\n");
		for(int i = 0 ; i < orders.size() ; i++)
		{
			FF ff = new FF(orders.get(i));
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