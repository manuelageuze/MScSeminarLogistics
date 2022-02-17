import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class MultiThread implements Runnable {

	private Order order;
	private int choice_D2_VBO;
	private double lowerBound;
	private Chromosome chromosome;
	private int threadNumber;
	
	public MultiThread(Order order, int choice_D2_VBO, double lowerBound, int threadNumber) throws FileNotFoundException {
		this.order = order;
		this.choice_D2_VBO = choice_D2_VBO;
		this.lowerBound = lowerBound;
		this.threadNumber = threadNumber;
		this.chromosome = null;
	}
	
	@Override
	public void run(){
		Chromosome chrom = BRKGA.solve(this.order, this.lowerBound, this.choice_D2_VBO);
		this.chromosome = chrom;
	}
	
	/** Returns the best chromosome of BRKGA
	 * @return the chromosome
	 */
	public Chromosome getChromosome() {
		return this.chromosome;
	}
	
	/**
	 * Returns current thread number = order number
	 * @return
	 */
	public int getThreadNumber() {
		return this.threadNumber;
	}
	
	@SuppressWarnings("unused")
	private static void printCrate(Order order, Chromosome chrom) throws FileNotFoundException {
		List<Item> items = chrom.getItems();
		File crate = new File("items_crate.txt");
		PrintWriter out = new PrintWriter(crate);
		out.println("id\tx\ty\tz\tw\tl\th");
		int crateNumber = 0;
		for (Item item : items) {
			if (item.getCrateIndex() == crateNumber) {
				String s = (int) item.getItemId() + "\t" + (int) item.getInsertedX() + "\t" + (int) item.getInsertedY() + "\t" + (int) item.getInsertedZ() + "\t" + (int) item.getWidth() + "\t" + (int) item.getLength() + "\t" + (int) item.getHeight();
				out.println(s);
			}
		}
		out.close();
	}

		
}


