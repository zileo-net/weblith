package io.weblith.core.multitenancy;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

@Provider
public class TenantResolverFilterDynamicFeature implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext configurable) {
        configurable.register(TenantResolverFilter.class);
        configurable.register(TenantCleanerFilter.class);
    }

}
