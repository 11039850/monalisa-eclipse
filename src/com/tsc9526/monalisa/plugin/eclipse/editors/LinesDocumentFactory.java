package com.tsc9526.monalisa.plugin.eclipse.editors;

import org.eclipse.core.filebuffers.IDocumentFactory;
import org.eclipse.jface.text.IDocument;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
@SuppressWarnings({"deprecation"})
public class LinesDocumentFactory implements IDocumentFactory{
 
	public IDocument createDocument() {
		return new LinesDocument();
	}

}
