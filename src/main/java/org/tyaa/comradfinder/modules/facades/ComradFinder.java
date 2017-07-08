/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.modules.facades;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import static java.lang.System.out;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tyaa.comradfinder.MainApp;
import org.tyaa.comradfinder.model.TypicalWords;
import org.tyaa.comradfinder.model.VKCandidate;
import org.tyaa.comradfinder.model.VKCity;
import org.tyaa.comradfinder.model.VKCountry;
import org.tyaa.comradfinder.model.VKRegion;
import org.tyaa.comradfinder.model.VKUser;
import org.tyaa.comradfinder.modules.ExcelSaver;
import org.tyaa.comradfinder.modules.JsonFetcher;
import org.tyaa.comradfinder.modules.JsonParser;
import org.tyaa.comradfinder.modules.XmlExporter;
import org.tyaa.comradfinder.modules.XmlImporter;
import org.tyaa.comradfinder.modules.exception.FailJsonFetchException;
import org.xml.sax.SAXException;

/**
 *
 * Поиск кандидатов по модели
 * 
 * @author Yurii
 */
public class ComradFinder {
    
    public static Task findByModel(
        String _countryId
            , String _regionId
            , String _cityId
            , String _age
            , String _sex) throws FailJsonFetchException
    {
        //out.println("*** Find users by model ***");
        
        return new Task(){
            @Override
            protected Object call() throws Exception
            {
                List<VKCandidate> candidatesList = new ArrayList();

                List<String> statusList = new ArrayList();

                VKUser vKUser = null;

                /*Получаем из соцсети список id пользователей из определенной страны*/
                String jsonString = "";
                JsonFetcher jsonFetcher = new JsonFetcher();
                JsonParser jsonParser = new JsonParser();


                /************************************************/

                //Читаем набор типичных слов из файла XML в Java объект
                TypicalWords typicalWords = new TypicalWords();
                
                String sex = "";
                String age1 = _age;
                String age2 = _age;
                
                switch(_sex){
                
                    case "female" :
                        sex = "1";
                        break;
                    case "male" :
                        sex = "2";
                        break;
                    default :
                        sex = "0";
                }
                
                if (_age.equals("0")) {
                    
                    age1 = "14";
                    age2 = "100";
                }
                
                typicalWords =
                        MainApp.globalModel.curerntTypicalWords;

                if (typicalWords != null) {
                    
                    List<String> groupMembersIds = new ArrayList<>();
                    
                    if(MainApp.globalModel.groupMembersIds != null) {
                    
                        groupMembersIds = MainApp.globalModel.groupMembersIds;
                    }

                    for (int i_status = 1; i_status <= 8; i_status++) {

                        String statusString = String.valueOf(i_status);

                        //Получаем информацию о первой тысяче пользователей
                        //с заданными страной и городом
                        /* tested with parameters values country=2&city=455*/
                        jsonString = jsonFetcher.fetchByUrl(
                            "https://api.vk.com/method/users.search?access_token=5e8976369e5ba9ffa778029ccd5792e36b99c236870d62f1e4b442af1b5bdd1c360d25fa264daafb6288d&photo=1&count=1000"
                                + "&status="
                                + statusString
                                + "&country="
                                + _countryId
                                + "&city="
                                + _cityId
                                + "&age_from="
                                + age1
                                + "&age_to="
                                + age2
                                + "&sex="
                                + sex
                        );

                        JSONArray usersItems = jsonParser.parseVKSearch(jsonString);

                        //Перебираем все JSONObject с краткой информацией о найденных пользователях
                        for (int i = 1; i < usersItems.length(); i++) {

                            //if (i > 5) break;
                            int score = 0;

                            //Получаем id текущего пользователя
                            String userIdString =
                                ((JSONObject)usersItems.get(i)).get("uid").toString();
                            Integer userId =
                                Integer.parseInt(userIdString);
                                //(JSONObject)((JSONArray)usersItems.get(i)).get(0);
                            //out.println(userId);

                            //Рассматриваем кандидатуру пользователя с текущим
                            //идент-м только если он:
                            //- не состоит в текущей группе;
                            //- его никто не пришлашал (отсутствует в удаленной БД)
                            if(!groupMembersIds.contains(userIdString)
                                && !MainApp.globalModel.groupInvitedUsersIds.contains(userIdString)) {
                                //Получаем более полную информацию о текущем пользователе
                                jsonString = jsonFetcher.fetchByUrl(
                                    "https://api.vk.com/method/users.get"
                                    +"?user_ids="
                                    + userId
                                    +"&fields=about,activities,interests,personal,books,music,movies"
                                );
                                //out.println(jsonString);

                                //Переводим информацию о пользователе из json в 
                                vKUser = jsonParser.parseVKUser(jsonString);
                                //out.println(jsonString);

                                //Проверяем тексты разделов личной информации пользователя
                                //на наличие слова из соответствующего раздела модели
                                //Если нашли - добавляем баллы (частоту встречи этого слова в 
                                //разделе модели)

                                for (Map.Entry<String, Integer> interestItem : typicalWords.mInterestMap.entrySet()) {

                                    if (vKUser.getInterests().contains(interestItem.getKey())) {
                                        score += interestItem.getValue();
                                    }
                                }

                                for (Map.Entry<String, Integer> activityItem : typicalWords.mActivityMap.entrySet()) {

                                    if (vKUser.getActivities().contains(activityItem.getKey())) {
                                        score += activityItem.getValue();
                                    }
                                }

                                for (Map.Entry<String, Integer> aboutItem : typicalWords.mAboutMap.entrySet()) {

                                    if (vKUser.getAbout().contains(aboutItem.getKey())) {
                                        score += aboutItem.getValue();
                                    }
                                }

                                for (Map.Entry<String, Integer> booksItem : typicalWords.mBooksMap.entrySet()) {

                                    if (vKUser.getBooks().contains(booksItem.getKey())) {
                                        score += booksItem.getValue();
                                    }
                                }

                                for (Map.Entry<String, Integer> musicItem : typicalWords.mMusicMap.entrySet()) {

                                    if (vKUser.getMusic().contains(musicItem.getKey())) {
                                        score += musicItem.getValue();
                                    }
                                }

                                for (Map.Entry<String, Integer> moviesItem : typicalWords.mMoviesMap.entrySet()) {

                                    if (vKUser.getMovies().contains(moviesItem.getKey())) {
                                        score += moviesItem.getValue();
                                    }
                                }

                                for (Map.Entry<Integer, Integer> politicalItem : typicalWords.mPoliticalMap.entrySet()) {

                                    if (vKUser.getPolitical().intValue() == politicalItem.getKey().intValue()) {
                                        score += politicalItem.getValue();
                                    }
                                }

                                for (Map.Entry<String, Integer> religionItem : typicalWords.mReligionMap.entrySet()) {

                                    if (vKUser.getReligion().contains(religionItem.getKey())) {
                                        score += religionItem.getValue();
                                    }
                                }

                                for (Map.Entry<String, Integer> inspiredItem : typicalWords.mInspiredByMap.entrySet()) {

                                    if (vKUser.getInspiredBy().contains(inspiredItem.getKey())) {
                                        score += inspiredItem.getValue();
                                    }
                                }

                                for (Map.Entry<Integer, Integer> peopleItem : typicalWords.mPeopleMainMap.entrySet()) {

                                    if (vKUser.getPeopleMain().intValue() == peopleItem.getKey().intValue()) {
                                        score += peopleItem.getValue();
                                    }
                                }

                                for (Map.Entry<Integer, Integer> lifeMainItem : typicalWords.mLifeMainMap.entrySet()) {

                                    if (vKUser.getLifeMain().intValue() == lifeMainItem.getKey().intValue()) {
                                        score += lifeMainItem.getValue();
                                    }
                                }

                                for (Map.Entry<Integer, Integer> smokingItem : typicalWords.mSmokingMap.entrySet()) {

                                    if (vKUser.getSmoking().intValue() == smokingItem.getKey().intValue()) {
                                        score += smokingItem.getValue();
                                    }
                                }

                                for (Map.Entry<Integer, Integer> alcoholItem : typicalWords.mAlcoholMap.entrySet()) {

                                    if (vKUser.getAlcohol().intValue() == alcoholItem.getKey().intValue()) {
                                        score += alcoholItem.getValue();
                                    }
                                }

                                //Если набранные пользователем баллы больше нуля,
                                //заносим его в кандидаты на приглашение
                                if (score != 0) {

                                    VKCandidate vKCandidate = new VKCandidate();
                                    vKCandidate.setUID((Integer) ((JSONObject)usersItems.get(i)).get("uid"));
                                    vKCandidate.setFirstName((String) ((JSONObject)usersItems.get(i)).get("first_name"));
                                    vKCandidate.setLastName((String) ((JSONObject)usersItems.get(i)).get("last_name"));
                                    vKCandidate.setScore(score);

                                    candidatesList.add(vKCandidate);
                                }
                            }
                            updateProgress(i, usersItems.length());
                        }
                    }


                    candidatesList.sort((o1, o2) ->
                        o2.getScore().compareTo(o1.getScore()));

                    /*candidatesList.stream().forEach((vKCandidate) -> {

                        out.println(vKCandidate.getUID());
                        out.println(vKCandidate.getFirstName());
                        out.println(vKCandidate.getLastName());
                        out.println(vKCandidate.getScore());
                        out.println();
                    });*/

                    /*//Сохраняем результаты в таблицу
                    ExcelSaver es = new ExcelSaver();
                    try {
                        es.saveCandidates(candidatesList);
                    } catch (IOException ex) {
                        Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                    }*/
                    
                    //TODO проверить актуальность кандидатов!
                    //Сохраняем результаты (список кандидатов) в xml файл
                    
                    XmlExporter.VKCandidatesToXml(candidatesList, "VKCandidates.xml");
                }
                //out.println(jsonString);
                return true;
            }
        };
    }
    
    public static List<VKCountry> getCountries() throws FailJsonFetchException
    {
        String jsonString = "";
        JsonFetcher jsonFetcher = new JsonFetcher();
        JsonParser jsonParser = new JsonParser();
        List<VKCountry> mVKCountries = new ArrayList<>();
        
        jsonString = jsonFetcher.fetchByUrl(
            "https://api.vk.com/method/database.getCountries?need_all=1&count=1000"
        );
        
        JSONArray countriesItems = jsonParser.parseVKSearch(jsonString);

        //Перебираем все JSONObject с информацией о найденных countries
        for (int i = 1; i < countriesItems.length(); i++) {
        
            VKCountry newVKCountry = new VKCountry();
            newVKCountry.id =
                Integer.parseInt(((JSONObject)countriesItems.get(i)).get("cid").toString());
            newVKCountry.name =
                (String)((JSONObject)countriesItems.get(i)).get("title");
            mVKCountries.add(newVKCountry);
        }
        
        return mVKCountries;
    }
    
    public static List<VKRegion> getRegionsByCountryId(int _countryId) throws FailJsonFetchException
    {
        String jsonString = "";
        JsonFetcher jsonFetcher = new JsonFetcher();
        JsonParser jsonParser = new JsonParser();
        List<VKRegion> mVKRegions = new ArrayList<>();
        
        jsonString = jsonFetcher.fetchByUrl(
            "https://api.vk.com/method/database.getRegions?country_id=" + _countryId + "&count=1000"
        );
        
        JSONArray regionsItems = jsonParser.parseVKSearch(jsonString);

        //Перебираем все JSONObject с информацией о найденных regions
        for (int i = 1; i < regionsItems.length(); i++) {
        
            VKRegion newVKRegion = new VKRegion();
            newVKRegion.id =
                Integer.parseInt(((JSONObject)regionsItems.get(i)).get("region_id").toString());
            newVKRegion.name =
                (String)((JSONObject)regionsItems.get(i)).get("title");
            mVKRegions.add(newVKRegion);
        }
        
        return mVKRegions;
    }
    
    public static List<VKCity> getCitiesByCountryRegionIds(int _countryId, int _regionId, String _q) throws FailJsonFetchException, UnsupportedEncodingException
    {
        String jsonString = "";
        JsonFetcher jsonFetcher = new JsonFetcher();
        JsonParser jsonParser = new JsonParser();
        List<VKCity> mVKCities = new ArrayList<>();
        if (_q != null) {
            
            String q = URLEncoder.encode(_q, "UTF-8");
            //System.out.println("_q " + _q);
            //https://api.vk.com/method/database.getCities?country_id=1&region_id=1053480&q=%D0%BC%D0%BE&need_all=1&count=1000
            jsonString = jsonFetcher.fetchByUrl(
                "https://api.vk.com/method/database.getCities?country_id="
                    + _countryId
                    + "&region_id="
                    + _regionId
                    + "&q="
                    + q
                    + "&need_all=1&count=1000"
            );
        } else {
        
            jsonString = jsonFetcher.fetchByUrl(
                "https://api.vk.com/method/database.getCities?country_id="
                    + _countryId
                    + "&region_id="
                    + _regionId
                    + "&need_all=1&count=1000"
            );
        }
        
        JSONArray citiesItems = jsonParser.parseVKSearch(jsonString);

        //Перебираем все JSONObject с информацией о найденных regions
        for (int i = 0; i < citiesItems.length(); i++) {
        
            VKCity newVKCity = new VKCity();
            newVKCity.id =
                Integer.parseInt(((JSONObject)citiesItems.get(i)).get("cid").toString());
            newVKCity.name =
                (String)((JSONObject)citiesItems.get(i)).get("title");
            mVKCities.add(newVKCity);
        }
        
        return mVKCities;
    }
}
