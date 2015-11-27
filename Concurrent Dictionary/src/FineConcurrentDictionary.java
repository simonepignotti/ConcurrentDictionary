
public class FineConcurrentDictionary<K extends Comparable<K>,V> implements MyDictionary<K,V> {
	
	private volatile int size;
	private DictionaryEntry<K,V> head;
	
	public FineConcurrentDictionary() {
		size = 0;
		DictionaryEntry<K,V> sl = new DictionaryEntry<K,V>();
		DictionaryEntry<K,V> sr = new DictionaryEntry<K,V>();
		head = sl;
		sl.setNext(sr);
	}
	
	@Override
	public V put(K key, V value) {
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
				size++;
			}
		}
		finally{
			pre.unlock();
			cur.unlock();
			return value;
		}
	}

	@Override
	public Boolean remove(K key, V value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V replace(K key, V value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V replace(K key, V oldValue, V newValue) {
		// TODO Auto-generated method stub
		return null;
	}

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
			if (key.equals(cur.getKey()))
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
				dicToString += cur.getKey() + " : " + cur.getValue() + "\n";
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
