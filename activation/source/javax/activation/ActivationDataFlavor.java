/*
 * ActivationDataFlavor.java
 * Copyright (C) 2004 The Free Software Foundation
 *
 * This file is part of GNU Classpath Extensions (classpathx).
 * For more information please visit https://www.gnu.org/software/classpathx/
 *
 * classpathx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * classpathx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with classpathx.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package javax.activation;

import java.awt.datatransfer.DataFlavor;
import java.io.InputStream;

/**
 * Activation-specific DataFlavor with improved MIME parsing.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.0.2
 */
public class ActivationDataFlavor extends DataFlavor
{

    private String mimeType;
    private String humanPresentableName;
    private Class representationClass;

    /**
     * Constructor.
     * @param representationClass the representation class
     * @param mimeType the MIME type of the data
     * @param humanPresentableName the human-presentable name of the data
     * flavor
     */
    public ActivationDataFlavor(Class representationClass, String mimeType,
            String humanPresentableName)
    {
        super(mimeType, humanPresentableName);
        this.mimeType = mimeType;
        this.humanPresentableName = humanPresentableName;
        this.representationClass = representationClass;
    }

    /**
     * Constructor.
     * @param representationClass the representation class
     * @param humanPresentableName the human-presentable name of the data
     * flavor
     */
    public ActivationDataFlavor(Class representationClass,
            String humanPresentableName)
    {
        super(representationClass, humanPresentableName);
        mimeType = super.getMimeType();
        this.representationClass = representationClass;
        this.humanPresentableName = humanPresentableName;
    }

    /**
     * Constructor. The representation class is an InputStream.
     * @param mimeType the MIME type of the data
     * @param humanPresentableName the human-presentable name of the data
     * flavor
     */
    public ActivationDataFlavor(String mimeType, String humanPresentableName)
    {
        super(mimeType, humanPresentableName);
        this.mimeType = mimeType;
        this.humanPresentableName = humanPresentableName;
        representationClass = InputStream.class;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public Class getRepresentationClass()
    {
        return representationClass;
    }

    public String getHumanPresentableName()
    {
        return humanPresentableName;
    }

    public void setHumanPresentableName(String humanPresentableName)
    {
        this.humanPresentableName = humanPresentableName;
    }

    public boolean equals(DataFlavor dataFlavor)
    {
        return (isMimeTypeEqual(dataFlavor) &&
                dataFlavor.getRepresentationClass() == representationClass);
    }

    public boolean isMimeTypeEqual(String mimeType)
    {
        try
        {
            return new MimeType(this.mimeType).match(new MimeType(mimeType));
        }
        catch (MimeTypeParseException e)
        {
            return false;
        }
    }

    protected String normalizeMimeTypeParameter(String parameterName,
            String parameterValue)
    {
        return new StringBuffer(parameterName)
            .append('=')
            .append(parameterValue)
            .toString();
    }

    protected String normalizeMimeType(String mimeType)
    {
        return mimeType;
    }

}
