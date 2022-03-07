import java.util.ArrayList;
import java.util.List;

public class LocalSearch {

	public LocalSearch() {

	}

	public List<OrderPicker> performLocalSearch(List<OrderPicker> orderPickers, Graph graph) {
			// Do neighborhood one
			return NeighborhoodOne(orderPickers, graph);	
	}

	public List<OrderPicker> NeighborhoodOne(List<OrderPicker> orderPickers, Graph graph) {
		boolean improvement = true;
		while(improvement == true) {
			improvement = false;
			for(int i = 0 ; i < orderPickers.size(); i++) { // Voor alle orderpickers
				OrderPicker basePicker = orderPickers.get(i);
				for(int j = i + 1; j < orderPickers.size(); j++) {
					OrderPicker possiblePicker = orderPickers.get(j);
					for(int c = 0 ; c < orderPickers.get(i).getCrates().size(); c++) {		
						for(int d = 0; d < orderPickers.get(j).getCrates().size(); d++) {
							List<Crate> baseCrates = new ArrayList<>(basePicker.getCrates());
							List<Crate> possibleCrates = new ArrayList<>(possiblePicker.getCrates());
							

							// Waardes nu
							int value1 = basePicker.getShortestPath();
							int value2 = possiblePicker.getShortestPath();
							int currentShortestPath = value1 + value2;

							// Veranderen
							baseCrates.set(c, possiblePicker.getCrates().get(d));
							possibleCrates.set(d, basePicker.getCrates().get(d));
							// Nieuwe waarde
							ShortestPath basePaths = new ShortestPath(baseCrates, graph);
							int totalAislesBase = basePaths.computeTotalPathLength(baseCrates, graph);
							ShortestPath possiblePaths = new ShortestPath(possibleCrates, graph);
							int totalAislesPos = possiblePaths.computeTotalPathLength(possibleCrates, graph);
							int newShortestPath = totalAislesBase + totalAislesPos;

							if(newShortestPath < currentShortestPath) {
								basePicker.getCrates().set(c, baseCrates.get(d));
								possiblePicker.getCrates().set(d, possibleCrates.get(c));
								
								basePicker.computeAislesToVisit();
								basePicker.setShortestPath(totalAislesBase);
								possiblePicker.computeAislesToVisit();		
								possiblePicker.setShortestPath(totalAislesPos);
								improvement = true;
								System.out.println("improvement done");
							}
						}
					}
				}
			}
		}
		return orderPickers;
	}


}
