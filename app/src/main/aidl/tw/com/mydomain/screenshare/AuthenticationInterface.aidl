// AuthenticationInterface.aidl
package tw.com.mydomain.screenshare;

interface AuthenticationInterface {
    /**
    * To test whether the interface binding successfully
    * @return (boolean) always true
    */
    boolean isBinding();

    /**
    * @return (boolean) whether the user is login
    */
    boolean isLogin();

    /**
    * @return (String) the room id
    */
    String getRoomId();

    /**
    * @return (String) user's name
    */
    String getName();

    /**
    * @return (String) user's name
    */
    String getEmail();

    /**
    * @param (String) User's email
    * @param (String) User's password
    * @return (String) whether login succeed
    */
    boolean login(String email, String password);

    /**
    * @param (String) User's name
    * @param (String) User's email
    * @param (String) User's password
    * @param (String) Confirmed password
    * @return (String) whether register succeed
    */
    boolean register(String name, String email, String password, String confirmPassword);

    /**
    * @param (String) User's email
    * @return (String) whether logout succeed
    */
    boolean logout(String email);
}
