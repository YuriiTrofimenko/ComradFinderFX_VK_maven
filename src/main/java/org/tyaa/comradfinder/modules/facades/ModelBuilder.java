/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.modules.facades;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.concurrent.Task;
import javax.xml.stream.XMLStreamException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tyaa.comradfinder.Globals;
import org.tyaa.comradfinder.MainApp;
import org.tyaa.comradfinder.model.TypicalWords;
import org.tyaa.comradfinder.model.VKCandidate;
import org.tyaa.comradfinder.model.VKUser;
import org.tyaa.comradfinder.modules.JsonFetcher;
import org.tyaa.comradfinder.modules.JsonParser;
import org.tyaa.comradfinder.modules.XmlExporter;
import org.tyaa.comradfinder.modules.exception.FailJsonFetchException;

/**
 *
 * Построение текстовой модели совокупного пользователя группы
 * 
 * @author Yurii
 */
public class ModelBuilder {
    
    //public static String groupId;
    
    public static Task buildModel(String _groupId)
    {
        //out.println("***Users model builder***");
        
        //String _groupId = groupId;
        
        return new Task() {
            
            @Override
            protected Object call() throws Exception {
        
                String jsonString = "";
                JsonFetcher jsonFetcher = new JsonFetcher();
                JsonParser jsonParser = new JsonParser();
                VKUser vKUser = null;

                List interestsList = new ArrayList<>();
                List activitiesList = new ArrayList<>();
                List aboutList = new ArrayList<>();

                List politicalList = new ArrayList<>();
                List religionList = new ArrayList<>();
                List inspiredByList = new ArrayList<>();
                List peopleMainList = new ArrayList<>();
                List lifeMainList = new ArrayList<>();
                List smokingList = new ArrayList<>();
                List alcoholList = new ArrayList<>();

                List booksList = new ArrayList<>();
                List musicList = new ArrayList<>();
                List moviesList = new ArrayList<>();

                //Получаем список идентификаторов пользователей указанной группы ВК
                /* tested with parameter value tehnokom_su*/
                
                //java.net.UnknownHostException
                //cult_rev
                jsonString = jsonFetcher.fetchByUrl(
                    Globals.BASE_VK_API_URL
                        + "/method/groups.getMembers"
                        +"?group_id=" + _groupId
                        + "&" + Globals.ACCESS_TOCKEN
                        + "&" + Globals.VK_API_VERSION
                );
                JSONArray usersIds = jsonParser.parseVKGroup(jsonString);
                
                //
                MainApp.globalModel.groupMembersIds.clear();
                
                //перебираем userId
                for (int i = 0; i < usersIds.length(); i++) {

                    //if (i > 5) break;

                    String userId = usersIds.get(i).toString();
                    //
                    MainApp.globalModel.groupMembersIds.add(userId);
                    //out.println(usersIds.get(i));

                    //Получаем более полную информацию о текущем пользователе
                    jsonString = jsonFetcher.fetchByUrl(
                        Globals.BASE_VK_API_URL + "/method/users.get"
                        + "?user_ids=" + userId
                        + "&fields=about,activities,interests,personal,books,music,movies"
                        + "&" + Globals.ACCESS_TOCKEN
                        + "&" + Globals.VK_API_VERSION
                    );
                    //out.println(jsonString);

                    vKUser = jsonParser.parseVKUser(jsonString);
                    //out.println(jsonString);
                    //out.println(vKUser);

                    //Разбиваем текст на слова и добавляем их в список слов
                    //соответствующего раздела
                    if(!vKUser.getInterests().equals("")){

                        String tmp = vKUser.getInterests().replace(", ", " ");
                        tmp = tmp.replace("\n\n", " ");
                        interestsList.addAll(Arrays.asList(tmp.split(" ")));
                    }


                    if(!vKUser.getActivities().equals("")){

                        String tmp = vKUser.getActivities().replaceAll(", ", " ");
                        tmp = tmp.replace("\n\n", " ");
                        activitiesList.addAll(Arrays.asList(tmp.split(" ")));
                    }


                    if(!vKUser.getAbout().equals("")){

                        String tmp = vKUser.getAbout().replace(", ", " ");
                        tmp = tmp.replace("\n\n", " ");
                        aboutList.addAll(Arrays.asList(tmp.split(" ")));
                    }

                    if(!vKUser.getBooks().equals("")){

                        String tmp = vKUser.getBooks().replace(", ", " ");
                        tmp = tmp.replace("\n\n", " ");
                        booksList.addAll(Arrays.asList(tmp.split(" ")));
                    }

                    if(!vKUser.getMusic().equals("")){

                        String tmp = vKUser.getMusic().replace(", ", " ");
                        tmp = tmp.replace("\n\n", " ");
                        musicList.addAll(Arrays.asList(tmp.split(" ")));
                    }

                    if(!vKUser.getMovies().equals("")){

                        String tmp = vKUser.getMovies().replace(", ", " ");
                        tmp = tmp.replace("\n\n", " ");
                        moviesList.addAll(Arrays.asList(tmp.split(" ")));
                    }

                    //Добавляем идентификатор варианта выбора для раздела Полит. взглядов
                    //в список
                    if(vKUser.getPolitical() != 0){

                        politicalList.add(vKUser.getPolitical());
                    }

                    if(!vKUser.getReligion().equals("")){

                        String tmp = vKUser.getReligion().replace(", ", " ");
                        tmp = tmp.replace("\n\n", " ");
                        religionList.addAll(Arrays.asList(tmp.split(" ")));
                    }

                    if(!vKUser.getInspiredBy().equals("")){

                        String tmp = vKUser.getInspiredBy().replace(", ", " ");
                        tmp = tmp.replace("\n\n", " ");
                        inspiredByList.addAll(Arrays.asList(tmp.split(" ")));
                    }

                    if(vKUser.getPeopleMain()!= 0){

                        peopleMainList.add(vKUser.getPeopleMain());
                    }

                    if(vKUser.getLifeMain()!= 0){

                        lifeMainList.add(vKUser.getLifeMain());
                    }

                    if(vKUser.getSmoking()!= 0){

                        smokingList.add(vKUser.getSmoking());
                    }

                    if(vKUser.getAlcohol()!= 0){

                        alcoholList.add(vKUser.getAlcohol());
                    }

                    //out.println("Processed users: " + i);
                    updateProgress(i, usersIds.length());
                }

                /**/
                //out.println("Calculating the frequency of words... ");
                //Вычисляем частоту каждого слова в списке типа Карта
                Iterator<String> interestsWordIterator = interestsList.iterator();
                Map<String, Integer> interestsFreqMap = new HashMap<>();
                interestsWordIterator.forEachRemaining(s -> interestsFreqMap.merge(
                        s.toLowerCase()
                        , 1
                        , (a, b) -> a + b
                    )
                );

                Iterator<String> activitiesWordIterator = activitiesList.iterator();
                Map<String, Integer> activitiesFreqMap = new HashMap<>();
                activitiesWordIterator.forEachRemaining(s -> activitiesFreqMap.merge(
                        s.toLowerCase()
                        , 1
                        , (a, b) -> a + b
                    )
                );

                Iterator<String> aboutWordIterator = aboutList.iterator();
                Map<String, Integer> aboutFreqMap = new HashMap<>();
                aboutWordIterator.forEachRemaining(s -> aboutFreqMap.merge(
                        s.toLowerCase()
                        , 1
                        , (a, b) -> a + b
                    )
                );

                Iterator<String> booksWordIterator = booksList.iterator();
                Map<String, Integer> booksFreqMap = new HashMap<>();
                booksWordIterator.forEachRemaining(s -> booksFreqMap.merge(
                        s.toLowerCase()
                        , 1
                        , (a, b) -> a + b
                    )
                );

                Iterator<String> musicWordIterator = musicList.iterator();
                Map<String, Integer> musicFreqMap = new HashMap<>();
                musicWordIterator.forEachRemaining(s -> musicFreqMap.merge(
                        s.toLowerCase()
                        , 1
                        , (a, b) -> a + b
                    )
                );

                Iterator<String> moviesWordIterator = moviesList.iterator();
                Map<String, Integer> moviesFreqMap = new HashMap<>();
                moviesWordIterator.forEachRemaining(s -> moviesFreqMap.merge(
                        s.toLowerCase()
                        , 1
                        , (a, b) -> a + b
                    )
                );

                Iterator<Integer> politicalIndexIterator = politicalList.iterator();
                Map<Integer, Integer> politicalFreqMap = new HashMap<>();
                politicalIndexIterator.forEachRemaining(i -> politicalFreqMap.merge(
                        i
                        , 1
                        , (a, b) -> a + b
                    )
                );

                Iterator<String> religionWordIterator = religionList.iterator();
                Map<String, Integer> religionFreqMap = new HashMap<>();
                religionWordIterator.forEachRemaining(s -> religionFreqMap.merge(
                        s.toLowerCase()
                        , 1
                        , (a, b) -> a + b
                    )
                );

                Iterator<String> inspiredByWordIterator = inspiredByList.iterator();
                Map<String, Integer> inspiredByFreqMap = new HashMap<>();
                inspiredByWordIterator.forEachRemaining(s -> inspiredByFreqMap.merge(
                        s.toLowerCase()
                        , 1
                        , (a, b) -> a + b
                    )
                );

                Iterator<Integer> peopleMainIndexIterator = peopleMainList.iterator();
                Map<Integer, Integer> peopleMainFreqMap = new HashMap<>();
                peopleMainIndexIterator.forEachRemaining(i -> peopleMainFreqMap.merge(
                        i
                        , 1
                        , (a, b) -> a + b
                    )
                );

                Iterator<Integer> lifeMainIndexIterator = lifeMainList.iterator();
                Map<Integer, Integer> lifeMainFreqMap = new HashMap<>();
                lifeMainIndexIterator.forEachRemaining(i -> lifeMainFreqMap.merge(
                        i
                        , 1
                        , (a, b) -> a + b
                    )
                );

                Iterator<Integer> smokingIndexIterator = smokingList.iterator();
                Map<Integer, Integer> smokingFreqMap = new HashMap<>();
                smokingIndexIterator.forEachRemaining(i -> smokingFreqMap.merge(
                        i
                        , 1
                        , (a, b) -> a + b
                    )
                );

                Iterator<Integer> alcoholIndexIterator = alcoholList.iterator();
                Map<Integer, Integer> alcoholFreqMap = new HashMap<>();
                alcoholIndexIterator.forEachRemaining(i -> alcoholFreqMap.merge(
                        i
                        , 1
                        , (a, b) -> a + b
                    )
                );

                /*Заносим в модель TypicalWords списки-карты*/

                TypicalWords typicalWords = new TypicalWords();
                
                typicalWords.mGroupId = _groupId;

                //В каждом спике-карте отбрасываем пары слово - частота
                //с длиной слова более трех символов,
                //сортируем по убыванию частоты
                //и отбираем первые десять пар
                typicalWords.mInterestMap = interestsFreqMap.entrySet().stream() // получим стрим пар (слово, частота)
                        .filter(m -> m.getKey().length() > 3)
                        .sorted(descendingFrequencyOrder()) // отсортируем в обратном порядке по частоте
                        .limit(10) // возьмем первые 10
                        .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue())); // соберем в список-карту

                typicalWords.mActivityMap = activitiesFreqMap.entrySet().stream()
                        .filter(m -> m.getKey().length() > 3)
                        .sorted(descendingFrequencyOrder())
                        .limit(10)
                        .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue()));

                typicalWords.mAboutMap = aboutFreqMap.entrySet().stream()
                        .filter(m -> m.getKey().length() > 3)
                        .sorted(descendingFrequencyOrder())
                        .limit(10)
                        .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue()));

                typicalWords.mBooksMap = booksFreqMap.entrySet().stream()
                        .filter(m -> m.getKey().length() > 3)
                        .sorted(descendingFrequencyOrder())
                        .limit(10)
                        .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue()));

                typicalWords.mMusicMap = musicFreqMap.entrySet().stream()
                        .filter(m -> m.getKey().length() > 3)
                        .sorted(descendingFrequencyOrder())
                        .limit(10)
                        .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue()));

                typicalWords.mMoviesMap = moviesFreqMap.entrySet().stream()
                        .filter(m -> m.getKey().length() > 3)
                        .sorted(descendingFrequencyOrder())
                        .limit(10)
                        .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue()));

                typicalWords.mPoliticalMap = politicalFreqMap.entrySet().stream()
                        .sorted(descendingIntFrequencyOrder())
                        .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue()));

                typicalWords.mReligionMap = religionFreqMap.entrySet().stream()
                        .filter(m -> m.getKey().length() > 3)
                        .sorted(descendingFrequencyOrder())
                        .limit(10)
                        .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue()));

                typicalWords.mInspiredByMap = inspiredByFreqMap.entrySet().stream()
                        .filter(m -> m.getKey().length() > 3)
                        .sorted(descendingFrequencyOrder())
                        .limit(10)
                        .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue()));

                typicalWords.mPeopleMainMap = peopleMainFreqMap.entrySet().stream()
                        .sorted(descendingIntFrequencyOrder())
                        .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue()));

                typicalWords.mLifeMainMap = lifeMainFreqMap.entrySet().stream()
                        .sorted(descendingIntFrequencyOrder())
                        .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue()));

                typicalWords.mSmokingMap = smokingFreqMap.entrySet().stream()
                        .sorted(descendingIntFrequencyOrder())
                        .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue()));

                typicalWords.mAlcoholMap = alcoholFreqMap.entrySet().stream()
                        .sorted(descendingIntFrequencyOrder())
                        .collect(Collectors.toMap(m -> m.getKey(), m -> m.getValue()));

                /*End*/

                //out.println("Output results for interests");
                /*interestsFreqMap.entrySet().stream()                 // получим стрим пар (слово, частота)
                        .filter(m -> m.getKey().length() > 3)
                        .sorted(descendingFrequencyOrder()) // отсортируем
                        .limit(10)                          // возьмем первые 10
                        .map(Map.Entry::toString)             // из каждой пары возьмем
                        .forEach(System.out::println);      // выведем в консоль*/
                /*typicalWords.mInterestMap.forEach(
                    (k,v) -> System.out.println("key: " + k + " value: " + v)
                );*/

                //out.println("");
                //out.println("Output results for activities");
                /*activitiesFreqMap.entrySet().stream()
                        .filter(m -> m.getKey().length() > 3)
                        .sorted(descendingFrequencyOrder())
                        .limit(10)
                        .map(Map.Entry::toString)
                        .forEach(System.out::println);*/
                /*typicalWords.mActivityMap.forEach(
                    (k,v) -> System.out.println("key: " + k + " value: " + v)
                );*/

                //out.println("");
                //out.println("Output results for about");
                /*aboutFreqMap.entrySet().stream()
                        .filter(m -> m.getKey().length() > 3)
                        .sorted(descendingFrequencyOrder())
                        .limit(10)
                        .map(Map.Entry::toString)
                        .forEach(System.out::println);*/
                /*typicalWords.mAboutMap.forEach(
                    (k,v) -> System.out.println("key: " + k + " value: " + v)
                );*/

                //out.println("");
                //out.println("Output results for political");
                /*politicalFreqMap.entrySet().stream()
                        .sorted(descendingIntFrequencyOrder())
                        .map(Map.Entry::toString)
                        .forEach(System.out::println);*/
                /*typicalWords.mPoliticalMap.forEach(
                    (k,v) -> System.out.println("key: " + k + " value: " + v)
                );*/

                //out.println("Output results for religion");
                /*religionFreqMap.entrySet().stream()
                        .filter(m -> m.getKey().length() > 3)
                        .sorted(descendingFrequencyOrder())
                        .limit(10)
                        .map(Map.Entry::toString)
                        .forEach(System.out::println);*/
                /*typicalWords.mReligionMap.forEach(
                    (k,v) -> System.out.println("key: " + k + " value: " + v)
                );*/

                //out.println("Output results for inspired_by");
                /*inspiredByFreqMap.entrySet().stream()
                        .filter(m -> m.getKey().length() > 3)
                        .sorted(descendingFrequencyOrder())
                        .limit(10)
                        .map(Map.Entry::toString)
                        .forEach(System.out::println);*/
                /*typicalWords.mInspiredByMap.forEach(
                    (k,v) -> System.out.println("key: " + k + " value: " + v)
                );*/

                //out.println("");
                //out.println("Output results for people_main");
                /*peopleMainFreqMap.entrySet().stream()
                        .sorted(descendingIntFrequencyOrder())
                        .map(Map.Entry::toString)
                        .forEach(System.out::println);*/
                /*typicalWords.mPeopleMainMap.forEach(
                    (k,v) -> System.out.println("key: " + k + " value: " + v)
                );*/

                //out.println("");
                //out.println("Output results for life_main");
                /*lifeMainFreqMap.entrySet().stream()
                        .sorted(descendingIntFrequencyOrder())
                        .map(Map.Entry::toString)
                        .forEach(System.out::println);*/
                /*typicalWords.mLifeMainMap.forEach(
                    (k,v) -> System.out.println("key: " + k + " value: " + v)
                );*/

                //out.println("");
                //out.println("Output results for smoking");
                /*smokingFreqMap.entrySet().stream()
                        .sorted(descendingIntFrequencyOrder())
                        .map(Map.Entry::toString)
                        .forEach(System.out::println);*/
                /*typicalWords.mSmokingMap.forEach(
                    (k,v) -> System.out.println("key: " + k + " value: " + v)
                );*/

                //out.println("");
                //out.println("Output results for alcohol");
                /*alcoholFreqMap.entrySet().stream()
                        .sorted(descendingIntFrequencyOrder())
                        .map(Map.Entry::toString)
                        .forEach(System.out::println);*/
                /*typicalWords.mAlcoholMap.forEach(
                    (k,v) -> System.out.println("key: " + k + " value: " + v)
                );*/

                //
                try {

                    //Сохраняем модель в файл xml
                    XmlExporter.TypicalWordsToXml(typicalWords, "TypicalWords.xml");
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
                catch(XMLStreamException e) {
                    e.printStackTrace();
                }
                
                return true;
            }
        };
    }
    
    //Метод сравнения значений частоты у разных пар-элементов списка-карты
    private static Comparator<Map.Entry<String, Integer>> descendingFrequencyOrder() {
        //
        return Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue)
                .reversed();
                //.thenComparing(Map.Entry::getKey);
    }
    
    private static Comparator<Map.Entry<Integer, Integer>> descendingIntFrequencyOrder() {
        //
        return Comparator.<Map.Entry<Integer, Integer>>comparingInt(Map.Entry::getValue)
                .reversed();
                //.thenComparing(Map.Entry::getKey);
    }
    
    public static Task fillGroupMembersIds(String _groupId)
        throws FailJsonFetchException
    {
        return new Task() {
            
            @Override
            protected Object call() throws Exception {
        
                String jsonString = "";
                JsonFetcher jsonFetcher = new JsonFetcher();
                JsonParser jsonParser = new JsonParser();

                jsonString = jsonFetcher.fetchByUrl(
                    Globals.BASE_VK_API_URL + "/method/groups.getMembers"
                        +"?group_id=" + _groupId
                        + "&" + Globals.ACCESS_TOCKEN
                        + "&" + Globals.VK_API_VERSION
                        
                );
                JSONArray usersIds = jsonParser.parseVKGroup(jsonString);

                //
                MainApp.globalModel.groupMembersIds.clear();

                //перебираем userId
                for (int i = 0; i < usersIds.length(); i++) {

                    //if (i > 5) break;

                    String userId = usersIds.get(i).toString();
                    //
                    MainApp.globalModel.groupMembersIds.add(userId);
                }
                return true;
            }
        };
    }
    
    public static Task fillGroupInvitedUsersIds(String _groupId)
        throws FailJsonFetchException
    {
        return new Task() {
            
            @Override
            protected Object call() throws Exception {

                List<String> groupInvitedUsersIds = new ArrayList<>();
                String jsonString = "";
                JsonFetcher jsonFetcher = new JsonFetcher();
                JsonParser jsonParser = new JsonParser();

                jsonString = jsonFetcher.fetchByUrl(
                    "http://1-dot-comradfinder.appspot.com/comradfinder?action=get-ivited-users&groupid=" + _groupId
                );
                JSONArray users = jsonParser.parseGAEIvitedUsers(jsonString);

                //System.out.println("http://1-dot-comradfinder.appspot.com/comradfinder?action=get-ivited-users&groupid=" + _groupId);
                //System.out.println(jsonString);
                //
                MainApp.globalModel.groupInvitedUsersIds.clear();

                //перебираем userId
                for (int i = 0; i < users.length(); i++) {

                    //if (i > 5) break;

                    JSONObject userDataJSONObject = users.getJSONObject(i);

                    String userId = "";

                    if(userDataJSONObject.has("vkUid"))
                        userId = String.valueOf(userDataJSONObject.getInt("vkUid"));

                    //
                    MainApp.globalModel.groupInvitedUsersIds.add(userId);
                }
                
                return true;
            }
        };
    }
    
    public static boolean addGroupInvitedUser(String _groupId, VKCandidate _vKCandidate)
        throws FailJsonFetchException
    {
        boolean result = false;
        String jsonString = "";
        JsonFetcher jsonFetcher = new JsonFetcher();
        //JsonParser jsonParser = new JsonParser();
        String uid = _vKCandidate.getUID().toString();
        String fname = "";
        String lname = "";
        String score = _vKCandidate.getScore().toString();
        try {
            fname = URLEncoder.encode(_vKCandidate.getFirstName(), "UTF-8");
            lname = URLEncoder.encode(_vKCandidate.getLastName(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            fname = "";
            lname = "";
            return false;
        }
        
        //https://1-dot-comradfinder.appspot.com/comradfinder?action=add-user&groupid=cult_rev&vkuid=201378344&fname=qwe&lname=asd&score=30
        
        jsonString = jsonFetcher.fetchByUrl(
            "http://1-dot-comradfinder.appspot.com/comradfinder?action=add-user&groupid="
                    + _groupId
                    + "&vkuid="
                    + uid
                    + "&fname="
                    + fname
                    + "&lname="
                    + lname
                    + "&score="
                    + score
        );

        System.out.println(jsonString);
        //
        if (jsonString != null && !jsonString.equals("")) {
            
            //
            MainApp.globalModel.groupInvitedUsersIds.add(uid);
            result = true;
        }
        return result;
    }
}
