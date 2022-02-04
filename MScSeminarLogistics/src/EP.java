/**
 * Class that represents an Extreme Point / Empty Maximal Space (for EP, BRKGA algorithms, resp.)
 * @author Geuze et al.
 *
 */
public class EP implements Comparable<EP>{
	private double x;
	private double y;
	private double z;
	private double RSx;
	private double RSy;
	private double RSz;
	
	/**
	 * Constructor of an EP/EMS
	 * @param x 
	 * @param y
	 * @param z
	 */
	public EP(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.RSx = 321; // When creating a new extreme point, set the residual space equal to the crates dimensions
		this.RSy = 501; 
		this.RSz = 273;
	}
	
	/**
	 * Constructor of an EMS
	 * @param x13
	 * @param y13
	 * @param z13
	 * @param x24
	 * @param y24
	 * @param z24
	 */
	public EP(double x13, double y13, double z13, double x24, double y24, double z24) {
		this.x = x13;
		this.y = y13;
		this.z = z13;
		this.RSx = x24;
		this.RSy = y24;
		this.RSz = z24;		
	}
	
	/**
	 * Get x-value of EP / Get x1/x3 of EMS
	 * @return
	 */
	public double getX() {
		return x;
	}
	
	/**
	 * Get y-value of EP / Get y1/y3 of EMS
	 * @return
	 */
	public double getY() {
		return y;
	}
	
	/**
	 * Get z-value of EP / Get z1/z3 of EMS
	 * @return
	 */
	public double getZ() {
		return z;
	}
	
	/**
	 * Get x-value of EP / Get x2/x4 of EMS
	 * @return
	 */
	public double getRSx() {
		return this.RSx;
	}
	
	/**
	 * Get y-value of EP / Get y2/y4 of EMS
	 * @return
	 */
	public double getRSy() {
		return this.RSy;
	}
	
	/**
	 * Get z-value of EP / Get z2/z4 of EMS
	 * @return
	 */
	public double getRSz() {
		return this.RSz;
	}
	
	/**
	 * Set x-value of EP / Set x1/x3 of EMS
	 * @param x
	 */
	public void setX(double x) {
		this.x = x;
	}
	
	/**
	 * Set y-value of EP / Set y1/y3 of EMS
	 * @param y
	 */
	public void setY(double y) {
		this.y = y;
	}
	
	/**
	 * Set z-value of EP / Set z1/z3 of EMS
	 * @param z
	 */
	public void setZ(double z) {
		this.z = z;
	}
	
	/**
	 * Set RSx of EP / Set x2/x4 of EMS
	 * @param x
	 */
	public void setRSx(double x) {
		this.RSx = x;
	}
	
	/**
	 * Set RSy of EP / Set y2/y4 of EMS
	 * @param y
	 */
	public void setRSy(double y) {
		this.RSy = y;
	}
	
	/**
	 * Set RSz of EP / Set z2/z4 of EMS
	 * @param z
	 */
	public void setRSz(double z) {
		this.RSz = z;
	}

	@Override
	public int compareTo(EP o) {
		if(this.z > o.z) {
			return 1;
		}
		else if(this.z < o.z) {
			return -1;
		}
		else { // Als gelijk, sorteer op y
			if( this.y > o.y) {
				return 1;
			}
			else if(this.y < o.y) {
				return -1;
			}
			else { // sorteer op x
				if(this.x > o.x) {
					return 1;
				}
				else if(this.x < o.x) {
					return -1;
				}
				else {
					return 0;
				}
			}
		}
	}

}
