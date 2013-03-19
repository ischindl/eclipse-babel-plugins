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
package org.eclipse.babel.tapiji.tools.core.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.babel.tapiji.tools.core.Logger;
import org.eclipse.babel.tapiji.tools.core.model.IResourceExclusionListener;
import org.eclipse.babel.tapiji.tools.core.ui.analyzer.ResourceBundleDetectionVisitor;
import org.eclipse.babel.tapiji.tools.core.util.FragmentProjectUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * Holds and creates ResourceBundleManager per IProject.
 * This is a helper class of ResourceBundleManager's static content.
 */
public class ResourceBundleManagerByProject {

    private final Map<IProject, ResourceBundleManager> rbmanagerByProject = new HashMap<IProject, ResourceBundleManager>();

    ResourceBundleManager getManager(IProject project) {
        // set host-project
        if (FragmentProjectUtils.isFragment(project)) {
            project = FragmentProjectUtils.getFragmentHost(project);
        }

        ResourceBundleManager manager = rbmanagerByProject.get(project);
        if (manager == null) {
            manager = new ResourceBundleManager(project);
            rbmanagerByProject.put(project, manager);
            detectResourceBundles(project);
        }
        return manager;
    }

    private void detectResourceBundles(IProject project) {
        try {
            project.accept(new ResourceBundleDetectionVisitor(project));

            IProject[] fragments = FragmentProjectUtils.lookupFragment(project);
            if (fragments != null) {
                for (IProject p : fragments) {
                    p.accept(new ResourceBundleDetectionVisitor(project));
                }
            }
        } catch (CoreException e) {
            // TODO no empty catch suckers
        }
    }

    ResourceBundleManager getManager(String projectName) {
        for (IProject p : getAllSupportedProjects()) {
            if (p.getName().equalsIgnoreCase(projectName)) {
                // check if the projectName is a fragment and return the manager for the host
                if (FragmentProjectUtils.isFragment(p)) {
                    return getManager(FragmentProjectUtils.getFragmentHost(p));
                } else {
                    return getManager(p);
                }
            }
        }
        return null;
    }

    private Set<IProject> getAllSupportedProjects() {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        Set<IProject> projs = new HashSet<IProject>();

        for (IProject p : projects) {
            try {
                if (p.isOpen() && p.hasNature(ResourceBundleManager.NATURE_ID)) {
                    projs.add(p);
                }
            } catch (CoreException e) {
                Logger.logError(e);
            }
        }
        return projs;
    }

    void unregisterResourceExclusionListenerFromAllManagers(IResourceExclusionListener excludedResource) {
        for (ResourceBundleManager mgr : rbmanagerByProject.values()) {
            mgr.unregisterResourceExclusionListener(excludedResource);
        }
    }

}
