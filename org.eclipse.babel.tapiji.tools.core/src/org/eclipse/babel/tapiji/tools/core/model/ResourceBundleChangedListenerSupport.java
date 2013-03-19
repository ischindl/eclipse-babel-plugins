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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.babel.tapiji.tools.core.model.manager.ResourceBundleChangedEvent;

public class ResourceBundleChangedListenerSupport {

    private final Map<String, List<IResourceBundleChangedListener>> listenersByBundle = new HashMap<String, List<IResourceBundleChangedListener>>();

    public void fireResourceBundleChangedEvent(String bundleName, ResourceBundleChangedEvent event) {
        List<IResourceBundleChangedListener> l = listenersByBundle.get(bundleName);

        if (l == null) {
            return;
        }

        for (IResourceBundleChangedListener listener : l) {
            listener.resourceBundleChanged(event);
        }
    }

    public void registerResourceBundleChangeListener(String bundleName, IResourceBundleChangedListener listener) {
        List<IResourceBundleChangedListener> l = listenersByBundle.get(bundleName);
        if (l == null) {
            l = new ArrayList<IResourceBundleChangedListener>();
        }
        l.add(listener);
        listenersByBundle.put(bundleName, l);
    }

    public void unregisterResourceBundleChangeListener(String bundleName, IResourceBundleChangedListener listener) {
        List<IResourceBundleChangedListener> l = listenersByBundle.get(bundleName);
        if (l == null) {
            return;
        }
        l.remove(listener);
        listenersByBundle.put(bundleName, l);
    }

}
