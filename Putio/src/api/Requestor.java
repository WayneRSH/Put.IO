/*
 *  Copyright (c) 2010 Mark Manes
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
//import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

/**
 * Performs requests against the put.io web service.  This class can be used
 * across multiple threads provided each thread invokes {@link #setThreadCredentials(String)}.
 *
 * @author Mark Manes
 *
 */
public class Requestor
{
    private final ThreadLocal<String> oauth_token = new ThreadLocal<String>();
    private final JSONSerializer json;
    private static Requestor instance;
    private static String baseAPI = "https://api.put.io/v2/";

    /**
     * Returns the singleton instance.
     */
    public static Requestor instance()
    {
        if( instance == null )
        {
            synchronized( Requestor.class )
            {
                if( instance == null )
                    instance = new Requestor();
            }
        }
        return instance;
    }

    private Requestor()
    {
        json = new JSONSerializer();
        json.exclude( "*.class" );
    }

    /**
     * Sets the put.io API credentials to use for this thread.  Each API must invoke
     * this method before invoking any requests.
     *
     * @param oauth_token oauth token.
     */
    public void setThreadCredentials( String oauth_token )
    {
        this.oauth_token.set( oauth_token );
    }

    public static String getBaseAPI() {
        return baseAPI;
    }

    /**
     * Requests the Item from the server.
     *
     * @param i Item to retrieve
     * @return InputStream
     */
    public InputStream getItemStream( Item i ) throws Exception
    {
        if( i.isDir() )
            throw new Exception( "Item must be a file" );

        String token = new User().acctoken();

        URL url = new URL( i.getStreamUrl() + "/atk/" + token );
        URLConnection conn = url.openConnection();
        return conn.getInputStream();
    }

    /**
     * Performs a request against the put.io server.
     *
     * @param req Request object to send
     * @return {@link Response} returned from server
     */
    Response makeRequest( Request req ) throws Exception
    {
        if( oauth_token.get() == null )
            throw new Exception( "Credentials not set on this thread" );

        String uri = baseAPI + req.getPath() + "?oauth_token=" + oauth_token.get();
        if (!req.getParams().isEmpty()) {
            Iterator<String> it = req.getParams().keySet().iterator();
            while (it.hasNext()) {
                String cle = it.next();
                uri += "&" + cle + "=" + req.getParams().get(cle);
            }
        }
        //System.out.println(uri);
        URL url = new URL( uri );


        URLConnection conn = url.openConnection();

        conn.setRequestProperty( "Accept", "application/json" );
        conn.setDoOutput( true );
        conn.setReadTimeout(15000);

        String json = new RequestSerializer( req ).toString();

        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader rd = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
            String line;

            while( (line = rd.readLine()) != null )
                sb.append( line );

            rd.close();
        }
        catch(SocketTimeoutException ex) {
            throw new TimeoutException();
        }

        json = sb.toString();
        //System.out.println( json );

        Response response = new Response();

        return new JSONDeserializer<Response>().use( (String) null, response ).deserialize( json );
    }

    /**
     * Simple encapsulation of the API credentials to serialize as a JSON object.
     */
    public class RequestSerializer
    {
        private String oauth_token;
        private Map<String, Object> params;

        RequestSerializer( Request req )
        {
            this.oauth_token = Requestor.this.oauth_token.get();
            this.params = req.getParams();
        }

        @Override
        public String toString()
        {
            return json.deepSerialize( this );
        }

        public String getOauth_token()
        {
            return oauth_token;
        }

        public Map<String, Object> getParams()
        {
            return params;
        }

    }

}
