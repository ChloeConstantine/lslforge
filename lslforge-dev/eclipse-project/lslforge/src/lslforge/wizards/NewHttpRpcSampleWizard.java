package lslforge.wizards;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.osgi.framework.Bundle;

import lslforge.LSLForgePlugin;
import lslforge.util.Util;

public class NewHttpRpcSampleWizard extends Wizard implements INewWizard {
	private SampleWizardPage mainPage;
	private IStructuredSelection selection;
	private class SampleWizardPage extends LSLSampleCreationWizardPage {
		public SampleWizardPage(IStructuredSelection selection) {
			super("createSample", selection); //$NON-NLS-1$
			setTitle(Messages.getString("NewHttpRpcSampleWizard.0")); //$NON-NLS-1$
			setPageComplete(false);
			setFileExtension(null);
			setDefaultPageImageDescriptor(image());
		}


		@Override
		protected List<Sample> getSampleItems() {
		    Bundle bundle = LSLForgePlugin.getDefault().getBundle();
		    Sample evhandlerSample =
		        new Sample("eventhandler.lslm", bundle, //$NON-NLS-1$
		                new Path("samples/http_rpc_example/eventhandler.lslm"), false); //$NON-NLS-1$
		    Sample rpcServerSample = new Sample("rpc_server.lslp", bundle, //$NON-NLS-1$
		            new Path("samples/http_rpc_example/rpc_server.lslp"), false); //$NON-NLS-1$
		    Sample rpcSimSample = new Sample("rpcsim.simp", bundle, //$NON-NLS-1$
		            new Path("samples/http_rpc_example/rpcsim.simp"), true); //$NON-NLS-1$

		    LinkedList<Sample> list = new LinkedList<>();
		    Collections.addAll(list, new Sample[] { evhandlerSample, rpcServerSample, rpcSimSample });
		    return list;
        }

		@Override
		protected IStatus validateFileName(String fileName) {
			return new Status(IStatus.OK,  "lslforge",""); //$NON-NLS-1$ //$NON-NLS-2$
		}


        @Override
		protected String getNewFileLabel() {
            return Messages.getString("NewHttpRpcSampleWizard.DirName"); //$NON-NLS-1$
        }
	}

	public NewHttpRpcSampleWizard() {
		this.setDefaultPageImageDescriptor(image());
	}

	private static ImageDescriptor image() {
		return Util.findDescriptor("$nl$/icons/new_test.png"); //$NON-NLS-1$
	}

	@Override
	public boolean performFinish() {
        mainPage.createSample();
		return true;
	}

	@Override
	public void addPages() {
		super.addPages();
		mainPage = new SampleWizardPage(selection);
		addPage(mainPage);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}