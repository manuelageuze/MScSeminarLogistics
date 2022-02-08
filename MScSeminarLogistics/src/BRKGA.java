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
	 * @param choice_D2_VBO 1 if DFTRC-2^2 is used, 2 if DFTRC-2-VBO is used
	 */
	public static int solve(Order order, double lowerBound, int choice_D2_VBO) {
		// Parameters
		final int numItems = order.getItems().size();
		final int numPop = 30*numItems; // p : number of vectors in population
		final int numPopElite = (int) (0.1*numPop); // p_e : number of elite vectors in population
		final int numPopMutant = (int) (0.15*numPop); // p_m : number of mutants in population
		final double probElite = 0.7; // rho_e : probability offspring inherits elite's vector component
		final int numGeneration = 200; // Stopping criterion
		Random rand = new Random();
		// Initialize population chromosomes
		ArrayList<Chromosome> population = new ArrayList<Chromosome>();
		for (int p=0; p < numPop; p++) {
			population.add(createMutation(order, rand, numItems, choice_D2_VBO));
		}		
		// Start algorithm
		for (int g=1; g < numGeneration; g++) {
			Collections.sort(population);
			if (population.get(0).getNumCrates() == lowerBound) break;
//			System.out.println("Min number of bins in generation g = " + g + " is " + population.get(0).getNumCrates());
			ArrayList<Chromosome> populationG = new ArrayList<Chromosome>();
			for (int p=0; p < numPopElite; p++) { // Copy elite directly
				populationG.add(population.get(p));
			}
			for (int p=0; p < numPopMutant; p++) { // Create mutations
				populationG.add(createMutation(order, rand, numItems, choice_D2_VBO));
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
				ArrayList<ArrayList<Integer>> openCrates = placement(order, BPS, VBO, choice_D2_VBO);
				double adjNumBins = getAdjustedNumberBins(order, openCrates);
				populationG.add(new Chromosome(BPS, VBO, adjNumBins, (int) adjNumBins));
			}
			// Replace by new population
			population.clear();
			population.addAll(populationG);
		}
		Collections.sort(population);
		int minNumberBins = population.get(0).getNumCrates();
		System.out.println("YOU DID IT!!!!!!! NUMBER OF BINS IS " + minNumberBins);
		return minNumberBins;
	}

	/**
	 * Placement of items in bins for a given chromosome
	 * @param order Order to pack
	 * @param BPS Box Packing Sequence of chromosome of order to pack
	 * @param VBO Vector of Box Orientations of choromose of order to pack
	 * @param choice_D2_VBO 1 if DFTRC-2^2 is used, 2 if DFTRC-2-VBO is used
	 * @return a_ij with size numItems*numCrates, value is 1 if item i is in crate j, 0 otherwise
	 */
	private static ArrayList<ArrayList<Integer>> placement(Order order, double[] BPS, double[] VBO, int choice_D2_VBO) {
		// Initialization
		ArrayList<Crate> crates = new ArrayList<Crate>(); // List of bins
		ArrayList<ArrayList<Integer>> openCrates = new ArrayList<ArrayList<Integer>>(); // Let element be 1 if item is in bin, 0 o.w.
		int numCrates = 0; // Number of open bins
		List<Item> itemsToPack = new ArrayList<Item>(order.getItems());
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
			int crateIndex = -1;
			List<Double> orient = new ArrayList<Double>();
			orient.add(itemToPack.getWidth());
			orient.add(itemToPack.getLength());
			orient.add(itemToPack.getHeight());
			for (int k=0; k < numCrates; k++) {
//				System.out.println("num crates = " + openCrates.size() + ", weights = ");
//				for (Crate c : crates) {
//					System.out.print(c.getCurrentWeight() + " ");
//				}
//				System.out.println();
				Crate crate = crates.get(k);
				switch (choice_D2_VBO) { // Box Orientation Selection
				case 1: EMS = DFTRC2(itemToPack, crate, orient);
				break;
				case 2: EMS = DFTRC2VBO(itemToPack, crate, orient, VBO[mapBPSIndex.get(sortedBPS[i])]);
				break;
				}
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
				ArrayList<Integer> openCrate = new ArrayList<Integer>();
				for (int j=0; j < BPS.length; j++) {
					openCrate.add(0);
				}
				openCrates.add(openCrate);
				numCrates++;
				crateIndex = openCrates.size()-1;
				EMS = newEMS;
				switch (choice_D2_VBO) { // Box Orientation Selection
				case 1: DFTRC2(itemToPack, crateSelected, orient);
				break;
				case 2: DFTRC2VBO(itemToPack, crateSelected, orient, VBO[mapBPSIndex.get(sortedBPS[i])]);
				break;
				}
			}
			// Item packing
			openCrates.get(crateIndex).set(mapBPSIndex.get(sortedBPS[i]), 1);
			crateSelected.addItemToCrate(itemToPack);
			itemsToPack.remove(itemToPack);
			// Update EMSs of crate
			crateSelected.setEPList(updateEMS(crateSelected, itemsToPack, EMS, orient));
		}
		return openCrates;
	}

	/**
	 * DFTRC-2^2 algorithm to find optimal Empty Maximal Space to fit item to pack in
	 * @param itemToPack 
	 * @param crate
	 * @param winningOrientation
	 * @return Empty Maximal Space
	 */
	private static EP DFTRC2(Item itemToPack, Crate crate, List<Double> winningOrientation) {
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
			double[] dim = {x1, y1, z1};
			List<List<Double>> boxOrientations = boxOrientations(dim);
			for (List<Double> orientation : boxOrientations) {
				if (EMSi.getRSx()-EMSi.getX() >= orientation.get(0) && EMSi.getRSy()-EMSi.getY() >= orientation.get(1) && EMSi.getRSz()-EMSi.getZ() >= orientation.get(2) && crate.getCurrentWeight() + itemToPack.getWeight() <= crate.getMaxWeight()) {
					double distance = Math.pow(width-x1-orientation.get(0), 2) + Math.pow(length-y1-orientation.get(1), 2) + Math.pow(height-z1-orientation.get(2), 2);
					if (distance > maxDist) {
						maxDist = distance;
						// Save winning orientation
						winningOrientation.clear();
						winningOrientation.addAll(orientation);
						// Save winning EMS
						EMS = EMSi;
					}
				}
			}
		}
		return EMS;
	}

	/**
	 * DFTRC-2-VBO algorithm to find optimal Empty Maximal Space to fit item to pack in
	 * @param itemToPack
	 * @param crate
	 * @param orient
	 * @param boxOrientation
	 * @return Empty Maximal Space
	 */
	private static EP DFTRC2VBO(Item itemToPack, Crate crate, List<Double> orient, double boxOrientation) {
		double maxDist = -1;
		EP EMS = null;
		// Dimensions crate
		double width = crate.getWidth();
		double length = crate.getLength();
		double height = crate.getHeight();
		for (EP EMSi : crate.getEP()) {
			// Find boxOrientation
			double x1 = EMSi.getX();
			double y1 = EMSi.getY();
			double z1 = EMSi.getZ();
			double[] dim = {x1, y1, z1};
			List<List<Double>> boxOrientations = boxOrientations(dim);
			int numOrientations = boxOrientations.size();
			orient.clear();
			orient.addAll(boxOrientations.get((int) Math.ceil(boxOrientation*numOrientations)-1));
			if (EMSi.getRSx()-EMSi.getX() >= orient.get(0) && EMSi.getRSy()-EMSi.getY() >= orient.get(1) && EMSi.getRSz()-EMSi.getZ() >= orient.get(2) && crate.getCurrentWeight() + itemToPack.getWeight() <= crate.getMaxWeight()) {
				double distance = Math.pow(width-x1-orient.get(0), 2) + Math.pow(length-y1-orient.get(1), 2) + Math.pow(height-z1-orient.get(2), 2);
				if (distance > maxDist) {
					maxDist = distance;
					EMS = EMSi;
				}
			}
		}
		return EMS;
	}

	/**
	 * Returns fitness value of a chromosome
	 * @param order
	 * @param openCrates
	 * @return fitness value
	 */
	private static double getAdjustedNumberBins(Order order, ArrayList<ArrayList<Integer>> openCrates) {
		double leastLoad = Double.POSITIVE_INFINITY;
		Crate crate = new Crate();
		double crateCapacity = crate.getVolume();
		for (int k=0; k < openCrates.size(); k++) {
			double loadCrate = 0;
			for (int l=0; l < openCrates.get(k).size(); l++) {
				if (openCrates.get(k).get(l) == 1)
					loadCrate += order.getItems().get(l).getVolume();
			}
			if (loadCrate < leastLoad)
				leastLoad = loadCrate;
		}
		return openCrates.size() + leastLoad/crateCapacity;
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
				if (x4 <= x1 && x2 <= x3 && y4 <= y1 && y2 <= y3 && z4 <= z1 && z2 <= z3) { // x4 <= x1 && y4 <= y1 && z4 <= z1 
					newList.add(EMS12);
					continue; // Do not update EMS
				}
				if (x4 > x1) x3 = x1; 
				if (x2 > x3) x4 = x2;
				if (y4 > y1) y3 = y1;
				if (y2 > y3) y4 = y2;
				if (z4 > z1) z3 = z1;
				if (z2 > z3) z4 = z2;
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
	private static Chromosome createMutation(Order order, Random rand, int numItems, int choice_D2_VBO) {
		double[] BPS = new double[numItems]; // Box Packing Sequence BPS
		double[] VBO = new double[numItems]; // Vector Box Orientation VBO
		for (int i=0; i < numItems; i++) { 
			BPS[i] = rand.nextDouble();
			VBO[i] = rand.nextDouble();
		}
		ArrayList<ArrayList<Integer>> openCrates = placement(order, BPS, VBO, choice_D2_VBO);
		double adjNumBins = getAdjustedNumberBins(order, openCrates);
		return new Chromosome(BPS, VBO, adjNumBins, (int) adjNumBins);
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
}