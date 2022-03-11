import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Class that solves the Bin Packing Problem by using a Biased Random Key Genetic Algorithm
 * @author Geuze et al.
 *
 */
public class BRKGA {

	/**
	 * Solve Bin Packing Problem
	 * @param order Order to solve problem for
	 * @param lowerBound Lower bound of an order
	 * @param choiceAisles Choice of incorporating number of aisles or not: 1 for incorporating, 2 for not
	 */
	public static Chromosome solve(Order order, double lowerBound, int choiceAisles, int numCrates) {
		// Parameters
		final int numItems = order.getItems().size();
		final int numPop = 30*numItems; // p : number of vectors in population
		final int numPopElite = (int) (0.1*numPop); // p_e : number of elite vectors in population
		final int numPopMutant = (int) (0.15*numPop); // p_m : number of mutants in population
		final double probElite = 0.7; // rho_e : probability offspring inherits elite's vector component
		final int numGeneration = 200; // Stopping criterion
		Random rand = new Random();
		double aNB = 0.0;
		int numSameANB = 0;
		int boundSameANB = choiceAisles == 1? 10 : 25;
		// Initialize population chromosomes
		ArrayList<Chromosome> population = new ArrayList<Chromosome>();
		for (int p=0; p < numPop; p++) {
			population.add(createMutation(order, rand, numItems, numCrates, choiceAisles));
		}
		// Start algorithm
		for (int g=1; g < numGeneration; g++) {
			Collections.sort(population);
			double newANB = population.get(0).getFitness();
			if (choiceAisles == 1 && population.get(0).getNumCrates() == lowerBound) break;
			if (Math.abs(newANB-aNB) <= 1E-6) {
				numSameANB++;
				if (numSameANB == boundSameANB) break;
			}
			else numSameANB = 0;
			aNB = newANB;
			//			System.out.println("Min number of bins in generation g = " + g + " is " + population.get(0).getNumCrates());
			ArrayList<Chromosome> populationG = new ArrayList<Chromosome>();
			for (int p=0; p < numPopElite; p++) { // Copy elite directly
				populationG.add(population.get(p));
			}
			for (int p=0; p < numPopMutant; p++) { // Create mutations
				populationG.add(createMutation(order, rand, numItems, numCrates, choiceAisles));
			}
			for (int p=0; p < numPop - numPopElite - numPopMutant; p++) { // Create offspring
				double[] BPS = new double[numItems];
				double[] VBO = new double[numItems];
				// Randomly select elite and non-elite parent
				int elite = rand.nextInt(numPopElite); // index on interval [0,p_e]
				int nonElite = rand.nextInt(numPop - numPopElite) + numPopElite; // index on interval [p_e,p]
				// Select each component of elite parent with probability probElite
				for (int i=0; i < numItems; i++) { 
					double prob = rand.nextDouble();
					if (prob < probElite) {
						BPS[i] = population.get(elite).getBPS()[i];
						VBO[i] = population.get(elite).getVBO()[i];
					}
					else {
						BPS[i] = population.get(nonElite).getBPS()[i];
						VBO[i] = population.get(nonElite).getVBO()[i];
					}
				}
				List<Item> items = new ArrayList<>(order.getItems());
				List<Crate> crates = placement(order, items, BPS, VBO);
				//				switch (choiceAisles) {
				//				case 1: crates = placement(order, items, BPS, VBO);
				//				break;
				//				case 2: crates = placementOptAisles(order, items, BPS, VBO, numCrates);
				//				break;
				//				}
				double adjNumBins = 0;
				switch(choiceAisles) {
				case 1: adjNumBins = getAdjustedNumberBins(crates);
				break;
				case 2: adjNumBins = getAdjustedNumberBinsOptAisles(crates);
				break;
				case 3: adjNumBins = getAdjustedNumberBinsFillRateOptAisles(crates);
				break;
				default: break;
				}
				populationG.add(new Chromosome(BPS, VBO, crates, items, adjNumBins, (int) adjNumBins, order.getOrderId()));
			}
			// Replace by new population
			population.clear();
			population.addAll(populationG);
		}
		Collections.sort(population);
		//		int minNumberBins = population.get(0).getNumCrates();
		//		System.out.println("YOU DID IT!!!!!!! NUMBER OF BINS IS " + minNumberBins);
		return population.get(0);
	}

	/**
	 * Placement of items in bins for a given chromosome
	 * @param order Order to pack
	 * @param BPS Box Packing Sequence of chromosome of order to pack
	 * @param VBO Vector of Box Orientations of choromose of order to pack
	 * @return a_ij with size numItems*numCrates, value is 1 if item i is in crate j, 0 otherwise
	 */
	private static List<Crate> placement(Order order, List<Item> items, double[] BPS, double[] VBO) {
		// Initialization
		List<Crate> crates = new ArrayList<Crate>(); // List of bins
		int numCrates = 0; // Number of open bins
		List<Item> itemsToPack = new ArrayList<Item>(items);
		items.clear();
		// Set Box Packing Sequence
		Map<Double, Item> mapBPS = new HashMap<>(); // Pairs of items with random keys
		Map<Double, Integer> mapBPSIndex = new HashMap<>(); // Pairs of items indices with random keys
		for (int i=0; i < BPS.length; i++) {
			mapBPS.put(BPS[i], order.getItems().get(i));
			mapBPSIndex.put(BPS[i], i);
		}
		double[] sortedBPS = Arrays.copyOf(BPS, BPS.length);
		Arrays.sort(sortedBPS); // Sort on fitness
		// Assign each element to a bin
		for (int i=0; i < BPS.length; i++) {
			Item itemToPack = mapBPS.get(sortedBPS[i]); // Item selection
			Crate crateSelected = null; // Initialize with no bin/crate selected
			EP EMS = null; // Initialize with no Empty Maximal Space selected
			@SuppressWarnings("unused")
			int crateIndex = -1;
			double[] dim = {itemToPack.getWidth(), itemToPack.getLength(), itemToPack.getHeight()};
			List<List<Double>> boxOrientations = boxOrientations(dim);
			for (int k=0; k < numCrates; k++) {
				Crate crate = crates.get(k);
				// Box Orientation Selection
				EMS = DFTRC2VBO(itemToPack, crate, boxOrientations);
				if (EMS != null) {
					crateSelected = crate;
					crateIndex = k;
					break;
				}
			}
			// No crate found: open a new one
			if (crateSelected == null) {
				Crate newCrate = new Crate();
				EP newEMS = new EP(0, 0, 0);
				newCrate.addEPToCrate(newEMS);
				crateSelected = newCrate;
				crates.add(crateSelected);
				numCrates++;
				crateIndex = crates.size()-1;
				EMS = newEMS;
			}
			// Box Orientation Selection
			Iterator<List<Double>> iter = boxOrientations.iterator();
			while (iter.hasNext()) {
				List<Double> orientation = iter.next();
				if (!(EMS.getRSx()-EMS.getX() >= orientation.get(0) && EMS.getRSy()-EMS.getY() >= orientation.get(1) && EMS.getRSz()-EMS.getZ() >= orientation.get(2) && crateSelected.getCurrentWeight() + itemToPack.getWeight() <= crateSelected.getMaxWeight())) {
					iter.remove();
				}
			}
			List<Double> orient = new ArrayList<Double>(boxOrientations.get((int) Math.ceil(VBO[mapBPSIndex.get(sortedBPS[i])]*boxOrientations.size()-1)));
			// Item packing
			itemsToPack.remove(itemToPack);
			itemToPack.setInsertedX(EMS.getX());
			itemToPack.setInsertedY(EMS.getY());
			itemToPack.setInsertedZ(EMS.getZ());
			crateSelected.addItemToCrate(itemToPack);
			items.add(itemToPack);
			// Update EMSs of crate
			crateSelected.setEPList(updateEMS(crateSelected, itemsToPack, EMS, orient));
		}
		for (Crate crate : crates) crate.setAllAisles();
		return crates;
	}
	/**
	 * Placement of items in bins for a given chromosome
	 * @param order Order to pack
	 * @param BPS Box Packing Sequence of chromosome of order to pack
	 * @param VBO Vector of Box Orientations of choromose of order to pack
	 * @return a_ij with size numItems*numCrates, value is 1 if item i is in crate j, 0 otherwise
	 */
	@SuppressWarnings("unused")
	private static List<Crate> placementOptAisles(Order order, List<Item> items, double[] BPS, double[] VBO, int numCrates) {
		// Initialization
		ArrayList<Crate> crates = new ArrayList<Crate>(); // List of bins
		List<boolean[]> isAislesVisited = new ArrayList<boolean[]>();
		for (int i=0; i < numCrates; i++) { // Set initial open crates
			Crate newCrate = new Crate();
			EP newEMS = new EP(0, 0, 0);
			newCrate.addEPToCrate(newEMS);
			crates.add(newCrate);
			isAislesVisited.add(new boolean[8]);
		}
		List<Item> itemsToPack = new ArrayList<Item>(items);
		items.clear();
		// Set Box Packing Sequence
		Map<Double, Item> mapBPS = new HashMap<>(); // Pairs of items with random keys
		Map<Double, Integer> mapBPSIndex = new HashMap<>(); // Pairs of items indices with random keys
		for (int i=0; i < BPS.length; i++) {
			mapBPS.put(BPS[i], order.getItems().get(i));
			mapBPSIndex.put(BPS[i], i);
		}
		double[] sortedBPS = Arrays.copyOf(BPS, BPS.length);
		Arrays.sort(sortedBPS); // Sort on fitness
		// Assign each element to a bin
		for (int i=0; i < BPS.length; i++) {
			Item itemToPack = mapBPS.get(sortedBPS[i]); // Item selection
			double[] dim = {itemToPack.getWidth(), itemToPack.getLength(), itemToPack.getHeight()};
			List<List<Double>> boxOrientations = boxOrientations(dim);
			Crate crateSelected = null; // Initialize with no bin/crate selected
			EP EMS = null; // Initialize with no Empty Maximal Space selected
			// Crate selection variables
			int minNumAisles = Integer.MAX_VALUE;
			int crateIndex = -1;
			for (int k=0; k < numCrates; k++) {
				Crate crate = crates.get(k);
				// Box Orientation Selection
				EP EMSCrateK = DFTRC2VBO(itemToPack, crate, boxOrientations);
				if (EMSCrateK != null) { // Determine number of aisles to go through
					boolean inCrate = isAislesVisited.get(k)[itemToPack.getAisle()];
					int numAisles = getNumAisles(isAislesVisited.get(k), itemToPack); // TODO: het verschil bepalen
					if (inCrate) {
						minNumAisles = numAisles;
						crateSelected = crate;
						EMS = EMSCrateK;
						crateIndex = k;
						break;
					}
					else if (numAisles < minNumAisles) {
						minNumAisles = numAisles;
						crateSelected = crate;
						EMS = EMSCrateK;
						crateIndex = k;
					}
				}
			}
			// No crate found: open a new one
			if (crateSelected == null) {
				Crate newCrate = new Crate();
				EP newEMS = new EP(0, 0, 0);
				newCrate.addEPToCrate(newEMS);
				crateSelected = newCrate;
				crates.add(crateSelected);
				isAislesVisited.add(new boolean[8]);
				numCrates++;
				crateIndex = crates.size()-1;
				EMS = newEMS;
			}
			// Box Orientation Selection
			Iterator<List<Double>> iter = boxOrientations.iterator();
			while (iter.hasNext()) {
				List<Double> orientation = iter.next();
				if (!(EMS.getRSx()-EMS.getX() >= orientation.get(0) && EMS.getRSy()-EMS.getY() >= orientation.get(1) && EMS.getRSz()-EMS.getZ() >= orientation.get(2) && crateSelected.getCurrentWeight() + itemToPack.getWeight() <= crateSelected.getMaxWeight())) {
					iter.remove();
				}
			}
			List<Double> orient = new ArrayList<Double>(boxOrientations.get((int) Math.ceil(VBO[mapBPSIndex.get(sortedBPS[i])]*boxOrientations.size()-1)));
			// Item packing
			crateSelected.addItemToCrate(itemToPack);
			isAislesVisited.get(crateIndex)[itemToPack.getAisle()] = true;
			itemsToPack.remove(itemToPack);
			items.add(new Item(itemToPack.getItemId(), orient.get(0), orient.get(1), orient.get(2), itemToPack.getWeight(), EMS.getX(), EMS.getY(), EMS.getZ(), crateIndex, itemToPack.getAisle()));
			// Update EMSs of crate
			crateSelected.setEPList(updateEMS(crateSelected, itemsToPack, EMS, orient));
		}
		for (Crate crate : crates) crate.setAllAisles();
		return crates;
	}

	/**
	 * DFTRC-2^2 algorithm to find optimal Empty Maximal Space to fit item to pack in
	 * @param itemToPack 
	 * @param crate
	 * @param winningOrientation
	 * @return Empty Maximal Space
	 */
	private static EP DFTRC2VBO(Item itemToPack, Crate crate, List<List<Double>> boxOrientations) {
		double maxDist = -1;
		EP EMS = null;
		// Dimensions crate
		double width = crate.getWidth();
		double length = crate.getLength();
		double height = crate.getHeight();
		for (EP EMSi : crate.getEP()) {
			double x1 = EMSi.getX();
			double y1 = EMSi.getY();
			double z1 = EMSi.getZ();
			for (List<Double> orientation : boxOrientations) {
				if (EMSi.getRSx()-EMSi.getX() >= orientation.get(0) && EMSi.getRSy()-EMSi.getY() >= orientation.get(1) && EMSi.getRSz()-EMSi.getZ() >= orientation.get(2) && crate.getCurrentWeight() + itemToPack.getWeight() <= crate.getMaxWeight()) {
					double distance = Math.pow(width-x1-orientation.get(0), 2) + Math.pow(length-y1-orientation.get(1), 2) + Math.pow(height-z1-orientation.get(2), 2);
					if (distance > maxDist) {
						maxDist = distance;
						EMS = EMSi; // Save winning EMS
					}
				}
			}
		}
		return EMS;
	}

	/**
	 * Returns fitness value of a chromosome, based on NB + fill rate least loaded bin
	 * @param crates
	 * @return fitness value
	 */
	public static double getAdjustedNumberBins(List<Crate> crates) {
		double leastLoad = Double.POSITIVE_INFINITY;
		for (int k=0; k < crates.size(); k++) {
			double loadCrate = 0;
			for (int l=0; l < crates.get(k).getItemList().size(); l++) {
				loadCrate += crates.get(k).getItemList().get(l).getVolume();
			}
			if (loadCrate < leastLoad)
				leastLoad = loadCrate;
		}
		return crates.size() + leastLoad/crates.get(0).getVolume();
	}

	/**
	 * Returns fitness value of a chromosome, based on NB + number of aisles. 
	 * 	= NB + 0.005 * numAisles 
	 * @param crates
	 * @return
	 */
	private static double getAdjustedNumberBinsOptAisles(List<Crate> crates) {
		ShortestPath sp = new ShortestPath(crates);
		double aNBA = (double) crates.size();
		double penaltyAisle = 0.005;
		aNBA += penaltyAisle*sp.computeTotalPathLength(crates);
		return aNBA;
	}

	/**
	 * Returns fitness value of a chromosome, based on NB + fill rate least loaded bin (LLB) + number of aisles
	 *  = NB + fill rate LLB + 0.005 * numAisles
	 * @param crates
	 * @return
	 */
	private static double getAdjustedNumberBinsFillRateOptAisles(List<Crate> crates) {
		double leastLoad = Double.POSITIVE_INFINITY;
		for (int k=0; k < crates.size(); k++) {
			double loadCrate = 0;
			for (int l=0; l < crates.get(k).getItemList().size(); l++) {
				loadCrate += crates.get(k).getItemList().get(l).getVolume();
			}
			if (loadCrate < leastLoad)
				leastLoad = loadCrate;
		}
		leastLoad = Math.round(leastLoad*10)/10.0; // Round to 1 decimal
		ShortestPath sp = new ShortestPath(crates);
		double penaltyAisle = 0.005;
		return (double) crates.size() + leastLoad/crates.get(0).getVolume() + penaltyAisle * sp.computeTotalPathLength(crates);
	}

	/**
	 * Return updated list of Empty Maximal Spaces of a crate
	 * @param crate
	 * @param EMS34
	 * @param itemsToPack
	 * @return List of EMSs
	 */
	private static List<EP> updateEMS(Crate crate, List<Item> itemsToPack, EP EMS34, List<Double> orientation) {
		List<EP> newList = new ArrayList<EP>();
		for (EP EMS12 : crate.getEP()) { // For every EMS
			double x1 = EMS12.getX();
			double x2 = EMS12.getRSx();
			double x3 = EMS34.getX();
			double x4 = EMS34.getX() + orientation.get(0);
			double y1 = EMS12.getY();
			double y2 = EMS12.getRSy();
			double y3 = EMS34.getY();
			double y4 = EMS34.getY() + orientation.get(1);
			double z1 = EMS12.getZ();
			double z2 = EMS12.getRSz();
			double z3 = EMS34.getZ();
			double z4 = EMS34.getZ() + orientation.get(2);
			if (!(x1 <= x3 && x4 <= x2 && y1 <= y3 && y4 <= y2 && z1 <= z3 && z4 <= z2)) { // V1
				if ((x4 <= x1 || x2 <= x3) || (y4 <= y1 || y2 <= y3) || (z4 <= z1 || z2 <= z3)) { // x4 <= x1 && y4 <= y1 && z4 <= z1 
					newList.add(EMS12);
					continue; // Do not update EMS
				}
				boolean x13 = x1 <= x3;
				boolean x42 = x4 <= x2;
				boolean y13 = y1 <= y3;
				boolean y42 = y4 <= y2;
				boolean z13 = z1 <= z3;
				boolean z42 = z4 <= z2;
				if (!x13 && !x42) {
					x3 = x1;
					x4 = x2;
				}
				else if (!x13 && x42) x3 = x1;
				else if (x13 && !x42) x4 = x2;
				if (!y13 && !y42) {
					y3 = y1;
					y4 = y2;
				}
				else if (!y13 && y42) y3 = y1;
				else if (y13 && !y42) y4 = y2;
				if (!z13 && !z42) {
					z3 = z1;
					z4 = z2;
				}
				else if (!z13 && z42) z3 = z1;
				else if (z13 && !z42) z4 = z2;
			}
			// Update EMS
			newList.add(new EP(x1, y1, z1, x3, y2, z2));
			newList.add(new EP(x4, y1, z1, x2, y2, z2));
			newList.add(new EP(x1, y1, z1, x2, y3, z2));
			newList.add(new EP(x1, y4, z1, x2, y2, z2));
			newList.add(new EP(x1, y1, z1, x2, y2, z3));
			newList.add(new EP(x1, y1, z4, x2, y2, z2));
		}
		removeInfiniteThinness(newList);
		removeCheckVolume(newList, itemsToPack);
		removeCheckDimensions(newList, itemsToPack);
		removeInscribed(newList);
		return newList;
	}

	/**
	 * Remove Empty Maximal Spaces from list that have infinite thinness
	 * @param list
	 */
	private static void removeInfiniteThinness(List<EP> list) {
		Iterator<EP> iter = list.iterator();
		while (iter.hasNext()) {
			EP EMS = iter.next();
			if (EMS.getRSx() - EMS.getX() <= 1E-6 || EMS.getRSy() - EMS.getY() <= 1E-6 || EMS.getRSz() - EMS.getZ() <= 1E-6) {
				iter.remove();
			}
		}
	}

	/**
	 * Remove Empty Maximal Spaces from list which are smaller than the volume of each of the remaining items to pack
	 * @param list
	 * @param itemsToPack
	 */
	private static void removeCheckVolume(List<EP> list, List<Item> itemsToPack) {
		double volumeSmallestItem = Double.POSITIVE_INFINITY;
		for (Item item : itemsToPack) {
			double volumeItem = item.getVolume();
			if (volumeItem < volumeSmallestItem) volumeSmallestItem = volumeItem;
		}
		Iterator<EP> iter = list.iterator();
		while (iter.hasNext()) {
			EP EMS = iter.next();
			double volumeEMS = (EMS.getRSx()-EMS.getX())*(EMS.getRSy()-EMS.getY())*(EMS.getRSz()-EMS.getZ());
			if (volumeEMS < volumeSmallestItem)
				iter.remove();
		}
	}

	/**
	 * Remove Empty Maximal Spaces from list from which the smallest dimension is smaller than the smallest dimension
	 * 	of each of the remaining items to pack
	 * @param list
	 * @param itemsToPack
	 */
	private static void removeCheckDimensions(List<EP> list, List<Item> itemsToPack) {
		double smallestDimItems = Double.POSITIVE_INFINITY;
		for (Item item : itemsToPack) {
			if (item.getWidth() < smallestDimItems) smallestDimItems = item.getWidth();
			if (item.getLength() < smallestDimItems) smallestDimItems = item.getLength();
			if (item.getHeight() < smallestDimItems) smallestDimItems = item.getHeight();
		}
		Iterator<EP> iter = list.iterator();
		while (iter.hasNext()) {
			EP EMS = iter.next();
			double smallestDimEMS = EMS.getRSx()-EMS.getX();
			if (EMS.getRSy()-EMS.getY() < smallestDimEMS) smallestDimEMS = EMS.getRSy()-EMS.getY();
			if (EMS.getRSz()-EMS.getZ() < smallestDimEMS) smallestDimEMS = EMS.getRSz()-EMS.getZ();
			if (smallestDimEMS < smallestDimItems) iter.remove();
		}
	}

	/**
	 * Remove Empty Maximal Spaces from list which are totally inscribed in other EMSs
	 * @param list
	 */
	private static void removeInscribed(List<EP> list) {
		Iterator<EP> iterInscr = list.iterator();
		while (iterInscr.hasNext()) {
			EP EMSInscr = iterInscr.next();
			Iterator<EP> iterInscrIn = list.iterator();
			while (iterInscrIn.hasNext()) {
				EP EMSInscrIn = iterInscrIn.next();
				if (EMSInscr != EMSInscrIn) {
					if (EMSInscrIn.getX() <= EMSInscr.getX() && EMSInscrIn.getRSx() >= EMSInscr.getRSx() && EMSInscrIn.getY() <= EMSInscr.getY() && EMSInscrIn.getRSy() >= EMSInscr.getRSy() && EMSInscrIn.getZ() <= EMSInscr.getZ() && EMSInscrIn.getRSz() >= EMSInscr.getRSz()) {
						iterInscr.remove();
						break;
					}
				}
			}
		}
	}

	/**
	 * Create new chromosome
	 * @param order
	 * @param rand
	 * @param numItems
	 * @param choice_D2_VBO
	 * @return Chromosome
	 */
	private static Chromosome createMutation(Order order, Random rand, int numItems, int numCrates, int choiceAisles) {
		double[] BPS = new double[numItems]; // Box Packing Sequence BPS
		double[] VBO = new double[numItems]; // Vector Box Orientation VBO
		for (int i=0; i < numItems; i++) { 
			BPS[i] = rand.nextDouble();
			VBO[i] = rand.nextDouble();
		}
		List<Item> items = new ArrayList<>(order.getItems());
		List<Crate> crates = placement(order, items, BPS, VBO);
		//		switch (choiceAisles) {
		//		case 1: crates = placement(order, items, BPS, VBO);
		//		break;
		//		case 2: crates = placementOptAisles(order, items, BPS, VBO, numCrates);
		//		break;
		//		}
		double adjNumBins = 0;
		switch(choiceAisles) {
		case 1: adjNumBins = getAdjustedNumberBins(crates);
		break;
		case 2: adjNumBins = getAdjustedNumberBinsOptAisles(crates);
		break;
		case 3: adjNumBins = getAdjustedNumberBinsFillRateOptAisles(crates);
		break;
		default: break;
		}
		return new Chromosome(BPS, VBO, crates, items, adjNumBins, (int) adjNumBins, order.getOrderId());
	}

	/**
	 * Returns a list of all possible orientations of an item
	 * @param dim Dimensions of the item
	 * @return
	 */
	private static List<List<Double>> boxOrientations(double[] dim) {
		List<List<Double>> ans = new ArrayList<>();
		List<Double> ds = new ArrayList<>();
		boolean[] freq = new boolean[dim.length];
		permute(dim, ds,ans, freq); // Get all possible orientations
		return ans;
	}

	/**
	 * Recursion for box orientations
	 * @param dim
	 * @param ds
	 * @param ans
	 * @param freq
	 */
	private static void permute(double[] dim, List<Double> ds, List<List<Double>> ans, boolean[] freq){
		if(ds.size() == dim.length){
			ans.add(new ArrayList<>(ds));
			return;
		}
		for(int i=0; i < dim.length; i++){
			if(!freq[i]){
				freq[i] = true;
				ds.add(dim[i]);
				permute(dim, ds, ans, freq);
				ds.remove(ds.size()-1);
				freq[i] = false;
			}
		}
	}

	private static int getNumAisles(boolean[] isAislesVisited, Item item) {
		int left = 0;
		int right = 0;
		for (int i=0; i < isAislesVisited.length; i++) {
			if (i==0 || i==2 || i==4 || i==6  && (isAislesVisited[i] || item.getAisle() == i)) left++;
			else if (isAislesVisited[i] || item.getAisle() == i) right++;
		}
		return 2*Math.max(left, right);
	}

}
