/*******************************************************************************
 * Copyright (c) 2007 Pascal Essiembre.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pascal Essiembre - initial API and implementation
 *    Alexej Strelzow - moved code to here
 ******************************************************************************/
package org.eclipse.babel.editor.preferences;

import org.eclipse.babel.core.configuration.ConfigurationManager;
import org.eclipse.babel.core.message.resource.ser.IPropertiesSerializerConfig;
import org.eclipse.babel.editor.plugin.MessagesEditorPlugin;
import org.eclipse.core.runtime.Preferences;

/**
 * The concrete implementation of {@link IPropertiesSerializerConfig}.
 * 
 * @author Alexej Strelzow
 */
public class PropertiesSerializerConfig implements IPropertiesSerializerConfig {
    // Moved from MsgEditorPreferences, to make it more flexible.

    /** MsgEditorPreferences. */
    private static final Preferences PREFS = MessagesEditorPlugin.getDefault()
            .getPluginPreferences();

    PropertiesSerializerConfig() {
        ConfigurationManager.getInstance().setSerializerConfig(this);
    }

    /**
     * Gets whether to escape unicode characters when generating file.
     * 
     * @return <code>true</code> if escaping
     */
    public boolean isUnicodeEscapeEnabled() {
        return PREFS.getBoolean(MsgEditorPreferences.UNICODE_ESCAPE_ENABLED);
    }

    /**
     * Gets the new line type to use when overwriting system (or Eclipse)
     * default new line type when generating file. Use constants to this effect.
     * 
     * @return new line type
     */
    public int getNewLineStyle() {
        return PREFS.getInt(MsgEditorPreferences.NEW_LINE_STYLE);
    }

    /**
     * Gets how many blank lines should separate groups when generating file.
     * 
     * @return how many blank lines between groups
     */
    public int getGroupSepBlankLineCount() {
        return PREFS.getInt(MsgEditorPreferences.GROUP_SEP_BLANK_LINE_COUNT);
    }

    /**
     * Gets whether to print "Generated By..." comment when generating file.
     * 
     * @return <code>true</code> if we print it
     */
    public boolean isShowSupportEnabled() {
        return PREFS.getBoolean(MsgEditorPreferences.SHOW_SUPPORT_ENABLED);
    }

    /**
     * Gets whether keys should be grouped when generating file.
     * 
     * @return <code>true</code> if keys should be grouped
     */
    public boolean isGroupKeysEnabled() {
        return PREFS.getBoolean(MsgEditorPreferences.GROUP_KEYS_ENABLED);
    }

    /**
     * Gets whether escaped unicode "alpha" characters should be uppercase when
     * generating file.
     * 
     * @return <code>true</code> if uppercase
     */
    public boolean isUnicodeEscapeUppercase() {
        return PREFS.getBoolean(MsgEditorPreferences.UNICODE_ESCAPE_UPPERCASE);
    }

    /**
     * Gets the number of character after which lines should be wrapped when
     * generating file.
     * 
     * @return number of characters
     */
    public int getWrapLineLength() {
        return PREFS.getInt(MsgEditorPreferences.WRAP_LINE_LENGTH);
    }

    /**
     * Gets whether lines should be wrapped if too big when generating file.
     * 
     * @return <code>true</code> if wrapped
     */
    public boolean isWrapLinesEnabled() {
        return PREFS.getBoolean(MsgEditorPreferences.WRAP_LINES_ENABLED);
    }

    /**
     * Gets whether wrapped lines should be aligned with equal sign when
     * generating file.
     * 
     * @return <code>true</code> if aligned
     */
    public boolean isWrapAlignEqualsEnabled() {
        return PREFS.getBoolean(MsgEditorPreferences.WRAP_ALIGN_EQUALS_ENABLED);
    }

    /**
     * Gets the number of spaces to use for indentation of wrapped lines when
     * generating file.
     * 
     * @return number of spaces
     */
    public int getWrapIndentLength() {
        return PREFS.getInt(MsgEditorPreferences.WRAP_INDENT_LENGTH);
    }

    /**
     * Gets whether there should be spaces around equals signs when generating
     * file.
     * 
     * @return <code>true</code> there if should be spaces around equals signs
     */
    public boolean isSpacesAroundEqualsEnabled() {
        return PREFS
                .getBoolean(MsgEditorPreferences.SPACES_AROUND_EQUALS_ENABLED);
    }

    /**
     * Gets whether new lines are escaped or printed as is when generating file.
     * 
     * @return <code>true</code> if printed as is.
     */
    public boolean isNewLineNice() {
        return PREFS.getBoolean(MsgEditorPreferences.NEW_LINE_NICE);
    }

    /**
     * Gets how many level deep keys should be grouped when generating file.
     * 
     * @return how many level deep
     */
    public int getGroupLevelDepth() {
        return PREFS.getInt(MsgEditorPreferences.GROUP_LEVEL_DEEP);
    }

    /**
     * Gets key group separator.
     * 
     * @return key group separator.
     */
    public String getGroupLevelSeparator() {
        return PREFS.getString(MsgEditorPreferences.GROUP__LEVEL_SEPARATOR);
    }

    /**
     * Gets whether equals signs should be aligned when generating file.
     * 
     * @return <code>true</code> if equals signs should be aligned
     */
    public boolean isAlignEqualsEnabled() {
        return PREFS.getBoolean(MsgEditorPreferences.ALIGN_EQUALS_ENABLED);
    }

    /**
     * Gets whether equal signs should be aligned within each groups when
     * generating file.
     * 
     * @return <code>true</code> if equal signs should be aligned within groups
     */
    public boolean isGroupAlignEqualsEnabled() {
        return PREFS
                .getBoolean(MsgEditorPreferences.GROUP_ALIGN_EQUALS_ENABLED);
    }

    /**
     * Gets whether to sort keys upon serializing them.
     * 
     * @return <code>true</code> if keys are to be sorted.
     */
    public boolean isKeySortingEnabled() {
        return PREFS.getBoolean(MsgEditorPreferences.SORT_KEYS);
    }

}
