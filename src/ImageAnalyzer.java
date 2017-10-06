/*
 * ImageAnalyzer.java
 * Starter code for A4 Part II 
 * Most of your changes for Part II (B) of the assignment should NOT be in this file
 * but the companion file IndexedColorEncoder.java.  You only have to turn in this
 * file if you made changes to it.
 * 
 * See also the file CustomHashtables.java and its supporting files.
 * 
 * If you change this file, then add a comment here describing your changes, and 
 * "Changes by " with your name and student number.
 * +--------------------------------------------------------+
 * | (default: No changes, but describe any changes here.)  |
 * +--------------------------------------------------------+
 * CSE 373, University of Washington, Winter 2016.
 * 
 * Starter Code for CSE 373 Assignment 4, Part II.    Starter Code Version 2.0.
 * S. Tanimoto,  with contributions from J. Goh, Oct 21, 2014, and incorporating
 * suggestions from William H. Zahn, Feb. 5, 2016.
 * 
 */ 

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ImageAnalyzer extends JFrame implements ActionListener {
	public static ImageAnalyzer appInstance; // Used in main().

	// Your start-up image can be set up here:
	String startingImage = "Mt-Rainier.jpg";
	//String startingImage = "q3Image.png";
	//String startingImage = "Mt-Rainier-micro.jpg";
	//String startingImage = "Mt-Rainier-miniature.jpg";
	//String startingImage = "UW-Campus-1961.jpg";
	BufferedImage biTemp, biWorking, biFiltered; // These hold arrays of pixels.
	Graphics gOrig, gWorking; // Used to access the drawImage method.
	int w; // width of the current image.
	int h; // height of the current image.
	IndexedColorEncoder theIndexedColorEncoder; // Instance of the class that will do the
	// palette generation and image encoding.
	int maxDim; // Used to figure out how to scale up small images.
	int sizeThresh = 400; // Images with maxDim smaller than this will be scaled up.
	double scaleFactor; // Used especially when scaling up a small image.
	int s=1; // int version of the scaleFactor.
	int hMarginAdjust = 15; // Two constants used to adjust the window size. Not very important.
	int vMarginAdjust = 60;

	// Here are the Swing GUI components:
	JPanel viewPanel; // Where the image will be painted.
	JPopupMenu popup;
	JMenuBar menuBar;
	JMenu fileMenu, imageOpMenu, paletteMenu, encodeMenu, hashingMenu, helpMenu;
	JMenuItem loadImageItem, saveAsItem, exitItem;
	JMenuItem lowPassItem, highPassItem, photoNegItem, RGBThreshItem;
	JMenuItem createPItem2, createPItem4, createPItem16, createPItem256, selectBItem4, selectBItem8, selectBItem16;
	JMenuItem encodeSSItem, encodeFItem, decodeItem;
	JMenuItem hashFunctionItem1, hashFunctionItem2, hashFunctionItem3;
	JMenuItem chainingItem, linProbItem;
	JMenuItem aboutItem, helpItem;

	JFileChooser fileChooser; // For loading and saving images.

	// Some image manipulation data definitions that won't change...
	static LookupOp PHOTONEG_OP, RGBTHRESH_OP;
	static ConvolveOp LOWPASS_OP, HIGHPASS_OP;

	public static final float[] SHARPENING_KERNEL = { // sharpening filter kernel
			0.f, -1.f,  0.f,
			-1.f,  5.f, -1.f,
			0.f, -1.f,  0.f
	};

	public static final float[] BLURRING_KERNEL = {
			0.1f, 0.1f, 0.1f,    // low-pass filter kernel
			0.1f, 0.2f, 0.1f,
			0.1f, 0.1f, 0.1f
	};

	public ImageAnalyzer() { // Constructor for the application.
		setTitle("Image Analyzer"); 
		addWindowListener(new WindowAdapter() { // Handle any window close-box clicks.
			public void windowClosing(WindowEvent e) {System.exit(0);}
		});

		// Create the panel for showing the current image, and override its
		// default paint method to call our paintPanel method to draw the image.
		viewPanel = new JPanel(){public void paint(Graphics g) { paintPanel(g);}};
		add("Center", viewPanel); // Put it smack dab in the middle of the JFrame.

		// Create standard menu bar
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		fileMenu = new JMenu("File");
		imageOpMenu = new JMenu("Image Operations");
		paletteMenu = new JMenu("Palettes");
		encodeMenu = new JMenu("Encode");
		hashingMenu = new JMenu("Hashing");
		helpMenu = new JMenu("Help");
		menuBar.add(fileMenu);
		menuBar.add(imageOpMenu);
		menuBar.add(paletteMenu);
		menuBar.add(encodeMenu);
		menuBar.add(hashingMenu);
		menuBar.add(helpMenu);

		// Create the File menu's menu items.
		loadImageItem = new JMenuItem("Load image...");
		loadImageItem.addActionListener(this);
		fileMenu.add(loadImageItem);
		saveAsItem = new JMenuItem("Save as full-color PNG");
		saveAsItem.addActionListener(this);
		fileMenu.add(saveAsItem);
		exitItem = new JMenuItem("Quit");
		exitItem.addActionListener(this);
		fileMenu.add(exitItem);

		// Create the Image Operation menu items.
		lowPassItem = new JMenuItem("Convolve with blurring kernel");
		lowPassItem.addActionListener(this);
		imageOpMenu.add(lowPassItem);
		highPassItem = new JMenuItem("Convolve with sharpening kernel");
		highPassItem.addActionListener(this);
		imageOpMenu.add(highPassItem);
		photoNegItem = new JMenuItem("Photonegative");
		photoNegItem.addActionListener(this);
		imageOpMenu.add(photoNegItem);
		RGBThreshItem = new JMenuItem("RGB Thresholds at 128");
		RGBThreshItem.addActionListener(this);
		imageOpMenu.add(RGBThreshItem);

		// Create the Palette menu items.
		createPItem2 = new JMenuItem("Create Palette of Size 2");
		createPItem2.addActionListener(this);
		paletteMenu.add(createPItem2);
		createPItem4 = new JMenuItem("Create Palette of Size 4");
		createPItem4.addActionListener(this);
		paletteMenu.add(createPItem4);
		createPItem16 = new JMenuItem("Create Palette of Size 16");
		createPItem16.addActionListener(this);
		paletteMenu.add(createPItem16);
		createPItem256 = new JMenuItem("Create Palette of Size 256");
		createPItem256.addActionListener(this);
		paletteMenu.add(createPItem256);
		selectBItem4 = new JCheckBoxMenuItem("Set block size to 4x4x4", true);
		selectBItem4.addActionListener(this);
		paletteMenu.add(selectBItem4);
		selectBItem8 = new JCheckBoxMenuItem("Set block size to 8x8x8");
		selectBItem8.addActionListener(this);
		paletteMenu.add(selectBItem8);
		selectBItem16 = new JCheckBoxMenuItem("Set block size to 16x16x16");
		selectBItem16.addActionListener(this);
		paletteMenu.add(selectBItem16);

		// Create the Encode menu items.
		encodeSSItem = new JMenuItem("Encode: Slow and Simple");
		encodeSSItem.addActionListener(this);
		encodeMenu.add(encodeSSItem);
		encodeSSItem.setEnabled(false);

		encodeFItem = new JMenuItem("Encode: Fast");
		encodeFItem.addActionListener(this);
		encodeMenu.add(encodeFItem);
		encodeFItem.setEnabled(false);

		decodeItem = new JMenuItem("Decode");
		decodeItem.addActionListener(this);
		encodeMenu.add(decodeItem);
		decodeItem.setEnabled(false);

		// Create the Hashing menu items.
		hashFunctionItem1 = new JCheckBoxMenuItem("Use Hash Function H1", true);
		hashFunctionItem1.addActionListener(this);
		hashingMenu.add(hashFunctionItem1);

		hashFunctionItem2 = new JCheckBoxMenuItem("Use Hash Function H2");
		hashFunctionItem2.addActionListener(this);
		hashingMenu.add(hashFunctionItem2);

		hashFunctionItem3 = new JCheckBoxMenuItem("Use Hash Function H3");
		hashFunctionItem3.addActionListener(this);
		hashingMenu.add(hashFunctionItem3);

		chainingItem = new JCheckBoxMenuItem("Use custom hashtable class with separate chaining.");
		chainingItem.addActionListener(this);
		hashingMenu.add(chainingItem);
		chainingItem.setEnabled(true);
		chainingItem.setSelected(true);

		linProbItem = new JCheckBoxMenuItem("Use custom hashtable class with linear probing.");
		linProbItem.addActionListener(this);
		hashingMenu.add(linProbItem);
		linProbItem.setEnabled(false);

		// Create the Help menu's item.
		aboutItem = new JMenuItem("About");
		aboutItem.addActionListener(this);
		helpMenu.add(aboutItem);
		helpItem = new JMenuItem("Help");
		helpItem.addActionListener(this);
		helpMenu.add(helpItem);

		// Initialize the image operators, if this is the first call to the constructor:
		if (PHOTONEG_OP==null) {
			byte[] lut = new byte[256];
			for (int j=0; j<256; j++) {
				lut[j] = (byte)(256-j); 
			}
			ByteLookupTable blut = new ByteLookupTable(0, lut); 
			PHOTONEG_OP = new LookupOp(blut, null);
		}
		if (RGBTHRESH_OP==null) {
			byte[] lut = new byte[256];
			for (int j=0; j<256; j++) {
				lut[j] = (byte)(j < 128 ? 0: 200);
			}
			ByteLookupTable blut = new ByteLookupTable(0, lut); 
			RGBTHRESH_OP = new LookupOp(blut, null);
		}
		if (LOWPASS_OP==null) {
			float[] data = BLURRING_KERNEL;
			LOWPASS_OP = new ConvolveOp(new Kernel(3, 3, data),
					ConvolveOp.EDGE_NO_OP,
					null);
		}
		if (HIGHPASS_OP==null) {
			float[] data = SHARPENING_KERNEL;
			HIGHPASS_OP = new ConvolveOp(new Kernel(3, 3, data),
					ConvolveOp.EDGE_NO_OP,
					null);
		}
		loadImage(startingImage); // Read in the pre-selected starting image.
		setVisible(true); // Display it.

		theIndexedColorEncoder = new IndexedColorEncoder(this);
	}

	/*
	 * Given a path to a file on the file system, try to load in the file
	 * as an image.  If that works, replace any current image by the new one.
	 * Re-make the biFiltered buffered image, too, because its size usually
	 * needs to be different to match that of the new image.
	 */
	public void loadImage(String filename) {
		try {
			biTemp = ImageIO.read(new File(filename));
			w = biTemp.getWidth();
			h = biTemp.getHeight();
			viewPanel.setSize(w,h);
			biWorking = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			gWorking = biWorking.getGraphics();
			gWorking.drawImage(biTemp, 0, 0, null);
			biFiltered = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			scaleFactor = 1.0; // re-initialize.
			maxDim = Math.max(w, h);
			if (maxDim < sizeThresh) { scaleFactor = 600.0 / maxDim; }
			s = (int) Math.ceil(scaleFactor);

			setMinimumSize(new Dimension(s*w + hMarginAdjust, s*h + vMarginAdjust));
			pack(); // Lay out the JFrame and set its size.
			repaint();
			encodeSSItem.setEnabled(false);
			encodeFItem.setEnabled(false);
			decodeItem.setEnabled(false);
		} catch (IOException e) {
			System.out.println("Image could not be read: "+filename);
			System.exit(1);
		}
	}

	/* Menu handlers
	 */
	void handleFileMenu(JMenuItem mi){
		//System.out.println("A file menu item was selected.");
		if (mi==loadImageItem) {
			File loadFile = new File("image-to-load.png");
			if (fileChooser==null) {
				fileChooser = new JFileChooser();
				//fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
				fileChooser.setCurrentDirectory(new java.io.File("."));

				fileChooser.setSelectedFile(loadFile);
				fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", new String[] { "JPG", "JPEG", "GIF", "PNG" }));
			}
			int rval = fileChooser.showOpenDialog(this);
			if (rval == JFileChooser.APPROVE_OPTION) {
				loadFile = fileChooser.getSelectedFile();
				loadImage(loadFile.getPath());
			}
		}
		if (mi==saveAsItem) {
			File saveFile = new File("savedimage.png");
			fileChooser = new JFileChooser();
			fileChooser.setSelectedFile(saveFile);
			int rval = fileChooser.showSaveDialog(this);
			if (rval == JFileChooser.APPROVE_OPTION) {
				saveFile = fileChooser.getSelectedFile();
				// Save the current image in PNG format, to a file.
				try {
					ImageIO.write(biWorking, "png", saveFile);
				} catch (IOException ex) {
					System.out.println("There was some problem saving the image.");
				}
			}
		}
		if (mi==exitItem) { this.setVisible(false); System.exit(0); }
	}

	void handleEditMenu(JMenuItem mi){
		System.out.println("An edit menu item was selected.");
	}

	void handleImageOpMenu(JMenuItem mi){
		//System.out.println("An imageOp menu item was selected.");
		if (mi==lowPassItem) { applyOp(LOWPASS_OP); }
		else if (mi==highPassItem) { applyOp(HIGHPASS_OP); }
		else if (mi==photoNegItem) { applyOp(PHOTONEG_OP); }
		else if (mi==RGBThreshItem) { applyOp(RGBTHRESH_OP); }
		repaint();
	}

	void handlePaletteMenu(JMenuItem mi){
		//System.out.println("A palette menu item was selected.");
		if (mi==createPItem2) { theIndexedColorEncoder.buildPalette(2); }
		else if (mi==createPItem4) { theIndexedColorEncoder.buildPalette(4); }
		else if (mi==createPItem16) { theIndexedColorEncoder.buildPalette(16); }
		else if (mi==createPItem256) { theIndexedColorEncoder.buildPalette(256); }
		else if (mi==selectBItem4) { 
			biSetChecked(mi);
			theIndexedColorEncoder.setBlockSize(4); }
		else if (mi==selectBItem8) {
			biSetChecked(mi);
			theIndexedColorEncoder.setBlockSize(8); }
		else if (mi==selectBItem16) { 
			biSetChecked(mi);
			theIndexedColorEncoder.setBlockSize(16); }
	}

	void biSetChecked(JMenuItem mi) {
		selectBItem4.setSelected(false);
		selectBItem8.setSelected(false);
		selectBItem16.setSelected(false);
		mi.setSelected(true);
	}
	void handleEncodeMenu(JMenuItem mi){
		//System.out.println("An encode menu item was selected.");
		if (mi==encodeSSItem){ theIndexedColorEncoder.encodeSlowAndSimple(); }
		else if (mi==encodeFItem) { theIndexedColorEncoder.encodeFast(); }
		else if (mi==decodeItem) { handleDecode(); }
	}

	void handleHashingMenu(JMenuItem mi){
		//System.out.println("A hashing menu item was selected.");
		if (mi==hashFunctionItem1) { 
			hiSetChecked(mi);
			theIndexedColorEncoder.setHashFunctionChoice(1); }
		else if (mi==hashFunctionItem2) { 
			hiSetChecked(mi);
			theIndexedColorEncoder.setHashFunctionChoice(2); }
		else if (mi==hashFunctionItem3) { 
			hiSetChecked(mi);
			theIndexedColorEncoder.setHashFunctionChoice(3); }
	}
	void hiSetChecked(JMenuItem mi) {
		hashFunctionItem1.setSelected(false);
		hashFunctionItem2.setSelected(false);
		hashFunctionItem3.setSelected(false);
		mi.setSelected(true);
	}

	void handleHelpMenu(JMenuItem mi){
		//System.out.println("A help menu item was selected.");
		if (mi==aboutItem) {
			System.out.println("About: Well this is my program.");
			JOptionPane.showMessageDialog(this,
					"Image Analyzer, Starter-Code Version.",
					"About",
					JOptionPane.PLAIN_MESSAGE);
		}
		else if (mi==helpItem) {
			System.out.println("In case of panic attack, select File: Quit.");
			JOptionPane.showMessageDialog(this,
					"To load a new image, choose File: Load image...\nFor anything else, just try different things.",
					"Help",
					JOptionPane.PLAIN_MESSAGE);
		}
	}

	/*
	 * Used by Swing to set the size of the JFrame when pack() is called.
	 */
	public Dimension getPreferredSize() {
		return new Dimension(w, h+50); // Leave some extra height for the menu bar.
	}

	public void paintPanel(Graphics g) {
		//g.drawImage(biWorking, 0, 0, null);
		g.drawImage(biWorking, 0, 0, s*w, s*h, null);
	}

	public void applyOp(BufferedImageOp operation) {
		operation.filter(biWorking, biFiltered);
		gWorking.drawImage(biFiltered, 0, 0, null);
	}

	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource(); // What Swing object issued the event?
		if (obj instanceof JMenuItem) { // Was it a menu item?
			JMenuItem mi = (JMenuItem)obj; // Yes, cast it.
			JPopupMenu pum = (JPopupMenu)mi.getParent(); // Get the object it's a child of.
			JMenu m = (JMenu) pum.getInvoker(); // Get the menu from that (popup menu) object.
			//System.out.println("Selected from the menu: "+m.getText()); // Printing this is a debugging aid.

			if (m==fileMenu)    { handleFileMenu(mi);    return; }  // Handle the item depending on what menu it's from.
			if (m==imageOpMenu) { handleImageOpMenu(mi); return; }
			if (m==paletteMenu) { handlePaletteMenu(mi); return; }
			if (m==encodeMenu)  { handleEncodeMenu(mi);  return; }
			if (m==hashingMenu) { handleHashingMenu(mi); return; }
			if (m==helpMenu)    { handleHelpMenu(mi);    return; }
		} else {
			System.out.println("Unhandled ActionEvent: "+e.getActionCommand());
		}
	}
	// We use this to put a color into a pixel of a BufferedImage object.
	void putPixel(BufferedImage bi, int i, int j, IndexedColorEncoder.Color c) {
		int rgb = (c.r << 16) | (c.g << 8) | c.b; // pack 3 bytes into a word.
		bi.setRGB(j,  i, rgb);
	}

	public void handleDecode() {
		IndexedColorEncoder.Color[][] originalPixels = storeCurrentPixels(biWorking);
		// TODO
		// Add your code here to determine RGB values for each pixel from the encoded information, and
		// put the RGB information into biWorking.
		// Use the putPixel function defined below to store a color into a pixel
		theIndexedColorEncoder.decode();
		double averageEncodingError = computeError(originalPixels, biWorking);
	}

	// Returns an array of Colors based on the pixels from a BufferedImage
	IndexedColorEncoder.Color[][] storeCurrentPixels(BufferedImage bi) {
		IndexedColorEncoder.Color[][] pixels = new IndexedColorEncoder.Color[h][w];
		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				int rgb = bi.getRGB(col, row);

				int red = (rgb & 0x00ff0000) >> 16;
			int green = (rgb & 0x0000ff00) >> 8;
		int blue = rgb & 0x000000ff;

		pixels[row][col] = theIndexedColorEncoder.new Color(red, green, blue);
			}
		}
		return pixels;
	}

	// Computes the average pixel encoding error between a pixel array and the pixels in a BufferedImage
	double computeError(IndexedColorEncoder.Color[][] pixels, BufferedImage bi) {
		double totalError = 0.0;
		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				int rgb = bi.getRGB(col, row);

				int red = (rgb & 0x00ff0000) >> 16;
			int green = (rgb & 0x0000ff00) >> 8;
		int blue = rgb & 0x000000ff;

		totalError += pixels[row][col].euclideanDistance(theIndexedColorEncoder.new Color(red, green, blue));
			}
		}
		return totalError / (h * w);
	}
	class ElapsedTime {
		long startTime;
		String msg;
		void startTiming() {
			startTime = System.nanoTime();
		}
		void reportTime() {
			long difference = System.nanoTime() - startTime;
			
			msg = String.format("%d microseconds",
					TimeUnit.NANOSECONDS.toMicros(difference));
			System.out.println(msg);
		}
	}

	/* This main method can be used to run the application. */
	public static void main(String s[]) {
		appInstance = new ImageAnalyzer();
	}
}
