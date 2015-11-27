
public class Test {

	public static void main(String[] args) {
		FineConcurrentDictionary<String,Integer> myDictionary = new FineConcurrentDictionary<String,Integer>();
		myDictionary.put("a", 1);
		myDictionary.put("c", 3);
		myDictionary.put("b", 2);
		myDictionary.put("b", 20);
		System.out.println(myDictionary.toString());
	}
	
}
