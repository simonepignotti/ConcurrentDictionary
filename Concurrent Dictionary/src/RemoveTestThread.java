import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

class RemoveTestThread implements Runnable {
		
		private int beginIndex;
		private MyDictionary<Integer,Integer> testDictionary;
		private CyclicBarrier barrier;
		
		public RemoveTestThread(
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
				testDictionary.remove(i,i);
			}
		}
		
	}