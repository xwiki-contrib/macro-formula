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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.formula.FormulaRenderer;
import org.xwiki.formula.ImageData;
import org.xwiki.formula.ImageStorage;
import org.xwiki.resource.temporary.TemporaryResourceReference;
import org.xwiki.resource.temporary.TemporaryResourceStore;

/**
 * Implementation for the {@link ImageStorage} component using Temporary resources.
 * 
 * @version $Id$
 * @since 14.7
 */
@Component
@Singleton
public class TemporaryImageStorage implements ImageStorage
{
    /**
     * Used to create and access the temporary files.
     */
    @Inject
    private TemporaryResourceStore temporaryResourceStore;

    @Inject
    private TemporaryResourceReferenceProvider resourceReferenceProvider;

    @Override
    public ImageData get(String id)
    {
        if (StringUtils.isEmpty(id)) {
            return null;
        }

        ImageData imageData = null;

        // Load the image
        File file = loadFile(this.resourceReferenceProvider.getImageReference(id));
        if (file != null) {
            // Load the image type
            File typeFile = loadFile(this.resourceReferenceProvider.getImageTypeReference(id));
            if (typeFile != null) {
                try {
                    byte[] imageBytes = FileUtils.readFileToByteArray(file);
                    FormulaRenderer.Type imageType =
                        FormulaRenderer.Type.valueOf(FileUtils.readFileToString(typeFile, "UTF-8"));
                    imageData = new ImageData(imageBytes, imageType);
                } catch (IOException e) {
                    throw new RuntimeException(String.format("Failed to read Formula image data for id [%s]", id), e);
                }
            }
        }

        return imageData;
    }

    @Override
    public void put(String id, ImageData data)
    {
        if (StringUtils.isEmpty(id)) {
            return;
        }

        try {
            // Save the image
            InputStream is = new ByteArrayInputStream(data.getData());
            this.temporaryResourceStore.createTemporaryFile(this.resourceReferenceProvider.getImageReference(id), is);
            // Save the image type
            InputStream typeIs = new ByteArrayInputStream(data.getType().name().getBytes());
            this.temporaryResourceStore.createTemporaryFile(this.resourceReferenceProvider.getImageTypeReference(id),
                typeIs);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to write Formula image data [%s] for id [%s]",
                data, id), e);
        }
    }

    private File loadFile(TemporaryResourceReference reference)
    {
        File file;
        try {
            file = this.temporaryResourceStore.getTemporaryFile(reference);
        } catch (IOException e) {
            // This happens when the resource path is invalid. In our case it's always valid since we control the
            // passed id
            file = null;
        }
        if (file != null && !file.exists()) {
            file = null;
        }
        return file;
    }
}
