package io.weblith.core.results;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import org.jboss.logging.Logger;
import org.jboss.resteasy.util.DateUtil;
import org.jboss.resteasy.util.DateUtil.DateParseException;

import io.weblith.core.config.HttpCacheConfig;
import io.weblith.core.config.WeblithConfig;
import io.weblith.core.results.Result.AutomaticCachingPolicy;
/**
 * Useful info taken from Heroku documentation : <br>
 * https://devcenter.heroku.com/articles/increasing-application-performance-with-http-cache-headers
 */
@ApplicationScoped
public class HttpCacheHelper {

    private static final Logger LOGGER = Logger.getLogger(HttpCacheHelper.class);

    private final HttpCacheConfig cacheConfig;

    @Inject
    public HttpCacheHelper(WeblithConfig weblithConfiguration) {
        this.cacheConfig = weblithConfiguration.httpCache;
    }

    public void setCachingPolicy(ContainerRequestContext requestContext, AbstractResult<?> result) {
        if (Result.AutomaticCachingPolicy.class.isAssignableFrom(result.getClass())) {

            AutomaticCachingPolicy autoCachingResult = (AutomaticCachingPolicy) result;
            if (autoCachingResult.isHttpCacheEnabled()) {

                if (!isModified(requestContext, autoCachingResult.getLastModified())) {
                    result.status(Status.NOT_MODIFIED);
                }

                setCacheControl(result, autoCachingResult.getLastModified());
            }

        } else if (result.getContentType() != null && !result.getHeaders().containsKey(HttpHeaders.CACHE_CONTROL)) {
            // By default, do not cache any result that don't have any cache-control statement
            result.doNotCacheContent();
        }
    }

    protected boolean isModified(ContainerRequestContext request, Long lastModified) {
        return isModified(request, lastModified, buildETag(lastModified));
    }

    protected boolean isModified(ContainerRequestContext request, Long lastModified, String etag) {
        if (lastModified == null) {
            return true;
        }

        final String ifNoneMatch = request.getHeaderString(HttpHeaders.IF_NONE_MATCH);
        if (ifNoneMatch != null) {
            return !ifNoneMatch.equals(etag);
        }

        final String ifModifiedSince = request.getHeaderString(HttpHeaders.IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            try {
                return DateUtil.parseDate(ifModifiedSince).getTime() < lastModified;
            } catch (DateParseException ex) {
                LOGGER.warnv("Cannot parse If-Modified-Since date '{0}'", ifModifiedSince);
            }
        }

        return true;

    }

    public void setCacheControl(AbstractResult<?> result, Long lastModified) {
        setCacheControl(result, lastModified, buildETag(lastModified));
    }

    public void setCacheControl(AbstractResult<?> result, Long lastModified, String etag) {

        if (!cacheConfig.enabled || cacheConfig.cacheControl.isZero()) {
            result.addHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store");
        } else {
            result.addHeader(HttpHeaders.CACHE_CONTROL, "max-age=" + cacheConfig.cacheControl.getSeconds());
            result.addHeader(HttpHeaders.LAST_MODIFIED, new Date(lastModified));
            result.addHeader(HttpHeaders.ETAG, etag);
        }

    }

    protected String buildETag(Long lastModified) {
        return String.format("\"%d\"", lastModified);
    }

}