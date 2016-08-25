/*******************************************************************************************
 *	Copyright (c) 2016, zzg.zhou(11039850@qq.com)
 * 
 *  Monalisa Eclipse Plugin is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.

 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Lesser General Public License for more details.

 *	You should have received a copy of the GNU Lesser General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************************/
package com.tsc9526.monalisa.plugin.eclipse.proposal;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.internal.texteditor.HippieCompletionEngine;
import org.eclipse.ui.texteditor.HippieProposalProcessor;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
@SuppressWarnings("restriction")
public class SQLProposalProcessor   implements IContentAssistProcessor {
	protected HippieProposalProcessor fProcessor= new HippieProposalProcessor();
	
	private static final ICompletionProposal[] NO_PROPOSALS= new ICompletionProposal[0];
	private static final IContextInformation[] NO_CONTEXTS= new IContextInformation[0];

	private static final class Proposal implements ICompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2, ICompletionProposalExtension3, ICompletionProposalExtension4 {

		private final String fString;
		private final String fPrefix;
		private final int fOffset;

		public Proposal(String string, String prefix, int offset) {
			fString= string;
			fPrefix= prefix;
			fOffset= offset;
		}

		public void apply(IDocument document) {
			apply(null, '\0', 0, fOffset);
		}

		public Point getSelection(IDocument document) {
			return new Point(fOffset + fString.length(), 0);
		}

		public String getAdditionalProposalInfo() {
			return null;
		}

		public String getDisplayString() {
			return fString;
		}

		public Image getImage() {
			return null;
		}

		public IContextInformation getContextInformation() {
			return null;
		}

		public void apply(IDocument document, char trigger, int offset) {
			try {
				String replacement= fString.substring(offset - fOffset);
				document.replace(offset, 0, replacement);
			} catch (BadLocationException x) {
				// TODO Auto-generated catch block
				x.printStackTrace();
			}
		}

		public boolean isValidFor(IDocument document, int offset) {
			return validate(document, offset, null);
		}

		public char[] getTriggerCharacters() {
			return null;
		}

		public int getContextInformationPosition() {
			return 0;
		}

		public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
			apply(viewer.getDocument(), trigger, offset);
		}

		public void selected(ITextViewer viewer, boolean smartToggle) {
		}

		public void unselected(ITextViewer viewer) {
		}

		public boolean validate(IDocument document, int offset, DocumentEvent event) {
			try {
				int prefixStart= fOffset - fPrefix.length();
				return offset >= fOffset && offset < fOffset + fString.length() && document.get(prefixStart, offset - (prefixStart)).equals((fPrefix + fString).substring(0, offset - prefixStart));
			} catch (BadLocationException x) {
				return false;
			}
		}

		public IInformationControlCreator getInformationControlCreator() {
			return null;
		}

		public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
			return fPrefix + fString;
		}

		public int getPrefixCompletionStart(IDocument document, int completionOffset) {
			return fOffset - fPrefix.length();
		}

		public boolean isAutoInsertable() {
			return true;
		}

	}

	private final HippieCompletionEngine fEngine= new HippieCompletionEngine();

	/**
	 * Creates a new hippie completion proposal computer.
	 */
	public SQLProposalProcessor() {
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		try {
			String prefix= getPrefix(viewer, offset);
			if (prefix == null || prefix.length() == 0)
				return NO_PROPOSALS;

			List suggestions= getSuggestions(viewer, offset, prefix);

			List result= new ArrayList();
			for (Iterator it= suggestions.iterator(); it.hasNext();) {
				String string= (String) it.next();
				if (string.length() > 0)
					result.add(createProposal(string, prefix, offset));
			}

			return (ICompletionProposal[]) result.toArray(new ICompletionProposal[result.size()]);

		} catch (BadLocationException x) {
			// ignore and return no proposals
			return NO_PROPOSALS;
		}
	}

	private String getPrefix(ITextViewer viewer, int offset) throws BadLocationException {
		IDocument doc= viewer.getDocument();
		if (doc == null || offset > doc.getLength())
			return null;

		int length= 0;
		while (--offset >= 0 && Character.isJavaIdentifierPart(doc.getChar(offset)))
			length++;

		return doc.get(offset + 1, length);
	}

	private ICompletionProposal createProposal(String string, String prefix, int offset) {
		return new Proposal(string, prefix, offset);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		// no context informations for hippie completions
		return NO_CONTEXTS;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	/**
	 * Return the list of suggestions from the current document. First the document is searched
	 * backwards from the caret position and then forwards.
	 *
	 * @param offset the offset
	 * @param viewer the viewer
	 * @param prefix the completion prefix
	 * @return all possible completions that were found in the current document
	 * @throws BadLocationException if accessing the document fails
	 */
	private ArrayList createSuggestionsFromOpenDocument(ITextViewer viewer, int offset, String prefix) throws BadLocationException {
		IDocument document= viewer.getDocument();
		ArrayList completions= new ArrayList();
		completions.addAll(fEngine.getCompletionsBackwards(document, prefix, offset));
		completions.addAll(fEngine.getCompletionsForward(document, prefix, offset - prefix.length(), true));

		return completions;
	}

	/**
	 * Create the array of suggestions. It scans all open text editors and prefers suggestions from
	 * the currently open editor. It also adds the empty suggestion at the end.
	 *
	 * @param viewer the viewer
	 * @param offset the offset
	 * @param prefix the prefix to search for
	 * @return the list of all possible suggestions in the currently open editors
	 * @throws BadLocationException if accessing the current document fails
	 */
	private List<String> getSuggestions(ITextViewer viewer, int offset, String prefix) throws BadLocationException {
//		ArrayList suggestions= createSuggestionsFromOpenDocument(viewer, offset, prefix);
//		IDocument currentDocument= viewer.getDocument();
//
//		IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//		IEditorReference editorReferences[]= window.getActivePage().getEditorReferences();
//
//		for (int i= 0; i < editorReferences.length; i++) {
//			IEditorPart editor= editorReferences[i].getEditor(false); // don't create!
//			if (editor instanceof ITextEditor) {
//				ITextEditor textEditor= (ITextEditor) editor;
//				IEditorInput input= textEditor.getEditorInput();
//				IDocument doc= textEditor.getDocumentProvider().getDocument(input);
//				if (!currentDocument.equals(doc))
//					suggestions.addAll(fEngine.getCompletionsForward(doc, prefix, 0, false));
//			}
//		}
//		 
//		List<String> uniqueSuggestions= fEngine.makeUnique(suggestions);

		return new ArrayList<String>();
		
// 		add the empty suggestion
//		IDocument doc=viewer.getDocument();
//		if(doc instanceof LinesDocument){
//			LinesDocument document=(LinesDocument)doc;
//				
//			if(document.inLines(offset)){
//		
//			}
//					
//		}
		
//		if(prefix.startsWith("sel")){
//			suggestions.add("SELECT * FROM"); //$NON-NLS-1$
//			suggestions.add("SELECT ~ FROM");
//		} 
//		suggestions.add("");
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalComputer#getErrorMessage()
	 */
	public String getErrorMessage() {
		return null; // no custom error message
	}
}
