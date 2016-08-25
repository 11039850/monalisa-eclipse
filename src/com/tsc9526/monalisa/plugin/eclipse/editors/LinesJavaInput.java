package com.tsc9526.monalisa.plugin.eclipse.editors;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.part.FileEditorInput;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class LinesJavaInput extends FileEditorInput{
	private FileEditorInput input;
	
	public LinesJavaInput(IFile file) {
		super(file);
	}
	
	public LinesJavaInput(FileEditorInput input) {
		super(input.getFile());
		
		this.input=input;
	}

	public int hashCode() {
		return input.hashCode();
	}

	public boolean equals(Object obj) {
		return input.equals(obj);
	}

	public boolean exists() {
		return input.exists();
	}

	public String getFactoryId() {
		return input.getFactoryId();
	}

	public IFile getFile() {
		return input.getFile();
	}

	public ImageDescriptor getImageDescriptor() {
		return input.getImageDescriptor();
	}

	public String getName() {
		return input.getName();
	}

	public IPersistableElement getPersistable() {
		return input.getPersistable();
	}

	public IStorage getStorage() {
		return input.getStorage();
	}

	public String getToolTipText() {
		return input.getToolTipText();
	}

	public void saveState(IMemento memento) {
		input.saveState(memento);
	}

	public URI getURI() {
		return input.getURI();
	}

	public IPath getPath() {
		return input.getPath();
	}

	public String toString() {
		return input.toString();
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		Object v= input.getAdapter(adapter);
		
		if(adapter.equals(org.eclipse.jdt.core.IJavaElement.class)){
			return v;
		}else{
			return v;
		}
	}

}
