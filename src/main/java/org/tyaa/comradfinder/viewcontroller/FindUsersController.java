/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.viewcontroller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.tyaa.ws.screensframework.ControlledScreen;
import org.tyaa.ws.screensframework.ScreensController;
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
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.tools.ValueExtractor;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationMessage;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.tyaa.ws.WS1;
import org.tyaa.ws.common.Globals;
import org.tyaa.ws.dao.impl.BarrelCapacitiesDAOImpl;
import org.tyaa.ws.dao.impl.ShopsDAOImpl;
import org.tyaa.ws.dao.impl.BarrelsDAOImpl;
import org.tyaa.ws.dao.impl.DriversDAOImpl;
import org.tyaa.ws.dao.impl.CarsDAOImpl;
import org.tyaa.ws.dao.impl.DebtChangesDAOImpl;
import org.tyaa.ws.dao.impl.DebtsDAOImpl;
import org.tyaa.ws.dao.impl.SalesDAOImpl;
import org.tyaa.ws.entity.Sale;
import org.tyaa.ws.entity.Shop;
import org.tyaa.ws.entity.Barrel;
import org.tyaa.ws.entity.Driver;
import org.tyaa.ws.entity.Car;
import org.tyaa.ws.entity.DebtChange;
import org.tyaa.ws.viewmodel.ExistingDebtModel;

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
    CustomTextField cityCTextField;
    @FXML
    ComboBox ageComboBox;
    @FXML
    CheckBox femaleCheckBox;
    @FXML
    CheckBox mailCheckBox;
    
    //
    @FXML
    Button findUsersButton;
    @FXML
    Button resetFormButton;
    @FXML
    Button backButton;
    
    //Объекты доступа к данным
    private SalesDAOImpl mSalesDAOImpl;
    private ShopsDAOImpl mShopsDAOImpl;
    private BarrelsDAOImpl mBarrelsDAOImpl;
    private BarrelCapacitiesDAOImpl mBarrelCapacitiesDAOImpl;
    private DriversDAOImpl mDriversDAOImpl;
    private CarsDAOImpl mCarsDAOImpl;
    private DebtChangesDAOImpl mDebtChangesDAOImpl;
    private DebtsDAOImpl mDebtsDAOImpl;
    
    //Списки объектов
    private List<Shop> mShops;
    private List<Barrel> mBarrels;
    //Бочки выбранного магазина 
    private List<Barrel> mShopBarrels;
    private List<Driver> mDrivers;
    private List<Car> mCars;
    //Непогашенные долги выбранного магазина
    private List<DebtChange> mShopActiveDebts;
    //did not used credits of choosed shop
    private List<DebtChange> mShopActiveCredits;
    
    //Наборы для автодополнения в полях ввода
    private Set<String> mCountryNamesSet;
    private Set<String> mCityNamesSet;
    
    //хендлеры к наборам автодополнения
    private AutoCompletionBinding<String> mCountryAutoCompletionBinding;
    private AutoCompletionBinding<String> mCityAutoCompletionBinding;
    
    //Выбранные объекты
    private Shop mSelectedCountry;
    private Barrel mSelectedCity;
    
    
    //ID только что добавленной доставки
    private int mTmpLastSaleId;
    
    //Должная сумма, вычисленная по литрам
    private BigDecimal mMustPay;
    
    //Активные, не активные, все?
    //(1, 0, -1)
    private int mActive;
    //Показывать ли блок долгов?
    private boolean mShowDebtsBlock;
    //В таблицу доставок в БД уже добавлена первая разность между добавившимся долгом
    //и суммой первого погашения
    private boolean mSaleDebtAdded;
    
    //Флаг: изменение значения в поле дохода вызвано редактированием
    //значений других полей
    private boolean mNoProfitFieldChange;
    
    //Разрядность водомера
    private int mCounterMaxPosition;
    //Максимальное значение на водомере
    private int mCounterMaxNumber;
    
    //
    private double mCredit;
    //
    //private boolean mShopCreditApplied;
    
    //
    private boolean mEditMode;
    private Sale mEditingSale;
    
    public boolean mAbortState = false;
    
    ObservableList<Barrel> mBarrelsObservableList;
    ObservableList<DebtChange> mDebtChangesObservableList;
    
    ScreensController myController;
    ValidationSupport validationSupport;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        mShopNamesSet = new HashSet<>();
        
        //mBarrels = mBarrelsDAOImpl.getAllBarrels();
        
        mBarrels = mBarrelsDAOImpl.getFilteredBarels(
                -1
                //, mFilterBarrelId
                //, -1
                , null
                , mActive
                , -1
                , -1
            );
        mShopBarrels = new ArrayList();
        mShopActiveDebts = new ArrayList();
        mShopActiveCredits = new ArrayList();
        
        //mDrivers = mDriversDAOImpl.getAllDrivers();
        mDrivers = mDriversDAOImpl.getFilteredDrivers(mActive, -1, -1);
        mDriverNamesSet = new HashSet<>();
        
        //mCars = mCarsDAOImpl.getAllCars();
        mCars = mCarsDAOImpl.getFilteredCars(mActive, -1, -1);
        mCarNamesSet = new HashSet<>();
        
        mBarrelsObservableList = FXCollections.observableArrayList();
        mDebtChangesObservableList = FXCollections.observableArrayList();
        
        //Поле ввода "старые показания водомера" всегда будет пропускаться
        //при обходе контролов табуляцией
        countOldCTextField.setFocusTraversable(false);
        
        //По умолчанию контроллер будет настраиваться
        //на режим добавления новой доставки
        setEditMode(false);
        
        /*Приведение формы в исходное состояние*/
        resetForm();
        

        //Активация механизма валидации для элементов ввода типа CustomTextField
        ValueExtractor.addObservableValueExtractor(
                c -> c instanceof CustomTextField
                , c -> ((CustomTextField) c).textProperty());
        validationSupport = new ValidationSupport();
        //Явная настройка включения визуального оформления валидации
        validationSupport.setErrorDecorationEnabled(true);
        //Настройки валидации для каждого элемента ввода, подлежащего проверке
        validationSupport.registerValidator(
                shopCTextField
                , Validator.createEmptyValidator("Название магазина обязательно"));
        /*validationSupport.registerValidator(
                barrelComboBox
                , Validator.createEmptyValidator("Barrel is required"));*/
        validationSupport.registerValidator(
                driverCTextField
                , Validator.createEmptyValidator("Имя водителя обязательно"));
        validationSupport.registerValidator(
                carCTextField
                , Validator.createEmptyValidator("Номер автомобиля обязателен"));
//        validationSupport.registerValidator(
//                countOldCTextField
//                , Validator.createEmptyValidator("Старые показания счетчика обязательны"));
//        validationSupport.registerValidator(
//                countOldCTextField
//                , Validator.createRegexValidator("Введите целое число от 0 до 99999", "[0-9]{1,5}", Severity.ERROR));
//        validationSupport.registerValidator(
//                countNewCTextField
//                , Validator.createEmptyValidator("Новые показания счетчика обязательны"));
        
        /*Не применяем автоматическую валидацию к полю счетчика, т.к.
        правило проверки может меняться в зависимости от выбранной бочки (разрядности ее водомера)*/
        validationSupport.registerValidator(
            countNewCTextField
            , Validator.createRegexValidator("Введите целое число, максимальное число знаков - " + mCounterMaxPosition, "[0-9]{1," + mCounterMaxPosition + "}", Severity.ERROR));
        
        validationSupport.registerValidator(
            volumeCTextField
            , Validator.createRegexValidator("Введите целое число от 0 до 9999", "[0-9]{1,4}", Severity.ERROR));
//        validationSupport.registerValidator(
//                profitCTextField
//                , Validator.createEmptyValidator("Сумма оплаты обязательна"));
        validationSupport.registerValidator(
                profitCTextField
                , Validator.createRegexValidator("Введите положительное целое или дробное число (максимум - 99999.99)", "[0-9]{1,5}([.,][0-9]{0,2}){0,1}", Severity.ERROR));
//        validationSupport.registerValidator(
//                debtCTextField
//                , Validator.createRegexValidator("Введите положительное целое или дробное число (максимум - 99999.99)", "[-]{0,1}[0-9]{1,5}[.]{0,1}[0-9]{0,2}", Severity.ERROR));
        validationSupport.registerValidator(
                debtCTextField
                , Validator.createRegexValidator("Введите положительное целое или дробное число (максимум - 99999.99)", "[0-9]{1,5}([.,][0-9]{0,2}){0,1}", Severity.ERROR));
        validationSupport.registerValidator(
                debtAmortCTextField
                , Validator.createRegexValidator("Введите целое или дробное число (максимум - 99999.99)", "[-]{0,1}[0-9]{1,5}([.,][0-9]{0,2}){0,1}", Severity.ERROR));
        
        /*Настраиваем способ отображения информации из объектов,
        подключенных в составе коллекции-источника к контролам списочного типа*/
        
        //прорисовка выпадающего списка бочек
        barrelComboBox.setCellFactory((comboBox) -> {
            return new ListCell<Barrel>() {
                @Override
                protected void updateItem(Barrel barrelItem, boolean empty) {
                    super.updateItem(barrelItem, empty);

                    if (barrelItem == null || empty) {
                        setText(null);
                    } else {
                        setText(
                            barrelItem.getWhaterTId().getName()
                            + ", "
                            + mBarrelCapacitiesDAOImpl.getBarrelCapacity(
                                    barrelItem.getCapacityId()
                            ).getCapacity()
                            + " л"
                        );
                    }
                }
            };
        });
        //прорисовка выбранного элемента выпадающего списка бочек
        barrelComboBox.setConverter(new StringConverter<Barrel>() {
            @Override
            public String toString(Barrel barrel) {
                if (barrel == null) {
                    return null;
                } else {
                    return (
                        barrel.getWhaterTId().getName()
                        + ", "
                        + mBarrelCapacitiesDAOImpl.getBarrelCapacity(
                                barrel.getCapacityId()
                        ).getCapacity()
                        + " л"
                    );
                }
            }

            @Override
            public Barrel fromString(String barrelString) {
                return null; // No conversion fromString needed.
            }
        });
        
        //прорисовка выпадающего списка существующих долгов выбранного магазина
        debtsComboBox.setCellFactory((comboBox) -> {
            return new ListCell<DebtChange>() {
                @Override
                protected void updateItem(DebtChange debtChangeItem, boolean empty) {
                    super.updateItem(debtChangeItem, empty);

                    if (debtChangeItem == null || empty) {
                        setText(null);
                    } else {
                        setText(
                            debtChangeItem.getBalance()
                            + " ("
                            + debtChangeItem.getValue()
                            + " грн "
                            + new SimpleDateFormat("dd.MM.yyyy").format(debtChangeItem.getDate())
                            + ")"
                        );
                    }
                }
            };
        });
        
        //прорисовка выбранного элемента выпадающего списка
        //существующих долгов выбранного магазина
        debtsComboBox.setConverter(new StringConverter<DebtChange>() {
            @Override
            public String toString(DebtChange debtChangeItem) {
                if (debtChangeItem == null) {
                    return null;
                } else {
                    return (
                        debtChangeItem.getBalance()
                            + " ("
                            + debtChangeItem.getValue()
                            + " грн "
                            + new SimpleDateFormat("dd.MM.yyyy").format(debtChangeItem.getDate())
                            + ")"
                    );
                }
            }

            @Override
            public DebtChange fromString(String debtChangeString) {
                return null; // No conversion fromString needed.
            }
        });
        
        /*Настраиваем здесь обработчики событий тех контрлов,
        для которых это невозможно при помощи внедрения */
        
        //обработка события "ввод названия магазина"
        shopCTextField.textProperty().addListener(new ChangeListener(){
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                
                mShopBarrels.clear();
                mShopActiveDebts.clear();
                mShopActiveCredits.clear();
                mBarrelsObservableList.clear();
                mDebtChangesObservableList.clear();
                
                /*Выбираем бочки и долги, относящиеся к выбранному магазину*/
                
                //Находим объект магазина с таким именем, которое выбрано из списка
                if (!shopCTextField.textProperty().getValue().equals("")
                        && mShopNamesSet.contains(shopCTextField.textProperty().getValue())) {
                    
                    for (Shop shop : mShops) {
                        
                        if (shop.getName().equals(shopCTextField.textProperty().getValue())) {
                            
                            mSelectedShop = shop;
                            break;
                        }
                    }
                    
                    /*Работа с бочками*/
                    
                    //Набираем в список объекты бочек выбранного магазина
                    for (Barrel barrel : mBarrels) {
                        
                        if (Objects.equals(barrel.getShopId(), mSelectedShop.getId())) {
                            mShopBarrels.add(barrel);
                        }
                    }
                    
                    //... и заполняем этими объектами наблюдабельный список
                    for (Barrel barrel : mShopBarrels) {

                        mBarrelsObservableList.add(barrel);
                    }
                    
                    //Если в наблюдабельном списке есть хотя бы один объект -
                    //устанавливаем его как источник данных для комбо-бокса
                    if (mBarrelsObservableList.size() > 0) {
                        
                        barrelComboBox.setItems(mBarrelsObservableList);
                    }
                    
                    /*Работа с непогашеными долгами*/
                    
                    //Пытаемся получить список долгов по ИД магазина
                    mShopActiveDebts =
                        mDebtChangesDAOImpl.getFilteredDebtChanges(
                            mSelectedShop.getId()
                            , true
                        );
                    
                    for (DebtChange debtChange : mShopActiveDebts) {
                        
                        //Предлагаем гасить только активные долги,
                        //требующие погашения,
                        //исключаем авансы
                        if (!debtChange.isNotReqAmort()
                                && !debtChange.getIsCredit()) {
                            
                            mDebtChangesObservableList.add(debtChange);
                        }
                    }
                    
                    if (mDebtChangesObservableList.size() > 0) {
                        
                        debtsComboBox.setItems(mDebtChangesObservableList);
                    }
                    
                    /*Get credits*/
                    mShopActiveCredits =
                        mDebtChangesDAOImpl.getActiveCreditDebtChanges(
                            mSelectedShop.getId()
                            , true
                        );
                }
            }
        });
        
        //обработка события "выбор бочки"
        barrelComboBox.valueProperty().addListener(new ChangeListener()
        {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue)
            {
                mSelectedBarrel = (Barrel) barrelComboBox
                    .getSelectionModel()
                    .getSelectedItem();
                if (mSelectedBarrel != null) {

                    if (isEditMode()) {
                        
                        countOldCTextField.setText("");
                    } else {
                    
                        countOldCTextField.setText(
                            String.valueOf(mSelectedBarrel.getCounter())
                        );
                    }
                    mCounterMaxPosition = mSelectedBarrel.getPositions();
                    validationSupport.registerValidator(
                        countNewCTextField
                        , Validator.createRegexValidator("Введите целое число, максимальное число знаков - " + mCounterMaxPosition, "[0-9]{1," + mCounterMaxPosition + "}", Severity.ERROR)
                    );
                    positionLabel.setText("не более " + mCounterMaxPosition + " знаков");
                    switch(mCounterMaxPosition){

                        case 5:
                        {
                            mCounterMaxNumber = 99999;
                            break;
                        }
                        case 6:
                        {
                            mCounterMaxNumber = 999999;
                            break;
                        }
                        case 7:
                        {
                            mCounterMaxNumber = 9999999;
                            break;
                        }
                    }
                } else {
                    
                    countOldCTextField.setText("");
                }
            }
        });
        //По нажатию клавиши Ввод или Пробел комбобокс
        //должен показать свой список
        barrelComboBox.setOnKeyPressed((event) -> {
            
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                
                barrelComboBox.show();
            }
        });
        
        debtsComboBox.setOnKeyPressed((event) -> {
            
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                
                debtsComboBox.show();
            }
        });
                
        //обработка события "переключение чекбокса"
        cleanCheckBox.setOnAction((event) -> {
            cleanCheckBoxEventHandler();
        });
        repairCheckBox.setOnAction((event) -> {
            repairCheckBoxEventHandler();
        });
        showDebtsBlockCheckBox.setOnAction((event) -> {
            
            if (showDebtsBlockCheckBox.isSelected()) {
                
                mShowDebtsBlock = true;
            } else {
                
                mShowDebtsBlock = false;
            }
        });
        
        //обработка события "ввод текста в поле счетчик New"
        countNewCTextField.textProperty().addListener(new ChangeListener(){
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                
                //Копируем текст из поля "счетчик New"
                //в поле "счетчик NewAfter"
                //(в дальнейшем текст 3 поля можно изменить вручную)
                countNewAfterCTextField.textProperty().setValue(
                        countNewCTextField.textProperty().getValue()
                );               
                
                if (
                    !countNewCTextField.textProperty().getValue().equals("")
                    && mSelectedBarrel != null
                ) {
                    //рассчитываем количество потребленной ранее воды
                    int volume = 0;
                    try{
                    
                        volume = Integer.parseInt(
                            countNewCTextField.textProperty().getValue()
                        ) - Integer.parseInt(countOldCTextField.getText());
                    }catch(NumberFormatException ex){

                        //volume = 0;
                    }
                    //System.out.println("voloume = " + volume);
                    //Если значение счетчика пересекло максимум,
                    //пересчитываем объем с учетом этой особенности
                    if (volume < 0) {
                        
                        volume =
                            (mCounterMaxNumber - mSelectedBarrel.getCounter())
                            + Integer.parseInt(countNewCTextField.textProperty().getValue())
                            + 1;
                        //Введено значение нового счетчика меньше значения старого,
                        //и при этом объем потребления оказался слишком большим
                        if (volume > 9999) {
                            
                            volume = 0;
                        }
                    }
                    
                    //
                    mNoProfitFieldChange = true;
                    
                    //и выводим его в поле "запрвлено",
                    //это же значение считаем объемом воды,
                    //на который была пополнена бочка при данной доставке
                    volumeCTextField.textProperty().setValue(
                        String.valueOf(volume)
                    );
                    
                    //рассчитываем сумму оплаты за потребленные литры...
                    
                    //base cost
                    BigDecimal profit =
                        BigDecimal.valueOf(
                            (double)volume).multiply(mSelectedBarrel.getPrice()
                        );
                    System.out.println("profit " + profit);
                    //cost - credit (if exists)
                    
                    /*Get credits*/
                    /*mShopActiveCredits.clear();
                    mShopActiveCredits =
                        mDebtChangesDAOImpl.getActiveCreditDebtChanges(
                            mSelectedShop.getId()
                            , true
                        );
                    System.out.println("mShopActiveCredits " + mShopActiveCredits);
                    System.out.println("mShopActiveCredits.size() " + mShopActiveCredits.size());
                    
                    String creditRestString = null;
                    Double initialProfit = profit.doubleValue();
                    Double diffProfit = 0.0d;
                    
                    if (mShopActiveCredits != null
                        && mShopActiveCredits.size() > 0) {
                        
                        double existingCreditSum = 0.0d;
                        
                        for (DebtChange creditDebtChange : mShopActiveCredits) {
                            
                            existingCreditSum +=
                                creditDebtChange.getBalance().doubleValue();
                        }
                        System.out.println("existingCreditSum " + existingCreditSum);
                        if(profit.doubleValue() > 0.0d
                            && existingCreditSum > 0.0d){
                            
                             //mNoProfitFieldChange = true;
                             mShopCreditApplied = true;
                        
                            double profitDouble = profit.doubleValue();
                            
                            Iterator creditIterator =
                                mShopActiveCredits.iterator();
                            
                            while (creditIterator.hasNext()) {
                            
                                DebtChange credit =
                                    (DebtChange)creditIterator.next();
                                
                                double creditRestDouble =
                                    credit.getBalance().doubleValue();
                                
                                if (creditRestDouble > 0.0d) {
                                    
                                    profitDouble =
                                        profitDouble - credit.getBalance().doubleValue();

                                    System.out.println(profitDouble);
                                    if (profitDouble < 0.0d) {

                                        creditRestDouble = - profitDouble;
                                        profitDouble = 0.0d;
                                    } else {
                                    
                                        creditRestDouble = 0;
                                    }
                                    System.out.println(creditRestDouble);
                                    System.out.println();
                                    credit.setBalance(BigDecimal.valueOf(creditRestDouble));

                                    if (profitDouble == 0.0d) {

                                        break;
                                    }
                                }
                            }
                            profit =
                                BigDecimal.valueOf(
                                    profitDouble
                                );
                        }
                    }
                    
                    if (mShopCreditApplied) {
                    
                        creditRestString = "";
                    }*/
                    
                    //... и выводим ее в поле "оплачено", ...
                    if (profit.doubleValue() > 0.0d) {
                        
                        profitCTextField.textProperty().setValue(
                            String.valueOf(profit).substring(0, String.valueOf(profit).length() - 1)
                        );
                    } else {
                    
                        profitCTextField.textProperty().setValue("0.0");
                    }
                    
                    
                    //... а также - на метку "к оплате"
                
                    //сохраняем в переменную должной оплаты
                    mMustPay = new BigDecimal(profit.doubleValue());
                    //
                    String toPayString = null;
                    /*if (creditRestString != null) {
                        
                        diffProfit = initialProfit - profit.doubleValue();
                        creditRestString = diffProfit.toString();
                        
                        toPayString = "к оплате: "
                            + mMustPay.toString()
                            + " (- "
                            + creditRestString
                            + " )";
                    } else {*/
                    
                        toPayString = "(к оплате: "
                            + new DecimalFormat("#0.00").format(mMustPay.doubleValue())
                            + " )";
                    //}
                    toPayLabel.setText(toPayString);
                }
            }
        });
        
        //обработка события "ввод текста в поле volume"
        volumeCTextField.textProperty().addListener(new ChangeListener() {
            
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue)
            {

                //Реагируем, только если выбрана бочка и что-то введено
                //в поле объема воды
                if (
                    !volumeCTextField.textProperty().getValue().equals("")
                    && mSelectedBarrel != null
                ) {
                    //считываем количество потребленной ранее воды
                    int volume = 0;
                    try{

                        volume = Integer.parseInt(volumeCTextField.getText());
                    }catch(NumberFormatException ex){

                    }

                    //рассчитываем сумму оплаты за потребленные литры
                    BigDecimal profit =
                        BigDecimal.valueOf(
                            (double)volume
                        ).multiply(mSelectedBarrel.getPrice());

                    //
                    mNoProfitFieldChange = true;
                    
                    //и выводим ее в поле "оплачено"
                    profitCTextField.textProperty().setValue(
                        String.valueOf(profit).substring(0, String.valueOf(profit).length() - 1)
                    );
                    
                    //сохраняем в переменную должной оплаты
                    mMustPay = new BigDecimal(profit.doubleValue());
                    toPayLabel.setText("(к оплате: "
                            + new DecimalFormat("#0.00").format(mMustPay.doubleValue())
                            + " )");
                }
            }
        });
        
        //обработка события "ввод текста в поле оплата"
        profitCTextField.textProperty().addListener(new ChangeListener() {
            
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue)
            {
                //
                if (mNoProfitFieldChange != true) {
                    //если в поле есть не пустая строка
                    if (!profitCTextField.textProperty().getValue().equals("")) {
                        
                        //считываем ее значение как дробное число
                        Double profit = 0.0d;
                        Double deltaProfit = 0.0d;
                        try{
                            //если символы строки содержат integer or дробное число -
                            //сохраняем его в переменную
                            profit = Double.parseDouble(profitCTextField.getText().replaceAll(",", "."));
                        }catch(NumberFormatException ex){
                            //если символы строки не содержат дробное число - ничего не делаем
                        }
                        //если есть положительное значение должной оплаты,
                        //и оно больше, чем реально полученная оплата,
                        //и отключен режим редактирования доставки
                        //
                        if (mMustPay.doubleValue() > 0.0d
                            && mMustPay.doubleValue() > profit
                            && !mEditMode) {
                            //&& !mShopCreditApplied) {

                            //Находим разность между должной и реальной оплатами
                            deltaProfit = mMustPay.doubleValue() - profit;
                            //...если она положительная
                            if (deltaProfit > 0.0d) {
                                // - копируем ее в поле суммы нового долга
                                //и устанавливаем чекбокс работы с долгами
                                
                                debtCTextField.textProperty().setValue(
                                    //String.valueOf(deltaProfit)
                                    /*String.valueOf(
                                        new BigDecimal(deltaProfit)
                                            .setScale(2, RoundingMode.UP)
                                                .doubleValue()
                                    )*/
                                    String.valueOf(new DecimalFormat("#0.00").format(deltaProfit))
                                        //new BigDecimal(d).setScale(2, RoundingMode.UP).doubleValue()
                                );
                                showDebtsBlockCheckBox.setSelected(true);
                                mShowDebtsBlock = true;
                            }
                        } else {
                            
                            /*if (mShopCreditApplied) {
                                
                                mShopCreditApplied = false;
                            }*/

                            debtCTextField.setText("0");
                            showDebtsBlockCheckBox.setSelected(false);
                            mShowDebtsBlock = false;
                            
                            //if profit is greight than mustPay
                            if (mMustPay.doubleValue() < profit) {
                                
                                //itialize class field mCredit
                                deltaProfit = profit - mMustPay.doubleValue();
                                if (deltaProfit > 0.0d) {
                                
                                    mCredit = deltaProfit;
                                }
                            }
                        }
                    } else {

                        debtCTextField.setText("0");
                        showDebtsBlockCheckBox.setSelected(false);
                        mShowDebtsBlock = false;
                    }
                } else {
                    
                    //
                    mNoProfitFieldChange = false;
                    
                    debtCTextField.setText("0");
                    showDebtsBlockCheckBox.setSelected(false);
                    mShowDebtsBlock = false;
                }
            }
        });
    }    

    @Override
    public void setScreenParent(ScreensController screenParent) {
        myController = screenParent;
    }
    
    @FXML
    private void goToSalesScreen(ActionEvent event){
        
        goToSalesScreen();
    }
    
    private void goToSalesScreen(){
        
        myController.setScreen(WS1.salesID);
        WS1.primaryStage.setMaximized(true);
        //Если был режим редактирования, то выключаем его флаг,
        //сбрасываем выбранную пару авто - водитель
        if (isEditMode()) {
            
            setEditMode(false);
            resetCarDriver();
            resetForm();
        }
    }
    
    @FXML
    private void resetForm(ActionEvent event){
       resetCarDriver();
       resetForm();
    }
    
    @FXML
    private void setDateNow(ActionEvent event){
    
        //Элементу "выбор даты" устанавливаем текущую дату
        createdDatePicker.setValue(LocalDate.now());
    }
    
    //Действие погашения выбранного долга на указанную в поле сумму
    @FXML
    private void amortDebt(ActionEvent event){
    
        //Если произошел клик по кнопке погашения, значит
        //блок долгов отображается, и должен быть выбран соотв-й чекбокс,
        //тогда выполняем эти условия принудительно в случае,
        //если они не выполнены:
        if (!showDebtsBlockCheckBox.isSelected()) {
            
            showDebtsBlockCheckBox.setSelected(true);
            mShowDebtsBlock = true;
        }
        //Выполняем погашение долга, толко если блок долгов отображается,
        //а также есть ИД только что созданной доставки
        if (mShowDebtsBlock && mTmpLastSaleId > 0) {
            
            //Если долг выбран, то засчитываем сумму погашения, если нет -
            //считаем ее нулевой (не засчитываем).
            //Выше есть проверка, выводящая соотв-е предупреждение перед сохр-м

            Double debtAmortDouble = 0.0;

            //Получаем объект долга, выбранного для погашения
            mSelectedExistingDebt =
                (DebtChange) debtsComboBox.getSelectionModel().getSelectedItem();

            //Если долг выбран, считываем из поля ввода сумму погашения
            if (mSelectedExistingDebt != null) {

                debtAmortDouble =
                    Double.parseDouble(
                        debtAmortCTextField.getText().replaceAll(",", ".")
                    );
                //Если сумма погашения больше суммы баланса выбранного для погашения долга -
                //показываем сообщение об ошибке и выходим из обработчика события
                if (debtAmortDouble > mSelectedExistingDebt.getBalance().doubleValue()) {

                    Alert warningAlert =
                        new Alert(Alert.AlertType.WARNING);
                    warningAlert.setTitle("Предупреждение");
                    warningAlert.setHeaderText("Предотвращена попытка погасить долг на сумму, бОльшую, чем его баланс");
                    warningAlert.setContentText("Выделите сумму погашения, меньшую, чем сумма погашаемого долга");
                    warningAlert.showAndWait();

                    return;
                }
                //если не выбран - показываем предупреждение и выходим из обработчкика
            } else {
            
                Alert warningAlert =
                    new Alert(Alert.AlertType.WARNING);
                warningAlert.setTitle("Предупреждение");
                warningAlert.setHeaderText("Не выбран ни один долг для погашения");
                warningAlert.setContentText("Выберите из выпадающего списка долг для погашения и введите рядом сумму погашения");
                warningAlert.showAndWait();

                return;
            }
            
            //Получаем из БД объект-сущность только что добавленной продажи
            Sale createdSale = mSalesDAOImpl.getSale(mTmpLastSaleId);
            
            //Проверяем, первое ли это нажатие кнопки погашения
            if (!mSaleDebtAdded) {
                
                //В запись продажи сохраняется разность сумм добавляющегося
                //и возвращаемого долгов
                createdSale.setDebt(
                    BigDecimal.valueOf(Double.parseDouble(debtCTextField.getText().replaceAll(",", "."))
                        - debtAmortDouble)
                );
                mSalesDAOImpl.updateSale(createdSale);

                /* Суммируем новое значение долга с балансом долга выбранного магазина */

                //В запись магазина add разность сумм добавляющегося
                //и возвращаемого долгов
                BigDecimal shopDebt = mSelectedShop.getDebt();
                shopDebt =
                    shopDebt.add(
                        BigDecimal.valueOf(Double.parseDouble(debtCTextField.getText().replaceAll(",", "."))
                            - debtAmortDouble)
                    );
                mSelectedShop.setDebt(shopDebt);
                mShopsDAOImpl.updateShop(mSelectedShop);

                //Если есть ненулевой новый долг - добавляем запись о нем в таблицу изменений долгов
                if (!debtCTextField.getText().equals("0")) {

                    DebtChange newDebt = new DebtChange();
                    newDebt.setShopId(mSelectedShop.getId());
                    newDebt.setIsDebt(true);
                    newDebt.setValue(
                        BigDecimal.valueOf(
                            Double.parseDouble(debtCTextField.getText().replaceAll(",", "."))
                        )
                    );
                    newDebt.setDate(Date.from((createdDatePicker
                        .getValue()
                        .atStartOfDay()
                        .atZone(ZoneId.systemDefault())).toInstant())
                    );
                    newDebt.setDebtId(-1);
                    newDebt.setBalance(
                        BigDecimal.valueOf(
                            Double.parseDouble(debtCTextField.getText().replaceAll(",", "."))
                        )
                    );
                    newDebt.setSaleId(mTmpLastSaleId);
                    //Если это долг, не требующий возврата
                    if (notRequireAmortCheckBox.isSelected()) {
                        
                        newDebt.setNotReqAmort(true);
                    }
                    mDebtChangesDAOImpl.createDebtChange(newDebt);
                }
                //Устанавливаем флаг "новый долг добавлен"
                mSaleDebtAdded = true;
            } else {
            
                //В запись продажи сохраняется разность сумм
                //ранее рассчитанной разности нового долга и возвращаемых долгов
                //и дополнительно возвращаемого долга
                createdSale.setDebt(
                    BigDecimal.valueOf(createdSale.getDebt().doubleValue()
                        - debtAmortDouble)
                );
                mSalesDAOImpl.updateSale(createdSale);
                
                /* Вычитаем новое погашение долга из баланса долга выбранного магазина */

                //В запись магазина сохраняется разность сумм баланса
                //и возвращаемого долга
                BigDecimal shopDebt = mSelectedShop.getDebt();
                shopDebt =
                    shopDebt.add(
                        BigDecimal.valueOf( - debtAmortDouble)
                    );
                mSelectedShop.setDebt(shopDebt);
                mShopsDAOImpl.updateShop(mSelectedShop);
            }

            //Если есть ненулевое погашение (amortization) долга -
            //добавляем запись о нем в таблицу изменений долгов
            if (!debtAmortCTextField.getText().equals("0")
                    && !debtAmortCTextField.getText().equals("0.0")
                    && !debtAmortCTextField.getText().equals("0.00")
                    && !debtAmortCTextField.getText().equals("0,0")
                    && !debtAmortCTextField.getText().equals("0,00")
                    && !debtAmortCTextField.getText().equals("")) {

                if (mSelectedExistingDebt != null) {

                    DebtChange newDebtAmort = new DebtChange();
                    newDebtAmort.setShopId(mSelectedShop.getId());
                    //'это не долг (это - запись погашения)'
                    newDebtAmort.setIsDebt(false);
                    newDebtAmort.setValue(
                        BigDecimal.valueOf(debtAmortDouble)
                    );
                    newDebtAmort.setDate(Date.from((createdDatePicker
                        .getValue()
                        .atStartOfDay()
                        .atZone(ZoneId.systemDefault())).toInstant())
                    );
                    newDebtAmort.setDebtId(mSelectedExistingDebt.getId());
                    //Отрицательный баланс - признак отсутствия баланса, т.к. это не
                    //долг, а погашение
                    newDebtAmort.setBalance(
                        BigDecimal.valueOf(
                            -1.0
                        )
                    );
                    newDebtAmort.setSaleId(mTmpLastSaleId);
                    
                    /*//Добавляем в БД новую запись погашения
                    mDebtChangesDAOImpl.createDebtChange(newDebtAmort);
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(AddSaleController.class.getName())
                            .log(Level.SEVERE, null, ex);
                    }
                    //Изменяем в БД запись долга, который погашался
                    mSelectedExistingDebt.setBalance(
                        mSelectedExistingDebt.getBalance().add(BigDecimal.valueOf(-debtAmortDouble))
                    );
                    mDebtChangesDAOImpl.updateDebtChange(mSelectedExistingDebt);*/
                    
                    //Транзакционное погашение долга (создание записи о погашении
                    // и изменение баланса погашаемого долга)
                    int transResult = mDebtChangesDAOImpl.doAmortDebtChange(
                        mSelectedExistingDebt
                            , newDebtAmort);
                    
                    if (transResult == -1) {
                        
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText("Выбранный долг не погашен");
                        alert.setContentText("Сбой в системе не позволил выполнить транзакцию погашения долга");
                        alert.showAndWait();
                    }
                }
            }
            //Полям ввода "долг" устанавливаем значение по умолчанию 0
            debtCTextField.setText("0");
            debtAmortCTextField.setText("0");
            //Обновляем информацию о наличных долгах
            mShopActiveDebts.clear();
            mDebtChangesObservableList.clear();
            //Пытаемся получить список долгов по ИД магазина
            mShopActiveDebts =
                mDebtChangesDAOImpl.getFilteredDebtChanges(
                    mSelectedShop.getId()
                    , true
                );

            for (DebtChange debtChange : mShopActiveDebts) {

                //Если долг - требующий погашения, то
                //добавляем его в коллекцию для комбо-бокса
                if (!debtChange.isNotReqAmort()
                        && !debtChange.getIsCredit()) {
                    
                    mDebtChangesObservableList.add(debtChange);
                }
            }

            if (mDebtChangesObservableList.size() > 0) {

                debtsComboBox.setItems(mDebtChangesObservableList);
            }
        }
        //Вызываем обновление данных в коллекции-источнике для
        //таблицы магазинов
        WS1.shopsControllerInstance.updateShopsForPage();
        //
        WS1.salesControllerInstance.updateShops();
        WS1.salesControllerInstance.updateSalesForPage();
        //
        //WS1.addSaleControllerInstance.updateShops();
        //WS1.addSaleControllerInstance.updateDebts();
        //
        WS1.debtsControllerInstance.updateShops();
        WS1.debtsControllerInstance.updateDebtChangesForPage();
    }
    
    //Действие завершения работы с блоком долгов
    @FXML
    private void finishDebtChanges(ActionEvent event){
        
        volumeCTextField.setEditable(true);
    
        //Выполняем сброс и скрытие блока долгов, толко если этот блок отображается
        if (mShowDebtsBlock) {
            
            if (!mSaleDebtAdded) {
                
                //Получаем из БД объект-сущность только что добавленной продажи
                Sale createdSale = mSalesDAOImpl.getSale(mTmpLastSaleId);
                //В запись продажи повторно сохраняется сумма полученной выручки,
                //если она не равна сумме должной оплаты
                createdSale.setProfit(
                    BigDecimal.valueOf(
                        mMustPay.doubleValue()
                        - Double.parseDouble(debtCTextField.getText().replaceAll(",", "."))
                    )
                );
                //В запись продажи сохраняется сумма добавляющегося долга
                createdSale.setDebt(
                    BigDecimal.valueOf(
                        Double.parseDouble(debtCTextField.getText().replaceAll(",", "."))
                    )
                );
                mSalesDAOImpl.updateSale(createdSale);

                /* Суммируем новое значение долга с балансом долга выбранного магазина */
                if (!notRequireAmortCheckBox.isSelected()) {
                    
                    //В запись магазина сохраняется сумма добавляющегося долга
                    BigDecimal shopDebt = mSelectedShop.getDebt();
                    shopDebt =
                        shopDebt.add(
                            BigDecimal.valueOf(Double.parseDouble(debtCTextField.getText().replaceAll(",", ".")))
                        );
                    mSelectedShop.setDebt(shopDebt);
                    mShopsDAOImpl.updateShop(mSelectedShop);
                }

                //Если есть ненулевой новый долг - добавляем запись о нем в таблицу изменений долгов
                if (!debtCTextField.getText().equals("0")
                        && !debtCTextField.getText().equals("0.0")
                        && !debtCTextField.getText().equals("0.00")
                        && !debtCTextField.getText().equals("0,0")
                        && !debtCTextField.getText().equals("0,00")
                        && !debtCTextField.getText().equals("")) {

                    DebtChange newDebt = new DebtChange();
                    newDebt.setShopId(mSelectedShop.getId());
                    newDebt.setIsDebt(true);
                    newDebt.setValue(
                        BigDecimal.valueOf(
                            Double.parseDouble(debtCTextField.getText().replaceAll(",", "."))
                        )
                    );
                    newDebt.setDate(Date.from((createdDatePicker
                        .getValue()
                        .atStartOfDay()
                        .atZone(ZoneId.systemDefault())).toInstant())
                    );
                    newDebt.setDebtId(-1);
                    newDebt.setBalance(
                        BigDecimal.valueOf(
                            Double.parseDouble(debtCTextField.getText().replaceAll(",", "."))
                        )
                    );
                    newDebt.setSaleId(mTmpLastSaleId);
                    //Если это долг, не требующий возврата
                    if (notRequireAmortCheckBox.isSelected()) {
                        
                        newDebt.setNotReqAmort(true);
                    }
                    mDebtChangesDAOImpl.createDebtChange(newDebt);
                }
                //Вызываем обновление данных в коллекции-источнике для
                //таблицы магазинов
                WS1.shopsControllerInstance.updateShopsForPage();
                //
                WS1.salesControllerInstance.updateShops();
                WS1.salesControllerInstance.updateSalesForPage();
                //
                //WS1.addSaleControllerInstance.updateShops();
                //WS1.addSaleControllerInstance.updateDebts();
                //
                WS1.debtsControllerInstance.updateShops();
                WS1.debtsControllerInstance.updateDebtChangesForPage();
                //Устанавливаем флаг "новый долг добавлен"
                //mSaleDebtAdded = true;
            }
            
            mShowDebtsBlock = false;
            mSaleDebtAdded = false;
            //Действия после окончания процесса сохранения информации о доставке, ее долге и погашениях
            doPostSaleSaving();
        }
    }
    
//    public void shopCTextFieldChanged(InputMethodEvent event){
//        System.out.println("event");
//        if (!shopCTextField.textProperty().getValue().equals("") && mShopNamesSet.contains(shopCTextField.textProperty().getValue())) {
//            System.out.println("shops");
//            for (Shop shop : mShops) {
//                System.out.println("shop");
//                if (shop.getName().equals(shopCTextField.textProperty().getValue())) {
//                    System.out.println("selected shop");
//                    mSelectedShop = shop;
//                }
//            }
//            mBarrels = mBarrelsDAOImpl.getAllBarrels();
//            for (Barrel barrel : mBarrels) {
//                System.out.println(barrel.getCapacityId());
//            }
//        }
//    }
    @FXML
    private void actionAddSale(ActionEvent actionEvent) {
        
        volumeCTextField.setEditable(false);
                
        List<ValidationMessage> validationMessageList =
                (List<ValidationMessage>) validationSupport
                        .getValidationResult()
                        .getMessages();
        if (validationMessageList.isEmpty()) {
            
            //Если магазина с введенным названием нет в БД,
            //показываем окно ошибки
            String errorsString = "";
            boolean hasErrors = false;
            if (!mShopNamesSet.contains(shopCTextField.textProperty().getValue())) {
                errorsString += "Нет магазина с таким названием. ";
                hasErrors = true;
            }
            if (mSelectedBarrel == null) {
                errorsString += "Не выбрана бочка. ";
                hasErrors = true;
            }
            if (!mDriverNamesSet.contains(driverCTextField.textProperty().getValue())) {
                errorsString += "Нет водителя с таким именем. ";
                hasErrors = true;
            }
            if (!mCarNamesSet.contains(carCTextField.textProperty().getValue())) {
                errorsString += "Нет автомобиля с таким номером. ";
                hasErrors = true;
            }
            
            if (createdDatePicker.getValue() == null) {
                
                errorsString += "Не установлена дата. ";
                hasErrors = true;
            }
            
            if (hasErrors) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText(
                    !mEditMode
                        ?"Доставка не добавлена"
                        :"Доставка не изменена"
                );
                alert.setContentText(errorsString);
                alert.showAndWait();
            } else {
                
                /*проверки на подозрительные данные о счетчике и объеме*/
                String warningString = "";
                boolean hasWarnings = false;
                //Если сейчас режим добавления доставки
                if(!mEditMode){

                    //по расзности счетчика с прошлым значением: израсходовано больше,
                    //чем емкость бочки (проверять всегда, а если предыдущая продажа
                    //не найдена - пропускать)

                    //находим ID предыдущ. дост. для данной бочки
                    Integer lastSaleId = mSelectedBarrel.getLastSaleId();
                    //емкость выбранной бочки
                    int selectedBarrelCapacityInt =
                        mBarrelCapacitiesDAOImpl.getBarrelCapacity(
                                mSelectedBarrel.getCapacityId()
                        ).getCapacity();
                    //максимальная емкость, доступная сейчас
                    int maxCurrentCapacity = selectedBarrelCapacityInt;
                    //если существует информация о предыдущей доставке для бочки
                    /*if (lastSaleId != null) {
                        Sale lastSale =
                            mSalesDAOImpl.getSale(lastSaleId);
                        if (lastSale != null) {
                            int diff = Integer.parseInt(
                                    countOldCTextField.getText()
                            ) - lastSale.getCounterNew();

                            //корректируем максимальную емкость, доступную сейчас
                            maxCurrentCapacity =
                                    maxCurrentCapacity -
                                    (selectedBarrelCapacityInt - diff);

                            diff = diff - (int)((double)diff * 0.02D);
                            if (diff > selectedBarrelCapacityInt) {
                                warningString +=
                                        "Израсходовано больше, чем емкость бочки. ";
                                hasWarnings = true;
                            }
                        }
                    }*/


                    if (lastSaleId != null) {
                        /*Sale lastSale =
                            mSalesDAOImpl.getSale(lastSaleId);*/
                        //if (lastSale != null) {
                            int diff = Integer.parseInt(
                                    countNewCTextField.getText()
                            ) - mSelectedBarrel.getCounter();

                            int diffTest = diff - (int)((double)diff * 0.02D);
                            if (diffTest > selectedBarrelCapacityInt) {
                                warningString +=
                                        "Израсходовано больше, чем емкость бочки. ";
                                hasWarnings = true;
                            }

                            //корректируем максимальную емкость, доступную сейчас
                            maxCurrentCapacity =
                                maxCurrentCapacity -
                                (selectedBarrelCapacityInt - diff);

                        //}
                    }


                    //заправлено больше, чем емкость бочки [+ остаток]

                    //если чистка или замена произведены,
                    //то доступная полная емкость бочки
                    if (cleanCheckBox.isSelected() || repairCheckBox.isSelected()) {
                        maxCurrentCapacity = selectedBarrelCapacityInt;
                    }

                    if (Integer.parseInt(volumeCTextField.getText()) > maxCurrentCapacity) {

                        warningString +=
                                "Заправлено больше, чем доступная емкость бочки. ";
                        hasWarnings = true;
                    }

                    //
                    if (!debtAmortCTextField.getText().equals("0")
                            && debtsComboBox.getSelectionModel().getSelectedItem() == null) {

                        warningString +=
                                "В поле погашения долга ненулевое значение, но погашаемый долг не выбран (ПОГАШЕНИЕ НЕ БУДЕТ ЗАСЧИТАНО!). ";
                        hasWarnings = true;
                    }
                }
                //***
                
                //Были подготовлены предупреждения - показываем их,
                //и спрашиваем: добавлять доставку или нет?
                if (hasWarnings) {
                    
                    Alert warningConfirmationAlert =
                        new Alert(Alert.AlertType.CONFIRMATION);
                    warningConfirmationAlert.setTitle("Подозрительные значения");
                    warningConfirmationAlert.setHeaderText("Все равно добавить доставку?");
                    warningConfirmationAlert.setContentText(warningString);

                    Optional<ButtonType> result =
                            warningConfirmationAlert.showAndWait();
                    if (result.get() == ButtonType.OK){

                        //Нажата кнопка OK - сохраняем доставку в БД
                        processSaleSaving();
                    } else {
                        //Нажата кнопка CANCEL - показываем сообщение об отмене
                        Alert cancelWarningAlert =
                                new Alert(Alert.AlertType.WARNING);
                        cancelWarningAlert.setTitle("Предупреждение");
                        cancelWarningAlert.setHeaderText(
                            !mEditMode
                                ?"Доставка не добавлена"
                                :"Доставка не изменена"
                        );
                        cancelWarningAlert.setContentText("Добавление доставки отменено");
                        cancelWarningAlert.showAndWait();
                    }
                } else {
                    
                    //Не было ни ошибок, ни предупреждений -
                    //сохраняем доставку в БД
                    processSaleSaving();
                }

                
                
            }
//            securityNameCTextField.promptTextProperty()
//                                .setValue("security name");
//            quantityCTextField.promptTextProperty()
//                                .setValue("quantity");
//            priceCTextField.promptTextProperty()
//                                .setValue("price");
//            securityNameCTextField.setStyle("-fx-border-color:lightgray;");
//            quantityCTextField.setStyle("-fx-border-color:lightgray;");
//            priceCTextField.setStyle("-fx-border-color:lightgray;");
            
            //System.out.println(securityNameCTextField.getText());
            
            //Добавление записи в таблицу "Продажи"
//            Sale newSale = new Sale();
//            newSale.setPrice((int)(Double.parseDouble(priceCTextField.getText()) * 100));
//            newSale.setQuantity(Integer.parseInt(quantityCTextField.getText()));
//            newSale.setSecurityName(securityNameCTextField.getText());
//            remoteServiceRemote.addSale(newSale, 1, 1);
            
//            securityNameCTextField.textProperty().setValue("");
//            quantityCTextField.textProperty().setValue("");
//            priceCTextField.textProperty().setValue("");
            
        } else {
            
            //Если хотя бы одно из валидируемых полей не заполнено,
            //показываем окно ошибок валидации
            
            String errorsString = "";
            
            for (ValidationMessage validationMessage : validationMessageList) {
                
                //System.out.println(validationMessage.getText());
                errorsString += "поле \""
                        + ((CustomTextField)validationMessage.getTarget()).getPromptText()
                        + "\": "
                        + validationMessage.getText()
                        + ". ";
            }
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(
                !mEditMode
                    ?"Доставка не добавлена"
                    :"Доставка не изменена"
            );
            alert.setContentText(errorsString);
            alert.showAndWait();
                        
//            String currentControlIdString;
//            for (ValidationMessage validationMessage : validationMessageList) {
//                currentControlIdString =
//                        ((CustomTextField)validationMessage.getTarget()).getId();
//                switch(currentControlIdString){
//                    case "securityNameCTextField" : {
//                        securityNameCTextField.promptTextProperty()
//                                .setValue(validationMessage.getText());
//                        //securityNameCTextField.getStyleClass().remove("custom-text-field");
//                        //securityNameCTextField.getStyleClass().add("error-c-text-field");
//                        securityNameCTextField.setStyle("-fx-border-color:red;");
//                        break;
//                    }
//                    case "quantityCTextField" : {
//                        quantityCTextField.promptTextProperty()
//                                .setValue(validationMessage.getText());
//                        quantityCTextField.setStyle("-fx-border-color:red;");
//                        break;
//                    }
//                    case "priceCTextField" : {
//                        priceCTextField.promptTextProperty()
//                                .setValue(validationMessage.getText());
//                        priceCTextField.setStyle("-fx-border-color:red;");
//                        break;
//                    }
//                }
                
                //System.out.println(((CustomTextField)validationMessage.getTarget()).getId());
                
            //}
        }
    }
    
    //Приведение формы в исходное состояние
    private void resetForm(){
        
        //очистка элементов ввода
        shopCTextField.clear();
//        driverCTextField.clear();
//        carCTextField.clear();
        countOldCTextField.clear();
        countNewCTextField.clear();
        volumeCTextField.clear();
        cleanCheckBox.setSelected(false);
        repairCheckBox.setSelected(false);
        //Стираем значение поля реальной оплаты
        profitCTextField.clear();
        //Создаем новый объект должной оплаты = 0
        mMustPay = new BigDecimal(0);
        
        //Бочка сброшена, по умолчанию разрядность водомера 6 знаков
        positionLabel.setText("не более 6 знаков");
        
        toPayLabel.setText("");
        
        //Скрывем дополнительное поле счетчика и отключаем его редактируемость
        countNewAfterHBox.visibleProperty().setValue(false);
        countNewAfterCTextField.setEditable(false);
        //countNewAfterHBox.setFocusTraversable(false);
        
        //Полям ввода "долг" устанавливаем значение по умолчанию 0
        debtCTextField.setText("0");
        debtAmortCTextField.setText("0");
        
        notRequireAmortCheckBox.setSelected(false);
        //Элементу "выбор даты" устанавливаем текущую дату
        //createdDatePicker.setValue(LocalDate.now());
        noticeCTextField.clear();
        
        //Подключение автодополнения к полям с наборами вариантов выбора
        mShopNamesSet.clear();
        for (Shop shop : mShops) {
            
            mShopNamesSet.add(shop.getName());
        }
        //отписываемся от предыдущего набора автодополнения
        if (mShopAutoCompletionBinding != null) {
            
            mShopAutoCompletionBinding.dispose();
        }
        //подписываемся на новый набор автодополнения
        mShopAutoCompletionBinding = TextFields.bindAutoCompletion(shopCTextField, mShopNamesSet);
        
        mDriverNamesSet.clear();
        for (Driver driver : mDrivers) {
            
            mDriverNamesSet.add(driver.getName());
        }
        if (mDriverAutoCompletionBinding != null) {
            
            mDriverAutoCompletionBinding.dispose();
        }
        mDriverAutoCompletionBinding = TextFields.bindAutoCompletion(driverCTextField, mDriverNamesSet);
        
        mCarNamesSet.clear();
        for (Car car : mCars) {
            
            mCarNamesSet.add(String.valueOf(car.getNumber()));
        }
        if (mCarAutoCompletionBinding != null) {
            
            mCarAutoCompletionBinding.dispose();
        }
        mCarAutoCompletionBinding = TextFields.bindAutoCompletion(carCTextField, mCarNamesSet);
        
        //Сбрасываем чекбокс включения блока работы с долгами
        showDebtsBlockCheckBox.setSelected(false);
        
        //Скрываем блок работы с долгами
        newDebtHBox.setVisible(false);
        amortDebtsHBox.setVisible(false);
        finishEditDebtsHBox.setVisible(false);
        
        //newDebtHBox.setFocusTraversable(false);
        //amortDebtsHBox.setFocusTraversable(false);
        //finishEditDebtsHBox.setFocusTraversable(false);
        
        debtCTextField.setEditable(false);
        debtAmortCTextField.setEditable(false);
        
        //Скрываем основные кнопки формы
        //mainButtonsHBox.setFocusTraversable(true);

        addSaleButton.setDisable(false);
        resetFormButton.setDisable(false);
        backButton.setDisable(false);
    }
    
    private void resetCarDriver(){
        
        driverCTextField.clear();
        carCTextField.clear();
    }
    
    //Обновление списка магазинов (вызывается извне,
    //если магазин был добавлен или деактивирован)
    public void updateShops(){
        
        //mShops = mShopsDAOImpl.getAllShops();
        mShops = mShopsDAOImpl.getFilteredShops(mActive, -1, -1);
        /*for (Shop mShop : mShops) {
            
            System.out.println(mShop);
        }*/
        resetForm();
    }
    public void updateBarrels(){
        
        //mBarrels = mBarrelsDAOImpl.getAllBarrels();
        mBarrels = mBarrelsDAOImpl.getFilteredBarels(
                -1
                //, mFilterBarrelId
                //, -1
                , null
                , mActive
                , -1
                , -1
            );
        resetForm();
    }
    public void updateDrivers(){
        
        //mDrivers = mDriversDAOImpl.getAllDrivers();
        mDrivers = mDriversDAOImpl.getFilteredDrivers(mActive, -1 , -1);
        resetForm();
        resetCarDriver();
    }
    public void updateCars(){
        
        //mCars = mCarsDAOImpl.getAllCars();
        mCars = mCarsDAOImpl.getFilteredCars(mActive, -1, -1);
        resetForm();
    }
    //TODO вызов к фасаду: получить обновленный список бочек
    
    public void updateDebts(){
        
        //Пытаемся получить список долгов по ИД магазина
        if (mSelectedShop != null) {
            
            mDebtChangesObservableList.clear();
         
            mShopActiveDebts =
            mDebtChangesDAOImpl.getFilteredDebtChanges(
                mSelectedShop.getId()
                , true
            );
            
            for (DebtChange debtChange : mShopActiveDebts) {

                if (!debtChange.isNotReqAmort()
                        && !debtChange.getIsCredit()) {
                    
                    mDebtChangesObservableList.add(debtChange);
                }
            }

            if (mDebtChangesObservableList.size() > 0) {

                //Подключаем наблюдабельную коллекцию активных долгов
                //в качестве источника для пунктов списка комбобокса долгов,
                //предлагаемых к погашению
                debtsComboBox.setItems(mDebtChangesObservableList);
            }
        }
    }
    
    private void processSaleSaving(){
        
        /*Устанавливаем объекты выбранного водителя и выбранного авто*/
        for (Driver driver : mDrivers) {
            if (driver.getName().equals(driverCTextField.getText())) {
                mSelectedDriver = driver;
            }
        }
        for (Car car : mCars) {
            if (String.valueOf(car.getNumber()).equals(carCTextField.getText())) {
                mSelectedCar = car;
            }
        }

        //Если пользователь удалил текст из поля ввода "долг",
        //устанавливаем значение для записи в БД: 0
        /*String debtString = !debtCTextField.getText().equals("")
                ? debtCTextField.getText()
                : "0";*/

        //TODO добавить предупреждение, если показания счетчика
        //или сумма опалты - подозрительные
        //TODO во время выполнения запроса отображать метку "Загрузка..."
        //TODO для замены бочки учитывать слив через счетчик
        //TODO добавлять второе значение счетчика в таблицу "Бочки"

        /*System.out.println(mSelectedShop.getId());
        System.out.println(mSelectedBarrel.getId());
        System.out.println(mSelectedDriver.getId());
        System.out.println(mSelectedCar.getId());
        System.out.println(countOldCTextField.getText());
        System.out.println(countNewCTextField.getText());
        System.out.println(volumeCTextField.getText());
        System.out.println(cleanCheckBox.isSelected());
        System.out.println(repairCheckBox.isSelected());
        System.out.println(profitCTextField.getText());
        System.out.println(debtCTextField.getText());
        System.out.println(createdDatePicker.getValue());
        System.out.println(noticeCTextField.getText());*/

        //Создаем ненастроенный объект-сущность доставки
        //
        Date oldDate = null;
        Sale newSale = null;
        if(mEditMode){
        
            newSale = mEditingSale;
            oldDate = mEditingSale.getCreatedAt();
        } else {
        
            newSale = new Sale();
        }
        //Настраиваем его всеми полученными и вычисленными данными
        newSale.setShopId(mSelectedShop.getId());
        newSale.setBarrelId(mSelectedBarrel.getId());
        newSale.setDriverId(mSelectedDriver.getId());
        newSale.setCarId(mSelectedCar.getId());
        newSale.setCounterOld(
                Integer.parseInt(countOldCTextField.getText())
        );
        newSale.setCounterNew(
            Integer.parseInt(countNewCTextField.getText())
        );
        newSale.setVolume(
            Integer.parseInt(volumeCTextField.getText())
        );
        newSale.setCleaning(cleanCheckBox.isSelected());
        newSale.setRepair(repairCheckBox.isSelected());
        
        newSale.setToPay(mMustPay);
        newSale.setProfit(
            BigDecimal.valueOf(Double.parseDouble(profitCTextField.getText().replaceAll(",", ".")))
        );
                
        //В запись продажи на данном этапе сохраняется
        //нулевая сумма долга (она может быть изменена на этапе работы с долгами)
        if(!mEditMode){
        
            newSale.setDebt(BigDecimal.ZERO);
        }
        
        newSale.setCreatedAt(Date.from((createdDatePicker
            .getValue()
            .atStartOfDay()
            .atZone(ZoneId.systemDefault())).toInstant())
        );
        newSale.setUpdatedAt(Date.from((createdDatePicker
            .getValue()
            .atStartOfDay()
            .atZone(ZoneId.systemDefault())).toInstant())
        );
        newSale.setNotice(noticeCTextField.getText());
        //В БД добавляется новая запись, а соответствующий JPAController
        //заносит ИД этой записи в глобальный объект - для дальнейшего использования
        if(mEditMode){
        
            mSalesDAOImpl.updateSale(newSale);
            
            int newSaleBarrelNewPeriod =
                WS1.barrelsControllerInstance.callCalcPeriod(mSelectedBarrel, newSale.getCreatedAt());
            
            System.out.println("newSaleBarrelNewPeriod" + newSaleBarrelNewPeriod);
            
            mTmpLastSaleId = newSale.getId();
            //Если у доставки были изменения долга,
            //и старая дата доставки отличается от новой,
            //то во всех изменениях долга доставки
            //меняем дату на новую
            List<DebtChange> saleDayDebtChangeList =
                mDebtsDAOImpl.getFilteredDebts(
                    -1
                    , -1
                    , oldDate
                    , -1
                    , -1
                    , -1
                    , -1
                );
            saleDayDebtChangeList =
                saleDayDebtChangeList.stream()
                    .filter(debt ->
                        ((DebtChange)debt).getSaleId() == mEditingSale.getId())
                    .collect(Collectors.toList());
            //
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            //
            if (saleDayDebtChangeList != null
                    &&  oldDate != null
                    && saleDayDebtChangeList.size() > 0
                    && !simpleDateFormat.format(oldDate).equals(simpleDateFormat.format(newSale.getCreatedAt()))) {
                
                //
                for (DebtChange saleDayDebtChange : saleDayDebtChangeList) {
                    
                    saleDayDebtChange.setDate(newSale.getCreatedAt());
                    mDebtChangesDAOImpl.updateDebtChange(saleDayDebtChange);
                }
            }
        } else {
        
            mSalesDAOImpl.createSale(newSale);
            
            int newSaleBarrelNewPeriod =
                WS1.barrelsControllerInstance.callCalcPeriod(mSelectedBarrel, newSale.getCreatedAt());
            
            System.out.println("newSaleBarrelNewPeriod" + newSaleBarrelNewPeriod);
            
            //Получаем ID только что сохраненной доставки
            mTmpLastSaleId = Globals.getLastSaleId();
        }

        //if new credit exists
        if(mCredit > 0.0d){
        
            //add new debt change row (credit)
            DebtChange newDebt = new DebtChange();
            newDebt.setShopId(mSelectedShop.getId());
            newDebt.setIsDebt(false);
            newDebt.setValue(BigDecimal.valueOf(mCredit));
            newDebt.setDate(Date.from((createdDatePicker
                .getValue()
                .atStartOfDay()
                .atZone(ZoneId.systemDefault())).toInstant())
            );
            newDebt.setDebtId(-1);
            newDebt.setBalance(BigDecimal.valueOf(mCredit));
            newDebt.setSaleId(mTmpLastSaleId);
            
            newDebt.setNotReqAmort(false);
            
            newDebt.setIsCredit(true);
            
            mDebtChangesDAOImpl.createDebtChange(newDebt);
            
            mCredit = 0.0d;
        }
        
        //если был включен чекбокс "замена",
        //делаем настройку выбранной бочке "недавно заменена"
        if (repairCheckBox.isSelected()) {
            
            mSelectedBarrel.setRecentlyReplaced(true);
        } else {
            //иначе - сбрасываем настройку "недавно заменена"
            // выбранной бочке
            mSelectedBarrel.setRecentlyReplaced(false);
        }
        //если был включен чекбокс "чистка",
        //делаем настройку выбранной бочке:
        //обновляем дату последней чистки у объекта
        if (cleanCheckBox.isSelected()) {
            
            mSelectedBarrel.setLastCDate(Date.from((createdDatePicker
                .getValue()
                .atStartOfDay()
                .atZone(ZoneId.systemDefault())).toInstant())

            );
        }

        //в любом случае указываем выбранной бочке ИД
        //только что сохраненной доставки
        mSelectedBarrel.setLastSaleId(mTmpLastSaleId);
        //а также сохраняем последнее значение счетчика
        mSelectedBarrel.setCounter(
            Integer.parseInt(countNewAfterCTextField.getText())
        );

        mBarrelsDAOImpl.updateBarrel(mSelectedBarrel);
        
        /*Update credits*/
        if (mShopActiveCredits != null
            && mShopActiveCredits.size() > 0) {

            for (DebtChange creditDebtChange : mShopActiveCredits) {

                mDebtChangesDAOImpl.updateDebtChange(creditDebtChange);
                System.out.println(creditDebtChange.getBalance());
                                    System.out.println();
            }
            WS1.debtsControllerInstance.updateDebtChangesForPage();
        }

        //если был включен чекбокс "работа с долгами" (проверяем флаг) -
        //показываем блок работы с долгами, иначе - показываем сообщение
        //об успешном добавлении информации о доставке
        //if (showDebtsBlockCheckBox.isSelected()) {
            
            //Когда доставка уже сохранилась в БД, отображаем блок
            //работы с долгами, если был выбран соотв-й чекбокс
        if (mShowDebtsBlock && !mEditMode) {

            newDebtHBox.setVisible(true);
            amortDebtsHBox.setVisible(true);
            finishEditDebtsHBox.setVisible(true);

            //newDebtHBox.setFocusTraversable(true);
            //amortDebtsHBox.setFocusTraversable(true);
            //finishEditDebtsHBox.setFocusTraversable(true);

            debtCTextField.setEditable(true);
            debtAmortCTextField.setEditable(true);
            
            //Скрываем основные кнопки формы
            //mainButtonsHBox.setFocusTraversable(false);
            
            addSaleButton.setDisable(true);
            resetFormButton.setDisable(true);
            backButton.setDisable(true);
            //}
        } else {
        
            doPostSaleSaving();
        }
    }
    
    private void doPostSaleSaving(){
    
        //Сообщение пользователю об успешном добавлении доставки в БД
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(
            !mEditMode
                ?"Доставка успешно добавлена"
                :"Доставка успешно изменена"
        );
        //Показать сообщение и ждать
        alert.showAndWait();
        
        //Приводим форму в исходное состояние
        resetForm();
        //Вызываем обновление данных в коллекции-источнике для
        //таблицы доставок (в представлении доставок)
        WS1.salesControllerInstance.updateSalesForPage();
        //Оповещаем контроллер бочек, что список бочек нужно обновить,
        //т.к. добавление продажи для некоторой бочки меняет ее
        //состояние, в частности - дату последней чистки и факт замены
        WS1.barrelsControllerInstance.updateBarrelsForPage();
        //Вызываем обновление данных в коллекции-источнике для
        //таблицы магазинов (в представлении магазинов)
        WS1.shopsControllerInstance.updateShopsForPage();
    }
    
    public boolean isEditMode()
    {
        return mEditMode;
    }

    public void setEditMode(boolean _editMode)
    {
        mEditMode = _editMode;
        //для режима редактирования
        if (mEditMode) {
            
            captionLabel.setText("Изменить доставку");
            addSaleButton.setText("Изменить");
            debtsTogglerHBox.setVisible(false);
            countOldCTextField.setEditable(true);
            countOldCTextField.setFocusTraversable(true);
            
            //
            createdDatePicker.setValue(
                mEditingSale.getCreatedAt()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            );
            carCTextField.setText(
                String.valueOf(
                    mCarsDAOImpl
                        .getCar(mEditingSale.getCarId())
                        .getNumber()
                )
            );
            driverCTextField.setText(
                String.valueOf(
                    mDriversDAOImpl
                        .getDriver(mEditingSale.getDriverId())
                        .getName()
                )
            );
            mSelectedShop = mShopsDAOImpl.getShop(mEditingSale.getShopId());
            shopCTextField.setText(
                String.valueOf(mSelectedShop.getName())
            );
            mSelectedBarrel =
                mBarrelsDAOImpl.getBarrel(mEditingSale.getBarrelId());
            if (!Objects.equals(mEditingSale.getId(), mSelectedBarrel.getLastSaleId())) {
                
                mAbortState = true;
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Доставка не изменена");
                alert.setContentText("Отмена попытки изменить не самую последнюю доставку для данной бочки. Данное действие могло бы привести базу данных в некорректное состояние");
                alert.showAndWait();
                //goToSalesScreen();
            }
            barrelComboBox.getSelectionModel().select(mSelectedBarrel);
            cleanCheckBox.setSelected(mEditingSale.getCleaning());
            cleanCheckBoxEventHandler();
            repairCheckBox.setSelected(mEditingSale.getRepair());
            repairCheckBoxEventHandler();
            countOldCTextField.setText(
                String.valueOf(mEditingSale.getCounterOld())
            );
            /*countNewCTextField.setText(
                String.valueOf(mEditingSale.getCounterNew())
            );
            countNewAfterCTextField.setText(
                String.valueOf(mEditingSale.getCounterNew())
            );*/
        //для режима добавления
        } else {
        
            captionLabel.setText("Добавить доставку");
            addSaleButton.setText("Добавить");
            //показываем переключатель видимости блока работы с долгами
            debtsTogglerHBox.setVisible(true);
            countOldCTextField.setEditable(false);
            countOldCTextField.setFocusTraversable(false);
        }
    }

    public void setEditingSale(Sale _editingSale)
    {
        mEditingSale = _editingSale;
    }
    
    private void cleanCheckBoxEventHandler(){
    
        if (cleanCheckBox.isSelected() || repairCheckBox.isSelected()) {
            countNewAfterHBox.visibleProperty().setValue(true);
            countNewAfterCTextField.setEditable(true);
        } else {
            countNewAfterHBox.visibleProperty().setValue(false);
            countNewAfterCTextField.setEditable(false);
        }
    }
    
    private void repairCheckBoxEventHandler(){
    
        if (cleanCheckBox.isSelected() || repairCheckBox.isSelected()) {
            countNewAfterHBox.visibleProperty().setValue(true);
            countNewAfterCTextField.setEditable(true);
        } else {
            countNewAfterHBox.visibleProperty().setValue(false);
            countNewAfterCTextField.setEditable(false);
        }
    }
}
