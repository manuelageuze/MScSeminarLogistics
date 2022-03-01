import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Graph {
	private final List<Node> nodes;
	private final List<Arc> arcs;
	private final Map<Node,List<Arc>> outArcs;
	private final Map<Node,List<Arc>> inArcs;

	public Graph() {
		this.nodes = new ArrayList<>();
		this.arcs = new ArrayList<>();
		this.outArcs = new LinkedHashMap<>();
		this.inArcs = new LinkedHashMap<>();
	}

	/**
	 * Add a new node to this graph
	 * @param node the data associated with the node that is added
	 * @throws IllegalArgumentException if the node is already in the graph or is null
	 */
	public void addNode(Node node) throws IllegalArgumentException {
		nodes.add(node);
		inArcs.put(node, new ArrayList<>());
		outArcs.put(node, new ArrayList<>());	
	}

	/**
	 * Adds an arc to this graph. 
	 * @param from the origin node of the arc to be added
	 * @param to the destination of the arc to be added 
	 */
	public void addArc(Node from, Node to) {	
		Arc a = new Arc(from, to);
		outArcs.get(from).add(a);
		inArcs.get(to).add(a);
		arcs.add(a);
	}
	
	public void addArc(Node from, Node to, int weight) {
		Arc a = new Arc(from, to, weight);
		outArcs.get(from).add(a);
		inArcs.get(to).add(a);
		arcs.add(a);		
	}
	
	public void addArc(Arc a) {
		outArcs.get(a.getFrom()).add(a);
		inArcs.get(a.getTo()).add(a);
		arcs.add(a);
	}

	public List<Node> getNodes()
	{
		return Collections.unmodifiableList(nodes);
	}

	public List<Arc> getArcs()
	{
		return Collections.unmodifiableList(arcs);
	}
	
	/**
	 * Gives all the arcs that leave a particular node in the graph.
	 * Note that this list may be empty if no arcs leave this node.
	 * */
	public List<Arc> getOutArcs(Node node){
		return Collections.unmodifiableList(outArcs.get(node));
	}

	
	public List<Arc> getInArcs(Node node) throws IllegalArgumentException
	{
		return Collections.unmodifiableList(inArcs.get(node));
	}

	/**
	 * The total number of nodes in this graph
	 * @return the number of nodes in the graph
	 */
	public int getNumberOfNodes()
	{
		return nodes.size();
	}

	/**
	 * The total number of arcs in this graph
	 * @return the number of arcs in the graph
	 */
	public int getNumberOfArcs()
	{
		return arcs.size();
	}

	/**
	 * Gives the in-degree of a node in the graph.
	 * @param node the node for which we want the in-degree
	 * @return the in-degree of the node
	 * @throws IllegalArgumentException if the node is not in the graph
	 */
	public int getInDegree(Node node) throws IllegalArgumentException
	{
		return getInArcs(node).size();
	}

	/**
	 * Gives the out-degree of a node in the graph
	 * @param node the node for which we want the out-degree
	 * @return the out-degree of the node
	 * @throws IllegalArgumentException if the node is not in the graph
	 */
	public int getOutDegree(Node node) throws IllegalArgumentException
	{
		return getOutArcs(node).size();
	}
	
	/**
	 * Create AH warehouse graph
	 * @param items
	 * @param orders
	 * @return
	 */
	public static Graph createGraph() {
		Graph g = new Graph();
		// Add nodes
		Node S = new Node('S', -1); // Source heeft -1 als index
		Node A = new Node('A', 0);
		Node B = new Node('B', 1);
		Node C = new Node('C', 2);
		Node D = new Node('D', 3);
		Node E = new Node('E', 4);
		Node F = new Node('F', 5);
		Node G = new Node('G', 6);
		Node H = new Node('H', 7);
		Node T = new Node('T', -2); // Sink heeft -2 als index
		g.addNode(S);
		g.addNode(A);
		g.addNode(B);
		g.addNode(C);
		g.addNode(D);
		g.addNode(E);
		g.addNode(F);
		g.addNode(G);
		g.addNode(H);
		g.addNode(T);

		// Add arcs
		g.addArc(S, A, 1);
		g.addArc(S, C, 1);
		g.addArc(S, E, 1);
		g.addArc(S, G, 1);
		g.addArc(A, B, 1);
		g.addArc(A, D, 1);
		g.addArc(A, F, 1);
		g.addArc(A, H, 1);
		g.addArc(B, E, 1);
		g.addArc(B, G, 1);
		g.addArc(B, C, 1);
		g.addArc(B, T, 0);
		g.addArc(C, D, 1);
		g.addArc(C, F, 1);
		g.addArc(C, H, 1);
		g.addArc(D, E, 1);
		g.addArc(D, G, 1);
		g.addArc(D, T, 0);
		g.addArc(E, H, 1);
		g.addArc(E, F, 1);
		g.addArc(F, G, 1);
		g.addArc(F, T, 0);
		g.addArc(G, H, 1);
		g.addArc(H, T, 0);
		return g;	
	}
}

