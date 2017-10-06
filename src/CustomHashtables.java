// Sheng Chen, UW ID: 1263891

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CustomHashtables {
	
	public class SeparateChainingHashtable<Key, Value> implements CustomHashInterface<Key, Value> {
		
		// A hash table that stores all the separate chains, each corresponding to an index.
		List<SeparateChain<Key, Value>> ht;
		// A hash table used for rehashing.
		List<SeparateChain<Key, Value>> ht2;
		// A boolean that decides whether a rehash is going to be conducted.
		boolean rehashingEnabled;
		double rehashingThreshold;
		
		// The constructor of the separateChainingHashtable class. Takes in an integer tableSize and
		// a double rehashingThreshold. Initializes and populates the hash table.
		public SeparateChainingHashtable(int tableSize, double rehashingThreshold){
			this.rehashingThreshold = rehashingThreshold;
			rehashingEnabled = false;
			ht = new ArrayList<SeparateChain<Key, Value>>();
			ht2 = new ArrayList<SeparateChain<Key, Value>>();
			for (int i = 0; i < tableSize; i++) {
				ht.add(new SeparateChain<Key, Value>());
			}
		}
		
		// This method returns the number of keys that are in the hash table.
		public int size() {
			return keySet().size();
		}

		// This method returns the size of the ArrayList being used to represent the hash table.
		public int getTableSize() {
			return ht.size();
		}
		
		// This method takes in a Key and a Value. It then checks if key k is in the hash table.
		// If so, replace its value by v. If not, insert this pair. Also returns a response
		// item that describes what happened and updates the location index.
		public HashPutResponseItem putAndMore(Key k, Value v){
			if (rehashingEnabled && getLambda() > rehashingThreshold) {
				rehash();
				return reput(k, v);
			}else {
				int arrayIndex = myMod(k.hashCode(), getTableSize());
				SeparateChain<Key, Value> currChain = ht.get(arrayIndex);
				HashPutResponseItem currPutRI = currChain.insert(k, v);
				currPutRI.index = arrayIndex;
				return currPutRI;
			}
		}

		// This is the rehash method that rehashes the table when the load factor exceeds the
		// rehashing threshold. All the key value pairs are transfered into a new hash table. 
		// The new table's size should be the smallest prime number that is at least double the
		// old size. The HashPutResponseItem that is returned should include in its rd field a 
		// description of the rehashing: "Rehashed to table size 61".
		void rehash() {
			int newTableSize = 2 * getTableSize();
			while (!primeTest(newTableSize)) {
				newTableSize++;
			}
			for (int i = 0; i < newTableSize; i++) {
				ht2.add(new SeparateChain<Key, Value>());
			}
			for (int i = 0; i < getTableSize(); i++) {
				SeparateChain<Key, Value> currChain = ht.get(i);
				SeparateChain.SC_Node current = currChain.head;
				while (current != null) {
					reput((Key)current.key, (Value)current.value);
					current = current.next;
				}
			}
		}

		// This method reputs the key and value pair into the new, rehashed table.
		public HashPutResponseItem reput(Key k, Value v) {
			int arrayIndex = myMod(k.hashCode(), ht2.size());
			SeparateChain<Key, Value> currChain = ht2.get(arrayIndex);
			HashPutResponseItem currPutRI = currChain.insert(k, v);
			currPutRI.index = arrayIndex;
			currPutRI.rd = "Rehashed to table size " + ht2.size();
			return currPutRI;
		}
		
		// This method removes all items from the hash table, so that it is empty, and any 
		//  insertion that immediately follows will be guaranteed to not have a collision.
		public void clear() {
			for (int i = 0; i < getTableSize(); i++) {
				ht.get(i).clear();
			}
		}

		// This method checks if key k is in the hash table. If so, return the corresponding 
		// value v in the response item. If not, return null as the value in the response item.
		// It also updates the location index.
		public HashGetResponseItem<Value> getAndMore(Key key) {
			int arrayIndex = myMod(key.hashCode(), getTableSize());
			SeparateChain<Key, Value> currChain = ht.get(arrayIndex);
			HashGetResponseItem<Value> currGetRI = currChain.find(key);
			currGetRI.index = arrayIndex;
			return currGetRI;
		}

		// This method returns the current load factor for the hash table.
		public double getLambda(){
			return (float)size()/getTableSize();
		}

		// This method collects and returns a set of all the keys currently in the hash table.
		public Set<Key> keySet() {
			HashSet<Key> keySet = new HashSet<Key>();
			for (SeparateChain<Key, Value> curr: ht) {
				keySet.addAll(curr.getKeysFromThisChain());
			}
			return keySet;
		}
		
		@Override
		public String toString() {
			String a = "";
			for (int i = 0; i < ht.size(); i++) {
				a += ht.get(i).toString();
			}
			return a;
		}
		

		/* Part of an extra-credit option is to implement this:
		void rehash() {}
		 */
	}
	// Stub for the extra-credit option on linear probing:
	//public class LinearProbingHashtable<Key, Value> implements CustomHashInterface<Key, Value> {

	// If you implement LinearProbingHashtable, automatically rehash, when necessary, 
	// to a newTableSize of the smallest prime that is greater than 2*tableSize.


	/* Useful for turning int value into hash table index values.
	 * Avoids the bug where % can return a negative value.
	 */
	public int myMod(int a, int b) {
		return ((a % b) + b) % b; 
	}

	/* primeTest is useful in implementing rehashing.
	 * It takes an int and returns true if it is prime, and false otherwise.
	 */
	public boolean primeTest(int n) {
		if (n < 2) { return false; }
		if (n==2 || n==3) { return true; }
		if ((n % 2) == 0 || (n % 3) == 0) { return false; }
		int limit = (int) Math.ceil(Math.sqrt(n))+1;
		for (int k=5; k <= limit; k+=2) {
			if (n % k == 0) { 
				return false; }
		}
		return true;
	}
	
	
//	 Extra Credit B
	
	public class LinearProbingHashTable<Key, Value> implements CustomHashInterface<Key, Value> {
		
		// A hash table that stores all the separate chains, each corresponding to an index.
		List<SeparateChain<Key, Value>> ht;
		final int tableSize = 9999;

		// The constructor of the separateChainingHashtable class. Takes in an integer tableSize and
		// a double rehashingThreshold. Initializes and populates the hash table.
		public LinearProbingHashTable(int size, double rehashingThreshold){
			ht = new ArrayList<SeparateChain<Key, Value>>();
			for (int i = 0; i < tableSize; i++) {
				ht.add(new SeparateChain<Key, Value>());
			}
		}
		
		// This method returns the number of keys that are in the hash table.
		public int size() {
			return keySet().size();
		}

		// This method returns the size of the ArrayList being used to represent the hash table.
		public int getTableSize() {
			return ht.size();
		}
		
		// This method takes in a Key and a Value. It then checks if key k is in the hash table.
		// If so, replace its value by v. If not, insert this pair. Also returns a response
		// item that describes what happened and updates the location index.
		public HashPutResponseItem putAndMore(Key k, Value v){
			int arrayIndex = myModLP(k.hashCode(), getTableSize());
			SeparateChain<Key, Value> currChain = ht.get(arrayIndex);
			HashPutResponseItem currPutRI = currChain.insert(k, v);
			if (currChain.getKeysFromThisChain() == null) {
				currPutRI.index = arrayIndex;
			}else {
				while (currChain.getKeysFromThisChain() != null) {
					arrayIndex++;
					currChain = ht.get(arrayIndex);
				}
				currPutRI = currChain.insert(k, v);
				currPutRI.index = arrayIndex;
			}
			return currPutRI;
		}
		
		// This method removes all items from the hash table, so that it is empty, and any 
		//  insertion that immediately follows will be guaranteed to not have a collision.
		public void clear() {
			for (int i = 0; i < getTableSize(); i++) {
				ht.get(i).clear();
			}
		}

		// This method checks if key k is in the hash table. If so, return the corresponding 
		// value v in the response item. If not, return null as the value in the response item.
		// It also updates the location index.
		public HashGetResponseItem<Value> getAndMore(Key key) {
			int arrayIndex = myModLP(key.hashCode(), getTableSize());
			SeparateChain<Key, Value> currChain = ht.get(arrayIndex);
			HashGetResponseItem<Value> currGetRI = currChain.find(key);
			currGetRI.index = arrayIndex;
			return currGetRI;
		}

		// This method returns the current load factor for the hash table.
		public double getLambda(){
			return (float)size()/getTableSize();
		}

		// This method collects and returns a set of all the keys currently in the hash table.
		public Set<Key> keySet() {
			HashSet<Key> keySet = new HashSet<Key>();
			for (SeparateChain<Key, Value> curr: ht) {
				keySet.addAll(curr.getKeysFromThisChain());
			}
			return keySet;
		}
	}

	/* Useful for turning int value into hash table index values.
	 * Avoids the bug where % can return a negative value.
	 */
	public int myModLP(int a, int b) {
		return ((a % b) + b) % b; 
	}

	/* primeTest is useful in implementing rehashing.
	 * It takes an int and returns true if it is prime, and false otherwise.
	 */
	public boolean primeTestLP(int n) {
		if (n < 2) { return false; }
		if (n==2 || n==3) { return true; }
		if ((n % 2) == 0 || (n % 3) == 0) { return false; }
		int limit = (int) Math.ceil(Math.sqrt(n))+1;
		for (int k=5; k <= limit; k+=2) {
			if (n % k == 0) { 
				return false; }
		}
		return true;
	}	
}
