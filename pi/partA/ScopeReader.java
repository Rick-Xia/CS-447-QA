import java.util.*;

public class ScopeReader {
	
	HashMap< String, ArrayList<String> > scopes;
	HashMap< String, ArrayList<String> > funcApparence;
	HashMap< projectMain.Pair, Integer > pairFrequency;
	projectMain.Pair funcPair;
	
	public ScopeReader() {
		funcApparence = (HashMap< String, ArrayList<String> >)projectMain.funcApparence;
		pairFrequency = (HashMap< projectMain.Pair, Integer >)projectMain.pairFrequency;
		scopes = (HashMap< String, ArrayList<String> >)projectMain.scopes;
	}

	public void read () {
		Set scopesKeys = scopes.keySet();
   		for (Iterator i = scopesKeys.iterator(); i.hasNext(); ) {
			// get unique function name like scope1(), scope2()...
			String scopeName = (String) i.next();
			// store A(), B(), C()... in hash table, automatically remove duplicate value
			HashSet<String> funcInScopeH = new HashSet<String>();
			if (0 == scopes.get(scopeName).size()) continue;
			for (int j = 0; j < scopes.get(scopeName).size(); j++) {
				String funcName = (String)scopes.get(scopeName).get(j);
				funcInScopeH.add(funcName);
			}

			// transfer hashtable to array, then can use index to check
			List<String> funcInScopeA = new ArrayList<String>(funcInScopeH);
			int size = funcInScopeA.size();
			// reserve idividual function
			for (int j = 0; j < size; j++) {
				ArrayList<String> funcCalledBy = new ArrayList<String>(); 
				// check if A(), B(), C()... already exist in funcApparence
				if (funcApparence.containsKey(funcInScopeA.get(j))) { 
					funcCalledBy = funcApparence.get(funcInScopeA.get(j));
				}
				funcCalledBy.add(scopeName);
				funcApparence.put(funcInScopeA.get(j), funcCalledBy);
			}

			// reserve function pair 
			for (int j = 0; j < size; j++) {
				for (int k = j + 1; k < size; k++) {
					// generate function pair
					String funcNameOne = funcInScopeA.get(j);
					String funcNameTwo = funcInScopeA.get(k);
					if (funcNameOne.compareTo(funcNameTwo) < 0) {
						funcPair = new projectMain.Pair(funcNameOne, funcNameTwo);
					} else {
						funcPair = new projectMain.Pair(funcNameTwo, funcNameOne);
					}
					// add pair or update pair counter
					Integer count = 1;
					if (pairFrequency.containsKey(funcPair)){
						count = pairFrequency.get(funcPair);
						pairFrequency.put(funcPair, count + 1);
					} else {
						pairFrequency.put(funcPair, count);
					}
				}
			}
		}
	}	
}
