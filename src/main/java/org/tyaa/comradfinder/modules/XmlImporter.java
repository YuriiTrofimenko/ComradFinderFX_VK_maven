/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.modules;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.tyaa.comradfinder.model.TypicalWords;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.tyaa.comradfinder.MainApp;
import org.tyaa.comradfinder.model.VKCandidate;
import org.xml.sax.SAXException;

/**
 *
 * Заполнение текстовой модели совокупного пользователя группы
 * из файла формата XML из той же директории, что и исполняемый файл приложения
 * 
 * @author Юрий
 */
public class XmlImporter
{
    private static TypicalWords mTypicalWords;
    private static List<VKCandidate> mVKCandidatesList;
    private static String mGroupId;
    
    static {
    
        mGroupId = "";
    }
    
    public static TypicalWords getTypicalWords(String _filePath)
        throws IOException
            , XMLStreamException
            , SAXException
            , ParserConfigurationException {
                
        //try {
            mTypicalWords = new TypicalWords();
            File fXmlFile = new File(_filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            
            Node rootNode = doc.getDocumentElement();
            mGroupId = rootNode.getAttributes().getNamedItem("group_id").getTextContent();
            NodeList rootChildNodes = rootNode.getChildNodes();
            ArrayList<Node> rootChildList = new ArrayList<>();
            
            for (int i = 0; i < rootChildNodes.getLength(); i++) {
                
                if (rootChildNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    
                    rootChildList.add(rootChildNodes.item(i));
                }
            }
            
            mTypicalWords.mGroupId = mGroupId;
            
            for (Node currentNode : rootChildList) {

                String infoItemName = currentNode.getNodeName();
                NodeList infoItemChildNodes = currentNode.getChildNodes();
                
                populateField(infoItemName, infoItemChildNodes);
            }
            
        //} catch (Exception e) {
            
            //System.out.println(e);
        //}
        
        return mTypicalWords;
    }
    
    public static List<VKCandidate> getVKCandidates(String _filePath)
        throws IOException
            , XMLStreamException
            , SAXException
            , ParserConfigurationException {
        
            mVKCandidatesList = new ArrayList<>();
            File fXmlFile = new File(_filePath);
            DocumentBuilderFactory dbFactory =
                DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            
            Node rootNode = doc.getDocumentElement();
            NodeList rootChildNodes = rootNode.getChildNodes();
            ArrayList<Node> rootChildList = new ArrayList<>();
            
            for (int i = 0; i < rootChildNodes.getLength(); i++) {
                if (rootChildNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    rootChildList.add(rootChildNodes.item(i));
                }
            }
            
            for (Node currentNode : rootChildList) {
                
                String currentId = "";
                String currentFName = "";
                String currentLName = "";
                String currentScore = "";

                //String vKCandidateItemName = "candidate";
                NodeList vKCandidateItemChildNodes = currentNode.getChildNodes();
                
                //System.out.println(currentNode.getNodeType());
                //System.out.println(currentNode.getNodeName());
                //System.out.println(currentNode.getTextContent());

                currentId =
                    currentNode
                        .getAttributes()
                        .getNamedItem("id")
                        .getTextContent();
                
                System.out.println("currentId " + currentId);
                for (String uid : MainApp.globalModel.groupInvitedUsersIds) {
                    System.out.println("uid " + uid);
                }
                
                if (!MainApp.globalModel
                        .groupInvitedUsersIds
                        .contains(currentId)) {
                    
                    for (int i = 0; i < vKCandidateItemChildNodes.getLength(); i++) {

                        if (vKCandidateItemChildNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {

                            if (vKCandidateItemChildNodes.item(i).getNodeName().equals("fname")) {

                                currentFName = vKCandidateItemChildNodes.item(i).getTextContent();
                            } else if (vKCandidateItemChildNodes.item(i).getNodeName().equals("lname")) {

                                currentLName = vKCandidateItemChildNodes.item(i).getTextContent();
                            } else if (vKCandidateItemChildNodes.item(i).getNodeName().equals("score")) {

                                currentScore = vKCandidateItemChildNodes.item(i).getTextContent();
                            }
                        }
                    }

                    VKCandidate newVKCandidate = new VKCandidate();
                    newVKCandidate.setUID(Integer.parseInt(currentId));
                    newVKCandidate.setFirstName(currentFName);
                    newVKCandidate.setLastName(currentLName);
                    newVKCandidate.setScore(Integer.parseInt(currentScore));

                    mVKCandidatesList.add(newVKCandidate);
                }
            }
        
        return mVKCandidatesList;
    }
    
    //Внутренний метод для заполнения того поля объекта TypicalWords,
    //имя которого передано в качестве аргумента
    private static void populateField(String _field_name, NodeList _infoItemChildNodes){
    
        for (int i = 0; i < _infoItemChildNodes.getLength(); i++) {

            if (_infoItemChildNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {

                if (_infoItemChildNodes.item(i).getNodeName().equals("variant")) {

                    NodeList variantChildNodes =
                        _infoItemChildNodes.item(i).getChildNodes();
                    
                    String currentValue = "";
                    String currentCount = "";
                    
                    for (int j = 0; j < variantChildNodes.getLength(); j++) {

                        if (variantChildNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            
                            if (variantChildNodes.item(j).getNodeName().equals("value")) {

                                //System.out.println("value = " + variantChildNodes.item(j).getTextContent());
                                currentValue = variantChildNodes.item(j).getTextContent();
                            } else if (variantChildNodes.item(j).getNodeName().equals("count")) {

                                //System.out.println("count = " + variantChildNodes.item(j).getTextContent());
                                currentCount = variantChildNodes.item(j).getTextContent();
                            }
                        }
                    }
                    switch(_field_name)
                    {
                        case "interest":{

                            mTypicalWords.mInterestMap.put(
                                currentValue
                                , Integer.parseInt(currentCount)
                            );
                            break;
                        }
                        case "activity":{

                            mTypicalWords.mActivityMap.put(
                                currentValue
                                , Integer.parseInt(currentCount)
                            );
                            break;
                        }
                        case "about":{

                            mTypicalWords.mAboutMap.put(
                                currentValue
                                , Integer.parseInt(currentCount)
                            );
                            break;
                        }
                        case "books":{

                            mTypicalWords.mBooksMap.put(
                                currentValue
                                , Integer.parseInt(currentCount)
                            );
                            break;
                        }
                        case "music":{

                            mTypicalWords.mMusicMap.put(
                                currentValue
                                , Integer.parseInt(currentCount)
                            );
                            break;
                        }
                        case "movies":{

                            mTypicalWords.mMoviesMap.put(
                                currentValue
                                , Integer.parseInt(currentCount)
                            );
                            break;
                        }
                        case "political":{

                            mTypicalWords.mPoliticalMap.put(
                                Integer.parseInt(currentValue)
                                , Integer.parseInt(currentCount)
                            );
                            break;
                        }
                        case "religion":{

                            mTypicalWords.mReligionMap.put(
                                currentValue
                                , Integer.parseInt(currentCount)
                            );
                            break;
                        }
                        case "inspiredby":{

                            mTypicalWords.mInspiredByMap.put(
                                currentValue
                                , Integer.parseInt(currentCount)
                            );
                            break;
                        }
                        case "peoplemain":{

                            mTypicalWords.mPeopleMainMap.put(
                                Integer.parseInt(currentValue)
                                , Integer.parseInt(currentCount)
                            );
                            break;
                        }
                        case "lifemain":{

                            mTypicalWords.mLifeMainMap.put(
                                Integer.parseInt(currentValue)
                                , Integer.parseInt(currentCount)
                            );
                            break;
                        }
                        case "smoking":{

                            mTypicalWords.mSmokingMap.put(
                                Integer.parseInt(currentValue)
                                , Integer.parseInt(currentCount)
                            );
                            break;
                        }
                        case "alcohol":{

                            mTypicalWords.mAlcoholMap.put(
                                Integer.parseInt(currentValue)
                                , Integer.parseInt(currentCount)
                            );
                            break;
                        }
                        default:{}
                    }
                }
                //rootChildList.add(rootChildNodes.item(i));
            }
        }
    }
}
