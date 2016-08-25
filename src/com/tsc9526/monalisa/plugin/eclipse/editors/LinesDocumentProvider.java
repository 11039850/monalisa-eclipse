package com.tsc9526.monalisa.plugin.eclipse.editors;

 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
@SuppressWarnings("restriction")
public class LinesDocumentProvider extends CompilationUnitDocumentProvider{
	 
	public void saveDocumentContent(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException {
		super.saveDocumentContent(monitor, element, document, overwrite);
	}
	 
	public IDocument getDocument(Object element) {
		 
		return super.getDocument(element);
	}
	
	public void connect(Object element) throws CoreException {
		 System.out.println("Connect: "+element);
		
		super.connect(element);
	}
	
	public void resetDocument(Object element) throws CoreException {
		super.resetDocument(element);
	}

	protected void commitFileBuffer(IProgressMonitor monitor, FileInfo info, boolean overwrite) throws CoreException {
		super.commitFileBuffer(monitor, info, overwrite);
	}
}
