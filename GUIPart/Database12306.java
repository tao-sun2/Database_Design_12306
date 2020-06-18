package sample;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Database12306 {
    private Connection con = null;
    private ResultSet resultSet;


    private String host = "localhost";
    private String dbname = "last";
    private String port = "5432";

    //SuperUser
    private String user;
    private String pwd;

    private int row;


    protected void getConnectionWithSuperuser() {
        try {
            Class.forName("org.postgresql.Driver");

        } catch (Exception e) {
            System.err.println("Cannot find the PostgreSQL driver. Check CLASSPATH.");
            System.exit(1);
        }

        try {
            //superuser
            user = "checker";
            pwd = "123456";
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;
            con = DriverManager.getConnection(url, user, pwd);

        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    protected void getConnectionWithCommonUser() {
        try {
            Class.forName("org.postgresql.Driver");

        } catch (Exception e) {
            System.err.println("Cannot find the PostgreSQL driver. Check CLASSPATH.");
            System.exit(1);
        }

        try {
            //CommonUser with limited privilege.
            user = "visitor";
            pwd = "123456";
//            user = "checker";
//            pwd = "123456";
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
            str.append("Stations of train: " + trainCode + "\n");
            str.append("StationName\t\tArriveTime\t\tDepartTime\n");
            int i = 0;
            resultSet.getRow();

            //  t.next();
            while (resultSet.next()) {
                String temp = resultSet.getString("station_name");
                if (temp.length() == 3)
                    str.append(temp + "\t\t\t");
                else str.append(temp + "\t\t\t\t");
                if (i != 0)
                    str.append(resultSet.getString("arrive_time") + "\t\t\t");
                else str.append("---\t\t\t\t");
                if (!resultSet.isLast())
                    str.append(resultSet.getString("depart_time") + "\n");
                else str.append("---");
                i++;

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return str.toString();

    }

    public int removeStationFormTrain(String trainCode, String stationName) {
        String sql1 = "select trains.train_id from trains where train_code=?";
        String sql2 = "select stations.id from stations where  station_name=?";
        String sql3 = "update route_detail set valid=0 where route_detail.train_id=?\n" +
                "and route_detail.station_id=?;";
        String sql4 = "update ticket_detail set valid=0 where ticket_detail.train_id=?\n" +
                "and (ticket_detail.start_station_id=?\n" +
                "or ticket_detail.end_station_id=?);";
        String sql5="update rest_tickets\n" +
                "set valid=0\n" +
                "where trains_stations_seats_id in\n" +
                "      (select trains_stations_seats.id\n" +
                "       from trains_stations_seats\n" +
                "       where route_detail_id = (select id\n" +
                "                                from route_detail\n" +
                "                                where train_id = ?\n" +
                "                                  and station_id = ?))";
        try {
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
            preparedStatement3.setInt(2, stationID);
            preparedStatement3.executeUpdate();

            PreparedStatement preparedStatement4 = con.prepareStatement(sql4);
            preparedStatement4.setInt(1, trainID);
            preparedStatement4.setInt(2, stationID);
            preparedStatement4.setInt(3, stationID);
            preparedStatement4.executeUpdate();

            PreparedStatement preparedStatement5 = con.prepareStatement(sql5);
            preparedStatement5.setInt(1, trainID);
            preparedStatement5.setInt(2, stationID);
            preparedStatement4.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return 1;
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
            DateFormat d = new SimpleDateFormat("hh:mm");
            Date d1 = d.parse(s1);
            Date d2 = d.parse(s2);

            Time ar = new java.sql.Time(d1.getTime());
            Time dp = new java.sql.Time(d2.getTime());
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


    public String searchTrainInformation(String strat, String end) {
        StringBuilder str = new StringBuilder();
        String sql =
                "select train_code,train_type,                                                                                            \n" +
                        "       m.station_name as               start_station,                                                         \n" +
                        "       s.station_name as               end_station,                                                           \n" +
                        "       seat_type,                                                                                             \n" +
                        "       findSeat(seat_type, train_type) seat_type_name,                                                        \n" +
                        "       rd.depart_time start_time,                                                                             \n" +
                        "       rd2.arrive_time end_time,                                                                              \n" +
                        "       price                                                                                                  \n" +
                        "from ticket_detail                                                                                            \n" +
                        "         join                                                                                                 \n" +
                        "     ((select id as id1                                                                                       \n" +
                        "       from stations                                                                                          \n" +
                        "       where station_name like ?) a1                                                                      \n" +
                        "         cross join (                                                                                         \n" +
                        "             select id as id2                                                                                 \n" +
                        "             from stations                                                                                    \n" +
                        "             where station_name like ?) a2) sub on start_station_id = id1 and end_station_id = id2        \n" +
                        "         join stations s on ticket_detail.end_station_id = s.id                                               \n" +
                        "         join stations m on ticket_detail.start_station_id = m.id                                             \n" +
                        "         join trains t on ticket_detail.train_id = t.train_id                                                 \n" +
                        "         join route_detail rd on t.train_id = rd.train_id and ticket_detail.start_station_id = rd.station_id  \n" +
                        "         join route_detail rd2 on t.train_id = rd2.train_id and ticket_detail.end_station_id = rd2.station_id;;";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, strat + "%");
            preparedStatement.setString(2, end + "%");
//            System.out.println(preparedStatement.toString());
            resultSet = preparedStatement.executeQuery();
            System.out.println("TrainCode\tTrainType\tstart_station\t" +
                    "end_station\tseat_type\tstart_time\tend_time" +
                    "\tprice");
            while (resultSet.next()) {
                str.append(resultSet.getString("train_code") + "\t\t");
                str.append(resultSet.getString("train_type") + "\t\t");
                str.append(resultSet.getString("start_station") + "\t\t");
                str.append(resultSet.getString("end_station") + "\t\t");
//                str.append(resultSet.getString("seat_type") + "\t");
                str.append(resultSet.getString("seat_type_name") + "\t");
                str.append(resultSet.getString("start_time") + "\t");
                str.append(resultSet.getString("end_time") + "\t");
                str.append(resultSet.getString("price") + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return str.toString();
    }

    protected int LoginIn(String userName, String passWord) {
        String sql =
                "select credit from users where user_name=? and password=?;";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, passWord);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("credit");
            } else return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public ArrayList<Integer> si = new ArrayList(), ei = new ArrayList(), seat = new ArrayList(), ti = new ArrayList();

    public String searchTrainInformation(String strat, String end, String date) {
        StringBuilder str = new StringBuilder();
        si = new ArrayList();
        ei = new ArrayList();
        seat = new ArrayList();
        ti = new ArrayList();

        String sql =
                "select *,howmanytickets(tid,start_id,end_id,seat_type,cast (? as date))tk from \n" +
                        "                (select train_code,train_type,t.train_id tid,                                                                                            \n" +
                        "       m.station_name as               start_station,                                                         \n" +
                        "       s.station_name as               end_station, " +
                        "m.id start_id,\n" +
                        "           s.id end_id,                                                          \n" +
                        "       seat_type,                                                                                             \n" +
                        "       findSeat(seat_type, train_type) seat_type_name,                                                        \n" +
                        "       rd.depart_time start_time,                                                                             \n" +
                        "       rd2.arrive_time end_time,                                                                              \n" +
                        "       price                                                                                                  \n" +
                        "from ticket_detail                                                                                            \n" +
                        "         join                                                                                                 \n" +
                        "     ((select id as id1                                                                                       \n" +
                        "       from stations                                                                                          \n" +
                        "       where station_name like ?) a1                                                                      \n" +
                        "         cross join (                                                                                         \n" +
                        "             select id as id2                                                                                 \n" +
                        "             from stations                                                                                    \n" +
                        "             where station_name like ?) a2) sub on start_station_id = id1 and end_station_id = id2        \n" +
                        "         join stations s on ticket_detail.end_station_id = s.id                                               \n" +
                        "         join stations m on ticket_detail.start_station_id = m.id                                             \n" +
                        "         join trains t on ticket_detail.train_id = t.train_id                                                 \n" +
                        "         join route_detail rd on t.train_id = rd.train_id and ticket_detail.start_station_id = rd.station_id  \n" +
                        "         join route_detail rd2 on t.train_id = rd2.train_id and ticket_detail.end_station_id = rd2.station_id where ticket_detail.valid=1)subs";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(2, strat + "%");
            preparedStatement.setString(3, end + "%");
            preparedStatement.setString(1, date);
//            preparedStatement.setString(1,date);

            resultSet = preparedStatement.executeQuery();
            str.append("row\tTrainCode\tTrainType\t\tstart_station\t" +
                    "end_station\tseat_type\tstart_time\tend_time" +
                    "\tprice\trest_tickets\n");
            int i = 0;
            while (resultSet.next()) {
                ti.add(Integer.parseInt(resultSet.getString("tid")));
                ei.add(Integer.parseInt(resultSet.getString("end_id")));
                si.add(Integer.parseInt(resultSet.getString("start_id")));
                seat.add(Integer.parseInt(resultSet.getString("seat_type")));
                str.append(i++);
                str.append("\t");
                str.append(resultSet.getString("train_code") + "\t\t");
                str.append(resultSet.getString("train_type") + "\t\t");
                str.append(resultSet.getString("start_station") + "\t\t");
                str.append(resultSet.getString("end_station") + "\t\t");
                str.append(resultSet.getString("seat_type_name") + "\t\t");
                str.append(resultSet.getString("start_time") + "\t");
                str.append(resultSet.getString("end_time") + "\t");
                str.append(resultSet.getString("price") + "\t");
                str.append(resultSet.getString("tk") + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return str.toString();
    }

    public String searchReservingOrder(String username) {
        StringBuilder str = new StringBuilder();
        String sql =
                "select create_time,\n" +
                        "       user_name,\n" +
                        "       m.station_name as               start,\n" +
                        "       s.station_name as               end,\n" +
                        "       train_code,\n" +
                        "       rd2.depart_time de,\n" +
                        "       rd.arrive_time ar\n" +
                        "        ,\n" +
                        "       findSeat(seat_type, train_type) seat_type,\n" +
                        "       price,\n" +
                        "       carriage_no,\n" +
                        "       seat_no\n" +
                        "from orders\n" +
                        "--          join orders_tickets ot on orders.order_id = ot.order_id\n" +
                        "         join tickets t on orders.ticket_id = t.ticket_id\n" +
                        "         join ticket_detail td on t.ticket_detail_id = td.id\n" +
                        "         join stations s on td.end_station_id = s.id\n" +
                        "         join stations m on td.start_station_id = m.id\n" +
                        "         join trains t2 on t.train_id = t2.train_id\n" +
                        "         join route_detail rd on s.id = rd.station_id and t.train_id = rd.train_id\n" +
                        "         join route_detail rd2 on m.id = rd2.station_id and t.train_id = rd2.train_id\n" +
                        "\n" +
                        "where user_name = ?\n" +
                        "order by create_time\n" +
                        ";";

        try {
            System.out.println(sql);
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, username);
            resultSet = preparedStatement.executeQuery();
            // System.out.println("Orders of user: " + username);
            str.append("create_time\t\tuser_name\t\tstart_station\t\tend_station\t\ttrain_code" +
                    "\t\tdepart_time\t\tarrive_time\t\tseat_type\t\tprice\t\tcarriage_no\t\tseat_no\n");
            int i = 0;

            while (resultSet.next()) {
                str.append(resultSet.getString("create_time") + "\t\t\t");
                str.append(resultSet.getString("user_name") + "\t\t");
                str.append(resultSet.getString("start") + "\t\t");
                str.append(resultSet.getString("end") + "\t\t");
                str.append(resultSet.getString("train_code") + "\t\t");
                str.append(resultSet.getString("de") + "\t\t");
                str.append(resultSet.getString("ar") + "\t\t");
                str.append(resultSet.getString("seat_type") + "\t\t");
                str.append(resultSet.getString("price") + "\t\t");
                str.append(resultSet.getString("carriage_no") + "\t\t");
                str.append(resultSet.getString("seat_no") + "\n");


            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return str.toString();
    }

    public String searchTrainsP(String start, String end) {
        StringBuilder str = new StringBuilder();
        String sql =
                "select *\n" +
                        "from (select (select train_code from trains where train_id = train1) train1,\n" +
                        "             (select station_name from stations where id = dID1)     d1,\n" +
                        "             (select station_name from stations where id = aID1)     a1,\n" +
                        "             dat1,\n" +
                        "             ddt1,\n" +
                        "             aat1,\n" +
                        "             adt1,\n" +
                        "             (select train_code from trains where train_id = train2) train2,\n" +
                        "             (select station_name from stations where id = aID2)     d2,\n" +
                        "             (select station_name from stations where id = dID2)     a2,\n" +
                        "             dat2,\n" +
                        "             ddt2,\n" +
                        "             aat2,\n" +
                        "             adt2,\n" +
                        "             total_time,\n" +
                        "             rnk                                                     rnk_time,\n" +
                        "             total_price,\n" +
                        "             rank() over (order by total_price )                     rnk_price\n" +
                        "      from (select train1,\n" +
                        "                   dID1,\n" +
                        "                   aID1,\n" +
                        "                   dat1,\n" +
                        "                   ddt1,\n" +
                        "                   aat1,\n" +
                        "                   adt1,\n" +
                        "                   train2,\n" +
                        "                   dID2,\n" +
                        "                   aID2,\n" +
                        "                   dat2,\n" +
                        "                   ddt2,\n" +
                        "                   aat2,\n" +
                        "                   adt2,\n" +
                        "                   total_time,\n" +
                        "                   rnk,\n" +
                        "                   (select price\n" +
                        "                    from ticket_detail\n" +
                        "                    where train_id = train1\n" +
                        "                      and start_station_id = dID1\n" +
                        "                      and end_station_id = aID1\n" +
                        "                      and seat_type = 1)\n" +
                        "                       + (select price\n" +
                        "                          from ticket_detail\n" +
                        "                          where train_id = train2\n" +
                        "                            and start_station_id = aID2\n" +
                        "                            and end_station_id = dID2\n" +
                        "                            and seat_type = 1) total_price\n" +
                        "            from (select train1,\n" +
                        "                         dID1,\n" +
                        "                         aID1,\n" +
                        "                         dat1,\n" +
                        "                         ddt1,\n" +
                        "                         aat1,\n" +
                        "                         adt1,\n" +
                        "                         train2,\n" +
                        "                         dID2,\n" +
                        "                         aID2,\n" +
                        "                         dat2,\n" +
                        "                         ddt2,\n" +
                        "                         aat2,\n" +
                        "                         adt2,\n" +
                        "                         total_time,\n" +
                        "                         rank() over (order by total_time ) rnk\n" +
                        "                  from (select distinct train1,\n" +
                        "                                        dID1,\n" +
                        "                                        aID1,\n" +
                        "                                        dat1,\n" +
                        "                                        ddt1,\n" +
                        "                                        aat1,\n" +
                        "                                        adt1,\n" +
                        "                                        train2,\n" +
                        "                                        dID2,\n" +
                        "                                        aID2,\n" +
                        "                                        dat2,\n" +
                        "                                        ddt2,\n" +
                        "                                        aat2,\n" +
                        "                                        adt2,\n" +
                        "                                        dat2 - ddt1 total_time\n" +
                        "                        from (select distinct sub4.id1  train1,\n" +
                        "                                              sub4.depa d1,\n" +
                        "                                              sub4.arri a1,\n" +
                        "                                              sub4.deI  dID1,\n" +
                        "                                              sub4.ai   aID1,\n" +
                        "                                              sub4.dat  dat1,\n" +
                        "                                              sub4.ddt  ddt1,\n" +
                        "                                              sub4.art  aat1,\n" +
                        "                                              sub4.adt  adt1\n" +
                        "                              from (select *\n" +
                        "                                    from (select sub.id1, sub1.depa, sub1.deI, sub1.dat, sub1.ddt\n" +
                        "                                          from (select train_id id1\n" +
                        "                                                from route_detail\n" +
                        "                                                where (select train_code\n" +
                        "                                                       from trains\n" +
                        "                                                       where trains.train_id = route_detail.train_id) like 'G%'\n" +
                        "                                                  and train_id in\n" +
                        "                                                      (select route_detail.train_id from route_detail where station_id = 77)\n" +
                        "                                                  and train_id not in (select train_id\n" +
                        "                                                                       from route_detail\n" +
                        "                                                                       where station_id = 77\n" +
                        "                                                                         and train_id in (\n" +
                        "                                                                           select train_id\n" +
                        "                                                                           from route_detail\n" +
                        "                                                                           where station_id = 2351))) sub\n" +
                        "                                                   join (select route_detail.train_id,\n" +
                        "                                                                station_number depa,\n" +
                        "                                                                station_id     deI,\n" +
                        "                                                                arrive_time    dat,\n" +
                        "                                                                depart_time    ddt\n" +
                        "                                                         from route_detail\n" +
                        "                                                         where station_id = 77) sub1\n" +
                        "                                                        on sub.id1 = sub1.train_id\n" +
                        "                                         ) sub2\n" +
                        "                                             join\n" +
                        "                                         (select train_id,\n" +
                        "                                                 station_number arri,\n" +
                        "                                                 station_id     ai,\n" +
                        "                                                 arrive_time    art,\n" +
                        "                                                 depart_time    adt\n" +
                        "                                          from route_detail\n" +
                        "                                          where (select train_code\n" +
                        "                                                 from trains\n" +
                        "                                                 where trains.train_id = route_detail.train_id) like 'G%'\n" +
                        "                                            and train_id not in\n" +
                        "                                                (select train_id\n" +
                        "                                                 from route_detail\n" +
                        "                                                 where station_id = 77\n" +
                        "                                                   and train_id in (\n" +
                        "                                                     select train_id\n" +
                        "                                                     from route_detail\n" +
                        "                                                     where station_id = 2351))) sub3\n" +
                        "                                         on sub2.id1 = sub3.train_id) sub4\n" +
                        "                              where sub4.arri > sub4.depa\n" +
                        "                                AND art > ddt) fir\n" +
                        "                                 join\n" +
                        "                             (select distinct sub4.id1  train2,\n" +
                        "                                              sub4.depa d2,\n" +
                        "                                              sub4.arri a2,\n" +
                        "                                              sub4.deI  dID2,\n" +
                        "                                              sub4.ai   aID2,\n" +
                        "                                              sub4.dat  dat2,\n" +
                        "                                              sub4.ddt  ddt2,\n" +
                        "                                              sub4.art  aat2,\n" +
                        "                                              sub4.adt  adt2\n" +
                        "                              from (select *\n" +
                        "                                    from (select sub.id1, sub1.depa, sub1.deI, sub1.dat, sub1.ddt\n" +
                        "                                          from (select train_id id1\n" +
                        "                                                from route_detail\n" +
                        "                                                where (select train_code\n" +
                        "                                                       from trains\n" +
                        "                                                       where trains.train_id = route_detail.train_id) like 'G%'\n" +
                        "                                                  and train_id in\n" +
                        "                                                      (select route_detail.train_id from route_detail where station_id = 2351)\n" +
                        "                                                  and train_id not in (select train_id\n" +
                        "                                                                       from route_detail\n" +
                        "                                                                       where station_id = 77\n" +
                        "                                                                         and train_id in (\n" +
                        "                                                                           select train_id\n" +
                        "                                                                           from route_detail\n" +
                        "                                                                           where station_id = 2351))) sub\n" +
                        "                                                   join (select route_detail.train_id,\n" +
                        "                                                                station_number depa,\n" +
                        "                                                                station_id     deI,\n" +
                        "                                                                arrive_time    dat,\n" +
                        "                                                                depart_time    ddt\n" +
                        "                                                         from route_detail\n" +
                        "                                                         where station_id = 2351) sub1\n" +
                        "                                                        on sub.id1 = sub1.train_id\n" +
                        "                                         ) sub2\n" +
                        "                                             join\n" +
                        "                                         (select train_id,\n" +
                        "                                                 station_number arri,\n" +
                        "                                                 station_id     ai,\n" +
                        "                                                 arrive_time    art,\n" +
                        "                                                 depart_time    adt\n" +
                        "                                          from route_detail\n" +
                        "                                          where (select train_code\n" +
                        "                                                 from trains\n" +
                        "                                                 where trains.train_id = route_detail.train_id) like 'G%'\n" +
                        "                                            and train_id not in\n" +
                        "                                                (select train_id\n" +
                        "                                                 from route_detail\n" +
                        "                                                 where station_id = 77\n" +
                        "                                                   and train_id in (\n" +
                        "                                                     select train_id\n" +
                        "                                                     from route_detail\n" +
                        "                                                     where station_id = 2351))) sub3\n" +
                        "                                         on sub2.id1 = sub3.train_id) sub4\n" +
                        "                              where sub4.arri < sub4.depa\n" +
                        "                                AND dat > adt) seco\n" +
                        "                             on aID1 = aID2) subt\n" +
                        "                  where subt.aat1 + '20 min' < subt.adt2\n" +
                        "                    and subt.dat2 - subt.ddt1 > '5 hour') subtt\n" +
                        "            where rnk <= 100) subttt) subtab\n" +
                        "where subtab.rnk_price <= 10\n" +
                        "   or subtab.rnk_time <= 10\n" +
                        "order by subtab.rnk_time;";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            str.append("Train1\t\t出发站\t\t到达站\t\t出发时间\t\t到达时间\t\t" +
                    "Train2\t\t出发站\t\t到达站\t\t出发时间\t\t到达时间\t\t总耗时\t\t总票价\t\t耗时排序\t\t票价排序\n");
            while (resultSet.next()) {
                str.append(resultSet.getString("train1") + "\t\t");
                str.append(resultSet.getString("d1") + "\t\t");
                str.append(resultSet.getString("a1") + "\t\t");
                str.append(resultSet.getString("ddt1") + "\t\t");
                str.append(resultSet.getString("aat1") + "\t\t");

                str.append(resultSet.getString("train2") + "\t\t");
                str.append(resultSet.getString("d2") + "\t\t");
                str.append(resultSet.getString("a2") + "\t\t");
                str.append(resultSet.getString("adt2") + "\t\t");
                str.append(resultSet.getString("dat2") + "\t\t");
                str.append(resultSet.getString("total_time") + "\t\t");
                str.append(resultSet.getString("total_price") + "\t\t");
                str.append(resultSet.getInt("rnk_time") + "\t\t");
                str.append(resultSet.getInt("rnk_price") + "\n");

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return str.toString();

    }
    public String Buy(String username, int choice, String date, String IDCARD) {
        StringBuilder str = new StringBuilder();
        int tid = ti.get(choice), sid = si.get(choice),
                eid = ei.get(choice), seat_type = seat.get(choice);
        String sql1 = "select station_number from route_detail where " +
                "station_id=" + sid + " and train_id=" + tid + ";\n";

        String sql2 = "select station_number from route_detail where " +
                "station_id=" + eid + " and train_id=" + tid + ";\n";


        try {

            PreparedStatement
                    p2 = con.prepareStatement(sql1), p3 = con.prepareStatement(sql2);
            resultSet = p2.executeQuery();
            int num1 = 0, num2 = 0;
            while (resultSet.next()) {
                num1 = Integer.parseInt(resultSet.getString("station_number"));
            }

            resultSet = p3.executeQuery();
            while (resultSet.next()) {
                num2 = Integer.parseInt(resultSet.getString("station_number"));
            }


            String sql = "select rest_tickets_id,seat_no,carriage_no,valid,id,rn from " +
                    "( select rest_tickets_id,seat_no,carriage_no,valid,id,row_number() over (partition by carriage_no,seat_no)rn\n" +
                    "from seat_information\n" +
                    "where rest_tickets_id in\n" +
                    "      (\n" +
                    "          select id\n" +
                    "          from rest_tickets\n" +
                    "          where trains_stations_seats_id in (select tss.id\n" +
                    "                                             from trains_stations_seats tss\n" +
                    "                                                      join route_detail rd on tss.route_detail_id = rd.id\n" +
                    "                                                      join stations s2 on rd.station_id = s2.id\n" +
                    "                                             where station_number between " + num1 + " and " + num2 + "\n" +
                    "                                               and train_id = " + tid + "\n" +
                    "                                               and seat_type = " + seat_type + ")\n" +
                    "            and date = cast(? as date) )\n" +
                    "and valid=1 )subs where rn=" + (num2 - num1 + 1) + "\n" +
                    "\n" +
                    ";";
            System.out.println(sql);
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, date);
            resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                String s = "insert into seat_information (rest_tickets_id, seat_no, carriage_no)\n" +
                        " select id,seat_no,carriage from rest_tickets\n" +
                        " cross join generate_series(1, 5)carriage  cross join generate_series(1,30)seat_no\n" +
                        " where trains_stations_seats_id in (select tss.id\n" +
                        "                                           from trains_stations_seats tss\n" +
                        "                                                    join route_detail rd on tss.route_detail_id = rd.id\n" +
                        "                                                    join stations s2 on rd.station_id = s2.id\n" +
                        "                                           where\n" +
                        "                                           train_id = " + tid + "\n" +
                        "                                           )\n" +
                        "          and date =cast (? as date);";

                PreparedStatement ins = con.prepareStatement(s);
                ins.setString(1, date);
                System.out.println(ins.execute());

                resultSet = preparedStatement.executeQuery();
                resultSet.next();


            }
            carriage_no = resultSet.getString("carriage_no");
            int c = Integer.parseInt(resultSet.getString("seat_no"));
            int l = (c - 1) / 5, no = c % 5;
            seat_no = String.valueOf(l + 1);
            if (no == 1) seat_no += "A";
            else if (no == 2) seat_no += "B";
            else if (no == 3) seat_no += "C";
            else if (no == 4) seat_no += "D";
            else seat_no += "E";
            System.out.println(seat_no);
            System.out.println((seat_type - 1) * 5 + Integer.parseInt(carriage_no));

            String update = "update seat_information set valid=0\n" +
                    "where rest_tickets_id in\n" +
                    "      (\n" +
                    "          select id\n" +
                    "          from rest_tickets\n" +
                    "          where trains_stations_seats_id in (select tss.id\n" +
                    "                                             from trains_stations_seats tss\n" +
                    "                                                      join route_detail rd on tss.route_detail_id = rd.id\n" +
                    "                                                      join stations s2 on rd.station_id = s2.id\n" +
                    "                                             where station_number between ? and ?\n" +
                    "                                               and train_id = ?\n" +
                    "                                               and seat_type = ?)\n" +
                    "            and date = cast(? as date) )\n" +
                    "and seat_no=? and carriage_no=?;";
            PreparedStatement preparedStatement1 = con.prepareStatement(update);
            preparedStatement1.setInt(1, num1);
            preparedStatement1.setInt(2, num2);
            preparedStatement1.setInt(3, tid);
            preparedStatement1.setInt(4, seat_type);
            preparedStatement1.setString(5, date);
            preparedStatement1.setInt(6, c);
            preparedStatement1.setInt(7, Integer.parseInt(carriage_no));

            preparedStatement1.execute();

            String BuySql = "select buyticket(?,?,?,?,?,?,cast (? as date),cast (? as char(18)),?,?,?)";

            PreparedStatement buyPre = con.prepareStatement(BuySql);
            buyPre.setInt(1, tid);
            buyPre.setInt(2, sid);
            buyPre.setInt(3, eid);
            buyPre.setInt(4, num1);
            buyPre.setInt(5, num2);
            buyPre.setInt(6, seat_type);
            buyPre.setString(7, date);
            buyPre.setString(8, IDCARD);
            buyPre.setString(9, username);
            buyPre.setInt(10,(seat_type - 1) * 5 +Integer.parseInt(carriage_no));
            buyPre.setString(11,seat_no);
            System.out.println(BuySql);
            System.out.println(num1 + " " + num2);
            System.out.println(tid + " " + sid + " " + eid + " " + num1 + " " + num2 + " " + seat_type + " " + date + " " + 411002200003081029l);

            buyPre.execute();
            System.out.println("You have successfully bought a ticket");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return str.toString();
    }


    String carriage_no;String seat_no;
    public String Refund(String username, int choice, String date, String IDCARD) {
        int tid = ti.get(choice), sid = si.get(choice),
                eid = ei.get(choice), seat_type = seat.get(choice);
        String sql1 = "select station_number from route_detail where " +
                "station_id=" + sid + " and train_id=" + tid + ";\n";

        String sql2 = "select station_number from route_detail where " +
                "station_id=" + eid + " and train_id=" + tid + ";\n";

        try {
            PreparedStatement
                    p2 = con.prepareStatement(sql1), p3 = con.prepareStatement(sql2);

            resultSet = p2.executeQuery();
            int num1 = 0, num2 = 0;
            while (resultSet.next()) {
                num1 = Integer.parseInt(resultSet.getString("station_number"));
            }

            resultSet = p3.executeQuery();
            while (resultSet.next()) {
                num2 = Integer.parseInt(resultSet.getString("station_number"));
            }
            String ReSql = "select refund(?,?,?,?,?,?,cast (? as date),cast (? as char(18)),?,?,?)";

            PreparedStatement buyPre = con.prepareStatement(ReSql);
            buyPre.setInt(1, tid);
            buyPre.setInt(2, sid);
            buyPre.setInt(3, eid);
            buyPre.setInt(4, num1);
            buyPre.setInt(5, num2);
            buyPre.setInt(6, seat_type);
            buyPre.setString(7, date);
            buyPre.setString(8, IDCARD);
            buyPre.setString(9, username);
            buyPre.setInt(10,(seat_type - 1) * 5 +Integer.parseInt(carriage_no));
            buyPre.setString(11,seat_no);
            buyPre.execute();

        }catch (Exception e){
            e.printStackTrace();
        }
        return "You have refunded a ticket";

    }


}

