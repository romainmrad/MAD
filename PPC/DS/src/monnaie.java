import static choco.Choco.*;

import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.solver.Solver;

public class monnaie {
    public static void main (String[] args){
        int pieces[] = {1,2,5,10,20,50,100,200};
        int p=195;
        int q=200;
        int r = q-p;
        Model m = new CPModel();
        //nombre de pièces de chaque type rendues. 100 max arbitrairement
        IntegerVariable[] rendu = makeIntVarArray("rendu",8,0,100);
        
        //montant du rendu
        IntegerExpressionVariable valrendu = constant(0);
        for (int j=0;j<8;j++) {
            valrendu=plus(mult(rendu[j], pieces[j]), valrendu);
        }
        //assurer que la somme rendue est la bonne
        m.addConstraint(eq(valrendu, r));

        //ajouter une fonction de coût à minimiser (nombre de pièces rendus)
        IntegerVariable c = makeIntVar("cost",0,800,Options.V_OBJECTIVE);
        m.addConstraint(eq(sum(rendu),c));
        Solver s = new CPSolver();
		
		s.read(m);
		//s.solve();
        s.minimize(s.getVar(c),true);
		int i = 1; 
		do{
		System.out.println("Solution "+i);
		affichePlanif(rendu,s);
        System.out.println();
		i++;
		}while(s.nextSolution());

    }

    public static void affichePlanif(IntegerVariable[] e,Solver s){
		for (int j=0;j<8;j++) {
		    	System.out.print(s.getVar(e[j]).getVal()+" ");
		}
	}
}     
