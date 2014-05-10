/*******************************************************************************
 * Copyright (c) 2007 Pascal Essiembre.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pascal Essiembre - initial API and implementation
 ******************************************************************************/
package org.eclipse.babel.editor.tree.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.babel.core.message.tree.internal.KeyTreeNode;
import org.eclipse.babel.editor.internal.AbstractMessagesEditor;
import org.eclipse.babel.editor.plugin.MessagesEditorPlugin;
import org.eclipse.babel.editor.util.UIUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.pde.internal.core.SearchablePluginsManager;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search.ui.text.TextSearchQueryProvider;
import org.eclipse.ui.PlatformUI;

/**
 * @author Pascal Essiembre
 *
 */
public class SearchKeyAction extends AbstractTreeAction {

	/**
     *
     */
	public SearchKeyAction(AbstractMessagesEditor editor, TreeViewer treeViewer) {
		super(editor, treeViewer);
		setText(MessagesEditorPlugin.getString("key.search") + " ..."); //$NON-NLS-1$
		setImageDescriptor(UIUtils
				.getImageDescriptor(UIUtils.IMAGE_REFACTORING));
		setToolTipText("TODO put something here"); // TODO put tooltip
	}

	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
    public void run() {
        KeyTreeNode node = getNodeSelection();
        final String key = node.getMessageKey();

        try {
        	NewSearchUI.runQueryInBackground(TextSearchQueryProvider.getPreferred().createQuery(key));
		} catch (Exception e) {
			e.printStackTrace();
		}

    }
}
