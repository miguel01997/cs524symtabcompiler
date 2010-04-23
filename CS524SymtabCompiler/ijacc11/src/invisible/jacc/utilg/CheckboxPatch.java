// Copyright 1998 Invisible Software, Inc.

package invisible.jacc.utilg;

import java.awt.*;
import java.awt.event.*;


/*->

CheckboxPatch is a subclass of java.awt.Checkbox that exists only to patch a
bug in Microsoft Visual J++ 6.0.

In VJ++ 6.0, if a Checkbox is "on" at the time its peer is created, the state
may change spontaneously to "off".  The problem is intermittent, and appears to
occur about 1 in 5 times when running under the debugger, and about 1 in 40
times when running stand-alone.

We patch the bug by hooking the addNotify() method and making sure that the
state is preserved across the call.

->*/


public class CheckboxPatch extends Checkbox
{
	public CheckboxPatch (String label, CheckboxGroup group, boolean state)
	{
		super (label, group, state);
		return;
	}
	
	public CheckboxPatch (String label)
	{
		super (label);
		return;
	}
	
	public CheckboxPatch ()
	{
		super ();
		return;
	}
	

	public void addNotify ()
	{
		boolean state = getState();
		super.addNotify();
		setState (state);
		return;
	}
}
