import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.sql.*;

public class GoodLoader {
    private static final int BATCH_SIZE = 500;
    private static Connection con = null;
    private static PreparedStatement stmt = null;
    private static boolean verbose = false;
    static boolean[] ju = new boolean[82659];

    private static void openDB(String host, String dbname,
                               String user, String pwd) {
        try {
            //
            Class.forName("org.postgresql.Driver");
        } catch (Exception e) {
            System.err.println("Cannot find the Postgres driver. Check CLASSPATH.");
            System.exit(1);
        }
        String url = "jdbc:postgresql://" + host + "/" + dbname;
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pwd);
        try {
            con = DriverManager.getConnection(url, props);
            if (verbose) {
                System.out.println("Successfully connected to the database "
                        + dbname + " as " + user);
            }
            con.setAutoCommit(false);
        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }
        try {
//            stmt = con.prepareStatement("insert into stations values(?,?,?)");
//            stmt = con.prepareStatement("insert into trains values(?,?,?,?,?,?)");

            //   stmt = con.prepareStatement("insert into route_detail(id,train_id,station_number,station_id,arrive_time,depart_time)"
            //          + " values(?,?,?,?,?,?)");

            //stmt = con.prepareStatement("insert into trains_stations_seats values(?,?,?)");
            //   stmt=con.prepareStatement("insert into rest_tickets values(?,?,?)");
            stmt = con.prepareStatement("insert into ticket_detail(id,start_station_id,end_station_id" +
                    ",train_id,seat_type,price)"
                    + " values(?,?,?,?,?,?)");
        } catch (SQLException e) {
            System.err.println("Insert statement failed");
            System.err.println(e.getMessage());
            closeDB();
            System.exit(1);
        }
    }

    private static void closeDB() {
        if (con != null) {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                con.close();
                con = null;
            } catch (Exception e) {
                // Forget about it
            }
        }
    }

    private static void loadData1(Integer id, String studentid, String name) {
        try {
            if (con != null) {
                stmt.setInt(1, id);
                stmt.setString(2, studentid);
                stmt.setString(3, name);
                stmt.addBatch();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void loadData2(Integer id, String code, String type,
                                  int start_id, int end_id, int status)
            throws SQLException {
        if (con != null) {
            if (end_id != -1) {
                stmt.setInt(1, id);
                stmt.setString(2, code);
                stmt.setString(3, type);
                stmt.setInt(4, start_id);
                stmt.setInt(5, end_id);
                stmt.setInt(6, status);
                stmt.addBatch();
            }
        }
    }

    private static void loadData3(Integer id,
                                  int start_id, int end_id, int type, int seat_type, double price)
            throws SQLException {


        if (con != null) {

            stmt.setInt(1, id);
            stmt.setInt(2, start_id);
            stmt.setInt(3, end_id);
            stmt.setInt(4, type);
            stmt.setInt(5, seat_type);
            stmt.setDouble(6, price);
            stmt.addBatch();
        }
    }


    public static void main(String[] args) {
        String fileName;
        //fileName= "station.txt";
        // fileName="new_train.txt";
        fileName = "new_ticket_price.txt";
        boolean verbose = false;

        Properties defprop = new Properties();
        defprop.put("host", "localhost");
        defprop.put("user", "tao");
        defprop.put("password", "314413");
        defprop.put("database", "database_12306");
        Properties prop = new Properties(defprop);

        openDB(prop.getProperty("host"), prop.getProperty("database"),
                prop.getProperty("user"), prop.getProperty("password"));

        //  HashMap<Integer, Integer> m1 = new HashMap<>();
        HashMap<Integer, Integer> m2 = new HashMap<>();
        int count = 0;
        try (BufferedReader infile
                     = new BufferedReader(new FileReader("new_train.txt"))) {
            String line;
            String[] parts;
            while ((line = infile.readLine()) != null) {
                parts = line.split(",");
                if (parts[5].equals("null"))
                    continue;
                count++;
                //  m1.put(count, Integer.parseInt(parts[0]));
                m2.put(Integer.parseInt(parts[0]), count);
            }
            int cnt = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (BufferedReader infile
                     = new BufferedReader(new FileReader(fileName))) {
            long start;
            long end;
            String line;
            String[] parts;

            int cnt = 0;

            start = System.currentTimeMillis();


            //   DateFormat f = new SimpleDateFormat("yyyyMMdd");
            // DateFormat f = new SimpleDateFormat("hh:mm");
            while ((line = infile.readLine()) != null) {
                parts = line.split(",");

                int t = Integer.parseInt(parts[3]);
                if (parts[2].equals("null") || parts[1].equals("null"))
                    continue;
//                     cnt++;
//               loadData1(cnt,parts[1],parts[2]);
//                 loadData2(cnt,parts[1],parts[2],Integer.parseInt(parts[3]),Integer.parseInt(parts[5]),Integer.parseInt(parts[7]));
//                int t = Integer.parseInt(parts[1]);
                if (!m2.containsKey(t))
                    continue;

                cnt++;

                loadData3(cnt, Integer.parseInt(parts[1]), Integer.parseInt(parts[2])
                        , m2.get(Integer.parseInt(parts[3])), Integer.parseInt(parts[4]), Double.parseDouble(parts[5]));


//                stmt.setInt(1, cnt);
//                stmt.setInt(2, m2.get(Integer.parseInt(parts[1])));
//                stmt.setInt(3, Integer.parseInt(parts[2]));
//                stmt.setInt(4, Integer.parseInt(parts[3]));
//                java.util.Date d1 = f.parse(parts[4]);
//                java.sql.Time time = new Time(d1.getTime());
//                stmt.setTime(5, new java.sql.Time(d1.getTime()));
//                d1 = f.parse(parts[5]);
//                stmt.setTime(6, new java.sql.Time(d1.getTime()));
//                stmt.addBatch();


//               stmt.setInt(1, cnt);
//                 stmt.setInt(2, Integer.parseInt(parts[1]));
//                 stmt.setInt(3, Integer.parseInt(parts[2]));
//                 stmt.setInt(4, Integer.parseInt(parts[3]));
//                 java.util.Date d1 = f.parse(parts[4]);
//                 java.sql.Time time = new Time(d1.getTime());
//                 stmt.setTime(5, new java.sql.Time(d1.getTime()));
//                 d1 = f.parse(parts[5]);
//                 stmt.setTime(6, new java.sql.Time(d1.getTime()));
//                 stmt.addBatch();


//            for (int i = 1; i <= 82569; i++) {
//                for (int j = 1; j <=3 ; j++) {
//                      cnt++;
//                      stmt.setInt(1,cnt);
//                    stmt.setInt(2,j);
//                    stmt.setInt(3,i);
//                    stmt.addBatch();
//                    if (cnt % BATCH_SIZE == 0) {
//                        stmt.executeBatch();
//                        stmt.clearBatch();
//                    }
//                }
//
//
//
//            }


//            for (int j = 1; j <= 247707; j++) {
//                Integer data = 20200501;
//                for (int k = 0; k < 31; k++) {
//                    cnt++;
//
//                    java.util.Date d1 = f.parse(data.toString());
//                    stmt.setInt(1, j);
//                    stmt.setDate(2, new java.sql.Date(d1.getTime()));
//                    stmt.setInt(3,100);
//                    stmt.addBatch();
//                    data++;
                if (cnt % BATCH_SIZE == 0) {
                    stmt.executeBatch();
                    stmt.clearBatch();
                }
            }
//            }
            if (cnt % BATCH_SIZE != 0) {
                stmt.executeBatch();
            }
            con.commit();
            stmt.close();
            closeDB();
            end = System.currentTimeMillis();
            System.out.println(cnt + " records successfully loaded");
            System.out.println("Loading speed : "
                    + (cnt * 1000) / (end - start)
                    + " records/s");
        } catch (SQLException se) {
            System.err.println("SQL error: " + se.getMessage());
            try {
                con.rollback();
                stmt.close();
            } catch (Exception e2) {
            }
            closeDB();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Fatal error: " + e.getMessage());
            try {
                con.rollback();
                stmt.close();
            } catch (Exception e2) {
            }
            closeDB();
            System.exit(1);
        }
//        catch (ParseException e) {
//            e.printStackTrace();
//        }
        closeDB();
    }
}

