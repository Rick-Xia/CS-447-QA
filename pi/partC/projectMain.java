import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class projectMain {

    public static class Pair {
        private final String first;
        private final String second;

        public Pair(String first, String second) {
            super();
            this.first = first;
            this.second = second;
        }

        public int hashCode() {
            int hashFirst = first != null ? first.hashCode() : 0;
            int hashSecond = second != null ? second.hashCode() : 0;

            return (hashFirst + hashSecond) * hashSecond + hashFirst;
        }

        public boolean equals(Object other) {
            if (other instanceof Pair) {
                Pair otherPair = (Pair) other;
                return
                    ((  this.first == otherPair.first ||
                        ( this.first != null && otherPair.first != null &&
                        this.first.equals(otherPair.first))) &&
                    (  this.second == otherPair.second ||
                        ( this.second != null && otherPair.second != null &&
                        this.second.equals(otherPair.second))) );
            }

            return false;
        }

        public String getFirst() {
            return first;
        }

        public String getSecond() {
            return second;
        }
    }

    /** global variables **/
    /* threshold support */
    public static Integer T_SUPPORT = 3;

    /* threshold confidence */
    public static Integer T_CONFIDENCE = 65;

    /* expand level */
    public static Integer EXP_LEVEL = 0;

    /* targer file */
    public static String FILE_NAME;

	/* Type:HashMap< pair<String, String>, int >
	 * 	Key:pair<String, String>, name of functions, alphabetical order
	 *  Value:int, times of pair
	*/
    public static HashMap< Pair, Integer > pairFrequency = new HashMap< Pair, Integer >();

    /* Type:map< String, arraylist<String> >
     * 	Key:String, name of function
     *  Value:arraylist<String>, listof scopes which contain the function
    */
    public static HashMap< String, ArrayList<String> > funcApparence = new HashMap< String, ArrayList<String> >();

    /* Type:arrayList<arrayList>
     *	each element is an arraylist of Strings:
     *		[0] is the name of scope; [1...] are names of functions in the scope
    */
    public static HashMap< String,  ArrayList<String> > scopes = new HashMap< String,  ArrayList<String> > ();


    /*
    Parse the call graph into an arraylist of arrarlist of strings
    Each element of scopes is a scope.
    Each arraylist of strings will begin with the scope name, then followed by the functions called within that scope
    */
    static void scopeParser(String file){
        String scope=null;
        boolean skip=true;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line
                //System.out.format("line: %s\n", line);
                String[] splitStr = line.trim().split("\\s+|<+|\'");// split the line by space, "<", and "'"
                switch (splitStr[0]) { // check what the line begins with
                    case "Call":
                        if(splitStr[3].equals("for")){
                            ArrayList<String> function= new ArrayList<String> ();
														scope=splitStr[6];
                            scopes.put(scope,function);
                            skip=false;
                        }
                        else if(splitStr[4].equals("null")){
                            // skip the null function scope and its function calls
                            skip=true;  // make the skip flag true to indicate following function calls should be skipped
                            continue;
                        }
                        else{
                            // the program should not reach here
                            System.out.format("Error state 1 in scopeParser\n");
                        }
                        break;
                    case "CS":
                        if(skip)continue;
                        if(splitStr[3].equals("external"))continue;   // external nodes are ignored
                        scopes.get(scope).add(splitStr[5]);
                        break;
                    default:
                        break;
                }
            }// while
        }catch(Exception e){
            return;
        }
    }


    /*
    prints out content of scopes
    */
    static void print_scopes(){

			for(Map.Entry< String, ArrayList<String> > entry : scopes.entrySet()){   //print keys and values
        System.out.format("%s\n",entry.getKey());
        for(int j=0; j< entry.getValue().size(); j++){
						System.out.format("%s\n",entry.getValue().get(j));
				}

				System.out.format("======================\n"); // divider
    }
}


    /*  Commandline Format:
     *      java projectMain arg1: arg1 is the input file name.
     *      java projectMain arg1 arg2 arg3:
     *          arg1 same as above
     *          arg2 is the customed support threshold;
     *          arg3 is the customed confidence threshold.
     *      java projectMain arg1 arg2 arg3 arg4:
     *          arg1,2,3 same as above;
     *          arg4 is the customed expand level.
    */
    public static void main(String[] args) {

        /** basic setting setup **/
        switch( args.length ) {
            case 1:
                FILE_NAME = args[0];
                break;
            case 3:
                FILE_NAME = args[0];
                T_SUPPORT = Integer.parseInt(args[1]);
                T_CONFIDENCE = Integer.parseInt(args[2]);
                break;
            case 4:
                FILE_NAME = args[0];
                T_SUPPORT = Integer.parseInt(args[1]);
                T_CONFIDENCE = Integer.parseInt(args[2]);
                EXP_LEVEL = Integer.parseInt(args[3]);
                break;
            default:
                break;
        }// switch

		/*		input: file
		 *		output: void; store values in global list
		 *				list of list, each element is an arraylist of Strings:
		 *				[0] is the name of scope; [1...] are names of functions in the scope
		 * part1 using "Call" to detect scopes
		 * 	 parse the strings
		*/
        scopeParser(FILE_NAME);
        // print_scopes();

		/*		input: arraylist of list Strings
		 *		output: void; store values in global maps
		 * part2 for each scope:
		 * 	 1. filter out duplicated functions
		 * 	 2. increment the frequency of functions and pairs
		*/
        ScopeReader sr = new ScopeReader();
        sr.read();

		/*		intput: global maps
		 *		output:
		 * part3 for each pair:
		 *	count the confidence:
		 *		ifnot "bug"
		 *			continue;
		 *		if "bug"
		 *			go through all functions they appear, report bugs
		*/
        ConfidenceCalc cc = new ConfidenceCalc();
        cc.confiCalc();

		/**/

    }
}
