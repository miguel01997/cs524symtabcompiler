// Copyright 1997-1998 Invisible Software, Inc.

package invisible.jacc.gen;

import java.awt.*;
import java.awt.event.*;

import java.io.CharArrayWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;

import invisible.jacc.parse.ErrorOutput;
import invisible.jacc.parse.ErrorOutputWriter;
import invisible.jacc.parse.ParserTable;
import invisible.jacc.parse.ProductInfo;
import invisible.jacc.parse.ScannerTable;

import invisible.jacc.utilg.CheckboxPatch;
import invisible.jacc.utilg.MessageDialog;
import invisible.jacc.utilg.StatusLabel;
import invisible.jacc.utilg.TextViewer;


/*->

GenGUIPanel is an AWT component that invokes the parser generator.  It allows
the user to enter the Jacc filename and select other options.  It also
displays the parser generator's output to the user.

->*/


public class GenGUIPanel extends Panel implements GenObserver, ActionListener
{
	
	// ----- Variables -----
	
	// Parent Frame for dialog boxes
	
	private Frame _parent = null;
	
	// Directory to use in file dialog, initialize to current directory or null
	
	private String _lastDir = System.getProperty ("user.dir");
	
	// Font size to use
	
	private int _fontSize = 14;
	
	
	
	
	// ----- Child components for constructing the input panel -----
	
	// The exit button on the input panel
	
	private Button _inExitButton = new Button ("Exit");
	
	// The button used to begin generation
	
	private Button _generateButton = new Button ("Generate");
	
	// The button used to view output
	
	private Button _outputButton = new Button ("View Output");
	
	// The button used to reset all input fields
	
	private Button _resetButton = new Button ("Reset");
	
	// The button used to browse for the filename
	
	private Button _browseButton = new Button ("Browse");
	
	// Checkbox to enable scanner table generation
	
	private Checkbox _scannerTableCheckbox
		= new CheckboxPatch ("Generate scanner table", null, true);
	
	// Checkbox to enable parser table generation
	
	private Checkbox _parserTableCheckbox
		= new CheckboxPatch ("Generate parser table", null, true);
	
	// Checkbox to create generated file
	
	private Checkbox _genFileCheckbox
		= new CheckboxPatch ("Create generated file  (Filename.gen)", null, false);
	
	// Checkbox to create Java source files
	
	private Checkbox _javaSourceCheckbox
		= new CheckboxPatch ("Create Java source files  (XxxxxxTable.java)", null, true);
	
	// Checkbox to write output messages to file
	
	private Checkbox _outFileCheckbox
		= new CheckboxPatch ("Write output messages to file  (Filename.out)", null, false);
	
	// Checkbox to enable verbose output
	
	private Checkbox _verboseCheckbox
		= new CheckboxPatch ("Write verbose output", null, false);
	
	// The filename field label
	
	private Label _filenameLabel = new Label ("Grammar specification file:");
	
	// The filename text field
	
	private TextField _filenameText = new TextField (60);
	
	
	
	
	// ----- Variables for managing the parser generator front end -----
	
	// The parser generator front end
	
	private GenFrontEnd _genFrontEnd = null;
	
	// The window used to display progress
	
	private GenGUIProgressWindow _progressWindow = null;
	
	// The writer used to capture output messages
	
	private CharArrayWriter _outWriter = null;
	
	
	
	
	// ----- Child components for constructing the output panel -----
	
	// The exit button on the output panel
	
	private Button _outExitButton = new Button ("Exit");
	
	// The button used to view input
	
	private Button _inputButton = new Button ("View Input");
	
	// The output field label
	
	private Label _outputLabel = new Label ("");
	
	// The output text area
	
	private TextViewer _outputText = new TextViewer (15, 70);
	
	// The summary label
	
	private StatusLabel _summaryLabel = new StatusLabel ("");
	
	
	
	
	// ----- Boilerplate GridBag code -----
	
	
	// addToGridBag - Add a Component to a Container with a gridbag layout
	//
	// Parameters:
	//	cont = Container object, which must have a GridBagLayout layout manager
	//	comp = Component to add to the container
	//	others = See class java.io.GridBagConstraints
	//
	// Return value:
	//	The function returns the comp argument
	
	private static final int _C = GridBagConstraints.CENTER;
	private static final int _E = GridBagConstraints.EAST;
	private static final int _N = GridBagConstraints.NORTH;
	private static final int _NE = GridBagConstraints.NORTHEAST;
	private static final int _NW = GridBagConstraints.NORTHWEST;
	private static final int _S = GridBagConstraints.SOUTH;
	private static final int _SE = GridBagConstraints.SOUTHEAST;
	private static final int _SW = GridBagConstraints.SOUTHWEST;
	private static final int _W = GridBagConstraints.WEST;
	private static final int _B = GridBagConstraints.BOTH;
	private static final int _H = GridBagConstraints.HORIZONTAL;
	private static final int _X = GridBagConstraints.NONE;
	private static final int _V = GridBagConstraints.VERTICAL;
	private static final int _Z = GridBagConstraints.REMAINDER;
	private static final int _R = GridBagConstraints.RELATIVE;
	
	private static Component addToGridBag (Container cont, Component comp,
		int anchor, int fill, int gridx, int gridy, int gridwidth, int gridheight,
		int insetsTop, int insetsLeft, int insetsBottom, int insetsRight,
		int ipadx, int ipady, double weightx, double weighty)
	{
		
		// Get the layout manager, which must be a GridBagLayout
		
		GridBagLayout layout = (GridBagLayout) cont.getLayout ();
		
		// Create a constraints object
		
		GridBagConstraints constraints = new GridBagConstraints ();
		
		// Set the constraints from the function arguments
		
		constraints.anchor = anchor;
		constraints.fill = fill;
		constraints.gridx = gridx;
		constraints.gridy = gridy;
		constraints.gridwidth = gridwidth;
		constraints.gridheight = gridheight;
		constraints.insets = new Insets (insetsTop, insetsLeft, insetsBottom, insetsRight);
		constraints.ipadx = ipadx;
		constraints.ipady = ipady;
		constraints.weightx = weightx;
		constraints.weighty = weighty;
		
		// Specify constraints for this component
		
		layout.setConstraints (comp, constraints);
		
		// Add the component to the container
		
		cont.add (comp);
		
		// Return the component object
		
		return comp;
	}
	
	
	
	
	// ----- Boilerplate window positioning code -----
	
	
	// Position a child Window relative to a parent Window
	//
	// Parameters:
	//	child = child Window
	//	parent = parent Window
	//	others = see below
	//
	// This function works by defining an "anchor point" on each of the two
	// windows.  The coordinates of the anchor point are defined by offsets
	// relative to the window's upper left corner.  Each offset is computed
	// as the sum of two parts: a double that gives an offset as a fraction
	// of the window's size, and an int that gives an absolute offset in pixels.
	// For example, the x coordinate of the child window's anchor point is
	// equal to the x coordinate of the child window's upper left corner,
	// plus cfrx times the width of the child window, plus cofx.
	//
	// The position of the child window is set so that the child window's
	// anchor point coincides with the parent window's anchor point.
	// For example, setting cfrx=cfry=pfrx=pfry=0.5 and cofx=cofy=pofx=pofy=0
	// will center the child window over the parent window.  Likewise, setting
	// cfrx=cfry=pfrx=pfry=0.0 and cofx=cofy=0 and pofx=pofy=20 will display
	// the child window's upper left corner 20 pixels below and to the right of
	// the parent window's upper left corner.
	//
	// If superimposing the anchor points would make part of the child window
	// appear off-screen, the position of the child window is adjusted so that
	// the entire child window is on-screen.  If the child window is wider than
	// the screen, the left edge of the child window is set to the left edge of
	// the screen.  Likewise, if the child window is higher than the screen,
	// the top of the child window is set to the top of the screen.
	
	private static void setChildPos (
						Window child, double cfrx, double cfry, int cofx, int cofy,
						Window parent, double pfrx, double pfry, int pofx, int pofy )
	{
		
		// Calculate the desired child window position
		
		int x = (int)Math.rint ((double)(parent.location().x)
								+ (((double)pofx) + pfrx * (double)parent.getSize().width)
								- (((double)cofx) + cfrx * (double)child.getSize().width) );
		
		int y = (int)Math.rint ((double)(parent.location().y)
								+ (((double)pofy) + pfry * (double)parent.getSize().height)
								- (((double)cofy) + cfry * (double)child.getSize().height) );
		
		// Make sure the child window is on-screen
		
		x = Math.max (0, Math.min (x, child.getToolkit().getScreenSize().width
									  - child.getSize().width ));
		
		y = Math.max (0, Math.min (y, child.getToolkit().getScreenSize().height
									  - child.getSize().height ));
		
		// Now move the child to the specified location
		
		child.setLocation (x, y);
		
		return;
	}
	
	
	

	// ----- Constructor -----
	
	
	// Create the parser generator component.
	//
	// The parent parameter is the top-level window that contains this panel.
	// This is used as the parent for any dialog boxes that we put up.
	//
	// The fontSize parameter is the desired font point size.  A value of 14 is
	// recommended.
	
	public GenGUIPanel (Frame parent, int fontSize)
	{
		super ();
		
		// Validate the arguments
		
		if (parent == null || fontSize < 1)
		{
			throw new IllegalArgumentException ("GenGUIPanel.GenGUIPanel");
		}
		
		// Save the parent frame
		
		_parent = parent;
		
		// Save the font size
		
		_fontSize = fontSize;
		
		// Add event listeners
		
		_inExitButton.addActionListener (this);
		_outExitButton.addActionListener (this);
		_resetButton.addActionListener (this);
		_browseButton.addActionListener (this);
		_outputButton.addActionListener (this);
		_inputButton.addActionListener (this);
		_generateButton.addActionListener (this);
		_filenameText.addActionListener (this);
		
		// Vertical and horizontal gaps for component spacing
		
		int vgap = 15;
		int hgap = 10;
		
		// Set our fonts
		
		this.setFont (new Font ("Dialog", Font.PLAIN, _fontSize));
		_filenameText.setFont (new Font ("Courier", Font.PLAIN, _fontSize));
		_outputText.setFont (new Font ("Courier", Font.PLAIN, _fontSize));
		
		// Set our colors
		
		this.setBackground (Color.lightGray);
		_filenameText.setBackground (Color.white);
		_outputText.setBackground (Color.white);
		
		// Our layout manager is a card layout, so we can switch between
		// the input and output
		
		setLayout (new CardLayout ());
		
		
		
		
		// Panel for the input screen.
		// It is layed out as a gridbag, with one column.
		// There is x-stretch and y-stretch.
		// Since none of the elements in this panel have y-stretch, y-weights
		// are used to distribute extra space into the gaps between elements.
		// Insets create a border around this panel and gaps between elements.
		
		Panel inputPanel = new Panel ();
		inputPanel.setLayout (new GridBagLayout());
		
		// Add the filename label
		
		addToGridBag (inputPanel, _filenameLabel, _SW, _H, _R, _R, _Z, 1,
					  vgap, hgap, 0, hgap, 0, 0, 1.0, 0.0);
		
		// This panel will hold the filename and browse button.
		// It is layed out as a gridbag, with one row.
		// All the x-stretch is allocated to the filename text field.
		// There is no y-stretch.
		// Insets create a gap between the text field and the browse button.
		
		Panel filenamePanel = new Panel ();
		filenamePanel.setLayout (new GridBagLayout ());
		
		// Add the filename text field
		
		addToGridBag (filenamePanel, _filenameText, _C, _H, _R, _R, _R, 1,
					  0, 0, 0, hgap, 0, 0, 1.0, 0.0);
		
		// Add the filename browse button
		
		addToGridBag (filenamePanel, _browseButton, _C, _X, _R, _R, _Z, 1,
					  0, 0, 0, 0, hgap, 0, 0.0, 0.0);
		
		// Now add the filename panel as a row in the input panel
		
		addToGridBag (inputPanel, filenamePanel, _SW, _H, _R, _R, _Z, 1,
					  vgap/10, hgap, 0, hgap, 0, 0, 1.0, 0.0);
		
		// Add the scanner table checkbox
		
		addToGridBag (inputPanel, _scannerTableCheckbox, _SW, _X, _R, _R, _Z, 1,
					  vgap, hgap, 0, hgap, 0, 0, 1.0, 1.0);
		
		// Add the parser table checkbox
		
		addToGridBag (inputPanel, _parserTableCheckbox, _W, _X, _R, _R, _Z, 1,
					  0, hgap, 0, hgap, 0, 0, 1.0, 0.0);
		
		// Add the generated file checkbox
		
		addToGridBag (inputPanel, _genFileCheckbox, _SW, _X, _R, _R, _Z, 1,
					  vgap, hgap, 0, hgap, 0, 0, 1.0, 1.0);
		
		// Add the Java source files checkbox
		
		addToGridBag (inputPanel, _javaSourceCheckbox, _W, _X, _R, _R, _Z, 1,
					  0, hgap, 0, hgap, 0, 0, 1.0, 0.0);
		
		// Add the output file checkbox
		
		addToGridBag (inputPanel, _outFileCheckbox, _SW, _X, _R, _R, _Z, 1,
					  vgap, hgap, 0, hgap, 0, 0, 1.0, 1.0);
		
		// Add the verbose output checkbox
		
		addToGridBag (inputPanel, _verboseCheckbox, _NW, _X, _R, _R, _Z, 1,
					  0, hgap, vgap, hgap, 0, 0, 1.0, 1.0);
		
		// This panel holds the input action buttons.
		// It is layed out as a grid (not a gridbag) with one row.
		// This makes each button have the same width.
		
		Panel inButtonPanel = new Panel ();
		inButtonPanel.setLayout (new GridLayout (1, 0, hgap, 0));
		
		// Add the generate button
		
		inButtonPanel.add (_generateButton);
		
		// Add the view output button
		
		inButtonPanel.add (_outputButton);
		
		// Add the reset button
		
		inButtonPanel.add (_resetButton);
		
		// Add the exit button
		
		inButtonPanel.add (_inExitButton);
		
		// Add the button panel as a row in the input panel.
		
		addToGridBag (inputPanel, inButtonPanel, _N, _X, _R, _R, _Z, 1,
					  0, hgap, vgap, hgap, 0, 0, 1.0, 0.0);
		
		// Add the input panel to our card layout
		
		add ("Input", inputPanel);
		
		
		
		
		// Panel for the outut screen.
		// It is layed out as a gridbag, with one column.
		// There is x-stretch and y-stretch.
		// All the y-stretch is allocated to the text area.
		// Insets create a border around this panel and gaps between elements.
		
		Panel outputPanel = new Panel ();
		outputPanel.setLayout (new GridBagLayout());
		
		// Add the output label
		
		addToGridBag (outputPanel, _outputLabel, _SW, _H, _R, _R, _Z, 1,
					  vgap, hgap, 0, hgap, 0, 0, 1.0, 0.0);
		
		// Add the output text area
		
		addToGridBag (outputPanel, _outputText, _C, _B, _R, _R, _Z, 1,
					  vgap/10, hgap, vgap/10, hgap, 0, 0, 1.0, 1.0);
		
		// Add the summary label
		
		addToGridBag (outputPanel, _summaryLabel, _NW, _H, _R, _R, _Z, 1,
					  0, hgap, vgap, hgap, 0, 0, 1.0, 0.0);
		
		// This panel holds the output action buttons.
		// It is layed out as a grid (not a gridbag) with one row.
		// This makes each button have the same width.
		
		Panel outButtonPanel = new Panel ();
		outButtonPanel.setLayout (new GridLayout (1, 0, hgap, 0));
		
		// Add the view input button
		
		outButtonPanel.add (_inputButton);
		
		// Add the exit button
		
		outButtonPanel.add (_outExitButton);
		
		// Add the button panel as a row in the input panel.
		
		addToGridBag (outputPanel, outButtonPanel, _N, _X, _R, _R, _Z, 1,
					  0, hgap, vgap, hgap, 0, 0, 1.0, 0.0);
		
		// Add the input panel to our card layout
		
		add ("Output", outputPanel);
		
		
		
	
		// Done
		
		return;
	}
	
	
	
	
	// Reset the input panel to defaults.
	
	public void reset ()
	{
		
		// Empty the text field
		
		_filenameText.setText ("");
		
		// Establish defaults for checkboxes
		
		_scannerTableCheckbox.setState (true);
		_parserTableCheckbox.setState (true);
		_genFileCheckbox.setState (false);
		_javaSourceCheckbox.setState (true);
		_outFileCheckbox.setState (false);
		_verboseCheckbox.setState (false);
		
		return;
	}
	
	
	
	
	// Set the card to display.
	//
	// Parameters:
	//	card = String specifying card: "Input" or "Output"
	
	public void showCard (String card)
	{
		
		// Show the selected card
		
		((CardLayout) getLayout()).show (this, card);
		
		return;
	}
	
	
	
	
	// Show a one-line message in a dialog box.
	
	private void showMessage (String message)
	{
		MessageDialog.show (_parent, ProductInfo.product, message,
							MessageDialog.OK, true,
							new Font ("Dialog", Font.PLAIN, _fontSize),
							Color.lightGray, null );
		
		return;
	}
	
	
	
	
	// This function is called by AWT when an action occurs on any
	// of our components.
	
	public void actionPerformed (ActionEvent evt)
	{
		
		// Exit button
		
		if ((evt.getSource() == _inExitButton) || (evt.getSource() == _outExitButton))
		{
			System.exit (0);
			return;
		}
		
		// Reset button
		
		else if (evt.getSource() == _resetButton)
		{
			
			// Reset the input panel
			
			reset ();
			
			return;
		}
		
		// Browse button
		
		else if (evt.getSource() == _browseButton)
		{
			
			// Put up a file dialog
			// Note: Because the true size of a FileDialog is not
			// available, setChildPos should not be used.
			// Note: There is a bug in Microsoft's FileDialog, in that when
			// you double-click on a filename, one of the mouse clicks is
			// passed through to this panel.  So, we disable/enable this
			// panel around the call to FileDialog.setVisible(true), so that AWT will
			// discard the "extra" mouse click.
			
			FileDialog fd = new FileDialog (_parent, "Select Grammar File");
			fd.setDirectory (_lastDir);
			fd.setFilenameFilter (new GenGUIJaccFilter());
			//setChildPos (fd, 0.25, 0.25, 0, 0, _parent, 0.25, 0.25, 0, 0);
			fd.setLocation (_parent.location().x + 20, _parent.location().y + 20);
			
			setEnabled(false);
			fd.setVisible(true);
			setEnabled(true);
			
			// Get the filename and directory name
			
			String filename = fd.getFile();
			String dirname = fd.getDirectory();
			
			// If there's a filename ...
			
			if (filename != null)
			{
				
				// If there's a directory name ...
				
				if (dirname != null)
				{
					
					// Save the directory name for use the next time
					
					_lastDir = dirname;
					
					// Concatenate directory and filename
					//
					// Note:  Microsoft's FileDialog tends to return directory
					// names that end in backslash.  In VJ++ 1.X, using the File
					// class to concatenate the directory name and filename
					// results in two consecutive backslashes.  (In VJ++ 6.0,
					// the File class was made smarter so it doesn't insert an
					// extra backslash.)  Therefore, we check if the directory
					// name ends in a separator character;  if so, we just
					// concatenate the names instead of using the File class.
					
					if (dirname != null
						&& dirname.length() > 0
						&& dirname.charAt (dirname.length() - 1) == File.separatorChar)
					{
						filename = dirname + filename;
					}
					else
					{
						filename = (new File (dirname, filename)).getPath();
					}
				}
				
				// Insert the filename into the filename text field
				
				_filenameText.setText (filename);
				
				// Move the text selection to the end of the field
				
				_filenameText.select (filename.length(), filename.length());
				
				// Move the focus to the text field
				
				_filenameText.requestFocus ();
			}
			
			// Done
			
			return;
		}
		
		// View output button
		
		else if (evt.getSource() == _outputButton)
		{
			
			// Show the output card
			
			showCard ("Output");
			_outputText.requestFocus ();
			
			return;
		}
		
		// View input button
		
		else if (evt.getSource() == _inputButton)
		{
			
			// Show the input card
			
			showCard ("Input");
			_filenameText.requestFocus ();
			
			return;
		}
		
		// Generate button, or Enter in the filename text
		
		else if (evt.getSource() == _generateButton
				 || evt.getSource() == _filenameText )
		{
			
			// Get the filename
			
			String filename = _filenameText.getText();
			
			// If the filename is empty ...
			
			if (filename.length() == 0)
			{
				
				// Ask the user to enter the filename
				
				showMessage ("You need to enter the name of the grammar file");
				
				// Move the focus to the text field
				
				_filenameText.requestFocus ();
				
				// And stop
			
				return;
			}
			
			// Set up the parser generator front end
			
			_genFrontEnd = new GenFrontEnd ();
			
			// Set up a stream to receive the output
			
			_outWriter = new CharArrayWriter (2000);
			
			// Disable ourselves
			
			this.setEnabled (false);
			_parent.setEnabled (false);
			
			// Start the parser generator
			
			_genFrontEnd.generate (this, true,
								   new ErrorOutputWriter (
											new PrintWriter (_outWriter), null, "\n" ),
								   _verboseCheckbox.getState(),
								   filename,
								   _scannerTableCheckbox.getState(),
								   _parserTableCheckbox.getState(),
								   _outFileCheckbox.getState(),
								   _genFileCheckbox.getState(),
								   _javaSourceCheckbox.getState() );
							  
			// Done for now
			
			return;
		}
		
		// Ignore everything else
		
		return;
	}
	
	
	
	
	// This function is called when the parser generator begins execution.
	
	public void generatorBegin (String filename, String shortFilename)
	{
			
		// Create the progress window
		
		_progressWindow = new GenGUIProgressWindow (
									_parent, _genFrontEnd, shortFilename, _fontSize );
		
		return;
	}
	
	
	
	
	// This function is called when the parser generator ends execution.
	
	public void generatorEnd (String summary, int errorFlags)
	{
			
		// Enable ourselves
		// Note:  We must enable ourselves before discarding the progress
		// window, otherwise some other application will get the focus (in
		// Microsoft Windows) when the progress window is destroyed.
			
		_parent.setEnabled (true);
		this.setEnabled (true);
			
		// Discard the progress window
			
		_progressWindow.dispose();
		_progressWindow = null;
			
		// Transfer the filename to the output label
			
		if (_genFrontEnd.jaccFilename() == null)
		{
			_outputLabel.setText ("");
		}
		else
		{
			_outputLabel.setText (_genFrontEnd.jaccFilename());
		}
			
		// Transfer the result summary to the summary label
			
		_summaryLabel.setText (_genFrontEnd.summary());
		
		// Transfer the output into the text area
		
		_outputText.setText (_outWriter.toString());
			
		// If there was an abort ...
			
		if ((errorFlags & GenObserver.efAborted) != 0)
		{
				
			// Display the result summary in a dialog box
				
			showMessage (summary);
				
			// Move the focus to the text field
				
			_filenameText.requestFocus ();
		}
			
		// Otherwise, display the output panel ...
			
		else
		{
			
			// Show the output card
			
			showCard ("Output");
			
			// Move the focus to the text viewer
			
			_outputText.requestFocus ();
		}
		
		// Discard the background thread resources
		
		_genFrontEnd = null;
		_outWriter = null;
		
		return;
	}
	
	
	
	
	// This function is called when the parser generator begins a new stage.
	
	public void generatorStage (String stage)
	{
		_progressWindow.setStage (stage);
		return;
	}
	
	
	
	
	// This function is called when the parser generator has performed some
	// work within a stage.
	
	public void generatorWork (int amount)
	{
		_progressWindow.setWork (amount);
		return;
	}
	
	
}




// GenGUIJaccFilter is a FilenameFilter that selects files which end
// in the extension ".jacc".

class GenGUIJaccFilter implements FilenameFilter
{
	public boolean accept (File dir, String name)
	{
		return name.toLowerCase().endsWith(".jacc");
	}
}




// GenGUIProgressWindow is a Window that displays the progress of the parser
// generator.

class GenGUIProgressWindow extends Window implements ActionListener
{
	
	// ----- Variables -----
	
	// The target for interrupt requests
	
	private GenFrontEnd _genFrontEnd;
	
	// Our border width
	
	private int _border = 3;
	
	
	
	
	// ----- Child components -----
	
	// The interrupt button on the progress panel
	
	private Button _interruptButton = new Button ("Interrupt");
	
	// The progress message label
	
	private Label _progressTitleLabel = new Label ("", Label.CENTER);
	
	// The progress stage label
	
	private Label _progressStageLabel = new Label (
			"01234567890123456789012345678901234567890123456789", Label.CENTER);
	
	// The progress count label
	
	private Label _progressCountLabel = new Label ("", Label.CENTER);
	
	
	
	
	// ----- Boilerplate GridBag code -----
	
	
	// addToGridBag - Add a Component to a Container with a gridbag layout
	//
	// Parameters:
	//	cont = Container object, which must have a GridBagLayout layout manager
	//	comp = Component to add to the container
	//	others = See class java.io.GridBagConstraints
	//
	// Return value:
	//	The function returns the comp argument
	
	private static final int _C = GridBagConstraints.CENTER;
	private static final int _E = GridBagConstraints.EAST;
	private static final int _N = GridBagConstraints.NORTH;
	private static final int _NE = GridBagConstraints.NORTHEAST;
	private static final int _NW = GridBagConstraints.NORTHWEST;
	private static final int _S = GridBagConstraints.SOUTH;
	private static final int _SE = GridBagConstraints.SOUTHEAST;
	private static final int _SW = GridBagConstraints.SOUTHWEST;
	private static final int _W = GridBagConstraints.WEST;
	private static final int _B = GridBagConstraints.BOTH;
	private static final int _H = GridBagConstraints.HORIZONTAL;
	private static final int _X = GridBagConstraints.NONE;
	private static final int _V = GridBagConstraints.VERTICAL;
	private static final int _Z = GridBagConstraints.REMAINDER;
	private static final int _R = GridBagConstraints.RELATIVE;
	
	private static Component addToGridBag (Container cont, Component comp,
		int anchor, int fill, int gridx, int gridy, int gridwidth, int gridheight,
		int insetsTop, int insetsLeft, int insetsBottom, int insetsRight,
		int ipadx, int ipady, double weightx, double weighty)
	{
		
		// Get the layout manager, which must be a GridBagLayout
		
		GridBagLayout layout = (GridBagLayout) cont.getLayout ();
		
		// Create a constraints object
		
		GridBagConstraints constraints = new GridBagConstraints ();
		
		// Set the constraints from the function arguments
		
		constraints.anchor = anchor;
		constraints.fill = fill;
		constraints.gridx = gridx;
		constraints.gridy = gridy;
		constraints.gridwidth = gridwidth;
		constraints.gridheight = gridheight;
		constraints.insets = new Insets (insetsTop, insetsLeft, insetsBottom, insetsRight);
		constraints.ipadx = ipadx;
		constraints.ipady = ipady;
		constraints.weightx = weightx;
		constraints.weighty = weighty;
		
		// Specify constraints for this component
		
		layout.setConstraints (comp, constraints);
		
		// Add the component to the container
		
		cont.add (comp);
		
		// Return the component object
		
		return comp;
	}
	
	
	
	
	// ----- Boilerplate window positioning code -----
	
	
	// Position a child Window relative to a parent Window
	//
	// Parameters:
	//	child = child Window
	//	parent = parent Window
	//	others = see below
	//
	// This function works by defining an "anchor point" on each of the two
	// windows.  The coordinates of the anchor point are defined by offsets
	// relative to the window's upper left corner.  Each offset is computed
	// as the sum of two parts: a double that gives an offset as a fraction
	// of the window's size, and an int that gives an absolute offset in pixels.
	// For example, the x coordinate of the child window's anchor point is
	// equal to the x coordinate of the child window's upper left corner,
	// plus cfrx times the width of the child window, plus cofx.
	//
	// The position of the child window is set so that the child window's
	// anchor point coincides with the parent window's anchor point.
	// For example, setting cfrx=cfry=pfrx=pfry=0.5 and cofx=cofy=pofx=pofy=0
	// will center the child window over the parent window.  Likewise, setting
	// cfrx=cfry=pfrx=pfry=0.0 and cofx=cofy=0 and pofx=pofy=20 will display
	// the child window's upper left corner 20 pixels below and to the right of
	// the parent window's upper left corner.
	//
	// If superimposing the anchor points would make part of the child window
	// appear off-screen, the position of the child window is adjusted so that
	// the entire child window is on-screen.  If the child window is wider than
	// the screen, the left edge of the child window is set to the left edge of
	// the screen.  Likewise, if the child window is higher than the screen,
	// the top of the child window is set to the top of the screen.
	
	private static void setChildPos (
						Window child, double cfrx, double cfry, int cofx, int cofy,
						Window parent, double pfrx, double pfry, int pofx, int pofy )
	{
		
		// Calculate the desired child window position
		
		int x = (int)Math.rint ((double)(parent.location().x)
								+ (((double)pofx) + pfrx * (double)parent.getSize().width)
								- (((double)cofx) + cfrx * (double)child.getSize().width) );
		
		int y = (int)Math.rint ((double)(parent.location().y)
								+ (((double)pofy) + pfry * (double)parent.getSize().height)
								- (((double)cofy) + cfry * (double)child.getSize().height) );
		
		// Make sure the child window is on-screen
		
		x = Math.max (0, Math.min (x, child.getToolkit().getScreenSize().width
									  - child.getSize().width ));
		
		y = Math.max (0, Math.min (y, child.getToolkit().getScreenSize().height
									  - child.getSize().height ));
		
		// Now move the child to the specified location
		
		child.setLocation (x, y);
		
		return;
	}
	
	
	
		
	// ----- Constructor -----
	
	
	// Parameters:
	//	parent = a Frame to use as the window's parent, and for centering.
	//	genFrontEnd = target for interrupt requests.
	//	filename = filename to display, or null if none.
	//	fontSize = font size to use.
	
	public GenGUIProgressWindow (Frame parent, GenFrontEnd genFrontEnd,
								 String filename, int fontSize)
	{
		
		// Create a window with the specified parent
		
		super (parent);
		
		// Save the target for interrupt requests
		
		_genFrontEnd = genFrontEnd;
		
		// Add event listeners
		
		_interruptButton.addActionListener (this);
		
		// Vertical and horizontal gaps for component spacing
		
		int vgap = 15;
		int hgap = 10;
		
		// Set our fonts
		
		this.setFont (new Font ("Dialog", Font.PLAIN, fontSize));
		
		// Set our colors
		
		this.setBackground (Color.lightGray);
	
		// If we are given a filename, display it
	
		if (filename != null && filename.length() != 0)
		{
			_progressTitleLabel.setText ("Generating: " + filename);
		}
		
		// The window is layed out as a gridbag, with one column.
		// There is x-stretch and y-stretch.
		// Insets create a border around this panel and gaps between elements.
		
		this.setLayout (new GridBagLayout());
		
		// Add the title text
		
		addToGridBag (this, _progressTitleLabel, _S, _H, _R, _R, _Z, 1,
					  vgap, hgap, 0, hgap, 0, 0, 1.0, 0.0);
		
		// Add the progress stage label
		
		addToGridBag (this, _progressStageLabel, _S, _H, _R, _R, _Z, 1,
					  vgap*2, hgap, 0, hgap, 0, 0, 1.0, 2.0);
		
		// Add the progress count label
		
		addToGridBag (this, _progressCountLabel, _N, _H, _R, _R, _Z, 1,
					  0, hgap, vgap*2, hgap, 0, 0, 1.0, 2.0);
		
		// Add the button
		
		addToGridBag (this, _interruptButton, _N, _X, _R, _R, _Z, 1,
					  0, hgap, vgap, hgap, hgap, 0, 1.0, 0.0);
		
		// Adjust size to the preferred size
		
		this.pack ();
		
		// Center the window over the parent frame
		
		setChildPos (this, 0.5, 0.5, 0, 0, parent, 0.5, 0.5, 0, 0);
		
		// Clear the stage text
		
		_progressStageLabel.setText ("");
		
		// Display the window
		
		this.setVisible (true);
		
		// Give the focus to the interrupt button
		
		_interruptButton.requestFocus();
		
		// Done
		
		return;
	}
	
	
	
	
	// ----- Client methods -----
	
	
	// Set the current stage.
	
	public void setStage (String stage)
	{
		
		// Put our message into the progress window
		
		_progressStageLabel.setText (stage);

		_progressCountLabel.setText ("");
		
		// Done
		
		return;
	}
	
	
	// Set the current work amount.
	
	public void setWork (int amount)
	{
		
		// Display the new progress count
		
		_progressCountLabel.setText (Integer.toString (amount));
		
		// Done
		
		return;
	}
	
	
	
	
	// ----- ActionListener methods -----
	
	
	// This function is called by AWT when the interrupt button is pressed.
	
	public void actionPerformed (ActionEvent evt)
	{
		_genFrontEnd.requestInterrupt();
		return;
	}
	
	
	
	
	// ----- Component methods -----
	
	
	// Make a border on our window
	
	public void update (Graphics g)
	{
		Insets ins = getInsets();
		int x = 0;
		int y = 0;
		int w = getSize().width - ins.left - ins.right;
		int h = getSize().height - ins.top - ins.bottom;
		
		Color col = g.getColor();
		g.setColor (getBackground());
		
		for (int i = 0; i < _border; ++i)
		{
			g.draw3DRect (x++, y++, w-1, h-1, true);
			w -= 2;
			h -= 2;
		}
		
		g.fillRect (x, y, w, h);
		
		g.setColor (col);
		
		return;
	}
	
	
	// Paint defers to update
	
	public void paint (Graphics g)
	{
		update (g);
		return;
	}
	
	
}
