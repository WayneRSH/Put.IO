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
 * Represents a user on the put.io server.
 *
 * @author Mark Manes
 *
 */
public class User implements ObjectFactory
{
    private String name;
    private String mail;
    private Integer friendsCount;
    private Long bwAvailLastMonth;
    private Long sharedSpace;
    private Long sharedItems;
    private Long diskQuota;
    private Long diskQuotaAvailable;
    private Long bwQuota;

    /**
     * @return A new User object representing the currently logged on user.
     */
    public User info() throws Exception
    {
        Request req = new Request( "account/info" );
        List<Map<String, ?>> results = getResults( req );

        if( results.isEmpty() )
            return null;

        return bind( results.get( 0 ), User.class );
    }

    /**
     * Retrieves friends of the currently logged on user.
     * @return List of User objects
     */
    public List<User> friends() throws Exception
    {
        Request req = new Request( "account/info" );
        return bindArray( getResults( req ), User.class );
    }

    /**
     * Retrieves a new account token from the server.
     * @return String token to be used for streaming
     */
    public String acctoken() throws Exception
    {
        Request req = new Request( "user?method=acctoken" );
        List<Map<String, ?>> results = getResults( req );

        if( results.isEmpty() )
            return null;

        return (String) results.get( 0 ).get( "token" );

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object instantiate( ObjectBinder context, Object value, Type targetType, Class targetClass )
    {
        if( value instanceof Map && targetClass == User.class )
        {
            Map<String, ?> map = (Map<String, ?>) value;
            User u = new User();
            u.name = stringVal( map.get( "username" ) );
            u.mail = stringVal( map.get( "mail" ) );
            u.friendsCount = 0;
            u.bwAvailLastMonth = 0L;
            u.sharedSpace = 0L;
            u.sharedItems = 0L;
            Map<String, ?> mapDisk = (Map<String, ?>) map.get( "disk" );
            u.diskQuota = longVal( mapDisk.get( "size" ) );
            u.diskQuotaAvailable = longVal( mapDisk.get( "avail" ) );
            u.bwQuota = longVal( mapDisk.get( "used" ) );
            return u;
        }
        throw context.cannotConvertValueToTargetType( value, targetClass );
    }

    @Override
    public String toString()
    {
        return name;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getMail()
    {
        return mail;
    }

    public void setMail( String mail )
    {
        this.mail = mail;
    }

    public Integer getFriendsCount()
    {
        return friendsCount;
    }

    public void setFriendsCount( Integer friendsCount )
    {
        this.friendsCount = friendsCount;
    }

    public Long getBwAvailLastMonth()
    {
        return bwAvailLastMonth;
    }

    public void setBwAvailLastMonth( Long bwAvailLastMonth )
    {
        this.bwAvailLastMonth = bwAvailLastMonth;
    }

    public Long getSharedSpace()
    {
        return sharedSpace;
    }

    public void setSharedSpace( Long sharedSpace )
    {
        this.sharedSpace = sharedSpace;
    }

    public Long getSharedItems()
    {
        return sharedItems;
    }

    public void setSharedItems( Long sharedItems )
    {
        this.sharedItems = sharedItems;
    }

    public Long getDiskQuota()
    {
        return diskQuota;
    }

    public void setDiskQuota( Long diskQuota )
    {
        this.diskQuota = diskQuota;
    }

    public Long getDiskQuotaAvailable()
    {
        return diskQuotaAvailable;
    }

    public void setDiskQuotaAvailable( Long diskQuotaAvailable )
    {
        this.diskQuotaAvailable = diskQuotaAvailable;
    }

    public Long getBwQuota()
    {
        return bwQuota;
    }

    public void setBwQuota( Long bwQuota )
    {
        this.bwQuota = bwQuota;
    }

}
