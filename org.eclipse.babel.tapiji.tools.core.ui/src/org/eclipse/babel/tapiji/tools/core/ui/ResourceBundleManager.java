/*******************************************************************************
 * Copyright (c) 2012 Martin Reiterer, Alexej Strelzow. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Martin Reiterer - initial API and implementation Alexej
 * Strelzow - moved object management to RBManager, Babel integration
 ******************************************************************************/
package org.eclipse.babel.tapiji.tools.core.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.babel.core.message.IMessagesBundleGroup;
import org.eclipse.babel.core.message.manager.IResourceDeltaListener;
import org.eclipse.babel.core.message.manager.RBManager;
import org.eclipse.babel.core.util.NameUtils;
import org.eclipse.babel.tapiji.tools.core.Logger;
import org.eclipse.babel.tapiji.tools.core.model.IResourceBundleChangedListener;
import org.eclipse.babel.tapiji.tools.core.model.IResourceDescriptor;
import org.eclipse.babel.tapiji.tools.core.model.IResourceExclusionListener;
import org.eclipse.babel.tapiji.tools.core.model.PersistantResourceDescriptors;
import org.eclipse.babel.tapiji.tools.core.model.ResourceBundleChangedListenerSupport;
import org.eclipse.babel.tapiji.tools.core.model.ResourceDescriptor;
import org.eclipse.babel.tapiji.tools.core.model.ResourceExclusionListenerSupport;
import org.eclipse.babel.tapiji.tools.core.model.manager.ResourceBundleChangedEvent;
import org.eclipse.babel.tapiji.tools.core.model.manager.ResourceExclusionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;

public class ResourceBundleManager {

	public static String defaultLocaleTag = "[default]"; // TODO externalize

	/*** CONFIG SECTION ***/
	private static boolean checkResourceExclusionRoot;

    /*** MEMBER SECTION ***/
    private static final ResourceBundleManagerByProject rbmanagerByProject = new ResourceBundleManagerByProject();

	// project-specific
	private final Map<String, Set<IResource>> resources = new HashMap<String, Set<IResource>>();

	private final Map<String, String> bundleNames = new HashMap<String, String>();

	private final ResourceBundleChangedListenerSupport listenersByBundle = new ResourceBundleChangedListenerSupport();

	private final ResourceExclusionListenerSupport exclusionListeners = new ResourceExclusionListenerSupport();

	// global
    private static final PersistantResourceDescriptors excludedResources = new PersistantResourceDescriptors();
	private static final Map<String, Set<IResource>> allBundles = new HashMap<String, Set<IResource>>();

	// private static IResourceChangeListener changelistener; //
	// RBChangeListener -> see stateLoader!

	public static final String NATURE_ID = "org.eclipse.babel.tapiji.tools.core.ui.nature";

	public static final String BUILDER_ID = Activator.PLUGIN_ID
			+ ".I18NBuilder";

	/* Host project */
	private final IProject project;

	// Define private constructor
	ResourceBundleManager(IProject project) {
		this.project = project;

		excludedResources.load();

		RBManager.getInstance(project).addResourceDeltaListener(
				new IResourceDeltaListener() {

					@Override
					public void onDelete(IMessagesBundleGroup bundleGroup) {
						resources.remove(bundleGroup.getResourceBundleId());
					}

					@Override
					public void onDelete(String resourceBundleId,
							IResource resource) {
						resources.get(resourceBundleId).remove(resource);
					}
				});
	}

	public static ResourceBundleManager getManager(IProject project) {
	    return rbmanagerByProject.getManager(project);
	}

	public static String getResourceBundleName(IResource res) {
		String name = res.getName();
		String regex = "^(.*?)" //$NON-NLS-1$
				+ "((_[a-z]{2,3})|(_[a-z]{2,3}_[A-Z]{2})" //$NON-NLS-1$
				+ "|(_[a-z]{2,3}_[A-Z]{2}_\\w*))?(\\." //$NON-NLS-1$
				+ res.getFileExtension() + ")$"; //$NON-NLS-1$
		return name.replaceFirst(regex, "$1"); //$NON-NLS-1$
	}

	private void unloadResource(String bundleName, IResource resource) {
		// TODO implement more efficient
		unloadResourceBundle(bundleName);
		// loadResourceBundle(bundleName);
	}

	public static String getResourceBundleId(IResource resource) {
		String packageFragment = "";

		IJavaElement propertyFile = JavaCore.create(resource.getParent());
		if (propertyFile != null && propertyFile instanceof IPackageFragment) {
			packageFragment = ((IPackageFragment) propertyFile)
					.getElementName();
		}

		return (packageFragment.length() > 0 ? packageFragment + "." : "")
				+ getResourceBundleName(resource);
	}

	public void addBundleResource(IResource resource) {
		if (resource.isDerived()) {
			return;
		}

		final String bundleName = getResourceBundleId(resource);
		final Set<IResource> res;

		if (!resources.containsKey(bundleName)) {
			res = new HashSet<IResource>();
		} else {
			res = resources.get(bundleName);
		}

		// check if the resource bundle manager is already aware of this
		// resource
		if (res.contains(resource)) {
			return;
		}

		res.add(resource);
		resources.put(bundleName, res);
		allBundles.put(bundleName, new HashSet<IResource>(res));
		bundleNames.put(bundleName, getResourceBundleName(resource));

		// notify RBManager instance
		RBManager.getInstance(resource.getProject())
				.addBundleResource(resource);

		// Fire resource changed event
		ResourceBundleChangedEvent event = new ResourceBundleChangedEvent(
				ResourceBundleChangedEvent.ADDED, bundleName,
				resource.getProject());
		this.fireResourceBundleChangedEvent(bundleName, event);
	}

	private void unloadResourceBundle(String name) {
		RBManager.getInstance(project).deleteMessagesBundleGroup(name);
	}

	public Collection<IResource> getResourceBundles(String bundleName) {
		return resources.get(bundleName);
	}

	public List<String> getResourceBundleNames() {
		List<String> returnList = new ArrayList<String>();

		Iterator<String> it = resources.keySet().iterator();
		while (it.hasNext()) {
			returnList.add(it.next());
		}
		return returnList;
	}

	private void fireResourceBundleChangedEvent(String bundleName,
			ResourceBundleChangedEvent event) {
	    listenersByBundle.fireResourceBundleChangedEvent(bundleName, event);
	}

	public void registerResourceBundleChangeListener(String bundleName,
			IResourceBundleChangedListener listener) {
        listenersByBundle.registerResourceBundleChangeListener(bundleName, listener);
	}

	public void unregisterResourceBundleChangeListener(String bundleName,
			IResourceBundleChangedListener listener) {
        listenersByBundle.unregisterResourceBundleChangeListener(bundleName, listener);
	}

	public IProject getProject() {
		return project;
	}

	public List<String> getResourceBundleIdentifiers() {
		List<String> returnList = new ArrayList<String>();

		// TODO check other resource bundles that are available on the curren
		// class path
		Iterator<String> it = this.resources.keySet().iterator();
		while (it.hasNext()) {
			returnList.add(it.next());
		}

		return returnList;
	}

	private boolean excludeSingleResource(IResource res) {
		boolean rbRemoved = false;
		final IResourceDescriptor rd = new ResourceDescriptor(res);
		org.eclipse.babel.tapiji.tools.core.ui.utils.EditorUtils
				.deleteAuditMarkersForResource(res);

		// exclude resource
		excludedResources.add(rd);
		Collection<IResource> changedExclusions = new HashSet<IResource>();
		changedExclusions.add(res);
		fireResourceExclusionEvent(changedExclusions);

		// Check if the excluded resource represents a resource-bundle
		if (org.eclipse.babel.tapiji.tools.core.ui.utils.RBFileUtils
				.isResourceBundleFile(res)) {
			String bundleName = getResourceBundleId(res);
			Set<IResource> resSet = resources.remove(bundleName);
			if (resSet != null) {
				resSet.remove(res);

				if (!resSet.isEmpty()) {
					resources.put(bundleName, resSet);
					unloadResource(bundleName, res);
				} else {
					rd.setBundleId(bundleName);
					unloadResourceBundle(bundleName);
					rbRemoved = true;
				}

				fireResourceBundleChangedEvent(getResourceBundleId(res),
						new ResourceBundleChangedEvent(
								ResourceBundleChangedEvent.EXCLUDED,
								bundleName, res.getProject()));
			}
		}

		return rbRemoved;
	}

	public void excludeResource(IResource res, IProgressMonitor monitor) {
		try {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}

			final List<IResource> resourceSubTree = new ArrayList<IResource>();
			res.accept(new IResourceVisitor() {

				@Override
				public boolean visit(IResource resource) {
					Logger.logInfo("Excluding resource '"
							+ resource.getFullPath().toOSString() + "'");
					resourceSubTree.add(resource);
					return true;
				}

			});

			// Iterate previously retrieved resource and exclude them from
			// Internationalization
			monitor.beginTask(
					"Exclude resources from Internationalization context",
					resourceSubTree.size());
			try {
				boolean rbRemoved = false;
				for (IResource resource : resourceSubTree) {
					rbRemoved |= excludeSingleResource(resource);
					monitor.worked(1);
				}

				if (rbRemoved) {
					try {
						res.getProject().build(
								IncrementalProjectBuilder.FULL_BUILD,
								BUILDER_ID, null, null);
					} catch (CoreException e) {
						Logger.logError(e);
					}
				}
			} catch (Exception e) {
				Logger.logError(e);
			} finally {
				monitor.done();
			}
		} catch (CoreException e) {
			Logger.logError(e);
		}
	}

	public void includeResource(IResource res, IProgressMonitor monitor,
			boolean preventBuild) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		final Collection<IResource> changedResources = new HashSet<IResource>();
		final Collection<IResource> changedResourceBundles = new HashSet<IResource>();
		final IResource resource = res;

		if (!excludedResources.contains(new ResourceDescriptor(res))) {
			Logger.logError("requested to include non-excluded resource");
		} else {
			try {
				res.accept(new IResourceVisitor() {

					@Override
					public boolean visit(IResource resource) {
						if (excludedResources.contains(new ResourceDescriptor(
								resource))) {
							// check if the changed resource is a resource
							// bundle
							if (org.eclipse.babel.tapiji.tools.core.ui.utils.RBFileUtils
									.isResourceBundleFile(resource)
									&& !changedResourceBundles
											.contains(resource)) {
								changedResourceBundles.add(resource);
							}
							changedResources.add(resource);
						}
						return true;
					}
				});

				monitor.beginTask(
						"Add resources to Internationalization context",
						changedResources.size());
				try {
					for (Object r : changedResources) {
						excludedResources.remove(new ResourceDescriptor(
								(IResource) r));
						monitor.worked(1);
					}

					// check if parent resource exclusion marker can be removed
					// too
					final IResource parentResource = res.getParent();
					if (parentResource instanceof IFolder
							&& excludedResources
									.contains(new ResourceDescriptor(
											parentResource))) {
						final Collection<IResource> childResources = new HashSet<IResource>();

						parentResource.accept(new IResourceVisitor() {

							@Override
							public boolean visit(IResource resource) {
								if (excludedResources
										.contains(new ResourceDescriptor(
												resource))
										&& !resource.equals(parentResource)) {
									childResources.add(resource);
									return false;
								}
								return true;
							}
						});

						if (childResources.size() == 0) {
							excludedResources.remove(new ResourceDescriptor(
									parentResource));
							changedResources.add(parentResource);
							monitor.worked(1);
						}
					}
				} catch (Exception e) {
					Logger.logError(e);
				} finally {
					monitor.done();
				}
			} catch (Exception e) {
				Logger.logError(e);
			}

			// Check if the included resources represent a fully excluded
			// resource-bundle
			// -> YES: trigger a full build
			// -> OTHERWISE: rebuild only included resources
			boolean fullBuildRequired = (changedResourceBundles.size() > 0);

			for (IResource rbResource : changedResourceBundles) {
				String bundleName = getResourceBundleId(rbResource);

				// TODO check if fullbuild needs only be triggered if a complete
				// bundle was excluded
				// fullBuildRequired &= !resources.containsKey(bundleName);

				// RBManager.getInstance(rbResource.getProject())
				// .addBundleResource(rbResource);

				this.addBundleResource(rbResource);
				Logger.logInfo("Including resource bundle '"
						+ rbResource.getFullPath().toOSString() + "'");

				fireResourceBundleChangedEvent(getResourceBundleId(rbResource),
						new ResourceBundleChangedEvent(
								ResourceBundleChangedEvent.INCLUDED,
								bundleName, rbResource.getProject()));
			}

			if (!preventBuild) {
				if (fullBuildRequired) {
					try {
						resource.getProject().build(
								IncrementalProjectBuilder.FULL_BUILD,
								BUILDER_ID, null, null);
					} catch (CoreException e) {
						Logger.logError(e);
					}
				} else {
					// trigger incremental build of included resources
					for (IResource changedResource : changedResources) {
						try {
							Logger.logInfo(String.format(
									"trigger rebuild for resource: %s",
									changedResource));
							changedResource.touch(monitor);
						} catch (CoreException e) {
							Logger.logError(String.format(
									"error during rebuild of resource: %s",
									changedResource), e);
						}
					}
				}
			}

			fireResourceExclusionEvent(changedResources);
		}
	}

	private void fireResourceExclusionEvent(Collection<IResource> exclResources) {
	    exclusionListeners.fireResourceExclusionEvent(new ResourceExclusionEvent(exclResources));
	}

	public static boolean isResourceExcluded(IResource res) {
		IResource resource = res;

		excludedResources.load();

		boolean isExcluded = false;

		do {
			if (excludedResources.contains(new ResourceDescriptor(resource))) {
				if (org.eclipse.babel.tapiji.tools.core.ui.utils.RBFileUtils
						.isResourceBundleFile(resource)) {
					Set<IResource> resources = allBundles
							.remove(getResourceBundleName(resource));
					if (resources == null) {
						resources = new HashSet<IResource>();
					}
					resources.add(resource);
					allBundles.put(getResourceBundleName(resource), resources);
				}

				isExcluded = true;
				break;
			}
			resource = resource.getParent();
		} while (resource != null
				&& !(resource instanceof IProject || resource instanceof IWorkspaceRoot)
				&& checkResourceExclusionRoot);

		return isExcluded; // excludedResources.contains(new
		// ResourceDescriptor(res));
	}

	public static ResourceBundleManager getManager(String projectName) {
	    return rbmanagerByProject.getManager(projectName);
	}

	public IFile getResourceBundleFile(String resourceBundle, Locale l) {
		IFile res = null;
		Set<IResource> resSet = resources.get(resourceBundle);

		// get normalized simple resource bundle name
		String normalizedResourceBundleName = resourceBundle;
		if (normalizedResourceBundleName.contains(".")) {
			normalizedResourceBundleName = normalizedResourceBundleName
					.substring(resourceBundle.lastIndexOf(".") + 1);
		}

		if (resSet != null) {
			for (IResource resource : resSet) {
				Locale refLoc = NameUtils.getLocaleByName(
						normalizedResourceBundleName, resource.getName());
				if (refLoc == null
						&& l == null
						|| (refLoc != null && refLoc.equals(l) || l != null
								&& l.equals(refLoc))) {
					res = resource.getProject().getFile(
							resource.getProjectRelativePath());
					break;
				}
			}
		}

		return res;
	}

	public Set<IResource> getAllResourceBundleResources(String resourceBundle) {
		return allBundles.get(resourceBundle);
	}

	public void registerResourceExclusionListener(
			IResourceExclusionListener listener) {
		exclusionListeners.add(listener);
	}

	void unregisterResourceExclusionListener(
			IResourceExclusionListener listener) {
        exclusionListeners.remove(listener);
	}

	public boolean isResourceExclusionListenerRegistered(
			IResourceExclusionListener listener) {
		return exclusionListeners.contains(listener);
	}

	public static void unregisterResourceExclusionListenerFromAllManagers(
			IResourceExclusionListener excludedResource) {
        for (ResourceBundleManager mgr : rbmanagerByProject.allManagers()) {
            mgr.unregisterResourceExclusionListener(excludedResource);
        }
	}

	public Set<Locale> getProjectProvidedLocales() {
		Set<Locale> locales = new HashSet<Locale>();

		for (String bundleId : getResourceBundleNames()) {
			Set<Locale> rb_l = RBManager.getInstance(project).getProvidedLocales(bundleId);
			if (!rb_l.isEmpty()) {
				Object[] bundlelocales = rb_l.toArray();
				for (Object l : bundlelocales) {
					/*
					 * TODO check if useful to add the default. For now the
					 * default is not being used and is ignored as it is null
					 */
					if (l != null && !locales.contains(l)) {
						locales.add((Locale) l);
					}
				}
			}
		}
		return locales;
	}

	static void saveManagerState() {
	    excludedResources.save();
	}

}
