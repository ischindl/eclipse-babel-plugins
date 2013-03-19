/*******************************************************************************
 * Copyright (c) 2013 Peter Kofler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Kofler - extracted out of ResourceBundleManager
 ******************************************************************************/
package org.eclipse.babel.tapiji.tools.core.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.babel.tapiji.tools.core.model.manager.ResourceExclusionEvent;

/**
 * Support for handling IResourceExclusionListener events.
 */
public class ResourceExclusionListenerSupport {

    private final List<IResourceExclusionListener> exclusionListeners = new CopyOnWriteArrayList<IResourceExclusionListener>();

    public void fireResourceExclusionEvent(ResourceExclusionEvent event) {
        for (IResourceExclusionListener listener : exclusionListeners) {
            listener.exclusionChanged(event);
        }
    }

    public void add(IResourceExclusionListener listener) {
        exclusionListeners.add(listener);
    }

    public void remove(IResourceExclusionListener listener) {
        exclusionListeners.remove(listener);
    }

    public boolean contains(IResourceExclusionListener listener) {
        return exclusionListeners.contains(listener);
    }

}
