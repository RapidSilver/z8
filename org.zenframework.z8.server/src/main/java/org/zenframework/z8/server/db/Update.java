package org.zenframework.z8.server.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.sql.FormatOptions;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.sql.sql_bool;

public class Update extends Statement {
	private Query query;
	private Collection<Field> fields;

	private guid recordId;
	private sql_bool where;

	public Update(Query query, Collection<Field> fields, guid recordId) {
		this(query, fields, recordId, null);

		if(recordId == null)
			throw new RuntimeException("Update: recordId == null");
	}

	public Update(Query query, Collection<Field> fields, guid recordId, sql_bool where) {
		super(query.getConnection());

		this.query = query;
		this.fields = new ArrayList<Field>();
		this.recordId = recordId;
		this.where = where;

		for(Field field : fields) {
			if(!field.isExpression())
				this.fields.add(field);
		}

		sql = buildSql();
	}

	private String tableName(Query query) {
		DatabaseVendor vendor = vendor();
		return database().tableName(query.name()) + (vendor == DatabaseVendor.SqlServer ? " as " : " ") + query.getAlias();
	}

	private String buildSql() {
		DatabaseVendor vendor = vendor();

		String sql = "update " + tableName(query);

		String set = "";

		for(Field field : fields) {
			if(!field.isPrimaryKey())
				set += (set.isEmpty() ? "" : ", ") + vendor.quote(field.name()) + "=" + "?";
		}

		String where = "";

		if(recordId != null)
			where = vendor.quote(query.primaryKey().name()) + "=?";

		if(this.where != null)
			where += (where.isEmpty() ? "" : " and ") + "(" + this.where.format(vendor, new FormatOptions(), true) + ")";

		sql += " set " + set + (where.isEmpty() ? "" : " where " + where);

		return sql;
	}

	@Override
	public void prepare(String sql, int priority) throws SQLException {
		super.prepare(sql, priority);

		int position = 1;

		for(Field field : fields) {
			if(!field.isPrimaryKey()) {
				set(position, field, field.getDefaultValue());
				position++;
			}
		}

		if(recordId != null)
			set(position, FieldType.Guid, recordId);
	}

	public int execute() {
		try {
			prepare(sql, query.priority());
			return executeUpdate();
		} catch(Throwable e) {
			Trace.logEvent(sql());

			for(Field field : fields)
				Trace.logEvent(field.name() + ": " + field.get());

			Trace.logError(e);

			throw new RuntimeException(e);
		} finally {
			close();
		}
	}
}
