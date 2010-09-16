package me.prettyprint.cassandra.service.spring;

import java.util.List;

import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
import me.prettyprint.cassandra.model.ConsistencyLevelPolicy;
import me.prettyprint.cassandra.model.CountQuery;
import me.prettyprint.cassandra.model.HColumn;
import me.prettyprint.cassandra.model.HSuperColumn;
import me.prettyprint.cassandra.model.IndexedSlicesQuery;
import me.prettyprint.cassandra.model.KeyspaceOperator;
import me.prettyprint.cassandra.model.MultigetSliceQuery;
import me.prettyprint.cassandra.model.MultigetSubSliceQuery;
import me.prettyprint.cassandra.model.MultigetSuperSliceQuery;
import me.prettyprint.cassandra.model.Mutator;
import me.prettyprint.cassandra.model.RangeSlicesQuery;
import me.prettyprint.cassandra.model.RangeSubSlicesQuery;
import me.prettyprint.cassandra.model.RangeSuperSlicesQuery;
import me.prettyprint.cassandra.model.Serializer;
import me.prettyprint.cassandra.model.SliceQuery;
import me.prettyprint.cassandra.model.SubCountQuery;
import me.prettyprint.cassandra.model.SubSliceQuery;
import me.prettyprint.cassandra.model.SuperCountQuery;
import me.prettyprint.cassandra.model.SuperSliceQuery;
import me.prettyprint.cassandra.model.thrift.ThriftColumnQuery;
import me.prettyprint.cassandra.serializers.BytesSerializer;
import me.prettyprint.cassandra.serializers.TypeInferringSerializer;
import me.prettyprint.cassandra.service.CassandraHost;
import me.prettyprint.cassandra.service.Cluster;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.SuperColumnQuery;

import org.apache.cassandra.thrift.Clock;
import org.apache.cassandra.thrift.ColumnPath;
import org.apache.commons.lang.Validate;

/**
 * Implementation of the HectorTemplate
 *
 * @author Bozhidar Bozhanov
 *
 */
public class HectorTemplateImpl implements HectorTemplate {

  private String keyspace;
  private Cluster cluster;
  private KeyspaceOperator keyspaceOperator;

  private ConfigurableConsistencyLevel configurableConsistencyLevelPolicy;
  private String replicationStrategyClass;
  private int replicationFactor;


  public HectorTemplateImpl() {
  }

  public HectorTemplateImpl(Cluster cluster, String keyspace, int replicationFactor, String replicationStrategyClass, ConfigurableConsistencyLevel configurableConsistencyLevelPolicy) {
    this.cluster = cluster;
    this.keyspace = keyspace;
    this.replicationFactor = replicationFactor;
    this.replicationStrategyClass = replicationStrategyClass;
    this.configurableConsistencyLevelPolicy = configurableConsistencyLevelPolicy;
    initKeyspaceOperator();
  }

  public void init() {
    initKeyspaceOperator();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.helenus.HectorFactory#createKeyspaceOperator(java.lang.String,
   * me.prettyprint.cassandra.service.Cluster)
   */
  private void initKeyspaceOperator() {
    ConsistencyLevelPolicy clPolicy;
    if (configurableConsistencyLevelPolicy == null) {
      clPolicy = HFactory.createDefaultConsistencyLevelPolicy();
    } else {
      clPolicy = configurableConsistencyLevelPolicy;
    }
    keyspaceOperator = HFactory.createKeyspaceOperator(keyspace, cluster, clPolicy);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.helenus.HectorFactory#createMutator(me.prettyprint.cassandra.model.
   * KeyspaceOperator, me.prettyprint.cassandra.model.Serializer)
   */
  @Override
  public <K, N, V> Mutator<K> createMutator(Serializer<K> keySerializer) {
    return HFactory.createMutator(keyspaceOperator, keySerializer);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.helenus.HectorFactory#createColumnQuery(me.prettyprint.cassandra.model
   * .KeyspaceOperator, me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer)
   */
  @Override
  public <K, N, V> ColumnQuery<K, N, V> createColumnQuery() {
    return new ThriftColumnQuery<K, N, V>(keyspaceOperator);
  }

  @Override
  public <K, N, V> ColumnQuery<K, N, V> createColumnQuery(Serializer<V> valueSerializer) {
    return new ThriftColumnQuery<K, N, V>(keyspaceOperator, valueSerializer);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.helenus.HectorFactory#createCountQuery(me.prettyprint.cassandra.model
   * .KeyspaceOperator, me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer)
   */
  @Override
  public <K, N> CountQuery<K, N> createCountQuery(Serializer<K> keySerializer,
      Serializer<N> nameSerializer) {
    return HFactory.createCountQuery(keyspaceOperator, keySerializer, nameSerializer);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.helenus.HectorFactory#createSuperCountQuery(me.prettyprint.cassandra
   * .model.KeyspaceOperator, me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer)
   */
  @Override
  public <K, SN> SuperCountQuery<K, SN> createSuperCountQuery(Serializer<K> keySerializer,
      Serializer<SN> superNameSerializer) {
    return HFactory.createSuperCountQuery(keyspaceOperator, keySerializer, superNameSerializer);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.helenus.HectorFactory#createSubCountQuery(me.prettyprint.cassandra.
   * model.KeyspaceOperator, me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer)
   */
  @Override
  public <K, SN, N> SubCountQuery<K, SN, N> createSubCountQuery(Serializer<K> keySerializer,
      Serializer<SN> superNameSerializer, Serializer<N> nameSerializer) {
    return HFactory.createSubCountQuery(keyspaceOperator, keySerializer, superNameSerializer,
        nameSerializer);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.helenus.HectorFactory#createSuperColumnQuery(me.prettyprint.cassandra
   * .model.KeyspaceOperator, me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer)
   */
  @Override
  public <K, SN, N, V> SuperColumnQuery<K, SN, N, V> createSuperColumnQuery(
      Serializer<K> keySerializer, Serializer<SN> sNameSerializer, Serializer<N> nameSerializer,
      Serializer<V> valueSerializer) {
    return HFactory.createSuperColumnQuery(keyspaceOperator, keySerializer, sNameSerializer,
        nameSerializer, valueSerializer);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.helenus.HectorFactory#createMultigetSliceQuery(me.prettyprint.cassandra
   * .model.KeyspaceOperator, me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer)
   */
  @Override
  public <K, N, V> MultigetSliceQuery<K, N, V> createMultigetSliceQuery(
      Serializer<K> keySerializer, Serializer<N> nameSerializer, Serializer<V> valueSerializer) {
    return HFactory.createMultigetSliceQuery(keyspaceOperator, keySerializer, nameSerializer,
        valueSerializer);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.helenus.HectorFactory#createMultigetSuperSliceQuery(me.prettyprint.
   * cassandra.model.KeyspaceOperator,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer)
   */
  @Override
  public <K, SN, N, V> MultigetSuperSliceQuery<K, SN, N, V> createMultigetSuperSliceQuery(
      Serializer<K> keySerializer, Serializer<SN> sNameSerializer, Serializer<N> nameSerializer,
      Serializer<V> valueSerializer) {
    return HFactory.createMultigetSuperSliceQuery(keyspaceOperator, keySerializer, sNameSerializer,
        nameSerializer, valueSerializer);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.helenus.HectorFactory#createMultigetSubSliceQuery(me.prettyprint.cassandra
   * .model.KeyspaceOperator, me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer)
   */
  @Override
  public <K, SN, N, V> MultigetSubSliceQuery<K, SN, N, V> createMultigetSubSliceQuery(
      Serializer<K> keySerializer, Serializer<SN> sNameSerializer, Serializer<N> nameSerializer,
      Serializer<V> valueSerializer) {
    return HFactory.createMultigetSubSliceQuery(keyspaceOperator, keySerializer, sNameSerializer,
        nameSerializer, valueSerializer);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.helenus.HectorFactory#createRangeSlicesQuery(me.prettyprint.cassandra
   * .model.KeyspaceOperator, me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer)
   */
  @Override
  public <K, N, V> RangeSlicesQuery<K, N, V> createRangeSlicesQuery(Serializer<K> keySerializer,
      Serializer<N> nameSerializer, Serializer<V> valueSerializer) {
    return HFactory.createRangeSlicesQuery(keyspaceOperator, keySerializer, nameSerializer,
        valueSerializer);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.helenus.HectorFactory#createRangeSuperSlicesQuery(me.prettyprint.cassandra
   * .model.KeyspaceOperator, me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer)
   */
  @Override
  public <K, SN, N, V> RangeSuperSlicesQuery<K, SN, N, V> createRangeSuperSlicesQuery(
      Serializer<K> keySerializer, Serializer<SN> sNameSerializer, Serializer<N> nameSerializer,
      Serializer<V> valueSerializer) {
    return HFactory.createRangeSuperSlicesQuery(keyspaceOperator, keySerializer, sNameSerializer,
        nameSerializer, valueSerializer);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.helenus.HectorFactory#createRangeSubSlicesQuery(me.prettyprint.cassandra
   * .model.KeyspaceOperator, me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer)
   */
  @Override
  public <K, SN, N, V> RangeSubSlicesQuery<K, SN, N, V> createRangeSubSlicesQuery(
      Serializer<K> keySerializer, Serializer<SN> sNameSerializer, Serializer<N> nameSerializer,
      Serializer<V> valueSerializer) {
    return HFactory.createRangeSubSlicesQuery(keyspaceOperator, keySerializer, sNameSerializer,
        nameSerializer, valueSerializer);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.helenus.HectorFactory#createSliceQuery(me.prettyprint.cassandra.model
   * .KeyspaceOperator, me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer)
   */
  @Override
  public <K, N, V> SliceQuery<K, N, V> createSliceQuery(Serializer<K> keySerializer,
      Serializer<N> nameSerializer, Serializer<V> valueSerializer) {
    return HFactory.createSliceQuery(keyspaceOperator, keySerializer, nameSerializer,
        valueSerializer);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.helenus.HectorFactory#createSubSliceQuery(me.prettyprint.cassandra.
   * model.KeyspaceOperator, me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer)
   */
  @Override
  public <K, SN, N, V> SubSliceQuery<K, SN, N, V> createSubSliceQuery(Serializer<K> keySerializer,
      Serializer<SN> sNameSerializer, Serializer<N> nameSerializer, Serializer<V> valueSerializer) {
    return HFactory.createSubSliceQuery(keyspaceOperator, keySerializer, sNameSerializer,
        nameSerializer, valueSerializer);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.helenus.HectorFactory#createSuperSliceQuery(me.prettyprint.cassandra
   * .model.KeyspaceOperator, me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer)
   */
  @Override
  public <K, SN, N, V> SuperSliceQuery<K, SN, N, V> createSuperSliceQuery(
      Serializer<K> keySerializer, Serializer<SN> sNameSerializer, Serializer<N> nameSerializer,
      Serializer<V> valueSerializer) {
    return HFactory.createSuperSliceQuery(keyspaceOperator, keySerializer, sNameSerializer,
        nameSerializer, valueSerializer);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.helenus.HectorFactory#createSuperColumn(SN, java.util.List,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer)
   */
  @Override
  public <SN, N, V> HSuperColumn<SN, N, V> createSuperColumn(SN name, List<HColumn<N, V>> columns,
      Serializer<SN> superNameSerializer, Serializer<N> nameSerializer,
      Serializer<V> valueSerializer) {
    return HFactory.createSuperColumn(name, columns, createClock(), superNameSerializer,
        nameSerializer, valueSerializer);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.helenus.HectorFactory#createSuperColumn(SN, java.util.List,
   * org.apache.cassandra.thrift.Clock,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer)
   */
  @Override
  public <SN, N, V> HSuperColumn<SN, N, V> createSuperColumn(SN name, List<HColumn<N, V>> columns,
      Clock clock, Serializer<SN> superNameSerializer, Serializer<N> nameSerializer,
      Serializer<V> valueSerializer) {
    return HFactory.createSuperColumn(name, columns, clock, superNameSerializer, nameSerializer,
        valueSerializer);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.helenus.HectorFactory#createColumn(N, V,
   * org.apache.cassandra.thrift.Clock,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer)
   */
  @Override
  public <N, V> HColumn<N, V> createColumn(N name, V value, Clock clock,
      Serializer<N> nameSerializer, Serializer<V> valueSerializer) {
    return HFactory.createColumn(name, value, clock, nameSerializer, valueSerializer);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.helenus.HectorFactory#createColumn(N, V,
   * me.prettyprint.cassandra.model.Serializer,
   * me.prettyprint.cassandra.model.Serializer)
   */
  @Override
  public <N, V> HColumn<N, V> createColumn(N name, V value) {
    return new HColumn<N, V>(name, value, createClock());
  }

  /*
   * (non-Javadoc)
   *
   * @see org.helenus.HectorFactory#createClock()
   */
  @Override
  public Clock createClock() {
    return CassandraHost.DEFAULT_TIMESTAMP_RESOLUTION.createClock();
  }

  // probably should be typed for thrift vs. avro
  <N> ColumnPath createColumnPath(String columnFamilyName, N columnName,
      Serializer<N> nameSerializer) {
    return createColumnPath(columnFamilyName, nameSerializer.toBytes(columnName));
  }

  private <N> ColumnPath createColumnPath(String columnFamilyName, byte[] columnName) {
    Validate.notNull(columnFamilyName, "columnFamilyName cannot be null");
    ColumnPath columnPath = new ColumnPath(columnFamilyName);
    if (columnName != null) {
      columnPath.setColumn(columnName);
    }
    return columnPath;
  }

  <N> ColumnPath createColumnPath(String columnFamilyName) {
    return createColumnPath(columnFamilyName, null);
  }

  <SN, N> ColumnPath createSuperColumnPath(String columnFamilyName, SN superColumnName,
      N columnName, Serializer<SN> superNameSerializer, Serializer<N> nameSerializer) {
    noNullElements(columnFamilyName, superColumnName, superNameSerializer, nameSerializer);
    ColumnPath columnPath = createColumnPath(columnFamilyName, nameSerializer.toBytes(columnName));
    columnPath.setSuper_column(superNameSerializer.toBytes(superColumnName));
    return columnPath;
  }

  <SN> ColumnPath createSuperColumnPath(String columnFamilyName, SN superColumnName,
      Serializer<SN> superNameSerializer) {
    noNullElements(columnFamilyName, superNameSerializer);
    ColumnPath columnPath = createColumnPath(columnFamilyName, null);
    if (superColumnName != null) {
      columnPath.setSuper_column(superNameSerializer.toBytes(superColumnName));
    }
    return columnPath;
  }

  private void noNullElements(Object... elements) {
    Validate.noNullElements(elements);
  }

  @Override
  public <K, N, V> Mutator<K> createMutator() {
    return new Mutator<K>(keyspaceOperator);
  }

  @Override
  public <K, N, V> IndexedSlicesQuery<K, N, V> createIndexSlicesQuery(Serializer<K> keySerializer,
      Serializer<N> nameSerializer, Serializer<V> valueSerializer) {
    return new IndexedSlicesQuery<K, N, V>(keyspaceOperator, keySerializer, nameSerializer,
        valueSerializer);
  }

  @Override
  public <SN, N, V> HSuperColumn<SN, N, V> createSuperColumn(SN name,
      List<HColumn<N, V>> columns) {
    return createSuperColumn(name, columns, TypeInferringSerializer.<SN>get(), TypeInferringSerializer.<N>get(), TypeInferringSerializer.<V>get());
  }

  @Override
  public <K> SliceQuery<K, byte[], byte[]> createSliceQuery() {
    return createSliceQuery(TypeInferringSerializer.<K>get(), BytesSerializer.get(), BytesSerializer.get());
  }


  @Override
  public <K> SuperSliceQuery<K, byte[], byte[], byte[]> createSuperSliceQuery() {
    return createSuperSliceQuery(TypeInferringSerializer.<K>get(), BytesSerializer.get(), BytesSerializer.get(), BytesSerializer.get());
  }

  @Override
  public String getKeyspace() {
    return keyspace;
  }

  public void setKeyspace(String keyspace) {
    this.keyspace = keyspace;
  }

  public KeyspaceOperator getKeyspaceOperator() {
    return keyspaceOperator;
  }

  public void setKeyspaceOperator(KeyspaceOperator keyspaceOperator) {
    this.keyspaceOperator = keyspaceOperator;
  }

  public ConfigurableConsistencyLevel getConfigurableConsistencyLevelPolicy() {
    return configurableConsistencyLevelPolicy;
  }

  public void setConfigurableConsistencyLevelPolicy(
      ConfigurableConsistencyLevel configurableConsistencyLevelPolicy) {
    this.configurableConsistencyLevelPolicy = configurableConsistencyLevelPolicy;
  }

  @Override
  public String getReplicationStrategyClass() {
    return replicationStrategyClass;
  }

  public void setReplicationStrategyClass(String replicationStrategyClass) {
    this.replicationStrategyClass = replicationStrategyClass;
  }

  @Override
  public int getReplicationFactor() {
    return replicationFactor;
  }

  public void setReplicationFactor(int replicationFactor) {
    this.replicationFactor = replicationFactor;
  }

  @Override
  public Cluster getCluster() {
    return cluster;
  }

  public void setCluster(Cluster cluster) {
    this.cluster = cluster;
  }

}