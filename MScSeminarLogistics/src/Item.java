import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Class that is one item
 * @author Geuze et al.
 * 
 */
public class Item implements Comparable<Item> {
	private final double itemId;
	private final double width;
	private final double length;
	private final double height;
	private final double weight;
	private final double volume;
	private double insertedX;
	private double insertedY;
	private double insertedZ;
	private int crateIndex;

	/**
	 * Constructor that creates a new item
	 * @param id item_id 0 to 499
	 * @param width in mm
	 * @param length in mm
	 * @param height in mm
	 * @param weight in grams
	 */
	public Item(double itemId, double width, double length, double height, double weight) {
		this.itemId = itemId;
		this.width = width;
		this.length = length;
		this.height = height;
		this.weight = weight;
		this.volume = width*length*height;
		this.insertedX = 0.0;
		this.insertedY = 0.0;
		this.insertedZ = 0.0;
		this.crateIndex = -1;
	}

	/**
	 * Constructor that creates a new item
	 * @param id item_id 0 to 499
	 * @param width in mm
	 * @param length in mm
	 * @param height in mm
	 * @param weight in grams
	 */
	public Item(double itemId, double width, double length, double height, double weight, double insertedX, double insertedY, double insertedZ, int crateIndex) {
		this.itemId = itemId;
		this.width = width;
		this.length = length;
		this.height = height;
		this.weight = weight;
		this.volume = width*length*height;
		this.insertedX = insertedX;
		this.insertedY = insertedY;
		this.insertedZ = insertedZ;
		this.crateIndex = crateIndex;
	}

	/**
	 * Method to obtain the ID of the item
	 * @return itemId
	 */
	public double getItemId() {
		return this.itemId;
	}

	/**
	 * Method to obtain the width of the item
	 * @return width in mm
	 */
	public double getWidth() {
		return this.width;
	}

	/**
	 * Method to obtain the length of the item
	 * @return length in mm
	 */
	public double getLength() {
		return this.length;
	}

	/**
	 * Method to obtain the height of the item
	 * @return height in mm
	 */
	public double getHeight() {
		return this.height;
	}

	/**
	 * Method to obtain the weight of the item
	 * @return weight in grams
	 */
	public double getWeight() {
		return this.weight;
	}

	/**
	 * Method to obtain the volume of the item
	 * @return volume in mm3
	 */
	public double getVolume() {
		return this.volume;
	}

	public double getInsertedX() {
		return this.insertedX;
	}

	public void setInsertedX(double value) {
		this.insertedX = value;
	}

	public double getInsertedY() {
		return this.insertedY;
	}

	public void setInsertedY(double value) {
		this.insertedY = value;
	}

	public double getInsertedZ() {
		return this.insertedZ;
	}

	public void setInsertedZ(double value) {
		this.insertedZ = value;
	}
	
	public int getCrateIndex() {
		return this.crateIndex;
	}

	/**
	 * Method to read the item file
	 * @return items Map of all items, with their ID as key
	 * @throws FileNotFoundException 
	 */
	public static Map<Double,Item> readItems(File ItemFile) throws FileNotFoundException {
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
					Item item = new Item(itemId, width, length, height, weight);
					items.put(itemId, item);
				}
				else if(itemId == 161 || itemId == 16 || itemId == 180 || itemId == 199 || itemId == 307 || itemId == 223 || itemId == 285) {
					double width = Double.parseDouble(parts[3]);
					double length = Double.parseDouble(parts[4]);
					double height = Double.parseDouble((parts[5]));
					double weight = Double.parseDouble(parts[6]);
					Item item = new Item(itemId, width, length, height, weight);
					items.put(itemId, item);
				}
				else {
					double width = Double.parseDouble(parts[2]);
					double length = Double.parseDouble(parts[3]);
					double height = Double.parseDouble((parts[4]));
					double weight = Double.parseDouble(parts[5]);
					Item item = new Item(itemId, width, length, height, weight);
					items.put(itemId, item);
				}
			}
			return items;	
		}	
	}

	@Override
	public int compareTo(Item o) {		
		// Area-Height
		double area = this.width*this.length;
		double oArea = o.width*o.length;
		if(area > oArea) {
			return -1;
		}
		else if(area < oArea) {
			return 1;
		}
		else { // Als gelijk, sorteer op height
			if( this.height > o.height) {
				return -1;
			}
			else if(this.height < o.height) {
				return 1;
			}
			return 0;
		}
	}

}
