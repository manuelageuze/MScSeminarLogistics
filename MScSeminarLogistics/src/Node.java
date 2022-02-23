
public class Node {
	private int index;
	private char letter;
	private double dijkstra;
	private Node predecessor;
	
	public Node(char letter, int index) {
		this.letter = letter;
		this.index = index;
		this.dijkstra = Double.POSITIVE_INFINITY;
		this.predecessor = null;
	}
	
	public void setPredecessor(Node n) {
		this.predecessor = n;
	}
	
	public Node getPredecessor() {
		return this.predecessor;
	}
	
	public int getIndex() {
		return index;
	}
	
	public char getLetter() {
		return this.letter;
	}
	
	public void setDijkstra(double value) {
		this.dijkstra = value;
	}
	
	public void increaseDijkstr(double value) {
		this.dijkstra = this.dijkstra + value;
	}
	
	public double getDijkstra() {
		return this.dijkstra;
	}
	
}
