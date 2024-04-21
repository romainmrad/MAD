import static choco.Choco.*;

import choco.cp.model.CPModel;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.solver.Solver;
import choco.cp.solver.CPSolver;

public class Main {

    /**
     * Print checkers board
     * @param grid The grid to print
     * @param s The solver
     */
    public static void printGrid(IntegerVariable[][] grid, Solver s) {
        for (IntegerVariable[] integerVariables : grid) {
            for (IntegerVariable integerVariable : integerVariables) {
                if (s.getVar(integerVariable).getVal() == 0) System.out.print(" _ ");
                else System.out.print(" X ");
            }
            System.out.println();
        }
    }

    /**
     * Sum the first diagonal of a grid '\'
     * @param grid The grid
     * @param x X coordinate of current cell
     * @param y Y coordinate of current cell
     * @return The sum of all cells in current diagonal
     */
    public static IntegerExpressionVariable first_diag_sum(IntegerVariable[][] grid, int x, int y) {
        if (x == grid.length - 1 || y == grid.length - 1) {
            return grid[x][y];
        } else {
            return plus(grid[x][y], first_diag_sum(grid, x + 1, y + 1));
        }
    }

    /**
     * Sum the first diagonal of a grid '/'
     * @param grid The grid
     * @param x X coordinate of current cell
     * @param y Y coordinate of current cell
     * @return The sum of all cells in current diagonal
     */
    public static IntegerExpressionVariable second_diag_sum(IntegerVariable[][] grid, int x, int y) {
        if (x == 0 || y == grid.length - 1) {
            return grid[x][y];
        } else {
            return plus(grid[x][y], second_diag_sum(grid, x - 1, y + 1));
        }
    }

    public static void main(String[] args) {
        int n = 10;
        Model m = new CPModel();

        // Les variables
        IntegerVariable[][] rows = makeIntVarArray("Q", n, n, 0, 1);
        IntegerVariable[][] columns = makeIntVarArray("Q", n, n, 0, 1);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                m.addConstraint(eq(columns[i][j], rows[j][i]));
            }
        }

        // Les contraintes
        for (int i = 0; i < n; i++) {
            m.addConstraint(eq(sum(columns[i]), 1));  // Summing rows
            m.addConstraints(eq(sum(rows[i]), 1));  // Summing columns
            m.addConstraints(leq(first_diag_sum(rows, i, 0), 1));  // Summing first diag '\' on first row
            m.addConstraints(leq(first_diag_sum(rows, 0, i), 1));  // Summing first diag '\' on first col
            m.addConstraints(leq(second_diag_sum(rows, i, 0), 1));  // Summing second diag '\' on first row
            m.addConstraints(leq(second_diag_sum(rows, n - 1, i), 1));  // Summing second diag '\' on last col
        }
        // Création du solver
        Solver s = new CPSolver();
        s.read(m);
        s.solve();
        int i = 1;
        do {
            System.out.println("Solution " + i);
            printGrid(rows, s);
            System.out.println();
            i++;
        } while (s.nextSolution());
    }
}
