import java.util.Map;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class LowerBoundModel {
	@SuppressWarnings("unused")
	private final Order order;
	@SuppressWarnings("unused")
	private final Map<Double,Item> items;
//
	public LowerBoundModel(Order order, Map<Double,Item> items) throws IloException {
		this.order = order;
		this.items = items;
	}

	public static double setCoveringLB(Order order, Map<Double, Item> items) throws IloException {
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
		
		for(int i = 0; i < order.getItems().size(); i++) {
			IloNumExpr lhs = cplex.constant(0.0);
			for(int j = 0; j < order.getItems().size() + 1; j++) {
				lhs = cplex.sum(lhs,x[i][j]);
			}
			cplex.addGe(lhs, 1.0);
		}
		cplex.solve();
		System.out.println(cplex.isPrimalFeasible());
		
		if(cplex.isPrimalFeasible()) {
			System.out.println("Solution value: " + cplex.getObjValue());
			cplex.close();
			return cplex.getObjValue();
			
		}
		cplex.close();
		return Double.NEGATIVE_INFINITY;
		
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
