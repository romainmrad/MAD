import static choco.Choco.*;

import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;


public class Alloc {

    public static void main(String[] args) {
        // TODO Auto-generated method stub

        int rangMax = 4;
        Model m = new CPModel();
        String[] groupes = {
                "Groupe1"/*"Ndione Bhija Lo"*/,
                "Groupe2"/*"Tabary Benard-Dupas Dromard"*/,
                "Groupe3"/*"Bartz Junlei Zhongling"*/,
                "Groupe4"/*"Bojda Gesret Pernin"*/,
                "Groupe5"/*"Decreuse Marty Rampelberg Veziat"*/,
                "Groupe6"/*"Cabot Ouallet"*/,
                "Groupe7"/*"Toureau Damasse"*/,
                "Groupe8"/*"Belkaid Huberty"*/,
                "Groupe9"/*"Salhaoui Noachovitch Froumenty"*/,
                "Groupe10"/*"Falcone Germe Reynier"*/,
                "Groupe11"/*"Ren Huang"*/};
        int[][] pref = {
                {2, 1, 8, 9, 5, 3, 4, 12, 13, 7, 6, 10, 11},
                {2, 1, 7, 12, 6, 4, 11, 8, 13, 5, 3, 9, 10},
                {1, 2, 8, 7, 5, 4, 9, 10, 11, 6, 3, 12, 13},
                {2, 3, 9, 5, 1, 6, 4, 12, 11, 7, 8, 10, 13},
                {5, 4, 6, 12, 10, 2, 13, 8, 11, 3, 1, 7, 9},
                {2, 5, 6, 1, 3, 9, 13, 8, 11, 7, 4, 10, 12},
                {7, 1, 3, 9, 13, 5, 12, 2, 8, 11, 6, 4, 10},
                {2, 1, 9, 12, 5, 3, 11, 10, 13, 8, 4, 6, 7},
                {2, 3, 9, 6, 4, 8, 13, 10, 7, 1, 5, 11, 12},
                {1, 2, 13, 12, 6, 3, 10, 11, 9, 5, 4, 8, 7},
                {2, 1, 5, 8, 12, 7, 10, 6, 3, 9, 4, 11, 13}
        };


        IntegerVariable[] affectationSujet = makeIntVarArray("sujet affecté", groupes.length, 0, 12);
        IntegerVariable[] affectationVoeux = makeIntVarArray("voeux affecté", groupes.length, 0, 12);
        //deux groupes ne peuvent pas être affectés à un même sujet
        m.addConstraint(allDifferent(affectationSujet));
        // liens entre affectionSujet et affectationVoeux
        for (int g = 0; g < groupes.length; g++)
            m.addConstraint(nth(affectationSujet[g], pref[g], affectationVoeux[g]));
        //un groupe ne doit pas avoir pire que son 6eme voeux
        for (int g = 0; g < groupes.length; g++)
            m.addConstraint(leq(affectationVoeux[g], 6));
//		// satisfaction sociale: somme des rangs des voeux
        IntegerExpressionVariable satisfactionGlobal = constant(0);
        for (int g = 0; g < groupes.length; g++)
            satisfactionGlobal = plus(satisfactionGlobal, affectationVoeux[g]);
        IntegerVariable c = makeIntVar("satisfaction", 1, 1000, Options.V_OBJECTIVE);
        m.addConstraint(eq(satisfactionGlobal, c));
        //résolution
        Solver s = new CPSolver();
        s.read(m);
        //boolean ok=s.solveAll();
        boolean ok = s.minimize(false);
        //System.out.println("Nombre de solutions:"+s.getNbSolutions());
        //System.out.println("-----------------------------------------");
        //boolean ok = s.solve();
        int n = 0;
        if (ok) {
            //affichage
            do {
                System.out.println("Solution " + n);
                for (int i = 0; i < groupes.length; i++) {
                    System.out.println(groupes[i] + ": sujet " + ((s.getVar(affectationSujet[i]).getVal() + 1)) + ", voeux obtenu:" + (s.getVar(affectationVoeux[i]).getVal() + 1));
                }

                System.out.println("Score solution:" + s.getVar(c).getVal());
                System.out.println("-----------------------------------------");
                n++;
            }
            while (s.nextSolution());
        } else {
            System.out.println("Pas de solution");
        }
    }
}