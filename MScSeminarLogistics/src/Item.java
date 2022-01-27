import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Class that is one item
 * @author Geuze et al.
 * 
 */
public class Item {
	private final double itemId;
	private final double width;
	private final double length;
	private final double height;
	private final double weight;
	private final double volume;

	/**
	 * Conscructor that creates a new item
	 * @param id item_id 0 tot 499
	 * @param width 
	 * @param length
	 * @param height
	 * @param weight
	 */
	public Item(double id, double width, double length, double height, double weight) {
		this.itemId = id;
		this.width = width;
		this.length = length;
		this.height = height;
		this.weight = weight;
		this.volume = width*length*height;
	}

	/**
	 * method to obtain the id of the item
	 * @return id
	 */
	public double getId() {
		return this.itemId;
	}

	/**
	 * method to obtain the width of the item
	 * @return width
	 */
	public double getWidth() {
		return this.width;
	}

	/**
	 * method to obtain the length of the item
	 * @return length
	 */
	public double getLength() {
		return this.length;
	}

	/**
	 * method to obtain the height of the item
	 * @return height
	 */
	public double getHeight() {
		return this.height;
	}

	/**
	 * method to obtain the weight of the item
	 * @return weight
	 */
	public double getWeight() {
		return this.weight;
	}

	/**
	 * method to obtain the volume of the item
	 * @return volume
	 */
	public double getVolume() {
		return this.volume;
	}

	/**
	 * method to read the itemfile
	 * @return itemlist Map of all items, with their ID as key
	 * @throws FileNotFoundException 
	 */
	public static Map<Double,Item> readItem(File ItemFile) throws FileNotFoundException {
		try(Scanner s = new Scanner(ItemFile)){
			s.useDelimiter(",");
			Map<Double, Item> items = new HashMap<>();
			//Ignore first description list
			s.nextLine();
			while(s.hasNextLine()) {
				String line = s.nextLine();
				String[] parts = line.split(",");
				double itemId = Double.parseDouble(parts[0]);
				if(itemId == 455.0) {
					double width = Double.parseDouble(parts[4]);
					double length = Double.parseDouble(parts[5]);
					double height = Double.parseDouble(parts[6]);
					double weight = Double.parseDouble(parts[7]);
					Item item = new Item(itemId,width,length,height,weight);
					items.put(itemId, item);
				}
				else if(itemId == 161 || itemId == 16 || itemId == 180 || itemId == 199 || itemId == 307 || itemId == 223 || itemId == 285) {
					double width = Double.parseDouble(parts[3]);
					double length = Double.parseDouble(parts[4]);
					double height = Double.parseDouble((parts[5]));
					double weight = Double.parseDouble(parts[6]);

					Item item = new Item(itemId,width,length,height,weight);
					items.put(itemId, item);
				}
				
				else {
					double width = Double.parseDouble(parts[2]);
					double length = Double.parseDouble(parts[3]);
					double height = Double.parseDouble((parts[4]));
					double weight = Double.parseDouble(parts[5]);

					Item item = new Item(itemId,width,length,height,weight);
					items.put(itemId, item);
				}
			}
			return items;	
		}	
	}

}
