package lslforge;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {

	public PropertyTester() {
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (receiver instanceof IAdaptable) {
			LSLForgeElement element = ((IAdaptable)receiver).getAdapter(LSLForgeElement.class);
			try {
				return element != null &&
				    element.getResource().getProject().getNature("LSLForge") != null; //$NON-NLS-1$
			} catch (CoreException e) {
				return false;
			}
		} else {
		    return false;
		}
	}

}
