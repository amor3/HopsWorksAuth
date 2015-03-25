package io.sics.hopsworks.service.auth;

/**
 *
 * @author AMore
 */
import io.sics.hopsworks.business.Authenticator;
import java.io.IOException;
import java.util.logging.Logger;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
 
@Provider
@PreMatching
public class RequestFilter implements ContainerRequestFilter {
 
    private final static Logger log = Logger.getLogger( RequestFilter.class.getName() );
 
    @Override
    public void filter( ContainerRequestContext requestCtx ) throws IOException {
 
        String path = requestCtx.getUriInfo().getPath();
        log.info( "Filtering request path: " + path );
 
        // IMPORTANT!!! First, Acknowledge any pre-flight test from browsers for this case before validating the headers (CORS stuff)
        if ( requestCtx.getRequest().getMethod().equals( "OPTIONS" ) ) {
            requestCtx.abortWith( Response.status( Response.Status.OK ).build() );
 
            return;
        }
 
        // Then check is the service key exists and is valid.
        Authenticator demoAuthenticator = Authenticator.getInstance();
 
 
        // For any pther methods besides login, the authToken must be verified
        if ( !path.startsWith( "/auth/login/" ) ) {
            String authToken = requestCtx.getHeaderString( HTTPHeaders.AUTH_TOKEN );
 
            // if it isn't valid, just kick them out.
            if ( !demoAuthenticator.isAuthTokenValid( authToken ) ) {
                requestCtx.abortWith( Response.status( Response.Status.UNAUTHORIZED ).build() );
            }
        }
    }
}