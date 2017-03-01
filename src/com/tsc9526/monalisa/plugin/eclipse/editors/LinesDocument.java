package com.tsc9526.monalisa.plugin.eclipse.editors;

import java.util.List;

import org.eclipse.core.internal.filebuffers.ResourceTextFileBuffer;
import org.eclipse.core.internal.filebuffers.SynchronizableDocument;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.internal.ui.text.java.JavaFormattingStrategy;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextStore;

import com.tsc9526.monalisa.plugin.eclipse.activator.MonalisaPlugin;
import com.tsc9526.monalisa.plugin.eclipse.console.MMC;
import com.tsc9526.monalisa.tools.io.JavaFile;
 
/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
@SuppressWarnings("restriction")
public class LinesDocument extends SynchronizableDocument implements IDocumentListener {
	private boolean increase="true".equalsIgnoreCase( System.getProperty("com.tsc9526.monalisa.plugin.eclipse.editors.increase", "true"));
	
	private int delta=-1;
	private List<Integer[]> linesArray=null;
 	 
	public LinesDocument() {
		super();
	   
		setTextStore(new StringTextStore(""));
		completeInitialization();
		
		this.addDocumentListener(this);
	}
	
	protected boolean isLinesOn() {
		return MonalisaPlugin.getDefault().isLinesOn();
	}
	  
	
	public void set(String text) {
		if(isLinesOn()){
			text=LinesCodeTransform.fromJavaCode(text);
		}
		super.set(text);
	}
	
	public void set(String text, long modificationStamp) {
		if(isLinesOn()){
			text=LinesCodeTransform.fromJavaCode(text);
		}
		super.set(text, modificationStamp);
	}
	 
	public String get() {
		String text=super.get();
	
		Exception exception=new Exception();
		StackTraceElement ste=exception.getStackTrace()[1];
		
		if(ste.getClassName().equals(ResourceTextFileBuffer.class.getName())
			&& ste.getMethodName().equals("commitFileBufferContent")){
			
			if(increase){
				text=doVersionIncrease(text);
			}
			
			text=LinesCodeTransform.toJavaCode(text);
		}	
		
		return text;
	}
	
	protected String doVersionIncrease(String text){
		try{
			JavaFile java=new JavaFile(text);
			
			long newVersion=java.increaseVersion();
			if(newVersion>=0){
				int p1=java.getpVersion1();
				int p2=java.getpVersion2();
				
				super.replace(p1,p2-p1, " "+newVersion+"L");
				
				text=super.get();
			 	
//				IResource r = unit.getUnit().getJavaElement().getResource();
//				String filePath = unit.getProjectPath() + "/" + r.getProjectRelativePath();
//
//				MMC mmc = MMC.getConsole();
//				mmc.print(MelpDate.now() + " [I] ****** Starting generate result classes from: ", SWT.COLOR_BLACK);
//				mmc.print(new HyperLink("file://" + filePath, unit.getPackageName() + "." + unit.getUnitName()), SWT.COLOR_DARK_BLUE);
				
				MMC.getConsole().info("$VERSION"+(java.isNaturalIncreasing()?"$":"")+" update to: "+newVersion);
			}
		}catch(Exception e){
			MMC.getConsole().error(e);
		}
		return text;
	}
	
	public void replace(int offset, int length, String text) throws BadLocationException {
		super.replace(offset,length, text);
	}
	
	public void replace(int offset, int length, String text, long modificationStamp) throws BadLocationException {
		int[] r=getRange(offset,length,text);
		
		if(r!=null){
			super.replace(r[0],r[1], text, modificationStamp);
			
			if(linesArray!=null){
				
			}
		}
	}
	
	
	protected int[] getRange(int offset, int length, String text){
		int[] r=new int[]{offset,length};
		//System.err.println("replace: ["+offset+","+length+"] "+text);
		
		if(formatting()){
			if(delta==-1){
				delta=0;
				
				linesArray=LinesCodeTransform.getLinesArray(super.get());
			}
			
			if(inLines(offset)){
				return null;
			}
			
		}
		return r;
	}
	
	public boolean inLines(int offset){
		if(linesArray!=null){
			for (Integer[] xs : linesArray) {
				if (offset >= xs[0] && offset <= xs[1]) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected boolean formatting(){
		Exception exception=new Exception();
		StackTraceElement[] stes=exception.getStackTrace();
		for(int i=0;i<stes.length && i<15;i++){
			StackTraceElement ste=stes[i];
			if(ste.getClassName().equals(JavaFormattingStrategy.class.getName())
				&& ste.getMethodName().equals("format")){
				return true;
			}
		}
		
		return false;
	}
	
	public static class StringTextStore implements ITextStore {
		
		private StringBuffer fContent;
 
		public StringTextStore(String content) {
			Assert.isNotNull(content);
			fContent= new StringBuffer(content);
		}
	 
		public char get(int offset) {
			return fContent.charAt(offset);
		}

		public String get(int offset, int length) {
			return fContent.substring(offset, offset + length);
		}

		public int getLength() {
			return fContent.length();
		}

		public void replace(int offset, int length, String text) {
			fContent.replace(offset, offset+length, text);
		}

		public void set(String text) {
			fContent.replace(0, getLength(), text);
		}

	}

	public void documentAboutToBeChanged(DocumentEvent event) {
		 //System.out.println("documentAboutToBeChanged： "+event);
	}

	public void documentChanged(DocumentEvent event) {
		delta=-1;
		linesArray=null;
		
//		IEditorPart editor=null;
//		IWorkbenchWindow window=PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//		if(window!=null && window.getActivePage()!=null){
//			editor=window.getActivePage().getActiveEditor();
//		}
//		
//		if(editor instanceof CompilationUnitEditor){
//			ISourceViewer view=((CompilationUnitEditor) editor).getViewer();
//			StyledText txt=view.getTextWidget();
//			
//			int offset=event.getOffset();
//			
//			Integer[] cls=null;
//			List<Integer[]> ls=LinesCodeTransform.getLinesArray(txt.getText());
//			for(Integer[] l:ls){
//				if(offset>=l[0] && offset<l[1]){
//					cls=l;
//					break;
//				}
//			}
//			
//			if(cls!=null){
//				int start=cls[0];
//				int length=cls[1]-cls[0];
//				
//				StyleRange range=new StyleRange();
//				range.start=start;
//				range.length=length;
//				range.foreground=new Color(null, 0xff,0x00,0x00);
//				
//				StyleRange[] srs=txt.getStyleRanges();
//				List<StyleRange> xrs=new ArrayList<StyleRange>();
//				for(StyleRange r:srs){
//					if( (r.start+r.length) < start || r.start>= (start+length)){
//						xrs.add(r);
//					}else{
//						 
//					}
//				}
//				xrs.add(range);
//				
//				StyleRange[] rs=xrs.toArray(new StyleRange[0]);
//				Arrays.sort(rs, new Comparator<StyleRange>() {
//					public int compare(StyleRange o1, StyleRange o2) {
//						return o1.start-o2.start;
//					}
//				});
//				
//				for(StyleRange r:rs){
//					System.out.println(r.start+" ~ "+r.length+": "+(r.start+r.length));
//				}
//				txt.setStyleRanges(rs);
//			}
//		}
//		System.out.println("documentChanged=： "+event);
	}
}
