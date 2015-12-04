
public interface MyDictionary<K extends Comparable<K>,V> {
	
	//if the key is not already associated with a value,
	//associate it with the given value.
	public boolean put(K key, V value)
			throws NullKeyException,
				NullValueException,
				FullDictionaryException;
	
	//Removes the entry for a key only if currently
	//associated to a given value
	public boolean remove(K key, V value);
	
	//Replaces the entry for a key only if currently
	//associated to some value.
	public boolean replace(K key, V value)
			throws NullValueException;
	
	//Replaces the entry for a key only if currently
	//associated to the given value old.
	public boolean replace(K key, V oldValue, V newValue)
			throws NullValueException;
	
	//Results the value for the key, if present
	public V get (K key);
	
	//Returns the number of elements in the dictionary
	//(its cardinality).
	public int size();
	
}
