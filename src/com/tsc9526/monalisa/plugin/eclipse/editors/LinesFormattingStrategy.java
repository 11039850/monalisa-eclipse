package com.tsc9526.monalisa.plugin.eclipse.editors;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.JavaFormattingStrategy;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
@SuppressWarnings("restriction")
public class LinesFormattingStrategy extends JavaFormattingStrategy{
	 
	public LinesFormattingStrategy() {
	}

	private final LinkedList<IDocument> fDocuments= new LinkedList<IDocument>();
	private final LinkedList<TypedPosition> fPartitions= new LinkedList<TypedPosition>();
	
	public void formatterStarts(final IFormattingContext context) {
		super.formatterStarts(context);

		fPartitions.addLast((TypedPosition) context.getProperty(FormattingContextProperties.CONTEXT_PARTITION));
		fDocuments.addLast((IDocument) context.getProperty(FormattingContextProperties.CONTEXT_MEDIUM));
	}

	public void formatterStops() {
		super.formatterStops();

		fPartitions.clear();
		fDocuments.clear();
	}
	
	public void format() {
		IDocument document= fDocuments.removeFirst();
		TypedPosition partition= fPartitions.removeFirst();
		if (document != null && partition != null) {
			doFormat(document,partition);
		}
 	}
	
	@SuppressWarnings("unchecked")
	protected void doFormat(final IDocument document,final TypedPosition partition){
		Map<String, IDocumentPartitioner> partitioners= null;
		try {
			final TextEdit edit= CodeFormatterUtil.reformat(CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, document.get(), partition.getOffset(), partition.getLength(), 0, TextUtilities.getDefaultLineDelimiter(document), getPreferences());
			if (edit != null) {
				if (edit.getChildrenSize() > 20){
					partitioners= TextUtilities.removeDocumentPartitioners(document);
				}
				
				List<Integer[]> linesArray=LinesCodeTransform.getLinesArray(document.get());
				
				if (edit instanceof MultiTextEdit) {
					MultiTextEdit medit = (MultiTextEdit) edit;
					for (TextEdit x : medit.getChildren()) {
						for (Integer[] xs : linesArray) {
							int offset = x.getOffset();
							if (offset >= xs[0] && offset <= xs[1]) {
								medit.removeChild(x);
							}
						}
					}
				}
				
				edit.apply(document);
			}
		} catch (MalformedTreeException exception) {
			JavaPlugin.log(exception);
		} catch (BadLocationException exception) {
			JavaPlugin.log(exception);
		} finally {
			if (partitioners != null){
				TextUtilities.addDocumentPartitioners(document, partitioners);
			}
		}
	}
}