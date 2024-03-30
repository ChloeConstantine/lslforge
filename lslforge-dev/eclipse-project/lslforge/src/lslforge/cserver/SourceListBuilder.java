package lslforge.cserver;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import lslforge.LSLForgeElement;
import lslforge.LSLProjectNature;
import lslforge.generated.Tuple2;
import lslforge.generated.Tuple3;
import lslforge.util.Log;

public class SourceListBuilder implements IResourceVisitor {
	private HashMap<String,String> moduleMap = new HashMap<>();
	private HashMap<String,String> moduleNameToPath = new HashMap<>();
	private HashMap<String,String> scriptMap = new HashMap<>();
	private HashMap<String,String> scriptNameToPath = new HashMap<>();
	private boolean optimize;
	private boolean modulesOnly = true;

	public SourceListBuilder(boolean addOptimizeOption) {
	    optimize = addOptimizeOption;
	}

	public String getModulePath(String name) {
		return moduleNameToPath.get(name);
	}

	public String getScriptPath(String name) {
		return scriptNameToPath.get(name);
	}

	@Override
	public boolean visit(IResource resource) throws CoreException {
		LSLForgeElement element = resource.getAdapter(LSLForgeElement.class);

		if (element != null) {
			IFile f = (IFile) resource;
			IPath p = f.getLocation();
			IPath pp = f.getProjectRelativePath();
			String name = LSLProjectNature.resourceToLSLForgeName(resource);

			if (element.isModule()) {
			    moduleNameToPath.put(name,pp.toString());
			    moduleMap.put(name,p.toOSString());
			} else if (element.isScript(true) && !modulesOnly) {
				scriptNameToPath.put(name, pp.toString());
				scriptMap.put(name, p.toOSString());
			}
		}
		return true;
	}

	public Tuple3<Boolean,
		LinkedList<Tuple2<String, String>>,
		LinkedList<Tuple2<String,String>>> compilationInfo() {

		Log.debug("Building source list"); //$NON-NLS-1$

		Tuple3<Boolean,
		LinkedList<Tuple2<String, String>>,
		LinkedList<Tuple2<String,String>>> result =
			new Tuple3<>();

		result.el1 = optimize;

		LinkedList<Tuple2<String,String>> modules = new LinkedList<>();
		for (Map.Entry<String, String> entry : moduleMap.entrySet()) {
			Tuple2<String,String> tup = new Tuple2<>();
			Log.debug("Adding " + entry.getKey() + " to modules"); //$NON-NLS-1$ //$NON-NLS-2$
			tup.el1 = entry.getKey();
			tup.el2 = entry.getValue();
			modules.add(tup);
		}

		result.el2 = modules;
		LinkedList<Tuple2<String,String>> scripts = new LinkedList<>();
		for (Map.Entry<String, String> entry : scriptMap.entrySet()) {
			Tuple2<String,String> tup = new Tuple2<>();
			Log.debug("Adding " + entry.getKey() + " to scripts"); //$NON-NLS-1$ //$NON-NLS-2$
			tup.el1 = entry.getKey();
			tup.el2 = entry.getValue();
			scripts.add(tup);
		}

		result.el3 = scripts;

		Log.debug("Building source list completed."); //$NON-NLS-1$
		return result;
	}

	public void setModulesOnly(boolean val) {
		this.modulesOnly = val;
	}
}
