/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.viewcontroller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.tyaa.comradfinder.screensframework.ControlledScreen;
import org.tyaa.comradfinder.screensframework.ScreensController;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.tools.ValueExtractor;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationMessage;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.tyaa.comradfinder.MainApp;
import org.tyaa.comradfinder.model.TypicalWords;
import org.tyaa.comradfinder.model.VKCandidate;
import org.tyaa.comradfinder.model.VKCity;
import org.tyaa.comradfinder.model.VKCountry;
import org.tyaa.comradfinder.model.VKRegion;
import org.tyaa.comradfinder.modules.XmlImporter;
import org.tyaa.comradfinder.modules.exception.FailJsonFetchException;
import org.tyaa.comradfinder.modules.facades.ComradFinder;
import org.tyaa.comradfinder.modules.facades.ModelBuilder;
import org.tyaa.comradfinder.screensframework.ProgressForm;
import org.tyaa.comradfinder.utils.MapUtils;
import org.tyaa.comradfinder.utils.SexEnum;
import org.xml.sax.SAXException;

/**
 * FXML Controller class
 *
 * @author Yurii
 */
public class FindUsersController implements Initializable, ControlledScreen {

    /*Внедренные хендлеры элементов UI*/
    
    /*@FXML
    Label captionLabel;*/
    
    //элементы ввода информации
    @FXML
    CustomTextField countryCTextField;
    @FXML
    CustomTextField regionCTextField;
    @FXML
    CustomTextField cityCTextField;
    @FXML
    ComboBox ageComboBox;
    @FXML
    CheckBox femaleCheckBox;
    @FXML
    CheckBox maleCheckBox;
    
    //
    @FXML
    Button findUsersButton;
    @FXML
    Button resetFormButton;
    @FXML
    Button backButton;
    
    //Объекты доступа к данным
    //private SalesDAOImpl mSalesDAOImpl;
    
    //Списки объектов
    private List<VKCountry> mVKCountries;
    private List<VKRegion> mVKCountryRegions;
    //Cities выбранного country and region
    private List<VKCity> mVKCountryRegionCities;
    
    //
    private List<Integer> mAgeList;
    
    //Наборы для автодополнения в полях ввода
    private Set<String> mCountryNamesSet;
    private Set<String> mRegionNamesSet;
    private Set<String> mCityNamesSet;
    
    //хендлеры к наборам автодополнения
    private AutoCompletionBinding<String> mCountryAutoCompletionBinding;
    private AutoCompletionBinding<String> mRegionAutoCompletionBinding;
    private AutoCompletionBinding<String> mCityAutoCompletionBinding;
    
    //Выбранные объекты
    private VKCountry mSelectedCountry;
    private VKRegion mSelectedRegion;
    private VKCity mSelectedCity;
    
    private Integer mSelectedAge;
    
    private SexEnum mSelectedSex;
    
    private boolean mChangeCitiesByRegion;
    private boolean mCityWasSelected;
    
    //ID только что добавленной доставки
    //private int mTmpLastSaleId;
    
    //ObservableList<Barrel> mBarrelsObservableList;
    //ObservableList<DebtChange> mDebtChangesObservableList;
    
    ScreensController myController;
    ValidationSupport validationSupport;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        mChangeCitiesByRegion = false;
        mCityWasSelected = false;
        
        mCountryNamesSet = new HashSet<>();
        mRegionNamesSet = new HashSet<>();
        mCityNamesSet = new HashSet<>();
        
        mAgeList = new ArrayList<>();
        
        for (int i = 14; i < 100; i++) {
            
            mAgeList.add(i);
        }
        
        ObservableList<Integer> ageObservableList =
            FXCollections.observableArrayList();
        
        ageObservableList.addAll(mAgeList);
        
        ageComboBox.setItems(ageObservableList);
                
        try {
            mVKCountries = ComradFinder.getCountries();
        } catch (FailJsonFetchException ex) {

            showJsonFetchErrorMsg();
        }
        
        /*Приведение формы в исходное состояние*/
        resetForm();
        
        //обработка события "ввод названия country"
        countryCTextField.textProperty().addListener(new ChangeListener(){
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                
                if (mVKCountryRegions != null) {
                    
                    mVKCountryRegions.clear();
                }
                
                /*Выбираем regions, относящиеся к the country*/
                
                //Находим объект country с таким именем, которое выбрано из списка
                if (mVKCountries != null
                        && !countryCTextField.textProperty().getValue().equals("")
                        && mCountryNamesSet.contains(countryCTextField.textProperty().getValue())) {
                    
                    for (VKCountry vKCountry : mVKCountries) {
                        
                        if (vKCountry.name.equals(countryCTextField.textProperty().getValue())) {
                            
                            mSelectedCountry = vKCountry;
                            break;
                        }
                    }
                    
                    try {
                        /*Работа с regions*/
                        
                        mVKCountryRegions =
                                ComradFinder.getRegionsByCountryId(mSelectedCountry.id);
                    } catch (FailJsonFetchException ex) {

                        showJsonFetchErrorMsg();
                    }
                 
                    //System.out.println(mVKCountryRegions);
                    if (mVKCountryRegions != null
                            && mVKCountryRegions.size() > 0) {
                    
                        //regionCTextField.setDisable(false);
                        rebindRegionNames();
                    } else {
                    
                        regionCTextField.setText("");
                        regionCTextField.setDisable(true);
                    }
                } else {
                
                    regionCTextField.setText("");
                    regionCTextField.setDisable(true);
                }
            }
        });
        
        //обработка события "ввод названия region"
        regionCTextField.textProperty().addListener(new ChangeListener(){
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                
                if (mVKCountryRegionCities != null) {
                    
                    mVKCountryRegionCities.clear();
                }
                
                /*Выбираем cities, относящиеся к the country and the region*/
                
                //Находим объект region с таким именем, которое выбрано из списка
                if (mVKCountryRegions != null
                        && !regionCTextField.textProperty().getValue().equals("")
                        && mRegionNamesSet.contains(regionCTextField.textProperty().getValue())) {
                    
                    for (VKRegion vKRegion : mVKCountryRegions) {
                        
                        if (vKRegion.name.equals(regionCTextField.textProperty().getValue())) {
                            
                            mSelectedRegion = vKRegion;
                            break;
                        }
                    }
                    
                    try {
                        /*Работа с cities*/
                        
                        mVKCountryRegionCities =
                            ComradFinder.getCitiesByCountryRegionIds(
                                mSelectedCountry.id
                                , mSelectedRegion.id
                                , null
                            );
                    } catch (FailJsonFetchException ex) {

                        showJsonFetchErrorMsg();
                    } catch (UnsupportedEncodingException ex) {

                        showEncodingErrorMsg();
                    }
                 
                    if (mVKCountryRegionCities != null
                            && mVKCountryRegionCities.size() > 0) {
                    
                        rebindCityNames();
                        mChangeCitiesByRegion = true;
                    } else {
                    
                        cityCTextField.setText("");
                        cityCTextField.setDisable(true);
                    }
                } else {
                
                    cityCTextField.setText("");
                    cityCTextField.setDisable(true);
                }
            }
        });
        
        //обработка события "ввод названия city"
        cityCTextField.textProperty().addListener(new ChangeListener(){
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                
                
                if (!cityCTextField.getText().equals("")) {
                    
                    
                    try {
                        System.out.println("mSelectedCountry.id is " + mSelectedCountry.id + mSelectedCountry.name);
                        System.out.println("mSelectedRegion.id is " + mSelectedRegion.id + mSelectedRegion.name);
                        mVKCountryRegionCities =
                            ComradFinder.getCitiesByCountryRegionIds(
                                mSelectedCountry.id
                                , mSelectedRegion.id
                                , cityCTextField.getText()
                            );
                        System.out.println("mVKCountryRegionCities is " + mVKCountryRegionCities);
                    } catch (FailJsonFetchException ex) {

                        showJsonFetchErrorMsg();
                    } catch (UnsupportedEncodingException ex) {

                        showEncodingErrorMsg();
                    }
                    
                    if (mVKCountryRegionCities != null
                            && mVKCountryRegionCities.size() > 0) {
                    
                        rebindCityNames();
                        cityCTextField.setDisable(false);
                        
                        /*Выбираем city*/
                        for (VKCity vKCity : mVKCountryRegionCities) {

                                System.out.println("vKCity.name " + vKCity.name);
                        }

                        if (!cityCTextField.textProperty().getValue().equals("")
                                && mCityNamesSet.contains(cityCTextField.textProperty().getValue())) {

                            for (VKCity vKCity : mVKCountryRegionCities) {

                                if (vKCity.name.equals(cityCTextField.textProperty().getValue())) {

                                    mSelectedCity = vKCity;
                                    
                                    mCityWasSelected = true;
                                    
                                    cityCTextField.fireEvent(
                                        new KeyEvent(
                                            KeyEvent.KEY_PRESSED
                                                , ""
                                                , ""
                                                , KeyCode.ESCAPE
                                                , true
                                                , true
                                                , true
                                                , true
                                        )
                                    );
                                    break;
                                } else {
                                
                                    mSelectedCity = null;
                                }
                            }
                        }
                    }
                }
                if (mSelectedCity != null) {
                    System.out.println("mSelectedCity is " + mSelectedCity.name);
                } else {
                
                    System.out.println("mSelectedCity is null");
                }
                
            }
        });
                
        //обработка события "выбор age"
        ageComboBox.valueProperty().addListener(new ChangeListener()
        {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue)
            {
                mSelectedAge = (Integer) ageComboBox
                    .getSelectionModel()
                    .getSelectedItem();
            }
        });
        //По нажатию клавиши Ввод или Пробел комбобокс
        //должен показать свой список
        ageComboBox.setOnKeyPressed((event) -> {
            
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                
                ageComboBox.show();
            }
        });
        
        //обработка события "переключение чекбокса"
        femaleCheckBox.setOnAction((event) -> {
            
            if (femaleCheckBox.isSelected()) {
                
                mSelectedSex = SexEnum.female;
                maleCheckBox.setDisable(true);
            } else {
                
                mSelectedSex = null;
                maleCheckBox.setDisable(false);
            }
        });
        
        maleCheckBox.setOnAction((event) -> {
            
            if (maleCheckBox.isSelected()) {
                
                mSelectedSex = SexEnum.male;
                femaleCheckBox.setDisable(true);
            } else {
                
                mSelectedSex = null;
                femaleCheckBox.setDisable(false);
            }
        });
        
        //Активация механизма валидации для элементов ввода типа CustomTextField
        ValueExtractor.addObservableValueExtractor(
                c -> c instanceof CustomTextField
                , c -> ((CustomTextField) c).textProperty());
        
        validationSupport = new ValidationSupport();
        
        //Явная настройка включения визуального оформления валидации
        validationSupport.setErrorDecorationEnabled(true);
        
        //Настройки валидации для каждого элемента ввода, подлежащего проверке
        validationSupport.registerValidator(
                countryCTextField
                , Validator.createEmptyValidator("Название страны обязательно"));
        validationSupport.registerValidator(
                regionCTextField
                , Validator.createEmptyValidator("Название региона обязательно"));
        validationSupport.registerValidator(
                cityCTextField
                , Validator.createEmptyValidator("Название населенного пункта обязательно"));
    }    

    @Override
    public void setScreenParent(ScreensController screenParent) {
        myController = screenParent;
    }
    
    @FXML
    private void goHomeScreenAction(){
        
        myController.setScreen(MainApp.homeID);
        //MainApp.primaryStage.setWidth(1124);
        //MainApp.primaryStage.setHeight(600);
        MainApp.primaryStage.setMaximized(true);
    }
    
    @FXML
    private void resetFormAction(){
       
        countryCTextField.clear();
        ageComboBox.getSelectionModel().select(null);
        femaleCheckBox.indeterminateProperty().setValue(true);
        maleCheckBox.indeterminateProperty().setValue(true);
        
        mSelectedCountry = null;
        mSelectedRegion = null;
        mSelectedCity = null;
        mSelectedAge = 0;
        mSelectedSex = null;
    }
    
    @FXML
    private void findUsersAction() {
                        
        List<ValidationMessage> validationMessageList =
                (List<ValidationMessage>) validationSupport
                        .getValidationResult()
                        .getMessages();
        if (validationMessageList.isEmpty()) {
            
            //Если страны с введенным названием нет в списке автозавершения,
            //показываем окно ошибки
            String errorsString = "";
            boolean hasErrors = false;
            
            if (!mCountryNamesSet.contains(countryCTextField.getText())) {
                errorsString += "Нет страны с таким названием. ";
                hasErrors = true;
            }
            if (!mRegionNamesSet.contains(regionCTextField.getText())) {
                errorsString += "Нет региона с таким названием. ";
                hasErrors = true;
            }
            if (!mCityNamesSet.contains(cityCTextField.getText())) {
                errorsString += "Нет населенного пункта с таким названием. ";
                hasErrors = true;
            }
            if (mSelectedAge == null) {
                //errorsString += "Не выбран возраст. ";
                //hasErrors = true;
                mSelectedAge = 0;
            }
            
            if (hasErrors) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Поиск пользователей невозможно запустить - недостаточно данных для высокоселективного запроса к VK API");
                alert.setContentText(errorsString);
                alert.showAndWait();
            } else {
                
                //Не было ошибок - начинаем поиск
                System.out.println("countryCTextField " + countryCTextField.getText());
                System.out.println("regionCTextField " + regionCTextField.getText());
                System.out.println("cityCTextField " + cityCTextField.getText());
                
                System.out.println("ageComboBox " + ageComboBox.getSelectionModel().getSelectedItem());
                
                System.out.println("mSelectedSex " + mSelectedSex);
                
                Alert infoAalert = new Alert(Alert.AlertType.INFORMATION);
                infoAalert.setTitle("Информация");
                infoAalert.setHeaderText("Этот процесс может занять значительное время. Прогресс поиска будет отображаться восемь раз,");
                infoAalert.setContentText("так как поиск производится последовательно по пользователям с 8 различными статусами");
                infoAalert.showAndWait();
                
                boolean hasFetchJsonErrors = false;

                try{

                    //Подготавливаем фоновую задачу поиска кандидатов
                    Task findUsersTask =
                        ComradFinder.findByModel(
                            String.valueOf(mSelectedCountry.id)
                                , String.valueOf(mSelectedRegion.id)
                                , String.valueOf(mSelectedCity.id)
                                , String.valueOf(mSelectedAge)
                                , mSelectedSex != null ? mSelectedSex.toString() : ""
                        );

                    ProgressForm pForm = new ProgressForm();

                    // binds progress of progress bars to progress of task:
                    pForm.activateProgressBar(findUsersTask);

                    // this method would get the result of the task
                    // and update the UI based on its value
                    findUsersTask.setOnSucceeded(event -> {
                        
                        //Закрываем окно отображения прогресса
                        pForm.getDialogStage().close();
                        
                        //Загружаем в список объектов Java данные о кандидатах
                        //из только что сохраненного файла
                        //(этим осуществляентся проверка нормальной работы с файловой системой)
                        List<VKCandidate> vKCandidateList = null;
                        try {
                            vKCandidateList = XmlImporter.getVKCandidates("VKCandidates.xml");
                        } catch (Exception ex) {
                            
                            vKCandidateList = null;
                        }
                        if (vKCandidateList != null) {

                            if (vKCandidateList.size() > 0) {
                            
                                //Информируем подписчиков об обновлении данных
                                //о кандидатах
                                MainApp.updateCandidatesGenerator.fire(vKCandidateList);
                                //Переходим на главный экран приложения
                                goHomeScreenAction();
                            } else {
                        
                                Alert warningAlert =
                                    new Alert(Alert.AlertType.WARNING);
                                warningAlert.setTitle("Предупреждение");
                                warningAlert.setHeaderText("Ни один кандидат не найден");
                                warningAlert.setContentText("Попробуйте изменить фильтр по возрасту и полу");
                                warningAlert.showAndWait();
                            }
                            
                        } else {
                        
                            Alert errorAlert =
                                new Alert(Alert.AlertType.WARNING);
                            errorAlert.setTitle("Ошибка");
                            errorAlert.setHeaderText("Не загружен сформированный список кандидатов из файла");
                            errorAlert.setContentText("Неизвестная проблема работы с файловой системой");
                            errorAlert.showAndWait();
                        }
                    });

                    //Отображаем окно прогресса
                    pForm.getDialogStage().show();

                    //Запускаем поиск пользователей как фоновую задачу
                    Thread thread = new Thread(findUsersTask);
                    thread.setDaemon(true);
                    thread.start();
                } catch (NumberFormatException | FailJsonFetchException ex){

                    hasFetchJsonErrors = true;
                }
                if (hasFetchJsonErrors) {

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("Сбой поиска кандидатов");
                    alert.setContentText("Сетевой ресурс недоступен");
                    alert.showAndWait();
                }
            }
            
        } else {
            
            //Если хотя бы одно из валидируемых полей не заполнено,
            //показываем окно ошибок валидации
            
            String errorsString = "";
            
            for (ValidationMessage validationMessage : validationMessageList) {
                
                errorsString += "поле \""
                        + ((CustomTextField)validationMessage.getTarget()).getPromptText()
                        + "\": "
                        + validationMessage.getText()
                        + ". ";
            }
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Поиск пользователей невозможно запустить");
            alert.setContentText(errorsString);
            alert.showAndWait();
        }
    }
    
//    
//    @FXML
//    private void setDateNow(ActionEvent event){
//    
//        //Элементу "выбор даты" устанавливаем текущую дату
//        createdDatePicker.setValue(LocalDate.now());
//    }
//    
//    //Действие погашения выбранного долга на указанную в поле сумму
//    @FXML
//    private void amortDebt(ActionEvent event){
//    
//        //Если произошел клик по кнопке погашения, значит
//        //блок долгов отображается, и должен быть выбран соотв-й чекбокс,
//        //тогда выполняем эти условия принудительно в случае,
//        //если они не выполнены:
//        if (!showDebtsBlockCheckBox.isSelected()) {
//            
//            showDebtsBlockCheckBox.setSelected(true);
//            mShowDebtsBlock = true;
//        }
//        //Выполняем погашение долга, толко если блок долгов отображается,
//        //а также есть ИД только что созданной доставки
//        if (mShowDebtsBlock && mTmpLastSaleId > 0) {
//            
//            //Если долг выбран, то засчитываем сумму погашения, если нет -
//            //считаем ее нулевой (не засчитываем).
//            //Выше есть проверка, выводящая соотв-е предупреждение перед сохр-м
//
//            Double debtAmortDouble = 0.0;
//
//            //Получаем объект долга, выбранного для погашения
//            mSelectedExistingDebt =
//                (DebtChange) debtsComboBox.getSelectionModel().getSelectedItem();
//
//            //Если долг выбран, считываем из поля ввода сумму погашения
//            if (mSelectedExistingDebt != null) {
//
//                debtAmortDouble =
//                    Double.parseDouble(
//                        debtAmortCTextField.getText().replaceAll(",", ".")
//                    );
//                //Если сумма погашения больше суммы баланса выбранного для погашения долга -
//                //показываем сообщение об ошибке и выходим из обработчика события
//                if (debtAmortDouble > mSelectedExistingDebt.getBalance().doubleValue()) {
//
//                    Alert warningAlert =
//                        new Alert(Alert.AlertType.WARNING);
//                    warningAlert.setTitle("Предупреждение");
//                    warningAlert.setHeaderText("Предотвращена попытка погасить долг на сумму, бОльшую, чем его баланс");
//                    warningAlert.setContentText("Выделите сумму погашения, меньшую, чем сумма погашаемого долга");
//                    warningAlert.showAndWait();
//
//                    return;
//                }
//                //если не выбран - показываем предупреждение и выходим из обработчкика
//            } else {
//            
//                Alert warningAlert =
//                    new Alert(Alert.AlertType.WARNING);
//                warningAlert.setTitle("Предупреждение");
//                warningAlert.setHeaderText("Не выбран ни один долг для погашения");
//                warningAlert.setContentText("Выберите из выпадающего списка долг для погашения и введите рядом сумму погашения");
//                warningAlert.showAndWait();
//
//                return;
//            }
//            
//            //Получаем из БД объект-сущность только что добавленной продажи
//            Sale createdSale = mSalesDAOImpl.getSale(mTmpLastSaleId);
//            
//            //Проверяем, первое ли это нажатие кнопки погашения
//            if (!mSaleDebtAdded) {
//                
//                //В запись продажи сохраняется разность сумм добавляющегося
//                //и возвращаемого долгов
//                createdSale.setDebt(
//                    BigDecimal.valueOf(Double.parseDouble(debtCTextField.getText().replaceAll(",", "."))
//                        - debtAmortDouble)
//                );
//                mSalesDAOImpl.updateSale(createdSale);
//
//                /* Суммируем новое значение долга с балансом долга выбранного магазина */
//
//                //В запись магазина add разность сумм добавляющегося
//                //и возвращаемого долгов
//                BigDecimal shopDebt = mSelectedShop.getDebt();
//                shopDebt =
//                    shopDebt.add(
//                        BigDecimal.valueOf(Double.parseDouble(debtCTextField.getText().replaceAll(",", "."))
//                            - debtAmortDouble)
//                    );
//                mSelectedShop.setDebt(shopDebt);
//                mShopsDAOImpl.updateShop(mSelectedShop);
//
//                //Если есть ненулевой новый долг - добавляем запись о нем в таблицу изменений долгов
//                if (!debtCTextField.getText().equals("0")) {
//
//                    DebtChange newDebt = new DebtChange();
//                    newDebt.setShopId(mSelectedShop.getId());
//                    newDebt.setIsDebt(true);
//                    newDebt.setValue(
//                        BigDecimal.valueOf(
//                            Double.parseDouble(debtCTextField.getText().replaceAll(",", "."))
//                        )
//                    );
//                    newDebt.setDate(Date.from((createdDatePicker
//                        .getValue()
//                        .atStartOfDay()
//                        .atZone(ZoneId.systemDefault())).toInstant())
//                    );
//                    newDebt.setDebtId(-1);
//                    newDebt.setBalance(
//                        BigDecimal.valueOf(
//                            Double.parseDouble(debtCTextField.getText().replaceAll(",", "."))
//                        )
//                    );
//                    newDebt.setSaleId(mTmpLastSaleId);
//                    //Если это долг, не требующий возврата
//                    if (notRequireAmortCheckBox.isSelected()) {
//                        
//                        newDebt.setNotReqAmort(true);
//                    }
//                    mDebtChangesDAOImpl.createDebtChange(newDebt);
//                }
//                //Устанавливаем флаг "новый долг добавлен"
//                mSaleDebtAdded = true;
//            } else {
//            
//                //В запись продажи сохраняется разность сумм
//                //ранее рассчитанной разности нового долга и возвращаемых долгов
//                //и дополнительно возвращаемого долга
//                createdSale.setDebt(
//                    BigDecimal.valueOf(createdSale.getDebt().doubleValue()
//                        - debtAmortDouble)
//                );
//                mSalesDAOImpl.updateSale(createdSale);
//                
//                /* Вычитаем новое погашение долга из баланса долга выбранного магазина */
//
//                //В запись магазина сохраняется разность сумм баланса
//                //и возвращаемого долга
//                BigDecimal shopDebt = mSelectedShop.getDebt();
//                shopDebt =
//                    shopDebt.add(
//                        BigDecimal.valueOf( - debtAmortDouble)
//                    );
//                mSelectedShop.setDebt(shopDebt);
//                mShopsDAOImpl.updateShop(mSelectedShop);
//            }
//
//            //Если есть ненулевое погашение (amortization) долга -
//            //добавляем запись о нем в таблицу изменений долгов
//            if (!debtAmortCTextField.getText().equals("0")
//                    && !debtAmortCTextField.getText().equals("0.0")
//                    && !debtAmortCTextField.getText().equals("0.00")
//                    && !debtAmortCTextField.getText().equals("0,0")
//                    && !debtAmortCTextField.getText().equals("0,00")
//                    && !debtAmortCTextField.getText().equals("")) {
//
//                if (mSelectedExistingDebt != null) {
//
//                    DebtChange newDebtAmort = new DebtChange();
//                    newDebtAmort.setShopId(mSelectedShop.getId());
//                    //'это не долг (это - запись погашения)'
//                    newDebtAmort.setIsDebt(false);
//                    newDebtAmort.setValue(
//                        BigDecimal.valueOf(debtAmortDouble)
//                    );
//                    newDebtAmort.setDate(Date.from((createdDatePicker
//                        .getValue()
//                        .atStartOfDay()
//                        .atZone(ZoneId.systemDefault())).toInstant())
//                    );
//                    newDebtAmort.setDebtId(mSelectedExistingDebt.getId());
//                    //Отрицательный баланс - признак отсутствия баланса, т.к. это не
//                    //долг, а погашение
//                    newDebtAmort.setBalance(
//                        BigDecimal.valueOf(
//                            -1.0
//                        )
//                    );
//                    newDebtAmort.setSaleId(mTmpLastSaleId);
//                    
//                    /*//Добавляем в БД новую запись погашения
//                    mDebtChangesDAOImpl.createDebtChange(newDebtAmort);
//                    try {
//                        Thread.sleep(30000);
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(AddSaleController.class.getName())
//                            .log(Level.SEVERE, null, ex);
//                    }
//                    //Изменяем в БД запись долга, который погашался
//                    mSelectedExistingDebt.setBalance(
//                        mSelectedExistingDebt.getBalance().add(BigDecimal.valueOf(-debtAmortDouble))
//                    );
//                    mDebtChangesDAOImpl.updateDebtChange(mSelectedExistingDebt);*/
//                    
//                    //Транзакционное погашение долга (создание записи о погашении
//                    // и изменение баланса погашаемого долга)
//                    int transResult = mDebtChangesDAOImpl.doAmortDebtChange(
//                        mSelectedExistingDebt
//                            , newDebtAmort);
//                    
//                    if (transResult == -1) {
//                        
//                        Alert alert = new Alert(Alert.AlertType.ERROR);
//                        alert.setTitle("Ошибка");
//                        alert.setHeaderText("Выбранный долг не погашен");
//                        alert.setContentText("Сбой в системе не позволил выполнить транзакцию погашения долга");
//                        alert.showAndWait();
//                    }
//                }
//            }
//            //Полям ввода "долг" устанавливаем значение по умолчанию 0
//            debtCTextField.setText("0");
//            debtAmortCTextField.setText("0");
//            //Обновляем информацию о наличных долгах
//            mShopActiveDebts.clear();
//            mDebtChangesObservableList.clear();
//            //Пытаемся получить список долгов по ИД магазина
//            mShopActiveDebts =
//                mDebtChangesDAOImpl.getFilteredDebtChanges(
//                    mSelectedShop.getId()
//                    , true
//                );
//
//            for (DebtChange debtChange : mShopActiveDebts) {
//
//                //Если долг - требующий погашения, то
//                //добавляем его в коллекцию для комбо-бокса
//                if (!debtChange.isNotReqAmort()
//                        && !debtChange.getIsCredit()) {
//                    
//                    mDebtChangesObservableList.add(debtChange);
//                }
//            }
//
//            if (mDebtChangesObservableList.size() > 0) {
//
//                debtsComboBox.setItems(mDebtChangesObservableList);
//            }
//        }
//        //Вызываем обновление данных в коллекции-источнике для
//        //таблицы магазинов
//        WS1.shopsControllerInstance.updateShopsForPage();
//        //
//        WS1.salesControllerInstance.updateShops();
//        WS1.salesControllerInstance.updateSalesForPage();
//        //
//        //WS1.addSaleControllerInstance.updateShops();
//        //WS1.addSaleControllerInstance.updateDebts();
//        //
//        WS1.debtsControllerInstance.updateShops();
//        WS1.debtsControllerInstance.updateDebtChangesForPage();
//    }
//    
//    //Действие завершения работы с блоком долгов
//    @FXML
//    private void finishDebtChanges(ActionEvent event){
//        
//        volumeCTextField.setEditable(true);
//    
//        //Выполняем сброс и скрытие блока долгов, толко если этот блок отображается
//        if (mShowDebtsBlock) {
//            
//            if (!mSaleDebtAdded) {
//                
//                //Получаем из БД объект-сущность только что добавленной продажи
//                Sale createdSale = mSalesDAOImpl.getSale(mTmpLastSaleId);
//                //В запись продажи повторно сохраняется сумма полученной выручки,
//                //если она не равна сумме должной оплаты
//                createdSale.setProfit(
//                    BigDecimal.valueOf(
//                        mMustPay.doubleValue()
//                        - Double.parseDouble(debtCTextField.getText().replaceAll(",", "."))
//                    )
//                );
//                //В запись продажи сохраняется сумма добавляющегося долга
//                createdSale.setDebt(
//                    BigDecimal.valueOf(
//                        Double.parseDouble(debtCTextField.getText().replaceAll(",", "."))
//                    )
//                );
//                mSalesDAOImpl.updateSale(createdSale);
//
//                /* Суммируем новое значение долга с балансом долга выбранного магазина */
//                if (!notRequireAmortCheckBox.isSelected()) {
//                    
//                    //В запись магазина сохраняется сумма добавляющегося долга
//                    BigDecimal shopDebt = mSelectedShop.getDebt();
//                    shopDebt =
//                        shopDebt.add(
//                            BigDecimal.valueOf(Double.parseDouble(debtCTextField.getText().replaceAll(",", ".")))
//                        );
//                    mSelectedShop.setDebt(shopDebt);
//                    mShopsDAOImpl.updateShop(mSelectedShop);
//                }
//
//                //Если есть ненулевой новый долг - добавляем запись о нем в таблицу изменений долгов
//                if (!debtCTextField.getText().equals("0")
//                        && !debtCTextField.getText().equals("0.0")
//                        && !debtCTextField.getText().equals("0.00")
//                        && !debtCTextField.getText().equals("0,0")
//                        && !debtCTextField.getText().equals("0,00")
//                        && !debtCTextField.getText().equals("")) {
//
//                    DebtChange newDebt = new DebtChange();
//                    newDebt.setShopId(mSelectedShop.getId());
//                    newDebt.setIsDebt(true);
//                    newDebt.setValue(
//                        BigDecimal.valueOf(
//                            Double.parseDouble(debtCTextField.getText().replaceAll(",", "."))
//                        )
//                    );
//                    newDebt.setDate(Date.from((createdDatePicker
//                        .getValue()
//                        .atStartOfDay()
//                        .atZone(ZoneId.systemDefault())).toInstant())
//                    );
//                    newDebt.setDebtId(-1);
//                    newDebt.setBalance(
//                        BigDecimal.valueOf(
//                            Double.parseDouble(debtCTextField.getText().replaceAll(",", "."))
//                        )
//                    );
//                    newDebt.setSaleId(mTmpLastSaleId);
//                    //Если это долг, не требующий возврата
//                    if (notRequireAmortCheckBox.isSelected()) {
//                        
//                        newDebt.setNotReqAmort(true);
//                    }
//                    mDebtChangesDAOImpl.createDebtChange(newDebt);
//                }
//                //Вызываем обновление данных в коллекции-источнике для
//                //таблицы магазинов
//                WS1.shopsControllerInstance.updateShopsForPage();
//                //
//                WS1.salesControllerInstance.updateShops();
//                WS1.salesControllerInstance.updateSalesForPage();
//                //
//                //WS1.addSaleControllerInstance.updateShops();
//                //WS1.addSaleControllerInstance.updateDebts();
//                //
//                WS1.debtsControllerInstance.updateShops();
//                WS1.debtsControllerInstance.updateDebtChangesForPage();
//                //Устанавливаем флаг "новый долг добавлен"
//                //mSaleDebtAdded = true;
//            }
//            
//            mShowDebtsBlock = false;
//            mSaleDebtAdded = false;
//            //Действия после окончания процесса сохранения информации о доставке, ее долге и погашениях
//            doPostSaleSaving();
//        }
//    }
//    
////    public void shopCTextFieldChanged(InputMethodEvent event){
////        System.out.println("event");
////        if (!shopCTextField.textProperty().getValue().equals("") && mShopNamesSet.contains(shopCTextField.textProperty().getValue())) {
////            System.out.println("shops");
////            for (Shop shop : mShops) {
////                System.out.println("shop");
////                if (shop.getName().equals(shopCTextField.textProperty().getValue())) {
////                    System.out.println("selected shop");
////                    mSelectedShop = shop;
////                }
////            }
////            mBarrels = mBarrelsDAOImpl.getAllBarrels();
////            for (Barrel barrel : mBarrels) {
////                System.out.println(barrel.getCapacityId());
////            }
////        }
////    }

    //Приведение формы в исходное состояние
    private void resetForm(){
        
        //очистка элементов ввода
        countryCTextField.clear();
        regionCTextField.clear();
        cityCTextField.clear();

        femaleCheckBox.setSelected(false);
        maleCheckBox.setSelected(false);
        if (mVKCountries != null) {
            
            rebindCountryNames();
        }
    }
    
    private void rebindCountryNames(){
    
        //Подключение автодополнения к полям с наборами вариантов выбора
        mCountryNamesSet.clear();
        for (VKCountry vKCountry : mVKCountries) {
            
            mCountryNamesSet.add(vKCountry.name);
        }
        //отписываемся от предыдущего набора автодополнения
        if (mCountryAutoCompletionBinding != null) {
            
            mCountryAutoCompletionBinding.dispose();
        }
        //подписываемся на новый набор автодополнения
        mCountryAutoCompletionBinding =
            TextFields.bindAutoCompletion(countryCTextField, mCountryNamesSet);
    }
    
    private void rebindRegionNames(){
    
        //Подключение автодополнения к полям с наборами вариантов выбора
        mRegionNamesSet.clear();
        for (VKRegion vKRegion : mVKCountryRegions) {
            
            mRegionNamesSet.add(vKRegion.name);
        }
        //отписываемся от предыдущего набора автодополнения
        if (mRegionAutoCompletionBinding != null) {
            
            mRegionAutoCompletionBinding.dispose();
        }
        //подписываемся на новый набор автодополнения
        mRegionAutoCompletionBinding =
            TextFields.bindAutoCompletion(regionCTextField, mRegionNamesSet);
        
        if (mRegionNamesSet.size() > 0
            && regionCTextField.disableProperty().getValue() == true) {
            //System.out.println(regionCTextField.disableProperty().getValue());
            regionCTextField.setDisable(false);
        } else {
        
            //System.out.println(regionCTextField.disableProperty().getValue());
            regionCTextField.setDisable(true);
        }
    }

    private void rebindCityNames(){
    System.out.println("rebindCityNames");
        //Подключение автодополнения к полям с наборами вариантов выбора
        mCityNamesSet.clear();
        /*for (VKCity vKCity : mVKCountryRegionCities) {

                System.out.println("vKCity.name " + vKCity.name);
        }*/
        for (VKCity vKCity : mVKCountryRegionCities) {
            System.out.println("vKCity.name " + vKCity.name);
            mCityNamesSet.add(vKCity.name);
        }
        //отписываемся от предыдущего набора автодополнения
        if (mCityAutoCompletionBinding != null) {
            
            mCityAutoCompletionBinding.dispose();
        }
        
        if (!mChangeCitiesByRegion) {
            
            cityCTextField.fireEvent(
                new KeyEvent(
                    KeyEvent.KEY_PRESSED
                        , ""
                        , ""
                        , KeyCode.ESCAPE
                        , true
                        , true
                        , true
                        , true
                )
            );
        } else {
        
            mChangeCitiesByRegion = false;
        }
        
        //подписываемся на новый набор автодополнения
        mCityAutoCompletionBinding =
            TextFields.bindAutoCompletion(cityCTextField, mCityNamesSet);
        
        if (mCityWasSelected) {
            
            cityCTextField.fireEvent(
                new KeyEvent(
                    KeyEvent.KEY_PRESSED
                        , ""
                        , ""
                        , KeyCode.ESCAPE
                        , true
                        , true
                        , true
                        , true
                )
            );
        } else {
        
            mCityWasSelected = false;
        }
        
        if (mCityNamesSet.size() > 0
            && cityCTextField.disableProperty().getValue() == true) {
            //System.out.println(regionCTextField.disableProperty().getValue());
            cityCTextField.setDisable(false);
        } else {
        
            //System.out.println(regionCTextField.disableProperty().getValue());
            cityCTextField.setDisable(true);
        }
    }
    
//    private void processSaleSaving(){
//        
//        /*Устанавливаем объекты выбранного водителя и выбранного авто*/
//        for (Driver driver : mDrivers) {
//            if (driver.getName().equals(driverCTextField.getText())) {
//                mSelectedDriver = driver;
//            }
//        }
//        for (Car car : mCars) {
//            if (String.valueOf(car.getNumber()).equals(carCTextField.getText())) {
//                mSelectedCar = car;
//            }
//        }
//
//        //Если пользователь удалил текст из поля ввода "долг",
//        //устанавливаем значение для записи в БД: 0
//        /*String debtString = !debtCTextField.getText().equals("")
//                ? debtCTextField.getText()
//                : "0";*/
//
//        //TODO добавить предупреждение, если показания счетчика
//        //или сумма опалты - подозрительные
//        //TODO во время выполнения запроса отображать метку "Загрузка..."
//        //TODO для замены бочки учитывать слив через счетчик
//        //TODO добавлять второе значение счетчика в таблицу "Бочки"
//
//        /*System.out.println(mSelectedShop.getId());
//        System.out.println(mSelectedBarrel.getId());
//        System.out.println(mSelectedDriver.getId());
//        System.out.println(mSelectedCar.getId());
//        System.out.println(countOldCTextField.getText());
//        System.out.println(countNewCTextField.getText());
//        System.out.println(volumeCTextField.getText());
//        System.out.println(cleanCheckBox.isSelected());
//        System.out.println(repairCheckBox.isSelected());
//        System.out.println(profitCTextField.getText());
//        System.out.println(debtCTextField.getText());
//        System.out.println(createdDatePicker.getValue());
//        System.out.println(noticeCTextField.getText());*/
//
//        //Создаем ненастроенный объект-сущность доставки
//        //
//        Date oldDate = null;
//        Sale newSale = null;
//        if(mEditMode){
//        
//            newSale = mEditingSale;
//            oldDate = mEditingSale.getCreatedAt();
//        } else {
//        
//            newSale = new Sale();
//        }
//        //Настраиваем его всеми полученными и вычисленными данными
//        newSale.setShopId(mSelectedShop.getId());
//        newSale.setBarrelId(mSelectedBarrel.getId());
//        newSale.setDriverId(mSelectedDriver.getId());
//        newSale.setCarId(mSelectedCar.getId());
//        newSale.setCounterOld(
//                Integer.parseInt(countOldCTextField.getText())
//        );
//        newSale.setCounterNew(
//            Integer.parseInt(countNewCTextField.getText())
//        );
//        newSale.setVolume(
//            Integer.parseInt(volumeCTextField.getText())
//        );
//        newSale.setCleaning(cleanCheckBox.isSelected());
//        newSale.setRepair(repairCheckBox.isSelected());
//        
//        newSale.setToPay(mMustPay);
//        newSale.setProfit(
//            BigDecimal.valueOf(Double.parseDouble(profitCTextField.getText().replaceAll(",", ".")))
//        );
//                
//        //В запись продажи на данном этапе сохраняется
//        //нулевая сумма долга (она может быть изменена на этапе работы с долгами)
//        if(!mEditMode){
//        
//            newSale.setDebt(BigDecimal.ZERO);
//        }
//        
//        newSale.setCreatedAt(Date.from((createdDatePicker
//            .getValue()
//            .atStartOfDay()
//            .atZone(ZoneId.systemDefault())).toInstant())
//        );
//        newSale.setUpdatedAt(Date.from((createdDatePicker
//            .getValue()
//            .atStartOfDay()
//            .atZone(ZoneId.systemDefault())).toInstant())
//        );
//        newSale.setNotice(noticeCTextField.getText());
//        //В БД добавляется новая запись, а соответствующий JPAController
//        //заносит ИД этой записи в глобальный объект - для дальнейшего использования
//        if(mEditMode){
//        
//            mSalesDAOImpl.updateSale(newSale);
//            
//            int newSaleBarrelNewPeriod =
//                WS1.barrelsControllerInstance.callCalcPeriod(mSelectedBarrel, newSale.getCreatedAt());
//            
//            System.out.println("newSaleBarrelNewPeriod" + newSaleBarrelNewPeriod);
//            
//            mTmpLastSaleId = newSale.getId();
//            //Если у доставки были изменения долга,
//            //и старая дата доставки отличается от новой,
//            //то во всех изменениях долга доставки
//            //меняем дату на новую
//            List<DebtChange> saleDayDebtChangeList =
//                mDebtsDAOImpl.getFilteredDebts(
//                    -1
//                    , -1
//                    , oldDate
//                    , -1
//                    , -1
//                    , -1
//                    , -1
//                );
//            saleDayDebtChangeList =
//                saleDayDebtChangeList.stream()
//                    .filter(debt ->
//                        ((DebtChange)debt).getSaleId() == mEditingSale.getId())
//                    .collect(Collectors.toList());
//            //
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            
//            //
//            if (saleDayDebtChangeList != null
//                    &&  oldDate != null
//                    && saleDayDebtChangeList.size() > 0
//                    && !simpleDateFormat.format(oldDate).equals(simpleDateFormat.format(newSale.getCreatedAt()))) {
//                
//                //
//                for (DebtChange saleDayDebtChange : saleDayDebtChangeList) {
//                    
//                    saleDayDebtChange.setDate(newSale.getCreatedAt());
//                    mDebtChangesDAOImpl.updateDebtChange(saleDayDebtChange);
//                }
//            }
//        } else {
//        
//            mSalesDAOImpl.createSale(newSale);
//            
//            int newSaleBarrelNewPeriod =
//                WS1.barrelsControllerInstance.callCalcPeriod(mSelectedBarrel, newSale.getCreatedAt());
//            
//            System.out.println("newSaleBarrelNewPeriod" + newSaleBarrelNewPeriod);
//            
//            //Получаем ID только что сохраненной доставки
//            mTmpLastSaleId = Globals.getLastSaleId();
//        }
//
//        //if new credit exists
//        if(mCredit > 0.0d){
//        
//            //add new debt change row (credit)
//            DebtChange newDebt = new DebtChange();
//            newDebt.setShopId(mSelectedShop.getId());
//            newDebt.setIsDebt(false);
//            newDebt.setValue(BigDecimal.valueOf(mCredit));
//            newDebt.setDate(Date.from((createdDatePicker
//                .getValue()
//                .atStartOfDay()
//                .atZone(ZoneId.systemDefault())).toInstant())
//            );
//            newDebt.setDebtId(-1);
//            newDebt.setBalance(BigDecimal.valueOf(mCredit));
//            newDebt.setSaleId(mTmpLastSaleId);
//            
//            newDebt.setNotReqAmort(false);
//            
//            newDebt.setIsCredit(true);
//            
//            mDebtChangesDAOImpl.createDebtChange(newDebt);
//            
//            mCredit = 0.0d;
//        }
//        
//        //если был включен чекбокс "замена",
//        //делаем настройку выбранной бочке "недавно заменена"
//        if (repairCheckBox.isSelected()) {
//            
//            mSelectedBarrel.setRecentlyReplaced(true);
//        } else {
//            //иначе - сбрасываем настройку "недавно заменена"
//            // выбранной бочке
//            mSelectedBarrel.setRecentlyReplaced(false);
//        }
//        //если был включен чекбокс "чистка",
//        //делаем настройку выбранной бочке:
//        //обновляем дату последней чистки у объекта
//        if (cleanCheckBox.isSelected()) {
//            
//            mSelectedBarrel.setLastCDate(Date.from((createdDatePicker
//                .getValue()
//                .atStartOfDay()
//                .atZone(ZoneId.systemDefault())).toInstant())
//
//            );
//        }
//
//        //в любом случае указываем выбранной бочке ИД
//        //только что сохраненной доставки
//        mSelectedBarrel.setLastSaleId(mTmpLastSaleId);
//        //а также сохраняем последнее значение счетчика
//        mSelectedBarrel.setCounter(
//            Integer.parseInt(countNewAfterCTextField.getText())
//        );
//
//        mBarrelsDAOImpl.updateBarrel(mSelectedBarrel);
//        
//        /*Update credits*/
//        if (mShopActiveCredits != null
//            && mShopActiveCredits.size() > 0) {
//
//            for (DebtChange creditDebtChange : mShopActiveCredits) {
//
//                mDebtChangesDAOImpl.updateDebtChange(creditDebtChange);
//                System.out.println(creditDebtChange.getBalance());
//                                    System.out.println();
//            }
//            WS1.debtsControllerInstance.updateDebtChangesForPage();
//        }
//
//        //если был включен чекбокс "работа с долгами" (проверяем флаг) -
//        //показываем блок работы с долгами, иначе - показываем сообщение
//        //об успешном добавлении информации о доставке
//        //if (showDebtsBlockCheckBox.isSelected()) {
//            
//            //Когда доставка уже сохранилась в БД, отображаем блок
//            //работы с долгами, если был выбран соотв-й чекбокс
//        if (mShowDebtsBlock && !mEditMode) {
//
//            newDebtHBox.setVisible(true);
//            amortDebtsHBox.setVisible(true);
//            finishEditDebtsHBox.setVisible(true);
//
//            //newDebtHBox.setFocusTraversable(true);
//            //amortDebtsHBox.setFocusTraversable(true);
//            //finishEditDebtsHBox.setFocusTraversable(true);
//
//            debtCTextField.setEditable(true);
//            debtAmortCTextField.setEditable(true);
//            
//            //Скрываем основные кнопки формы
//            //mainButtonsHBox.setFocusTraversable(false);
//            
//            addSaleButton.setDisable(true);
//            resetFormButton.setDisable(true);
//            backButton.setDisable(true);
//            //}
//        } else {
//        
//            doPostSaleSaving();
//        }
//    }
//    
//    private void doPostSaleSaving(){
//    
//        //Сообщение пользователю об успешном добавлении доставки в БД
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("Информация");
//        alert.setHeaderText(
//            !mEditMode
//                ?"Доставка успешно добавлена"
//                :"Доставка успешно изменена"
//        );
//        //Показать сообщение и ждать
//        alert.showAndWait();
//        
//        //Приводим форму в исходное состояние
//        resetForm();
//        //Вызываем обновление данных в коллекции-источнике для
//        //таблицы доставок (в представлении доставок)
//        WS1.salesControllerInstance.updateSalesForPage();
//        //Оповещаем контроллер бочек, что список бочек нужно обновить,
//        //т.к. добавление продажи для некоторой бочки меняет ее
//        //состояние, в частности - дату последней чистки и факт замены
//        WS1.barrelsControllerInstance.updateBarrelsForPage();
//        //Вызываем обновление данных в коллекции-источнике для
//        //таблицы магазинов (в представлении магазинов)
//        WS1.shopsControllerInstance.updateShopsForPage();
//    }
//    
//    public boolean isEditMode()
//    {
//        return mEditMode;
//    }
//
//    public void setEditMode(boolean _editMode)
//    {
//        mEditMode = _editMode;
//        //для режима редактирования
//        if (mEditMode) {
//            
//            captionLabel.setText("Изменить доставку");
//            addSaleButton.setText("Изменить");
//            debtsTogglerHBox.setVisible(false);
//            countOldCTextField.setEditable(true);
//            countOldCTextField.setFocusTraversable(true);
//            
//            //
//            createdDatePicker.setValue(
//                mEditingSale.getCreatedAt()
//                    .toInstant()
//                    .atZone(ZoneId.systemDefault())
//                    .toLocalDate()
//            );
//            carCTextField.setText(
//                String.valueOf(
//                    mCarsDAOImpl
//                        .getCar(mEditingSale.getCarId())
//                        .getNumber()
//                )
//            );
//            driverCTextField.setText(
//                String.valueOf(
//                    mDriversDAOImpl
//                        .getDriver(mEditingSale.getDriverId())
//                        .getName()
//                )
//            );
//            mSelectedShop = mShopsDAOImpl.getShop(mEditingSale.getShopId());
//            shopCTextField.setText(
//                String.valueOf(mSelectedShop.getName())
//            );
//            mSelectedBarrel =
//                mBarrelsDAOImpl.getBarrel(mEditingSale.getBarrelId());
//            if (!Objects.equals(mEditingSale.getId(), mSelectedBarrel.getLastSaleId())) {
//                
//                mAbortState = true;
//                Alert alert = new Alert(Alert.AlertType.ERROR);
//                alert.setTitle("Ошибка");
//                alert.setHeaderText("Доставка не изменена");
//                alert.setContentText("Отмена попытки изменить не самую последнюю доставку для данной бочки. Данное действие могло бы привести базу данных в некорректное состояние");
//                alert.showAndWait();
//                //goToSalesScreen();
//            }
//            barrelComboBox.getSelectionModel().select(mSelectedBarrel);
//            cleanCheckBox.setSelected(mEditingSale.getCleaning());
//            cleanCheckBoxEventHandler();
//            repairCheckBox.setSelected(mEditingSale.getRepair());
//            repairCheckBoxEventHandler();
//            countOldCTextField.setText(
//                String.valueOf(mEditingSale.getCounterOld())
//            );
//            /*countNewCTextField.setText(
//                String.valueOf(mEditingSale.getCounterNew())
//            );
//            countNewAfterCTextField.setText(
//                String.valueOf(mEditingSale.getCounterNew())
//            );*/
//        //для режима добавления
//        } else {
//        
//            captionLabel.setText("Добавить доставку");
//            addSaleButton.setText("Добавить");
//            //показываем переключатель видимости блока работы с долгами
//            debtsTogglerHBox.setVisible(true);
//            countOldCTextField.setEditable(false);
//            countOldCTextField.setFocusTraversable(false);
//        }
//    }
//
//    public void setEditingSale(Sale _editingSale)
//    {
//        mEditingSale = _editingSale;
//    }
//    
//    private void cleanCheckBoxEventHandler(){
//    
//        if (cleanCheckBox.isSelected() || repairCheckBox.isSelected()) {
//            countNewAfterHBox.visibleProperty().setValue(true);
//            countNewAfterCTextField.setEditable(true);
//        } else {
//            countNewAfterHBox.visibleProperty().setValue(false);
//            countNewAfterCTextField.setEditable(false);
//        }
//    }
//    
//    private void repairCheckBoxEventHandler(){
//    
//        if (cleanCheckBox.isSelected() || repairCheckBox.isSelected()) {
//            countNewAfterHBox.visibleProperty().setValue(true);
//            countNewAfterCTextField.setEditable(true);
//        } else {
//            countNewAfterHBox.visibleProperty().setValue(false);
//            countNewAfterCTextField.setEditable(false);
//        }
//    }
    
    private void showJsonFetchErrorMsg(){
    
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Ошибка получения данных по сети");
        alert.showAndWait();
    }
    
    private void showEncodingErrorMsg(){
    
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Неподходящий символ");
        alert.showAndWait();
    }
}
