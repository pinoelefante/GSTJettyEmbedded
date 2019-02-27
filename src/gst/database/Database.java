package gst.database;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javafx.util.Pair;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableUtils;

/* Using http://ormlite.com/ */
public class Database
{

	public final static Object INSERT_OK = new Object();
	private static Database	   instance;

	public static Database GetInstance()
	{
		if (instance == null)
		{
			try
			{
				instance = new Database();
			}
			catch (SQLException ex)
			{
				Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
				System.exit(-1);
			}
		}
		return instance;
	}

	private ConnectionSource connectionSource;

	private Database() throws SQLException
	{
		String databaseUrl = "jdbc:sqlite:gstnew.sqlite";
		connectionSource = new JdbcPooledConnectionSource(databaseUrl);
	}

	public HashMap<Class<?>, Dao> daos = new HashMap<>();

	public void CreateDB(Class<?>... classList)
	{
		try
		{
			for (Class<?> cur : classList)
			{
				daos.put(cur, DaoManager.createDao(connectionSource, cur));
				TableUtils.createTableIfNotExists(connectionSource, cur);
			}
		}
		catch (SQLException e)
		{
			System.out.println("Can't create database: " + e.getSQLState());
			System.exit(-1);
		}
	}

	private Dao GetDao(Class<?> c)
	{
		return daos.get(c);
	}

	public QueryBuilder GetQueryBuilder(Class c)
	{
		return GetDao(c).queryBuilder();
	}

	public <ID, R> R SelectById(Class<R> c, ID id)
	{
		try
		{
			return c.cast(GetDao(c).queryForId(id));
		}
		catch (SQLException ex)
		{
			Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	public <R> List<R> SelectAll(Class<R> c)
	{
		try
		{
			return GetDao(c).queryForAll();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			// Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	public <R> List<R> SelectWhere(Class<R> c, Pair<String, Object>... where)
	{
		try
		{
			Map<String, Object> where_clausole = get_map_from_pairs(where);
			return GetDao(c).queryForFieldValuesArgs(where_clausole);
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			// Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	public <R> List<R> SelectWhereOrder(Class<R> c, Pair<String, Object>[] where, Pair<String, String>[] order)
	{
		try
		{
			QueryBuilder qb = GetDao(c).queryBuilder();

			if (where != null && where.length > 0)
			{
				Where where_qb = qb.where();

				for (int i = 0; i < where.length; i++)
				{
					Pair<String, Object> wh = where[i];
					if (i > 0)
					{
						where_qb.and();
					}
					where_qb.eq(wh.getKey(), wh.getValue());
				}
				qb.setWhere(where_qb);
			}

			if (order != null)
			{
				for (Pair<String, String> or_by : order)
				{
					qb.orderBy(or_by.getKey(), or_by.getValue().compareTo("ASC") == 0);
				}
			}

			return qb.query();
		}
		catch (SQLException ex)
		{
			Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	public <R> R SelectFirstWhere(Class<R> c, Pair<String, Object>... where)
	{
		List<R> list = SelectWhere(c, where);
		if (list != null && !list.isEmpty())
		{
			return list.get(0);
		}
		return null;
	}

	public <R> R SelectLastWhere(Class<R> c, Pair<String, Object>... where)
	{
		List<R> list = SelectWhere(c, where);
		if (list != null && !list.isEmpty())
		{
			return list.get(list.size() - 1);
		}
		return null;
	}

	private Map<String, Object> get_map_from_pairs(Pair<String, Object>... where)
	{
		HashMap<String, Object> map = new HashMap<>(where.length);
		for (Pair<String, Object> item : where)
		{
			map.put(item.getKey(), item.getValue());
		}
		return map;
	}

	public <T> DatabaseSave SaveItem(T item)
	{
		return SaveItem(item, null);
	}

	public <T> DatabaseSave SaveItem(T item, Runnable runnable)
	{
		if (item != null)
		{
			DatabaseSave db = new DatabaseSave(item.getClass(), item, false, runnable);
			save(db);
			return db;
		}
		return null;
	}

	public <T> DatabaseSave SaveList(Class<?> c, List<T> item, Runnable runnable)
	{
		if (item != null && item.size() > 0)
		{
			DatabaseSave db = new DatabaseSave(c, item, true, runnable);
			save(db);
			return db;
		}
		return null;
	}

	public <T> DatabaseSave Union(Class<?> c, List<T> items)
	{

		return null;
	}

	public <T> DatabaseSave SaveList(Class<?> c, List<T> item)
	{
		return SaveList(c, item, null);
	}

	private void save(DatabaseSave db_save)
	{
		try
		{
			Dao dao = GetDao(db_save.getContentClass());
			DatabaseConnection db_conn = dao.startThreadConnection();
			dao.startThreadConnection().setAutoCommit(false);
			dao.setAutoCommit(db_conn, false);
			boolean delete = db_save.isDelete();
			if (db_save.isCollection())
			{
				List<?> items = (List<?>) db_save.getContent();
				if (delete)
				{
					dao.delete(items);
				}
				else
				{

					for (int i = 0; i < items.size(); i++)
					{
						dao.createOrUpdate(items.get(i));
					}
				}
			}
			else
			{
				if (delete)
				{
					dao.delete(db_save.getContent());
				}
				else
				{
					// System.out.println("Saving:
					// "+db_save.getContent().getClass().getName());
					dao.createOrUpdate(db_save.getContent());
				}
			}
			dao.commit(db_conn);
			dao.endThreadConnection(db_conn);
			db_save.setComplete(true);
			if(db_save.getRunnable() != null)
				db_save.getRunnable().run();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			db_save.setFail(true);
		}

	}
	public <T> DatabaseSave DeleteItem(T item)
	{
		return DeleteItem(item, null);
	}
	public <T> DatabaseSave DeleteItem(T item, Runnable runnable)
	{
		if (item != null)
		{
			DatabaseSave db = new DatabaseSave(item.getClass(), item, false, runnable);
			db.setDelete(true);
			save(db);
			return db;
		}
		return null;
	}

	public <T> DatabaseSave DeleteList(Class<?> c, List<T> items, Runnable runnable)
	{
		if (items != null && items.size() > 0)
		{
			DatabaseSave db = new DatabaseSave(c, items, true, runnable);
			db.setDelete(true);
			save(db);
			return db;
		}
		return null;
	}
}
