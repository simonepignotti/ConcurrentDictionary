import java.util.concurrent.CyclicBarrier;

public class Test {
	
	public static int CAPACITY = 100;
	public static int SIZE = 100;
	
	public static void main(String[] args) {
		
		MyDictionary<Integer,Integer> testDictionary = null;
		CyclicBarrier barrier = new CyclicBarrier(2);
		Thread t0, t1;
		
		for(int k = 0; k < 10; k++) {
		
			for (int i = 0; i < 3; i++) {
				switch (i) {
					case 0:	System.out.println("FINE GRAINED VERSION TEST");
							testDictionary = new FineConcurrentDictionary<Integer,Integer>(CAPACITY);
							break;
					case 1: System.out.println("LAZY VERSION TEST");
							testDictionary = new LazyConcurrentDictionary<Integer,Integer>(CAPACITY);
							break;
					case 2: System.out.println("LOCK FREE VERSION TEST");
							testDictionary = new LockFreeConcurrentDictionary<Integer,Integer>(CAPACITY);
							break;
				}
				
				// PUT
				
				t0 = new Thread(new PutTestThread(0,testDictionary,barrier));
				t1 = new Thread(new PutTestThread(1,testDictionary,barrier));
				
				t0.start();
				t1.start();
				
				try {
					t0.join();
					t1.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				assert(testDictionary.size() == SIZE);
				
				System.out.println("INITIAL: " + testDictionary.toString());
				System.out.println("SIZE: " + testDictionary.size());
				
				// REMOVE
				
				t0 = new Thread(new RemoveTestThread(0,testDictionary,barrier));
				t1 = new Thread(new RemoveTestThread(1,testDictionary,barrier));
				
				t0.start();
				t1.start();
				
				try {
					t0.join();
					t1.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				assert(testDictionary.size() == 0) : testDictionary.size();
				assert(testDictionary.toString().equals("[]")) : testDictionary.toString();
				
				System.out.println("FINAL: " + testDictionary.toString());
				System.out.println("SIZE: " + testDictionary.size());
			}
		
		}

	}
	
}
