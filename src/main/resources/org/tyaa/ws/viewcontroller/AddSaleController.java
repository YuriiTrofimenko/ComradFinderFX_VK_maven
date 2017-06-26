/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.ws.viewcontroller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.controlsfx.control.textfield.CustomTextField;

/**
 * FXML Controller class
 *
 * @author Юрий
 */
public class AddSaleController implements Initializable
{

    @FXML
    private Label captionLabel;
    @FXML
    private DatePicker createdDatePicker;
    @FXML
    private CustomTextField carCTextField;
    @FXML
    private CustomTextField driverCTextField;
    @FXML
    private CustomTextField shopCTextField;
    @FXML
    private ComboBox<?> barrelComboBox;
    @FXML
    private CheckBox cleanCheckBox;
    @FXML
    private CheckBox repairCheckBox;
    @FXML
    private CustomTextField countOldCTextField;
    @FXML
    private CustomTextField countNewCTextField;
    @FXML
    private Label positionLabel;
    @FXML
    private HBox countNewAfterHBox;
    @FXML
    private CustomTextField countNewAfterCTextField;
    @FXML
    private CustomTextField volumeCTextField;
    @FXML
    private CustomTextField profitCTextField;
    @FXML
    private Label toPayLabel;
    @FXML
    private CustomTextField noticeCTextField;
    @FXML
    private HBox debtsTogglerHBox;
    @FXML
    private CheckBox showDebtsBlockCheckBox;
    @FXML
    private HBox mainButtonsHBox;
    @FXML
    private Button addSaleButton;
    @FXML
    private Button resetFormButton;
    @FXML
    private Button backButton;
    @FXML
    private HBox newDebtHBox;
    @FXML
    private CustomTextField debtCTextField;
    @FXML
    private CheckBox notRequireAmortCheckBox;
    @FXML
    private HBox amortDebtsHBox;
    @FXML
    private ComboBox<?> debtsComboBox;
    @FXML
    private CustomTextField debtAmortCTextField;
    @FXML
    private HBox finishEditDebtsHBox;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        // TODO
    }    

    @FXML
    private void setDateNow(ActionEvent event)
    {
    }

    @FXML
    private void actionAddSale(ActionEvent event)
    {
    }

    @FXML
    private void resetForm(ActionEvent event)
    {
    }

    @FXML
    private void goToSalesScreen(ActionEvent event)
    {
    }

    @FXML
    private void amortDebt(ActionEvent event)
    {
    }

    @FXML
    private void finishDebtChanges(ActionEvent event)
    {
    }
    
}
