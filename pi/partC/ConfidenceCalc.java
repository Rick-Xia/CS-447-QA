import java.util.*;
import java.math.BigDecimal;

public class ConfidenceCalc {

	// Comparator: SortName for compare function name
	// used in sorting
	public static class SortName implements Comparator {
		@Override
		public int compare(Object s1, Object s2) {
				return ((String)s1).compareTo((String)s2);
		}
	}

	HashMap< projectMain.Pair, Integer > pairFrequency;
	HashMap< String, ArrayList<String> > funcApparence;
	HashMap< String,  ArrayList<String> > scopes;
	Integer T_SUPPORT = 3;
	Integer T_CONFIDENCE = 65;
	Integer EXP_LEVEL = 0;
	final String STRING_BUG = "bug: ";
	final String STRING_IN = " in ";
	final String STRING_PAIR = ", pair: (";
	final String STRING_COMMA = ", ";
	final String STRING_SUPPORT = "), support: ";
	final String STRING_CONFIDENCE = ", confidence: ";
	final String STRING_PERCENTAGE = "%";

	/* ctor */
	public ConfidenceCalc() {
		pairFrequency = (HashMap< projectMain.Pair, Integer >)projectMain.pairFrequency;
		funcApparence = (HashMap< String, ArrayList<String> >)projectMain.funcApparence;
		scopes = (HashMap< String, ArrayList<String> >)projectMain.scopes;
		T_SUPPORT = projectMain.T_SUPPORT;
		T_CONFIDENCE = projectMain.T_CONFIDENCE;
		EXP_LEVEL = projectMain.EXP_LEVEL;
	}

	public BigDecimal round(float d) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);       
        return bd;
    }

    /* Helper function: interProcedureExpand
     *	Parameters:
     *		String mainFunc: function name being reported as a bug
     *		String missingFunc: function we want to find during inter-procedural expansion
     *		String scope: the first scope they appear
     *	Return:
     *		true: if we found the missingFunc
     *		false: if we fail to find the missingFunc
    */
    public boolean interProcedureExpand(String mainFunc, String missingFunc, String scope) {

    	// number of levels we want to expand
    	int elevel = EXP_LEVEL;

    	// number of missingFunc we want to find
    	int num = 1;

    	// all functions under the current scope
    	Set<String> functionlist = new HashSet<String>(scopes.get(scope));
    	functionlist.remove(mainFunc); // we dont want to expand mainFunc

    	// expand all functions contained in scope
    	while ( elevel > 0 ) {

    		// using a hashset to remove duplicates
    		Set<String> functionlistTemp = new HashSet<String>();

    		// iterator through all functions in the current scope
    		for ( Iterator i = functionlist.iterator(); i.hasNext(); ) {

    			// get the function we want to expand
    			String middleScope = (String) i.next();

    			// get the list all functions it contains
    			ArrayList<String> middleFuncList = scopes.get(middleScope);

    			// if it contains nothing, skip it
    			if ( middleFuncList.size() == 0 ) continue;

    			// check every function it contains
    			for ( int j = 0; j < middleFuncList.size(); ++j ) {
    				String temp = middleFuncList.get(j);
    				if ( temp.compareTo(missingFunc) == 0 ) {
    					num--; // found a missingFunc! decrement the number of missingFunc we want to find
    					continue;
    				} else if ( temp.compareTo(mainFunc) == 0 ) {
    					num++; // found another mainFunc, then we want to find one more missingFunc
    					continue;
    				}
    				functionlistTemp.add(temp); // push into hashset for the next iteration

    			}// for loop 
    		}// for loop

    		// check if we have found enough missingFunc we want
    		if ( num <= 0 ) return true;

    		functionlist = functionlistTemp;
    		elevel--; // decrement level of expansion
    	}// while

    	return false;
    }

	public void confiCalc() {

		// iterator of pairs: go through every pair that appears
		for (Map.Entry< projectMain.Pair, Integer> entry : pairFrequency.entrySet()) {

			// if the frequency < T_SUPPORT, we dont care about this pair
			if ( ((Integer)entry.getValue()) < T_SUPPORT ) continue;

			// set up flag for checking;
			// if flagA is true, then for A, pair(A,B) is important
			boolean flagA = false;
			boolean flagB = false;

			// get the name of the first function
			String funcA = ((projectMain.Pair)entry.getKey()).getFirst();
			String funcB = ((projectMain.Pair)entry.getKey()).getSecond();
			Integer support = (Integer)entry.getValue(); // the frequency of the pair

			// the whole number of time that function A and B appears
			Integer timeA = funcApparence.get(funcA).size();
			Integer timeB = funcApparence.get(funcB).size();

			// confidence of A and B
			float confidenceA = (float)(support*100) / (float)timeA;
			BigDecimal retA = round(confidenceA);
			float confidenceB = (float)(support*100) / (float)timeB;
			BigDecimal retB = round(confidenceB);

			// if confidence is greater than threshold and is not 100% show up
			// 	set flag to true
			if ( confidenceA >= T_CONFIDENCE && confidenceA < 100 ) flagA = true;
			if ( confidenceB >= T_CONFIDENCE && confidenceB < 100 ) flagB = true;

			// if both flag is false, check next pair
			if ( (!flagA) && (!flagB) ) continue;

			// list of functions where A or B appears
			ArrayList<String> listA = funcApparence.get(funcA);
			ArrayList<String> listB = funcApparence.get(funcB);

			// sort lists lexicographically
			SortName sortname = new SortName();
			Collections.sort(listA, sortname);
			Collections.sort(listB, sortname);

			// set up index and size for loop
			int a = 0;
			int b = 0;
			int sizeA = listA.size();
			int sizeB = listB.size();

			// posA/B are String we are using for comparasion
			String posA = listA.get(a);
			String posB = listB.get(b);

			// while both list are not finished
			while ( a < sizeA || b < sizeB ) {

				String ret = "";

				// if listA is finished
				if ( a >= sizeA ) {
					if ( !flagB ) {
						if ( ++b < sizeB )
							posB = listB.get(b);
						break;
					}// if
					if ( !interProcedureExpand(funcB, funcA, posB) ) {
						ret = STRING_BUG + funcB + STRING_IN + posB +
									STRING_PAIR + funcA + STRING_COMMA + funcB +
									STRING_SUPPORT + support.toString() + STRING_CONFIDENCE +
									retB.toString() + STRING_PERCENTAGE;
						System.out.println(ret);
					}// if
					if ( ++b < sizeB )
						posB = listB.get(b);
					continue;
				}// if

				// if listB is finished
				if ( b >= sizeB ) {
					if ( !flagA ) {
						if ( ++a < sizeA )
							posA = listA.get(a);
						break;
					}// if
					if ( !interProcedureExpand(funcA, funcB, posA) ) {
						ret = STRING_BUG + funcA + STRING_IN + posA +
									STRING_PAIR + funcA + STRING_COMMA + funcB +
									STRING_SUPPORT + support.toString() + STRING_CONFIDENCE +
									retA.toString() + STRING_PERCENTAGE;
						System.out.println(ret);
					}// if
					if ( ++a < sizeA )
						posA = listA.get(a);
					continue;
				}// if

				// if posA equals to posB, which means two functions are the same
				if ( posA.compareTo(posB) == 0 ) {
					if ( ++a < sizeA )
						posA = listA.get(a);
					if ( ++b < sizeB )
						posB = listB.get(b);
					continue;
				}// if

				// if posA is before posB
				if ( posA.compareTo(posB) < 0 ) {
					if ( flagA ) {
						if ( !interProcedureExpand(funcA, funcB, posA) ) {
							ret = STRING_BUG + funcA + STRING_IN + posA +
									STRING_PAIR + funcA + STRING_COMMA + funcB +
									STRING_SUPPORT + support.toString() + STRING_CONFIDENCE +
									retA.toString() + STRING_PERCENTAGE;
							System.out.println(ret);
						}// if
					}// if
					if ( ++a < sizeA )
							posA = listA.get(a);
					continue;
				}// if

				// if posB is before posA
				if ( posA.compareTo(posB) > 0 ) {
					if ( flagB ) {
						if ( !interProcedureExpand(funcB, funcA, posB) ) {
							ret = STRING_BUG + funcB + STRING_IN + posB +
									STRING_PAIR + funcA + STRING_COMMA + funcB +
									STRING_SUPPORT + support.toString() + STRING_CONFIDENCE +
									retB.toString() + STRING_PERCENTAGE;
							System.out.println(ret);
						}// if
					}// if
					if ( ++b < sizeB )
							posB = listB.get(b);
					continue;
				}// if
			}// while
			
		}// for loop
	}// confiCalc()
}// class
