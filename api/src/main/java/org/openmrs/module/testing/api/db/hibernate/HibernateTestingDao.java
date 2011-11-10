/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.testing.api.db.hibernate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.testing.api.db.TestingDao;
import org.openmrs.util.OpenmrsConstants;

/**
 * Hibernate specific implementation for {@link TestingDao}.
 */
public class HibernateTestingDao implements TestingDao {
	
	protected Logger log = Logger.getLogger(getClass());
	
	private SessionFactory sessionFactory;
	
	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public void generateTestDataSet(OutputStream os) throws DAOException {
		ZipOutputStream zip = null;
		try {
			zip = new ZipOutputStream(os);
			zip.putNextEntry(new ZipEntry("data.txt"));
			
			OutputStreamWriter osWriter = new OutputStreamWriter(zip, "UTF-8");
			PrintWriter out = new PrintWriter(osWriter);
			
			// Write the DDL Header as mysqldump does
			out.println("-- ------------------------------------------------------");
			out.println("-- Database dump with test data");
			out.println("-- OpenMRS Version: " + OpenmrsConstants.OPENMRS_VERSION);
			out.println("-- ------------------------------------------------------");
			out.println("");
			out.println("/*!40101 SET CHARACTER_SET_CLIENT=utf8 */;");
			out.println("/*!40101 SET NAMES utf8 */;");
			out.println("/*!40103 SET TIME_ZONE='+00:00' */;");
			out.println("/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;");
			out.println("/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;");
			out.println("/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;");
			out.println("/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;");
			out.println("/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;");
			out.println("/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;");
			out.println("/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;");
			out.println("/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;");
			out.println("");
			
			List<String> tablesToDump = new ArrayList<String>();
			Session session = sessionFactory.getCurrentSession();
			String schema = (String) session.createSQLQuery("select schema()").uniqueResult();
			log.warn("schema: " + schema);
			// Get all tables that we'll need to dump
			{
				Query query = session
				        .createSQLQuery("SELECT tabs.table_name FROM INFORMATION_SCHEMA.TABLES tabs WHERE tabs.table_schema = '"
				                + schema + "'");
				for (Object tn : query.list()) {
					String tableName = (String) tn;
					tablesToDump.add(tableName);
				}
			}
			log.warn("tables to dump: " + tablesToDump);
			
			@SuppressWarnings("deprecation")
			Connection conn = sessionFactory.getCurrentSession().connection();
			try {
				Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				
				for (String tableName : tablesToDump) {
					out.println();
					out.println("--");
					out.println("-- Table structure for table `" + tableName + "`");
					out.println("--");
					out.println("DROP TABLE IF EXISTS `" + tableName + "`;");
					out.println("SET @saved_cs_client     = @@character_set_client;");
					out.println("SET character_set_client = utf8;");
					ResultSet rs = st.executeQuery("SHOW CREATE TABLE " + tableName);
					while (rs.next()) {
						out.println(rs.getString("Create Table") + ";");
					}
					out.println("SET character_set_client = @saved_cs_client;");
					out.println();
					
					{
						out.println("-- Dumping data for table `" + tableName + "`");
						out.println("LOCK TABLES `" + tableName + "` WRITE;");
						out.println("/*!40000 ALTER TABLE `" + tableName + "` DISABLE KEYS */;");
						boolean first = true;
						
						rs = st.executeQuery("SELECT * FROM " + tableName);
						ResultSetMetaData md = rs.getMetaData();
						int numColumns = md.getColumnCount();
						int rowNum = 0;
						boolean insert = false;
						
						while (rs.next()) {
							if (rowNum == 0) {
								insert = true;
								out.print("INSERT INTO `" + tableName + "` VALUES ");
							}
							++rowNum;
							if (first) {
								first = false;
							} else {
								out.print(", ");
							}
							if (rowNum % 20 == 0) {
								out.println();
							}
							out.print("(");
							for (int i = 1; i <= numColumns; ++i) {
								if (i != 1) {
									out.print(",");
								}
								if (rs.getObject(i) == null) {
									out.print("NULL");
								} else {
									switch (md.getColumnType(i)) {
										case Types.VARCHAR:
										case Types.CHAR:
										case Types.LONGVARCHAR:
											out.print("'");
											out.print(rs.getString(i).replaceAll("\n", "\\\\n").replaceAll("'", "\\\\'"));
											out.print("'");
											break;
										case Types.BIGINT:
										case Types.DECIMAL:
										case Types.NUMERIC:
											out.print(rs.getBigDecimal(i));
											break;
										case Types.BIT:
											out.print(rs.getBoolean(i));
											break;
										case Types.INTEGER:
										case Types.SMALLINT:
										case Types.TINYINT:
											out.print(rs.getInt(i));
											break;
										case Types.REAL:
										case Types.FLOAT:
										case Types.DOUBLE:
											out.print(rs.getDouble(i));
											break;
										case Types.BLOB:
										case Types.VARBINARY:
										case Types.LONGVARBINARY:
											Blob blob = rs.getBlob(i);
											out.print("'");
											InputStream in = blob.getBinaryStream();
											while (true) {
												int b = in.read();
												if (b < 0) {
													break;
												}
												char c = (char) b;
												if (c == '\'') {
													out.print("\'");
												} else {
													out.print(c);
												}
											}
											out.print("'");
											break;
										case Types.CLOB:
											out.print("'");
											out.print(rs.getString(i).replaceAll("\n", "\\\\n").replaceAll("'", "\\\\'"));
											out.print("'");
											break;
										case Types.DATE:
											out.print("'" + rs.getDate(i) + "'");
											break;
										case Types.TIMESTAMP:
											out.print("'" + rs.getTimestamp(i) + "'");
											break;
										default:
											throw new RuntimeException("TODO: handle type code " + md.getColumnType(i)
											        + " (name " + md.getColumnTypeName(i) + ")");
									}
								}
							}
							out.print(")");
						}
						if (insert) {
							out.println(";");
							insert = false;
						}
						
						out.println("/*!40000 ALTER TABLE `" + tableName + "` ENABLE KEYS */;");
						out.println("UNLOCK TABLES;");
						out.println();
					}
				}
			}
			finally {
				if (conn != null) {
					conn.close();
				}
			}
			
			// Write the footer of the DDL script
			out.println("/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;");
			out.println("/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;");
			out.println("/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;");
			out.println("/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;");
			out.println("/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;");
			out.println("/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;");
			out.println("/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;");
			out.println("/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;");
			out.flush();
			
			zip.closeEntry();
			zip.close();
		}
		catch (IOException e) {
			throw new DAOException(e);
		}
		catch (SQLException e) {
			throw new DAOException(e);
		}
		finally {
			IOUtils.closeQuietly(zip);
		}
	}
	
	/**
	 * @see TestingDAO#getPatientWithMostEncounters()
	 */
	public Integer getPatientWithMostEncounters() {
		
		String sql = "select patient_id from encounter " + "group by patient_id " + "having count(patient_id) =  "
		        + "(select max(the_count) from (select patient_id, count(patient_id) as the_count from encounter "
		        + "group by patient_id) as t) limit 1";
		
		return (Integer) sessionFactory.getCurrentSession().createSQLQuery(sql).uniqueResult();
	}
	
	/**
	 * @see TestingDAO#getPatientWithMostObs()
	 */
	public Integer getPatientWithMostObs() {
		
		String sql = "select patient_id from encounter e inner join obs o " + "on e.encounter_id = o.encounter_id "
		        + "group by patient_id " + "having count(obs_id) =  " + "(select max(the_count) from  "
		        + "(select patient_id, count(obs_id) as the_count from encounter e "
		        + "inner join obs o on e.encounter_id = o.encounter_id " + "group by patient_id) as t) limit 1";
		
		return (Integer) sessionFactory.getCurrentSession().createSQLQuery(sql).uniqueResult();
	}
}
