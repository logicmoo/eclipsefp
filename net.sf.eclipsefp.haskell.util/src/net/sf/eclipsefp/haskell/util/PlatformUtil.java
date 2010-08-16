/**
 * Operating system-specific utility class.
 * 
 * This is the place where operating system-specific hacks should be placed. Currently, this
 * holds Win32-specific file extensions and predicates.
 */
package net.sf.eclipsefp.haskell.util;

import org.eclipse.core.runtime.Platform;

/**
 * Operating system-specific platform utilities.
 * @author Scott Michel (scottm@aero.org)
 */
public class PlatformUtil {
	/**
	 * The primary Windows platform executable extension.
	 */
	public static final String WINDOWS_EXTENSION_EXE = "exe"; //$NON-NLS-1$
	/**
	 * List of extensions of files that can be executed under Windows.
	 */
	public static final String[] WINDOWS_EXECUTABLE_EXTENSIONS = new String[] {
			WINDOWS_EXTENSION_EXE, "bat" }; //$NON-NLS-1$

	/**
	 * Predicate that tests if the plug-in is currently running on Windows.
	 */
	public static boolean runningOnWindows() {
		return Platform.getOS().equals(Platform.OS_WIN32);
	}
}
