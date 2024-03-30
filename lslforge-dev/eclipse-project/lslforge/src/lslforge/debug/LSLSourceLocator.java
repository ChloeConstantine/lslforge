package lslforge.debug;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.ui.ISourcePresentation;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

import lslforge.editor.LSLForgeEditor;

public class LSLSourceLocator implements ISourceLocator, ISourcePresentation {

    @Override
	public Object getSourceElement(IStackFrame stackFrame) {
        if (stackFrame instanceof LSLStackFrame) {
            LSLStackFrame frame = (LSLStackFrame) stackFrame;

            if (frame.getFile() == null) return null;
            Path p = new Path(frame.getFile());

            IWorkspace w = ResourcesPlugin.getWorkspace();

            IFile[] files = w.getRoot().findFilesForLocationURI(URI.create(p.makeAbsolute().toString()));
            //IFile[] files = w.getRoot().findFilesForLocation(p);

            if (files != null && files.length > 0) return files[0];
        }
        return null;
    }

    @Override
	public String getEditorId(IEditorInput input, Object element) {
        return LSLForgeEditor.ID;
    }

    @Override
	public IEditorInput getEditorInput(Object element) {
        if (element instanceof IFile) {
            return new FileEditorInput((IFile)element);
        }

        return null;
    }

}
