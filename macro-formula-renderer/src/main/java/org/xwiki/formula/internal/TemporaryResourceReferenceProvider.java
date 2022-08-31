/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.formula.internal;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.formula.FormulaRenderer;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.resource.temporary.TemporaryResourceReference;

import com.xpn.xwiki.XWikiContext;

/**
 * Create a Temporary Resource Reference pointing at a generated formula image, based on a passed id. This is
 * temporary while waiting for a refactoring of ImageStorage and/or FormulaRenderer.
 *
 * @version $Id$
 * @since 14.7
 */
@Component(roles = TemporaryResourceReferenceProvider.class)
@Singleton
public class TemporaryResourceReferenceProvider
{
    private static final String MODULE_ID = "formula";

    @Inject
    private Provider<XWikiContext> xwikiContextProvider;

    /**
     * @param imageId the identifier for the image, returned by
     *        {@link FormulaRenderer#process(String, boolean, FormulaRenderer.FontSize, FormulaRenderer.Type)}
     * @return the Temporary Resource Reference pointing at a generated formula image, based on the passed id
     */
    public TemporaryResourceReference getImageReference(String imageId)
    {
        return getReference(imageId, "");
    }

    /**
     * @param imageId the identifier for the image, returned by
     *        {@link FormulaRenderer#process(String, boolean, FormulaRenderer.FontSize, FormulaRenderer.Type)}
     * @return the Temporary Resource Reference pointing at a generated formula image type, based on the passed id
     */
    public TemporaryResourceReference getImageTypeReference(String imageId)
    {
        return getReference(imageId, "Type");
    }

    private TemporaryResourceReference getReference(String imageId, String suffix)
    {
        // TODO: Fix this by changing the ImageStorage interface and passing an EntityReference. FTM we don't change
        // this in version 14.7 to not break backward compatibility and still offer the same ImageData and ImageStorage
        // classes for those using it when it was in platform (v <= 14.6).
        // Note: It's possible this won't work if the context is not set up correctly prior to calling ImageStorage.
        XWikiContext xcontext = this.xwikiContextProvider.get();
        DocumentReference currentDocumentReference = xcontext.getDoc().getDocumentReference();
        return new TemporaryResourceReference(MODULE_ID, String.format("%s%s", imageId, suffix),
            currentDocumentReference);
    }
}
