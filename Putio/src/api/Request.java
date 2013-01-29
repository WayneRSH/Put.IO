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

import java.util.HashMap;
import java.util.Map;

/**
 * API request to put.io.
 *
 * @author Mark Manes
 *
 */
public class Request
{
    private final Map<String, Object> params;
    private final String path;

    public Request( String path )
    {
        this.path = path;
        this.params = new HashMap<String, Object>();
    }

    public Request( String path, Map<String, Object> params )
    {
        this.path = path;
        this.params = params;
    }

    public Map<String, Object> getParams()
    {
        return params;
    }

    public String getPath()
    {
        return path;
    }
}
