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

import static api.Util.*;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import flexjson.ObjectBinder;
import flexjson.ObjectFactory;

/**
 * URL extraction and analysis
 *
 * @author Mark Manes
 */
public class URL implements ObjectFactory
{
    private String url;
    private String name;
    private Long size;
    private String humanSize;
    private Long paidBandwidth;
    private String type;
    private String error;
    private String source;
    private Boolean needsPass;

    /**
     * Requests the server to analyze the list of URLs.
     * @return {@link URLList} representing the results
     */
    public URLList analyze( List<URL> urls ) throws Exception
    {
        Request req = new Request( "urls?method=analyze" );
        Map<String, Object> params = req.getParams();

        String[] sers = new String[urls.size()];

        for( int i = 0; i < sers.length; i++ )
            sers[i] = urls.get( i ).url;

        params.put( "links", sers );
        return bind( getResults( req ).get( 0 ), URLList.class );
    }

    /**
     * Extracts {@link URL} objects from text.
     */
    public List<URL> extractUrls( String text ) throws Exception
    {
        Request req = new Request( "urls?method=extracturls" );
        Map<String, Object> params = req.getParams();
        params.put( "txt", text );
        return bindArray( getResults( req ), URL.class );
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object instantiate( ObjectBinder context, Object value, Type targetType, Class targetClass )
    {
        if( value instanceof Map && targetClass == URL.class )
        {
            Map<String, ?> map = (Map<String, ?>) value;
            URL u = new URL();
            u.type = stringVal( map.get( "type" ) );
            u.url = stringVal( map.get( "url" ) );
            u.error = stringVal( map.get( "error" ) );
            u.source = stringVal( map.get( "source" ) );
            u.paidBandwidth = longVal( map.get( "paid_bw" ) );
            u.name = stringVal( map.get( "name" ) );
            u.needsPass = boolVal( map.get( "needs_pass" ) );
            u.humanSize = stringVal( map.get( "human_size" ) );
            u.size = longVal( map.get( "size" ) );
            return u;
        }
        throw context.cannotConvertValueToTargetType( value, targetClass );
    }

    @Override
    public String toString()
    {
        return url;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public Long getSize()
    {
        return size;
    }

    public void setSize( Long size )
    {
        this.size = size;
    }

    public String getHumanSize()
    {
        return humanSize;
    }

    public void setHumanSize( String humanSize )
    {
        this.humanSize = humanSize;
    }

    public Long getPaidBandwidth()
    {
        return paidBandwidth;
    }

    public void setPaidBandwidth( Long paidBandwidth )
    {
        this.paidBandwidth = paidBandwidth;
    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getError()
    {
        return error;
    }

    public void setError( String error )
    {
        this.error = error;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource( String source )
    {
        this.source = source;
    }

    public Boolean getNeedsPass()
    {
        return needsPass;
    }

    public void setNeedsPass( Boolean needsPass )
    {
        this.needsPass = needsPass;
    }

}
/*public enum DownloadHandler
{
    Torrent( 1, "torrent" ),
    Rapid( 2, "rapid" ),
    SingleUrl( 3, "single url" ),
    Update( 4, "upload" ),
    Multipart( 5, "multipart" );

    public final int id;
    public final String name;

    DownloadHandler( int id, String name )
    {
        this.id = id;
        this.name = name;
    }

    DownloadHandler byId( int id )
    {
        for( DownloadHandler t : DownloadHandler.values() )
        {
            if( t.id == id )
                return t;
        }
        return null;
    }

    DownloadHandler byName( String name )
    {
        for( DownloadHandler t : DownloadHandler.values() )
        {
            if( t.name.equalsIgnoreCase( name ) )
                return t;
        }
        return null;
    }
}

public enum DownloadType
{
    Paid( 1, "paid" ),
    Torrent( 2, "torrent" ),
    Url( 3, "url" ),
    PaidMultipart( 4, "paid multipart" );

    public final int id;
    public final String name;

    DownloadType( int id, String name )
    {
        this.id = id;
        this.name = name;
    }

    DownloadType byId( int id )
    {
        for( DownloadType t : DownloadType.values() )
        {
            if( t.id == id )
                return t;
        }
        return null;
    }

    DownloadType byName( String name )
    {
        for( DownloadType t : DownloadType.values() )
        {
            if( t.name.equalsIgnoreCase( name ) )
                return t;
        }
        return null;
    }
}*/