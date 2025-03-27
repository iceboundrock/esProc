package com.esproc.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.scudata.app.config.ConfigUtil;
import com.scudata.app.config.RaqsoftConfig;
import com.scudata.common.ArgumentTokenizer;
import com.scudata.common.Escape;
import com.scudata.common.IOUtils;
import com.scudata.common.Logger;
import com.scudata.common.StringUtils;
import com.scudata.dm.Env;
import com.scudata.dm.LocalFile;
import com.scudata.util.Variant;

/**
 * esProc jdbc驱动类，实现了java.sql.Driver。 URL参数如下:
 * config=raqsoftConfig.xml指定配置文件名称。配置文件只会加载一次。
 * onlyserver=true/false。true在服务器执行，false先在本地执行，找不到时在配置的服务器上执行。
 * jobVars=varname1:value1,varname2:value2,...设置任务变量(JobSpace)
 * debugmode=true/false。true会输出调试信息，false不输出调试信息
 * compatiblesql=true/false。简单SQL现在以$开头，true时兼容不以$开头的。兼容一段时间后取消此选项。
 */
public class InternalDriver implements java.sql.Driver, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public InternalDriver() {
		JDBCUtil.log("InternalDriver()");
	}

	/**
	 * Statically register driver class
	 */
	static {
		try {
			DriverManager.registerDriver(new com.esproc.jdbc.InternalDriver());
		} catch (SQLException e) {
			throw new RuntimeException(JDBCMessage.get().getMessage(
					"error.cantregist"), e);
		}
	}

	/**
	 * Attempts to make a database connection to the given URL. The driver
	 * should return "null" if it realizes it is the wrong kind of driver to
	 * connect to the given URL. This will be common, as when the JDBC driver
	 * manager is asked to connect to a given URL it passes the URL to each
	 * loaded driver in turn.
	 * 
	 * @param url
	 *            the URL of the database to which to connect
	 * @param info
	 *            a list of arbitrary string tag/value pairs as connection
	 *            arguments.
	 */
	public Connection connect(String url, Properties info) throws SQLException {
		JDBCUtil.log("InternalDriver.connect(" + url + "," + info + ")");
		return connect(url, info, null);
	}

	/**
	 * Extended connection function. Added RaqsoftConfig as a parameter
	 * 
	 * @param url
	 *            the URL of the database to which to connect
	 * @param info
	 *            a list of arbitrary string tag/value pairs as connection
	 *            arguments.
	 * @param rc
	 *            The RaqsoftConfig object
	 * @return Connection
	 * @throws SQLException
	 */
	public Connection connect(String url, Properties info, RaqsoftConfig rc)
			throws SQLException {
		if (!acceptsURL(url)) {
			// The URL format is incorrect. Expected: {0}.
			throw new SQLException(JDBCMessage.get().getMessage(
					"jdbcdriver.incorrecturl", getDemoUrl()));
		}
		Properties props = getProperties(url, info);
		String sconfig = props.getProperty(KEY_CONFIG);
		String sonlyServer = props.getProperty(KEY_ONLY_SERVER);
		String sjobVars = props.getProperty(KEY_JOB_VARS);
		String sdebugmode = props.getProperty(KEY_DEBUGMODE);
		String scompatiblesql = props.getProperty(KEY_COMPATIBLESQL);
		boolean isOnlyServer = false;
		if (StringUtils.isValidString(sonlyServer))
			try {
				isOnlyServer = Boolean.valueOf(sonlyServer);
			} catch (Exception e) {
				Logger.warn("Invalid onlyServer parameter: " + sonlyServer);
			}
		JDBCUtil.log(KEY_ONLY_SERVER + "=" + isOnlyServer);

		Map<String, Object> jobVars = null;
		if (StringUtils.isValidString(sjobVars)) {
			// 设置任务变量
			// jobVars=varname1:value1,varname2:value2,...
			ArgumentTokenizer at = new ArgumentTokenizer(sjobVars);
			while (at.hasMoreTokens()) {
				String pv = at.nextToken();
				if (!StringUtils.isValidString(pv))
					continue;
				int index = pv.indexOf(":");
				if (index < 1 || index == pv.length() - 1)
					continue;
				String paramName = pv.substring(0, index);
				String valueStr = pv.substring(index + 1, pv.length());
				if (!StringUtils.isValidString(paramName))
					continue;
				paramName = paramName.trim();
				Object value = null;
				if (StringUtils.isValidString(valueStr)) {
					value = Variant.parse(valueStr);
				}
				if (jobVars == null)
					jobVars = new HashMap<String, Object>();
				jobVars.put(paramName, value);
			}
		}
		JDBCUtil.log(KEY_JOB_VARS + "=" + sjobVars);

		boolean isDebugMode = false;
		if (StringUtils.isValidString(sdebugmode)) {
			try {
				isDebugMode = Boolean.valueOf(sdebugmode);
			} catch (Exception e) {
			}
		}
		JDBCUtil.isDebugMode = isDebugMode;

		boolean isCompatiblesql = false;
		if (StringUtils.isValidString(scompatiblesql)) {
			try {
				isCompatiblesql = Boolean.valueOf(scompatiblesql);
			} catch (Exception e) {
			}
		}
		JDBCUtil.isCompatiblesql = isCompatiblesql;

		initConfig(rc, sconfig);
		InternalConnection con = newConnection(config, hostNames, jobVars);
		if (con != null) {
			con.setUrl(url);
			con.setClientInfo(info);
			con.setOnlyServer(isOnlyServer);
		}
		return con;
	}

	/**
	 * Retrieves whether the driver thinks that it can open a connection to the
	 * given URL. Typically drivers will return true if they understand the
	 * sub-protocol specified in the URL and false if they do not.
	 * 
	 * @param url
	 *            the URL of the database
	 * @return true if this driver understands the given URL; false otherwise
	 */
	public boolean acceptsURL(String url) throws SQLException {
		JDBCUtil.log("InternalDriver.acceptsURL(" + url + ")");
		if (url == null) {
			return false;
		}
		return url.toLowerCase().startsWith(getAcceptUrl());
	}

	/**
	 * 可接受的URL
	 */
	protected String getAcceptUrl() {
		return "jdbc:esproc:local:";
	}

	/**
	 * 示例URL
	 */
	protected String getDemoUrl() {
		return "jdbc:esproc:local://";
	}

	/**
	 * Gets information about the possible properties for this driver.
	 * 
	 * @param url
	 *            the URL of the database to which to connect
	 * @param info
	 *            a proposed list of tag/value pairs that will be sent on
	 *            connect open
	 * @return an array of DriverPropertyInfo objects describing possible
	 *         properties. This array may be an empty array if no properties are
	 *         required.
	 */
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			throws SQLException {
		JDBCUtil.log("InternalDriver.getPropertyInfo(" + url + "," + info + ")");
		Properties props = getProperties(url, info);
		DriverPropertyInfo[] dpis = new DriverPropertyInfo[3];
		dpis[0] = new DriverPropertyInfo(KEY_CONFIG,
				props.getProperty(KEY_CONFIG));
		dpis[1] = new DriverPropertyInfo(KEY_ONLY_SERVER,
				props.getProperty(KEY_ONLY_SERVER));
		dpis[2] = new DriverPropertyInfo(KEY_JOB_VARS,
				props.getProperty(KEY_JOB_VARS));
		return dpis;
	}

	/**
	 * Retrieves the driver's major version number. Initially this should be 1.
	 * 
	 * @return this driver's major version number
	 */
	public int getMajorVersion() {
		JDBCUtil.log("InternalDriver.getMajorVersion()");
		return 1;
	}

	/**
	 * Gets the driver's minor version number. Initially this should be 0.
	 * 
	 * @return this driver's minor version number
	 */
	public int getMinorVersion() {
		JDBCUtil.log("InternalDriver.getMinorVersion()");
		return 0;
	}

	/**
	 * Reports whether this driver is a genuine JDBC Compliant driver. A driver
	 * may only report true here if it passes the JDBC compliance tests;
	 * otherwise it is required to return false.
	 * 
	 * @return true if this driver is JDBC Compliant; false otherwise
	 */
	public boolean jdbcCompliant() {
		JDBCUtil.log("InternalDriver.jdbcCompliant()");
		return true;
	}

	/**
	 * Return the parent Logger of all the Loggers used by this driver. This
	 * should be the Logger farthest from the root Logger that is still an
	 * ancestor of all of the Loggers used by this driver. Configuring this
	 * Logger will affect all of the log messages generated by the driver. In
	 * the worst case, this may be the root Logger.
	 * 
	 * 
	 * @return the parent Logger for this driver
	 */
	public java.util.logging.Logger getParentLogger()
			throws SQLFeatureNotSupportedException {
		JDBCUtil.log("InternalDriver.getParentLogger()");
		Logger.debug(JDBCMessage.get().getMessage("error.methodnotimpl",
				"getParentLogger()"));
		return null;
	}

	/**
	 * 创建连接对象
	 * 
	 * @return
	 * @throws SQLException
	 */
	protected InternalConnection newConnection(RaqsoftConfig config,
			List<String> hostNames, Map<String, Object> jobVars)
			throws SQLException {
		InternalConnection con = new InternalConnection(this, config,
				hostNames, jobVars) {

			private static final long serialVersionUID = 1L;

			public void close() throws SQLException {
				super.close();
				reduceConnectionCount();
			}

			public void checkExec() throws SQLException {
				checkRunState();
			}
		};
		addConnectionCount();
		return con;
	}

	protected void checkRunState() throws SQLException {
	}

	protected Properties getProperties(String url, Properties info) {
		Properties props = new Properties();
		if (info != null) {
			props.putAll(info);
		}
		if (url != null) {
			List<String> paramNames = getValidParamNames();
			String[] parts = url.split("&");
			for (String part : parts) {
				for (String paramName : paramNames) {
					int index = part.toLowerCase().indexOf(paramName + "=");
					if (index < 0)
						continue;
					String key = part.substring(index,
							index + paramName.length());
					String value = part.substring(index + paramName.length()
							+ 1);
					try {
						key = URLDecoder.decode(key,
								StandardCharsets.UTF_8.toString());
						value = URLDecoder.decode(value,
								StandardCharsets.UTF_8.toString());
					} catch (Exception ex) {
						Logger.warn("Invalid URL parameter: " + part);
						continue;
					}
					props.put(key, value);
				}
			}
		}
		return props;
	}

	protected boolean isValidParamName(String paramName) {
		return getValidParamNames().contains(paramName.toLowerCase());
	}

	private List<String> paramNames = null;

	protected List<String> getValidParamNames() {
		if (paramNames == null) {
			paramNames = new ArrayList<String>();
			paramNames.add(KEY_CONFIG.toLowerCase());
			paramNames.add(KEY_ONLY_SERVER.toLowerCase());
			paramNames.add(KEY_DEBUGMODE.toLowerCase());
			paramNames.add(KEY_COMPATIBLESQL.toLowerCase());
			paramNames.add(KEY_JOB_VARS.toLowerCase());
		}
		return paramNames;
	}

	/**
	 * The host names
	 */
	protected List<String> hostNames = new ArrayList<String>();
	/**
	 * The RaqsoftConfig object
	 */
	protected RaqsoftConfig config = null;

	/**
	 * The default name of the configuration file
	 */
	private static final String CONFIG_FILE = "raqsoftConfig.xml";

	protected String currentConfig = null;

	/**
	 * Initialize the configuration file
	 * 
	 * @param rc
	 *            The RaqsoftConfig object
	 * @throws SQLException
	 */
	protected synchronized void initConfig(RaqsoftConfig rc, String sconfig)
			throws SQLException {
		if (rc != null) { // DQL API加载的
			this.config = rc;
			try {
				ConfigUtil.setConfig(Env.getApplication(),
						System.getProperty("start.home"), config, true, false,
						true);
			} catch (Exception e) {
				throw new SQLException(e);
			}
		} else {
			loadConfig(sconfig);
		}
	}

	/**
	 * Load configuration file
	 * 
	 * @throws SQLException
	 */
	protected boolean loadConfig(String sconfig) throws SQLException {
		if (config != null) {
			if (StringUtils.isValidString(sconfig))
				if (currentConfig == null
						|| !currentConfig.equalsIgnoreCase(sconfig)) { // 通过API加载过了
					Logger.info(JDBCMessage.get().getMessage(
							"server.configloadonce"));
				}
			return false;
		}
		InputStream is = null;
		String fileName = sconfig;
		try {
			if (StringUtils.isValidString(sconfig)) {
				sconfig = sconfig.trim();
				if (sconfig.startsWith("\"") && sconfig.endsWith("\"")) {
					sconfig = Escape.removeEscAndQuote(sconfig, '"');
				} else if (sconfig.startsWith("'") && sconfig.endsWith("'")) {
					sconfig = sconfig.substring(1, sconfig.length() - 1);
				}
				fileName = sconfig;
				LocalFile lf = new LocalFile(fileName, "s");
				is = lf.getInputStream();
			} else {
				is = findResource(CONFIG_FILE);
				fileName = CONFIG_FILE;
			}
		} catch (Exception ex) {
		}
		if (is != null) {
			try {
				config = ConfigUtil.load(is, true, true);
				currentConfig = sconfig;
				Logger.info(JDBCMessage.get().getMessage("error.configloaded",
						fileName));
				Logger.debug("parallelNum=" + config.getParallelNum());
			} catch (Exception e) {
				String errorMessage = JDBCMessage.get().getMessage(
						"error.loadconfigerror", fileName);
				Logger.error(errorMessage);
				e.printStackTrace();
				throw new SQLException(errorMessage + " : " + e.getMessage(), e);
			} finally {
				if (is != null)
					try {
						is.close();
					} catch (IOException e) {
					}
			}
		} else {
			String errorMessage = JDBCMessage.get().getMessage(
					"error.confignotfound", fileName);
			Logger.error(errorMessage);
			if (StringUtils.isValidString(sconfig)) {
				// URL指定的config加载出错时抛异常，默认加载类路径下的不抛异常
				throw new SQLException(errorMessage);
			}
		}
		hostNames = new ArrayList<String>();
		if (config != null) {
			// 读取分机
			List<String> units = config.getUnitList();
			if (units != null && !units.isEmpty()) {
				for (String unit : units)
					hostNames.add(unit);
			}
			// 加载日志配置(*.properties)
			Properties sps = config.getServerProperties();
			if (sps != null) {
				String logConfig = sps.getProperty("logConfig");
				if (StringUtils.isValidString(logConfig)) {
					logConfig = logConfig.trim();
					InputStream lcis = null;
					try {
						LocalFile logFile = new LocalFile(logConfig, "s");
						if (logFile.exists()) {
							lcis = logFile.getInputStream();
							if (lcis != null) {
								Properties p = new Properties();
								p.load(lcis);
								Logger.setPropertyConfig(p);
								// Log configuration file: {0} loaded.
								Logger.debug(JDBCMessage.get().getMessage(
										"internaldriver.loadlogconfig",
										logConfig));
							}
						} else {
							Logger.debug(JDBCMessage.get().getMessage(
									"internaldriver.logconfignotfound",
									logConfig));
						}
					} catch (Exception e1) {
						// Log configuration file: {0} failed to load.
						Logger.debug(JDBCMessage.get()
								.getMessage(
										"internaldriver.loadlogconfigfailed",
										logConfig));
						Logger.error(e1);
					} finally {
						if (lcis != null) {
							try {
								lcis.close();
							} catch (Exception e) {
							}
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Get input stream by file name
	 * 
	 * @param fileName
	 *            The file name
	 * @return the input stream
	 */
	protected InputStream findResource(String fileName) {
		InputStream in = null;
		if (in == null) {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			if (cl != null) {
				try {
					URL url = cl.getResource(fileName);
					if (url != null) {
						try {
							in = url.openStream();
							if (in != null) {
								Logger.info("JDBC config load from : "
										+ url.toString());
							}
						} catch (Exception e) {
						}
					}
				} catch (Exception e) {
				}
			}
		}
		if (in == null) {
			try {
				URL url = IOUtils.class.getResource(fileName);
				if (url != null) {
					try {
						in = url.openStream();
						Logger.info("JDBC config load from : " + url.toString());
					} catch (Exception e) {
					}
				}
			} catch (Exception e) {
			}
		}
		return in;
	}

	protected int connectionCount = 0;

	protected Object countLock = new Object();

	protected void addConnectionCount() {
		synchronized (countLock) {
			connectionCount = connectionCount + 1;
		}
	}

	protected void reduceConnectionCount() {
		synchronized (countLock) {
			connectionCount = connectionCount - 1;
		}
	}

	private static final String KEY_CONFIG = "config";
	private static final String KEY_ONLY_SERVER = "onlyServer";
	private static final String KEY_JOB_VARS = "jobVars";

	// 仅调试用
	private static final String KEY_DEBUGMODE = "debugmode";
	// 兼容之前简单SQL没有$开头时的用法
	private static final String KEY_COMPATIBLESQL = "compatiblesql";

}
