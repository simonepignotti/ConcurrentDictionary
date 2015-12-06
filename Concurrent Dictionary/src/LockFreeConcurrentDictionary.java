import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class LockFreeConcurrentDictionary<K extends Comparable<K>,V>
		implements MyDictionary<K,V> {

	private static final class DictionaryEntry<K extends Comparable<K>,V>
			extends AtomicMarkableReference<DictionaryEntry<K,V>> {
		
		private final K key;
		private volatile V value;
		
		public DictionaryEntry(K key, V value, DictionaryEntry<K,V> next) {
			super(next, false);
			this.key = key;
			this.value = value;
		}
		
		public DictionaryEntry() {
			super(null, false);
			key = null;
			value = null;
		}
		
		public K getKey() {
			return key;
		}
		
		public V getValue() {
			return value;
		}
		
		public void setValue(V value) {
			this.value = value;
		}
		
		public boolean isSentinel() {
			return (key == null);
		}
		
		public String toString() {
			return key.toString() + ":" + value.toString();
		}
		
	}
	
	private volatile AtomicInteger size;
	private final int capacity;
	private final DictionaryEntry<K,V> head;
	
	public LockFreeConcurrentDictionary(int capacity) {
		size = new AtomicInteger(0);
		this.capacity = capacity;
		DictionaryEntry<K,V> sl = new DictionaryEntry<K,V>();
		DictionaryEntry<K,V> sr = new DictionaryEntry<K,V>();
		head = sl;
		sl.set(sr, false);
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
		boolean finished = false;
		while (!finished) {
			DictionaryEntry<K,V> pre = searchAndClean(key);
			DictionaryEntry<K,V> cur = pre.getReference();
			if (cur.isSentinel() || !key.equals(cur.getKey())) {
				/*
				 * increment the size before the cas to "reserve" a place for
				 * the new entry, and decrement it in case of failure
				 */
				incrementSize();
				finished = pre.compareAndSet(cur,
						new DictionaryEntry<K,V>(key, value, cur),
						false, false);
				if (finished)
					success = true;
				else
					size.decrementAndGet();
			}
			else {
				finished = true;
			}
		}
		return success;
	}

	@Override
	public boolean remove(K key, V value) {
		boolean removed = false;
		boolean finished = false;
		while (!finished) {
			DictionaryEntry<K,V> pre = searchAndClean(key);
			DictionaryEntry<K,V> cur = pre.getReference();
			if (cur != null
					&& !cur.isSentinel()
					&& key.equals(cur.getKey())
					&& value.equals(cur.getValue())
					&& !cur.isMarked()) {
				if(cur.compareAndSet(cur.getReference(), cur.getReference(),
						false, true)) {
					size.decrementAndGet();
					finished = true;
					removed = true;
				}
			}
			else {
				finished = true;
			}
		}
		return removed;
	}

	@Override
	public boolean replace(K key, V value) {
		DictionaryEntry<K,V> cur = search(key);
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
		DictionaryEntry<K,V> cur = search(key);
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
		DictionaryEntry<K,V> cur = search(key);
		if (!cur.isSentinel()
				&& !cur.isMarked()
				&& key.equals(cur.getKey()))
			return cur.getValue();
		else
			return null;
	}

	@Override
	public int size() {
		return size.get();
	}
	
	@Override
	public synchronized String toString() {
		String dicToString = "[";
		boolean[] mark = new boolean[1];
		DictionaryEntry<K,V> cur = head.getReference();
		DictionaryEntry<K,V> suc = cur.get(mark);
		while (!cur.isSentinel()) {
			if (!mark[0])
				dicToString += cur.toString() + ", ";
			cur = suc;
			suc = suc.get(mark);
		}
		if (dicToString.length() > 3)
			return dicToString.substring(0, dicToString.length()-2) + "]";
		else
			return "[]";
	}
	
	private DictionaryEntry<K,V> searchAndClean(K key) {
		DictionaryEntry<K,V> pre = head;
		DictionaryEntry<K,V> cur = head.getReference();
		DictionaryEntry<K,V> suc;
		boolean[] mark = new boolean[1];
		while (!cur.isSentinel() && key.compareTo(cur.getKey()) > 0) {
			suc = cur.get(mark);
			if (mark[0]) {
				if(pre.compareAndSet(cur, suc, false, false)) {
					cur = suc;
				}
				else {
				pre = head;
				cur = head.getReference();
				}
			}
			else {
				pre = cur;
				cur = suc;
			}
		}
		return pre;
	}
	
	private DictionaryEntry<K,V> search(K key) {
		DictionaryEntry<K,V> cur = head.getReference();
		while (!cur.isSentinel() && key.compareTo(cur.getKey()) > 0) {
			cur = cur.getReference();
		}
		return cur;
	}
	
	private void incrementSize() throws FullDictionaryException {
		if (size.incrementAndGet() > capacity) {
			size.decrementAndGet();
			throw new FullDictionaryException(
					"The dictionary is full, "
					+ "cannot insert any more entries");
		}
	}

}
