package org.apache.cassandra.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.RangeSliceCommand;
import org.apache.cassandra.db.RangeSliceReply;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.net.IVerbHandler;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
public class RangeSliceVerbHandler implements IVerbHandler
{
    private static final Logger logger = LoggerFactory.getLogger(RangeSliceVerbHandler.class);
    public void doVerb(Message message)
    {
        try
        {
            if (StorageService.instance.isBootstrapMode())
            {
                throw new RuntimeException("Cannot service reads while bootstrapping!");
            }
            RangeSliceCommand command = RangeSliceCommand.read(message);
            ColumnFamilyStore cfs = Table.open(command.keyspace).getColumnFamilyStore(command.column_family);
            RangeSliceReply reply = new RangeSliceReply(cfs.getRangeSlice(command.super_column,
                                                                          command.range,
                                                                          command.max_keys,
                                                                          QueryFilter.getFilter(command.predicate, cfs.getComparator())));
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
