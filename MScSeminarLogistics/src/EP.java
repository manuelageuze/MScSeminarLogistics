
public class EP implements Comparable<EP>{
	private double x;
	private double y;
	private double z;
	private double RSx;
	private double RSy;
	private double RSz;
	
	
	public EP(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.RSx = 321; // When creating a new extreme point, set the residual space equal to the crates dimensions
		this.RSy = 501; 
		this.RSz = 273;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getZ() {
		return z;
	}
	
	public double getRSx() {
		return this.RSx;
	}
	
	public double getRSy() {
		return this.RSy;
	}
	
	public double getRSz() {
		return this.RSz;
	}
	
	public void setRSx(double x) {
		this.RSx = x;
	}
	
	public void setRSy(double y) {
		this.RSy = y;
	}
	
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
