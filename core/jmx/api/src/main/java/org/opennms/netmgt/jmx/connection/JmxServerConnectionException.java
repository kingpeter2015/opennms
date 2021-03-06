/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.jmx.connection;

import java.io.IOException;

/**
 * Is used to indicate that a connection to a JMX Server (MBeanServer) could not be established.
 * The reason may be that the server is not reachable, or credentials are invalid or
 * there is no {@link org.opennms.netmgt.jmx.connection.JmxServerConnector} registered
 * for the {@link org.opennms.netmgt.jmx.connection.JmxConnectors}.
 * <p/>
 * The exception's <code>errorMessage</code> should provide details about the concrete error.
 */
public class JmxServerConnectionException extends Exception {
    private static final long serialVersionUID = 1L;

    public JmxServerConnectionException(final String errorMessage) {
        super(errorMessage);
    }

    public JmxServerConnectionException(final IOException ioException) {
        super(ioException);
    }

    public JmxServerConnectionException(String errorMessage, Exception exception) {
        super(errorMessage, exception);
    }
}
