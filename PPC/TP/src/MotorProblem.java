import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;

import static choco.Choco.*;

public class MotorProblem {
    /**
     * Print a group of motors
     * @param group_power_output Array of power outputs
     * @param state Array of group state
     * @param s Solver used in the problem
     */
    public static void printGroup(IntegerVariable[] group_power_output, IntegerVariable[] state, Solver s) {
        System.out.print((group_power_output[0].getName() + ": ").replace("_0", ""));
        for (IntegerVariable integerVariable : group_power_output) {
            System.out.print(String.format("%02d", s.getVar(integerVariable).getVal()) + "  ");
        }
        System.out.println();
        System.out.print("State  : ");
        for (IntegerVariable integerVariable : state) {
            if (s.getVar(integerVariable).getVal() == 1) System.out.print("On  ");
            else System.out.print("Off ");
        }
        System.out.println();
        System.out.println();
    }

    /**
     * Print all groups in an array of groups
     * @param groups Array of groups
     * @param states Array of group states
     * @param s Solver used in the problem
     */
    public static void printGroups(IntegerVariable[][] groups, IntegerVariable[][] states, Solver s) {
        int i = 0;
        do {
            System.out.println("Solution " + i);
            System.out.println("------------------------------------");
            printGroup(groups[0], states[0], s);
            printGroup(groups[1], states[1], s);
            printGroup(groups[2], states[2], s);
            System.out.println();
            i++;
        } while (s.nextSolution());
    }

    public static void main(String[] args) {
        int[] required_power_output = {0, 120, 90, 50, 30, 70, 80};
        // Instantiating model
        Model model = new CPModel();
        // Instantiating group power outputs arrays
        IntegerVariable[] group1_power_output = makeIntVarArray("Group 1", 7, 0, 60);
        IntegerVariable[] group2_power_output = makeIntVarArray("Group 2", 7, 0, 60);
        IntegerVariable[] group3_power_output = makeIntVarArray("Group 3", 7, 0, 40);
        IntegerVariable[][] groups_power_output = {group1_power_output, group2_power_output, group3_power_output};
        // Instantiating group state arrays
        IntegerVariable[] group1_state = makeIntVarArray("Group 1 State", 7, 0, 1);
        IntegerVariable[] group2_state = makeIntVarArray("Group 2 State", 7, 0, 1);
        IntegerVariable[] group3_state = makeIntVarArray("Group 3 State", 7, 0, 1);
        IntegerVariable[][] groups_states = {group1_state, group2_state, group3_state};
        // Instantiating cost variable
        IntegerExpressionVariable cost = makeIntVar("Cost", Options.V_OBJECTIVE);
        model.addVariables(Options.V_OBJECTIVE, cost);
        for (int i = 0; i < 7; i++) {
            // Adding cost
            cost = plus(cost, plus(plus(mult(group1_state[i], 10), mult(group1_power_output[i], 2)), plus(plus(mult(group2_state[i], 10), mult(group2_power_output[i], 2)), plus(mult(group3_state[i], 10), mult(group3_power_output[i], 2)))));
            // Sum of outputs has to be equal to the required output
            model.addConstraints(eq(plus(group1_power_output[i], plus(group2_power_output[i], group3_power_output[i])), required_power_output[i]));
            // If output != 0, then state is 'ON'
            model.addConstraints(implies(neq(group1_power_output[i], 0), eq(group1_state[i], 1)));
            model.addConstraints(implies(neq(group2_power_output[i], 0), eq(group2_state[i], 1)));
            model.addConstraints(implies(neq(group3_power_output[i], 0), eq(group3_state[i], 1)));
            // If state is 'OFF', then output = 0
            model.addConstraints(implies(eq(group1_state[i], 0), eq(group1_power_output[i], 0)));
            model.addConstraints(implies(eq(group2_state[i], 0), eq(group2_power_output[i], 0)));
            model.addConstraints(implies(eq(group3_state[i], 0), eq(group3_power_output[i], 0)));
            // If motor is on at i, it has to be preheated at i-1 (on at i-1)
            if (i != 0) {
                model.addConstraints(implies(neq(group1_power_output[i], 0), eq(group1_state[i - 1], 1)));
                model.addConstraints(implies(neq(group2_power_output[i], 0), eq(group2_state[i - 1], 1)));
                model.addConstraints(implies(neq(group3_power_output[i], 0), eq(group3_state[i - 1], 1)));
            }
            // If a motor is on at i and has to be on at i+2, then it should stay on during i+1
            if (i < 5) {
                model.addConstraints(implies(and(eq(group1_state[i], 1), eq(group1_state[i + 2], 1)), eq(group1_state[i + 1], 1)));
                model.addConstraints(implies(and(eq(group2_state[i], 1), eq(group2_state[i + 2], 1)), eq(group2_state[i + 1], 1)));
                model.addConstraints(implies(and(eq(group3_state[i], 1), eq(group3_state[i + 2], 1)), eq(group3_state[i + 1], 1)));
            }
            // If two motors are ON, then they should both produce electricity to even-out workload
            model.addConstraints(implies(eq(group1_state[i], 1), implies(eq(group2_state[i], 1), implies(neq(group1_power_output[i], 0), neq(group2_power_output[i], 0)))));
            model.addConstraints(implies(eq(group1_state[i], 1), implies(eq(group3_state[i], 1), implies(neq(group1_power_output[i], 0), neq(group3_power_output[i], 0)))));
            model.addConstraints(implies(eq(group2_state[i], 1), implies(eq(group3_state[i], 1), implies(neq(group2_power_output[i], 0), neq(group3_power_output[i], 0)))));
        }

        // Solving problem
        Solver solver = new CPSolver();
        solver.read(model);
        solver.minimize(true);
        printGroups(groups_power_output, groups_states, solver);
    }
}