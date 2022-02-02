import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BF {
	private final Order order;

	public BF(Order order) {
		this.order = order;
	}

	public void computeBF() {
		List<Item> sortedItemList = new ArrayList<>(order.getItems());	
		Collections.sort(sortedItemList); // Order a list of items

		List<Crate> crates = new ArrayList<Crate>();
		crates.add(new Crate()); // Add a first crate to the list of crates
		crates.get(0).ep.add(new EP(0,0,0));

		for(int i = 0; i < sortedItemList.size(); i++) {// Voor alle items
			// Find the best extreme points	
			int epIndex = 0;
			int crateIndex = 0;
			double f = Double.POSITIVE_INFINITY;
			for(int k = 0; k < crates.size(); k++) { // Voor alle kratten
				if(crates.get(k).getCurrentWeight() + sortedItemList.get(i).getWeight() <= crates.get(k).getMaxWeight()) {
					for(int j = 0; j < crates.get(k).ep.size(); j++) { // Voor alle EP's in de kratten
						EP ep = crates.get(k).ep.get(j);
						double rsx = ep.getRSx() - sortedItemList.get(i).getWidth();
						double rsy = ep.getRSy() - sortedItemList.get(i).getLength();
						double rsz = ep.getRSz() - sortedItemList.get(i).getHeight();
						if(rsx > 0 && rsy > 0 && rsz > 0) {
							double compare = rsx + rsy + rsz;
							if(compare < f) {
								epIndex = j;
								crateIndex = k;
								f = compare;
							}
						}	
					}
				}
			}
			// If no position is found, create new bin, add the item to the bin
			if(f == Double.POSITIVE_INFINITY) {
				Crate c = new Crate();
				EP one = new EP(sortedItemList.get(i).getWidth(),0,0);
				EP two = new EP(0,sortedItemList.get(i).getLength(),0);
				EP three = new EP(0,0,sortedItemList.get(i).getHeight());
				c.ep.add(one);
				c.ep.add(two);
				c.ep.add(three);
				c.addItemToCrate(sortedItemList.get(i));
				crates.add(c);
				sortedItemList.get(i).insertedx = 0.0;
				sortedItemList.get(i).insertedy = 0.0;
				sortedItemList.get(i).insertedz = 0.0;
			}
			else { // so the item is added to an already existing bin
				crates.get(crateIndex).addItemToCrate(sortedItemList.get(i));
				// weight is also updated
				sortedItemList.get(i).insertedx = crates.get(crateIndex).ep.get(epIndex).getX();
				sortedItemList.get(i).insertedy = crates.get(crateIndex).ep.get(epIndex).getY();
				sortedItemList.get(i).insertedz = crates.get(crateIndex).ep.get(epIndex).getZ();
				crates.get(crateIndex).ep.remove(epIndex);
			}

			// Add new extreme points (algorithm 1)
			List<EP> newEP = new ArrayList<EP>();
			double[] maxbound = new double[6];
			for(int i1 = 0; i1 < maxbound.length; i1++) {
				maxbound[i1] = -1.0;
			}
			for(int j = 0; j < crates.get(crateIndex).items.size(); j++) {
				if(canTakeProjectionYX(crates.get(crateIndex).items.get(j), sortedItemList.get(i)) == true && crates.get(crateIndex).items.get(j).insertedx + crates.get(crateIndex).items.get(j).getWidth() > maxbound[0]) {
					maxbound[0] = crates.get(crateIndex).items.get(j).insertedx + crates.get(crateIndex).items.get(j).getWidth();
					newEP.add(new EP(crates.get(crateIndex).items.get(j).insertedx + crates.get(crateIndex).items.get(j).getWidth(), sortedItemList.get(i).insertedy + sortedItemList.get(i).getLength(), sortedItemList.get(i).insertedz));
				}
				if(canTakeProjectionYZ(crates.get(crateIndex).items.get(j), sortedItemList.get(i)) == true && crates.get(crateIndex).items.get(j).insertedz + crates.get(crateIndex).items.get(j).getHeight() > maxbound[1]) {
					maxbound[1] = crates.get(crateIndex).items.get(j).insertedz + crates.get(crateIndex).items.get(j).getHeight();
					newEP.add(new EP(sortedItemList.get(j).insertedx, sortedItemList.get(j).insertedy + sortedItemList.get(j).getLength(), crates.get(crateIndex).items.get(j).insertedz + crates.get(crateIndex).items.get(j).getHeight()));
				}
				if(canTakeProjectionXY(crates.get(crateIndex).items.get(j), sortedItemList.get(i)) == true && crates.get(crateIndex).items.get(j).insertedy + crates.get(crateIndex).items.get(j).getLength() > maxbound[2]) {
					maxbound[2] = crates.get(crateIndex).items.get(j).insertedy + crates.get(crateIndex).items.get(j).getLength();
					newEP.add(new EP(sortedItemList.get(i).insertedx + sortedItemList.get(i).getWidth(), crates.get(crateIndex).items.get(j).insertedy + crates.get(crateIndex).items.get(j).getLength(), sortedItemList.get(i).insertedz));
				}
				if(canTakeProjectionXZ(crates.get(crateIndex).items.get(j), sortedItemList.get(i)) == true && crates.get(crateIndex).items.get(j).insertedz + crates.get(crateIndex).items.get(j).getHeight() > maxbound[3]) {
					maxbound[3] = crates.get(crateIndex).items.get(j).insertedz + crates.get(crateIndex).items.get(j).getHeight();
					newEP.add(new EP(sortedItemList.get(i).insertedx + sortedItemList.get(i).getWidth(), sortedItemList.get(i).insertedy, crates.get(crateIndex).items.get(j).insertedz + crates.get(crateIndex).items.get(j).getHeight() ));
				}
				if(canTakeProjectionZX(crates.get(crateIndex).items.get(j), sortedItemList.get(i)) == true && crates.get(crateIndex).items.get(j).insertedx + crates.get(crateIndex).items.get(j).getWidth() > maxbound[4]) {
					maxbound[4] = crates.get(crateIndex).items.get(j).insertedx + crates.get(crateIndex).items.get(j).getWidth();
					newEP.add(new EP(crates.get(crateIndex).items.get(j).insertedx + crates.get(crateIndex).items.get(j).getWidth(), sortedItemList.get(i).insertedy, sortedItemList.get(i).insertedz + sortedItemList.get(i).getHeight()));
				}
				if(canTakeProjectionZY(crates.get(crateIndex).items.get(j), sortedItemList.get(i)) == true && crates.get(crateIndex).items.get(j).insertedy + crates.get(crateIndex).items.get(j).getLength() > maxbound[5]) {
					maxbound[5] = crates.get(crateIndex).items.get(j).insertedy + crates.get(crateIndex).items.get(j).getLength();
					newEP.add(new EP(sortedItemList.get(i).insertedx, crates.get(crateIndex).items.get(j).insertedy + crates.get(crateIndex).items.get(j).getLength(), sortedItemList.get(i).insertedz + sortedItemList.get(i).getHeight() ));
				}
			}
			for(int j = 0; j < newEP.size();j++) {
				crates.get(crateIndex).ep.add(newEP.get(j));
			}
			
			// Sort list and remove duplicates
			List<EP> q = new ArrayList<>(crates.get(crateIndex).ep);
			List<EP> temp = new ArrayList<>();
			for(int j = 0; j < q.size();j++) {
				temp.add(q.get(j));
			}
			for(int j = 0; j < newEP.size();j++) {
				temp.add(newEP.get(j));
			}
			Collections.sort(temp);
			List<EP> newlist = new ArrayList<>();
			for(int j = 0; j < temp.size() - 1;j++) {
				if(temp.get(j) != temp.get(j+1)) {
					newlist.add(temp.get(j));
				}
			}
			newlist.add(temp.get(temp.size() -1));
			crates.get(crateIndex).ep = new ArrayList<>(newlist);
			
			/*
			// Order the list and remove duplicates
			Collections.sort(crates.get(crateIndex).ep);
			for(int j = crates.get(crateIndex).ep.size() - 1; j > 0; j--) {
				EP last = crates.get(crateIndex).ep.get(j);
				EP first = crates.get(crateIndex).ep.get(j - 1);
				if(last.getX() == first.getX() && last.getY() == first.getY() && last.getZ() == first.getZ()) {
					crates.get(crateIndex).ep.remove(j);
				}
			}*/

			// Update the RS of all extreme points with algorithm 2
			Item nItem = sortedItemList.get(i);
			for(int j = 0; j < crates.get(crateIndex).ep.size(); j++) {
				EP ep = crates.get(crateIndex).ep.get(j);
				if(ep.getZ() >= nItem.insertedz && ep.getZ() < nItem.insertedz + nItem.getHeight()) {
					if(ep.getX() <= nItem.insertedx && isOnSideY(ep, nItem)) {
						ep.setRSx(Math.min(ep.getRSx(), nItem.insertedx + ep.getX()));
					}
					if(ep.getY() <= nItem.insertedy && isOnSideX(ep, nItem)) {
						ep.setRSy(Math.min(ep.getRSy(),nItem.insertedy + ep.getY() ));
					}
				}
				if(ep.getZ() <= nItem.insertedz && isOnSideX(ep, nItem) && isOnSideY(ep,nItem)) {
					ep.setRSz(Math.min(ep.getRSz(), nItem.insertedz + ep.getZ()));
				}
			}
			

		}
	}
	
	public boolean isOnSideY(EP ep, Item nItem) {
		return nItem.insertedy <= ep.getY() && ep.getY() <= nItem.insertedy + nItem.getLength();
	}
	
	public boolean isOnSideX(EP ep, Item nItem) {
		return nItem.insertedx <= ep.getZ() && ep.getZ() <= nItem.insertedx + nItem.getWidth(); 
	}

	public boolean canTakeProjectionYX(Item i, Item k) {
		boolean fit = false;
		if(k.insertedx >= i.insertedx + i.getWidth() && k.insertedy + k.getLength() < i.insertedy + i.getLength() && k.insertedz < i.insertedz + i.getHeight()) {
			fit = true;
		}
		return fit;
	}

	public boolean canTakeProjectionYZ(Item i, Item k) {
		boolean fit = false;
		if(k.insertedz >= i.insertedz + i.getHeight() && k.insertedy + k.getLength()  < i.insertedy + i.getLength() && k.insertedx < i.insertedx + i.getWidth()) {
			fit = true;
		}
		return fit;
	}

	public boolean canTakeProjectionXY(Item i, Item k) {
		boolean fit = false;
		if(k.insertedy >= i.insertedy + i.getLength() && k.insertedx + k.getWidth() < i.insertedx + i.getWidth() && k.insertedz < i.insertedz + i.getHeight()) {
			fit = true;
		}
		return fit;
	}

	public boolean canTakeProjectionXZ(Item i, Item k) {
		boolean fit = false;
		if(k.insertedz >= i.insertedz + i.getHeight() && k.insertedx + k.getWidth() < i.insertedx + i.getWidth() && k.insertedy < i.insertedy + i.getLength()) {
			fit = true;
		}
		return fit;
	}

	public boolean canTakeProjectionZX(Item i, Item k) {
		boolean fit = false;
		if(k.insertedx >= i.insertedx + i.getWidth() && k.insertedz + k.getHeight() < i.insertedz + i.getHeight() && k.insertedy < i.insertedy + i.getLength()) {
			fit = true;
		}
		return fit;
	}

	public boolean canTakeProjectionZY(Item i, Item k) {
		boolean fit = false;
		if(k.insertedy >= i.insertedy + i.getLength() && k.insertedz + k.getHeight() < i.insertedz + i.getHeight() && k.insertedx < i.insertedx + i.getWidth() ) {
			fit = true;
		}
		return fit;
	}




}
