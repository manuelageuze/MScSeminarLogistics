import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents a chromosome
 * @author Geuze et al.
 *
 */
public class Chromosome implements Comparable<Chromosome> {
	private double[] BPS;
	private double[] VBO;
	private ArrayList<ArrayList<Integer>> openCrates;
	private List<Item> items;
	private double fitness;
	private int numCrates;
	
	/**
	 * Constructor of a chromosome (for BRKGA)
	 * @param BPS Box Packing Sequence random key: contains value of gene_1,...,gene_n: value in [0,1]
	 * @param VBO Vector of Box Orientation: contains orientation of gene_1,...,gene_n: value in [0,1]
	 * @param fitness Fitness value aNB: adjusted number of bins
	 * @param numCrates Number of crates
	 */
	public Chromosome(double[] BPS, double[] VBO, ArrayList<ArrayList<Integer>> openCrates, List<Item> items, double fitness, int numCrates) {
		this.BPS = BPS;
		this.VBO = VBO;
		this.openCrates = openCrates;
		this.items = items;
		this.fitness = fitness;
		this.numCrates = numCrates;
	}
	
	/** Get genes of BPS
	 * @return the valueBPS
	 */
	public double[] getBPS() {
		return BPS;
	}

	/** Set genes of BPS
	 * @param valueBPS the valueBPS to set
	 */
	public void setBPS(double[] geneBPS) {
		this.BPS = geneBPS;
	}

	/**
	 * Get Vector of Box Orientations
	 * @return VBO
	 */
	public double[] getVBO() {
		return VBO;
	}

	/**
	 * Set Vector of Box Orientations
	 * @param VBO
	 */
	public void setVBO(double[] VBO) {
		this.VBO = VBO;
	}

	/** Get open crates: element is 1 if item is in bin, 0 o.w.
	 * @return openCrates
	 */
	public ArrayList<ArrayList<Integer>> getOpenCrates() {
		return openCrates;
	}

	/** Set open crates: element is 1 if item is in bin, 0 o.w.
	 * @param openCrates the openCrates to set
	 */
	public void setOpenCrates(ArrayList<ArrayList<Integer>> openCrates) {
		this.openCrates = openCrates;
	}
	
	/**
	 * @return the items
	 */
	public List<Item> getItems() {
		return items;
	}

	/**
	 * @param items the items to set
	 */
	public void setItems(List<Item> items) {
		this.items = items;
	}	
	
	/**
	 * Get fitness value aNB
	 * @return aNB
	 */
	public double getFitness() {
		return fitness;
	}

	/**
	 * Set fitness value aNB
	 * @param fitness
	 */
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	
	/** Get number of crates NB
	 * @return numCrates
	 */
	public int getNumCrates() {
		return numCrates;
	}

	/** Set number of crates NB
	 * @param numCrates
	 */
	public void setNumCrates(int numCrates) {
		this.numCrates = numCrates;
	}
	
	/**
	 * Compare chromosomes based on fitness: ascending
	 */
	@Override
	public int compareTo(Chromosome chromosome) {
		if (this.fitness > chromosome.getFitness())
			return 1;
		else if (this.fitness < chromosome.getFitness())
			return -1;
		else return 0;
	}
}