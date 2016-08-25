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
 
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.w3c.dom.Node;

import com.tsc9526.monalisa.plugin.eclipse.logger.Logger;
import com.tsc9526.monalisa.plugin.eclipse.tools.MonalisaConstants;
import com.tsc9526.monalisa.plugin.eclipse.tools.XpathUtil;

/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
@SuppressWarnings("restriction")
public class QueryNamespaceCache
{
	private static final QueryNamespaceCache INSTANCE = new QueryNamespaceCache();

	private IContentType queryContentType = Platform.getContentTypeManager().getContentType(MonalisaConstants.CONTENT_TYPE_QUERY);

	private final Map<String, Map<String, IFile>> cache = new ConcurrentHashMap<String, Map<String, IFile>>();

	public IFile get(IJavaProject javaProject, String namespace, IReporter reporter)
	{
		Map<String, IFile> map = getCacheMap(javaProject, reporter);
		return map.get(namespace);
	}

	public void clear()
	{
		cache.clear();
	}

	public void remove(IProject project)
	{
		cache.remove(project.getName());
	}

	public void remove(String projectName, IFile file)
	{
		Map<String, IFile> map = cache.get(projectName);
		if (map == null)
			return;
		Iterator<Entry<String, IFile>> iterator = map.entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<String, IFile> entry = iterator.next();
			if (file.equals(entry.getValue()))
			{
				iterator.remove();
			}
		}
	}

	public void put(String projectName, IFile file)
	{
		remove(projectName, file);

		Map<String, IFile> map = cache.get(projectName);
		if (map == null)
			return;

		String namespace = extractNamespace(file);
		if (namespace != null)
		{
			map.put(namespace, file);
		}
	}

	public Map<String, IFile> getCacheMap(IJavaProject javaProject, IReporter reporter)
	{
		String projectName = javaProject.getElementName();
		Map<String, IFile> map = cache.get(projectName);
		if (map == null)
		{
			map = new ConcurrentHashMap<String, IFile>();
			cache.put(projectName, map);
			collectMappers(javaProject, map, reporter);
		}
		return map;
	}

	private void collectMappers(IJavaProject project, final Map<String, IFile> map,
		final IReporter reporter)
	{
		try
		{
			for (IPackageFragmentRoot root : project.getAllPackageFragmentRoots())
			{
				if (root.getKind() != IPackageFragmentRoot.K_SOURCE)
				{
					continue;
				}

				root.getResource().accept(new IResourceProxyVisitor()
				{
					@Override
					public boolean visit(IResourceProxy proxy) throws CoreException
					{
						if (!proxy.isDerived() && proxy.getType() == IResource.FILE
							&& proxy.getName().endsWith(".xml"))
						{
							IFile file = (IFile)proxy.requestResource();
							IContentDescription contentDesc = file.getContentDescription();
							if (contentDesc != null)
							{
								IContentType contentType = contentDesc.getContentType();
								if (contentType != null && contentType.isKindOf(queryContentType))
								{
									String namespace = extractNamespace(file);
									if (namespace != null)
									{
										map.put(namespace, file);
									}
									return false;
								}
							}
						}
						return true;
					}
				}, IContainer.NONE);
			}
		}
		catch (CoreException e)
		{
			Logger.error("Searching MyBatis Mapper xml failed.", e);
		}
	}

	private String extractNamespace(IFile file)
	{
		IStructuredModel model = null;
		try
		{
			model = StructuredModelManager.getModelManager().getModelForRead(file);
			IDOMModel domModel = (IDOMModel)model;
			IDOMDocument domDoc = domModel.getDocument();

			Node node = XpathUtil.xpathNode(domDoc, "//mapper/@namespace");
			return node == null ? null : node.getNodeValue();
		}
		catch (Exception e)
		{
			Logger.error("Error occurred during parsing mapper:" + file.getFullPath(),e);
		}
		finally
		{
			if (model != null)
			{
				model.releaseFromRead();
			}
		}
		return null;
	}

	public static QueryNamespaceCache getInstance()
	{
		return INSTANCE;
	}

	private QueryNamespaceCache()
	{
		super();
	}
}
