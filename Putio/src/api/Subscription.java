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
import java.util.Date;
import java.util.List;
import java.util.Map;

import flexjson.ObjectBinder;
import flexjson.ObjectFactory;

/**
 * Represents and manages RSS subscriptions on the server.
 *
 * @author Mark Manes
 *
 */
public class Subscription implements ObjectFactory
{
    private Integer id;
    private String url;
    private String name;
    private String[] doFilters;
    private String[] dontFilters;
    private Integer parentFolderId;
    private Date lastUpdateTime;
    private Date nextUpdateTime;
    private Boolean paused;

    private void update( Subscription sub )
    {
        this.doFilters = sub.doFilters;
        this.dontFilters = sub.dontFilters;
        this.id = sub.id;
        this.lastUpdateTime = sub.lastUpdateTime;
        this.name = sub.name;
        this.nextUpdateTime = sub.nextUpdateTime;
        this.parentFolderId = sub.parentFolderId;
        this.paused = sub.paused;
        this.url = sub.url;
    }

    /**
     * Retrieves a List of subscriptions from the server
     */
    public List<Subscription> list() throws Exception
    {
        Request req = new Request( "subscriptions?method=list" );
        return bindArray( getResults( req ), Subscription.class );
    }

    /**
     * Creates a subscription on the server with the values stored on this object.
     */
    public void create() throws Exception
    {
        Request req = new Request( "subscriptions?method=create" );
        Map<String, Object> params = req.getParams();
        params.put( "title", name );
        params.put( "url", url );
        params.put( "do_filters", doFilters );
        params.put( "dont_filters", dontFilters );
        params.put( "parent_folder_id", parentFolderId );
        update( bind( getResults( req ).get( 0 ), Subscription.class ) );
    }

    /**
     * Modifies server subscription with new values on this object
     */
    public void edit() throws Exception
    {
        Request req = new Request( "subscriptions?method=edit" );
        Map<String, Object> params = req.getParams();
        params.put( "id", id );
        params.put( "title", name );
        params.put( "url", url );
        params.put( "do_filters", join( doFilters, "," ) );
        params.put( "dont_filters", join( dontFilters, "," ) );
        params.put( "parent_folder_id", parentFolderId );

        // Copy new values in, just in case
        update( bind( getResults( req ).get( 0 ), Subscription.class ) );
    }

    /**
     * Deletes this subscription from the server.
     */
    public void delete() throws Exception
    {
        Request req = new Request( "subscriptions?method=delete" );
        Map<String, Object> params = req.getParams();
        params.put( "id", id );
        getResults( req );
    }

    /**
     * Toggles the paused bit for this subscription on the server.
     */
    public void pause() throws Exception
    {
        Request req = new Request( "subscriptions?method=pause" );
        Map<String, Object> params = req.getParams();
        params.put( "id", id );
        update( bind( getResults( req ).get( 0 ), Subscription.class ) );
    }

    /**
     * Populates this object with info from the server.  {@link #id} must
     * be set.
     */
    public void info() throws Exception
    {
        if( id == null )
            throw new Exception( "Must set id before retrieving subscription info" );

        Request req = new Request( "subscriptions?method=info" );
        Map<String, Object> params = req.getParams();
        params.put( "id", id );
        update( bind( getResults( req ).get( 0 ), Subscription.class ) );
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object instantiate( ObjectBinder context, Object value, Type targetType, Class targetClass )
    {
        if( value instanceof Map && targetClass == Subscription.class )
        {
            Map<String, ?> map = (Map<String, ?>) value;
            Subscription s = new Subscription();
            s.id = intVal( map.get( "id" ) );
            s.name = stringVal( map.get( "name" ) );
            s.lastUpdateTime = dateVal( map.get( "last_update_time" ) );
            s.nextUpdateTime = dateVal( map.get( "next_update_time" ) );
            s.parentFolderId = intVal( map.get( "parent_folder_id" ) );
            s.url = stringVal( map.get( "url" ) );
            s.paused = boolVal( map.get( "paused" ) );
            s.doFilters = stringArrayVal( map.get( "do_filters" ), "," );
            s.dontFilters = stringArrayVal( map.get( "dont_filters" ), "," );
            return s;
        }
        throw context.cannotConvertValueToTargetType( value, targetClass );
    }

    @Override
    public String toString()
    {
        return "ID: " + id + " Name: " + name + " URL: " + url;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId( Integer id )
    {
        this.id = id;
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

    public String[] getDoFilters()
    {
        return doFilters;
    }

    public void setDoFilters( String[] doFilters )
    {
        this.doFilters = doFilters;
    }

    public String[] getDontFilters()
    {
        return dontFilters;
    }

    public void setDontFilters( String[] dontFilters )
    {
        this.dontFilters = dontFilters;
    }

    public Integer getParentFolderId()
    {
        return parentFolderId;
    }

    public void setParentFolderId( Integer parentFolderId )
    {
        this.parentFolderId = parentFolderId;
    }

    public Date getLastUpdateTime()
    {
        return lastUpdateTime;
    }

    public void setLastUpdateTime( Date lastUpdateTime )
    {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Date getNextUpdateTime()
    {
        return nextUpdateTime;
    }

    public void setNextUpdateTime( Date nextUpdateTime )
    {
        this.nextUpdateTime = nextUpdateTime;
    }

    public Boolean getPaused()
    {
        return paused;
    }

    public void setPaused( Boolean paused )
    {
        this.paused = paused;
    }
}
