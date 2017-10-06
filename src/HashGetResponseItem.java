/**
 * This class is used to wrap objects coming back from basically the GET kind of
 * operation on custom hashtables.  It contains not only the object being returned
 * but also (a) a count of the collisions on the way to find the item, if found, and
 * if not found, the number of collisions on the way to determining it was not there,
 * and
 * (b) the integer index in the hashtable where this item lives, if it was there, 
 * and if it wasn't there, the last location that was checked; For separate
 * chaining, this is the index in the hashtable where the linked list for
 * this item's bin starts or would start if it were there.
 */

/**
 * @author Steve Tanimoto
 * This class wraps the information to be passed back to the caller after a
 * call to the method getAndMore.
 *
 */
public class HashGetResponseItem<ValueType> {
	final ValueType val;
	final int k;
	int index;
	final boolean success;
	
	public HashGetResponseItem(ValueType val, int k, int index, boolean success) {
		this.val = val; // The value that is being returned (associated with the key).
		this.k = k; // Number of collisions that occurred while trying to find the key.
		this.index = index; // Position in the hash table where this was found,
		   // or if not found, the last hash table location that was examined.
		this.success = success; // True if the key was found.
	}
	
	public String toString() {
		if (val==null) { return "Value: null; "+k+" collisions; "+" at "+index;}
		else {return "Value: "+val.toString()+"; "+k+" collisions; "+" at "+index;}
	}

}
