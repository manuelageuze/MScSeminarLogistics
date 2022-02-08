import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class LowerBoundModel {
	private final Order order;
	private final Map<Double,Item> items;
//
	public LowerBoundModel(Order order, Map<Double,Item> items) throws IloException {
		this.order = order;
		this.items = items;
	}

	public static double setCoveringLB(Order order, Map<Double, Item> items) throws IloException, IOException {
		Crate crate = new Crate();
		IloCplex cplex = new IloCplex();
		cplex.setOut(null);
		
		IloNumVar[] y = new IloNumVar[order.getItems().size() + 1];
		IloNumVar[][] x = new IloNumVar[order.getItems().size()][order.getItems().size() + 1];
		
		for(int j = 0; j < order.getItems().size() + 1;j++) {
			y[j] = cplex.boolVar("Y"+j);
			for(int i = 0; i < order.getItems().size();i++) {
				x[i][j] = cplex.boolVar("X"+i+","+j);
			}
		}
		
		// Objective
		IloNumExpr obj = cplex.constant(0.0);
		for(int j = 0; j < order.getItems().size() +1; j++) {
			obj = cplex.sum(obj, y[j]);
		}
		cplex.addMinimize(obj);
		
		// Constraint 1 weight
		for(int j = 0; j < order.getItems().size() + 1;j++) {
			IloNumExpr lhs = cplex.constant(0.0);
			for(int i = 0; i < order.getItems().size(); i++) {
				lhs = cplex.sum(lhs, cplex.prod(x[i][j], order.getItems().get(i).getWeight()));
			}
			IloNumExpr rhs = cplex.constant(0.0);
			rhs = cplex.sum(rhs,cplex.prod(crate.getMaxWeight(), y[j]));
			cplex.addLe(lhs, rhs);	
		}
		
		// Constraint 2 volume
		for(int j = 0; j < order.getItems().size() + 1;j++) {
			IloNumExpr lhs = cplex.constant(0.0);
			for(int i = 0; i < order.getItems().size(); i++) {
				lhs = cplex.sum(lhs, cplex.prod(x[i][j], order.getItems().get(i).getVolume()));
			}
			IloNumExpr rhs = cplex.constant(0.0);
			rhs = cplex.sum(rhs,cplex.prod(crate.getVolume(), y[j]));
			cplex.addLe(lhs, rhs);	
		}
		// Constraint 3 all items are placed
		for(int i = 0; i < order.getItems().size(); i++) {
			IloNumExpr lhs = cplex.constant(0.0);
			for(int j = 0; j < order.getItems().size() + 1; j++) {
				lhs = cplex.sum(lhs,x[i][j]);
			}
			cplex.addGe(lhs, 1.0);
		}
		cplex.solve();
//		System.out.println(cplex.isPrimalFeasible());
		
		if(cplex.isPrimalFeasible()) {
//			System.out.println("Solution value: " + cplex.getObjValue());
			int objValue = (int)cplex.getObjValue();
			writeSolution(order,x, y, crate, cplex);
			cplex.close();
			return objValue;
			
		}
		cplex.close();
		return Double.NEGATIVE_INFINITY;
		
	}
	private static void writeSolution(Order order,IloNumVar[][] x, IloNumVar[] y, Crate crate, IloCplex cplex) throws IloException, IOException
	{
		int counter = 0;
		List<List<Integer>> bins = new ArrayList<>();
		List<Double> volumes = new ArrayList<>();
		List<Item> items = order.getItems();
		FileWriter myWriter = new FileWriter("LB_solution.txt");
		for(int j = 0 ; j < y.length ; j++)
		{
			int y_j = (int)(cplex.getValue(y[j])+0.5);
			if(y_j == 1)
			{
				double volume = 0.0;
				List<Integer> bin = new ArrayList<>();
				for(int i = 0 ; i < x.length ; i++)
				{
					int x_ij = (int) (cplex.getValue(x[i][j])+0.5);
					if(x_ij == 1)
					{
						bin.add((int)items.get(i).getItemId());
						volume += (items.get(i).getVolume());
					}
				}
				bins.add(bin);
				volumes.add(volume);
				counter++;
			}
		}
		System.out.print("Order: "+order.getOrderId()+"\nCrates: "+counter+"\n");
		for(int i = 0 ; i < bins.size() ; i++)
		{
			System.out.print((i+1)+"\t"+volumes.get(i)/crate.getVolume()+"\t");
			for(Integer j : bins.get(i))
			{
				System.out.print(j+" ");
			}
			System.out.print("\n");
		}
		System.out.print("\n");
		myWriter.close();
	}
	/*
	 * Deze methode werkt niet toch?
	 */
	public static void findLowerBound(Order order, Map<Double,Item> items) throws IloException {
		Crate crate = new Crate();
		IloCplex cplex = new IloCplex();
//		cplex.setOut(null);

		IloNumVar B = cplex.intVar(0, order.getItems().size() + 1, "B");
		IloNumVar[] n = new IloNumVar[order.getItems().size()];
		IloNumVar delta = cplex.boolVar("delta");
		IloNumVar[][] s = new IloNumVar[order.getItems().size()][order.getItems().size()];
		double M = 1000;//order.getItems().size() + 1;

		for(int i = 0; i < order.getItems().size(); i++) {
			n[i] = cplex.intVar(1, order.getItems().size() + 1, "n"+i);
			for(int j = 0; j < order.getItems().size(); j++) {
				s[i][j] = cplex.boolVar("s"+i+","+j);
			}
		}

		// Objective
		IloNumExpr obj = cplex.constant(0.0);
		obj = cplex.sum(obj, B);
		cplex.addMinimize(obj);

		// Constraint 21
		for(int j = 0; j < order.getItems().size(); j++) {
			IloNumExpr expr = cplex.constant(0.0);
			expr = cplex.sum(expr,order.getItems().get(j).getWeight());
			for(int i = 0; i < order.getItems().size();i++) {
				if(i != j) {
					expr = cplex.sum(expr, cplex.prod(order.getItems().get(i).getWeight(),s[i][j]));
				}
			}
			cplex.addLe(expr, crate.getMaxWeight());
		}

		// Constraint 22
		for(int j = 0; j < order.getItems().size(); j++) {
			IloNumExpr expr = cplex.constant(0.0);
			expr = cplex.sum(expr,order.getItems().get(j).getVolume());
			for(int i = 0; i < order.getItems().size();i++) {
				if(i != j) {
					expr = cplex.sum(expr, cplex.prod(order.getItems().get(i).getVolume(),s[i][j]));
				}
			}
			cplex.addLe(expr, crate.getVolume());
		}

		// Constraint 23
		for(int i = 0; i < order.getItems().size(); i++) {
			for(int j = 0; j < order.getItems().size(); j++) {
				if(i != j) {
					IloNumExpr lhs = cplex.constant(0.0);
					lhs = cplex.sum(lhs,cplex.diff(n[i],n[j]));
					IloNumExpr rhs = cplex.constant(0.0);
					rhs = cplex.prod(M, cplex.diff(1.0, s[i][j]));
					cplex.addLe(lhs, rhs);
				}
			}
		}

		// Constraint 24
		for(int i = 0; i < order.getItems().size(); i++) {
			for(int j = 0; j < order.getItems().size(); j++) {
				if(i != j) {
					IloNumExpr lhs = cplex.constant(0.0);
					lhs = cplex.sum(lhs,cplex.diff(n[j], n[i]));
					IloNumExpr rhs = cplex.constant(0.0);
					rhs = cplex.prod(M, cplex.diff(1.0, s[i][j]));
					cplex.addLe(lhs, rhs);
				}
			}
		}

		// Constraint 25
		for(int i = 0; i < order.getItems().size(); i++) {
			for(int j = 0; j < order.getItems().size(); j++) {
				if(i != j) {
					IloNumExpr lhs = cplex.constant(0.0);
					lhs = cplex.sum(lhs,cplex.diff(n[i], n[j]));
					IloNumExpr rhs = cplex.constant(0.0);
					rhs = cplex.sum(rhs, cplex.prod(M, s[i][j]));
					rhs = cplex.sum(rhs, cplex.prod(M, delta));
					rhs = cplex.diff(rhs, 1);
					cplex.addLe(lhs, rhs);
				}
			}
		}

		// Constraint 26
		for(int i = 0; i < order.getItems().size(); i++) {
			for(int j = 0; j < order.getItems().size(); j++) {
				if(i != j) {
					IloNumExpr lhs = cplex.constant(0.0);
					lhs = cplex.sum(lhs,cplex.diff(n[j], n[i]));
					IloNumExpr rhs = cplex.constant(0.0);
					rhs = cplex.sum(rhs,cplex.prod(M, s[i][j]));
					rhs = cplex.sum(rhs, cplex.prod(M, cplex.diff(1.0, delta)));
					rhs = cplex.diff(rhs, 1);
					cplex.addLe(lhs, rhs);
				}
			}
		}
		
		// Constraint 27
		for(int i = 0; i < order.getItems().size(); i++) {
			//cplex.addGe(n[i],1.0);
			cplex.addLe(n[i], B);
		}
		
		cplex.exportModel("lowerbound.lp");
		cplex.solve();
		// Solve
		System.out.print(cplex.isPrimalFeasible());
		double volume = 0;
		for(int i = 0 ; i < order.getItems().size() ; i++)
		{
			volume += order.getItems().get(i).getVolume();
		}
		System.out.print("Volume items: "+ volume + " ");
		System.out.println("Crate volume: " + crate.getVolume());
		if(cplex.isPrimalFeasible()) {
			
//			double volume = 0;
			for(int i = 0 ; i < order.getItems().size(); i++) {
//				volume = volume + order.getItems().get(i).getVolume();
				for(int j = 0; j < order.getItems().size(); j++) {
					if( i != j) {
					System.out.println(i + " " + j + " " + cplex.getValue(s[i][j]) + " ");
					}
				}
			}
			System.out.println("delta: " + cplex.getValue(delta));
			System.out.print("Solution value: " + cplex.getObjValue() + " ");
			
		}

		cplex.close();
	}
}
