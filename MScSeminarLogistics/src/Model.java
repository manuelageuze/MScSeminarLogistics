import java.util.ArrayList;
import java.util.List;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class Model {

	public Model() {

	}

	public List<ArrayList<Crate>> solveModel(List<Crate> crates) throws IloException {
		@SuppressWarnings("unused")
		int crateSize = crates.size();
		int jSize = 12;


		int[][] b = new int[crates.size()][8];
		for(int i = 0; i < crates.size(); i++) {
			for(int k = 0; k < 8; k++) {
				if(crates.get(i).getAisles()[k] == true) {
					b[i][k] = 1;
				}
			}
		}

		IloCplex cplex = new IloCplex();

		//IloNumVar[] y = new IloNumVar[crates.size()];
		IloNumVar[][] x = new IloNumVar[crates.size()][crates.size()];
		IloNumVar[][] c = new IloNumVar[crates.size()][8];
		IloNumVar[][] z = new IloNumVar[jSize][8];

		for(int i = 0; i < crates.size(); i++) {
			for(int j = 0; j < jSize ; j++) {
				x[i][j] = cplex.boolVar("X" + i + "," + j);
			}
		}

		/*
		for(int j = 0; j < crates.size(); j++) {
			y[j] = cplex.boolVar("Y" + j);
		}
		 */
		for(int j = 0; j < jSize;j++) {
			for(int k = 0; k < 8; k++ ) {
				//c[j][k] = cplex.intVar(0, Integer.MAX_VALUE);
				c[j][k] = cplex.boolVar("C"+ j + "," + k);
				z[j][k] = cplex.boolVar();
			}
		}

		// Objective
		IloNumExpr obj = cplex.constant(0.0);
		for(int j = 0; j < jSize;j++) {
			for(int k = 0; k < 8; k++) {
				obj = cplex.sum(obj,c[j][k]);
			}
		}
		cplex.addMinimize(obj);

		// Constraint 1
		for(int i = 0; i < crates.size(); i++) {
			IloNumExpr expr = cplex.constant(0.0);
			for(int j = 0; j < jSize; j++) {
				expr = cplex.sum(expr, x[i][j]);
			}
			cplex.addEq(expr, 1.0);
		}

		// Constraint 2
		for(int j = 0; j < jSize; j++) {
			IloNumExpr lhs = cplex.constant(0.0);
			for(int i = 0; i < crates.size(); i++) {
				lhs = cplex.sum(lhs, x[i][j]);
			}
			IloNumExpr rhs = cplex.constant(0.0);
			rhs = cplex.sum(rhs, 8.0);
			cplex.addLe(lhs,rhs);
		}

		// Constraint 3
		for(int j = 0; j < jSize; j++) {
			for(int k = 0; k < 8; k++) {
				IloNumExpr lhs = cplex.constant(0.0);
				for(int i = 0; i < crates.size();i++) {
					lhs = cplex.sum(lhs, cplex.prod(b[i][k], x[i][j]));
				}
				lhs = cplex.diff(lhs, 1);
				IloNumExpr rhs = cplex.constant(0.0);
				Double M1 = 10.0;
				rhs = cplex.prod(M1, cplex.diff(1.0, z[j][k]));
				rhs = cplex.diff(rhs, 0.001);
				cplex.addLe(lhs, rhs);
			}
		}

		//Constraint 4
		for(int j = 0; j < jSize; j++) {
			for(int k = 0; k < 8; k++) {
				IloNumExpr lhs = cplex.constant(0.0);
				lhs = cplex.sum(lhs, 1);
				lhs = cplex.sum(lhs, cplex.prod(-1, c[j][k]));
				IloNumExpr rhs = cplex.constant(0.0);
				rhs = cplex.sum(rhs,cplex.prod(5, z[j][k]));
				cplex.addLe(lhs, rhs);
			}
		}

		cplex.exportModel("Model order picking.lp");

		cplex.solve();
		System.out.println(cplex.isPrimalFeasible());

		if(cplex.isPrimalFeasible()) {
			System.out.println("Solution value: " + cplex.getObjValue());			
		}

		List<ArrayList<Crate>> returncrates = new ArrayList<ArrayList<Crate>>();
		for(int j = 0; j < jSize; j++) {

			ArrayList<Crate> cr = new ArrayList<Crate>();
			//System.out.println("Order packer " + j + " packs items: ");
			for(int i = 0; i < crates.size(); i++) {
				if(cplex.getValue(x[i][j]) > 0.001) {
					System.out.print( i + " ");
					cr.add(crates.get(i));
				}
			}
			System.out.println();
			returncrates.add(cr);
			//}
		}
		
		for(int j = 0; j < jSize;j++) {
			for(int k = 0; k < 8; k++) {
				System.out.println(cplex.getValue(c[j][k]));
			}
		}
		cplex.close();
		return returncrates;		
	}

}

