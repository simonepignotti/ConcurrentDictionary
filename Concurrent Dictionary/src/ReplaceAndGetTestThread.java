
public class ReplaceAndGetTestThread extends Thread {
	
	private int beginIndex;
	private MyDictionary<Integer,Integer> testDictionary;
	
	public ReplaceAndGetTestThread(
			int beginIndex,
			MyDictionary<Integer,Integer> testDictionary) {
		this.beginIndex = beginIndex;
		this.testDictionary = testDictionary;
	}
	
	public void run() {
		for (int i = beginIndex; i < Test.SIZE; i += Test.THREAD_NUM) {
			try {
				testDictionary.replace(i,i+1);
			} catch (NullValueException e) {
				e.printStackTrace();
			}
			assert(testDictionary.get(i) == i+1);
		}
	}
	
}
