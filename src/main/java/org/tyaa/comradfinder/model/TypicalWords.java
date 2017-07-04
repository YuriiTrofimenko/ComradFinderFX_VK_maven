/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Модель слов, часто встречающихся на страницах участников группы
 * 
 * @author Юрий
 */

public class TypicalWords implements Serializable
{
    public String mGroupId;
    
    public Map<String, Integer> mInterestMap;
    public Map<String, Integer> mActivityMap;
    public Map<String, Integer> mAboutMap;
    
    public Map<Integer, Integer> mPoliticalMap;
    public Map<String, Integer> mReligionMap;
    public Map<String, Integer> mInspiredByMap;
    public Map<Integer, Integer> mPeopleMainMap;
    public Map<Integer, Integer> mLifeMainMap;
    public Map<Integer, Integer> mSmokingMap;
    public Map<Integer, Integer> mAlcoholMap;
    
    public Map<String, Integer> mBooksMap;
    public Map<String, Integer> mMusicMap;
    public Map<String, Integer> mMoviesMap;
    
    /*Карты соответствия кодового и текстового представлений значения
    для полей вариантов типа Map<Integer, Integer> */
    
    public static Map<Integer, String> mPoliticalMapping;
    public static Map<Integer, String> mPeopleMainMapping;
    public static Map<Integer, String> mLifeMainMapping;
    public static Map<Integer, String> mSmokingMapping;
    public static Map<Integer, String> mAlcoholMapping;
    
    public TypicalWords(){
    
        mInterestMap = new HashMap<>();
        mActivityMap = new HashMap<>();
        mAboutMap = new HashMap<>();

        mPoliticalMap = new HashMap<>();
        mReligionMap = new HashMap<>();
        mInspiredByMap = new HashMap<>();
        mPeopleMainMap = new HashMap<>();
        mLifeMainMap = new HashMap<>();
        mSmokingMap = new HashMap<>();
        mAlcoholMap = new HashMap<>();
        
        mBooksMap = new HashMap<>();
        mMusicMap = new HashMap<>();
        mMoviesMap = new HashMap<>();
        
        /*Populate the mappings*/
        
        mPoliticalMapping = new HashMap<>();
        mPoliticalMapping.put(1, "коммунистические");
        mPoliticalMapping.put(2, "социалистические");
        mPoliticalMapping.put(3, "умеренные");
        mPoliticalMapping.put(4, "либеральные");
        mPoliticalMapping.put(5, "консервативные");
        mPoliticalMapping.put(6, "монархические");
        mPoliticalMapping.put(7, "ультраконсервативные");
        mPoliticalMapping.put(8, "индифферентные");
        mPoliticalMapping.put(9, "либертарианские");
        
        mPeopleMainMapping = new HashMap<>();
        mPeopleMainMapping.put(1, "ум и креативность");
        mPeopleMainMapping.put(2, "доброта и честность");
        mPeopleMainMapping.put(3, "красота и здоровье");
        mPeopleMainMapping.put(4, "власть и богатство");
        mPeopleMainMapping.put(5, "смелость и упорство");
        mPeopleMainMapping.put(6, "юмор и жизнелюбие");
        
        mLifeMainMapping = new HashMap<>();
        mLifeMainMapping.put(1, "семья и дети");
        mLifeMainMapping.put(2, "карьера и деньги");
        mLifeMainMapping.put(3, "развлечения и отдых");
        mLifeMainMapping.put(4, "наука и исследования");
        mLifeMainMapping.put(5, "совершенствование мира");
        mLifeMainMapping.put(6, "саморазвитие");
        mLifeMainMapping.put(7, "красота и искусство");
        mLifeMainMapping.put(8, "слава и влияние");
        
        mSmokingMapping = new HashMap<>();
        mSmokingMapping.put(1, "резко негативное");
        mSmokingMapping.put(2, "негативное");
        mSmokingMapping.put(3, "компромиссное");
        mSmokingMapping.put(4, "нейтральное");
        mSmokingMapping.put(5, "положительное");
        
        mAlcoholMapping = new HashMap<>();
        mAlcoholMapping.put(1, "резко негативное");
        mAlcoholMapping.put(2, "негативное");
        mAlcoholMapping.put(3, "компромиссное");
        mAlcoholMapping.put(4, "нейтральное");
        mAlcoholMapping.put(5, "положительное");
    }
}
