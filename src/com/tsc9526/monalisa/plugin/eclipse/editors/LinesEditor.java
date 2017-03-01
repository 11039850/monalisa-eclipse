package com.tsc9526.monalisa.plugin.eclipse.editors;

 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.MultiPassContentFormatter;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
@SuppressWarnings("restriction")
public class LinesEditor extends CompilationUnitEditor{  
	protected LinesDocumentProvider documentProvider=new LinesDocumentProvider();
	
	public LinesEditor(){
		super();	
	}
	
	public IDocumentProvider getDocumentProvider() {
		return documentProvider;
	}
	
	protected JavaSourceViewerConfiguration createJavaSourceViewerConfiguration() {
		JavaTextTools textTools= JavaPlugin.getDefault().getJavaTextTools();
		return new JavaSourceViewerConfiguration(textTools.getColorManager(), getPreferenceStore(), this, IJavaPartitions.JAVA_PARTITIONING){
			public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
				final MultiPassContentFormatter formatter= new MultiPassContentFormatter(getConfiguredDocumentPartitioning(sourceViewer), IDocument.DEFAULT_CONTENT_TYPE);
				formatter.setMasterStrategy(new LinesFormattingStrategy());
				return formatter;
			}
		};
	} 
	 
	
	protected void doSetInput(IEditorInput input) throws CoreException {
		if(input instanceof LinesJavaInput){
			super.doSetInput(input);
		}else if(input instanceof FileEditorInput){
			super.doSetInput(new LinesJavaInput((FileEditorInput)input));
		}else{
			this.getDocumentProvider();
			super.doSetInput(input);
		}
	}
	
	protected void setSourceViewerConfiguration(SourceViewerConfiguration configuration) {
		if(configuration!=null &&  configuration.getClass()==JavaSourceViewerConfiguration.class){
			configuration=createJavaSourceViewerConfiguration();
		}
	 	super.setSourceViewerConfiguration(configuration);
	}
}
