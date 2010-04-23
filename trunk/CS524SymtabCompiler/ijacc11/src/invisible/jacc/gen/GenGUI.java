// Copyright 1997-1998 Invisible Software, Inc.

package invisible.jacc.gen;

import java.awt.*;
import java.awt.event.*;

import invisible.jacc.parse.ProductInfo;


/*->

GenGUI is the main entry point for the parser generator's graphical user
interface.  The main() function simply creates a Frame, and inserts a
GenGUIPanel into it.

By default, GenGUI uses 14-point fonts.  If desired, you can change the font
size by entering the desired font size on the command line.  For example,
entering "12" on the command line will yield 12-point fonts (which is the
normal font size on most systems).

Examples:  Under Microsoft Windows, the first command below runs the graphical
user interface with the default 14-point font size, and the second command
runs it with 12-point fonts.

	jview  invisible.jacc.gen.GenGUI

	jview  invisible.jacc.gen.GenGUI  12

->*/


public class GenGUI 
{
	
	// The default font size
	
	public static final int defaultFontSize = 14;
	
	
	// Generate scanner and parser tables.
	//
	// This function puts up a Frame on the screen, that allows the user
	// to run the parser generator.

	public static void main (String[] args)
	{
		
		// Read the font size from the command line, if it's there
		
		int fontSize = defaultFontSize;
		
		if (args.length >= 1)
		{
			try
			{
				fontSize = Integer.parseInt (args[0]);
				if (fontSize < 1 || fontSize > 99)
				{
					fontSize = defaultFontSize;
				}
			}
			catch (NumberFormatException e)
			{
				fontSize = defaultFontSize;
			}
		}
		
		// Make a top-level window and insert our component into it
		
		GenGUIFrame f = new GenGUIFrame (ProductInfo.product);
		GenGUIPanel p = new GenGUIPanel (f, fontSize);
		f.add ("Center", p);
		f.pack ();
		f.setVisible (true);
		return;
	}


}




// GenGUIFrame is a subclass of Frame that terminates the program when the
// user closes the window.

class GenGUIFrame extends Frame implements WindowListener
{
	
	// Constructor passes through the title
	
	public GenGUIFrame (String title)
	{
		super (title);
		this.addWindowListener (this);
	}
	
	
	
	
	// ----- WindowListener methods -----
	
	
	// The window is activated (gets the focus).
	
	public void windowActivated (WindowEvent evt)
	{
		return;
	}
	
	
	// The window is closed (Window.dispose() was called).
	
	public void windowClosed (WindowEvent evt)
	{
		return;
	}
	
	
	// The user has requested that the window be closed (in a platform-specific way).
	
	public void windowClosing (WindowEvent evt)
	{
		dispose ();
		System.exit (0);
		return;
	}
	
	
	// The window is deactivated (loses the focus).
	
	public void windowDeactivated (WindowEvent evt)
	{
		return;
	}
	
	
	// The window is deiconified (by the user in a platform-specific way).
	
	public void windowDeiconified (WindowEvent evt)
	{
		return;
	}
	
	
	// The window is iconified (by the user in a platform-specific way).
	
	public void windowIconified (WindowEvent evt)
	{
		return;
	}
	
	
	// The window has been shown for the first time.
	
	public void windowOpened (WindowEvent evt)
	{
		return;
	}
	
	
}
