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
 * в графической таблице кандидатов на приглашение в группу
 * 
 * @author Юрий
 */
public class GlobalModel
{
    //private IntegerProperty uid;
    private StringProperty groupId;
    //private StringProperty lname;
    //private IntegerProperty score;
    
    public GlobalModel(
        //int _uid,
        String _groupId
        //String _lname,
        //int _score
    ){
    
        //uid = new SimpleIntegerProperty(_uid);
        groupId = new SimpleStringProperty(_groupId);
        //lname = new SimpleStringProperty(_lname);
        //score = new SimpleIntegerProperty(_score);
    }
    
    /*public Integer getUid() {
        return uid.getValue();
    }

    public IntegerProperty uidProperty() {
        return uid;
    }*/
    
    public String getGroupId() {
        return groupId.getValue();
    }

    public StringProperty groupIdProperty() {
        return groupId;
    }
    
    /*public String getLname() {
        return lname.getValue();
    }

    public StringProperty lnameProperty() {
        return lname;
    }
    
    public int getScore() {
        return score.getValue();
    }

    public IntegerProperty scoreProperty() {
        return score;
    }*/
}
