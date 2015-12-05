import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

class PutTestThread implements Runnable {
		
		private int beginIndex;
		private MyDictionary<Integer,Integer> testDictionary;
		private CyclicBarrier barrier;
		
		public PutTestThread(
				int beginIndex,
				MyDictionary<Integer,Integer> testDictionary,
				CyclicBarrier barrier) {
			this.beginIndex = beginIndex;
			this.testDictionary = testDictionary;
			this.barrier = barrier;
		}
		
		public void run() {
			for (int i = beginIndex; i < Test.SIZE+10; i += 2) {
				try {
					barrier.await();
				} catch (InterruptedException | BrokenBarrierException e) {
					e.printStackTrace();
				}
				try {
					testDictionary.put(i,i);
				} catch (NullKeyException | NullValueException e1) {
					e1.printStackTrace();
				} catch (FullDictionaryException e2) {}
			}
		}
		
	}