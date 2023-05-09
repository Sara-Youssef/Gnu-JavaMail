/*
 * StatusSource.java
 * Copyright (C) 1999 Chris Burdess <dog@gnu.org>
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

package gnu.mail.treeutil;

/**
 * An interface for defining that an object can pass status messages.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.0.2
 */
public interface StatusSource
{

	/**
	 * Adds a status listener to this source.
	 * @param l the listener
	 */
	void addStatusListener(StatusListener l);

	/**
	 * Removes a status listener from this source.
	 * @param l the listener
	 */
	void removeStatusListener(StatusListener l);

}
