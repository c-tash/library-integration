package ru.umeta.rnbharvester;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Ad-hoc solution to extracting data from rnd dump
 * Created by Kirill Kosolapov (https://github.com/c-tash) on 14.10.2015.
 */
public class ModsCreator {

    public static final ResourceBundle PROPERTIES = ResourceBundle.getBundle("application");
    private final static String SQL_DRIVER_NAME = PROPERTIES.getString("SQL_DRIVER_NAME");
    private final static String SQL_DB_CONNECT_STRING = PROPERTIES.getString("SQL_DB_CONNECT_STRING");
    private final static String SQL_DB_NAME = PROPERTIES.getString("SQL_DB_NAME");
    private final static String SQL_DB_USER = PROPERTIES.getString("SQL_DB_USER");
    private final static String SQL_DB_PASS = PROPERTIES.getString("SQL_DB_PASS");
    private static final long BATCH_SIZE = 1000;

    private static boolean driverLoaded = false;

    static {
        try {
            Class.forName(SQL_DRIVER_NAME);
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            System.err.println(SQL_DRIVER_NAME + " driver failed to load");
        }
    }

    private static void getRecordListFromDB(long startId) throws SQLException, IOException {
        Connection conn = DriverManager.getConnection(SQL_DB_CONNECT_STRING + SQL_DB_NAME, SQL_DB_USER, SQL_DB_PASS);
        PreparedStatement statement = conn.prepareStatement(
                "SELECT Id, Author, Name, SubName, PublishYear, ISBN " +
                        "FROM `test2`.`tbl_common_biblio_card` " +
                        "WHERE Id >= ? AND Id < ? + 1000");
        statement.setLong(1, startId);
        statement.setLong(2, startId);
        ResultSet result = statement.executeQuery();
        List<String> recordList = new ArrayList<>();
        while (result.next()) {
            String author = result.getString(2);
            String title = result.getString(3);
            String subTitle = result.getString(4);
            String publishYear = result.getString(5);
            String isbn = result.getString(6);

            recordList.add(
                    "<mods version=\"3.5\">" +
                        "<titleInfo>" +
                            "<title>" + (title != null ? title : "") + "</title>" +
                            "<subTitle>" + (subTitle != null ? subTitle : "") + "</subTitle>" +
                        "</titleInfo>" +
                        "<name>" +
                            "<namePart type=\"personal\" usage=\"primary\" nameTitleGroup=\"1\">" +
                                (author != null ? author : "") +
                            "</namePart>" +
                        "</name>" +
                        "<originInfo>" +
                            "<dateIssued>" + (publishYear != null ? publishYear : "") + "</dateIssued>" +
                        "</originInfo>" +
                        "<identifier type=\"isbn\">" + (isbn != null ? isbn : "") + "</identifier>" +
                    "</mods>");
        }

        new File("results").mkdirs();
        File file = new File("results/" + startId + ".xml");
        file.createNewFile();
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<modsCollection xsi:schemaLocation=\"http://www.loc.gov/mods/v3 " +
                    "http://www.loc.gov/standards/mods/v3/mods-3-5.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                    "xmlns=\"http://www.loc.gov/mods/v3\">\n");

            recordList.stream().forEach(writer::println);

            writer.println("</modsCollection>");
        }

        conn.close();
    }

    public static void main(String[] args) {
        if (driverLoaded) {
            try {
                long minId = getRecordMinIdFromDB();
                long maxId = getRecordMaxIdFromDB();
                for (long curId = minId; curId < maxId; curId += BATCH_SIZE) {
                    getRecordListFromDB(curId);
                }
            } catch (Exception e) {
                System.err.println("Internal Error");
                e.printStackTrace();
            }

        }
    }

    private static long getRecordMinIdFromDB() throws SQLException {
        try (Connection conn = DriverManager.getConnection(SQL_DB_CONNECT_STRING + SQL_DB_NAME, SQL_DB_USER, SQL_DB_PASS)) {
            PreparedStatement statement = conn.prepareStatement(
                    "SELECT Id FROM `test2`.`tbl_common_biblio_card` ORDER BY Id ASC LIMIT 1");
            ResultSet result = statement.executeQuery();
            return result.getLong(1);
        }
    }

    private static long getRecordMaxIdFromDB() throws SQLException {
        try (Connection conn = DriverManager.getConnection(SQL_DB_CONNECT_STRING + SQL_DB_NAME, SQL_DB_USER, SQL_DB_PASS)) {
            PreparedStatement statement = conn.prepareStatement(
                    "SELECT Id FROM `test2`.`tbl_common_biblio_card` ORDER BY Id DESC LIMIT 1");
            ResultSet result = statement.executeQuery();
            return result.getLong(1);
        }
    }

}
