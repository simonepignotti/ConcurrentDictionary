import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

/*
public class TestThread implements Callable<Object> {
	
	private MyDictionary<Integer,Integer> testDictionary;
	private LinkedList<Integer> keys;
	private LinkedList<Integer> values;
	public static int MIN = 10;
	public static int MAX = 20;
	
	public TestThread (MyDictionary<Integer,Integer> testDictionary) {
		this.testDictionary = testDictionary;
		keys = new LinkedList<Integer>();
		values = new LinkedList<Integer>();
	}

	@Override
	public Object call() {
		int putNumber = ThreadLocalRandom.current().nextInt(MIN, MAX);
		int removeNumber = ThreadLocalRandom.current().nextInt(MIN, putNumber);
		int getNumber = ThreadLocalRandom.current().nextInt(MIN, putNumber);
		int replaceNumber = ThreadLocalRandom.current().nextInt(MIN, putNumber);
		Integer key;
		Integer value;
		boolean success;
		for (int i = 0; i < putNumber; i++) {
			key = ThreadLocalRandom.current().nextInt(MIN, MAX);
			value = ThreadLocalRandom.current().nextInt(MIN, MAX);
			keys.push(key);
			values.push(value);
			try {
				testDictionary.put(key, value);
			} catch (NullKeyException | NullValueException | FullDictionaryException e1) {
				System.out.println("ERROR");
				e1.printStackTrace();
			}catch (FullDictionaryException e) {
				assert(testDictionary.size() == Test.CAPACITY);
			}
		}
		
		System.out.println(testDictionary.toString());
		
		for (int i = 0; i < removeNumber; i++) {
			key = keys.pop();
			value = values.pop();
			success = testDictionary.remove(key, value);
			assert(success);
		}
		
		return null;
	}

}
*/

public class TestThread extends Thread {
	
	private MyDictionary<Integer,Integer> testDictionary;
	private int i;
	
	public TestThread (MyDictionary<Integer,Integer> testDictionary, int i) {
		this.testDictionary = testDictionary;
		this.i = i;
	}
	
	public void start() {
		try {
			testDictionary.put(i,i);
		} catch (NullKeyException | NullValueException | FullDictionaryException e) {
			e.printStackTrace();
		}
	}
	
}
