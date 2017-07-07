/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.modules;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tyaa.comradfinder.model.VKUser;

/**
 *
 * Заполнение модели участника группы из данных json-строки
 * 
 * @author Юрий
 */
public class JsonParser
{
    public VKUser parseVKUser(String _jsonString){
    
        JSONObject dataJsonObj = null;
        VKUser vKUser = new VKUser();

        try {
            dataJsonObj = new JSONObject(_jsonString);
            JSONArray response = dataJsonObj.getJSONArray("response");

            // 1. достаем индекс 0
            JSONObject userDataJSONObject = response.getJSONObject(0);
            if(userDataJSONObject.has("interests"))
                vKUser.setInterests(userDataJSONObject.getString("interests"));
            if(userDataJSONObject.has("activities"))
                vKUser.setActivities(userDataJSONObject.getString("activities"));
            if(userDataJSONObject.has("about"))
                vKUser.setAbout(userDataJSONObject.getString("about"));
            
            if(userDataJSONObject.has("books"))
                vKUser.setBooks(userDataJSONObject.getString("books"));
            if(userDataJSONObject.has("music"))
                vKUser.setMusic(userDataJSONObject.getString("music"));
            if(userDataJSONObject.has("movies"))
                vKUser.setMovies(userDataJSONObject.getString("movies"));
            
            if(userDataJSONObject.has("personal")){
            
                JSONObject personalJSONObject =
                    userDataJSONObject.getJSONObject("personal");
                
                if(personalJSONObject.has("political"))
                    vKUser.setPolitical(
                        personalJSONObject.getInt("political")
                    );
                
                if(personalJSONObject.has("religion"))
                    vKUser.setReligion(
                        personalJSONObject.getString("religion")
                    );
                
                if(personalJSONObject.has("inspired_by"))
                    vKUser.setInspiredBy(
                        personalJSONObject.getString("inspired_by")
                    );
                
                if(personalJSONObject.has("people_main"))
                    vKUser.setPeopleMain(
                        personalJSONObject.getInt("people_main")
                    );
                
                if(personalJSONObject.has("life_main"))
                    vKUser.setLifeMain(
                        personalJSONObject.getInt("life_main")
                    );
                
                if(personalJSONObject.has("smoking"))
                    vKUser.setSmoking(
                        personalJSONObject.getInt("smoking")
                    );
                
                if(personalJSONObject.has("alcohol"))
                    vKUser.setAlcohol(
                        personalJSONObject.getInt("alcohol")
                    );
            }
        } catch (JSONException e) {
            //e.printStackTrace();
            //System.err.println("There are some bad symbols in the JSON data");
        }
        
        return vKUser;
    }
    
    public JSONArray parseVKGroup(String _jsonString){
    
        JSONArray usersIds = null;

        try {
            JSONObject dataJsonObj = new JSONObject(_jsonString);
            JSONObject response = dataJsonObj.getJSONObject("response");
            usersIds = response.getJSONArray("users");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return usersIds;
    }
    
    public JSONArray parseVKSearch(String _jsonString){
    
        return jsonStringToJSONArray(_jsonString);
    }
    
    public JSONArray parseVKCountries(String _jsonString){
    
        return jsonStringToJSONArray(_jsonString);
    }
    
    public JSONArray parseVKRegions(String _jsonString){
    
        return jsonStringToJSONArray(_jsonString);
    }
    
    public JSONArray parseVKCities(String _jsonString){
    
        return jsonStringToJSONArray(_jsonString);
    }
    
    public JSONArray parseGAEIvitedUsers(String _jsonString){
    
        JSONArray items = null;

        try {
            JSONObject responseJSONObject = new JSONObject(_jsonString);
            items = responseJSONObject.getJSONArray("response");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return items;
    }
    
    private JSONArray jsonStringToJSONArray(String _jsonString){
    
        JSONArray items = null;

        try {
            items = new JSONArray(_jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return items;
    }
}
