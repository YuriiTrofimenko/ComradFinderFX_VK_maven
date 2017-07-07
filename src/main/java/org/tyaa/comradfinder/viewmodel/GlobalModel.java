/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.viewmodel;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.tyaa.comradfinder.model.TypicalWords;

/**
 * Модель для заполнения строк
 * в графической таблице кандидатов на приглашение в группу
 * 
 * @author Юрий
 */
public class GlobalModel
{
    //Идентификатор текущей группы
    private StringProperty groupId;
    //Список иденитификаторов наличных пользователей текущей группы
    public List<String> groupMembersIds;
    //Список иденитификаторов ранее приглашенных пользователей текущей группы
    public List<String> groupInvitedUsersIds;
    //Текущий путь к xml - файлу модели типичных слов
    public TypicalWords curerntTypicalWords;
    
    public GlobalModel(
        String _groupId
    ){
    
        groupId = new SimpleStringProperty(_groupId);
        groupMembersIds = new ArrayList<>();
        groupInvitedUsersIds = new ArrayList<>();
    }

    public String getGroupId() {
        return groupId.getValue();
    }

    public StringProperty groupIdProperty() {
        return groupId;
    }
}
