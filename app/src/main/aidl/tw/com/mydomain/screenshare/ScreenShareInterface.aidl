// ScreenShareInterface.aidl
package tw.com.mydomain.screenshare;

interface ScreenShareInterface {
    /**
    * To test whether the interface binding successfully
    * @return (boolean) always true
    */
    boolean isBinding();
    /**
    * To create a sp session and send it to cloud server and start screen capture
    * @param (String) User's email for checking authentication
    * @param (String) Room ID
    * @param (String) Room Password
    */
    void startScreenShare(String email, String roomId, String roomPassword);
    /**
    * To stop screen capture
    */
    void stopScreenCapture();
}
