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
 * with modifications made for the Liberty Tools Microprofile LS plugin
 *
 */
package io.openliberty.tools.eclipse.mpls;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lspcommon.commons.JavaCursorContextResult;
import org.eclipse.lspcommon.commons.JavaDefinitionParams;
import org.eclipse.lspcommon.commons.JavaDiagnosticsParams;
import org.eclipse.lspcommon.commons.JavaFileInfoParams;
import org.eclipse.lspcommon.commons.JavaHoverParams;
import org.eclipse.lspcommon.commons.JavaProjectLabelsParams;
//import org.eclipse.lspcommon.jdt.core.utils.IJDTUtils;
import org.eclipse.lspcommon.commons.JavaFileInfo;
import org.eclipse.lsp4mp.commons.MicroProfileDefinition;
import org.eclipse.lsp4mp.commons.MicroProfileJavaDiagnosticsParams;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfoParams;
import org.eclipse.lsp4mp.commons.MicroProfilePropertyDefinitionParams;
import org.eclipse.lsp4mp.commons.MicroProfilePropertyDocumentationParams;
import org.eclipse.lspcommon.commons.ProjectLabelInfoEntry;
import org.eclipse.lspcommon.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4mp.commons.diagnostics.MicroProfileDiagnosticsHandler;
import org.eclipse.lsp4mp.commons.utils.JSONUtility;
import org.eclipse.lsp4mp.jdt.core.IMicroProfilePropertiesChangedListener;
import org.eclipse.lsp4mp.jdt.core.MicroProfileCorePlugin;
import org.eclipse.lspcommon.jdt.core.ProjectLabelManager;
import org.eclipse.lsp4mp.jdt.core.PropertiesManager;
//import org.eclipse.lsp4mp.jdt.core.PropertiesManagerForJava;
import org.eclipse.lspcommon.jdt.internal.core.ls.JDTUtilsLSImpl;
import org.eclipse.lsp4mp.ls.api.MicroProfileLanguageClientAPI;
import org.eclipse.lsp4mp.ls.api.MicroProfileLanguageServerAPI;
import org.eclipse.lspcommon.commons.JavaCodeActionParams;
import org.eclipse.lspcommon.commons.JavaCodeLensParams;
import org.eclipse.lspcommon.commons.JavaCompletionParams;
import org.eclipse.lspcommon.commons.JavaCompletionResult;

import io.openliberty.tools.eclipse.ls.plugin.LibertyToolsLSPlugin;
import org.eclipse.lsp4mp.jdt.core.MPNewPropertiesManagerForJava;

/**
 * Liberty Devex MicroProfile language client.
 * 
 * @author
 */
public class LibertyMPLSClientImpl extends LanguageClientImpl implements MicroProfileLanguageClientAPI {

    private static IMicroProfilePropertiesChangedListener SINGLETON_LISTENER;

    private IMicroProfilePropertiesChangedListener listener = event -> {
        ((MicroProfileLanguageServerAPI) getLanguageServer()).propertiesChanged(event);
    };

    public LibertyMPLSClientImpl() {
        if (SINGLETON_LISTENER != null) {
            MicroProfileCorePlugin.getDefault().removeMicroProfilePropertiesChangedListener(SINGLETON_LISTENER);
        }
        SINGLETON_LISTENER = listener;
        MicroProfileCorePlugin.getDefault().addMicroProfilePropertiesChangedListener(listener);
    }

    @Override
    public CompletableFuture<MicroProfileProjectInfo> getProjectInfo(MicroProfileProjectInfoParams params) {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            final MicroProfileProjectInfo[] projectInfo = new MicroProfileProjectInfo[1];
            Job job = Job.create("MicroProfile properties collector", (ICoreRunnable) monitor -> {
                projectInfo[0] = PropertiesManager.getInstance().getMicroProfileProjectInfo(params, JDTUtilsLSImpl.getInstance(), monitor);
            });
            job.schedule();
            try {
                job.join();
            } catch (InterruptedException e) {
                LibertyToolsLSPlugin.logException(e.getLocalizedMessage(), e);
            }
            return projectInfo[0];
        });

    }

    private IProgressMonitor getProgressMonitor(CancelChecker cancelChecker) {
        IProgressMonitor monitor = new NullProgressMonitor() {
            public boolean isCanceled() {
                cancelChecker.checkCanceled();
                return false;
            };
        };
        return monitor;
    }

    @Override
    public CompletableFuture<Location> getPropertyDefinition(MicroProfilePropertyDefinitionParams params) {

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> getJavaCodelens(JavaCodeLensParams javaParams) {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            IProgressMonitor monitor = getProgressMonitor(cancelChecker);
            try {
                return MPNewPropertiesManagerForJava.getInstance().codeLens(javaParams, JDTUtilsLSImpl.getInstance(), monitor);
            } catch (JavaModelException e) {
                LibertyToolsLSPlugin.logException(e.getLocalizedMessage(), e);
                return Collections.emptyList();
            }
        });
    }

    @Override
    public CompletableFuture<List<PublishDiagnosticsParams>> getJavaDiagnostics(Object javaParams) {
    	  	
    	MicroProfileDiagnosticsHandler mpDiagnosticsHandler = (MicroProfileDiagnosticsHandler) MPNewPropertiesManagerForJava.getInstance().getDiagnosticsHandler();
    	   	
        return CompletableFutures.computeAsync((cancelChecker) -> {
            IProgressMonitor monitor = getProgressMonitor(cancelChecker);
            try {
                return mpDiagnosticsHandler.diagnostics(MPNewPropertiesManagerForJava.getInstance().getPluginId(), javaParams, JDTUtilsLSImpl.getInstance(), monitor);
            	
            } catch (JavaModelException e) {
                LibertyToolsLSPlugin.logException(e.getLocalizedMessage(), e);
                return Collections.emptyList();
            }
        });
    }

    @Override
    public CompletableFuture<List<CodeAction>> getJavaCodeAction(JavaCodeActionParams javaParams) {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            IProgressMonitor monitor = getProgressMonitor(cancelChecker);
            try {
                return (List<CodeAction>) MPNewPropertiesManagerForJava.getInstance().codeAction(javaParams, JDTUtilsLSImpl.getInstance(),
                        monitor);
            } catch (JavaModelException e) {
                LibertyToolsLSPlugin.logException(e.getLocalizedMessage(), e);
                return Collections.emptyList();
            }
        });
    }

    @Override
    public CompletableFuture<List<ProjectLabelInfoEntry>> getAllJavaProjectLabels() {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            IProgressMonitor monitor = getProgressMonitor(cancelChecker);
            return ProjectLabelManager.getInstance().getProjectLabelInfo( MPNewPropertiesManagerForJava.getInstance().getPluginId());
        });
    }

    @Override
    public CompletableFuture<ProjectLabelInfoEntry> getJavaProjectLabels(JavaProjectLabelsParams javaParams) {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            IProgressMonitor monitor = getProgressMonitor(cancelChecker);
            return ProjectLabelManager.getInstance().getProjectLabelInfo(javaParams, MPNewPropertiesManagerForJava.getInstance().getPluginId(), JDTUtilsLSImpl.getInstance(), monitor);
        });
    }

    
    @Override
    public CompletableFuture<JavaFileInfo> getJavaFileInfo(JavaFileInfoParams javaParams) {
        return CompletableFutures.computeAsync(cancelChecker -> {
            IProgressMonitor monitor = getProgressMonitor(cancelChecker);
            return MPNewPropertiesManagerForJava.getInstance().fileInfo(javaParams, JDTUtilsLSImpl.getInstance());
        });
    }

    @Override
    public CompletableFuture<List<MicroProfileDefinition>> getJavaDefinition(JavaDefinitionParams javaParams) {
        return CompletableFutures.computeAsync(cancelChecker -> {
            IProgressMonitor monitor = getProgressMonitor(cancelChecker);
            try {
                List<Object> results = MPNewPropertiesManagerForJava.getInstance().definition(javaParams, JDTUtilsLSImpl.getInstance(), monitor);
            	
            		return results.stream().filter(s -> s instanceof MicroProfileDefinition)
            							   .map(MicroProfileDefinition.class::cast)
            							   .collect(Collectors.toList());
            	
            } catch (JavaModelException e) {
                LibertyToolsLSPlugin.logException(e.getLocalizedMessage(), e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<JavaCompletionResult> getJavaCompletion(JavaCompletionParams javaParams) {
        return CompletableFutures.computeAsync(cancelChecker -> {
            IProgressMonitor monitor = getProgressMonitor(cancelChecker);
            try {
                CompletionList completionList = MPNewPropertiesManagerForJava.getInstance().completion(javaParams, org.eclipse.lspcommon.jdt.internal.core.ls.JDTUtilsLSImpl.getInstance(), monitor);
                JavaCursorContextResult javaCursorContext = MPNewPropertiesManagerForJava.getInstance().javaCursorContext(javaParams,
                		org.eclipse.lspcommon.jdt.internal.core.ls.JDTUtilsLSImpl.getInstance(), monitor);
                return new JavaCompletionResult(completionList, javaCursorContext);
            } catch (JavaModelException e) {
                LibertyToolsLSPlugin.logException(e.getLocalizedMessage(), e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Hover> getJavaHover(JavaHoverParams javaParams) {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            IProgressMonitor monitor = getProgressMonitor(cancelChecker);
            try {
                return MPNewPropertiesManagerForJava.getInstance().hover(javaParams, JDTUtilsLSImpl.getInstance(), monitor);
            } catch (JavaModelException e) {
                LibertyToolsLSPlugin.logException(e.getLocalizedMessage(), e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<CodeAction> resolveCodeAction(CodeAction unresolved) {
        return CompletableFutures.computeAsync((cancelChecker) -> {
            try {
                IProgressMonitor monitor = getProgressMonitor(cancelChecker);
                // Deserialize CodeAction#data which is a JSonObject to CodeActionResolveData
                CodeActionResolveData resolveData = JSONUtility.toModel(unresolved.getData(), CodeActionResolveData.class);
                unresolved.setData(resolveData);
                return MPNewPropertiesManagerForJava.getInstance().resolveCodeAction(unresolved, JDTUtilsLSImpl.getInstance(), monitor);
            } catch (JavaModelException e) {
                LibertyToolsLSPlugin.logException(e.getLocalizedMessage(), e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<String> getPropertyDocumentation(MicroProfilePropertyDocumentationParams arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CompletableFuture<JavaCursorContextResult> getJavaCursorContext(JavaCompletionParams arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CompletableFuture<List<SymbolInformation>> getJavaWorkspaceSymbols(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }
}
