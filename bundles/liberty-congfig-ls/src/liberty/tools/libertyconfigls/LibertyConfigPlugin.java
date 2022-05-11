package liberty.tools.libertyconfigls;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class LibertyConfigPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "liberty"; //$NON-NLS-1$

	// The shared instance
	private static LibertyConfigPlugin plugin;

	/**
	 * The constructor
	 */
	public LibertyConfigPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static LibertyConfigPlugin getDefault() {
		return plugin;
	}

	public static void logException(String localizedMessage, JavaModelException e) {
		// TODO Auto-generated method stub

	}

	public static String getPluginId() {
		return LibertyConfigPlugin.PLUGIN_ID;
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void logException(String errMsg, Throwable ex) {
		getDefault().getLog().log(new Status(IStatus.ERROR, getPluginId(), errMsg, ex));

	}

}
