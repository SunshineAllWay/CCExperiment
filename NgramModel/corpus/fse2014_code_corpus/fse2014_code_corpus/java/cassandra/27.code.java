package org.apache.cassandra.thrift;
import org.apache.commons.lang.builder.HashCodeBuilder;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.thrift.*;
import org.apache.thrift.async.*;
import org.apache.thrift.meta_data.*;
import org.apache.thrift.transport.*;
import org.apache.thrift.protocol.*;
public class Cassandra {
  public interface Iface {
    public void login(AuthenticationRequest auth_request) throws AuthenticationException, AuthorizationException, TException;
    public void set_keyspace(String keyspace) throws InvalidRequestException, TException;
    public ColumnOrSuperColumn get(ByteBuffer key, ColumnPath column_path, ConsistencyLevel consistency_level) throws InvalidRequestException, NotFoundException, UnavailableException, TimedOutException, TException;
    public List<ColumnOrSuperColumn> get_slice(ByteBuffer key, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException;
    public int get_count(ByteBuffer key, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException;
    public Map<ByteBuffer,List<ColumnOrSuperColumn>> multiget_slice(List<ByteBuffer> keys, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException;
    public Map<ByteBuffer,Integer> multiget_count(List<ByteBuffer> keys, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException;
    public List<KeySlice> get_range_slices(ColumnParent column_parent, SlicePredicate predicate, KeyRange range, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException;
    public List<KeySlice> get_indexed_slices(ColumnParent column_parent, IndexClause index_clause, SlicePredicate column_predicate, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException;
    public void insert(ByteBuffer key, ColumnParent column_parent, Column column, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException;
    public void remove(ByteBuffer key, ColumnPath column_path, long timestamp, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException;
    public void batch_mutate(Map<ByteBuffer,Map<String,List<Mutation>>> mutation_map, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException;
    public void truncate(String cfname) throws InvalidRequestException, UnavailableException, TException;
    public void add(ByteBuffer key, ColumnParent column_parent, CounterColumn column, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException;
    public void batch_add(Map<ByteBuffer,Map<String,List<CounterMutation>>> update_map, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException;
    public Counter get_counter(ByteBuffer key, ColumnPath path, ConsistencyLevel consistency_level) throws InvalidRequestException, NotFoundException, UnavailableException, TimedOutException, TException;
    public List<Counter> get_counter_slice(ByteBuffer key, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException;
    public Map<ByteBuffer,List<Counter>> multiget_counter_slice(List<ByteBuffer> keys, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException;
    public void remove_counter(ByteBuffer key, ColumnPath path, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException;
    public Map<String,List<String>> describe_schema_versions() throws InvalidRequestException, TException;
    public List<KsDef> describe_keyspaces() throws InvalidRequestException, TException;
    public String describe_cluster_name() throws TException;
    public String describe_version() throws TException;
    public List<TokenRange> describe_ring(String keyspace) throws InvalidRequestException, TException;
    public String describe_partitioner() throws TException;
    public String describe_snitch() throws TException;
    public KsDef describe_keyspace(String keyspace) throws NotFoundException, InvalidRequestException, TException;
    public List<String> describe_splits(String cfName, String start_token, String end_token, int keys_per_split) throws TException;
    public String system_add_column_family(CfDef cf_def) throws InvalidRequestException, TException;
    public String system_drop_column_family(String column_family) throws InvalidRequestException, TException;
    public String system_add_keyspace(KsDef ks_def) throws InvalidRequestException, TException;
    public String system_drop_keyspace(String keyspace) throws InvalidRequestException, TException;
    public String system_update_keyspace(KsDef ks_def) throws InvalidRequestException, TException;
    public String system_update_column_family(CfDef cf_def) throws InvalidRequestException, TException;
    public CqlResult execute_cql_query(ByteBuffer query, Compression compression) throws InvalidRequestException, UnavailableException, TimedOutException, TException;
  }
  public interface AsyncIface {
    public void login(AuthenticationRequest auth_request, AsyncMethodCallback<AsyncClient.login_call> resultHandler) throws TException;
    public void set_keyspace(String keyspace, AsyncMethodCallback<AsyncClient.set_keyspace_call> resultHandler) throws TException;
    public void get(ByteBuffer key, ColumnPath column_path, ConsistencyLevel consistency_level, AsyncMethodCallback<AsyncClient.get_call> resultHandler) throws TException;
    public void get_slice(ByteBuffer key, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<AsyncClient.get_slice_call> resultHandler) throws TException;
    public void get_count(ByteBuffer key, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<AsyncClient.get_count_call> resultHandler) throws TException;
    public void multiget_slice(List<ByteBuffer> keys, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<AsyncClient.multiget_slice_call> resultHandler) throws TException;
    public void multiget_count(List<ByteBuffer> keys, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<AsyncClient.multiget_count_call> resultHandler) throws TException;
    public void get_range_slices(ColumnParent column_parent, SlicePredicate predicate, KeyRange range, ConsistencyLevel consistency_level, AsyncMethodCallback<AsyncClient.get_range_slices_call> resultHandler) throws TException;
    public void get_indexed_slices(ColumnParent column_parent, IndexClause index_clause, SlicePredicate column_predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<AsyncClient.get_indexed_slices_call> resultHandler) throws TException;
    public void insert(ByteBuffer key, ColumnParent column_parent, Column column, ConsistencyLevel consistency_level, AsyncMethodCallback<AsyncClient.insert_call> resultHandler) throws TException;
    public void remove(ByteBuffer key, ColumnPath column_path, long timestamp, ConsistencyLevel consistency_level, AsyncMethodCallback<AsyncClient.remove_call> resultHandler) throws TException;
    public void batch_mutate(Map<ByteBuffer,Map<String,List<Mutation>>> mutation_map, ConsistencyLevel consistency_level, AsyncMethodCallback<AsyncClient.batch_mutate_call> resultHandler) throws TException;
    public void truncate(String cfname, AsyncMethodCallback<AsyncClient.truncate_call> resultHandler) throws TException;
    public void add(ByteBuffer key, ColumnParent column_parent, CounterColumn column, ConsistencyLevel consistency_level, AsyncMethodCallback<AsyncClient.add_call> resultHandler) throws TException;
    public void batch_add(Map<ByteBuffer,Map<String,List<CounterMutation>>> update_map, ConsistencyLevel consistency_level, AsyncMethodCallback<AsyncClient.batch_add_call> resultHandler) throws TException;
    public void get_counter(ByteBuffer key, ColumnPath path, ConsistencyLevel consistency_level, AsyncMethodCallback<AsyncClient.get_counter_call> resultHandler) throws TException;
    public void get_counter_slice(ByteBuffer key, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<AsyncClient.get_counter_slice_call> resultHandler) throws TException;
    public void multiget_counter_slice(List<ByteBuffer> keys, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<AsyncClient.multiget_counter_slice_call> resultHandler) throws TException;
    public void remove_counter(ByteBuffer key, ColumnPath path, ConsistencyLevel consistency_level, AsyncMethodCallback<AsyncClient.remove_counter_call> resultHandler) throws TException;
    public void describe_schema_versions(AsyncMethodCallback<AsyncClient.describe_schema_versions_call> resultHandler) throws TException;
    public void describe_keyspaces(AsyncMethodCallback<AsyncClient.describe_keyspaces_call> resultHandler) throws TException;
    public void describe_cluster_name(AsyncMethodCallback<AsyncClient.describe_cluster_name_call> resultHandler) throws TException;
    public void describe_version(AsyncMethodCallback<AsyncClient.describe_version_call> resultHandler) throws TException;
    public void describe_ring(String keyspace, AsyncMethodCallback<AsyncClient.describe_ring_call> resultHandler) throws TException;
    public void describe_partitioner(AsyncMethodCallback<AsyncClient.describe_partitioner_call> resultHandler) throws TException;
    public void describe_snitch(AsyncMethodCallback<AsyncClient.describe_snitch_call> resultHandler) throws TException;
    public void describe_keyspace(String keyspace, AsyncMethodCallback<AsyncClient.describe_keyspace_call> resultHandler) throws TException;
    public void describe_splits(String cfName, String start_token, String end_token, int keys_per_split, AsyncMethodCallback<AsyncClient.describe_splits_call> resultHandler) throws TException;
    public void system_add_column_family(CfDef cf_def, AsyncMethodCallback<AsyncClient.system_add_column_family_call> resultHandler) throws TException;
    public void system_drop_column_family(String column_family, AsyncMethodCallback<AsyncClient.system_drop_column_family_call> resultHandler) throws TException;
    public void system_add_keyspace(KsDef ks_def, AsyncMethodCallback<AsyncClient.system_add_keyspace_call> resultHandler) throws TException;
    public void system_drop_keyspace(String keyspace, AsyncMethodCallback<AsyncClient.system_drop_keyspace_call> resultHandler) throws TException;
    public void system_update_keyspace(KsDef ks_def, AsyncMethodCallback<AsyncClient.system_update_keyspace_call> resultHandler) throws TException;
    public void system_update_column_family(CfDef cf_def, AsyncMethodCallback<AsyncClient.system_update_column_family_call> resultHandler) throws TException;
    public void execute_cql_query(ByteBuffer query, Compression compression, AsyncMethodCallback<AsyncClient.execute_cql_query_call> resultHandler) throws TException;
  }
  public static class Client implements TServiceClient, Iface {
    public static class Factory implements TServiceClientFactory<Client> {
      public Factory() {}
      public Client getClient(TProtocol prot) {
        return new Client(prot);
      }
      public Client getClient(TProtocol iprot, TProtocol oprot) {
        return new Client(iprot, oprot);
      }
    }
    public Client(TProtocol prot)
    {
      this(prot, prot);
    }
    public Client(TProtocol iprot, TProtocol oprot)
    {
      iprot_ = iprot;
      oprot_ = oprot;
    }
    protected TProtocol iprot_;
    protected TProtocol oprot_;
    protected int seqid_;
    public TProtocol getInputProtocol()
    {
      return this.iprot_;
    }
    public TProtocol getOutputProtocol()
    {
      return this.oprot_;
    }
    public void login(AuthenticationRequest auth_request) throws AuthenticationException, AuthorizationException, TException
    {
      send_login(auth_request);
      recv_login();
    }
    public void send_login(AuthenticationRequest auth_request) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("login", TMessageType.CALL, ++seqid_));
      login_args args = new login_args();
      args.setAuth_request(auth_request);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public void recv_login() throws AuthenticationException, AuthorizationException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "login failed: out of sequence response");
      }
      login_result result = new login_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.authnx != null) {
        throw result.authnx;
      }
      if (result.authzx != null) {
        throw result.authzx;
      }
      return;
    }
    public void set_keyspace(String keyspace) throws InvalidRequestException, TException
    {
      send_set_keyspace(keyspace);
      recv_set_keyspace();
    }
    public void send_set_keyspace(String keyspace) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("set_keyspace", TMessageType.CALL, ++seqid_));
      set_keyspace_args args = new set_keyspace_args();
      args.setKeyspace(keyspace);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public void recv_set_keyspace() throws InvalidRequestException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "set_keyspace failed: out of sequence response");
      }
      set_keyspace_result result = new set_keyspace_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.ire != null) {
        throw result.ire;
      }
      return;
    }
    public ColumnOrSuperColumn get(ByteBuffer key, ColumnPath column_path, ConsistencyLevel consistency_level) throws InvalidRequestException, NotFoundException, UnavailableException, TimedOutException, TException
    {
      send_get(key, column_path, consistency_level);
      return recv_get();
    }
    public void send_get(ByteBuffer key, ColumnPath column_path, ConsistencyLevel consistency_level) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("get", TMessageType.CALL, ++seqid_));
      get_args args = new get_args();
      args.setKey(key);
      args.setColumn_path(column_path);
      args.setConsistency_level(consistency_level);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public ColumnOrSuperColumn recv_get() throws InvalidRequestException, NotFoundException, UnavailableException, TimedOutException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "get failed: out of sequence response");
      }
      get_result result = new get_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      if (result.nfe != null) {
        throw result.nfe;
      }
      if (result.ue != null) {
        throw result.ue;
      }
      if (result.te != null) {
        throw result.te;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "get failed: unknown result");
    }
    public List<ColumnOrSuperColumn> get_slice(ByteBuffer key, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      send_get_slice(key, column_parent, predicate, consistency_level);
      return recv_get_slice();
    }
    public void send_get_slice(ByteBuffer key, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("get_slice", TMessageType.CALL, ++seqid_));
      get_slice_args args = new get_slice_args();
      args.setKey(key);
      args.setColumn_parent(column_parent);
      args.setPredicate(predicate);
      args.setConsistency_level(consistency_level);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public List<ColumnOrSuperColumn> recv_get_slice() throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "get_slice failed: out of sequence response");
      }
      get_slice_result result = new get_slice_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      if (result.ue != null) {
        throw result.ue;
      }
      if (result.te != null) {
        throw result.te;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "get_slice failed: unknown result");
    }
    public int get_count(ByteBuffer key, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      send_get_count(key, column_parent, predicate, consistency_level);
      return recv_get_count();
    }
    public void send_get_count(ByteBuffer key, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("get_count", TMessageType.CALL, ++seqid_));
      get_count_args args = new get_count_args();
      args.setKey(key);
      args.setColumn_parent(column_parent);
      args.setPredicate(predicate);
      args.setConsistency_level(consistency_level);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public int recv_get_count() throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "get_count failed: out of sequence response");
      }
      get_count_result result = new get_count_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      if (result.ue != null) {
        throw result.ue;
      }
      if (result.te != null) {
        throw result.te;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "get_count failed: unknown result");
    }
    public Map<ByteBuffer,List<ColumnOrSuperColumn>> multiget_slice(List<ByteBuffer> keys, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      send_multiget_slice(keys, column_parent, predicate, consistency_level);
      return recv_multiget_slice();
    }
    public void send_multiget_slice(List<ByteBuffer> keys, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("multiget_slice", TMessageType.CALL, ++seqid_));
      multiget_slice_args args = new multiget_slice_args();
      args.setKeys(keys);
      args.setColumn_parent(column_parent);
      args.setPredicate(predicate);
      args.setConsistency_level(consistency_level);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public Map<ByteBuffer,List<ColumnOrSuperColumn>> recv_multiget_slice() throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "multiget_slice failed: out of sequence response");
      }
      multiget_slice_result result = new multiget_slice_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      if (result.ue != null) {
        throw result.ue;
      }
      if (result.te != null) {
        throw result.te;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "multiget_slice failed: unknown result");
    }
    public Map<ByteBuffer,Integer> multiget_count(List<ByteBuffer> keys, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      send_multiget_count(keys, column_parent, predicate, consistency_level);
      return recv_multiget_count();
    }
    public void send_multiget_count(List<ByteBuffer> keys, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("multiget_count", TMessageType.CALL, ++seqid_));
      multiget_count_args args = new multiget_count_args();
      args.setKeys(keys);
      args.setColumn_parent(column_parent);
      args.setPredicate(predicate);
      args.setConsistency_level(consistency_level);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public Map<ByteBuffer,Integer> recv_multiget_count() throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "multiget_count failed: out of sequence response");
      }
      multiget_count_result result = new multiget_count_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      if (result.ue != null) {
        throw result.ue;
      }
      if (result.te != null) {
        throw result.te;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "multiget_count failed: unknown result");
    }
    public List<KeySlice> get_range_slices(ColumnParent column_parent, SlicePredicate predicate, KeyRange range, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      send_get_range_slices(column_parent, predicate, range, consistency_level);
      return recv_get_range_slices();
    }
    public void send_get_range_slices(ColumnParent column_parent, SlicePredicate predicate, KeyRange range, ConsistencyLevel consistency_level) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("get_range_slices", TMessageType.CALL, ++seqid_));
      get_range_slices_args args = new get_range_slices_args();
      args.setColumn_parent(column_parent);
      args.setPredicate(predicate);
      args.setRange(range);
      args.setConsistency_level(consistency_level);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public List<KeySlice> recv_get_range_slices() throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "get_range_slices failed: out of sequence response");
      }
      get_range_slices_result result = new get_range_slices_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      if (result.ue != null) {
        throw result.ue;
      }
      if (result.te != null) {
        throw result.te;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "get_range_slices failed: unknown result");
    }
    public List<KeySlice> get_indexed_slices(ColumnParent column_parent, IndexClause index_clause, SlicePredicate column_predicate, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      send_get_indexed_slices(column_parent, index_clause, column_predicate, consistency_level);
      return recv_get_indexed_slices();
    }
    public void send_get_indexed_slices(ColumnParent column_parent, IndexClause index_clause, SlicePredicate column_predicate, ConsistencyLevel consistency_level) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("get_indexed_slices", TMessageType.CALL, ++seqid_));
      get_indexed_slices_args args = new get_indexed_slices_args();
      args.setColumn_parent(column_parent);
      args.setIndex_clause(index_clause);
      args.setColumn_predicate(column_predicate);
      args.setConsistency_level(consistency_level);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public List<KeySlice> recv_get_indexed_slices() throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "get_indexed_slices failed: out of sequence response");
      }
      get_indexed_slices_result result = new get_indexed_slices_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      if (result.ue != null) {
        throw result.ue;
      }
      if (result.te != null) {
        throw result.te;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "get_indexed_slices failed: unknown result");
    }
    public void insert(ByteBuffer key, ColumnParent column_parent, Column column, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      send_insert(key, column_parent, column, consistency_level);
      recv_insert();
    }
    public void send_insert(ByteBuffer key, ColumnParent column_parent, Column column, ConsistencyLevel consistency_level) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("insert", TMessageType.CALL, ++seqid_));
      insert_args args = new insert_args();
      args.setKey(key);
      args.setColumn_parent(column_parent);
      args.setColumn(column);
      args.setConsistency_level(consistency_level);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public void recv_insert() throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "insert failed: out of sequence response");
      }
      insert_result result = new insert_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.ire != null) {
        throw result.ire;
      }
      if (result.ue != null) {
        throw result.ue;
      }
      if (result.te != null) {
        throw result.te;
      }
      return;
    }
    public void remove(ByteBuffer key, ColumnPath column_path, long timestamp, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      send_remove(key, column_path, timestamp, consistency_level);
      recv_remove();
    }
    public void send_remove(ByteBuffer key, ColumnPath column_path, long timestamp, ConsistencyLevel consistency_level) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("remove", TMessageType.CALL, ++seqid_));
      remove_args args = new remove_args();
      args.setKey(key);
      args.setColumn_path(column_path);
      args.setTimestamp(timestamp);
      args.setConsistency_level(consistency_level);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public void recv_remove() throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "remove failed: out of sequence response");
      }
      remove_result result = new remove_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.ire != null) {
        throw result.ire;
      }
      if (result.ue != null) {
        throw result.ue;
      }
      if (result.te != null) {
        throw result.te;
      }
      return;
    }
    public void batch_mutate(Map<ByteBuffer,Map<String,List<Mutation>>> mutation_map, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      send_batch_mutate(mutation_map, consistency_level);
      recv_batch_mutate();
    }
    public void send_batch_mutate(Map<ByteBuffer,Map<String,List<Mutation>>> mutation_map, ConsistencyLevel consistency_level) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("batch_mutate", TMessageType.CALL, ++seqid_));
      batch_mutate_args args = new batch_mutate_args();
      args.setMutation_map(mutation_map);
      args.setConsistency_level(consistency_level);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public void recv_batch_mutate() throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "batch_mutate failed: out of sequence response");
      }
      batch_mutate_result result = new batch_mutate_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.ire != null) {
        throw result.ire;
      }
      if (result.ue != null) {
        throw result.ue;
      }
      if (result.te != null) {
        throw result.te;
      }
      return;
    }
    public void truncate(String cfname) throws InvalidRequestException, UnavailableException, TException
    {
      send_truncate(cfname);
      recv_truncate();
    }
    public void send_truncate(String cfname) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("truncate", TMessageType.CALL, ++seqid_));
      truncate_args args = new truncate_args();
      args.setCfname(cfname);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public void recv_truncate() throws InvalidRequestException, UnavailableException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "truncate failed: out of sequence response");
      }
      truncate_result result = new truncate_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.ire != null) {
        throw result.ire;
      }
      if (result.ue != null) {
        throw result.ue;
      }
      return;
    }
    public void add(ByteBuffer key, ColumnParent column_parent, CounterColumn column, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      send_add(key, column_parent, column, consistency_level);
      recv_add();
    }
    public void send_add(ByteBuffer key, ColumnParent column_parent, CounterColumn column, ConsistencyLevel consistency_level) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("add", TMessageType.CALL, ++seqid_));
      add_args args = new add_args();
      args.setKey(key);
      args.setColumn_parent(column_parent);
      args.setColumn(column);
      args.setConsistency_level(consistency_level);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public void recv_add() throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "add failed: out of sequence response");
      }
      add_result result = new add_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.ire != null) {
        throw result.ire;
      }
      if (result.ue != null) {
        throw result.ue;
      }
      if (result.te != null) {
        throw result.te;
      }
      return;
    }
    public void batch_add(Map<ByteBuffer,Map<String,List<CounterMutation>>> update_map, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      send_batch_add(update_map, consistency_level);
      recv_batch_add();
    }
    public void send_batch_add(Map<ByteBuffer,Map<String,List<CounterMutation>>> update_map, ConsistencyLevel consistency_level) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("batch_add", TMessageType.CALL, ++seqid_));
      batch_add_args args = new batch_add_args();
      args.setUpdate_map(update_map);
      args.setConsistency_level(consistency_level);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public void recv_batch_add() throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "batch_add failed: out of sequence response");
      }
      batch_add_result result = new batch_add_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.ire != null) {
        throw result.ire;
      }
      if (result.ue != null) {
        throw result.ue;
      }
      if (result.te != null) {
        throw result.te;
      }
      return;
    }
    public Counter get_counter(ByteBuffer key, ColumnPath path, ConsistencyLevel consistency_level) throws InvalidRequestException, NotFoundException, UnavailableException, TimedOutException, TException
    {
      send_get_counter(key, path, consistency_level);
      return recv_get_counter();
    }
    public void send_get_counter(ByteBuffer key, ColumnPath path, ConsistencyLevel consistency_level) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("get_counter", TMessageType.CALL, ++seqid_));
      get_counter_args args = new get_counter_args();
      args.setKey(key);
      args.setPath(path);
      args.setConsistency_level(consistency_level);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public Counter recv_get_counter() throws InvalidRequestException, NotFoundException, UnavailableException, TimedOutException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "get_counter failed: out of sequence response");
      }
      get_counter_result result = new get_counter_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      if (result.nfe != null) {
        throw result.nfe;
      }
      if (result.ue != null) {
        throw result.ue;
      }
      if (result.te != null) {
        throw result.te;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "get_counter failed: unknown result");
    }
    public List<Counter> get_counter_slice(ByteBuffer key, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      send_get_counter_slice(key, column_parent, predicate, consistency_level);
      return recv_get_counter_slice();
    }
    public void send_get_counter_slice(ByteBuffer key, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("get_counter_slice", TMessageType.CALL, ++seqid_));
      get_counter_slice_args args = new get_counter_slice_args();
      args.setKey(key);
      args.setColumn_parent(column_parent);
      args.setPredicate(predicate);
      args.setConsistency_level(consistency_level);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public List<Counter> recv_get_counter_slice() throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "get_counter_slice failed: out of sequence response");
      }
      get_counter_slice_result result = new get_counter_slice_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      if (result.ue != null) {
        throw result.ue;
      }
      if (result.te != null) {
        throw result.te;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "get_counter_slice failed: unknown result");
    }
    public Map<ByteBuffer,List<Counter>> multiget_counter_slice(List<ByteBuffer> keys, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      send_multiget_counter_slice(keys, column_parent, predicate, consistency_level);
      return recv_multiget_counter_slice();
    }
    public void send_multiget_counter_slice(List<ByteBuffer> keys, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("multiget_counter_slice", TMessageType.CALL, ++seqid_));
      multiget_counter_slice_args args = new multiget_counter_slice_args();
      args.setKeys(keys);
      args.setColumn_parent(column_parent);
      args.setPredicate(predicate);
      args.setConsistency_level(consistency_level);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public Map<ByteBuffer,List<Counter>> recv_multiget_counter_slice() throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "multiget_counter_slice failed: out of sequence response");
      }
      multiget_counter_slice_result result = new multiget_counter_slice_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      if (result.ue != null) {
        throw result.ue;
      }
      if (result.te != null) {
        throw result.te;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "multiget_counter_slice failed: unknown result");
    }
    public void remove_counter(ByteBuffer key, ColumnPath path, ConsistencyLevel consistency_level) throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      send_remove_counter(key, path, consistency_level);
      recv_remove_counter();
    }
    public void send_remove_counter(ByteBuffer key, ColumnPath path, ConsistencyLevel consistency_level) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("remove_counter", TMessageType.CALL, ++seqid_));
      remove_counter_args args = new remove_counter_args();
      args.setKey(key);
      args.setPath(path);
      args.setConsistency_level(consistency_level);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public void recv_remove_counter() throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "remove_counter failed: out of sequence response");
      }
      remove_counter_result result = new remove_counter_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.ire != null) {
        throw result.ire;
      }
      if (result.ue != null) {
        throw result.ue;
      }
      if (result.te != null) {
        throw result.te;
      }
      return;
    }
    public Map<String,List<String>> describe_schema_versions() throws InvalidRequestException, TException
    {
      send_describe_schema_versions();
      return recv_describe_schema_versions();
    }
    public void send_describe_schema_versions() throws TException
    {
      oprot_.writeMessageBegin(new TMessage("describe_schema_versions", TMessageType.CALL, ++seqid_));
      describe_schema_versions_args args = new describe_schema_versions_args();
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public Map<String,List<String>> recv_describe_schema_versions() throws InvalidRequestException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "describe_schema_versions failed: out of sequence response");
      }
      describe_schema_versions_result result = new describe_schema_versions_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "describe_schema_versions failed: unknown result");
    }
    public List<KsDef> describe_keyspaces() throws InvalidRequestException, TException
    {
      send_describe_keyspaces();
      return recv_describe_keyspaces();
    }
    public void send_describe_keyspaces() throws TException
    {
      oprot_.writeMessageBegin(new TMessage("describe_keyspaces", TMessageType.CALL, ++seqid_));
      describe_keyspaces_args args = new describe_keyspaces_args();
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public List<KsDef> recv_describe_keyspaces() throws InvalidRequestException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "describe_keyspaces failed: out of sequence response");
      }
      describe_keyspaces_result result = new describe_keyspaces_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "describe_keyspaces failed: unknown result");
    }
    public String describe_cluster_name() throws TException
    {
      send_describe_cluster_name();
      return recv_describe_cluster_name();
    }
    public void send_describe_cluster_name() throws TException
    {
      oprot_.writeMessageBegin(new TMessage("describe_cluster_name", TMessageType.CALL, ++seqid_));
      describe_cluster_name_args args = new describe_cluster_name_args();
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public String recv_describe_cluster_name() throws TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "describe_cluster_name failed: out of sequence response");
      }
      describe_cluster_name_result result = new describe_cluster_name_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "describe_cluster_name failed: unknown result");
    }
    public String describe_version() throws TException
    {
      send_describe_version();
      return recv_describe_version();
    }
    public void send_describe_version() throws TException
    {
      oprot_.writeMessageBegin(new TMessage("describe_version", TMessageType.CALL, ++seqid_));
      describe_version_args args = new describe_version_args();
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public String recv_describe_version() throws TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "describe_version failed: out of sequence response");
      }
      describe_version_result result = new describe_version_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "describe_version failed: unknown result");
    }
    public List<TokenRange> describe_ring(String keyspace) throws InvalidRequestException, TException
    {
      send_describe_ring(keyspace);
      return recv_describe_ring();
    }
    public void send_describe_ring(String keyspace) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("describe_ring", TMessageType.CALL, ++seqid_));
      describe_ring_args args = new describe_ring_args();
      args.setKeyspace(keyspace);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public List<TokenRange> recv_describe_ring() throws InvalidRequestException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "describe_ring failed: out of sequence response");
      }
      describe_ring_result result = new describe_ring_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "describe_ring failed: unknown result");
    }
    public String describe_partitioner() throws TException
    {
      send_describe_partitioner();
      return recv_describe_partitioner();
    }
    public void send_describe_partitioner() throws TException
    {
      oprot_.writeMessageBegin(new TMessage("describe_partitioner", TMessageType.CALL, ++seqid_));
      describe_partitioner_args args = new describe_partitioner_args();
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public String recv_describe_partitioner() throws TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "describe_partitioner failed: out of sequence response");
      }
      describe_partitioner_result result = new describe_partitioner_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "describe_partitioner failed: unknown result");
    }
    public String describe_snitch() throws TException
    {
      send_describe_snitch();
      return recv_describe_snitch();
    }
    public void send_describe_snitch() throws TException
    {
      oprot_.writeMessageBegin(new TMessage("describe_snitch", TMessageType.CALL, ++seqid_));
      describe_snitch_args args = new describe_snitch_args();
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public String recv_describe_snitch() throws TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "describe_snitch failed: out of sequence response");
      }
      describe_snitch_result result = new describe_snitch_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "describe_snitch failed: unknown result");
    }
    public KsDef describe_keyspace(String keyspace) throws NotFoundException, InvalidRequestException, TException
    {
      send_describe_keyspace(keyspace);
      return recv_describe_keyspace();
    }
    public void send_describe_keyspace(String keyspace) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("describe_keyspace", TMessageType.CALL, ++seqid_));
      describe_keyspace_args args = new describe_keyspace_args();
      args.setKeyspace(keyspace);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public KsDef recv_describe_keyspace() throws NotFoundException, InvalidRequestException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "describe_keyspace failed: out of sequence response");
      }
      describe_keyspace_result result = new describe_keyspace_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.nfe != null) {
        throw result.nfe;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "describe_keyspace failed: unknown result");
    }
    public List<String> describe_splits(String cfName, String start_token, String end_token, int keys_per_split) throws TException
    {
      send_describe_splits(cfName, start_token, end_token, keys_per_split);
      return recv_describe_splits();
    }
    public void send_describe_splits(String cfName, String start_token, String end_token, int keys_per_split) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("describe_splits", TMessageType.CALL, ++seqid_));
      describe_splits_args args = new describe_splits_args();
      args.setCfName(cfName);
      args.setStart_token(start_token);
      args.setEnd_token(end_token);
      args.setKeys_per_split(keys_per_split);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public List<String> recv_describe_splits() throws TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "describe_splits failed: out of sequence response");
      }
      describe_splits_result result = new describe_splits_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "describe_splits failed: unknown result");
    }
    public String system_add_column_family(CfDef cf_def) throws InvalidRequestException, TException
    {
      send_system_add_column_family(cf_def);
      return recv_system_add_column_family();
    }
    public void send_system_add_column_family(CfDef cf_def) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("system_add_column_family", TMessageType.CALL, ++seqid_));
      system_add_column_family_args args = new system_add_column_family_args();
      args.setCf_def(cf_def);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public String recv_system_add_column_family() throws InvalidRequestException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "system_add_column_family failed: out of sequence response");
      }
      system_add_column_family_result result = new system_add_column_family_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "system_add_column_family failed: unknown result");
    }
    public String system_drop_column_family(String column_family) throws InvalidRequestException, TException
    {
      send_system_drop_column_family(column_family);
      return recv_system_drop_column_family();
    }
    public void send_system_drop_column_family(String column_family) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("system_drop_column_family", TMessageType.CALL, ++seqid_));
      system_drop_column_family_args args = new system_drop_column_family_args();
      args.setColumn_family(column_family);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public String recv_system_drop_column_family() throws InvalidRequestException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "system_drop_column_family failed: out of sequence response");
      }
      system_drop_column_family_result result = new system_drop_column_family_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "system_drop_column_family failed: unknown result");
    }
    public String system_add_keyspace(KsDef ks_def) throws InvalidRequestException, TException
    {
      send_system_add_keyspace(ks_def);
      return recv_system_add_keyspace();
    }
    public void send_system_add_keyspace(KsDef ks_def) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("system_add_keyspace", TMessageType.CALL, ++seqid_));
      system_add_keyspace_args args = new system_add_keyspace_args();
      args.setKs_def(ks_def);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public String recv_system_add_keyspace() throws InvalidRequestException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "system_add_keyspace failed: out of sequence response");
      }
      system_add_keyspace_result result = new system_add_keyspace_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "system_add_keyspace failed: unknown result");
    }
    public String system_drop_keyspace(String keyspace) throws InvalidRequestException, TException
    {
      send_system_drop_keyspace(keyspace);
      return recv_system_drop_keyspace();
    }
    public void send_system_drop_keyspace(String keyspace) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("system_drop_keyspace", TMessageType.CALL, ++seqid_));
      system_drop_keyspace_args args = new system_drop_keyspace_args();
      args.setKeyspace(keyspace);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public String recv_system_drop_keyspace() throws InvalidRequestException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "system_drop_keyspace failed: out of sequence response");
      }
      system_drop_keyspace_result result = new system_drop_keyspace_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "system_drop_keyspace failed: unknown result");
    }
    public String system_update_keyspace(KsDef ks_def) throws InvalidRequestException, TException
    {
      send_system_update_keyspace(ks_def);
      return recv_system_update_keyspace();
    }
    public void send_system_update_keyspace(KsDef ks_def) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("system_update_keyspace", TMessageType.CALL, ++seqid_));
      system_update_keyspace_args args = new system_update_keyspace_args();
      args.setKs_def(ks_def);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public String recv_system_update_keyspace() throws InvalidRequestException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "system_update_keyspace failed: out of sequence response");
      }
      system_update_keyspace_result result = new system_update_keyspace_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "system_update_keyspace failed: unknown result");
    }
    public String system_update_column_family(CfDef cf_def) throws InvalidRequestException, TException
    {
      send_system_update_column_family(cf_def);
      return recv_system_update_column_family();
    }
    public void send_system_update_column_family(CfDef cf_def) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("system_update_column_family", TMessageType.CALL, ++seqid_));
      system_update_column_family_args args = new system_update_column_family_args();
      args.setCf_def(cf_def);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public String recv_system_update_column_family() throws InvalidRequestException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "system_update_column_family failed: out of sequence response");
      }
      system_update_column_family_result result = new system_update_column_family_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "system_update_column_family failed: unknown result");
    }
    public CqlResult execute_cql_query(ByteBuffer query, Compression compression) throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      send_execute_cql_query(query, compression);
      return recv_execute_cql_query();
    }
    public void send_execute_cql_query(ByteBuffer query, Compression compression) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("execute_cql_query", TMessageType.CALL, ++seqid_));
      execute_cql_query_args args = new execute_cql_query_args();
      args.setQuery(query);
      args.setCompression(compression);
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }
    public CqlResult recv_execute_cql_query() throws InvalidRequestException, UnavailableException, TimedOutException, TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      if (msg.seqid != seqid_) {
        throw new TApplicationException(TApplicationException.BAD_SEQUENCE_ID, "execute_cql_query failed: out of sequence response");
      }
      execute_cql_query_result result = new execute_cql_query_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      if (result.ire != null) {
        throw result.ire;
      }
      if (result.ue != null) {
        throw result.ue;
      }
      if (result.te != null) {
        throw result.te;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "execute_cql_query failed: unknown result");
    }
  }
  public static class AsyncClient extends TAsyncClient implements AsyncIface {
    public static class Factory implements TAsyncClientFactory<AsyncClient> {
      private TAsyncClientManager clientManager;
      private TProtocolFactory protocolFactory;
      public Factory(TAsyncClientManager clientManager, TProtocolFactory protocolFactory) {
        this.clientManager = clientManager;
        this.protocolFactory = protocolFactory;
      }
      public AsyncClient getAsyncClient(TNonblockingTransport transport) {
        return new AsyncClient(protocolFactory, clientManager, transport);
      }
    }
    public AsyncClient(TProtocolFactory protocolFactory, TAsyncClientManager clientManager, TNonblockingTransport transport) {
      super(protocolFactory, clientManager, transport);
    }
    public void login(AuthenticationRequest auth_request, AsyncMethodCallback<login_call> resultHandler) throws TException {
      checkReady();
      login_call method_call = new login_call(auth_request, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class login_call extends TAsyncMethodCall {
      private AuthenticationRequest auth_request;
      public login_call(AuthenticationRequest auth_request, AsyncMethodCallback<login_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.auth_request = auth_request;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("login", TMessageType.CALL, 0));
        login_args args = new login_args();
        args.setAuth_request(auth_request);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public void getResult() throws AuthenticationException, AuthorizationException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        (new Client(prot)).recv_login();
      }
    }
    public void set_keyspace(String keyspace, AsyncMethodCallback<set_keyspace_call> resultHandler) throws TException {
      checkReady();
      set_keyspace_call method_call = new set_keyspace_call(keyspace, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class set_keyspace_call extends TAsyncMethodCall {
      private String keyspace;
      public set_keyspace_call(String keyspace, AsyncMethodCallback<set_keyspace_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.keyspace = keyspace;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("set_keyspace", TMessageType.CALL, 0));
        set_keyspace_args args = new set_keyspace_args();
        args.setKeyspace(keyspace);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public void getResult() throws InvalidRequestException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        (new Client(prot)).recv_set_keyspace();
      }
    }
    public void get(ByteBuffer key, ColumnPath column_path, ConsistencyLevel consistency_level, AsyncMethodCallback<get_call> resultHandler) throws TException {
      checkReady();
      get_call method_call = new get_call(key, column_path, consistency_level, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class get_call extends TAsyncMethodCall {
      private ByteBuffer key;
      private ColumnPath column_path;
      private ConsistencyLevel consistency_level;
      public get_call(ByteBuffer key, ColumnPath column_path, ConsistencyLevel consistency_level, AsyncMethodCallback<get_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.key = key;
        this.column_path = column_path;
        this.consistency_level = consistency_level;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("get", TMessageType.CALL, 0));
        get_args args = new get_args();
        args.setKey(key);
        args.setColumn_path(column_path);
        args.setConsistency_level(consistency_level);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public ColumnOrSuperColumn getResult() throws InvalidRequestException, NotFoundException, UnavailableException, TimedOutException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_get();
      }
    }
    public void get_slice(ByteBuffer key, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<get_slice_call> resultHandler) throws TException {
      checkReady();
      get_slice_call method_call = new get_slice_call(key, column_parent, predicate, consistency_level, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class get_slice_call extends TAsyncMethodCall {
      private ByteBuffer key;
      private ColumnParent column_parent;
      private SlicePredicate predicate;
      private ConsistencyLevel consistency_level;
      public get_slice_call(ByteBuffer key, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<get_slice_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.key = key;
        this.column_parent = column_parent;
        this.predicate = predicate;
        this.consistency_level = consistency_level;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("get_slice", TMessageType.CALL, 0));
        get_slice_args args = new get_slice_args();
        args.setKey(key);
        args.setColumn_parent(column_parent);
        args.setPredicate(predicate);
        args.setConsistency_level(consistency_level);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public List<ColumnOrSuperColumn> getResult() throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_get_slice();
      }
    }
    public void get_count(ByteBuffer key, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<get_count_call> resultHandler) throws TException {
      checkReady();
      get_count_call method_call = new get_count_call(key, column_parent, predicate, consistency_level, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class get_count_call extends TAsyncMethodCall {
      private ByteBuffer key;
      private ColumnParent column_parent;
      private SlicePredicate predicate;
      private ConsistencyLevel consistency_level;
      public get_count_call(ByteBuffer key, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<get_count_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.key = key;
        this.column_parent = column_parent;
        this.predicate = predicate;
        this.consistency_level = consistency_level;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("get_count", TMessageType.CALL, 0));
        get_count_args args = new get_count_args();
        args.setKey(key);
        args.setColumn_parent(column_parent);
        args.setPredicate(predicate);
        args.setConsistency_level(consistency_level);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public int getResult() throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_get_count();
      }
    }
    public void multiget_slice(List<ByteBuffer> keys, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<multiget_slice_call> resultHandler) throws TException {
      checkReady();
      multiget_slice_call method_call = new multiget_slice_call(keys, column_parent, predicate, consistency_level, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class multiget_slice_call extends TAsyncMethodCall {
      private List<ByteBuffer> keys;
      private ColumnParent column_parent;
      private SlicePredicate predicate;
      private ConsistencyLevel consistency_level;
      public multiget_slice_call(List<ByteBuffer> keys, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<multiget_slice_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.keys = keys;
        this.column_parent = column_parent;
        this.predicate = predicate;
        this.consistency_level = consistency_level;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("multiget_slice", TMessageType.CALL, 0));
        multiget_slice_args args = new multiget_slice_args();
        args.setKeys(keys);
        args.setColumn_parent(column_parent);
        args.setPredicate(predicate);
        args.setConsistency_level(consistency_level);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public Map<ByteBuffer,List<ColumnOrSuperColumn>> getResult() throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_multiget_slice();
      }
    }
    public void multiget_count(List<ByteBuffer> keys, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<multiget_count_call> resultHandler) throws TException {
      checkReady();
      multiget_count_call method_call = new multiget_count_call(keys, column_parent, predicate, consistency_level, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class multiget_count_call extends TAsyncMethodCall {
      private List<ByteBuffer> keys;
      private ColumnParent column_parent;
      private SlicePredicate predicate;
      private ConsistencyLevel consistency_level;
      public multiget_count_call(List<ByteBuffer> keys, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<multiget_count_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.keys = keys;
        this.column_parent = column_parent;
        this.predicate = predicate;
        this.consistency_level = consistency_level;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("multiget_count", TMessageType.CALL, 0));
        multiget_count_args args = new multiget_count_args();
        args.setKeys(keys);
        args.setColumn_parent(column_parent);
        args.setPredicate(predicate);
        args.setConsistency_level(consistency_level);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public Map<ByteBuffer,Integer> getResult() throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_multiget_count();
      }
    }
    public void get_range_slices(ColumnParent column_parent, SlicePredicate predicate, KeyRange range, ConsistencyLevel consistency_level, AsyncMethodCallback<get_range_slices_call> resultHandler) throws TException {
      checkReady();
      get_range_slices_call method_call = new get_range_slices_call(column_parent, predicate, range, consistency_level, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class get_range_slices_call extends TAsyncMethodCall {
      private ColumnParent column_parent;
      private SlicePredicate predicate;
      private KeyRange range;
      private ConsistencyLevel consistency_level;
      public get_range_slices_call(ColumnParent column_parent, SlicePredicate predicate, KeyRange range, ConsistencyLevel consistency_level, AsyncMethodCallback<get_range_slices_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.column_parent = column_parent;
        this.predicate = predicate;
        this.range = range;
        this.consistency_level = consistency_level;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("get_range_slices", TMessageType.CALL, 0));
        get_range_slices_args args = new get_range_slices_args();
        args.setColumn_parent(column_parent);
        args.setPredicate(predicate);
        args.setRange(range);
        args.setConsistency_level(consistency_level);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public List<KeySlice> getResult() throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_get_range_slices();
      }
    }
    public void get_indexed_slices(ColumnParent column_parent, IndexClause index_clause, SlicePredicate column_predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<get_indexed_slices_call> resultHandler) throws TException {
      checkReady();
      get_indexed_slices_call method_call = new get_indexed_slices_call(column_parent, index_clause, column_predicate, consistency_level, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class get_indexed_slices_call extends TAsyncMethodCall {
      private ColumnParent column_parent;
      private IndexClause index_clause;
      private SlicePredicate column_predicate;
      private ConsistencyLevel consistency_level;
      public get_indexed_slices_call(ColumnParent column_parent, IndexClause index_clause, SlicePredicate column_predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<get_indexed_slices_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.column_parent = column_parent;
        this.index_clause = index_clause;
        this.column_predicate = column_predicate;
        this.consistency_level = consistency_level;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("get_indexed_slices", TMessageType.CALL, 0));
        get_indexed_slices_args args = new get_indexed_slices_args();
        args.setColumn_parent(column_parent);
        args.setIndex_clause(index_clause);
        args.setColumn_predicate(column_predicate);
        args.setConsistency_level(consistency_level);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public List<KeySlice> getResult() throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_get_indexed_slices();
      }
    }
    public void insert(ByteBuffer key, ColumnParent column_parent, Column column, ConsistencyLevel consistency_level, AsyncMethodCallback<insert_call> resultHandler) throws TException {
      checkReady();
      insert_call method_call = new insert_call(key, column_parent, column, consistency_level, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class insert_call extends TAsyncMethodCall {
      private ByteBuffer key;
      private ColumnParent column_parent;
      private Column column;
      private ConsistencyLevel consistency_level;
      public insert_call(ByteBuffer key, ColumnParent column_parent, Column column, ConsistencyLevel consistency_level, AsyncMethodCallback<insert_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.key = key;
        this.column_parent = column_parent;
        this.column = column;
        this.consistency_level = consistency_level;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("insert", TMessageType.CALL, 0));
        insert_args args = new insert_args();
        args.setKey(key);
        args.setColumn_parent(column_parent);
        args.setColumn(column);
        args.setConsistency_level(consistency_level);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public void getResult() throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        (new Client(prot)).recv_insert();
      }
    }
    public void remove(ByteBuffer key, ColumnPath column_path, long timestamp, ConsistencyLevel consistency_level, AsyncMethodCallback<remove_call> resultHandler) throws TException {
      checkReady();
      remove_call method_call = new remove_call(key, column_path, timestamp, consistency_level, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class remove_call extends TAsyncMethodCall {
      private ByteBuffer key;
      private ColumnPath column_path;
      private long timestamp;
      private ConsistencyLevel consistency_level;
      public remove_call(ByteBuffer key, ColumnPath column_path, long timestamp, ConsistencyLevel consistency_level, AsyncMethodCallback<remove_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.key = key;
        this.column_path = column_path;
        this.timestamp = timestamp;
        this.consistency_level = consistency_level;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("remove", TMessageType.CALL, 0));
        remove_args args = new remove_args();
        args.setKey(key);
        args.setColumn_path(column_path);
        args.setTimestamp(timestamp);
        args.setConsistency_level(consistency_level);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public void getResult() throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        (new Client(prot)).recv_remove();
      }
    }
    public void batch_mutate(Map<ByteBuffer,Map<String,List<Mutation>>> mutation_map, ConsistencyLevel consistency_level, AsyncMethodCallback<batch_mutate_call> resultHandler) throws TException {
      checkReady();
      batch_mutate_call method_call = new batch_mutate_call(mutation_map, consistency_level, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class batch_mutate_call extends TAsyncMethodCall {
      private Map<ByteBuffer,Map<String,List<Mutation>>> mutation_map;
      private ConsistencyLevel consistency_level;
      public batch_mutate_call(Map<ByteBuffer,Map<String,List<Mutation>>> mutation_map, ConsistencyLevel consistency_level, AsyncMethodCallback<batch_mutate_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.mutation_map = mutation_map;
        this.consistency_level = consistency_level;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("batch_mutate", TMessageType.CALL, 0));
        batch_mutate_args args = new batch_mutate_args();
        args.setMutation_map(mutation_map);
        args.setConsistency_level(consistency_level);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public void getResult() throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        (new Client(prot)).recv_batch_mutate();
      }
    }
    public void truncate(String cfname, AsyncMethodCallback<truncate_call> resultHandler) throws TException {
      checkReady();
      truncate_call method_call = new truncate_call(cfname, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class truncate_call extends TAsyncMethodCall {
      private String cfname;
      public truncate_call(String cfname, AsyncMethodCallback<truncate_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.cfname = cfname;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("truncate", TMessageType.CALL, 0));
        truncate_args args = new truncate_args();
        args.setCfname(cfname);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public void getResult() throws InvalidRequestException, UnavailableException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        (new Client(prot)).recv_truncate();
      }
    }
    public void add(ByteBuffer key, ColumnParent column_parent, CounterColumn column, ConsistencyLevel consistency_level, AsyncMethodCallback<add_call> resultHandler) throws TException {
      checkReady();
      add_call method_call = new add_call(key, column_parent, column, consistency_level, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class add_call extends TAsyncMethodCall {
      private ByteBuffer key;
      private ColumnParent column_parent;
      private CounterColumn column;
      private ConsistencyLevel consistency_level;
      public add_call(ByteBuffer key, ColumnParent column_parent, CounterColumn column, ConsistencyLevel consistency_level, AsyncMethodCallback<add_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.key = key;
        this.column_parent = column_parent;
        this.column = column;
        this.consistency_level = consistency_level;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("add", TMessageType.CALL, 0));
        add_args args = new add_args();
        args.setKey(key);
        args.setColumn_parent(column_parent);
        args.setColumn(column);
        args.setConsistency_level(consistency_level);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public void getResult() throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        (new Client(prot)).recv_add();
      }
    }
    public void batch_add(Map<ByteBuffer,Map<String,List<CounterMutation>>> update_map, ConsistencyLevel consistency_level, AsyncMethodCallback<batch_add_call> resultHandler) throws TException {
      checkReady();
      batch_add_call method_call = new batch_add_call(update_map, consistency_level, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class batch_add_call extends TAsyncMethodCall {
      private Map<ByteBuffer,Map<String,List<CounterMutation>>> update_map;
      private ConsistencyLevel consistency_level;
      public batch_add_call(Map<ByteBuffer,Map<String,List<CounterMutation>>> update_map, ConsistencyLevel consistency_level, AsyncMethodCallback<batch_add_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.update_map = update_map;
        this.consistency_level = consistency_level;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("batch_add", TMessageType.CALL, 0));
        batch_add_args args = new batch_add_args();
        args.setUpdate_map(update_map);
        args.setConsistency_level(consistency_level);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public void getResult() throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        (new Client(prot)).recv_batch_add();
      }
    }
    public void get_counter(ByteBuffer key, ColumnPath path, ConsistencyLevel consistency_level, AsyncMethodCallback<get_counter_call> resultHandler) throws TException {
      checkReady();
      get_counter_call method_call = new get_counter_call(key, path, consistency_level, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class get_counter_call extends TAsyncMethodCall {
      private ByteBuffer key;
      private ColumnPath path;
      private ConsistencyLevel consistency_level;
      public get_counter_call(ByteBuffer key, ColumnPath path, ConsistencyLevel consistency_level, AsyncMethodCallback<get_counter_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.key = key;
        this.path = path;
        this.consistency_level = consistency_level;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("get_counter", TMessageType.CALL, 0));
        get_counter_args args = new get_counter_args();
        args.setKey(key);
        args.setPath(path);
        args.setConsistency_level(consistency_level);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public Counter getResult() throws InvalidRequestException, NotFoundException, UnavailableException, TimedOutException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_get_counter();
      }
    }
    public void get_counter_slice(ByteBuffer key, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<get_counter_slice_call> resultHandler) throws TException {
      checkReady();
      get_counter_slice_call method_call = new get_counter_slice_call(key, column_parent, predicate, consistency_level, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class get_counter_slice_call extends TAsyncMethodCall {
      private ByteBuffer key;
      private ColumnParent column_parent;
      private SlicePredicate predicate;
      private ConsistencyLevel consistency_level;
      public get_counter_slice_call(ByteBuffer key, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<get_counter_slice_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.key = key;
        this.column_parent = column_parent;
        this.predicate = predicate;
        this.consistency_level = consistency_level;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("get_counter_slice", TMessageType.CALL, 0));
        get_counter_slice_args args = new get_counter_slice_args();
        args.setKey(key);
        args.setColumn_parent(column_parent);
        args.setPredicate(predicate);
        args.setConsistency_level(consistency_level);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public List<Counter> getResult() throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_get_counter_slice();
      }
    }
    public void multiget_counter_slice(List<ByteBuffer> keys, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<multiget_counter_slice_call> resultHandler) throws TException {
      checkReady();
      multiget_counter_slice_call method_call = new multiget_counter_slice_call(keys, column_parent, predicate, consistency_level, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class multiget_counter_slice_call extends TAsyncMethodCall {
      private List<ByteBuffer> keys;
      private ColumnParent column_parent;
      private SlicePredicate predicate;
      private ConsistencyLevel consistency_level;
      public multiget_counter_slice_call(List<ByteBuffer> keys, ColumnParent column_parent, SlicePredicate predicate, ConsistencyLevel consistency_level, AsyncMethodCallback<multiget_counter_slice_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.keys = keys;
        this.column_parent = column_parent;
        this.predicate = predicate;
        this.consistency_level = consistency_level;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("multiget_counter_slice", TMessageType.CALL, 0));
        multiget_counter_slice_args args = new multiget_counter_slice_args();
        args.setKeys(keys);
        args.setColumn_parent(column_parent);
        args.setPredicate(predicate);
        args.setConsistency_level(consistency_level);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public Map<ByteBuffer,List<Counter>> getResult() throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_multiget_counter_slice();
      }
    }
    public void remove_counter(ByteBuffer key, ColumnPath path, ConsistencyLevel consistency_level, AsyncMethodCallback<remove_counter_call> resultHandler) throws TException {
      checkReady();
      remove_counter_call method_call = new remove_counter_call(key, path, consistency_level, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class remove_counter_call extends TAsyncMethodCall {
      private ByteBuffer key;
      private ColumnPath path;
      private ConsistencyLevel consistency_level;
      public remove_counter_call(ByteBuffer key, ColumnPath path, ConsistencyLevel consistency_level, AsyncMethodCallback<remove_counter_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.key = key;
        this.path = path;
        this.consistency_level = consistency_level;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("remove_counter", TMessageType.CALL, 0));
        remove_counter_args args = new remove_counter_args();
        args.setKey(key);
        args.setPath(path);
        args.setConsistency_level(consistency_level);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public void getResult() throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        (new Client(prot)).recv_remove_counter();
      }
    }
    public void describe_schema_versions(AsyncMethodCallback<describe_schema_versions_call> resultHandler) throws TException {
      checkReady();
      describe_schema_versions_call method_call = new describe_schema_versions_call(resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class describe_schema_versions_call extends TAsyncMethodCall {
      public describe_schema_versions_call(AsyncMethodCallback<describe_schema_versions_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("describe_schema_versions", TMessageType.CALL, 0));
        describe_schema_versions_args args = new describe_schema_versions_args();
        args.write(prot);
        prot.writeMessageEnd();
      }
      public Map<String,List<String>> getResult() throws InvalidRequestException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_describe_schema_versions();
      }
    }
    public void describe_keyspaces(AsyncMethodCallback<describe_keyspaces_call> resultHandler) throws TException {
      checkReady();
      describe_keyspaces_call method_call = new describe_keyspaces_call(resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class describe_keyspaces_call extends TAsyncMethodCall {
      public describe_keyspaces_call(AsyncMethodCallback<describe_keyspaces_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("describe_keyspaces", TMessageType.CALL, 0));
        describe_keyspaces_args args = new describe_keyspaces_args();
        args.write(prot);
        prot.writeMessageEnd();
      }
      public List<KsDef> getResult() throws InvalidRequestException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_describe_keyspaces();
      }
    }
    public void describe_cluster_name(AsyncMethodCallback<describe_cluster_name_call> resultHandler) throws TException {
      checkReady();
      describe_cluster_name_call method_call = new describe_cluster_name_call(resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class describe_cluster_name_call extends TAsyncMethodCall {
      public describe_cluster_name_call(AsyncMethodCallback<describe_cluster_name_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("describe_cluster_name", TMessageType.CALL, 0));
        describe_cluster_name_args args = new describe_cluster_name_args();
        args.write(prot);
        prot.writeMessageEnd();
      }
      public String getResult() throws TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_describe_cluster_name();
      }
    }
    public void describe_version(AsyncMethodCallback<describe_version_call> resultHandler) throws TException {
      checkReady();
      describe_version_call method_call = new describe_version_call(resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class describe_version_call extends TAsyncMethodCall {
      public describe_version_call(AsyncMethodCallback<describe_version_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("describe_version", TMessageType.CALL, 0));
        describe_version_args args = new describe_version_args();
        args.write(prot);
        prot.writeMessageEnd();
      }
      public String getResult() throws TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_describe_version();
      }
    }
    public void describe_ring(String keyspace, AsyncMethodCallback<describe_ring_call> resultHandler) throws TException {
      checkReady();
      describe_ring_call method_call = new describe_ring_call(keyspace, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class describe_ring_call extends TAsyncMethodCall {
      private String keyspace;
      public describe_ring_call(String keyspace, AsyncMethodCallback<describe_ring_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.keyspace = keyspace;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("describe_ring", TMessageType.CALL, 0));
        describe_ring_args args = new describe_ring_args();
        args.setKeyspace(keyspace);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public List<TokenRange> getResult() throws InvalidRequestException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_describe_ring();
      }
    }
    public void describe_partitioner(AsyncMethodCallback<describe_partitioner_call> resultHandler) throws TException {
      checkReady();
      describe_partitioner_call method_call = new describe_partitioner_call(resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class describe_partitioner_call extends TAsyncMethodCall {
      public describe_partitioner_call(AsyncMethodCallback<describe_partitioner_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("describe_partitioner", TMessageType.CALL, 0));
        describe_partitioner_args args = new describe_partitioner_args();
        args.write(prot);
        prot.writeMessageEnd();
      }
      public String getResult() throws TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_describe_partitioner();
      }
    }
    public void describe_snitch(AsyncMethodCallback<describe_snitch_call> resultHandler) throws TException {
      checkReady();
      describe_snitch_call method_call = new describe_snitch_call(resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class describe_snitch_call extends TAsyncMethodCall {
      public describe_snitch_call(AsyncMethodCallback<describe_snitch_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("describe_snitch", TMessageType.CALL, 0));
        describe_snitch_args args = new describe_snitch_args();
        args.write(prot);
        prot.writeMessageEnd();
      }
      public String getResult() throws TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_describe_snitch();
      }
    }
    public void describe_keyspace(String keyspace, AsyncMethodCallback<describe_keyspace_call> resultHandler) throws TException {
      checkReady();
      describe_keyspace_call method_call = new describe_keyspace_call(keyspace, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class describe_keyspace_call extends TAsyncMethodCall {
      private String keyspace;
      public describe_keyspace_call(String keyspace, AsyncMethodCallback<describe_keyspace_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.keyspace = keyspace;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("describe_keyspace", TMessageType.CALL, 0));
        describe_keyspace_args args = new describe_keyspace_args();
        args.setKeyspace(keyspace);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public KsDef getResult() throws NotFoundException, InvalidRequestException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_describe_keyspace();
      }
    }
    public void describe_splits(String cfName, String start_token, String end_token, int keys_per_split, AsyncMethodCallback<describe_splits_call> resultHandler) throws TException {
      checkReady();
      describe_splits_call method_call = new describe_splits_call(cfName, start_token, end_token, keys_per_split, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class describe_splits_call extends TAsyncMethodCall {
      private String cfName;
      private String start_token;
      private String end_token;
      private int keys_per_split;
      public describe_splits_call(String cfName, String start_token, String end_token, int keys_per_split, AsyncMethodCallback<describe_splits_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.cfName = cfName;
        this.start_token = start_token;
        this.end_token = end_token;
        this.keys_per_split = keys_per_split;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("describe_splits", TMessageType.CALL, 0));
        describe_splits_args args = new describe_splits_args();
        args.setCfName(cfName);
        args.setStart_token(start_token);
        args.setEnd_token(end_token);
        args.setKeys_per_split(keys_per_split);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public List<String> getResult() throws TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_describe_splits();
      }
    }
    public void system_add_column_family(CfDef cf_def, AsyncMethodCallback<system_add_column_family_call> resultHandler) throws TException {
      checkReady();
      system_add_column_family_call method_call = new system_add_column_family_call(cf_def, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class system_add_column_family_call extends TAsyncMethodCall {
      private CfDef cf_def;
      public system_add_column_family_call(CfDef cf_def, AsyncMethodCallback<system_add_column_family_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.cf_def = cf_def;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("system_add_column_family", TMessageType.CALL, 0));
        system_add_column_family_args args = new system_add_column_family_args();
        args.setCf_def(cf_def);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public String getResult() throws InvalidRequestException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_system_add_column_family();
      }
    }
    public void system_drop_column_family(String column_family, AsyncMethodCallback<system_drop_column_family_call> resultHandler) throws TException {
      checkReady();
      system_drop_column_family_call method_call = new system_drop_column_family_call(column_family, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class system_drop_column_family_call extends TAsyncMethodCall {
      private String column_family;
      public system_drop_column_family_call(String column_family, AsyncMethodCallback<system_drop_column_family_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.column_family = column_family;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("system_drop_column_family", TMessageType.CALL, 0));
        system_drop_column_family_args args = new system_drop_column_family_args();
        args.setColumn_family(column_family);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public String getResult() throws InvalidRequestException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_system_drop_column_family();
      }
    }
    public void system_add_keyspace(KsDef ks_def, AsyncMethodCallback<system_add_keyspace_call> resultHandler) throws TException {
      checkReady();
      system_add_keyspace_call method_call = new system_add_keyspace_call(ks_def, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class system_add_keyspace_call extends TAsyncMethodCall {
      private KsDef ks_def;
      public system_add_keyspace_call(KsDef ks_def, AsyncMethodCallback<system_add_keyspace_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.ks_def = ks_def;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("system_add_keyspace", TMessageType.CALL, 0));
        system_add_keyspace_args args = new system_add_keyspace_args();
        args.setKs_def(ks_def);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public String getResult() throws InvalidRequestException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_system_add_keyspace();
      }
    }
    public void system_drop_keyspace(String keyspace, AsyncMethodCallback<system_drop_keyspace_call> resultHandler) throws TException {
      checkReady();
      system_drop_keyspace_call method_call = new system_drop_keyspace_call(keyspace, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class system_drop_keyspace_call extends TAsyncMethodCall {
      private String keyspace;
      public system_drop_keyspace_call(String keyspace, AsyncMethodCallback<system_drop_keyspace_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.keyspace = keyspace;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("system_drop_keyspace", TMessageType.CALL, 0));
        system_drop_keyspace_args args = new system_drop_keyspace_args();
        args.setKeyspace(keyspace);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public String getResult() throws InvalidRequestException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_system_drop_keyspace();
      }
    }
    public void system_update_keyspace(KsDef ks_def, AsyncMethodCallback<system_update_keyspace_call> resultHandler) throws TException {
      checkReady();
      system_update_keyspace_call method_call = new system_update_keyspace_call(ks_def, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class system_update_keyspace_call extends TAsyncMethodCall {
      private KsDef ks_def;
      public system_update_keyspace_call(KsDef ks_def, AsyncMethodCallback<system_update_keyspace_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.ks_def = ks_def;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("system_update_keyspace", TMessageType.CALL, 0));
        system_update_keyspace_args args = new system_update_keyspace_args();
        args.setKs_def(ks_def);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public String getResult() throws InvalidRequestException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_system_update_keyspace();
      }
    }
    public void system_update_column_family(CfDef cf_def, AsyncMethodCallback<system_update_column_family_call> resultHandler) throws TException {
      checkReady();
      system_update_column_family_call method_call = new system_update_column_family_call(cf_def, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class system_update_column_family_call extends TAsyncMethodCall {
      private CfDef cf_def;
      public system_update_column_family_call(CfDef cf_def, AsyncMethodCallback<system_update_column_family_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.cf_def = cf_def;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("system_update_column_family", TMessageType.CALL, 0));
        system_update_column_family_args args = new system_update_column_family_args();
        args.setCf_def(cf_def);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public String getResult() throws InvalidRequestException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_system_update_column_family();
      }
    }
    public void execute_cql_query(ByteBuffer query, Compression compression, AsyncMethodCallback<execute_cql_query_call> resultHandler) throws TException {
      checkReady();
      execute_cql_query_call method_call = new execute_cql_query_call(query, compression, resultHandler, this, protocolFactory, transport);
      manager.call(method_call);
    }
    public static class execute_cql_query_call extends TAsyncMethodCall {
      private ByteBuffer query;
      private Compression compression;
      public execute_cql_query_call(ByteBuffer query, Compression compression, AsyncMethodCallback<execute_cql_query_call> resultHandler, TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport) throws TException {
        super(client, protocolFactory, transport, resultHandler, false);
        this.query = query;
        this.compression = compression;
      }
      public void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new TMessage("execute_cql_query", TMessageType.CALL, 0));
        execute_cql_query_args args = new execute_cql_query_args();
        args.setQuery(query);
        args.setCompression(compression);
        args.write(prot);
        prot.writeMessageEnd();
      }
      public CqlResult getResult() throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        if (getState() != State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        TMemoryInputTransport memoryTransport = new TMemoryInputTransport(getFrameBuffer().array());
        TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        return (new Client(prot)).recv_execute_cql_query();
      }
    }
  }
  public static class Processor implements TProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class.getName());
    public Processor(Iface iface)
    {
      iface_ = iface;
      processMap_.put("login", new login());
      processMap_.put("set_keyspace", new set_keyspace());
      processMap_.put("get", new get());
      processMap_.put("get_slice", new get_slice());
      processMap_.put("get_count", new get_count());
      processMap_.put("multiget_slice", new multiget_slice());
      processMap_.put("multiget_count", new multiget_count());
      processMap_.put("get_range_slices", new get_range_slices());
      processMap_.put("get_indexed_slices", new get_indexed_slices());
      processMap_.put("insert", new insert());
      processMap_.put("remove", new remove());
      processMap_.put("batch_mutate", new batch_mutate());
      processMap_.put("truncate", new truncate());
      processMap_.put("add", new add());
      processMap_.put("batch_add", new batch_add());
      processMap_.put("get_counter", new get_counter());
      processMap_.put("get_counter_slice", new get_counter_slice());
      processMap_.put("multiget_counter_slice", new multiget_counter_slice());
      processMap_.put("remove_counter", new remove_counter());
      processMap_.put("describe_schema_versions", new describe_schema_versions());
      processMap_.put("describe_keyspaces", new describe_keyspaces());
      processMap_.put("describe_cluster_name", new describe_cluster_name());
      processMap_.put("describe_version", new describe_version());
      processMap_.put("describe_ring", new describe_ring());
      processMap_.put("describe_partitioner", new describe_partitioner());
      processMap_.put("describe_snitch", new describe_snitch());
      processMap_.put("describe_keyspace", new describe_keyspace());
      processMap_.put("describe_splits", new describe_splits());
      processMap_.put("system_add_column_family", new system_add_column_family());
      processMap_.put("system_drop_column_family", new system_drop_column_family());
      processMap_.put("system_add_keyspace", new system_add_keyspace());
      processMap_.put("system_drop_keyspace", new system_drop_keyspace());
      processMap_.put("system_update_keyspace", new system_update_keyspace());
      processMap_.put("system_update_column_family", new system_update_column_family());
      processMap_.put("execute_cql_query", new execute_cql_query());
    }
    protected static interface ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException;
    }
    private Iface iface_;
    protected final HashMap<String,ProcessFunction> processMap_ = new HashMap<String,ProcessFunction>();
    public boolean process(TProtocol iprot, TProtocol oprot) throws TException
    {
      TMessage msg = iprot.readMessageBegin();
      ProcessFunction fn = processMap_.get(msg.name);
      if (fn == null) {
        TProtocolUtil.skip(iprot, TType.STRUCT);
        iprot.readMessageEnd();
        TApplicationException x = new TApplicationException(TApplicationException.UNKNOWN_METHOD, "Invalid method name: '"+msg.name+"'");
        oprot.writeMessageBegin(new TMessage(msg.name, TMessageType.EXCEPTION, msg.seqid));
        x.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
        return true;
      }
      fn.process(msg.seqid, iprot, oprot);
      return true;
    }
    private class login implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        login_args args = new login_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("login", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        login_result result = new login_result();
        try {
          iface_.login(args.auth_request);
        } catch (AuthenticationException authnx) {
          result.authnx = authnx;
        } catch (AuthorizationException authzx) {
          result.authzx = authzx;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing login", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing login");
          oprot.writeMessageBegin(new TMessage("login", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("login", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class set_keyspace implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        set_keyspace_args args = new set_keyspace_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("set_keyspace", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        set_keyspace_result result = new set_keyspace_result();
        try {
          iface_.set_keyspace(args.keyspace);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing set_keyspace", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing set_keyspace");
          oprot.writeMessageBegin(new TMessage("set_keyspace", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("set_keyspace", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class get implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        get_args args = new get_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("get", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        get_result result = new get_result();
        try {
          result.success = iface_.get(args.key, args.column_path, args.consistency_level);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (NotFoundException nfe) {
          result.nfe = nfe;
        } catch (UnavailableException ue) {
          result.ue = ue;
        } catch (TimedOutException te) {
          result.te = te;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing get", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing get");
          oprot.writeMessageBegin(new TMessage("get", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("get", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class get_slice implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        get_slice_args args = new get_slice_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("get_slice", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        get_slice_result result = new get_slice_result();
        try {
          result.success = iface_.get_slice(args.key, args.column_parent, args.predicate, args.consistency_level);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (UnavailableException ue) {
          result.ue = ue;
        } catch (TimedOutException te) {
          result.te = te;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing get_slice", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing get_slice");
          oprot.writeMessageBegin(new TMessage("get_slice", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("get_slice", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class get_count implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        get_count_args args = new get_count_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("get_count", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        get_count_result result = new get_count_result();
        try {
          result.success = iface_.get_count(args.key, args.column_parent, args.predicate, args.consistency_level);
          result.setSuccessIsSet(true);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (UnavailableException ue) {
          result.ue = ue;
        } catch (TimedOutException te) {
          result.te = te;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing get_count", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing get_count");
          oprot.writeMessageBegin(new TMessage("get_count", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("get_count", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class multiget_slice implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        multiget_slice_args args = new multiget_slice_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("multiget_slice", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        multiget_slice_result result = new multiget_slice_result();
        try {
          result.success = iface_.multiget_slice(args.keys, args.column_parent, args.predicate, args.consistency_level);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (UnavailableException ue) {
          result.ue = ue;
        } catch (TimedOutException te) {
          result.te = te;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing multiget_slice", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing multiget_slice");
          oprot.writeMessageBegin(new TMessage("multiget_slice", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("multiget_slice", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class multiget_count implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        multiget_count_args args = new multiget_count_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("multiget_count", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        multiget_count_result result = new multiget_count_result();
        try {
          result.success = iface_.multiget_count(args.keys, args.column_parent, args.predicate, args.consistency_level);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (UnavailableException ue) {
          result.ue = ue;
        } catch (TimedOutException te) {
          result.te = te;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing multiget_count", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing multiget_count");
          oprot.writeMessageBegin(new TMessage("multiget_count", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("multiget_count", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class get_range_slices implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        get_range_slices_args args = new get_range_slices_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("get_range_slices", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        get_range_slices_result result = new get_range_slices_result();
        try {
          result.success = iface_.get_range_slices(args.column_parent, args.predicate, args.range, args.consistency_level);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (UnavailableException ue) {
          result.ue = ue;
        } catch (TimedOutException te) {
          result.te = te;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing get_range_slices", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing get_range_slices");
          oprot.writeMessageBegin(new TMessage("get_range_slices", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("get_range_slices", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class get_indexed_slices implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        get_indexed_slices_args args = new get_indexed_slices_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("get_indexed_slices", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        get_indexed_slices_result result = new get_indexed_slices_result();
        try {
          result.success = iface_.get_indexed_slices(args.column_parent, args.index_clause, args.column_predicate, args.consistency_level);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (UnavailableException ue) {
          result.ue = ue;
        } catch (TimedOutException te) {
          result.te = te;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing get_indexed_slices", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing get_indexed_slices");
          oprot.writeMessageBegin(new TMessage("get_indexed_slices", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("get_indexed_slices", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class insert implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        insert_args args = new insert_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("insert", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        insert_result result = new insert_result();
        try {
          iface_.insert(args.key, args.column_parent, args.column, args.consistency_level);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (UnavailableException ue) {
          result.ue = ue;
        } catch (TimedOutException te) {
          result.te = te;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing insert", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing insert");
          oprot.writeMessageBegin(new TMessage("insert", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("insert", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class remove implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        remove_args args = new remove_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("remove", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        remove_result result = new remove_result();
        try {
          iface_.remove(args.key, args.column_path, args.timestamp, args.consistency_level);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (UnavailableException ue) {
          result.ue = ue;
        } catch (TimedOutException te) {
          result.te = te;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing remove", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing remove");
          oprot.writeMessageBegin(new TMessage("remove", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("remove", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class batch_mutate implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        batch_mutate_args args = new batch_mutate_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("batch_mutate", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        batch_mutate_result result = new batch_mutate_result();
        try {
          iface_.batch_mutate(args.mutation_map, args.consistency_level);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (UnavailableException ue) {
          result.ue = ue;
        } catch (TimedOutException te) {
          result.te = te;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing batch_mutate", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing batch_mutate");
          oprot.writeMessageBegin(new TMessage("batch_mutate", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("batch_mutate", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class truncate implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        truncate_args args = new truncate_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("truncate", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        truncate_result result = new truncate_result();
        try {
          iface_.truncate(args.cfname);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (UnavailableException ue) {
          result.ue = ue;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing truncate", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing truncate");
          oprot.writeMessageBegin(new TMessage("truncate", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("truncate", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class add implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        add_args args = new add_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("add", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        add_result result = new add_result();
        try {
          iface_.add(args.key, args.column_parent, args.column, args.consistency_level);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (UnavailableException ue) {
          result.ue = ue;
        } catch (TimedOutException te) {
          result.te = te;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing add", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing add");
          oprot.writeMessageBegin(new TMessage("add", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("add", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class batch_add implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        batch_add_args args = new batch_add_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("batch_add", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        batch_add_result result = new batch_add_result();
        try {
          iface_.batch_add(args.update_map, args.consistency_level);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (UnavailableException ue) {
          result.ue = ue;
        } catch (TimedOutException te) {
          result.te = te;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing batch_add", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing batch_add");
          oprot.writeMessageBegin(new TMessage("batch_add", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("batch_add", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class get_counter implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        get_counter_args args = new get_counter_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("get_counter", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        get_counter_result result = new get_counter_result();
        try {
          result.success = iface_.get_counter(args.key, args.path, args.consistency_level);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (NotFoundException nfe) {
          result.nfe = nfe;
        } catch (UnavailableException ue) {
          result.ue = ue;
        } catch (TimedOutException te) {
          result.te = te;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing get_counter", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing get_counter");
          oprot.writeMessageBegin(new TMessage("get_counter", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("get_counter", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class get_counter_slice implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        get_counter_slice_args args = new get_counter_slice_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("get_counter_slice", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        get_counter_slice_result result = new get_counter_slice_result();
        try {
          result.success = iface_.get_counter_slice(args.key, args.column_parent, args.predicate, args.consistency_level);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (UnavailableException ue) {
          result.ue = ue;
        } catch (TimedOutException te) {
          result.te = te;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing get_counter_slice", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing get_counter_slice");
          oprot.writeMessageBegin(new TMessage("get_counter_slice", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("get_counter_slice", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class multiget_counter_slice implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        multiget_counter_slice_args args = new multiget_counter_slice_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("multiget_counter_slice", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        multiget_counter_slice_result result = new multiget_counter_slice_result();
        try {
          result.success = iface_.multiget_counter_slice(args.keys, args.column_parent, args.predicate, args.consistency_level);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (UnavailableException ue) {
          result.ue = ue;
        } catch (TimedOutException te) {
          result.te = te;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing multiget_counter_slice", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing multiget_counter_slice");
          oprot.writeMessageBegin(new TMessage("multiget_counter_slice", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("multiget_counter_slice", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class remove_counter implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        remove_counter_args args = new remove_counter_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("remove_counter", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        remove_counter_result result = new remove_counter_result();
        try {
          iface_.remove_counter(args.key, args.path, args.consistency_level);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (UnavailableException ue) {
          result.ue = ue;
        } catch (TimedOutException te) {
          result.te = te;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing remove_counter", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing remove_counter");
          oprot.writeMessageBegin(new TMessage("remove_counter", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("remove_counter", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class describe_schema_versions implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        describe_schema_versions_args args = new describe_schema_versions_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("describe_schema_versions", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        describe_schema_versions_result result = new describe_schema_versions_result();
        try {
          result.success = iface_.describe_schema_versions();
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing describe_schema_versions", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing describe_schema_versions");
          oprot.writeMessageBegin(new TMessage("describe_schema_versions", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("describe_schema_versions", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class describe_keyspaces implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        describe_keyspaces_args args = new describe_keyspaces_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("describe_keyspaces", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        describe_keyspaces_result result = new describe_keyspaces_result();
        try {
          result.success = iface_.describe_keyspaces();
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing describe_keyspaces", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing describe_keyspaces");
          oprot.writeMessageBegin(new TMessage("describe_keyspaces", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("describe_keyspaces", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class describe_cluster_name implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        describe_cluster_name_args args = new describe_cluster_name_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("describe_cluster_name", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        describe_cluster_name_result result = new describe_cluster_name_result();
        result.success = iface_.describe_cluster_name();
        oprot.writeMessageBegin(new TMessage("describe_cluster_name", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class describe_version implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        describe_version_args args = new describe_version_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("describe_version", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        describe_version_result result = new describe_version_result();
        result.success = iface_.describe_version();
        oprot.writeMessageBegin(new TMessage("describe_version", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class describe_ring implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        describe_ring_args args = new describe_ring_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("describe_ring", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        describe_ring_result result = new describe_ring_result();
        try {
          result.success = iface_.describe_ring(args.keyspace);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing describe_ring", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing describe_ring");
          oprot.writeMessageBegin(new TMessage("describe_ring", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("describe_ring", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class describe_partitioner implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        describe_partitioner_args args = new describe_partitioner_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("describe_partitioner", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        describe_partitioner_result result = new describe_partitioner_result();
        result.success = iface_.describe_partitioner();
        oprot.writeMessageBegin(new TMessage("describe_partitioner", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class describe_snitch implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        describe_snitch_args args = new describe_snitch_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("describe_snitch", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        describe_snitch_result result = new describe_snitch_result();
        result.success = iface_.describe_snitch();
        oprot.writeMessageBegin(new TMessage("describe_snitch", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class describe_keyspace implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        describe_keyspace_args args = new describe_keyspace_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("describe_keyspace", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        describe_keyspace_result result = new describe_keyspace_result();
        try {
          result.success = iface_.describe_keyspace(args.keyspace);
        } catch (NotFoundException nfe) {
          result.nfe = nfe;
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing describe_keyspace", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing describe_keyspace");
          oprot.writeMessageBegin(new TMessage("describe_keyspace", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("describe_keyspace", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class describe_splits implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        describe_splits_args args = new describe_splits_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("describe_splits", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        describe_splits_result result = new describe_splits_result();
        result.success = iface_.describe_splits(args.cfName, args.start_token, args.end_token, args.keys_per_split);
        oprot.writeMessageBegin(new TMessage("describe_splits", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class system_add_column_family implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        system_add_column_family_args args = new system_add_column_family_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("system_add_column_family", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        system_add_column_family_result result = new system_add_column_family_result();
        try {
          result.success = iface_.system_add_column_family(args.cf_def);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing system_add_column_family", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing system_add_column_family");
          oprot.writeMessageBegin(new TMessage("system_add_column_family", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("system_add_column_family", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class system_drop_column_family implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        system_drop_column_family_args args = new system_drop_column_family_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("system_drop_column_family", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        system_drop_column_family_result result = new system_drop_column_family_result();
        try {
          result.success = iface_.system_drop_column_family(args.column_family);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing system_drop_column_family", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing system_drop_column_family");
          oprot.writeMessageBegin(new TMessage("system_drop_column_family", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("system_drop_column_family", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class system_add_keyspace implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        system_add_keyspace_args args = new system_add_keyspace_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("system_add_keyspace", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        system_add_keyspace_result result = new system_add_keyspace_result();
        try {
          result.success = iface_.system_add_keyspace(args.ks_def);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing system_add_keyspace", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing system_add_keyspace");
          oprot.writeMessageBegin(new TMessage("system_add_keyspace", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("system_add_keyspace", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class system_drop_keyspace implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        system_drop_keyspace_args args = new system_drop_keyspace_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("system_drop_keyspace", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        system_drop_keyspace_result result = new system_drop_keyspace_result();
        try {
          result.success = iface_.system_drop_keyspace(args.keyspace);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing system_drop_keyspace", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing system_drop_keyspace");
          oprot.writeMessageBegin(new TMessage("system_drop_keyspace", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("system_drop_keyspace", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class system_update_keyspace implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        system_update_keyspace_args args = new system_update_keyspace_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("system_update_keyspace", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        system_update_keyspace_result result = new system_update_keyspace_result();
        try {
          result.success = iface_.system_update_keyspace(args.ks_def);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing system_update_keyspace", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing system_update_keyspace");
          oprot.writeMessageBegin(new TMessage("system_update_keyspace", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("system_update_keyspace", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class system_update_column_family implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        system_update_column_family_args args = new system_update_column_family_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("system_update_column_family", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        system_update_column_family_result result = new system_update_column_family_result();
        try {
          result.success = iface_.system_update_column_family(args.cf_def);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing system_update_column_family", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing system_update_column_family");
          oprot.writeMessageBegin(new TMessage("system_update_column_family", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("system_update_column_family", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
    private class execute_cql_query implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        execute_cql_query_args args = new execute_cql_query_args();
        try {
          args.read(iprot);
        } catch (TProtocolException e) {
          iprot.readMessageEnd();
          TApplicationException x = new TApplicationException(TApplicationException.PROTOCOL_ERROR, e.getMessage());
          oprot.writeMessageBegin(new TMessage("execute_cql_query", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        iprot.readMessageEnd();
        execute_cql_query_result result = new execute_cql_query_result();
        try {
          result.success = iface_.execute_cql_query(args.query, args.compression);
        } catch (InvalidRequestException ire) {
          result.ire = ire;
        } catch (UnavailableException ue) {
          result.ue = ue;
        } catch (TimedOutException te) {
          result.te = te;
        } catch (Throwable th) {
          LOGGER.error("Internal error processing execute_cql_query", th);
          TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR, "Internal error processing execute_cql_query");
          oprot.writeMessageBegin(new TMessage("execute_cql_query", TMessageType.EXCEPTION, seqid));
          x.write(oprot);
          oprot.writeMessageEnd();
          oprot.getTransport().flush();
          return;
        }
        oprot.writeMessageBegin(new TMessage("execute_cql_query", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }
    }
  }
  public static class login_args implements TBase<login_args, login_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("login_args");
    private static final TField AUTH_REQUEST_FIELD_DESC = new TField("auth_request", TType.STRUCT, (short)1);
    public AuthenticationRequest auth_request;
    public enum _Fields implements TFieldIdEnum {
      AUTH_REQUEST((short)1, "auth_request");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return AUTH_REQUEST;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.AUTH_REQUEST, new FieldMetaData("auth_request", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, AuthenticationRequest.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(login_args.class, metaDataMap);
    }
    public login_args() {
    }
    public login_args(
      AuthenticationRequest auth_request)
    {
      this();
      this.auth_request = auth_request;
    }
    public login_args(login_args other) {
      if (other.isSetAuth_request()) {
        this.auth_request = new AuthenticationRequest(other.auth_request);
      }
    }
    public login_args deepCopy() {
      return new login_args(this);
    }
    @Override
    public void clear() {
      this.auth_request = null;
    }
    public AuthenticationRequest getAuth_request() {
      return this.auth_request;
    }
    public login_args setAuth_request(AuthenticationRequest auth_request) {
      this.auth_request = auth_request;
      return this;
    }
    public void unsetAuth_request() {
      this.auth_request = null;
    }
    public boolean isSetAuth_request() {
      return this.auth_request != null;
    }
    public void setAuth_requestIsSet(boolean value) {
      if (!value) {
        this.auth_request = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case AUTH_REQUEST:
        if (value == null) {
          unsetAuth_request();
        } else {
          setAuth_request((AuthenticationRequest)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case AUTH_REQUEST:
        return getAuth_request();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case AUTH_REQUEST:
        return isSetAuth_request();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof login_args)
        return this.equals((login_args)that);
      return false;
    }
    public boolean equals(login_args that) {
      if (that == null)
        return false;
      boolean this_present_auth_request = true && this.isSetAuth_request();
      boolean that_present_auth_request = true && that.isSetAuth_request();
      if (this_present_auth_request || that_present_auth_request) {
        if (!(this_present_auth_request && that_present_auth_request))
          return false;
        if (!this.auth_request.equals(that.auth_request))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_auth_request = true && (isSetAuth_request());
      builder.append(present_auth_request);
      if (present_auth_request)
        builder.append(auth_request);
      return builder.toHashCode();
    }
    public int compareTo(login_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      login_args typedOther = (login_args)other;
      lastComparison = Boolean.valueOf(isSetAuth_request()).compareTo(typedOther.isSetAuth_request());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetAuth_request()) {
        lastComparison = TBaseHelper.compareTo(this.auth_request, typedOther.auth_request);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRUCT) {
              this.auth_request = new AuthenticationRequest();
              this.auth_request.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.auth_request != null) {
        oprot.writeFieldBegin(AUTH_REQUEST_FIELD_DESC);
        this.auth_request.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("login_args(");
      boolean first = true;
      sb.append("auth_request:");
      if (this.auth_request == null) {
        sb.append("null");
      } else {
        sb.append(this.auth_request);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (auth_request == null) {
        throw new TProtocolException("Required field 'auth_request' was not present! Struct: " + toString());
      }
    }
  }
  public static class login_result implements TBase<login_result, login_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("login_result");
    private static final TField AUTHNX_FIELD_DESC = new TField("authnx", TType.STRUCT, (short)1);
    private static final TField AUTHZX_FIELD_DESC = new TField("authzx", TType.STRUCT, (short)2);
    public AuthenticationException authnx;
    public AuthorizationException authzx;
    public enum _Fields implements TFieldIdEnum {
      AUTHNX((short)1, "authnx"),
      AUTHZX((short)2, "authzx");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return AUTHNX;
          case 2: 
            return AUTHZX;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.AUTHNX, new FieldMetaData("authnx", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.AUTHZX, new FieldMetaData("authzx", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(login_result.class, metaDataMap);
    }
    public login_result() {
    }
    public login_result(
      AuthenticationException authnx,
      AuthorizationException authzx)
    {
      this();
      this.authnx = authnx;
      this.authzx = authzx;
    }
    public login_result(login_result other) {
      if (other.isSetAuthnx()) {
        this.authnx = new AuthenticationException(other.authnx);
      }
      if (other.isSetAuthzx()) {
        this.authzx = new AuthorizationException(other.authzx);
      }
    }
    public login_result deepCopy() {
      return new login_result(this);
    }
    @Override
    public void clear() {
      this.authnx = null;
      this.authzx = null;
    }
    public AuthenticationException getAuthnx() {
      return this.authnx;
    }
    public login_result setAuthnx(AuthenticationException authnx) {
      this.authnx = authnx;
      return this;
    }
    public void unsetAuthnx() {
      this.authnx = null;
    }
    public boolean isSetAuthnx() {
      return this.authnx != null;
    }
    public void setAuthnxIsSet(boolean value) {
      if (!value) {
        this.authnx = null;
      }
    }
    public AuthorizationException getAuthzx() {
      return this.authzx;
    }
    public login_result setAuthzx(AuthorizationException authzx) {
      this.authzx = authzx;
      return this;
    }
    public void unsetAuthzx() {
      this.authzx = null;
    }
    public boolean isSetAuthzx() {
      return this.authzx != null;
    }
    public void setAuthzxIsSet(boolean value) {
      if (!value) {
        this.authzx = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case AUTHNX:
        if (value == null) {
          unsetAuthnx();
        } else {
          setAuthnx((AuthenticationException)value);
        }
        break;
      case AUTHZX:
        if (value == null) {
          unsetAuthzx();
        } else {
          setAuthzx((AuthorizationException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case AUTHNX:
        return getAuthnx();
      case AUTHZX:
        return getAuthzx();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case AUTHNX:
        return isSetAuthnx();
      case AUTHZX:
        return isSetAuthzx();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof login_result)
        return this.equals((login_result)that);
      return false;
    }
    public boolean equals(login_result that) {
      if (that == null)
        return false;
      boolean this_present_authnx = true && this.isSetAuthnx();
      boolean that_present_authnx = true && that.isSetAuthnx();
      if (this_present_authnx || that_present_authnx) {
        if (!(this_present_authnx && that_present_authnx))
          return false;
        if (!this.authnx.equals(that.authnx))
          return false;
      }
      boolean this_present_authzx = true && this.isSetAuthzx();
      boolean that_present_authzx = true && that.isSetAuthzx();
      if (this_present_authzx || that_present_authzx) {
        if (!(this_present_authzx && that_present_authzx))
          return false;
        if (!this.authzx.equals(that.authzx))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_authnx = true && (isSetAuthnx());
      builder.append(present_authnx);
      if (present_authnx)
        builder.append(authnx);
      boolean present_authzx = true && (isSetAuthzx());
      builder.append(present_authzx);
      if (present_authzx)
        builder.append(authzx);
      return builder.toHashCode();
    }
    public int compareTo(login_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      login_result typedOther = (login_result)other;
      lastComparison = Boolean.valueOf(isSetAuthnx()).compareTo(typedOther.isSetAuthnx());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetAuthnx()) {
        lastComparison = TBaseHelper.compareTo(this.authnx, typedOther.authnx);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetAuthzx()).compareTo(typedOther.isSetAuthzx());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetAuthzx()) {
        lastComparison = TBaseHelper.compareTo(this.authzx, typedOther.authzx);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRUCT) {
              this.authnx = new AuthenticationException();
              this.authnx.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.authzx = new AuthorizationException();
              this.authzx.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetAuthnx()) {
        oprot.writeFieldBegin(AUTHNX_FIELD_DESC);
        this.authnx.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetAuthzx()) {
        oprot.writeFieldBegin(AUTHZX_FIELD_DESC);
        this.authzx.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("login_result(");
      boolean first = true;
      sb.append("authnx:");
      if (this.authnx == null) {
        sb.append("null");
      } else {
        sb.append(this.authnx);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("authzx:");
      if (this.authzx == null) {
        sb.append("null");
      } else {
        sb.append(this.authzx);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class set_keyspace_args implements TBase<set_keyspace_args, set_keyspace_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("set_keyspace_args");
    private static final TField KEYSPACE_FIELD_DESC = new TField("keyspace", TType.STRING, (short)1);
    public String keyspace;
    public enum _Fields implements TFieldIdEnum {
      KEYSPACE((short)1, "keyspace");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return KEYSPACE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.KEYSPACE, new FieldMetaData("keyspace", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.STRING)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(set_keyspace_args.class, metaDataMap);
    }
    public set_keyspace_args() {
    }
    public set_keyspace_args(
      String keyspace)
    {
      this();
      this.keyspace = keyspace;
    }
    public set_keyspace_args(set_keyspace_args other) {
      if (other.isSetKeyspace()) {
        this.keyspace = other.keyspace;
      }
    }
    public set_keyspace_args deepCopy() {
      return new set_keyspace_args(this);
    }
    @Override
    public void clear() {
      this.keyspace = null;
    }
    public String getKeyspace() {
      return this.keyspace;
    }
    public set_keyspace_args setKeyspace(String keyspace) {
      this.keyspace = keyspace;
      return this;
    }
    public void unsetKeyspace() {
      this.keyspace = null;
    }
    public boolean isSetKeyspace() {
      return this.keyspace != null;
    }
    public void setKeyspaceIsSet(boolean value) {
      if (!value) {
        this.keyspace = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case KEYSPACE:
        if (value == null) {
          unsetKeyspace();
        } else {
          setKeyspace((String)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case KEYSPACE:
        return getKeyspace();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case KEYSPACE:
        return isSetKeyspace();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof set_keyspace_args)
        return this.equals((set_keyspace_args)that);
      return false;
    }
    public boolean equals(set_keyspace_args that) {
      if (that == null)
        return false;
      boolean this_present_keyspace = true && this.isSetKeyspace();
      boolean that_present_keyspace = true && that.isSetKeyspace();
      if (this_present_keyspace || that_present_keyspace) {
        if (!(this_present_keyspace && that_present_keyspace))
          return false;
        if (!this.keyspace.equals(that.keyspace))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_keyspace = true && (isSetKeyspace());
      builder.append(present_keyspace);
      if (present_keyspace)
        builder.append(keyspace);
      return builder.toHashCode();
    }
    public int compareTo(set_keyspace_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      set_keyspace_args typedOther = (set_keyspace_args)other;
      lastComparison = Boolean.valueOf(isSetKeyspace()).compareTo(typedOther.isSetKeyspace());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetKeyspace()) {
        lastComparison = TBaseHelper.compareTo(this.keyspace, typedOther.keyspace);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRING) {
              this.keyspace = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.keyspace != null) {
        oprot.writeFieldBegin(KEYSPACE_FIELD_DESC);
        oprot.writeString(this.keyspace);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("set_keyspace_args(");
      boolean first = true;
      sb.append("keyspace:");
      if (this.keyspace == null) {
        sb.append("null");
      } else {
        sb.append(this.keyspace);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (keyspace == null) {
        throw new TProtocolException("Required field 'keyspace' was not present! Struct: " + toString());
      }
    }
  }
  public static class set_keyspace_result implements TBase<set_keyspace_result, set_keyspace_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("set_keyspace_result");
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    public InvalidRequestException ire;
    public enum _Fields implements TFieldIdEnum {
      IRE((short)1, "ire");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return IRE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(set_keyspace_result.class, metaDataMap);
    }
    public set_keyspace_result() {
    }
    public set_keyspace_result(
      InvalidRequestException ire)
    {
      this();
      this.ire = ire;
    }
    public set_keyspace_result(set_keyspace_result other) {
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
    }
    public set_keyspace_result deepCopy() {
      return new set_keyspace_result(this);
    }
    @Override
    public void clear() {
      this.ire = null;
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public set_keyspace_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case IRE:
        return getIre();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case IRE:
        return isSetIre();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof set_keyspace_result)
        return this.equals((set_keyspace_result)that);
      return false;
    }
    public boolean equals(set_keyspace_result that) {
      if (that == null)
        return false;
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      return builder.toHashCode();
    }
    public int compareTo(set_keyspace_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      set_keyspace_result typedOther = (set_keyspace_result)other;
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("set_keyspace_result(");
      boolean first = true;
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class get_args implements TBase<get_args, get_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("get_args");
    private static final TField KEY_FIELD_DESC = new TField("key", TType.STRING, (short)1);
    private static final TField COLUMN_PATH_FIELD_DESC = new TField("column_path", TType.STRUCT, (short)2);
    private static final TField CONSISTENCY_LEVEL_FIELD_DESC = new TField("consistency_level", TType.I32, (short)3);
    public ByteBuffer key;
    public ColumnPath column_path;
    public ConsistencyLevel consistency_level;
    public enum _Fields implements TFieldIdEnum {
      KEY((short)1, "key"),
      COLUMN_PATH((short)2, "column_path"),
      CONSISTENCY_LEVEL((short)3, "consistency_level");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return KEY;
          case 2: 
            return COLUMN_PATH;
          case 3: 
            return CONSISTENCY_LEVEL;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.KEY, new FieldMetaData("key", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.STRING)));
      tmpMap.put(_Fields.COLUMN_PATH, new FieldMetaData("column_path", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, ColumnPath.class)));
      tmpMap.put(_Fields.CONSISTENCY_LEVEL, new FieldMetaData("consistency_level", TFieldRequirementType.REQUIRED, 
          new EnumMetaData(TType.ENUM, ConsistencyLevel.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(get_args.class, metaDataMap);
    }
    public get_args() {
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public get_args(
      ByteBuffer key,
      ColumnPath column_path,
      ConsistencyLevel consistency_level)
    {
      this();
      this.key = key;
      this.column_path = column_path;
      this.consistency_level = consistency_level;
    }
    public get_args(get_args other) {
      if (other.isSetKey()) {
        this.key = TBaseHelper.copyBinary(other.key);
;
      }
      if (other.isSetColumn_path()) {
        this.column_path = new ColumnPath(other.column_path);
      }
      if (other.isSetConsistency_level()) {
        this.consistency_level = other.consistency_level;
      }
    }
    public get_args deepCopy() {
      return new get_args(this);
    }
    @Override
    public void clear() {
      this.key = null;
      this.column_path = null;
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public byte[] getKey() {
      setKey(TBaseHelper.rightSize(key));
      return key.array();
    }
    public ByteBuffer BufferForKey() {
      return key;
    }
    public get_args setKey(byte[] key) {
      setKey(ByteBuffer.wrap(key));
      return this;
    }
    public get_args setKey(ByteBuffer key) {
      this.key = key;
      return this;
    }
    public void unsetKey() {
      this.key = null;
    }
    public boolean isSetKey() {
      return this.key != null;
    }
    public void setKeyIsSet(boolean value) {
      if (!value) {
        this.key = null;
      }
    }
    public ColumnPath getColumn_path() {
      return this.column_path;
    }
    public get_args setColumn_path(ColumnPath column_path) {
      this.column_path = column_path;
      return this;
    }
    public void unsetColumn_path() {
      this.column_path = null;
    }
    public boolean isSetColumn_path() {
      return this.column_path != null;
    }
    public void setColumn_pathIsSet(boolean value) {
      if (!value) {
        this.column_path = null;
      }
    }
    public ConsistencyLevel getConsistency_level() {
      return this.consistency_level;
    }
    public get_args setConsistency_level(ConsistencyLevel consistency_level) {
      this.consistency_level = consistency_level;
      return this;
    }
    public void unsetConsistency_level() {
      this.consistency_level = null;
    }
    public boolean isSetConsistency_level() {
      return this.consistency_level != null;
    }
    public void setConsistency_levelIsSet(boolean value) {
      if (!value) {
        this.consistency_level = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case KEY:
        if (value == null) {
          unsetKey();
        } else {
          setKey((ByteBuffer)value);
        }
        break;
      case COLUMN_PATH:
        if (value == null) {
          unsetColumn_path();
        } else {
          setColumn_path((ColumnPath)value);
        }
        break;
      case CONSISTENCY_LEVEL:
        if (value == null) {
          unsetConsistency_level();
        } else {
          setConsistency_level((ConsistencyLevel)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case KEY:
        return getKey();
      case COLUMN_PATH:
        return getColumn_path();
      case CONSISTENCY_LEVEL:
        return getConsistency_level();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case KEY:
        return isSetKey();
      case COLUMN_PATH:
        return isSetColumn_path();
      case CONSISTENCY_LEVEL:
        return isSetConsistency_level();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof get_args)
        return this.equals((get_args)that);
      return false;
    }
    public boolean equals(get_args that) {
      if (that == null)
        return false;
      boolean this_present_key = true && this.isSetKey();
      boolean that_present_key = true && that.isSetKey();
      if (this_present_key || that_present_key) {
        if (!(this_present_key && that_present_key))
          return false;
        if (!this.key.equals(that.key))
          return false;
      }
      boolean this_present_column_path = true && this.isSetColumn_path();
      boolean that_present_column_path = true && that.isSetColumn_path();
      if (this_present_column_path || that_present_column_path) {
        if (!(this_present_column_path && that_present_column_path))
          return false;
        if (!this.column_path.equals(that.column_path))
          return false;
      }
      boolean this_present_consistency_level = true && this.isSetConsistency_level();
      boolean that_present_consistency_level = true && that.isSetConsistency_level();
      if (this_present_consistency_level || that_present_consistency_level) {
        if (!(this_present_consistency_level && that_present_consistency_level))
          return false;
        if (!this.consistency_level.equals(that.consistency_level))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_key = true && (isSetKey());
      builder.append(present_key);
      if (present_key)
        builder.append(key);
      boolean present_column_path = true && (isSetColumn_path());
      builder.append(present_column_path);
      if (present_column_path)
        builder.append(column_path);
      boolean present_consistency_level = true && (isSetConsistency_level());
      builder.append(present_consistency_level);
      if (present_consistency_level)
        builder.append(consistency_level.getValue());
      return builder.toHashCode();
    }
    public int compareTo(get_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      get_args typedOther = (get_args)other;
      lastComparison = Boolean.valueOf(isSetKey()).compareTo(typedOther.isSetKey());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetKey()) {
        lastComparison = TBaseHelper.compareTo(this.key, typedOther.key);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetColumn_path()).compareTo(typedOther.isSetColumn_path());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetColumn_path()) {
        lastComparison = TBaseHelper.compareTo(this.column_path, typedOther.column_path);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetConsistency_level()).compareTo(typedOther.isSetConsistency_level());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetConsistency_level()) {
        lastComparison = TBaseHelper.compareTo(this.consistency_level, typedOther.consistency_level);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRING) {
              this.key = iprot.readBinary();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.column_path = new ColumnPath();
              this.column_path.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.I32) {
              this.consistency_level = ConsistencyLevel.findByValue(iprot.readI32());
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.key != null) {
        oprot.writeFieldBegin(KEY_FIELD_DESC);
        oprot.writeBinary(this.key);
        oprot.writeFieldEnd();
      }
      if (this.column_path != null) {
        oprot.writeFieldBegin(COLUMN_PATH_FIELD_DESC);
        this.column_path.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.consistency_level != null) {
        oprot.writeFieldBegin(CONSISTENCY_LEVEL_FIELD_DESC);
        oprot.writeI32(this.consistency_level.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("get_args(");
      boolean first = true;
      sb.append("key:");
      if (this.key == null) {
        sb.append("null");
      } else {
        TBaseHelper.toString(this.key, sb);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("column_path:");
      if (this.column_path == null) {
        sb.append("null");
      } else {
        sb.append(this.column_path);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("consistency_level:");
      if (this.consistency_level == null) {
        sb.append("null");
      } else {
        sb.append(this.consistency_level);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (key == null) {
        throw new TProtocolException("Required field 'key' was not present! Struct: " + toString());
      }
      if (column_path == null) {
        throw new TProtocolException("Required field 'column_path' was not present! Struct: " + toString());
      }
      if (consistency_level == null) {
        throw new TProtocolException("Required field 'consistency_level' was not present! Struct: " + toString());
      }
    }
  }
  public static class get_result implements TBase<get_result, get_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("get_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.STRUCT, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    private static final TField NFE_FIELD_DESC = new TField("nfe", TType.STRUCT, (short)2);
    private static final TField UE_FIELD_DESC = new TField("ue", TType.STRUCT, (short)3);
    private static final TField TE_FIELD_DESC = new TField("te", TType.STRUCT, (short)4);
    public ColumnOrSuperColumn success;
    public InvalidRequestException ire;
    public NotFoundException nfe;
    public UnavailableException ue;
    public TimedOutException te;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire"),
      NFE((short)2, "nfe"),
      UE((short)3, "ue"),
      TE((short)4, "te");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          case 2: 
            return NFE;
          case 3: 
            return UE;
          case 4: 
            return TE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new StructMetaData(TType.STRUCT, ColumnOrSuperColumn.class)));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.NFE, new FieldMetaData("nfe", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.UE, new FieldMetaData("ue", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.TE, new FieldMetaData("te", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(get_result.class, metaDataMap);
    }
    public get_result() {
    }
    public get_result(
      ColumnOrSuperColumn success,
      InvalidRequestException ire,
      NotFoundException nfe,
      UnavailableException ue,
      TimedOutException te)
    {
      this();
      this.success = success;
      this.ire = ire;
      this.nfe = nfe;
      this.ue = ue;
      this.te = te;
    }
    public get_result(get_result other) {
      if (other.isSetSuccess()) {
        this.success = new ColumnOrSuperColumn(other.success);
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
      if (other.isSetNfe()) {
        this.nfe = new NotFoundException(other.nfe);
      }
      if (other.isSetUe()) {
        this.ue = new UnavailableException(other.ue);
      }
      if (other.isSetTe()) {
        this.te = new TimedOutException(other.te);
      }
    }
    public get_result deepCopy() {
      return new get_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.ire = null;
      this.nfe = null;
      this.ue = null;
      this.te = null;
    }
    public ColumnOrSuperColumn getSuccess() {
      return this.success;
    }
    public get_result setSuccess(ColumnOrSuperColumn success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public get_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public NotFoundException getNfe() {
      return this.nfe;
    }
    public get_result setNfe(NotFoundException nfe) {
      this.nfe = nfe;
      return this;
    }
    public void unsetNfe() {
      this.nfe = null;
    }
    public boolean isSetNfe() {
      return this.nfe != null;
    }
    public void setNfeIsSet(boolean value) {
      if (!value) {
        this.nfe = null;
      }
    }
    public UnavailableException getUe() {
      return this.ue;
    }
    public get_result setUe(UnavailableException ue) {
      this.ue = ue;
      return this;
    }
    public void unsetUe() {
      this.ue = null;
    }
    public boolean isSetUe() {
      return this.ue != null;
    }
    public void setUeIsSet(boolean value) {
      if (!value) {
        this.ue = null;
      }
    }
    public TimedOutException getTe() {
      return this.te;
    }
    public get_result setTe(TimedOutException te) {
      this.te = te;
      return this;
    }
    public void unsetTe() {
      this.te = null;
    }
    public boolean isSetTe() {
      return this.te != null;
    }
    public void setTeIsSet(boolean value) {
      if (!value) {
        this.te = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((ColumnOrSuperColumn)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      case NFE:
        if (value == null) {
          unsetNfe();
        } else {
          setNfe((NotFoundException)value);
        }
        break;
      case UE:
        if (value == null) {
          unsetUe();
        } else {
          setUe((UnavailableException)value);
        }
        break;
      case TE:
        if (value == null) {
          unsetTe();
        } else {
          setTe((TimedOutException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case IRE:
        return getIre();
      case NFE:
        return getNfe();
      case UE:
        return getUe();
      case TE:
        return getTe();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      case NFE:
        return isSetNfe();
      case UE:
        return isSetUe();
      case TE:
        return isSetTe();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof get_result)
        return this.equals((get_result)that);
      return false;
    }
    public boolean equals(get_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      boolean this_present_nfe = true && this.isSetNfe();
      boolean that_present_nfe = true && that.isSetNfe();
      if (this_present_nfe || that_present_nfe) {
        if (!(this_present_nfe && that_present_nfe))
          return false;
        if (!this.nfe.equals(that.nfe))
          return false;
      }
      boolean this_present_ue = true && this.isSetUe();
      boolean that_present_ue = true && that.isSetUe();
      if (this_present_ue || that_present_ue) {
        if (!(this_present_ue && that_present_ue))
          return false;
        if (!this.ue.equals(that.ue))
          return false;
      }
      boolean this_present_te = true && this.isSetTe();
      boolean that_present_te = true && that.isSetTe();
      if (this_present_te || that_present_te) {
        if (!(this_present_te && that_present_te))
          return false;
        if (!this.te.equals(that.te))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      boolean present_nfe = true && (isSetNfe());
      builder.append(present_nfe);
      if (present_nfe)
        builder.append(nfe);
      boolean present_ue = true && (isSetUe());
      builder.append(present_ue);
      if (present_ue)
        builder.append(ue);
      boolean present_te = true && (isSetTe());
      builder.append(present_te);
      if (present_te)
        builder.append(te);
      return builder.toHashCode();
    }
    public int compareTo(get_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      get_result typedOther = (get_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetNfe()).compareTo(typedOther.isSetNfe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetNfe()) {
        lastComparison = TBaseHelper.compareTo(this.nfe, typedOther.nfe);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetUe()).compareTo(typedOther.isSetUe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetUe()) {
        lastComparison = TBaseHelper.compareTo(this.ue, typedOther.ue);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetTe()).compareTo(typedOther.isSetTe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetTe()) {
        lastComparison = TBaseHelper.compareTo(this.te, typedOther.te);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.STRUCT) {
              this.success = new ColumnOrSuperColumn();
              this.success.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.nfe = new NotFoundException();
              this.nfe.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.ue = new UnavailableException();
              this.ue.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 4: 
            if (field.type == TType.STRUCT) {
              this.te = new TimedOutException();
              this.te.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        this.success.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetNfe()) {
        oprot.writeFieldBegin(NFE_FIELD_DESC);
        this.nfe.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetUe()) {
        oprot.writeFieldBegin(UE_FIELD_DESC);
        this.ue.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetTe()) {
        oprot.writeFieldBegin(TE_FIELD_DESC);
        this.te.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("get_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("nfe:");
      if (this.nfe == null) {
        sb.append("null");
      } else {
        sb.append(this.nfe);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ue:");
      if (this.ue == null) {
        sb.append("null");
      } else {
        sb.append(this.ue);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("te:");
      if (this.te == null) {
        sb.append("null");
      } else {
        sb.append(this.te);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class get_slice_args implements TBase<get_slice_args, get_slice_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("get_slice_args");
    private static final TField KEY_FIELD_DESC = new TField("key", TType.STRING, (short)1);
    private static final TField COLUMN_PARENT_FIELD_DESC = new TField("column_parent", TType.STRUCT, (short)2);
    private static final TField PREDICATE_FIELD_DESC = new TField("predicate", TType.STRUCT, (short)3);
    private static final TField CONSISTENCY_LEVEL_FIELD_DESC = new TField("consistency_level", TType.I32, (short)4);
    public ByteBuffer key;
    public ColumnParent column_parent;
    public SlicePredicate predicate;
    public ConsistencyLevel consistency_level;
    public enum _Fields implements TFieldIdEnum {
      KEY((short)1, "key"),
      COLUMN_PARENT((short)2, "column_parent"),
      PREDICATE((short)3, "predicate"),
      CONSISTENCY_LEVEL((short)4, "consistency_level");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return KEY;
          case 2: 
            return COLUMN_PARENT;
          case 3: 
            return PREDICATE;
          case 4: 
            return CONSISTENCY_LEVEL;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.KEY, new FieldMetaData("key", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.STRING)));
      tmpMap.put(_Fields.COLUMN_PARENT, new FieldMetaData("column_parent", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, ColumnParent.class)));
      tmpMap.put(_Fields.PREDICATE, new FieldMetaData("predicate", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, SlicePredicate.class)));
      tmpMap.put(_Fields.CONSISTENCY_LEVEL, new FieldMetaData("consistency_level", TFieldRequirementType.REQUIRED, 
          new EnumMetaData(TType.ENUM, ConsistencyLevel.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(get_slice_args.class, metaDataMap);
    }
    public get_slice_args() {
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public get_slice_args(
      ByteBuffer key,
      ColumnParent column_parent,
      SlicePredicate predicate,
      ConsistencyLevel consistency_level)
    {
      this();
      this.key = key;
      this.column_parent = column_parent;
      this.predicate = predicate;
      this.consistency_level = consistency_level;
    }
    public get_slice_args(get_slice_args other) {
      if (other.isSetKey()) {
        this.key = TBaseHelper.copyBinary(other.key);
;
      }
      if (other.isSetColumn_parent()) {
        this.column_parent = new ColumnParent(other.column_parent);
      }
      if (other.isSetPredicate()) {
        this.predicate = new SlicePredicate(other.predicate);
      }
      if (other.isSetConsistency_level()) {
        this.consistency_level = other.consistency_level;
      }
    }
    public get_slice_args deepCopy() {
      return new get_slice_args(this);
    }
    @Override
    public void clear() {
      this.key = null;
      this.column_parent = null;
      this.predicate = null;
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public byte[] getKey() {
      setKey(TBaseHelper.rightSize(key));
      return key.array();
    }
    public ByteBuffer BufferForKey() {
      return key;
    }
    public get_slice_args setKey(byte[] key) {
      setKey(ByteBuffer.wrap(key));
      return this;
    }
    public get_slice_args setKey(ByteBuffer key) {
      this.key = key;
      return this;
    }
    public void unsetKey() {
      this.key = null;
    }
    public boolean isSetKey() {
      return this.key != null;
    }
    public void setKeyIsSet(boolean value) {
      if (!value) {
        this.key = null;
      }
    }
    public ColumnParent getColumn_parent() {
      return this.column_parent;
    }
    public get_slice_args setColumn_parent(ColumnParent column_parent) {
      this.column_parent = column_parent;
      return this;
    }
    public void unsetColumn_parent() {
      this.column_parent = null;
    }
    public boolean isSetColumn_parent() {
      return this.column_parent != null;
    }
    public void setColumn_parentIsSet(boolean value) {
      if (!value) {
        this.column_parent = null;
      }
    }
    public SlicePredicate getPredicate() {
      return this.predicate;
    }
    public get_slice_args setPredicate(SlicePredicate predicate) {
      this.predicate = predicate;
      return this;
    }
    public void unsetPredicate() {
      this.predicate = null;
    }
    public boolean isSetPredicate() {
      return this.predicate != null;
    }
    public void setPredicateIsSet(boolean value) {
      if (!value) {
        this.predicate = null;
      }
    }
    public ConsistencyLevel getConsistency_level() {
      return this.consistency_level;
    }
    public get_slice_args setConsistency_level(ConsistencyLevel consistency_level) {
      this.consistency_level = consistency_level;
      return this;
    }
    public void unsetConsistency_level() {
      this.consistency_level = null;
    }
    public boolean isSetConsistency_level() {
      return this.consistency_level != null;
    }
    public void setConsistency_levelIsSet(boolean value) {
      if (!value) {
        this.consistency_level = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case KEY:
        if (value == null) {
          unsetKey();
        } else {
          setKey((ByteBuffer)value);
        }
        break;
      case COLUMN_PARENT:
        if (value == null) {
          unsetColumn_parent();
        } else {
          setColumn_parent((ColumnParent)value);
        }
        break;
      case PREDICATE:
        if (value == null) {
          unsetPredicate();
        } else {
          setPredicate((SlicePredicate)value);
        }
        break;
      case CONSISTENCY_LEVEL:
        if (value == null) {
          unsetConsistency_level();
        } else {
          setConsistency_level((ConsistencyLevel)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case KEY:
        return getKey();
      case COLUMN_PARENT:
        return getColumn_parent();
      case PREDICATE:
        return getPredicate();
      case CONSISTENCY_LEVEL:
        return getConsistency_level();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case KEY:
        return isSetKey();
      case COLUMN_PARENT:
        return isSetColumn_parent();
      case PREDICATE:
        return isSetPredicate();
      case CONSISTENCY_LEVEL:
        return isSetConsistency_level();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof get_slice_args)
        return this.equals((get_slice_args)that);
      return false;
    }
    public boolean equals(get_slice_args that) {
      if (that == null)
        return false;
      boolean this_present_key = true && this.isSetKey();
      boolean that_present_key = true && that.isSetKey();
      if (this_present_key || that_present_key) {
        if (!(this_present_key && that_present_key))
          return false;
        if (!this.key.equals(that.key))
          return false;
      }
      boolean this_present_column_parent = true && this.isSetColumn_parent();
      boolean that_present_column_parent = true && that.isSetColumn_parent();
      if (this_present_column_parent || that_present_column_parent) {
        if (!(this_present_column_parent && that_present_column_parent))
          return false;
        if (!this.column_parent.equals(that.column_parent))
          return false;
      }
      boolean this_present_predicate = true && this.isSetPredicate();
      boolean that_present_predicate = true && that.isSetPredicate();
      if (this_present_predicate || that_present_predicate) {
        if (!(this_present_predicate && that_present_predicate))
          return false;
        if (!this.predicate.equals(that.predicate))
          return false;
      }
      boolean this_present_consistency_level = true && this.isSetConsistency_level();
      boolean that_present_consistency_level = true && that.isSetConsistency_level();
      if (this_present_consistency_level || that_present_consistency_level) {
        if (!(this_present_consistency_level && that_present_consistency_level))
          return false;
        if (!this.consistency_level.equals(that.consistency_level))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_key = true && (isSetKey());
      builder.append(present_key);
      if (present_key)
        builder.append(key);
      boolean present_column_parent = true && (isSetColumn_parent());
      builder.append(present_column_parent);
      if (present_column_parent)
        builder.append(column_parent);
      boolean present_predicate = true && (isSetPredicate());
      builder.append(present_predicate);
      if (present_predicate)
        builder.append(predicate);
      boolean present_consistency_level = true && (isSetConsistency_level());
      builder.append(present_consistency_level);
      if (present_consistency_level)
        builder.append(consistency_level.getValue());
      return builder.toHashCode();
    }
    public int compareTo(get_slice_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      get_slice_args typedOther = (get_slice_args)other;
      lastComparison = Boolean.valueOf(isSetKey()).compareTo(typedOther.isSetKey());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetKey()) {
        lastComparison = TBaseHelper.compareTo(this.key, typedOther.key);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetColumn_parent()).compareTo(typedOther.isSetColumn_parent());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetColumn_parent()) {
        lastComparison = TBaseHelper.compareTo(this.column_parent, typedOther.column_parent);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetPredicate()).compareTo(typedOther.isSetPredicate());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetPredicate()) {
        lastComparison = TBaseHelper.compareTo(this.predicate, typedOther.predicate);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetConsistency_level()).compareTo(typedOther.isSetConsistency_level());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetConsistency_level()) {
        lastComparison = TBaseHelper.compareTo(this.consistency_level, typedOther.consistency_level);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRING) {
              this.key = iprot.readBinary();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.column_parent = new ColumnParent();
              this.column_parent.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.predicate = new SlicePredicate();
              this.predicate.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 4: 
            if (field.type == TType.I32) {
              this.consistency_level = ConsistencyLevel.findByValue(iprot.readI32());
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.key != null) {
        oprot.writeFieldBegin(KEY_FIELD_DESC);
        oprot.writeBinary(this.key);
        oprot.writeFieldEnd();
      }
      if (this.column_parent != null) {
        oprot.writeFieldBegin(COLUMN_PARENT_FIELD_DESC);
        this.column_parent.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.predicate != null) {
        oprot.writeFieldBegin(PREDICATE_FIELD_DESC);
        this.predicate.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.consistency_level != null) {
        oprot.writeFieldBegin(CONSISTENCY_LEVEL_FIELD_DESC);
        oprot.writeI32(this.consistency_level.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("get_slice_args(");
      boolean first = true;
      sb.append("key:");
      if (this.key == null) {
        sb.append("null");
      } else {
        TBaseHelper.toString(this.key, sb);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("column_parent:");
      if (this.column_parent == null) {
        sb.append("null");
      } else {
        sb.append(this.column_parent);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("predicate:");
      if (this.predicate == null) {
        sb.append("null");
      } else {
        sb.append(this.predicate);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("consistency_level:");
      if (this.consistency_level == null) {
        sb.append("null");
      } else {
        sb.append(this.consistency_level);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (key == null) {
        throw new TProtocolException("Required field 'key' was not present! Struct: " + toString());
      }
      if (column_parent == null) {
        throw new TProtocolException("Required field 'column_parent' was not present! Struct: " + toString());
      }
      if (predicate == null) {
        throw new TProtocolException("Required field 'predicate' was not present! Struct: " + toString());
      }
      if (consistency_level == null) {
        throw new TProtocolException("Required field 'consistency_level' was not present! Struct: " + toString());
      }
    }
  }
  public static class get_slice_result implements TBase<get_slice_result, get_slice_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("get_slice_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.LIST, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    private static final TField UE_FIELD_DESC = new TField("ue", TType.STRUCT, (short)2);
    private static final TField TE_FIELD_DESC = new TField("te", TType.STRUCT, (short)3);
    public List<ColumnOrSuperColumn> success;
    public InvalidRequestException ire;
    public UnavailableException ue;
    public TimedOutException te;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire"),
      UE((short)2, "ue"),
      TE((short)3, "te");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          case 2: 
            return UE;
          case 3: 
            return TE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new ListMetaData(TType.LIST, 
              new StructMetaData(TType.STRUCT, ColumnOrSuperColumn.class))));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.UE, new FieldMetaData("ue", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.TE, new FieldMetaData("te", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(get_slice_result.class, metaDataMap);
    }
    public get_slice_result() {
    }
    public get_slice_result(
      List<ColumnOrSuperColumn> success,
      InvalidRequestException ire,
      UnavailableException ue,
      TimedOutException te)
    {
      this();
      this.success = success;
      this.ire = ire;
      this.ue = ue;
      this.te = te;
    }
    public get_slice_result(get_slice_result other) {
      if (other.isSetSuccess()) {
        List<ColumnOrSuperColumn> __this__success = new ArrayList<ColumnOrSuperColumn>();
        for (ColumnOrSuperColumn other_element : other.success) {
          __this__success.add(new ColumnOrSuperColumn(other_element));
        }
        this.success = __this__success;
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
      if (other.isSetUe()) {
        this.ue = new UnavailableException(other.ue);
      }
      if (other.isSetTe()) {
        this.te = new TimedOutException(other.te);
      }
    }
    public get_slice_result deepCopy() {
      return new get_slice_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.ire = null;
      this.ue = null;
      this.te = null;
    }
    public int getSuccessSize() {
      return (this.success == null) ? 0 : this.success.size();
    }
    public java.util.Iterator<ColumnOrSuperColumn> getSuccessIterator() {
      return (this.success == null) ? null : this.success.iterator();
    }
    public void addToSuccess(ColumnOrSuperColumn elem) {
      if (this.success == null) {
        this.success = new ArrayList<ColumnOrSuperColumn>();
      }
      this.success.add(elem);
    }
    public List<ColumnOrSuperColumn> getSuccess() {
      return this.success;
    }
    public get_slice_result setSuccess(List<ColumnOrSuperColumn> success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public get_slice_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public UnavailableException getUe() {
      return this.ue;
    }
    public get_slice_result setUe(UnavailableException ue) {
      this.ue = ue;
      return this;
    }
    public void unsetUe() {
      this.ue = null;
    }
    public boolean isSetUe() {
      return this.ue != null;
    }
    public void setUeIsSet(boolean value) {
      if (!value) {
        this.ue = null;
      }
    }
    public TimedOutException getTe() {
      return this.te;
    }
    public get_slice_result setTe(TimedOutException te) {
      this.te = te;
      return this;
    }
    public void unsetTe() {
      this.te = null;
    }
    public boolean isSetTe() {
      return this.te != null;
    }
    public void setTeIsSet(boolean value) {
      if (!value) {
        this.te = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((List<ColumnOrSuperColumn>)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      case UE:
        if (value == null) {
          unsetUe();
        } else {
          setUe((UnavailableException)value);
        }
        break;
      case TE:
        if (value == null) {
          unsetTe();
        } else {
          setTe((TimedOutException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case IRE:
        return getIre();
      case UE:
        return getUe();
      case TE:
        return getTe();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      case UE:
        return isSetUe();
      case TE:
        return isSetTe();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof get_slice_result)
        return this.equals((get_slice_result)that);
      return false;
    }
    public boolean equals(get_slice_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      boolean this_present_ue = true && this.isSetUe();
      boolean that_present_ue = true && that.isSetUe();
      if (this_present_ue || that_present_ue) {
        if (!(this_present_ue && that_present_ue))
          return false;
        if (!this.ue.equals(that.ue))
          return false;
      }
      boolean this_present_te = true && this.isSetTe();
      boolean that_present_te = true && that.isSetTe();
      if (this_present_te || that_present_te) {
        if (!(this_present_te && that_present_te))
          return false;
        if (!this.te.equals(that.te))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      boolean present_ue = true && (isSetUe());
      builder.append(present_ue);
      if (present_ue)
        builder.append(ue);
      boolean present_te = true && (isSetTe());
      builder.append(present_te);
      if (present_te)
        builder.append(te);
      return builder.toHashCode();
    }
    public int compareTo(get_slice_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      get_slice_result typedOther = (get_slice_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetUe()).compareTo(typedOther.isSetUe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetUe()) {
        lastComparison = TBaseHelper.compareTo(this.ue, typedOther.ue);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetTe()).compareTo(typedOther.isSetTe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetTe()) {
        lastComparison = TBaseHelper.compareTo(this.te, typedOther.te);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.LIST) {
              {
                TList _list50 = iprot.readListBegin();
                this.success = new ArrayList<ColumnOrSuperColumn>(_list50.size);
                for (int _i51 = 0; _i51 < _list50.size; ++_i51)
                {
                  ColumnOrSuperColumn _elem52;
                  _elem52 = new ColumnOrSuperColumn();
                  _elem52.read(iprot);
                  this.success.add(_elem52);
                }
                iprot.readListEnd();
              }
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.ue = new UnavailableException();
              this.ue.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.te = new TimedOutException();
              this.te.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        {
          oprot.writeListBegin(new TList(TType.STRUCT, this.success.size()));
          for (ColumnOrSuperColumn _iter53 : this.success)
          {
            _iter53.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetUe()) {
        oprot.writeFieldBegin(UE_FIELD_DESC);
        this.ue.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetTe()) {
        oprot.writeFieldBegin(TE_FIELD_DESC);
        this.te.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("get_slice_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ue:");
      if (this.ue == null) {
        sb.append("null");
      } else {
        sb.append(this.ue);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("te:");
      if (this.te == null) {
        sb.append("null");
      } else {
        sb.append(this.te);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class get_count_args implements TBase<get_count_args, get_count_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("get_count_args");
    private static final TField KEY_FIELD_DESC = new TField("key", TType.STRING, (short)1);
    private static final TField COLUMN_PARENT_FIELD_DESC = new TField("column_parent", TType.STRUCT, (short)2);
    private static final TField PREDICATE_FIELD_DESC = new TField("predicate", TType.STRUCT, (short)3);
    private static final TField CONSISTENCY_LEVEL_FIELD_DESC = new TField("consistency_level", TType.I32, (short)4);
    public ByteBuffer key;
    public ColumnParent column_parent;
    public SlicePredicate predicate;
    public ConsistencyLevel consistency_level;
    public enum _Fields implements TFieldIdEnum {
      KEY((short)1, "key"),
      COLUMN_PARENT((short)2, "column_parent"),
      PREDICATE((short)3, "predicate"),
      CONSISTENCY_LEVEL((short)4, "consistency_level");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return KEY;
          case 2: 
            return COLUMN_PARENT;
          case 3: 
            return PREDICATE;
          case 4: 
            return CONSISTENCY_LEVEL;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.KEY, new FieldMetaData("key", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.STRING)));
      tmpMap.put(_Fields.COLUMN_PARENT, new FieldMetaData("column_parent", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, ColumnParent.class)));
      tmpMap.put(_Fields.PREDICATE, new FieldMetaData("predicate", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, SlicePredicate.class)));
      tmpMap.put(_Fields.CONSISTENCY_LEVEL, new FieldMetaData("consistency_level", TFieldRequirementType.REQUIRED, 
          new EnumMetaData(TType.ENUM, ConsistencyLevel.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(get_count_args.class, metaDataMap);
    }
    public get_count_args() {
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public get_count_args(
      ByteBuffer key,
      ColumnParent column_parent,
      SlicePredicate predicate,
      ConsistencyLevel consistency_level)
    {
      this();
      this.key = key;
      this.column_parent = column_parent;
      this.predicate = predicate;
      this.consistency_level = consistency_level;
    }
    public get_count_args(get_count_args other) {
      if (other.isSetKey()) {
        this.key = TBaseHelper.copyBinary(other.key);
;
      }
      if (other.isSetColumn_parent()) {
        this.column_parent = new ColumnParent(other.column_parent);
      }
      if (other.isSetPredicate()) {
        this.predicate = new SlicePredicate(other.predicate);
      }
      if (other.isSetConsistency_level()) {
        this.consistency_level = other.consistency_level;
      }
    }
    public get_count_args deepCopy() {
      return new get_count_args(this);
    }
    @Override
    public void clear() {
      this.key = null;
      this.column_parent = null;
      this.predicate = null;
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public byte[] getKey() {
      setKey(TBaseHelper.rightSize(key));
      return key.array();
    }
    public ByteBuffer BufferForKey() {
      return key;
    }
    public get_count_args setKey(byte[] key) {
      setKey(ByteBuffer.wrap(key));
      return this;
    }
    public get_count_args setKey(ByteBuffer key) {
      this.key = key;
      return this;
    }
    public void unsetKey() {
      this.key = null;
    }
    public boolean isSetKey() {
      return this.key != null;
    }
    public void setKeyIsSet(boolean value) {
      if (!value) {
        this.key = null;
      }
    }
    public ColumnParent getColumn_parent() {
      return this.column_parent;
    }
    public get_count_args setColumn_parent(ColumnParent column_parent) {
      this.column_parent = column_parent;
      return this;
    }
    public void unsetColumn_parent() {
      this.column_parent = null;
    }
    public boolean isSetColumn_parent() {
      return this.column_parent != null;
    }
    public void setColumn_parentIsSet(boolean value) {
      if (!value) {
        this.column_parent = null;
      }
    }
    public SlicePredicate getPredicate() {
      return this.predicate;
    }
    public get_count_args setPredicate(SlicePredicate predicate) {
      this.predicate = predicate;
      return this;
    }
    public void unsetPredicate() {
      this.predicate = null;
    }
    public boolean isSetPredicate() {
      return this.predicate != null;
    }
    public void setPredicateIsSet(boolean value) {
      if (!value) {
        this.predicate = null;
      }
    }
    public ConsistencyLevel getConsistency_level() {
      return this.consistency_level;
    }
    public get_count_args setConsistency_level(ConsistencyLevel consistency_level) {
      this.consistency_level = consistency_level;
      return this;
    }
    public void unsetConsistency_level() {
      this.consistency_level = null;
    }
    public boolean isSetConsistency_level() {
      return this.consistency_level != null;
    }
    public void setConsistency_levelIsSet(boolean value) {
      if (!value) {
        this.consistency_level = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case KEY:
        if (value == null) {
          unsetKey();
        } else {
          setKey((ByteBuffer)value);
        }
        break;
      case COLUMN_PARENT:
        if (value == null) {
          unsetColumn_parent();
        } else {
          setColumn_parent((ColumnParent)value);
        }
        break;
      case PREDICATE:
        if (value == null) {
          unsetPredicate();
        } else {
          setPredicate((SlicePredicate)value);
        }
        break;
      case CONSISTENCY_LEVEL:
        if (value == null) {
          unsetConsistency_level();
        } else {
          setConsistency_level((ConsistencyLevel)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case KEY:
        return getKey();
      case COLUMN_PARENT:
        return getColumn_parent();
      case PREDICATE:
        return getPredicate();
      case CONSISTENCY_LEVEL:
        return getConsistency_level();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case KEY:
        return isSetKey();
      case COLUMN_PARENT:
        return isSetColumn_parent();
      case PREDICATE:
        return isSetPredicate();
      case CONSISTENCY_LEVEL:
        return isSetConsistency_level();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof get_count_args)
        return this.equals((get_count_args)that);
      return false;
    }
    public boolean equals(get_count_args that) {
      if (that == null)
        return false;
      boolean this_present_key = true && this.isSetKey();
      boolean that_present_key = true && that.isSetKey();
      if (this_present_key || that_present_key) {
        if (!(this_present_key && that_present_key))
          return false;
        if (!this.key.equals(that.key))
          return false;
      }
      boolean this_present_column_parent = true && this.isSetColumn_parent();
      boolean that_present_column_parent = true && that.isSetColumn_parent();
      if (this_present_column_parent || that_present_column_parent) {
        if (!(this_present_column_parent && that_present_column_parent))
          return false;
        if (!this.column_parent.equals(that.column_parent))
          return false;
      }
      boolean this_present_predicate = true && this.isSetPredicate();
      boolean that_present_predicate = true && that.isSetPredicate();
      if (this_present_predicate || that_present_predicate) {
        if (!(this_present_predicate && that_present_predicate))
          return false;
        if (!this.predicate.equals(that.predicate))
          return false;
      }
      boolean this_present_consistency_level = true && this.isSetConsistency_level();
      boolean that_present_consistency_level = true && that.isSetConsistency_level();
      if (this_present_consistency_level || that_present_consistency_level) {
        if (!(this_present_consistency_level && that_present_consistency_level))
          return false;
        if (!this.consistency_level.equals(that.consistency_level))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_key = true && (isSetKey());
      builder.append(present_key);
      if (present_key)
        builder.append(key);
      boolean present_column_parent = true && (isSetColumn_parent());
      builder.append(present_column_parent);
      if (present_column_parent)
        builder.append(column_parent);
      boolean present_predicate = true && (isSetPredicate());
      builder.append(present_predicate);
      if (present_predicate)
        builder.append(predicate);
      boolean present_consistency_level = true && (isSetConsistency_level());
      builder.append(present_consistency_level);
      if (present_consistency_level)
        builder.append(consistency_level.getValue());
      return builder.toHashCode();
    }
    public int compareTo(get_count_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      get_count_args typedOther = (get_count_args)other;
      lastComparison = Boolean.valueOf(isSetKey()).compareTo(typedOther.isSetKey());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetKey()) {
        lastComparison = TBaseHelper.compareTo(this.key, typedOther.key);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetColumn_parent()).compareTo(typedOther.isSetColumn_parent());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetColumn_parent()) {
        lastComparison = TBaseHelper.compareTo(this.column_parent, typedOther.column_parent);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetPredicate()).compareTo(typedOther.isSetPredicate());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetPredicate()) {
        lastComparison = TBaseHelper.compareTo(this.predicate, typedOther.predicate);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetConsistency_level()).compareTo(typedOther.isSetConsistency_level());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetConsistency_level()) {
        lastComparison = TBaseHelper.compareTo(this.consistency_level, typedOther.consistency_level);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRING) {
              this.key = iprot.readBinary();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.column_parent = new ColumnParent();
              this.column_parent.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.predicate = new SlicePredicate();
              this.predicate.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 4: 
            if (field.type == TType.I32) {
              this.consistency_level = ConsistencyLevel.findByValue(iprot.readI32());
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.key != null) {
        oprot.writeFieldBegin(KEY_FIELD_DESC);
        oprot.writeBinary(this.key);
        oprot.writeFieldEnd();
      }
      if (this.column_parent != null) {
        oprot.writeFieldBegin(COLUMN_PARENT_FIELD_DESC);
        this.column_parent.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.predicate != null) {
        oprot.writeFieldBegin(PREDICATE_FIELD_DESC);
        this.predicate.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.consistency_level != null) {
        oprot.writeFieldBegin(CONSISTENCY_LEVEL_FIELD_DESC);
        oprot.writeI32(this.consistency_level.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("get_count_args(");
      boolean first = true;
      sb.append("key:");
      if (this.key == null) {
        sb.append("null");
      } else {
        TBaseHelper.toString(this.key, sb);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("column_parent:");
      if (this.column_parent == null) {
        sb.append("null");
      } else {
        sb.append(this.column_parent);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("predicate:");
      if (this.predicate == null) {
        sb.append("null");
      } else {
        sb.append(this.predicate);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("consistency_level:");
      if (this.consistency_level == null) {
        sb.append("null");
      } else {
        sb.append(this.consistency_level);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (key == null) {
        throw new TProtocolException("Required field 'key' was not present! Struct: " + toString());
      }
      if (column_parent == null) {
        throw new TProtocolException("Required field 'column_parent' was not present! Struct: " + toString());
      }
      if (predicate == null) {
        throw new TProtocolException("Required field 'predicate' was not present! Struct: " + toString());
      }
      if (consistency_level == null) {
        throw new TProtocolException("Required field 'consistency_level' was not present! Struct: " + toString());
      }
    }
  }
  public static class get_count_result implements TBase<get_count_result, get_count_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("get_count_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.I32, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    private static final TField UE_FIELD_DESC = new TField("ue", TType.STRUCT, (short)2);
    private static final TField TE_FIELD_DESC = new TField("te", TType.STRUCT, (short)3);
    public int success;
    public InvalidRequestException ire;
    public UnavailableException ue;
    public TimedOutException te;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire"),
      UE((short)2, "ue"),
      TE((short)3, "te");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          case 2: 
            return UE;
          case 3: 
            return TE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    private static final int __SUCCESS_ISSET_ID = 0;
    private BitSet __isset_bit_vector = new BitSet(1);
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.I32)));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.UE, new FieldMetaData("ue", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.TE, new FieldMetaData("te", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(get_count_result.class, metaDataMap);
    }
    public get_count_result() {
    }
    public get_count_result(
      int success,
      InvalidRequestException ire,
      UnavailableException ue,
      TimedOutException te)
    {
      this();
      this.success = success;
      setSuccessIsSet(true);
      this.ire = ire;
      this.ue = ue;
      this.te = te;
    }
    public get_count_result(get_count_result other) {
      __isset_bit_vector.clear();
      __isset_bit_vector.or(other.__isset_bit_vector);
      this.success = other.success;
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
      if (other.isSetUe()) {
        this.ue = new UnavailableException(other.ue);
      }
      if (other.isSetTe()) {
        this.te = new TimedOutException(other.te);
      }
    }
    public get_count_result deepCopy() {
      return new get_count_result(this);
    }
    @Override
    public void clear() {
      setSuccessIsSet(false);
      this.success = 0;
      this.ire = null;
      this.ue = null;
      this.te = null;
    }
    public int getSuccess() {
      return this.success;
    }
    public get_count_result setSuccess(int success) {
      this.success = success;
      setSuccessIsSet(true);
      return this;
    }
    public void unsetSuccess() {
      __isset_bit_vector.clear(__SUCCESS_ISSET_ID);
    }
    public boolean isSetSuccess() {
      return __isset_bit_vector.get(__SUCCESS_ISSET_ID);
    }
    public void setSuccessIsSet(boolean value) {
      __isset_bit_vector.set(__SUCCESS_ISSET_ID, value);
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public get_count_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public UnavailableException getUe() {
      return this.ue;
    }
    public get_count_result setUe(UnavailableException ue) {
      this.ue = ue;
      return this;
    }
    public void unsetUe() {
      this.ue = null;
    }
    public boolean isSetUe() {
      return this.ue != null;
    }
    public void setUeIsSet(boolean value) {
      if (!value) {
        this.ue = null;
      }
    }
    public TimedOutException getTe() {
      return this.te;
    }
    public get_count_result setTe(TimedOutException te) {
      this.te = te;
      return this;
    }
    public void unsetTe() {
      this.te = null;
    }
    public boolean isSetTe() {
      return this.te != null;
    }
    public void setTeIsSet(boolean value) {
      if (!value) {
        this.te = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((Integer)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      case UE:
        if (value == null) {
          unsetUe();
        } else {
          setUe((UnavailableException)value);
        }
        break;
      case TE:
        if (value == null) {
          unsetTe();
        } else {
          setTe((TimedOutException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return new Integer(getSuccess());
      case IRE:
        return getIre();
      case UE:
        return getUe();
      case TE:
        return getTe();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      case UE:
        return isSetUe();
      case TE:
        return isSetTe();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof get_count_result)
        return this.equals((get_count_result)that);
      return false;
    }
    public boolean equals(get_count_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true;
      boolean that_present_success = true;
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (this.success != that.success)
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      boolean this_present_ue = true && this.isSetUe();
      boolean that_present_ue = true && that.isSetUe();
      if (this_present_ue || that_present_ue) {
        if (!(this_present_ue && that_present_ue))
          return false;
        if (!this.ue.equals(that.ue))
          return false;
      }
      boolean this_present_te = true && this.isSetTe();
      boolean that_present_te = true && that.isSetTe();
      if (this_present_te || that_present_te) {
        if (!(this_present_te && that_present_te))
          return false;
        if (!this.te.equals(that.te))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true;
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      boolean present_ue = true && (isSetUe());
      builder.append(present_ue);
      if (present_ue)
        builder.append(ue);
      boolean present_te = true && (isSetTe());
      builder.append(present_te);
      if (present_te)
        builder.append(te);
      return builder.toHashCode();
    }
    public int compareTo(get_count_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      get_count_result typedOther = (get_count_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetUe()).compareTo(typedOther.isSetUe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetUe()) {
        lastComparison = TBaseHelper.compareTo(this.ue, typedOther.ue);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetTe()).compareTo(typedOther.isSetTe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetTe()) {
        lastComparison = TBaseHelper.compareTo(this.te, typedOther.te);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.I32) {
              this.success = iprot.readI32();
              setSuccessIsSet(true);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.ue = new UnavailableException();
              this.ue.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.te = new TimedOutException();
              this.te.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        oprot.writeI32(this.success);
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetUe()) {
        oprot.writeFieldBegin(UE_FIELD_DESC);
        this.ue.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetTe()) {
        oprot.writeFieldBegin(TE_FIELD_DESC);
        this.te.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("get_count_result(");
      boolean first = true;
      sb.append("success:");
      sb.append(this.success);
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ue:");
      if (this.ue == null) {
        sb.append("null");
      } else {
        sb.append(this.ue);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("te:");
      if (this.te == null) {
        sb.append("null");
      } else {
        sb.append(this.te);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class multiget_slice_args implements TBase<multiget_slice_args, multiget_slice_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("multiget_slice_args");
    private static final TField KEYS_FIELD_DESC = new TField("keys", TType.LIST, (short)1);
    private static final TField COLUMN_PARENT_FIELD_DESC = new TField("column_parent", TType.STRUCT, (short)2);
    private static final TField PREDICATE_FIELD_DESC = new TField("predicate", TType.STRUCT, (short)3);
    private static final TField CONSISTENCY_LEVEL_FIELD_DESC = new TField("consistency_level", TType.I32, (short)4);
    public List<ByteBuffer> keys;
    public ColumnParent column_parent;
    public SlicePredicate predicate;
    public ConsistencyLevel consistency_level;
    public enum _Fields implements TFieldIdEnum {
      KEYS((short)1, "keys"),
      COLUMN_PARENT((short)2, "column_parent"),
      PREDICATE((short)3, "predicate"),
      CONSISTENCY_LEVEL((short)4, "consistency_level");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return KEYS;
          case 2: 
            return COLUMN_PARENT;
          case 3: 
            return PREDICATE;
          case 4: 
            return CONSISTENCY_LEVEL;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.KEYS, new FieldMetaData("keys", TFieldRequirementType.REQUIRED, 
          new ListMetaData(TType.LIST, 
              new FieldValueMetaData(TType.STRING))));
      tmpMap.put(_Fields.COLUMN_PARENT, new FieldMetaData("column_parent", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, ColumnParent.class)));
      tmpMap.put(_Fields.PREDICATE, new FieldMetaData("predicate", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, SlicePredicate.class)));
      tmpMap.put(_Fields.CONSISTENCY_LEVEL, new FieldMetaData("consistency_level", TFieldRequirementType.REQUIRED, 
          new EnumMetaData(TType.ENUM, ConsistencyLevel.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(multiget_slice_args.class, metaDataMap);
    }
    public multiget_slice_args() {
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public multiget_slice_args(
      List<ByteBuffer> keys,
      ColumnParent column_parent,
      SlicePredicate predicate,
      ConsistencyLevel consistency_level)
    {
      this();
      this.keys = keys;
      this.column_parent = column_parent;
      this.predicate = predicate;
      this.consistency_level = consistency_level;
    }
    public multiget_slice_args(multiget_slice_args other) {
      if (other.isSetKeys()) {
        List<ByteBuffer> __this__keys = new ArrayList<ByteBuffer>();
        for (ByteBuffer other_element : other.keys) {
          ByteBuffer temp_binary_element = TBaseHelper.copyBinary(other_element);
;
          __this__keys.add(temp_binary_element);
        }
        this.keys = __this__keys;
      }
      if (other.isSetColumn_parent()) {
        this.column_parent = new ColumnParent(other.column_parent);
      }
      if (other.isSetPredicate()) {
        this.predicate = new SlicePredicate(other.predicate);
      }
      if (other.isSetConsistency_level()) {
        this.consistency_level = other.consistency_level;
      }
    }
    public multiget_slice_args deepCopy() {
      return new multiget_slice_args(this);
    }
    @Override
    public void clear() {
      this.keys = null;
      this.column_parent = null;
      this.predicate = null;
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public int getKeysSize() {
      return (this.keys == null) ? 0 : this.keys.size();
    }
    public java.util.Iterator<ByteBuffer> getKeysIterator() {
      return (this.keys == null) ? null : this.keys.iterator();
    }
    public void addToKeys(ByteBuffer elem) {
      if (this.keys == null) {
        this.keys = new ArrayList<ByteBuffer>();
      }
      this.keys.add(elem);
    }
    public List<ByteBuffer> getKeys() {
      return this.keys;
    }
    public multiget_slice_args setKeys(List<ByteBuffer> keys) {
      this.keys = keys;
      return this;
    }
    public void unsetKeys() {
      this.keys = null;
    }
    public boolean isSetKeys() {
      return this.keys != null;
    }
    public void setKeysIsSet(boolean value) {
      if (!value) {
        this.keys = null;
      }
    }
    public ColumnParent getColumn_parent() {
      return this.column_parent;
    }
    public multiget_slice_args setColumn_parent(ColumnParent column_parent) {
      this.column_parent = column_parent;
      return this;
    }
    public void unsetColumn_parent() {
      this.column_parent = null;
    }
    public boolean isSetColumn_parent() {
      return this.column_parent != null;
    }
    public void setColumn_parentIsSet(boolean value) {
      if (!value) {
        this.column_parent = null;
      }
    }
    public SlicePredicate getPredicate() {
      return this.predicate;
    }
    public multiget_slice_args setPredicate(SlicePredicate predicate) {
      this.predicate = predicate;
      return this;
    }
    public void unsetPredicate() {
      this.predicate = null;
    }
    public boolean isSetPredicate() {
      return this.predicate != null;
    }
    public void setPredicateIsSet(boolean value) {
      if (!value) {
        this.predicate = null;
      }
    }
    public ConsistencyLevel getConsistency_level() {
      return this.consistency_level;
    }
    public multiget_slice_args setConsistency_level(ConsistencyLevel consistency_level) {
      this.consistency_level = consistency_level;
      return this;
    }
    public void unsetConsistency_level() {
      this.consistency_level = null;
    }
    public boolean isSetConsistency_level() {
      return this.consistency_level != null;
    }
    public void setConsistency_levelIsSet(boolean value) {
      if (!value) {
        this.consistency_level = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case KEYS:
        if (value == null) {
          unsetKeys();
        } else {
          setKeys((List<ByteBuffer>)value);
        }
        break;
      case COLUMN_PARENT:
        if (value == null) {
          unsetColumn_parent();
        } else {
          setColumn_parent((ColumnParent)value);
        }
        break;
      case PREDICATE:
        if (value == null) {
          unsetPredicate();
        } else {
          setPredicate((SlicePredicate)value);
        }
        break;
      case CONSISTENCY_LEVEL:
        if (value == null) {
          unsetConsistency_level();
        } else {
          setConsistency_level((ConsistencyLevel)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case KEYS:
        return getKeys();
      case COLUMN_PARENT:
        return getColumn_parent();
      case PREDICATE:
        return getPredicate();
      case CONSISTENCY_LEVEL:
        return getConsistency_level();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case KEYS:
        return isSetKeys();
      case COLUMN_PARENT:
        return isSetColumn_parent();
      case PREDICATE:
        return isSetPredicate();
      case CONSISTENCY_LEVEL:
        return isSetConsistency_level();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof multiget_slice_args)
        return this.equals((multiget_slice_args)that);
      return false;
    }
    public boolean equals(multiget_slice_args that) {
      if (that == null)
        return false;
      boolean this_present_keys = true && this.isSetKeys();
      boolean that_present_keys = true && that.isSetKeys();
      if (this_present_keys || that_present_keys) {
        if (!(this_present_keys && that_present_keys))
          return false;
        if (!this.keys.equals(that.keys))
          return false;
      }
      boolean this_present_column_parent = true && this.isSetColumn_parent();
      boolean that_present_column_parent = true && that.isSetColumn_parent();
      if (this_present_column_parent || that_present_column_parent) {
        if (!(this_present_column_parent && that_present_column_parent))
          return false;
        if (!this.column_parent.equals(that.column_parent))
          return false;
      }
      boolean this_present_predicate = true && this.isSetPredicate();
      boolean that_present_predicate = true && that.isSetPredicate();
      if (this_present_predicate || that_present_predicate) {
        if (!(this_present_predicate && that_present_predicate))
          return false;
        if (!this.predicate.equals(that.predicate))
          return false;
      }
      boolean this_present_consistency_level = true && this.isSetConsistency_level();
      boolean that_present_consistency_level = true && that.isSetConsistency_level();
      if (this_present_consistency_level || that_present_consistency_level) {
        if (!(this_present_consistency_level && that_present_consistency_level))
          return false;
        if (!this.consistency_level.equals(that.consistency_level))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_keys = true && (isSetKeys());
      builder.append(present_keys);
      if (present_keys)
        builder.append(keys);
      boolean present_column_parent = true && (isSetColumn_parent());
      builder.append(present_column_parent);
      if (present_column_parent)
        builder.append(column_parent);
      boolean present_predicate = true && (isSetPredicate());
      builder.append(present_predicate);
      if (present_predicate)
        builder.append(predicate);
      boolean present_consistency_level = true && (isSetConsistency_level());
      builder.append(present_consistency_level);
      if (present_consistency_level)
        builder.append(consistency_level.getValue());
      return builder.toHashCode();
    }
    public int compareTo(multiget_slice_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      multiget_slice_args typedOther = (multiget_slice_args)other;
      lastComparison = Boolean.valueOf(isSetKeys()).compareTo(typedOther.isSetKeys());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetKeys()) {
        lastComparison = TBaseHelper.compareTo(this.keys, typedOther.keys);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetColumn_parent()).compareTo(typedOther.isSetColumn_parent());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetColumn_parent()) {
        lastComparison = TBaseHelper.compareTo(this.column_parent, typedOther.column_parent);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetPredicate()).compareTo(typedOther.isSetPredicate());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetPredicate()) {
        lastComparison = TBaseHelper.compareTo(this.predicate, typedOther.predicate);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetConsistency_level()).compareTo(typedOther.isSetConsistency_level());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetConsistency_level()) {
        lastComparison = TBaseHelper.compareTo(this.consistency_level, typedOther.consistency_level);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.LIST) {
              {
                TList _list54 = iprot.readListBegin();
                this.keys = new ArrayList<ByteBuffer>(_list54.size);
                for (int _i55 = 0; _i55 < _list54.size; ++_i55)
                {
                  ByteBuffer _elem56;
                  _elem56 = iprot.readBinary();
                  this.keys.add(_elem56);
                }
                iprot.readListEnd();
              }
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.column_parent = new ColumnParent();
              this.column_parent.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.predicate = new SlicePredicate();
              this.predicate.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 4: 
            if (field.type == TType.I32) {
              this.consistency_level = ConsistencyLevel.findByValue(iprot.readI32());
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.keys != null) {
        oprot.writeFieldBegin(KEYS_FIELD_DESC);
        {
          oprot.writeListBegin(new TList(TType.STRING, this.keys.size()));
          for (ByteBuffer _iter57 : this.keys)
          {
            oprot.writeBinary(_iter57);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (this.column_parent != null) {
        oprot.writeFieldBegin(COLUMN_PARENT_FIELD_DESC);
        this.column_parent.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.predicate != null) {
        oprot.writeFieldBegin(PREDICATE_FIELD_DESC);
        this.predicate.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.consistency_level != null) {
        oprot.writeFieldBegin(CONSISTENCY_LEVEL_FIELD_DESC);
        oprot.writeI32(this.consistency_level.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("multiget_slice_args(");
      boolean first = true;
      sb.append("keys:");
      if (this.keys == null) {
        sb.append("null");
      } else {
        sb.append(this.keys);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("column_parent:");
      if (this.column_parent == null) {
        sb.append("null");
      } else {
        sb.append(this.column_parent);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("predicate:");
      if (this.predicate == null) {
        sb.append("null");
      } else {
        sb.append(this.predicate);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("consistency_level:");
      if (this.consistency_level == null) {
        sb.append("null");
      } else {
        sb.append(this.consistency_level);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (keys == null) {
        throw new TProtocolException("Required field 'keys' was not present! Struct: " + toString());
      }
      if (column_parent == null) {
        throw new TProtocolException("Required field 'column_parent' was not present! Struct: " + toString());
      }
      if (predicate == null) {
        throw new TProtocolException("Required field 'predicate' was not present! Struct: " + toString());
      }
      if (consistency_level == null) {
        throw new TProtocolException("Required field 'consistency_level' was not present! Struct: " + toString());
      }
    }
  }
  public static class multiget_slice_result implements TBase<multiget_slice_result, multiget_slice_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("multiget_slice_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.MAP, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    private static final TField UE_FIELD_DESC = new TField("ue", TType.STRUCT, (short)2);
    private static final TField TE_FIELD_DESC = new TField("te", TType.STRUCT, (short)3);
    public Map<ByteBuffer,List<ColumnOrSuperColumn>> success;
    public InvalidRequestException ire;
    public UnavailableException ue;
    public TimedOutException te;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire"),
      UE((short)2, "ue"),
      TE((short)3, "te");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          case 2: 
            return UE;
          case 3: 
            return TE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new MapMetaData(TType.MAP, 
              new FieldValueMetaData(TType.STRING), 
              new ListMetaData(TType.LIST, 
                  new StructMetaData(TType.STRUCT, ColumnOrSuperColumn.class)))));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.UE, new FieldMetaData("ue", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.TE, new FieldMetaData("te", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(multiget_slice_result.class, metaDataMap);
    }
    public multiget_slice_result() {
    }
    public multiget_slice_result(
      Map<ByteBuffer,List<ColumnOrSuperColumn>> success,
      InvalidRequestException ire,
      UnavailableException ue,
      TimedOutException te)
    {
      this();
      this.success = success;
      this.ire = ire;
      this.ue = ue;
      this.te = te;
    }
    public multiget_slice_result(multiget_slice_result other) {
      if (other.isSetSuccess()) {
        Map<ByteBuffer,List<ColumnOrSuperColumn>> __this__success = new HashMap<ByteBuffer,List<ColumnOrSuperColumn>>();
        for (Map.Entry<ByteBuffer, List<ColumnOrSuperColumn>> other_element : other.success.entrySet()) {
          ByteBuffer other_element_key = other_element.getKey();
          List<ColumnOrSuperColumn> other_element_value = other_element.getValue();
          ByteBuffer __this__success_copy_key = TBaseHelper.copyBinary(other_element_key);
;
          List<ColumnOrSuperColumn> __this__success_copy_value = new ArrayList<ColumnOrSuperColumn>();
          for (ColumnOrSuperColumn other_element_value_element : other_element_value) {
            __this__success_copy_value.add(new ColumnOrSuperColumn(other_element_value_element));
          }
          __this__success.put(__this__success_copy_key, __this__success_copy_value);
        }
        this.success = __this__success;
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
      if (other.isSetUe()) {
        this.ue = new UnavailableException(other.ue);
      }
      if (other.isSetTe()) {
        this.te = new TimedOutException(other.te);
      }
    }
    public multiget_slice_result deepCopy() {
      return new multiget_slice_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.ire = null;
      this.ue = null;
      this.te = null;
    }
    public int getSuccessSize() {
      return (this.success == null) ? 0 : this.success.size();
    }
    public void putToSuccess(ByteBuffer key, List<ColumnOrSuperColumn> val) {
      if (this.success == null) {
        this.success = new HashMap<ByteBuffer,List<ColumnOrSuperColumn>>();
      }
      this.success.put(key, val);
    }
    public Map<ByteBuffer,List<ColumnOrSuperColumn>> getSuccess() {
      return this.success;
    }
    public multiget_slice_result setSuccess(Map<ByteBuffer,List<ColumnOrSuperColumn>> success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public multiget_slice_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public UnavailableException getUe() {
      return this.ue;
    }
    public multiget_slice_result setUe(UnavailableException ue) {
      this.ue = ue;
      return this;
    }
    public void unsetUe() {
      this.ue = null;
    }
    public boolean isSetUe() {
      return this.ue != null;
    }
    public void setUeIsSet(boolean value) {
      if (!value) {
        this.ue = null;
      }
    }
    public TimedOutException getTe() {
      return this.te;
    }
    public multiget_slice_result setTe(TimedOutException te) {
      this.te = te;
      return this;
    }
    public void unsetTe() {
      this.te = null;
    }
    public boolean isSetTe() {
      return this.te != null;
    }
    public void setTeIsSet(boolean value) {
      if (!value) {
        this.te = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((Map<ByteBuffer,List<ColumnOrSuperColumn>>)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      case UE:
        if (value == null) {
          unsetUe();
        } else {
          setUe((UnavailableException)value);
        }
        break;
      case TE:
        if (value == null) {
          unsetTe();
        } else {
          setTe((TimedOutException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case IRE:
        return getIre();
      case UE:
        return getUe();
      case TE:
        return getTe();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      case UE:
        return isSetUe();
      case TE:
        return isSetTe();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof multiget_slice_result)
        return this.equals((multiget_slice_result)that);
      return false;
    }
    public boolean equals(multiget_slice_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      boolean this_present_ue = true && this.isSetUe();
      boolean that_present_ue = true && that.isSetUe();
      if (this_present_ue || that_present_ue) {
        if (!(this_present_ue && that_present_ue))
          return false;
        if (!this.ue.equals(that.ue))
          return false;
      }
      boolean this_present_te = true && this.isSetTe();
      boolean that_present_te = true && that.isSetTe();
      if (this_present_te || that_present_te) {
        if (!(this_present_te && that_present_te))
          return false;
        if (!this.te.equals(that.te))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      boolean present_ue = true && (isSetUe());
      builder.append(present_ue);
      if (present_ue)
        builder.append(ue);
      boolean present_te = true && (isSetTe());
      builder.append(present_te);
      if (present_te)
        builder.append(te);
      return builder.toHashCode();
    }
    public int compareTo(multiget_slice_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      multiget_slice_result typedOther = (multiget_slice_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetUe()).compareTo(typedOther.isSetUe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetUe()) {
        lastComparison = TBaseHelper.compareTo(this.ue, typedOther.ue);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetTe()).compareTo(typedOther.isSetTe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetTe()) {
        lastComparison = TBaseHelper.compareTo(this.te, typedOther.te);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.MAP) {
              {
                TMap _map58 = iprot.readMapBegin();
                this.success = new HashMap<ByteBuffer,List<ColumnOrSuperColumn>>(2*_map58.size);
                for (int _i59 = 0; _i59 < _map58.size; ++_i59)
                {
                  ByteBuffer _key60;
                  List<ColumnOrSuperColumn> _val61;
                  _key60 = iprot.readBinary();
                  {
                    TList _list62 = iprot.readListBegin();
                    _val61 = new ArrayList<ColumnOrSuperColumn>(_list62.size);
                    for (int _i63 = 0; _i63 < _list62.size; ++_i63)
                    {
                      ColumnOrSuperColumn _elem64;
                      _elem64 = new ColumnOrSuperColumn();
                      _elem64.read(iprot);
                      _val61.add(_elem64);
                    }
                    iprot.readListEnd();
                  }
                  this.success.put(_key60, _val61);
                }
                iprot.readMapEnd();
              }
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.ue = new UnavailableException();
              this.ue.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.te = new TimedOutException();
              this.te.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        {
          oprot.writeMapBegin(new TMap(TType.STRING, TType.LIST, this.success.size()));
          for (Map.Entry<ByteBuffer, List<ColumnOrSuperColumn>> _iter65 : this.success.entrySet())
          {
            oprot.writeBinary(_iter65.getKey());
            {
              oprot.writeListBegin(new TList(TType.STRUCT, _iter65.getValue().size()));
              for (ColumnOrSuperColumn _iter66 : _iter65.getValue())
              {
                _iter66.write(oprot);
              }
              oprot.writeListEnd();
            }
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetUe()) {
        oprot.writeFieldBegin(UE_FIELD_DESC);
        this.ue.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetTe()) {
        oprot.writeFieldBegin(TE_FIELD_DESC);
        this.te.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("multiget_slice_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ue:");
      if (this.ue == null) {
        sb.append("null");
      } else {
        sb.append(this.ue);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("te:");
      if (this.te == null) {
        sb.append("null");
      } else {
        sb.append(this.te);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class multiget_count_args implements TBase<multiget_count_args, multiget_count_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("multiget_count_args");
    private static final TField KEYS_FIELD_DESC = new TField("keys", TType.LIST, (short)1);
    private static final TField COLUMN_PARENT_FIELD_DESC = new TField("column_parent", TType.STRUCT, (short)2);
    private static final TField PREDICATE_FIELD_DESC = new TField("predicate", TType.STRUCT, (short)3);
    private static final TField CONSISTENCY_LEVEL_FIELD_DESC = new TField("consistency_level", TType.I32, (short)4);
    public List<ByteBuffer> keys;
    public ColumnParent column_parent;
    public SlicePredicate predicate;
    public ConsistencyLevel consistency_level;
    public enum _Fields implements TFieldIdEnum {
      KEYS((short)1, "keys"),
      COLUMN_PARENT((short)2, "column_parent"),
      PREDICATE((short)3, "predicate"),
      CONSISTENCY_LEVEL((short)4, "consistency_level");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return KEYS;
          case 2: 
            return COLUMN_PARENT;
          case 3: 
            return PREDICATE;
          case 4: 
            return CONSISTENCY_LEVEL;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.KEYS, new FieldMetaData("keys", TFieldRequirementType.REQUIRED, 
          new ListMetaData(TType.LIST, 
              new FieldValueMetaData(TType.STRING))));
      tmpMap.put(_Fields.COLUMN_PARENT, new FieldMetaData("column_parent", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, ColumnParent.class)));
      tmpMap.put(_Fields.PREDICATE, new FieldMetaData("predicate", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, SlicePredicate.class)));
      tmpMap.put(_Fields.CONSISTENCY_LEVEL, new FieldMetaData("consistency_level", TFieldRequirementType.REQUIRED, 
          new EnumMetaData(TType.ENUM, ConsistencyLevel.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(multiget_count_args.class, metaDataMap);
    }
    public multiget_count_args() {
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public multiget_count_args(
      List<ByteBuffer> keys,
      ColumnParent column_parent,
      SlicePredicate predicate,
      ConsistencyLevel consistency_level)
    {
      this();
      this.keys = keys;
      this.column_parent = column_parent;
      this.predicate = predicate;
      this.consistency_level = consistency_level;
    }
    public multiget_count_args(multiget_count_args other) {
      if (other.isSetKeys()) {
        List<ByteBuffer> __this__keys = new ArrayList<ByteBuffer>();
        for (ByteBuffer other_element : other.keys) {
          ByteBuffer temp_binary_element = TBaseHelper.copyBinary(other_element);
;
          __this__keys.add(temp_binary_element);
        }
        this.keys = __this__keys;
      }
      if (other.isSetColumn_parent()) {
        this.column_parent = new ColumnParent(other.column_parent);
      }
      if (other.isSetPredicate()) {
        this.predicate = new SlicePredicate(other.predicate);
      }
      if (other.isSetConsistency_level()) {
        this.consistency_level = other.consistency_level;
      }
    }
    public multiget_count_args deepCopy() {
      return new multiget_count_args(this);
    }
    @Override
    public void clear() {
      this.keys = null;
      this.column_parent = null;
      this.predicate = null;
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public int getKeysSize() {
      return (this.keys == null) ? 0 : this.keys.size();
    }
    public java.util.Iterator<ByteBuffer> getKeysIterator() {
      return (this.keys == null) ? null : this.keys.iterator();
    }
    public void addToKeys(ByteBuffer elem) {
      if (this.keys == null) {
        this.keys = new ArrayList<ByteBuffer>();
      }
      this.keys.add(elem);
    }
    public List<ByteBuffer> getKeys() {
      return this.keys;
    }
    public multiget_count_args setKeys(List<ByteBuffer> keys) {
      this.keys = keys;
      return this;
    }
    public void unsetKeys() {
      this.keys = null;
    }
    public boolean isSetKeys() {
      return this.keys != null;
    }
    public void setKeysIsSet(boolean value) {
      if (!value) {
        this.keys = null;
      }
    }
    public ColumnParent getColumn_parent() {
      return this.column_parent;
    }
    public multiget_count_args setColumn_parent(ColumnParent column_parent) {
      this.column_parent = column_parent;
      return this;
    }
    public void unsetColumn_parent() {
      this.column_parent = null;
    }
    public boolean isSetColumn_parent() {
      return this.column_parent != null;
    }
    public void setColumn_parentIsSet(boolean value) {
      if (!value) {
        this.column_parent = null;
      }
    }
    public SlicePredicate getPredicate() {
      return this.predicate;
    }
    public multiget_count_args setPredicate(SlicePredicate predicate) {
      this.predicate = predicate;
      return this;
    }
    public void unsetPredicate() {
      this.predicate = null;
    }
    public boolean isSetPredicate() {
      return this.predicate != null;
    }
    public void setPredicateIsSet(boolean value) {
      if (!value) {
        this.predicate = null;
      }
    }
    public ConsistencyLevel getConsistency_level() {
      return this.consistency_level;
    }
    public multiget_count_args setConsistency_level(ConsistencyLevel consistency_level) {
      this.consistency_level = consistency_level;
      return this;
    }
    public void unsetConsistency_level() {
      this.consistency_level = null;
    }
    public boolean isSetConsistency_level() {
      return this.consistency_level != null;
    }
    public void setConsistency_levelIsSet(boolean value) {
      if (!value) {
        this.consistency_level = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case KEYS:
        if (value == null) {
          unsetKeys();
        } else {
          setKeys((List<ByteBuffer>)value);
        }
        break;
      case COLUMN_PARENT:
        if (value == null) {
          unsetColumn_parent();
        } else {
          setColumn_parent((ColumnParent)value);
        }
        break;
      case PREDICATE:
        if (value == null) {
          unsetPredicate();
        } else {
          setPredicate((SlicePredicate)value);
        }
        break;
      case CONSISTENCY_LEVEL:
        if (value == null) {
          unsetConsistency_level();
        } else {
          setConsistency_level((ConsistencyLevel)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case KEYS:
        return getKeys();
      case COLUMN_PARENT:
        return getColumn_parent();
      case PREDICATE:
        return getPredicate();
      case CONSISTENCY_LEVEL:
        return getConsistency_level();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case KEYS:
        return isSetKeys();
      case COLUMN_PARENT:
        return isSetColumn_parent();
      case PREDICATE:
        return isSetPredicate();
      case CONSISTENCY_LEVEL:
        return isSetConsistency_level();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof multiget_count_args)
        return this.equals((multiget_count_args)that);
      return false;
    }
    public boolean equals(multiget_count_args that) {
      if (that == null)
        return false;
      boolean this_present_keys = true && this.isSetKeys();
      boolean that_present_keys = true && that.isSetKeys();
      if (this_present_keys || that_present_keys) {
        if (!(this_present_keys && that_present_keys))
          return false;
        if (!this.keys.equals(that.keys))
          return false;
      }
      boolean this_present_column_parent = true && this.isSetColumn_parent();
      boolean that_present_column_parent = true && that.isSetColumn_parent();
      if (this_present_column_parent || that_present_column_parent) {
        if (!(this_present_column_parent && that_present_column_parent))
          return false;
        if (!this.column_parent.equals(that.column_parent))
          return false;
      }
      boolean this_present_predicate = true && this.isSetPredicate();
      boolean that_present_predicate = true && that.isSetPredicate();
      if (this_present_predicate || that_present_predicate) {
        if (!(this_present_predicate && that_present_predicate))
          return false;
        if (!this.predicate.equals(that.predicate))
          return false;
      }
      boolean this_present_consistency_level = true && this.isSetConsistency_level();
      boolean that_present_consistency_level = true && that.isSetConsistency_level();
      if (this_present_consistency_level || that_present_consistency_level) {
        if (!(this_present_consistency_level && that_present_consistency_level))
          return false;
        if (!this.consistency_level.equals(that.consistency_level))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_keys = true && (isSetKeys());
      builder.append(present_keys);
      if (present_keys)
        builder.append(keys);
      boolean present_column_parent = true && (isSetColumn_parent());
      builder.append(present_column_parent);
      if (present_column_parent)
        builder.append(column_parent);
      boolean present_predicate = true && (isSetPredicate());
      builder.append(present_predicate);
      if (present_predicate)
        builder.append(predicate);
      boolean present_consistency_level = true && (isSetConsistency_level());
      builder.append(present_consistency_level);
      if (present_consistency_level)
        builder.append(consistency_level.getValue());
      return builder.toHashCode();
    }
    public int compareTo(multiget_count_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      multiget_count_args typedOther = (multiget_count_args)other;
      lastComparison = Boolean.valueOf(isSetKeys()).compareTo(typedOther.isSetKeys());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetKeys()) {
        lastComparison = TBaseHelper.compareTo(this.keys, typedOther.keys);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetColumn_parent()).compareTo(typedOther.isSetColumn_parent());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetColumn_parent()) {
        lastComparison = TBaseHelper.compareTo(this.column_parent, typedOther.column_parent);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetPredicate()).compareTo(typedOther.isSetPredicate());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetPredicate()) {
        lastComparison = TBaseHelper.compareTo(this.predicate, typedOther.predicate);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetConsistency_level()).compareTo(typedOther.isSetConsistency_level());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetConsistency_level()) {
        lastComparison = TBaseHelper.compareTo(this.consistency_level, typedOther.consistency_level);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.LIST) {
              {
                TList _list67 = iprot.readListBegin();
                this.keys = new ArrayList<ByteBuffer>(_list67.size);
                for (int _i68 = 0; _i68 < _list67.size; ++_i68)
                {
                  ByteBuffer _elem69;
                  _elem69 = iprot.readBinary();
                  this.keys.add(_elem69);
                }
                iprot.readListEnd();
              }
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.column_parent = new ColumnParent();
              this.column_parent.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.predicate = new SlicePredicate();
              this.predicate.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 4: 
            if (field.type == TType.I32) {
              this.consistency_level = ConsistencyLevel.findByValue(iprot.readI32());
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.keys != null) {
        oprot.writeFieldBegin(KEYS_FIELD_DESC);
        {
          oprot.writeListBegin(new TList(TType.STRING, this.keys.size()));
          for (ByteBuffer _iter70 : this.keys)
          {
            oprot.writeBinary(_iter70);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (this.column_parent != null) {
        oprot.writeFieldBegin(COLUMN_PARENT_FIELD_DESC);
        this.column_parent.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.predicate != null) {
        oprot.writeFieldBegin(PREDICATE_FIELD_DESC);
        this.predicate.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.consistency_level != null) {
        oprot.writeFieldBegin(CONSISTENCY_LEVEL_FIELD_DESC);
        oprot.writeI32(this.consistency_level.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("multiget_count_args(");
      boolean first = true;
      sb.append("keys:");
      if (this.keys == null) {
        sb.append("null");
      } else {
        sb.append(this.keys);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("column_parent:");
      if (this.column_parent == null) {
        sb.append("null");
      } else {
        sb.append(this.column_parent);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("predicate:");
      if (this.predicate == null) {
        sb.append("null");
      } else {
        sb.append(this.predicate);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("consistency_level:");
      if (this.consistency_level == null) {
        sb.append("null");
      } else {
        sb.append(this.consistency_level);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (keys == null) {
        throw new TProtocolException("Required field 'keys' was not present! Struct: " + toString());
      }
      if (column_parent == null) {
        throw new TProtocolException("Required field 'column_parent' was not present! Struct: " + toString());
      }
      if (predicate == null) {
        throw new TProtocolException("Required field 'predicate' was not present! Struct: " + toString());
      }
      if (consistency_level == null) {
        throw new TProtocolException("Required field 'consistency_level' was not present! Struct: " + toString());
      }
    }
  }
  public static class multiget_count_result implements TBase<multiget_count_result, multiget_count_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("multiget_count_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.MAP, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    private static final TField UE_FIELD_DESC = new TField("ue", TType.STRUCT, (short)2);
    private static final TField TE_FIELD_DESC = new TField("te", TType.STRUCT, (short)3);
    public Map<ByteBuffer,Integer> success;
    public InvalidRequestException ire;
    public UnavailableException ue;
    public TimedOutException te;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire"),
      UE((short)2, "ue"),
      TE((short)3, "te");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          case 2: 
            return UE;
          case 3: 
            return TE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new MapMetaData(TType.MAP, 
              new FieldValueMetaData(TType.STRING), 
              new FieldValueMetaData(TType.I32))));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.UE, new FieldMetaData("ue", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.TE, new FieldMetaData("te", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(multiget_count_result.class, metaDataMap);
    }
    public multiget_count_result() {
    }
    public multiget_count_result(
      Map<ByteBuffer,Integer> success,
      InvalidRequestException ire,
      UnavailableException ue,
      TimedOutException te)
    {
      this();
      this.success = success;
      this.ire = ire;
      this.ue = ue;
      this.te = te;
    }
    public multiget_count_result(multiget_count_result other) {
      if (other.isSetSuccess()) {
        Map<ByteBuffer,Integer> __this__success = new HashMap<ByteBuffer,Integer>();
        for (Map.Entry<ByteBuffer, Integer> other_element : other.success.entrySet()) {
          ByteBuffer other_element_key = other_element.getKey();
          Integer other_element_value = other_element.getValue();
          ByteBuffer __this__success_copy_key = TBaseHelper.copyBinary(other_element_key);
;
          Integer __this__success_copy_value = other_element_value;
          __this__success.put(__this__success_copy_key, __this__success_copy_value);
        }
        this.success = __this__success;
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
      if (other.isSetUe()) {
        this.ue = new UnavailableException(other.ue);
      }
      if (other.isSetTe()) {
        this.te = new TimedOutException(other.te);
      }
    }
    public multiget_count_result deepCopy() {
      return new multiget_count_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.ire = null;
      this.ue = null;
      this.te = null;
    }
    public int getSuccessSize() {
      return (this.success == null) ? 0 : this.success.size();
    }
    public void putToSuccess(ByteBuffer key, int val) {
      if (this.success == null) {
        this.success = new HashMap<ByteBuffer,Integer>();
      }
      this.success.put(key, val);
    }
    public Map<ByteBuffer,Integer> getSuccess() {
      return this.success;
    }
    public multiget_count_result setSuccess(Map<ByteBuffer,Integer> success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public multiget_count_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public UnavailableException getUe() {
      return this.ue;
    }
    public multiget_count_result setUe(UnavailableException ue) {
      this.ue = ue;
      return this;
    }
    public void unsetUe() {
      this.ue = null;
    }
    public boolean isSetUe() {
      return this.ue != null;
    }
    public void setUeIsSet(boolean value) {
      if (!value) {
        this.ue = null;
      }
    }
    public TimedOutException getTe() {
      return this.te;
    }
    public multiget_count_result setTe(TimedOutException te) {
      this.te = te;
      return this;
    }
    public void unsetTe() {
      this.te = null;
    }
    public boolean isSetTe() {
      return this.te != null;
    }
    public void setTeIsSet(boolean value) {
      if (!value) {
        this.te = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((Map<ByteBuffer,Integer>)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      case UE:
        if (value == null) {
          unsetUe();
        } else {
          setUe((UnavailableException)value);
        }
        break;
      case TE:
        if (value == null) {
          unsetTe();
        } else {
          setTe((TimedOutException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case IRE:
        return getIre();
      case UE:
        return getUe();
      case TE:
        return getTe();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      case UE:
        return isSetUe();
      case TE:
        return isSetTe();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof multiget_count_result)
        return this.equals((multiget_count_result)that);
      return false;
    }
    public boolean equals(multiget_count_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      boolean this_present_ue = true && this.isSetUe();
      boolean that_present_ue = true && that.isSetUe();
      if (this_present_ue || that_present_ue) {
        if (!(this_present_ue && that_present_ue))
          return false;
        if (!this.ue.equals(that.ue))
          return false;
      }
      boolean this_present_te = true && this.isSetTe();
      boolean that_present_te = true && that.isSetTe();
      if (this_present_te || that_present_te) {
        if (!(this_present_te && that_present_te))
          return false;
        if (!this.te.equals(that.te))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      boolean present_ue = true && (isSetUe());
      builder.append(present_ue);
      if (present_ue)
        builder.append(ue);
      boolean present_te = true && (isSetTe());
      builder.append(present_te);
      if (present_te)
        builder.append(te);
      return builder.toHashCode();
    }
    public int compareTo(multiget_count_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      multiget_count_result typedOther = (multiget_count_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetUe()).compareTo(typedOther.isSetUe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetUe()) {
        lastComparison = TBaseHelper.compareTo(this.ue, typedOther.ue);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetTe()).compareTo(typedOther.isSetTe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetTe()) {
        lastComparison = TBaseHelper.compareTo(this.te, typedOther.te);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.MAP) {
              {
                TMap _map71 = iprot.readMapBegin();
                this.success = new HashMap<ByteBuffer,Integer>(2*_map71.size);
                for (int _i72 = 0; _i72 < _map71.size; ++_i72)
                {
                  ByteBuffer _key73;
                  int _val74;
                  _key73 = iprot.readBinary();
                  _val74 = iprot.readI32();
                  this.success.put(_key73, _val74);
                }
                iprot.readMapEnd();
              }
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.ue = new UnavailableException();
              this.ue.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.te = new TimedOutException();
              this.te.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        {
          oprot.writeMapBegin(new TMap(TType.STRING, TType.I32, this.success.size()));
          for (Map.Entry<ByteBuffer, Integer> _iter75 : this.success.entrySet())
          {
            oprot.writeBinary(_iter75.getKey());
            oprot.writeI32(_iter75.getValue());
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetUe()) {
        oprot.writeFieldBegin(UE_FIELD_DESC);
        this.ue.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetTe()) {
        oprot.writeFieldBegin(TE_FIELD_DESC);
        this.te.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("multiget_count_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ue:");
      if (this.ue == null) {
        sb.append("null");
      } else {
        sb.append(this.ue);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("te:");
      if (this.te == null) {
        sb.append("null");
      } else {
        sb.append(this.te);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class get_range_slices_args implements TBase<get_range_slices_args, get_range_slices_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("get_range_slices_args");
    private static final TField COLUMN_PARENT_FIELD_DESC = new TField("column_parent", TType.STRUCT, (short)1);
    private static final TField PREDICATE_FIELD_DESC = new TField("predicate", TType.STRUCT, (short)2);
    private static final TField RANGE_FIELD_DESC = new TField("range", TType.STRUCT, (short)3);
    private static final TField CONSISTENCY_LEVEL_FIELD_DESC = new TField("consistency_level", TType.I32, (short)4);
    public ColumnParent column_parent;
    public SlicePredicate predicate;
    public KeyRange range;
    public ConsistencyLevel consistency_level;
    public enum _Fields implements TFieldIdEnum {
      COLUMN_PARENT((short)1, "column_parent"),
      PREDICATE((short)2, "predicate"),
      RANGE((short)3, "range"),
      CONSISTENCY_LEVEL((short)4, "consistency_level");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return COLUMN_PARENT;
          case 2: 
            return PREDICATE;
          case 3: 
            return RANGE;
          case 4: 
            return CONSISTENCY_LEVEL;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.COLUMN_PARENT, new FieldMetaData("column_parent", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, ColumnParent.class)));
      tmpMap.put(_Fields.PREDICATE, new FieldMetaData("predicate", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, SlicePredicate.class)));
      tmpMap.put(_Fields.RANGE, new FieldMetaData("range", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, KeyRange.class)));
      tmpMap.put(_Fields.CONSISTENCY_LEVEL, new FieldMetaData("consistency_level", TFieldRequirementType.REQUIRED, 
          new EnumMetaData(TType.ENUM, ConsistencyLevel.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(get_range_slices_args.class, metaDataMap);
    }
    public get_range_slices_args() {
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public get_range_slices_args(
      ColumnParent column_parent,
      SlicePredicate predicate,
      KeyRange range,
      ConsistencyLevel consistency_level)
    {
      this();
      this.column_parent = column_parent;
      this.predicate = predicate;
      this.range = range;
      this.consistency_level = consistency_level;
    }
    public get_range_slices_args(get_range_slices_args other) {
      if (other.isSetColumn_parent()) {
        this.column_parent = new ColumnParent(other.column_parent);
      }
      if (other.isSetPredicate()) {
        this.predicate = new SlicePredicate(other.predicate);
      }
      if (other.isSetRange()) {
        this.range = new KeyRange(other.range);
      }
      if (other.isSetConsistency_level()) {
        this.consistency_level = other.consistency_level;
      }
    }
    public get_range_slices_args deepCopy() {
      return new get_range_slices_args(this);
    }
    @Override
    public void clear() {
      this.column_parent = null;
      this.predicate = null;
      this.range = null;
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public ColumnParent getColumn_parent() {
      return this.column_parent;
    }
    public get_range_slices_args setColumn_parent(ColumnParent column_parent) {
      this.column_parent = column_parent;
      return this;
    }
    public void unsetColumn_parent() {
      this.column_parent = null;
    }
    public boolean isSetColumn_parent() {
      return this.column_parent != null;
    }
    public void setColumn_parentIsSet(boolean value) {
      if (!value) {
        this.column_parent = null;
      }
    }
    public SlicePredicate getPredicate() {
      return this.predicate;
    }
    public get_range_slices_args setPredicate(SlicePredicate predicate) {
      this.predicate = predicate;
      return this;
    }
    public void unsetPredicate() {
      this.predicate = null;
    }
    public boolean isSetPredicate() {
      return this.predicate != null;
    }
    public void setPredicateIsSet(boolean value) {
      if (!value) {
        this.predicate = null;
      }
    }
    public KeyRange getRange() {
      return this.range;
    }
    public get_range_slices_args setRange(KeyRange range) {
      this.range = range;
      return this;
    }
    public void unsetRange() {
      this.range = null;
    }
    public boolean isSetRange() {
      return this.range != null;
    }
    public void setRangeIsSet(boolean value) {
      if (!value) {
        this.range = null;
      }
    }
    public ConsistencyLevel getConsistency_level() {
      return this.consistency_level;
    }
    public get_range_slices_args setConsistency_level(ConsistencyLevel consistency_level) {
      this.consistency_level = consistency_level;
      return this;
    }
    public void unsetConsistency_level() {
      this.consistency_level = null;
    }
    public boolean isSetConsistency_level() {
      return this.consistency_level != null;
    }
    public void setConsistency_levelIsSet(boolean value) {
      if (!value) {
        this.consistency_level = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case COLUMN_PARENT:
        if (value == null) {
          unsetColumn_parent();
        } else {
          setColumn_parent((ColumnParent)value);
        }
        break;
      case PREDICATE:
        if (value == null) {
          unsetPredicate();
        } else {
          setPredicate((SlicePredicate)value);
        }
        break;
      case RANGE:
        if (value == null) {
          unsetRange();
        } else {
          setRange((KeyRange)value);
        }
        break;
      case CONSISTENCY_LEVEL:
        if (value == null) {
          unsetConsistency_level();
        } else {
          setConsistency_level((ConsistencyLevel)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case COLUMN_PARENT:
        return getColumn_parent();
      case PREDICATE:
        return getPredicate();
      case RANGE:
        return getRange();
      case CONSISTENCY_LEVEL:
        return getConsistency_level();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case COLUMN_PARENT:
        return isSetColumn_parent();
      case PREDICATE:
        return isSetPredicate();
      case RANGE:
        return isSetRange();
      case CONSISTENCY_LEVEL:
        return isSetConsistency_level();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof get_range_slices_args)
        return this.equals((get_range_slices_args)that);
      return false;
    }
    public boolean equals(get_range_slices_args that) {
      if (that == null)
        return false;
      boolean this_present_column_parent = true && this.isSetColumn_parent();
      boolean that_present_column_parent = true && that.isSetColumn_parent();
      if (this_present_column_parent || that_present_column_parent) {
        if (!(this_present_column_parent && that_present_column_parent))
          return false;
        if (!this.column_parent.equals(that.column_parent))
          return false;
      }
      boolean this_present_predicate = true && this.isSetPredicate();
      boolean that_present_predicate = true && that.isSetPredicate();
      if (this_present_predicate || that_present_predicate) {
        if (!(this_present_predicate && that_present_predicate))
          return false;
        if (!this.predicate.equals(that.predicate))
          return false;
      }
      boolean this_present_range = true && this.isSetRange();
      boolean that_present_range = true && that.isSetRange();
      if (this_present_range || that_present_range) {
        if (!(this_present_range && that_present_range))
          return false;
        if (!this.range.equals(that.range))
          return false;
      }
      boolean this_present_consistency_level = true && this.isSetConsistency_level();
      boolean that_present_consistency_level = true && that.isSetConsistency_level();
      if (this_present_consistency_level || that_present_consistency_level) {
        if (!(this_present_consistency_level && that_present_consistency_level))
          return false;
        if (!this.consistency_level.equals(that.consistency_level))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_column_parent = true && (isSetColumn_parent());
      builder.append(present_column_parent);
      if (present_column_parent)
        builder.append(column_parent);
      boolean present_predicate = true && (isSetPredicate());
      builder.append(present_predicate);
      if (present_predicate)
        builder.append(predicate);
      boolean present_range = true && (isSetRange());
      builder.append(present_range);
      if (present_range)
        builder.append(range);
      boolean present_consistency_level = true && (isSetConsistency_level());
      builder.append(present_consistency_level);
      if (present_consistency_level)
        builder.append(consistency_level.getValue());
      return builder.toHashCode();
    }
    public int compareTo(get_range_slices_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      get_range_slices_args typedOther = (get_range_slices_args)other;
      lastComparison = Boolean.valueOf(isSetColumn_parent()).compareTo(typedOther.isSetColumn_parent());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetColumn_parent()) {
        lastComparison = TBaseHelper.compareTo(this.column_parent, typedOther.column_parent);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetPredicate()).compareTo(typedOther.isSetPredicate());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetPredicate()) {
        lastComparison = TBaseHelper.compareTo(this.predicate, typedOther.predicate);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetRange()).compareTo(typedOther.isSetRange());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetRange()) {
        lastComparison = TBaseHelper.compareTo(this.range, typedOther.range);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetConsistency_level()).compareTo(typedOther.isSetConsistency_level());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetConsistency_level()) {
        lastComparison = TBaseHelper.compareTo(this.consistency_level, typedOther.consistency_level);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRUCT) {
              this.column_parent = new ColumnParent();
              this.column_parent.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.predicate = new SlicePredicate();
              this.predicate.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.range = new KeyRange();
              this.range.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 4: 
            if (field.type == TType.I32) {
              this.consistency_level = ConsistencyLevel.findByValue(iprot.readI32());
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.column_parent != null) {
        oprot.writeFieldBegin(COLUMN_PARENT_FIELD_DESC);
        this.column_parent.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.predicate != null) {
        oprot.writeFieldBegin(PREDICATE_FIELD_DESC);
        this.predicate.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.range != null) {
        oprot.writeFieldBegin(RANGE_FIELD_DESC);
        this.range.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.consistency_level != null) {
        oprot.writeFieldBegin(CONSISTENCY_LEVEL_FIELD_DESC);
        oprot.writeI32(this.consistency_level.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("get_range_slices_args(");
      boolean first = true;
      sb.append("column_parent:");
      if (this.column_parent == null) {
        sb.append("null");
      } else {
        sb.append(this.column_parent);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("predicate:");
      if (this.predicate == null) {
        sb.append("null");
      } else {
        sb.append(this.predicate);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("range:");
      if (this.range == null) {
        sb.append("null");
      } else {
        sb.append(this.range);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("consistency_level:");
      if (this.consistency_level == null) {
        sb.append("null");
      } else {
        sb.append(this.consistency_level);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (column_parent == null) {
        throw new TProtocolException("Required field 'column_parent' was not present! Struct: " + toString());
      }
      if (predicate == null) {
        throw new TProtocolException("Required field 'predicate' was not present! Struct: " + toString());
      }
      if (range == null) {
        throw new TProtocolException("Required field 'range' was not present! Struct: " + toString());
      }
      if (consistency_level == null) {
        throw new TProtocolException("Required field 'consistency_level' was not present! Struct: " + toString());
      }
    }
  }
  public static class get_range_slices_result implements TBase<get_range_slices_result, get_range_slices_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("get_range_slices_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.LIST, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    private static final TField UE_FIELD_DESC = new TField("ue", TType.STRUCT, (short)2);
    private static final TField TE_FIELD_DESC = new TField("te", TType.STRUCT, (short)3);
    public List<KeySlice> success;
    public InvalidRequestException ire;
    public UnavailableException ue;
    public TimedOutException te;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire"),
      UE((short)2, "ue"),
      TE((short)3, "te");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          case 2: 
            return UE;
          case 3: 
            return TE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new ListMetaData(TType.LIST, 
              new StructMetaData(TType.STRUCT, KeySlice.class))));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.UE, new FieldMetaData("ue", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.TE, new FieldMetaData("te", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(get_range_slices_result.class, metaDataMap);
    }
    public get_range_slices_result() {
    }
    public get_range_slices_result(
      List<KeySlice> success,
      InvalidRequestException ire,
      UnavailableException ue,
      TimedOutException te)
    {
      this();
      this.success = success;
      this.ire = ire;
      this.ue = ue;
      this.te = te;
    }
    public get_range_slices_result(get_range_slices_result other) {
      if (other.isSetSuccess()) {
        List<KeySlice> __this__success = new ArrayList<KeySlice>();
        for (KeySlice other_element : other.success) {
          __this__success.add(new KeySlice(other_element));
        }
        this.success = __this__success;
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
      if (other.isSetUe()) {
        this.ue = new UnavailableException(other.ue);
      }
      if (other.isSetTe()) {
        this.te = new TimedOutException(other.te);
      }
    }
    public get_range_slices_result deepCopy() {
      return new get_range_slices_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.ire = null;
      this.ue = null;
      this.te = null;
    }
    public int getSuccessSize() {
      return (this.success == null) ? 0 : this.success.size();
    }
    public java.util.Iterator<KeySlice> getSuccessIterator() {
      return (this.success == null) ? null : this.success.iterator();
    }
    public void addToSuccess(KeySlice elem) {
      if (this.success == null) {
        this.success = new ArrayList<KeySlice>();
      }
      this.success.add(elem);
    }
    public List<KeySlice> getSuccess() {
      return this.success;
    }
    public get_range_slices_result setSuccess(List<KeySlice> success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public get_range_slices_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public UnavailableException getUe() {
      return this.ue;
    }
    public get_range_slices_result setUe(UnavailableException ue) {
      this.ue = ue;
      return this;
    }
    public void unsetUe() {
      this.ue = null;
    }
    public boolean isSetUe() {
      return this.ue != null;
    }
    public void setUeIsSet(boolean value) {
      if (!value) {
        this.ue = null;
      }
    }
    public TimedOutException getTe() {
      return this.te;
    }
    public get_range_slices_result setTe(TimedOutException te) {
      this.te = te;
      return this;
    }
    public void unsetTe() {
      this.te = null;
    }
    public boolean isSetTe() {
      return this.te != null;
    }
    public void setTeIsSet(boolean value) {
      if (!value) {
        this.te = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((List<KeySlice>)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      case UE:
        if (value == null) {
          unsetUe();
        } else {
          setUe((UnavailableException)value);
        }
        break;
      case TE:
        if (value == null) {
          unsetTe();
        } else {
          setTe((TimedOutException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case IRE:
        return getIre();
      case UE:
        return getUe();
      case TE:
        return getTe();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      case UE:
        return isSetUe();
      case TE:
        return isSetTe();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof get_range_slices_result)
        return this.equals((get_range_slices_result)that);
      return false;
    }
    public boolean equals(get_range_slices_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      boolean this_present_ue = true && this.isSetUe();
      boolean that_present_ue = true && that.isSetUe();
      if (this_present_ue || that_present_ue) {
        if (!(this_present_ue && that_present_ue))
          return false;
        if (!this.ue.equals(that.ue))
          return false;
      }
      boolean this_present_te = true && this.isSetTe();
      boolean that_present_te = true && that.isSetTe();
      if (this_present_te || that_present_te) {
        if (!(this_present_te && that_present_te))
          return false;
        if (!this.te.equals(that.te))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      boolean present_ue = true && (isSetUe());
      builder.append(present_ue);
      if (present_ue)
        builder.append(ue);
      boolean present_te = true && (isSetTe());
      builder.append(present_te);
      if (present_te)
        builder.append(te);
      return builder.toHashCode();
    }
    public int compareTo(get_range_slices_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      get_range_slices_result typedOther = (get_range_slices_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetUe()).compareTo(typedOther.isSetUe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetUe()) {
        lastComparison = TBaseHelper.compareTo(this.ue, typedOther.ue);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetTe()).compareTo(typedOther.isSetTe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetTe()) {
        lastComparison = TBaseHelper.compareTo(this.te, typedOther.te);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.LIST) {
              {
                TList _list76 = iprot.readListBegin();
                this.success = new ArrayList<KeySlice>(_list76.size);
                for (int _i77 = 0; _i77 < _list76.size; ++_i77)
                {
                  KeySlice _elem78;
                  _elem78 = new KeySlice();
                  _elem78.read(iprot);
                  this.success.add(_elem78);
                }
                iprot.readListEnd();
              }
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.ue = new UnavailableException();
              this.ue.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.te = new TimedOutException();
              this.te.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        {
          oprot.writeListBegin(new TList(TType.STRUCT, this.success.size()));
          for (KeySlice _iter79 : this.success)
          {
            _iter79.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetUe()) {
        oprot.writeFieldBegin(UE_FIELD_DESC);
        this.ue.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetTe()) {
        oprot.writeFieldBegin(TE_FIELD_DESC);
        this.te.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("get_range_slices_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ue:");
      if (this.ue == null) {
        sb.append("null");
      } else {
        sb.append(this.ue);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("te:");
      if (this.te == null) {
        sb.append("null");
      } else {
        sb.append(this.te);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class get_indexed_slices_args implements TBase<get_indexed_slices_args, get_indexed_slices_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("get_indexed_slices_args");
    private static final TField COLUMN_PARENT_FIELD_DESC = new TField("column_parent", TType.STRUCT, (short)1);
    private static final TField INDEX_CLAUSE_FIELD_DESC = new TField("index_clause", TType.STRUCT, (short)2);
    private static final TField COLUMN_PREDICATE_FIELD_DESC = new TField("column_predicate", TType.STRUCT, (short)3);
    private static final TField CONSISTENCY_LEVEL_FIELD_DESC = new TField("consistency_level", TType.I32, (short)4);
    public ColumnParent column_parent;
    public IndexClause index_clause;
    public SlicePredicate column_predicate;
    public ConsistencyLevel consistency_level;
    public enum _Fields implements TFieldIdEnum {
      COLUMN_PARENT((short)1, "column_parent"),
      INDEX_CLAUSE((short)2, "index_clause"),
      COLUMN_PREDICATE((short)3, "column_predicate"),
      CONSISTENCY_LEVEL((short)4, "consistency_level");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return COLUMN_PARENT;
          case 2: 
            return INDEX_CLAUSE;
          case 3: 
            return COLUMN_PREDICATE;
          case 4: 
            return CONSISTENCY_LEVEL;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.COLUMN_PARENT, new FieldMetaData("column_parent", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, ColumnParent.class)));
      tmpMap.put(_Fields.INDEX_CLAUSE, new FieldMetaData("index_clause", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, IndexClause.class)));
      tmpMap.put(_Fields.COLUMN_PREDICATE, new FieldMetaData("column_predicate", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, SlicePredicate.class)));
      tmpMap.put(_Fields.CONSISTENCY_LEVEL, new FieldMetaData("consistency_level", TFieldRequirementType.REQUIRED, 
          new EnumMetaData(TType.ENUM, ConsistencyLevel.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(get_indexed_slices_args.class, metaDataMap);
    }
    public get_indexed_slices_args() {
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public get_indexed_slices_args(
      ColumnParent column_parent,
      IndexClause index_clause,
      SlicePredicate column_predicate,
      ConsistencyLevel consistency_level)
    {
      this();
      this.column_parent = column_parent;
      this.index_clause = index_clause;
      this.column_predicate = column_predicate;
      this.consistency_level = consistency_level;
    }
    public get_indexed_slices_args(get_indexed_slices_args other) {
      if (other.isSetColumn_parent()) {
        this.column_parent = new ColumnParent(other.column_parent);
      }
      if (other.isSetIndex_clause()) {
        this.index_clause = new IndexClause(other.index_clause);
      }
      if (other.isSetColumn_predicate()) {
        this.column_predicate = new SlicePredicate(other.column_predicate);
      }
      if (other.isSetConsistency_level()) {
        this.consistency_level = other.consistency_level;
      }
    }
    public get_indexed_slices_args deepCopy() {
      return new get_indexed_slices_args(this);
    }
    @Override
    public void clear() {
      this.column_parent = null;
      this.index_clause = null;
      this.column_predicate = null;
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public ColumnParent getColumn_parent() {
      return this.column_parent;
    }
    public get_indexed_slices_args setColumn_parent(ColumnParent column_parent) {
      this.column_parent = column_parent;
      return this;
    }
    public void unsetColumn_parent() {
      this.column_parent = null;
    }
    public boolean isSetColumn_parent() {
      return this.column_parent != null;
    }
    public void setColumn_parentIsSet(boolean value) {
      if (!value) {
        this.column_parent = null;
      }
    }
    public IndexClause getIndex_clause() {
      return this.index_clause;
    }
    public get_indexed_slices_args setIndex_clause(IndexClause index_clause) {
      this.index_clause = index_clause;
      return this;
    }
    public void unsetIndex_clause() {
      this.index_clause = null;
    }
    public boolean isSetIndex_clause() {
      return this.index_clause != null;
    }
    public void setIndex_clauseIsSet(boolean value) {
      if (!value) {
        this.index_clause = null;
      }
    }
    public SlicePredicate getColumn_predicate() {
      return this.column_predicate;
    }
    public get_indexed_slices_args setColumn_predicate(SlicePredicate column_predicate) {
      this.column_predicate = column_predicate;
      return this;
    }
    public void unsetColumn_predicate() {
      this.column_predicate = null;
    }
    public boolean isSetColumn_predicate() {
      return this.column_predicate != null;
    }
    public void setColumn_predicateIsSet(boolean value) {
      if (!value) {
        this.column_predicate = null;
      }
    }
    public ConsistencyLevel getConsistency_level() {
      return this.consistency_level;
    }
    public get_indexed_slices_args setConsistency_level(ConsistencyLevel consistency_level) {
      this.consistency_level = consistency_level;
      return this;
    }
    public void unsetConsistency_level() {
      this.consistency_level = null;
    }
    public boolean isSetConsistency_level() {
      return this.consistency_level != null;
    }
    public void setConsistency_levelIsSet(boolean value) {
      if (!value) {
        this.consistency_level = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case COLUMN_PARENT:
        if (value == null) {
          unsetColumn_parent();
        } else {
          setColumn_parent((ColumnParent)value);
        }
        break;
      case INDEX_CLAUSE:
        if (value == null) {
          unsetIndex_clause();
        } else {
          setIndex_clause((IndexClause)value);
        }
        break;
      case COLUMN_PREDICATE:
        if (value == null) {
          unsetColumn_predicate();
        } else {
          setColumn_predicate((SlicePredicate)value);
        }
        break;
      case CONSISTENCY_LEVEL:
        if (value == null) {
          unsetConsistency_level();
        } else {
          setConsistency_level((ConsistencyLevel)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case COLUMN_PARENT:
        return getColumn_parent();
      case INDEX_CLAUSE:
        return getIndex_clause();
      case COLUMN_PREDICATE:
        return getColumn_predicate();
      case CONSISTENCY_LEVEL:
        return getConsistency_level();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case COLUMN_PARENT:
        return isSetColumn_parent();
      case INDEX_CLAUSE:
        return isSetIndex_clause();
      case COLUMN_PREDICATE:
        return isSetColumn_predicate();
      case CONSISTENCY_LEVEL:
        return isSetConsistency_level();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof get_indexed_slices_args)
        return this.equals((get_indexed_slices_args)that);
      return false;
    }
    public boolean equals(get_indexed_slices_args that) {
      if (that == null)
        return false;
      boolean this_present_column_parent = true && this.isSetColumn_parent();
      boolean that_present_column_parent = true && that.isSetColumn_parent();
      if (this_present_column_parent || that_present_column_parent) {
        if (!(this_present_column_parent && that_present_column_parent))
          return false;
        if (!this.column_parent.equals(that.column_parent))
          return false;
      }
      boolean this_present_index_clause = true && this.isSetIndex_clause();
      boolean that_present_index_clause = true && that.isSetIndex_clause();
      if (this_present_index_clause || that_present_index_clause) {
        if (!(this_present_index_clause && that_present_index_clause))
          return false;
        if (!this.index_clause.equals(that.index_clause))
          return false;
      }
      boolean this_present_column_predicate = true && this.isSetColumn_predicate();
      boolean that_present_column_predicate = true && that.isSetColumn_predicate();
      if (this_present_column_predicate || that_present_column_predicate) {
        if (!(this_present_column_predicate && that_present_column_predicate))
          return false;
        if (!this.column_predicate.equals(that.column_predicate))
          return false;
      }
      boolean this_present_consistency_level = true && this.isSetConsistency_level();
      boolean that_present_consistency_level = true && that.isSetConsistency_level();
      if (this_present_consistency_level || that_present_consistency_level) {
        if (!(this_present_consistency_level && that_present_consistency_level))
          return false;
        if (!this.consistency_level.equals(that.consistency_level))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_column_parent = true && (isSetColumn_parent());
      builder.append(present_column_parent);
      if (present_column_parent)
        builder.append(column_parent);
      boolean present_index_clause = true && (isSetIndex_clause());
      builder.append(present_index_clause);
      if (present_index_clause)
        builder.append(index_clause);
      boolean present_column_predicate = true && (isSetColumn_predicate());
      builder.append(present_column_predicate);
      if (present_column_predicate)
        builder.append(column_predicate);
      boolean present_consistency_level = true && (isSetConsistency_level());
      builder.append(present_consistency_level);
      if (present_consistency_level)
        builder.append(consistency_level.getValue());
      return builder.toHashCode();
    }
    public int compareTo(get_indexed_slices_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      get_indexed_slices_args typedOther = (get_indexed_slices_args)other;
      lastComparison = Boolean.valueOf(isSetColumn_parent()).compareTo(typedOther.isSetColumn_parent());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetColumn_parent()) {
        lastComparison = TBaseHelper.compareTo(this.column_parent, typedOther.column_parent);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIndex_clause()).compareTo(typedOther.isSetIndex_clause());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIndex_clause()) {
        lastComparison = TBaseHelper.compareTo(this.index_clause, typedOther.index_clause);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetColumn_predicate()).compareTo(typedOther.isSetColumn_predicate());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetColumn_predicate()) {
        lastComparison = TBaseHelper.compareTo(this.column_predicate, typedOther.column_predicate);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetConsistency_level()).compareTo(typedOther.isSetConsistency_level());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetConsistency_level()) {
        lastComparison = TBaseHelper.compareTo(this.consistency_level, typedOther.consistency_level);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRUCT) {
              this.column_parent = new ColumnParent();
              this.column_parent.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.index_clause = new IndexClause();
              this.index_clause.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.column_predicate = new SlicePredicate();
              this.column_predicate.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 4: 
            if (field.type == TType.I32) {
              this.consistency_level = ConsistencyLevel.findByValue(iprot.readI32());
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.column_parent != null) {
        oprot.writeFieldBegin(COLUMN_PARENT_FIELD_DESC);
        this.column_parent.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.index_clause != null) {
        oprot.writeFieldBegin(INDEX_CLAUSE_FIELD_DESC);
        this.index_clause.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.column_predicate != null) {
        oprot.writeFieldBegin(COLUMN_PREDICATE_FIELD_DESC);
        this.column_predicate.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.consistency_level != null) {
        oprot.writeFieldBegin(CONSISTENCY_LEVEL_FIELD_DESC);
        oprot.writeI32(this.consistency_level.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("get_indexed_slices_args(");
      boolean first = true;
      sb.append("column_parent:");
      if (this.column_parent == null) {
        sb.append("null");
      } else {
        sb.append(this.column_parent);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("index_clause:");
      if (this.index_clause == null) {
        sb.append("null");
      } else {
        sb.append(this.index_clause);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("column_predicate:");
      if (this.column_predicate == null) {
        sb.append("null");
      } else {
        sb.append(this.column_predicate);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("consistency_level:");
      if (this.consistency_level == null) {
        sb.append("null");
      } else {
        sb.append(this.consistency_level);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (column_parent == null) {
        throw new TProtocolException("Required field 'column_parent' was not present! Struct: " + toString());
      }
      if (index_clause == null) {
        throw new TProtocolException("Required field 'index_clause' was not present! Struct: " + toString());
      }
      if (column_predicate == null) {
        throw new TProtocolException("Required field 'column_predicate' was not present! Struct: " + toString());
      }
      if (consistency_level == null) {
        throw new TProtocolException("Required field 'consistency_level' was not present! Struct: " + toString());
      }
    }
  }
  public static class get_indexed_slices_result implements TBase<get_indexed_slices_result, get_indexed_slices_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("get_indexed_slices_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.LIST, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    private static final TField UE_FIELD_DESC = new TField("ue", TType.STRUCT, (short)2);
    private static final TField TE_FIELD_DESC = new TField("te", TType.STRUCT, (short)3);
    public List<KeySlice> success;
    public InvalidRequestException ire;
    public UnavailableException ue;
    public TimedOutException te;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire"),
      UE((short)2, "ue"),
      TE((short)3, "te");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          case 2: 
            return UE;
          case 3: 
            return TE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new ListMetaData(TType.LIST, 
              new StructMetaData(TType.STRUCT, KeySlice.class))));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.UE, new FieldMetaData("ue", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.TE, new FieldMetaData("te", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(get_indexed_slices_result.class, metaDataMap);
    }
    public get_indexed_slices_result() {
    }
    public get_indexed_slices_result(
      List<KeySlice> success,
      InvalidRequestException ire,
      UnavailableException ue,
      TimedOutException te)
    {
      this();
      this.success = success;
      this.ire = ire;
      this.ue = ue;
      this.te = te;
    }
    public get_indexed_slices_result(get_indexed_slices_result other) {
      if (other.isSetSuccess()) {
        List<KeySlice> __this__success = new ArrayList<KeySlice>();
        for (KeySlice other_element : other.success) {
          __this__success.add(new KeySlice(other_element));
        }
        this.success = __this__success;
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
      if (other.isSetUe()) {
        this.ue = new UnavailableException(other.ue);
      }
      if (other.isSetTe()) {
        this.te = new TimedOutException(other.te);
      }
    }
    public get_indexed_slices_result deepCopy() {
      return new get_indexed_slices_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.ire = null;
      this.ue = null;
      this.te = null;
    }
    public int getSuccessSize() {
      return (this.success == null) ? 0 : this.success.size();
    }
    public java.util.Iterator<KeySlice> getSuccessIterator() {
      return (this.success == null) ? null : this.success.iterator();
    }
    public void addToSuccess(KeySlice elem) {
      if (this.success == null) {
        this.success = new ArrayList<KeySlice>();
      }
      this.success.add(elem);
    }
    public List<KeySlice> getSuccess() {
      return this.success;
    }
    public get_indexed_slices_result setSuccess(List<KeySlice> success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public get_indexed_slices_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public UnavailableException getUe() {
      return this.ue;
    }
    public get_indexed_slices_result setUe(UnavailableException ue) {
      this.ue = ue;
      return this;
    }
    public void unsetUe() {
      this.ue = null;
    }
    public boolean isSetUe() {
      return this.ue != null;
    }
    public void setUeIsSet(boolean value) {
      if (!value) {
        this.ue = null;
      }
    }
    public TimedOutException getTe() {
      return this.te;
    }
    public get_indexed_slices_result setTe(TimedOutException te) {
      this.te = te;
      return this;
    }
    public void unsetTe() {
      this.te = null;
    }
    public boolean isSetTe() {
      return this.te != null;
    }
    public void setTeIsSet(boolean value) {
      if (!value) {
        this.te = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((List<KeySlice>)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      case UE:
        if (value == null) {
          unsetUe();
        } else {
          setUe((UnavailableException)value);
        }
        break;
      case TE:
        if (value == null) {
          unsetTe();
        } else {
          setTe((TimedOutException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case IRE:
        return getIre();
      case UE:
        return getUe();
      case TE:
        return getTe();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      case UE:
        return isSetUe();
      case TE:
        return isSetTe();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof get_indexed_slices_result)
        return this.equals((get_indexed_slices_result)that);
      return false;
    }
    public boolean equals(get_indexed_slices_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      boolean this_present_ue = true && this.isSetUe();
      boolean that_present_ue = true && that.isSetUe();
      if (this_present_ue || that_present_ue) {
        if (!(this_present_ue && that_present_ue))
          return false;
        if (!this.ue.equals(that.ue))
          return false;
      }
      boolean this_present_te = true && this.isSetTe();
      boolean that_present_te = true && that.isSetTe();
      if (this_present_te || that_present_te) {
        if (!(this_present_te && that_present_te))
          return false;
        if (!this.te.equals(that.te))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      boolean present_ue = true && (isSetUe());
      builder.append(present_ue);
      if (present_ue)
        builder.append(ue);
      boolean present_te = true && (isSetTe());
      builder.append(present_te);
      if (present_te)
        builder.append(te);
      return builder.toHashCode();
    }
    public int compareTo(get_indexed_slices_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      get_indexed_slices_result typedOther = (get_indexed_slices_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetUe()).compareTo(typedOther.isSetUe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetUe()) {
        lastComparison = TBaseHelper.compareTo(this.ue, typedOther.ue);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetTe()).compareTo(typedOther.isSetTe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetTe()) {
        lastComparison = TBaseHelper.compareTo(this.te, typedOther.te);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.LIST) {
              {
                TList _list80 = iprot.readListBegin();
                this.success = new ArrayList<KeySlice>(_list80.size);
                for (int _i81 = 0; _i81 < _list80.size; ++_i81)
                {
                  KeySlice _elem82;
                  _elem82 = new KeySlice();
                  _elem82.read(iprot);
                  this.success.add(_elem82);
                }
                iprot.readListEnd();
              }
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.ue = new UnavailableException();
              this.ue.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.te = new TimedOutException();
              this.te.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        {
          oprot.writeListBegin(new TList(TType.STRUCT, this.success.size()));
          for (KeySlice _iter83 : this.success)
          {
            _iter83.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetUe()) {
        oprot.writeFieldBegin(UE_FIELD_DESC);
        this.ue.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetTe()) {
        oprot.writeFieldBegin(TE_FIELD_DESC);
        this.te.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("get_indexed_slices_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ue:");
      if (this.ue == null) {
        sb.append("null");
      } else {
        sb.append(this.ue);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("te:");
      if (this.te == null) {
        sb.append("null");
      } else {
        sb.append(this.te);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class insert_args implements TBase<insert_args, insert_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("insert_args");
    private static final TField KEY_FIELD_DESC = new TField("key", TType.STRING, (short)1);
    private static final TField COLUMN_PARENT_FIELD_DESC = new TField("column_parent", TType.STRUCT, (short)2);
    private static final TField COLUMN_FIELD_DESC = new TField("column", TType.STRUCT, (short)3);
    private static final TField CONSISTENCY_LEVEL_FIELD_DESC = new TField("consistency_level", TType.I32, (short)4);
    public ByteBuffer key;
    public ColumnParent column_parent;
    public Column column;
    public ConsistencyLevel consistency_level;
    public enum _Fields implements TFieldIdEnum {
      KEY((short)1, "key"),
      COLUMN_PARENT((short)2, "column_parent"),
      COLUMN((short)3, "column"),
      CONSISTENCY_LEVEL((short)4, "consistency_level");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return KEY;
          case 2: 
            return COLUMN_PARENT;
          case 3: 
            return COLUMN;
          case 4: 
            return CONSISTENCY_LEVEL;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.KEY, new FieldMetaData("key", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.STRING)));
      tmpMap.put(_Fields.COLUMN_PARENT, new FieldMetaData("column_parent", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, ColumnParent.class)));
      tmpMap.put(_Fields.COLUMN, new FieldMetaData("column", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, Column.class)));
      tmpMap.put(_Fields.CONSISTENCY_LEVEL, new FieldMetaData("consistency_level", TFieldRequirementType.REQUIRED, 
          new EnumMetaData(TType.ENUM, ConsistencyLevel.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(insert_args.class, metaDataMap);
    }
    public insert_args() {
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public insert_args(
      ByteBuffer key,
      ColumnParent column_parent,
      Column column,
      ConsistencyLevel consistency_level)
    {
      this();
      this.key = key;
      this.column_parent = column_parent;
      this.column = column;
      this.consistency_level = consistency_level;
    }
    public insert_args(insert_args other) {
      if (other.isSetKey()) {
        this.key = TBaseHelper.copyBinary(other.key);
;
      }
      if (other.isSetColumn_parent()) {
        this.column_parent = new ColumnParent(other.column_parent);
      }
      if (other.isSetColumn()) {
        this.column = new Column(other.column);
      }
      if (other.isSetConsistency_level()) {
        this.consistency_level = other.consistency_level;
      }
    }
    public insert_args deepCopy() {
      return new insert_args(this);
    }
    @Override
    public void clear() {
      this.key = null;
      this.column_parent = null;
      this.column = null;
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public byte[] getKey() {
      setKey(TBaseHelper.rightSize(key));
      return key.array();
    }
    public ByteBuffer BufferForKey() {
      return key;
    }
    public insert_args setKey(byte[] key) {
      setKey(ByteBuffer.wrap(key));
      return this;
    }
    public insert_args setKey(ByteBuffer key) {
      this.key = key;
      return this;
    }
    public void unsetKey() {
      this.key = null;
    }
    public boolean isSetKey() {
      return this.key != null;
    }
    public void setKeyIsSet(boolean value) {
      if (!value) {
        this.key = null;
      }
    }
    public ColumnParent getColumn_parent() {
      return this.column_parent;
    }
    public insert_args setColumn_parent(ColumnParent column_parent) {
      this.column_parent = column_parent;
      return this;
    }
    public void unsetColumn_parent() {
      this.column_parent = null;
    }
    public boolean isSetColumn_parent() {
      return this.column_parent != null;
    }
    public void setColumn_parentIsSet(boolean value) {
      if (!value) {
        this.column_parent = null;
      }
    }
    public Column getColumn() {
      return this.column;
    }
    public insert_args setColumn(Column column) {
      this.column = column;
      return this;
    }
    public void unsetColumn() {
      this.column = null;
    }
    public boolean isSetColumn() {
      return this.column != null;
    }
    public void setColumnIsSet(boolean value) {
      if (!value) {
        this.column = null;
      }
    }
    public ConsistencyLevel getConsistency_level() {
      return this.consistency_level;
    }
    public insert_args setConsistency_level(ConsistencyLevel consistency_level) {
      this.consistency_level = consistency_level;
      return this;
    }
    public void unsetConsistency_level() {
      this.consistency_level = null;
    }
    public boolean isSetConsistency_level() {
      return this.consistency_level != null;
    }
    public void setConsistency_levelIsSet(boolean value) {
      if (!value) {
        this.consistency_level = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case KEY:
        if (value == null) {
          unsetKey();
        } else {
          setKey((ByteBuffer)value);
        }
        break;
      case COLUMN_PARENT:
        if (value == null) {
          unsetColumn_parent();
        } else {
          setColumn_parent((ColumnParent)value);
        }
        break;
      case COLUMN:
        if (value == null) {
          unsetColumn();
        } else {
          setColumn((Column)value);
        }
        break;
      case CONSISTENCY_LEVEL:
        if (value == null) {
          unsetConsistency_level();
        } else {
          setConsistency_level((ConsistencyLevel)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case KEY:
        return getKey();
      case COLUMN_PARENT:
        return getColumn_parent();
      case COLUMN:
        return getColumn();
      case CONSISTENCY_LEVEL:
        return getConsistency_level();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case KEY:
        return isSetKey();
      case COLUMN_PARENT:
        return isSetColumn_parent();
      case COLUMN:
        return isSetColumn();
      case CONSISTENCY_LEVEL:
        return isSetConsistency_level();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof insert_args)
        return this.equals((insert_args)that);
      return false;
    }
    public boolean equals(insert_args that) {
      if (that == null)
        return false;
      boolean this_present_key = true && this.isSetKey();
      boolean that_present_key = true && that.isSetKey();
      if (this_present_key || that_present_key) {
        if (!(this_present_key && that_present_key))
          return false;
        if (!this.key.equals(that.key))
          return false;
      }
      boolean this_present_column_parent = true && this.isSetColumn_parent();
      boolean that_present_column_parent = true && that.isSetColumn_parent();
      if (this_present_column_parent || that_present_column_parent) {
        if (!(this_present_column_parent && that_present_column_parent))
          return false;
        if (!this.column_parent.equals(that.column_parent))
          return false;
      }
      boolean this_present_column = true && this.isSetColumn();
      boolean that_present_column = true && that.isSetColumn();
      if (this_present_column || that_present_column) {
        if (!(this_present_column && that_present_column))
          return false;
        if (!this.column.equals(that.column))
          return false;
      }
      boolean this_present_consistency_level = true && this.isSetConsistency_level();
      boolean that_present_consistency_level = true && that.isSetConsistency_level();
      if (this_present_consistency_level || that_present_consistency_level) {
        if (!(this_present_consistency_level && that_present_consistency_level))
          return false;
        if (!this.consistency_level.equals(that.consistency_level))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_key = true && (isSetKey());
      builder.append(present_key);
      if (present_key)
        builder.append(key);
      boolean present_column_parent = true && (isSetColumn_parent());
      builder.append(present_column_parent);
      if (present_column_parent)
        builder.append(column_parent);
      boolean present_column = true && (isSetColumn());
      builder.append(present_column);
      if (present_column)
        builder.append(column);
      boolean present_consistency_level = true && (isSetConsistency_level());
      builder.append(present_consistency_level);
      if (present_consistency_level)
        builder.append(consistency_level.getValue());
      return builder.toHashCode();
    }
    public int compareTo(insert_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      insert_args typedOther = (insert_args)other;
      lastComparison = Boolean.valueOf(isSetKey()).compareTo(typedOther.isSetKey());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetKey()) {
        lastComparison = TBaseHelper.compareTo(this.key, typedOther.key);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetColumn_parent()).compareTo(typedOther.isSetColumn_parent());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetColumn_parent()) {
        lastComparison = TBaseHelper.compareTo(this.column_parent, typedOther.column_parent);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetColumn()).compareTo(typedOther.isSetColumn());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetColumn()) {
        lastComparison = TBaseHelper.compareTo(this.column, typedOther.column);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetConsistency_level()).compareTo(typedOther.isSetConsistency_level());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetConsistency_level()) {
        lastComparison = TBaseHelper.compareTo(this.consistency_level, typedOther.consistency_level);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRING) {
              this.key = iprot.readBinary();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.column_parent = new ColumnParent();
              this.column_parent.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.column = new Column();
              this.column.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 4: 
            if (field.type == TType.I32) {
              this.consistency_level = ConsistencyLevel.findByValue(iprot.readI32());
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.key != null) {
        oprot.writeFieldBegin(KEY_FIELD_DESC);
        oprot.writeBinary(this.key);
        oprot.writeFieldEnd();
      }
      if (this.column_parent != null) {
        oprot.writeFieldBegin(COLUMN_PARENT_FIELD_DESC);
        this.column_parent.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.column != null) {
        oprot.writeFieldBegin(COLUMN_FIELD_DESC);
        this.column.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.consistency_level != null) {
        oprot.writeFieldBegin(CONSISTENCY_LEVEL_FIELD_DESC);
        oprot.writeI32(this.consistency_level.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("insert_args(");
      boolean first = true;
      sb.append("key:");
      if (this.key == null) {
        sb.append("null");
      } else {
        TBaseHelper.toString(this.key, sb);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("column_parent:");
      if (this.column_parent == null) {
        sb.append("null");
      } else {
        sb.append(this.column_parent);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("column:");
      if (this.column == null) {
        sb.append("null");
      } else {
        sb.append(this.column);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("consistency_level:");
      if (this.consistency_level == null) {
        sb.append("null");
      } else {
        sb.append(this.consistency_level);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (key == null) {
        throw new TProtocolException("Required field 'key' was not present! Struct: " + toString());
      }
      if (column_parent == null) {
        throw new TProtocolException("Required field 'column_parent' was not present! Struct: " + toString());
      }
      if (column == null) {
        throw new TProtocolException("Required field 'column' was not present! Struct: " + toString());
      }
      if (consistency_level == null) {
        throw new TProtocolException("Required field 'consistency_level' was not present! Struct: " + toString());
      }
    }
  }
  public static class insert_result implements TBase<insert_result, insert_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("insert_result");
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    private static final TField UE_FIELD_DESC = new TField("ue", TType.STRUCT, (short)2);
    private static final TField TE_FIELD_DESC = new TField("te", TType.STRUCT, (short)3);
    public InvalidRequestException ire;
    public UnavailableException ue;
    public TimedOutException te;
    public enum _Fields implements TFieldIdEnum {
      IRE((short)1, "ire"),
      UE((short)2, "ue"),
      TE((short)3, "te");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return IRE;
          case 2: 
            return UE;
          case 3: 
            return TE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.UE, new FieldMetaData("ue", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.TE, new FieldMetaData("te", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(insert_result.class, metaDataMap);
    }
    public insert_result() {
    }
    public insert_result(
      InvalidRequestException ire,
      UnavailableException ue,
      TimedOutException te)
    {
      this();
      this.ire = ire;
      this.ue = ue;
      this.te = te;
    }
    public insert_result(insert_result other) {
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
      if (other.isSetUe()) {
        this.ue = new UnavailableException(other.ue);
      }
      if (other.isSetTe()) {
        this.te = new TimedOutException(other.te);
      }
    }
    public insert_result deepCopy() {
      return new insert_result(this);
    }
    @Override
    public void clear() {
      this.ire = null;
      this.ue = null;
      this.te = null;
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public insert_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public UnavailableException getUe() {
      return this.ue;
    }
    public insert_result setUe(UnavailableException ue) {
      this.ue = ue;
      return this;
    }
    public void unsetUe() {
      this.ue = null;
    }
    public boolean isSetUe() {
      return this.ue != null;
    }
    public void setUeIsSet(boolean value) {
      if (!value) {
        this.ue = null;
      }
    }
    public TimedOutException getTe() {
      return this.te;
    }
    public insert_result setTe(TimedOutException te) {
      this.te = te;
      return this;
    }
    public void unsetTe() {
      this.te = null;
    }
    public boolean isSetTe() {
      return this.te != null;
    }
    public void setTeIsSet(boolean value) {
      if (!value) {
        this.te = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      case UE:
        if (value == null) {
          unsetUe();
        } else {
          setUe((UnavailableException)value);
        }
        break;
      case TE:
        if (value == null) {
          unsetTe();
        } else {
          setTe((TimedOutException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case IRE:
        return getIre();
      case UE:
        return getUe();
      case TE:
        return getTe();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case IRE:
        return isSetIre();
      case UE:
        return isSetUe();
      case TE:
        return isSetTe();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof insert_result)
        return this.equals((insert_result)that);
      return false;
    }
    public boolean equals(insert_result that) {
      if (that == null)
        return false;
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      boolean this_present_ue = true && this.isSetUe();
      boolean that_present_ue = true && that.isSetUe();
      if (this_present_ue || that_present_ue) {
        if (!(this_present_ue && that_present_ue))
          return false;
        if (!this.ue.equals(that.ue))
          return false;
      }
      boolean this_present_te = true && this.isSetTe();
      boolean that_present_te = true && that.isSetTe();
      if (this_present_te || that_present_te) {
        if (!(this_present_te && that_present_te))
          return false;
        if (!this.te.equals(that.te))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      boolean present_ue = true && (isSetUe());
      builder.append(present_ue);
      if (present_ue)
        builder.append(ue);
      boolean present_te = true && (isSetTe());
      builder.append(present_te);
      if (present_te)
        builder.append(te);
      return builder.toHashCode();
    }
    public int compareTo(insert_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      insert_result typedOther = (insert_result)other;
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetUe()).compareTo(typedOther.isSetUe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetUe()) {
        lastComparison = TBaseHelper.compareTo(this.ue, typedOther.ue);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetTe()).compareTo(typedOther.isSetTe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetTe()) {
        lastComparison = TBaseHelper.compareTo(this.te, typedOther.te);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.ue = new UnavailableException();
              this.ue.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.te = new TimedOutException();
              this.te.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetUe()) {
        oprot.writeFieldBegin(UE_FIELD_DESC);
        this.ue.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetTe()) {
        oprot.writeFieldBegin(TE_FIELD_DESC);
        this.te.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("insert_result(");
      boolean first = true;
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ue:");
      if (this.ue == null) {
        sb.append("null");
      } else {
        sb.append(this.ue);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("te:");
      if (this.te == null) {
        sb.append("null");
      } else {
        sb.append(this.te);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class remove_args implements TBase<remove_args, remove_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("remove_args");
    private static final TField KEY_FIELD_DESC = new TField("key", TType.STRING, (short)1);
    private static final TField COLUMN_PATH_FIELD_DESC = new TField("column_path", TType.STRUCT, (short)2);
    private static final TField TIMESTAMP_FIELD_DESC = new TField("timestamp", TType.I64, (short)3);
    private static final TField CONSISTENCY_LEVEL_FIELD_DESC = new TField("consistency_level", TType.I32, (short)4);
    public ByteBuffer key;
    public ColumnPath column_path;
    public long timestamp;
    public ConsistencyLevel consistency_level;
    public enum _Fields implements TFieldIdEnum {
      KEY((short)1, "key"),
      COLUMN_PATH((short)2, "column_path"),
      TIMESTAMP((short)3, "timestamp"),
      CONSISTENCY_LEVEL((short)4, "consistency_level");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return KEY;
          case 2: 
            return COLUMN_PATH;
          case 3: 
            return TIMESTAMP;
          case 4: 
            return CONSISTENCY_LEVEL;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    private static final int __TIMESTAMP_ISSET_ID = 0;
    private BitSet __isset_bit_vector = new BitSet(1);
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.KEY, new FieldMetaData("key", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.STRING)));
      tmpMap.put(_Fields.COLUMN_PATH, new FieldMetaData("column_path", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, ColumnPath.class)));
      tmpMap.put(_Fields.TIMESTAMP, new FieldMetaData("timestamp", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.I64)));
      tmpMap.put(_Fields.CONSISTENCY_LEVEL, new FieldMetaData("consistency_level", TFieldRequirementType.DEFAULT, 
          new EnumMetaData(TType.ENUM, ConsistencyLevel.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(remove_args.class, metaDataMap);
    }
    public remove_args() {
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public remove_args(
      ByteBuffer key,
      ColumnPath column_path,
      long timestamp,
      ConsistencyLevel consistency_level)
    {
      this();
      this.key = key;
      this.column_path = column_path;
      this.timestamp = timestamp;
      setTimestampIsSet(true);
      this.consistency_level = consistency_level;
    }
    public remove_args(remove_args other) {
      __isset_bit_vector.clear();
      __isset_bit_vector.or(other.__isset_bit_vector);
      if (other.isSetKey()) {
        this.key = TBaseHelper.copyBinary(other.key);
;
      }
      if (other.isSetColumn_path()) {
        this.column_path = new ColumnPath(other.column_path);
      }
      this.timestamp = other.timestamp;
      if (other.isSetConsistency_level()) {
        this.consistency_level = other.consistency_level;
      }
    }
    public remove_args deepCopy() {
      return new remove_args(this);
    }
    @Override
    public void clear() {
      this.key = null;
      this.column_path = null;
      setTimestampIsSet(false);
      this.timestamp = 0;
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public byte[] getKey() {
      setKey(TBaseHelper.rightSize(key));
      return key.array();
    }
    public ByteBuffer BufferForKey() {
      return key;
    }
    public remove_args setKey(byte[] key) {
      setKey(ByteBuffer.wrap(key));
      return this;
    }
    public remove_args setKey(ByteBuffer key) {
      this.key = key;
      return this;
    }
    public void unsetKey() {
      this.key = null;
    }
    public boolean isSetKey() {
      return this.key != null;
    }
    public void setKeyIsSet(boolean value) {
      if (!value) {
        this.key = null;
      }
    }
    public ColumnPath getColumn_path() {
      return this.column_path;
    }
    public remove_args setColumn_path(ColumnPath column_path) {
      this.column_path = column_path;
      return this;
    }
    public void unsetColumn_path() {
      this.column_path = null;
    }
    public boolean isSetColumn_path() {
      return this.column_path != null;
    }
    public void setColumn_pathIsSet(boolean value) {
      if (!value) {
        this.column_path = null;
      }
    }
    public long getTimestamp() {
      return this.timestamp;
    }
    public remove_args setTimestamp(long timestamp) {
      this.timestamp = timestamp;
      setTimestampIsSet(true);
      return this;
    }
    public void unsetTimestamp() {
      __isset_bit_vector.clear(__TIMESTAMP_ISSET_ID);
    }
    public boolean isSetTimestamp() {
      return __isset_bit_vector.get(__TIMESTAMP_ISSET_ID);
    }
    public void setTimestampIsSet(boolean value) {
      __isset_bit_vector.set(__TIMESTAMP_ISSET_ID, value);
    }
    public ConsistencyLevel getConsistency_level() {
      return this.consistency_level;
    }
    public remove_args setConsistency_level(ConsistencyLevel consistency_level) {
      this.consistency_level = consistency_level;
      return this;
    }
    public void unsetConsistency_level() {
      this.consistency_level = null;
    }
    public boolean isSetConsistency_level() {
      return this.consistency_level != null;
    }
    public void setConsistency_levelIsSet(boolean value) {
      if (!value) {
        this.consistency_level = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case KEY:
        if (value == null) {
          unsetKey();
        } else {
          setKey((ByteBuffer)value);
        }
        break;
      case COLUMN_PATH:
        if (value == null) {
          unsetColumn_path();
        } else {
          setColumn_path((ColumnPath)value);
        }
        break;
      case TIMESTAMP:
        if (value == null) {
          unsetTimestamp();
        } else {
          setTimestamp((Long)value);
        }
        break;
      case CONSISTENCY_LEVEL:
        if (value == null) {
          unsetConsistency_level();
        } else {
          setConsistency_level((ConsistencyLevel)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case KEY:
        return getKey();
      case COLUMN_PATH:
        return getColumn_path();
      case TIMESTAMP:
        return new Long(getTimestamp());
      case CONSISTENCY_LEVEL:
        return getConsistency_level();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case KEY:
        return isSetKey();
      case COLUMN_PATH:
        return isSetColumn_path();
      case TIMESTAMP:
        return isSetTimestamp();
      case CONSISTENCY_LEVEL:
        return isSetConsistency_level();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof remove_args)
        return this.equals((remove_args)that);
      return false;
    }
    public boolean equals(remove_args that) {
      if (that == null)
        return false;
      boolean this_present_key = true && this.isSetKey();
      boolean that_present_key = true && that.isSetKey();
      if (this_present_key || that_present_key) {
        if (!(this_present_key && that_present_key))
          return false;
        if (!this.key.equals(that.key))
          return false;
      }
      boolean this_present_column_path = true && this.isSetColumn_path();
      boolean that_present_column_path = true && that.isSetColumn_path();
      if (this_present_column_path || that_present_column_path) {
        if (!(this_present_column_path && that_present_column_path))
          return false;
        if (!this.column_path.equals(that.column_path))
          return false;
      }
      boolean this_present_timestamp = true;
      boolean that_present_timestamp = true;
      if (this_present_timestamp || that_present_timestamp) {
        if (!(this_present_timestamp && that_present_timestamp))
          return false;
        if (this.timestamp != that.timestamp)
          return false;
      }
      boolean this_present_consistency_level = true && this.isSetConsistency_level();
      boolean that_present_consistency_level = true && that.isSetConsistency_level();
      if (this_present_consistency_level || that_present_consistency_level) {
        if (!(this_present_consistency_level && that_present_consistency_level))
          return false;
        if (!this.consistency_level.equals(that.consistency_level))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_key = true && (isSetKey());
      builder.append(present_key);
      if (present_key)
        builder.append(key);
      boolean present_column_path = true && (isSetColumn_path());
      builder.append(present_column_path);
      if (present_column_path)
        builder.append(column_path);
      boolean present_timestamp = true;
      builder.append(present_timestamp);
      if (present_timestamp)
        builder.append(timestamp);
      boolean present_consistency_level = true && (isSetConsistency_level());
      builder.append(present_consistency_level);
      if (present_consistency_level)
        builder.append(consistency_level.getValue());
      return builder.toHashCode();
    }
    public int compareTo(remove_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      remove_args typedOther = (remove_args)other;
      lastComparison = Boolean.valueOf(isSetKey()).compareTo(typedOther.isSetKey());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetKey()) {
        lastComparison = TBaseHelper.compareTo(this.key, typedOther.key);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetColumn_path()).compareTo(typedOther.isSetColumn_path());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetColumn_path()) {
        lastComparison = TBaseHelper.compareTo(this.column_path, typedOther.column_path);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetTimestamp()).compareTo(typedOther.isSetTimestamp());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetTimestamp()) {
        lastComparison = TBaseHelper.compareTo(this.timestamp, typedOther.timestamp);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetConsistency_level()).compareTo(typedOther.isSetConsistency_level());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetConsistency_level()) {
        lastComparison = TBaseHelper.compareTo(this.consistency_level, typedOther.consistency_level);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRING) {
              this.key = iprot.readBinary();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.column_path = new ColumnPath();
              this.column_path.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.I64) {
              this.timestamp = iprot.readI64();
              setTimestampIsSet(true);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 4: 
            if (field.type == TType.I32) {
              this.consistency_level = ConsistencyLevel.findByValue(iprot.readI32());
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      if (!isSetTimestamp()) {
        throw new TProtocolException("Required field 'timestamp' was not found in serialized data! Struct: " + toString());
      }
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.key != null) {
        oprot.writeFieldBegin(KEY_FIELD_DESC);
        oprot.writeBinary(this.key);
        oprot.writeFieldEnd();
      }
      if (this.column_path != null) {
        oprot.writeFieldBegin(COLUMN_PATH_FIELD_DESC);
        this.column_path.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(TIMESTAMP_FIELD_DESC);
      oprot.writeI64(this.timestamp);
      oprot.writeFieldEnd();
      if (this.consistency_level != null) {
        oprot.writeFieldBegin(CONSISTENCY_LEVEL_FIELD_DESC);
        oprot.writeI32(this.consistency_level.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("remove_args(");
      boolean first = true;
      sb.append("key:");
      if (this.key == null) {
        sb.append("null");
      } else {
        TBaseHelper.toString(this.key, sb);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("column_path:");
      if (this.column_path == null) {
        sb.append("null");
      } else {
        sb.append(this.column_path);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("timestamp:");
      sb.append(this.timestamp);
      first = false;
      if (!first) sb.append(", ");
      sb.append("consistency_level:");
      if (this.consistency_level == null) {
        sb.append("null");
      } else {
        sb.append(this.consistency_level);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (key == null) {
        throw new TProtocolException("Required field 'key' was not present! Struct: " + toString());
      }
      if (column_path == null) {
        throw new TProtocolException("Required field 'column_path' was not present! Struct: " + toString());
      }
    }
  }
  public static class remove_result implements TBase<remove_result, remove_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("remove_result");
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    private static final TField UE_FIELD_DESC = new TField("ue", TType.STRUCT, (short)2);
    private static final TField TE_FIELD_DESC = new TField("te", TType.STRUCT, (short)3);
    public InvalidRequestException ire;
    public UnavailableException ue;
    public TimedOutException te;
    public enum _Fields implements TFieldIdEnum {
      IRE((short)1, "ire"),
      UE((short)2, "ue"),
      TE((short)3, "te");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return IRE;
          case 2: 
            return UE;
          case 3: 
            return TE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.UE, new FieldMetaData("ue", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.TE, new FieldMetaData("te", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(remove_result.class, metaDataMap);
    }
    public remove_result() {
    }
    public remove_result(
      InvalidRequestException ire,
      UnavailableException ue,
      TimedOutException te)
    {
      this();
      this.ire = ire;
      this.ue = ue;
      this.te = te;
    }
    public remove_result(remove_result other) {
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
      if (other.isSetUe()) {
        this.ue = new UnavailableException(other.ue);
      }
      if (other.isSetTe()) {
        this.te = new TimedOutException(other.te);
      }
    }
    public remove_result deepCopy() {
      return new remove_result(this);
    }
    @Override
    public void clear() {
      this.ire = null;
      this.ue = null;
      this.te = null;
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public remove_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public UnavailableException getUe() {
      return this.ue;
    }
    public remove_result setUe(UnavailableException ue) {
      this.ue = ue;
      return this;
    }
    public void unsetUe() {
      this.ue = null;
    }
    public boolean isSetUe() {
      return this.ue != null;
    }
    public void setUeIsSet(boolean value) {
      if (!value) {
        this.ue = null;
      }
    }
    public TimedOutException getTe() {
      return this.te;
    }
    public remove_result setTe(TimedOutException te) {
      this.te = te;
      return this;
    }
    public void unsetTe() {
      this.te = null;
    }
    public boolean isSetTe() {
      return this.te != null;
    }
    public void setTeIsSet(boolean value) {
      if (!value) {
        this.te = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      case UE:
        if (value == null) {
          unsetUe();
        } else {
          setUe((UnavailableException)value);
        }
        break;
      case TE:
        if (value == null) {
          unsetTe();
        } else {
          setTe((TimedOutException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case IRE:
        return getIre();
      case UE:
        return getUe();
      case TE:
        return getTe();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case IRE:
        return isSetIre();
      case UE:
        return isSetUe();
      case TE:
        return isSetTe();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof remove_result)
        return this.equals((remove_result)that);
      return false;
    }
    public boolean equals(remove_result that) {
      if (that == null)
        return false;
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      boolean this_present_ue = true && this.isSetUe();
      boolean that_present_ue = true && that.isSetUe();
      if (this_present_ue || that_present_ue) {
        if (!(this_present_ue && that_present_ue))
          return false;
        if (!this.ue.equals(that.ue))
          return false;
      }
      boolean this_present_te = true && this.isSetTe();
      boolean that_present_te = true && that.isSetTe();
      if (this_present_te || that_present_te) {
        if (!(this_present_te && that_present_te))
          return false;
        if (!this.te.equals(that.te))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      boolean present_ue = true && (isSetUe());
      builder.append(present_ue);
      if (present_ue)
        builder.append(ue);
      boolean present_te = true && (isSetTe());
      builder.append(present_te);
      if (present_te)
        builder.append(te);
      return builder.toHashCode();
    }
    public int compareTo(remove_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      remove_result typedOther = (remove_result)other;
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetUe()).compareTo(typedOther.isSetUe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetUe()) {
        lastComparison = TBaseHelper.compareTo(this.ue, typedOther.ue);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetTe()).compareTo(typedOther.isSetTe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetTe()) {
        lastComparison = TBaseHelper.compareTo(this.te, typedOther.te);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.ue = new UnavailableException();
              this.ue.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.te = new TimedOutException();
              this.te.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetUe()) {
        oprot.writeFieldBegin(UE_FIELD_DESC);
        this.ue.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetTe()) {
        oprot.writeFieldBegin(TE_FIELD_DESC);
        this.te.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("remove_result(");
      boolean first = true;
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ue:");
      if (this.ue == null) {
        sb.append("null");
      } else {
        sb.append(this.ue);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("te:");
      if (this.te == null) {
        sb.append("null");
      } else {
        sb.append(this.te);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class batch_mutate_args implements TBase<batch_mutate_args, batch_mutate_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("batch_mutate_args");
    private static final TField MUTATION_MAP_FIELD_DESC = new TField("mutation_map", TType.MAP, (short)1);
    private static final TField CONSISTENCY_LEVEL_FIELD_DESC = new TField("consistency_level", TType.I32, (short)2);
    public Map<ByteBuffer,Map<String,List<Mutation>>> mutation_map;
    public ConsistencyLevel consistency_level;
    public enum _Fields implements TFieldIdEnum {
      MUTATION_MAP((short)1, "mutation_map"),
      CONSISTENCY_LEVEL((short)2, "consistency_level");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return MUTATION_MAP;
          case 2: 
            return CONSISTENCY_LEVEL;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.MUTATION_MAP, new FieldMetaData("mutation_map", TFieldRequirementType.REQUIRED, 
          new MapMetaData(TType.MAP, 
              new FieldValueMetaData(TType.STRING), 
              new MapMetaData(TType.MAP, 
                  new FieldValueMetaData(TType.STRING), 
                  new ListMetaData(TType.LIST, 
                      new StructMetaData(TType.STRUCT, Mutation.class))))));
      tmpMap.put(_Fields.CONSISTENCY_LEVEL, new FieldMetaData("consistency_level", TFieldRequirementType.REQUIRED, 
          new EnumMetaData(TType.ENUM, ConsistencyLevel.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(batch_mutate_args.class, metaDataMap);
    }
    public batch_mutate_args() {
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public batch_mutate_args(
      Map<ByteBuffer,Map<String,List<Mutation>>> mutation_map,
      ConsistencyLevel consistency_level)
    {
      this();
      this.mutation_map = mutation_map;
      this.consistency_level = consistency_level;
    }
    public batch_mutate_args(batch_mutate_args other) {
      if (other.isSetMutation_map()) {
        Map<ByteBuffer,Map<String,List<Mutation>>> __this__mutation_map = new HashMap<ByteBuffer,Map<String,List<Mutation>>>();
        for (Map.Entry<ByteBuffer, Map<String,List<Mutation>>> other_element : other.mutation_map.entrySet()) {
          ByteBuffer other_element_key = other_element.getKey();
          Map<String,List<Mutation>> other_element_value = other_element.getValue();
          ByteBuffer __this__mutation_map_copy_key = TBaseHelper.copyBinary(other_element_key);
;
          Map<String,List<Mutation>> __this__mutation_map_copy_value = new HashMap<String,List<Mutation>>();
          for (Map.Entry<String, List<Mutation>> other_element_value_element : other_element_value.entrySet()) {
            String other_element_value_element_key = other_element_value_element.getKey();
            List<Mutation> other_element_value_element_value = other_element_value_element.getValue();
            String __this__mutation_map_copy_value_copy_key = other_element_value_element_key;
            List<Mutation> __this__mutation_map_copy_value_copy_value = new ArrayList<Mutation>();
            for (Mutation other_element_value_element_value_element : other_element_value_element_value) {
              __this__mutation_map_copy_value_copy_value.add(new Mutation(other_element_value_element_value_element));
            }
            __this__mutation_map_copy_value.put(__this__mutation_map_copy_value_copy_key, __this__mutation_map_copy_value_copy_value);
          }
          __this__mutation_map.put(__this__mutation_map_copy_key, __this__mutation_map_copy_value);
        }
        this.mutation_map = __this__mutation_map;
      }
      if (other.isSetConsistency_level()) {
        this.consistency_level = other.consistency_level;
      }
    }
    public batch_mutate_args deepCopy() {
      return new batch_mutate_args(this);
    }
    @Override
    public void clear() {
      this.mutation_map = null;
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public int getMutation_mapSize() {
      return (this.mutation_map == null) ? 0 : this.mutation_map.size();
    }
    public void putToMutation_map(ByteBuffer key, Map<String,List<Mutation>> val) {
      if (this.mutation_map == null) {
        this.mutation_map = new HashMap<ByteBuffer,Map<String,List<Mutation>>>();
      }
      this.mutation_map.put(key, val);
    }
    public Map<ByteBuffer,Map<String,List<Mutation>>> getMutation_map() {
      return this.mutation_map;
    }
    public batch_mutate_args setMutation_map(Map<ByteBuffer,Map<String,List<Mutation>>> mutation_map) {
      this.mutation_map = mutation_map;
      return this;
    }
    public void unsetMutation_map() {
      this.mutation_map = null;
    }
    public boolean isSetMutation_map() {
      return this.mutation_map != null;
    }
    public void setMutation_mapIsSet(boolean value) {
      if (!value) {
        this.mutation_map = null;
      }
    }
    public ConsistencyLevel getConsistency_level() {
      return this.consistency_level;
    }
    public batch_mutate_args setConsistency_level(ConsistencyLevel consistency_level) {
      this.consistency_level = consistency_level;
      return this;
    }
    public void unsetConsistency_level() {
      this.consistency_level = null;
    }
    public boolean isSetConsistency_level() {
      return this.consistency_level != null;
    }
    public void setConsistency_levelIsSet(boolean value) {
      if (!value) {
        this.consistency_level = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case MUTATION_MAP:
        if (value == null) {
          unsetMutation_map();
        } else {
          setMutation_map((Map<ByteBuffer,Map<String,List<Mutation>>>)value);
        }
        break;
      case CONSISTENCY_LEVEL:
        if (value == null) {
          unsetConsistency_level();
        } else {
          setConsistency_level((ConsistencyLevel)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case MUTATION_MAP:
        return getMutation_map();
      case CONSISTENCY_LEVEL:
        return getConsistency_level();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case MUTATION_MAP:
        return isSetMutation_map();
      case CONSISTENCY_LEVEL:
        return isSetConsistency_level();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof batch_mutate_args)
        return this.equals((batch_mutate_args)that);
      return false;
    }
    public boolean equals(batch_mutate_args that) {
      if (that == null)
        return false;
      boolean this_present_mutation_map = true && this.isSetMutation_map();
      boolean that_present_mutation_map = true && that.isSetMutation_map();
      if (this_present_mutation_map || that_present_mutation_map) {
        if (!(this_present_mutation_map && that_present_mutation_map))
          return false;
        if (!this.mutation_map.equals(that.mutation_map))
          return false;
      }
      boolean this_present_consistency_level = true && this.isSetConsistency_level();
      boolean that_present_consistency_level = true && that.isSetConsistency_level();
      if (this_present_consistency_level || that_present_consistency_level) {
        if (!(this_present_consistency_level && that_present_consistency_level))
          return false;
        if (!this.consistency_level.equals(that.consistency_level))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_mutation_map = true && (isSetMutation_map());
      builder.append(present_mutation_map);
      if (present_mutation_map)
        builder.append(mutation_map);
      boolean present_consistency_level = true && (isSetConsistency_level());
      builder.append(present_consistency_level);
      if (present_consistency_level)
        builder.append(consistency_level.getValue());
      return builder.toHashCode();
    }
    public int compareTo(batch_mutate_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      batch_mutate_args typedOther = (batch_mutate_args)other;
      lastComparison = Boolean.valueOf(isSetMutation_map()).compareTo(typedOther.isSetMutation_map());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetMutation_map()) {
        lastComparison = TBaseHelper.compareTo(this.mutation_map, typedOther.mutation_map);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetConsistency_level()).compareTo(typedOther.isSetConsistency_level());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetConsistency_level()) {
        lastComparison = TBaseHelper.compareTo(this.consistency_level, typedOther.consistency_level);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.MAP) {
              {
                TMap _map84 = iprot.readMapBegin();
                this.mutation_map = new HashMap<ByteBuffer,Map<String,List<Mutation>>>(2*_map84.size);
                for (int _i85 = 0; _i85 < _map84.size; ++_i85)
                {
                  ByteBuffer _key86;
                  Map<String,List<Mutation>> _val87;
                  _key86 = iprot.readBinary();
                  {
                    TMap _map88 = iprot.readMapBegin();
                    _val87 = new HashMap<String,List<Mutation>>(2*_map88.size);
                    for (int _i89 = 0; _i89 < _map88.size; ++_i89)
                    {
                      String _key90;
                      List<Mutation> _val91;
                      _key90 = iprot.readString();
                      {
                        TList _list92 = iprot.readListBegin();
                        _val91 = new ArrayList<Mutation>(_list92.size);
                        for (int _i93 = 0; _i93 < _list92.size; ++_i93)
                        {
                          Mutation _elem94;
                          _elem94 = new Mutation();
                          _elem94.read(iprot);
                          _val91.add(_elem94);
                        }
                        iprot.readListEnd();
                      }
                      _val87.put(_key90, _val91);
                    }
                    iprot.readMapEnd();
                  }
                  this.mutation_map.put(_key86, _val87);
                }
                iprot.readMapEnd();
              }
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.I32) {
              this.consistency_level = ConsistencyLevel.findByValue(iprot.readI32());
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.mutation_map != null) {
        oprot.writeFieldBegin(MUTATION_MAP_FIELD_DESC);
        {
          oprot.writeMapBegin(new TMap(TType.STRING, TType.MAP, this.mutation_map.size()));
          for (Map.Entry<ByteBuffer, Map<String,List<Mutation>>> _iter95 : this.mutation_map.entrySet())
          {
            oprot.writeBinary(_iter95.getKey());
            {
              oprot.writeMapBegin(new TMap(TType.STRING, TType.LIST, _iter95.getValue().size()));
              for (Map.Entry<String, List<Mutation>> _iter96 : _iter95.getValue().entrySet())
              {
                oprot.writeString(_iter96.getKey());
                {
                  oprot.writeListBegin(new TList(TType.STRUCT, _iter96.getValue().size()));
                  for (Mutation _iter97 : _iter96.getValue())
                  {
                    _iter97.write(oprot);
                  }
                  oprot.writeListEnd();
                }
              }
              oprot.writeMapEnd();
            }
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      }
      if (this.consistency_level != null) {
        oprot.writeFieldBegin(CONSISTENCY_LEVEL_FIELD_DESC);
        oprot.writeI32(this.consistency_level.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("batch_mutate_args(");
      boolean first = true;
      sb.append("mutation_map:");
      if (this.mutation_map == null) {
        sb.append("null");
      } else {
        sb.append(this.mutation_map);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("consistency_level:");
      if (this.consistency_level == null) {
        sb.append("null");
      } else {
        sb.append(this.consistency_level);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (mutation_map == null) {
        throw new TProtocolException("Required field 'mutation_map' was not present! Struct: " + toString());
      }
      if (consistency_level == null) {
        throw new TProtocolException("Required field 'consistency_level' was not present! Struct: " + toString());
      }
    }
  }
  public static class batch_mutate_result implements TBase<batch_mutate_result, batch_mutate_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("batch_mutate_result");
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    private static final TField UE_FIELD_DESC = new TField("ue", TType.STRUCT, (short)2);
    private static final TField TE_FIELD_DESC = new TField("te", TType.STRUCT, (short)3);
    public InvalidRequestException ire;
    public UnavailableException ue;
    public TimedOutException te;
    public enum _Fields implements TFieldIdEnum {
      IRE((short)1, "ire"),
      UE((short)2, "ue"),
      TE((short)3, "te");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return IRE;
          case 2: 
            return UE;
          case 3: 
            return TE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.UE, new FieldMetaData("ue", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.TE, new FieldMetaData("te", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(batch_mutate_result.class, metaDataMap);
    }
    public batch_mutate_result() {
    }
    public batch_mutate_result(
      InvalidRequestException ire,
      UnavailableException ue,
      TimedOutException te)
    {
      this();
      this.ire = ire;
      this.ue = ue;
      this.te = te;
    }
    public batch_mutate_result(batch_mutate_result other) {
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
      if (other.isSetUe()) {
        this.ue = new UnavailableException(other.ue);
      }
      if (other.isSetTe()) {
        this.te = new TimedOutException(other.te);
      }
    }
    public batch_mutate_result deepCopy() {
      return new batch_mutate_result(this);
    }
    @Override
    public void clear() {
      this.ire = null;
      this.ue = null;
      this.te = null;
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public batch_mutate_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public UnavailableException getUe() {
      return this.ue;
    }
    public batch_mutate_result setUe(UnavailableException ue) {
      this.ue = ue;
      return this;
    }
    public void unsetUe() {
      this.ue = null;
    }
    public boolean isSetUe() {
      return this.ue != null;
    }
    public void setUeIsSet(boolean value) {
      if (!value) {
        this.ue = null;
      }
    }
    public TimedOutException getTe() {
      return this.te;
    }
    public batch_mutate_result setTe(TimedOutException te) {
      this.te = te;
      return this;
    }
    public void unsetTe() {
      this.te = null;
    }
    public boolean isSetTe() {
      return this.te != null;
    }
    public void setTeIsSet(boolean value) {
      if (!value) {
        this.te = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      case UE:
        if (value == null) {
          unsetUe();
        } else {
          setUe((UnavailableException)value);
        }
        break;
      case TE:
        if (value == null) {
          unsetTe();
        } else {
          setTe((TimedOutException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case IRE:
        return getIre();
      case UE:
        return getUe();
      case TE:
        return getTe();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case IRE:
        return isSetIre();
      case UE:
        return isSetUe();
      case TE:
        return isSetTe();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof batch_mutate_result)
        return this.equals((batch_mutate_result)that);
      return false;
    }
    public boolean equals(batch_mutate_result that) {
      if (that == null)
        return false;
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      boolean this_present_ue = true && this.isSetUe();
      boolean that_present_ue = true && that.isSetUe();
      if (this_present_ue || that_present_ue) {
        if (!(this_present_ue && that_present_ue))
          return false;
        if (!this.ue.equals(that.ue))
          return false;
      }
      boolean this_present_te = true && this.isSetTe();
      boolean that_present_te = true && that.isSetTe();
      if (this_present_te || that_present_te) {
        if (!(this_present_te && that_present_te))
          return false;
        if (!this.te.equals(that.te))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      boolean present_ue = true && (isSetUe());
      builder.append(present_ue);
      if (present_ue)
        builder.append(ue);
      boolean present_te = true && (isSetTe());
      builder.append(present_te);
      if (present_te)
        builder.append(te);
      return builder.toHashCode();
    }
    public int compareTo(batch_mutate_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      batch_mutate_result typedOther = (batch_mutate_result)other;
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetUe()).compareTo(typedOther.isSetUe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetUe()) {
        lastComparison = TBaseHelper.compareTo(this.ue, typedOther.ue);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetTe()).compareTo(typedOther.isSetTe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetTe()) {
        lastComparison = TBaseHelper.compareTo(this.te, typedOther.te);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.ue = new UnavailableException();
              this.ue.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.te = new TimedOutException();
              this.te.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetUe()) {
        oprot.writeFieldBegin(UE_FIELD_DESC);
        this.ue.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetTe()) {
        oprot.writeFieldBegin(TE_FIELD_DESC);
        this.te.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("batch_mutate_result(");
      boolean first = true;
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ue:");
      if (this.ue == null) {
        sb.append("null");
      } else {
        sb.append(this.ue);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("te:");
      if (this.te == null) {
        sb.append("null");
      } else {
        sb.append(this.te);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class truncate_args implements TBase<truncate_args, truncate_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("truncate_args");
    private static final TField CFNAME_FIELD_DESC = new TField("cfname", TType.STRING, (short)1);
    public String cfname;
    public enum _Fields implements TFieldIdEnum {
      CFNAME((short)1, "cfname");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return CFNAME;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.CFNAME, new FieldMetaData("cfname", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.STRING)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(truncate_args.class, metaDataMap);
    }
    public truncate_args() {
    }
    public truncate_args(
      String cfname)
    {
      this();
      this.cfname = cfname;
    }
    public truncate_args(truncate_args other) {
      if (other.isSetCfname()) {
        this.cfname = other.cfname;
      }
    }
    public truncate_args deepCopy() {
      return new truncate_args(this);
    }
    @Override
    public void clear() {
      this.cfname = null;
    }
    public String getCfname() {
      return this.cfname;
    }
    public truncate_args setCfname(String cfname) {
      this.cfname = cfname;
      return this;
    }
    public void unsetCfname() {
      this.cfname = null;
    }
    public boolean isSetCfname() {
      return this.cfname != null;
    }
    public void setCfnameIsSet(boolean value) {
      if (!value) {
        this.cfname = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case CFNAME:
        if (value == null) {
          unsetCfname();
        } else {
          setCfname((String)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case CFNAME:
        return getCfname();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case CFNAME:
        return isSetCfname();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof truncate_args)
        return this.equals((truncate_args)that);
      return false;
    }
    public boolean equals(truncate_args that) {
      if (that == null)
        return false;
      boolean this_present_cfname = true && this.isSetCfname();
      boolean that_present_cfname = true && that.isSetCfname();
      if (this_present_cfname || that_present_cfname) {
        if (!(this_present_cfname && that_present_cfname))
          return false;
        if (!this.cfname.equals(that.cfname))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_cfname = true && (isSetCfname());
      builder.append(present_cfname);
      if (present_cfname)
        builder.append(cfname);
      return builder.toHashCode();
    }
    public int compareTo(truncate_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      truncate_args typedOther = (truncate_args)other;
      lastComparison = Boolean.valueOf(isSetCfname()).compareTo(typedOther.isSetCfname());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetCfname()) {
        lastComparison = TBaseHelper.compareTo(this.cfname, typedOther.cfname);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRING) {
              this.cfname = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.cfname != null) {
        oprot.writeFieldBegin(CFNAME_FIELD_DESC);
        oprot.writeString(this.cfname);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("truncate_args(");
      boolean first = true;
      sb.append("cfname:");
      if (this.cfname == null) {
        sb.append("null");
      } else {
        sb.append(this.cfname);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (cfname == null) {
        throw new TProtocolException("Required field 'cfname' was not present! Struct: " + toString());
      }
    }
  }
  public static class truncate_result implements TBase<truncate_result, truncate_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("truncate_result");
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    private static final TField UE_FIELD_DESC = new TField("ue", TType.STRUCT, (short)2);
    public InvalidRequestException ire;
    public UnavailableException ue;
    public enum _Fields implements TFieldIdEnum {
      IRE((short)1, "ire"),
      UE((short)2, "ue");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return IRE;
          case 2: 
            return UE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.UE, new FieldMetaData("ue", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(truncate_result.class, metaDataMap);
    }
    public truncate_result() {
    }
    public truncate_result(
      InvalidRequestException ire,
      UnavailableException ue)
    {
      this();
      this.ire = ire;
      this.ue = ue;
    }
    public truncate_result(truncate_result other) {
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
      if (other.isSetUe()) {
        this.ue = new UnavailableException(other.ue);
      }
    }
    public truncate_result deepCopy() {
      return new truncate_result(this);
    }
    @Override
    public void clear() {
      this.ire = null;
      this.ue = null;
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public truncate_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public UnavailableException getUe() {
      return this.ue;
    }
    public truncate_result setUe(UnavailableException ue) {
      this.ue = ue;
      return this;
    }
    public void unsetUe() {
      this.ue = null;
    }
    public boolean isSetUe() {
      return this.ue != null;
    }
    public void setUeIsSet(boolean value) {
      if (!value) {
        this.ue = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      case UE:
        if (value == null) {
          unsetUe();
        } else {
          setUe((UnavailableException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case IRE:
        return getIre();
      case UE:
        return getUe();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case IRE:
        return isSetIre();
      case UE:
        return isSetUe();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof truncate_result)
        return this.equals((truncate_result)that);
      return false;
    }
    public boolean equals(truncate_result that) {
      if (that == null)
        return false;
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      boolean this_present_ue = true && this.isSetUe();
      boolean that_present_ue = true && that.isSetUe();
      if (this_present_ue || that_present_ue) {
        if (!(this_present_ue && that_present_ue))
          return false;
        if (!this.ue.equals(that.ue))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      boolean present_ue = true && (isSetUe());
      builder.append(present_ue);
      if (present_ue)
        builder.append(ue);
      return builder.toHashCode();
    }
    public int compareTo(truncate_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      truncate_result typedOther = (truncate_result)other;
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetUe()).compareTo(typedOther.isSetUe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetUe()) {
        lastComparison = TBaseHelper.compareTo(this.ue, typedOther.ue);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.ue = new UnavailableException();
              this.ue.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetUe()) {
        oprot.writeFieldBegin(UE_FIELD_DESC);
        this.ue.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("truncate_result(");
      boolean first = true;
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ue:");
      if (this.ue == null) {
        sb.append("null");
      } else {
        sb.append(this.ue);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class add_args implements TBase<add_args, add_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("add_args");
    private static final TField KEY_FIELD_DESC = new TField("key", TType.STRING, (short)1);
    private static final TField COLUMN_PARENT_FIELD_DESC = new TField("column_parent", TType.STRUCT, (short)2);
    private static final TField COLUMN_FIELD_DESC = new TField("column", TType.STRUCT, (short)3);
    private static final TField CONSISTENCY_LEVEL_FIELD_DESC = new TField("consistency_level", TType.I32, (short)4);
    public ByteBuffer key;
    public ColumnParent column_parent;
    public CounterColumn column;
    public ConsistencyLevel consistency_level;
    public enum _Fields implements TFieldIdEnum {
      KEY((short)1, "key"),
      COLUMN_PARENT((short)2, "column_parent"),
      COLUMN((short)3, "column"),
      CONSISTENCY_LEVEL((short)4, "consistency_level");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return KEY;
          case 2: 
            return COLUMN_PARENT;
          case 3: 
            return COLUMN;
          case 4: 
            return CONSISTENCY_LEVEL;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.KEY, new FieldMetaData("key", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.STRING)));
      tmpMap.put(_Fields.COLUMN_PARENT, new FieldMetaData("column_parent", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, ColumnParent.class)));
      tmpMap.put(_Fields.COLUMN, new FieldMetaData("column", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, CounterColumn.class)));
      tmpMap.put(_Fields.CONSISTENCY_LEVEL, new FieldMetaData("consistency_level", TFieldRequirementType.REQUIRED, 
          new EnumMetaData(TType.ENUM, ConsistencyLevel.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(add_args.class, metaDataMap);
    }
    public add_args() {
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public add_args(
      ByteBuffer key,
      ColumnParent column_parent,
      CounterColumn column,
      ConsistencyLevel consistency_level)
    {
      this();
      this.key = key;
      this.column_parent = column_parent;
      this.column = column;
      this.consistency_level = consistency_level;
    }
    public add_args(add_args other) {
      if (other.isSetKey()) {
        this.key = TBaseHelper.copyBinary(other.key);
;
      }
      if (other.isSetColumn_parent()) {
        this.column_parent = new ColumnParent(other.column_parent);
      }
      if (other.isSetColumn()) {
        this.column = new CounterColumn(other.column);
      }
      if (other.isSetConsistency_level()) {
        this.consistency_level = other.consistency_level;
      }
    }
    public add_args deepCopy() {
      return new add_args(this);
    }
    @Override
    public void clear() {
      this.key = null;
      this.column_parent = null;
      this.column = null;
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public byte[] getKey() {
      setKey(TBaseHelper.rightSize(key));
      return key.array();
    }
    public ByteBuffer BufferForKey() {
      return key;
    }
    public add_args setKey(byte[] key) {
      setKey(ByteBuffer.wrap(key));
      return this;
    }
    public add_args setKey(ByteBuffer key) {
      this.key = key;
      return this;
    }
    public void unsetKey() {
      this.key = null;
    }
    public boolean isSetKey() {
      return this.key != null;
    }
    public void setKeyIsSet(boolean value) {
      if (!value) {
        this.key = null;
      }
    }
    public ColumnParent getColumn_parent() {
      return this.column_parent;
    }
    public add_args setColumn_parent(ColumnParent column_parent) {
      this.column_parent = column_parent;
      return this;
    }
    public void unsetColumn_parent() {
      this.column_parent = null;
    }
    public boolean isSetColumn_parent() {
      return this.column_parent != null;
    }
    public void setColumn_parentIsSet(boolean value) {
      if (!value) {
        this.column_parent = null;
      }
    }
    public CounterColumn getColumn() {
      return this.column;
    }
    public add_args setColumn(CounterColumn column) {
      this.column = column;
      return this;
    }
    public void unsetColumn() {
      this.column = null;
    }
    public boolean isSetColumn() {
      return this.column != null;
    }
    public void setColumnIsSet(boolean value) {
      if (!value) {
        this.column = null;
      }
    }
    public ConsistencyLevel getConsistency_level() {
      return this.consistency_level;
    }
    public add_args setConsistency_level(ConsistencyLevel consistency_level) {
      this.consistency_level = consistency_level;
      return this;
    }
    public void unsetConsistency_level() {
      this.consistency_level = null;
    }
    public boolean isSetConsistency_level() {
      return this.consistency_level != null;
    }
    public void setConsistency_levelIsSet(boolean value) {
      if (!value) {
        this.consistency_level = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case KEY:
        if (value == null) {
          unsetKey();
        } else {
          setKey((ByteBuffer)value);
        }
        break;
      case COLUMN_PARENT:
        if (value == null) {
          unsetColumn_parent();
        } else {
          setColumn_parent((ColumnParent)value);
        }
        break;
      case COLUMN:
        if (value == null) {
          unsetColumn();
        } else {
          setColumn((CounterColumn)value);
        }
        break;
      case CONSISTENCY_LEVEL:
        if (value == null) {
          unsetConsistency_level();
        } else {
          setConsistency_level((ConsistencyLevel)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case KEY:
        return getKey();
      case COLUMN_PARENT:
        return getColumn_parent();
      case COLUMN:
        return getColumn();
      case CONSISTENCY_LEVEL:
        return getConsistency_level();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case KEY:
        return isSetKey();
      case COLUMN_PARENT:
        return isSetColumn_parent();
      case COLUMN:
        return isSetColumn();
      case CONSISTENCY_LEVEL:
        return isSetConsistency_level();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof add_args)
        return this.equals((add_args)that);
      return false;
    }
    public boolean equals(add_args that) {
      if (that == null)
        return false;
      boolean this_present_key = true && this.isSetKey();
      boolean that_present_key = true && that.isSetKey();
      if (this_present_key || that_present_key) {
        if (!(this_present_key && that_present_key))
          return false;
        if (!this.key.equals(that.key))
          return false;
      }
      boolean this_present_column_parent = true && this.isSetColumn_parent();
      boolean that_present_column_parent = true && that.isSetColumn_parent();
      if (this_present_column_parent || that_present_column_parent) {
        if (!(this_present_column_parent && that_present_column_parent))
          return false;
        if (!this.column_parent.equals(that.column_parent))
          return false;
      }
      boolean this_present_column = true && this.isSetColumn();
      boolean that_present_column = true && that.isSetColumn();
      if (this_present_column || that_present_column) {
        if (!(this_present_column && that_present_column))
          return false;
        if (!this.column.equals(that.column))
          return false;
      }
      boolean this_present_consistency_level = true && this.isSetConsistency_level();
      boolean that_present_consistency_level = true && that.isSetConsistency_level();
      if (this_present_consistency_level || that_present_consistency_level) {
        if (!(this_present_consistency_level && that_present_consistency_level))
          return false;
        if (!this.consistency_level.equals(that.consistency_level))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_key = true && (isSetKey());
      builder.append(present_key);
      if (present_key)
        builder.append(key);
      boolean present_column_parent = true && (isSetColumn_parent());
      builder.append(present_column_parent);
      if (present_column_parent)
        builder.append(column_parent);
      boolean present_column = true && (isSetColumn());
      builder.append(present_column);
      if (present_column)
        builder.append(column);
      boolean present_consistency_level = true && (isSetConsistency_level());
      builder.append(present_consistency_level);
      if (present_consistency_level)
        builder.append(consistency_level.getValue());
      return builder.toHashCode();
    }
    public int compareTo(add_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      add_args typedOther = (add_args)other;
      lastComparison = Boolean.valueOf(isSetKey()).compareTo(typedOther.isSetKey());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetKey()) {
        lastComparison = TBaseHelper.compareTo(this.key, typedOther.key);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetColumn_parent()).compareTo(typedOther.isSetColumn_parent());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetColumn_parent()) {
        lastComparison = TBaseHelper.compareTo(this.column_parent, typedOther.column_parent);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetColumn()).compareTo(typedOther.isSetColumn());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetColumn()) {
        lastComparison = TBaseHelper.compareTo(this.column, typedOther.column);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetConsistency_level()).compareTo(typedOther.isSetConsistency_level());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetConsistency_level()) {
        lastComparison = TBaseHelper.compareTo(this.consistency_level, typedOther.consistency_level);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRING) {
              this.key = iprot.readBinary();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.column_parent = new ColumnParent();
              this.column_parent.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.column = new CounterColumn();
              this.column.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 4: 
            if (field.type == TType.I32) {
              this.consistency_level = ConsistencyLevel.findByValue(iprot.readI32());
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.key != null) {
        oprot.writeFieldBegin(KEY_FIELD_DESC);
        oprot.writeBinary(this.key);
        oprot.writeFieldEnd();
      }
      if (this.column_parent != null) {
        oprot.writeFieldBegin(COLUMN_PARENT_FIELD_DESC);
        this.column_parent.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.column != null) {
        oprot.writeFieldBegin(COLUMN_FIELD_DESC);
        this.column.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.consistency_level != null) {
        oprot.writeFieldBegin(CONSISTENCY_LEVEL_FIELD_DESC);
        oprot.writeI32(this.consistency_level.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("add_args(");
      boolean first = true;
      sb.append("key:");
      if (this.key == null) {
        sb.append("null");
      } else {
        TBaseHelper.toString(this.key, sb);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("column_parent:");
      if (this.column_parent == null) {
        sb.append("null");
      } else {
        sb.append(this.column_parent);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("column:");
      if (this.column == null) {
        sb.append("null");
      } else {
        sb.append(this.column);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("consistency_level:");
      if (this.consistency_level == null) {
        sb.append("null");
      } else {
        sb.append(this.consistency_level);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (key == null) {
        throw new TProtocolException("Required field 'key' was not present! Struct: " + toString());
      }
      if (column_parent == null) {
        throw new TProtocolException("Required field 'column_parent' was not present! Struct: " + toString());
      }
      if (column == null) {
        throw new TProtocolException("Required field 'column' was not present! Struct: " + toString());
      }
      if (consistency_level == null) {
        throw new TProtocolException("Required field 'consistency_level' was not present! Struct: " + toString());
      }
    }
  }
  public static class add_result implements TBase<add_result, add_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("add_result");
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    private static final TField UE_FIELD_DESC = new TField("ue", TType.STRUCT, (short)2);
    private static final TField TE_FIELD_DESC = new TField("te", TType.STRUCT, (short)3);
    public InvalidRequestException ire;
    public UnavailableException ue;
    public TimedOutException te;
    public enum _Fields implements TFieldIdEnum {
      IRE((short)1, "ire"),
      UE((short)2, "ue"),
      TE((short)3, "te");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return IRE;
          case 2: 
            return UE;
          case 3: 
            return TE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.UE, new FieldMetaData("ue", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.TE, new FieldMetaData("te", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(add_result.class, metaDataMap);
    }
    public add_result() {
    }
    public add_result(
      InvalidRequestException ire,
      UnavailableException ue,
      TimedOutException te)
    {
      this();
      this.ire = ire;
      this.ue = ue;
      this.te = te;
    }
    public add_result(add_result other) {
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
      if (other.isSetUe()) {
        this.ue = new UnavailableException(other.ue);
      }
      if (other.isSetTe()) {
        this.te = new TimedOutException(other.te);
      }
    }
    public add_result deepCopy() {
      return new add_result(this);
    }
    @Override
    public void clear() {
      this.ire = null;
      this.ue = null;
      this.te = null;
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public add_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public UnavailableException getUe() {
      return this.ue;
    }
    public add_result setUe(UnavailableException ue) {
      this.ue = ue;
      return this;
    }
    public void unsetUe() {
      this.ue = null;
    }
    public boolean isSetUe() {
      return this.ue != null;
    }
    public void setUeIsSet(boolean value) {
      if (!value) {
        this.ue = null;
      }
    }
    public TimedOutException getTe() {
      return this.te;
    }
    public add_result setTe(TimedOutException te) {
      this.te = te;
      return this;
    }
    public void unsetTe() {
      this.te = null;
    }
    public boolean isSetTe() {
      return this.te != null;
    }
    public void setTeIsSet(boolean value) {
      if (!value) {
        this.te = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      case UE:
        if (value == null) {
          unsetUe();
        } else {
          setUe((UnavailableException)value);
        }
        break;
      case TE:
        if (value == null) {
          unsetTe();
        } else {
          setTe((TimedOutException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case IRE:
        return getIre();
      case UE:
        return getUe();
      case TE:
        return getTe();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case IRE:
        return isSetIre();
      case UE:
        return isSetUe();
      case TE:
        return isSetTe();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof add_result)
        return this.equals((add_result)that);
      return false;
    }
    public boolean equals(add_result that) {
      if (that == null)
        return false;
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      boolean this_present_ue = true && this.isSetUe();
      boolean that_present_ue = true && that.isSetUe();
      if (this_present_ue || that_present_ue) {
        if (!(this_present_ue && that_present_ue))
          return false;
        if (!this.ue.equals(that.ue))
          return false;
      }
      boolean this_present_te = true && this.isSetTe();
      boolean that_present_te = true && that.isSetTe();
      if (this_present_te || that_present_te) {
        if (!(this_present_te && that_present_te))
          return false;
        if (!this.te.equals(that.te))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      boolean present_ue = true && (isSetUe());
      builder.append(present_ue);
      if (present_ue)
        builder.append(ue);
      boolean present_te = true && (isSetTe());
      builder.append(present_te);
      if (present_te)
        builder.append(te);
      return builder.toHashCode();
    }
    public int compareTo(add_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      add_result typedOther = (add_result)other;
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetUe()).compareTo(typedOther.isSetUe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetUe()) {
        lastComparison = TBaseHelper.compareTo(this.ue, typedOther.ue);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetTe()).compareTo(typedOther.isSetTe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetTe()) {
        lastComparison = TBaseHelper.compareTo(this.te, typedOther.te);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.ue = new UnavailableException();
              this.ue.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.te = new TimedOutException();
              this.te.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetUe()) {
        oprot.writeFieldBegin(UE_FIELD_DESC);
        this.ue.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetTe()) {
        oprot.writeFieldBegin(TE_FIELD_DESC);
        this.te.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("add_result(");
      boolean first = true;
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ue:");
      if (this.ue == null) {
        sb.append("null");
      } else {
        sb.append(this.ue);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("te:");
      if (this.te == null) {
        sb.append("null");
      } else {
        sb.append(this.te);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class batch_add_args implements TBase<batch_add_args, batch_add_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("batch_add_args");
    private static final TField UPDATE_MAP_FIELD_DESC = new TField("update_map", TType.MAP, (short)1);
    private static final TField CONSISTENCY_LEVEL_FIELD_DESC = new TField("consistency_level", TType.I32, (short)2);
    public Map<ByteBuffer,Map<String,List<CounterMutation>>> update_map;
    public ConsistencyLevel consistency_level;
    public enum _Fields implements TFieldIdEnum {
      UPDATE_MAP((short)1, "update_map"),
      CONSISTENCY_LEVEL((short)2, "consistency_level");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return UPDATE_MAP;
          case 2: 
            return CONSISTENCY_LEVEL;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.UPDATE_MAP, new FieldMetaData("update_map", TFieldRequirementType.REQUIRED, 
          new MapMetaData(TType.MAP, 
              new FieldValueMetaData(TType.STRING), 
              new MapMetaData(TType.MAP, 
                  new FieldValueMetaData(TType.STRING), 
                  new ListMetaData(TType.LIST, 
                      new StructMetaData(TType.STRUCT, CounterMutation.class))))));
      tmpMap.put(_Fields.CONSISTENCY_LEVEL, new FieldMetaData("consistency_level", TFieldRequirementType.REQUIRED, 
          new EnumMetaData(TType.ENUM, ConsistencyLevel.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(batch_add_args.class, metaDataMap);
    }
    public batch_add_args() {
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public batch_add_args(
      Map<ByteBuffer,Map<String,List<CounterMutation>>> update_map,
      ConsistencyLevel consistency_level)
    {
      this();
      this.update_map = update_map;
      this.consistency_level = consistency_level;
    }
    public batch_add_args(batch_add_args other) {
      if (other.isSetUpdate_map()) {
        Map<ByteBuffer,Map<String,List<CounterMutation>>> __this__update_map = new HashMap<ByteBuffer,Map<String,List<CounterMutation>>>();
        for (Map.Entry<ByteBuffer, Map<String,List<CounterMutation>>> other_element : other.update_map.entrySet()) {
          ByteBuffer other_element_key = other_element.getKey();
          Map<String,List<CounterMutation>> other_element_value = other_element.getValue();
          ByteBuffer __this__update_map_copy_key = TBaseHelper.copyBinary(other_element_key);
;
          Map<String,List<CounterMutation>> __this__update_map_copy_value = new HashMap<String,List<CounterMutation>>();
          for (Map.Entry<String, List<CounterMutation>> other_element_value_element : other_element_value.entrySet()) {
            String other_element_value_element_key = other_element_value_element.getKey();
            List<CounterMutation> other_element_value_element_value = other_element_value_element.getValue();
            String __this__update_map_copy_value_copy_key = other_element_value_element_key;
            List<CounterMutation> __this__update_map_copy_value_copy_value = new ArrayList<CounterMutation>();
            for (CounterMutation other_element_value_element_value_element : other_element_value_element_value) {
              __this__update_map_copy_value_copy_value.add(new CounterMutation(other_element_value_element_value_element));
            }
            __this__update_map_copy_value.put(__this__update_map_copy_value_copy_key, __this__update_map_copy_value_copy_value);
          }
          __this__update_map.put(__this__update_map_copy_key, __this__update_map_copy_value);
        }
        this.update_map = __this__update_map;
      }
      if (other.isSetConsistency_level()) {
        this.consistency_level = other.consistency_level;
      }
    }
    public batch_add_args deepCopy() {
      return new batch_add_args(this);
    }
    @Override
    public void clear() {
      this.update_map = null;
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public int getUpdate_mapSize() {
      return (this.update_map == null) ? 0 : this.update_map.size();
    }
    public void putToUpdate_map(ByteBuffer key, Map<String,List<CounterMutation>> val) {
      if (this.update_map == null) {
        this.update_map = new HashMap<ByteBuffer,Map<String,List<CounterMutation>>>();
      }
      this.update_map.put(key, val);
    }
    public Map<ByteBuffer,Map<String,List<CounterMutation>>> getUpdate_map() {
      return this.update_map;
    }
    public batch_add_args setUpdate_map(Map<ByteBuffer,Map<String,List<CounterMutation>>> update_map) {
      this.update_map = update_map;
      return this;
    }
    public void unsetUpdate_map() {
      this.update_map = null;
    }
    public boolean isSetUpdate_map() {
      return this.update_map != null;
    }
    public void setUpdate_mapIsSet(boolean value) {
      if (!value) {
        this.update_map = null;
      }
    }
    public ConsistencyLevel getConsistency_level() {
      return this.consistency_level;
    }
    public batch_add_args setConsistency_level(ConsistencyLevel consistency_level) {
      this.consistency_level = consistency_level;
      return this;
    }
    public void unsetConsistency_level() {
      this.consistency_level = null;
    }
    public boolean isSetConsistency_level() {
      return this.consistency_level != null;
    }
    public void setConsistency_levelIsSet(boolean value) {
      if (!value) {
        this.consistency_level = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case UPDATE_MAP:
        if (value == null) {
          unsetUpdate_map();
        } else {
          setUpdate_map((Map<ByteBuffer,Map<String,List<CounterMutation>>>)value);
        }
        break;
      case CONSISTENCY_LEVEL:
        if (value == null) {
          unsetConsistency_level();
        } else {
          setConsistency_level((ConsistencyLevel)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case UPDATE_MAP:
        return getUpdate_map();
      case CONSISTENCY_LEVEL:
        return getConsistency_level();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case UPDATE_MAP:
        return isSetUpdate_map();
      case CONSISTENCY_LEVEL:
        return isSetConsistency_level();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof batch_add_args)
        return this.equals((batch_add_args)that);
      return false;
    }
    public boolean equals(batch_add_args that) {
      if (that == null)
        return false;
      boolean this_present_update_map = true && this.isSetUpdate_map();
      boolean that_present_update_map = true && that.isSetUpdate_map();
      if (this_present_update_map || that_present_update_map) {
        if (!(this_present_update_map && that_present_update_map))
          return false;
        if (!this.update_map.equals(that.update_map))
          return false;
      }
      boolean this_present_consistency_level = true && this.isSetConsistency_level();
      boolean that_present_consistency_level = true && that.isSetConsistency_level();
      if (this_present_consistency_level || that_present_consistency_level) {
        if (!(this_present_consistency_level && that_present_consistency_level))
          return false;
        if (!this.consistency_level.equals(that.consistency_level))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_update_map = true && (isSetUpdate_map());
      builder.append(present_update_map);
      if (present_update_map)
        builder.append(update_map);
      boolean present_consistency_level = true && (isSetConsistency_level());
      builder.append(present_consistency_level);
      if (present_consistency_level)
        builder.append(consistency_level.getValue());
      return builder.toHashCode();
    }
    public int compareTo(batch_add_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      batch_add_args typedOther = (batch_add_args)other;
      lastComparison = Boolean.valueOf(isSetUpdate_map()).compareTo(typedOther.isSetUpdate_map());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetUpdate_map()) {
        lastComparison = TBaseHelper.compareTo(this.update_map, typedOther.update_map);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetConsistency_level()).compareTo(typedOther.isSetConsistency_level());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetConsistency_level()) {
        lastComparison = TBaseHelper.compareTo(this.consistency_level, typedOther.consistency_level);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.MAP) {
              {
                TMap _map98 = iprot.readMapBegin();
                this.update_map = new HashMap<ByteBuffer,Map<String,List<CounterMutation>>>(2*_map98.size);
                for (int _i99 = 0; _i99 < _map98.size; ++_i99)
                {
                  ByteBuffer _key100;
                  Map<String,List<CounterMutation>> _val101;
                  _key100 = iprot.readBinary();
                  {
                    TMap _map102 = iprot.readMapBegin();
                    _val101 = new HashMap<String,List<CounterMutation>>(2*_map102.size);
                    for (int _i103 = 0; _i103 < _map102.size; ++_i103)
                    {
                      String _key104;
                      List<CounterMutation> _val105;
                      _key104 = iprot.readString();
                      {
                        TList _list106 = iprot.readListBegin();
                        _val105 = new ArrayList<CounterMutation>(_list106.size);
                        for (int _i107 = 0; _i107 < _list106.size; ++_i107)
                        {
                          CounterMutation _elem108;
                          _elem108 = new CounterMutation();
                          _elem108.read(iprot);
                          _val105.add(_elem108);
                        }
                        iprot.readListEnd();
                      }
                      _val101.put(_key104, _val105);
                    }
                    iprot.readMapEnd();
                  }
                  this.update_map.put(_key100, _val101);
                }
                iprot.readMapEnd();
              }
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.I32) {
              this.consistency_level = ConsistencyLevel.findByValue(iprot.readI32());
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.update_map != null) {
        oprot.writeFieldBegin(UPDATE_MAP_FIELD_DESC);
        {
          oprot.writeMapBegin(new TMap(TType.STRING, TType.MAP, this.update_map.size()));
          for (Map.Entry<ByteBuffer, Map<String,List<CounterMutation>>> _iter109 : this.update_map.entrySet())
          {
            oprot.writeBinary(_iter109.getKey());
            {
              oprot.writeMapBegin(new TMap(TType.STRING, TType.LIST, _iter109.getValue().size()));
              for (Map.Entry<String, List<CounterMutation>> _iter110 : _iter109.getValue().entrySet())
              {
                oprot.writeString(_iter110.getKey());
                {
                  oprot.writeListBegin(new TList(TType.STRUCT, _iter110.getValue().size()));
                  for (CounterMutation _iter111 : _iter110.getValue())
                  {
                    _iter111.write(oprot);
                  }
                  oprot.writeListEnd();
                }
              }
              oprot.writeMapEnd();
            }
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      }
      if (this.consistency_level != null) {
        oprot.writeFieldBegin(CONSISTENCY_LEVEL_FIELD_DESC);
        oprot.writeI32(this.consistency_level.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("batch_add_args(");
      boolean first = true;
      sb.append("update_map:");
      if (this.update_map == null) {
        sb.append("null");
      } else {
        sb.append(this.update_map);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("consistency_level:");
      if (this.consistency_level == null) {
        sb.append("null");
      } else {
        sb.append(this.consistency_level);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (update_map == null) {
        throw new TProtocolException("Required field 'update_map' was not present! Struct: " + toString());
      }
      if (consistency_level == null) {
        throw new TProtocolException("Required field 'consistency_level' was not present! Struct: " + toString());
      }
    }
  }
  public static class batch_add_result implements TBase<batch_add_result, batch_add_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("batch_add_result");
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    private static final TField UE_FIELD_DESC = new TField("ue", TType.STRUCT, (short)2);
    private static final TField TE_FIELD_DESC = new TField("te", TType.STRUCT, (short)3);
    public InvalidRequestException ire;
    public UnavailableException ue;
    public TimedOutException te;
    public enum _Fields implements TFieldIdEnum {
      IRE((short)1, "ire"),
      UE((short)2, "ue"),
      TE((short)3, "te");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return IRE;
          case 2: 
            return UE;
          case 3: 
            return TE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.UE, new FieldMetaData("ue", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.TE, new FieldMetaData("te", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(batch_add_result.class, metaDataMap);
    }
    public batch_add_result() {
    }
    public batch_add_result(
      InvalidRequestException ire,
      UnavailableException ue,
      TimedOutException te)
    {
      this();
      this.ire = ire;
      this.ue = ue;
      this.te = te;
    }
    public batch_add_result(batch_add_result other) {
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
      if (other.isSetUe()) {
        this.ue = new UnavailableException(other.ue);
      }
      if (other.isSetTe()) {
        this.te = new TimedOutException(other.te);
      }
    }
    public batch_add_result deepCopy() {
      return new batch_add_result(this);
    }
    @Override
    public void clear() {
      this.ire = null;
      this.ue = null;
      this.te = null;
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public batch_add_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public UnavailableException getUe() {
      return this.ue;
    }
    public batch_add_result setUe(UnavailableException ue) {
      this.ue = ue;
      return this;
    }
    public void unsetUe() {
      this.ue = null;
    }
    public boolean isSetUe() {
      return this.ue != null;
    }
    public void setUeIsSet(boolean value) {
      if (!value) {
        this.ue = null;
      }
    }
    public TimedOutException getTe() {
      return this.te;
    }
    public batch_add_result setTe(TimedOutException te) {
      this.te = te;
      return this;
    }
    public void unsetTe() {
      this.te = null;
    }
    public boolean isSetTe() {
      return this.te != null;
    }
    public void setTeIsSet(boolean value) {
      if (!value) {
        this.te = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      case UE:
        if (value == null) {
          unsetUe();
        } else {
          setUe((UnavailableException)value);
        }
        break;
      case TE:
        if (value == null) {
          unsetTe();
        } else {
          setTe((TimedOutException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case IRE:
        return getIre();
      case UE:
        return getUe();
      case TE:
        return getTe();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case IRE:
        return isSetIre();
      case UE:
        return isSetUe();
      case TE:
        return isSetTe();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof batch_add_result)
        return this.equals((batch_add_result)that);
      return false;
    }
    public boolean equals(batch_add_result that) {
      if (that == null)
        return false;
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      boolean this_present_ue = true && this.isSetUe();
      boolean that_present_ue = true && that.isSetUe();
      if (this_present_ue || that_present_ue) {
        if (!(this_present_ue && that_present_ue))
          return false;
        if (!this.ue.equals(that.ue))
          return false;
      }
      boolean this_present_te = true && this.isSetTe();
      boolean that_present_te = true && that.isSetTe();
      if (this_present_te || that_present_te) {
        if (!(this_present_te && that_present_te))
          return false;
        if (!this.te.equals(that.te))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      boolean present_ue = true && (isSetUe());
      builder.append(present_ue);
      if (present_ue)
        builder.append(ue);
      boolean present_te = true && (isSetTe());
      builder.append(present_te);
      if (present_te)
        builder.append(te);
      return builder.toHashCode();
    }
    public int compareTo(batch_add_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      batch_add_result typedOther = (batch_add_result)other;
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetUe()).compareTo(typedOther.isSetUe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetUe()) {
        lastComparison = TBaseHelper.compareTo(this.ue, typedOther.ue);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetTe()).compareTo(typedOther.isSetTe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetTe()) {
        lastComparison = TBaseHelper.compareTo(this.te, typedOther.te);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.ue = new UnavailableException();
              this.ue.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.te = new TimedOutException();
              this.te.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetUe()) {
        oprot.writeFieldBegin(UE_FIELD_DESC);
        this.ue.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetTe()) {
        oprot.writeFieldBegin(TE_FIELD_DESC);
        this.te.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("batch_add_result(");
      boolean first = true;
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ue:");
      if (this.ue == null) {
        sb.append("null");
      } else {
        sb.append(this.ue);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("te:");
      if (this.te == null) {
        sb.append("null");
      } else {
        sb.append(this.te);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class get_counter_args implements TBase<get_counter_args, get_counter_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("get_counter_args");
    private static final TField KEY_FIELD_DESC = new TField("key", TType.STRING, (short)1);
    private static final TField PATH_FIELD_DESC = new TField("path", TType.STRUCT, (short)2);
    private static final TField CONSISTENCY_LEVEL_FIELD_DESC = new TField("consistency_level", TType.I32, (short)3);
    public ByteBuffer key;
    public ColumnPath path;
    public ConsistencyLevel consistency_level;
    public enum _Fields implements TFieldIdEnum {
      KEY((short)1, "key"),
      PATH((short)2, "path"),
      CONSISTENCY_LEVEL((short)3, "consistency_level");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return KEY;
          case 2: 
            return PATH;
          case 3: 
            return CONSISTENCY_LEVEL;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.KEY, new FieldMetaData("key", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.STRING)));
      tmpMap.put(_Fields.PATH, new FieldMetaData("path", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, ColumnPath.class)));
      tmpMap.put(_Fields.CONSISTENCY_LEVEL, new FieldMetaData("consistency_level", TFieldRequirementType.REQUIRED, 
          new EnumMetaData(TType.ENUM, ConsistencyLevel.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(get_counter_args.class, metaDataMap);
    }
    public get_counter_args() {
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public get_counter_args(
      ByteBuffer key,
      ColumnPath path,
      ConsistencyLevel consistency_level)
    {
      this();
      this.key = key;
      this.path = path;
      this.consistency_level = consistency_level;
    }
    public get_counter_args(get_counter_args other) {
      if (other.isSetKey()) {
        this.key = TBaseHelper.copyBinary(other.key);
;
      }
      if (other.isSetPath()) {
        this.path = new ColumnPath(other.path);
      }
      if (other.isSetConsistency_level()) {
        this.consistency_level = other.consistency_level;
      }
    }
    public get_counter_args deepCopy() {
      return new get_counter_args(this);
    }
    @Override
    public void clear() {
      this.key = null;
      this.path = null;
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public byte[] getKey() {
      setKey(TBaseHelper.rightSize(key));
      return key.array();
    }
    public ByteBuffer BufferForKey() {
      return key;
    }
    public get_counter_args setKey(byte[] key) {
      setKey(ByteBuffer.wrap(key));
      return this;
    }
    public get_counter_args setKey(ByteBuffer key) {
      this.key = key;
      return this;
    }
    public void unsetKey() {
      this.key = null;
    }
    public boolean isSetKey() {
      return this.key != null;
    }
    public void setKeyIsSet(boolean value) {
      if (!value) {
        this.key = null;
      }
    }
    public ColumnPath getPath() {
      return this.path;
    }
    public get_counter_args setPath(ColumnPath path) {
      this.path = path;
      return this;
    }
    public void unsetPath() {
      this.path = null;
    }
    public boolean isSetPath() {
      return this.path != null;
    }
    public void setPathIsSet(boolean value) {
      if (!value) {
        this.path = null;
      }
    }
    public ConsistencyLevel getConsistency_level() {
      return this.consistency_level;
    }
    public get_counter_args setConsistency_level(ConsistencyLevel consistency_level) {
      this.consistency_level = consistency_level;
      return this;
    }
    public void unsetConsistency_level() {
      this.consistency_level = null;
    }
    public boolean isSetConsistency_level() {
      return this.consistency_level != null;
    }
    public void setConsistency_levelIsSet(boolean value) {
      if (!value) {
        this.consistency_level = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case KEY:
        if (value == null) {
          unsetKey();
        } else {
          setKey((ByteBuffer)value);
        }
        break;
      case PATH:
        if (value == null) {
          unsetPath();
        } else {
          setPath((ColumnPath)value);
        }
        break;
      case CONSISTENCY_LEVEL:
        if (value == null) {
          unsetConsistency_level();
        } else {
          setConsistency_level((ConsistencyLevel)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case KEY:
        return getKey();
      case PATH:
        return getPath();
      case CONSISTENCY_LEVEL:
        return getConsistency_level();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case KEY:
        return isSetKey();
      case PATH:
        return isSetPath();
      case CONSISTENCY_LEVEL:
        return isSetConsistency_level();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof get_counter_args)
        return this.equals((get_counter_args)that);
      return false;
    }
    public boolean equals(get_counter_args that) {
      if (that == null)
        return false;
      boolean this_present_key = true && this.isSetKey();
      boolean that_present_key = true && that.isSetKey();
      if (this_present_key || that_present_key) {
        if (!(this_present_key && that_present_key))
          return false;
        if (!this.key.equals(that.key))
          return false;
      }
      boolean this_present_path = true && this.isSetPath();
      boolean that_present_path = true && that.isSetPath();
      if (this_present_path || that_present_path) {
        if (!(this_present_path && that_present_path))
          return false;
        if (!this.path.equals(that.path))
          return false;
      }
      boolean this_present_consistency_level = true && this.isSetConsistency_level();
      boolean that_present_consistency_level = true && that.isSetConsistency_level();
      if (this_present_consistency_level || that_present_consistency_level) {
        if (!(this_present_consistency_level && that_present_consistency_level))
          return false;
        if (!this.consistency_level.equals(that.consistency_level))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_key = true && (isSetKey());
      builder.append(present_key);
      if (present_key)
        builder.append(key);
      boolean present_path = true && (isSetPath());
      builder.append(present_path);
      if (present_path)
        builder.append(path);
      boolean present_consistency_level = true && (isSetConsistency_level());
      builder.append(present_consistency_level);
      if (present_consistency_level)
        builder.append(consistency_level.getValue());
      return builder.toHashCode();
    }
    public int compareTo(get_counter_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      get_counter_args typedOther = (get_counter_args)other;
      lastComparison = Boolean.valueOf(isSetKey()).compareTo(typedOther.isSetKey());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetKey()) {
        lastComparison = TBaseHelper.compareTo(this.key, typedOther.key);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetPath()).compareTo(typedOther.isSetPath());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetPath()) {
        lastComparison = TBaseHelper.compareTo(this.path, typedOther.path);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetConsistency_level()).compareTo(typedOther.isSetConsistency_level());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetConsistency_level()) {
        lastComparison = TBaseHelper.compareTo(this.consistency_level, typedOther.consistency_level);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRING) {
              this.key = iprot.readBinary();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.path = new ColumnPath();
              this.path.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.I32) {
              this.consistency_level = ConsistencyLevel.findByValue(iprot.readI32());
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.key != null) {
        oprot.writeFieldBegin(KEY_FIELD_DESC);
        oprot.writeBinary(this.key);
        oprot.writeFieldEnd();
      }
      if (this.path != null) {
        oprot.writeFieldBegin(PATH_FIELD_DESC);
        this.path.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.consistency_level != null) {
        oprot.writeFieldBegin(CONSISTENCY_LEVEL_FIELD_DESC);
        oprot.writeI32(this.consistency_level.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("get_counter_args(");
      boolean first = true;
      sb.append("key:");
      if (this.key == null) {
        sb.append("null");
      } else {
        TBaseHelper.toString(this.key, sb);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("path:");
      if (this.path == null) {
        sb.append("null");
      } else {
        sb.append(this.path);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("consistency_level:");
      if (this.consistency_level == null) {
        sb.append("null");
      } else {
        sb.append(this.consistency_level);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (key == null) {
        throw new TProtocolException("Required field 'key' was not present! Struct: " + toString());
      }
      if (path == null) {
        throw new TProtocolException("Required field 'path' was not present! Struct: " + toString());
      }
      if (consistency_level == null) {
        throw new TProtocolException("Required field 'consistency_level' was not present! Struct: " + toString());
      }
    }
  }
  public static class get_counter_result implements TBase<get_counter_result, get_counter_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("get_counter_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.STRUCT, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    private static final TField NFE_FIELD_DESC = new TField("nfe", TType.STRUCT, (short)2);
    private static final TField UE_FIELD_DESC = new TField("ue", TType.STRUCT, (short)3);
    private static final TField TE_FIELD_DESC = new TField("te", TType.STRUCT, (short)4);
    public Counter success;
    public InvalidRequestException ire;
    public NotFoundException nfe;
    public UnavailableException ue;
    public TimedOutException te;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire"),
      NFE((short)2, "nfe"),
      UE((short)3, "ue"),
      TE((short)4, "te");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          case 2: 
            return NFE;
          case 3: 
            return UE;
          case 4: 
            return TE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new StructMetaData(TType.STRUCT, Counter.class)));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.NFE, new FieldMetaData("nfe", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.UE, new FieldMetaData("ue", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.TE, new FieldMetaData("te", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(get_counter_result.class, metaDataMap);
    }
    public get_counter_result() {
    }
    public get_counter_result(
      Counter success,
      InvalidRequestException ire,
      NotFoundException nfe,
      UnavailableException ue,
      TimedOutException te)
    {
      this();
      this.success = success;
      this.ire = ire;
      this.nfe = nfe;
      this.ue = ue;
      this.te = te;
    }
    public get_counter_result(get_counter_result other) {
      if (other.isSetSuccess()) {
        this.success = new Counter(other.success);
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
      if (other.isSetNfe()) {
        this.nfe = new NotFoundException(other.nfe);
      }
      if (other.isSetUe()) {
        this.ue = new UnavailableException(other.ue);
      }
      if (other.isSetTe()) {
        this.te = new TimedOutException(other.te);
      }
    }
    public get_counter_result deepCopy() {
      return new get_counter_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.ire = null;
      this.nfe = null;
      this.ue = null;
      this.te = null;
    }
    public Counter getSuccess() {
      return this.success;
    }
    public get_counter_result setSuccess(Counter success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public get_counter_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public NotFoundException getNfe() {
      return this.nfe;
    }
    public get_counter_result setNfe(NotFoundException nfe) {
      this.nfe = nfe;
      return this;
    }
    public void unsetNfe() {
      this.nfe = null;
    }
    public boolean isSetNfe() {
      return this.nfe != null;
    }
    public void setNfeIsSet(boolean value) {
      if (!value) {
        this.nfe = null;
      }
    }
    public UnavailableException getUe() {
      return this.ue;
    }
    public get_counter_result setUe(UnavailableException ue) {
      this.ue = ue;
      return this;
    }
    public void unsetUe() {
      this.ue = null;
    }
    public boolean isSetUe() {
      return this.ue != null;
    }
    public void setUeIsSet(boolean value) {
      if (!value) {
        this.ue = null;
      }
    }
    public TimedOutException getTe() {
      return this.te;
    }
    public get_counter_result setTe(TimedOutException te) {
      this.te = te;
      return this;
    }
    public void unsetTe() {
      this.te = null;
    }
    public boolean isSetTe() {
      return this.te != null;
    }
    public void setTeIsSet(boolean value) {
      if (!value) {
        this.te = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((Counter)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      case NFE:
        if (value == null) {
          unsetNfe();
        } else {
          setNfe((NotFoundException)value);
        }
        break;
      case UE:
        if (value == null) {
          unsetUe();
        } else {
          setUe((UnavailableException)value);
        }
        break;
      case TE:
        if (value == null) {
          unsetTe();
        } else {
          setTe((TimedOutException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case IRE:
        return getIre();
      case NFE:
        return getNfe();
      case UE:
        return getUe();
      case TE:
        return getTe();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      case NFE:
        return isSetNfe();
      case UE:
        return isSetUe();
      case TE:
        return isSetTe();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof get_counter_result)
        return this.equals((get_counter_result)that);
      return false;
    }
    public boolean equals(get_counter_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      boolean this_present_nfe = true && this.isSetNfe();
      boolean that_present_nfe = true && that.isSetNfe();
      if (this_present_nfe || that_present_nfe) {
        if (!(this_present_nfe && that_present_nfe))
          return false;
        if (!this.nfe.equals(that.nfe))
          return false;
      }
      boolean this_present_ue = true && this.isSetUe();
      boolean that_present_ue = true && that.isSetUe();
      if (this_present_ue || that_present_ue) {
        if (!(this_present_ue && that_present_ue))
          return false;
        if (!this.ue.equals(that.ue))
          return false;
      }
      boolean this_present_te = true && this.isSetTe();
      boolean that_present_te = true && that.isSetTe();
      if (this_present_te || that_present_te) {
        if (!(this_present_te && that_present_te))
          return false;
        if (!this.te.equals(that.te))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      boolean present_nfe = true && (isSetNfe());
      builder.append(present_nfe);
      if (present_nfe)
        builder.append(nfe);
      boolean present_ue = true && (isSetUe());
      builder.append(present_ue);
      if (present_ue)
        builder.append(ue);
      boolean present_te = true && (isSetTe());
      builder.append(present_te);
      if (present_te)
        builder.append(te);
      return builder.toHashCode();
    }
    public int compareTo(get_counter_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      get_counter_result typedOther = (get_counter_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetNfe()).compareTo(typedOther.isSetNfe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetNfe()) {
        lastComparison = TBaseHelper.compareTo(this.nfe, typedOther.nfe);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetUe()).compareTo(typedOther.isSetUe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetUe()) {
        lastComparison = TBaseHelper.compareTo(this.ue, typedOther.ue);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetTe()).compareTo(typedOther.isSetTe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetTe()) {
        lastComparison = TBaseHelper.compareTo(this.te, typedOther.te);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.STRUCT) {
              this.success = new Counter();
              this.success.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.nfe = new NotFoundException();
              this.nfe.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.ue = new UnavailableException();
              this.ue.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 4: 
            if (field.type == TType.STRUCT) {
              this.te = new TimedOutException();
              this.te.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        this.success.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetNfe()) {
        oprot.writeFieldBegin(NFE_FIELD_DESC);
        this.nfe.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetUe()) {
        oprot.writeFieldBegin(UE_FIELD_DESC);
        this.ue.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetTe()) {
        oprot.writeFieldBegin(TE_FIELD_DESC);
        this.te.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("get_counter_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("nfe:");
      if (this.nfe == null) {
        sb.append("null");
      } else {
        sb.append(this.nfe);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ue:");
      if (this.ue == null) {
        sb.append("null");
      } else {
        sb.append(this.ue);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("te:");
      if (this.te == null) {
        sb.append("null");
      } else {
        sb.append(this.te);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class get_counter_slice_args implements TBase<get_counter_slice_args, get_counter_slice_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("get_counter_slice_args");
    private static final TField KEY_FIELD_DESC = new TField("key", TType.STRING, (short)1);
    private static final TField COLUMN_PARENT_FIELD_DESC = new TField("column_parent", TType.STRUCT, (short)2);
    private static final TField PREDICATE_FIELD_DESC = new TField("predicate", TType.STRUCT, (short)3);
    private static final TField CONSISTENCY_LEVEL_FIELD_DESC = new TField("consistency_level", TType.I32, (short)4);
    public ByteBuffer key;
    public ColumnParent column_parent;
    public SlicePredicate predicate;
    public ConsistencyLevel consistency_level;
    public enum _Fields implements TFieldIdEnum {
      KEY((short)1, "key"),
      COLUMN_PARENT((short)2, "column_parent"),
      PREDICATE((short)3, "predicate"),
      CONSISTENCY_LEVEL((short)4, "consistency_level");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return KEY;
          case 2: 
            return COLUMN_PARENT;
          case 3: 
            return PREDICATE;
          case 4: 
            return CONSISTENCY_LEVEL;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.KEY, new FieldMetaData("key", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.STRING)));
      tmpMap.put(_Fields.COLUMN_PARENT, new FieldMetaData("column_parent", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, ColumnParent.class)));
      tmpMap.put(_Fields.PREDICATE, new FieldMetaData("predicate", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, SlicePredicate.class)));
      tmpMap.put(_Fields.CONSISTENCY_LEVEL, new FieldMetaData("consistency_level", TFieldRequirementType.REQUIRED, 
          new EnumMetaData(TType.ENUM, ConsistencyLevel.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(get_counter_slice_args.class, metaDataMap);
    }
    public get_counter_slice_args() {
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public get_counter_slice_args(
      ByteBuffer key,
      ColumnParent column_parent,
      SlicePredicate predicate,
      ConsistencyLevel consistency_level)
    {
      this();
      this.key = key;
      this.column_parent = column_parent;
      this.predicate = predicate;
      this.consistency_level = consistency_level;
    }
    public get_counter_slice_args(get_counter_slice_args other) {
      if (other.isSetKey()) {
        this.key = TBaseHelper.copyBinary(other.key);
;
      }
      if (other.isSetColumn_parent()) {
        this.column_parent = new ColumnParent(other.column_parent);
      }
      if (other.isSetPredicate()) {
        this.predicate = new SlicePredicate(other.predicate);
      }
      if (other.isSetConsistency_level()) {
        this.consistency_level = other.consistency_level;
      }
    }
    public get_counter_slice_args deepCopy() {
      return new get_counter_slice_args(this);
    }
    @Override
    public void clear() {
      this.key = null;
      this.column_parent = null;
      this.predicate = null;
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public byte[] getKey() {
      setKey(TBaseHelper.rightSize(key));
      return key.array();
    }
    public ByteBuffer BufferForKey() {
      return key;
    }
    public get_counter_slice_args setKey(byte[] key) {
      setKey(ByteBuffer.wrap(key));
      return this;
    }
    public get_counter_slice_args setKey(ByteBuffer key) {
      this.key = key;
      return this;
    }
    public void unsetKey() {
      this.key = null;
    }
    public boolean isSetKey() {
      return this.key != null;
    }
    public void setKeyIsSet(boolean value) {
      if (!value) {
        this.key = null;
      }
    }
    public ColumnParent getColumn_parent() {
      return this.column_parent;
    }
    public get_counter_slice_args setColumn_parent(ColumnParent column_parent) {
      this.column_parent = column_parent;
      return this;
    }
    public void unsetColumn_parent() {
      this.column_parent = null;
    }
    public boolean isSetColumn_parent() {
      return this.column_parent != null;
    }
    public void setColumn_parentIsSet(boolean value) {
      if (!value) {
        this.column_parent = null;
      }
    }
    public SlicePredicate getPredicate() {
      return this.predicate;
    }
    public get_counter_slice_args setPredicate(SlicePredicate predicate) {
      this.predicate = predicate;
      return this;
    }
    public void unsetPredicate() {
      this.predicate = null;
    }
    public boolean isSetPredicate() {
      return this.predicate != null;
    }
    public void setPredicateIsSet(boolean value) {
      if (!value) {
        this.predicate = null;
      }
    }
    public ConsistencyLevel getConsistency_level() {
      return this.consistency_level;
    }
    public get_counter_slice_args setConsistency_level(ConsistencyLevel consistency_level) {
      this.consistency_level = consistency_level;
      return this;
    }
    public void unsetConsistency_level() {
      this.consistency_level = null;
    }
    public boolean isSetConsistency_level() {
      return this.consistency_level != null;
    }
    public void setConsistency_levelIsSet(boolean value) {
      if (!value) {
        this.consistency_level = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case KEY:
        if (value == null) {
          unsetKey();
        } else {
          setKey((ByteBuffer)value);
        }
        break;
      case COLUMN_PARENT:
        if (value == null) {
          unsetColumn_parent();
        } else {
          setColumn_parent((ColumnParent)value);
        }
        break;
      case PREDICATE:
        if (value == null) {
          unsetPredicate();
        } else {
          setPredicate((SlicePredicate)value);
        }
        break;
      case CONSISTENCY_LEVEL:
        if (value == null) {
          unsetConsistency_level();
        } else {
          setConsistency_level((ConsistencyLevel)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case KEY:
        return getKey();
      case COLUMN_PARENT:
        return getColumn_parent();
      case PREDICATE:
        return getPredicate();
      case CONSISTENCY_LEVEL:
        return getConsistency_level();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case KEY:
        return isSetKey();
      case COLUMN_PARENT:
        return isSetColumn_parent();
      case PREDICATE:
        return isSetPredicate();
      case CONSISTENCY_LEVEL:
        return isSetConsistency_level();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof get_counter_slice_args)
        return this.equals((get_counter_slice_args)that);
      return false;
    }
    public boolean equals(get_counter_slice_args that) {
      if (that == null)
        return false;
      boolean this_present_key = true && this.isSetKey();
      boolean that_present_key = true && that.isSetKey();
      if (this_present_key || that_present_key) {
        if (!(this_present_key && that_present_key))
          return false;
        if (!this.key.equals(that.key))
          return false;
      }
      boolean this_present_column_parent = true && this.isSetColumn_parent();
      boolean that_present_column_parent = true && that.isSetColumn_parent();
      if (this_present_column_parent || that_present_column_parent) {
        if (!(this_present_column_parent && that_present_column_parent))
          return false;
        if (!this.column_parent.equals(that.column_parent))
          return false;
      }
      boolean this_present_predicate = true && this.isSetPredicate();
      boolean that_present_predicate = true && that.isSetPredicate();
      if (this_present_predicate || that_present_predicate) {
        if (!(this_present_predicate && that_present_predicate))
          return false;
        if (!this.predicate.equals(that.predicate))
          return false;
      }
      boolean this_present_consistency_level = true && this.isSetConsistency_level();
      boolean that_present_consistency_level = true && that.isSetConsistency_level();
      if (this_present_consistency_level || that_present_consistency_level) {
        if (!(this_present_consistency_level && that_present_consistency_level))
          return false;
        if (!this.consistency_level.equals(that.consistency_level))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_key = true && (isSetKey());
      builder.append(present_key);
      if (present_key)
        builder.append(key);
      boolean present_column_parent = true && (isSetColumn_parent());
      builder.append(present_column_parent);
      if (present_column_parent)
        builder.append(column_parent);
      boolean present_predicate = true && (isSetPredicate());
      builder.append(present_predicate);
      if (present_predicate)
        builder.append(predicate);
      boolean present_consistency_level = true && (isSetConsistency_level());
      builder.append(present_consistency_level);
      if (present_consistency_level)
        builder.append(consistency_level.getValue());
      return builder.toHashCode();
    }
    public int compareTo(get_counter_slice_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      get_counter_slice_args typedOther = (get_counter_slice_args)other;
      lastComparison = Boolean.valueOf(isSetKey()).compareTo(typedOther.isSetKey());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetKey()) {
        lastComparison = TBaseHelper.compareTo(this.key, typedOther.key);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetColumn_parent()).compareTo(typedOther.isSetColumn_parent());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetColumn_parent()) {
        lastComparison = TBaseHelper.compareTo(this.column_parent, typedOther.column_parent);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetPredicate()).compareTo(typedOther.isSetPredicate());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetPredicate()) {
        lastComparison = TBaseHelper.compareTo(this.predicate, typedOther.predicate);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetConsistency_level()).compareTo(typedOther.isSetConsistency_level());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetConsistency_level()) {
        lastComparison = TBaseHelper.compareTo(this.consistency_level, typedOther.consistency_level);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRING) {
              this.key = iprot.readBinary();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.column_parent = new ColumnParent();
              this.column_parent.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.predicate = new SlicePredicate();
              this.predicate.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 4: 
            if (field.type == TType.I32) {
              this.consistency_level = ConsistencyLevel.findByValue(iprot.readI32());
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.key != null) {
        oprot.writeFieldBegin(KEY_FIELD_DESC);
        oprot.writeBinary(this.key);
        oprot.writeFieldEnd();
      }
      if (this.column_parent != null) {
        oprot.writeFieldBegin(COLUMN_PARENT_FIELD_DESC);
        this.column_parent.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.predicate != null) {
        oprot.writeFieldBegin(PREDICATE_FIELD_DESC);
        this.predicate.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.consistency_level != null) {
        oprot.writeFieldBegin(CONSISTENCY_LEVEL_FIELD_DESC);
        oprot.writeI32(this.consistency_level.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("get_counter_slice_args(");
      boolean first = true;
      sb.append("key:");
      if (this.key == null) {
        sb.append("null");
      } else {
        TBaseHelper.toString(this.key, sb);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("column_parent:");
      if (this.column_parent == null) {
        sb.append("null");
      } else {
        sb.append(this.column_parent);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("predicate:");
      if (this.predicate == null) {
        sb.append("null");
      } else {
        sb.append(this.predicate);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("consistency_level:");
      if (this.consistency_level == null) {
        sb.append("null");
      } else {
        sb.append(this.consistency_level);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (key == null) {
        throw new TProtocolException("Required field 'key' was not present! Struct: " + toString());
      }
      if (column_parent == null) {
        throw new TProtocolException("Required field 'column_parent' was not present! Struct: " + toString());
      }
      if (predicate == null) {
        throw new TProtocolException("Required field 'predicate' was not present! Struct: " + toString());
      }
      if (consistency_level == null) {
        throw new TProtocolException("Required field 'consistency_level' was not present! Struct: " + toString());
      }
    }
  }
  public static class get_counter_slice_result implements TBase<get_counter_slice_result, get_counter_slice_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("get_counter_slice_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.LIST, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    private static final TField UE_FIELD_DESC = new TField("ue", TType.STRUCT, (short)2);
    private static final TField TE_FIELD_DESC = new TField("te", TType.STRUCT, (short)3);
    public List<Counter> success;
    public InvalidRequestException ire;
    public UnavailableException ue;
    public TimedOutException te;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire"),
      UE((short)2, "ue"),
      TE((short)3, "te");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          case 2: 
            return UE;
          case 3: 
            return TE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new ListMetaData(TType.LIST, 
              new StructMetaData(TType.STRUCT, Counter.class))));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.UE, new FieldMetaData("ue", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.TE, new FieldMetaData("te", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(get_counter_slice_result.class, metaDataMap);
    }
    public get_counter_slice_result() {
    }
    public get_counter_slice_result(
      List<Counter> success,
      InvalidRequestException ire,
      UnavailableException ue,
      TimedOutException te)
    {
      this();
      this.success = success;
      this.ire = ire;
      this.ue = ue;
      this.te = te;
    }
    public get_counter_slice_result(get_counter_slice_result other) {
      if (other.isSetSuccess()) {
        List<Counter> __this__success = new ArrayList<Counter>();
        for (Counter other_element : other.success) {
          __this__success.add(new Counter(other_element));
        }
        this.success = __this__success;
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
      if (other.isSetUe()) {
        this.ue = new UnavailableException(other.ue);
      }
      if (other.isSetTe()) {
        this.te = new TimedOutException(other.te);
      }
    }
    public get_counter_slice_result deepCopy() {
      return new get_counter_slice_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.ire = null;
      this.ue = null;
      this.te = null;
    }
    public int getSuccessSize() {
      return (this.success == null) ? 0 : this.success.size();
    }
    public java.util.Iterator<Counter> getSuccessIterator() {
      return (this.success == null) ? null : this.success.iterator();
    }
    public void addToSuccess(Counter elem) {
      if (this.success == null) {
        this.success = new ArrayList<Counter>();
      }
      this.success.add(elem);
    }
    public List<Counter> getSuccess() {
      return this.success;
    }
    public get_counter_slice_result setSuccess(List<Counter> success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public get_counter_slice_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public UnavailableException getUe() {
      return this.ue;
    }
    public get_counter_slice_result setUe(UnavailableException ue) {
      this.ue = ue;
      return this;
    }
    public void unsetUe() {
      this.ue = null;
    }
    public boolean isSetUe() {
      return this.ue != null;
    }
    public void setUeIsSet(boolean value) {
      if (!value) {
        this.ue = null;
      }
    }
    public TimedOutException getTe() {
      return this.te;
    }
    public get_counter_slice_result setTe(TimedOutException te) {
      this.te = te;
      return this;
    }
    public void unsetTe() {
      this.te = null;
    }
    public boolean isSetTe() {
      return this.te != null;
    }
    public void setTeIsSet(boolean value) {
      if (!value) {
        this.te = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((List<Counter>)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      case UE:
        if (value == null) {
          unsetUe();
        } else {
          setUe((UnavailableException)value);
        }
        break;
      case TE:
        if (value == null) {
          unsetTe();
        } else {
          setTe((TimedOutException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case IRE:
        return getIre();
      case UE:
        return getUe();
      case TE:
        return getTe();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      case UE:
        return isSetUe();
      case TE:
        return isSetTe();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof get_counter_slice_result)
        return this.equals((get_counter_slice_result)that);
      return false;
    }
    public boolean equals(get_counter_slice_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      boolean this_present_ue = true && this.isSetUe();
      boolean that_present_ue = true && that.isSetUe();
      if (this_present_ue || that_present_ue) {
        if (!(this_present_ue && that_present_ue))
          return false;
        if (!this.ue.equals(that.ue))
          return false;
      }
      boolean this_present_te = true && this.isSetTe();
      boolean that_present_te = true && that.isSetTe();
      if (this_present_te || that_present_te) {
        if (!(this_present_te && that_present_te))
          return false;
        if (!this.te.equals(that.te))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      boolean present_ue = true && (isSetUe());
      builder.append(present_ue);
      if (present_ue)
        builder.append(ue);
      boolean present_te = true && (isSetTe());
      builder.append(present_te);
      if (present_te)
        builder.append(te);
      return builder.toHashCode();
    }
    public int compareTo(get_counter_slice_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      get_counter_slice_result typedOther = (get_counter_slice_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetUe()).compareTo(typedOther.isSetUe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetUe()) {
        lastComparison = TBaseHelper.compareTo(this.ue, typedOther.ue);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetTe()).compareTo(typedOther.isSetTe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetTe()) {
        lastComparison = TBaseHelper.compareTo(this.te, typedOther.te);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.LIST) {
              {
                TList _list112 = iprot.readListBegin();
                this.success = new ArrayList<Counter>(_list112.size);
                for (int _i113 = 0; _i113 < _list112.size; ++_i113)
                {
                  Counter _elem114;
                  _elem114 = new Counter();
                  _elem114.read(iprot);
                  this.success.add(_elem114);
                }
                iprot.readListEnd();
              }
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.ue = new UnavailableException();
              this.ue.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.te = new TimedOutException();
              this.te.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        {
          oprot.writeListBegin(new TList(TType.STRUCT, this.success.size()));
          for (Counter _iter115 : this.success)
          {
            _iter115.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetUe()) {
        oprot.writeFieldBegin(UE_FIELD_DESC);
        this.ue.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetTe()) {
        oprot.writeFieldBegin(TE_FIELD_DESC);
        this.te.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("get_counter_slice_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ue:");
      if (this.ue == null) {
        sb.append("null");
      } else {
        sb.append(this.ue);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("te:");
      if (this.te == null) {
        sb.append("null");
      } else {
        sb.append(this.te);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class multiget_counter_slice_args implements TBase<multiget_counter_slice_args, multiget_counter_slice_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("multiget_counter_slice_args");
    private static final TField KEYS_FIELD_DESC = new TField("keys", TType.LIST, (short)1);
    private static final TField COLUMN_PARENT_FIELD_DESC = new TField("column_parent", TType.STRUCT, (short)2);
    private static final TField PREDICATE_FIELD_DESC = new TField("predicate", TType.STRUCT, (short)3);
    private static final TField CONSISTENCY_LEVEL_FIELD_DESC = new TField("consistency_level", TType.I32, (short)4);
    public List<ByteBuffer> keys;
    public ColumnParent column_parent;
    public SlicePredicate predicate;
    public ConsistencyLevel consistency_level;
    public enum _Fields implements TFieldIdEnum {
      KEYS((short)1, "keys"),
      COLUMN_PARENT((short)2, "column_parent"),
      PREDICATE((short)3, "predicate"),
      CONSISTENCY_LEVEL((short)4, "consistency_level");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return KEYS;
          case 2: 
            return COLUMN_PARENT;
          case 3: 
            return PREDICATE;
          case 4: 
            return CONSISTENCY_LEVEL;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.KEYS, new FieldMetaData("keys", TFieldRequirementType.REQUIRED, 
          new ListMetaData(TType.LIST, 
              new FieldValueMetaData(TType.STRING))));
      tmpMap.put(_Fields.COLUMN_PARENT, new FieldMetaData("column_parent", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, ColumnParent.class)));
      tmpMap.put(_Fields.PREDICATE, new FieldMetaData("predicate", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, SlicePredicate.class)));
      tmpMap.put(_Fields.CONSISTENCY_LEVEL, new FieldMetaData("consistency_level", TFieldRequirementType.REQUIRED, 
          new EnumMetaData(TType.ENUM, ConsistencyLevel.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(multiget_counter_slice_args.class, metaDataMap);
    }
    public multiget_counter_slice_args() {
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public multiget_counter_slice_args(
      List<ByteBuffer> keys,
      ColumnParent column_parent,
      SlicePredicate predicate,
      ConsistencyLevel consistency_level)
    {
      this();
      this.keys = keys;
      this.column_parent = column_parent;
      this.predicate = predicate;
      this.consistency_level = consistency_level;
    }
    public multiget_counter_slice_args(multiget_counter_slice_args other) {
      if (other.isSetKeys()) {
        List<ByteBuffer> __this__keys = new ArrayList<ByteBuffer>();
        for (ByteBuffer other_element : other.keys) {
          ByteBuffer temp_binary_element = TBaseHelper.copyBinary(other_element);
;
          __this__keys.add(temp_binary_element);
        }
        this.keys = __this__keys;
      }
      if (other.isSetColumn_parent()) {
        this.column_parent = new ColumnParent(other.column_parent);
      }
      if (other.isSetPredicate()) {
        this.predicate = new SlicePredicate(other.predicate);
      }
      if (other.isSetConsistency_level()) {
        this.consistency_level = other.consistency_level;
      }
    }
    public multiget_counter_slice_args deepCopy() {
      return new multiget_counter_slice_args(this);
    }
    @Override
    public void clear() {
      this.keys = null;
      this.column_parent = null;
      this.predicate = null;
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public int getKeysSize() {
      return (this.keys == null) ? 0 : this.keys.size();
    }
    public java.util.Iterator<ByteBuffer> getKeysIterator() {
      return (this.keys == null) ? null : this.keys.iterator();
    }
    public void addToKeys(ByteBuffer elem) {
      if (this.keys == null) {
        this.keys = new ArrayList<ByteBuffer>();
      }
      this.keys.add(elem);
    }
    public List<ByteBuffer> getKeys() {
      return this.keys;
    }
    public multiget_counter_slice_args setKeys(List<ByteBuffer> keys) {
      this.keys = keys;
      return this;
    }
    public void unsetKeys() {
      this.keys = null;
    }
    public boolean isSetKeys() {
      return this.keys != null;
    }
    public void setKeysIsSet(boolean value) {
      if (!value) {
        this.keys = null;
      }
    }
    public ColumnParent getColumn_parent() {
      return this.column_parent;
    }
    public multiget_counter_slice_args setColumn_parent(ColumnParent column_parent) {
      this.column_parent = column_parent;
      return this;
    }
    public void unsetColumn_parent() {
      this.column_parent = null;
    }
    public boolean isSetColumn_parent() {
      return this.column_parent != null;
    }
    public void setColumn_parentIsSet(boolean value) {
      if (!value) {
        this.column_parent = null;
      }
    }
    public SlicePredicate getPredicate() {
      return this.predicate;
    }
    public multiget_counter_slice_args setPredicate(SlicePredicate predicate) {
      this.predicate = predicate;
      return this;
    }
    public void unsetPredicate() {
      this.predicate = null;
    }
    public boolean isSetPredicate() {
      return this.predicate != null;
    }
    public void setPredicateIsSet(boolean value) {
      if (!value) {
        this.predicate = null;
      }
    }
    public ConsistencyLevel getConsistency_level() {
      return this.consistency_level;
    }
    public multiget_counter_slice_args setConsistency_level(ConsistencyLevel consistency_level) {
      this.consistency_level = consistency_level;
      return this;
    }
    public void unsetConsistency_level() {
      this.consistency_level = null;
    }
    public boolean isSetConsistency_level() {
      return this.consistency_level != null;
    }
    public void setConsistency_levelIsSet(boolean value) {
      if (!value) {
        this.consistency_level = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case KEYS:
        if (value == null) {
          unsetKeys();
        } else {
          setKeys((List<ByteBuffer>)value);
        }
        break;
      case COLUMN_PARENT:
        if (value == null) {
          unsetColumn_parent();
        } else {
          setColumn_parent((ColumnParent)value);
        }
        break;
      case PREDICATE:
        if (value == null) {
          unsetPredicate();
        } else {
          setPredicate((SlicePredicate)value);
        }
        break;
      case CONSISTENCY_LEVEL:
        if (value == null) {
          unsetConsistency_level();
        } else {
          setConsistency_level((ConsistencyLevel)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case KEYS:
        return getKeys();
      case COLUMN_PARENT:
        return getColumn_parent();
      case PREDICATE:
        return getPredicate();
      case CONSISTENCY_LEVEL:
        return getConsistency_level();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case KEYS:
        return isSetKeys();
      case COLUMN_PARENT:
        return isSetColumn_parent();
      case PREDICATE:
        return isSetPredicate();
      case CONSISTENCY_LEVEL:
        return isSetConsistency_level();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof multiget_counter_slice_args)
        return this.equals((multiget_counter_slice_args)that);
      return false;
    }
    public boolean equals(multiget_counter_slice_args that) {
      if (that == null)
        return false;
      boolean this_present_keys = true && this.isSetKeys();
      boolean that_present_keys = true && that.isSetKeys();
      if (this_present_keys || that_present_keys) {
        if (!(this_present_keys && that_present_keys))
          return false;
        if (!this.keys.equals(that.keys))
          return false;
      }
      boolean this_present_column_parent = true && this.isSetColumn_parent();
      boolean that_present_column_parent = true && that.isSetColumn_parent();
      if (this_present_column_parent || that_present_column_parent) {
        if (!(this_present_column_parent && that_present_column_parent))
          return false;
        if (!this.column_parent.equals(that.column_parent))
          return false;
      }
      boolean this_present_predicate = true && this.isSetPredicate();
      boolean that_present_predicate = true && that.isSetPredicate();
      if (this_present_predicate || that_present_predicate) {
        if (!(this_present_predicate && that_present_predicate))
          return false;
        if (!this.predicate.equals(that.predicate))
          return false;
      }
      boolean this_present_consistency_level = true && this.isSetConsistency_level();
      boolean that_present_consistency_level = true && that.isSetConsistency_level();
      if (this_present_consistency_level || that_present_consistency_level) {
        if (!(this_present_consistency_level && that_present_consistency_level))
          return false;
        if (!this.consistency_level.equals(that.consistency_level))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_keys = true && (isSetKeys());
      builder.append(present_keys);
      if (present_keys)
        builder.append(keys);
      boolean present_column_parent = true && (isSetColumn_parent());
      builder.append(present_column_parent);
      if (present_column_parent)
        builder.append(column_parent);
      boolean present_predicate = true && (isSetPredicate());
      builder.append(present_predicate);
      if (present_predicate)
        builder.append(predicate);
      boolean present_consistency_level = true && (isSetConsistency_level());
      builder.append(present_consistency_level);
      if (present_consistency_level)
        builder.append(consistency_level.getValue());
      return builder.toHashCode();
    }
    public int compareTo(multiget_counter_slice_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      multiget_counter_slice_args typedOther = (multiget_counter_slice_args)other;
      lastComparison = Boolean.valueOf(isSetKeys()).compareTo(typedOther.isSetKeys());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetKeys()) {
        lastComparison = TBaseHelper.compareTo(this.keys, typedOther.keys);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetColumn_parent()).compareTo(typedOther.isSetColumn_parent());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetColumn_parent()) {
        lastComparison = TBaseHelper.compareTo(this.column_parent, typedOther.column_parent);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetPredicate()).compareTo(typedOther.isSetPredicate());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetPredicate()) {
        lastComparison = TBaseHelper.compareTo(this.predicate, typedOther.predicate);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetConsistency_level()).compareTo(typedOther.isSetConsistency_level());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetConsistency_level()) {
        lastComparison = TBaseHelper.compareTo(this.consistency_level, typedOther.consistency_level);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.LIST) {
              {
                TList _list116 = iprot.readListBegin();
                this.keys = new ArrayList<ByteBuffer>(_list116.size);
                for (int _i117 = 0; _i117 < _list116.size; ++_i117)
                {
                  ByteBuffer _elem118;
                  _elem118 = iprot.readBinary();
                  this.keys.add(_elem118);
                }
                iprot.readListEnd();
              }
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.column_parent = new ColumnParent();
              this.column_parent.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.predicate = new SlicePredicate();
              this.predicate.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 4: 
            if (field.type == TType.I32) {
              this.consistency_level = ConsistencyLevel.findByValue(iprot.readI32());
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.keys != null) {
        oprot.writeFieldBegin(KEYS_FIELD_DESC);
        {
          oprot.writeListBegin(new TList(TType.STRING, this.keys.size()));
          for (ByteBuffer _iter119 : this.keys)
          {
            oprot.writeBinary(_iter119);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (this.column_parent != null) {
        oprot.writeFieldBegin(COLUMN_PARENT_FIELD_DESC);
        this.column_parent.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.predicate != null) {
        oprot.writeFieldBegin(PREDICATE_FIELD_DESC);
        this.predicate.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.consistency_level != null) {
        oprot.writeFieldBegin(CONSISTENCY_LEVEL_FIELD_DESC);
        oprot.writeI32(this.consistency_level.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("multiget_counter_slice_args(");
      boolean first = true;
      sb.append("keys:");
      if (this.keys == null) {
        sb.append("null");
      } else {
        sb.append(this.keys);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("column_parent:");
      if (this.column_parent == null) {
        sb.append("null");
      } else {
        sb.append(this.column_parent);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("predicate:");
      if (this.predicate == null) {
        sb.append("null");
      } else {
        sb.append(this.predicate);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("consistency_level:");
      if (this.consistency_level == null) {
        sb.append("null");
      } else {
        sb.append(this.consistency_level);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (keys == null) {
        throw new TProtocolException("Required field 'keys' was not present! Struct: " + toString());
      }
      if (column_parent == null) {
        throw new TProtocolException("Required field 'column_parent' was not present! Struct: " + toString());
      }
      if (predicate == null) {
        throw new TProtocolException("Required field 'predicate' was not present! Struct: " + toString());
      }
      if (consistency_level == null) {
        throw new TProtocolException("Required field 'consistency_level' was not present! Struct: " + toString());
      }
    }
  }
  public static class multiget_counter_slice_result implements TBase<multiget_counter_slice_result, multiget_counter_slice_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("multiget_counter_slice_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.MAP, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    private static final TField UE_FIELD_DESC = new TField("ue", TType.STRUCT, (short)2);
    private static final TField TE_FIELD_DESC = new TField("te", TType.STRUCT, (short)3);
    public Map<ByteBuffer,List<Counter>> success;
    public InvalidRequestException ire;
    public UnavailableException ue;
    public TimedOutException te;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire"),
      UE((short)2, "ue"),
      TE((short)3, "te");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          case 2: 
            return UE;
          case 3: 
            return TE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new MapMetaData(TType.MAP, 
              new FieldValueMetaData(TType.STRING), 
              new ListMetaData(TType.LIST, 
                  new StructMetaData(TType.STRUCT, Counter.class)))));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.UE, new FieldMetaData("ue", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.TE, new FieldMetaData("te", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(multiget_counter_slice_result.class, metaDataMap);
    }
    public multiget_counter_slice_result() {
    }
    public multiget_counter_slice_result(
      Map<ByteBuffer,List<Counter>> success,
      InvalidRequestException ire,
      UnavailableException ue,
      TimedOutException te)
    {
      this();
      this.success = success;
      this.ire = ire;
      this.ue = ue;
      this.te = te;
    }
    public multiget_counter_slice_result(multiget_counter_slice_result other) {
      if (other.isSetSuccess()) {
        Map<ByteBuffer,List<Counter>> __this__success = new HashMap<ByteBuffer,List<Counter>>();
        for (Map.Entry<ByteBuffer, List<Counter>> other_element : other.success.entrySet()) {
          ByteBuffer other_element_key = other_element.getKey();
          List<Counter> other_element_value = other_element.getValue();
          ByteBuffer __this__success_copy_key = TBaseHelper.copyBinary(other_element_key);
;
          List<Counter> __this__success_copy_value = new ArrayList<Counter>();
          for (Counter other_element_value_element : other_element_value) {
            __this__success_copy_value.add(new Counter(other_element_value_element));
          }
          __this__success.put(__this__success_copy_key, __this__success_copy_value);
        }
        this.success = __this__success;
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
      if (other.isSetUe()) {
        this.ue = new UnavailableException(other.ue);
      }
      if (other.isSetTe()) {
        this.te = new TimedOutException(other.te);
      }
    }
    public multiget_counter_slice_result deepCopy() {
      return new multiget_counter_slice_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.ire = null;
      this.ue = null;
      this.te = null;
    }
    public int getSuccessSize() {
      return (this.success == null) ? 0 : this.success.size();
    }
    public void putToSuccess(ByteBuffer key, List<Counter> val) {
      if (this.success == null) {
        this.success = new HashMap<ByteBuffer,List<Counter>>();
      }
      this.success.put(key, val);
    }
    public Map<ByteBuffer,List<Counter>> getSuccess() {
      return this.success;
    }
    public multiget_counter_slice_result setSuccess(Map<ByteBuffer,List<Counter>> success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public multiget_counter_slice_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public UnavailableException getUe() {
      return this.ue;
    }
    public multiget_counter_slice_result setUe(UnavailableException ue) {
      this.ue = ue;
      return this;
    }
    public void unsetUe() {
      this.ue = null;
    }
    public boolean isSetUe() {
      return this.ue != null;
    }
    public void setUeIsSet(boolean value) {
      if (!value) {
        this.ue = null;
      }
    }
    public TimedOutException getTe() {
      return this.te;
    }
    public multiget_counter_slice_result setTe(TimedOutException te) {
      this.te = te;
      return this;
    }
    public void unsetTe() {
      this.te = null;
    }
    public boolean isSetTe() {
      return this.te != null;
    }
    public void setTeIsSet(boolean value) {
      if (!value) {
        this.te = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((Map<ByteBuffer,List<Counter>>)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      case UE:
        if (value == null) {
          unsetUe();
        } else {
          setUe((UnavailableException)value);
        }
        break;
      case TE:
        if (value == null) {
          unsetTe();
        } else {
          setTe((TimedOutException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case IRE:
        return getIre();
      case UE:
        return getUe();
      case TE:
        return getTe();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      case UE:
        return isSetUe();
      case TE:
        return isSetTe();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof multiget_counter_slice_result)
        return this.equals((multiget_counter_slice_result)that);
      return false;
    }
    public boolean equals(multiget_counter_slice_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      boolean this_present_ue = true && this.isSetUe();
      boolean that_present_ue = true && that.isSetUe();
      if (this_present_ue || that_present_ue) {
        if (!(this_present_ue && that_present_ue))
          return false;
        if (!this.ue.equals(that.ue))
          return false;
      }
      boolean this_present_te = true && this.isSetTe();
      boolean that_present_te = true && that.isSetTe();
      if (this_present_te || that_present_te) {
        if (!(this_present_te && that_present_te))
          return false;
        if (!this.te.equals(that.te))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      boolean present_ue = true && (isSetUe());
      builder.append(present_ue);
      if (present_ue)
        builder.append(ue);
      boolean present_te = true && (isSetTe());
      builder.append(present_te);
      if (present_te)
        builder.append(te);
      return builder.toHashCode();
    }
    public int compareTo(multiget_counter_slice_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      multiget_counter_slice_result typedOther = (multiget_counter_slice_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetUe()).compareTo(typedOther.isSetUe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetUe()) {
        lastComparison = TBaseHelper.compareTo(this.ue, typedOther.ue);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetTe()).compareTo(typedOther.isSetTe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetTe()) {
        lastComparison = TBaseHelper.compareTo(this.te, typedOther.te);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.MAP) {
              {
                TMap _map120 = iprot.readMapBegin();
                this.success = new HashMap<ByteBuffer,List<Counter>>(2*_map120.size);
                for (int _i121 = 0; _i121 < _map120.size; ++_i121)
                {
                  ByteBuffer _key122;
                  List<Counter> _val123;
                  _key122 = iprot.readBinary();
                  {
                    TList _list124 = iprot.readListBegin();
                    _val123 = new ArrayList<Counter>(_list124.size);
                    for (int _i125 = 0; _i125 < _list124.size; ++_i125)
                    {
                      Counter _elem126;
                      _elem126 = new Counter();
                      _elem126.read(iprot);
                      _val123.add(_elem126);
                    }
                    iprot.readListEnd();
                  }
                  this.success.put(_key122, _val123);
                }
                iprot.readMapEnd();
              }
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.ue = new UnavailableException();
              this.ue.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.te = new TimedOutException();
              this.te.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        {
          oprot.writeMapBegin(new TMap(TType.STRING, TType.LIST, this.success.size()));
          for (Map.Entry<ByteBuffer, List<Counter>> _iter127 : this.success.entrySet())
          {
            oprot.writeBinary(_iter127.getKey());
            {
              oprot.writeListBegin(new TList(TType.STRUCT, _iter127.getValue().size()));
              for (Counter _iter128 : _iter127.getValue())
              {
                _iter128.write(oprot);
              }
              oprot.writeListEnd();
            }
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetUe()) {
        oprot.writeFieldBegin(UE_FIELD_DESC);
        this.ue.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetTe()) {
        oprot.writeFieldBegin(TE_FIELD_DESC);
        this.te.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("multiget_counter_slice_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ue:");
      if (this.ue == null) {
        sb.append("null");
      } else {
        sb.append(this.ue);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("te:");
      if (this.te == null) {
        sb.append("null");
      } else {
        sb.append(this.te);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class remove_counter_args implements TBase<remove_counter_args, remove_counter_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("remove_counter_args");
    private static final TField KEY_FIELD_DESC = new TField("key", TType.STRING, (short)1);
    private static final TField PATH_FIELD_DESC = new TField("path", TType.STRUCT, (short)2);
    private static final TField CONSISTENCY_LEVEL_FIELD_DESC = new TField("consistency_level", TType.I32, (short)3);
    public ByteBuffer key;
    public ColumnPath path;
    public ConsistencyLevel consistency_level;
    public enum _Fields implements TFieldIdEnum {
      KEY((short)1, "key"),
      PATH((short)2, "path"),
      CONSISTENCY_LEVEL((short)3, "consistency_level");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return KEY;
          case 2: 
            return PATH;
          case 3: 
            return CONSISTENCY_LEVEL;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.KEY, new FieldMetaData("key", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.STRING)));
      tmpMap.put(_Fields.PATH, new FieldMetaData("path", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, ColumnPath.class)));
      tmpMap.put(_Fields.CONSISTENCY_LEVEL, new FieldMetaData("consistency_level", TFieldRequirementType.REQUIRED, 
          new EnumMetaData(TType.ENUM, ConsistencyLevel.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(remove_counter_args.class, metaDataMap);
    }
    public remove_counter_args() {
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public remove_counter_args(
      ByteBuffer key,
      ColumnPath path,
      ConsistencyLevel consistency_level)
    {
      this();
      this.key = key;
      this.path = path;
      this.consistency_level = consistency_level;
    }
    public remove_counter_args(remove_counter_args other) {
      if (other.isSetKey()) {
        this.key = TBaseHelper.copyBinary(other.key);
;
      }
      if (other.isSetPath()) {
        this.path = new ColumnPath(other.path);
      }
      if (other.isSetConsistency_level()) {
        this.consistency_level = other.consistency_level;
      }
    }
    public remove_counter_args deepCopy() {
      return new remove_counter_args(this);
    }
    @Override
    public void clear() {
      this.key = null;
      this.path = null;
      this.consistency_level = ConsistencyLevel.ONE;
    }
    public byte[] getKey() {
      setKey(TBaseHelper.rightSize(key));
      return key.array();
    }
    public ByteBuffer BufferForKey() {
      return key;
    }
    public remove_counter_args setKey(byte[] key) {
      setKey(ByteBuffer.wrap(key));
      return this;
    }
    public remove_counter_args setKey(ByteBuffer key) {
      this.key = key;
      return this;
    }
    public void unsetKey() {
      this.key = null;
    }
    public boolean isSetKey() {
      return this.key != null;
    }
    public void setKeyIsSet(boolean value) {
      if (!value) {
        this.key = null;
      }
    }
    public ColumnPath getPath() {
      return this.path;
    }
    public remove_counter_args setPath(ColumnPath path) {
      this.path = path;
      return this;
    }
    public void unsetPath() {
      this.path = null;
    }
    public boolean isSetPath() {
      return this.path != null;
    }
    public void setPathIsSet(boolean value) {
      if (!value) {
        this.path = null;
      }
    }
    public ConsistencyLevel getConsistency_level() {
      return this.consistency_level;
    }
    public remove_counter_args setConsistency_level(ConsistencyLevel consistency_level) {
      this.consistency_level = consistency_level;
      return this;
    }
    public void unsetConsistency_level() {
      this.consistency_level = null;
    }
    public boolean isSetConsistency_level() {
      return this.consistency_level != null;
    }
    public void setConsistency_levelIsSet(boolean value) {
      if (!value) {
        this.consistency_level = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case KEY:
        if (value == null) {
          unsetKey();
        } else {
          setKey((ByteBuffer)value);
        }
        break;
      case PATH:
        if (value == null) {
          unsetPath();
        } else {
          setPath((ColumnPath)value);
        }
        break;
      case CONSISTENCY_LEVEL:
        if (value == null) {
          unsetConsistency_level();
        } else {
          setConsistency_level((ConsistencyLevel)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case KEY:
        return getKey();
      case PATH:
        return getPath();
      case CONSISTENCY_LEVEL:
        return getConsistency_level();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case KEY:
        return isSetKey();
      case PATH:
        return isSetPath();
      case CONSISTENCY_LEVEL:
        return isSetConsistency_level();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof remove_counter_args)
        return this.equals((remove_counter_args)that);
      return false;
    }
    public boolean equals(remove_counter_args that) {
      if (that == null)
        return false;
      boolean this_present_key = true && this.isSetKey();
      boolean that_present_key = true && that.isSetKey();
      if (this_present_key || that_present_key) {
        if (!(this_present_key && that_present_key))
          return false;
        if (!this.key.equals(that.key))
          return false;
      }
      boolean this_present_path = true && this.isSetPath();
      boolean that_present_path = true && that.isSetPath();
      if (this_present_path || that_present_path) {
        if (!(this_present_path && that_present_path))
          return false;
        if (!this.path.equals(that.path))
          return false;
      }
      boolean this_present_consistency_level = true && this.isSetConsistency_level();
      boolean that_present_consistency_level = true && that.isSetConsistency_level();
      if (this_present_consistency_level || that_present_consistency_level) {
        if (!(this_present_consistency_level && that_present_consistency_level))
          return false;
        if (!this.consistency_level.equals(that.consistency_level))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_key = true && (isSetKey());
      builder.append(present_key);
      if (present_key)
        builder.append(key);
      boolean present_path = true && (isSetPath());
      builder.append(present_path);
      if (present_path)
        builder.append(path);
      boolean present_consistency_level = true && (isSetConsistency_level());
      builder.append(present_consistency_level);
      if (present_consistency_level)
        builder.append(consistency_level.getValue());
      return builder.toHashCode();
    }
    public int compareTo(remove_counter_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      remove_counter_args typedOther = (remove_counter_args)other;
      lastComparison = Boolean.valueOf(isSetKey()).compareTo(typedOther.isSetKey());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetKey()) {
        lastComparison = TBaseHelper.compareTo(this.key, typedOther.key);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetPath()).compareTo(typedOther.isSetPath());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetPath()) {
        lastComparison = TBaseHelper.compareTo(this.path, typedOther.path);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetConsistency_level()).compareTo(typedOther.isSetConsistency_level());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetConsistency_level()) {
        lastComparison = TBaseHelper.compareTo(this.consistency_level, typedOther.consistency_level);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRING) {
              this.key = iprot.readBinary();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.path = new ColumnPath();
              this.path.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.I32) {
              this.consistency_level = ConsistencyLevel.findByValue(iprot.readI32());
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.key != null) {
        oprot.writeFieldBegin(KEY_FIELD_DESC);
        oprot.writeBinary(this.key);
        oprot.writeFieldEnd();
      }
      if (this.path != null) {
        oprot.writeFieldBegin(PATH_FIELD_DESC);
        this.path.write(oprot);
        oprot.writeFieldEnd();
      }
      if (this.consistency_level != null) {
        oprot.writeFieldBegin(CONSISTENCY_LEVEL_FIELD_DESC);
        oprot.writeI32(this.consistency_level.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("remove_counter_args(");
      boolean first = true;
      sb.append("key:");
      if (this.key == null) {
        sb.append("null");
      } else {
        TBaseHelper.toString(this.key, sb);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("path:");
      if (this.path == null) {
        sb.append("null");
      } else {
        sb.append(this.path);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("consistency_level:");
      if (this.consistency_level == null) {
        sb.append("null");
      } else {
        sb.append(this.consistency_level);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (key == null) {
        throw new TProtocolException("Required field 'key' was not present! Struct: " + toString());
      }
      if (path == null) {
        throw new TProtocolException("Required field 'path' was not present! Struct: " + toString());
      }
      if (consistency_level == null) {
        throw new TProtocolException("Required field 'consistency_level' was not present! Struct: " + toString());
      }
    }
  }
  public static class remove_counter_result implements TBase<remove_counter_result, remove_counter_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("remove_counter_result");
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    private static final TField UE_FIELD_DESC = new TField("ue", TType.STRUCT, (short)2);
    private static final TField TE_FIELD_DESC = new TField("te", TType.STRUCT, (short)3);
    public InvalidRequestException ire;
    public UnavailableException ue;
    public TimedOutException te;
    public enum _Fields implements TFieldIdEnum {
      IRE((short)1, "ire"),
      UE((short)2, "ue"),
      TE((short)3, "te");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return IRE;
          case 2: 
            return UE;
          case 3: 
            return TE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.UE, new FieldMetaData("ue", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.TE, new FieldMetaData("te", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(remove_counter_result.class, metaDataMap);
    }
    public remove_counter_result() {
    }
    public remove_counter_result(
      InvalidRequestException ire,
      UnavailableException ue,
      TimedOutException te)
    {
      this();
      this.ire = ire;
      this.ue = ue;
      this.te = te;
    }
    public remove_counter_result(remove_counter_result other) {
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
      if (other.isSetUe()) {
        this.ue = new UnavailableException(other.ue);
      }
      if (other.isSetTe()) {
        this.te = new TimedOutException(other.te);
      }
    }
    public remove_counter_result deepCopy() {
      return new remove_counter_result(this);
    }
    @Override
    public void clear() {
      this.ire = null;
      this.ue = null;
      this.te = null;
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public remove_counter_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public UnavailableException getUe() {
      return this.ue;
    }
    public remove_counter_result setUe(UnavailableException ue) {
      this.ue = ue;
      return this;
    }
    public void unsetUe() {
      this.ue = null;
    }
    public boolean isSetUe() {
      return this.ue != null;
    }
    public void setUeIsSet(boolean value) {
      if (!value) {
        this.ue = null;
      }
    }
    public TimedOutException getTe() {
      return this.te;
    }
    public remove_counter_result setTe(TimedOutException te) {
      this.te = te;
      return this;
    }
    public void unsetTe() {
      this.te = null;
    }
    public boolean isSetTe() {
      return this.te != null;
    }
    public void setTeIsSet(boolean value) {
      if (!value) {
        this.te = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      case UE:
        if (value == null) {
          unsetUe();
        } else {
          setUe((UnavailableException)value);
        }
        break;
      case TE:
        if (value == null) {
          unsetTe();
        } else {
          setTe((TimedOutException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case IRE:
        return getIre();
      case UE:
        return getUe();
      case TE:
        return getTe();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case IRE:
        return isSetIre();
      case UE:
        return isSetUe();
      case TE:
        return isSetTe();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof remove_counter_result)
        return this.equals((remove_counter_result)that);
      return false;
    }
    public boolean equals(remove_counter_result that) {
      if (that == null)
        return false;
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      boolean this_present_ue = true && this.isSetUe();
      boolean that_present_ue = true && that.isSetUe();
      if (this_present_ue || that_present_ue) {
        if (!(this_present_ue && that_present_ue))
          return false;
        if (!this.ue.equals(that.ue))
          return false;
      }
      boolean this_present_te = true && this.isSetTe();
      boolean that_present_te = true && that.isSetTe();
      if (this_present_te || that_present_te) {
        if (!(this_present_te && that_present_te))
          return false;
        if (!this.te.equals(that.te))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      boolean present_ue = true && (isSetUe());
      builder.append(present_ue);
      if (present_ue)
        builder.append(ue);
      boolean present_te = true && (isSetTe());
      builder.append(present_te);
      if (present_te)
        builder.append(te);
      return builder.toHashCode();
    }
    public int compareTo(remove_counter_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      remove_counter_result typedOther = (remove_counter_result)other;
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetUe()).compareTo(typedOther.isSetUe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetUe()) {
        lastComparison = TBaseHelper.compareTo(this.ue, typedOther.ue);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetTe()).compareTo(typedOther.isSetTe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetTe()) {
        lastComparison = TBaseHelper.compareTo(this.te, typedOther.te);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.ue = new UnavailableException();
              this.ue.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.te = new TimedOutException();
              this.te.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetUe()) {
        oprot.writeFieldBegin(UE_FIELD_DESC);
        this.ue.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetTe()) {
        oprot.writeFieldBegin(TE_FIELD_DESC);
        this.te.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("remove_counter_result(");
      boolean first = true;
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ue:");
      if (this.ue == null) {
        sb.append("null");
      } else {
        sb.append(this.ue);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("te:");
      if (this.te == null) {
        sb.append("null");
      } else {
        sb.append(this.te);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class describe_schema_versions_args implements TBase<describe_schema_versions_args, describe_schema_versions_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("describe_schema_versions_args");
    public enum _Fields implements TFieldIdEnum {
;
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(describe_schema_versions_args.class, metaDataMap);
    }
    public describe_schema_versions_args() {
    }
    public describe_schema_versions_args(describe_schema_versions_args other) {
    }
    public describe_schema_versions_args deepCopy() {
      return new describe_schema_versions_args(this);
    }
    @Override
    public void clear() {
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof describe_schema_versions_args)
        return this.equals((describe_schema_versions_args)that);
      return false;
    }
    public boolean equals(describe_schema_versions_args that) {
      if (that == null)
        return false;
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      return builder.toHashCode();
    }
    public int compareTo(describe_schema_versions_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      describe_schema_versions_args typedOther = (describe_schema_versions_args)other;
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("describe_schema_versions_args(");
      boolean first = true;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class describe_schema_versions_result implements TBase<describe_schema_versions_result, describe_schema_versions_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("describe_schema_versions_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.MAP, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    public Map<String,List<String>> success;
    public InvalidRequestException ire;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new MapMetaData(TType.MAP, 
              new FieldValueMetaData(TType.STRING), 
              new ListMetaData(TType.LIST, 
                  new FieldValueMetaData(TType.STRING)))));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(describe_schema_versions_result.class, metaDataMap);
    }
    public describe_schema_versions_result() {
    }
    public describe_schema_versions_result(
      Map<String,List<String>> success,
      InvalidRequestException ire)
    {
      this();
      this.success = success;
      this.ire = ire;
    }
    public describe_schema_versions_result(describe_schema_versions_result other) {
      if (other.isSetSuccess()) {
        Map<String,List<String>> __this__success = new HashMap<String,List<String>>();
        for (Map.Entry<String, List<String>> other_element : other.success.entrySet()) {
          String other_element_key = other_element.getKey();
          List<String> other_element_value = other_element.getValue();
          String __this__success_copy_key = other_element_key;
          List<String> __this__success_copy_value = new ArrayList<String>();
          for (String other_element_value_element : other_element_value) {
            __this__success_copy_value.add(other_element_value_element);
          }
          __this__success.put(__this__success_copy_key, __this__success_copy_value);
        }
        this.success = __this__success;
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
    }
    public describe_schema_versions_result deepCopy() {
      return new describe_schema_versions_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.ire = null;
    }
    public int getSuccessSize() {
      return (this.success == null) ? 0 : this.success.size();
    }
    public void putToSuccess(String key, List<String> val) {
      if (this.success == null) {
        this.success = new HashMap<String,List<String>>();
      }
      this.success.put(key, val);
    }
    public Map<String,List<String>> getSuccess() {
      return this.success;
    }
    public describe_schema_versions_result setSuccess(Map<String,List<String>> success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public describe_schema_versions_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((Map<String,List<String>>)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case IRE:
        return getIre();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof describe_schema_versions_result)
        return this.equals((describe_schema_versions_result)that);
      return false;
    }
    public boolean equals(describe_schema_versions_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      return builder.toHashCode();
    }
    public int compareTo(describe_schema_versions_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      describe_schema_versions_result typedOther = (describe_schema_versions_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.MAP) {
              {
                TMap _map129 = iprot.readMapBegin();
                this.success = new HashMap<String,List<String>>(2*_map129.size);
                for (int _i130 = 0; _i130 < _map129.size; ++_i130)
                {
                  String _key131;
                  List<String> _val132;
                  _key131 = iprot.readString();
                  {
                    TList _list133 = iprot.readListBegin();
                    _val132 = new ArrayList<String>(_list133.size);
                    for (int _i134 = 0; _i134 < _list133.size; ++_i134)
                    {
                      String _elem135;
                      _elem135 = iprot.readString();
                      _val132.add(_elem135);
                    }
                    iprot.readListEnd();
                  }
                  this.success.put(_key131, _val132);
                }
                iprot.readMapEnd();
              }
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        {
          oprot.writeMapBegin(new TMap(TType.STRING, TType.LIST, this.success.size()));
          for (Map.Entry<String, List<String>> _iter136 : this.success.entrySet())
          {
            oprot.writeString(_iter136.getKey());
            {
              oprot.writeListBegin(new TList(TType.STRING, _iter136.getValue().size()));
              for (String _iter137 : _iter136.getValue())
              {
                oprot.writeString(_iter137);
              }
              oprot.writeListEnd();
            }
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("describe_schema_versions_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class describe_keyspaces_args implements TBase<describe_keyspaces_args, describe_keyspaces_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("describe_keyspaces_args");
    public enum _Fields implements TFieldIdEnum {
;
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(describe_keyspaces_args.class, metaDataMap);
    }
    public describe_keyspaces_args() {
    }
    public describe_keyspaces_args(describe_keyspaces_args other) {
    }
    public describe_keyspaces_args deepCopy() {
      return new describe_keyspaces_args(this);
    }
    @Override
    public void clear() {
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof describe_keyspaces_args)
        return this.equals((describe_keyspaces_args)that);
      return false;
    }
    public boolean equals(describe_keyspaces_args that) {
      if (that == null)
        return false;
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      return builder.toHashCode();
    }
    public int compareTo(describe_keyspaces_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      describe_keyspaces_args typedOther = (describe_keyspaces_args)other;
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("describe_keyspaces_args(");
      boolean first = true;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class describe_keyspaces_result implements TBase<describe_keyspaces_result, describe_keyspaces_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("describe_keyspaces_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.LIST, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    public List<KsDef> success;
    public InvalidRequestException ire;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new ListMetaData(TType.LIST, 
              new StructMetaData(TType.STRUCT, KsDef.class))));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(describe_keyspaces_result.class, metaDataMap);
    }
    public describe_keyspaces_result() {
    }
    public describe_keyspaces_result(
      List<KsDef> success,
      InvalidRequestException ire)
    {
      this();
      this.success = success;
      this.ire = ire;
    }
    public describe_keyspaces_result(describe_keyspaces_result other) {
      if (other.isSetSuccess()) {
        List<KsDef> __this__success = new ArrayList<KsDef>();
        for (KsDef other_element : other.success) {
          __this__success.add(new KsDef(other_element));
        }
        this.success = __this__success;
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
    }
    public describe_keyspaces_result deepCopy() {
      return new describe_keyspaces_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.ire = null;
    }
    public int getSuccessSize() {
      return (this.success == null) ? 0 : this.success.size();
    }
    public java.util.Iterator<KsDef> getSuccessIterator() {
      return (this.success == null) ? null : this.success.iterator();
    }
    public void addToSuccess(KsDef elem) {
      if (this.success == null) {
        this.success = new ArrayList<KsDef>();
      }
      this.success.add(elem);
    }
    public List<KsDef> getSuccess() {
      return this.success;
    }
    public describe_keyspaces_result setSuccess(List<KsDef> success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public describe_keyspaces_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((List<KsDef>)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case IRE:
        return getIre();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof describe_keyspaces_result)
        return this.equals((describe_keyspaces_result)that);
      return false;
    }
    public boolean equals(describe_keyspaces_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      return builder.toHashCode();
    }
    public int compareTo(describe_keyspaces_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      describe_keyspaces_result typedOther = (describe_keyspaces_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.LIST) {
              {
                TList _list138 = iprot.readListBegin();
                this.success = new ArrayList<KsDef>(_list138.size);
                for (int _i139 = 0; _i139 < _list138.size; ++_i139)
                {
                  KsDef _elem140;
                  _elem140 = new KsDef();
                  _elem140.read(iprot);
                  this.success.add(_elem140);
                }
                iprot.readListEnd();
              }
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        {
          oprot.writeListBegin(new TList(TType.STRUCT, this.success.size()));
          for (KsDef _iter141 : this.success)
          {
            _iter141.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("describe_keyspaces_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class describe_cluster_name_args implements TBase<describe_cluster_name_args, describe_cluster_name_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("describe_cluster_name_args");
    public enum _Fields implements TFieldIdEnum {
;
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(describe_cluster_name_args.class, metaDataMap);
    }
    public describe_cluster_name_args() {
    }
    public describe_cluster_name_args(describe_cluster_name_args other) {
    }
    public describe_cluster_name_args deepCopy() {
      return new describe_cluster_name_args(this);
    }
    @Override
    public void clear() {
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof describe_cluster_name_args)
        return this.equals((describe_cluster_name_args)that);
      return false;
    }
    public boolean equals(describe_cluster_name_args that) {
      if (that == null)
        return false;
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      return builder.toHashCode();
    }
    public int compareTo(describe_cluster_name_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      describe_cluster_name_args typedOther = (describe_cluster_name_args)other;
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("describe_cluster_name_args(");
      boolean first = true;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class describe_cluster_name_result implements TBase<describe_cluster_name_result, describe_cluster_name_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("describe_cluster_name_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.STRING, (short)0);
    public String success;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRING)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(describe_cluster_name_result.class, metaDataMap);
    }
    public describe_cluster_name_result() {
    }
    public describe_cluster_name_result(
      String success)
    {
      this();
      this.success = success;
    }
    public describe_cluster_name_result(describe_cluster_name_result other) {
      if (other.isSetSuccess()) {
        this.success = other.success;
      }
    }
    public describe_cluster_name_result deepCopy() {
      return new describe_cluster_name_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
    }
    public String getSuccess() {
      return this.success;
    }
    public describe_cluster_name_result setSuccess(String success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((String)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof describe_cluster_name_result)
        return this.equals((describe_cluster_name_result)that);
      return false;
    }
    public boolean equals(describe_cluster_name_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      return builder.toHashCode();
    }
    public int compareTo(describe_cluster_name_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      describe_cluster_name_result typedOther = (describe_cluster_name_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.STRING) {
              this.success = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        oprot.writeString(this.success);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("describe_cluster_name_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class describe_version_args implements TBase<describe_version_args, describe_version_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("describe_version_args");
    public enum _Fields implements TFieldIdEnum {
;
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(describe_version_args.class, metaDataMap);
    }
    public describe_version_args() {
    }
    public describe_version_args(describe_version_args other) {
    }
    public describe_version_args deepCopy() {
      return new describe_version_args(this);
    }
    @Override
    public void clear() {
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof describe_version_args)
        return this.equals((describe_version_args)that);
      return false;
    }
    public boolean equals(describe_version_args that) {
      if (that == null)
        return false;
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      return builder.toHashCode();
    }
    public int compareTo(describe_version_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      describe_version_args typedOther = (describe_version_args)other;
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("describe_version_args(");
      boolean first = true;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class describe_version_result implements TBase<describe_version_result, describe_version_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("describe_version_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.STRING, (short)0);
    public String success;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRING)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(describe_version_result.class, metaDataMap);
    }
    public describe_version_result() {
    }
    public describe_version_result(
      String success)
    {
      this();
      this.success = success;
    }
    public describe_version_result(describe_version_result other) {
      if (other.isSetSuccess()) {
        this.success = other.success;
      }
    }
    public describe_version_result deepCopy() {
      return new describe_version_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
    }
    public String getSuccess() {
      return this.success;
    }
    public describe_version_result setSuccess(String success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((String)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof describe_version_result)
        return this.equals((describe_version_result)that);
      return false;
    }
    public boolean equals(describe_version_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      return builder.toHashCode();
    }
    public int compareTo(describe_version_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      describe_version_result typedOther = (describe_version_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.STRING) {
              this.success = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        oprot.writeString(this.success);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("describe_version_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class describe_ring_args implements TBase<describe_ring_args, describe_ring_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("describe_ring_args");
    private static final TField KEYSPACE_FIELD_DESC = new TField("keyspace", TType.STRING, (short)1);
    public String keyspace;
    public enum _Fields implements TFieldIdEnum {
      KEYSPACE((short)1, "keyspace");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return KEYSPACE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.KEYSPACE, new FieldMetaData("keyspace", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.STRING)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(describe_ring_args.class, metaDataMap);
    }
    public describe_ring_args() {
    }
    public describe_ring_args(
      String keyspace)
    {
      this();
      this.keyspace = keyspace;
    }
    public describe_ring_args(describe_ring_args other) {
      if (other.isSetKeyspace()) {
        this.keyspace = other.keyspace;
      }
    }
    public describe_ring_args deepCopy() {
      return new describe_ring_args(this);
    }
    @Override
    public void clear() {
      this.keyspace = null;
    }
    public String getKeyspace() {
      return this.keyspace;
    }
    public describe_ring_args setKeyspace(String keyspace) {
      this.keyspace = keyspace;
      return this;
    }
    public void unsetKeyspace() {
      this.keyspace = null;
    }
    public boolean isSetKeyspace() {
      return this.keyspace != null;
    }
    public void setKeyspaceIsSet(boolean value) {
      if (!value) {
        this.keyspace = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case KEYSPACE:
        if (value == null) {
          unsetKeyspace();
        } else {
          setKeyspace((String)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case KEYSPACE:
        return getKeyspace();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case KEYSPACE:
        return isSetKeyspace();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof describe_ring_args)
        return this.equals((describe_ring_args)that);
      return false;
    }
    public boolean equals(describe_ring_args that) {
      if (that == null)
        return false;
      boolean this_present_keyspace = true && this.isSetKeyspace();
      boolean that_present_keyspace = true && that.isSetKeyspace();
      if (this_present_keyspace || that_present_keyspace) {
        if (!(this_present_keyspace && that_present_keyspace))
          return false;
        if (!this.keyspace.equals(that.keyspace))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_keyspace = true && (isSetKeyspace());
      builder.append(present_keyspace);
      if (present_keyspace)
        builder.append(keyspace);
      return builder.toHashCode();
    }
    public int compareTo(describe_ring_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      describe_ring_args typedOther = (describe_ring_args)other;
      lastComparison = Boolean.valueOf(isSetKeyspace()).compareTo(typedOther.isSetKeyspace());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetKeyspace()) {
        lastComparison = TBaseHelper.compareTo(this.keyspace, typedOther.keyspace);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRING) {
              this.keyspace = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.keyspace != null) {
        oprot.writeFieldBegin(KEYSPACE_FIELD_DESC);
        oprot.writeString(this.keyspace);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("describe_ring_args(");
      boolean first = true;
      sb.append("keyspace:");
      if (this.keyspace == null) {
        sb.append("null");
      } else {
        sb.append(this.keyspace);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (keyspace == null) {
        throw new TProtocolException("Required field 'keyspace' was not present! Struct: " + toString());
      }
    }
  }
  public static class describe_ring_result implements TBase<describe_ring_result, describe_ring_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("describe_ring_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.LIST, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    public List<TokenRange> success;
    public InvalidRequestException ire;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new ListMetaData(TType.LIST, 
              new StructMetaData(TType.STRUCT, TokenRange.class))));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(describe_ring_result.class, metaDataMap);
    }
    public describe_ring_result() {
    }
    public describe_ring_result(
      List<TokenRange> success,
      InvalidRequestException ire)
    {
      this();
      this.success = success;
      this.ire = ire;
    }
    public describe_ring_result(describe_ring_result other) {
      if (other.isSetSuccess()) {
        List<TokenRange> __this__success = new ArrayList<TokenRange>();
        for (TokenRange other_element : other.success) {
          __this__success.add(new TokenRange(other_element));
        }
        this.success = __this__success;
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
    }
    public describe_ring_result deepCopy() {
      return new describe_ring_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.ire = null;
    }
    public int getSuccessSize() {
      return (this.success == null) ? 0 : this.success.size();
    }
    public java.util.Iterator<TokenRange> getSuccessIterator() {
      return (this.success == null) ? null : this.success.iterator();
    }
    public void addToSuccess(TokenRange elem) {
      if (this.success == null) {
        this.success = new ArrayList<TokenRange>();
      }
      this.success.add(elem);
    }
    public List<TokenRange> getSuccess() {
      return this.success;
    }
    public describe_ring_result setSuccess(List<TokenRange> success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public describe_ring_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((List<TokenRange>)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case IRE:
        return getIre();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof describe_ring_result)
        return this.equals((describe_ring_result)that);
      return false;
    }
    public boolean equals(describe_ring_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      return builder.toHashCode();
    }
    public int compareTo(describe_ring_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      describe_ring_result typedOther = (describe_ring_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.LIST) {
              {
                TList _list142 = iprot.readListBegin();
                this.success = new ArrayList<TokenRange>(_list142.size);
                for (int _i143 = 0; _i143 < _list142.size; ++_i143)
                {
                  TokenRange _elem144;
                  _elem144 = new TokenRange();
                  _elem144.read(iprot);
                  this.success.add(_elem144);
                }
                iprot.readListEnd();
              }
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        {
          oprot.writeListBegin(new TList(TType.STRUCT, this.success.size()));
          for (TokenRange _iter145 : this.success)
          {
            _iter145.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("describe_ring_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class describe_partitioner_args implements TBase<describe_partitioner_args, describe_partitioner_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("describe_partitioner_args");
    public enum _Fields implements TFieldIdEnum {
;
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(describe_partitioner_args.class, metaDataMap);
    }
    public describe_partitioner_args() {
    }
    public describe_partitioner_args(describe_partitioner_args other) {
    }
    public describe_partitioner_args deepCopy() {
      return new describe_partitioner_args(this);
    }
    @Override
    public void clear() {
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof describe_partitioner_args)
        return this.equals((describe_partitioner_args)that);
      return false;
    }
    public boolean equals(describe_partitioner_args that) {
      if (that == null)
        return false;
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      return builder.toHashCode();
    }
    public int compareTo(describe_partitioner_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      describe_partitioner_args typedOther = (describe_partitioner_args)other;
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("describe_partitioner_args(");
      boolean first = true;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class describe_partitioner_result implements TBase<describe_partitioner_result, describe_partitioner_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("describe_partitioner_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.STRING, (short)0);
    public String success;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRING)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(describe_partitioner_result.class, metaDataMap);
    }
    public describe_partitioner_result() {
    }
    public describe_partitioner_result(
      String success)
    {
      this();
      this.success = success;
    }
    public describe_partitioner_result(describe_partitioner_result other) {
      if (other.isSetSuccess()) {
        this.success = other.success;
      }
    }
    public describe_partitioner_result deepCopy() {
      return new describe_partitioner_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
    }
    public String getSuccess() {
      return this.success;
    }
    public describe_partitioner_result setSuccess(String success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((String)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof describe_partitioner_result)
        return this.equals((describe_partitioner_result)that);
      return false;
    }
    public boolean equals(describe_partitioner_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      return builder.toHashCode();
    }
    public int compareTo(describe_partitioner_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      describe_partitioner_result typedOther = (describe_partitioner_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.STRING) {
              this.success = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        oprot.writeString(this.success);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("describe_partitioner_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class describe_snitch_args implements TBase<describe_snitch_args, describe_snitch_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("describe_snitch_args");
    public enum _Fields implements TFieldIdEnum {
;
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(describe_snitch_args.class, metaDataMap);
    }
    public describe_snitch_args() {
    }
    public describe_snitch_args(describe_snitch_args other) {
    }
    public describe_snitch_args deepCopy() {
      return new describe_snitch_args(this);
    }
    @Override
    public void clear() {
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof describe_snitch_args)
        return this.equals((describe_snitch_args)that);
      return false;
    }
    public boolean equals(describe_snitch_args that) {
      if (that == null)
        return false;
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      return builder.toHashCode();
    }
    public int compareTo(describe_snitch_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      describe_snitch_args typedOther = (describe_snitch_args)other;
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("describe_snitch_args(");
      boolean first = true;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class describe_snitch_result implements TBase<describe_snitch_result, describe_snitch_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("describe_snitch_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.STRING, (short)0);
    public String success;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRING)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(describe_snitch_result.class, metaDataMap);
    }
    public describe_snitch_result() {
    }
    public describe_snitch_result(
      String success)
    {
      this();
      this.success = success;
    }
    public describe_snitch_result(describe_snitch_result other) {
      if (other.isSetSuccess()) {
        this.success = other.success;
      }
    }
    public describe_snitch_result deepCopy() {
      return new describe_snitch_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
    }
    public String getSuccess() {
      return this.success;
    }
    public describe_snitch_result setSuccess(String success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((String)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof describe_snitch_result)
        return this.equals((describe_snitch_result)that);
      return false;
    }
    public boolean equals(describe_snitch_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      return builder.toHashCode();
    }
    public int compareTo(describe_snitch_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      describe_snitch_result typedOther = (describe_snitch_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.STRING) {
              this.success = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        oprot.writeString(this.success);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("describe_snitch_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class describe_keyspace_args implements TBase<describe_keyspace_args, describe_keyspace_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("describe_keyspace_args");
    private static final TField KEYSPACE_FIELD_DESC = new TField("keyspace", TType.STRING, (short)1);
    public String keyspace;
    public enum _Fields implements TFieldIdEnum {
      KEYSPACE((short)1, "keyspace");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return KEYSPACE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.KEYSPACE, new FieldMetaData("keyspace", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.STRING)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(describe_keyspace_args.class, metaDataMap);
    }
    public describe_keyspace_args() {
    }
    public describe_keyspace_args(
      String keyspace)
    {
      this();
      this.keyspace = keyspace;
    }
    public describe_keyspace_args(describe_keyspace_args other) {
      if (other.isSetKeyspace()) {
        this.keyspace = other.keyspace;
      }
    }
    public describe_keyspace_args deepCopy() {
      return new describe_keyspace_args(this);
    }
    @Override
    public void clear() {
      this.keyspace = null;
    }
    public String getKeyspace() {
      return this.keyspace;
    }
    public describe_keyspace_args setKeyspace(String keyspace) {
      this.keyspace = keyspace;
      return this;
    }
    public void unsetKeyspace() {
      this.keyspace = null;
    }
    public boolean isSetKeyspace() {
      return this.keyspace != null;
    }
    public void setKeyspaceIsSet(boolean value) {
      if (!value) {
        this.keyspace = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case KEYSPACE:
        if (value == null) {
          unsetKeyspace();
        } else {
          setKeyspace((String)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case KEYSPACE:
        return getKeyspace();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case KEYSPACE:
        return isSetKeyspace();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof describe_keyspace_args)
        return this.equals((describe_keyspace_args)that);
      return false;
    }
    public boolean equals(describe_keyspace_args that) {
      if (that == null)
        return false;
      boolean this_present_keyspace = true && this.isSetKeyspace();
      boolean that_present_keyspace = true && that.isSetKeyspace();
      if (this_present_keyspace || that_present_keyspace) {
        if (!(this_present_keyspace && that_present_keyspace))
          return false;
        if (!this.keyspace.equals(that.keyspace))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_keyspace = true && (isSetKeyspace());
      builder.append(present_keyspace);
      if (present_keyspace)
        builder.append(keyspace);
      return builder.toHashCode();
    }
    public int compareTo(describe_keyspace_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      describe_keyspace_args typedOther = (describe_keyspace_args)other;
      lastComparison = Boolean.valueOf(isSetKeyspace()).compareTo(typedOther.isSetKeyspace());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetKeyspace()) {
        lastComparison = TBaseHelper.compareTo(this.keyspace, typedOther.keyspace);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRING) {
              this.keyspace = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.keyspace != null) {
        oprot.writeFieldBegin(KEYSPACE_FIELD_DESC);
        oprot.writeString(this.keyspace);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("describe_keyspace_args(");
      boolean first = true;
      sb.append("keyspace:");
      if (this.keyspace == null) {
        sb.append("null");
      } else {
        sb.append(this.keyspace);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (keyspace == null) {
        throw new TProtocolException("Required field 'keyspace' was not present! Struct: " + toString());
      }
    }
  }
  public static class describe_keyspace_result implements TBase<describe_keyspace_result, describe_keyspace_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("describe_keyspace_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.STRUCT, (short)0);
    private static final TField NFE_FIELD_DESC = new TField("nfe", TType.STRUCT, (short)1);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)2);
    public KsDef success;
    public NotFoundException nfe;
    public InvalidRequestException ire;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      NFE((short)1, "nfe"),
      IRE((short)2, "ire");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return NFE;
          case 2: 
            return IRE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new StructMetaData(TType.STRUCT, KsDef.class)));
      tmpMap.put(_Fields.NFE, new FieldMetaData("nfe", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(describe_keyspace_result.class, metaDataMap);
    }
    public describe_keyspace_result() {
    }
    public describe_keyspace_result(
      KsDef success,
      NotFoundException nfe,
      InvalidRequestException ire)
    {
      this();
      this.success = success;
      this.nfe = nfe;
      this.ire = ire;
    }
    public describe_keyspace_result(describe_keyspace_result other) {
      if (other.isSetSuccess()) {
        this.success = new KsDef(other.success);
      }
      if (other.isSetNfe()) {
        this.nfe = new NotFoundException(other.nfe);
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
    }
    public describe_keyspace_result deepCopy() {
      return new describe_keyspace_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.nfe = null;
      this.ire = null;
    }
    public KsDef getSuccess() {
      return this.success;
    }
    public describe_keyspace_result setSuccess(KsDef success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public NotFoundException getNfe() {
      return this.nfe;
    }
    public describe_keyspace_result setNfe(NotFoundException nfe) {
      this.nfe = nfe;
      return this;
    }
    public void unsetNfe() {
      this.nfe = null;
    }
    public boolean isSetNfe() {
      return this.nfe != null;
    }
    public void setNfeIsSet(boolean value) {
      if (!value) {
        this.nfe = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public describe_keyspace_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((KsDef)value);
        }
        break;
      case NFE:
        if (value == null) {
          unsetNfe();
        } else {
          setNfe((NotFoundException)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case NFE:
        return getNfe();
      case IRE:
        return getIre();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case NFE:
        return isSetNfe();
      case IRE:
        return isSetIre();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof describe_keyspace_result)
        return this.equals((describe_keyspace_result)that);
      return false;
    }
    public boolean equals(describe_keyspace_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_nfe = true && this.isSetNfe();
      boolean that_present_nfe = true && that.isSetNfe();
      if (this_present_nfe || that_present_nfe) {
        if (!(this_present_nfe && that_present_nfe))
          return false;
        if (!this.nfe.equals(that.nfe))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_nfe = true && (isSetNfe());
      builder.append(present_nfe);
      if (present_nfe)
        builder.append(nfe);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      return builder.toHashCode();
    }
    public int compareTo(describe_keyspace_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      describe_keyspace_result typedOther = (describe_keyspace_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetNfe()).compareTo(typedOther.isSetNfe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetNfe()) {
        lastComparison = TBaseHelper.compareTo(this.nfe, typedOther.nfe);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.STRUCT) {
              this.success = new KsDef();
              this.success.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.nfe = new NotFoundException();
              this.nfe.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        this.success.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetNfe()) {
        oprot.writeFieldBegin(NFE_FIELD_DESC);
        this.nfe.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("describe_keyspace_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("nfe:");
      if (this.nfe == null) {
        sb.append("null");
      } else {
        sb.append(this.nfe);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class describe_splits_args implements TBase<describe_splits_args, describe_splits_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("describe_splits_args");
    private static final TField CF_NAME_FIELD_DESC = new TField("cfName", TType.STRING, (short)1);
    private static final TField START_TOKEN_FIELD_DESC = new TField("start_token", TType.STRING, (short)2);
    private static final TField END_TOKEN_FIELD_DESC = new TField("end_token", TType.STRING, (short)3);
    private static final TField KEYS_PER_SPLIT_FIELD_DESC = new TField("keys_per_split", TType.I32, (short)4);
    public String cfName;
    public String start_token;
    public String end_token;
    public int keys_per_split;
    public enum _Fields implements TFieldIdEnum {
      CF_NAME((short)1, "cfName"),
      START_TOKEN((short)2, "start_token"),
      END_TOKEN((short)3, "end_token"),
      KEYS_PER_SPLIT((short)4, "keys_per_split");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return CF_NAME;
          case 2: 
            return START_TOKEN;
          case 3: 
            return END_TOKEN;
          case 4: 
            return KEYS_PER_SPLIT;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    private static final int __KEYS_PER_SPLIT_ISSET_ID = 0;
    private BitSet __isset_bit_vector = new BitSet(1);
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.CF_NAME, new FieldMetaData("cfName", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.STRING)));
      tmpMap.put(_Fields.START_TOKEN, new FieldMetaData("start_token", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.STRING)));
      tmpMap.put(_Fields.END_TOKEN, new FieldMetaData("end_token", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.STRING)));
      tmpMap.put(_Fields.KEYS_PER_SPLIT, new FieldMetaData("keys_per_split", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.I32)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(describe_splits_args.class, metaDataMap);
    }
    public describe_splits_args() {
    }
    public describe_splits_args(
      String cfName,
      String start_token,
      String end_token,
      int keys_per_split)
    {
      this();
      this.cfName = cfName;
      this.start_token = start_token;
      this.end_token = end_token;
      this.keys_per_split = keys_per_split;
      setKeys_per_splitIsSet(true);
    }
    public describe_splits_args(describe_splits_args other) {
      __isset_bit_vector.clear();
      __isset_bit_vector.or(other.__isset_bit_vector);
      if (other.isSetCfName()) {
        this.cfName = other.cfName;
      }
      if (other.isSetStart_token()) {
        this.start_token = other.start_token;
      }
      if (other.isSetEnd_token()) {
        this.end_token = other.end_token;
      }
      this.keys_per_split = other.keys_per_split;
    }
    public describe_splits_args deepCopy() {
      return new describe_splits_args(this);
    }
    @Override
    public void clear() {
      this.cfName = null;
      this.start_token = null;
      this.end_token = null;
      setKeys_per_splitIsSet(false);
      this.keys_per_split = 0;
    }
    public String getCfName() {
      return this.cfName;
    }
    public describe_splits_args setCfName(String cfName) {
      this.cfName = cfName;
      return this;
    }
    public void unsetCfName() {
      this.cfName = null;
    }
    public boolean isSetCfName() {
      return this.cfName != null;
    }
    public void setCfNameIsSet(boolean value) {
      if (!value) {
        this.cfName = null;
      }
    }
    public String getStart_token() {
      return this.start_token;
    }
    public describe_splits_args setStart_token(String start_token) {
      this.start_token = start_token;
      return this;
    }
    public void unsetStart_token() {
      this.start_token = null;
    }
    public boolean isSetStart_token() {
      return this.start_token != null;
    }
    public void setStart_tokenIsSet(boolean value) {
      if (!value) {
        this.start_token = null;
      }
    }
    public String getEnd_token() {
      return this.end_token;
    }
    public describe_splits_args setEnd_token(String end_token) {
      this.end_token = end_token;
      return this;
    }
    public void unsetEnd_token() {
      this.end_token = null;
    }
    public boolean isSetEnd_token() {
      return this.end_token != null;
    }
    public void setEnd_tokenIsSet(boolean value) {
      if (!value) {
        this.end_token = null;
      }
    }
    public int getKeys_per_split() {
      return this.keys_per_split;
    }
    public describe_splits_args setKeys_per_split(int keys_per_split) {
      this.keys_per_split = keys_per_split;
      setKeys_per_splitIsSet(true);
      return this;
    }
    public void unsetKeys_per_split() {
      __isset_bit_vector.clear(__KEYS_PER_SPLIT_ISSET_ID);
    }
    public boolean isSetKeys_per_split() {
      return __isset_bit_vector.get(__KEYS_PER_SPLIT_ISSET_ID);
    }
    public void setKeys_per_splitIsSet(boolean value) {
      __isset_bit_vector.set(__KEYS_PER_SPLIT_ISSET_ID, value);
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case CF_NAME:
        if (value == null) {
          unsetCfName();
        } else {
          setCfName((String)value);
        }
        break;
      case START_TOKEN:
        if (value == null) {
          unsetStart_token();
        } else {
          setStart_token((String)value);
        }
        break;
      case END_TOKEN:
        if (value == null) {
          unsetEnd_token();
        } else {
          setEnd_token((String)value);
        }
        break;
      case KEYS_PER_SPLIT:
        if (value == null) {
          unsetKeys_per_split();
        } else {
          setKeys_per_split((Integer)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case CF_NAME:
        return getCfName();
      case START_TOKEN:
        return getStart_token();
      case END_TOKEN:
        return getEnd_token();
      case KEYS_PER_SPLIT:
        return new Integer(getKeys_per_split());
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case CF_NAME:
        return isSetCfName();
      case START_TOKEN:
        return isSetStart_token();
      case END_TOKEN:
        return isSetEnd_token();
      case KEYS_PER_SPLIT:
        return isSetKeys_per_split();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof describe_splits_args)
        return this.equals((describe_splits_args)that);
      return false;
    }
    public boolean equals(describe_splits_args that) {
      if (that == null)
        return false;
      boolean this_present_cfName = true && this.isSetCfName();
      boolean that_present_cfName = true && that.isSetCfName();
      if (this_present_cfName || that_present_cfName) {
        if (!(this_present_cfName && that_present_cfName))
          return false;
        if (!this.cfName.equals(that.cfName))
          return false;
      }
      boolean this_present_start_token = true && this.isSetStart_token();
      boolean that_present_start_token = true && that.isSetStart_token();
      if (this_present_start_token || that_present_start_token) {
        if (!(this_present_start_token && that_present_start_token))
          return false;
        if (!this.start_token.equals(that.start_token))
          return false;
      }
      boolean this_present_end_token = true && this.isSetEnd_token();
      boolean that_present_end_token = true && that.isSetEnd_token();
      if (this_present_end_token || that_present_end_token) {
        if (!(this_present_end_token && that_present_end_token))
          return false;
        if (!this.end_token.equals(that.end_token))
          return false;
      }
      boolean this_present_keys_per_split = true;
      boolean that_present_keys_per_split = true;
      if (this_present_keys_per_split || that_present_keys_per_split) {
        if (!(this_present_keys_per_split && that_present_keys_per_split))
          return false;
        if (this.keys_per_split != that.keys_per_split)
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_cfName = true && (isSetCfName());
      builder.append(present_cfName);
      if (present_cfName)
        builder.append(cfName);
      boolean present_start_token = true && (isSetStart_token());
      builder.append(present_start_token);
      if (present_start_token)
        builder.append(start_token);
      boolean present_end_token = true && (isSetEnd_token());
      builder.append(present_end_token);
      if (present_end_token)
        builder.append(end_token);
      boolean present_keys_per_split = true;
      builder.append(present_keys_per_split);
      if (present_keys_per_split)
        builder.append(keys_per_split);
      return builder.toHashCode();
    }
    public int compareTo(describe_splits_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      describe_splits_args typedOther = (describe_splits_args)other;
      lastComparison = Boolean.valueOf(isSetCfName()).compareTo(typedOther.isSetCfName());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetCfName()) {
        lastComparison = TBaseHelper.compareTo(this.cfName, typedOther.cfName);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetStart_token()).compareTo(typedOther.isSetStart_token());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetStart_token()) {
        lastComparison = TBaseHelper.compareTo(this.start_token, typedOther.start_token);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetEnd_token()).compareTo(typedOther.isSetEnd_token());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetEnd_token()) {
        lastComparison = TBaseHelper.compareTo(this.end_token, typedOther.end_token);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetKeys_per_split()).compareTo(typedOther.isSetKeys_per_split());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetKeys_per_split()) {
        lastComparison = TBaseHelper.compareTo(this.keys_per_split, typedOther.keys_per_split);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRING) {
              this.cfName = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRING) {
              this.start_token = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRING) {
              this.end_token = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 4: 
            if (field.type == TType.I32) {
              this.keys_per_split = iprot.readI32();
              setKeys_per_splitIsSet(true);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      if (!isSetKeys_per_split()) {
        throw new TProtocolException("Required field 'keys_per_split' was not found in serialized data! Struct: " + toString());
      }
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.cfName != null) {
        oprot.writeFieldBegin(CF_NAME_FIELD_DESC);
        oprot.writeString(this.cfName);
        oprot.writeFieldEnd();
      }
      if (this.start_token != null) {
        oprot.writeFieldBegin(START_TOKEN_FIELD_DESC);
        oprot.writeString(this.start_token);
        oprot.writeFieldEnd();
      }
      if (this.end_token != null) {
        oprot.writeFieldBegin(END_TOKEN_FIELD_DESC);
        oprot.writeString(this.end_token);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(KEYS_PER_SPLIT_FIELD_DESC);
      oprot.writeI32(this.keys_per_split);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("describe_splits_args(");
      boolean first = true;
      sb.append("cfName:");
      if (this.cfName == null) {
        sb.append("null");
      } else {
        sb.append(this.cfName);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("start_token:");
      if (this.start_token == null) {
        sb.append("null");
      } else {
        sb.append(this.start_token);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("end_token:");
      if (this.end_token == null) {
        sb.append("null");
      } else {
        sb.append(this.end_token);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("keys_per_split:");
      sb.append(this.keys_per_split);
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (cfName == null) {
        throw new TProtocolException("Required field 'cfName' was not present! Struct: " + toString());
      }
      if (start_token == null) {
        throw new TProtocolException("Required field 'start_token' was not present! Struct: " + toString());
      }
      if (end_token == null) {
        throw new TProtocolException("Required field 'end_token' was not present! Struct: " + toString());
      }
    }
  }
  public static class describe_splits_result implements TBase<describe_splits_result, describe_splits_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("describe_splits_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.LIST, (short)0);
    public List<String> success;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new ListMetaData(TType.LIST, 
              new FieldValueMetaData(TType.STRING))));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(describe_splits_result.class, metaDataMap);
    }
    public describe_splits_result() {
    }
    public describe_splits_result(
      List<String> success)
    {
      this();
      this.success = success;
    }
    public describe_splits_result(describe_splits_result other) {
      if (other.isSetSuccess()) {
        List<String> __this__success = new ArrayList<String>();
        for (String other_element : other.success) {
          __this__success.add(other_element);
        }
        this.success = __this__success;
      }
    }
    public describe_splits_result deepCopy() {
      return new describe_splits_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
    }
    public int getSuccessSize() {
      return (this.success == null) ? 0 : this.success.size();
    }
    public java.util.Iterator<String> getSuccessIterator() {
      return (this.success == null) ? null : this.success.iterator();
    }
    public void addToSuccess(String elem) {
      if (this.success == null) {
        this.success = new ArrayList<String>();
      }
      this.success.add(elem);
    }
    public List<String> getSuccess() {
      return this.success;
    }
    public describe_splits_result setSuccess(List<String> success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((List<String>)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof describe_splits_result)
        return this.equals((describe_splits_result)that);
      return false;
    }
    public boolean equals(describe_splits_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      return builder.toHashCode();
    }
    public int compareTo(describe_splits_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      describe_splits_result typedOther = (describe_splits_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.LIST) {
              {
                TList _list146 = iprot.readListBegin();
                this.success = new ArrayList<String>(_list146.size);
                for (int _i147 = 0; _i147 < _list146.size; ++_i147)
                {
                  String _elem148;
                  _elem148 = iprot.readString();
                  this.success.add(_elem148);
                }
                iprot.readListEnd();
              }
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        {
          oprot.writeListBegin(new TList(TType.STRING, this.success.size()));
          for (String _iter149 : this.success)
          {
            oprot.writeString(_iter149);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("describe_splits_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class system_add_column_family_args implements TBase<system_add_column_family_args, system_add_column_family_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("system_add_column_family_args");
    private static final TField CF_DEF_FIELD_DESC = new TField("cf_def", TType.STRUCT, (short)1);
    public CfDef cf_def;
    public enum _Fields implements TFieldIdEnum {
      CF_DEF((short)1, "cf_def");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return CF_DEF;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.CF_DEF, new FieldMetaData("cf_def", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, CfDef.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(system_add_column_family_args.class, metaDataMap);
    }
    public system_add_column_family_args() {
    }
    public system_add_column_family_args(
      CfDef cf_def)
    {
      this();
      this.cf_def = cf_def;
    }
    public system_add_column_family_args(system_add_column_family_args other) {
      if (other.isSetCf_def()) {
        this.cf_def = new CfDef(other.cf_def);
      }
    }
    public system_add_column_family_args deepCopy() {
      return new system_add_column_family_args(this);
    }
    @Override
    public void clear() {
      this.cf_def = null;
    }
    public CfDef getCf_def() {
      return this.cf_def;
    }
    public system_add_column_family_args setCf_def(CfDef cf_def) {
      this.cf_def = cf_def;
      return this;
    }
    public void unsetCf_def() {
      this.cf_def = null;
    }
    public boolean isSetCf_def() {
      return this.cf_def != null;
    }
    public void setCf_defIsSet(boolean value) {
      if (!value) {
        this.cf_def = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case CF_DEF:
        if (value == null) {
          unsetCf_def();
        } else {
          setCf_def((CfDef)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case CF_DEF:
        return getCf_def();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case CF_DEF:
        return isSetCf_def();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof system_add_column_family_args)
        return this.equals((system_add_column_family_args)that);
      return false;
    }
    public boolean equals(system_add_column_family_args that) {
      if (that == null)
        return false;
      boolean this_present_cf_def = true && this.isSetCf_def();
      boolean that_present_cf_def = true && that.isSetCf_def();
      if (this_present_cf_def || that_present_cf_def) {
        if (!(this_present_cf_def && that_present_cf_def))
          return false;
        if (!this.cf_def.equals(that.cf_def))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_cf_def = true && (isSetCf_def());
      builder.append(present_cf_def);
      if (present_cf_def)
        builder.append(cf_def);
      return builder.toHashCode();
    }
    public int compareTo(system_add_column_family_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      system_add_column_family_args typedOther = (system_add_column_family_args)other;
      lastComparison = Boolean.valueOf(isSetCf_def()).compareTo(typedOther.isSetCf_def());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetCf_def()) {
        lastComparison = TBaseHelper.compareTo(this.cf_def, typedOther.cf_def);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRUCT) {
              this.cf_def = new CfDef();
              this.cf_def.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.cf_def != null) {
        oprot.writeFieldBegin(CF_DEF_FIELD_DESC);
        this.cf_def.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("system_add_column_family_args(");
      boolean first = true;
      sb.append("cf_def:");
      if (this.cf_def == null) {
        sb.append("null");
      } else {
        sb.append(this.cf_def);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (cf_def == null) {
        throw new TProtocolException("Required field 'cf_def' was not present! Struct: " + toString());
      }
    }
  }
  public static class system_add_column_family_result implements TBase<system_add_column_family_result, system_add_column_family_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("system_add_column_family_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.STRING, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    public String success;
    public InvalidRequestException ire;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRING)));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(system_add_column_family_result.class, metaDataMap);
    }
    public system_add_column_family_result() {
    }
    public system_add_column_family_result(
      String success,
      InvalidRequestException ire)
    {
      this();
      this.success = success;
      this.ire = ire;
    }
    public system_add_column_family_result(system_add_column_family_result other) {
      if (other.isSetSuccess()) {
        this.success = other.success;
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
    }
    public system_add_column_family_result deepCopy() {
      return new system_add_column_family_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.ire = null;
    }
    public String getSuccess() {
      return this.success;
    }
    public system_add_column_family_result setSuccess(String success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public system_add_column_family_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((String)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case IRE:
        return getIre();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof system_add_column_family_result)
        return this.equals((system_add_column_family_result)that);
      return false;
    }
    public boolean equals(system_add_column_family_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      return builder.toHashCode();
    }
    public int compareTo(system_add_column_family_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      system_add_column_family_result typedOther = (system_add_column_family_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.STRING) {
              this.success = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        oprot.writeString(this.success);
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("system_add_column_family_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class system_drop_column_family_args implements TBase<system_drop_column_family_args, system_drop_column_family_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("system_drop_column_family_args");
    private static final TField COLUMN_FAMILY_FIELD_DESC = new TField("column_family", TType.STRING, (short)1);
    public String column_family;
    public enum _Fields implements TFieldIdEnum {
      COLUMN_FAMILY((short)1, "column_family");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return COLUMN_FAMILY;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.COLUMN_FAMILY, new FieldMetaData("column_family", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.STRING)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(system_drop_column_family_args.class, metaDataMap);
    }
    public system_drop_column_family_args() {
    }
    public system_drop_column_family_args(
      String column_family)
    {
      this();
      this.column_family = column_family;
    }
    public system_drop_column_family_args(system_drop_column_family_args other) {
      if (other.isSetColumn_family()) {
        this.column_family = other.column_family;
      }
    }
    public system_drop_column_family_args deepCopy() {
      return new system_drop_column_family_args(this);
    }
    @Override
    public void clear() {
      this.column_family = null;
    }
    public String getColumn_family() {
      return this.column_family;
    }
    public system_drop_column_family_args setColumn_family(String column_family) {
      this.column_family = column_family;
      return this;
    }
    public void unsetColumn_family() {
      this.column_family = null;
    }
    public boolean isSetColumn_family() {
      return this.column_family != null;
    }
    public void setColumn_familyIsSet(boolean value) {
      if (!value) {
        this.column_family = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case COLUMN_FAMILY:
        if (value == null) {
          unsetColumn_family();
        } else {
          setColumn_family((String)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case COLUMN_FAMILY:
        return getColumn_family();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case COLUMN_FAMILY:
        return isSetColumn_family();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof system_drop_column_family_args)
        return this.equals((system_drop_column_family_args)that);
      return false;
    }
    public boolean equals(system_drop_column_family_args that) {
      if (that == null)
        return false;
      boolean this_present_column_family = true && this.isSetColumn_family();
      boolean that_present_column_family = true && that.isSetColumn_family();
      if (this_present_column_family || that_present_column_family) {
        if (!(this_present_column_family && that_present_column_family))
          return false;
        if (!this.column_family.equals(that.column_family))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_column_family = true && (isSetColumn_family());
      builder.append(present_column_family);
      if (present_column_family)
        builder.append(column_family);
      return builder.toHashCode();
    }
    public int compareTo(system_drop_column_family_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      system_drop_column_family_args typedOther = (system_drop_column_family_args)other;
      lastComparison = Boolean.valueOf(isSetColumn_family()).compareTo(typedOther.isSetColumn_family());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetColumn_family()) {
        lastComparison = TBaseHelper.compareTo(this.column_family, typedOther.column_family);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRING) {
              this.column_family = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.column_family != null) {
        oprot.writeFieldBegin(COLUMN_FAMILY_FIELD_DESC);
        oprot.writeString(this.column_family);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("system_drop_column_family_args(");
      boolean first = true;
      sb.append("column_family:");
      if (this.column_family == null) {
        sb.append("null");
      } else {
        sb.append(this.column_family);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (column_family == null) {
        throw new TProtocolException("Required field 'column_family' was not present! Struct: " + toString());
      }
    }
  }
  public static class system_drop_column_family_result implements TBase<system_drop_column_family_result, system_drop_column_family_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("system_drop_column_family_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.STRING, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    public String success;
    public InvalidRequestException ire;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRING)));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(system_drop_column_family_result.class, metaDataMap);
    }
    public system_drop_column_family_result() {
    }
    public system_drop_column_family_result(
      String success,
      InvalidRequestException ire)
    {
      this();
      this.success = success;
      this.ire = ire;
    }
    public system_drop_column_family_result(system_drop_column_family_result other) {
      if (other.isSetSuccess()) {
        this.success = other.success;
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
    }
    public system_drop_column_family_result deepCopy() {
      return new system_drop_column_family_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.ire = null;
    }
    public String getSuccess() {
      return this.success;
    }
    public system_drop_column_family_result setSuccess(String success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public system_drop_column_family_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((String)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case IRE:
        return getIre();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof system_drop_column_family_result)
        return this.equals((system_drop_column_family_result)that);
      return false;
    }
    public boolean equals(system_drop_column_family_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      return builder.toHashCode();
    }
    public int compareTo(system_drop_column_family_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      system_drop_column_family_result typedOther = (system_drop_column_family_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.STRING) {
              this.success = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        oprot.writeString(this.success);
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("system_drop_column_family_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class system_add_keyspace_args implements TBase<system_add_keyspace_args, system_add_keyspace_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("system_add_keyspace_args");
    private static final TField KS_DEF_FIELD_DESC = new TField("ks_def", TType.STRUCT, (short)1);
    public KsDef ks_def;
    public enum _Fields implements TFieldIdEnum {
      KS_DEF((short)1, "ks_def");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return KS_DEF;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.KS_DEF, new FieldMetaData("ks_def", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, KsDef.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(system_add_keyspace_args.class, metaDataMap);
    }
    public system_add_keyspace_args() {
    }
    public system_add_keyspace_args(
      KsDef ks_def)
    {
      this();
      this.ks_def = ks_def;
    }
    public system_add_keyspace_args(system_add_keyspace_args other) {
      if (other.isSetKs_def()) {
        this.ks_def = new KsDef(other.ks_def);
      }
    }
    public system_add_keyspace_args deepCopy() {
      return new system_add_keyspace_args(this);
    }
    @Override
    public void clear() {
      this.ks_def = null;
    }
    public KsDef getKs_def() {
      return this.ks_def;
    }
    public system_add_keyspace_args setKs_def(KsDef ks_def) {
      this.ks_def = ks_def;
      return this;
    }
    public void unsetKs_def() {
      this.ks_def = null;
    }
    public boolean isSetKs_def() {
      return this.ks_def != null;
    }
    public void setKs_defIsSet(boolean value) {
      if (!value) {
        this.ks_def = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case KS_DEF:
        if (value == null) {
          unsetKs_def();
        } else {
          setKs_def((KsDef)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case KS_DEF:
        return getKs_def();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case KS_DEF:
        return isSetKs_def();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof system_add_keyspace_args)
        return this.equals((system_add_keyspace_args)that);
      return false;
    }
    public boolean equals(system_add_keyspace_args that) {
      if (that == null)
        return false;
      boolean this_present_ks_def = true && this.isSetKs_def();
      boolean that_present_ks_def = true && that.isSetKs_def();
      if (this_present_ks_def || that_present_ks_def) {
        if (!(this_present_ks_def && that_present_ks_def))
          return false;
        if (!this.ks_def.equals(that.ks_def))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_ks_def = true && (isSetKs_def());
      builder.append(present_ks_def);
      if (present_ks_def)
        builder.append(ks_def);
      return builder.toHashCode();
    }
    public int compareTo(system_add_keyspace_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      system_add_keyspace_args typedOther = (system_add_keyspace_args)other;
      lastComparison = Boolean.valueOf(isSetKs_def()).compareTo(typedOther.isSetKs_def());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetKs_def()) {
        lastComparison = TBaseHelper.compareTo(this.ks_def, typedOther.ks_def);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ks_def = new KsDef();
              this.ks_def.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.ks_def != null) {
        oprot.writeFieldBegin(KS_DEF_FIELD_DESC);
        this.ks_def.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("system_add_keyspace_args(");
      boolean first = true;
      sb.append("ks_def:");
      if (this.ks_def == null) {
        sb.append("null");
      } else {
        sb.append(this.ks_def);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (ks_def == null) {
        throw new TProtocolException("Required field 'ks_def' was not present! Struct: " + toString());
      }
    }
  }
  public static class system_add_keyspace_result implements TBase<system_add_keyspace_result, system_add_keyspace_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("system_add_keyspace_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.STRING, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    public String success;
    public InvalidRequestException ire;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRING)));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(system_add_keyspace_result.class, metaDataMap);
    }
    public system_add_keyspace_result() {
    }
    public system_add_keyspace_result(
      String success,
      InvalidRequestException ire)
    {
      this();
      this.success = success;
      this.ire = ire;
    }
    public system_add_keyspace_result(system_add_keyspace_result other) {
      if (other.isSetSuccess()) {
        this.success = other.success;
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
    }
    public system_add_keyspace_result deepCopy() {
      return new system_add_keyspace_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.ire = null;
    }
    public String getSuccess() {
      return this.success;
    }
    public system_add_keyspace_result setSuccess(String success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public system_add_keyspace_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((String)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case IRE:
        return getIre();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof system_add_keyspace_result)
        return this.equals((system_add_keyspace_result)that);
      return false;
    }
    public boolean equals(system_add_keyspace_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      return builder.toHashCode();
    }
    public int compareTo(system_add_keyspace_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      system_add_keyspace_result typedOther = (system_add_keyspace_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.STRING) {
              this.success = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        oprot.writeString(this.success);
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("system_add_keyspace_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class system_drop_keyspace_args implements TBase<system_drop_keyspace_args, system_drop_keyspace_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("system_drop_keyspace_args");
    private static final TField KEYSPACE_FIELD_DESC = new TField("keyspace", TType.STRING, (short)1);
    public String keyspace;
    public enum _Fields implements TFieldIdEnum {
      KEYSPACE((short)1, "keyspace");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return KEYSPACE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.KEYSPACE, new FieldMetaData("keyspace", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.STRING)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(system_drop_keyspace_args.class, metaDataMap);
    }
    public system_drop_keyspace_args() {
    }
    public system_drop_keyspace_args(
      String keyspace)
    {
      this();
      this.keyspace = keyspace;
    }
    public system_drop_keyspace_args(system_drop_keyspace_args other) {
      if (other.isSetKeyspace()) {
        this.keyspace = other.keyspace;
      }
    }
    public system_drop_keyspace_args deepCopy() {
      return new system_drop_keyspace_args(this);
    }
    @Override
    public void clear() {
      this.keyspace = null;
    }
    public String getKeyspace() {
      return this.keyspace;
    }
    public system_drop_keyspace_args setKeyspace(String keyspace) {
      this.keyspace = keyspace;
      return this;
    }
    public void unsetKeyspace() {
      this.keyspace = null;
    }
    public boolean isSetKeyspace() {
      return this.keyspace != null;
    }
    public void setKeyspaceIsSet(boolean value) {
      if (!value) {
        this.keyspace = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case KEYSPACE:
        if (value == null) {
          unsetKeyspace();
        } else {
          setKeyspace((String)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case KEYSPACE:
        return getKeyspace();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case KEYSPACE:
        return isSetKeyspace();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof system_drop_keyspace_args)
        return this.equals((system_drop_keyspace_args)that);
      return false;
    }
    public boolean equals(system_drop_keyspace_args that) {
      if (that == null)
        return false;
      boolean this_present_keyspace = true && this.isSetKeyspace();
      boolean that_present_keyspace = true && that.isSetKeyspace();
      if (this_present_keyspace || that_present_keyspace) {
        if (!(this_present_keyspace && that_present_keyspace))
          return false;
        if (!this.keyspace.equals(that.keyspace))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_keyspace = true && (isSetKeyspace());
      builder.append(present_keyspace);
      if (present_keyspace)
        builder.append(keyspace);
      return builder.toHashCode();
    }
    public int compareTo(system_drop_keyspace_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      system_drop_keyspace_args typedOther = (system_drop_keyspace_args)other;
      lastComparison = Boolean.valueOf(isSetKeyspace()).compareTo(typedOther.isSetKeyspace());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetKeyspace()) {
        lastComparison = TBaseHelper.compareTo(this.keyspace, typedOther.keyspace);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRING) {
              this.keyspace = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.keyspace != null) {
        oprot.writeFieldBegin(KEYSPACE_FIELD_DESC);
        oprot.writeString(this.keyspace);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("system_drop_keyspace_args(");
      boolean first = true;
      sb.append("keyspace:");
      if (this.keyspace == null) {
        sb.append("null");
      } else {
        sb.append(this.keyspace);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (keyspace == null) {
        throw new TProtocolException("Required field 'keyspace' was not present! Struct: " + toString());
      }
    }
  }
  public static class system_drop_keyspace_result implements TBase<system_drop_keyspace_result, system_drop_keyspace_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("system_drop_keyspace_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.STRING, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    public String success;
    public InvalidRequestException ire;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRING)));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(system_drop_keyspace_result.class, metaDataMap);
    }
    public system_drop_keyspace_result() {
    }
    public system_drop_keyspace_result(
      String success,
      InvalidRequestException ire)
    {
      this();
      this.success = success;
      this.ire = ire;
    }
    public system_drop_keyspace_result(system_drop_keyspace_result other) {
      if (other.isSetSuccess()) {
        this.success = other.success;
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
    }
    public system_drop_keyspace_result deepCopy() {
      return new system_drop_keyspace_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.ire = null;
    }
    public String getSuccess() {
      return this.success;
    }
    public system_drop_keyspace_result setSuccess(String success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public system_drop_keyspace_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((String)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case IRE:
        return getIre();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof system_drop_keyspace_result)
        return this.equals((system_drop_keyspace_result)that);
      return false;
    }
    public boolean equals(system_drop_keyspace_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      return builder.toHashCode();
    }
    public int compareTo(system_drop_keyspace_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      system_drop_keyspace_result typedOther = (system_drop_keyspace_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.STRING) {
              this.success = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        oprot.writeString(this.success);
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("system_drop_keyspace_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class system_update_keyspace_args implements TBase<system_update_keyspace_args, system_update_keyspace_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("system_update_keyspace_args");
    private static final TField KS_DEF_FIELD_DESC = new TField("ks_def", TType.STRUCT, (short)1);
    public KsDef ks_def;
    public enum _Fields implements TFieldIdEnum {
      KS_DEF((short)1, "ks_def");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return KS_DEF;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.KS_DEF, new FieldMetaData("ks_def", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, KsDef.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(system_update_keyspace_args.class, metaDataMap);
    }
    public system_update_keyspace_args() {
    }
    public system_update_keyspace_args(
      KsDef ks_def)
    {
      this();
      this.ks_def = ks_def;
    }
    public system_update_keyspace_args(system_update_keyspace_args other) {
      if (other.isSetKs_def()) {
        this.ks_def = new KsDef(other.ks_def);
      }
    }
    public system_update_keyspace_args deepCopy() {
      return new system_update_keyspace_args(this);
    }
    @Override
    public void clear() {
      this.ks_def = null;
    }
    public KsDef getKs_def() {
      return this.ks_def;
    }
    public system_update_keyspace_args setKs_def(KsDef ks_def) {
      this.ks_def = ks_def;
      return this;
    }
    public void unsetKs_def() {
      this.ks_def = null;
    }
    public boolean isSetKs_def() {
      return this.ks_def != null;
    }
    public void setKs_defIsSet(boolean value) {
      if (!value) {
        this.ks_def = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case KS_DEF:
        if (value == null) {
          unsetKs_def();
        } else {
          setKs_def((KsDef)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case KS_DEF:
        return getKs_def();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case KS_DEF:
        return isSetKs_def();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof system_update_keyspace_args)
        return this.equals((system_update_keyspace_args)that);
      return false;
    }
    public boolean equals(system_update_keyspace_args that) {
      if (that == null)
        return false;
      boolean this_present_ks_def = true && this.isSetKs_def();
      boolean that_present_ks_def = true && that.isSetKs_def();
      if (this_present_ks_def || that_present_ks_def) {
        if (!(this_present_ks_def && that_present_ks_def))
          return false;
        if (!this.ks_def.equals(that.ks_def))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_ks_def = true && (isSetKs_def());
      builder.append(present_ks_def);
      if (present_ks_def)
        builder.append(ks_def);
      return builder.toHashCode();
    }
    public int compareTo(system_update_keyspace_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      system_update_keyspace_args typedOther = (system_update_keyspace_args)other;
      lastComparison = Boolean.valueOf(isSetKs_def()).compareTo(typedOther.isSetKs_def());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetKs_def()) {
        lastComparison = TBaseHelper.compareTo(this.ks_def, typedOther.ks_def);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ks_def = new KsDef();
              this.ks_def.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.ks_def != null) {
        oprot.writeFieldBegin(KS_DEF_FIELD_DESC);
        this.ks_def.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("system_update_keyspace_args(");
      boolean first = true;
      sb.append("ks_def:");
      if (this.ks_def == null) {
        sb.append("null");
      } else {
        sb.append(this.ks_def);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (ks_def == null) {
        throw new TProtocolException("Required field 'ks_def' was not present! Struct: " + toString());
      }
    }
  }
  public static class system_update_keyspace_result implements TBase<system_update_keyspace_result, system_update_keyspace_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("system_update_keyspace_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.STRING, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    public String success;
    public InvalidRequestException ire;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRING)));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(system_update_keyspace_result.class, metaDataMap);
    }
    public system_update_keyspace_result() {
    }
    public system_update_keyspace_result(
      String success,
      InvalidRequestException ire)
    {
      this();
      this.success = success;
      this.ire = ire;
    }
    public system_update_keyspace_result(system_update_keyspace_result other) {
      if (other.isSetSuccess()) {
        this.success = other.success;
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
    }
    public system_update_keyspace_result deepCopy() {
      return new system_update_keyspace_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.ire = null;
    }
    public String getSuccess() {
      return this.success;
    }
    public system_update_keyspace_result setSuccess(String success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public system_update_keyspace_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((String)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case IRE:
        return getIre();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof system_update_keyspace_result)
        return this.equals((system_update_keyspace_result)that);
      return false;
    }
    public boolean equals(system_update_keyspace_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      return builder.toHashCode();
    }
    public int compareTo(system_update_keyspace_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      system_update_keyspace_result typedOther = (system_update_keyspace_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.STRING) {
              this.success = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        oprot.writeString(this.success);
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("system_update_keyspace_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class system_update_column_family_args implements TBase<system_update_column_family_args, system_update_column_family_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("system_update_column_family_args");
    private static final TField CF_DEF_FIELD_DESC = new TField("cf_def", TType.STRUCT, (short)1);
    public CfDef cf_def;
    public enum _Fields implements TFieldIdEnum {
      CF_DEF((short)1, "cf_def");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return CF_DEF;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.CF_DEF, new FieldMetaData("cf_def", TFieldRequirementType.REQUIRED, 
          new StructMetaData(TType.STRUCT, CfDef.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(system_update_column_family_args.class, metaDataMap);
    }
    public system_update_column_family_args() {
    }
    public system_update_column_family_args(
      CfDef cf_def)
    {
      this();
      this.cf_def = cf_def;
    }
    public system_update_column_family_args(system_update_column_family_args other) {
      if (other.isSetCf_def()) {
        this.cf_def = new CfDef(other.cf_def);
      }
    }
    public system_update_column_family_args deepCopy() {
      return new system_update_column_family_args(this);
    }
    @Override
    public void clear() {
      this.cf_def = null;
    }
    public CfDef getCf_def() {
      return this.cf_def;
    }
    public system_update_column_family_args setCf_def(CfDef cf_def) {
      this.cf_def = cf_def;
      return this;
    }
    public void unsetCf_def() {
      this.cf_def = null;
    }
    public boolean isSetCf_def() {
      return this.cf_def != null;
    }
    public void setCf_defIsSet(boolean value) {
      if (!value) {
        this.cf_def = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case CF_DEF:
        if (value == null) {
          unsetCf_def();
        } else {
          setCf_def((CfDef)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case CF_DEF:
        return getCf_def();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case CF_DEF:
        return isSetCf_def();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof system_update_column_family_args)
        return this.equals((system_update_column_family_args)that);
      return false;
    }
    public boolean equals(system_update_column_family_args that) {
      if (that == null)
        return false;
      boolean this_present_cf_def = true && this.isSetCf_def();
      boolean that_present_cf_def = true && that.isSetCf_def();
      if (this_present_cf_def || that_present_cf_def) {
        if (!(this_present_cf_def && that_present_cf_def))
          return false;
        if (!this.cf_def.equals(that.cf_def))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_cf_def = true && (isSetCf_def());
      builder.append(present_cf_def);
      if (present_cf_def)
        builder.append(cf_def);
      return builder.toHashCode();
    }
    public int compareTo(system_update_column_family_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      system_update_column_family_args typedOther = (system_update_column_family_args)other;
      lastComparison = Boolean.valueOf(isSetCf_def()).compareTo(typedOther.isSetCf_def());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetCf_def()) {
        lastComparison = TBaseHelper.compareTo(this.cf_def, typedOther.cf_def);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRUCT) {
              this.cf_def = new CfDef();
              this.cf_def.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.cf_def != null) {
        oprot.writeFieldBegin(CF_DEF_FIELD_DESC);
        this.cf_def.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("system_update_column_family_args(");
      boolean first = true;
      sb.append("cf_def:");
      if (this.cf_def == null) {
        sb.append("null");
      } else {
        sb.append(this.cf_def);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (cf_def == null) {
        throw new TProtocolException("Required field 'cf_def' was not present! Struct: " + toString());
      }
    }
  }
  public static class system_update_column_family_result implements TBase<system_update_column_family_result, system_update_column_family_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("system_update_column_family_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.STRING, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    public String success;
    public InvalidRequestException ire;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRING)));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(system_update_column_family_result.class, metaDataMap);
    }
    public system_update_column_family_result() {
    }
    public system_update_column_family_result(
      String success,
      InvalidRequestException ire)
    {
      this();
      this.success = success;
      this.ire = ire;
    }
    public system_update_column_family_result(system_update_column_family_result other) {
      if (other.isSetSuccess()) {
        this.success = other.success;
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
    }
    public system_update_column_family_result deepCopy() {
      return new system_update_column_family_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.ire = null;
    }
    public String getSuccess() {
      return this.success;
    }
    public system_update_column_family_result setSuccess(String success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public system_update_column_family_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((String)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case IRE:
        return getIre();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof system_update_column_family_result)
        return this.equals((system_update_column_family_result)that);
      return false;
    }
    public boolean equals(system_update_column_family_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      return builder.toHashCode();
    }
    public int compareTo(system_update_column_family_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      system_update_column_family_result typedOther = (system_update_column_family_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.STRING) {
              this.success = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        oprot.writeString(this.success);
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("system_update_column_family_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
  public static class execute_cql_query_args implements TBase<execute_cql_query_args, execute_cql_query_args._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("execute_cql_query_args");
    private static final TField QUERY_FIELD_DESC = new TField("query", TType.STRING, (short)1);
    private static final TField COMPRESSION_FIELD_DESC = new TField("compression", TType.I32, (short)2);
    public ByteBuffer query;
    public Compression compression;
    public enum _Fields implements TFieldIdEnum {
      QUERY((short)1, "query"),
      COMPRESSION((short)2, "compression");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 1: 
            return QUERY;
          case 2: 
            return COMPRESSION;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.QUERY, new FieldMetaData("query", TFieldRequirementType.REQUIRED, 
          new FieldValueMetaData(TType.STRING)));
      tmpMap.put(_Fields.COMPRESSION, new FieldMetaData("compression", TFieldRequirementType.REQUIRED, 
          new EnumMetaData(TType.ENUM, Compression.class)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(execute_cql_query_args.class, metaDataMap);
    }
    public execute_cql_query_args() {
    }
    public execute_cql_query_args(
      ByteBuffer query,
      Compression compression)
    {
      this();
      this.query = query;
      this.compression = compression;
    }
    public execute_cql_query_args(execute_cql_query_args other) {
      if (other.isSetQuery()) {
        this.query = TBaseHelper.copyBinary(other.query);
;
      }
      if (other.isSetCompression()) {
        this.compression = other.compression;
      }
    }
    public execute_cql_query_args deepCopy() {
      return new execute_cql_query_args(this);
    }
    @Override
    public void clear() {
      this.query = null;
      this.compression = null;
    }
    public byte[] getQuery() {
      setQuery(TBaseHelper.rightSize(query));
      return query.array();
    }
    public ByteBuffer BufferForQuery() {
      return query;
    }
    public execute_cql_query_args setQuery(byte[] query) {
      setQuery(ByteBuffer.wrap(query));
      return this;
    }
    public execute_cql_query_args setQuery(ByteBuffer query) {
      this.query = query;
      return this;
    }
    public void unsetQuery() {
      this.query = null;
    }
    public boolean isSetQuery() {
      return this.query != null;
    }
    public void setQueryIsSet(boolean value) {
      if (!value) {
        this.query = null;
      }
    }
    public Compression getCompression() {
      return this.compression;
    }
    public execute_cql_query_args setCompression(Compression compression) {
      this.compression = compression;
      return this;
    }
    public void unsetCompression() {
      this.compression = null;
    }
    public boolean isSetCompression() {
      return this.compression != null;
    }
    public void setCompressionIsSet(boolean value) {
      if (!value) {
        this.compression = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case QUERY:
        if (value == null) {
          unsetQuery();
        } else {
          setQuery((ByteBuffer)value);
        }
        break;
      case COMPRESSION:
        if (value == null) {
          unsetCompression();
        } else {
          setCompression((Compression)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case QUERY:
        return getQuery();
      case COMPRESSION:
        return getCompression();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case QUERY:
        return isSetQuery();
      case COMPRESSION:
        return isSetCompression();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof execute_cql_query_args)
        return this.equals((execute_cql_query_args)that);
      return false;
    }
    public boolean equals(execute_cql_query_args that) {
      if (that == null)
        return false;
      boolean this_present_query = true && this.isSetQuery();
      boolean that_present_query = true && that.isSetQuery();
      if (this_present_query || that_present_query) {
        if (!(this_present_query && that_present_query))
          return false;
        if (!this.query.equals(that.query))
          return false;
      }
      boolean this_present_compression = true && this.isSetCompression();
      boolean that_present_compression = true && that.isSetCompression();
      if (this_present_compression || that_present_compression) {
        if (!(this_present_compression && that_present_compression))
          return false;
        if (!this.compression.equals(that.compression))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_query = true && (isSetQuery());
      builder.append(present_query);
      if (present_query)
        builder.append(query);
      boolean present_compression = true && (isSetCompression());
      builder.append(present_compression);
      if (present_compression)
        builder.append(compression.getValue());
      return builder.toHashCode();
    }
    public int compareTo(execute_cql_query_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      execute_cql_query_args typedOther = (execute_cql_query_args)other;
      lastComparison = Boolean.valueOf(isSetQuery()).compareTo(typedOther.isSetQuery());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetQuery()) {
        lastComparison = TBaseHelper.compareTo(this.query, typedOther.query);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetCompression()).compareTo(typedOther.isSetCompression());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetCompression()) {
        lastComparison = TBaseHelper.compareTo(this.compression, typedOther.compression);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 1: 
            if (field.type == TType.STRING) {
              this.query = iprot.readBinary();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.I32) {
              this.compression = Compression.findByValue(iprot.readI32());
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      validate();
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.query != null) {
        oprot.writeFieldBegin(QUERY_FIELD_DESC);
        oprot.writeBinary(this.query);
        oprot.writeFieldEnd();
      }
      if (this.compression != null) {
        oprot.writeFieldBegin(COMPRESSION_FIELD_DESC);
        oprot.writeI32(this.compression.getValue());
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("execute_cql_query_args(");
      boolean first = true;
      sb.append("query:");
      if (this.query == null) {
        sb.append("null");
      } else {
        TBaseHelper.toString(this.query, sb);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("compression:");
      if (this.compression == null) {
        sb.append("null");
      } else {
        sb.append(this.compression);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
      if (query == null) {
        throw new TProtocolException("Required field 'query' was not present! Struct: " + toString());
      }
      if (compression == null) {
        throw new TProtocolException("Required field 'compression' was not present! Struct: " + toString());
      }
    }
  }
  public static class execute_cql_query_result implements TBase<execute_cql_query_result, execute_cql_query_result._Fields>, java.io.Serializable, Cloneable   {
    private static final TStruct STRUCT_DESC = new TStruct("execute_cql_query_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.STRUCT, (short)0);
    private static final TField IRE_FIELD_DESC = new TField("ire", TType.STRUCT, (short)1);
    private static final TField UE_FIELD_DESC = new TField("ue", TType.STRUCT, (short)2);
    private static final TField TE_FIELD_DESC = new TField("te", TType.STRUCT, (short)3);
    public CqlResult success;
    public InvalidRequestException ire;
    public UnavailableException ue;
    public TimedOutException te;
    public enum _Fields implements TFieldIdEnum {
      SUCCESS((short)0, "success"),
      IRE((short)1, "ire"),
      UE((short)2, "ue"),
      TE((short)3, "te");
      private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
      static {
        for (_Fields field : EnumSet.allOf(_Fields.class)) {
          byName.put(field.getFieldName(), field);
        }
      }
      public static _Fields findByThriftId(int fieldId) {
        switch(fieldId) {
          case 0: 
            return SUCCESS;
          case 1: 
            return IRE;
          case 2: 
            return UE;
          case 3: 
            return TE;
          default:
            return null;
        }
      }
      public static _Fields findByThriftIdOrThrow(int fieldId) {
        _Fields fields = findByThriftId(fieldId);
        if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
        return fields;
      }
      public static _Fields findByName(String name) {
        return byName.get(name);
      }
      private final short _thriftId;
      private final String _fieldName;
      _Fields(short thriftId, String fieldName) {
        _thriftId = thriftId;
        _fieldName = fieldName;
      }
      public short getThriftFieldId() {
        return _thriftId;
      }
      public String getFieldName() {
        return _fieldName;
      }
    }
    public static final Map<_Fields, FieldMetaData> metaDataMap;
    static {
      Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
      tmpMap.put(_Fields.SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new StructMetaData(TType.STRUCT, CqlResult.class)));
      tmpMap.put(_Fields.IRE, new FieldMetaData("ire", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.UE, new FieldMetaData("ue", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      tmpMap.put(_Fields.TE, new FieldMetaData("te", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRUCT)));
      metaDataMap = Collections.unmodifiableMap(tmpMap);
      FieldMetaData.addStructMetaDataMap(execute_cql_query_result.class, metaDataMap);
    }
    public execute_cql_query_result() {
    }
    public execute_cql_query_result(
      CqlResult success,
      InvalidRequestException ire,
      UnavailableException ue,
      TimedOutException te)
    {
      this();
      this.success = success;
      this.ire = ire;
      this.ue = ue;
      this.te = te;
    }
    public execute_cql_query_result(execute_cql_query_result other) {
      if (other.isSetSuccess()) {
        this.success = new CqlResult(other.success);
      }
      if (other.isSetIre()) {
        this.ire = new InvalidRequestException(other.ire);
      }
      if (other.isSetUe()) {
        this.ue = new UnavailableException(other.ue);
      }
      if (other.isSetTe()) {
        this.te = new TimedOutException(other.te);
      }
    }
    public execute_cql_query_result deepCopy() {
      return new execute_cql_query_result(this);
    }
    @Override
    public void clear() {
      this.success = null;
      this.ire = null;
      this.ue = null;
      this.te = null;
    }
    public CqlResult getSuccess() {
      return this.success;
    }
    public execute_cql_query_result setSuccess(CqlResult success) {
      this.success = success;
      return this;
    }
    public void unsetSuccess() {
      this.success = null;
    }
    public boolean isSetSuccess() {
      return this.success != null;
    }
    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }
    public InvalidRequestException getIre() {
      return this.ire;
    }
    public execute_cql_query_result setIre(InvalidRequestException ire) {
      this.ire = ire;
      return this;
    }
    public void unsetIre() {
      this.ire = null;
    }
    public boolean isSetIre() {
      return this.ire != null;
    }
    public void setIreIsSet(boolean value) {
      if (!value) {
        this.ire = null;
      }
    }
    public UnavailableException getUe() {
      return this.ue;
    }
    public execute_cql_query_result setUe(UnavailableException ue) {
      this.ue = ue;
      return this;
    }
    public void unsetUe() {
      this.ue = null;
    }
    public boolean isSetUe() {
      return this.ue != null;
    }
    public void setUeIsSet(boolean value) {
      if (!value) {
        this.ue = null;
      }
    }
    public TimedOutException getTe() {
      return this.te;
    }
    public execute_cql_query_result setTe(TimedOutException te) {
      this.te = te;
      return this;
    }
    public void unsetTe() {
      this.te = null;
    }
    public boolean isSetTe() {
      return this.te != null;
    }
    public void setTeIsSet(boolean value) {
      if (!value) {
        this.te = null;
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((CqlResult)value);
        }
        break;
      case IRE:
        if (value == null) {
          unsetIre();
        } else {
          setIre((InvalidRequestException)value);
        }
        break;
      case UE:
        if (value == null) {
          unsetUe();
        } else {
          setUe((UnavailableException)value);
        }
        break;
      case TE:
        if (value == null) {
          unsetTe();
        } else {
          setTe((TimedOutException)value);
        }
        break;
      }
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case SUCCESS:
        return getSuccess();
      case IRE:
        return getIre();
      case UE:
        return getUe();
      case TE:
        return getTe();
      }
      throw new IllegalStateException();
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }
      switch (field) {
      case SUCCESS:
        return isSetSuccess();
      case IRE:
        return isSetIre();
      case UE:
        return isSetUe();
      case TE:
        return isSetTe();
      }
      throw new IllegalStateException();
    }
    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof execute_cql_query_result)
        return this.equals((execute_cql_query_result)that);
      return false;
    }
    public boolean equals(execute_cql_query_result that) {
      if (that == null)
        return false;
      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }
      boolean this_present_ire = true && this.isSetIre();
      boolean that_present_ire = true && that.isSetIre();
      if (this_present_ire || that_present_ire) {
        if (!(this_present_ire && that_present_ire))
          return false;
        if (!this.ire.equals(that.ire))
          return false;
      }
      boolean this_present_ue = true && this.isSetUe();
      boolean that_present_ue = true && that.isSetUe();
      if (this_present_ue || that_present_ue) {
        if (!(this_present_ue && that_present_ue))
          return false;
        if (!this.ue.equals(that.ue))
          return false;
      }
      boolean this_present_te = true && this.isSetTe();
      boolean that_present_te = true && that.isSetTe();
      if (this_present_te || that_present_te) {
        if (!(this_present_te && that_present_te))
          return false;
        if (!this.te.equals(that.te))
          return false;
      }
      return true;
    }
    @Override
    public int hashCode() {
      HashCodeBuilder builder = new HashCodeBuilder();
      boolean present_success = true && (isSetSuccess());
      builder.append(present_success);
      if (present_success)
        builder.append(success);
      boolean present_ire = true && (isSetIre());
      builder.append(present_ire);
      if (present_ire)
        builder.append(ire);
      boolean present_ue = true && (isSetUe());
      builder.append(present_ue);
      if (present_ue)
        builder.append(ue);
      boolean present_te = true && (isSetTe());
      builder.append(present_te);
      if (present_te)
        builder.append(te);
      return builder.toHashCode();
    }
    public int compareTo(execute_cql_query_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }
      int lastComparison = 0;
      execute_cql_query_result typedOther = (execute_cql_query_result)other;
      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetSuccess()) {
        lastComparison = TBaseHelper.compareTo(this.success, typedOther.success);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetIre()).compareTo(typedOther.isSetIre());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetIre()) {
        lastComparison = TBaseHelper.compareTo(this.ire, typedOther.ire);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetUe()).compareTo(typedOther.isSetUe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetUe()) {
        lastComparison = TBaseHelper.compareTo(this.ue, typedOther.ue);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetTe()).compareTo(typedOther.isSetTe());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetTe()) {
        lastComparison = TBaseHelper.compareTo(this.te, typedOther.te);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
    public void read(TProtocol iprot) throws TException {
      TField field;
      iprot.readStructBegin();
      while (true)
      {
        field = iprot.readFieldBegin();
        if (field.type == TType.STOP) { 
          break;
        }
        switch (field.id) {
          case 0: 
            if (field.type == TType.STRUCT) {
              this.success = new CqlResult();
              this.success.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 1: 
            if (field.type == TType.STRUCT) {
              this.ire = new InvalidRequestException();
              this.ire.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 2: 
            if (field.type == TType.STRUCT) {
              this.ue = new UnavailableException();
              this.ue.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case 3: 
            if (field.type == TType.STRUCT) {
              this.te = new TimedOutException();
              this.te.read(iprot);
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          default:
            TProtocolUtil.skip(iprot, field.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      validate();
    }
    public void write(TProtocol oprot) throws TException {
      oprot.writeStructBegin(STRUCT_DESC);
      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        this.success.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetIre()) {
        oprot.writeFieldBegin(IRE_FIELD_DESC);
        this.ire.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetUe()) {
        oprot.writeFieldBegin(UE_FIELD_DESC);
        this.ue.write(oprot);
        oprot.writeFieldEnd();
      } else if (this.isSetTe()) {
        oprot.writeFieldBegin(TE_FIELD_DESC);
        this.te.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("execute_cql_query_result(");
      boolean first = true;
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ire:");
      if (this.ire == null) {
        sb.append("null");
      } else {
        sb.append(this.ire);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ue:");
      if (this.ue == null) {
        sb.append("null");
      } else {
        sb.append(this.ue);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("te:");
      if (this.te == null) {
        sb.append("null");
      } else {
        sb.append(this.te);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void validate() throws TException {
    }
  }
}
