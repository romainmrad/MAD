import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;

import static choco.Choco.*;

public class MagicHexProblem {

    public static void printHexagon(IntegerVariable[][] hexagon, Solver solver) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (hexagon[i].length == 3) {
                stringBuilder.append("  ");
            } else if (hexagon[i].length == 4) {
                stringBuilder.append(" ");
            }
            for (IntegerVariable cell : hexagon[i]) {
                stringBuilder.append(String.format("%02d", solver.getVar(cell).getVal())).append(" ");
            }
            stringBuilder.append('\n');
        }
        System.out.println(stringBuilder);
    }

    public static void main(String[] args) {
        // Instantiating model
        Model model = new CPModel();
        // Instantiating variables
        IntegerVariable[] row1 = makeIntVarArray("Row 1", 3, 1, 20);
        IntegerVariable[] row2 = makeIntVarArray("Row 2", 4, 1, 20);
        IntegerVariable[] row3 = makeIntVarArray("Row 3", 5, 1, 20);
        IntegerVariable[] row4 = makeIntVarArray("Row 4", 4, 1, 20);
        IntegerVariable[] row5 = makeIntVarArray("Row 5", 3, 1, 20);
        IntegerVariable[][] hexagon = {row1, row2, row3, row4, row5};
        // Adding constraint
        // All different constraint
        for (IntegerVariable[] row : hexagon) {
            for (IntegerVariable cell : row) {
                for (IntegerVariable[] other_row : hexagon) {
                    for (IntegerVariable other_cell : other_row) {
                        if (cell != other_cell) {
                            model.addConstraints(neq(cell, other_cell));
                        }
                    }
                }
            }
        }
        // Row sum constraint
        for (IntegerVariable[] row : hexagon) {
            model.addConstraints(eq(sum(row), 38));
        }
        // Diagonal sums \
        model.addConstraints(eq(plus(hexagon[0][2], plus(hexagon[1][3], hexagon[2][4])), 38));
        model.addConstraints(eq(plus(hexagon[0][1], plus(hexagon[1][2], plus(hexagon[2][3], hexagon[3][3]))), 38));
        model.addConstraints(eq(plus(hexagon[0][0], plus(hexagon[1][1], plus(hexagon[2][2], plus(hexagon[3][2], hexagon[4][2])))), 38));
        model.addConstraints(eq(plus(hexagon[1][0], plus(hexagon[2][1], plus(hexagon[3][1], hexagon[4][1]))), 38));
        model.addConstraints(eq(plus(hexagon[2][0], plus(hexagon[3][0], hexagon[4][0])), 38));
        // Diagonal sums /
        model.addConstraints(eq(plus(hexagon[2][0], plus(hexagon[1][0], hexagon[0][0])), 38));
        model.addConstraints(eq(plus(hexagon[3][0], plus(hexagon[2][1], plus(hexagon[1][1], hexagon[0][1]))), 38));
        model.addConstraints(eq(plus(hexagon[4][0], plus(hexagon[3][1], plus(hexagon[2][2], plus(hexagon[1][2], hexagon[0][2])))), 38));
        model.addConstraints(eq(plus(hexagon[4][1], plus(hexagon[3][2], plus(hexagon[2][3], hexagon[1][3]))), 38));
        model.addConstraints(eq(plus(hexagon[4][2], plus(hexagon[3][3], hexagon[2][4])), 38));
        // Instantiating solver and solving problem
        Solver solver = new CPSolver();
        solver.read(model);
        solver.solve();
        printHexagon(hexagon, solver);
    }
}