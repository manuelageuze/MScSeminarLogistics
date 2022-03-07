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
		this.aislesToVisit = initiateAislesToVisit();
		this.numAisles = aislesToVisit.size();
		this.shortestPath = 0;
	}

	public OrderPicker(int index, List<Crate> crates, int numaisles, List<Integer> aislesToVisit) {
		this.index = index;
		this.crates = crates;
		this.numAisles = numaisles;
		this.shortestPath = 0;
		this.aislesToVisit = aislesToVisit;
	}

	// Compute all aisles that are visited in this list of crates
	public void computeAislesToVisit() {
		List<Integer> ais = new ArrayList<Integer>();
		boolean[] testaisles = new boolean[8];	
		for(Crate crate : crates) {
			for(int z = 0; z < 8; z++) {
				if(crate.getAisles()[z] == true) {
					testaisles[z] = true;;
				}
			}	
		}
		for(int z = 0; z < 8 ; z++) {
			if(testaisles[z] == true) {
				ais.add(z);
			}
		}
		this.aislesToVisit = ais;
		this.numAisles = ais.size();
	}

	public List<Integer> initiateAislesToVisit() {
		boolean[] testaisles = new boolean[8];
		for(Crate crate : this.crates) {
			for(int z = 0; z < 8; z++) {
				if(crate.getAisles()[z] == true) {
					testaisles[z] = true;;
				}
			}	
		}
		List<Integer> aislesToVisit = new ArrayList<Integer>();
		for(int z = 0; z < 8 ; z++) {
			if(testaisles[z] == true) {
				aislesToVisit.add(z);
			}
		}
		return aislesToVisit;
	}

	public List<Integer> getAislesToVisit(){
		return this.aislesToVisit;
	}

	public void setAislesToVisit(ArrayList<Integer> aisles) {
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
