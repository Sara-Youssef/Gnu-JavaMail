/*
 * BODYSTRUCTURE.java
 * Copyright (C) 2013 The Free Software Foundation
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

package gnu.inet.imap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A BODYSTRUCTURE data item in a FETCH response.
 * @version 1.2
 * @since 1.2
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class BODYSTRUCTURE
  extends FetchDataItem
{

  /**
   * A MIME part.
   */
  public abstract static class Part
  {

    private final String primaryType;
    private final String subtype;

    Part(String primaryType, String subtype)
    {
      this.primaryType = primaryType;
      this.subtype = subtype;
    }

    public String getPrimaryType()
    {
      return primaryType;
    }

    public String getSubtype()
    {
      return subtype;
    }

    public abstract Map<String,String> getParameters();

  }

  public static class BodyPart
    extends Part
  {

    private final Map<String,String> parameters;
    private final String id;
    private final String description;
    private final String encoding;
    private final int size;

    BodyPart(String primaryType,
             String subtype,
             Map<String,String> parameters,
             String id,
             String description,
             String encoding,
             int size)
    {
      super(primaryType, subtype);
      this.parameters = parameters;
      this.id = id;
      this.description = description;
      this.encoding = encoding;
      this.size = size;
    }

    public Map<String,String> getParameters()
    {
      return parameters;
    }

    public String getId()
    {
      return id;
    }

    public String getDescription()
    {
      return description;
    }

    public String getEncoding()
    {
      return encoding;
    }

    public int getSize()
    {
      return size;
    }

  }

  public static final class TextPart
    extends BodyPart
  {

    private final int lines;

    TextPart(String primaryType,
             String subtype,
             Map<String,String> parameters,
             String id,
             String description,
             String encoding,
             int size,
             int lines)
    {
      super(primaryType, subtype, parameters, id, description, encoding, size);
      this.lines = lines;
    }

    public int getLines()
    {
      return lines;
    }

  }

  public static final class MessagePart
    extends BodyPart
  {

    private final ENVELOPE envelope;
    private final Part bodystructure;
    private final int lines;

    MessagePart(String primaryType,
                String subtype,
                Map<String,String> parameters,
                String id,
                String description,
                String encoding,
                int size,
                ENVELOPE envelope,
                Part bodystructure,
                int lines)
    {
      super(primaryType, subtype, parameters, id, description, encoding, size);
      this.envelope = envelope;
      this.bodystructure = bodystructure;
      this.lines = lines;
    }

    public ENVELOPE getEnvelope()
    {
      return envelope;
    }

    public Part getBodystructure()
    {
      return bodystructure;
    }

    public int getLines()
    {
      return lines;
    }

  }

  public static final class Multipart
    extends Part
  {

    private final List<Part> parts;
    private final Map<String,String> parameters;
    private final Disposition disposition;
    private final List<String> language;
    private final List<String> location;

    Multipart(List<Part> parts,
              String subtype,
              Map<String,String> parameters,
              Disposition disposition,
              List<String> language,
              List<String> location)
    {
      super("multipart", subtype);
      this.parts = parts;
      this.parameters = parameters;
      this.disposition = disposition;
      this.language = language;
      this.location = location;
    }

    public List<Part> getParts()
    {
      return parts;
    }

    /**
     * Returns the parameters for this part.
     */
    public Map<String,String> getParameters()
    {
      return parameters;
    }

    /**
     * Returns the content disposition of this part.
     */
    public Disposition getDisposition()
    {
      return disposition;
    }

    public List<String> getLanguage()
    {
      return language;
    }

    public List<String> getLocation()
    {
      return location;
    }

  }

  public static class Disposition
  {

    private final String type;
    private final Map<String,String> parameters;

    Disposition(String type, Map<String,String> parameters)
    {
      this.type = type;
      this.parameters = parameters;
    }

    public String getType()
    {
      return type;
    }

    public Map<String,String> getParameters()
    {
      return parameters;
    }

  }

  private final Part part;

  BODYSTRUCTURE(Part part)
  {
    this.part = part;
  }

  public Part getPart()
  {
    return part;
  }

}
