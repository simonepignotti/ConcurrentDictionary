import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

class PutTestThread extends Thread {
		
		private int beginIndex;
		private boolean functionalityTest;
		private MyDictionary<Integer,Integer> testDictionary;
		private CyclicBarrier barrier;
		
		public PutTestThread(
				int beginIndex,
				boolean functionalityTest,
				MyDictionary<Integer,Integer> testDictionary,
				CyclicBarrier barrier) {
			this.beginIndex = beginIndex;
			this.functionalityTest = functionalityTest;
			this.testDictionary = testDictionary;
			this.barrier = barrier;
		}
		
		public void run() {
			for (int i = beginIndex; i < Test.SIZE; i += Test.THREAD_NUM) {
				if (functionalityTest) {
					try {
						barrier.await();
					} catch (InterruptedException | BrokenBarrierException e) {
						e.printStackTrace();
					}
				}
				try {
					testDictionary.put(i,i);
				} catch (NullKeyException | NullValueException e1) {
					e1.printStackTrace();
				} catch (FullDictionaryException e2) {}
			}
			return;
		}
		
	}