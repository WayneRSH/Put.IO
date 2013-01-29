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
 * Represents and manages dashboard messages on the server.
 *
 * @author Mark Manes
 *
 */
public class Message implements ObjectFactory
{

    private String fileId;
    private String userId;
    private Boolean hidden;
    private String description;
    private String title;
    private String importance;
    private String fileName;
    private String fileType;
    private String fromUserId;
    private String id;
    private String channel;

    /**
     * Retrieves a list of messages from the server.
     * @return List of messages
     */
    public List<Message> list() throws Exception
    {
        Request req = new Request( "messages?method=list" );
        List<Map<String, ?>> results = getResults( req );

        return bindArray(results, Message.class);
    }

    /**
     * Deletes this message from the server
     */
    public void delete() throws Exception
    {
        if( id == null )
            throw new Exception( "Null message id" );

        Request req = new Request( "messages?method=delete" );

        Map<String, Object> params = req.getParams();
        params.put( "id", id );

        getResults( req );
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object instantiate( ObjectBinder context, Object value, Type targetType, Class targetClass )
    {
        if( value instanceof Map && targetClass == Message.class )
        {
            Map<String, ?> map = (Map<String, ?>) value;
            Message m = new Message();
            m.fileId = stringVal( map.get( "user_file_id" ) );
            m.hidden = boolVal( map.get( "hidden" ) );
            m.userId = stringVal( map.get( "user_id" ) );
            m.description = stringVal( map.get( "description" ) );
            m.title = stringVal( map.get( "title" ) );
            m.importance = stringVal( map.get( "importance" ) );
            m.fileName = stringVal( map.get( "file_name" ) );
            m.fileType = stringVal( map.get( "file_type" ) );
            m.fromUserId = stringVal( map.get( "from_user_id" ) );
            m.id = stringVal( map.get( "id" ) );
            m.channel = stringVal( map.get( "channel" ) );

            return m;
        }
        throw context.cannotConvertValueToTargetType( value, targetClass );
    }

    public String getFileId()
    {
        return fileId;
    }

    public void setFileId( String fileId )
    {
        this.fileId = fileId;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId( String userId )
    {
        this.userId = userId;
    }

    public Boolean getHidden()
    {
        return hidden;
    }

    public void setHidden( Boolean hidden )
    {
        this.hidden = hidden;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle( String title )
    {
        this.title = title;
    }

    public String getImportance()
    {
        return importance;
    }

    public void setImportance( String importance )
    {
        this.importance = importance;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName( String fileName )
    {
        this.fileName = fileName;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType( String fileType )
    {
        this.fileType = fileType;
    }

    public String getFromUserId()
    {
        return fromUserId;
    }

    public void setFromUserId( String fromUserId )
    {
        this.fromUserId = fromUserId;
    }

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public String getChannel()
    {
        return channel;
    }

    public void setChannel( String channel )
    {
        this.channel = channel;
    }
}
