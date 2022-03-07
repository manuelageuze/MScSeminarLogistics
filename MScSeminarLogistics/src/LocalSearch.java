import java.util.ArrayList;
import java.util.List;

public class LocalSearch {

	public LocalSearch() {

	}

	public List<OrderPicker> performLocalSearch(List<OrderPicker> orderPickers, Graph graph) {
		// Do neighborhood one
		List<OrderPicker> best = orderPickers;
		int iter = 0;
		System.out.println();
		while(iter < 100)
		{
			List<OrderPicker> curr = N1(best,graph);
			if(curr == null)
			{
				System.out.println("Try N2");
				List<OrderPicker> curr2 = N2(best,graph);
				if(curr2 == null)break;
				best = curr2;
			}
			else
			{
				best = curr;
			}
			iter++;
			int value = 0;
			for(OrderPicker i : best)
			{
				value += i.getShortestPath();
			}
			System.out.println("Iteration: "+iter+" , value: "+value);
		}
		return best;
	}

	public List<OrderPicker> NeighborhoodOne(List<OrderPicker> orderPickers, Graph graph) {
		boolean improvement = true;
		// Perform LS until no improvement
		while(improvement == true)
		{
			improvement = false;
			
			for(int i = 0 ; i < orderPickers.size(); i++)// for all order pickers
			{ 
				OrderPicker basePicker = orderPickers.get(i);
				for(int j = i + 1; j < orderPickers.size(); j++)
				{
					OrderPicker changePicker = orderPickers.get(j);
					// current values
					int value1 = basePicker.getShortestPath();
					int value2 = changePicker.getShortestPath();
					int currentShortestPath = value1 + value2;
					
					for(int c = 0 ; c < orderPickers.get(i).getCrates().size(); c++) {		
						for(int d = 0; d < orderPickers.get(j).getCrates().size(); d++) {
							List<Crate> baseCrates = new ArrayList<>(basePicker.getCrates());
							List<Crate> changeCrates = new ArrayList<>(changePicker.getCrates());

							// Change solution
							baseCrates.set(c, changePicker.getCrates().get(d));
							changeCrates.set(d, basePicker.getCrates().get(c));
							
							// Changed solution
							ShortestPath basePaths = new ShortestPath(baseCrates, graph);
							int totalAislesBase = basePaths.computeTotalPathLength(baseCrates, graph);
							ShortestPath possiblePaths = new ShortestPath(changeCrates, graph);
							int totalAislesPos = possiblePaths.computeTotalPathLength(changeCrates, graph);
							int newShortestPath = totalAislesBase + totalAislesPos;

							if(newShortestPath < currentShortestPath) {
								basePicker.getCrates().set(c, baseCrates.get(d));
								changePicker.getCrates().set(d, changeCrates.get(c));
								
								basePicker.computeAislesToVisit();
								basePicker.setShortestPath(totalAislesBase);
								changePicker.computeAislesToVisit();		
								changePicker.setShortestPath(totalAislesPos);
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
	
	public List<OrderPicker> N1(List<OrderPicker> orderPickers, Graph graph)
	{
		List<OrderPicker> improved = new ArrayList<>();
		OrderPicker improvedBase = orderPickers.get(0);
		OrderPicker improvedChange = orderPickers.get(0);
		int bestImprovement = 0;
		for(int i = 0 ; i < orderPickers.size() ; i++)
		{
			OrderPicker base = orderPickers.get(i);
			for(int j = i+1 ; j < orderPickers.size() ; j++)
			{
				OrderPicker change = orderPickers.get(j);
				int currentShortestPath = base.getShortestPath() + change.getShortestPath();
				for(int c = 0 ; c < base.getCrates().size() ; c++)
				{
					for(int d = 0 ; d < change.getCrates().size() ; d++)
					{
						List<Crate> baseCrates = new ArrayList<>();
						List<Crate> changeCrates = new ArrayList<>();
						for(int p = 0 ; p < base.getCrates().size() ; p++)
						{
							if(p == c)baseCrates.add(change.getCrates().get(d));
							else baseCrates.add(base.getCrates().get(p));
						}
						for(int p = 0 ; p < change.getCrates().size();p++)
						{
							if(p == d)changeCrates.add(base.getCrates().get(c));
							else changeCrates.add(change.getCrates().get(p));
						}
//						List<Crate> baseCrates = new ArrayList<>(base.getCrates());
//						List<Crate> changeCrates = new ArrayList<>(change.getCrates());
//						baseCrates.set(c, change.getCrates().get(d));
//						changeCrates.set(d, base.getCrates().get(c));
						
						ShortestPath basePaths = new ShortestPath(baseCrates, graph);
						int totalAislesBase = basePaths.computeTotalPathLength(baseCrates, graph);
						ShortestPath possiblePaths = new ShortestPath(changeCrates, graph);
						int totalAislesPos = possiblePaths.computeTotalPathLength(changeCrates, graph);
						int newShortestPath = totalAislesBase + totalAislesPos;
						
						if(currentShortestPath - newShortestPath > bestImprovement)
						{
							bestImprovement = currentShortestPath - newShortestPath;
							improvedBase = new OrderPicker(i,baseCrates);
							improvedChange = new OrderPicker(j,changeCrates);
							improvedBase.computeAislesToVisit();
							improvedBase.setShortestPath(totalAislesBase);
							improvedChange.computeAislesToVisit();		
							improvedChange.setShortestPath(totalAislesPos);
						}
					}
				}
			}
		}
		if(bestImprovement > 0)
		{
			for(int p = 0; p < orderPickers.size() ; p++)
			{
				if(p == improvedBase.getIndex())
				{
					improved.add(improvedBase);
				}
				else if(p == improvedChange.getIndex())
				{
					improved.add(improvedChange);
				}
				else
				{
					improved.add(orderPickers.get(p));
				}
			}
			return improved;
		}
		return null;
	}
	
	public List<OrderPicker> N2(List<OrderPicker> orderPickers, Graph graph)
	{
		List<OrderPicker> improved = new ArrayList<>();
		OrderPicker improvedBase = orderPickers.get(0);
		OrderPicker improvedChange = orderPickers.get(0);
		int bestImprovement = 0;
		for(int i = 0 ; i < orderPickers.size() ; i++)
		{
			OrderPicker base = orderPickers.get(i);
			for(int j = i+1 ; j < orderPickers.size() ; j++)
			{
				OrderPicker change = orderPickers.get(j);
				int currentShortestPath = base.getShortestPath() + change.getShortestPath();
				for(int c1 = 0 ; c1 < base.getCrates().size() ; c1++)
				{
					for(int c2 = c1+1 ; c2 < base.getCrates().size(); c2++)
					{
						for(int d1 = 0 ; d1 < change.getCrates().size() ; d1++)
						{
							for(int d2 = d1+1 ; d2 < change.getCrates().size() ;d2++)
							{
								List<Crate> baseCrates = new ArrayList<>();
								List<Crate> changeCrates = new ArrayList<>();
								for(int p = 0 ; p < base.getCrates().size() ; p++)
								{
									if(p == c1)baseCrates.add(change.getCrates().get(d1));
									else if(p == c2)baseCrates.add(change.getCrates().get(d2));
									else baseCrates.add(base.getCrates().get(p));
								}
								for(int p = 0 ; p < change.getCrates().size();p++)
								{
									if(p == d1)changeCrates.add(base.getCrates().get(c1));
									else if(p == d2)changeCrates.add(base.getCrates().get(c2));
									else changeCrates.add(change.getCrates().get(p));
								}
								ShortestPath basePaths = new ShortestPath(baseCrates, graph);
								int totalAislesBase = basePaths.computeTotalPathLength(baseCrates, graph);
								ShortestPath possiblePaths = new ShortestPath(changeCrates, graph);
								int totalAislesPos = possiblePaths.computeTotalPathLength(changeCrates, graph);
								int newShortestPath = totalAislesBase + totalAislesPos;
								
								if(currentShortestPath - newShortestPath > bestImprovement)
								{
									bestImprovement = currentShortestPath - newShortestPath;
									improvedBase = new OrderPicker(i,baseCrates);
									improvedChange = new OrderPicker(j,changeCrates);
									improvedBase.computeAislesToVisit();
									improvedBase.setShortestPath(totalAislesBase);
									improvedChange.computeAislesToVisit();		
									improvedChange.setShortestPath(totalAislesPos);
								}
							}
						}
					}
				}
			}
		}
		if(bestImprovement > 0)
		{
			for(int p = 0; p < orderPickers.size() ; p++)
			{
				if(p == improvedBase.getIndex())
				{
					improved.add(improvedBase);
				}
				else if(p == improvedChange.getIndex())
				{
					improved.add(improvedChange);
				}
				else
				{
					improved.add(orderPickers.get(p));
				}
			}
			return improved;
		}
		return null;
	}


}
