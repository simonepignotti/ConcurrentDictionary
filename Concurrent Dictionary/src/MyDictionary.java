import java.util.concurrent.locks.ReentrantLock;

public interface MyDictionary<K extends Comparable<K>,V> {

	public static final class DictionaryEntry<K,V> {
		
		private final K key;
		private volatile V value;
		private volatile DictionaryEntry <K,V> next;
				
		private ReentrantLock lock;
		
		public DictionaryEntry(K key, V value, DictionaryEntry<K,V> next) {
			this.key = key;
			this.value = value;
			this.next = next;
			lock = new ReentrantLock();
		}
		
		public DictionaryEntry() {
			key = null;
			lock = new ReentrantLock();
		}
		
		public K getKey() {
			return key;
		}
		
		public V getValue() {
			return value;
		}
		
		public DictionaryEntry<K,V> getNext() {
			return next;
		}
		
		public void setValue(V value) {
			this.value = value;
		}
		
		public void setNext(DictionaryEntry<K,V> next) {
			this.next = next;
		}
		
		public void lock() {
			lock.lock();
		}
		
		public void unlock() {
			lock.unlock();
		}
		
		public boolean isSentinel() {
			return (key == null);
		}
		
	}
	
	//if the key is not already associated with a value,
	//associate it with the given value.
	public V put(K key, V value);
	
	//Removes the entry for a key only if currently
	//associated to a given value
	public Boolean remove(K key, V value);
	
	//Replaces the entry for a key only if currently
	//associated to some value.
	public V replace(K key, V value);
	
	//Replaces the entry for a key only if currently
	//associated to the given value old.
	public V replace(K key, V oldValue, V newValue);
	
	//Results the value for the key, if present
	public V get (K key);
	
	//Returns the number of elements in the dictionary
	//(its cardinality).
	public int size();
	
}
