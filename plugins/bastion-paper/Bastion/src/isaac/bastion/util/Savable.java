package isaac.bastion.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

import isaac.bastion.storage.Database;

public abstract class Savable {
	protected int id;
	protected boolean modified = true;
	protected boolean ghost = false;

	public Savable(int newId) {
		id = newId;
	}

	public Savable(Database databe, int newId) {
		id = newId;
	}
	public int close() {
		ghost = true;

		return 0;
	}

	public static int saveAll() {
		Map<Integer, Savable> elements = getElements();
		for (int i = 1; i <= getMaxId(); ++i) {
			Savable element = elements.get(i);
			if (element != null && element.modified) {
				element.save();
			}
		}
		return 0;
	}

	public int load(ResultSet set) {
		try {
			set.getString(getPrefix()+"_id");
			modified=false;
			return 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return 1;
		}
	}

	public int add() {
		Map<Integer, Savable> elements = getElements();
		elements.put(id, this);
		return 0;

	}

	public static int remove(int id) {
		Map<Integer, Savable> elements = getElements();
		Savable toRemove = elements.get(id);
		if (toRemove != null) {
			toRemove.close();
			elements.remove(id);
			return 0;
		}

		return 1;
	}


	abstract protected int getNewId();
	abstract protected int save();
	protected static Map<Integer, Savable> getElements() {
		throw new IllegalStateException(
				"getElements hasn't been set up in the subclass");
	}

	protected static int getMaxId() {
		throw new IllegalStateException(
				"getMaxId hasn't been set up in the subclass");
	}
	protected static String getPrefix() {
		throw new IllegalStateException(
				"getMaxId hasn't been set up in the subclass");
	}
	public static Set<String> getFields(){
		Set<String> result=new TreeSet();
		result.add(getPrefix()+"_id int(10)  unsigned NOT NULL AUTO_INCREMENT");
		return result;
	}

}
