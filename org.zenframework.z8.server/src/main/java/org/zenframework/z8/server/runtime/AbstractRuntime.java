package org.zenframework.z8.server.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.base.simple.Procedure;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.types.guid;

public abstract class AbstractRuntime implements IRuntime {
	private Map<String, Table.CLASS<? extends Table>> tableClasses = new HashMap<String, Table.CLASS<? extends Table>>();
	private Map<String, Table.CLASS<? extends Table>> tableNames = new HashMap<String, Table.CLASS<? extends Table>>();
	private Map<guid, Table.CLASS<? extends Table>> tableKeys = new HashMap<guid, Table.CLASS<? extends Table>>();

	private List<OBJECT.CLASS<? extends OBJECT>> entries = new ArrayList<OBJECT.CLASS<? extends OBJECT>>();
	private List<Procedure.CLASS<? extends Procedure>> jobs = new ArrayList<Procedure.CLASS<? extends Procedure>>();

	@Override
	public Collection<Table.CLASS<? extends Table>> tables() {
		return tableClasses.values();
	}

	@Override
	public Collection<guid> tableKeys() {
		return tableKeys.keySet();
	}

	@Override
	public Collection<OBJECT.CLASS<? extends OBJECT>> entries() {
		return entries;
	}

	@Override
	public Collection<Procedure.CLASS<? extends Procedure>> jobs() {
		return jobs;
	}

	@Override
	public Table.CLASS<? extends Table> getTable(String className) {
		return tableClasses.get(className);
	}

	@Override
	public Table.CLASS<? extends Table> getTableByName(String name) {
		return tableNames.get(name);
	}

	@Override
	public Table.CLASS<? extends Table> getTableByKey(guid key) {
		return tableKeys.get(key);
	}

	@Override
	public OBJECT.CLASS<? extends OBJECT> getEntry(String name) {
		return AbstractRuntime.<OBJECT.CLASS<? extends OBJECT>> get(name, entries);
	}

	@Override
	public Procedure.CLASS<? extends Procedure> getJob(String name) {
		return AbstractRuntime.<Procedure.CLASS<? extends Procedure>> get(name, jobs);
	}

	private static <T extends OBJECT.CLASS<? extends OBJECT>> T get(String name, List<T> list) {
		for(T cls : list) {
			if(name.equals(cls.classId()))
				return cls;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected void addTable(Table.CLASS<? extends Table> cls) {
		for(Map.Entry<String, Table.CLASS<? extends Table>> entry : tableClasses.entrySet().toArray(new Map.Entry[0])) {
			Table.CLASS<? extends Table> table = entry.getValue();
			if(table.getClass().isAssignableFrom(cls.getClass())) {
				tableClasses.remove(table.classId());
				tableNames.remove(table.name());
				tableKeys.remove(table.key());
			}
		}
		tableClasses.put(cls.classId(), cls);
		tableNames.put(cls.name(), cls);
		tableKeys.put(cls.key(), cls);
	}

	protected void addEntry(OBJECT.CLASS<? extends OBJECT> cls) {
		for(CLASS<? extends OBJECT> entry : entries) {
			if(entry.classId().equals(cls.classId()))
				return;
		}
		entries.add(cls);
	}

	protected void addJob(Procedure.CLASS<? extends Procedure> cls) {
		for(CLASS<? extends Procedure> job : jobs) {
			if(job.classId().equals(cls.classId()))
				return;
		}
		jobs.add(cls);
	}

	protected void mergeWith(IRuntime runtime) {
		for(Table.CLASS<? extends Table> table : runtime.tables())
			addTable(table);

		for(Procedure.CLASS<? extends Procedure> job : runtime.jobs())
			addJob(job);

		for(OBJECT.CLASS<? extends OBJECT> entry : runtime.entries())
			addEntry(entry);
	}
}
