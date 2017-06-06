/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.model;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Модель слов, часто встречающихся на страницах участников группы
 * 
 * @author Юрий
 */
public class TypicalWords
{
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
    }
}
