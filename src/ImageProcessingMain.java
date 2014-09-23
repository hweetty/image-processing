import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.LinkedList;

/**
 * Processes an image
 * @author G Ridout
 * @author Jerry Yu
 * @version October 2012
 */
public class ImageProcessingMain extends JFrame
{
	final private String []FILE_NAMES = {
		"../res/shapes.png",
		"../res/morerectangles.png",
		"../res/hardshapes.png",
		"../res/waterlilies.png",
		"../res/uber.png",
		"../res/ultra.png"};
	final private String DEFAULT_FILE_NAME = FILE_NAMES[4];
	
	// The main drawing area
	private PicturePanel pictureArea;
	final private int MIN_SIZE = 200;

	// Keeps track of the Picture objects
	private Picture currentPicture;
	private LinkedList<Picture> previousPictures;

	// Used to show Pixel data
	private JLabel pixelInfo;
	
	// Undo option in main menu
	private JMenuItem undoMenuItem;

	public ImageProcessingMain()
	{
		// Set up the main window and the panel for the Picture
		setTitle("Image processing with shape detection");
		setLocation(250, 20);
		setLayout(new BorderLayout());
		
		// Set up list of previous pictures for undo
		previousPictures = new LinkedList<Picture>();
		
		// Load up a default Picture and the panel for the Picture
		currentPicture = new Picture(DEFAULT_FILE_NAME, this);
		pictureArea = new PicturePanel();
		add(pictureArea, BorderLayout.CENTER);
		refresh();

		// Add in a Menu
		// For each menu item we need to add in an ActionListener
		// that tells the computer what to do when the menu item is selected
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem loadMenuItem = new JMenuItem("Load...", 'L');
		loadMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event)
			{
				loadNewImage();
			}
		});

		undoMenuItem = new JMenuItem("Undo");
		undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				InputEvent.CTRL_MASK));
		undoMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event)
			{
				if (!previousPictures.isEmpty())
					currentPicture = previousPictures.removeLast();
				if (previousPictures.isEmpty())
					undoMenuItem.setEnabled(false);
				repaint();
			}
		});
		undoMenuItem.setEnabled(false);

		JMenuItem quitMenuItem = new JMenuItem("Exit", 'x');
		quitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event)
			{
				System.exit(0);
			}
		});

		fileMenu.add(loadMenuItem);
		fileMenu.add(undoMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(quitMenuItem);
		menuBar.add(fileMenu);

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		JMenuItem aboutMenuItem = new JMenuItem("About...", 'A');
		aboutMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event)
			{
				JOptionPane.showMessageDialog(pictureArea,
						"Image Processor by Ridout and Jerry Yu\n\u00a9 2012",
						"About Image Processer",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});

		helpMenu.add(aboutMenuItem);
		menuBar.add(helpMenu);
		setJMenuBar(menuBar);
		
		// Set up a Panel of buttons at the bottom of the window
		// Each button is used to modify the picture
		// Each button is set up for the undo feature
		JPanel buttonPanel = new JPanel();
		JButton flip = new JButton("Flip Picture");
		flip.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event)
			{
				if (currentPicture == null)
					return;
				// Make a copy of the current Picture (for undo)
				saveCurrentPicture();
				currentPicture.flip();
				repaint();
			}
		});
		

		JButton negative = new JButton("Negative");
		negative.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event)
			{
				if (currentPicture == null)
					return;
				// Make a copy of the current Picture (for undo)
				saveCurrentPicture();
				currentPicture.negative();
				repaint();
			}
		});
		JButton bAndW = new JButton("B & W");
		bAndW.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event)
			{
				if (currentPicture == null)
					return;
				// Make a copy of the current Picture (for undo)
				saveCurrentPicture();
				currentPicture.blackAndWhite();
				repaint();
			}
		});
		JButton blur = new JButton("Blur");
		blur.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event)
			{
				if (currentPicture == null)
					return;
				// Make a copy of the current Picture (for undo)
				saveCurrentPicture();
				currentPicture.blur();
				repaint();
			}
		});

		JButton sharpen = new JButton("Sharpen");
		sharpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event)
			{
				if (currentPicture == null)
					return;
				// Make a copy of the current Picture (for undo)
				saveCurrentPicture();
				currentPicture.sharpen();
				repaint();
			}
		});
		
			JButton shrink = new JButton("Shrink");
		shrink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event)
			{
				if (currentPicture == null)
					return;
				// Make a copy of the current Picture (for undo)
				saveCurrentPicture();
				currentPicture.shrink();
				repaint();
			}
		});
		JButton somethingElse = new JButton("Snape");
		somethingElse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event)
			{
				if (currentPicture == null)
					return;
				// Make a copy of the current Picture (for undo)
				saveCurrentPicture();
				
				currentPicture.shapes();
				repaint();
			}
		});
	
		// Add all of the buttons to the panel
		buttonPanel.add(flip);
		buttonPanel.add(negative);
		buttonPanel.add(bAndW);
		buttonPanel.add(sharpen);
		buttonPanel.add(blur);
		buttonPanel.add(shrink);
		buttonPanel.add(somethingElse);
		add(buttonPanel, BorderLayout.SOUTH);

		// Set up a Panel with the current Pixel info at the top of the window
		JPanel topPanel = new JPanel();
		pixelInfo = new JLabel("Pixel: ");
		pixelInfo.setFont(new Font("Courier New", Font.PLAIN, 14));
		topPanel.add(pixelInfo);
		add(topPanel, BorderLayout.NORTH);
	}
	
	private void saveCurrentPicture()
	{
		previousPictures.addLast(new Picture(currentPicture));
		// Only allow up to 5 undo steps (to save on memory)
		if (previousPictures.size() > 5)
			previousPictures.removeFirst();
		undoMenuItem.setEnabled(true);
	}

	public void loadNewImage()
	{
		// Set up a FileChooser Dialog to select the image file to load
		JFileChooser fileChooser = new JFileChooser(".");
		FileFilter filter = new FileNameExtensionFilter(
				"Image files (jpg, gif etc.)", "jpg", "jpeg", "gif", "png");
		fileChooser.setFileFilter(filter);

		// Show the dialog and if not cancel load up the new file
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			File file = fileChooser.getSelectedFile();
			currentPicture = new Picture(file.getAbsolutePath(), this);
			previousPictures = new LinkedList<Picture>();

			// Recreate the picture area to match the size of the image
			pictureArea.setPreferredSize(new Dimension(Math.max(
					currentPicture.getWidth(), MIN_SIZE), Math.max(
					currentPicture.getHeight(), MIN_SIZE)));
			this.pack();
			refresh();
		}
	}

	public void refresh()
	{
		pictureArea.paintImmediately(new Rectangle(0, 0,
				pictureArea.getWidth(), pictureArea.getHeight()));
	}

	/** Inner class for the drawing area for the Picture
	 */
	private class PicturePanel extends JPanel
	{
		public PicturePanel()
		{
			if (currentPicture == null)
				this.setPreferredSize(new Dimension(MIN_SIZE, MIN_SIZE));
			else
				this.setPreferredSize(new Dimension(Math.max(
						currentPicture.getWidth(), MIN_SIZE), Math.max(
						currentPicture.getHeight(), MIN_SIZE)));

			// Used to show the data for the current pixel
			this.addMouseMotionListener(new MouseMotionHandler());
			this.addMouseListener(new MouseHandler());
		}

		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			if (currentPicture == null)
			{
				g.setFont(new Font("Arial", Font.PLAIN, 20));
				g.drawString("Please load an Image", 60, 75);
			}
			else
				currentPicture.draw(g, 0, 0);
		} // paint component method

	}

	// Inner class to handle mouse events
	private class MouseHandler extends MouseAdapter
	{
		public void mousePressed(MouseEvent event)
		{
			if (currentPicture != null)
			{
				// Did we agree to do floodFill ????
				saveCurrentPicture();
				currentPicture.floodFill(event.getX(), event.getY());
				currentPicture.updateImage();
				repaint();
			}
		}
	}

	// Inner Class to handle mouse movement
	private class MouseMotionHandler extends MouseMotionAdapter
	{
		public void mouseMoved(MouseEvent event)
		{
			if (currentPicture != null)
			{
				pixelInfo.setText("Pixel: "
						+ currentPicture.getPixel(event.getX(), event.getY()));

			}
		}
	}
	
	/** Creates and shows the Image Processing Main Window
	 * 
	 * @param args not used
	 */
	public static void main(String[] args)
	{
		ImageProcessingMain mainWindow = new ImageProcessingMain();
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.pack();
		mainWindow.setVisible(true);
	}
}
