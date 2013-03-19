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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.babel.tapiji.tools.core.Logger;
import org.eclipse.babel.tapiji.tools.core.model.manager.IStateLoader;
import org.eclipse.babel.tapiji.tools.core.model.manager.StateLoaderFactory;

/**
 * Contains a set of ResourceDescriptors that are loaded and saved with IStateLoader.
 */
public class PersistantResourceDescriptors {

    private Set<IResourceDescriptor> resources = new HashSet<IResourceDescriptor>();

    /** State-Serialization Information **/
    private boolean stateLoaded;

    private IStateLoader stateLoader;

    public void load() {
        // check if persistant state has been loaded
        if (!stateLoaded) {
            createStateLoader();
            if (stateLoader != null) {
                stateLoader.loadState();
                stateLoaded = true;
                resources = stateLoader.getExcludedResources();
            } else {
                Logger.logError("State-Loader uninitialized! Unable to restore project state.");
            }
        }
    }

    private void createStateLoader() {
        if (stateLoader == null) {
            stateLoader = new StateLoaderFactory().create();
        }
    }

    public void save() {
        createStateLoader();
        if (stateLoader != null) {
            stateLoader.saveState();
        }
    }

    // delegate methods for the resources
    
    public void add(IResourceDescriptor rd) {
        resources.add(rd);
    }

    public boolean contains(IResourceDescriptor rd) {
        return resources.contains(rd);
    }

    public void remove(IResourceDescriptor rd) {
        resources.remove(rd);
    }

}
