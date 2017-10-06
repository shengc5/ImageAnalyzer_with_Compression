/**
 * This class is used to return status of a PUT
 * operation on custom hashtables.  It contains 
 * (a) a count of the steps executed to find a place for the item, 
 * (b) the integer index in the hashtable where this item lives.  For separate
 * chaining, this is the index in the hashtable where the linked list for
 * this item's bin starts.
 * (c) Whether the PUT required replacing rather than adding a value, and
 * (d) If there was rehashing, a description of the size change,
 * and the number steps required in total to do the rehashing.
 */

/**
 * @author Steve Tanimoto
 *
 */
public class HashPutResponseItem {
	final int k;
	int index;
	final boolean replaced;
	String rd;
	
	public HashPutResponseItem(int k, int index, boolean replaced, String rd) {
		this.k = k;
		this.index = index;
		this.replaced = replaced;
		this.rd = rd;
	}

	public String toString() {
		String ans = "PUT took "+k+" steps; "+" at "+index;
		if (replaced) { ans += "; (value replaced);";}
		ans += rd;
		return ans;
	}

}
