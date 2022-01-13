package lslforge.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lslforge.LSLForgePlugin;
import lslforge.LSLProjectNature;
import lslforge.outline.LSLForgeMultiOutlinePage;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * Multi-page editor that displays both the LSLForge source file and the
 * compiled output
 */
public class LSLMultiPageEditor extends MultiPageEditorPart implements IResourceChangeListener, LSLProjectNature.RecompileListener {
	private static final Image IMAGE = LSLForgePlugin.createImage("icons/obj16/lslforge.gif"); //$NON-NLS-1$
	
	/** The text editor used in page 0. */
	private LSLForgeEditor sourceEditor = null;
	private IFileEditorInput sourceEditorInput = null;
	private IFileEditorInput compiledEditorInput = null;
	private LSLForgeEditor compiledEditor = null;
	
	private LSLForgeMultiOutlinePage outlinePage = null;
	private LSLForgeEditor currentEditor = null;
	private int compiledPage = -1;
	private int rememberedPage = -1;
	
	private final Map<Integer, LSLForgeEditor> pages;

	public LSLMultiPageEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		pages = new HashMap<Integer, LSLForgeEditor>();
	}
	
	/**
	 * Creates the main source editor tab
	 */
	void createSourcePage() {

		try {
			if(ResourcesPlugin.getWorkspace().getRoot().exists(sourceEditorInput.getFile().getFullPath())) {
				sourceEditor = new LSLForgeEditor();
				sourceEditor.setParent(this);
				currentEditor = sourceEditor;
				sourceEditor.updateOutline();
				
				int newPage = addPage(sourceEditor, sourceEditorInput);
				pages.put(newPage, sourceEditor);
				setPageText(newPage, sourceEditorInput.getName());
			}
			
		} catch (PartInitException e) {
			ErrorDialog.openError(
				getSite().getShell(),
				"Error creating editor contents", //$NON-NLS-1$
				null,
				e.getStatus());
		}
	}

	void createCompiledPage() {
		try {
			if(ResourcesPlugin.getWorkspace().getRoot().exists(compiledEditorInput.getFile().getFullPath())) {
				compiledEditor = new LSLForgeEditor();
				compiledEditor.setParent(this);
				
				//Set read-only when we have a .lslp file to go with it
				if(ResourcesPlugin.getWorkspace().getRoot().exists(sourceEditorInput.getFile().getFullPath())) {
					compiledEditor.setReadOnly();
				}
				
				compiledEditor.updateOutline();
				compiledPage = addPage(compiledEditor, compiledEditorInput);
				pages.put(compiledPage, compiledEditor);
				setPageText(compiledPage, compiledEditorInput.getName());
			}
			
		} catch (PartInitException e) {
			ErrorDialog.openError(
				getSite().getShell(),
				"Error creating compiled editor contents", //$NON-NLS-1$
				null,
				e.getStatus());
		}

	}

	@Override
	protected void pageChange(final int newPageIndex) {
		Display.getDefault().asyncExec(new Runnable(){
			public void run(){
				if(newPageIndex == compiledPage) {
					currentEditor = compiledEditor;
					
				} else {
					currentEditor = sourceEditor;
				}
				
				getOutlinePage().setPageActive(currentEditor.getOutlinePage());
			}
		});

		super.pageChange(newPageIndex);
	}
	
	/**
	 * Creates the pages of the multi-page editor.
	 */
	@Override
	protected void createPages() {
		createSourcePage();
		createCompiledPage();
		
		//Now set the title name
		if(ResourcesPlugin.getWorkspace().getRoot().exists(sourceEditorInput.getFile().getFullPath())) {
			setPartName(sourceEditor.getTitle());
			currentEditor = sourceEditor;
		} else {
			setPartName(compiledEditor.getTitle());
			currentEditor = compiledEditor;
		}
		
		getOutlinePage().setPageActive(currentEditor.getOutlinePage());
		if(getEditorInput() instanceof IFileEditorInput) {
			setActiveByFile(((IFileEditorInput)getEditorInput()).getFile());
		}
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (IContentOutlinePage.class.equals(adapter)) {
            return getOutlinePage();
        }

		return super.getAdapter(adapter);
	}
	
	public LSLForgeMultiOutlinePage getOutlinePage() {
		if(outlinePage == null) {
			outlinePage = new LSLForgeMultiOutlinePage();
			if(currentEditor != null) outlinePage.setPageActive(currentEditor.getOutlinePage());
		}
		
		return outlinePage;
	}
	
	/**
	 * The <code>MultiPageEditorPart</code> implementation of this 
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}
	
	/**
	 * Saves the multi-page editor's document.
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		getEditor(0).doSave(monitor);
	}
	
	/**
	 * Saves the multi-page editor's document as another file.
	 * Also updates the text for page 0's tab, and updates this multi-page editor's input
	 * to correspond to the nested editor's.
	 */
	@Override
	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}
	
	/* (non-Javadoc)
	 * Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		IEditorPart editor = setActivePage(marker.getResource());
		if(editor != null)
			IDE.gotoMarker(editor, marker);
	}
	
	public IEditorPart setActivePage(IResource resource) {
		for(int page: pages.keySet()) {
			IEditorPart editor = pages.get(page);
			IResource testPage = (IResource)editor.getEditorInput().getAdapter(IResource.class);
			if(testPage.equals(resource)) {
				setActivePage(page);
				return editor;
			}
		}
		
		return null;
	}
	
	public IEditorPart setActiveByFile(IFile file) {
		for(int page: pages.keySet()) {
			if(!(pages.get(page).getEditorInput() instanceof IFileEditorInput)) continue;	//Skip to next item
			IFileEditorInput input = (IFileEditorInput)pages.get(page).getEditorInput();
			if(input.getFile().equals(file)) {
				//This is the one
				setActivePage(page);
				return pages.get(page);
			}
		}
		
		return null;
	}
	
	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	@Override
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput"); //$NON-NLS-1$
		
		setTitleImage(IMAGE);
		
		//Try to open the associated .lsl/.lslp file that goes with this file
		IFileEditorInput ei = (IFileEditorInput)editorInput;
		String extension = ei.getFile().getFileExtension();
		IPath basePath = ei.getFile().getFullPath().removeFileExtension();
		
		if("lslp".equals(extension)) { //$NON-NLS-1$
			sourceEditorInput = (IFileEditorInput)editorInput;
			IPath eiPath = basePath.addFileExtension("lsl"); //$NON-NLS-1$
			compiledEditorInput = new FileEditorInput(ResourcesPlugin.getWorkspace().getRoot().getFile(eiPath));
			
		} else if("lsl".equals(extension)) { //$NON-NLS-1$
			IPath eiPath = basePath.addFileExtension("lslp"); //$NON-NLS-1$
			sourceEditorInput = new FileEditorInput(ResourcesPlugin.getWorkspace().getRoot().getFile(eiPath));
			compiledEditorInput = (IFileEditorInput)editorInput;
			setInput(sourceEditorInput);
		}
		
		super.init(site, editorInput);
	}
	
	/* (non-Javadoc)
	 * Method declared on IEditorPart.
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * Closes all project files on project close.
	 * @param event 
	 */
	public void resourceChanged(final IResourceChangeEvent event){
		Display.getDefault().asyncExec(new Runnable(){
			public void run(){
				//Was it our compiled file?
				IResourceDelta[] deltas = getDeltasForPath(compiledEditorInput.getFile().getFullPath(), event.getDelta());
				if(deltas.length > 0) {
					//What kind of change was recorded?
					for(IResourceDelta delta: deltas) {
						int kind = delta.getKind();
						switch(kind) {
						case IResourceDelta.ADDED:
							//Compiled file added, so add its corresponding tab
							createCompiledPage();
							break;
							
						case IResourceDelta.CHANGED:
							//We have this here for debugging purposes
							break;
							
						case IResourceDelta.REMOVED:
							//File removed, so remove the tab if necessary
							
							//Switch back to main tab first
							if(currentEditor.equals(getEditor(compiledPage))) {
								currentEditor = sourceEditor;
								getOutlinePage().setPageActive(currentEditor.getOutlinePage());
							}
							
							//Now toss the compiled version
							if(compiledEditor != null && compiledPage>=0 && compiledPage < getPageCount()) {
								removePage(compiledPage);
								compiledEditor = null;
							}
							break;
						}
					}
				}
			}
		});
		
		if(event.getType() == IResourceChangeEvent.PRE_CLOSE){
			Display.getDefault().asyncExec(new Runnable(){
				public void run(){
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i<pages.length; i++){
						if(((FileEditorInput)sourceEditor.getEditorInput()).getFile().getProject().equals(event.getResource())){
							IEditorPart editorPart = pages[i].findEditor(compiledEditorInput);
							pages[i].closeEditor(editorPart,true);
						}
					}
				}            
			});
		}
	}

	private IResourceDelta[] getDeltasForPath(IPath path, IResourceDelta delta) {
		List<IResourceDelta> matches = new ArrayList<IResourceDelta>();
		matches = getDeltasForPath(path, delta, matches);
		IResourceDelta[] matchesArray = new IResourceDelta[matches.size()];
		matches.toArray(matchesArray);
		return matchesArray;
	}
	
	private List<IResourceDelta> getDeltasForPath(IPath path, IResourceDelta delta, List<IResourceDelta> matches) {
		//Check ourselves
		if (delta!=null) {
			if(delta.getFullPath().equals(path)) matches.add(delta);
			
			//Now check any children entries
			if(delta.getAffectedChildren().length > 0) {
				for(IResourceDelta childDelta: delta.getAffectedChildren()) {
					matches = getDeltasForPath(path, childDelta, matches);
				}
			}
		}
		return matches;
	}
	
	protected void restoreSelection() {
		setActivePage(rememberedPage);
	}

	protected void rememberSelection() {
		rememberedPage = getActivePage();
	}

	public void recompile() {
		// TODO Auto-generated method stub
		
	}	
}
