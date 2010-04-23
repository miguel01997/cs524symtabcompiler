// Copyright 1998 Invisible Software, Inc.

package invisible.jacc.utilg;

import java.awt.*;
import java.awt.event.*;


/*->

MessageDialog is a modal dialog box which displays a message along with a row
of buttons.  The user can close the box by pressing any of the buttons, or by
closing the dialog window (in Microsoft Windows, pressing the X in the upper
right corner).  The button that the user pressed is identified to the caller.

MessageDialog has only one useful entry point:

	public static int show (Frame parent, String title, String message,
							String[] buttons, boolean uniform,
							Font font, Color bgColor, Color fgColor)

The return value identifies which button was pressed.

There are also predefined arrays with common sets of buttons:
	
	public static final String[] OK = {"OK"};
	public static final String[] OK_CANCEL = {"OK", "Cancel"};
	public static final String[] RETRY_CANCEL = {"Retry", "Cancel"};
	public static final String[] YES_NO = {"Yes", "No"};
	public static final String[] YES_NO_CANCEL = {"Yes", "No", "Cancel"};

At the moment, this class only displays one line of text.  We hope later to
enhance this class to be able to display multiple lines.

->*/


public class MessageDialog extends Dialog implements ActionListener
{
	
	// ----- Common sets of buttons -----
	
	public static final String[] OK = {"OK"};
	public static final String[] OK_CANCEL = {"OK", "Cancel"};
	public static final String[] RETRY_CANCEL = {"Retry", "Cancel"};
	public static final String[] YES_NO = {"Yes", "No"};
	public static final String[] YES_NO_CANCEL = {"Yes", "No", "Cancel"};
	
	
	
	
	// ----- Variables -----
	
	// The index of the button that was pressed, or -1 if none
	
	private int _closeIndex = -1;
	
	
	
	
	// ----- Child components -----
	
	// The array of buttons in the dialog box, can be zero-length
	
	private Button[] _buttons;
	
	// The text output area
	
	private Label _textOut;
	
	// The component that appears at the bottom of the box, or null if none
	
	private Component _bottomComp = null;
	
	// The horizontal padding for the component at the bottom of the box
	
	private int _bottomPad = 0;
	
	
	
	
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
	
	

	
	// ----- Factory function -----
	
		
	// Create a dialog box, display it, and return the result.
	//
	// Parameters:
	//	parent = a Frame to use as the dialog's parent window.
	//	title = String to appear in the dialog's title bar.
	//	message = message text to appear in the dialog box.
	//	buttons = array of strings which gives the labels of the buttons in
	//			  the dialog box;  can be null, and can be zero-length.
	//	font = font to use;  can be null.
	//	bgColor = background color;  can be null.
	//	fgColor = foreground color;  can be null.
	//
	// The return value is the index in the buttons array of the button that the
	// user pressed, or -1 if the user closed the dialog box without pressing
	// any of the buttons.
	
	public static int show (Frame parent, String title, String message,
							String[] buttons, boolean uniform,
							Font font, Color bgColor, Color fgColor)
	{
		
		// Vertical and horizontal gaps for component spacing
		
		int vgap = 15;
		int hgap = 10;
		
		// Create a dialog box
	
		MessageDialog box = new MessageDialog (parent, title);
		
		// Make the box non-resizeable
		
		box.setResizable (false);
		
		// Create the text output
		
		box._textOut = new Label (message);
		
		// Create the child buttons
		
		if (buttons == null)
		{
			box._buttons = new Button[0];
		}
		
		else
		{
			box._buttons = new Button[buttons.length];
			
			for (int i = 0; i < buttons.length; ++i)
			{
				box._buttons[i] = new Button (buttons[i]);
				box._buttons[i].addActionListener (box);
			}
		}
		
		// If there is exactly one button ...
		
		if (box._buttons.length == 1)
		{
		
			// The bottom component is the button, slightly enlarged
			
			box._bottomComp = box._buttons[0];
			box._bottomPad = hgap;
		}
		
		// If there is more than one button ...
		
		if (box._buttons.length > 1)
		{
		
			// This panel holds the buttons in a horizontal row.
			
			Panel buttonPanel = new Panel ();
			
			// If we want uniform button sizes ...
			
			if (uniform)
			{
			
				// The panel is layed out as a grid (not a gridbag) with one row.
				// This makes each button have the same width.
		
				buttonPanel.setLayout (new GridLayout (1, 0, hgap, 0));
		
				// Add the buttons
		
				for (int i = 0; i < box._buttons.length; ++i)
				{
					buttonPanel.add (box._buttons[i]);
				}
			}
			
			// Otherwise, we use non-uniform button sizes ...
			
			else
			{
				
				// The panel is layed out as a gridbag, with one row.
				// There is no x-stretch or y-stretch.
				// Insets create a gaps between buttons.
		
				buttonPanel.setLayout (new GridBagLayout ());
		
				// Add the buttons
		
				for (int i = 0; i < box._buttons.length; ++i)
				{
					addToGridBag (buttonPanel, box._buttons[i], _C, _X, _R, _R, 1, 1,
								  0, (i == 0) ? 0 : hgap, 0, 0, hgap, 0, 0.0, 0.0);
				}
			}
			
			// The bottom component is the panel with the buttons
			
			box._bottomComp = buttonPanel;
			box._bottomPad = 0;
		}
		
		// Set our fonts
		
		if (font != null)
		{
			box.setFont (font);
			box._textOut.setFont (font);
		}
		
		// Set our colors
		
		if (bgColor != null)
		{
			box.setBackground (bgColor);
			box._textOut.setBackground (bgColor);
		}
		
		if (fgColor != null)
		{
			box.setForeground (fgColor);
			box._textOut.setForeground (fgColor);
		}
		
		// The box is layed out as a gridbag, with one column.
		// There is x-stretch and y-stretch.
		// Insets create a border around this panel and gaps between elements.
		
		box.setLayout (new GridBagLayout());
		
		// Add the message text
		
		addToGridBag (box, box._textOut, _C, _X, _R, _R, _Z, 1,
					  vgap, hgap, vgap, hgap, 0, 0, 1.0, 2.0);
		
		// Add the bottom component, if any
		
		if (box._bottomComp != null)
		{
			addToGridBag (box, box._bottomComp, _N, _X, _R, _R, _Z, 1,
						  0, hgap, vgap, hgap, box._bottomPad, 0, 1.0, 0.0);
		}
		
		// Adjust size to the preferred size
		
		box.pack ();
		
		// Center the box over the parent frame
		
		setChildPos (box, 0.5, 0.5, 0, 0, parent, 0.5, 0.5, 0, 0);
		
		// Display the box
		
		box.setVisible (true);
		
		// Return the index of the button that was pressed
		
		return box._closeIndex;
	}
	
	

	
	// ----- Constructor -----
	
		
	// This constructor requires a Frame as the dialog's parent, and a title
	// string.  It is protected so that clients are forced to use the factory
	// functin.
	
	protected MessageDialog (Frame parent, String title)
	{
		
		// Create a modal dialog with the specified parent and title
	
		super (parent, title, true);
		
		this.addWindowListener (new WindowEventHandler());
		
		return;
	}
	
	
	
	
	// ----- Event handlers -----

		
	// Handler for window destroy event
	
	class WindowEventHandler extends WindowAdapter
	{
		public void windowClosing (WindowEvent evt)
		{
			dispose ();
			return;
		}
	}
	
	
	
	
	// This function is called by AWT when an action occurs on any
	// of our components.
	
	public void actionPerformed (ActionEvent evt)
	{
		
		// On any button press, close the dialog
		
		if (evt.getSource() instanceof Button)
		{
			for (int i = 0; i < _buttons.length; ++i)
			{
				if (evt.getSource() == _buttons[i])
				{
					_closeIndex = i;
					break;
				}
			}
			
			dispose ();
			return;
		}
		
		// Everything else is ignored
		
		return;
	}
	
	
}
