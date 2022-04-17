package com.github.maxopoly.finale.misc.ally;

import java.io.File;
import java.sql.*;
import java.util.logging.Logger;

public class SQLite {

	private Logger logger;
	private File sqlFile;
	private Connection connection = null;

	public SQLite(Logger logger, File sqlFile) {
		this.logger = logger;
		this.sqlFile = sqlFile;
	}

	public Connection open() {
		if (connection != null) {
			return connection;
		}

		try {
			Class.forName("org.sqlite.JDBC");

			this.connection = DriverManager.getConnection("jdbc:sqlite:" + sqlFile.getAbsolutePath());
			logger.info("Connected to SQLite");

			return connection;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void close() {
		if (this.connection != null) {
			try {
				this.connection.close();
				this.connection = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}


}
