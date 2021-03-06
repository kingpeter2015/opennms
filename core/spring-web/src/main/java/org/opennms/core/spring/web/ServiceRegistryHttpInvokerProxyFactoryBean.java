/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.core.spring.web;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.support.RemoteInvocation;

/**
 */
public class ServiceRegistryHttpInvokerProxyFactoryBean extends HttpInvokerProxyFactoryBean {

	public static final String ATTRIBUTE_INTERFACE_NAME = ServiceRegistryHttpInvokerProxyFactoryBean.class.getName() + ".interface";

	@Override
	public RemoteInvocation createRemoteInvocation(MethodInvocation methodInvocation) {
		RemoteInvocation retval = super.createRemoteInvocation(methodInvocation);

		// Add the interface that is being used to access this service as an invocation attibute
		retval.addAttribute(ATTRIBUTE_INTERFACE_NAME, this.getServiceInterface().getName());

		return retval;
	}
}
