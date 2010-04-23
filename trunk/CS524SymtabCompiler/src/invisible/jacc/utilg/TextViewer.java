// Copyright 1998 Invisible Software, Inc.

package invisible.jacc.utilg;

import invisible.jacc.util.IntDeque;

import java.awt.*;
import java.awt.event.*;


/*->

TextViewer is an AWT component that is used for viewing text.

TextViewer is in many ways similar to java.awt.TextArea set to be non-editable.
We have created TextViewer because most implementations of TextArea seem to
have trouble displaying large amounts of text.  For example, in VJ++ 1.01 a
TextArea cannot hold more than 32K of text (calls to setText with a string
longer than 32K are silently ignored).  In VJ++ 6.0, a TextArea does not scroll
properly if it holds a large amount of text.  Also, TextArea can have poor
performance when it holds a large amount of text.

The following function calls are useful:

	public TextViewer ()
	public TextViewer (int rows, int cols)
	public TextViewer (String text)
	public TextViewer (String text, int rows, int cols)
	public void setFont (Font f)
	public void setBackground (Color c)
	public void setForeground (Color c)
	public void setText (String text)
	public String getText ()
	public int getColumns ()
	public int getRows ()
	public void requestFocus ()

The constructors create a TextViewer with the specified text, number of rows,
and number of columns.  If the text is not specified, and empty string is used.
If the number of rows and columns are not specified, default values are used.
The number of rows and columns (along with the current font) determines the
preferred size of the TextViewer, but otherwise does not affect its operation.

The setFont, setBackground, and setForeground functions determine the font and
colors used for displaying the text.

The setText function changes the text displayed.  The text is a String in which
lines are separated by a single newline character '\n'.  There is no limit on
the length of a line, or on the number of lines.  At present, TextViewer does
not support tab characters.  The getText function can be used to retrieve the
currently displayed text.

The getRows and getColumns functions retrieve the row and column numbers used
to create the TextViewer;  they do not indicate it's current size on-screen.

The requestFocus function gives the input focus to the TextViewer.  When the
TextViewer has input focus, the following keys are recognized:

	Home						-- Scroll to the left side of the text.
	End							-- Scroll to the right side of the text.
	Ctrl-PgDn					-- Scroll to the bottom of the text.
	Ctrl-PgUp					-- Scroll to the top of the text.
	PgDn or Ctrl-Arrow-Down		-- Scroll down one screen.
	PgUp or Ctrl-Arrow-Up		-- Scroll up one screen.
	Arrow-Down					-- Scroll down one line.
	Arrow-Up					-- Scroll up one line.
	Ctrl-Arrow-Right			-- Scroll right 20 columns.
	Ctrl-Arrow-Left				-- Scroll left 20 columns.
	Arrow-Right					-- Scroll right one column.
	Arrow-Left					-- Scroll left one column.

->*/


public class TextViewer extends Panel
	implements AdjustmentListener, FocusListener, MouseListener
{
	
	// ----- Default layout parameters -----
	
	// The width of the outer 3D border, in pixels
	
	private static int _defaultOuter3DBorder = 1;
	
	// The width of the inner 3D border, in pixels
	
	private static int _defaultInner3DBorder = 1;
	
	// The insets for the text viewport
	
	private static Insets _defaultTextInsets = new Insets (1, 1, 1, 1);
	
	// The number of rows, in characters
	
	private static int _defaultRows = 4;
	
	// The number of columns, in characters (column = width of 'm')
	
	private static int _defaultColumns = 40;
	
	
	
	
	// ----- Layout parameters -----
	
	// The width of the outer 3D border, in pixels
	
	private int _outer3DBorder = _defaultOuter3DBorder;
	
	// The width of the inner 3D border, in pixels
	
	private int _inner3DBorder = _defaultInner3DBorder;
	
	// The insets for the text viewport
	
	private Insets _textInsets = _defaultTextInsets;
	
	// The number of rows, in characters
	
	private int _rows;
	
	// The number of columns, in characters (column = width of 'm')
	
	private int _columns;
	
	
	
	
	// ----- Our child components -----
	
	// The canvas used to display text
	
	private TextViewerCanvas _textViewerCanvas;
	
	// The vertical scrollbar
	
	private Scrollbar _verticalScrollbar = new Scrollbar (Scrollbar.VERTICAL);
	
	// The horizontal scrollbar
	
	private Scrollbar _horizontalScrollbar = new Scrollbar (Scrollbar.HORIZONTAL);
	
	
	
	
	// ----- Constructors -----
	
	
	// Make a viewer with no text and default dimensions
	
	public TextViewer ()
	{
		this ("", _defaultRows, _defaultColumns);
		return;
	}
	
	
	// Make a viewer with no text and specified dimensions
	
	public TextViewer (int rows, int cols)
	{
		this ("", rows, cols);
		return;
	}
	
	
	// Make a viewer with specified text and default dimensions
	
	public TextViewer (String text)
	{
		this (text, _defaultRows, _defaultColumns);
		return;
	}
	
	
	// Make a viewer with specified text and dimensions
	
	public TextViewer (String text, int rows, int cols)
	{
		super();
		
		// Layout parameters
	
		_rows = rows;
		_columns = cols;
		
		// Total 3D border
		
		int border3D = _outer3DBorder + _inner3DBorder;
		
		// Add our event listeners
		
		_verticalScrollbar.addAdjustmentListener (this);
		_horizontalScrollbar.addAdjustmentListener (this);
		this.addFocusListener (this);
		this.addMouseListener (this);
		
		_verticalScrollbar.addMouseListener (this);
		_horizontalScrollbar.addMouseListener (this);
		
		// Create the text viewer canvas.
		// This will also initialize the scrollbars.
		
		_textViewerCanvas = new TextViewerCanvas (
					text, rows, cols, _verticalScrollbar, _horizontalScrollbar );
		
		// Hack.  In Visual J++ 1.01, a scrollbar inherits its background color
		// from its ancestor (the color is apparently remembered at the time the
		// peer is created) if the scrollbar's background color was never set
		// explicitly.  In Visual J++ 6.0, a scrollbar's background color is
		// a system color unless you set it to something different.  To force a
		// reasonable appearance in VJ++ 1.01 while preserving the use of a
		// system color in VJ++ 6.0, we put each scrollbar in front of panel that
		// has light gray background.  In VJ++ 1.01 the scrollbars come out with
		// light gray background (inherited from the panel) while in VJ++ 6.0
		// the scrollbars use the system color.
		
		Panel verticalScrollPanel = new Panel ();
		verticalScrollPanel.setBackground (new Color (224, 224, 224));
		verticalScrollPanel.setLayout (new BorderLayout());
		verticalScrollPanel.add ("Center", _verticalScrollbar);
		
		Panel horizontalScrollPanel = new Panel ();
		horizontalScrollPanel.setBackground (new Color (224, 224, 224));
		horizontalScrollPanel.setLayout (new BorderLayout());
		horizontalScrollPanel.add ("Center", _horizontalScrollbar);
		
//		// If you wanted to lay out this panel using a GridBagLayout, the
//		// following code shows how to do it.  See class GenGUIPanel for the
//		// definition of the addToGridBag function.
//		
//		// This panel is layed out as a gridbag, with two rows and two columns.
//		// There is x-stretch and y-stretch.
//		// All the stretch is allocated to the text viewer canvas.
//		// Insets create a border.
//		
//		this.setLayout (new GridBagLayout());
//		
//		// Add the text viewer canvas
//		
//		addToGridBag (this, _textViewerCanvas, _NW, _B, 0, 0, 1, 1,
//					  border3D + _textInsets.top, border3D + _textInsets.left,
//					  _textInsets.bottom, _textInsets.right, 0, 0, 1.0, 1.0);
//		
//		// Add the vertical scrollbar
//		
//		addToGridBag (this, verticalScrollPanel, _NE, _V, 1, 0, 1, 1,
//					  border3D, 0, 0, border3D, 0, 0, 0.0, 1.0);
//		
//		// Add the horizontal scrollbar
//		
//		addToGridBag (this, horizontalScrollPanel, _SW, _H, 0, 1, 1, 1,
//					  0, border3D, border3D, 0, 0, 0, 1.0, 0.0);
		
		// Instead of a gridbag, use our own layout
		
		this.setLayout (new TextViewerLayout (new Insets (border3D, border3D,
														  border3D, border3D),
											  _textInsets,
											  _textViewerCanvas,
											  verticalScrollPanel,
											  horizontalScrollPanel ));
		
		// Add the components to the container
		
		this.add (_textViewerCanvas);
		this.add (verticalScrollPanel);
		this.add (horizontalScrollPanel);
		
		// Done
		
		return;
	}
	
	
	
	
	// ----- Painting routines -----
	
	
	// The paint function just defers to update.
	// This avoids a background flash.
	
	public void paint (Graphics g)
	{
		update (g);
		return;
	}
	
	
	// Draw the border and lower right corner.
	
	public void update (Graphics g)
	{
		
		// Set the color to light gray
		
		Color col = g.getColor();
		g.setColor (Color.lightGray);
		
		// Calculate our drawing area
		
		Insets ins = getInsets();
		int x = 0;
		int y = 0;
		int w = getSize().width - ins.left - ins.right;
		int h = getSize().height - ins.top - ins.bottom;
		
		// Draw the outer 3D border, shrinking the drawing area accordingly
		
		g.setColor (Color.lightGray);
		
		for (int i = 0; i < _outer3DBorder; ++i)
		{
			g.draw3DRect (x++, y++, w-1, h-1, false);
			w -= 2;
			h -= 2;
		}
		
		// Draw the inner 3D border, shrinking the drawing area accordingly
		
		g.setColor (Color.gray);
		
		for (int i = 0; i < _inner3DBorder; ++i)
		{
			g.draw3DRect (x++, y++, w-1, h-1, false);
			w -= 2;
			h -= 2;
		}
		
		// Fill the lower right corner, and shrink the drawing area
		
		g.setColor (Color.lightGray);
		
		int lrw = _verticalScrollbar.getSize().width;
		int lrh = _horizontalScrollbar.getSize().height;
		g.fillRect (x + w - lrw, y + h - lrh, lrw, lrh);
		
		w -= lrw;
		h -= lrh;
		
		// Fill the insets around the text area
		
		g.setColor (this.getBackground());
		
		if (_textInsets.top > 0)
		{
			g.fillRect (x, y, w, _textInsets.top);
		}
		
		if (_textInsets.bottom > 0)
		{
			g.fillRect (x, y + h - _textInsets.bottom, w, _textInsets.bottom);
		}
		
		if (_textInsets.left > 0)
		{
			g.fillRect (x, y, _textInsets.left, h);
		}
		
		if (_textInsets.right > 0)
		{
			g.fillRect (x + w - _textInsets.right, y, _textInsets.right, h);
		}
		
		// Restore the original color
		
		g.setColor (col);
		
		return;
	}
	
	
	
	
	// ----- MouseListener methods -----
	
	
	// The mouse is pressed on this component.
	//
	// We give the focus to the text canvas, and consume the event.

	public void mousePressed (MouseEvent evt)
	{
		_textViewerCanvas.requestFocus();
		return;
	}
	
	
	// The mouse is released on this component.

	public void mouseReleased (MouseEvent evt)
	{
		return;
	}
	
	
	// The mouse is clicked (pressed and released) on this component.

	public void mouseClicked (MouseEvent evt)
	{
		return;
	}
	
	
	// The mouse entered this component.

	public void mouseEntered (MouseEvent evt)
	{
		return;
	}
	
	
	// The mouse exited this component.

	public void mouseExited (MouseEvent evt)
	{
		return;
	}
	
	
	
	
	// ----- FocusListener methods -----
	
	
	// We received the focus.
	//
	// We pass off the focus to the text canvas.

	public void focusGained (FocusEvent evt)
	{
		_textViewerCanvas.requestFocus();
		return;
	}
	
	
	// We lost the focus.

	public void focusLost (FocusEvent evt)
	{
		return;
	}
	
	
	
	
	// ----- AdjustmentListener methods -----
	
	
	// A scrollbar value changed.

	public void adjustmentValueChanged (AdjustmentEvent evt)
	{
		
		// Check vertical scrollbar
		
		if (evt.getSource() == _verticalScrollbar)
		{
			_textViewerCanvas.requestFocus();
			_textViewerCanvas.updateVertical();
			return;
		}
		
		// Check horizontal scrollbar
		
		if (evt.getSource() == _horizontalScrollbar)
		{
			_textViewerCanvas.requestFocus();
			_textViewerCanvas.updateHorizontal();
			return;
		}
		
		// Everything else is ignored
		
		return;
	}
	
	
	
	
	// ----- Component methods -----
	
	
	// Our font is being changed.
	
	public void setFont (Font f)
	{
		super.setFont (f);
		_textViewerCanvas.setFont (f);
		return;
	}
	
	
	// Our background color is being changed.
	
	public void setBackground (Color c)
	{
		super.setBackground (c);
		_textViewerCanvas.setBackground (c);
		return;
	}
	
	
	// Our foreground color is being changed.
	
	public void setForeground (Color c)
	{
		super.setForeground (c);
		_textViewerCanvas.setForeground (c);
		return;
	}
	
	
	
	
	// ----- Client interface routines -----

	
	// Set the text contents of this panel.
	
	public void setText (String text)
	{
		if (text == null)
		{
			throw new IllegalArgumentException ("TextViewer.setText");
		}
		
		_textViewerCanvas.setText (text);
		return;
	}

	
	// Get the text contents of this panel.
	
	public String getText ()
	{
		return _textViewerCanvas.getText();
	}

	
	// Get the number of columns used to create the component.
	
	public int getColumns ()
	{
		return _columns;
	}

	
	// Get the number of rows used to create the component.
	
	public int getRows ()
	{
		return _rows;
	}
	
	
}




// TextViewerLayout is the layout manager for TextViewer.
//
// Actually, this layout can easily be accomplished with a GridBagLayout.
// This class exists to solve the following problem:  The text viewer
// canvas needs to know when it is resized so that it can update the text
// layout.  The traditional way for a component to be notified when it has
// been resized is to hook the component method doLayout().  But it turns out
// that Visual J++ 6.0 never calls doLayout() on a component that is not a
// container.
//
// With this layout manager, when the text viewer canvas is resized it
// receives a call to setBounds(), which is a method that it can hook.

class TextViewerLayout implements LayoutManager2
{
	
	// These insets describe the text viewer panel's border
	
	private Insets _panelBorder;
	
	// These insets describe the text border
	
	private Insets _textBorder;
	
	// The text canvas component
	
	private Component _textCanvas;
	
	// The vertical scrollbar component
	
	private Component _verScroll;
	
	// The horizontal scrollbar component
	
	private Component _horScroll;
	
	
	// The constructor just saves its arguments
	
	public TextViewerLayout (Insets panelBorder, Insets textBorder,
							 Component textCanvas, Component verScroll, Component horScroll)
	{
		super();
		
		_panelBorder = panelBorder;
		_textBorder = textBorder;
		_textCanvas = textCanvas;
		_verScroll = verScroll;
		_horScroll = horScroll;
		
		return;
	}
	
	
	// Get the preferred layout size.
	
	public Dimension preferredLayoutSize (Container parent)
	{
		
		// Need to include the container's insets
		
		Insets ins = parent.getInsets();
		
		// Get component sizes
		
		Dimension prefText = _textCanvas.getPreferredSize();
		Dimension prefVer = _verScroll.getPreferredSize();
		Dimension prefHor = _horScroll.getPreferredSize();
		
		// Return preferred layout size
		
		return new Dimension (ins.left + ins.right + _panelBorder.left + _panelBorder.right
								+ Math.max (_textBorder.left + _textBorder.right
												+ prefText.width + prefVer.width,
											prefHor.width + prefVer.width),
							  ins.top + ins.bottom + _panelBorder.top + _panelBorder.bottom
								+ Math.max (_textBorder.top + _textBorder.bottom
												+ prefText.height + prefHor.height,
											prefVer.height + prefHor.height) );
	}
	
	
	// Get the minimum layout size.
	
	public Dimension minimumLayoutSize (Container parent)
	{
		
		// Need to include the container's insets
		
		Insets ins = parent.getInsets();
		
		// Get component sizes
		
		Dimension prefText = _textCanvas.getPreferredSize();
		Dimension prefVer = _verScroll.getPreferredSize();
		Dimension prefHor = _horScroll.getPreferredSize();
		
		Dimension minText = _textCanvas.getMinimumSize();
		Dimension minVer = _verScroll.getMinimumSize();
		Dimension minHor = _horScroll.getMinimumSize();
		
		// Return minimum layout size
		
		return new Dimension (ins.left + ins.right + _panelBorder.left + _panelBorder.right
								+ Math.max (_textBorder.left + _textBorder.right
												+ minText.width + prefVer.width,
											minHor.width + prefVer.width),
							  ins.top + ins.bottom + _panelBorder.top + _panelBorder.bottom
								+ Math.max (_textBorder.top + _textBorder.bottom
												+ minText.height + prefHor.height,
											minVer.height + prefHor.height) );
	}
	
	
	// Get the minimum layout size.
	
	public Dimension maximumLayoutSize (Container parent)
	{
		
		// Need to include the container's insets
		
		Insets ins = parent.getInsets();
		
		// Get component sizes
		
		Dimension prefText = _textCanvas.getPreferredSize();
		Dimension prefVer = _verScroll.getPreferredSize();
		Dimension prefHor = _horScroll.getPreferredSize();
		
		Dimension maxText = _textCanvas.getMaximumSize();
		Dimension maxVer = _verScroll.getMaximumSize();
		Dimension maxHor = _horScroll.getMaximumSize();
		
		// Return maximum layout size
		
		return new Dimension (ins.left + ins.right + _panelBorder.left + _panelBorder.right
								+ Math.max (_textBorder.left + _textBorder.right
												+ maxText.width + prefVer.width,
											maxHor.width + prefVer.width),
							  ins.top + ins.bottom + _panelBorder.top + _panelBorder.bottom
								+ Math.max (_textBorder.top + _textBorder.bottom
												+ maxText.height + prefHor.height,
											maxVer.height + prefHor.height) );
	}
	
	
	// Add a component to the container.
	
	public void addLayoutComponent (String name, Component comp)
	{
		return;
	}
	
	
	// Add a component to the container.
	
	public void addLayoutComponent (Component comp, Object constraints)
	{
		return;
	}
	
	
	// Remove a component from the container.
	
	public void removeLayoutComponent (Component comp)
	{
		return;
	}
	
	
	// Get X alignment value
	
	public float getLayoutAlignmentX (Container cont)
	{
		return 0.5f;
	}
	
	
	// Get Y alignment value
	
	public float getLayoutAlignmentY (Container cont)
	{
		return 0.5f;
	}
	
	
	// Invalidate the layout
	
	public void invalidateLayout (Container cont)
	{
		return;
	}
	
	
	// Lay out the container.
	
	public void layoutContainer (Container parent)
	{
		
		// Need to include the container's insets
		
		Insets ins = parent.getInsets();
		
		// Get scrollbar preferred sizes
		
		Dimension prefVer = _verScroll.getPreferredSize();
		Dimension prefHor = _horScroll.getPreferredSize();
		
		// Get the parent's size, which includes its insets
		
		Dimension parentSize = parent.getSize();
		
		// Lay out the text canvas
		
		_textCanvas.setBounds (ins.left + _panelBorder.left + _textBorder.left,
							   ins.top + _panelBorder.top + _textBorder.top,
							   Math.max (1, parentSize.width - prefVer.width
											  - ins.left - ins.right
											  - _panelBorder.left - _panelBorder.right
											  - _textBorder.left - _textBorder.right),
							   Math.max (1, parentSize.height - prefHor.height
											  - ins.top - ins.bottom
											  - _panelBorder.top - _panelBorder.bottom
											  - _textBorder.top - _textBorder.bottom) );
		
		// Lay out the vertical scrollbar
		
		_verScroll.setBounds (parentSize.width - prefVer.width
								  - ins.right - _panelBorder.right,
							  ins.top + _panelBorder.top,
							  prefVer.width,
							  Math.max (1, parentSize.height - prefHor.height
											  - ins.top - ins.bottom
											  - _panelBorder.top - _panelBorder.bottom) ); 
		
		// Lay out the horizontal scrollbar
		
		_horScroll.setBounds (ins.left + _panelBorder.left,
							  parentSize.height - prefHor.height
								  - ins.bottom - _panelBorder.bottom,
							  Math.max (1, parentSize.width - prefVer.width
											  - ins.left - ins.right
											  - _panelBorder.left - _panelBorder.right),
							  prefHor.height );
		
		// Done
		
		return;
	}
	
	
}




// TextViewerCanvas is the component that is used for drawing text.

class TextViewerCanvas extends Canvas
	implements MouseListener, FocusListener, KeyListener
{
	
	// ----- Layout parameters -----
	
	// The number of rows, in characters
	
	private int _rows;
	
	// The number of columns, in characters (column = width of 'm')
	
	private int _columns;
	
	
	
	
	// ----- Associated components -----
	
	// The vertical scrollbar
	
	private Scrollbar _verticalScrollbar;
	
	// The horizontal scrollbar
	
	private Scrollbar _horizontalScrollbar;
	
	// This variable is true if we have Java 1.1 type scrollbars.
	// In Java 1.0, a scrollbar value can range from minimum to maximum,
	// and the fraction displayed is visible/(maximum+visible).
	// In Java 1.1, a scrollbar value can range from minimum to
	// maximum-visible, and the fraction displayed is visible/maximum.
	//
	// Note that VJ++ 6.0 has Java 1.0 type scrollbars, even through it is
	// supposed to be a Java 1.1 compliant environment.  Also note that
	// the Scrollbar example in the Java 1.0 API spec calculates scrollbar
	// limits as if the author expects a Java 1.1 type scrollbar.  We
	// conclude that scrollbar type may vary randomly among platforms, and
	// so we test the scrollbars to determine dynamically which type they are.
	
	private boolean _java11Scroll = false;
	
	
	
	
	// ----- Component method variables -----
	
	// This variable is true if we currently have the input focus
	
	private boolean _hasFocus = false;
	
	
	
	
	// ----- Layout variables -----
	
	// Note: Access to all layout variables must be protected by
	// synchronization.  All layout variables of reference type must be
	// treated as immutable;  changing the value requires creation of a
	// new object.
	
	// The current text contents, must be non-null.
	
	private String _text;
	
	// The indexes of all line-ends.
	// The first element of _lineEnds is always -1 (which indicates that the
	// first line of text begins at index 0).
	// Subsequent elements of _lineEnds are the indexes of every character in
	// _text that is \n.
	// Also, if _text is nonempty and its last character is not \n then the
	// last element of _lineEnds is the length of _text.
	// The number of lines is _lineEnds.elementCount()-1, and each line is
	// delimited by two successive elements of _lineEnds.
	
	private IntDeque _lineEnds;
	
	// The rectangular region in which the text is displayed.
	
	private Rectangle _viewport = new Rectangle (0, 0, 0, 0);
	
	// The current scrolling position.
	// The x coordinate is the column at the left of the viewport.
	// The y coordinate is the row at the top of the viewport.
	
	private Point _scroll = new Point (0, 0);
	
	// The current font.
	
	private Font _font;
	
	// The current font metrics.
	
	private FontMetrics _fm;
									   
	// The size of a text cell.
	// This determines the size of rows and columns, in pixels, that are
	// used as units of scrolling.  For example, a change of 1 in the value
	// of the vertical scrollbar moves the image _textcell.height pixels.
	// We compute the width as the minimum of the width of the character 'm'
	// and the width of the viewport.
	// We compute the height as the minimum of the height of a line of text
	// and the height of the viewport.
	// In any case, the width and height are each guaranteed to be positive.
	
	private Dimension _textcell = new Dimension (1, 1);
	
	// The number of visible text cells.
	// The width is the number of whole text cells that fit horizontally in
	// the viewport (rounded down).
	// The height is the number of whole text cells that fit vertically in
	// the viewport (rounded down).
	// In any case, the width and height are each guaranteed to be positive.
	
	private Dimension _visibleCells = new Dimension (1, 1);
	
	// The ascent of the current font.
	
	private int _fontAscent = 0;
	
	// The height (line spacing) of the current font.
	// This is guaranteed to be positive.
	
	private int _fontHeight = 1;
	
	// The current number of columns in the text.
	// We compute this as the maximum number of columns, rounded up, of any
	// line of text that currently intersects the viewport.
	
	private int _currentColumns = 0;
	
	
	
	
	// ----- Painting methods -----
	
	
	// Paint text in the window.
	
	private static void paintText (Graphics g, Rectangle clip,
								   String text, IntDeque lineEnds,
								   Rectangle viewport, Dimension textcell, Point scroll,
								   Color fgColor, Color bgColor,
								   Font font, int fontHeight, int fontAscent)
	{
		
		// Set the clipping rectangle in the graphics context
		
		g.clipRect (clip.x, clip.y, clip.width, clip.height);
		
		// Set the font in the graphics context
		
		g.setFont (font);
		
		// Loop over all lines of text that intersect the clipping rectangle
		
		int firstTextPixel = (scroll.y * textcell.height) + clip.y - viewport.y;
		
		for (int line = firstTextPixel / fontHeight;
			 (line * fontHeight) < firstTextPixel + clip.height;
			 ++line )
		{
			
			// Clear the background of the line
			
			int topLinePixel = clip.y + (line * fontHeight) - firstTextPixel;
			
			g.setColor (bgColor);
			g.fillRect (clip.x, topLinePixel, clip.width, fontHeight);
			
			// If the line is within the text ...
			
			if (line >= 0 && line < lineEnds.elementCount() - 1)
			{
				
				// Get the line as a string
				
				String lineString = text.substring (lineEnds.peekFirst (line) + 1,
													lineEnds.peekFirst (line + 1) );
				
				// Draw the characters
				
				g.setColor (fgColor);
				g.drawString (lineString,
							  viewport.x - (scroll.x * textcell.width),
							  topLinePixel + fontAscent );
			}
		}
		
		// Done
		
		return;
	}
	
	
	// The paint function just defers to update.
	// This avoids a background flash.
	
	public void paint (Graphics g)
	{
		update (g);
		return;
	}
	
	
	// Draw the text.
	
	public void update (Graphics g)
	{
		
		// Get our layout variables
		
		String text;
		IntDeque lineEnds;
		Rectangle viewport;
		Dimension textcell;
		Point scroll;
		Font font;
		int fontHeight;
		int fontAscent;
		
		synchronized (this)
		{
			text = _text;
			lineEnds = _lineEnds;
			viewport = _viewport;
			textcell = _textcell;
			scroll = _scroll;
			font = _font;
			fontHeight = _fontHeight;
			fontAscent = _fontAscent;
		}
		
		// Get the clipping rectangle
		
		Rectangle clip = g.getClipBounds();
		
		if (clip == null)
		{
			clip = new Rectangle (this.getSize());
		}
		
		// Paint the text
		
		paintText (g, clip.intersection(viewport),
				   text, lineEnds,
				   viewport, textcell, scroll,
				   this.getForeground(), this.getBackground(),
				   font, fontHeight, fontAscent);
		
		// Done
		
		return;
	}
	
	
	
	
	// ----- Component methods -----
	
	
	// The mouse is pressed on this component.
	//
	// We give ourself the focus.

	public void mousePressed (MouseEvent evt)
	{
		this.requestFocus();
		return;
	}
	
	
	// The mouse is released on this component.

	public void mouseReleased (MouseEvent evt)
	{
		return;
	}
	
	
	// The mouse is clicked (pressed and released) on this component.

	public void mouseClicked (MouseEvent evt)
	{
		return;
	}
	
	
	// The mouse entered this component.

	public void mouseEntered (MouseEvent evt)
	{
		return;
	}
	
	
	// The mouse exited this component.

	public void mouseExited (MouseEvent evt)
	{
		return;
	}
	
	
	
	
	// ----- FocusListener methods -----
	
	
	// We received the focus.

	public void focusGained (FocusEvent evt)
	{
		_hasFocus = true;
		return;
	}
	
	
	// We lost the focus.

	public void focusLost (FocusEvent evt)
	{
		_hasFocus = false;
		return;
	}
	
	
	
	
	// ----- KeyListener methods -----
	
	
	// A key is pressed.
	
	public void keyPressed (KeyEvent evt)
	{
		switch (evt.getKeyCode())
		{
			
		case KeyEvent.VK_HOME:
			
			// For home, adjust the horizontal scrollbar
			
			_horizontalScrollbar.setValue (_horizontalScrollbar.getMinimum());
			updateHorizontal();
			evt.consume();
			return;
			
		case KeyEvent.VK_END:
			
			// For end, adjust the horizontal scrollbar
			
			_horizontalScrollbar.setValue (_horizontalScrollbar.getMaximum());
			updateHorizontal();
			evt.consume();
			return;
			
		case KeyEvent.VK_PAGE_DOWN:
			
			// For page down, adjust the vertical scrollbar
			
			if (evt.isControlDown())
			{
				_verticalScrollbar.setValue (_verticalScrollbar.getMaximum());
			}
			else
			{
				_verticalScrollbar.setValue (_verticalScrollbar.getValue()
											 + _verticalScrollbar.getBlockIncrement());
			}
			updateVertical();
			evt.consume();
			return;
			
		case KeyEvent.VK_PAGE_UP:
			
			// For page up, adjust the vertical scrollbar
			
			if (evt.isControlDown())
			{
				_verticalScrollbar.setValue (_verticalScrollbar.getMinimum());
			}
			else
			{
				_verticalScrollbar.setValue (_verticalScrollbar.getValue()
											 - _verticalScrollbar.getBlockIncrement());
			}
			updateVertical();
			evt.consume();
			return;
			
		case KeyEvent.VK_DOWN:
			
			// For arrow down, adjust the vertical scrollbar
			
			if (evt.isControlDown())
			{
				_verticalScrollbar.setValue (_verticalScrollbar.getValue()
											 + _verticalScrollbar.getBlockIncrement());
			}
			else
			{
				_verticalScrollbar.setValue (_verticalScrollbar.getValue()
											 + _verticalScrollbar.getUnitIncrement());
			}
			updateVertical();
			evt.consume();
			return;
			
		case KeyEvent.VK_UP:
			
			// For arrow up, adjust the vertical scrollbar
			
			if (evt.isControlDown())
			{
				_verticalScrollbar.setValue (_verticalScrollbar.getValue()
											 - _verticalScrollbar.getBlockIncrement());
			}
			else
			{
				_verticalScrollbar.setValue (_verticalScrollbar.getValue()
											 - _verticalScrollbar.getUnitIncrement());
			}
			updateVertical();
			evt.consume();
			return;
			
		case KeyEvent.VK_RIGHT:
			
			// For arrow right, adjust the horizontal scrollbar
			
			if (evt.isControlDown())
			{
				_horizontalScrollbar.setValue (_horizontalScrollbar.getValue()
											   + _horizontalScrollbar.getBlockIncrement());
			}
			else
			{
				_horizontalScrollbar.setValue (_horizontalScrollbar.getValue()
											   + _horizontalScrollbar.getUnitIncrement());
			}
			updateHorizontal();
			evt.consume();
			return;
			
		case KeyEvent.VK_LEFT:
			
			// For arrow left, adjust the horizontal scrollbar
			
			if (evt.isControlDown())
			{
				_horizontalScrollbar.setValue (_horizontalScrollbar.getValue()
											   - _horizontalScrollbar.getBlockIncrement());
			}
			else
			{
				_horizontalScrollbar.setValue (_horizontalScrollbar.getValue()
											   - _horizontalScrollbar.getUnitIncrement());
			}
			updateHorizontal();
			evt.consume();
			return;
		}
		
		// Anything else is ignored
		
		return;	
	}
	
	
	// A key is released.
	
	public void keyReleased (KeyEvent evt)
	{
		return;	
	}
	
	
	// A key is typed (pressed, and generates a character code).
	
	public void keyTyped (KeyEvent evt)
	{
		return;	
	}
	
	
	
	
	// ----- Component methods -----
	
	
	// For Java 1.1, this function indicates we can receive the focus
	// in response to tab or shift-tab.  For Java 1.0, this function
	// is never called and does nothing.
	
	public boolean isFocusTraversable ()
	{
		return true;
	}
	
	
	// We have been resized.
	
	public void setBounds (int x, int y, int width, int height)
	{
	
		// Pass through to our superclass
		
		super.setBounds (x, y, width, height);
		
		// Recalculate our layout and scrollbars
		
		updateLayout();
		return;
	}
	
	
	// Our font is being changed.
	
	public void setFont (Font f)
	{
		
		// Pass through to our superclass
		
		super.setFont (f);
		
		// Recalculate our layout and scrollbars
		
		updateLayout();
		return;
	}
	
	
	// Our preferred size is being requested, Java 1.0 version.
	// Note:  According to the Java 1.1 spec, the following "@deprecated" tag
	// should prevent the compiler from issuing a deprecation warning on the
	// method preferredSize().  But Visual J++ 6.0 issues the deprecation
	// warning anyway.
	
	/** @deprecated */
	public Dimension preferredSize ()
	{
		
		// Get the font
		
		Font f = this.getFont();
		if (f == null)
		{
			f =  new Font ("Dialog", Font.PLAIN, 12);
		}
		
		// Get the font metrics
		
		FontMetrics fm = this.getFontMetrics (f);
		
		// Return preferred size
		
		return new Dimension (Math.max (1, Math.max (1, _columns) * fm.charWidth('m')),
							  Math.max (1, Math.max (1, _rows) * fm.getHeight()) );
	}
	
	
	// Our preferred size is being requested, Java 1.1 version.
	
	public Dimension getPreferredSize ()
	{
		
		// Get the font
		
		Font f = this.getFont();
		if (f == null)
		{
			f =  new Font ("Dialog", Font.PLAIN, 12);
		}
		
		// Get the font metrics
		
		FontMetrics fm = this.getFontMetrics (f);
		
		// Return preferred size
		
		return new Dimension (Math.max (1, Math.max (1, _columns) * fm.charWidth('m')),
							  Math.max (1, Math.max (1, _rows) * fm.getHeight()) );
	}
	
	
	// Our minimum size is being requested, Java 1.0 version.
	// Note:  According to the Java 1.1 spec, the following "@deprecated" tag
	// should prevent the compiler from issuing a deprecation warning on the
	// method minimumSize().  But Visual J++ 6.0 issues the deprecation
	// warning anyway.
	
	/** @deprecated */
	public Dimension minimumSize ()
	{
		
		// Get the font
		
		Font f = this.getFont();
		if (f == null)
		{
			f =  new Font ("Dialog", Font.PLAIN, 12);
		}
		
		// Get the font metrics
		
		FontMetrics fm = this.getFontMetrics (f);
		
		// Return minimum size
		
		return new Dimension (Math.max (1, fm.charWidth('m')),
							  Math.max (1, fm.getHeight()) );
	}
	
	
	// Our minimum size is being requested, Java 1.1 version.
	
	public Dimension getMinimumSize ()
	{
		
		// Get the font
		
		Font f = this.getFont();
		if (f == null)
		{
			f =  new Font ("Dialog", Font.PLAIN, 12);
		}
		
		// Get the font metrics
		
		FontMetrics fm = this.getFontMetrics (f);
		
		// Return minimum size
		
		return new Dimension (Math.max (1, fm.charWidth('m')),
							  Math.max (1, fm.getHeight()) );
	}
	
	
	
	
	// ----- Layout functions -----
	
	
	// Calculate the line ends for a given string.
	//
	// The text parameter must be non-null.
	
	private static IntDeque calcLineEnds (String text)
	{
		
		// Allocate the deque
		
		IntDeque lineEnds = new IntDeque ();
		
		// The first element is always -1
		
		lineEnds.pushLast (-1);
		
		// Find all the line ends
		
		for (int i = 0; i < text.length(); ++i)
		{
			i = text.indexOf ('\n', i);
			if (i < 0)
			{
				i = text.length();
			}
			lineEnds.pushLast (i);
		}
		
		// Done
		
		return lineEnds;
	}
			
	
	// Update the current number of columns in the text.
	//
	// This function must be called whenever the vertical scroll position
	// changes, and whenever there is a change in size, text, or font.
	
	private synchronized void updateCurrentColumns ()
	{
		
		// Initialize maximum width in pixels
		
		int maxPixels = 0;
		
		// Loop over all lines of text that intersect the viewport
		
		int firstTextPixel = _scroll.y * _textcell.height;
		
		for (int line = firstTextPixel / _fontHeight;
			 (line * _fontHeight) < firstTextPixel + _viewport.height;
			 ++line )
		{
			
			// If the line is within the text ...
			
			if (line >= 0 && line < _lineEnds.elementCount() - 1)
			{
				
				// Get the line as a string
				
				String lineString = _text.substring (_lineEnds.peekFirst (line) + 1,
													 _lineEnds.peekFirst (line + 1) );
				
				// Adjust the maximum width
				
				maxPixels = Math.max (maxPixels, _fm.stringWidth (lineString));
			}
		}
		
		// Return the number of columns, rounding up
		
		_currentColumns = (maxPixels + _textcell.width - 1) / _textcell.width;
		return;
	}
	
	
	// Repaint the viewport.
	
	private void repaintViewport (Rectangle viewport)
	{
		repaint (viewport.x, viewport.y, viewport.width, viewport.height);
		return;
	}
	
	
	// Update the vertical scroll position.
	
	public void updateVertical ()
	{
		Rectangle viewport;
		
		synchronized (this)
		{
			
			// Save the viewport
			
			viewport = _viewport;
		
			// Read the vertical scrollbar to get the updated scroll position.
			// Note:  There is some variation among platforms as to the
			// actual maximum value that a scrollbar can return.
			
			int vval = _verticalScrollbar.getValue();
		
			// If the position is unchanged, do nothing
		
			if (vval == _scroll.y)
			{
				return;
			}
		
			// Establish the new scroll position
		
			_scroll = new Point (_scroll.x, vval);
			
			// Update the current number of columns
			
			updateCurrentColumns();
				
			// Update the horizontal scrollbar maximum to be the greater of
			// the current number of columns and the number of columns before
			// the right edge of the viewport
				
			int hmax = Math.max (_currentColumns, _scroll.x + _visibleCells.width)
							- (_java11Scroll ? 0 : _visibleCells.width);
				
			if (hmax != _horizontalScrollbar.getMaximum())
			{
				_horizontalScrollbar.setValues (_horizontalScrollbar.getValue(),
												_horizontalScrollbar.getVisibleAmount(),
												_horizontalScrollbar.getMinimum(),
												hmax );
				
				// Hack.  On Microsoft VMs, calling setValues() can make the
				// scrollbar forget its page increment value, so we restore it.
				
				_horizontalScrollbar.setUnitIncrement (1);
				_horizontalScrollbar.setBlockIncrement (Math.min (20, _visibleCells.width));
			}

			// Hack.  See the note regarding Internet Explorer 4.01 in the
			// updateHorizontal() function below.  Although we have not
			// observed the same problem in the vertical scrollbar, as a
			// precaution we restore its page increment value.
			
			_verticalScrollbar.setUnitIncrement (1);
			_verticalScrollbar.setBlockIncrement (_visibleCells.height);
		}
		
		// Repaint our window
		
		repaintViewport (viewport);
		return;
	}
	
	
	// Update the horizontal scroll position.
	
	public void updateHorizontal ()
	{
		Rectangle viewport;
		
		synchronized (this)
		{
			
			// Save the viewport
			
			viewport = _viewport;
		
			// Read the horizontal scrollbar to get the updated scroll position.
			// Note:  There is some variation among platforms as to the
			// actual maximum value that a scrollbar can return.
			
			int hval = _horizontalScrollbar.getValue();
		
			// If the position is unchanged, do nothing
		
			if (hval == _scroll.x)
			{
				return;
			}
		
			// Establish the new scroll position
		
			_scroll = new Point (hval, _scroll.y);
				
			// Update the horizontal scrollbar maximum to be the greater of
			// the current number of columns and the number of columns before
			// the right edge of the viewport, but only if it decreases
				
			int hmax = Math.max (_currentColumns,  _scroll.x + _visibleCells.width)
							- (_java11Scroll ? 0 : _visibleCells.width);
				
			if (hmax < _horizontalScrollbar.getMaximum())
			{
				_horizontalScrollbar.setValues (_horizontalScrollbar.getValue(),
												_horizontalScrollbar.getVisibleAmount(),
												_horizontalScrollbar.getMinimum(),
												hmax );
			}
			
			// Hack.  On Internet Explorer 4.01, any change in the value of
			// the horizontal scrollbar can cause it to forget its page
			// increment value.  This seems to occur on the first change in
			// value after the scrollbar is switched from disabled
			// (maximum==minimum) to enabled (maximum>minimum), and it occurs
			// regardless of whether the change in value is caused by a call to
			// setValue() or by the user clicking on the scrollbar.  So, we
			// restore the page increment on every change in value.
			
			_horizontalScrollbar.setUnitIncrement (1);
			_horizontalScrollbar.setBlockIncrement (Math.min (20, _visibleCells.width));
		}
		
		// Repaint our window
		
		repaintViewport (viewport);
		return;
	}
	
	
	// Calculate the text layout.
	//
	// This function needs to be called if there is any change in the
	// component's size, font, or text.
	
	private synchronized void calcLayout ()
	{
		
		// Get the number of pixels between the left edge of the text and
		// the right edge of the viewport, in the old layout
		
		int oldPixelWidth = (_scroll.x * _textcell.width) + _viewport.width;
		
		// Save some of the old layout values
		
		int oldFontHeight = _fontHeight;
		Dimension oldTextcell = _textcell;
		
		// Update the viewport according to our size
		
		_viewport = new Rectangle (this.getSize());
			
		// Update the font according to our current font
		
		_font = this.getFont();
		
		// If no font is set for this component or any of its ancestors,
		// use a default font
		
		if (_font == null)
		{
			_font = new Font ("Dialog", Font.PLAIN, 12);
		}
			
		// Update the font metrics according to our peer
			
		_fm = this.getFontMetrics (_font);
		
		// Calculate the size of a text cell.
		// Note: It is required that the text cell width depend only on the viewport
		// width and the font widths.  Likewise, it is required that the text cell
		// height depend only on the viewport height and the font height.
		
		_textcell = new Dimension (Math.max (1, Math.min (_viewport.width, _fm.charWidth('m'))),
								   Math.max (1, Math.min (_viewport.height, _fm.getHeight())) );
		
		// Calculate the ascent of the current font
		
		_fontAscent = _fm.getAscent();
		
		// Calculate the height (line spacing) of the current font
		
		_fontHeight = _fm.getHeight();
		
		// Calculate the number of visible text cells
		
		_visibleCells = new Dimension (Math.max (1, _viewport.width / _textcell.width),
									   Math.max (1, _viewport.height / _textcell.height));
		
		// Create a new object to hold the new scroll position, and scale the
		// scroll values from the old layout to the new layout.
		// The horizontal scroll position is scaled so that it represents the
		// same number of pixels.  It is required that the horizontal scroll
		// position be unchanged if the viewport width and font widths are
		// unchanged.
		// The vertical scroll position is scaled so that it represents the
		// same number of lines of text.  It is required that the vertical
		// scroll position be unchanged if the viewport height and font height
		// are unchanged.
		
		_scroll = new Point ((_scroll.x * oldTextcell.width) / _textcell.width,
					(int)( ((long)_scroll.y * (long)oldTextcell.height * (long)_fontHeight)
						   / ((long)oldFontHeight * (long)_textcell.height) ) );
		
		// This is the number of rows in the entire text, rounded up
		
		int textRows = ( ((_lineEnds.elementCount() - 1) * _fontHeight)
						 + _textcell.height - 1 ) / _textcell.height;
		
		// Update the vertical scroll position.
		// We want to leave the scroll position unchanged, unless the bottom
		// edge of the viewport extends past the end of the text, in which
		// case we scroll up accordingly. 
		
		_scroll.y = Math.min (_scroll.y, Math.max (0, textRows - _visibleCells.height));
			
		// Update the current number of columns in the text
			
		updateCurrentColumns();
		
		// Update the horizontal scroll position.
		// We want to leave the scroll position unchanged, unless the right
		// edge of the viewport extends past the right edge of the text and
		// past the rightmost pixel visible in the old layout, in which case
		// we scroll left accordingly. 
		
		_scroll.x = Math.min (_scroll.x, Math.max (0,
								Math.max (_currentColumns, oldPixelWidth / _textcell.width)
								- _visibleCells.width ));
		
		// Update the vertical scrollbar values
		
		_verticalScrollbar.setValues (_scroll.y,
									  _visibleCells.height,
									  0,
									  Math.max (textRows,
												_scroll.y + _visibleCells.height)
									  - (_java11Scroll ? 0 : _visibleCells.height) );
		_verticalScrollbar.setUnitIncrement (1);
		_verticalScrollbar.setBlockIncrement (_visibleCells.height);
		
		// Update the horizontal scrollbar values
		
		_horizontalScrollbar.setValues (_scroll.x,
										_visibleCells.width,
										0,
										Math.max (_currentColumns,
												  _scroll.x + _visibleCells.width)
										- (_java11Scroll ? 0 : _visibleCells.width) );
		_horizontalScrollbar.setUnitIncrement (1);
		_horizontalScrollbar.setBlockIncrement (Math.min (20, _visibleCells.width));
		
		// Done
		
		return;
	}
	
	
	// Recalculate the layout of this panel.
	
	public void updateLayout ()
	{
		
		// Recalculate our layout and scrollbars
		
		synchronized (this)
		{
			calcLayout();
		}
		
		// Repaint our entire window
		
		repaint();
		return;
	}

	
	// Set the text contents of this panel.
	
	public void setText (String text)
	{
		Rectangle viewport;
		
		// Calculate the line ends
		
		IntDeque lineEnds = calcLineEnds (text);
		
		synchronized (this)
		{
			
			// Establish the new text
			
			_text = text;
			_lineEnds = lineEnds;
			
			// Scroll back to upper left corner
			
			_scroll = new Point (0, 0);
			
			// Calculate the new layout
			
			calcLayout();
			
			// Save the viewport
			
			viewport = _viewport;
		}
		
		// Repaint our window
		
		repaintViewport (viewport);
		return;
	}

	
	// Get the text contents of this panel.
	
	public synchronized String getText ()
	{
		return _text;
	}
		
	
	
	
	// ----- Constructor -----
	
	
	// Constructor saves layout parameters and initial text, and performs
	// initial size calculations.
	
	public TextViewerCanvas (String text, int rows, int cols,
							 Scrollbar verticalScrollbar, Scrollbar horizontalScrollbar)
	{
		super();
		
		// Save layout parameters
		
		_rows = rows;
		_columns = cols;
		
		// Save associated components
		
		_verticalScrollbar = verticalScrollbar;
		_horizontalScrollbar = horizontalScrollbar;
		
		// Test the vertical scrollbar to find out which type we have
		
		_verticalScrollbar.setValues (0, 50, 0, 100);
		_verticalScrollbar.setValue (100);
		
		if (_verticalScrollbar.getValue() <= 50)
		{
			_java11Scroll = true;
		}
		
		// Set up our cursor
		
		this.setCursor (Cursor.getPredefinedCursor (Cursor.TEXT_CURSOR));
		
		// Add our event listeners
		
		this.addFocusListener (this);
		this.addMouseListener (this);
		this.addKeyListener (this);
		
		// Set the text and calculate the initial layout
		
		setText (text);
		
		// Done
		
		return;
	}
	
	
}
