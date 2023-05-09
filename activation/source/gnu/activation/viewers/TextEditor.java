/*
 * TextEditor.java
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
package gnu.activation.viewers;

import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.activation.CommandObject;
import javax.activation.DataHandler;

/**
 * Simple text editor component.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.0.2
 */
public class TextEditor extends TextArea
    implements CommandObject, ActionListener
{

    private transient DataHandler dh;

    public TextEditor()
    {
        super("", 24, 80, 1);
    }

    public Dimension getPreferredSize()
    {
        return getMinimumSize(24, 80);
    }

    public void setCommandContext(String verb, DataHandler dh)
        throws IOException
    {
        this.dh = dh;
        InputStream in = dh.getInputStream();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        for (int len = in.read(buf); len != -1; len = in.read(buf))
            bytes.write(buf, 0, len);
        in.close();
        setText(bytes.toString());
    }

    public void actionPerformed(ActionEvent event)
    {
        if ("save".equals(event.getActionCommand()) && dh != null)
        {
            OutputStream out = null;
            try
            {
                out = dh.getOutputStream();
                if (out != null)
                    out.write(getText().getBytes());
            }
            catch (IOException e)
            {
                e.printStackTrace(System.err);
            }
            finally
            {
                if (out != null)
                {
                    try
                    {

                        out.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace(System.err);
                    }
                }
            }
        }
    }

}
