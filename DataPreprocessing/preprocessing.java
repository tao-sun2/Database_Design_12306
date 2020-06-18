import java.io.*;
import java.util.*;

public class preprocessing {
    private static final int numberOfTickets = 5;

    private static final int numberOfSeatTypes = 3;
    static HashMap<String, String> nameToId = new HashMap<>();
    static HashMap<String, String> idToType = new HashMap<>();
    static HashSet<String> types=new HashSet<>();

    public static void main(String[] args) {

//        process1();
        generate1();
        generate2();
        process2();

        //generate2();
        for (String s:idToType.keySet()) {
            types.add(idToType.get(s));
        }
        for (String type : types) {
            System.out.println(type);
        }



    }

    static void generate1() {
        String readFileName = "station.txt";
        try (BufferedReader infile
                     = new BufferedReader(new FileReader(readFileName))) {
            String[] parts;
            String line;
            while ((line = infile.readLine()) != null) {
                parts = line.split(",");
                nameToId.put(parts[1], parts[0]);
            }
            System.out.println("Successfully1");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void generate2() {
        String readFileName = "new_train.txt";
        try (BufferedReader infile
                     = new BufferedReader(new FileReader(readFileName))) {
            String[] parts;
            String line;
            while ((line = infile.readLine()) != null) {
                parts = line.split(",");
                idToType.put(parts[0], parts[2]);
            }
            System.out.println("Successfully2");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static void process1() {
        String readFileName = "train.txt";
        String writeFileName = "new_train.txt";


        try (BufferedReader infile
                     = new BufferedReader(new FileReader(readFileName))) {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(writeFileName));
            String[] parts;
            String line;
            while ((line = infile.readLine()) != null) {
                StringBuilder str = new StringBuilder();
                parts = line.split(",");
                for (int i = 0; i < 5; i++) {
                    if (i == 3)
                        str.append(nameToId.get(parts[i]));
                    else str.append(parts[i]);
                    str.append(",");
                }
                // String s = parts[6].charAt(0) == ('当') ? parts[6].substring(0, 2) : parts[6].substring(0, 3);
                str.append(nameToId.get(parts[8]));
                str.append(",");
                str.append(parts[9]);
                str.append(",");
//                str.append(s);
//                str.append(",");
                str.append(parts[10]);
                bufferedWriter.write(str.toString());
                bufferedWriter.newLine();
            }
            System.out.println("Successfully Executed1.");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static void process2() {
        String readFileName = "train_detail.txt";
        try (BufferedReader infile
                     = new BufferedReader(new FileReader(readFileName))) {
//            BufferedWriter bufferedWriter1 =
//                    new BufferedWriter(new FileWriter("new_ticket_amount.txt"));
            BufferedWriter bufferedWriter2 =
                    new BufferedWriter(new FileWriter("new_ticket_price.txt"));
            String[] parts;
            String line;
//            Integer cnt1 = 0;
            Integer cnt = 0;
            ArrayList<String> array = new ArrayList<>();
            Random r = new Random(System.currentTimeMillis());
            while ((line = infile.readLine()) != null) {
//                array.add(parts[3]);
//                if (cnt1 > 10000)
//                    break;
//                if (!parts[3].equals(parts[4]) || parts[5].equals("00:00")) {
//                    int i = numberOfSeatTypes;
//                    while (i-- > 0) {
//                        int days = 10;
//                        Integer date = 20200601;
//                        while (days-- > 0) {
//                            StringBuilder str1 = new StringBuilder();
//                            cnt1++;
//                            str1.append(cnt1);
//                            str1.append(",");
//                            str1.append(parts[1]);
//                            str1.append(",");
//                            str1.append(parts[2]);
//                            str1.append(",");
//                            str1.append(date);
//                            date++;
//                            str1.append(",");
//                            switch (i) {
//                                case 0:
//                                    str1.append("1");
//                                    break;
//                                case 1:
//                                    str1.append("2");
//                                    break;
//                                case 2:
//                                    str1.append("3");
//                                    break;
//                                default:
//                                    str1.append(" ");
//                            }
//                            str1.append(",");
//                            str1.append(numberOfTickets);
//                            bufferedWriter1.write(str1.toString());
//                            bufferedWriter1.newLine();
//                        }
//                    }
//                }

                parts = line.split(",");
                array.add(nameToId.get(parts[2]));

                if (parts[3].equals(parts[4]) && !parts[5].equals("00:00")) {
                    for (int i = 0; i < array.size(); i++) {
                        System.out.print(array.get(i) + " ");
                    }
                    System.out.println();
                    for (int i = 0; i < array.size() - 1; i++) {
                        for (int j = i + 1; j < array.size(); j++) {
                            int k = numberOfSeatTypes;
                            while (k-- > 0) {
                                StringBuilder str2 = new StringBuilder();
                                cnt++;
                                str2.append(cnt);
                                str2.append(",");
                                str2.append(array.get(i));
                                str2.append(",");
                                str2.append(array.get(j));
                                str2.append(",");
                               // str2.append(idToType.get(parts[8]));
                                str2.append(parts[8]);
                                str2.append(",");
                                switch (k) {
                                    case 0:
                                        str2.append("1");
                                        break;
                                    case 1:
                                        str2.append("2");
                                        break;
                                    case 2:
                                        str2.append("3");
                                        break;
                                    default:
                                        str2.append(" ");
                                }
                                str2.append(",");
                                str2.append((j - i) * 51 + k * 51 + r.nextInt(51));
                                bufferedWriter2.write(str2.toString());
                                bufferedWriter2.newLine();
                            }

                        }
                    }
                    array.clear();
                }
            }


            System.out.println("Successfully Executed3.");

            bufferedWriter2.close();

//            bufferedWriter1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static void process3() {
        String readFileName = "train_detail.txt";
        String writeFileName = "new_train_detail.txt";
        try (BufferedReader infile
                     = new BufferedReader(new FileReader(readFileName))) {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(writeFileName));
            String[] parts;
            String line;
            while ((line = infile.readLine()) != null) {
                StringBuilder str = new StringBuilder();
                parts = line.split(",");
                str.append(parts[0]);
                str.append(",");
                str.append(parts[8]);
                str.append(",");
                for (int i = 1; i < 5; i++) {
                    if (i == 2)
                        str.append(nameToId.get(parts[i]));
                    else str.append(parts[i]);
                    if (i != 4)
                        str.append(",");
                }
                bufferedWriter.write(str.toString());
                bufferedWriter.newLine();
            }
            System.out.println("Successfully Executed2.");
            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

