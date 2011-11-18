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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.testing.TestingConstants;
import org.openmrs.module.testing.api.db.TestingDao;
import org.openmrs.module.testing.util.Security;
import org.openmrs.util.OpenmrsConstants;

/**
 * Hibernate specific implementation for {@link TestingDao}.
 */
public class HibernateTestingDao implements TestingDao {
	
	protected Logger log = Logger.getLogger(getClass());
	
	private SessionFactory sessionFactory;
	
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public void generateTestDataSet(OutputStream os, String salt, String encryptionkey) throws DAOException {
		ZipOutputStream zip = null;
		
		try {
			StringBuffer sb = new StringBuffer(65536);
			// Write the DDL Header as mysqldump does
			sb.append("-- ------------------------------------------------------");
			sb.append(LINE_SEPARATOR + "-- Database dump with test data");
			sb.append(LINE_SEPARATOR + "-- OpenMRS Version: " + OpenmrsConstants.OPENMRS_VERSION);
			sb.append(LINE_SEPARATOR + "-- ------------------------------------------------------");
			sb.append(LINE_SEPARATOR);
			sb.append(LINE_SEPARATOR + "/*!40101 SET CHARACTER_SET_CLIENT=utf8 */;");
			sb.append(LINE_SEPARATOR + "/*!40101 SET NAMES utf8 */;");
			sb.append(LINE_SEPARATOR + "/*!40103 SET TIME_ZONE='+00:00' */;");
			sb.append(LINE_SEPARATOR + "/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;");
			sb.append(LINE_SEPARATOR + "/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;");
			sb.append(LINE_SEPARATOR + "/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;");
			sb.append(LINE_SEPARATOR + "/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;");
			sb.append(LINE_SEPARATOR + "/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;");
			sb.append(LINE_SEPARATOR + "/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;");
			sb.append(LINE_SEPARATOR + "/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;");
			sb.append(LINE_SEPARATOR + "/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;");
			sb.append(LINE_SEPARATOR);
			
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
			
			Set<String> personIds = new HashSet<String>();
			
			Integer mostEncounters = getPatientWithMostEncounters();
			if (mostEncounters != null) {
				personIds.add(mostEncounters.toString());
			}
			
			Integer mostObs = getPatientWithMostObs();
			if (mostObs != null) {
				personIds.add(mostObs.toString());
			}
			
			String maxPatientCount = Context.getAdministrationService().getGlobalProperty(
			    TestingConstants.GP_KEY_MAX_PATIENT_COUNT, "0");
			if (StringUtils.isBlank(maxPatientCount)) {
				maxPatientCount = "0";
			}
			
			List<Integer> randomPersonIds = getRandomPatients(Integer.valueOf(maxPatientCount) - 2);
			for (Integer personId : randomPersonIds) {
				personIds.add(personId.toString());
			}
			
			@SuppressWarnings("deprecation")
			Connection conn = sessionFactory.getCurrentSession().connection();
			try {
				Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				
				for (String table : tablesToDump) {
					sb.append(LINE_SEPARATOR);
					sb.append(LINE_SEPARATOR + "--");
					sb.append(LINE_SEPARATOR + "-- Table structure for table `" + table + "`");
					sb.append(LINE_SEPARATOR + "--");
					sb.append(LINE_SEPARATOR + "DROP TABLE IF EXISTS `" + table + "`;");
					sb.append(LINE_SEPARATOR + "SET @saved_cs_client     = @@character_set_client;");
					sb.append(LINE_SEPARATOR + "SET character_set_client = utf8;");
					ResultSet rs = st.executeQuery("SHOW CREATE TABLE " + table);
					while (rs.next()) {
						sb.append(LINE_SEPARATOR + rs.getString("Create Table") + ";");
					}
					sb.append(LINE_SEPARATOR + "SET character_set_client = @saved_cs_client;");
					sb.append(LINE_SEPARATOR);
				}
				
				ResultSet rs = st.executeQuery("SELECT person_id FROM users");
				while (rs.next()) {
					Integer person_id = rs.getInt(1);
					personIds.add(person_id.toString());
				}
				
				Set<String> personIdsToQuery = new HashSet<String>(personIds);
				while (!personIdsToQuery.isEmpty()) {
					String joinedPersonIdsToQuery = StringUtils.join(personIdsToQuery, ",");
					rs = st.executeQuery("SELECT person_a, person_b FROM relationship WHERE person_a IN ("
					        + joinedPersonIdsToQuery + ") OR person_b IN (" + joinedPersonIdsToQuery + ")");
					
					personIdsToQuery.clear();
					
					while (rs.next()) {
						Integer person_id = rs.getInt(1); //person_a
						if (personIds.add(person_id.toString())) {
							personIdsToQuery.add(person_id.toString());
						}
						
						person_id = rs.getInt(2); //person_b
						if (personIds.add(person_id.toString())) {
							personIdsToQuery.add(person_id.toString());
						}
					}
				}
				
				String joinedPersonIds = StringUtils.join(personIds, ",");
				
				Set<String> personIdColumn = new HashSet<String>(Arrays.asList("person", "person_address",
				    "person_attribute", "person_name", "obs"));
				Set<String> patientIdColumn = new HashSet<String>(Arrays.asList("patient", "patient_identifier",
				    "patient_program", "encounter", "orders"));
				
				for (String table : tablesToDump) {
					if (personIdColumn.contains(table)) {
						dumpDataFromTable(sb, st, table, "person_id IN (" + joinedPersonIds + ")");
					} else if (patientIdColumn.contains(table)) {
						dumpDataFromTable(sb, st, table, "patient_id IN (" + joinedPersonIds + ")");
					} else if ("relationship".equals(table)) {
						dumpDataFromTable(sb, st, table, "person_a IN (" + joinedPersonIds + ") OR person_b IN ("
						        + joinedPersonIds + ")");
					} else if ("patient_state".equals(table)) {
						dumpDataFromTable(sb, st, table,
						    "patient_program_id IN (SELECT patient_program_id FROM patient_program WHERE patient_id IN ("
						            + joinedPersonIds + "))");
					} else if ("drug_order".equals(table)) {
						dumpDataFromTable(sb, st, table, "order_id IN (SELECT order_id FROM orders WHERE patient_id IN ("
						        + joinedPersonIds + "))");
					} else {
						dumpDataFromTable(sb, st, table, null);
					}
				}
			}
			finally {
				if (conn != null) {
					conn.close();
				}
			}
			
			// Write the footer of the DDL script
			sb.append(LINE_SEPARATOR + "/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;");
			sb.append(LINE_SEPARATOR + "/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;");
			sb.append(LINE_SEPARATOR + "/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;");
			sb.append(LINE_SEPARATOR + "/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;");
			sb.append(LINE_SEPARATOR + "/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;");
			sb.append(LINE_SEPARATOR + "/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;");
			sb.append(LINE_SEPARATOR + "/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;");
			sb.append(LINE_SEPARATOR + "/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;");
			sb.append(LINE_SEPARATOR);
			
			String encrptyedText = Security.encryptString(sb.toString(), salt, encryptionkey);
			
			zip = new ZipOutputStream(os);
			zip.putNextEntry(new ZipEntry("data.txt"));
			OutputStreamWriter osWriter = new OutputStreamWriter(zip, "UTF-8");
			BufferedWriter bw = new BufferedWriter(osWriter);
			bw.write(encrptyedText);
			bw.flush();
			
			zip.closeEntry();
		}
		catch (IOException e) {
			throw new DAOException(e);
		}
		catch (SQLException e) {
			throw new DAOException(e);
		}
	}
	
	/**
	 * Dumps data from the given table.
	 */
	private void dumpDataFromTable(StringBuffer sb, Statement st, String table, String where) throws SQLException,
	    IOException {
		if (where == null) {
			where = "";
		} else {
			where = " where " + where;
		}
		
		sb.append(LINE_SEPARATOR + "-- Dumping data for table `" + table + "`");
		sb.append(LINE_SEPARATOR + "LOCK TABLES `" + table + "` WRITE;");
		sb.append(LINE_SEPARATOR + "/*!40000 ALTER TABLE `" + table + "` DISABLE KEYS */;");
		boolean first = true;
		
		String query = "SELECT * FROM " + table + where;
		
		ResultSet rs = st.executeQuery(query);
		ResultSetMetaData md = rs.getMetaData();
		int numColumns = md.getColumnCount();
		int rowNum = 0;
		boolean insert = false;
		
		while (rs.next()) {
			if (rowNum == 0) {
				insert = true;
				sb.append("INSERT INTO `" + table + "` VALUES ");
			}
			++rowNum;
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			if (rowNum % 20 == 0) {
				sb.append(LINE_SEPARATOR);
			}
			sb.append("(");
			for (int i = 1; i <= numColumns; ++i) {
				if (i != 1) {
					sb.append(",");
				}
				if (rs.getObject(i) == null) {
					sb.append("NULL");
				} else {
					switch (md.getColumnType(i)) {
						case Types.VARCHAR:
						case Types.CHAR:
						case Types.LONGVARCHAR:
							sb.append("'");
							sb.append(rs.getString(i).replaceAll("\n", "\\\\n").replaceAll("'", "\\\\'"));
							sb.append("'");
							break;
						case Types.BIGINT:
						case Types.DECIMAL:
						case Types.NUMERIC:
							sb.append(rs.getBigDecimal(i));
							break;
						case Types.BIT:
							sb.append(rs.getBoolean(i));
							break;
						case Types.INTEGER:
						case Types.SMALLINT:
						case Types.TINYINT:
							sb.append(rs.getInt(i));
							break;
						case Types.REAL:
						case Types.FLOAT:
						case Types.DOUBLE:
							sb.append(rs.getDouble(i));
							break;
						case Types.BLOB:
						case Types.VARBINARY:
						case Types.LONGVARBINARY:
							Blob blob = rs.getBlob(i);
							sb.append("'");
							InputStream in = blob.getBinaryStream();
							while (true) {
								int b = in.read();
								if (b < 0) {
									break;
								}
								char c = (char) b;
								if (c == '\'') {
									sb.append("\\'");
								} else {
									sb.append(c);
								}
							}
							sb.append("'");
							break;
						case Types.CLOB:
							sb.append("'");
							sb.append(rs.getString(i).replaceAll("\n", "\\\\n").replaceAll("'", "\\\\'"));
							sb.append("'");
							break;
						case Types.DATE:
							sb.append("'" + rs.getDate(i) + "'");
							break;
						case Types.TIMESTAMP:
							sb.append("'" + rs.getTimestamp(i) + "'");
							break;
						default:
							throw new RuntimeException("TODO: handle type code " + md.getColumnType(i) + " (name "
							        + md.getColumnTypeName(i) + ")");
					}
				}
			}
			sb.append(")");
		}
		if (insert) {
			sb.append(LINE_SEPARATOR + ";");
			insert = false;
		}
		
		sb.append(LINE_SEPARATOR + "/*!40000 ALTER TABLE `" + table + "` ENABLE KEYS */;");
		sb.append(LINE_SEPARATOR + "UNLOCK TABLES;");
		sb.append(LINE_SEPARATOR);
	}
	
	/**
	 * @see TestingDAO#getRandomPatients()
	 */
	@SuppressWarnings("unchecked")
	public List<Integer> getRandomPatients(Integer limit) {
		if (limit <= 0) {
			return Collections.emptyList();
		}
		String sql = "SELECT patient_id FROM patient ORDER BY RAND() LIMIT " + limit;
		return (List<Integer>) sessionFactory.getCurrentSession().createSQLQuery(sql)
		        .addScalar("patient_id", Hibernate.INTEGER).list();
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
