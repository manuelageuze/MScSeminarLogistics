import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents a blue AH crate
 * @author Geuze et al.
 *	
 */
public class Crate implements Comparable<Crate> {
	final private double length = 501;
	final private double width = 321;
	final private double height = 273;
	final private double maxWeight = 17000;
	final private double volume = length*width*height;
	private double currentWeight;
	private List<EP> ep;
	private List<Item> items;
	private ArrayList<Integer> openCrate;
	private boolean[] aisles;
	private int numAisles;
	private List<Integer> aisleList;
	private int shortestPathLength;
	private ArrayList<Integer> shortestPathList;
	private int orderIndex;

	/**
	 * Constructor that creates a new crate with the standard sizes
	 */
	public Crate() {
		this.currentWeight = 0.0;
		this.ep = new ArrayList<>();
		this.items = new ArrayList<>();
		this.openCrate = new ArrayList<Integer>();
		this.aisles = new boolean[8];
		this.aisleList = new ArrayList<Integer>();
		this.orderIndex = 0;
		this.shortestPathLength = 0;
		this.shortestPathList = new ArrayList<Integer>();
	}

	public Crate(List<Item> list) {
		this.currentWeight = 0.0;
		this.ep = new ArrayList<>();
		this.items = list; // Gaat dit goed?
		this.aisles = new boolean[8];
		this.aisleList = new ArrayList<Integer>();
		this.orderIndex = 0;
		this.shortestPathLength = 0;
		this.shortestPathList = new ArrayList<Integer>();
	}
	
	public ArrayList<Integer> getShortestPathList(){
		return this.shortestPathList;
	}
	
	public void setShortestPathList(ArrayList<Integer> list) {
		this.shortestPathList = list;
	}

	public int getShortestPathLength() {
		return this.shortestPathLength;
	}

	public void setShortestPathLength(int value) {
		this.shortestPathLength = value;
	}

	public int getOrderIndex() {
		return this.orderIndex;
	}

	public void setOrderIndex(int value) {
		this.orderIndex = value;
	}

	public void setAisleList(List<Integer> list) {
		this.aisleList = list;
	}
	public List<Integer> getAisleList(){
		return this.aisleList;
	}

	public int getNumAisles() {
		return this.numAisles;
	}

	public void increaseNumAisles() {
		this.numAisles = this.numAisles + 1;
	}

	/**
	 * Method to get the array of aisles used in the crate
	 * @return aisles
	 */
	public boolean[] getAisles() {
		return this.aisles;
	}

	/**
	 * Method to set the aisle in the crate to either used or not used
	 * @param index aisle/ index of aisle to set to s
	 * @param s true or false, to set the boolean to.
	 */
	public void setAisles(int index, boolean s) {
		this.aisles[index] = s;
	}
	
	/**
	 * Method that sets booleans of all aisles, iterates over all items in crate
	 */
	public void setAllAisles() {
		for (Item item : this.items) {
			this.aisles[item.getAisle()] = true;
		}
	}

	/**
	 * Method that gets the list op extreme points in the crate
	 * @return extreme point list
	 */
	public List<EP> getEP() {
		return this.ep;
	}

	/**
	 * Method that sets the list of extreme points equal to the input
	 * @param list
	 */
	public void setEPList(List<EP> list){
		this.ep = list;
	}

	/**
	 * Method that adds an extreme point to the list of extreme points in the crate
	 * @param ep Extreme point to add
	 */
	public void addEPToCrate(EP ep) {
		this.ep.add(ep);
	}

	/**
	 * Method that gets the list of items in the crate
	 * @return itemlist list of items in crate
	 */
	public List<Item> getItemList(){
		return this.items;
	}

	/**
	 * Method that sets the list of items in the crate to list
	 * @param list itemlist to set the list of items to
	 */
	public void setItemList(List<Item> list) {
		this.items = new ArrayList<>(list);
	}

	/**
	 * Add an item to the crate, updates itemlist and weight of crate
	 * @param i
	 */
	public void addItemToCrate(Item i) {
		this.items.add(i);
		this.currentWeight += i.getWeight();
	}

	/**
	 * Get the current weight of a items in the crate
	 * @return weight
	 */
	public double getCurrentWeight() {
		return this.currentWeight;
	}

	/**
	 * Update the weight of the crate
	 * @param value weight to add to crate
	 */
	public void addtoCurrentWeight(double value) {
		this.currentWeight += value;
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

	public ArrayList<Integer> getOpenCrate() {
		return openCrate;
	}

	public void setOpenCrate(ArrayList<Integer> openCrate) {
		this.openCrate = openCrate;
	}
	
	@Override
	public int compareTo(Crate o) {
		// Sorteer op lengte shortest path, 8 eerst, 2 laatste
		double spLength = this.shortestPathLength;
		double oSpLength = o.shortestPathLength;
		if(spLength > oSpLength) {
			return -1;
		}
		else if(spLength < oSpLength) {
			return 1;
		}
		return 0;
	}

}
