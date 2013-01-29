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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import flexjson.ObjectBinder;
import flexjson.ObjectFactory;

/**
 * Encapsulates the raw response from a request.
 *
 * @author Mark Manes
 *
 */
public class Response implements ObjectFactory
{
    private int total;
    private List<Map<String, ?>> results;
    private Boolean error;
    private String errorMsg;
    private String errorDesc;
    private String status;

    public int getTotal()
    {
        return total;
    }

    public void setTotal( int total )
    {
        this.total = total;
    }

    public List<Map<String, ?>> getResults()
    {
        return results;
    }

    public void setResults( List<Map<String, ?>> results )
    {
        this.results = results;
    }

    public Boolean isError()
    {
        return error;
    }

    public void setError( Boolean error )
    {
        this.error = error;
    }

    public String getErrorDesc()
    {
        return errorDesc;
    }

    public void setErrorDesc( String errorDesc )
    {
        this.errorDesc = errorDesc;
    }
    
    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setErrorMsg( String errorMsg )
    {
        this.errorMsg = errorMsg;
    }
    
    public String getStatus()
    {
        return status;
    }

    public void setStatus( String status )
    {
        this.status = status;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object instantiate( ObjectBinder context, Object value, Type targetType, Class targetClass )
    {
        if( value instanceof Map && (targetClass == Response.class || targetClass == null) )
        {
            Map<String, ?> map = (Map<String, ?>) value;
            Response r = new Response();
            r.errorMsg = (String) map.get( "error" );
            r.errorDesc = (String) map.get( "error_description" );
            r.status = (String) map.get( "status" );
            if (!r.status.equals("OK")) {
            	r.error = true;
            	System.out.println("Err : " + r.errorMsg + r.errorDesc);
            }
            else
            	r.error = false;

            // User info
            if (map.containsKey( "info" )) {
	            Object oResponse = map.get( "info" );
	            if( oResponse instanceof Map )
	            {
	            	List<Map<String, ?>> results = new ArrayList<Map<String, ?>>( 1 );
                    results.add( (Map<String, ?>) map.get( "info" ) );
                    r.results = results;
	            }
	            else {
	            	r.error = true;
	            	r.errorMsg = "info_notfound";
	            }
            }
            // File info
            else if (map.containsKey( "files" )) {
	            Object oResponse = map.get( "files" );
	            if( oResponse instanceof List ) {
                    r.results = (List<Map<String, ?>>) oResponse;
	            }
	            else {
	            	r.error = true;
	            	r.errorMsg = "files_notfound";
	            }
            }

            return r;
        }
        return null;
    }

}
