package com.rmrf.statflux.service.config;

import com.rmrf.statflux.common.ConfigLoader;
import com.rmrf.statflux.integration.VideoProviderFactory;
import com.rmrf.statflux.repository.config.RepositoryConfig;
import com.rmrf.statflux.repository.transaction.TransactionalProxy;
import com.rmrf.statflux.service.ServiceLayer;
import com.rmrf.statflux.service.ServiceLayerImpl;
import com.rmrf.statflux.service.config.impl.ServiceParamsConfigFromEnv;

public class ServiceConfig {
    private final RepositoryConfig repositoryConfig = new RepositoryConfig();
    private final ServiceParamsConfig serviceParamsConfig = new ServiceParamsConfigFromEnv();

    public ServiceLayer serviceLayer(VideoProviderFactory hostingApiFactory) {
        var txManager = repositoryConfig.transactionManager(repositoryConfig.pooledDataSource());
        ServiceLayerImpl impl = new ServiceLayerImpl(
            repositoryConfig.linkRepository(),
            repositoryConfig.paginationStateRepository(),
            hostingApiFactory,
            serviceParamsConfig.getRefreshDelayMs(),
            serviceParamsConfig.getPageSize(),
            txManager
        );
        return TransactionalProxy.create(impl, txManager);
    }

}
