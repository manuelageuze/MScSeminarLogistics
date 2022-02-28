import java.util.ArrayList;
import java.util.List;

public class OrderPicker {
	private int index;
	private List<Crate> crates; // List of crates each order picker must pick (at most 8)
	private int numAisles;
	private int shortestPath;
	private List<Integer> aislesToVisit;

	public OrderPicker(int index, List<Crate> crates) {
		this.index = index;
		this.crates = crates;
		this.numAisles = 0;
		this.shortestPath = 0;
		this.aislesToVisit = new ArrayList<Integer>();
	}
	
	public OrderPicker(int index, List<Crate> crates, int numaisles, List<Integer> aislesToVisit) {
		this.index = index;
		this.crates = crates;
		this.numAisles = numaisles;
		this.shortestPath = 0;
		this.aislesToVisit = aislesToVisit;
	}
	
	public List<Integer> getAislesToVisist(){
		return this.aislesToVisit;
	}
	
	public void setAislesToVisist(ArrayList<Integer> aisles) {
		this.aislesToVisit = aisles;
	}
	
	public void setShortestPath(int value) {
		this.shortestPath = value;
	}
	
	public int getShortestPath() {
		return this.shortestPath;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public List<Crate> getCrates() {
		return this.crates;
	}
	
	public int getNumAisles() {
		return this.numAisles;
	}
	
	
	
}
