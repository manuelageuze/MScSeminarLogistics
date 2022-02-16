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
//		System.out.println(cplex.isPrimalFeasible());
		System.out.println("Order: "+order.getOrderId());
		if(cplex.isPrimalFeasible()) {
			double sol = cplex.getObjValue();
//			System.out.println("Solution value: " + sol);
			cplex.close();
			return sol;
			
		}
		cplex.close();
		return Double.NEGATIVE_INFINITY;
	}	
}
