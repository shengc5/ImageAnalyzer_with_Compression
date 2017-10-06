import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

// Note: this code should run under the assumption that the rehashing functionality
//       from my CustomHashtables.java is turned off. Thus I use a moderate number
//       of 241 as the size of the hash table. This is the result of my "standard"
//       test run, with the UW image and a block size of 16. The number of distinct
//       color bins is 482. Under the rehashing Threshold of 2.0, the calculated size
//       241 happens to be a prime number.

/**
 * @author Sheng Chen 1263891.
 *
 * IndexedColor Encoder is a helper class for ImageAnalyzer.
 * It is where the methods for palette building and image encoding will be
 * implemented.
 * 
 * Students: Implement code wherever you see "TODO".
 */
public class IndexedColorEncoder {
	public static CustomHashtables customHashtables; // Your own hash table facility.
	CustomHashtables.SeparateChainingHashtable<Block, Integer> H ;
	CustomHashtables.SeparateChainingHashtable<Block, Integer> tht1;
	
	// Two lists used to store number of collisions in get and put actions
	public List<Integer> putCollisionCount;
	public List<Integer> getCollisionCount;

	public Color[][] colorImg;
	public ArrayList<Block> sortedBlocks; // to store sorted blocks(list L)
	public Color[] palette;	// stores the first U elements of list L
	public int[][] encodedPixels;	// to store the value each pixel in the image is encoded to.
	int blockSize; // Controls how much colors are grouped during palette building.
	int hashFunctionChoice; // Either 1, 2, or 3. Controls whether to use h1, h2, or h3.
	ImageAnalyzer theMainApp;
	int w, h; // width and height of current image.
	ArrayList<BlockCountPair> L; // Used when building a palette and encoding pixels.
	public double error; // To hold the per-pixel lossy encoding error.

	/* Constructor sets up a link back to the application, so that parts of
	 * the GUI and the working image can be accessed from this class.
	 */
	public IndexedColorEncoder(ImageAnalyzer theMainApp) {
		putCollisionCount = new ArrayList<Integer>();
		getCollisionCount = new ArrayList<Integer>();
		this.theMainApp = theMainApp;
		customHashtables = new CustomHashtables(); // Getting ready for your own hash table.
		hashFunctionChoice = 1; // Default hash function number is 1.
		blockSize = 4; // Default blockSize is 4x4x4

	}
	public void setHashFunctionChoice(int hc) {
		hashFunctionChoice = hc;
		System.out.println("Hash function choice is now "+hashFunctionChoice);
	}

	public void setBlockSize(int bs) {
		blockSize = bs;
		System.out.println("Block size is now "+blockSize);
	}
	class BlockCountPair {
		Block b; Integer count;
		public BlockCountPair(Block b, Integer count) {
			this.b = b; this.count = count;
		}
	}

	// The private helper method that prints out stats of each run at the end of 
	// palette building and each encoding.
	private void printStats() {
		System.out.println("Hash function no." + hashFunctionChoice + " is in use");
		System.out.println("Number of pixels in the image is " + w * h);
		System.out.println("Number of possible distinct color bins is " + 
												(256/blockSize)*(256/blockSize)*(256/blockSize));
		System.out.println("TableSize of the hashtable is " + H.getTableSize());
		System.out.println("Number of key-value pairs in the hashtable is " + H.keySet().size());
		System.out.println("Load factor is " + (double)H.keySet().size()/H.getTableSize());
	}
	
	// Private helper method that prints collision stats.
	private void printCollision() {
		System.out.println("Average number of collisions per insertion is " + 
															calculateAvg(putCollisionCount));
		System.out.println("Maximum number of collisions for any one insertion: " + 
															Collections.max(putCollisionCount));
		System.out.println("Average number of collisions per get operation is " + 
															calculateAvg(getCollisionCount));
	}
	
	// Private helper method that calculate the average of a list
	private double calculateAvg(List<Integer> list) {
		int sum = 0;
		for (int i = 0; i < list.size(); i++) {
			sum += list.get(i);
		}
		return (double) sum / list.size();
	}
	
	public void buildPalette(int paletteSize) {
		// Get the image we will be processing.
		BufferedImage I = theMainApp.biWorking;
		// Convert it to a 2-D array of Color objects.
		colorImg = theMainApp.storeCurrentPixels(I);
		// cache the width and height of this image:
		w = I.getWidth(); h = I.getHeight();

		//Set up a hash table for our blocks (the keys) and their counts (the values).
		//Table size is changed to 421 which is a prime number calculated based on a "standard"
		//test run of the UW image, with block size of 16 (number of distinct color bins / 2).
        H = customHashtables.new SeparateChainingHashtable<Block, Integer>(241, 2.0);

		// Now we develop our implementation based on the pseudocode from Part I of the Assignment.
		//We scan through all the pixels of the source image I and for each pixel p:
		for (int row=0; row<I.getHeight(); row++) {
			for (int col=0; col<I.getWidth(); col++) {

				// Implement the instantiation of Color and Block objects and hash them,
				// counting occurrences of blocks.
				Color currColor = colorImg[row][col];
				Block currBlock = new Block(currColor);
				if (H.keySet().contains(currBlock)) {
					HashPutResponseItem currItem= H.putAndMore(currBlock, H.getAndMore(currBlock).val + 1);
					putCollisionCount.add(currItem.k);
				}else {
					H.putAndMore(currBlock, 1);
					putCollisionCount.add(0);
				}
			}
			System.out.println(row);
		}

		//By this point, we have a hash table full of blocks and their counts.
		//Next we make a list L of the blocks and their counts by getting all of the pairs out of the hash table.

		// THIS PART OF THE ALGORITHM IS DONE FOR YOU:
		Set<Block> keys = H.keySet();
		L = new ArrayList<BlockCountPair>();
		Iterator<Block> ib = keys.iterator();
		while (ib.hasNext()) {
			Block b = ib.next();
			HashGetResponseItem currItem = H.getAndMore(b);
			getCollisionCount.add(currItem.k);
			L.add(new BlockCountPair(b, (Integer)currItem.val));
		}
		// Now sort L by the weight values, so that the w values are in nonincreasing order. 
		// The largest is first.

		Collections.sort(L, new Comparator<BlockCountPair> (){
			public int compare(BlockCountPair b1, BlockCountPair b2) {
				return b2.count - b1.count; // Note the reverse order.
			}
		});
		//Assuming the palette should have U entries, choose the first U elements of L, and call this LU.
		int U = paletteSize;
		Color[] T = new Color[U]; // create an empty color table T.
		int s = blockSize;
		if (U <= L.size()) {
			for (int i = 0; i < U; i++) { // Populating the palette.
				BlockCountPair currPair = L.get(i);
				T[i] = new Color ((currPair.b.Br + 1/2)* s,(currPair.b.Bg + 1/2)* s, (currPair.b.Bb + 1/2)* s);
			}
		}else {
			// Deal with the case when palette size is larger than number of elements in L.
			for (int i = 0; i < L.size(); i++) { 
				BlockCountPair currPair = L.get(i);
				T[i] = new Color ((currPair.b.Br + 1/2)* s,(currPair.b.Bg + 1/2)* s, (currPair.b.Bb + 1/2)* s);
			}
			// Pad the the remaining palette entries with black(0,0,0).
			for (int i = L.size(); i < U; i++) {
				T[i] = new Color(0,0,0);
			}
		}
		
		palette = T; // save the palette
		printStats();// Print the stats after building palette.
		printCollision();
		
		// Now enable encoding:
		theMainApp.encodeSSItem.setEnabled(true);
		theMainApp.encodeFItem.setEnabled(true);
		// If using a small image, optionally show the hash table, for debugging:
		// System.out.println(H.toString());
		// This assumes you have written a toString method for your hash table that
		// makes a nice display of the contents of the table.
	}

	public void encodeSlowAndSimple() {
		ImageAnalyzer.ElapsedTime essTime = theMainApp.new ElapsedTime();
		essTime.startTiming();
	
		// Allocate a buffer for the new image.
		encodedPixels = new int[h][w] ;	// to store the value each pixel in the image is encoded to.
		// Scan the Color version of the original image.
		for (int i=0; i<h; i++) {
			for (int j=0; j<w; j++) {
				Color c = colorImg[i][j];
				int bestIndex = c.findIndexOfClosestColor(palette);
				encodedPixels[i][j] = bestIndex;
			}
		}
		essTime.reportTime(); 
		printStats(); // Print the stats after encoding.
		System.out.println("Encoding with the slow-and-simple method is complete. Try decoding to see how much information was lost.");
		theMainApp.decodeItem.setEnabled(true);
	}

	public void encodeFast() {  
		ImageAnalyzer.ElapsedTime efTime = theMainApp.new ElapsedTime();
		efTime.startTiming();
		// Loop through the elements of L.
		Iterator LIter = L.iterator();
		int s = blockSize;
		while (LIter.hasNext()) {
			BlockCountPair currPair = (BlockCountPair) LIter.next();
			Color repColor = new Color ((currPair.b.Br + 1/2)* s,(currPair.b.Bg + 1/2)* s, (currPair.b.Bb + 1/2)* s);
			int bestIndex = repColor.findIndexOfClosestColor(palette);
			H.putAndMore(currPair.b, bestIndex);
		}
		// Now scan the source image.
		encodedPixels = new int[h][w] ;	// to store the value each pixel in the image is encoded to.
		// Scan the Color version of the original image.
		for (int i=0; i<h; i++) {
			for (int j=0; j<w; j++) {
				// Get the BlockCountPair from the list and compute its representative color
				Color c = colorImg[i][j];
				Block currBlock = new Block(c);
				int colorTableIndex = H.getAndMore(currBlock).val;
				encodedPixels[i][j] = colorTableIndex;
			}
		}
		efTime.reportTime(); 
		printStats();// Print the stats after encoding.
		System.out.println("Encoding with the fast method is complete. Try decoding to see how much information was lost.");
		theMainApp.decodeItem.setEnabled(true);
	}

	public class Color {
		int r, g, b;

		Color(int r, int g, int b) {
			this.r = r; this.g = g; this.b = b;    		
		}
		
		public int toInt() {
			return (((r<<8) + g) << 8)+r;
		}
		
		public String toString() {
			return "("+r+","+g+","+b+")";
		}

		double euclideanDistance(Color c2) {

			if (c2==null) { return Double.MAX_VALUE; }
			int dr = r-c2.r;
			int dg = g-c2.g;
			int db = b-c2.b;
			int sum_sq = dr*dr + dg*dg + db*db;
			return Math.sqrt(sum_sq);
		}
		int findIndexOfClosestColor(Color[] palette){
			double dist_so_far = 256.0 * 2; // something greater than 255 * cube root of 3.
			int best_index_so_far = 0;
			for (int i=0; i<palette.length; i++) {
				double this_distance = euclideanDistance(palette[i]);
				if (this_distance < dist_so_far) {
					dist_so_far = this_distance;
					best_index_so_far = i;
				}
			}
			return best_index_so_far;
		}
	}


	public class Block extends Object {


		int Br, Bg, Bb; // ADDED BY SLT.
		public Block(int Br, int Bg, int Bb) {
			this.Br = Br; this.Bg = Bg; this.Bb = Bb;
		}
		public Block(Color c) {
			Br = c.r / blockSize; Bg = c.g / blockSize; Bb = c.b / blockSize;
		}

		public int hashCode() {
			if (hashFunctionChoice == 1) {
				return h1(this);
			} else if (hashFunctionChoice == 2) {
				return h2(this);
			} else if (hashFunctionChoice == 3) {
				return h3(this);
			} else {
				return -1; // This should never happen.
			}
		}
		@Override
		public boolean equals(Object o2) {
			Block b2 = (Block)o2;
			return Br == b2.Br && Bg == b2.Bg && Bb == b2.Bb;
		}
		public String toString() {
			return "("+Br+","+Bg+","+Bb+")";
		}
	}

	public int h1(Block b) {
		return b.Bb ^ b.Bg ^ b.Br; 
	}

	public int h2(Block b) {
		return 1024 * b.Br + 32 * b.Bg + b.Bb; 
	}

	public int h3(Block b) {
		return b.toString().hashCode();
	}

/*
 * The decode method is done for you.
 * Once you have either of the encoding methods working, you'll be able to run
 * this one from the menu and see how the image looks after compression.
 */
public void decode() {
	// Scan through the encodedPixels and look up each one's color, and put it into the image buffer.
	for (int i=0; i<h; i++ ) {
		for (int j=0; j<w; j++) {
			int pixelsColorIndex = encodedPixels[i][j];
			Color c = palette[pixelsColorIndex];
		    theMainApp.putPixel(theMainApp.biWorking, i, j, c);
		}
	}
	theMainApp.repaint();
	error = theMainApp.computeError(colorImg, theMainApp.biWorking);
	System.out.println("Average per-pixel error: "+error);
}
}
