import java.util.concurrent.locks.ReentrantLock;

public class FineConcurrentDictionary<K extends Comparable<K>,V>
		implements MyDictionary<K,V> {
	
	private static final class DictionaryEntry<K extends Comparable<K>,V> {
		
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
			value = null;
			next = null;
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
		
		public String toString() {
			return key.toString() + ":" + value.toString();
		}
		
	}
	
	private volatile Integer size;
	private Integer capacity;
	private DictionaryEntry<K,V> head;
	
	public FineConcurrentDictionary(int capacity) {
		size = 0;
		this.capacity = Integer.valueOf(capacity);
		DictionaryEntry<K,V> sl = new DictionaryEntry<K,V>();
		DictionaryEntry<K,V> sr = new DictionaryEntry<K,V>();
		head = sl;
		sl.setNext(sr);
	}
	
	@Override
	public boolean put(K key, V value)
			throws NullKeyException, NullValueException, FullDictionaryException {
		if (key == null)
			throw new NullKeyException(
					"Cannot insert null key entries into the dictionary");
		if (value == null)
			throw new NullValueException(
					"Cannot insert null value entries into the dictionary");
		boolean success = false;
		DictionaryEntry<K,V> pre = null;
		DictionaryEntry<K,V> cur = null;
		try {
			pre = search(key);
			cur = pre.getNext();
			if (cur.isSentinel() || !key.equals(cur.getKey())) {
				incrementSize();
				DictionaryEntry<K,V> newEntry =
						new DictionaryEntry<K,V>(key,value,cur);
				pre.setNext(newEntry);
				success = true;
			}
		}
		finally {
			pre.unlock();
			cur.unlock();
		}
		return success;
	}

	@Override
	public boolean remove(K key, V value) {
		boolean removed = false;
		DictionaryEntry<K,V> pre = null;
		DictionaryEntry<K,V> cur = null;
		try {
			pre = search(key);
			cur = pre.getNext();
			if (!cur.isSentinel()
					&& key.equals(cur.getKey())
					&& value.equals(cur.getValue())) {
				size--;
				pre.setNext(cur.getNext());
				removed = true;
			}
		}
		finally {
			pre.unlock();
			cur.unlock();
		}
		return removed;
	}

	@Override
	public boolean replace(K key, V value) throws NullValueException {
		if (value == null)
			throw new NullValueException(
					"Cannot insert null value entries into the dictionary");
		boolean replaced = false;
		DictionaryEntry<K,V> pre = null;
		DictionaryEntry<K,V> cur = null;
		try {
			pre = search(key);
			cur = pre.getNext();
			if (!cur.isSentinel() && key.equals(cur.getKey())) {
				cur.setValue(value);
				replaced = true;
			}
		}
		finally {
			pre.unlock();
			cur.unlock();
		}
		return replaced;
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue)
			throws NullValueException {
		if (newValue == null)
			throw new NullValueException(
					"Cannot insert null value entries into the dictionary");
		boolean replaced = false;
		DictionaryEntry<K,V> pre = null;
		DictionaryEntry<K,V> cur = null;
		try {
			pre = search(key);
			cur = pre.getNext();
			if (!cur.isSentinel()
					&& key.equals(cur.getKey())
					&& oldValue.equals(cur.getValue())) {
				cur.setValue(newValue);
				replaced = true;
			}
		}
		finally {
			pre.unlock();
			cur.unlock();
		}
		return replaced;
	}

	@Override
	public V get(K key) {
		V value = null;
		DictionaryEntry<K,V> pre = null;
		DictionaryEntry<K,V> cur = null;
		try {
			pre = search(key);
			cur = pre.getNext();
			if (!cur.isSentinel() && key.equals(cur.getKey()))
				value = cur.getValue();
		}
		finally {
			pre.unlock();
			cur.unlock();
		}
		return value;
	}

	@Override
	public int size() {
		return size;
	}
	
	public boolean isFull() {
		return size >= capacity;
	}
	
	@Override
	public synchronized String toString() {
		String dicToString = "[";
		DictionaryEntry<K,V> cur = head.getNext();
		while (!cur.isSentinel()) {
			dicToString += cur.toString() + ", ";
			cur = cur.getNext();
		}
		if (dicToString.length() > 3)
			return dicToString.substring(0, dicToString.length()-2) + "]";
		else
			return "[]";
	}
	
	private DictionaryEntry<K,V> search(K key) {
		head.lock();
		head.getNext().lock();
		DictionaryEntry<K,V> pre = head;
		DictionaryEntry<K,V> cur = head.getNext();
		while (!cur.isSentinel() && key.compareTo(cur.getKey()) > 0) {
			pre.unlock();
			pre = cur;
			cur = cur.getNext();
			cur.lock();
		}
		return pre;
	}
	
	private void incrementSize() throws FullDictionaryException {
		synchronized (size) {
			if (this.isFull())
				throw new FullDictionaryException(
						"The dictionary is full, "
						+ "cannot insert any more entries");
			else
				size++;
		}
	}
	
}
