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

import static api.Util.bindArray;
import static api.Util.boolVal;
import static api.Util.intVal;
import static api.Util.stringVal;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import flexjson.ObjectBinder;
import flexjson.ObjectFactory;

/**
 * Represents a server directory retrieved from an {@link Item#dirmap()} call.
 *
 * @author Mark Manes
 *
 */
public class Dir implements ObjectFactory
{
    private Integer id;
    private String name;
    private Integer parentId;
    private Boolean shared;
    private Boolean defaultShared;
    private List<Dir> subdirs;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object instantiate( ObjectBinder context, Object value, Type targetType, Class targetClass )
    {
        if( value instanceof Map && targetClass == Dir.class )
        {
            Map<String, ?> map = (Map<String, ?>) value;
            Dir d = new Dir();
            d.id = intVal( map.get( "id" ) );
            d.name = stringVal( map.get( "name" ) );
            d.parentId = intVal( map.get("parent_id"));
            d.shared = boolVal( map.get("shared"));
            d.defaultShared = boolVal( map.get("default_shared"));

            Object o = map.get( "dirs" );
            if(o instanceof List)
            {
                List dirs = (List) map.get( "dirs" );
                d.subdirs = bindArray( dirs, Dir.class );
            }
            return d;
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

    public Integer getParentId()
    {
        return parentId;
    }

    public void setParentId( Integer parentId )
    {
        this.parentId = parentId;
    }

    public Boolean getShared()
    {
        return shared;
    }

    public void setShared( Boolean shared )
    {
        this.shared = shared;
    }

    public Boolean getDefaultShared()
    {
        return defaultShared;
    }

    public void setDefaultShared( Boolean defaultShared )
    {
        this.defaultShared = defaultShared;
    }

    public List<Dir> getSubdirs()
    {
        return subdirs;
    }

    public void setSubdirs( List<Dir> subdirs )
    {
        this.subdirs = subdirs;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId( Integer id )
    {
        this.id = id;
    }
}
