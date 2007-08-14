//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.capsd.plugins;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.AbstractPlugin;
import org.opennms.netmgt.ping.Pinger;
import org.opennms.netmgt.ping.Reply;
import org.opennms.netmgt.ping.ReplyReceiver;
import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.protocols.icmp.ICMPEchoPacket;
import org.opennms.protocols.icmp.IcmpSocket;

/**
 * This class provides Capsd with the ability to check for ICMP support on new
 * interfaces as them are passed into the system. In order to minimize the
 * number of sockets and threads, this class creates a daemon thread to handle
 * all responses and a single socket for sending echo request to various hosts.
 * 
 * @author <A HREF="mailto:weave@oculan.com">Weave </a>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
public final class IcmpPlugin extends AbstractPlugin {
    /**
     * The name of the protocol that is supported by this plugin
     */
    private static final String PROTOCOL_NAME = "ICMP";

    /**
     * Constructs a new monitor.
     */
    public IcmpPlugin() throws IOException {
    }

    /**
     * Returns the name of the protocol that this plugin checks on the target
     * system for support.
     * 
     * @return The protocol name for this plugin.
     */
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    /**
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * 
     * @param address
     *            The address to check for support.
     * 
     * @return True if the protocol is supported by the address.
     */
    public boolean isProtocolSupported(InetAddress address) {
    	Pinger pinger;
		try {
			pinger = new Pinger();
	    	Long retval = pinger.ping(address);
	    	if (retval != null) {
	    		return true;
	    	}
		} catch (IOException e) {
	        Category log = ThreadCategory.getInstance(this.getClass());
			log.warn("Pinger failed to ping " + address, e);
		}
		return false;
    }

    /**
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * The qualifier map passed to the method is used by the plugin to return
     * additional information by key-name. These key-value pairs can be added to
     * service events if needed.
     * 
     * @param address
     *            The address to check for support.
     * @param qualifiers
     *            The map where qualification are set by the plugin.
     * 
     * @return True if the protocol is supported by the address.
     */
    public boolean isProtocolSupported(InetAddress address, Map qualifiers) {
    	Pinger pinger;
    	int retries;
    	long timeout;

    	try {
    		pinger = new Pinger();
    		if (qualifiers != null) {
    			retries = ParameterMap.getKeyedInteger(qualifiers, "retry", pinger.getRetries());
    			timeout = ParameterMap.getKeyedLong(qualifiers, "timeout", pinger.getTimeout());
    		} else {
    			retries = pinger.getRetries();
    			timeout = pinger.getTimeout();
    		}
    		Long retval = pinger.ping(address, timeout, retries);
    		if (retval != null) {
    			return true;
    		}
    	} catch (IOException e) {
	        Category log = ThreadCategory.getInstance(this.getClass());
			log.warn("Pinger failed to ping " + address, e);
    	}
    	
    	return false;
    }
}
