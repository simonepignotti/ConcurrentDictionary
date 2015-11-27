
public class FineTest {

	public static void main(String[] args) {
		MyDictionary<String,Integer> myDictionary = new FineConcurrentDictionary<String,Integer>();
		System.out.println("PUT");
		myDictionary.put("a", 1);
		myDictionary.put("c", 3);
		myDictionary.put("b", 2);
		myDictionary.put("b", 20);
		System.out.println(myDictionary.toString());
		System.out.println("SIZE: " + myDictionary.size());
		System.out.println("GET");
		System.out.println(myDictionary.get("b"));
		System.out.println(myDictionary.get("c"));
		System.out.println(myDictionary.get("a"));
		System.out.println(myDictionary.get("d"));
		System.out.println("SIZE: " + myDictionary.size());
		System.out.println("REMOVE");
		myDictionary.remove("b", 2);
		System.out.println(myDictionary.remove("c", 30));
		System.out.println(myDictionary.toString());
		System.out.println("SIZE: " + myDictionary.size());
		System.out.println("REPLACE");
		myDictionary.put("d", 4);
		myDictionary.put("e", 5);
		myDictionary.put("b", 2);
		myDictionary.put("f", 20);
		System.out.println(myDictionary.replace("f", 6));
		System.out.println(myDictionary.replace("f", 20, 6));
		System.out.println(myDictionary.replace("g", 7));
		System.out.println(myDictionary.replace("a", 1, 100));
		System.out.println(myDictionary.toString());
	}
	
}
