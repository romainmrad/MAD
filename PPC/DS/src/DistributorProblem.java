import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;

import static choco.Choco.*;


public class DistributorProblem {

    public static IntegerExpressionVariable sum_of_coins(IntegerVariable[] coin_numbers, int[] coins) {
        IntegerExpressionVariable res = constant(0);
        for (int i = 0; i < 8; i++){
            res = plus(res, mult(coins[i], coin_numbers[i]));
        }
        return res;
    }

    public static void printCoins(IntegerVariable[] coins, Solver solver) {
        System.out.println("Returned coins: ");
        System.out.println("1ct : " + solver.getVar(coins[0]).getVal());
        System.out.println("2ct : " + solver.getVar(coins[1]).getVal());
        System.out.println("5ct : " + solver.getVar(coins[2]).getVal());
        System.out.println("10ct: " + solver.getVar(coins[3]).getVal());
        System.out.println("20ct: " + solver.getVar(coins[4]).getVal());
        System.out.println("50ct: " + solver.getVar(coins[5]).getVal());
        System.out.println("1eur: " + solver.getVar(coins[6]).getVal());
        System.out.println("2eur: " + solver.getVar(coins[7]).getVal());
    }

    public static void main(String[] args) {
        // Problem constants
        int[] coins = {1, 2, 5, 10, 20, 50, 100, 200};
        int P = 195;
        int Q = 400;
        // Instantiating model
        Model model = new CPModel();
        // Instantiating variables
        IntegerVariable[] returned_coins_number = makeIntVarArray("Returned coins", 8, 0, 10000);
        // Returned amount equal to difference Q - P
        model.addConstraints(eq(sum_of_coins(returned_coins_number, coins), Q - P));

        // Instantiating variables
        IntegerVariable num_returned_coins = makeIntVar("Number of returned coins", 0, 8000, Options.V_OBJECTIVE);
        // Adding objective variable
        model.addVariables(Options.V_OBJECTIVE, num_returned_coins);
        // Number of returned coins equal to sum of returned coins array
        model.addConstraints(eq(sum(returned_coins_number), num_returned_coins));

        // Instantiating solver
        Solver solver = new CPSolver();
        solver.read(model);
        solver.minimize(true);
        printCoins(returned_coins_number, solver);
    }
}
