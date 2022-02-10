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
		int choice_D2_VBO = 2; // TODO: 1 if DFTRC-2^2 is used, 2 if DFTRC-2-VBO is used
		BF bf = new BF(orders.get(362));
		List<Crate> crates = bf.computeBF();
		checkSolution(crates,584);
//		System.out.println(checkSolution(crates));
		// Compute lower bound and write file
		//out.println("instance minimum_number_of_crates");
//		Long start = System.currentTimeMillis();
//		solveLP(orders, items);
//		Long timeLB = System.currentTimeMillis()-start;
//		start = System.currentTimeMillis();
//		solveBF(orders);
//		Long timeBF = System.currentTimeMillis()-start;
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
		for(Order order : orders)
		{
			int LB = (int)LowerBoundModel.setCoveringLB(order, items);
			int id = (int) order.getOrderId();
			myWriter.write(id+"\t"+LB+"\n");
		}
//		myWriter.close();
//		for(int i = 900 ; i < 1000 ; i++)
//		{
//			int LB = (int)LowerBoundModel.setCoveringLB(orders.get(i), items);
//		}
//		int LB = (int)LowerBoundModel.setCoveringLB(orders.get(27), items);
	}

	private static void solveBF(List<Order> orders) throws IOException {
		FileWriter myWriter = new FileWriter("BF_solution_value.txt");
		FileWriter myWriter2 = new FileWriter("BF_solution.txt");
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
			for(Crate crate : crates)
			{
				List<Item> items = crate.getItemList();
				double volume = 0.0;
				for(Item item : items)
				{
					volume += item.getVolume();
				}
				double fillRate = Math.round(volume/crate.getVolume()*10000)/100;
				myWriter2.write(counter+"\t"+fillRate+"\t");
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
					double x_i = itemi.getinsertedx();double x_j = itemj.getinsertedx();
					double y_i = itemi.getinsertedy();double y_j = itemj.getinsertedy();
					double z_i = itemi.getinsertedz();double z_j = itemj.getinsertedz();
					if(x_i < x_j && x_j < x_i+itemi.getWidth()){
						if(y_i < y_j && y_j < y_i + itemi.getLength()){
							if(z_i < z_j && z_j < z_i + itemi.getHeight()) {
								System.out.println("Order: "+order+", Crate "+counter+".");
								System.out.println("Item i: "+(int)itemi.getItemId());
								System.out.println("("+x_i+","+y_i+","+z_i+")"+
											"("+itemi.getWidth()+","+itemi.getLength()+","+itemi.getHeight()+")");
								System.out.println("Item j: "+(int)itemj.getItemId());
								System.out.println("("+x_j+","+y_j+","+z_j+")\n");
								return counter;
							}
						}
					}
				}
			}
			counter++;
		}
		return 0;
	}
}