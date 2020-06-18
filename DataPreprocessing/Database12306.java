

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.sql.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class Database12306 {
    private Connection con = null;
    private ResultSet resultSet;

    private String host = "localhost";
    private String dbname = "last";
    private String user = "checker";
    private String pwd = "123456";
    private String port = "5432";


    protected void getConnection() {
        try {
            Class.forName("org.postgresql.Driver");

        } catch (Exception e) {
            System.err.println("Cannot find the PostgreSQL driver. Check CLASSPATH.");
            System.exit(1);
        }

        try {
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;
            con = DriverManager.getConnection(url, user, pwd);

        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }


    protected void closeConnection() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public String searchTrains(String strat, String end) {
        StringBuilder str = new StringBuilder();
        String sql =
                "select *from trains where start_station_id in (select id  from stations where station_name like ?)\n" +
                        "and end_station_id in (select id from stations where station_name like ?);";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, strat + "%");
            preparedStatement.setString(2, end + "%");
            System.out.println(preparedStatement.toString());
            resultSet = preparedStatement.executeQuery();
            System.out.println("TrainCode\tTrainType\tStatus");
            while (resultSet.next()) {
                str.append(resultSet.getString("train_code") + "\t\t");
                str.append(resultSet.getString("train_type") + "\t\t");
                str.append(resultSet.getString("status") + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return str.toString();
    }





    public String searchStationsOfTrain(String trainCode) {
        StringBuilder str = new StringBuilder();
        String sql =
                "select s.station_name, depart_time, arrive_time\n" +
                        "from (select station_number, station_id, depart_time, arrive_time\n" +
                        "      from route_detail\n" +
                        "      where train_id = (select train_id from trains where train_code = ?)\n" +
                        "        and valid = 1) sub\n" +
                        "         join stations s on sub.station_id = s.id\n" +
                        "order by station_number;";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, trainCode);
            resultSet = preparedStatement.executeQuery();
            System.out.println("Stations of train: " + trainCode);
            System.out.println("StationName\t\tArriveTime\t\tDepartTime");
            int i = 0;
            resultSet.getRow();

            //  t.next();
            while (resultSet.next()) {
                str.append(resultSet.getString("station_name") + "\t\t\t");
                if (i != 0)
                    str.append(resultSet.getString("depart_time") + "\t\t");
                else str.append("---\t\t\t\t");
                if (!resultSet.isLast())
                    str.append(resultSet.getString("arrive_time") + "\n");
                else str.append("---");
                i++;

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return str.toString();
    }




    public int addStationFormTrain(String trainCode, String stationName
            , int stationNumber, String s1, String s2) {
        String sql1 = "select trains.train_id from trains where train_code=?";
        String sql2 = "select stations.id from stations where  station_name=?";
        String sql3 = "select max(station_number) m from route_detail where train_id=?;";
        String sql4 = "update route_detail\n" +
                "set station_number=station_number + 1 where train_id=? and station_number=?;";
        String sql5 = "insert into route_detail (id, train_id, station_number, station_id, arrive_time, depart_time)\n" +
                "values ((select count(*) from route_detail) + 1, ?, ?, ?, ?,?);";
        try {
            DateFormat d=new SimpleDateFormat("hh:mm");
            Date d1 = d.parse(s1);
            Date d2 = d.parse(s2);

            Time ar=new java.sql.Time(d1.getTime());
            Time dp=new java.sql.Time(d2.getTime());
            PreparedStatement preparedStatement1 = con.prepareStatement(sql1);
            preparedStatement1.setString(1, trainCode);
            resultSet = preparedStatement1.executeQuery();
            resultSet.next();
            int trainID = resultSet.getInt("train_id");

            PreparedStatement preparedStatement2 = con.prepareStatement(sql2);
            preparedStatement2.setString(1, stationName);
            resultSet = preparedStatement2.executeQuery();
            resultSet.next();
            int stationID = resultSet.getInt("id");

            PreparedStatement preparedStatement3 = con.prepareStatement(sql3);
            preparedStatement3.setInt(1, trainID);
            resultSet = preparedStatement3.executeQuery();
            resultSet.next();
            int max = resultSet.getInt("m");
            for (int i = max; i >= stationNumber; i--) {
                PreparedStatement preparedStatement4 = con.prepareStatement(sql4);
                preparedStatement4.setInt(1, trainID);
                preparedStatement4.setInt(2, i);
                preparedStatement4.executeUpdate();
            }

            PreparedStatement preparedStatement4 = con.prepareStatement(sql5);
            preparedStatement4.setInt(1, trainID);
            preparedStatement4.setInt(2, stationNumber);
            preparedStatement4.setInt(3, stationID);
            preparedStatement4.setTime(4, ar);
            preparedStatement4.setTime(5, dp);
            preparedStatement4.execute();

        } catch (SQLException | ParseException e) {
            e.printStackTrace();
            return -1;
        }
        return 1;

    }
    public int removeStationFormTrain(String trainCode,String stationName){
        String sql1 = "select trains.train_id from trains where train_code=?";
        String sql2 = "select stations.id from stations where  station_name=?";
        String sql3 = "update route_detail set valid=0 where route_detail.train_id=?\n" +
                "and route_detail.station_id=?;";
        String sql4 = "update ticket_detail set valid=0 where ticket_detail.train_id=?\n" +
                "and (ticket_detail.start_station_id=?\n" +
                "or ticket_detail.end_station_id=?);";
        try {
            PreparedStatement preparedStatement1 = con.prepareStatement(sql1);
            preparedStatement1.setString(1, trainCode);
            resultSet = preparedStatement1.executeQuery();
            resultSet.next();
            int trainID=resultSet.getInt("train_id");

            PreparedStatement preparedStatement2 = con.prepareStatement(sql2);
            preparedStatement2.setString(1, stationName);
            resultSet = preparedStatement2.executeQuery();
            resultSet.next();
            int stationID=resultSet.getInt("id");

            PreparedStatement preparedStatement3= con.prepareStatement(sql3);
            preparedStatement3.setInt(1, trainID);
            preparedStatement3.setInt(2, stationID );
            preparedStatement3.executeUpdate();

            PreparedStatement preparedStatement4= con.prepareStatement(sql4);
            preparedStatement4.setInt(1, trainID);
            preparedStatement4.setInt(2, stationID );
            preparedStatement4.setInt(3, stationID );
            preparedStatement4.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return 1;
    }





}
