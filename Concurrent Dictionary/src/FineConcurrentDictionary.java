import java.util.concurrent.locks.ReentrantLock;

public class FineConcurrentDictionary<K extends Comparable<K>,V> implements MyDictionary<K,V> {
	
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
		
		public Boolean isSentinel() {
			return (key == null);
		}
		
		public String toString() {
			return key.toString() + " : " + value.toString();
		}
		
	}
	
	private volatile int size;
	private DictionaryEntry<K,V> head;
	
	public FineConcurrentDictionary() {
		size = 0;
		DictionaryEntry<K,V> sl = new DictionaryEntry<K,V>();
		DictionaryEntry<K,V> sr = new DictionaryEntry<K,V>();
		head = sl;
		sl.setNext(sr);
	}
	
	@SuppressWarnings("finally")
	@Override
	public V put(K key, V value) {
		V putValue = null;
		DictionaryEntry<K,V> pre = null;
		DictionaryEntry<K,V> cur = null;
		try{
			head.lock();
			head.getNext().lock();
			pre = head;
			cur = head.getNext();
			while (!cur.isSentinel() && key.compareTo(cur.getKey()) > 0) {
				pre.unlock();
				cur.getNext().lock();
				pre = cur;
				cur = cur.getNext();
			}
			if (cur.isSentinel() || !key.equals(cur.getKey())) {
				DictionaryEntry<K,V> newEntry = new DictionaryEntry<K,V>(key,value,cur);
				pre.setNext(newEntry);
				putValue = value;
				size++;
			}
		}
		finally{
			pre.unlock();
			cur.unlock();
			return putValue;
		}
	}

	@SuppressWarnings("finally")
	@Override
	public Boolean remove(K key, V value) {
		Boolean removed = false;
		DictionaryEntry<K,V> pre = null;
		DictionaryEntry<K,V> cur = null;
		try{
			head.lock();
			head.getNext().lock();
			pre = head;
			cur = head.getNext();
			while (!cur.isSentinel() && key.compareTo(cur.getKey()) > 0) {
				pre.unlock();
				cur.getNext().lock();
				pre = cur;
				cur = cur.getNext();
			}
			if (!cur.isSentinel() && key.equals(cur.getKey()) && value.equals(cur.getValue())) {
				pre.setNext(cur.getNext());
				size--;
				removed = true;
			}
		}
		finally{
			pre.unlock();
			cur.unlock();
			return removed;
		}
	}

	@SuppressWarnings("finally")
	@Override
	public Boolean replace(K key, V value) {
		Boolean replaced = false;
		DictionaryEntry<K,V> pre = null;
		DictionaryEntry<K,V> cur = null;
		try{
			head.lock();
			head.getNext().lock();
			pre = head;
			cur = head.getNext();
			while (!cur.isSentinel() && key.compareTo(cur.getKey()) > 0) {
				pre.unlock();
				cur.getNext().lock();
				pre = cur;
				cur = cur.getNext();
			}
			if (key.equals(cur.getKey())) {
				cur.setValue(value);
				replaced = true;
			}
		}
		finally{
			pre.unlock();
			cur.unlock();
			return replaced;
		}
	}

	@SuppressWarnings("finally")
	@Override
	public Boolean replace(K key, V oldValue, V newValue) {
		Boolean replaced = false;
		DictionaryEntry<K,V> pre = null;
		DictionaryEntry<K,V> cur = null;
		try{
			head.lock();
			head.getNext().lock();
			pre = head;
			cur = head.getNext();
			while (!cur.isSentinel() && key.compareTo(cur.getKey()) > 0) {
				pre.unlock();
				cur.getNext().lock();
				pre = cur;
				cur = cur.getNext();
			}
			if (key.equals(cur.getKey()) && oldValue.equals(cur.getValue())) {
				cur.setValue(newValue);
				replaced = true;
			}
		}
		finally{
			pre.unlock();
			cur.unlock();
			return replaced;
		}
	}

	@SuppressWarnings("finally")
	@Override
	public V get(K key) {
		V value = null;
		DictionaryEntry<K,V> pre = null;
		DictionaryEntry<K,V> cur = null;
		try{
			head.lock();
			head.getNext().lock();
			pre = head;
			cur = head.getNext();
			while (!cur.isSentinel() && key.compareTo(cur.getKey()) > 0) {
				pre.unlock();
				cur.getNext().lock();
				pre = cur;
				cur = cur.getNext();
			}
			if (!cur.isSentinel() && key.equals(cur.getKey()))
				value = cur.getValue();
		}
		finally{
			pre.unlock();
			cur.unlock();
			return value;
		}
	}

	@Override
	public int size() {
		return size;
	}
	
	@SuppressWarnings("finally")
	@Override
	public String toString() {
		String dicToString = "";
		DictionaryEntry<K,V> pre = null;
		DictionaryEntry<K,V> cur = null;
		try{
			head.lock();
			head.getNext().lock();
			pre = head;
			cur = head.getNext();
			while (!cur.isSentinel()) {
				dicToString += cur.toString() + "\n";
				pre.unlock();
				cur.getNext().lock();
				pre = cur;
				cur = cur.getNext();
			}
		}
		finally{
			pre.unlock();
			cur.unlock();
			return dicToString;
		}
	}
	
}
