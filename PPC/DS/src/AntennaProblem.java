import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;

import java.util.ArrayList;
import java.util.List;

import static choco.Choco.*;

public class AntennaProblem {

    public static void printChannels(IntegerVariable[] channels, Solver solver) {
        final char[] antennas = {'A', 'B', 'C', 'D', 'E', 'F'};
        for (int i = 0; i < 6; i++) {
            System.out.println("Antenna: " + antennas[i] + " - Freq: " + solver.getVar(channels[i]).getVal());
        }
    }

    public static void printNumChannels(IntegerVariable[] channels, Solver solver) {
        List<Integer> unique_channels = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            int current_channel = solver.getVar(channels[i]).getVal();
            if (!unique_channels.contains(current_channel)) {
                unique_channels.add(current_channel);
            }
        }
        System.out.println("Number of unique channels need: " + unique_channels.size());
    }

    public static void main(String[] args) {
        // Problem constants
        final int numAntennas = 6;
        // Define the distances between antennas
        int[][] distances = {
                {0, 85, 175, 200, 50, 100}, // Distance from antenna A to others
                {85, 0, 125, 175, 100, 160}, // Distance from antenna B to others
                {175, 125, 0, 100, 200, 250}, // Distance from antenna C to others
                {200, 175, 100, 0, 210, 220},    // Distance from antenna D to others
                {50, 100, 200, 210, 0, 100},   // Distance from antenna E to others
                {100, 160, 250, 220, 100, 0}    // Distance from antenna F to others
        };
        // Instantiating model
        Model model = new CPModel();
        // Instantiating variables
        IntegerVariable[] channels = makeIntVarArray("Channels", numAntennas, 1, numAntennas);
        // Adding constraints
        for (int i = 0; i < numAntennas; i++) {
            for (int j = i + 1; j < numAntennas; j++) {
                if (distances[i][j] < 150) {
                    model.addConstraints(neq(channels[i], channels[j]));
                }
            }
        }
        // Instantiating Solver and solving problem
        Solver solver = new CPSolver();
        solver.read(model);
        solver.solve();
        printChannels(channels, solver);
        printNumChannels(channels, solver);
    }
}
