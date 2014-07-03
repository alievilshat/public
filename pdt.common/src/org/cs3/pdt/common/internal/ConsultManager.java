package org.cs3.pdt.common.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cs3.pdt.common.PDTCommon;
import org.cs3.pdt.common.PDTCommonPlugin;
import org.cs3.pdt.common.PrologProcessStartListener;
import org.cs3.pdt.connector.PDTConnectorPlugin;
import org.cs3.pdt.connector.internal.service.ext.IPrologProcessServiceExtension;
import org.cs3.pdt.connector.service.ConsultListener;
import org.cs3.pdt.connector.util.FileUtils;
import org.cs3.prolog.connector.common.Debug;
import org.cs3.prolog.connector.process.PrologProcess;
import org.cs3.prolog.connector.process.PrologProcessException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;

public class ConsultManager implements ConsultListener, PrologProcessStartListener {

	@Override
	public void beforeConsult(PrologProcess process, List<IFile> files, IProgressMonitor monitor) throws PrologProcessException {
	}

	@Override
	public void afterConsult(PrologProcess process, List<IFile> files, List<String> allConsultedFiles, IProgressMonitor monitor) throws PrologProcessException {
		for (IFile file : files) {
			String prologFileName = FileUtils.prologFileName(file);
			addConsultedFile(process, prologFileName);
		}
		monitor.done();
	}

	@Override
	public void prologProcessStarted(PrologProcess process) {
		final String reconsultFiles = PDTCommonPlugin.getDefault().getPreferenceValue(PDTCommon.PREF_RECONSULT_ON_RESTART, PDTCommon.RECONSULT_NONE);
		
		if (reconsultFiles.equals(PDTCommon.RECONSULT_NONE)) {
			getConsultedFileList(process).clear();
		} else {
			reconsultFiles(process, reconsultFiles.equals(PDTCommon.RECONSULT_ENTRY));
		}
	}
	
	// TODO: problem with quotes
	private void reconsultFiles(PrologProcess process, boolean onlyEntryPoints) {
		Debug.debug("Reconsult files");
		List<String> consultedFiles = getConsultedFileList(process);
		if (consultedFiles != null) {
			synchronized (consultedFiles) {
				
				ArrayList<IFile> files = new ArrayList<IFile>();
				ArrayList<IFile> entryPointFiles = new ArrayList<IFile>();
				collectFiles(consultedFiles, files);
				IPrologProcessServiceExtension service = (IPrologProcessServiceExtension) PDTConnectorPlugin.getDefault().getPrologProcessService();
				if (onlyEntryPoints) {
					filterEntryPoints(files, entryPointFiles);
					service.consultFilesSilent(entryPointFiles, process);
				} else {
					service.consultFilesSilent(files, process);
				}
			}
		}
	}

	private List<String> getConsultedFileList(PrologProcess process) {
		@SuppressWarnings("unchecked")
		List<String> consultedFiles = (List<String>) process.getAttribute(PDTCommon.CONSULTED_FILES);
		return consultedFiles;
	}
	
	private void addConsultedFile(PrologProcess process, String fileName) {
		List<String> consultedFiles = getConsultedFileList(process);
		if (consultedFiles == null) {
			consultedFiles = new ArrayList<String>();
			process.setAttribute(PDTCommon.CONSULTED_FILES, consultedFiles);
		}
		synchronized (consultedFiles) {
			// only take the last consult of a file
			if (consultedFiles.remove(fileName)) {
				Debug.debug("move " + fileName + " to end of consulted files");			
			} else {
				Debug.debug("add " + fileName + " to consulted files");
			}
			consultedFiles.add(fileName);
		}
	}
	
	
	private void collectFiles(List<String> consultedFiles, List<IFile> files) {
		for (String consultedFile : consultedFiles) {
			IFile file;
			try {
				file = FileUtils.findFileForLocation(consultedFile);
				if (file != null){
					files.add(file);
				}
			} catch (IOException e) {
				Debug.report(e);
			}
		}
	}
	
	private void filterEntryPoints(List<IFile> files, List<IFile> entryPointFiles) {
		for (IFile file : files) {
			try {
				String isEntryPoint = file.getPersistentProperty(new QualifiedName("pdt", "entry.point"));
				if (isEntryPoint != null && isEntryPoint.equalsIgnoreCase("true")) {
					entryPointFiles.add(file);
				}
			} catch (CoreException e) {
				Debug.report(e);
			}
		}
	}

}
