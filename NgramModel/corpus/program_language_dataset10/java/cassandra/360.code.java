package org.apache.cassandra.service;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.net.IVerbHandler;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
public class IndexScanVerbHandler implements IVerbHandler
{
    private static final Logger logger = LoggerFactory.getLogger(IndexScanVerbHandler.class);
    public void doVerb(Message message)
    {
        try
        {
            IndexScanCommand command = IndexScanCommand.read(message);
            ColumnFamilyStore cfs = Table.open(command.keyspace).getColumnFamilyStore(command.column_family);
            List<Row> rows = cfs.scan(command.index_clause, command.range, QueryFilter.getFilter(command.predicate, cfs.getComparator()));
            RangeSliceReply reply = new RangeSliceReply(rows);
            Message response = reply.getReply(message);
            if (logger.isDebugEnabled())
                logger.debug("Sending " + reply+ " to " + message.getMessageId() + "@" + message.getFrom());
            MessagingService.instance().sendOneWay(response, message.getFrom());
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
