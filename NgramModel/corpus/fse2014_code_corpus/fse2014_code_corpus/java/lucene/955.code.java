package com.sleepycat.db;
import com.sleepycat.db.internal.Db;
import com.sleepycat.db.internal.DbTxn;
public class DbHandleExtractor {
    private DbHandleExtractor()
    {
    }
    static public Db getDb(Database database)
    {
        return database.db;
    }
    static public DbTxn getDbTxn(Transaction transaction)
    {
        return transaction.txn;
    }
}
