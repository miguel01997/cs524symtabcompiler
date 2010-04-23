// Copyright 1998 Invisible Software, Inc.

package invisible.jacc.utilg;

import java.awt.*;
import java.awt.event.*;


/*->

StatusLabel is very similar to java.awt.Label, except that it draws a 3D border
around the text.

The following fields and methods are useful.  See java.awt.Label for descriptions.

	public static final int CENTER = Label.CENTER;
	public static final int LEFT = Label.LEFT;
	public static final int RIGHT = Label.RIGHT;
	public StatusLabel ()
	public StatusLabel (String text)
	public StatusLabel (String text, int alignment)
	public void setFont (Font f)
	public void setBackground (Color c)
	public void setForeground (Color c)
	public int getAlignment ()
	public String getText ()
	public void setAlignment (int alignment)
	public void setText (String text)


->*/


public class StatusLabel extends Panel
{
	
	// ----- Aligment constants -----
	
	public static final int CENTER = Label.CENTER;
	public static final int LEFT = Label.LEFT;
	public static final int RIGHT = Label.RIGHT;
	
	
	
	
	// ----- Internal variables -----
	
	// The thickness of our border
	
	private int _border = 1;
	
	// A Label to contain the text
	
	private Label _label;
	
	
	
	
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
	
	
	
	
	// ----- Constructors -----

	
	// Create label with no text and left-justified.
	
	public StatusLabel ()
	{
		this ("", LEFT);
		return;
	}

	
	// Create label with specified text and left-justified.
	
	public StatusLabel (String text)
	{
		this (text, LEFT);
		return;
	}

	
	// Create label with specified text and alignment.
	
	public StatusLabel (String text, int alignment)
	{
		super();
		
		// Create a Label for the text
	
		_label = new Label (text, alignment);
		
		// Use a GridBag layout
		
		this.setLayout (new GridBagLayout());
		
		// Add the Label, and make it fill the area inside the border, but
		// leave an extra pixel on the left and right edges
		
		addToGridBag (this, _label, _C, _B, 0, 0, 1, 1,
					  _border, _border + 1, _border, _border + 1, 0, 0, 1.0, 1.0);
		
		// Done
		
		return;
	}
	
	
	
	
	// ----- Component methods -----
	
	
	// Our font is being changed.
	
	public void setFont (Font f)
	{
		super.setFont (f);
		_label.setFont (f);
		return;
	}
	
	
	// Our background color is being changed.
	
	public void setBackground (Color c)
	{
		super.setBackground (c);
		_label.setBackground (c);
		return;
	}
	
	
	// Our foreground color is being changed.
	
	public void setForeground (Color c)
	{
		super.setForeground (c);
		_label.setForeground (c);
		return;
	}
	
	
	
	
	// ----- Label methods -----
	
	
	// Get the aligment value.
	
	public int getAlignment ()
	{
		return _label.getAlignment();
	}
	
	
	// Get the current text.
	
	public String getText ()
	{
		return _label.getText();
	}
	
	
	// Set the alignment value.
	
	public void setAlignment (int alignment)
	{
		_label.setAlignment (alignment);
		return;
	}
	
	
	// Set the text.
	
	public void setText (String text)
	{
		_label.setText (text);
		return;
	}
	
	
	
	
	// ----- Painting routine -----
	
	
	// Make a border on our window, and fill the background
	
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
			g.draw3DRect (x++, y++, w-1, h-1, false);
			w -= 2;
			h -= 2;
		}
		
		g.fillRect (x, y, w, h);
		
		g.setColor (col);
		
		return;
	}
	
	
	// Paint just defers to update
	
	public void paint (Graphics g)
	{
		update (g);
		return;
	}
	
	
}
