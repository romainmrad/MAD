import choco.cp.model.CPModel;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.cp.solver.CPSolver;

import static choco.Choco.*;

public class WorkerProblem {
    static String[] domain = {"   OFF   ", "00h - 08h", "08h - 16h", "16h - 00h"};

    /**
     * Print worker grid
     * @param rows workers array
     * @param solver solver used in the problem
     */
    public static void printGrid(IntegerVariable[][] rows, Solver solver) {
        final String[] shifts = {"00h - 08h", "08h - 16h", "16h - 00h", " day off "};

        System.out.println("=========== Monday ==== Tuesday == Wednesday == Thursday == Friday === Saturday === Sunday ===");
        for (int i = 0; i < 9; i++) {

            System.out.print("Worker " + (i + 1) + " | ");
            for (int j = 0; j < 7; j++)
                System.out.print(domain[solver.getVar(rows[i][j]).getVal()] + " | ");
            System.out.println();
        }
    }

    public static void main(String[] args) {
        // Instantiating model
        Model model = new CPModel();
        // Instantiating variable
        IntegerVariable[][] days = new IntegerVariable[7][9];
        IntegerVariable[][] workers = new IntegerVariable[9][7];
        // Initialising arrays
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 9; j++) {
                days[i][j] = makeIntVar("Days", 0, 3);
                workers[j][i] = days[i][j];
            }
        }
        // Workers cardinality constraint array
        int[] workerMin = {2, 1, 2, 1};
        int[] workerMax = {2, 2, 3, 3};

        // Days cardinality constraint array
        int[] dayMin = {0, 1, 1, 1};
        int[] dayMax = {7, 3, 3, 3};

        // Adding constraints
        for (int i = 0; i < 7; i++) {
            model.addConstraint("cp:bc", globalCardinality(days[i], dayMin, dayMax, 0));
        }

        for (int j = 0; j < 9; j++) {
            model.addConstraint("cp:bc", globalCardinality(workers[j], workerMin, workerMax, 0));
        }
        // Instantiating solver and solving problem
        Solver solver = new CPSolver();
        solver.read(model);
        solver.solve();
        printGrid(workers, solver);
    }
}
