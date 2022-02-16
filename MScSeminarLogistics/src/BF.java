import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BF {
	private final Order order;

	public BF(Order order) {
		this.order = order;
	}

	public List<Crate> computeBF() { // PrintWriter out, int index
		List<Item> sortedItemList = new ArrayList<>(order.getItems());	
		Collections.sort(sortedItemList); // Order a list of items

		List<Crate> crates = new ArrayList<Crate>();
		crates.add(new Crate()); // Add a first crate to the list of crates
		crates.get(0).getEP().add(new EP(0,0,0));

		// Maak de 6 items die de rand van de box voorstellen
		Item bottum = new Item(-1,crates.get(0).getWidth(), crates.get(0).getLength(), 0.0,0.0); // bodem item
		Item top = new Item(-2,crates.get(0).getWidth(), crates.get(0).getLength(), 0.0,0.0); // deksel item
		Item sideyz = new Item(-3, 0.0, crates.get(0).getLength(), crates.get(0).getHeight(), 0.0);
		Item sideyzOp = new Item(-4, 0.0, crates.get(0).getLength(), crates.get(0).getHeight(), 0.0);
		Item sidexz = new Item(-5,crates.get(0).getWidth(), 0.0, crates.get(0).getHeight(), 0.0);
		Item sidexzOp = new Item(-6, crates.get(0).getWidth(), 0.0, crates.get(0).getHeight(), 0.0);

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
			if(i == 15) {
				int test = 0;
				test = test +1;
			}
					
			List<Item> rotatedItem = new ArrayList<>(); // List with all rotations of an item
			Item a = sortedItemList.get(i);
			// Rotatie 1
			Item it = new Item(a.getItemId() , a.getWidth(), a.getLength(), a.getHeight(), a.getWeight());
			// Rotatie 2
			Item it2 = new Item(a.getItemId(), a.getLength(), a.getWidth(), a.getHeight(), a.getWeight());
			// Rotatie 3
			Item it3 = new Item(a.getItemId(), a.getHeight(), a.getLength(), a.getWidth(), a.getWeight());
			// Rotatie 4
			Item it4 = new Item(a.getItemId(), a.getLength(), a.getHeight(), a.getWidth(), a.getWeight());
			// Rotatie 5
			Item it5 = new Item(a.getItemId(), a.getWidth(), a.getHeight(), a.getLength(), a.getWeight());
			// Rotatie 6
			Item it6 = new Item(a.getItemId(), a.getHeight(), a.getWidth(), a.getLength(), a.getWeight());
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
			for(int k = 0; k < crates.size(); k++) { // Voor alle kratten
				if(crates.get(k).getCurrentWeight() + sortedItemList.get(i).getWeight() <= crates.get(k).getMaxWeight()) {
					for(int j = 0; j < crates.get(k).getEP().size(); j++) { // Voor alle EP's in de kratten
						for(int r = 0; r < rotatedItem.size();r++) { // Voor alle rotaties
							EP ep = crates.get(k).getEP().get(j);
							double rsx = ep.getRSx() - rotatedItem.get(r).getWidth();
							double rsy = ep.getRSy() - rotatedItem.get(r).getLength();
							double rsz = ep.getRSz() - rotatedItem.get(r).getHeight();
							if(rsx > 0 && rsy > 0 && rsz > 0) {
								double compare = rsx + rsy + rsz;
								if(compare < f) {
									epIndex = j;
									crateIndex = k;
									f = compare;
									rotatedIndex = r;
								}
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

				for(int r = 0; r < rotatedItem.size();r++) {
					rotatedItem.get(r);
					double rsx = 321 - rotatedItem.get(r).getWidth();
					double rsy = 501 - rotatedItem.get(r).getLength();
					double rsz = 273 - rotatedItem.get(r).getHeight();
					if(rsx > 0 && rsy > 0 && rsz > 0) {
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
				// Add it the crate
				c.addItemToCrate(rotatedItem.get(rotatedIndex));
				crates.add(c);		
			}
			else { // so the item is added to an already existing bin
				rotatedItem.get(rotatedIndex).setInsertedX(crates.get(crateIndex).getEP().get(epIndex).getX());
				rotatedItem.get(rotatedIndex).setInsertedY(crates.get(crateIndex).getEP().get(epIndex).getY());
				rotatedItem.get(rotatedIndex).setInsertedZ(crates.get(crateIndex).getEP().get(epIndex).getZ());
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
				if(j == 6)
				{
					int test = 0;
					test = test +1;
				}
				//#1
				if(crates.get(crateIndex).getItemList().get(j).insertedX + crates.get(crateIndex).getItemList().get(j).getWidth() > maxbound[0]) {
					EP ep = canTakeProjectionYX(crates.get(crateIndex).getItemList().get(j), crates.get(crateIndex).getItemList().get(crates.get(crateIndex).getItemList().size() - 1),crates.get(crateIndex).getItemList());

					if(ep != null) {
						newEP.add(ep);
						maxbound[0] = crates.get(crateIndex).getItemList().get(j).insertedX + crates.get(crateIndex).getItemList().get(j).getWidth();
					}
				}
				//	#2
				if(crates.get(crateIndex).getItemList().get(j).insertedZ + crates.get(crateIndex).getItemList().get(j).getHeight() > maxbound[1]) {
					EP ep = canTakeProjectionYZ(crates.get(crateIndex).getItemList().get(j), crates.get(crateIndex).getItemList().get(crates.get(crateIndex).getItemList().size() - 1), crates.get(crateIndex).getItemList());

					if(ep != null) {
						maxbound[1] = crates.get(crateIndex).getItemList().get(j).insertedZ + crates.get(crateIndex).getItemList().get(j).getHeight();
						newEP.add(ep);
					}
				}
				//	#3
				if(crates.get(crateIndex).getItemList().get(j).insertedY + crates.get(crateIndex).getItemList().get(j).getLength() > maxbound[2]) {
					EP ep = canTakeProjectionXY(crates.get(crateIndex).getItemList().get(j), crates.get(crateIndex).getItemList().get(crates.get(crateIndex).getItemList().size() - 1), crates.get(crateIndex).getItemList());
					if(ep != null) {
						maxbound[2] = crates.get(crateIndex).getItemList().get(j).insertedY + crates.get(crateIndex).getItemList().get(j).getLength();
						newEP.add(ep);
					}
				}
				//	#4
				if(crates.get(crateIndex).getItemList().get(j).insertedZ + crates.get(crateIndex).getItemList().get(j).getHeight() > maxbound[3]) {
					EP ep = canTakeProjectionXZ(crates.get(crateIndex).getItemList().get(j), crates.get(crateIndex).getItemList().get(crates.get(crateIndex).getItemList().size() - 1), crates.get(crateIndex).getItemList());
					if(ep != null) {
						maxbound[3] = crates.get(crateIndex).getItemList().get(j).insertedZ + crates.get(crateIndex).getItemList().get(j).getHeight();
						newEP.add(ep);
					}
				}
				//	#5
				if(crates.get(crateIndex).getItemList().get(j).insertedX + crates.get(crateIndex).getItemList().get(j).getWidth() > maxbound[4]) {
					EP ep = canTakeProjectionZX(crates.get(crateIndex).getItemList().get(j), crates.get(crateIndex).getItemList().get(crates.get(crateIndex).getItemList().size() - 1), crates.get(crateIndex).getItemList());
					if(ep != null) {
						maxbound[4] = crates.get(crateIndex).getItemList().get(j).insertedX + crates.get(crateIndex).getItemList().get(j).getWidth();
						newEP.add(ep);
					}
				}
				//				#6
				if(crates.get(crateIndex).getItemList().get(j).insertedY + crates.get(crateIndex).getItemList().get(j).getLength() > maxbound[5]) {
					EP ep = canTakeProjectionZY(crates.get(crateIndex).getItemList().get(j), crates.get(crateIndex).getItemList().get(crates.get(crateIndex).getItemList().size() - 1),crates.get(crateIndex).getItemList());
					if(ep != null) {
						maxbound[5] = crates.get(crateIndex).getItemList().get(j).insertedY + crates.get(crateIndex).getItemList().get(j).getLength();
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
			List<EP> toBeRemoved = new ArrayList<>();
			Item nItem = crates.get(crateIndex).getItemList().get(crates.get(crateIndex).getItemList().size() - 1);
			for(int j = 0; j < crates.get(crateIndex).getEP().size(); j++) {
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
				if(ep.getZ() >= nItem.insertedZ && ep.getZ() < nItem.insertedZ + nItem.getHeight()) {
					boolean isSidey = isOnSideY(ep, nItem);
//					if(ep.getX() <= nItem.insertedx && isOnSideY(ep, nItem) ) {
					if(isOnSideX(ep,nItem)) {
						ep.setRSx(Math.min(ep.getRSx(), nItem.insertedX - ep.getX()));
					}
					boolean isSidex = isOnSideX(ep,nItem);
//					if(ep.getY() <= nItem.insertedy && isOnSideY(ep, nItem)) {
					if(isOnSideY(ep,nItem)) {
						ep.setRSy(Math.min(ep.getRSy(),nItem.insertedY - ep.getY()));
					}
				}
				if(ep.getZ() <= nItem.insertedZ) {// && isOnSideY(ep,nItem) && isOnSideX(ep, nItem)
					if(ep.getX() <= nItem.getInsertedX() && nItem.getInsertedX() <= ep.getX()+ep.getRSx()
						&& ep.getY() <= nItem.getInsertedY() && nItem.getInsertedY()<=ep.getX()+ep.getRSy())
					{
						ep.setRSz(Math.min(ep.getRSz(), nItem.insertedZ - ep.getZ()));
					}
					else if(ep.getX() <= nItem.getInsertedX() && nItem.getInsertedX() < ep.getX()+ep.getRSx()
							&& ep.getY() < nItem.getInsertedY()+ nItem.getLength())
					{
						ep.setRSz(Math.min(ep.getRSz(), nItem.insertedZ - ep.getZ()));
					}
					else if(ep.getY() <= nItem.getInsertedY() && nItem.getInsertedY() < ep.getY()+ep.getRSy()
							&& ep.getX() < nItem.getInsertedX()+nItem.getWidth())
					{
						ep.setRSz(Math.min(ep.getRSz(), nItem.insertedZ - ep.getZ()));
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
			boolean test = true;
		}


		// Print solution
		//		System.out.println("Number of crates used in this order: " + crates.size());
		//		for(int i = 0; i < crates.size(); i++) {
		//			Crate c = crates.get(i);
		//			System.out.print("Crate number " + (i + 1)+"\t");
		//			for(int j = 6 ; j < c.getItemList().size(); j++) {
		//				System.out.print((int)c.getItemList().get(j).getItemId() + "\t");
		//			}
		//			System.out.println();
		//		}
		//		System.out.println("Order "+((int)order.getOrderId()));
		return crates;

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
		if(k.insertedX >= i.insertedX + i.getWidth() && k.insertedY + k.getLength() < i.insertedY + i.getLength() && k.insertedZ < i.insertedZ + i.getHeight()) {	

			double newx = i.insertedX + i.getWidth();
			double newy = k.insertedY + k.getLength();
			double newz = k.insertedZ;

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

				if(newy >= itemj.getInsertedY() + itemj.getLength() || newx >= itemj.getInsertedX() + itemj.getWidth()) {		
					continue;
				}
				if(itemj.getInsertedY() > i.getInsertedY() + i.getLength()) {
					if(newz <= itemj.getInsertedZ() && rsz > itemj.getInsertedZ()-newz)rsz = itemj.getInsertedZ() - newz;
					else if(itemj.getInsertedZ() < newz && newz < itemj.getInsertedZ()+itemj.getHeight())
					{
						if(rsx > itemj.getInsertedX()-newx)rsx = itemj.getInsertedX() - newx;
						if(rsy > itemj.getInsertedY()-newy)rsy = itemj.getInsertedY() - newy;
					}
					continue;
				}
				if(itemj.getInsertedX()> k.getInsertedX() + k.getWidth()) {
					if(newz <= itemj.getInsertedZ() && rsz > itemj.getInsertedZ()-newz)rsz = itemj.getInsertedZ() - newz;
					else if(itemj.getInsertedZ() < newz && newz < itemj.getInsertedZ()+itemj.getHeight())
					{
						if(rsx > itemj.getInsertedX()-newx)rsx = itemj.getInsertedX() - newx;
						if(rsy > itemj.getInsertedY()-newy)rsy = itemj.getInsertedY() - newy;
					}
					continue;
				}
				if(itemj.getInsertedX() >= newx && itemj.getInsertedY() >= newy)
				{
					if(newz <= itemj.getInsertedZ()&&rsz > itemj.getInsertedZ()-newz)rsz = itemj.getInsertedZ()-newz;
					else if(itemj.getInsertedZ() < newz && newz < itemj.getInsertedZ()+ itemj.getHeight())
					{
						if(rsx > itemj.getInsertedX()-newx)rsx = itemj.getInsertedX()-newx;
						if(rsy > itemj.getInsertedY()-newy)rsy = itemj.getInsertedY()-newy;
					}
					continue;
				}
				if(newz < itemj.getInsertedZ())
				{
					if(rsz > itemj.getInsertedZ() - newz)rsz = itemj.getInsertedZ()-newz;
					continue;
				}
				if(newz >= itemj.getInsertedZ() + itemj.getHeight())continue;

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
		if(k.insertedZ >= i.insertedZ + i.getHeight() && k.insertedY + k.getLength()  < i.insertedY + i.getLength() && k.insertedX < i.insertedX + i.getWidth()) {
			double newx = k.insertedX;
			double newy = k.insertedY + k.getLength();
			double newz = i.insertedZ + i.getHeight();

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

				if(newy >= itemj.getInsertedY() + itemj.getLength() || newz >= itemj.getInsertedZ() + itemj.getHeight()) {		
					continue;
				}
				if(itemj.getInsertedY() > i.getInsertedY() + i.getLength()) {
					if(newx <= itemj.getInsertedX() && rsx > itemj.getInsertedX()-newx)rsx = itemj.getInsertedX() - newx;
					else if(itemj.getInsertedX() < newx && newx < itemj.getInsertedX()+itemj.getWidth())
					{
						if(rsz > itemj.getInsertedZ()-newz)rsz = itemj.getInsertedZ() - newz;
						if(rsy > itemj.getInsertedY()-newy)rsy = itemj.getInsertedY() - newy;
					}
					continue;
				}
				if(itemj.getInsertedZ()> k.getInsertedZ() + k.getLength()) {
					if(newx <= itemj.getInsertedX() && rsx > itemj.getInsertedX()-newx)rsx = itemj.getInsertedX() - newx;
					else if(itemj.getInsertedX() < newx && newx < itemj.getInsertedX()+itemj.getWidth())
					{
						if(rsz > itemj.getInsertedZ()-newz)rsz = itemj.getInsertedZ() - newz;
						if(rsy > itemj.getInsertedY()-newy)rsy = itemj.getInsertedY() - newy;
					}
					continue;
				}
				if(itemj.getInsertedZ() >= newz && itemj.getInsertedY() >= newy)
				{
					if(newx <= itemj.getInsertedX()&&rsx > itemj.getInsertedX()-newx)rsx = itemj.getInsertedX()-newx;
					else if(itemj.getInsertedX() < newx && newx < itemj.getInsertedX()+ itemj.getWidth())
					{
						if(rsz > itemj.getInsertedZ()-newx)rsz = itemj.getInsertedZ()-newz;
						if(rsy > itemj.getInsertedY()-newy)rsy = itemj.getInsertedY()-newy;
					}
					continue;
				}
				if(newx < itemj.getInsertedX())
				{
					if(rsx > itemj.getInsertedX() - newx)rsx = itemj.getInsertedX()-newx;
					continue;
				}
				if(newx >= itemj.getInsertedX() + itemj.getWidth())
				{
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
		if(k.insertedY >= i.insertedY + i.getLength() && k.insertedX + k.getWidth() < i.insertedX + i.getWidth() && k.insertedZ < i.insertedZ + i.getHeight()) {
			double newx = k.insertedX + k.getWidth();
			double newy = i.insertedY + i.getLength();
			double newz = k.insertedZ;

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
				if(newx  >= itemj.getInsertedX()+itemj.getWidth() || newy >= itemj.getInsertedY()+ itemj.getLength())
				{
					continue;
				}
				if(itemj.getInsertedY() > k.getInsertedY() + k.getLength())
				{
					if(newz <= itemj.getInsertedZ() && rsz > itemj.getInsertedZ()-newz)rsz=itemj.getInsertedZ()-newz;
					else if(itemj.getInsertedZ() < newz && newz < itemj.getInsertedZ()+itemj.getHeight())
					{
						if(rsx > itemj.getInsertedX()-newx)rsx = itemj.getInsertedX()-newx;
						if(rsy > itemj.getInsertedY()-newy)rsy = itemj.getInsertedY()-newy;
					}
					continue;
				}
				if(itemj.getInsertedX() > i.getInsertedX()+i.getWidth())
				{
					if(newz <= itemj.getInsertedZ() && rsz > itemj.getInsertedZ()-newz)rsz=itemj.getInsertedZ()-newz;
					else if(itemj.getInsertedZ() < newz && newz < itemj.getInsertedZ()+itemj.getHeight())
					{
						if(rsx > itemj.getInsertedX()-newx)rsx = itemj.getInsertedX()-newx;
						if(rsy > itemj.getInsertedY()-newy)rsy = itemj.getInsertedY()-newy;
					}
					continue;
				}
				if(itemj.getInsertedX() >= newx && itemj.getInsertedY() >= newy)
				{
					if(newz <= itemj.getInsertedZ() && rsz > itemj.getInsertedZ()-newz)rsz=itemj.getInsertedZ()-newz;
					else if(itemj.getInsertedZ() < newz && newz < itemj.getInsertedZ()+itemj.getHeight())
					{
						if(rsx > itemj.getInsertedX()-newx)rsx = itemj.getInsertedX()-newx;
						if(rsy > itemj.getInsertedY()-newy)rsy = itemj.getInsertedY()-newy;
					}
					continue;
				}
				if(newz < itemj.getInsertedZ())
				{
					if(rsz > itemj.getInsertedZ()-newz)rsz=itemj.getInsertedZ()-newz;
					continue;
				}
				if(newz >= itemj.getInsertedZ()+itemj.getHeight())continue;

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
		if(k.insertedZ >= i.insertedZ + i.getHeight() && k.insertedX + k.getWidth() < i.insertedX + i.getWidth() && k.insertedY < i.insertedY + i.getLength()) {
			double newx = k.insertedX + k.getWidth();
			double newy = k.insertedY;
			double newz = i.insertedZ + i.getHeight();

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
				if(newx  >= itemj.getInsertedX()+itemj.getWidth() || newz >= itemj.getInsertedZ()+ itemj.getHeight())
				{
					continue;
				}
				if(itemj.getInsertedZ() > k.getInsertedZ() + k.getHeight())
				{
					if(newy <= itemj.getInsertedY() && rsy > itemj.getInsertedY()-newy)rsy=itemj.getInsertedY()-newy;
					else if(itemj.getInsertedY() < newy && newy < itemj.getInsertedY()+itemj.getLength())
					{
						if(rsx > itemj.getInsertedX()-newx)rsx = itemj.getInsertedX()-newx;
						if(rsz > itemj.getInsertedZ()-newz)rsz = itemj.getInsertedZ()-newz;
					}
					continue;
				}
				if(itemj.getInsertedX() > i.getInsertedX()+i.getWidth())
				{
					if(newy <= itemj.getInsertedY() && rsy > itemj.getInsertedY()-newy)rsy=itemj.getInsertedY()-newy;
					else if(itemj.getInsertedY() < newy && newy < itemj.getInsertedY()+itemj.getLength())
					{
						if(rsx > itemj.getInsertedX()-newx)rsx = itemj.getInsertedX()-newx;
						if(rsz > itemj.getInsertedZ()-newz)rsz = itemj.getInsertedZ()-newz;
					}
					continue;
				}
				if(itemj.getInsertedX() >= newx && itemj.getInsertedZ() >= newz)
				{
					if(newy <= itemj.getInsertedY() && rsy > itemj.getInsertedY()-newy)rsy=itemj.getInsertedY()-newy;
					else if(itemj.getInsertedY() < newy && newy < itemj.getInsertedY()+itemj.getLength())
					{
						if(rsx > itemj.getInsertedX()-newx)rsx = itemj.getInsertedX()-newx;
						if(rsz > itemj.getInsertedZ()-newz)rsz = itemj.getInsertedZ()-newz;
					}
					continue;
				}
				if(newy < itemj.getInsertedY())
				{
					if(rsy > itemj.getInsertedY()-newy)rsy=itemj.getInsertedY()-newy;
					continue;
				}
				if(newy >= itemj.getInsertedY()+itemj.getLength())continue;

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
		if(k.insertedX >= i.insertedX + i.getWidth() && k.insertedZ + k.getHeight() < i.insertedZ + i.getHeight() && k.insertedY < i.insertedY + i.getLength()) {
			double newx = i.insertedX + i.getWidth();
			double newy = k.insertedY;
			double newz = k.insertedZ + k.getHeight();

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
				if(newx >= itemj.getInsertedX() + itemj.getWidth() || newz >= itemj.getInsertedZ()+itemj.getHeight()) {
					continue;
				}
				if(itemj.getInsertedZ() > i.getInsertedZ()+i.getHeight()){
					if(newy <= itemj.getInsertedY() && rsy > itemj.getInsertedY()-newy)rsy = itemj.getInsertedY()-newy;
					else if(itemj.getInsertedY() < newy && newy < itemj.getInsertedY()+itemj.getLength())
					{
						if(rsx > itemj.getInsertedX()-newx)rsx = itemj.getInsertedX()-newx;
						if(rsz > itemj.getInsertedZ()-newz)rsz = itemj.getInsertedZ()-newz;
					}
					continue;
				}
				if(itemj.getInsertedX() > k.getInsertedX()+k.getWidth()) {
					if(newy <= itemj.getInsertedY() && rsy > itemj.getInsertedY()-newy)rsy = itemj.getInsertedY()-newy;
					else if(itemj.getInsertedY() < newy && newy < itemj.getInsertedY()+itemj.getLength())
					{
						if(rsx > itemj.getInsertedX()-newx)rsx = itemj.getInsertedX()-newx;
						if(rsz > itemj.getInsertedZ()-newz)rsz = itemj.getInsertedZ()-newz;
					}
					continue;
				}
				if(itemj.getInsertedX() >= newx && itemj.getInsertedZ() >= newz) {
					if(newy <= itemj.getInsertedY() && rsy > itemj.getInsertedY()-newy)rsy = itemj.getInsertedY()-newy;
					else if(itemj.getInsertedY() < newy && newy < itemj.getInsertedY()+itemj.getLength())
					{
						if(rsx > itemj.getInsertedX()-newx)rsx = itemj.getInsertedX()-newx;
						if(rsz > itemj.getInsertedZ()-newz)rsz = itemj.getInsertedZ()-newz;
					}
					continue;
				}
				if(newy < itemj.getInsertedY()) {
					if(rsy > itemj.getInsertedY()-newy)rsy = itemj.getInsertedY()-newy;
					continue;
				}
				if(newy > itemj.getInsertedY()+itemj.getLength())continue;

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
		if(k.insertedY >= i.insertedY + i.getLength() && k.insertedZ + k.getHeight() < i.insertedZ + i.getHeight() && k.insertedX < i.insertedX + i.getWidth() ) {
			double newx = k.insertedX;
			double newy = i.insertedY + i.getLength();
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
				if(newy >= itemj.getInsertedY() + itemj.getLength() || newz >= itemj.getInsertedZ()+itemj.getHeight()) {
					continue;
				}
				if(itemj.getInsertedZ() > i.getInsertedZ()+i.getHeight()) {
					if(newx <= itemj.getInsertedX() && rsx > itemj.getInsertedX()-newx)rsx = itemj.getInsertedX()-newx;
					else if(itemj.getInsertedX() < newx && newx < itemj.getInsertedX()+itemj.getWidth())
					{
						if(rsy > itemj.getInsertedY()-newy)rsy = itemj.getInsertedY()-newy;
						if(rsz > itemj.getInsertedZ()-newz)rsz = itemj.getInsertedZ()-newz;
					}
					continue;
				}
				if(itemj.getInsertedY() > k.getInsertedY() + k.getLength()) {
					if(newx <= itemj.getInsertedX() && rsx > itemj.getInsertedX()-newx)rsx = itemj.getInsertedX()-newx;
					else if(itemj.getInsertedX() < newx && newx < itemj.getInsertedX()+itemj.getWidth())
					{
						if(rsy > itemj.getInsertedY()-newy)rsy = itemj.getInsertedY()-newy;
						if(rsz > itemj.getInsertedZ()-newz)rsz = itemj.getInsertedZ()-newz;
					}
					continue;
				}
				if(itemj.getInsertedY() >= newy && itemj.getInsertedZ() >= newz) {
					if(newx <= itemj.getInsertedX() && rsx > itemj.getInsertedX()-newx)rsx = itemj.getInsertedX()-newx;
					else if(itemj.getInsertedX() < newx && newx < itemj.getInsertedX()+itemj.getWidth())
					{
						if(rsy > itemj.getInsertedY()-newy)rsy = itemj.getInsertedY()-newy;
						if(rsz > itemj.getInsertedZ()-newz)rsz = itemj.getInsertedZ()-newz;
					}
					continue;
				}
				if(newx < itemj.getInsertedX()) {
					if(rsx > itemj.getInsertedX()-newx)rsx = itemj.getInsertedX()-newx;
					continue;
				}
				if(newx > itemj.getInsertedX()+itemj.getWidth())continue;

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