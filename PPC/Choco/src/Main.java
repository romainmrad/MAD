import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;

import static choco.Choco.*;

public class Main {
    public static void main(String[] args) {
        // Instantiating model
        Model myCSP = new CPModel();
        // Creating colors list and domains
        String[] colors = {"blue", "yellow", "red", "green"};
        int[] domX1 = {0, 3, 2, 1};
        int[] domX2 = {0, 2, 1};
        int[] domX3 = {1, 3, 2};
        int[] domX4 = {2, 3, 0, 1};
        // Instantiating variables
        IntegerVariable x1 = makeIntVar("x1", domX1);
        IntegerVariable x2 = makeIntVar("x2", domX2);
        IntegerVariable x3 = makeIntVar("x3", domX3);
        IntegerVariable x4 = makeIntVar("x4", domX4);
        IntegerVariable[] variables = {x1, x2, x3, x4};
        // Adding variables to model
        for (IntegerVariable var : variables) {
            myCSP.addVariables(var);
        }
        // Adding constraints to model
        myCSP.addConstraints(neq(x1, x4));
        myCSP.addConstraints(neq(x1, x3));
        myCSP.addConstraints(eq(x2, x4));
        myCSP.addConstraints(eq(x4, x3));
        // Instantiating solver
        Solver s = new CPSolver();
        // Reading problem and solving
        s.read(myCSP);
        s.solve();
        // Printing solutions
        int i = 0;
        do {
            System.out.println("Solution n" + i);
            System.out.println("x1 = " + colors[s.getVar(x1).getVal()]);
            System.out.println("x2 = " + colors[s.getVar(x2).getVal()]);
            System.out.println("x3 = " + colors[s.getVar(x3).getVal()]);
            System.out.println("x4 = " + colors[s.getVar(x4).getVal()]);
            System.out.println();
            i++;
        } while (s.nextSolution());
    }
}