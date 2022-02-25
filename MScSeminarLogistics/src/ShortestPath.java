import java.util.ArrayList;
import java.util.List;

public class ShortestPath {
	private List<Crate> crates;
	private List<ArrayList<Integer>> aisles;
	private List<Integer> pathSizes; // Length of the shortest paths for each crate 
	private final Graph g;
	private int pathSizeOneCrate;

 	public ShortestPath(List<Crate> crates, Graph g) {
		this.crates = crates;
		this.pathSizes = new ArrayList<>();
		this.aisles = new ArrayList<ArrayList<Integer>>();
		this.g = g;
		this.pathSizeOneCrate = 0;
	}

	public List<Integer> getPathSizes(){
		return this.pathSizes;
	}
	public List<Crate> getCrates() {
		return this.crates;
	}

	public List<ArrayList<Integer>> computeAisles() {
		for(Crate crate : crates) {
			ArrayList<Integer> ail = new ArrayList<Integer>(); 
			for(int j = 0; j < 8; j++) {
				if(crate.getAisles()[j] == true) {
					ail.add(j);
					crate.increaseNumAisles();
				}
			}
			crate.setAisleList(ail);
			aisles.add(ail);
		}
		return aisles;
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

	public List<Integer> computeShortestPath() {
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
