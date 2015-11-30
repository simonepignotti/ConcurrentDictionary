import java.util.concurrent.atomic.AtomicMarkableReference;

public class LockFreeConcurrentDictionary<K extends Comparable<K>,V> implements MyDictionary<K,V> {

	private static final class DictionaryEntry<K extends Comparable<K>,V> extends AtomicMarkableReference<DictionaryEntry<K,V>> {
		
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
		
		public Boolean isSentinel() {
			return (key == null);
		}
		
		public String toString() {
			return key.toString() + " : " + value.toString();
		}
		
	}
	
	private volatile int size;
	private DictionaryEntry<K,V> head;
	
	public LockFreeConcurrentDictionary() {
		size = 0;
		DictionaryEntry<K,V> sl = new DictionaryEntry<K,V>();
		DictionaryEntry<K,V> sr = new DictionaryEntry<K,V>();
		head = sl;
		sl.set(sr, false);
	}

	@Override
	public V put(K key, V value) {
		V putValue = null;
		Boolean finished = false;
		boolean[] mark = new boolean[1];
		while (!finished) {
			DictionaryEntry<K,V> pre = head;
			DictionaryEntry<K,V> cur = head.getReference();
			DictionaryEntry<K,V> suc;
			while (!cur.isSentinel() && key.compareTo(cur.getKey()) > 0) {
				suc = cur.get(mark);
				if (mark[0]) {
					if (pre.compareAndSet(cur, suc, false, false)) {
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
			if (cur.isSentinel() || !key.equals(cur.getKey())) {
				finished = pre.compareAndSet(cur, new DictionaryEntry<K,V>(key, value, cur), false, false);
				if (finished) {
					putValue = value;
					size++;
				}
			}
			else {
				finished = true;
			}
		}
		return putValue;
	}

	@Override
	public Boolean remove(K key, V value) {
		Boolean finished = false;
		Boolean removed = false;
		boolean[] mark = new boolean[1];
		while (!finished) {
			DictionaryEntry<K,V> pre = head;
			DictionaryEntry<K,V> cur = head.getReference();
			DictionaryEntry<K,V> suc;
			while (!cur.isSentinel() && key.compareTo(cur.getKey()) > 0) {
				suc = cur.get(mark);
				if (mark[0]) {
					if (pre.compareAndSet(cur, suc, false, false)) {
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
			if (!cur.isSentinel() && key.equals(cur.getKey()) && value.equals(cur.getValue())) {
				suc = cur.getReference();
				if (cur.attemptMark(suc, true)) {
					finished = true;
					removed = true;
					size--;
				}
			}
			else {
				finished = true;
			}
		}
		return removed;
	}

	@Override
	public Boolean replace(K key, V value) {
		DictionaryEntry<K,V> temp = head.getReference();
		
		while (!temp.isSentinel() && key.compareTo(temp.getKey()) > 0)
			temp = temp.getReference();

		if (!temp.isSentinel() && key.equals(temp.getKey()) && !temp.isMarked()) {
			temp.setValue(value);
			return true;
		}
		else return false;
	}

	@Override
	public Boolean replace(K key, V oldValue, V newValue) {
		DictionaryEntry<K,V> temp = head.getReference();
		
		while (!temp.isSentinel() && key.compareTo(temp.getKey()) > 0)
			temp = temp.getReference();

		if (!temp.isSentinel() && key.equals(temp.getKey()) && oldValue.equals(temp.getValue()) && !temp.isMarked()) {
			temp.setValue(newValue);
			return true;
		}
		else return false;
	}

	@Override
	public V get(K key) {
		DictionaryEntry<K,V> temp = head.getReference();
		
		while (!temp.isSentinel() && key.compareTo(temp.getKey()) > 0)
			temp = temp.getReference();

		if (!temp.isSentinel() && key.equals(temp.getKey()) && !temp.isMarked())
			return temp.getValue();
		else return null;
	}

	@Override
	public int size() {
		return size;
	}
	
	@Override
	public String toString() {
		String dicToString = "";
		boolean[] mark = new boolean[1];
		DictionaryEntry<K,V> cur = head.getReference();
		DictionaryEntry<K,V> suc = cur.get(mark);
		while (!cur.isSentinel()) {
			if (!mark[0])
				dicToString += cur.toString() + "\n";
			cur = suc;
			suc = suc.get(mark);
		}
		return dicToString;
	}

}
