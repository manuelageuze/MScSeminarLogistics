/**
 * Class that represents a blue AH crate
 * @author Geuze et al.
 *	
 */
public class Crate {
		final private double length = 501;
		final private double width = 321;
		final private double height = 273;
		final private double maxWeight = 17000;
		final private double volume = length*width*height;
		
	/**
	 * Constructor that creates a new crate with the standard sizes
	 */
	public Crate() {
		
	}
	
	/**
	 * Method to get the length of a crate
	 * @return length in mm
	 */
	public double getLength() {
		return this.length;
	}
	
	/**
	 * Method to get the width of a crate
	 * @return width in mm
	 */
	public double getWidth() {
		return this.width;
	}
	
	/**
	 * Method to get the height of a crate
	 * @return height in mm
	 */
	public double getHeight() {
		return this.height;
	}
	
	/**
	 * Method to get the maximum weight a crate can handle
	 * @return maxWeight in grams
	 */
	public double getMaxWeight() {
		return this.maxWeight;
	}
	
	/**
	 * Method to get the volume of the crate
	 * @return volume in mm3
	 */
	public double getVolume() {
		return this.volume;
	}
}
