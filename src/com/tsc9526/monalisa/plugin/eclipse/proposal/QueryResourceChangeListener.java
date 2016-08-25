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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;

import com.tsc9526.monalisa.plugin.eclipse.logger.Logger;
import com.tsc9526.monalisa.plugin.eclipse.tools.MonalisaConstants;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public class QueryResourceChangeListener implements IResourceChangeListener {
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_BUILD && event.getBuildKind() == IncrementalProjectBuilder.CLEAN_BUILD) {
			Object source = event.getSource();
			if (source instanceof IWorkspace) {
				 
			} else if (source instanceof IProject) {
				 
			}
		} else if (event.getType() != IResourceChangeEvent.POST_CHANGE)
			return;

		IResourceDelta delta = event.getDelta();
		if (delta.getKind() == IResourceDelta.CHANGED && (delta.getFlags() == IResourceDelta.ENCODING || delta.getFlags() == IResourceDelta.MARKERS))
			return;

		IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
			public boolean visit(IResourceDelta delta) {
				IResource resource = delta.getResource();
				if (resource.isDerived())
					return false;

				if (resource.getType() == IResource.FILE) {
					if (delta.getKind() == IResourceDelta.CHANGED
							&& (delta.getFlags() == IResourceDelta.ENCODING || delta.getFlags() == IResourceDelta.MARKERS))
						return false;

					IProject project = resource.getProject();
					IFile file = (IFile) resource;
					if (!file.exists()){
						return false;
					}else if ("jsp".equals(file.getFileExtension())) {
						onJspChange(delta, resource, project, file);
						return true;
					}else if ("java".equals(file.getFileExtension())) {
						onJavaChange(delta, resource, project, file);
						return true;
					}
				} else if (resource.getType() == IResource.PROJECT) {
					if (delta.getKind() == IResourceDelta.REMOVED) {
						 
					}
				}
				return true;
			}
 
			protected void onJavaChange(IResourceDelta delta, IResource resource, IProject project, IFile file) {
				if (delta.getKind() == IResourceDelta.REMOVED || !file.exists()) {
					// Cannot get content-type. Try removing the cache anyway.
					 
				} else {
					try {
						IContentDescription contentDesc = file.getContentDescription();
						if (contentDesc != null) {
							IContentType contentType = contentDesc.getContentType();
							if (contentType != null) {
								if (contentType.isKindOf(MonalisaConstants.queryContentType)) {
									QueryNamespaceCache.getInstance().put(project.getName(), file);
								}
							}
						}
					} catch (CoreException e) {
						Logger.error(e.getMessage(), e);
					}
				}
			}
			

			protected void onJspChange(IResourceDelta delta, IResource resource, IProject project, IFile file) {
				if (delta.getKind() == IResourceDelta.REMOVED || !file.exists()) {
					// Cannot get content-type. Try removing the cache anyway.
					 
				} else {
					try {
						IContentDescription contentDesc = file.getContentDescription();
						if (contentDesc != null) {
							IContentType contentType = contentDesc.getContentType();
							if (contentType != null) {
								if (contentType.isKindOf(MonalisaConstants.queryContentType)) {
									QueryNamespaceCache.getInstance().put(project.getName(), file);
								}
							}
						}
					} catch (CoreException e) {
						Logger.error(e.getMessage(), e);
					}
				}
			}
		};
		
		try {
			delta.accept(visitor);
		} catch (CoreException e) {
			Logger.error(e.getMessage(), e);
		}
	}
}