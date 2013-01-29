package communication;

import api.Requestor;

public class RequestorHolder {
    private static Requestor REQUESTOR = Requestor.instance();

    /**
     * @return the REQUESTOR
     */
    public static Requestor getRequestor() {
        return REQUESTOR;
    }
}