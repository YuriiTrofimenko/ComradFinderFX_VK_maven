/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.viewcontroller;

import org.tyaa.comradfinder.screensframework.ControlledScreen;
import org.tyaa.comradfinder.screensframework.ScreensController;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.tools.ValueExtractor;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.tyaa.comradfinder.model.TypicalWords;
import org.tyaa.comradfinder.modules.facades.ModelBuilder;
import org.tyaa.comradfinder.screensframework.ProgressForm;
import org.tyaa.comradfinder.viewcontroller.viewmodel.VariantModel;


/**
 * FXML Controller class
 *
 * @author Yurii
 */
public class HomeController implements Initializable, ControlledScreen {

    /*Внедренные хендлеры элементов UI*/
    
    @FXML
    Label myLabel;
    
    @FXML
    Button createModelButton;
    
    //
    /*@FXML
    ListView waterTypesListView;
    @FXML
    ListView capacitiesListView;
    
    @FXML
    CustomTextField waterTypeCTextField;
    @FXML
    CustomTextField capacityCTextField;
    @FXML
    CustomTextField сleaningTCTimeCTextField;
    @FXML
    CustomTextField сleaningOCTimeCTextField;
    @FXML
    CustomTextField allowedRestPercent;*/
    
    /*@FXML
    Button waterTypesAddButton;
    @FXML
    Button waterTypesDeleteButton;
    @FXML
    Button capacityAddButton;
    @FXML
    Button capacityDeleteButton;*/
    /*@FXML
    CheckBox replaceCheckBox;
    @FXML
    DatePicker lastCleanDatePicker;*/
    
    private boolean mButtonsEnable;
    
    //Объекты доступа к данным
    //private SalesDAOImpl mSalesDAOImpl;
    //private static ShopsDAOImpl mShopsDAOImpl;
    //private BarrelsDAOImpl mBarrelsDAOImpl;
//    private BarrelCapacitiesDAOImpl mBarrelCapacitiesDAOImpl;
//    private WaterTypesDAOImpl mWaterTypesDAOImpl;
//    
//    private SettingsDAOImpl mSettingsDAOImpl;
    //private DriversDAOImpl mDriversDAOImpl;
    //private CarsDAOImpl mCarsDAOImpl;
    
    //Списки объектов
    //private static List<Shop> mShops;
    //private List<Barrel> mBarrels;
    //private List<Barrel> mShopBarrels;
//    private List<WaterType> mWaterTypes;
//    private List<BarrelCapacity> mBarrelCapacities;
    
    //Наборы полей из объектов
    //private Set<String> mShopNamesSet;
    //private Set<String> mWaterTypesSet;
    //private Set<String> mBarrelCapacitiesSet;
    
    //Выбранные объекты
    //private Shop mSelectedShop;
    //private Barrel mSelectedBarrel;
    //private WaterType mSelectedWaterType;
    //private BarrelCapacity mSelectedBarrelCapacity;
    
    //Наблюдабельный список объектов VariantModel
    ObservableList<VariantModel> mVariantObservableList;
//    ObservableList<BarrelCapacityModel> mBarrelCapacitiesObservableList;
    
    ScreensController myController;
    ValidationSupport validationSupport;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        
        //myLabel.setText("None");
        //WS1.addBarrelControllerInstance = this;
        //System.out.println("HomeController");
        //mSalesDAOImpl = new SalesDAOImpl();
        //mShopsDAOImpl = new ShopsDAOImpl();
        //mBarrelsDAOImpl = new BarrelsDAOImpl();
//        mBarrelCapacitiesDAOImpl = new BarrelCapacitiesDAOImpl();
//        mWaterTypesDAOImpl = new WaterTypesDAOImpl();
//        
//        mSettingsDAOImpl = new SettingsDAOImpl();
        //mShops = mShopsDAOImpl.getAllShops();
        //mShopNamesSet = new HashSet<>();
        
        //mBarrels = mBarrelsDAOImpl.getAllBarrels();
        //mShopBarrels = new ArrayList();
        
        /*mWaterTypes = mWaterTypesDAOImpl.getAllWaterTypes();
        mWaterTypesSet = new HashSet<>();
        
        mBarrelCapacities = mBarrelCapacitiesDAOImpl.getAllBarrelCapacities();
        mBarrelCapacitiesSet = new HashSet<>();*/
        
        //Инициализация пустым наблюдабельным списком
        mVariantObservableList = FXCollections.observableArrayList();
//        mBarrelCapacitiesObservableList = FXCollections.observableArrayList();
//        
//        waterTypesListView.setItems(mWaterTypesObservableList);
//        capacitiesListView.setItems(mBarrelCapacitiesObservableList);
        
        //updateWaterTypes();
        //updateBarrelCapacities();
        
        /*Приведение формы в исходное состояние*/
        
//        resetForm();
//        
//        allowedRestPercent.setText(String.valueOf((int)(Settings.getAllowedRestPecent() * 100)));
//        сleaningOCTimeCTextField.setText(String.valueOf(Settings.getCleaningOverdueCycleTime() / 2592000000D));
//        сleaningTCTimeCTextField.setText(String.valueOf(Settings.getCleaningTypicalCycleTime() / 2592000000D));
//
//        //Активация механизма валидации для элементов ввода типа CustomTextField
//        ValueExtractor.addObservableValueExtractor(
//                c -> c instanceof CustomTextField
//                , c -> ((CustomTextField) c).textProperty());
//        validationSupport = new ValidationSupport();
//        //Явная настройка включения визуального оформления валидации
//        validationSupport.setErrorDecorationEnabled(true);
//        //Настройки валидации для каждого элемента ввода, подлежащего проверке
//        validationSupport.registerValidator(
//            waterTypeCTextField
//            , Validator.createRegexValidator("Введите от одного до 50 символов", ".{1,50}", Severity.ERROR));
//        validationSupport.registerValidator(
//            capacityCTextField
//            , Validator.createRegexValidator("Введите целое число от 0 до 9999", "[0-9]{1,4}", Severity.ERROR));
//        
//        waterTypesListView.setCellFactory((listView) -> {
//        
//            return new ListCell<WaterTypeModel>(){
//                @Override
//                protected void updateItem(WaterTypeModel item, boolean empty)
//                {
//                    super.updateItem(item, empty);
//                    if (item == null || empty) {
//                        setText(null);
//                    } else {
//                        setText(item.getName());
//                    }
//                }
//            };
//        });
//        
//        capacitiesListView.setCellFactory((listView) -> {
//        
//            return new ListCell<BarrelCapacityModel>(){
//                @Override
//                protected void updateItem(BarrelCapacityModel item, boolean empty)
//                {
//                    super.updateItem(item, empty);
//                    if (item == null || empty) {
//                        setText(null);
//                    } else {
//                        setText(String.valueOf(item.getCapacity()));
//                    }
//                }
//            };
//        });
    }    

    @Override
    public void setScreenParent(ScreensController screenParent) {
        myController = screenParent;
    }
    
    @FXML
    private void showCreateModelDialog(){
        
        //progressIndicator.visibleProperty().set(true);
    
        //
        /*mSelectedBarrelModel =
                (BarrelModel) barrelsTableView.getSelectionModel().getSelectedItem();
        
        //
        if (mSelectedBarrelModel != null) {
            
            mSelectedBarrel =
                mBarrelsDAOImpl.getBarrel(mSelectedBarrelModel.getId());*/
            
            TextInputDialog dialog =
                new TextInputDialog("");
            dialog.setTitle("Создание модели");
            dialog.setHeaderText("Для какой группы ВК создать модель совокупного пользователя?");
            dialog.setContentText("Введите id или псевдоним группы (например, 137118920 или tehnokom_su): ");

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(
                groupIdString -> {

                    boolean hasNumFormatErrors = false;
                    //boolean tooMuchErrors = false;
                    
                    try{
                    
                        Task buildModelTask =
                            ModelBuilder.buildModel(groupIdString);
                        
                        ProgressForm pForm = new ProgressForm();
                        
                        // binds progress of progress bars to progress of task:
                        pForm.activateProgressBar(buildModelTask);
                        
                        
                        
                        // this method would get the result of the task
                        // and update the UI based on its value
                        buildModelTask.setOnSucceeded(event -> {
                            pForm.getDialogStage().close();
                            //startButton.setDisable(false);
                            toggleButtonsEnable();
                        });
                        
                        toggleButtonsEnable();
                        
                        pForm.getDialogStage().show();

                        Thread thread = new Thread(buildModelTask);
                        thread.setDaemon(true);
                        thread.start();
                        
                        /*Pattern pattern = 
                            Pattern.compile("[0-9]{1,4}");
                        if (pattern.matcher(changeAllowedRestString).matches()
                            && Integer.valueOf(changeAllowedRestString)
                                <= mBarrelCapacitiesDAOImpl
                                    .getBarrelCapacity(
                                            mSelectedBarrel.getCapacityId()).getCapacity()
                                ) {*/

                                //mSelectedBarrel.setAllowedRest(Integer.valueOf(changeAllowedRestString));
                                //mBarrelsDAOImpl.updateBarrel(mSelectedBarrel);
                                //Notify self
                                //updateBarrelsForPage();
                                //нотифицировать контроллер добавления доставок
                                //WS1.addSaleControllerInstance.updateBarrels();
                        /*} else {

                            hasNumFormatErrors = true;
                        }*/
                    } catch (NumberFormatException ex){

                        hasNumFormatErrors = true;
                    }
                    if (hasNumFormatErrors) {
                        
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText("Изменение допустимого остатка воды не выполнено");
                        alert.setContentText("Ошибка формата числа (максимум 9999) или число больше полного объема бочки");
                        alert.showAndWait();
                    }
                }
            );
        /*} else {
        
            Alert warningAlert =
                new Alert(Alert.AlertType.WARNING);
            warningAlert.setTitle("Предупреждение");
            warningAlert.setHeaderText("Не выбрана ни одна бочка");
            warningAlert.setContentText("Выделите одну строку в таблице");
            warningAlert.showAndWait();
        }*/
        //progressIndicator.visibleProperty().set(false);
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
    
    @FXML
    private void goToSalesScreen(ActionEvent event){
       myController.setScreen(WS1.salesID);
       WS1.primaryStage.setMaximized(true);
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
    private void fillVariantObservableList(TypicalWords _typicalWords){
        
        _typicalWords.mInterestMap.forEach (
                        
            (k, v) -> {
            
                mVariantObservableList.add(new VariantModel("interest", k, v));
            }
        );
        
        _typicalWords.mActivityMap.forEach (
                        
            (k, v) -> {
            
                mVariantObservableList.add(new VariantModel("activity", k, v));
            }
        );
        
        _typicalWords.mAboutMap.forEach (
                        
            (k, v) -> {
            
                mVariantObservableList.add(new VariantModel("about", k, v));
            }
        );
        
        _typicalWords.mAboutMap.forEach (
                        
            (k, v) -> {
            
                mVariantObservableList.add(new VariantModel("about", k, v));
            }
        );
    }
    
    private void toggleButtonsEnable(){
    
        if (mButtonsEnable) {
            
            createModelButton.setDisable(true);
        } else {
        
            createModelButton.setDisable(false);
        }
    }
    
}
