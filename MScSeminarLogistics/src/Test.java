import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test {

	public static void main(String[] args) {
		System.out.println("Test");

//		Item item1 = new Item(500, 50.0, 50.0, 50.0, 50.0);
//		Item item2 = new Item(501, 100.0, 100.0, 100.0, 100.0);
//		Item item3 = new Item(502, 20.0, 20.0, 20.0, 20.0);
//		List<Item> items = new ArrayList<Item>();
//		items.add(item1);
//		items.add(item2);
//		items.add(item3);
//		Order order = new Order(items, 1000);
//		Chromosome chrom = BRKGA.solve(order, 1, 1);
	}

	@SuppressWarnings({ "unused", "resource" })
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
