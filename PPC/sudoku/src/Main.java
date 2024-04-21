import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;

import static choco.Choco.*;

public class Main {
    public static void main(String[] args) {
        // Model 1
//        model_1();
        // Model 2
        model_2();
    }

    public static void printGrid(IntegerVariable[][] grid, Solver s) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                System.out.print(s.getVar(grid[i][j]).getVal() + " ");
                if ((j + 1) % 3 == 0 & j < 8) System.out.print("| ");
            }
            System.out.println();
            if ((i + 1) % 3 == 0 & i < 8) System.out.println("−−−−−−−−−−−−−−−−−−−−−−");
        }
    }

    public static void model_1() {
        // Instantiating Model
        Model m = new CPModel();
        IntegerVariable[][] grid = makeIntVarArray(
                "grid",
                9,
                9,
                1,
                9
        );
        // Adding lines constraints
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                for (int k = j; k < 9; k++) {
                    if (k != j) {
                        m.addConstraints(neq(grid[i][j], grid[i][k]));
                        m.addConstraints(neq(grid[j][i], grid[k][i]));
                    }
                }
            }
        }
        // Adding sub-squares constraints
        for (int ci = 0; ci < 9; ci += 3) {
            for (int cj = 0; cj < 9; cj += 3) {
                for (int i = ci; i < ci + 3; i++) {
                    for (int j = cj; j < cj + 3; j++) {
                        for (int k = ci; k < ci + 3; k++) {
                            for (int l = cj; l < cj + 3; l++) {
                                if (k != i || l != j) {
                                    m.addConstraints(neq(grid[i][j], grid[k][l]));
                                }
                            }
                        }
                    }
                }
            }
        }
        // Solving problem
        Solver s = new CPSolver();
        s.read(m);
        s.solve();
        // Printing solved sudoku
        printGrid(grid, s);
    }

    public static void model_2() {
        // Instantiating Model
        Model m = new CPModel();
        IntegerVariable[][] lines = makeIntVarArray("lines", 9, 9, 1, 9);
        IntegerVariable[][] columns = makeIntVarArray("columns", 9, 9, 1, 9);
        // Adding constraints
        for (int i = 0; i < 9; i++) {
            m.addConstraints(allDifferent(lines[i]));
            m.addConstraints(allDifferent(columns[i]));
        }
        for (int i = 0; i < 9; i++){
            for (int j = 0; j < 9; j++){
                m.addConstraints(eq(columns[i][j], lines[j][i]));
            }
        }
        // Instantiating grid
        IntegerVariable[][] grid = new IntegerVariable[9][9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    grid[j + k * 3][i] = lines[k * 3][i + j * 3];
                    grid[j + k * 3][i + 3] = lines[1 + k * 3][i + j * 3];
                    grid[j + k * 3][i + 6] = lines[2 + k * 3][i + j * 3];
                }
            }
        }
        // Adding constraints
        for (int i = 0; i < 9; i++) {
            m.addConstraints(allDifferent(grid[i]));
        }
        // Solving problem
        Solver s = new CPSolver();
        s.read(m);
        s.solve();
        // Printing solved sudoku
        printGrid(grid, s);
    }
}