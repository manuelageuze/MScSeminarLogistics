import java.util.ArrayList;
import java.util.List;

public class ShortestPath {
	private List<Crate> crates;
	private List<ArrayList<Integer>> aisles;
	private List<Integer> pathSizes; // Length of the shortest paths for each crate 
	private List<ArrayList<Integer>> shortestPathList;
	private final Graph g;
	private int pathSizeOneCrate;

 	public ShortestPath(List<Crate> crates, Graph g) {
		this.crates = crates;
		this.g = g;
		this.aisles = new ArrayList<ArrayList<Integer>>();
		this.aisles = this.computeAisles();
		this.shortestPathList = this.computeShortestPathList();
		this.pathSizes = new ArrayList<Integer>();
		this.pathSizes = this.computeShortestPathListLength();
		this.pathSizeOneCrate = 0;
	}

	public List<Integer> getPathSizes(){
		return this.pathSizes;
	}
	public List<Crate> getCrates() {
		return this.crates;
	}

	/**
	 * Method that gives a list containing the aisle numbers that a crate must go through, for each crate
	 * @return
	 */
	public List<ArrayList<Integer>> computeAisles() {
		for(Crate crate : crates) {
			ArrayList<Integer> aisle = new ArrayList<Integer>(); 
			for(int j = 0; j < 8; j++) {
				if(crate.getAisles()[j] == true) {
					aisle.add(j);
					crate.increaseNumAisles();
				}
			}
			crate.setAisleList(aisle);
			aisles.add(aisle);
		}
		return aisles;
	}
	
	/**
	 * Method that returns the number of aisles that a crate must to go through, for each crate
	 * @return
	 */
	public List<Integer> computeShortestPathListLength(){
		List<Integer> list = new ArrayList<Integer>();
		for(int i = 0; i < crates.size(); i++) {
			int value = shortestPathList.get(i).size();
			list.add(value);
			pathSizes.add(value);
			crates.get(i).setShortestPathLength(value);
		}
		return list;
	}
	
	/**
	 * Method that computes the shortest path for each crate
	 * @return
	 */
	public List<ArrayList<Integer>> computeShortestPathList(){
		List<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i < crates.size();i++) {
			ArrayList<Integer> aisle = new ArrayList<Integer>();
			for(int j = 0; j < crates.get(i).getAisleList().size(); j++) {
				int index = crates.get(i).getAisleList().get(j);
				if(j == 0) {
					if(index % 2 == 0) {
						// Dan is het goed
						aisle.add(index);
					} 
					else {
						aisle.add(index - 1);
						aisle.add(index);
					}
				}
				else {
					int prevIndex = crates.get(i).getAisleList().get(j - 1);
					int space = index - prevIndex;
					if(space % 2 != 0) { // dan is het goed
						aisle.add(index);
					}
					else {
						aisle.add(index - 1);
						aisle.add(index);
					}
				}
				if(j == crates.get(i).getAisleList().size() - 1) {
					// Als laatste Isle oneven is, kan je meteen naar eindpunt. Doe niets. Laatste ail al toegevoegd in vorige if statement
					if(index % 2 == 0) { 
						// Als laatste aisle even is, moet je nog terug door een aisle
						aisle.add(index + 1);
					}
				}	
			}
			crates.get(i).setShortestPathList(aisle);
			list.add(aisle);
			this.shortestPathList = list;
		}
		return list;
	}
	
	public Integer computeShortestPathOneCrate(List<Integer> aisles) {
		int length = 0;
		// Add length from s to first aisle
		length = length + (int) shortestPath(g, 0, aisles.get(0)+1);
		for(int j = 0; j < aisles.size() - 1; j++) {
			length = (int) (length + shortestPath(g, aisles.get(j)+1, aisles.get(j+1)+1));
		}
		// add length form last aisle to t
		length = length + (int) shortestPath(g, aisles.get(aisles.size() - 1) + 1, 9);		
		pathSizeOneCrate = length;
		return pathSizeOneCrate;
	}
	
	public List<Integer> computeShortestPathSize() {
		for(int i = 0; i < aisles.size();i++) { // for all crates
			int length = 0;
			// Add length from s to first aisle
			length = length + (int) shortestPath(g, 0, aisles.get(i).get(0)+1);
			for(int j = 0; j < aisles.get(i).size() - 1; j++) {
				length = (int) (length + shortestPath(g, aisles.get(i).get(j)+1, aisles.get(i).get(j+1)+1));
			}
			// add length form last aisle to t
			length = length + (int) shortestPath(g, aisles.get(i).get(aisles.get(i).size() - 1) + 1, 9);
			pathSizes.add(length);
			crates.get(i).setShortestPathLength(length);
		}
		return pathSizes;
	}
	
	public double shortestPath(Graph g, int beginIndex, int endIndex) {
		List<Node> nodes = g.getNodes();
		for(int i = 0; i < nodes.size(); i++) { // Set all things to zero or null after beginIndex
			nodes.get(i).setDijkstra(Double.POSITIVE_INFINITY);
			nodes.get(i).setPredecessor(null);
		}
		nodes.get(beginIndex).setDijkstra(0.0); // Set start distance to zero

		for(int i = beginIndex + 1; i <= endIndex;i++) { // For all nodes starting from beginnode
			// Go over all predecessors
			List<Arc> inarcs = g.getInArcs(nodes.get(i));// Get the inarcs of the node
			double minCost = Double.POSITIVE_INFINITY;
			Node predecessor = null;
			for(int j = 0; j < inarcs.size(); j++) {
				double cost = inarcs.get(j).getFrom().getDijkstra() + inarcs.get(j).getWeight();
				if(cost < minCost) { // If better, remember it
					minCost = cost;
					predecessor = inarcs.get(j).getFrom();
				}
			}
			nodes.get(i).setDijkstra(minCost);
			nodes.get(i).setPredecessor(predecessor);
		}
		double shortestPathCost = nodes.get(endIndex).getDijkstra();
		return shortestPathCost;	
	}


}
