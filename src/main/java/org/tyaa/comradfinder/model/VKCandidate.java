/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.model;

/**
 *
 * Модель кандидата на приглашение в группу
 * 
 * @author Юрий
 */
public class VKCandidate
{
    //User's VK id
    private Integer mUID;
    //First and last names
    private String mFirstName;
    private String mLastName;
    //Score
    private Integer mScore;
    
    public VKCandidate(){
        
        mUID = 0;
        mFirstName = "";
        mLastName = "";
        mScore = 0;
    }

    public Integer getUID()
    {
        return mUID;
    }

    public void setUID(Integer _uID)
    {
        mUID = _uID;
    }

    public String getFirstName()
    {
        return mFirstName;
    }

    public void setFirstName(String _firstName)
    {
        mFirstName = _firstName;
    }

    public String getLastName()
    {
        return mLastName;
    }

    public void setLastName(String _lastName)
    {
        mLastName = _lastName;
    }

    public Integer getScore()
    {
        return mScore;
    }

    public void setScore(Integer _score)
    {
        mScore = _score;
    }
}
