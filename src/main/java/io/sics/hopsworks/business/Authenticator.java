package io.sics.hopsworks.business;

/**
 *
 * @author AMore
 */
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.security.GeneralSecurityException;
import javax.security.auth.login.LoginException;

public final class Authenticator {

    private static Authenticator authenticator = null;

    // A user storage which stores <username, password>
    private final Map<String, String> usersStorage = new HashMap();

    // An authentication token storage which stores <auth_token, username>.
    private final Map<String, String> authorizationTokensStorage = new HashMap();
    
    //project users storage which stores <userProject, role>
    private final Map<String, String> projectUsersRoleStorage = new HashMap();

    private Authenticator() {
        // The usersStorage pretty much represents a user table in the database
        usersStorage.put("username1", "password1");
        usersStorage.put("username2", "password2");
        usersStorage.put("username3", "password3");
        
        projectUsersRoleStorage.put("username1Project1", "Master");
        projectUsersRoleStorage.put("username2Project1", "Guest");
        projectUsersRoleStorage.put("username2Project3", "Master");
        projectUsersRoleStorage.put("username3Project3", "Master");
        projectUsersRoleStorage.put("username1Project3", "Guest");
        projectUsersRoleStorage.put("username3Project2", "Master");
    }

    public static Authenticator getInstance() {
        if (authenticator == null) {
            authenticator = new Authenticator();
        }

        return authenticator;
    }

    public String login(String username, String password) throws LoginException {

        if (usersStorage.containsKey(username)) {
            String passwordMatch = usersStorage.get(username);

            if (passwordMatch.equals(password)) {

                /**
                 * Once all params are matched, the authToken will be generated
                 * and will be stored in the authorizationTokensStorage. The
                 * authToken will be needed for every REST API invocation and is
                 * only valid within the login session
                 */
                String authToken = UUID.randomUUID().toString();
                authorizationTokensStorage.put(authToken, username);

                return authToken;
            }
        }

        throw new LoginException("Don't Come Here Again!");
    }

    /**
     * The method that pre-validates if the client which invokes the REST API is
     * from a authorized and authenticated source.
     *
     * @param authToken The authorization token generated after login
     * @return TRUE for acceptance and FALSE for denied.
     */
    public boolean isAuthTokenValid(String authToken) {

        if (authorizationTokensStorage.containsKey(authToken)) {
            String usernameMatch2 = authorizationTokensStorage.get(authToken);

            return true;
        }

        return false;
    }

    public void logout(String authToken) throws GeneralSecurityException {

        if (authorizationTokensStorage.containsKey(authToken)) {
            String usernameMatch2 = authorizationTokensStorage.get(authToken);

            /**
             * When a client logs out, the authentication token will be remove
             * and will be made invalid.
             */
            authorizationTokensStorage.remove(authToken);
            return;
        }

        throw new GeneralSecurityException("Invalid authorization token match.");
    }

    public String getUserRoleForProject(String project, String user) throws GeneralSecurityException{
        if (projectUsersRoleStorage.containsKey(user + "" + project)) {
            return projectUsersRoleStorage.get(user + "" + project);
        }
        throw new GeneralSecurityException("Not a participant in this project"); 
    }
}
