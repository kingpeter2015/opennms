/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.hibernate;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.model.outage.OutageSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate4.HibernateCallback;

public class OutageDaoHibernate extends AbstractDaoHibernate<OnmsOutage, Integer> implements OutageDao {

    private static final Logger LOG = LoggerFactory.getLogger(OutageDaoHibernate.class);

    /**
     * <p>Constructor for OutageDaoHibernate.</p>
     */
    public OutageDaoHibernate() {
        super(OnmsOutage.class);
    }

    /**
     * <p>currentOutageCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer currentOutageCount() {
        return queryInt("select count(*) from OnmsOutage as o where o.ifRegainedService is null");
    }

    /**
     * <p>currentOutages</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<OnmsOutage> currentOutages() {
        return find("from OnmsOutage as o where o.ifRegainedService is null");
    }

    @Override
    public OnmsOutage currentOutageForService(OnmsMonitoredService service) {
        // TODO: I was seeing multiple open outages for a service on my system so we
        // can't use findUnique() until we fix the data integrity issues
        Collection<OnmsOutage> outages = find("from OnmsOutage as o where o.monitoredService = ? and o.ifRegainedService is null order by o.ifLostService desc", service);
        if (outages != null && outages.size() > 0) {
            if (outages.size() > 1) {
                LOG.warn("More than one open outage found for service: " + service.toString());
            }
            return outages.iterator().next();
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsOutage> findAll(final Integer offset, final Integer limit) {
        return (Collection<OnmsOutage>)getHibernateTemplate().execute(new HibernateCallback<Collection<OnmsOutage>>() {

            @SuppressWarnings("unchecked")
            @Override
            public Collection<OnmsOutage> doInHibernate(final Session session) throws HibernateException {
                return session.createCriteria(OnmsOutage.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
            }

        });
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsOutage> matchingCurrentOutages(final ServiceSelector selector) {
        final Set<InetAddress> matchingAddrs = new HashSet<InetAddress>(FilterDaoFactory.getInstance().getIPAddressList(selector.getFilterRule()));
        final Set<String> matchingSvcs = new HashSet<String>(selector.getServiceNames());

        final List<OnmsOutage> matchingOutages = new LinkedList<OnmsOutage>();
        final Collection<OnmsOutage> outages = currentOutages();
        for (final OnmsOutage outage : outages) {
            final OnmsMonitoredService svc = outage.getMonitoredService();
            if ((matchingSvcs.contains(svc.getServiceName()) || matchingSvcs.isEmpty()) && matchingAddrs.contains(svc.getIpAddress())) {
                matchingOutages.add(outage);
            }

        }

        return matchingOutages;
    }

    /** {@inheritDoc} */
    @Override
    public int countOutagesByNode() {
        return getNodeOutageSummaries(0).size();
    }

    // final int nodeId, final String nodeLabel, final Date timeDown, final Date timeUp, final Date timeNow
    /** {@inheritDoc} */
    @Override
    public List<OutageSummary> getNodeOutageSummaries(final int rows) {
        final List<OutageSummary> outages = findObjects(
            OutageSummary.class,
            "SELECT DISTINCT new org.opennms.netmgt.model.outage.OutageSummary(node.id, node.label, max(outage.ifLostService)) " +
            "FROM OnmsOutage AS outage " +
            "LEFT JOIN outage.monitoredService AS monitoredService " +
            "LEFT JOIN monitoredService.ipInterface AS ipInterface " + 
            "LEFT JOIN ipInterface.node AS node " +
            "WHERE outage.ifRegainedService IS NULL " +
            "GROUP BY node.id, node.label " +
            "ORDER BY max(outage.ifLostService) DESC, node.label ASC, node.id ASC"
        );
        if (rows == 0 || outages.size() < rows) {
            return outages;
        } else {
            return outages.subList(0, rows);
        }
    }

}
