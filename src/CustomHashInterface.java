import java.util.Set;

/**
 * 
 */

/**
 * @author Steve Tanimoto
 * For detailed information about the functions in this interface,
 * see the assignment specification.
 *
 */
public interface CustomHashInterface<Key, Value> {
	public HashPutResponseItem putAndMore(Key k, Value v);
	public void clear();
	public HashGetResponseItem getAndMore(Key key);
	public double getLambda();
	public Set keySet();
	public int getTableSize();
	public int size();
}
