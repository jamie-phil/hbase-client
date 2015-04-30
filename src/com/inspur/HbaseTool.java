package com.inspur;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.security.UserGroupInformation;

/**
 * Hbase读写操作。
 * 
 * @author chenyanpeng
 * @since 2013-08-22 0.91
 */
public class HbaseTool {

	private static Configuration conf = null;
	private final static String EOL = "\r\n";
	/**
	 * 初始化配置
	 */
	static {
		conf = HBaseConfiguration.create();
		if ("kerberos".equals(conf.get("hadoop.security.authentication")))
			try {
				// 获取kerberos配置文件路径(krb为kerberos配置文件)
				String krbStr = Thread.currentThread().getContextClassLoader()
						.getResource("krb").getFile();
				// 获取用户票据hhxa路径(hhxa为示例用户配置文件)
				String keyStr = Thread.currentThread().getContextClassLoader()
						.getResource("jwqd").getFile();
				// 初始化配置文件
				System.setProperty("java.security.krb5.conf", krbStr);
				// 使用示例用户hhxa登录
				UserGroupInformation.setConfiguration(conf);
				UserGroupInformation.loginUserFromKeytab(
						"jwqd/558536cb-ffff-461b-ab27-cf80edaaafec@POLICE.COM",
						keyStr);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	/**
	 * 插入一行
	 * 
	 * @param tableName
	 *            表名
	 * @param rowId
	 *            行ID
	 * @param family
	 *            列族
	 * @param qualifier
	 *            键值对键
	 * @param value
	 *            键值对值
	 */
	public static String insertRow(String tableName, String rowId,
			String family, String qualifier, String value) {
		try {
			HTable ht = new HTable(conf, tableName.getBytes());
			Put p = new Put(rowId.getBytes());
			p.add(Bytes.toBytes(family), Bytes.toBytes(qualifier),
					Bytes.toBytes(value));
			long msBeg = System.currentTimeMillis();// 起始时间
			ht.put(p);
			long msEnd = System.currentTimeMillis();// 完成时间
			ht.close();
			long duration = msEnd - msBeg;// HBase操作时间差
			return "成功插入行 (" + rowId + ")[HBaseTime:"
					+ (duration < 1 ? "<1" : duration) + "ms]" + EOL + EOL;
		} catch (IOException e) {
			return e.toString();
		}
	}

	/**
	 * 创建表操作
	 * 
	 * @param tableName
	 *            表名
	 * @param cfs
	 *            列族数组
	 * @param maxVersions
	 *            保留的最大版本数
	 * @throws IOException
	 */
	public static String createTable(String tableName, String[] cfs,
			int maxVersions) throws IOException {
		HBaseAdmin admin = new HBaseAdmin(conf);
		String status;
		if (admin.tableExists(tableName)) {
			status = "创建表 " + tableName + " 失败, 因为此表已存在." + EOL + EOL;
		} else {
			HTableDescriptor tableDesc = new HTableDescriptor(tableName);
			for (int i = 0; i < cfs.length; i++) {
				HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(
						cfs[i]);
				hColumnDescriptor.setMaxVersions(maxVersions);// 数据保留版本数
				tableDesc.addFamily(hColumnDescriptor);
			}
			long msBeg = System.currentTimeMillis();// 起始时间
			admin.createTable(tableDesc);
			long msEnd = System.currentTimeMillis();// 完成时间
			long duration = msEnd - msBeg;// HBase操作时间差
			status = "成功创建表 " + tableName + " .[HBaseTime:"
					+ (duration < 1 ? "<1" : duration) + "ms]" + EOL + EOL;
		}
		admin.close();
		return status;
	}

	/**
	 * 删除表操作
	 * 
	 * @param tableName
	 *            表名
	 * @throws IOException
	 */
	public static String deleteTable(String tableName) {
		String status;
		try {
			HBaseAdmin admin = new HBaseAdmin(conf);
			long msBeg = System.currentTimeMillis();// 起始时间
			admin.disableTable(tableName);
			admin.deleteTable(tableName);
			long msEnd = System.currentTimeMillis();// 完成时间
			admin.close();
			long duration = msEnd - msBeg;// HBase操作时间差
			status = "成功删除表 " + tableName + " .[HBaseTime:"
					+ (duration < 1 ? "<1" : duration) + "ms]" + EOL + EOL;
		} catch (MasterNotRunningException e) {
			status = "删除表 " + tableName + " 失败, 因为" + e.toString() + " ." + EOL
					+ EOL;
		} catch (ZooKeeperConnectionException e) {
			status = "删除表 " + tableName + " 失败, 因为" + e.toString() + " ." + EOL
					+ EOL;
		} catch (IOException e) {
			status = "删除表 " + tableName + " 失败, 因为" + e.toString() + " ." + EOL
					+ EOL;
		}
		return status;
	}

	/**
	 * 
	 * @param tableName
	 *            表名
	 * @param puts
	 *            put的list集合
	 */
	public static void batchInsertRow(String tableName, final List<Put> puts) {
		if (puts != null && !puts.isEmpty()) {
			try {
				HTable table = new HTable(conf, tableName);
				table.put(puts);
				table.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 删除一行记录
	 * 
	 * @param tableName
	 *            表名
	 * @param rowkey
	 *            行键
	 * @throws IOException
	 */
	public static String deleteRow(String tableName, String rowkey,
			String family, String qualifier) {
		long duration = 0l;
		if (rowkey != null && !rowkey.isEmpty()) {
			Delete del = new Delete(Bytes.toBytes(rowkey));
			if (qualifier != null && !qualifier.isEmpty()) {
				del.deleteColumn(Bytes.toBytes(family),
						Bytes.toBytes(qualifier));
			}
			try {
				HTable t = new HTable(conf, tableName);
				long msBeg = System.currentTimeMillis();// 起始时间
				t.delete(del);
				long msEnd = System.currentTimeMillis();// 完成时间
				t.close();
				duration = msEnd - msBeg;
			} catch (IOException e) {
				return "删除失败：" + e.getMessage() + EOL + EOL;
			}
		}

		return "已删除 " + rowkey + ". [HBaseTime:"
				+ (duration < 1 ? "<1" : duration) + "ms]" + EOL + EOL;
	}

	/**
	 * 批量删除行
	 * 
	 * @param tableName
	 *            表名
	 * @param rowkey
	 *            行名
	 */
	public static void batchDeleteRow(String tableName, String[] rowkey) {
		List<Delete> lst = new ArrayList<Delete>();
		for (String s : rowkey) {
			if (s != null && !s.isEmpty()) {
				lst.add(new Delete(Bytes.toBytes(s)));
			}
		}
		batchDeleteRow(tableName, lst);
	}

	/**
	 * 批量删除行
	 * 
	 * @param tableName
	 *            表名
	 * @param lst
	 *            删除对象列表
	 */
	public static void batchDeleteRow(String tableName, List<Delete> lst) {
		if (lst != null && !lst.isEmpty()) {
			try {
				HTable t = new HTable(conf, tableName);
				t.delete(lst);
				t.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 查找一行记录
	 * 
	 * @param tableName
	 *            表名
	 * @param rowKey
	 *            行键
	 */
	public static String selectRow(String tableName, String rowKey)
			throws IOException {
		HTable table = new HTable(conf, tableName);
		Get g = new Get(Bytes.toBytes(rowKey));
		long msBeg = System.currentTimeMillis();// 起始时间
		Result rs = table.get(g);
		long msEnd = System.currentTimeMillis();// 完成时间
		String row = rowKey;
		int cnt = 0;
		StringBuilder sb = new StringBuilder();
		if (rs != null) {
			row = Bytes.toString(rs.getRow());
		}
		sb.append("row = " + row + EOL);
		sb.append("\tCOLUMN\t\t\tCELL" + EOL);
		for (KeyValue kv : rs.raw()) {
			sb.append("\t" + Bytes.toString(kv.getFamily()) + ":"
					+ Bytes.toString(kv.getQualifier()) + "\t\ttimestamp="
					+ kv.getTimestamp() + ", value="
					+ Bytes.toString(kv.getValue()) + EOL);
			++cnt;
		}
		long duration = msEnd - msBeg;// HBase操作时间差
		sb.append(cnt + " column(s).[HBaseTime:"
				+ (duration < 1 ? "<1" : duration) + "ms]" + EOL + EOL);
		table.close();
		return sb.toString();
	}

	/**
	 * 查询表中所有行
	 * 
	 * @param tableName
	 *            表名
	 */
	public static void scan(String tableName) {
		scan(tableName, null, null);
	}

	/**
	 * 遍历指定范围内的内容
	 * 
	 * @param tableName
	 *            表名
	 * @param begRow
	 *            起始行的rowkey
	 * @param endRow
	 *            结束行的rowkey(ASCII码中小于此值的行，不包括此行)
	 */
	public static String scan(String tableName, String begRow, String endRow) {
		try {
			HTable table = new HTable(conf, tableName);
			Scan s = new Scan();
			if (begRow != null && !begRow.isEmpty()) {
				s.setStartRow(Bytes.toBytes(begRow));
			}
			if (endRow != null && !endRow.isEmpty()) {
				s.setStopRow(Bytes.toBytes(endRow));
			}
			long msBeg = System.currentTimeMillis();// 起始时间
			ResultScanner rs = table.getScanner(s);
			int count = 0;
			StringBuilder sb = new StringBuilder();
			// sb.append("ROW\t\t\tCOLUMN+CELL" + EOL);
			for (Result r : rs) {
				if (r == null) {
					continue;
				}
				sb.append("row = " + Bytes.toString(r.getRow()) + EOL);
				KeyValue[] rows = r.raw();
				for (KeyValue kv : rows) {
					sb.append("\tcolumn=" + Bytes.toString(kv.getFamily())
							+ ":" + Bytes.toString(kv.getQualifier())
							+ ", timestamp=" + kv.getTimestamp() + ", value="
							+ Bytes.toString(kv.getValue()) + EOL);
				}
				count++;
			}
			table.close();
			long msEnd = System.currentTimeMillis();// 完成时间
			long duration = msEnd - msBeg;// HBase操作时间差

			return "Find " + count + " row(s).[HBaseTime:"
					+ (duration < 1 ? "<1" : duration) + "ms]" + EOL
					+ sb.toString();
		} catch (IOException e) {
			return e.toString();
		}
	}

	/**
	 * 获取所有的表名
	 * 
	 * @return 表名列表
	 */
	public static List<String> getTables() {
		List<String> ret = new ArrayList<String>();
		try {
			HBaseAdmin admin = new HBaseAdmin(conf);
			HTableDescriptor[] htd = admin.listTables();
			if (htd != null) {
				for (HTableDescriptor t : htd) {
					ret.add(t.getNameAsString());
				}
			}
		} catch (MasterNotRunningException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ZooKeeperConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * 查询到某行后，分页显示qualifier和value
	 * 
	 * @param tableName
	 *            表名
	 * @param rowKey
	 *            行名
	 * @param begin
	 *            起始的qualifier下标，第一个元素是0
	 * @param end
	 *            第一个不想要的qualifier下标
	 * @return 数据
	 * @throws IOException
	 */
	public static String batchSelectRow(String tableName, String rowKey,
			int begin, int end) throws IOException {
		HTable table = new HTable(conf, tableName);
		Get g = new Get(rowKey.getBytes());
		int count = 0;
		Result rs = table.get(g);
		StringBuilder sb = new StringBuilder();
		if (rs != null) {
			sb.append("row = " + rowKey + EOL);
			sb.append("COLUMN\t\t\tCELL" + EOL);
			KeyValue[] kvs = rs.raw();
			end = end > rs.size() ? rs.size() : end;

			for (int i = begin; i < end; ++i) {
				sb.append(" " + Bytes.toString(kvs[i].getFamily()) + ":"
						+ Bytes.toString(kvs[i].getQualifier())
						+ "\t\ttimestamp=" + kvs[i].getTimestamp() + ", value="
						+ Bytes.toString(kvs[i].getValue()) + EOL);
				++count;
			}
		}
		sb.append(count + " row(s)." + EOL + EOL);
		table.close();
		return sb.toString();
	}

	private Map<String, String> toMap(Result r) {
		Map<String, String> ret = new HashMap<String, String>();
		if (r != null && !r.isEmpty()) {
			KeyValue[] kv = r.raw();
			for (KeyValue item : kv) {
				byte[] qualifier = item.getQualifier();
				byte[] value = item.getValue();
				ret.put(Bytes.toString(qualifier),
						value == null ? "" : Bytes.toString(value)).trim();
			}
		}
		return ret;
	}
}
