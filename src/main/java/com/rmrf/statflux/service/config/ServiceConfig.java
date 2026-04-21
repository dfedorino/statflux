package com.rmrf.statflux.service.config;

import com.rmrf.statflux.common.ConfigLoader;
import com.rmrf.statflux.integration.VideoProviderFactory;
import com.rmrf.statflux.repository.config.RepositoryConfig;
import com.rmrf.statflux.repository.transaction.TransactionalProxy;
import com.rmrf.statflux.service.ServiceLayer;
import com.rmrf.statflux.service.ServiceLayerImpl;
import java.util.Properties;

public class ServiceConfig {

    private final RepositoryConfig repositoryConfig = new RepositoryConfig();

    public ServiceLayer serviceLayer(VideoProviderFactory hostingApiFactory) {
        Properties props = ConfigLoader.loadProperties("application.properties");
        String refreshDelayMs = props.getProperty("service.refreshDelayMs", "100");
        String pageSize = props.getProperty("service.pageSize", "5");
        var txManager = repositoryConfig.transactionManager(repositoryConfig.pooledDataSource());
        ServiceLayerImpl impl = new ServiceLayerImpl(
            repositoryConfig.linkRepository(),
            repositoryConfig.paginationStateRepository(),
            hostingApiFactory,
            Long.parseLong(refreshDelayMs),
            Integer.parseInt(pageSize),
            txManager);
        return TransactionalProxy.create(impl, txManager);
    }

}
