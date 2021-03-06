/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.viewcontroller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import org.tyaa.comradfinder.screensframework.ControlledScreen;
import org.tyaa.comradfinder.screensframework.ScreensController;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.tools.ValueExtractor;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.tyaa.comradfinder.MainApp;
import static org.tyaa.comradfinder.MainApp.homeControllerInstance;
import org.tyaa.comradfinder.model.TypicalWords;
import org.tyaa.comradfinder.model.VKCandidate;
import org.tyaa.comradfinder.modules.ExcelSaver;
import org.tyaa.comradfinder.modules.XmlExporter;
import org.tyaa.comradfinder.modules.XmlImporter;
import org.tyaa.comradfinder.modules.events.UpdateCandidatesEvent;
import org.tyaa.comradfinder.modules.events.interfaces.UpdateCandidatesListener;
import org.tyaa.comradfinder.modules.exception.FailJsonFetchException;
import org.tyaa.comradfinder.modules.facades.ModelBuilder;
import org.tyaa.comradfinder.screensframework.ProgressForm;
import org.tyaa.comradfinder.utils.Cloner;
import org.tyaa.comradfinder.utils.MapUtils;
import org.tyaa.comradfinder.utils.XSDValidator;
import org.tyaa.comradfinder.viewmodel.CandidateModel;
import org.tyaa.comradfinder.viewmodel.VariantModel;
import org.xml.sax.SAXException;


/**
 * FXML Controller class
 *
 * @author Yurii
 */
public class HomeController implements
        Initializable
        , ControlledScreen
        , UpdateCandidatesListener{

    /*Внедренные хендлеры элементов UI*/
    
    @FXML
    Label groupIdLabel;
    
    @FXML
    Button createModelButton;
    
    @FXML
    Button useModelButton;
    
    //Внедренные ссылки на исходную таблицу модели типичных слов
    //и на ее колонки
    @FXML
    private TableView sourceModelTableView;
    @FXML
    private TableColumn srcCategoryTableColumn;
    @FXML
    private TableColumn srcVariantTableColumn;
    @FXML
    private TableColumn srcQuantityTableColumn;
    
    //Внедренные ссылки на рабочую таблицу модели типичных слов
    //и на ее колонки
    @FXML
    private TableView workModelTableView;
    @FXML
    private TableColumn workCategoryTableColumn;
    @FXML
    private TableColumn workVariantTableColumn;
    @FXML
    private TableColumn workQuantityTableColumn;
    
    //Внедренные ссылки на таблицу пользователей,
    //отобранных в кандидаты на приглашение в группу
    @FXML
    private TableView usersTableView;
    @FXML
    private TableColumn userIdTableColumn;
    @FXML
    private TableColumn fNameTableColumn;
    @FXML
    private TableColumn lNameTableColumn;
    @FXML
    private TableColumn scoreTableColumn;
    
    //Флаг "кнопки активны"
    private boolean mButtonsEnable;
    
    //Исходная модель типичных слов
    private TypicalWords mSrcTypicalWords;
    
    //Рабочая модель типичных слов
    private TypicalWords mWorkTypicalWords;
    //Список кандидатов на приглашение
    private List<VKCandidate> mVKCandidateList;
    
    //Выбранные объекты
    private VariantModel mSelectedVariantModel;
    private CandidateModel mSelectedCandidateModel;
    
    //Наблюдабельный список объектов VariantModel
    ObservableList<VariantModel> mSrcVariantObservableList;
    ObservableList<VariantModel> mWorkVariantObservableList;
    ObservableList<CandidateModel> mCandidateModelObservableList;
    
    ScreensController myController;
    ValidationSupport validationSupport;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        //myLabel.setText("None");
        MainApp.homeControllerInstance = this;
        
        //
        groupIdLabel
            .textProperty()
            .bindBidirectional(MainApp.globalModel.groupIdProperty());
        
        //Инициализация пустым наблюдабельным списком
        mSrcVariantObservableList = FXCollections.observableArrayList();
        mWorkVariantObservableList = FXCollections.observableArrayList();
        mCandidateModelObservableList = FXCollections.observableArrayList();
        
        mSrcTypicalWords = new TypicalWords();
        mWorkTypicalWords = new TypicalWords();
        
        sourceModelTableView.setItems(mSrcVariantObservableList);
        workModelTableView.setItems(mWorkVariantObservableList);
        usersTableView.setItems(mCandidateModelObservableList);

        /* Привязка полей источника данных типа VariantModel
        к колонкам исходной таблицы модели типичных слов */
        
        srcCategoryTableColumn.setCellValueFactory(
                new PropertyValueFactory<VariantModel, String>("category")
        );
        srcVariantTableColumn.setCellValueFactory(
                new PropertyValueFactory<VariantModel, String>("variant")
        );
        srcQuantityTableColumn.setCellValueFactory(
                new PropertyValueFactory<VariantModel, String>("quantity")
        );
        
        /* Привязка полей источника данных типа VariantModel
        к колонкам рабочей таблицы модели типичных слов */
        
        workCategoryTableColumn.setCellValueFactory(
                new PropertyValueFactory<VariantModel, String>("category")
        );
        workVariantTableColumn.setCellValueFactory(
                new PropertyValueFactory<VariantModel, String>("variant")
        );
        workQuantityTableColumn.setCellValueFactory(
                new PropertyValueFactory<VariantModel, String>("quantity")
        );
        
        /* Привязка полей источника данных типа CandidateModel
        к колонкам таблицы пользователей-кандидатов */
        
        userIdTableColumn.setCellValueFactory(
                new PropertyValueFactory<CandidateModel, String>("uid")
        );
        fNameTableColumn.setCellValueFactory(
                new PropertyValueFactory<CandidateModel, String>("fname")
        );
        lNameTableColumn.setCellValueFactory(
                new PropertyValueFactory<CandidateModel, String>("lname")
        );
        scoreTableColumn.setCellValueFactory(
                new PropertyValueFactory<CandidateModel, String>("score")
        );
        //
        Platform.runLater(() -> {
            MainApp.primaryStage.getScene().getAccelerators()
                .put(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY), new Runnable() {
                    @Override
                    public void run() {


                        if (usersTableView.getSelectionModel().getSelectedItem() != null) {

                            int rowIndex = usersTableView.getSelectionModel().getSelectedIndex();
                            CandidateModel dataRow = (CandidateModel) usersTableView.getItems().get(rowIndex);
                            final Clipboard clipboard = Clipboard.getSystemClipboard();
                            final ClipboardContent content = new ClipboardContent();
                            if(usersTableView.getSelectionModel().isSelected(rowIndex, userIdTableColumn)){
                                System.out.println(dataRow.getUid());
                                content.putString(dataRow.getUid().toString());
                            }
                            else{
                                //System.out.println(dataRow.getSelected());
                                //content.putString(dataRow.getSelected());
                            }
                            clipboard.setContent(content);
                        }
                    }
                });
            });
        //В таблицах моделей слов выводить названия категорий разными цветами
        /*srcVariantTableColumn.setCellFactory(column ->{
            
            return new TableCell<VariantModel, String>(){
                @Override
                protected void updateItem(String item, boolean empty)
                {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        SimpleDateFormat formatter =
                            new SimpleDateFormat("yyyy.MM.dd");
                        SimpleDateFormat formatter2 =
                            new SimpleDateFormat("dd.MM.yyyy");
                        Date date = null;
                        try {
                            date = formatter.parse(item);
                        } catch (ParseException ex) {
                            setText("Неверный формат даты");
                        }
                        if (date != null) {
                            setText(formatter2.format(date));
                            if (
                                (
                                    Date.from(Instant.now()).getTime()
                                    - date.getTime()
                                ) < Settings.getCleaningTypicalCycleTime()
                            ) {
                                setTextFill(Color.BLACK);
                            } else if(
                                (
                                    Date.from(Instant.now()).getTime()
                                    - date.getTime()
                                ) < Settings.getCleaningOverdueCycleTime()){
                                setTextFill(Color.ORANGE);
                            } else {
                                setTextFill(Color.RED);
                            }
                        }
                    }
                }
            };
        });*/
    }    

    @Override
    public void setScreenParent(ScreensController screenParent) {
        myController = screenParent;
    }
    
    /*
    * Действия с моделью типичных слов
    * наличных пользователей выбранной группы
    */
    
    //Действие отображения диалогового окна создания модели
    //по заданной группе
    @FXML
    private void showCreateModelDialog(){
            
        TextInputDialog dialog =
            new TextInputDialog("");
        dialog.setTitle("Создание модели");
        dialog.setHeaderText("Для какой группы ВК создать модель совокупного пользователя?");
        dialog.setContentText("Введите id или псевдоним группы (например, 137118920 или tehnokom_su): ");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(
            groupIdString -> {

                boolean hasNumFormatErrors = false;

                try{

                    //Получение списка наличных пользователей группы
                    //происходит внутри метода вызываемого здесь задания
                    Task buildModelTask =
                        ModelBuilder.buildModel(groupIdString);

                    ProgressForm pForm = new ProgressForm();

                    // binds progress of progress bars to progress of task:
                    pForm.activateProgressBar(buildModelTask);

                    // this method would get the result of the task
                    // and update the UI based on its value
                    buildModelTask.setOnSucceeded(event -> {
                        pForm.getDialogStage().close();
                        //Активируем кнопки основного экрана
                        toggleButtonsEnable();
                        //Загружаем модель типичных слов в объект Java
                        //из только что сохраненного файла
                        //Читаем набор типичных слов из файла XML в Java объект
                        try {
                            try {
                                
                                mSrcTypicalWords = XmlImporter.getTypicalWords("TypicalWords.xml");
                                MainApp.globalModel.curerntTypicalWords = mSrcTypicalWords;
                                MainApp.globalModel.groupIdProperty().setValue(groupIdString);
                            } catch (SAXException ex) {
                                Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (ParserConfigurationException ex) {
                                Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } catch (IOException | XMLStreamException ex) {
                            Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (mSrcTypicalWords != null) {

                            fillVariantObservableList(mSrcTypicalWords, mSrcVariantObservableList);
                        }

                        try {
                            
                            Task fillGroupInvitedTask =
                                ModelBuilder.fillGroupInvitedUsersIds(mSrcTypicalWords.mGroupId);

                            ProgressForm pForm2 = new ProgressForm();

                            // binds progress of progress bars to progress of task:
                            pForm2.activateProgressBar(fillGroupInvitedTask);

                            // this method would get the result of the task
                            // and update the UI based on its value
                            fillGroupInvitedTask.setOnSucceeded(event4 -> {
                                pForm2.getDialogStage().close();
                            });

                            toggleButtonsEnable();

                            pForm2.getDialogStage().show();

                            Thread thread = new Thread(fillGroupInvitedTask);
                            thread.setDaemon(true);
                            thread.start();
                            
                        } catch (FailJsonFetchException ex) {
                            
                            Alert warningAlert =
                                new Alert(Alert.AlertType.INFORMATION);
                            warningAlert.setTitle("Предупреждение");
                            warningAlert.setHeaderText("Не был получен список ранее приглашенных");
                            warningAlert.setContentText("Не удалось получить по сети список пользователей, ранее отмеченных как пришлашенные в группу. Возможно, таковых еще нет в удаленной БД");
                            warningAlert.showAndWait();
                        }
                    });

                    toggleButtonsEnable();

                    pForm.getDialogStage().show();

                    Thread thread = new Thread(buildModelTask);
                    thread.setDaemon(true);
                    thread.start();
                } catch (NumberFormatException ex){

                    hasNumFormatErrors = true;
                }
                if (hasNumFormatErrors) {

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("Сбой создания модели");
                    alert.setContentText("Сетевой ресурс недоступен или введен некорректный идентификатор группы");
                    alert.showAndWait();
                }
            }
        );
    }
    
    //Действие отображения диалогового окна сохранения модели
    //в указанной папке файловой системы
    @FXML
    private void showSaveModelDialog(){
        
        //TODO Добавить диалог, предлагающий сохранение данных при выходе
        //из приложения
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("XML Files", "*.xml")
                ,new ExtensionFilter("All Files", "*.*"));
        fileChooser.setTitle("Сохранение файла модели");
        fileChooser.setInitialFileName("TypicalWords.xml");
        File savedFile = fileChooser.showSaveDialog(null);

        if (savedFile != null) {
            
            int savingResult = 0;

            try {

                //TODO Добавить в класс модели TypicalWords
                //поле счетчика вариантов. Если оно == 0 - модель пуста
                
                if (mWorkTypicalWords != null
                    && (!mWorkTypicalWords.mAboutMap.isEmpty()
                        || !mWorkTypicalWords.mActivityMap.isEmpty()
                        || !mWorkTypicalWords.mAlcoholMap.isEmpty()
                        || !mWorkTypicalWords.mBooksMap.isEmpty())
                        || !mWorkTypicalWords.mInspiredByMap.isEmpty()
                        || !mWorkTypicalWords.mInterestMap.isEmpty()
                        || !mWorkTypicalWords.mLifeMainMap.isEmpty()
                        || !mWorkTypicalWords.mMoviesMap.isEmpty()
                        || !mWorkTypicalWords.mMusicMap.isEmpty()
                        || !mWorkTypicalWords.mPeopleMainMap.isEmpty()
                        || !mWorkTypicalWords.mPoliticalMap.isEmpty()
                        || !mWorkTypicalWords.mReligionMap.isEmpty()
                        || !mWorkTypicalWords.mSmokingMap.isEmpty()) {
                    
                    XmlExporter.TypicalWordsToXml(
                        mWorkTypicalWords
                            , savedFile.getAbsolutePath());
                } else if (mSrcTypicalWords != null
                            && (!mSrcTypicalWords.mAboutMap.isEmpty()
                                || !mSrcTypicalWords.mActivityMap.isEmpty()
                                || !mSrcTypicalWords.mAlcoholMap.isEmpty()
                                || !mSrcTypicalWords.mBooksMap.isEmpty())
                                || !mSrcTypicalWords.mInspiredByMap.isEmpty()
                                || !mSrcTypicalWords.mInterestMap.isEmpty()
                                || !mSrcTypicalWords.mLifeMainMap.isEmpty()
                                || !mSrcTypicalWords.mMoviesMap.isEmpty()
                                || !mSrcTypicalWords.mMusicMap.isEmpty()
                                || !mSrcTypicalWords.mPeopleMainMap.isEmpty()
                                || !mSrcTypicalWords.mPoliticalMap.isEmpty()
                                || !mSrcTypicalWords.mReligionMap.isEmpty()
                                || !mSrcTypicalWords.mSmokingMap.isEmpty()) {
                    
                    XmlExporter.TypicalWordsToXml(
                        mSrcTypicalWords
                            , savedFile.getAbsolutePath());
                } else {
                
                    Alert warningAlert =
                        new Alert(Alert.AlertType.INFORMATION);
                    warningAlert.setTitle("Предупреждение");
                    warningAlert.setHeaderText("Модель не сохранена в файл");
                    warningAlert.setContentText("Нечего сохранять");
                    warningAlert.showAndWait();
                    return;
                }
            }
            catch(IOException e) {

                savingResult = -1;
            }
            catch(XMLStreamException e) {

                savingResult = -1;
            }
            
            if (savingResult == 0) {
                
                Alert infoAlert =
                    new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Информация");
                infoAlert.setHeaderText("Модель сохранена в файл");
                infoAlert.setContentText(
                    "Сохранение файла модели "
                    + savedFile.toString()
                    + " завершено успешно"
                );
                infoAlert.showAndWait();
            } else {
            
                Alert errorAlert =
                    new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Ощибка");
                errorAlert.setHeaderText("Модель не сохранена в файл");
                errorAlert.setContentText("При попытке сохранить файл произошла ошибка");
                errorAlert.showAndWait();
            }
        } else {
            
            Alert infoAlert =
                new Alert(Alert.AlertType.INFORMATION);
            infoAlert.setTitle("Информация");
            infoAlert.setHeaderText("Модель не сохранена в файл");
            infoAlert.setContentText("Сохранение файла модели отменено");
            infoAlert.showAndWait();
        }
    }
    
    @FXML
    private void loadModelAction(ActionEvent event){
    
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("XML Files", "*.xml")
                ,new ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            
            if (
                XSDValidator.validateXMLSchema(
                    XSDValidator.DEFAULT_XSD_PATH + "TypicalWords.xsd"
                    , selectedFile.getAbsolutePath())
                ) {

                try {

                    //System.out.println("File selected: " + selectedFile.getAbsolutePath());
                    mSrcTypicalWords =
                        XmlImporter.getTypicalWords(selectedFile.getAbsolutePath());
                    MainApp.globalModel.curerntTypicalWords = mSrcTypicalWords;

                    if (mSrcTypicalWords != null) {

                        fillVariantObservableList(mSrcTypicalWords, mSrcVariantObservableList);
                        MainApp.globalModel.groupIdProperty().setValue(mSrcTypicalWords.mGroupId);
                        try {
                            //
                            Task fillGroupMembersTask =
                                ModelBuilder.fillGroupMembersIds(mSrcTypicalWords.mGroupId);

                            ProgressForm pForm = new ProgressForm();

                            // binds progress of progress bars to progress of task:
                            pForm.activateProgressBar(fillGroupMembersTask);

                            // this method would get the result of the task
                            // and update the UI based on its value
                            fillGroupMembersTask.setOnSucceeded(event2 -> {
                                pForm.getDialogStage().close();
                            });

                            toggleButtonsEnable();

                            pForm.getDialogStage().show();

                            Thread thread = new Thread(fillGroupMembersTask);
                            thread.setDaemon(true);
                            thread.start();

                        } catch (Exception ex) {

                            Alert warningAlert =
                                new Alert(Alert.AlertType.INFORMATION);
                            warningAlert.setTitle("Предупреждение");
                            warningAlert.setHeaderText("Не был получен список участников группы");
                            warningAlert.setContentText("Не удалось получить по сети список наличных участников группы");
                            warningAlert.showAndWait();
                        }

                        try {

                            Task fillGroupInvitedTask =
                                ModelBuilder.fillGroupInvitedUsersIds(mSrcTypicalWords.mGroupId);

                            ProgressForm pForm = new ProgressForm();

                            // binds progress of progress bars to progress of task:
                            pForm.activateProgressBar(fillGroupInvitedTask);

                            // this method would get the result of the task
                            // and update the UI based on its value
                            fillGroupInvitedTask.setOnSucceeded(event3 -> {
                                pForm.getDialogStage().close();
                            });

                            toggleButtonsEnable();

                            pForm.getDialogStage().show();

                            Thread thread = new Thread(fillGroupInvitedTask);
                            thread.setDaemon(true);
                            thread.start();

                        } catch (FailJsonFetchException ex) {

                            Alert warningAlert =
                                new Alert(Alert.AlertType.INFORMATION);
                            warningAlert.setTitle("Предупреждение");
                            warningAlert.setHeaderText("Не был получен список ранее приглашенных");
                            warningAlert.setContentText("Не удалось получить по сети список пользователей, ранее отмеченных как пришлашенные в группу. Возможно, таковых еще нет в удаленной БД");
                            warningAlert.showAndWait();
                        }
                    }
                } catch (IOException | XMLStreamException | SAXException | ParserConfigurationException ex) {

                    Alert errorAlert =
                        new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Ощибка");
                    errorAlert.setHeaderText("Модель не загружена");
                    errorAlert.setContentText("При попытке прочесть файл произошла ошибка");
                    errorAlert.showAndWait();
                }
            } else {
            
                Alert errorAlert =
                    new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Ощибка");
                errorAlert.setHeaderText("Модель не загружена");
                errorAlert.setContentText("Некорректная структура файла");
                errorAlert.showAndWait();
            }
        }
        else {
            
            Alert infoAlert =
                new Alert(Alert.AlertType.INFORMATION);
            infoAlert.setTitle("Информация");
            infoAlert.setHeaderText("Модель не загружена");
            infoAlert.setContentText("Загрузка исходной модели из файла отменена");
            infoAlert.showAndWait();
        }
    }
    
    @FXML
    private void useModelAction(ActionEvent event){
    
        if (mSrcTypicalWords != null) {
            
            //Глубокое клонирование исходного объекта модели типичных слов
            //и присвоение ссылки на клон полю рабочей модели типичных слов
            mWorkTypicalWords =
                (TypicalWords) Cloner.deepClone(mSrcTypicalWords);
            
            fillVariantObservableList(mWorkTypicalWords, mWorkVariantObservableList);
        }
    }
    
    /*
    * Действия со списком кандидатов
    */
    
    //Действие отображения диалогового окна сохранения списка кандидатов
    //в файл XML в указанной папке файловой системы
    @FXML
    private void showSaveUserslDialog(){
        
        //TODO Добавить диалог, предлагающий сохранение данных при выходе
        //из приложения
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("XML Files", "*.xml")
                ,new ExtensionFilter("All Files", "*.*"));
        fileChooser.setTitle("Сохранение файла кандидатов");
        fileChooser.setInitialFileName("VKCandidates.xml");
        File savedFile = fileChooser.showSaveDialog(null);

        if (savedFile != null) {
            
            int savingResult = 0;

            try {
                
                if (mVKCandidateList != null
                    && mVKCandidateList.size() > 0) {
                    
                    XmlExporter.VKCandidatesToXml(
                        mVKCandidateList
                            , savedFile.getAbsolutePath());
                } else {
                
                    Alert warningAlert =
                        new Alert(Alert.AlertType.INFORMATION);
                    warningAlert.setTitle("Предупреждение");
                    warningAlert.setHeaderText("Список не сохранен в файл");
                    warningAlert.setContentText("Нечего сохранять");
                    warningAlert.showAndWait();
                    return;
                }
            }
            catch(IOException | XMLStreamException e) {

                savingResult = -1;
            }
            
            if (savingResult == 0) {
                
                Alert infoAlert =
                    new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Информация");
                infoAlert.setHeaderText("Список сохранен в файл");
                infoAlert.setContentText(
                    "Сохранение файла списка "
                    + savedFile.toString()
                    + " завершено успешно"
                );
                infoAlert.showAndWait();
            } else {
            
                Alert errorAlert =
                    new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Ощибка");
                errorAlert.setHeaderText("Список не сохранен в файл");
                errorAlert.setContentText("При попытке сохранить файл произошла ошибка");
                errorAlert.showAndWait();
            }
        } else {
            
            Alert infoAlert =
                new Alert(Alert.AlertType.INFORMATION);
            infoAlert.setTitle("Информация");
            infoAlert.setHeaderText("Список не сохранен в файл");
            infoAlert.setContentText("Сохранение файла списка отменено");
            infoAlert.showAndWait();
        }
    }
    
    //TODO add validation XML by XSD
    @FXML
    private void loadUsersAction(ActionEvent event){
    
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("XML Files", "*.xml")
                ,new ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            
            if (
                XSDValidator.validateXMLSchema(
                    XSDValidator.DEFAULT_XSD_PATH + "VKCandidates.xsd"
                    , selectedFile.getAbsolutePath())
                ) {

                try {

                    //System.out.println("File selected: " + selectedFile.getAbsolutePath());
                    mVKCandidateList =
                        XmlImporter.getVKCandidates(selectedFile.getAbsolutePath());

                    if (mVKCandidateList != null) {

                        fillCandidateObservableList(mVKCandidateList, mCandidateModelObservableList);
                    }
                } catch (IOException | XMLStreamException | SAXException | ParserConfigurationException ex) {
                    
                    Alert errorAlert =
                        new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Ощибка");
                    errorAlert.setHeaderText("Список не загружен");
                    errorAlert.setContentText("При попытке прочесть файл произошла ошибка");
                    errorAlert.showAndWait();
                }
            } else {
            
                Alert errorAlert =
                    new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Ощибка");
                errorAlert.setHeaderText("Список не загружен");
                errorAlert.setContentText("Некорректная структура файла");
                errorAlert.showAndWait();
            }
        }
        else {
            
            Alert infoAlert =
                new Alert(Alert.AlertType.INFORMATION);
            infoAlert.setTitle("Информация");
            infoAlert.setHeaderText("Список не загружен");
            infoAlert.setContentText("Загрузка списка кандидатов из файла отменен");
            infoAlert.showAndWait();
        }
    }
    
    //Действие отображения диалогового окна сохранения списка кандидатов
    //в файл Excell в указанной папке файловой системы
    @FXML
    private void showExportUserslDialog(){
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("XML Files", "*.xlsx")
                ,new ExtensionFilter("All Files", "*.*"));
        fileChooser.setTitle("Экспорт файла кандидатов в Excell");
        fileChooser.setInitialFileName("VKCandidates.xlsx");
        File savedFile = fileChooser.showSaveDialog(null);

        if (savedFile != null) {
            
            int savingResult = 0;

            try {
                
                if (mVKCandidateList != null
                    && mVKCandidateList.size() > 0) {
                    
                    (new ExcelSaver()).saveCandidates(
                        mVKCandidateList
                            , savedFile.getAbsolutePath());
                } else {
                
                    Alert warningAlert =
                        new Alert(Alert.AlertType.INFORMATION);
                    warningAlert.setTitle("Предупреждение");
                    warningAlert.setHeaderText("Список не экспортирован в Excell");
                    warningAlert.setContentText("Нечего сохранять");
                    warningAlert.showAndWait();
                    return;
                }
            }
            catch(IOException e) {

                savingResult = -1;
            }
            
            if (savingResult == 0) {
                
                Alert infoAlert =
                    new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Информация");
                infoAlert.setHeaderText("Список экспортирован в Excell");
                infoAlert.setContentText(
                    "Экспорт файла списка "
                    + savedFile.toString()
                    + " завершен успешно"
                );
                infoAlert.showAndWait();
            } else {
            
                Alert errorAlert =
                    new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Ощибка");
                errorAlert.setHeaderText("Список не экспортирован в Excell");
                errorAlert.setContentText("При попытке сохранить файл произошла ошибка");
                errorAlert.showAndWait();
            }
        } else {
            
            Alert infoAlert =
                new Alert(Alert.AlertType.INFORMATION);
            infoAlert.setTitle("Информация");
            infoAlert.setHeaderText("Список не экспортирован в Excell");
            infoAlert.setContentText("Экспорт файла списка отменен");
            infoAlert.showAndWait();
        }
    }
    
    @FXML
    private void addUserToInvited(ActionEvent event){
    
        mSelectedCandidateModel =
            (CandidateModel) usersTableView.getSelectionModel().getSelectedItem();
        
        if (mSelectedCandidateModel != null) {
            
            final Integer uidInteger = mSelectedCandidateModel.getUid();
            VKCandidate selectedVKCandidate = null;
            for (VKCandidate vKCandidate : mVKCandidateList) {
                if (vKCandidate.getUID().intValue() == uidInteger.intValue()) {
                    selectedVKCandidate = vKCandidate;
                }
            }

            if (uidInteger != null && selectedVKCandidate != null) {

                boolean delResult = false;

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Отметить кандидата как приглашенного");
                alert.setHeaderText("Требуется подтверждение");
                alert.setContentText("Вы действительно хотите занести данного пользователя в список приглашенных в удаленной БД?");

                Optional<ButtonType> result = alert.showAndWait();

                if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
                    
                    boolean addGroupInvitedUserResult = false;
                    
                    try {
                        addGroupInvitedUserResult =
                                ModelBuilder.addGroupInvitedUser(
                                        MainApp.globalModel.getGroupId()
                                        , selectedVKCandidate
                                );
                    } catch (FailJsonFetchException ex) {
                        addGroupInvitedUserResult = false;
                    }
                    
                    if(addGroupInvitedUserResult){

                        delResult = mVKCandidateList.remove(selectedVKCandidate);

                        if (delResult != false) {

                            fillCandidateObservableList(
                                mVKCandidateList
                                , mCandidateModelObservableList);

                            alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Информация");
                            alert.setHeaderText("Выбранный кандидат успешно занесен в удаленную БД приглашенных");
                            //Показать сообщение и ждать клика по кнопке Ok
                            alert.showAndWait();
                        } else {

                            alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Ошибка");
                            alert.setHeaderText("Произошел сбой в работе приложения. Действие не выполнено");
                            alert.showAndWait();
                        }
                    } else {

                        alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText("Произошел сбой при отправке данных в удаленную БД. Действие не выполнено");
                        alert.showAndWait();
                    }
                } else {

                    Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                    infoAlert.setTitle("Информация");
                    infoAlert.setHeaderText("Процесс занесения в приглашенные отменен");
                    infoAlert.setContentText("Действие не выполнено");
                    infoAlert.showAndWait();
                }
            } else {

                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Информация");
                infoAlert.setHeaderText("Процесс удаления отменен автоматически");
                infoAlert.setContentText("Удаляемый объект отсутствует в объекте-списке");
                infoAlert.showAndWait();
            }
        } else {
        
            Alert warningAlert =
                new Alert(Alert.AlertType.WARNING);
            warningAlert.setTitle("Предупреждение");
            warningAlert.setHeaderText("Не выбрана ни одна строка в таблице кандидатов");
            warningAlert.setContentText("Выделите одну строку");
            warningAlert.showAndWait();
        }
    }
    
    /**/
    
    @FXML
    private void showEditVariantDlgAction(ActionEvent event){
    
        mSelectedVariantModel =
            (VariantModel) workModelTableView.getSelectionModel().getSelectedItem();
        
        if (mSelectedVariantModel != null) {
            
            String oldVariantText = mSelectedVariantModel.getVariant();
            
            if(mSelectedVariantModel.getCategory().equals("political")
                || mSelectedVariantModel.getCategory().equals("people main")
                || mSelectedVariantModel.getCategory().equals("life main")
                || mSelectedVariantModel.getCategory().equals("smoking")
                || mSelectedVariantModel.getCategory().equals("alcohol")
                    ){
                
                Class c = TypicalWords.class; 
                //Field[] publicFields = c.getFields();
                Field fieldTry = null;
                try {
                    
                    switch (mSelectedVariantModel.getCategory()) {
                        case "political":
                            fieldTry = c.getField("mPoliticalMap");
                            break;
                        case "people main":
                            fieldTry = c.getField("mPeopleMainMap");
                            break;
                        case "life main":
                            fieldTry = c.getField("mLifeMainMap");
                            break;
                        case "smoking":
                            fieldTry = c.getField("mSmokingMap");
                            break;
                        case "alcohol":
                            fieldTry = c.getField("mAlcoholMap");
                            break;
                        default:
                            break;
                    }
                } catch (NoSuchFieldException ex) {
                    Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                }

                Map<String, Integer> forEditMap = null;
                String forEditFieldName = null;

                if(fieldTry != null){
                //FOR_LABEL : for (Field field : publicFields) {

                    final Field field = fieldTry;
                    Class fieldType = field.getType(); 
                    //System.out.println("Имя: " + field.getName()); 
                    //System.out.println("Тип: " + fieldType.getName());

                    try {
                        if (field.get(mWorkTypicalWords) instanceof Map<?,?>) {

                            final Map<Integer, Integer> currentMap =
                                (Map)field.get(mWorkTypicalWords);

                            if (currentMap != null && !currentMap.isEmpty()) {

                                List<String> choices = null;
                                Integer oldVariantIntegerTry = null;

                                switch (mSelectedVariantModel.getCategory()) {
                                    case "political":
                                        choices =
                                            new ArrayList<>(TypicalWords.mPoliticalMapping.values());
                                        oldVariantIntegerTry = (Integer) MapUtils
                                            .getKeysByValue(TypicalWords.mPoliticalMapping, oldVariantText)
                                            .toArray()[0];
                                        break;
                                    case "people main":
                                        choices =
                                            new ArrayList<>(TypicalWords.mPeopleMainMapping.values());
                                        oldVariantIntegerTry = (Integer) MapUtils
                                            .getKeysByValue(TypicalWords.mPeopleMainMapping, oldVariantText)
                                            .toArray()[0];
                                        break;
                                    case "life main":
                                        choices =
                                            new ArrayList<>(TypicalWords.mLifeMainMapping.values());
                                        oldVariantIntegerTry = (Integer) MapUtils
                                            .getKeysByValue(TypicalWords.mLifeMainMapping, oldVariantText)
                                            .toArray()[0];
                                        break;
                                    case "smoking":
                                        choices =
                                            new ArrayList<>(TypicalWords.mSmokingMapping.values());
                                        oldVariantIntegerTry = (Integer) MapUtils
                                            .getKeysByValue(TypicalWords.mSmokingMapping, oldVariantText)
                                            .toArray()[0];
                                        break;
                                    case "alcohol":
                                        choices =
                                            new ArrayList<>(TypicalWords.mAlcoholMapping.values());
                                        oldVariantIntegerTry = (Integer) MapUtils
                                            .getKeysByValue(TypicalWords.mAlcoholMapping, oldVariantText)
                                            .toArray()[0];
                                        break;
                                    default:
                                        break;
                                }

                                final Integer oldVariantInteger =
                                    oldVariantIntegerTry;

                                if (choices != null
                                    && oldVariantInteger != null
                                    && currentMap.containsKey(oldVariantInteger)) {

                                    ChoiceDialog<String> dialog =
                                        new ChoiceDialog<>(oldVariantText, choices);
                                    dialog.setTitle("Изменение выбора варианта");
                                    dialog.setHeaderText("Какой новый выбор варианта?");
                                    dialog.setContentText("Выберите один из пунктов: ");

                                    Optional<String> result = dialog.showAndWait();

                                    result.ifPresent(
                                        editedVariantString -> {

                                            Integer editedVariantIntegerTry = null;

                                            switch (mSelectedVariantModel.getCategory()) {
                                                case "political":
                                                    editedVariantIntegerTry =
                                                        MapUtils
                                                            .getKeysByValue(TypicalWords.mPoliticalMapping, editedVariantString)
                                                            .iterator()
                                                            .next();
                                                    break;
                                                case "people main":
                                                    editedVariantIntegerTry =
                                                        MapUtils
                                                            .getKeysByValue(TypicalWords.mPeopleMainMapping, editedVariantString)
                                                            .iterator()
                                                            .next();
                                                    break;
                                                case "life main":
                                                    editedVariantIntegerTry =
                                                        MapUtils
                                                            .getKeysByValue(TypicalWords.mLifeMainMapping, editedVariantString)
                                                            .iterator()
                                                            .next();
                                                    break;
                                                case "smoking":
                                                    editedVariantIntegerTry =
                                                        MapUtils
                                                            .getKeysByValue(TypicalWords.mSmokingMapping, editedVariantString)
                                                            .iterator()
                                                            .next();
                                                    break;
                                                case "alcohol":
                                                    editedVariantIntegerTry =
                                                        MapUtils
                                                            .getKeysByValue(TypicalWords.mAlcoholMapping, editedVariantString)
                                                            .iterator()
                                                            .next();
                                                    break;
                                                default:
                                                    break;
                                            }

                                            final Integer editedVariantInteger = editedVariantIntegerTry;

                                            if (editedVariantInteger != null) {

                                                if (!currentMap.containsKey(editedVariantInteger)) {

                                                    currentMap.put(editedVariantInteger, currentMap.remove(oldVariantInteger));
                                                    try {
                                                        field.set(mWorkTypicalWords, currentMap);
                                                    } catch (IllegalArgumentException ex) {
                                                        Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                                                    } catch (IllegalAccessException ex) {
                                                        Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                                                    }
                                                    fillVariantObservableList(mWorkTypicalWords, mWorkVariantObservableList);
                                                } else {

                                                    Alert warningAlert =
                                                        new Alert(Alert.AlertType.WARNING);
                                                    warningAlert.setTitle("Предупреждение");
                                                    warningAlert.setHeaderText("Выбор варианта не изменен");
                                                    warningAlert.setContentText("В модели уже есть вариант с таким выбором");
                                                    warningAlert.showAndWait();
                                                }
                                            }
                                    });
                                }
                            }
                        }
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } 
            } else {
            
                TextInputDialog dialog =
                    new TextInputDialog(oldVariantText);
                dialog.setTitle("Изменение текста варианта");
                dialog.setHeaderText("Какой новый текст варианта?");
                dialog.setContentText("Введите строку длинной не менее двух символов: ");

                Optional<String> result = dialog.showAndWait();

                result.ifPresent(
                    editedVariantString -> {

                        Pattern pattern = 
                            Pattern.compile(".{3,}");
                        if (pattern.matcher(editedVariantString).matches()) {

                            Class c = TypicalWords.class; 
                            Field[] publicFields = c.getFields();

                            FOR_LABEL : for (Field field : publicFields) {

                                Class fieldType = field.getType(); 
                                //System.out.println("Имя: " + field.getName()); 
                                //System.out.println("Тип: " + fieldType.getName());

                                try {
                                    if (field.get(mWorkTypicalWords) instanceof Map<?,?>) {

                                        if (field.getName().equals("mInterestMap")
                                            || field.getName().equals("mActivityMap")
                                            || field.getName().equals("mAboutMap")
                                            || field.getName().equals("mReligionMap")
                                            || field.getName().equals("mInspiredByMap")
                                            || field.getName().equals("mBooksMap")
                                            || field.getName().equals("mMusicMap")
                                            || field.getName().equals("mMoviesMap")) {

                                            Map<String, Integer> currentMap =
                                                (Map)field.get(mWorkTypicalWords);

                                            if (currentMap != null && !currentMap.isEmpty()) {

                                                if (currentMap.containsKey(oldVariantText)) {

                                                    if (!currentMap.containsKey(editedVariantString)) {
                                                        
                                                        currentMap.put(editedVariantString, currentMap.remove(oldVariantText));
                                                            try {
                                                                field.set(mWorkTypicalWords, currentMap);
                                                            } catch (IllegalArgumentException ex) {
                                                                Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                                                            } catch (IllegalAccessException ex) {
                                                                Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                                                            }
                                                            fillVariantObservableList(mWorkTypicalWords, mWorkVariantObservableList);
                                                        } else {

                                                            Alert warningAlert =
                                                                new Alert(Alert.AlertType.WARNING);
                                                            warningAlert.setTitle("Предупреждение");
                                                            warningAlert.setHeaderText("Текст варианта не изменен");
                                                            warningAlert.setContentText("В модели уже есть вариант с таким текстом");
                                                            warningAlert.showAndWait();
                                                        }

                                                    break FOR_LABEL;
                                                }
                                            }
                                        }
                                    }
                                } catch (IllegalArgumentException ex) {
                                    Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (IllegalAccessException ex) {
                                    Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } 
                        } else {

                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Ошибка");
                            alert.setHeaderText("Изменение варианта не выполнено");
                            alert.setContentText("Введите строку длинной не менее двух символов");
                            alert.showAndWait();
                        }
                    }
                );
            }
        } else {
        
            Alert warningAlert =
                new Alert(Alert.AlertType.WARNING);
            warningAlert.setTitle("Предупреждение");
            warningAlert.setHeaderText("Не выбрана ни одна строка в рабочей таблице вариантов");
            warningAlert.setContentText("Выделите одну строку в таблице вариантов");
            warningAlert.showAndWait();
        }
    }
    
    @FXML
    private void showDeleteRowDlgAction(ActionEvent event){
    
        mSelectedVariantModel =
            (VariantModel) workModelTableView.getSelectionModel().getSelectedItem();
        
        if (mSelectedVariantModel != null) {
            
            String oldVariantText = mSelectedVariantModel.getVariant();
            
            if(mSelectedVariantModel.getCategory().equals("political")
                || mSelectedVariantModel.getCategory().equals("people main")
                || mSelectedVariantModel.getCategory().equals("life main")
                || mSelectedVariantModel.getCategory().equals("smoking")
                || mSelectedVariantModel.getCategory().equals("alcohol")
                    ){
                
                Class c = TypicalWords.class; 
                Field fieldTry = null;
                try {
                    
                    switch (mSelectedVariantModel.getCategory()) {
                        case "political":
                            fieldTry = c.getField("mPoliticalMap");
                            break;
                        case "people main":
                            fieldTry = c.getField("mPeopleMainMap");
                            break;
                        case "life main":
                            fieldTry = c.getField("mLifeMainMap");
                            break;
                        case "smoking":
                            fieldTry = c.getField("mSmokingMap");
                            break;
                        case "alcohol":
                            fieldTry = c.getField("mAlcoholMap");
                            break;
                        default:
                            break;
                    }
                } catch (NoSuchFieldException ex) {
                    Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                }

                if(fieldTry != null){

                    final Field field = fieldTry;
                    Class fieldType = field.getType(); 
                    //System.out.println("Имя: " + field.getName()); 
                    //System.out.println("Тип: " + fieldType.getName());

                    try {
                        if (field.get(mWorkTypicalWords) instanceof Map<?,?>) {

                            final Map<Integer, Integer> currentMap =
                                (Map)field.get(mWorkTypicalWords);

                            if (currentMap != null && !currentMap.isEmpty()) {

                                Integer oldVariantIntegerTry = null;

                                switch (mSelectedVariantModel.getCategory()) {
                                    case "political":

                                        oldVariantIntegerTry = (Integer) MapUtils
                                            .getKeysByValue(TypicalWords.mPoliticalMapping, oldVariantText)
                                            .toArray()[0];
                                        break;
                                    case "people main":

                                        oldVariantIntegerTry = (Integer) MapUtils
                                            .getKeysByValue(TypicalWords.mPeopleMainMapping, oldVariantText)
                                            .toArray()[0];
                                        break;
                                    case "life main":

                                        oldVariantIntegerTry = (Integer) MapUtils
                                            .getKeysByValue(TypicalWords.mLifeMainMapping, oldVariantText)
                                            .toArray()[0];
                                        break;
                                    case "smoking":

                                        oldVariantIntegerTry = (Integer) MapUtils
                                            .getKeysByValue(TypicalWords.mSmokingMapping, oldVariantText)
                                            .toArray()[0];
                                        break;
                                    case "alcohol":

                                        oldVariantIntegerTry = (Integer) MapUtils
                                            .getKeysByValue(TypicalWords.mAlcoholMapping, oldVariantText)
                                            .toArray()[0];
                                        break;
                                    default:
                                        break;
                                }

                                final Integer oldVariantInteger =
                                    oldVariantIntegerTry;

                                if (oldVariantInteger != null
                                    && currentMap.containsKey(oldVariantInteger)) {

                                    Object delResult = null;

                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                    alert.setTitle("Удаление строки");
                                    alert.setHeaderText("Требуется подтверждение");
                                    alert.setContentText("Вы действительно хотите удалить выбранную строку рабочей модели?");

                                    Optional<ButtonType> result = alert.showAndWait();

                                    if ((result.isPresent()) && (result.get() == ButtonType.OK)) {

                                        delResult = currentMap.remove(oldVariantInteger);
                                        try {
                                            field.set(mWorkTypicalWords, currentMap);
                                        } catch (IllegalArgumentException ex) {

                                            delResult = null;
                                            Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                                        } catch (IllegalAccessException ex) {

                                            delResult = null;
                                            Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                                        }

                                        if (delResult != null) {

                                            fillVariantObservableList(mWorkTypicalWords, mWorkVariantObservableList);

                                            alert = new Alert(Alert.AlertType.INFORMATION);
                                            alert.setTitle("Информация");
                                            alert.setHeaderText("Выбранная строка рабочей модели успешно удалена");
                                            //Показать сообщение и ждать клика по кнопке Ok
                                            alert.showAndWait();
                                        } else {

                                            alert = new Alert(Alert.AlertType.ERROR);
                                            alert.setTitle("Ошибка");
                                            alert.setHeaderText("Произошел сбой в работе приложения. Выбранная строка не найдена и/или не удалена.");
                                            alert.showAndWait();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } 
            } else {
            
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Удаление строки");
                alert.setHeaderText("Требуется подтверждение");
                alert.setContentText("Вы действительно хотите удалить выбранную строку рабочей модели?");

                Optional<ButtonType> result = alert.showAndWait();

                if ((result.isPresent()) && (result.get() == ButtonType.OK)) {

                    Class c = TypicalWords.class; 
                    Field[] publicFields = c.getFields();

                    FOR_LABEL : for (Field field : publicFields) {

                        Class fieldType = field.getType(); 
                        //System.out.println("Имя: " + field.getName()); 
                        //System.out.println("Тип: " + fieldType.getName());

                        try {
                            if (field.get(mWorkTypicalWords) instanceof Map<?,?>) {

                                if (field.getName().equals("mInterestMap")
                                    || field.getName().equals("mActivityMap")
                                    || field.getName().equals("mAboutMap")
                                    || field.getName().equals("mReligionMap")
                                    || field.getName().equals("mInspiredByMap")
                                    || field.getName().equals("mBooksMap")
                                    || field.getName().equals("mMusicMap")
                                    || field.getName().equals("mMoviesMap")) {

                                    Map<String, Integer> currentMap =
                                        (Map)field.get(mWorkTypicalWords);

                                    Object delResult = null;

                                    if (currentMap != null && !currentMap.isEmpty()) {

                                        if (currentMap.containsKey(oldVariantText)) {

                                            //if (!currentMap.containsKey(editedVariantString)) {

                                            delResult = currentMap.remove(oldVariantText);
                                            try {
                                                field.set(mWorkTypicalWords, currentMap);
                                            } catch (IllegalArgumentException ex) {

                                                delResult = null;
                                                Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                                            } catch (IllegalAccessException ex) {

                                                delResult = null;
                                                Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                                            }

                                            if (delResult != null) {

                                                fillVariantObservableList(mWorkTypicalWords, mWorkVariantObservableList);

                                                alert = new Alert(Alert.AlertType.INFORMATION);
                                                alert.setTitle("Информация");
                                                alert.setHeaderText("Выбранная строка рабочей модели успешно удалена");
                                                //Показать сообщение и ждать клика по кнопке Ok
                                                alert.showAndWait();
                                            } else {

                                                alert = new Alert(Alert.AlertType.ERROR);
                                                alert.setTitle("Ошибка");
                                                alert.setHeaderText("Произошел сбой в работе приложения. Выбранная строка не найдена и/или не удалена.");
                                                alert.showAndWait();
                                            }
                                            break FOR_LABEL;
                                        }
                                    }
                                }
                            }
                            /*
                            String oldVariantTextFromResource = (String) field.get(obj);*/
                        } catch (IllegalArgumentException ex) {
                            Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalAccessException ex) {
                            Logger.getLogger(HomeController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } 
                } else {

                    Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                    infoAlert.setTitle("Информация");
                    infoAlert.setHeaderText("Процесс удаления отменен");
                    infoAlert.setContentText("Удаление выбранной строки из рабочей модели не выполнено");
                    infoAlert.showAndWait();
                }
            }
        } else {
        
            Alert warningAlert =
                new Alert(Alert.AlertType.WARNING);
            warningAlert.setTitle("Предупреждение");
            warningAlert.setHeaderText("Не выбрана ни одна строка в рабочей таблице вариантов");
            warningAlert.setContentText("Выделите одну строку в таблице вариантов");
            warningAlert.showAndWait();
        }
    }
    
    @FXML
    private void goToFindUsersScreen(){
        
        //System.out.println(MainApp.globalModel.groupIdProperty().getValue());
        //System.out.println(MainApp.globalModel.curerntTypicalWords);
        
        if (MainApp.globalModel.groupIdProperty().getValue() != null 
            && !MainApp.globalModel.groupIdProperty().getValue().equals("")
                && MainApp.globalModel.curerntTypicalWords != null) {
            
            //TODO показать заставку и загрузить из локальной службы
            //список пользователей, уже зарегистрированных в группе,
            //и из удаленной службы - список пользователей,
            //ранее отмеченных, как получившие приглашение
            
            myController.setScreen(MainApp.findUsersID);
            MainApp.primaryStage.setMaximized(false);
            MainApp.primaryStage.setWidth(566);
            MainApp.primaryStage.setHeight(400);
            MainApp.primaryStage.setY(30);
        } else {
        
            Alert warningAlert =
                new Alert(Alert.AlertType.WARNING);
            warningAlert.setTitle("Предупреждение");
            warningAlert.setHeaderText("Не загружена модель текстов пользователей группы");
            warningAlert.setContentText("Воспользуйтесь кнопками Create model или Load model");
            warningAlert.showAndWait();
        }
    }
    
    /*@FXML
    private void addWaterType(ActionEvent event){
       
        Pattern waterTypePattern = Pattern.compile(".{1,50}");
        String newWaterTypeNameString = waterTypeCTextField.textProperty().getValue();
        if (waterTypePattern.matcher(newWaterTypeNameString).matches()) {
            
            boolean existFlag = false;
            for (WaterType waterType : mWaterTypes) {
                
                if (newWaterTypeNameString.equals(waterType.getName())) {
                    
                    existFlag = true;
                }
            }
            if (!existFlag) {
                
                WaterType newWaterType = new WaterType();
                newWaterType.setName(newWaterTypeNameString);
                newWaterType.setActive(true);
                mWaterTypesDAOImpl.createWaterType(newWaterType);
                updateWaterTypes();
                WS1.addBarrelControllerInstance.updateWaterTypes();
            }
        }
    }
    
    @FXML
    private void deleteWaterType(ActionEvent event){
       
        WaterTypeModel selectedWaterTypeModel =
            (WaterTypeModel) waterTypesListView.getSelectionModel().getSelectedItem();
        if (selectedWaterTypeModel != null) {
            
            WaterType selectedWaterType =
                mWaterTypesDAOImpl.getWaterType(selectedWaterTypeModel.getId());
            
            selectedWaterType.setActive(false);
            mWaterTypesDAOImpl.updateWaterType(selectedWaterType);
            updateWaterTypes();
            WS1.addBarrelControllerInstance.updateWaterTypes();
        }
    }
    
    @FXML
    private void addCapacity(ActionEvent event){
       
        Pattern capacityPattern = Pattern.compile("[0-9]{1,4}");
        String newCapacityString = capacityCTextField.textProperty().getValue();
        if (capacityPattern.matcher(newCapacityString).matches()) {
            
            boolean existFlag = false;
            for (BarrelCapacity _barrelCapacity : mBarrelCapacities) {
                
                if (newCapacityString.equals(String.valueOf(_barrelCapacity.getCapacity()))) {
                    
                    existFlag = true;
                }
            }
            if (!existFlag) {
                
                BarrelCapacity newBarrelCapacity = new BarrelCapacity();
                newBarrelCapacity.setCapacity(Integer.valueOf(newCapacityString));
                newBarrelCapacity.setActive(true);
                mBarrelCapacitiesDAOImpl.createBarrelCapacity(newBarrelCapacity);
                updateBarrelCapacities();
                WS1.addBarrelControllerInstance.updateBarrelCapacities();
            }
        }
    }
    
    @FXML
    private void deleteCapacity(ActionEvent event){
       
        BarrelCapacityModel selectedBarrelCapacityModel =
            (BarrelCapacityModel) capacitiesListView.getSelectionModel().getSelectedItem();
        if (selectedBarrelCapacityModel != null) {
            
            BarrelCapacity selectedBarrelCapacity =
                mBarrelCapacitiesDAOImpl.getBarrelCapacity(selectedBarrelCapacityModel.getId());
            
            selectedBarrelCapacity.setActive(false);
            mBarrelCapacitiesDAOImpl.updateBarrelCapacity(selectedBarrelCapacity);
            updateBarrelCapacities();
            WS1.addBarrelControllerInstance.updateBarrelCapacities();
        }
    }
    
    @FXML
    private void setCleaningTypicalCycleTime(ActionEvent event){
       
        Pattern cTCPattern = Pattern.compile("[1][0-2]([/.][0-9]){0,1}");
        Pattern cTCPattern2 = Pattern.compile("[1-9]([/.][0-9]){0,1}");
        String cTCString = сleaningTCTimeCTextField.textProperty().getValue().replaceAll(",", ".");
        if (cTCPattern.matcher(cTCString).matches()
                || cTCPattern2.matcher(cTCString).matches()) {
        
            mSettingsDAOImpl.updateSettings(
                new org.tyaa.ws.entity.Settings(
                    1
                    , "cleaning_period"
                    , String.valueOf((long)(Float.valueOf(cTCString) * 2592000000L)))
            );
            Settings.setCleaningTypicalCycleTime((long)(Float.valueOf(cTCString) * 2592000000L));
            WS1.barrelsControllerInstance.updateBarrelsForPage();
        } else {
        
            сleaningTCTimeCTextField.setText(String.valueOf(Settings.getCleaningTypicalCycleTime() / 2592000000D));
        }
    }
    
    @FXML
    private void setCleaningOverdueCycleTime(ActionEvent event){
       
        Pattern cOCPattern = Pattern.compile("[1][0-2]([/.][0-9]){0,1}");
        Pattern cOCPattern2 = Pattern.compile("[1-9]([/.][0-9]){0,1}");
        String cOCString = сleaningOCTimeCTextField.textProperty().getValue().replaceAll(",", ".");
        if (cOCPattern.matcher(cOCString).matches()
                || cOCPattern2.matcher(cOCString).matches()) {
        
            mSettingsDAOImpl.updateSettings(
                new org.tyaa.ws.entity.Settings(
                    2
                    , "cleaning_period_over"
                    , String.valueOf((long)(Float.valueOf(cOCString) * 2592000000L)))
            );
            Settings.setCleaningOverdueCycleTime((long)(Float.valueOf(cOCString) * 2592000000L));
            WS1.barrelsControllerInstance.updateBarrelsForPage();
        } else {
        
            сleaningOCTimeCTextField.setText(String.valueOf(Settings.getCleaningOverdueCycleTime() / 2592000000D));
        }
    }
    
    @FXML
    private void setAllowedRestPercent(ActionEvent event){
       
        Pattern aRPPattern = Pattern.compile("[1-9]");
        Pattern aRPPattern2 = Pattern.compile("[1-9][0-9]");
        String aRPString = allowedRestPercent.textProperty().getValue();
        if (aRPPattern.matcher(aRPString).matches()
                || aRPPattern2.matcher(aRPString).matches()) {
        
            //Обновляем параметр в БД
            mSettingsDAOImpl.updateSettings(
                new org.tyaa.ws.entity.Settings(
                    3
                    , "allowed_rest_percent"
                    , String.valueOf((float)Integer.valueOf(aRPString) / 100F)
                )
            );
            //... и в глобальном объекте
            Settings.setAllowedRestPecent((float)Integer.valueOf(aRPString) / 100F);
            WS1.barrelsControllerInstance.updateBarrelsForPage();
        } else {
        
            allowedRestPercent.setText(String.valueOf(Settings.getAllowedRestPecent() * 100));
        }
    }
    
    
    
    //Приведение формы в исходное состояние
    private void resetForm(){
        
        waterTypeCTextField.setText("");
        capacityCTextField.setText("");
        
        //mWaterTypes = mWaterTypesDAOImpl.getAllWaterTypes();
        List<WaterType> waterTypesTmp = mWaterTypesDAOImpl.getAllWaterTypes();
        if (waterTypesTmp == null) {
            
            waterTypesTmp = new ArrayList<>();
        }
        mWaterTypes =
            waterTypesTmp.stream()
                .filter(wType -> ((WaterType)wType).getActive())
                .collect(Collectors.toList());
        if (mWaterTypes == null) {
            
            mWaterTypes = new ArrayList<>();
        }
        
        //mBarrelCapacities = mBarrelCapacitiesDAOImpl.getAllBarrelCapacities();
        List<BarrelCapacity> barrelCapacitiesTmp =
            mBarrelCapacitiesDAOImpl.getAllBarrelCapacities();
        if (barrelCapacitiesTmp == null) {
            
            barrelCapacitiesTmp = new ArrayList<>();
        }
        mBarrelCapacities =
                barrelCapacitiesTmp.stream()
                .filter(bCap -> ((BarrelCapacity)bCap).getActive())
                .collect(Collectors.toList());
        if (mBarrelCapacities == null) {
            
            mBarrelCapacities = new ArrayList<>();
        }*/
        
        /*Fill observable collections*/
        
        /*mWaterTypesObservableList.clear();
        
        if (mWaterTypes == null) {
         
            mWaterTypes = new ArrayList<>();
        }
            
        for (WaterType waterType : mWaterTypes) {

            mWaterTypesObservableList.add(
                new WaterTypeModel(waterType.getId(), waterType.getName())
            );
        }
        
        mBarrelCapacitiesObservableList.clear();
        
        if (mBarrelCapacities == null) {
         
            mBarrelCapacities = new ArrayList<>();
        }
            
        for (BarrelCapacity barrelCapacity : mBarrelCapacities) {

            mBarrelCapacitiesObservableList.add(new BarrelCapacityModel(
                    barrelCapacity.getId(), barrelCapacity.getCapacity()
                )
            );
        }
    }
    
    public void updateWaterTypes(){
        
        mWaterTypes = mWaterTypesDAOImpl.getAllWaterTypes();
        resetForm();
    }
    
    public void updateBarrelCapacities(){
        
        mBarrelCapacities = mBarrelCapacitiesDAOImpl.getAllBarrelCapacities();
        resetForm();
    }*/
    
    //Метод заполнения наблюдабельного списка из карт объекта TypicalWords
    private void fillVariantObservableList(
        TypicalWords _typicalWords
        , ObservableList<VariantModel> _variantObservableList
    ){
        
        _variantObservableList.clear();
        
        _typicalWords.mInterestMap.forEach (
                        
            (k, v) -> {
            
                _variantObservableList.add(new VariantModel("interest", k, v));
            }
        );
        
        _typicalWords.mActivityMap.forEach (
                        
            (k, v) -> {
            
                _variantObservableList.add(new VariantModel("activity", k, v));
            }
        );
        
        _typicalWords.mAboutMap.forEach (
                        
            (k, v) -> {
            
                _variantObservableList.add(new VariantModel("about", k, v));
            }
        );
        
        _typicalWords.mPoliticalMap.forEach (
                        
            (k, v) -> {
            
                String politicalKeyString = "";
                
                switch (k){
                
                    case 1 : {
                    
                        politicalKeyString = "коммунистические";
                        break;
                    }
                    
                    case 2 : {
                    
                        politicalKeyString = "социалистические";
                        break;
                    }
                    
                    case 3 : {
                    
                        politicalKeyString = "умеренные";
                        break;
                    }
                    
                    case 4 : {
                    
                        politicalKeyString = "либеральные";
                        break;
                    }
                    
                    case 5 : {
                    
                        politicalKeyString = "консервативные";
                        break;
                    }
                    
                    case 6 : {
                    
                        politicalKeyString = "монархические";
                        break;
                    }
                    
                    case 7 : {
                    
                        politicalKeyString = "ультраконсервативные";
                        break;
                    }
                    
                    case 8 : {
                    
                        politicalKeyString = "индифферентные";
                        break;
                    }
                    
                    case 9 : {
                    
                        politicalKeyString = "либертарианские";
                        break;
                    }
                }
                _variantObservableList.add(
                    new VariantModel("political", politicalKeyString, v));
            }
        );
        
        _typicalWords.mReligionMap.forEach (
                        
            (k, v) -> {
            
                _variantObservableList.add(new VariantModel("religion", k, v));
            }
        );
        
        _typicalWords.mInspiredByMap.forEach (
                        
            (k, v) -> {
            
                _variantObservableList.add(new VariantModel("inspired by", k, v));
            }
        );
        
        _typicalWords.mPeopleMainMap.forEach (
                        
            (k, v) -> {
            
                String peopleKeyString = "";
                
                switch (k){
                
                    case 1 : {
                    
                        peopleKeyString = "ум и креативность";
                        break;
                    }
                    
                    case 2 : {
                    
                        peopleKeyString = "доброта и честность";
                        break;
                    }
                    
                    case 3 : {
                    
                        peopleKeyString = "красота и здоровье";
                        break;
                    }
                    
                    case 4 : {
                    
                        peopleKeyString = "власть и богатство";
                        break;
                    }
                    
                    case 5 : {
                    
                        peopleKeyString = "смелость и упорство";
                        break;
                    }
                    
                    case 6 : {
                    
                        peopleKeyString = "юмор и жизнелюбие";
                        break;
                    }
                }
                _variantObservableList.add(
                    new VariantModel("people main", peopleKeyString, v));
            }
        );
        
        _typicalWords.mLifeMainMap.forEach (
                        
            (k, v) -> {
            
                String lifeMainKeyString = "";
                
                switch (k){
                
                    case 1 : {
                    
                        lifeMainKeyString = "семья и дети";
                        break;
                    }
                    
                    case 2 : {
                    
                        lifeMainKeyString = "карьера и деньги";
                        break;
                    }
                    
                    case 3 : {
                    
                        lifeMainKeyString = "развлечения и отдых";
                        break;
                    }
                    
                    case 4 : {
                    
                        lifeMainKeyString = "наука и исследования";
                        break;
                    }
                    
                    case 5 : {
                    
                        lifeMainKeyString = "совершенствование мира";
                        break;
                    }
                    
                    case 6 : {
                    
                        lifeMainKeyString = "саморазвитие";
                        break;
                    }
                    
                    case 7 : {
                    
                        lifeMainKeyString = "красота и искусство";
                        break;
                    }
                    
                    case 8 : {
                    
                        lifeMainKeyString = "слава и влияние";
                        break;
                    }
                }
                _variantObservableList.add(
                    new VariantModel("life main", lifeMainKeyString, v));
            }
        );
        
        _typicalWords.mSmokingMap.forEach (
                        
            (k, v) -> {
            
                String smokingKeyString = "";
                
                switch (k){
                
                    case 1 : {
                    
                        smokingKeyString = "резко негативное";
                        break;
                    }
                    
                    case 2 : {
                    
                        smokingKeyString = "негативное";
                        break;
                    }
                    
                    case 3 : {
                    
                        smokingKeyString = "компромиссное";
                        break;
                    }
                    
                    case 4 : {
                    
                        smokingKeyString = "нейтральное";
                        break;
                    }
                    
                    case 5 : {
                    
                        smokingKeyString = "положительное";
                        break;
                    }
                }
                _variantObservableList.add(
                    new VariantModel("smoking", smokingKeyString, v));
            }
        );
        
        _typicalWords.mAlcoholMap.forEach (
                        
            (k, v) -> {
            
                String alcoholKeyString = "";
                
                switch (k){
                
                    case 1 : {
                    
                        alcoholKeyString = "резко негативное";
                        break;
                    }
                    
                    case 2 : {
                    
                        alcoholKeyString = "негативное";
                        break;
                    }
                    
                    case 3 : {
                    
                        alcoholKeyString = "компромиссное";
                        break;
                    }
                    
                    case 4 : {
                    
                        alcoholKeyString = "нейтральное";
                        break;
                    }
                    
                    case 5 : {
                    
                        alcoholKeyString = "положительное";
                        break;
                    }
                }
                _variantObservableList.add(
                    new VariantModel("alcohol", alcoholKeyString, v));
            }
        );
        
        _typicalWords.mBooksMap.forEach (
                        
            (k, v) -> {
            
                _variantObservableList.add(new VariantModel("books", k, v));
            }
        );
        
        _typicalWords.mMusicMap.forEach (
                        
            (k, v) -> {
            
                _variantObservableList.add(new VariantModel("music", k, v));
            }
        );
        
        _typicalWords.mMoviesMap.forEach (
                        
            (k, v) -> {
            
                _variantObservableList.add(new VariantModel("movies", k, v));
            }
        );
    }
    
    //Метод заполнения наблюдабельного списка из списка объектов кандидатов
    private void fillCandidateObservableList(
        List<VKCandidate> _vKCandidateList
        , ObservableList<CandidateModel> _candidateObservableList
    ){
        
        //TODO проверить актуальность кандидатов!
        
        _candidateObservableList.clear();
    
        for (VKCandidate vKCandidate : _vKCandidateList) {
            
            //System.out.println(vKCandidate.getUID());
            
            _candidateObservableList.add(
            
                new CandidateModel(
                    vKCandidate.getUID()
                    , vKCandidate.getFirstName()
                    , vKCandidate.getLastName()
                    , vKCandidate.getScore())
            );
        }
    }
    
    private void toggleButtonsEnable(){
    
        if (mButtonsEnable) {
            
            createModelButton.setDisable(true);
        } else {
        
            createModelButton.setDisable(false);
        }
    }

    @Override
    public void handleUpdateCandidatesEvent(EventObject e)
    {
        //System.out.println("Load new cand");
        
        mVKCandidateList = ((UpdateCandidatesEvent)e).vKCandidatesList;
        
        fillCandidateObservableList(mVKCandidateList, mCandidateModelObservableList);
    }
    
}
