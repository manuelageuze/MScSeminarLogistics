import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
		//		BRKGA.solve(order, 1, 1);

//		int NB = 0;
//		for (int j=0; j < 10; j++) {
			List<Item> items = new ArrayList<Item>();
			Random rand = new Random();
			for (int i=0; i < 50; i++) {
				double width = 1+34*rand.nextDouble();
				double height = 1+34*rand.nextDouble();
				double length = 1+34*rand.nextDouble();
				Item item = new Item(i, width, length, height, 0);
				items.add(item);
			}
			Order order = new Order(items, 1000);
			BRKGA.solve(order, 0, 1);
//		}
//		System.out.println(NB/10);
	}

}
