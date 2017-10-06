import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Test CustomHashtables, using JUnit, version 4.
 *
 * @author Steve Tanimoto
 * @since Feb. 2, 2016
 */
public class CustomHTTest {
	static CustomHashtables cht;
	CustomHashtables.SeparateChainingHashtable<String, Object> testHT;
	HashPutResponseItem pItem;
	HashGetResponseItem gItem;
	
	// The following class is a class of keys and is designed especially for testing
	// collision behavior in hash tables, because it lets the tester easily force
	// collisions by specifying, in constructor calls, exactly what the hash code
	// value (not the function) for that object should be.
	class TestKey {
		int h;
		String name;
		TestKey(int h, String name){
			this.h = h;
			this.name = name;
		}
		public int hashCode() { return h; }
		public boolean equals(TestKey t) {
			return h==t.h && name.equals(t.name);
		}
		public String toString() {
			return "[h="+h+", name="+name+"]";
		}
	}
	
	@BeforeClass
	public static void setUpCustomHashtablesObject() {
    	cht = new CustomHashtables();
	}
    
    public void prepare1() {
    	// Instantiates SeparateChainingHashtable, with some
    	// specific types for keys and values.
    	// Then it inserts a bunch of key/value pairs.
    	// The initial table size is 7, and the rehashing threshold is 2.0.
    	testHT = cht.new SeparateChainingHashtable<String, Object>(7, 2.0);
    	assertEquals(7, testHT.getTableSize());
    	pItem = testHT.putAndMore("A", new Integer(101));
    	pItem = testHT.putAndMore("B", new Integer(102));
    	pItem = testHT.putAndMore("C", new Integer(103));
    	pItem = testHT.putAndMore("D", new Integer(104));
    	pItem = testHT.putAndMore("E", new Integer(105));
    	pItem = testHT.putAndMore("F", new Integer(106));
    	pItem = testHT.putAndMore("G", new Integer(107));
    	pItem = testHT.putAndMore("H", new Integer(108));
    	pItem = testHT.putAndMore("I", new Integer(109));
    	pItem = testHT.putAndMore("J", new Integer(110));
    	pItem = testHT.putAndMore("K", new Integer(111));
    	pItem = testHT.putAndMore("First", new Integer(1));
    	pItem = testHT.putAndMore("Second", new Integer(2));
    	//System.out.println(pItem);
    }
   
    
    @Test
    public void testGetTableSize() {
    	prepare1();
    	assertEquals(7, testHT.getTableSize());
    }
    
    @Test
    public void putAndMoreTest() {
    	// A basic test of the putAndMore method.
    	// It tests whether we are detecting first insertions
    	// vs. updates of existing keys' values correctly.
    	prepare1();
    	pItem = testHT.putAndMore("One more", new Integer(54321));
    	//System.out.println(pItem);
    	assertFalse("Should be no replacement here.", pItem.replaced);
    	assertEquals("correct size?", 14, testHT.size());

    	pItem = testHT.putAndMore("One more", new Integer(12345));
    	//System.out.println(pItem);
    	assertTrue("Yes, we should have replacement here.", pItem.replaced);
    	assertEquals("correct size?:", 14, testHT.size());

    }
    	
    @Test
    public void getTest1() {
    	// Make sure we can PUT then GET with the same Key and Value.
    	// This is pretty basic.
    	prepare1();
    	pItem = testHT.putAndMore("Great", new Integer(1));
    	gItem = testHT.getAndMore("Great");
    	assertEquals(((Integer)gItem.val), new Integer(1));
    	//System.out.println("getAndMore returned "+gItem);
    }

    @Test
    public void getTest2() {
    	// Test more cases of GET.
    	// Key not in table:
    	prepare1();
        gItem = testHT.getAndMore("Another");
        assertNull(gItem.val);
        pItem = testHT.putAndMore("Second", new Integer(2));
    	//System.out.println(pItem);
    	pItem = testHT.putAndMore("Third", new Integer(3));
    	gItem = testHT.getAndMore("Second");
    	//System.out.println("GET returned "+gItem.val);
    	// Make sure updating is working, from GET's point of view:
    	assertTrue(((Integer)gItem.val).equals(new Integer(2)));
        //assertTrue(gItem.success);
    }
    
	@Test
    public void clearTest() {
		// Make sure the clear() method works.
    	CustomHashtables.SeparateChainingHashtable<TestKey, String> tht1 = 
    			cht.new SeparateChainingHashtable<TestKey, String>(13, 2.0);
    	TestKey t1 = new TestKey(1, "TestKey1");
    	pItem = tht1.putAndMore(t1, "TestValue1");
    	assertEquals(tht1.size(), 1);
    	tht1.clear();
    	assertEquals(tht1.size(), 0);
    	assertEquals(tht1.getLambda(), 0.0, 0.001);
    }
	
	@Test
    public void clearTest2() {
		// Make sure the clear() method works.
		testHT = cht.new SeparateChainingHashtable<String, Object>(7, 2.0);
    	assertEquals(7, testHT.getTableSize());
    	pItem = testHT.putAndMore("A", new Integer(101));
    	pItem = testHT.putAndMore("B", new Integer(102));
    	pItem = testHT.putAndMore("C", new Integer(103));
    	pItem = testHT.putAndMore("D", new Integer(104));
    	pItem = testHT.putAndMore("E", new Integer(105));
    	pItem = testHT.putAndMore("F", new Integer(106));
    	pItem = testHT.putAndMore("G", new Integer(107));
    	pItem = testHT.putAndMore("H", new Integer(108));
    	pItem = testHT.putAndMore("I", new Integer(109));
    	pItem = testHT.putAndMore("J", new Integer(110));
    	pItem = testHT.putAndMore("K", new Integer(111));
    	pItem = testHT.putAndMore("First", new Integer(1));
    	pItem = testHT.putAndMore("Second", new Integer(2));
    	assertEquals(13, testHT.size());
    	testHT.clear();
    	assertEquals(0, testHT.size());
    	assertEquals(testHT.getLambda(), 0.0, 0.001);
    }

	@Test
    public void locationTest() {
		// Tests whether keys are put in the HT where they should be.
    	CustomHashtables.SeparateChainingHashtable<TestKey, String> tht1 = 
    			cht.new SeparateChainingHashtable<TestKey, String>(13, 2.0);
    	TestKey t1 = new TestKey(5, "TestKey1");
    	pItem = tht1.putAndMore(t1, "TestValue1");
    	assertEquals("Latest key should be at index 5.", 5, pItem.index);
    	TestKey t2 = new TestKey(5, "TestKey2");
    	pItem = tht1.putAndMore(t2, "TestValue2");
    	assertEquals("Latest key should be at index 5.", 5, pItem.index);
    }

    @Test
    public void collisionCountingTest(){
    	// Checks to see that collisions are being counted and returned
    	// properly both for PUT and for GET.
    	CustomHashtables.SeparateChainingHashtable<TestKey, String> tht2 = cht.new SeparateChainingHashtable<TestKey, String>(13, 2.0);
    	TestKey t1 = new TestKey(1, "TestKey1");
    	pItem = tht2.putAndMore(t1, "TestValue1");
    	assertEquals("Should be no collisions.", pItem.k, 0);
    	TestKey t2 = new TestKey(1, "TestKey2");
    	pItem = tht2.putAndMore(t2, "TestValue2");
    	assertEquals("Should be 1 collision.", pItem.k, 1);
    	TestKey t3 = new TestKey(5, "TestKey3");
    	pItem = tht2.putAndMore(t3, "TestValue3");
    	assertEquals("Should be no collisions.", pItem.k, 0);
    	TestKey t4 = new TestKey(1, "TestKey4");
    	pItem = tht2.putAndMore(t4, "TestValue4");
    	assertEquals("Should be 2 collision.", pItem.k, 2);
    	gItem = tht2.getAndMore(t4);
    	assertEquals("Should be 2 collision.", gItem.k, 2);
    }
    
    @Test
    public void testKeySet() {
    	prepare1();
    	Set result = testHT.keySet();
    	System.out.println("All keys in the table: "+result);
    	String[] someKeys = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "Second", "First"};
    	List<String> listofkeys = Arrays.asList(someKeys);
    	Set<String> expected = new HashSet(listofkeys);
    	assertEquals("keySet should get all keys from the table as a set.", expected, result);
    }
    
    // If you are implementing rehashing, then you should uncomment this @Test.
    //@Test
    public void testRehashing() {
    	// Here rehashing should occur when the 4th item is inserted, because
    	// the load factor will go from 3/3=1.0 which is still allowed with
    	// no rehashing to a value of 4/3 which exceeds 1.0.
    	// The new tableSize should be the next prime greater than or
    	// equal to twice the current size, which means it becomes 7.
    	CustomHashtables.SeparateChainingHashtable<String, String> tht1 = 
    			cht.new SeparateChainingHashtable<String, String>(3, 1.0);
    	pItem = tht1.putAndMore("A", "1");
    	System.out.println(tht1);
    	pItem = tht1.putAndMore("B", "2");
    	System.out.println(tht1);
    	pItem = tht1.putAndMore("C", "3");
    	System.out.println(tht1);
    	pItem = tht1.putAndMore("D", "4");
    	assertEquals("Rehashing should occur here.", 7, tht1.getTableSize());
    	System.out.println(tht1);
    	pItem = tht1.putAndMore("E", "5");
    	System.out.println(tht1);
    	pItem = tht1.putAndMore("F", "6");
    	System.out.println(tht1);

    }
    
    @Test
    public void primeTest() {
    	int[] primes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97};
    	int[] nonprimes = {-1, 0, 1, 4, 6, 8, 9, 10, 12, 14, 15, 16, 18, 20, 21, 22, 24, 25, 26, 27, 28, 30, 32, 33, 34, 
    			35, 36, 38, 39, 40, 42, 44, 45, 46, 48, 49, 50, 51, 52, 54, 55, 56, 57, 58, 60, 62, 63, 64, 65, 66, 68,
    			69, 70, 72, 74, 75, 76, 77, 78, 80, 81, 82, 84, 85, 86, 87, 88, 90, 92, 93, 94, 95, 96, 98, 99,
    			100, 102};
    	for (int i=0; i<primes.length; i++) {
    		assertTrue("This should be prime: "+i, cht.primeTest(primes[i]));
    	}
    	for (int i=0; i<nonprimes.length; i++) {
    		
    		assertFalse("Offending number: "+i, cht.primeTest(nonprimes[i]));
    	}
    	
    }
    
}
