package lslforge.outline.items;

import org.eclipse.swt.graphics.Image;

import lslforge.LSLForgePlugin;

public class State extends OutlineItem
{
	private static final Image IMAGE = LSLForgePlugin.createImage("icons/state.gif"); //$NON-NLS-1$

	public State(String name) {
		super(name, IMAGE, 0, 0);
	}

	public State(String name, int start, int end) {
		super(name, IMAGE, start, end);
	}
}
