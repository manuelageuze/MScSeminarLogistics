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

		// Maak de 6 items die de rand van de box voorstellen
		Item bottum = new Item(-1,crates.get(0).getWidth(), crates.get(0).getLength(), 0.0,0.0); // bodum item
		Item top = new Item(-2,crates.get(0).getWidth(), crates.get(0).getLength(), 0.0,0.0); // deksel item
		Item sideyz = new Item(-3, 0.0, crates.get(0).getLength(), crates.get(0).getHeight(), 0.0);
		Item sideyzOp = new Item(-4, 0.0, crates.get(0).getLength(), crates.get(0).getHeight(), 0.0);
		Item sidexz = new Item(-5,crates.get(0).getWidth(), 0.0, crates.get(0).getHeight(), 0.0);
		Item sidexzOp = new Item(-6, crates.get(0).getWidth(), 0.0, crates.get(0).getHeight(), 0.0);
		
		// Geef deze 6 items de juiste startpositie
		bottum.setInsertedx(0.0);
		bottum.setInsertedy(0.0);
		bottum.setInsertedz(0.0);
		
		top.setInsertedx(0.0);
		top.setInsertedy(0.0);
		top.setInsertedz(crates.get(0).getHeight());
		
		sideyz.setInsertedx(0.0);
		sideyz.setInsertedy(0.0);
		sideyz.setInsertedz(0.0);
		
		sideyzOp.setInsertedx(crates.get(0).getWidth());
		sideyzOp.setInsertedy(0.0);
		sideyzOp.setInsertedz(0.0);
		
		sidexz.setInsertedx(0.0);
		sidexz.setInsertedy(0.0);
		sidexz.setInsertedz(0.0);
		
		sidexzOp.setInsertedx(0.0);
		sidexzOp.setInsertedy(crates.get(0).getLength());
		sidexzOp.setInsertedz(0.0);
		
		List<Item> sideItems = new ArrayList<>();
		sideItems.add(bottum);
		sideItems.add(top);
		sideItems.add(sideyz);
		sideItems.add(sidexz);
		sideItems.add(sideyzOp);
		sideItems.add(sidexzOp);
		
		crates.get(0).setItemList(sideItems);
		
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
				crateIndex = crates.size();
				
				Crate c = new Crate();
				c.setItemList(sideItems);
				EP one = new EP(sortedItemList.get(i).getWidth(),0,0);
				one.setRSx(crates.get(crateIndex -1).getWidth() - one.getX());
				one.setRSy(crates.get(crateIndex -1).getLength() - one.getY());
				one.setRSz(crates.get(crateIndex -1).getHeight() - one.getZ());
				EP two = new EP(0,sortedItemList.get(i).getLength(),0);
				two.setRSx(crates.get(crateIndex -1 ).getWidth() - two.getX());
				two.setRSy(crates.get(crateIndex -1 ).getLength() - two.getY());
				two.setRSz(crates.get(crateIndex -1).getHeight() - two.getZ());
				EP three = new EP(0,0,sortedItemList.get(i).getHeight());
				three.setRSx(crates.get(crateIndex -1).getWidth() - three.getX());
				three.setRSy(crates.get(crateIndex -1).getLength() - three.getY());
				three.setRSz(crates.get(crateIndex -1).getHeight() - three.getZ());
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
			for(int j = 0; j < crates.get(crateIndex).items.size() - 1; j++) {
				if( i == 2 && j == 3)
				{
					boolean test = true;
				}
//				#1
				if(canTakeProjectionYX(crates.get(crateIndex).items.get(j), sortedItemList.get(i), crates.get(crateIndex).getItemList()) == true && crates.get(crateIndex).items.get(j).insertedx + crates.get(crateIndex).items.get(j).getWidth() > maxbound[0]) {
					maxbound[0] = crates.get(crateIndex).items.get(j).insertedx + crates.get(crateIndex).items.get(j).getWidth();
					EP ep = new EP(crates.get(crateIndex).items.get(j).insertedx + crates.get(crateIndex).items.get(j).getWidth(), sortedItemList.get(i).insertedy + sortedItemList.get(i).getLength(), sortedItemList.get(i).insertedz);
					ep.setRSx(crates.get(crateIndex).getWidth() - ep.getX());
					ep.setRSy(crates.get(crateIndex).getLength() - ep.getY());
					ep.setRSz(crates.get(crateIndex).getHeight() - ep.getZ());
					newEP.add(ep);
				}
//				#2
				if(canTakeProjectionYZ(crates.get(crateIndex).items.get(j), sortedItemList.get(i), crates.get(crateIndex).getItemList()) == true && crates.get(crateIndex).items.get(j).insertedz + crates.get(crateIndex).items.get(j).getHeight() > maxbound[1]) {
					maxbound[1] = crates.get(crateIndex).items.get(j).insertedz + crates.get(crateIndex).items.get(j).getHeight();
					EP ep = new EP(sortedItemList.get(i).insertedx, sortedItemList.get(i).insertedy + sortedItemList.get(i).getLength(), crates.get(crateIndex).items.get(j).insertedz + crates.get(crateIndex).items.get(j).getHeight());
					ep.setRSx(crates.get(crateIndex).getWidth() - ep.getX());
					ep.setRSy(crates.get(crateIndex).getLength() - ep.getY());
					ep.setRSz(crates.get(crateIndex).getHeight() - ep.getZ());
					newEP.add(ep);
				}
//				#3
				if(canTakeProjectionXY(crates.get(crateIndex).items.get(j), sortedItemList.get(i), crates.get(crateIndex).getItemList()) == true && crates.get(crateIndex).items.get(j).insertedy + crates.get(crateIndex).items.get(j).getLength() > maxbound[2]) {
					maxbound[2] = crates.get(crateIndex).items.get(j).insertedy + crates.get(crateIndex).items.get(j).getLength();
					EP ep = new EP(sortedItemList.get(i).insertedx + sortedItemList.get(i).getWidth(), crates.get(crateIndex).items.get(j).insertedy + crates.get(crateIndex).items.get(j).getLength(), sortedItemList.get(i).insertedz);
					ep.setRSx(crates.get(crateIndex).getWidth() - ep.getX());
					ep.setRSy(crates.get(crateIndex).getLength() - ep.getY());
					ep.setRSz(crates.get(crateIndex).getHeight() - ep.getZ());
					newEP.add(ep);
				}
//				#4
				if(canTakeProjectionXZ(crates.get(crateIndex).items.get(j), sortedItemList.get(i), crates.get(crateIndex).getItemList()) == true && crates.get(crateIndex).items.get(j).insertedz + crates.get(crateIndex).items.get(j).getHeight() > maxbound[3]) {
					maxbound[3] = crates.get(crateIndex).items.get(j).insertedz + crates.get(crateIndex).items.get(j).getHeight();
					EP ep = new EP(sortedItemList.get(i).insertedx + sortedItemList.get(i).getWidth(), sortedItemList.get(i).insertedy, crates.get(crateIndex).items.get(j).insertedz + crates.get(crateIndex).items.get(j).getHeight());
					ep.setRSx(crates.get(crateIndex).getWidth() - ep.getX());
					ep.setRSy(crates.get(crateIndex).getLength() - ep.getY());
					ep.setRSz(crates.get(crateIndex).getHeight() - ep.getZ());
					newEP.add(ep);
				}
//				#5
				if(canTakeProjectionZX(crates.get(crateIndex).items.get(j), sortedItemList.get(i), crates.get(crateIndex).getItemList()) == true && crates.get(crateIndex).items.get(j).insertedx + crates.get(crateIndex).items.get(j).getWidth() > maxbound[4]) {
					maxbound[4] = crates.get(crateIndex).items.get(j).insertedx + crates.get(crateIndex).items.get(j).getWidth();
					EP ep = new EP(crates.get(crateIndex).items.get(j).insertedx + crates.get(crateIndex).items.get(j).getWidth(), sortedItemList.get(i).insertedy, sortedItemList.get(i).insertedz + sortedItemList.get(i).getHeight());
					ep.setRSx(crates.get(crateIndex).getWidth() - ep.getX());
					ep.setRSy(crates.get(crateIndex).getLength() - ep.getY());
					ep.setRSz(crates.get(crateIndex).getHeight() - ep.getZ());
					newEP.add(ep);
				}
//				#6
				if(canTakeProjectionZY(crates.get(crateIndex).items.get(j), sortedItemList.get(i),crates.get(crateIndex).getItemList()) == true && crates.get(crateIndex).items.get(j).insertedy + crates.get(crateIndex).items.get(j).getLength() > maxbound[5]) {
					maxbound[5] = crates.get(crateIndex).items.get(j).insertedy + crates.get(crateIndex).items.get(j).getLength();
					EP ep = new EP(sortedItemList.get(i).insertedx, crates.get(crateIndex).items.get(j).insertedy + crates.get(crateIndex).items.get(j).getLength(), sortedItemList.get(i).insertedz + sortedItemList.get(i).getHeight()) ;
					ep.setRSx(crates.get(crateIndex).getWidth() - ep.getX());
					ep.setRSy(crates.get(crateIndex).getLength() - ep.getY());
					ep.setRSz(crates.get(crateIndex).getHeight() - ep.getZ());
					newEP.add(ep);
				}
			}
			/*
			for(int j = 0; j < newEP.size();j++) {
				crates.get(crateIndex).ep.add(newEP.get(j));
			}*/
			
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
				if(temp.get(j).getX() != temp.get(j+1).getX() || temp.get(j).getY() != temp.get(j+1).getY() || temp.get(j).getZ() != temp.get(j+1).getZ()) {
					newlist.add(temp.get(j));
				}
			}
			newlist.add(temp.get(temp.size() - 1));
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
					boolean isSidey = isOnSideY(ep, nItem);
					if(ep.getX() <= nItem.insertedx && isOnSideY(ep, nItem) ) {
						ep.setRSx(Math.min(ep.getRSx(), nItem.insertedx - ep.getX()));
					}
					boolean isSidex = isOnSideX(ep,nItem);
					if(ep.getY() <= nItem.insertedy && isOnSideX(ep, nItem)) {
						ep.setRSy(Math.min(ep.getRSy(),nItem.insertedy - ep.getY()));
					}
				}
				if(ep.getZ() <= nItem.insertedz && isOnSideX(ep, nItem) && isOnSideY(ep,nItem)) {
					ep.setRSz(Math.min(ep.getRSz(), nItem.insertedz - ep.getZ()));
				}
			}
			
		}
		
		// Print solution
		for(int i = 0; i < crates.size(); i++) {
			Crate c = crates.get(i);
			System.out.println("Number of crates used in this order: " + crates.size());
			System.out.print("Crate number " + (i + 1)+"\t");
			for(int j = 6 ; j < c.getItemList().size(); j++) {
				System.out.print((int)c.getItemList().get(j).getItemId() + "\t");
			}
			System.out.println();
		}		
	}
	
	public boolean isOnSideY(EP ep, Item nItem) {
		return nItem.insertedy < ep.getY() && ep.getY() < nItem.insertedy + nItem.getLength();
	}
	
	public boolean isOnSideX(EP ep, Item nItem) {
		return nItem.insertedx < ep.getZ() && ep.getZ() < nItem.insertedx + nItem.getWidth(); 
	}

	public boolean canTakeProjectionYX(Item i, Item k, List<Item> items) {
		if(k.insertedx >= i.insertedx + i.getWidth() && k.insertedy + k.getLength() < i.insertedy + i.getLength() && k.insertedz < i.insertedz + i.getHeight()) {	
			for(int j = 0; j < items.size(); j++) {
				if(items.get(j).getItemId() == i.getItemId()) {
					continue;
				}
				double newx = i.insertedx + i.getWidth();
				double newy = k.insertedy + k.getLength();
				double newz = k.insertedz;
				
				if(newx >= items.get(j).getinsertedx() + items.get(j).getWidth() || newx < items.get(j).getinsertedx()) {
					continue;
				}
				if(newz >=items.get(j).getinsertedz() + items.get(j).getHeight() || newz < items.get(j).getinsertedz()) {
					continue;
				}
				if(newy >= items.get(j).getinsertedy() + items.get(j).getLength() || newy < items.get(j).getinsertedy()) {
					continue;
				}
				return false;
				
				/*
				if(newx < items.get(j).getinsertedx() + items.get(j).getWidth() && newx >= items.get(j).getinsertedx()) {
					return false;
				}
				else if(newy < items.get(j).getinsertedy() + items.get(j).getLength() && newy >= items.get(j).getinsertedy()) {
					return false;
				}
				else if(newz < items.get(j).getinsertedz() + items.get(j).getHeight() && newz >= items.get(j).getinsertedz()) {
					return false;
				}
				*/
			}
			return true;
		}
		return false;
	}

	public boolean canTakeProjectionYZ(Item i, Item k, List<Item> items) {
		if(k.insertedz >= i.insertedz + i.getHeight() && k.insertedy + k.getLength()  < i.insertedy + i.getLength() && k.insertedx < i.insertedx + i.getWidth()) {
			for(int j = 0; j < items.size(); j++) {
				if(items.get(j).getItemId() == i.getItemId()) {
					continue;
				}
				double newx = k.insertedx;
				double newy = k.insertedy + k.getLength();
				double newz = i.insertedz + i.getHeight();
					
				if(newx >= items.get(j).getinsertedx() + items.get(j).getWidth() || newx < items.get(j).getinsertedx()) {
					continue;
				}
				if(newz >=items.get(j).getinsertedz() + items.get(j).getHeight() || newz < items.get(j).getinsertedz()) {
					continue;
				}
				if(newy >= items.get(j).getinsertedy() + items.get(j).getLength() || newy < items.get(j).getinsertedy()) {
					continue;
				}
				return false;
				
				
				/*
				if(newx < items.get(j).getinsertedx() + items.get(j).getWidth() && newx >= items.get(j).getinsertedx()) {
					return false;
				}
				if(newy < items.get(j).getinsertedy() + items.get(j).getLength() && newy >= items.get(j).getinsertedy()) {
					return false;
				}
				if(newz < items.get(j).getinsertedz() + items.get(j).getHeight() && newz >= items.get(j).getinsertedz()) {
					return false;
				}
				*/
			}
			return true;
		}
		return false;
	}

	public boolean canTakeProjectionXY(Item i, Item k, List<Item> items) {
		if(k.insertedy >= i.insertedy + i.getLength() && k.insertedx + k.getWidth() < i.insertedx + i.getWidth() && k.insertedz < i.insertedz + i.getHeight()) {
			for(int j = 0; j < items.size(); j++) {
				if(items.get(j).getItemId() == i.getItemId()) {
					continue;
				}
				double newx = k.insertedx + k.getWidth();
				double newy = i.insertedy + i.getLength();
				double newz = k.insertedz;
				if(newx >= items.get(j).getinsertedx() + items.get(j).getWidth() || newx < items.get(j).getinsertedx()) {
					continue;
				}
				if(newz >=items.get(j).getinsertedz() + items.get(j).getHeight() || newz < items.get(j).getinsertedz()) {
					continue;
				}
				if(newy >= items.get(j).getinsertedy() + items.get(j).getLength() || newy < items.get(j).getinsertedy()) {
					continue;
				}
				return false;
				/*
				if(newx < items.get(j).getinsertedx() + items.get(j).getWidth() && newx >= items.get(j).getinsertedx()) {
					return false;
				}
				if(newy < items.get(j).getinsertedy() + items.get(j).getLength() && newy >= items.get(j).getinsertedy()) {
					return false;
				}
				if(newz < items.get(j).getinsertedz() + items.get(j).getHeight() && newz >= items.get(j).getinsertedz()) {
					return false;
				}*/
			}
			return true;
		}
		return false;
	}

	public boolean canTakeProjectionXZ(Item i, Item k, List<Item> items) {	
		if(k.insertedz >= i.insertedz + i.getHeight() && k.insertedx + k.getWidth() < i.insertedx + i.getWidth() && k.insertedy < i.insertedy + i.getLength()) {
			for(int j = 0; j < items.size(); j++) {
				if(items.get(j).getItemId() == i.getItemId()) {
					continue;
				}
				double newx = k.insertedx + k.getWidth();
				double newy = k.insertedy;
				double newz = i.insertedz + i.getHeight();
				if(newx >= items.get(j).getinsertedx() + items.get(j).getWidth() || newx < items.get(j).getinsertedx()) {
					continue;
				}
				if(newz >=items.get(j).getinsertedz() + items.get(j).getHeight() || newz < items.get(j).getinsertedz()) {
					continue;
				}
				if(newy >= items.get(j).getinsertedy() + items.get(j).getLength() || newy < items.get(j).getinsertedy()) {
					continue;
				}
				return false;
				/*
				if(newx < items.get(j).getinsertedx() + items.get(j).getWidth() && newx >= items.get(j).getinsertedx()) {
					return false;
				}
				if(newy < items.get(j).getinsertedy() + items.get(j).getLength() && newy >= items.get(j).getinsertedy()) {
					return false;
				}
				if(newz < items.get(j).getinsertedz() + items.get(j).getHeight() && newz >= items.get(j).getinsertedz()) {
					return false;
				}*/				
			}
			return true;
		}
		return false;
	}

	public boolean canTakeProjectionZX(Item i, Item k, List<Item> items) {
		if(k.insertedx >= i.insertedx + i.getWidth() && k.insertedz + k.getHeight() < i.insertedz + i.getHeight() && k.insertedy < i.insertedy + i.getLength()) {
			for(int j = 0; j < items.size(); j++) {
				if(items.get(j).getItemId() == i.getItemId()) {
					continue;
				}
				double newx = i.insertedx + i.getWidth();
				double newy = k.insertedy;
				double newz = k.insertedz + k.getHeight();
				if(newx >= items.get(j).getinsertedx() + items.get(j).getWidth() || newx < items.get(j).getinsertedx()) {
					continue;
				}
				if(newz >=items.get(j).getinsertedz() + items.get(j).getHeight() || newz < items.get(j).getinsertedz()) {
					continue;
				}
				if(newy >= items.get(j).getinsertedy() + items.get(j).getLength() || newy < items.get(j).getinsertedy()) {
					continue;
				}
				return false;
				/*
				if(newx < items.get(j).getinsertedx() + items.get(j).getWidth() && newx >= items.get(j).getinsertedx()) {
					return false;
				}
				if(newy < items.get(j).getinsertedy() + items.get(j).getLength() && newy >= items.get(j).getinsertedy()) {
					return false;
				}
				if(newz < items.get(j).getinsertedz() + items.get(j).getHeight() && newz >= items.get(j).getinsertedz()) {
					return false;
				}*/
			}
			return true;
		}
		return false;
	}

//	#6
	public boolean canTakeProjectionZY(Item i, Item k, List<Item> items) {
		if(k.insertedy >= i.insertedy + i.getLength() && k.insertedz + k.getHeight() < i.insertedz + i.getHeight() && k.insertedx < i.insertedx + i.getWidth() ) {
			for(int j = 0; j < items.size(); j++) {
				if(items.get(j).getItemId() == i.getItemId()) {
					continue;
				}
				double newx = k.insertedx;
				double newy = i.insertedy + i.getLength();
				double newz = k.getinsertedz() + k.getHeight();
//				int counter = 0;
				if(newx >= items.get(j).getinsertedx() + items.get(j).getWidth() || newx < items.get(j).getinsertedx()) {
					continue;
				}
				if(newz >=items.get(j).getinsertedz() + items.get(j).getHeight() || newz < items.get(j).getinsertedz()) {
					continue;
				}
				if(newy >= items.get(j).getinsertedy()|| i.getinsertedy()+i.getLength() >= items.get(j).getinsertedy()) {
					continue;
				}
				return false;
				
				/*
				if(newx < items.get(j).getinsertedx() + items.get(j).getWidth() && newx >= items.get(j).getinsertedx()) {
					return false;
				}
				if(newy < items.get(j).getinsertedy() + items.get(j).getLength() && newy >= items.get(j).getinsertedy()) {
					return false;
				}
				if(newz < items.get(j).getinsertedz() + items.get(j).getHeight() && newz >= items.get(j).getinsertedz()) {
					return false;
				}*/
			}
			return true;
		}
		return false;
	}




}
