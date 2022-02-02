import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents a blue AH crate
 * @author Geuze et al.
 *	
 */
public class Crate {
		final private double length = 501;
		final private double width = 321;
		final private double height = 273;
		final private double maxWeight = 17000;
		final private double volume = length*width*height;
		private double currentWeight;
		List<EP> ep;
		List<Item> items;
		
	/**
	 * Constructor that creates a new crate with the standard sizes
	 */
	public Crate() {
		this.currentWeight = 0.0;
		this.ep = new ArrayList<>();
		this.items = new ArrayList<>();
	}
	
	public Crate(List<Item> list) {
		this.currentWeight = 0.0;
		this.ep = new ArrayList<>();
		this.items = list; // Gaat dit goed?
	}
	
	public List<EP> getEP() {
		return this.ep;
	}
	
	public void setEPList(List<EP> list){
		this.ep = list;
	}
	
	public void addEPToCrate(EP ep) {
		this.ep.add(ep);
	}
		
	public List<Item> getItemList(){
		return this.items;
	}
	
	public void setItemList(List<Item> list) {
		this.items = new ArrayList<>(list);
	}
	
	public void addItemToCrate(Item i) {
		this.items.add(i);
		this.currentWeight =+ i.getWeight();
	}
	
	public double getCurrentWeight() {
		return this.currentWeight;
	}
	
	public void addtoCurrentWeight(double value) {
		this.currentWeight =+ value;
	}
	
	/**
	 * Method to get the length of a crate
	 * @return length in mm
	 */
	public double getLength() {
		return this.length;
	}
	
	/**
	 * Method to get the width of a crate
	 * @return width in mm
	 */
	public double getWidth() {
		return this.width;
	}
	
	/**
	 * Method to get the height of a crate
	 * @return height in mm
	 */
	public double getHeight() {
		return this.height;
	}
	
	/**
	 * Method to get the maximum weight a crate can handle
	 * @return maxWeight in grams
	 */
	public double getMaxWeight() {
		return this.maxWeight;
	}
	
	/**
	 * Method to get the volume of the crate
	 * @return volume in mm3
	 */
	public double getVolume() {
		return this.volume;
	}
}
