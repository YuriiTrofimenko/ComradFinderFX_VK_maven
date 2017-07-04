package org.tyaa.comradfinder;

import java.awt.Dimension;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.tyaa.comradfinder.modules.events.UpdateCandidatesGenerator;
import org.tyaa.comradfinder.screensframework.ScreensController;
import org.tyaa.comradfinder.viewcontroller.FindUsersController;
import org.tyaa.comradfinder.viewcontroller.HomeController;


public class MainApp extends Application {
    
    public static Stage primaryStage;
    
    public static Dimension d;
    
    //
    public static String homeID = "home";
    public static String homeView = "/fxml/Home.fxml";
    
    //
    public static String findUsersID = "find_users";
    public static String findUsersView = "/fxml/FindUsers.fxml";
    
    public static HomeController homeControllerInstance;
    public static FindUsersController findUsersControllerInstance;
    
    public static UpdateCandidatesGenerator updateCandidatesGenerator;

    public static void main(String[] args)
    {
        launch(args);
        //System.out.println(Date.from(Instant.now()).getTime() - Date.from(Instant.now()).getDate() + );
//        WS1 main = new WS1();
//        main.initEntityManager();
//        main.createAndRead();
        //main.createAndRollback();
    }
    
    @Override
    public void start(Stage _primaryStage){
        
        primaryStage = _primaryStage;
        
        //Создаем объект скринс-фреймворка (контейнер представлений)
        ScreensController screensContainer = new ScreensController();
        //Добавляем в него представления главного окна и окна поиска пользователей
        screensContainer.loadScreen(MainApp.homeID, MainApp.homeView);
        screensContainer.loadScreen(MainApp.findUsersID, MainApp.findUsersView);
        
        //Устанавливаем представление экрана входа в качестве текущего
        screensContainer.setScreen(MainApp.homeID);
        
        //Подписываем контроллеры экранов на события обновления данных
        updateCandidatesGenerator = new UpdateCandidatesGenerator();
        updateCandidatesGenerator.addListener(homeControllerInstance);
        
        //Создаем корневой контейнер, помещаем в него наш контейнер представлений,
        //на его базе - сцену, которую подключаем в главный стейдж и отображаем стейдж
        
//        BorderPane root = new BorderPane();
//        
//        AnchorPane menuRoot = new AnchorPane();
//        FXMLLoader loader = new FXMLLoader();
        
        AnchorPane root = new AnchorPane();
        AnchorPane.setTopAnchor(screensContainer, 0.0);
        AnchorPane.setRightAnchor(screensContainer, 0.0);
        AnchorPane.setLeftAnchor(screensContainer, 0.0);
        AnchorPane.setBottomAnchor(screensContainer, 0.0);
        root.getChildren().addAll(screensContainer);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("ComradFinderFx VK");
        //primaryStage.setFullScreen(true);
        primaryStage.setMaximized(true);
        
//        FxDemo.d = Toolkit.getDefaultToolkit().getScreenSize();
//        FxDemo.primaryStage.setMaximized(false);
//        FxDemo.primaryStage.setWidth(400);
//        FxDemo.primaryStage.setHeight(300);
//        FxDemo.primaryStage.setX(d.width/2-(primaryStage.getWidth()/2));
//        FxDemo.primaryStage.setY(d.height/2-(primaryStage.getHeight()/2));
        
        primaryStage.show();
    }

}
