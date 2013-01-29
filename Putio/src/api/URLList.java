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
import java.util.Map;

import flexjson.ObjectBinder;
import flexjson.ObjectFactory;

/**
 * Stores the results from an {@link URL#analyze(java.util.List)} call.
 *
 * @author Mark Manes
 */
public class URLList implements ObjectFactory
{
    private URL[] multipart;
    private URL[] torrent;
    private URL[] singleurl;
    private URL[] error;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object instantiate( ObjectBinder context, Object value, Type targetType, Class targetClass )
    {
        if( value instanceof Map && targetClass == URLList.class )
        {
            Map<String, ?> map = (Map<String, ?>) value;
            URLList u = new URLList();
            context.use( URL.class, new URL() );
            context.bindIntoObject( (Map) map.get( "items" ), u, targetClass );
            return u;
        }
        throw context.cannotConvertValueToTargetType( value, targetClass );
    }

    /**
     * Stores any valid multipart URLs.
     */
    public URL[] getMultipart()
    {
        return multipart;
    }

    public void setMultipart( URL[] multipart )
    {
        this.multipart = multipart;
    }

    /**
     * Stores any valid torrent URLs.
     */
    public URL[] getTorrent()
    {
        return torrent;
    }

    public void setTorrent( URL[] torrent )
    {
        this.torrent = torrent;
    }

    /**
     * Stores any valid normal URLs.
     */
    public URL[] getSingleurl()
    {
        return singleurl;
    }

    public void setSingleurl( URL[] singleurl )
    {
        this.singleurl = singleurl;
    }

    /**
     * Stores any invalid URLs.
     */
    public URL[] getError()
    {
        return error;
    }

    public void setError( URL[] error )
    {
        this.error = error;
    }

}
