package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;

import java.util.Iterator;

/**
 * Standard action for adding a bookmark to the currently selected file
 * resource(s).
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class AddBookmarkAction extends SelectionListenerAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".AddBookmarkAction";//$NON-NLS-1$
	
	/**
	 * The shell in which to show any dialogs.
	 */
	private Shell shell;
/**
 * Creates a new bookmark action.
 *
 * @param shell the shell for any dialogs
 */
public AddBookmarkAction(Shell shell) {
	super(WorkbenchMessages.getString("AddBookmarkLabel")); //$NON-NLS-1$
	setId(ID);
	Assert.isNotNull(shell);
	this.shell = shell;
	setToolTipText(WorkbenchMessages.getString("AddBookmarkToolTip")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.ADD_BOOKMARK_ACTION});
}
/**
 * Creates a marker of the given type on each of the files in the
 * current selection.
 *
 * @param markerType the marker type
 */
void createMarker(String markerType) {
	IStructuredSelection selection = getStructuredSelection();
	for (Iterator enum = selection.iterator(); enum.hasNext();) {
		Object o = enum.next();
		if (o instanceof IFile) {
			createMarker((IFile) o, markerType);
		} else if (o instanceof IAdaptable) {
			Object resource = ((IAdaptable)o).getAdapter(IResource.class);
			if (resource instanceof IFile)
				createMarker((IFile)resource, markerType);
		}
	}
}
/**
 * Creates a marker of the given type on the given file resource.
 *
 * @param file the file resource
 * @param markerType the marker type
 */
void createMarker(final IFile file, final String markerType) {
	try {
		file.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IMarker marker = file.createMarker(markerType);
				marker.setAttribute(IMarker.MESSAGE, file.getName());
			}
		}, null);
	} catch (CoreException e) {
		WorkbenchPlugin.log(null, e.getStatus()); // We don't care
	}
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void run() {
	createMarker(IMarker.BOOKMARK);
}
/**
 * The <code>AddBookmarkAction</code> implementation of this
 * <code>SelectionListenerAction</code> method enables the action only
 * if the selection is not empty and contains just file resources.
 */
protected boolean updateSelection(IStructuredSelection selection) {
	return super.updateSelection(selection)
		&& !selection.isEmpty()
		&& selectionAdaptsToType(IFile.FILE);
}

/**
 * Returns whether the current selection consists entirely of resources whose
 * types are among those that adapt to the given resource mask.
 * 
 * @param resourceMask a bitwise OR of resource types: 
 *   <code>IResource</code>.{<code>FILE</code>, <code>FOLDER</code>,
 *   <code>PROJECT</code>, <code>ROOT</code>}
 * @return <code>true</code> if all resources in the current selection are of the
 *   specified types or if the current selection is empty, and <code>false</code> if some
 *   elements adapt to resources of a different type or do not adapt to resources
 * @see IResource
 */
protected boolean selectionAdaptsToType(int resourceMask) {
	for (Iterator e = getSelectedResources().iterator(); e.hasNext();) {
		IResource next = (IResource) e.next();
		if (!resourceIsType(next, resourceMask))
			return false;
	}
	for (Iterator iterator = getSelectedNonResources().iterator();iterator.hasNext();) {
		Object nextObject = iterator.next();
		if (nextObject instanceof IAdaptable) {
			Object adapted = ((IAdaptable) nextObject).getAdapter(IResource.class);
			if (adapted == null)
				return false;
			if (!resourceIsType((IResource) adapted, resourceMask))
				return false;
		}
		//If it is not adaptable then get rid of it
		return false;
	}
	return true;
}
}
