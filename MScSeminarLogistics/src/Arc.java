
public class Arc {

	private final Node from; // An arc goes from one duty to another duty
	private final Node to;
	private int weight;

	/**
	 * Arc in graph with default weight of 0
	 * @param from node from 
	 * @param to node to
	 */
	public Arc(Node from, Node to) {
		this.from = from;
		this.to = to;
		this.weight = 0;
	}
	
	public Arc(Node from, Node to, int weight) {
		this.from = from;
		this.to = to;
		this.weight = weight;
	}

	public Node getFrom() {
		return from;
	}

	public Node getTo()	{
		return to;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public void setWeight(int w) {
		this.weight = w;
	}

	@Override
	public String toString()
	{
		return "Arc [from=" + from + ", to=" + to + "]";
	}

}