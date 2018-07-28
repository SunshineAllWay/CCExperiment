package org.apache.cassandra.service;
import org.apache.cassandra.locator.TokenMetadata;
public class StorageServiceAccessor
{
    public static TokenMetadata setTokenMetadata(TokenMetadata tmd)
    {
        return StorageService.instance.setTokenMetadataUnsafe(tmd);
    }
}
