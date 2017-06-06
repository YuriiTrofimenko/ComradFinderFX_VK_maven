/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.modules.facades;

import java.io.IOException;
import static java.lang.System.out;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tyaa.comradfinder.MainApp;
import org.tyaa.comradfinder.model.TypicalWords;
import org.tyaa.comradfinder.model.VKCandidate;
import org.tyaa.comradfinder.model.VKUser;
import org.tyaa.comradfinder.modules.ExcelSaver;
import org.tyaa.comradfinder.modules.JsonFetcher;
import org.tyaa.comradfinder.modules.JsonParser;
import org.tyaa.comradfinder.modules.XmlImporter;
import org.xml.sax.SAXException;

/**
 *
 * Поиск кандидатов по модели
 * 
 * @author Yurii
 */
public class ComradFinder {
    
    public static void findByModel(String _countryId, String _cityId, String _age, String _sex)
    {
        //out.println("*** Find users by model ***");
        
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
        try {
            try {
                typicalWords = XmlImporter.getTypicalWords("TypicalWords.xml");
            } catch (SAXException ex) {
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException | XMLStreamException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (typicalWords != null) {
            
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
                );

                JSONArray usersItems = jsonParser.parseVKSearch(jsonString);

                //Перебираем все JSONObject с краткой информацией о найденных пользователях
                for (int i = 1; i < usersItems.length(); i++) {

                    //if (i > 5) break;
                    int score = 0;

                    //Получаем id текущего пользователя
                    Integer userId =
                        (Integer)((JSONObject)usersItems.get(i)).get("uid");
                        //(JSONObject)((JSONArray)usersItems.get(i)).get(0);
                    //out.println(userId);

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
            
            //Сохраняем результаты в таблицу
            ExcelSaver es = new ExcelSaver();
            try {
                es.saveCandidates(candidatesList);
            } catch (IOException ex) {
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //out.println(jsonString);
    }
}
