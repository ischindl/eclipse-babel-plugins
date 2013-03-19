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
package org.eclipse.babel.tapiji.tools.core.model.manager;

import org.eclipse.babel.tapiji.tools.core.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

/**
 * Create the StateLoader from extension point.
 */
public class StateLoaderFactory {

    private static final String EXTENSION_POINT = "org.eclipse.babel.tapiji.tools.core" + ".stateLoader";

    public IStateLoader create() {
        IExtensionPoint extp = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT);
        IConfigurationElement[] elements = extp.getConfigurationElements();

        if (elements.length != 0) {
            try {
                return (IStateLoader) elements[0].createExecutableExtension("class");
            } catch (CoreException e) {
                Logger.logError(e);
            }
        }

        return null;
    }

}
