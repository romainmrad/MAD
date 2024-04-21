import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.solver.Solver;

import java.util.HashMap;

import static choco.Choco.*;

public class Main {
    public static final int red = 0;
    public static final int green = 1;
    public static final int yellow = 2;
    public static final int blue = 3;
    public static final int white = 4;
    public static final int english = 0;
    public static final int swedish = 1;
    public static final int danish = 2;
    public static final int norwegian = 3;
    public static final int german = 4;
    public static final int tea = 0;
    public static final int coffee = 1;
    public static final int milk = 2;
    public static final int beer = 3;
    public static final int water = 4;
    public static final int pall_mall = 0;
    public static final int dunhill = 1;
    public static final int rothman = 2;
    public static final int philip_moris = 3;
    public static final int marlboro = 4;
    public static final int dog = 0;
    public static final int bird = 1;
    public static final int cat = 2;
    public static final int horse = 3;
    public static final int fish = 4;
    public static HashMap<Integer, String> colors_map = new HashMap<>();
    public static HashMap<Integer, String> nationalities_map = new HashMap<>();
    public static HashMap<Integer, String> drinks_map = new HashMap<>();
    public static HashMap<Integer, String> cigarettes_map = new HashMap<>();
    public static HashMap<Integer, String> animals_map = new HashMap<>();

    /**
     * Populate hashmaps
     */
    public static void init_hashmaps() {
        // Adding colors
        colors_map.put(red, "red");
        colors_map.put(green, "green");
        colors_map.put(yellow, "yellow");
        colors_map.put(beer, "blue");
        colors_map.put(white, "white");
        // Adding nationalities
        nationalities_map.put(english, "english");
        nationalities_map.put(swedish, "swedish");
        nationalities_map.put(danish, "danish");
        nationalities_map.put(norwegian, "norwegian");
        nationalities_map.put(german, "german");
        // Adding drinks
        drinks_map.put(tea, "tea");
        drinks_map.put(coffee, "coffee");
        drinks_map.put(milk, "milk");
        drinks_map.put(beer, "beer");
        drinks_map.put(water, "water");
        // Adding cigarettes
        cigarettes_map.put(pall_mall, "Pall Mall");
        cigarettes_map.put(dunhill, "Dunhill");
        cigarettes_map.put(rothman, "Rothman");
        cigarettes_map.put(philip_moris, "Philip Morris");
        cigarettes_map.put(marlboro, "Marlboros");
        // Adding animals
        animals_map.put(dog, "dog");
        animals_map.put(bird, "bird");
        animals_map.put(cat, "cat");
        animals_map.put(horse, "horse");
        animals_map.put(fish, "fish");
    }

    /**
     * Print a House class
     *
     * @param house  The instance of House to print
     * @param solver The solver used in the problem
     * @param number The number of the House in a set of houses
     */
    public static void printHouse(House house, Solver solver, int number) {
        System.out.println("--- House n" + number + " ---");
        System.out.println("Order: " + solver.getVar(house.order).getVal());
        System.out.println("Color: " + colors_map.get(solver.getVar(house.color).getVal()));
        System.out.println("Nationality: " + nationalities_map.get(solver.getVar(house.nationality).getVal()));
        System.out.println("Drink: " + drinks_map.get(solver.getVar(house.drink).getVal()));
        System.out.println("Cigarette: " + cigarettes_map.get(solver.getVar(house.cigarette).getVal()));
        System.out.println("Animal: " + animals_map.get(solver.getVar(house.animal).getVal()));
        System.out.println();
    }

    /**
     * Print a solution of the solved problem
     *
     * @param houses The array of houses
     * @param solver The solver used in the problem
     */
    public static void printSolution(House[] houses, Solver solver) {
        System.out.println("Solution");
        System.out.println("------------------------------------");
        for (int i = 0; i < 5; i++) {
            if (solver.getVar(houses[i].animal).getVal() == fish) {
                printHouse(houses[i], solver, i + 1);
            }
        }
    }

    /**
     * Implements an 'All Different' constraint on all houses in an array
     *
     * @param model  The model to add constraints to
     * @param house  Particular House in array of houses
     * @param houses The array of houses
     * @param index  The index of the passed House in the array of houses
     */
    public static void allDifferentConstraint(Model model, House house, House[] houses, int index) {
        for (int i = 0; i < 5; i++) {
            if (i != index) {
                // Different house orders
                model.addConstraints(neq(house.order, houses[i].order));
                // Different house colors
                model.addConstraints(neq(house.color, houses[i].color));
                // Different nationalities
                model.addConstraints(neq(house.nationality, houses[i].nationality));
                // Different drinks
                model.addConstraints(neq(house.drink, houses[i].drink));
                // Different cigarette brands
                model.addConstraints(neq(house.cigarette, houses[i].cigarette));
                // Different animals
                model.addConstraints(neq(house.animal, houses[i].animal));
            }
        }
    }

    public static void main(String[] args) {
        init_hashmaps();
        // Instantiating model and variables
        Model model = new CPModel();
        House house1 = new House();
        House house2 = new House();
        House house3 = new House();
        House house4 = new House();
        House house5 = new House();
        House[] houses = {house1, house2, house3, house4, house5};

        // Adding constraints
        for (int i = 0; i < 5; i++) {
            // Ordering houses
            model.addConstraints(eq(houses[i].order, i));

            // 1 English lives in red house
            model.addConstraints(implies(eq(houses[i].nationality, english), eq(houses[i].color, red)));

            // 2 Swedish has a dog
            model.addConstraints(implies(eq(houses[i].nationality, swedish), eq(houses[i].animal, dog)));

            // 3 Danish drinks tea
            model.addConstraints(implies(eq(houses[i].nationality, danish), eq(houses[i].drink, tea)));

            // 4 Green house on left of white house
            for (int j = 0; j < 5; j++) {
                if (i != j) {
                    model.addConstraints(implies(eq(houses[i].color, green), implies(eq(houses[j].color, white), eq(houses[i].order, minus(houses[j].order, 1)))));
                }
            }

            // 5 Green house drinks coffee
            model.addConstraints(implies(eq(houses[i].color, green), eq(houses[i].drink, coffee)));

            // 6 Pall Mall house has bird
            model.addConstraints(implies(eq(houses[i].cigarette, pall_mall), eq(houses[i].animal, bird)));

            // 7 Middle house drinks milk
            model.addConstraints(implies(eq(houses[i].order, 2), eq(houses[i].drink, milk)));

            // 8 Yellow house smokes Dunhill
            model.addConstraints(implies(eq(houses[i].color, yellow), eq(houses[i].cigarette, dunhill)));

            // 9 Norwegian lives in first house
            model.addConstraints(implies(eq(houses[i].nationality, norwegian), eq(houses[i].order, 0)));

            // 10 Rothman smoker neighbor has cat
            model.addConstraints(implies(eq(houses[i].cigarette, rothman), neq(houses[i].animal, cat)));
            for (int j = 0; j < 5; j++) {
                if (i != j) {
                    model.addConstraints(
                            implies(
                                    eq(
                                            houses[i].cigarette,
                                            rothman
                                    ),
                                    implies(
                                            eq(
                                                    houses[j].animal,
                                                    cat
                                            ),
                                            eq(
                                                    abs(minus(houses[i].order, houses[j].order)),
                                                    1
                                            )
                                    )
                            )
                    );
                }
            }

            // 11 Horse house neighbor smokes Dunhill
            model.addConstraints(implies(eq(houses[i].animal, horse), neq(houses[i].cigarette, dunhill)));
            for (int j = 0; j < 5; j++) {
                if (i != j) {
                    model.addConstraints(
                            implies(
                                    eq(
                                            houses[i].animal,
                                            horse
                                    ),
                                    implies(
                                            eq(
                                                    houses[j].cigarette,
                                                    dunhill
                                            ),
                                            eq(
                                                    abs(minus(houses[i].order, houses[j].order)),
                                                    1
                                            )
                                    )
                            )
                    );
                }
            }

            // 12 Philip Moris smoker drinks beer
            model.addConstraints(implies(eq(houses[i].cigarette, philip_moris), eq(houses[i].drink, beer)));

            // 13 Norwegian neighbor is in blue house
            model.addConstraints(implies(eq(houses[i].nationality, norwegian), neq(houses[i].color, blue)));
            model.addConstraints(implies(eq(houses[i].order, 1), eq(houses[i].color, blue)));

            // 14 German smokes marlboro
            model.addConstraints(implies(eq(houses[i].nationality, german), eq(houses[i].cigarette, marlboro)));

            // 15 Rothman smoker neighbor drinks water
            model.addConstraints(implies(eq(houses[i].cigarette, rothman), neq(houses[i].drink, water)));
            for (int j = 0; j < 5; j++) {
                if (i != j) {
                    model.addConstraints(
                            implies(
                                    eq(
                                            houses[i].animal,
                                            rothman
                                    ),
                                    implies(
                                            eq(
                                                    houses[j].drink,
                                                    water
                                            ),
                                            eq(
                                                    abs(minus(houses[i].order, houses[j].order)),
                                                    1
                                            )
                                    )
                            )
                    );
                }
            }

            // Adding all different constraint
            allDifferentConstraint(model, houses[i], houses, i);
        }
        Solver solver = new CPSolver();
        solver.read(model);
        solver.solve();

        // ============================================================================
        printSolution(houses, solver);
    }
}