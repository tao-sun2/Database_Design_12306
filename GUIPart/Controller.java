package sample;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller {
    static boolean isSuperuser;//True means it is a Superuser who is connecting the database;
    static String currentUserName;
    static String currentDate;

    @FXML
    private TextField textField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button buttonLogin;
    @FXML
    private Button buttonRegister1;
    @FXML
    private Button buttonBack;
    @FXML
    private TextArea textArea;
    @FXML
    private Label label;
    @FXML
    private TitledPane titledPane;
    @FXML
    private Label labelLoginMessage;
    @FXML
    private Label labelAddStation;
    @FXML
    private Label labelDeleteStation;
    @FXML
    private TextField textFieldAddTrainCode;
    @FXML
    private TextField textFieldDeleteTrainCode;
    @FXML
    private TextField textFieldAddStation;
    @FXML
    private TextField textFieldDeleteStation;
    @FXML
    private TextField textFieldAddStationNum;
    @FXML
    private TextField textFieldAddArrive;
    @FXML
    private TextField textFieldAddDepart;
    @FXML
    private TextField searchDepartStation;
    @FXML
    private TextField searchArriveStation;
    @FXML
    private TextField searchDate;
    @FXML
    private TextArea searchD;
    @FXML
    private TextField buyTicket;
    @FXML
    private Button buttonBuyTicket;
    @FXML
    private Button refound;
    @FXML
    private Label currentUser;

    @FXML
    private Button searchOrders;
    @FXML
    private TextArea orders;
    @FXML
    private TextArea searchP;
    @FXML
    private Button searchTrainsP;


    @FXML
    public void clickRegister1() throws IOException {
        Stage stage = (Stage) buttonRegister1.getScene().getWindow();
        stage.close();
        Parent root = FXMLLoader.load(getClass().getResource("register.fxml"));
        System.out.println(textField.getText() + " " + passwordField.getText());
        stage = new Stage();
        stage.setTitle("Register");
        Scene scene = new Scene(root, 399, 489);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void clickRegister2() throws IOException {
        System.out.println("clickRegister2");
    }

    @FXML
    public void clickBack() throws IOException {
        Stage stage = (Stage) buttonBack.getScene().getWindow();
        stage.close();
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        stage = new Stage();
        stage.setTitle("Login");
        Scene scene = new Scene(root, 399, 489);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void clickLogin() throws IOException {
        int value = -1;
        Database12306 database12306 = new Database12306();
        database12306.getConnectionWithCommonUser();
        value = database12306.LoginIn(textField.getText(), passwordField.getText());
        System.out.println(value);
        if (value == 1)
            isSuperuser = true;
        else if (value == 0) isSuperuser = false;
        else {
            labelLoginMessage.setText("登录失败! 请重试");
            return;
        }
        currentUserName = textField.getText();
        database12306.closeConnection();
        Stage stage = (Stage) buttonLogin.getScene().getWindow();
        stage.close();
        Parent root = FXMLLoader.load(getClass().getResource("window.fxml"));
        System.out.println(textField.getText() + " " + passwordField.getText());
        stage = new Stage();
        stage.setTitle("Window");
        Scene scene = new Scene(root, 1134, 774);
        stage.setScene(scene);
        stage.show();
    }


    @FXML
    public void listStationsOfTrain() throws IOException {
        System.out.println("runing");
        Database12306 database12306 = new Database12306();
        if (isSuperuser)
            database12306.getConnectionWithSuperuser();
        else database12306.getConnectionWithCommonUser();
        textArea.setText(database12306.searchStationsOfTrain(textField.getText()));
        textArea.setFont(new Font(20));
        database12306.closeConnection();
    }

    @FXML
    private void quitLogin() throws IOException {
        System.out.println("aaaaaaaaaaaaaa");
        Stage stage = (Stage) titledPane.getScene().getWindow();
        stage.close();
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        stage = new Stage();
        Scene scene = new Scene(root, 399, 489);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void showWarningInformation() throws IOException {
        if (isSuperuser)
            label.setText("您正在使用管理员账号登录");
        else label.setText("警告：普通用户无权限使用此功能");

        textFieldAddStation.setText("");
        labelAddStation.setText("");

        labelDeleteStation.setText("");
        textFieldAddTrainCode.setText("");
        textFieldDeleteTrainCode.setText("");
        textFieldAddStation.setText("");
        textFieldDeleteStation.setText("");
        textFieldAddStationNum.setText("");
        textFieldAddArrive.setText("");
        textFieldAddDepart.setText("");
    }


    @FXML
    private void cleanOldInformation() throws IOException {
        textArea.setText("");
        textField.setText("");
    }

    @FXML
    private void deleteStationOfTrain() throws IOException {
        if (isSuperuser) {
            Database12306 database12306 = new Database12306();
            database12306.getConnectionWithSuperuser();
            int state = database12306.removeStationFormTrain(textFieldDeleteTrainCode.getText(),
                    textFieldDeleteStation.getText());
            database12306.closeConnection();
            if (state == 1)
                labelDeleteStation.setText("Execution Succeed!");
            else labelDeleteStation.setText("Execution Failed!");
        } else {
            labelDeleteStation.setText("Execution Failed!");
        }
    }


    @FXML
    private void addStationOfTrain() throws IOException {
        if (isSuperuser) {
            Database12306 database12306 = new Database12306();
            database12306.getConnectionWithSuperuser();
            int state = database12306.addStationFormTrain(textFieldAddTrainCode.getText(),
                    textFieldAddStation.getText(), Integer.parseInt(textFieldAddStationNum.getText())
                    , textFieldAddArrive.getText(), textFieldAddDepart.getText());
            database12306.closeConnection();
            if (state == 1)
                labelAddStation.setText("Execution Succeed!");
            else labelAddStation.setText("Execution Failed!");
        } else {
            labelAddStation.setText("Execution Failed!");
        }
    }

    @FXML
    private void searchTrainsD() throws IOException {
        Database12306 database = new Database12306();
        database.getConnectionWithCommonUser();
        System.out.println(searchDepartStation.getText());
        System.out.println(searchArriveStation.getText());
        System.out.println(searchDate.getText());
        searchD.setText(database.searchTrainInformation(searchDepartStation.getText()
                , searchArriveStation.getText(), searchDate.getText()));
        currentDate = searchDate.getText();

        buttonBuyTicket.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                database.getConnectionWithCommonUser();
                System.out.println("aaaaa");
                System.out.println(currentUserName + " " + Integer.parseInt(buyTicket.getText())
                        + currentDate + " " + "411302200002240014");

                //  System.out.println(database.Buy("t",5,"2020-5-26","411002200003081020"));

                database.Buy(currentUserName, Integer.parseInt(buyTicket.getText()), currentDate, "411302200002240014");
//                if(!ju)
//                    temp=Integer.parseInt(buyTicket.getText());
                System.out.println(database.carriage_no);
            }
        });
        currentUser.setText("当前用户：" + currentUserName);
        searchOrders.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                database.getConnectionWithCommonUser();
                orders.setText(database.searchReservingOrder(currentUserName));
            }
        });
        String s = database.searchTrainsP("", "");
        searchTrainsP.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                searchP.setText(s);

            }
        });

        refound.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                database.getConnectionWithCommonUser();
                database.Refund(currentUserName, Integer.parseInt(buyTicket.getText()), currentDate, "411302200002240014");

            }
        });


        database.closeConnection();
    }

    @FXML
    private void buyTickets() throws IOException {
    }


}
