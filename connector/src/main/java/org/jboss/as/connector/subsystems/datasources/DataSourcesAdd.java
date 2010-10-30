/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.as.connector.subsystems.datasources;

import org.jboss.as.connector.ConnectorServices;
import org.jboss.as.connector.deployers.RaDeploymentActivator;
import org.jboss.as.connector.deployers.RaDeploymentChainSelector;
import org.jboss.as.model.AbstractSubsystemAdd;
import org.jboss.as.model.UpdateContext;
import org.jboss.as.model.UpdateResultHandler;
import org.jboss.jca.common.api.metadata.ds.DataSources;
import org.jboss.msc.service.BatchBuilder;
import org.jboss.msc.service.BatchServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;

/**
 * @author <a href="mailto:stefano.maestri@redhat.comdhat.com">Stefano
 *         Maestri</a>
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class DataSourcesAdd extends AbstractSubsystemAdd<DataSourcesSubsystemElement> {

    private static final long serialVersionUID = -874698675049495644L;

    private DataSources datasources;

    public DataSources getDatasources() {
        return datasources;
    }

    public void setDatasources(DataSources datasources) {
        this.datasources = datasources;
    }

    protected DataSourcesAdd() {
        super(Namespace.CURRENT.getUriString());
    }

    @Override
    protected <P> void applyUpdate(final UpdateContext updateContext, final UpdateResultHandler<? super Void, P> resultHandler,
            final P param) {
        final BatchBuilder builder = updateContext.getBatchBuilder();

        final DataSourcesService dsService = new DataSourcesService(datasources);
        BatchServiceBuilder<?> serviceBuilder = builder.addService(ConnectorServices.DATASOURCES_SERVICE,
                dsService);
        serviceBuilder.setInitialMode(Mode.ACTIVE);

        if (datasources == null)
            return;

        if (datasources.getDataSource().size() > 0 || datasources.getXaDataSource().size() > 0) {
            serviceBuilder = builder.addServiceValueIfNotExist(JDBCRARDeployService.NAME, new JDBCRARDeployService());
            serviceBuilder.addDependency(RaDeploymentActivator.RAR_DEPLOYMENT_CHAIN_SERVICE_NAME);
            serviceBuilder.addDependency(ConnectorServices.RESOURCEADAPTERS_SERVICE);
            serviceBuilder.addDependency(ConnectorServices.CONNECTOR_CONFIG_SERVICE);
            serviceBuilder.addDependency(ConnectorServices.IRONJACAMAR_MDR);
            serviceBuilder.setInitialMode(Mode.ACTIVE);
        }
    }

    protected DataSourcesSubsystemElement createSubsystemElement() {
        DataSourcesSubsystemElement element = new DataSourcesSubsystemElement();
        element.setDatasources(datasources);
        return element;
    }

}
