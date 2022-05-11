/******************************************************************************* 
 * Copyright (c) 2019 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
/**
 * This class is a copy/paste of jbosstools-quarkus language server plugin
 * https://github.com/jbosstools/jbosstools-quarkus/blob/main/plugins/org.jboss.tools.quarkus.lsp4e/src/org/jboss/tools/quarkus/lsp4e/QuarkusLanguageClient.java
 * with modifications made for the Liberty Devtools Microprofile LS plugin
 *
 */

package liberty.tools.libertyconfigls;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4mp.jdt.core.IMicroProfilePropertiesChangedListener;
import io.openliberty.libertyls.api.LibertyLanguageClientAPI;
import org.eclipse.lsp4j.HoverParams;
//import org.eclipse.lsp4mp.ls.api.MicroProfileLanguageServerAPI;

/**
 * Liberty Devex Liberty Config language client.
 * 
 * @author
 *
 */
public class LibertyConfigClientImpl extends LanguageClientImpl implements LibertyLanguageClientAPI {

	private static IMicroProfilePropertiesChangedListener SINGLETON_LISTENER;

	//private IMicroProfilePropertiesChangedListener listener = event -> {
	//	((LibertyLanguageServerLauncherServerAPI) getLanguageServer()).propertiesChanged(event);
	//};

	private IProgressMonitor getProgressMonitor(CancelChecker cancelChecker) {
		IProgressMonitor monitor = new NullProgressMonitor() {
			public boolean isCanceled() {
				cancelChecker.checkCanceled();
				return false;
			};
		};
		return monitor;
	}
	
	public LibertyConfigClientImpl() {
		//if (SINGLETON_LISTENER != null) {
		//	MicroProfileCorePlugin.getDefault().removeMicroProfilePropertiesChangedListener(SINGLETON_LISTENER);
		//}
		//SINGLETON_LISTENER = listener;
		//MicroProfileCorePlugin.getDefault().addMicroProfilePropertiesChangedListener(listener);
	}

	@Override
	public CompletableFuture<Hover> getJavaHover(HoverParams params) {
	        //return CompletableFuture.completedFuture(null);
	        // return dummy test hover object
			return CompletableFutures.computeAsync((cancelChecker) -> {
				IProgressMonitor monitor = getProgressMonitor(cancelChecker);
				Hover testHover = new Hover();
				List<Either<String, MarkedString>> contents = new ArrayList<>();
				contents.add(Either.forLeft("this is test hover"));
				testHover.setContents(contents);
				return testHover;
			});
	}
}
