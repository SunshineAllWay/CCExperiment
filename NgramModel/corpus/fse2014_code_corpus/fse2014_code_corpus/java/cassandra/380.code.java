package org.apache.cassandra.streaming;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.net.IVerbHandler;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.service.StorageService;
public class ReplicationFinishedVerbHandler implements IVerbHandler
{
    private static Logger logger = LoggerFactory.getLogger(ReplicationFinishedVerbHandler.class);
    public void doVerb(Message msg)
    {
        StorageService.instance.confirmReplication(msg.getFrom());
        Message response = msg.getInternalReply(ArrayUtils.EMPTY_BYTE_ARRAY);
        if (logger.isDebugEnabled())
            logger.debug("Replying to " + msg.getMessageId() + "@" + msg.getFrom());
        MessagingService.instance().sendOneWay(response, msg.getFrom());
    }
}
