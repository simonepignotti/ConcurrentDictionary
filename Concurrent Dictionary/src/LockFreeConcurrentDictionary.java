import java.util.concurrent.atomic.AtomicMarkableReference;

public class LockFreeConcurrentDictionary<K extends Comparable<K>,V> implements MyDictionary<K,V> {

	private static final class DictionaryEntry<K extends Comparable<K>,V> extends AtomicMarkableReference<DictionaryEntry<K,V>> {
		
		private final K key;
		private volatile V value;
				
		public DictionaryEntry(K key, V value, AtomicMarkableReference<DictionaryEntry<K,V>> next) {
			this.key = key;
			this.value = value;
			this.next = next;
		}
		
		public DictionaryEntry() {
			key = null;
			value = null;
			next = null;
		}
		
		public K getKey() {
			return key;
		}
		
		public V getValue() {
			return value;
		}
		
		public AtomicMarkableReference<DictionaryEntry<K,V>> getNext() {
			return next;
		}
		
		public void setValue(V value) {
			this.value = value;
		}
		
		public void setNext(AtomicMarkableReference<DictionaryEntry<K,V>> next) {
			this.next = next;
		}
		
		public Boolean isSentinel() {
			return (key == null);
		}
		
		public String toString() {
			return key.toString() + " : " + value.toString();
		}
		
	}
	
	private volatile int size;
	private AtomicMarkableReference<DictionaryEntry<K,V>> head;
	
	public LockFreeConcurrentDictionary() {
		size = 0;
		AtomicMarkableReference<DictionaryEntry<K,V>> sl =
				new AtomicMarkableReference<DictionaryEntry<K,V>>(new DictionaryEntry<K,V>(),false);
		AtomicMarkableReference<DictionaryEntry<K,V>> sr =
				new AtomicMarkableReference<DictionaryEntry<K,V>>(new DictionaryEntry<K,V>(),false);
		head = sl;
		sl.getReference().setNext(sr);
	}

	@Override
	public V put(K key, V value) {
		V putValue = null;
		Boolean valid = false;
		boolean[] mark = new boolean[1];
		while (!valid) {
			AtomicMarkableReference<DictionaryEntry<K,V>> pre = head;
			AtomicMarkableReference<DictionaryEntry<K,V>> cur = head.getReference().getNext();
			DictionaryEntry<K,V> preRef = pre.getReference();
			DictionaryEntry<K,V> curRef = cur.get(mark);
			while (!curRef.isSentinel() && key.compareTo(curRef.getKey()) > 0) {
				if (mark[0]) {
					if (cur.compareAndSet(curRef, curRef.getNext().getReference(), false, false)) {
						// ???
					}
					else {
						pre = head;
						cur = head.getReference().getNext();
						preRef = pre.getReference();
						curRef = cur.get(mark);
					}
				}
				else {
				pre = cur;
				cur = curRef.getNext();
				preRef = curRef;
				curRef = cur.get(mark);
				}
			}
			if (curRef.isSentinel() || !key.equals(curRef.getKey())) {
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean replace(K key, V value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean replace(K key, V oldValue, V newValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V get(K key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

}
