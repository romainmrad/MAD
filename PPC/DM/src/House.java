import choco.kernel.model.variables.integer.IntegerVariable;

import static choco.Choco.*;

public class House {
    public IntegerVariable order;
    public IntegerVariable color;
    public IntegerVariable nationality;
    public IntegerVariable drink;
    public IntegerVariable cigarette;
    public IntegerVariable animal;

    public House() {
        this.order = makeIntVar("Order", 0, 4);
        this.color = makeIntVar("Color", 0, 4);
        this.nationality = makeIntVar("Nationality", 0, 4);
        this.drink = makeIntVar("Drink", 0, 4);
        this.cigarette = makeIntVar("Cigarette", 0, 4);
        this.animal = makeIntVar("Animal", 0, 4);
    }
}
