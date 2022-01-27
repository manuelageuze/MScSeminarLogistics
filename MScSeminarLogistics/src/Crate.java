/**
 * Class that represents a blue AH crate
 * @author Geuze et al.
 *	
 */
public class Crate {
		final private double length = 501;
		final private double width = 321;
		final private double heigth = 273;
		final private double maxWeight = 117000;
		final private double volume = length*width*heigth;
		
	/**
	 * Constructor that creates a new crate with the standard sizes
	 */
	public Crate() {
		
	}
	
	/**
	 * method to get the length of a crate
	 * @return length
	 */
	public double getLength() {
		return this.length;
	}
	
	/**
	 * method to get the width of a crate
	 * @return
	 */
	public double getWidth() {
		return this.width;
	}
	
	/**
	 * method to get the height of a crate
	 * @return
	 */
	public double getHeigth() {
		return this.heigth;
	}
	
	/**
	 * method to get the maximum weight we can put in a crate
	 * @return
	 */
	public double getMaxWeight() {
		return this.maxWeight;
	}
	
	/**
	 * method to get the volume of the crate
	 * @return volume
	 */
	public double getVolume() {
		return this.volume;
	}
}
