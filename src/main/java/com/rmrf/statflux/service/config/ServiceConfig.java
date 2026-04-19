package com.rmrf.statflux.service.config;

import com.rmrf.statflux.common.ConfigLoader;
import com.rmrf.statflux.integration.HostingApiFactory;
import com.rmrf.statflux.repository.config.RepositoryConfig;
import com.rmrf.statflux.repository.transaction.TransactionalProxy;
import com.rmrf.statflux.service.ServiceLayer;
import com.rmrf.statflux.service.ServiceLayerImpl;
import java.util.Properties;

public class ServiceConfig {

    private final RepositoryConfig repositoryConfig = new RepositoryConfig();

    public ServiceLayer serviceLayer(HostingApiFactory hostingApiFactory) {
        Properties props = ConfigLoader.loadProperties("application.properties");
        String refreshDelayMs = props.getProperty("service.refreshDelayMs", "100");
        ServiceLayerImpl impl = new ServiceLayerImpl(
            repositoryConfig.linkRepository(),
            hostingApiFactory,
            Long.parseLong(refreshDelayMs));
        return TransactionalProxy.create(impl,
            repositoryConfig.transactionManager(repositoryConfig.pooledDataSource())
        );
    }

}
