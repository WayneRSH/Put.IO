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
import java.util.List;
import java.util.Map;

import flexjson.ObjectBinder;
import flexjson.ObjectFactory;
import static api.Util.*;

/**
 * Represents and manages files tranfers to the put.io servers.
 *
 * @author Mark Manes
 *
 */
public class Transfer implements ObjectFactory
{

    private String status;
    private String percentDone;
    private String id;
    private String name;

    /**
     * Returns a list of all current transfers
     */
    public List<Transfer> list() throws Exception
    {
        Request req = new Request( "transfers?method=list" );
        return bindArray( getResults( req ), Transfer.class );
    }

    /**
     * Cancels this transfer.  {@link #id} must
     * be set.
     */
    public void cancel() throws Exception
    {
        Request req = new Request( "transfers?method=cancel" );

        Map<String, Object> params = req.getParams();
        params.put( "id", id );

        getResults( req );
    }

    /**
     * Adds the URLs as transfers on the server.  These URLs should have
     * been analyzed first through {@link URL#analyze(List)}.
     */
    public List<Transfer> add( List<URL> urls ) throws Exception
    {
        Request req = new Request( "transfers?method=add" );

        Map<String, Object> params = req.getParams();
        String[] sers = new String[urls.size()];

        for( int i = 0; i < sers.length; i++ )
            sers[i] = urls.get( i ).getUrl();

        params.put( "links", sers );

        return bindArray( getResults( req ), Transfer.class );
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object instantiate( ObjectBinder context, Object value, Type targetType, Class targetClass )
    {
        if( value instanceof Map && targetClass == Transfer.class )
        {
            Map<String, ?> map = (Map<String, ?>) value;
            Transfer t = new Transfer();
            t.status = stringVal( map.get( "status" ) );
            t.percentDone = stringVal( map.get( "percent_done" ) );
            t.id = stringVal( map.get( "id" ) );
            t.name = stringVal( map.get( "name" ) );
            return t;
        }
        throw context.cannotConvertValueToTargetType( value, targetClass );
    }

    @Override
    public String toString()
    {
        return "ID: " + id + " Name: " + name;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus( String status )
    {
        this.status = status;
    }

    public String getPercentDone()
    {
        return percentDone;
    }

    public void setPercentDone( String percentDone )
    {
        this.percentDone = percentDone;
    }

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

}
