import java.util.concurrent.locks.ReentrantLock;

public class LazyConcurrentDictionary<K extends Comparable<K>,V> implements MyDictionary<K,V> {
	
	private static final class DictionaryEntry<K extends Comparable<K>,V> {
		
		private final K key;
		private volatile V value;
		private volatile DictionaryEntry <K,V> next;
				
		private ReentrantLock lock;
		private Boolean mark;
		
		public DictionaryEntry(K key, V value, DictionaryEntry<K,V> next) {
			this.key = key;
			this.value = value;
			this.next = next;
			lock = new ReentrantLock();
			mark = false;
		}
		
		public DictionaryEntry() {
			key = null;
			lock = new ReentrantLock();
			mark = false;
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
		
		public void mark() {
			mark = true;
		}
		
		public Boolean isMarked() {
			return mark;
		}
		
		public Boolean isSentinel() {
			return (key == null);
		}
		
		public String toString() {
			return key.toString() + " : " + value.toString();
		}
		
	}
	
	private volatile int size;
	private DictionaryEntry<K,V> head;
	
	public LazyConcurrentDictionary() {
		size = 0;
		DictionaryEntry<K,V> sl = new DictionaryEntry<K,V>();
		DictionaryEntry<K,V> sr = new DictionaryEntry<K,V>();
		head = sl;
		sl.setNext(sr);
	}
	
	@Override
	public V put(K key, V value) {
		V putValue = null;
		Boolean valid = false;
		while (!valid) {
			DictionaryEntry<K,V> pre = head;
			DictionaryEntry<K,V> cur = head.getNext();
			while (!cur.isSentinel() && key.compareTo(cur.getKey()) > 0) {
				pre = cur;
				cur = cur.getNext();
			}
			if (cur.isSentinel() || !key.equals(cur.getKey())) {
				try {
					pre.lock();
					cur.lock();
					valid = !pre.isMarked() && (cur == pre.getNext());
					if (valid) {
						DictionaryEntry<K,V> newEntry = new DictionaryEntry<K,V>(key,value,cur);
						pre.setNext(newEntry);
						putValue = value;
						size++;
					}
				}
				finally {
					pre.unlock();
					cur.unlock();
				}
			}
			else {
				valid = true;
			}
		}
		return putValue;
	}

	@Override
	public Boolean remove(K key, V value) {
		Boolean valid = false;
		Boolean removed = false;
		while (!valid) {
			DictionaryEntry<K,V> pre = head;
			DictionaryEntry<K,V> cur = head.getNext();
			while (!cur.isSentinel() && key.compareTo(cur.getKey()) > 0) {
				pre = cur;
				cur = cur.getNext();
			}
			if (key.equals(cur.getKey()) && value.equals(cur.getValue())) {
				try {
					pre.lock();
					cur.lock();
					valid = !pre.isMarked() && (cur == pre.getNext());
					if (valid) {
						cur.mark();
						pre.setNext(cur.getNext());
						size--;
						removed = true;
					}
				}
				finally {
					pre.unlock();
					cur.unlock();
				}
			}
			else {
				valid = true;
			}
		}
		return removed;
	}

	@Override
	public Boolean replace(K key, V value) {
		DictionaryEntry<K,V> temp = head.getNext();
		
		while (!temp.isSentinel() && key.compareTo(temp.getKey()) > 0)
			temp = temp.getNext();
		
		if (key.equals(temp.getKey()) && !temp.isMarked()) {
			temp.setValue(value);
			return true;
		}
		else
			return false;
	}

	@Override
	public Boolean replace(K key, V oldValue, V newValue) {
		DictionaryEntry<K,V> temp = head.getNext();
		
		while (!temp.isSentinel() && key.compareTo(temp.getKey()) > 0)
			temp = temp.getNext();
		
		if (key.equals(temp.getKey()) && oldValue.equals(temp.getValue()) && !temp.isMarked()) {
			temp.setValue(newValue);
			return true;
		}
		else
			return false;
	}

	@Override
	public V get(K key) {
		DictionaryEntry<K,V> temp = head.getNext();
		
		while (!temp.isSentinel() && key.compareTo(temp.getKey()) > 0)
			temp = temp.getNext();
		
		if (key.equals(temp.getKey()) && !temp.isMarked())
			return temp.getValue();
		else return null;
	}

	@Override
	public int size() {
		return size;
	}
	
	public String toString() {
		String dicToString = "";
		DictionaryEntry<K,V> temp = head.getNext();
		while (!temp.isSentinel()) {
			if (!temp.isMarked())
				dicToString += temp.toString() + "\n";
			temp = temp.getNext();
		}
		return dicToString;
	}

}
