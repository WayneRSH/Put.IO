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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import flexjson.ObjectBinder;
import flexjson.ObjectFactory;
import flexjson.Path;
import flexjson.factories.ClassLocatorObjectFactory;
import flexjson.locators.StaticClassLocator;

/**
 * Provides helper methods for converting result objects and fields.
 *
 * @author Mark Manes
 *
 */
class Util
{
    private static final DateFormat dateFormat;

    static
    {
        dateFormat = new SimpleDateFormat( "y-m-d H:m:s z" );
    }

    private Util()
    {
    }

    /**
     * Performs a request and returns the results field.
     *
     * @param req Request to perform
     * @return List of raw result maps.
     */
    public static List<Map<String, ?>> getResults( Request req ) throws Exception
    {
        Response res = Requestor.instance().makeRequest( req );

        if( res.isError() )
            throw new Exception( res.getErrorMsg() );

        return res.getResults();
    }

    /**
     * Binds a Map to an object.
     *
     * @param <T>
     * @param map Raw map returned from server
     * @param klass Class of object to be returned
     * @return a new instance of klass populated from map
     *
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> T bind( Map map, Class<T> klass )
    {
        try
        {
            ObjectBinder ob = new ObjectBinder();

            if( ObjectFactory.class.isAssignableFrom( klass ) )
            {
                ob.use( klass, (ObjectFactory) klass.newInstance() );
                return (T) ob.bind( map, klass );
            }
            else
            {
                ob.use( Path.parse( null ), new ClassLocatorObjectFactory( new StaticClassLocator( klass ) ) );
                return (T) ob.bindIntoObject( map, klass.newInstance(), klass );
            }
        }
        catch( Exception ex )
        {
            throw new RuntimeException( "Could not bind map into class '" + klass.getName() + "'", ex );
        }
    }

    /**
     * Creates a new List of objects of type klass, populated from results map.
     *
     * @param <T> Type of instances of objects to create
     * @param results Result map from server
     * @param klass Class of instances to create
     * @return new List of objects of type klass, populated from results
     *
     */
    public static <T> List<T> bindArray( List<Map<String, ?>> results, Class<T> klass )
    {
        List<T> list = new ArrayList<T>( results.size() );

        for( Map<String, ?> m : results )
            list.add( bind( m, klass ) );

        return list;
    }

    public static Long longVal( Object o )
    {
        if( o == null )
            return 0L;
        if( o instanceof Number )
            return ((Number) o).longValue();
        if( o instanceof String )
            return Long.parseLong( (String) o );
        return 0L;
    }

    public static Integer intVal( Object o )
    {
        if( o == null )
            return 0;
        if( o instanceof Number )
            return ((Number) o).intValue();
        if( o instanceof String )
            return Integer.parseInt( (String) o );
        return 0;
    }

    public static String stringVal( Object o )
    {
        if( o == null )
            return null;

        return o.toString();
    }

    public static String[] stringArrayVal( Object o, String splitter )
    {
        String s = stringVal( o );
        if( s != null )
            return s.split( splitter );
        else
            return null;
    }

    public static Boolean boolVal( Object o )
    {
        if( o == null )
            return false;

        if( o instanceof String && o.equals( "1" ) )
            return true;

        return Boolean.parseBoolean( o.toString() );
    }

    public static Date dateVal( Object o )
    {
        if( o == null )
            return null;

        try
        {
            return dateFormat.parse( o.toString() + " UTC" );
        }
        catch( ParseException e )
        {
            return null;
        }
    }

    public static String join( String[] arr, String joiner )
    {
        StringBuffer sb = new StringBuffer();
        for( int i = 0; i < arr.length; i++ )
        {
            if( i != 0 )
                sb.append( joiner );
            sb.append( arr[i] );
        }
        return sb.toString();
    }

}
