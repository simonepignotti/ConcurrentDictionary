import java.util.concurrent.locks.ReentrantLock;

public class LazyConcurrentDictionary<K extends Comparable<K>,V>
		implements MyDictionary<K,V> {
	
	private static final class DictionaryEntry<K extends Comparable<K>,V> {
		
		private final K key;
		private volatile V value;
		private volatile DictionaryEntry <K,V> next;
				
		private ReentrantLock lock;
		private volatile boolean mark;
		
		public DictionaryEntry(K key, V value, DictionaryEntry<K,V> next) {
			this.key = key;
			this.value = value;
			this.next = next;
			lock = new ReentrantLock();
			mark = false;
		}
		
		public DictionaryEntry() {
			key = null;
			value = null;
			next = null;
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
		
		public boolean isMarked() {
			return mark;
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
	
	public LazyConcurrentDictionary(int capacity) {
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
		boolean valid = false;
		DictionaryEntry<K,V> pre = null;
		DictionaryEntry<K,V> cur = null;
		while (!valid) {
			try {
				pre = search(key);
				cur = pre.getNext();
				pre.lock();
				cur.lock();
				valid = validate(key,pre,cur);
				if (valid) {
					if (cur.isSentinel() || !key.equals(cur.getKey())) {
						incrementSize();
						DictionaryEntry<K,V> newEntry =
								new DictionaryEntry<K,V>(key,value,cur);
						pre.setNext(newEntry);
						success = true;
					}
				}
			}
			finally {
				pre.unlock();
				cur.unlock();
			}
		}
		return success;
	}

	@Override
	public boolean remove(K key, V value) {
		boolean valid = false;
		boolean removed = false;
		DictionaryEntry<K,V> pre = null;
		DictionaryEntry<K,V> cur = null;
		while (!valid) {
			try {
				pre = search(key);
				cur = pre.getNext();
				pre.lock();
				cur.lock();
				valid = validate(key,pre,cur);
				if (valid) {
					if (!cur.isSentinel()
							&& key.equals(cur.getKey())
							&& value.equals(cur.getValue())) {
						size--;
						cur.mark();
						pre.setNext(cur.getNext());
						removed = true;
					}
				}
			}
			finally {
				pre.unlock();
				cur.unlock();
			}
		}
		return removed;
	}

	@Override
	public boolean replace(K key, V value) {
		DictionaryEntry<K,V> cur = search(key).getNext();
		if (!cur.isSentinel()
				&& !cur.isMarked()
				&& key.equals(cur.getKey())) {
			cur.setValue(value);
			return true;
		}
		else
			return false;
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		DictionaryEntry<K,V> cur = search(key).getNext();
		if (!cur.isSentinel()
				&& !cur.isMarked()
				&& key.equals(cur.getKey())
				&& oldValue.equals(cur.getValue())) {
			cur.setValue(newValue);
			return true;
		}
		else
			return false;
	}

	@Override
	public V get(K key) {
		DictionaryEntry<K,V> cur = search(key).getNext();
		if (!cur.isSentinel()
				&& !cur.isMarked()
				&& key.equals(cur.getKey()))
			return cur.getValue();
		else
			return null;
	}

	@Override
	public int size() {
		return size.intValue();
	}
	
	public boolean isFull() {
		return (size >= capacity);
	}
	
	@Override
	public synchronized String toString() {
		String dicToString = "[";
		DictionaryEntry<K,V> cur = head.getNext();
		while (!cur.isSentinel()) {
			if (!cur.isMarked())
				dicToString += cur.toString() + ", ";
			cur = cur.getNext();
		}
		if (dicToString.length() > 3)
			return dicToString.substring(0, dicToString.length()-2) + "]";
		else
			return "[]";
	}
	
	private DictionaryEntry<K,V> search(K key) {
		DictionaryEntry<K,V> pre = head;
		DictionaryEntry<K,V> cur = head.getNext();
		while (!cur.isSentinel() && key.compareTo(cur.getKey()) > 0) {
			pre = cur;
			cur = cur.getNext();
		}
		return pre;
	}
	
	/*
	 * this also checks that a new entry was not inserted between
	 * pre and cur after the search but before cur = pre.getNext()
	 */
	private boolean validate(K key,
			DictionaryEntry<K,V> pre,
			DictionaryEntry<K,V> cur) {
		return (!pre.isMarked()
				&& (cur == pre.getNext())
				&& (cur.isSentinel()
						|| (key.compareTo(cur.getKey()) <= 0))
				);
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
