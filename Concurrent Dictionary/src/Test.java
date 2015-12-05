import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {
	
	public static int THREADS_NUMBER = 5;
	public static int CAPACITY = 100;

	public static void main(String[] args) {
		
		MyDictionary<Integer,Integer> testDictionary = new LockFreeConcurrentDictionary<Integer,Integer>(CAPACITY);
		
		for (int i = 0; i < 10; i++) {
			try {
				testDictionary.put(i,i);
			} catch (NullKeyException | NullValueException | FullDictionaryException e) {
				e.printStackTrace();
			}
		}
		
		TestThread t0;
		TestThread t1;
		
		for(int i = 0; i < 10; i++) {
			t0 = new TestThread(testDictionary,19-i);
			t1 = new TestThread(testDictionary,19-i);
			t0.start();
			t1.start();
			try {
				t0.join();
				t1.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(testDictionary.toString());
		}
		
		/*
		
		List<TestThread> tasks = new ArrayList<TestThread>();
		for (int i = 0; i < THREADS_NUMBER; i++) {
			tasks.add(new TestThread(testDictionary));
		}
		
		ExecutorService executor = Executors.newFixedThreadPool(THREADS_NUMBER);
		
		try {
			executor.invokeAll(tasks);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

		testDictionary = new LazyConcurrentDictionary<String,String>(CAPACITY);
		
		try {
			executor.invokeAll(tasks);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		testDictionary = new LockFreeConcurrentDictionary<String,String>(CAPACITY);
				
		try {
			executor.invokeAll(tasks);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		*/
		
		System.out.println("FINAL: " + testDictionary.toString());
		System.out.println("SIZE: " + testDictionary.size());
				
	}
	
}
