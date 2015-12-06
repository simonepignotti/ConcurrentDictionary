import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class Test {
	
	public static int THREAD_NUM = 100;
	public static int CAPACITY = 10000;
	public static int SIZE = 10000;
	
	public static void main(String[] args) {
		
		MyDictionary<Integer,Integer> testDictionary = null;
		CyclicBarrier barrier = new CyclicBarrier(THREAD_NUM);
		List<Thread> threads;
		long completionTime;
		
		for (int i = 0; i < 3; i++) {
			
			switch (i) {
				case 0:	System.out.println("FINE GRAINED VERSION TEST\n");
						testDictionary = new FineConcurrentDictionary<Integer,Integer>(CAPACITY);
						break;
				case 1: System.out.println("\nLAZY VERSION TEST\n");
						testDictionary = new LazyConcurrentDictionary<Integer,Integer>(CAPACITY);
						break;
				case 2: System.out.println("\nLOCK FREE VERSION TEST\n");
						testDictionary = new LockFreeConcurrentDictionary<Integer,Integer>(CAPACITY);
						break;
			}
			
			// FUNCTIONALITY PUT
			
			System.out.println("Functionality Put (successfull if no exception is raised)");
			
			threads = new LinkedList<Thread>();
			
			for (int j = 0; j < THREAD_NUM; j++)
				threads.add(new PutTestThread(j, true, testDictionary, barrier));
			
			for (Thread t : threads)
				t.start();
			
			for (Thread t : threads) {
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			assert(testDictionary.size() == SIZE) : testDictionary.size();
			
			// FUNCTIONALITY REMOVE
			
			System.out.println("Functionality Remove (successfull if no exception is raised)");
			
			threads = new LinkedList<Thread>();
			
			for (int j = 0; j < THREAD_NUM; j++)
				threads.add(new RemoveTestThread(j, true, testDictionary, barrier));
			
			for (Thread t : threads)
				t.start();
			
			for (Thread t : threads) {
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			assert(testDictionary.size() == 0) : testDictionary.size();
			
			// PUT
			
			threads = new LinkedList<Thread>();

			completionTime = System.currentTimeMillis();
			
			for (int j = 0; j < THREAD_NUM; j++)
				threads.add(new PutTestThread(j, false, testDictionary, barrier));
			
			for (Thread t : threads)
				t.start();
			
			for (Thread t : threads) {
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			completionTime = System.currentTimeMillis() - completionTime;
			System.out.println("Put completion time: " + completionTime);
			
			// REPLACE & GET
			
			threads = new LinkedList<Thread>();

			completionTime = System.currentTimeMillis();
			
			for (int j = 0; j < THREAD_NUM; j++)
				threads.add(new PutTestThread(j, false, testDictionary, barrier));
			
			for (Thread t : threads)
				t.start();
			
			for (Thread t : threads) {
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			completionTime = System.currentTimeMillis() - completionTime;
			System.out.println("Replace + get completion time: " + completionTime);
			
			// REMOVE
			
			threads = new LinkedList<Thread>();

			completionTime = System.currentTimeMillis();
			
			for (int j = 0; j < THREAD_NUM; j++)
				threads.add(new PutTestThread(j, false, testDictionary, barrier));
			
			for (Thread t : threads)
				t.start();
			
			for (Thread t : threads) {
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			completionTime = System.currentTimeMillis() - completionTime;
			System.out.println("Remove completion time: " + completionTime);
			
		}
	
	}
	
}
