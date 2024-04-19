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
package org.xwiki.rendering.macro.formula;

import java.util.Arrays;

import javax.inject.Provider;

import org.junit.jupiter.api.AfterEach;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.formula.FormulaRenderer;
import org.xwiki.formula.ImageStorage;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.test.integration.junit5.RenderingTests;
import org.xwiki.resource.ResourceReferenceSerializer;
import org.xwiki.resource.temporary.TemporaryResourceReference;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.url.ExtendedURL;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Run all tests found in {@code *.test} files located in the classpath. These {@code *.test} files must follow the
 * conventions described in {@link org.xwiki.rendering.test.integration.TestDataParser}.
 *
 * @version $Id$
 * @since 3.0RC1
 */
@AllComponents
public class IntegrationTests implements RenderingTests
{
    private ImageStorage mockImageStorage;

    private FormulaMacroConfiguration mockConfiguration;

    @RenderingTests.Initialized
    public void initialize(MockitoComponentManager componentManager) throws Exception
    {
        // XWiki Context Mock
        Provider<XWikiContext> mockXWikiContextProvider = componentManager.registerMockComponent(
            new DefaultParameterizedType(null, Provider.class, XWikiContext.class));
        XWikiContext xcontext = new XWikiContext();
        DocumentReference documentReference = new DocumentReference("wiki", "space", "page");
        XWikiDocument document = new XWikiDocument(documentReference);
        xcontext.setDoc(document);
        when(mockXWikiContextProvider.get()).thenReturn(xcontext);

        // Temporary Resource Serializer Mock
        ResourceReferenceSerializer<TemporaryResourceReference, ExtendedURL> mockResourceSerializer =
            componentManager.registerMockComponent(new DefaultParameterizedType(null,
                ResourceReferenceSerializer.class, TemporaryResourceReference.class,ExtendedURL.class));

        TemporaryResourceReference temporaryResourceReference1 = new TemporaryResourceReference("formula",
            "190ef2f68e7fbd75c869d74dea959b1a48faadefc7a0c9219e3e94d005821935", documentReference);
        ExtendedURL extendedURL1 = new ExtendedURL(Arrays.asList(
            "xwiki", "tmp", "formula", "190ef2f68e7fbd75c869d74dea959b1a48faadefc7a0c9219e3e94d005821935"));
        when(mockResourceSerializer.serialize(temporaryResourceReference1)).thenReturn(extendedURL1);

        TemporaryResourceReference temporaryResourceReference2 = new TemporaryResourceReference("formula",
            "06fbba0acf130efd9e147fdfe91a943cc4f3e29972c6cd1d972e9aabf0900966", documentReference);
        ExtendedURL extendedURL2 = new ExtendedURL(Arrays.asList(
            "xwiki", "tmp", "formula", "06fbba0acf130efd9e147fdfe91a943cc4f3e29972c6cd1d972e9aabf0900966"));
        when(mockResourceSerializer.serialize(temporaryResourceReference2)).thenReturn(extendedURL2);

        // Image Storage Mock
        this.mockImageStorage = componentManager.registerMockComponent(ImageStorage.class);

        // Configuration Mock
        this.mockConfiguration = componentManager.registerMockComponent(FormulaMacroConfiguration.class);
        when(this.mockConfiguration.getRenderer()).thenReturn("snuggletex");
        when(this.mockConfiguration.getDefaultType()).thenReturn(FormulaRenderer.Type.DEFAULT);
        when(this.mockConfiguration.getDefaultFontSize()).thenReturn(FormulaRenderer.FontSize.DEFAULT);
        when(this.mockImageStorage.get(any(String.class))).thenReturn(null);

        // Wiki Descriptor Manager (drawn by "default" ResourceReferenceSerializer)
        WikiDescriptorManager wikiDescriptorManager =
            componentManager.registerMockComponent(WikiDescriptorManager.class);
        when(wikiDescriptorManager.getCurrentWikiId()).thenReturn("wiki");
    }

    @AfterEach
    public void after()
    {
        verify(this.mockConfiguration, times(1)).getRenderer();
        verify(this.mockConfiguration, times(1)).getDefaultType();
        verify(this.mockImageStorage, times(1)).get(any(String.class));
    }
}