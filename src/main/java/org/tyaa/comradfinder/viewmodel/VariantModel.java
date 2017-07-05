/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.viewmodel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Модель для заполнения строк
 * в графических таблицах типичных слов
 * 
 * @author Юрий
 */
public class VariantModel
{
    private StringProperty category;
    private StringProperty variant;
    private IntegerProperty quantity;
    
    public VariantModel(
        String _category,
        String _variant,
        int _quantity
    ){
    
        category = new SimpleStringProperty(_category);
        variant = new SimpleStringProperty(_variant);
        quantity = new SimpleIntegerProperty(_quantity);
    }
    
    public String getCategory() {
        return category.getValue();
    }

    public StringProperty categoryProperty() {
        return category;
    }
    
    public String getVariant() {
        return variant.getValue();
    }

    public StringProperty variantProperty() {
        return variant;
    }
    
    public int getQuantity() {
        return quantity.getValue();
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }
}
