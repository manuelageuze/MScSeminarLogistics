import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BF {
	private final Order order;

	public BF(Order order) {
		this.order = order;
	}

	public List<Crate> computeBF(){

		List<Item> sortedItemList = new ArrayList<>(order.getItems());	
		Collections.sort(sortedItemList); // Order a list of items

		List<Crate> crates = new ArrayList<Crate>();
		crates.add(new Crate()); // Add a first crate to the list of crates
		crates.get(0).getEP().add(new EP(0,0,0));
		crates.get(0).setOrderIndex((int) order.getOrderId());

		// Maak de 6 items die de rand van de box voorstellen
		Item bottum = new Item(-1,crates.get(0).getWidth(), crates.get(0).getLength(), 0.0,0.0, -1); // bodem item
		Item top = new Item(-2,crates.get(0).getWidth(), crates.get(0).getLength(), 0.0,0.0, -1); // deksel item
		Item sideyz = new Item(-3, 0.0, crates.get(0).getLength(), crates.get(0).getHeight(), 0.0, -1);
		Item sideyzOp = new Item(-4, 0.0, crates.get(0).getLength(), crates.get(0).getHeight(), 0.0, -1);
		Item sidexz = new Item(-5,crates.get(0).getWidth(), 0.0, crates.get(0).getHeight(), 0.0, -1);
		Item sidexzOp = new Item(-6, crates.get(0).getWidth(), 0.0, crates.get(0).getHeight(), 0.0, -1);

		// Geef deze 6 items de juiste startpositie
		bottum.setInsertedX(0.0);
		bottum.setInsertedY(0.0);
		bottum.setInsertedZ(0.0);

		top.setInsertedX(0.0);
		top.setInsertedY(0.0);
		top.setInsertedZ(crates.get(0).getHeight());

		sideyz.setInsertedX(0.0);
		sideyz.setInsertedY(0.0);
		sideyz.setInsertedZ(0.0);

		sideyzOp.setInsertedX(crates.get(0).getWidth());
		sideyzOp.setInsertedY(0.0);
		sideyzOp.setInsertedZ(0.0);

		sidexz.setInsertedX(0.0);
		sidexz.setInsertedY(0.0);
		sidexz.setInsertedZ(0.0);

		sidexzOp.setInsertedX(0.0);
		sidexzOp.setInsertedY(crates.get(0).getLength());
		sidexzOp.setInsertedZ(0.0);

		List<Item> sideItems = new ArrayList<>();
		sideItems.add(bottum);
		sideItems.add(top);
		sideItems.add(sideyz);
		sideItems.add(sidexz);
		sideItems.add(sideyzOp);
		sideItems.add(sidexzOp);

		crates.get(0).setItemList(sideItems);

		for(int i = 0; i < sortedItemList.size(); i++) {// Voor alle items

			// Find the rotation of an item
			List<Item> rotatedItem = new ArrayList<>();
			Item a = sortedItemList.get(i);
			// Rotatie 1
			Item it = new Item(a.getItemId() , a.getWidth(), a.getLength(), a.getHeight(), a.getWeight(), a.getAisle());
			// Rotatie 2
			Item it2 = new Item(a.getItemId(), a.getLength(), a.getWidth(), a.getHeight(), a.getWeight(), a.getAisle());
			// Rotatie 3
			Item it3 = new Item(a.getItemId(), a.getHeight(), a.getLength(), a.getWidth(), a.getWeight(), a.getAisle());
			// Rotatie 4
			Item it4 = new Item(a.getItemId(), a.getLength(), a.getHeight(), a.getWidth(), a.getWeight(), a.getAisle());
			// Rotatie 5
			Item it5 = new Item(a.getItemId(), a.getWidth(), a.getHeight(), a.getLength(), a.getWeight(), a.getAisle());
			// Rotatie 6
			Item it6 = new Item(a.getItemId(), a.getHeight(), a.getWidth(), a.getLength(), a.getWeight(), a.getAisle());
			rotatedItem.add(it);
			rotatedItem.add(it2);
			rotatedItem.add(it3);
			rotatedItem.add(it4);
			rotatedItem.add(it5);
			rotatedItem.add(it6);

			// Find the best extreme points	
			int epIndex = 0;
			int crateIndex = 0;
			int rotatedIndex = 0;
			double f = Double.POSITIVE_INFINITY;
			boolean found = false;
			
			// BF without aisles
			for(int k = 0; k < crates.size(); k++) { // Voor alle kratten
				if(crates.get(k).getCurrentWeight() + sortedItemList.get(i).getWeight() <= crates.get(k).getMaxWeight()) {
					for(int j = 0; j < crates.get(k).getEP().size(); j++) { // Voor alle EP's in de kratten
						for(int r = 0; r < rotatedItem.size();r++) { // Voor alle rotaties
							EP ep = crates.get(k).getEP().get(j);
							double rsx = ep.getRSx() - rotatedItem.get(r).getWidth();
							double rsy = ep.getRSy() - rotatedItem.get(r).getLength();
							double rsz = ep.getRSz() - rotatedItem.get(r).getHeight();
							if(rsx > 0 && rsy > 0 && rsz > 0) {
								if(!isOverlapping(ep, crates.get(k).getItemList(), rotatedItem.get(r))) {
									double compare = rsx + rsy + rsz;
									if(compare < f) {
										epIndex = j;
										crateIndex = k;
										f = compare;
										rotatedIndex = r;
										found = true;
									}
								}

							}	
						}
					}
				}
			}
			// BF with aisles		
			/*
			for(int k = 0; k < crates.size(); k++) { // Voor alle kratten
				if(crates.get(k).getCurrentWeight() + sortedItemList.get(i).getWeight() <= crates.get(k).getMaxWeight()) { // Als het past voor gewicht
					for(int j = 0; j < crates.get(k).getEP().size(); j++) { // Voor alle EP's in de kratten
						EP ep = crates.get(k).getEP().get(j);
						for(int r = 0; r < rotatedItem.size();r++) { // Voor alle rotaties
							double rsx = ep.getRSx() - rotatedItem.get(r).getWidth();
							double rsy = ep.getRSy() - rotatedItem.get(r).getLength();
							double rsz = ep.getRSz() - rotatedItem.get(r).getHeight();
							int aislecost = 0;
							if(rsx > 0 && rsy > 0 && rsz > 0) { // Dus het item past in de residual space van het EP
								if(!isOverlapping(ep, crates.get(k).getItemList(), rotatedItem.get(r))) {
									int aisle = rotatedItem.get(r).getAisle();
									if(crates.get(k).getAisles()[aisle] == false) {
										// Then the aisle is not used in the crate yet
										// So add extra cost to placing in this bin.
										aislecost = 10;
									}
									double compare = rsx + rsy + rsz + aislecost;
									if(compare < f) {
										epIndex = j;
										crateIndex = k;
										f = compare;
										rotatedIndex = r;
										found = true;
									}
								}
							}	
						}
					}
				}
			}*/
			if(found == false) {
				crateIndex = crates.size();
				Crate c = new Crate();
				c.setOrderIndex((int) order.getOrderId());
				c.setItemList(sideItems);

				// Kies de beste rotatie
				for(int r = 0; r < rotatedItem.size(); r++) {
					double rsx = 321 - rotatedItem.get(r).getWidth();
					double rsy = 501 - rotatedItem.get(r).getLength();
					double rsz = 273 - rotatedItem.get(r).getHeight();
					if(rsx > 0 && rsy > 0 && rsz > 0) { // Er zijn nog geen andere items, dus niet nodig testen op andere items
						// Wel nodig testen of past in RS
						double compare = rsx + rsy + rsz;
						if(compare < f) {
							f = compare;
							rotatedIndex = r;
						}
					}
				}
				// Create the 3 extreme points
				EP one = new EP(rotatedItem.get(rotatedIndex).getWidth(),0,0);
				one.setRSx(crates.get(0).getWidth() - one.getX());
				one.setRSy(crates.get(0).getLength() - one.getY());
				one.setRSz(crates.get(0).getHeight() - one.getZ());
				EP two = new EP(0,rotatedItem.get(rotatedIndex).getLength(),0);
				two.setRSx(crates.get(0).getWidth() - two.getX());
				two.setRSy(crates.get(0).getLength() - two.getY());
				two.setRSz(crates.get(0).getHeight() - two.getZ());
				EP three = new EP(0,0,rotatedItem.get(rotatedIndex).getHeight());
				three.setRSx(crates.get(0).getWidth() - three.getX());
				three.setRSy(crates.get(0).getLength() - three.getY());
				three.setRSz(crates.get(0).getHeight() - three.getZ());
				c.getEP().add(one); // Add extreme points to the list of extreme points
				c.getEP().add(two);
				c.getEP().add(three);

				rotatedItem.get(rotatedIndex).setInsertedX(0.0);
				rotatedItem.get(rotatedIndex).setInsertedY(0.0);
				rotatedItem.get(rotatedIndex).setInsertedZ(0.0);
				// Add item to the crate
				c.addItemToCrate(rotatedItem.get(rotatedIndex));
				c.setAisles(rotatedItem.get(rotatedIndex).getAisle(), true);
				crates.add(c);	

			}
			else { // so the item is added to an already existing bin
				rotatedItem.get(rotatedIndex).setInsertedX(crates.get(crateIndex).getEP().get(epIndex).getX());
				rotatedItem.get(rotatedIndex).setInsertedY(crates.get(crateIndex).getEP().get(epIndex).getY());
				rotatedItem.get(rotatedIndex).setInsertedZ(crates.get(crateIndex).getEP().get(epIndex).getZ());
				crates.get(crateIndex).setAisles(rotatedItem.get(rotatedIndex).getAisle(), true); // Aisle has to be visited
				crates.get(crateIndex).addItemToCrate(rotatedItem.get(rotatedIndex));
				crates.get(crateIndex).getEP().remove(epIndex);
			}

			// Add new extreme points (algorithm 1)
			List<EP> newEP = new ArrayList<EP>();
			double[] maxbound = new double[6];
			for(int i1 = 0; i1 < maxbound.length; i1++) {
				maxbound[i1] = -1.0;
			}
			for(int j = 0; j < crates.get(crateIndex).getItemList().size() - 1; j++) {
				//#1
				if(crates.get(crateIndex).getItemList().get(j).getInsertedX() + crates.get(crateIndex).getItemList().get(j).getWidth() > maxbound[0]) {
					EP ep = canTakeProjectionYX(crates.get(crateIndex).getItemList().get(j), crates.get(crateIndex).getItemList().get(crates.get(crateIndex).getItemList().size() - 1),crates.get(crateIndex).getItemList());

					if(ep != null) {
						newEP.add(ep);
						maxbound[0] = crates.get(crateIndex).getItemList().get(j).getInsertedX() + crates.get(crateIndex).getItemList().get(j).getWidth();
					}
				}
				//	#2
				if(crates.get(crateIndex).getItemList().get(j).getInsertedZ() + crates.get(crateIndex).getItemList().get(j).getHeight() > maxbound[1]) {
					EP ep = canTakeProjectionYZ(crates.get(crateIndex).getItemList().get(j), crates.get(crateIndex).getItemList().get(crates.get(crateIndex).getItemList().size() - 1), crates.get(crateIndex).getItemList());

					if(ep != null) {
						maxbound[1] = crates.get(crateIndex).getItemList().get(j).getInsertedZ() + crates.get(crateIndex).getItemList().get(j).getHeight();
						newEP.add(ep);
					}
				}
				//	#3
				if(crates.get(crateIndex).getItemList().get(j).getInsertedY() + crates.get(crateIndex).getItemList().get(j).getLength() > maxbound[2]) {
					EP ep = canTakeProjectionXY(crates.get(crateIndex).getItemList().get(j), crates.get(crateIndex).getItemList().get(crates.get(crateIndex).getItemList().size() - 1), crates.get(crateIndex).getItemList());
					if(ep != null) {
						maxbound[2] = crates.get(crateIndex).getItemList().get(j).getInsertedY() + crates.get(crateIndex).getItemList().get(j).getLength();
						newEP.add(ep);
					}
				}
				//	#4
				if(crates.get(crateIndex).getItemList().get(j).getInsertedZ() + crates.get(crateIndex).getItemList().get(j).getHeight() > maxbound[3]) {
					EP ep = canTakeProjectionXZ(crates.get(crateIndex).getItemList().get(j), crates.get(crateIndex).getItemList().get(crates.get(crateIndex).getItemList().size() - 1), crates.get(crateIndex).getItemList());
					if(ep != null) {
						maxbound[3] = crates.get(crateIndex).getItemList().get(j).getInsertedZ() + crates.get(crateIndex).getItemList().get(j).getHeight();
						newEP.add(ep);
					}
				}
				//	#5
				if(crates.get(crateIndex).getItemList().get(j).getInsertedX() + crates.get(crateIndex).getItemList().get(j).getWidth() > maxbound[4]) {
					EP ep = canTakeProjectionZX(crates.get(crateIndex).getItemList().get(j), crates.get(crateIndex).getItemList().get(crates.get(crateIndex).getItemList().size() - 1), crates.get(crateIndex).getItemList());
					if(ep != null) {
						maxbound[4] = crates.get(crateIndex).getItemList().get(j).getInsertedX() + crates.get(crateIndex).getItemList().get(j).getWidth();
						newEP.add(ep);
					}
				}
				//				#6
				if(crates.get(crateIndex).getItemList().get(j).getInsertedY() + crates.get(crateIndex).getItemList().get(j).getLength() > maxbound[5]) {
					EP ep = canTakeProjectionZY(crates.get(crateIndex).getItemList().get(j), crates.get(crateIndex).getItemList().get(crates.get(crateIndex).getItemList().size() - 1),crates.get(crateIndex).getItemList());
					if(ep != null) {
						maxbound[5] = crates.get(crateIndex).getItemList().get(j).getInsertedY() + crates.get(crateIndex).getItemList().get(j).getLength();
						newEP.add(ep);
					}
				}
			}

			// Sort list and remove duplicates
			List<EP> q = new ArrayList<>(crates.get(crateIndex).getEP());
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
			crates.get(crateIndex).setEPList(new ArrayList<>(newlist));

			// Update the RS of all extreme points with algorithm 2
			Item nItem = crates.get(crateIndex).getItemList().get(crates.get(crateIndex).getItemList().size() - 1); // newly added item
			for(int j = 0; j < crates.get(crateIndex).getEP().size(); j++) { // For all extreme points
				EP ep = crates.get(crateIndex).getEP().get(j);
				if(nItem.getInsertedX()<= ep.getX() && ep.getX() < nItem.getInsertedX()+nItem.getWidth()) {
					if(nItem.getInsertedY()<= ep.getY() && ep.getY() < nItem.getInsertedY()+nItem.getLength()) {
						if(nItem.getInsertedZ()<= ep.getZ() && ep.getZ() < nItem.getInsertedZ()+nItem.getHeight()) {
							crates.get(crateIndex).getEP().remove(j);
							j--;
							continue;
						}
					}
				}
				if(ep.getZ() >= nItem.getInsertedZ() && ep.getZ() < nItem.getInsertedZ() + nItem.getHeight()) {				
					if(ep.getX() <= nItem.getInsertedX()) {
						if(nItem.getInsertedY() <= ep.getY() && ep.getY() < nItem.getInsertedY() + nItem.getLength()) {
							ep.setRSx(Math.min(ep.getRSx(), nItem.getInsertedX() - ep.getX()));
						}
					}
					if(ep.getY() <= nItem.getInsertedY()) {
						if(nItem.getInsertedX() <= ep.getX() && ep.getX() < nItem.getInsertedX() + nItem.getWidth()) {
							ep.setRSy(Math.min(ep.getRSy(),nItem.getInsertedY() - ep.getY()));
						}
					}
				}
				if(ep.getZ() <= nItem.getInsertedZ()) { // Als z erboven
					if(ep.getZ() + ep.getRSz() >= nItem.getInsertedZ()) { // Als het nieuwe item wel in de RS valt

						if(ep.getX() >= nItem.getInsertedX() && ep.getX() < nItem.getInsertedX() + nItem.getWidth()) {

							if(ep.getY() >= nItem.getInsertedY() && ep.getY() < nItem.getInsertedY() + nItem.getLength()) {
								ep.setRSz(Math.min(ep.getRSz(), nItem.getInsertedZ() - ep.getZ()));
							}

						}

					}
				}

				// If RS of ep zero, remove EP
				if(ep.getRSx() <= 0.0) {
					crates.get(crateIndex).getEP().remove(j);
					j--;
				}
				else if(ep.getRSy() <= 0.0) {
					crates.get(crateIndex).getEP().remove(j);
					j--;
				}
				else if(ep.getRSz() <= 0.0) {
					crates.get(crateIndex).getEP().remove(j);
					j--;
				}
			}
		}
		return crates;
	}


	@SuppressWarnings("unused")
	private boolean itemfits(EP ep, List<Item> itemList, Item nItem) {
		// This method should display if there is another item in this box that would block the placement of the new item
		int test = 0;
		test = test + 1;
		for(int i = 6; i < itemList.size(); i++) {
			Item itemi = itemList.get(i);


			if(ep.getZ() <= itemi.getInsertedZ() && itemi.getInsertedZ() < ep.getZ() + nItem.getHeight()) {
				if(ep.getX() <= itemi.getInsertedX() && itemi.getInsertedX() < ep.getX() + nItem.getWidth()) {
					if(ep.getY() <= itemi.getInsertedY() && itemi.getInsertedY() < ep.getY() + nItem.getLength()){
						return false;
					}
				}
			}	
		}
		return true;
	}

	private boolean isOverlapping(EP ep, List<Item> items, Item item1)
	{
		for(int i = 6 ; i < items.size() ; i++)
		{
			Item item2 = items.get(i);
			if(item2.getItemId()==69)
			{
				@SuppressWarnings("unused")
				boolean test = false;
				test =false;
			}
			double x1min = ep.getX();
			double x1max = x1min + item1.getWidth();
			double y1min = ep.getY();
			double y1max = y1min + item1.getLength();
			double z1min = ep.getZ();
			double z1max = z1min + item1.getHeight();

			double x2min = item2.getInsertedX();
			double x2max = x2min + item2.getWidth();
			double y2min = item2.getInsertedY();
			double y2max = y2min + item2.getLength();
			double z2min = item2.getInsertedZ();
			double z2max = z2min + item2.getHeight();

			if(x1min < x2max && x2min < x1max 
					&& y1min < y2max && y2min < y1max
					&& z1min < z2max && z2min < z1max)return true;
		}
		return false;
	}

	public boolean isOnSideY(EP ep, Item nItem) {
		if(nItem.getInsertedY() >= ep.getY() && nItem.getInsertedX()+nItem.getWidth() > ep.getX()) return true;
		else return false;
	}

	public boolean isOnSideX(EP ep, Item nItem) {
		if(nItem.getInsertedX() >= ep.getX() && nItem.getInsertedY()+nItem.getLength() > ep.getY())return true;
		return false;
	}

	//  #1
	public EP canTakeProjectionYX(Item i, Item k, List<Item> items) {		
		if(k.getInsertedX() >= i.getInsertedX() + i.getWidth() && k.getInsertedY() + k.getLength() < i.getInsertedY() + i.getLength() && k.getInsertedZ() < i.getInsertedZ() + i.getHeight()) {	

			double newx = i.getInsertedX() + i.getWidth();
			double newy = k.getInsertedY() + k.getLength();
			double newz = k.getInsertedZ();

			double rsx = 321 - newx;
			double rsy = 501 - newy;
			double rsz = 273 - newz;

			EP ep = new EP(newx, newy, newz);
			ep.setRSx(rsx);
			ep.setRSy(rsy);
			ep.setRSz(rsz);

			for(int j = 6; j < items.size(); j++) {
				Item itemj = items.get(j);
				if(itemj.getItemId() == i.getItemId()) { // Neem het eigen item niet mee
					if(itemj.getInsertedX() == i.getInsertedX()) {
						if(itemj.getInsertedY() == i.getInsertedY()) {
							if(itemj.getInsertedZ() == i.getInsertedZ()) {
								continue;
							}
						}
					}
				}

				if(newy >= itemj.getInsertedY() + itemj.getLength() || newx >= itemj.getInsertedX() + itemj.getWidth()) {//1	
					continue;
				}
				if(itemj.getInsertedY() > i.getInsertedY() + i.getLength()) { //2
					if(itemj.getInsertedZ() < newz && newz < itemj.getInsertedZ()+itemj.getHeight()) { // dus door hoogte blokt het item
						if(itemj.getInsertedX() <= newx && newx < itemj.getInsertedX() + itemj.getWidth()) {
							if(rsy > itemj.getInsertedY()-newy) {
								rsy = itemj.getInsertedY() - newy;
							}
						}
					}
					continue;
				}
				if(itemj.getInsertedX() > k.getInsertedX() + k.getWidth()) { //3
					if(itemj.getInsertedZ() < newz && newz < itemj.getInsertedZ()+itemj.getHeight()) {
						if(itemj.getInsertedY() <= newy && newy < itemj.getInsertedY() + itemj.getLength()) {
							if(rsx > itemj.getInsertedX()-newx) {
								rsx = itemj.getInsertedX() - newx;
							}
						}
					}
					continue;
				}
				if(itemj.getInsertedX() >= newx && itemj.getInsertedY() >= newy) {
					continue;
				}
				if(newz < itemj.getInsertedZ()) {
					if(itemj.getInsertedX() <= newx && newx < itemj.getInsertedX() + itemj.getWidth()) {
						if(itemj.getInsertedY() <= newy && newy < itemj.getInsertedY() + itemj.getLength()) {
							if(rsz > itemj.getInsertedZ() - newz) {
								rsz = itemj.getInsertedZ() - newz;
							}

						}
					}
					continue;
				}
				if(newz >= itemj.getInsertedZ() + itemj.getHeight()) {
					continue;
				}
				return null;
			}
			ep.setRSx(rsx);
			ep.setRSy(rsy);
			ep.setRSz(rsz);
			if(rsx==0||rsy==0||rsz == 0) return null;
			return ep;
		}
		return null;
	}

	//	#2
	public EP canTakeProjectionYZ(Item i, Item k, List<Item> items) {
		if(k.getInsertedZ() >= i.getInsertedZ() + i.getHeight() && k.getInsertedY() + k.getLength()  < i.getInsertedY() + i.getLength() && k.getInsertedX() < i.getInsertedX() + i.getWidth()) {
			double newx = k.getInsertedX();
			double newy = k.getInsertedY() + k.getLength();
			double newz = i.getInsertedZ() + i.getHeight();

			double rsx = 321 - newx;
			double rsy = 501 - newy;
			double rsz = 273 - newz;

			EP ep = new EP(newx, newy, newz);

			ep.setRSx(rsx);
			ep.setRSy(rsy);
			ep.setRSz(rsz);

			for(int j = 6; j < items.size(); j++) {
				Item itemj = items.get(j);
				if(itemj.getItemId() == i.getItemId()) { // Neem het eigen item niet mee
					if(itemj.getInsertedX() == i.getInsertedX()) {
						if(itemj.getInsertedY() == i.getInsertedY()) {
							if(itemj.getInsertedZ() == i.getInsertedZ()) {
								continue;
							}
						}
					}
				}

				if(newy >= itemj.getInsertedY() + itemj.getLength() || newz >= itemj.getInsertedZ() + itemj.getHeight()) {//1	
					continue;
				}
				if(itemj.getInsertedY() > i.getInsertedY() + i.getLength()) { //2
					if(itemj.getInsertedX() < newx && newx < itemj.getInsertedX()+itemj.getWidth()) { // dus door hoogte blokt het item
						if(itemj.getInsertedZ() <= newz && newz < itemj.getInsertedZ() + itemj.getHeight()) {
							if(rsy > itemj.getInsertedY()-newy) {
								rsy = itemj.getInsertedY() - newy;
							}
						}
					}
					continue;
				}
				if(itemj.getInsertedZ() > k.getInsertedZ() + k.getHeight()) { //3
					if(itemj.getInsertedX() < newx && newx < itemj.getInsertedX()+itemj.getWidth()) {
						if(itemj.getInsertedY() <= newy && newy < itemj.getInsertedY() + itemj.getLength()) {
							if(rsz > itemj.getInsertedZ()-newz) {
								rsz = itemj.getInsertedZ() - newz;
							}
						}
					}
					continue;
				}
				if(itemj.getInsertedY() >= newy && itemj.getInsertedZ() >= newz) {
					continue;
				}
				if(newx < itemj.getInsertedX()) {
					if(itemj.getInsertedZ() <= newz && newz < itemj.getInsertedZ() + itemj.getHeight()) {
						if(itemj.getInsertedY() <= newy && newy < itemj.getInsertedY() + itemj.getLength()) {
							if(rsx > itemj.getInsertedX() - newx) {
								rsx = itemj.getInsertedX() - newx;
							}

						}
					}
					continue;
				}
				if(newx >= itemj.getInsertedX() + itemj.getWidth()) {
					continue;
				}
				return null;
			}
			ep.setRSx(rsx);
			ep.setRSy(rsy);
			ep.setRSz(rsz);
			if(rsx==0||rsy==0||rsz == 0) return null;
			return ep;
		}	
		return null;

	}

	//	#3
	public EP canTakeProjectionXY(Item i, Item k, List<Item> items) {
		if(k.getInsertedY() >= i.getInsertedY() + i.getLength() && k.getInsertedX() + k.getWidth() < i.getInsertedX() + i.getWidth() && k.getInsertedZ() < i.getInsertedZ() + i.getHeight()) {
			double newx = k.getInsertedX() + k.getWidth();
			double newy = i.getInsertedY() + i.getLength();
			double newz = k.getInsertedZ();

			double rsx = 321 - newx;
			double rsy = 501 - newy;
			double rsz = 273 - newz;

			EP ep = new EP(newx, newy, newz);

			ep.setRSx(rsx);
			ep.setRSy(rsy);
			ep.setRSz(rsz);

			for(int j = 6; j < items.size(); j++) {
				Item itemj = items.get(j);
				if(itemj.getItemId() == i.getItemId()) {
					if(itemj.getInsertedX() == i.getInsertedX()) {
						if(itemj.getInsertedY() == i.getInsertedY()) {
							if(itemj.getInsertedZ() == i.getInsertedZ()) {
								continue;
							}
						}
					}
				}

				if(newx >= itemj.getInsertedX() + itemj.getWidth() || newy >= itemj.getInsertedY() + itemj.getLength()) {//1	
					continue;
				}
				if(itemj.getInsertedX() > i.getInsertedX() + i.getWidth()) { //2
					if(itemj.getInsertedZ() < newz && newz < itemj.getInsertedZ()+itemj.getHeight()) { // dus door hoogte blokt het item
						if(itemj.getInsertedY() <= newy && newy < itemj.getInsertedY() + itemj.getLength()) {
							if(rsx > itemj.getInsertedX()-newx) {
								rsx = itemj.getInsertedX() - newx;
							}
						}
					}
					continue;
				}
				if(itemj.getInsertedY() > k.getInsertedY() + k.getLength()) { //3
					if(itemj.getInsertedZ() < newz && newz < itemj.getInsertedZ()+itemj.getHeight()) {
						if(itemj.getInsertedX() <= newx && newx < itemj.getInsertedX() + itemj.getWidth()) {
							if(rsy > itemj.getInsertedY()-newy) {
								rsy = itemj.getInsertedY() - newy;
							}
						}
					}
					continue;
				}
				if(itemj.getInsertedY() >= newy && itemj.getInsertedX() >= newx) {
					continue;
				}
				if(newz < itemj.getInsertedZ()) {
					if(itemj.getInsertedY() <= newy && newy < itemj.getInsertedY() + itemj.getLength()) {
						if(itemj.getInsertedX() <= newx && newx < itemj.getInsertedX() + itemj.getWidth()) {
							if(rsz > itemj.getInsertedZ() - newz) {
								rsz = itemj.getInsertedZ() - newz;
							}

						}
					}
					continue;
				}
				if(newz >= itemj.getInsertedZ() + itemj.getHeight()) {
					continue;
				}
				return null;
			}

			ep.setRSx(rsx);
			ep.setRSy(rsy);
			ep.setRSz(rsz);
			if(rsx==0||rsy==0||rsz == 0) return null;
			return ep;
		}
		return null;
	}
	//	#4
	public EP canTakeProjectionXZ(Item i, Item k, List<Item> items) {	
		if(k.getInsertedZ() >= i.getInsertedZ() + i.getHeight() && k.getInsertedX() + k.getWidth() < i.getInsertedX() + i.getWidth() && k.getInsertedY() < i.getInsertedY() + i.getLength()) {
			double newx = k.getInsertedX() + k.getWidth();
			double newy = k.getInsertedY();
			double newz = i.getInsertedZ() + i.getHeight();

			double rsx = 321 - newx;
			double rsy = 501 - newy;
			double rsz = 273 - newz;

			EP ep = new EP(newx, newy, newz);

			ep.setRSx(rsx);
			ep.setRSy(rsy);
			ep.setRSz(rsz);

			for(int j = 6; j < items.size(); j++) {
				Item itemj = items.get(j);		
				if(itemj.getItemId() == i.getItemId()) {
					if(itemj.getInsertedX() == i.getInsertedX()) {
						if(itemj.getInsertedY() == i.getInsertedY()) {
							if(itemj.getInsertedZ() == i.getInsertedZ()) {
								continue;
							}
						}
					}
				}

				if(newx >= itemj.getInsertedX() + itemj.getWidth() || newz >= itemj.getInsertedZ() + itemj.getHeight()) {//1	
					continue;
				}
				if(itemj.getInsertedX() > i.getInsertedX() + i.getWidth()) { //2
					if(itemj.getInsertedY() < newy && newy < itemj.getInsertedY()+itemj.getLength()) { // dus door hoogte blokt het item
						if(itemj.getInsertedZ() <= newz && newz < itemj.getInsertedZ() + itemj.getHeight()) {
							if(rsx > itemj.getInsertedX()-newx) {
								rsx = itemj.getInsertedX() - newx;
							}
						}
					}
					continue;
				}
				if(itemj.getInsertedZ() > k.getInsertedZ() + k.getHeight()) { //3
					if(itemj.getInsertedY() < newy && newy < itemj.getInsertedY()+itemj.getLength()) {
						if(itemj.getInsertedX() <= newx && newx < itemj.getInsertedX() + itemj.getWidth()) {
							if(rsz > itemj.getInsertedZ()-newz) {
								rsz = itemj.getInsertedZ() - newz;
							}
						}
					}
					continue;
				}
				if(itemj.getInsertedZ() >= newz && itemj.getInsertedX() >= newx) {
					continue;
				}
				if(newy < itemj.getInsertedY()) {
					if(itemj.getInsertedZ() <= newz && newz < itemj.getInsertedZ() + itemj.getHeight()) {
						if(itemj.getInsertedX() <= newx && newx < itemj.getInsertedX() + itemj.getWidth()) {
							if(rsy > itemj.getInsertedY() - newy) {
								rsy = itemj.getInsertedY() - newy;
							}

						}
					}
					continue;
				}
				if(newy >= itemj.getInsertedY() + itemj.getLength()) {
					continue;
				}
				return null;
			}

			ep.setRSx(rsx);
			ep.setRSy(rsy);
			ep.setRSz(rsz);
			if(rsx==0||rsy==0||rsz == 0) return null;
			return ep;
		}
		return null;
	}
	//	#5
	public EP canTakeProjectionZX(Item i, Item k, List<Item> items) {
		if(k.getInsertedX() >= i.getInsertedX() + i.getWidth() && k.getInsertedZ() + k.getHeight() < i.getInsertedZ() + i.getHeight() && k.getInsertedY() < i.getInsertedY() + i.getLength()) {
			double newx = i.getInsertedX() + i.getWidth();
			double newy = k.getInsertedY();
			double newz = k.getInsertedZ() + k.getHeight();

			double rsx = 321 - newx;
			double rsy = 501 - newy;
			double rsz = 273 - newz;

			EP ep = new EP(newx, newy, newz);

			ep.setRSx(rsx);
			ep.setRSy(rsy);
			ep.setRSz(rsz);

			for(int j = 6; j < items.size(); j++) {
				Item itemj = items.get(j);
				if(itemj.getItemId() == i.getItemId()) {
					if(itemj.getInsertedX() == i.getInsertedX()) {
						if(itemj.getInsertedY() == i.getInsertedY()) {
							if(itemj.getInsertedZ() == i.getInsertedZ()) {
								continue;
							}
						}
					}
				}

				if(newz >= itemj.getInsertedZ() + itemj.getHeight() || newx >= itemj.getInsertedX() + itemj.getWidth()) {//1	
					continue;
				}
				if(itemj.getInsertedZ() > i.getInsertedZ() + i.getHeight()) { //2
					if(itemj.getInsertedY() < newy && newy < itemj.getInsertedY()+itemj.getLength()) { // dus door hoogte blokt het item
						if(itemj.getInsertedX() <= newx && newx < itemj.getInsertedX() + itemj.getWidth()) {
							if(rsz > itemj.getInsertedZ()-newz) {
								rsz = itemj.getInsertedZ() - newz;
							}
						}
					}
					continue;
				}
				if(itemj.getInsertedX() > k.getInsertedX() + k.getWidth()) { //3
					if(itemj.getInsertedY() < newy && newy < itemj.getInsertedY()+itemj.getLength()) {
						if(itemj.getInsertedZ() <= newz && newz < itemj.getInsertedZ() + itemj.getHeight()) {
							if(rsx > itemj.getInsertedX()-newx) {
								rsx = itemj.getInsertedX() - newx;
							}
						}
					}
					continue;
				}
				if(itemj.getInsertedX() >= newx && itemj.getInsertedZ() >= newz) {
					continue;
				}
				if(newy < itemj.getInsertedY()) {
					if(itemj.getInsertedX() <= newx && newx < itemj.getInsertedX() + itemj.getWidth()) {
						if(itemj.getInsertedZ() <= newz && newz < itemj.getInsertedZ() + itemj.getHeight()) {
							if(rsy > itemj.getInsertedY() - newy) {
								rsy = itemj.getInsertedY() - newy;
							}

						}
					}
					continue;
				}
				if(newy >= itemj.getInsertedY() + itemj.getLength()) {
					continue;
				}
				return null;
			}

			ep.setRSx(rsx);
			ep.setRSy(rsy);
			ep.setRSz(rsz);
			if(rsx==0||rsy==0||rsz == 0) return null;
			return ep;
		}
		return null;
	}

	//	#6
	public EP canTakeProjectionZY(Item i, Item k, List<Item> items) {
		if(k.getInsertedY() >= i.getInsertedY() + i.getLength() && k.getInsertedZ() + k.getHeight() < i.getInsertedZ() + i.getHeight() && k.getInsertedX() < i.getInsertedX() + i.getWidth() ) {
			double newx = k.getInsertedX();
			double newy = i.getInsertedY() + i.getLength();
			double newz = k.getInsertedZ() + k.getHeight();

			double rsx = 321 - newx;
			double rsy = 501 - newy;
			double rsz = 273 - newz;

			EP ep = new EP(newx, newy, newz);

			ep.setRSx(rsx);
			ep.setRSy(rsy);
			ep.setRSz(rsz);

			for(int j = 6; j < items.size(); j++) {
				Item itemj = items.get(j);
				if(itemj.getItemId() == i.getItemId()) {
					if(itemj.getInsertedX() == i.getInsertedX()) {
						if(itemj.getInsertedY() == i.getInsertedY()) {
							if(itemj.getInsertedZ() == i.getInsertedZ()) {
								continue;
							}
						}
					}
				}
				if(newz >= itemj.getInsertedZ() + itemj.getHeight() || newy >= itemj.getInsertedY() + itemj.getLength()) {//1	
					continue;
				}
				if(itemj.getInsertedZ() > i.getInsertedZ() + i.getHeight()) { //2
					if(itemj.getInsertedX() < newx && newx < itemj.getInsertedX()+itemj.getWidth()) { // dus door hoogte blokt het item
						if(itemj.getInsertedY() <= newy && newy < itemj.getInsertedY() + itemj.getLength()) {
							if(rsz > itemj.getInsertedZ()-newz) {
								rsz = itemj.getInsertedZ() - newz;
							}
						}
					}
					continue;
				}
				if(itemj.getInsertedY() > k.getInsertedY() + k.getLength()) { //3
					if(itemj.getInsertedX() < newx && newx < itemj.getInsertedX()+itemj.getWidth()) {
						if(itemj.getInsertedZ() <= newz && newz < itemj.getInsertedZ() + itemj.getHeight()) {
							if(rsy > itemj.getInsertedY()-newy) {
								rsy = itemj.getInsertedY() - newy;
							}
						}
					}
					continue;
				}
				if(itemj.getInsertedY() >= newy && itemj.getInsertedZ() >= newz) {
					continue;
				}
				if(newx < itemj.getInsertedX()) {
					if(itemj.getInsertedY() <= newy && newy < itemj.getInsertedY() + itemj.getLength()) {
						if(itemj.getInsertedZ() <= newz && newz < itemj.getInsertedZ() + itemj.getHeight()) {
							if(rsx > itemj.getInsertedX() - newx) {
								rsx = itemj.getInsertedX() - newx;
							}

						}
					}
					continue;
				}
				if(newx >= itemj.getInsertedX() + itemj.getWidth()) {
					continue;
				}
				return null;
			}	

			ep.setRSx(rsx);
			ep.setRSy(rsy);
			ep.setRSz(rsz);
			if(rsx==0||rsy==0||rsz == 0) return null;
			return ep;
		}
		return null;
	}
}
