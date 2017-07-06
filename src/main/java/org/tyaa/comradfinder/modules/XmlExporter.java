/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.modules;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javanet.staxutils.IndentingXMLStreamWriter;
import org.tyaa.comradfinder.model.TypicalWords;
import org.tyaa.comradfinder.model.VKCandidate;

/**
 *
 * Сохранение текстовой модели совокупного пользователя группы
 * или списка кандидатов на приглашение
 * в файл формата XML
 * 
 * @author Юрий
 */
public class XmlExporter
{
    public static <K,V> void TypicalWordsToXml(
        TypicalWords _typicalWords
            , String _filePath) throws IOException, XMLStreamException {
        
        XMLStreamWriter xsw = null;
        FileOutputStream fileOutputStream = new FileOutputStream(_filePath);
        
        try {
            
            try {
                
                Integer count = 1;
                
                XMLOutputFactory xof = XMLOutputFactory.newInstance();
                // If you want pretty-printing, you can use:
                xsw =
                    new IndentingXMLStreamWriter(
                        xof.createXMLStreamWriter(
                            fileOutputStream,
                                "UTF-8"
                        )
                    );
                //xsw = xof.createXMLStreamWriter(out);
                xsw.writeStartDocument("utf-8", "1.0");
                xsw.writeStartElement("infoitems");
                xsw.writeAttribute("group_id", _typicalWords.mGroupId);
                System.out.println("group_id " + _typicalWords.mGroupId);
                
                    xsw.writeStartElement("interest");
                    for(Entry<String, Integer> variantEntry : _typicalWords.mInterestMap.entrySet()) {
                        xsw.writeStartElement("variant");
                            xsw.writeAttribute("id", count.toString());
                            xsw.writeStartElement("value");
                                xsw.writeCharacters(variantEntry.getKey());
                            xsw.writeEndElement();
                            xsw.writeStartElement("count");
                                xsw.writeCharacters(variantEntry.getValue().toString());
                            xsw.writeEndElement();
                        xsw.writeEndElement();
                        count++;
                    }
                    xsw.writeEndElement();
                    count = 0;
                    xsw.writeStartElement("activity");
                    for(Entry<String, Integer> variantEntry : _typicalWords.mActivityMap.entrySet()) {
                        xsw.writeStartElement("variant");
                            xsw.writeAttribute("id", count.toString());
                            xsw.writeStartElement("value");
                                xsw.writeCharacters(variantEntry.getKey());
                            xsw.writeEndElement();
                            xsw.writeStartElement("count");
                                xsw.writeCharacters(variantEntry.getValue().toString());
                            xsw.writeEndElement();
                        xsw.writeEndElement();
                        count++;
                    }
                    xsw.writeEndElement();
                    count = 0;
                    xsw.writeStartElement("about");
                    for(Entry<String, Integer> variantEntry : _typicalWords.mAboutMap.entrySet()) {
                        xsw.writeStartElement("variant");
                            xsw.writeAttribute("id", count.toString());
                            xsw.writeStartElement("value");
                                xsw.writeCharacters(variantEntry.getKey());
                            xsw.writeEndElement();
                            xsw.writeStartElement("count");
                                xsw.writeCharacters(variantEntry.getValue().toString());
                            xsw.writeEndElement();
                        xsw.writeEndElement();
                        count++;
                    }
                    xsw.writeEndElement();
                    count = 0;
                    xsw.writeStartElement("books");
                    for(Entry<String, Integer> variantEntry : _typicalWords.mBooksMap.entrySet()) {
                        xsw.writeStartElement("variant");
                            xsw.writeAttribute("id", count.toString());
                            xsw.writeStartElement("value");
                                xsw.writeCharacters(variantEntry.getKey());
                            xsw.writeEndElement();
                            xsw.writeStartElement("count");
                                xsw.writeCharacters(variantEntry.getValue().toString());
                            xsw.writeEndElement();
                        xsw.writeEndElement();
                        count++;
                    }
                    xsw.writeEndElement();
                    count = 0;
                    xsw.writeStartElement("music");
                    for(Entry<String, Integer> variantEntry : _typicalWords.mMusicMap.entrySet()) {
                        xsw.writeStartElement("variant");
                            xsw.writeAttribute("id", count.toString());
                            xsw.writeStartElement("value");
                                xsw.writeCharacters(variantEntry.getKey());
                            xsw.writeEndElement();
                            xsw.writeStartElement("count");
                                xsw.writeCharacters(variantEntry.getValue().toString());
                            xsw.writeEndElement();
                        xsw.writeEndElement();
                        count++;
                    }
                    xsw.writeEndElement();
                    count = 0;
                    xsw.writeStartElement("movies");
                    for(Entry<String, Integer> variantEntry : _typicalWords.mMoviesMap.entrySet()) {
                        xsw.writeStartElement("variant");
                            xsw.writeAttribute("id", count.toString());
                            xsw.writeStartElement("value");
                                xsw.writeCharacters(variantEntry.getKey());
                            xsw.writeEndElement();
                            xsw.writeStartElement("count");
                                xsw.writeCharacters(variantEntry.getValue().toString());
                            xsw.writeEndElement();
                        xsw.writeEndElement();
                        count++;
                    }
                    xsw.writeEndElement();
                    count = 0;
                    xsw.writeStartElement("political");
                    for(Entry<Integer, Integer> variantEntry : _typicalWords.mPoliticalMap.entrySet()) {
                        xsw.writeStartElement("variant");
                            xsw.writeAttribute("id", count.toString());
                            xsw.writeStartElement("value");
                                xsw.writeCharacters(variantEntry.getKey().toString());
                            xsw.writeEndElement();
                            xsw.writeStartElement("count");
                                xsw.writeCharacters(variantEntry.getValue().toString());
                            xsw.writeEndElement();
                        xsw.writeEndElement();
                        count++;
                    }
                    xsw.writeEndElement();
                    count = 0;
                    xsw.writeStartElement("religion");
                    for(Entry<String, Integer> variantEntry : _typicalWords.mReligionMap.entrySet()) {
                        xsw.writeStartElement("variant");
                            xsw.writeAttribute("id", count.toString());
                            xsw.writeStartElement("value");
                                xsw.writeCharacters(variantEntry.getKey());
                            xsw.writeEndElement();
                            xsw.writeStartElement("count");
                                xsw.writeCharacters(variantEntry.getValue().toString());
                            xsw.writeEndElement();
                        xsw.writeEndElement();
                        count++;
                    }
                    xsw.writeEndElement();
                    count = 0;
                    xsw.writeStartElement("inspiredby");
                    for(Entry<String, Integer> variantEntry : _typicalWords.mInspiredByMap.entrySet()) {
                        xsw.writeStartElement("variant");
                            xsw.writeAttribute("id", count.toString());
                            xsw.writeStartElement("value");
                                xsw.writeCharacters(variantEntry.getKey());
                            xsw.writeEndElement();
                            xsw.writeStartElement("count");
                                xsw.writeCharacters(variantEntry.getValue().toString());
                            xsw.writeEndElement();
                        xsw.writeEndElement();
                        count++;
                    }
                    xsw.writeEndElement();
                    count = 0;
                    xsw.writeStartElement("peoplemain");
                    for(Entry<Integer, Integer> variantEntry : _typicalWords.mPeopleMainMap.entrySet()) {
                        xsw.writeStartElement("variant");
                            xsw.writeAttribute("id", count.toString());
                            xsw.writeStartElement("value");
                                xsw.writeCharacters(variantEntry.getKey().toString());
                            xsw.writeEndElement();
                            xsw.writeStartElement("count");
                                xsw.writeCharacters(variantEntry.getValue().toString());
                            xsw.writeEndElement();
                        xsw.writeEndElement();
                        count++;
                    }
                    xsw.writeEndElement();
                    count = 0;
                    xsw.writeStartElement("lifemain");
                    for(Entry<Integer, Integer> variantEntry : _typicalWords.mLifeMainMap.entrySet()) {
                        xsw.writeStartElement("variant");
                            xsw.writeAttribute("id", count.toString());
                            xsw.writeStartElement("value");
                                xsw.writeCharacters(variantEntry.getKey().toString());
                            xsw.writeEndElement();
                            xsw.writeStartElement("count");
                                xsw.writeCharacters(variantEntry.getValue().toString());
                            xsw.writeEndElement();
                        xsw.writeEndElement();
                        count++;
                    }
                    xsw.writeEndElement();
                    count = 0;
                    xsw.writeStartElement("smoking");
                    for(Entry<Integer, Integer> variantEntry : _typicalWords.mSmokingMap.entrySet()) {
                        xsw.writeStartElement("variant");
                            xsw.writeAttribute("id", count.toString());
                            xsw.writeStartElement("value");
                                xsw.writeCharacters(variantEntry.getKey().toString());
                            xsw.writeEndElement();
                            xsw.writeStartElement("count");
                                xsw.writeCharacters(variantEntry.getValue().toString());
                            xsw.writeEndElement();
                        xsw.writeEndElement();
                        count++;
                    }
                    xsw.writeEndElement();
                    count = 0;
                    xsw.writeStartElement("alcohol");
                    for(Entry<Integer, Integer> variantEntry : _typicalWords.mAlcoholMap.entrySet()) {
                        xsw.writeStartElement("variant");
                            xsw.writeAttribute("id", count.toString());
                            xsw.writeStartElement("value");
                                xsw.writeCharacters(variantEntry.getKey().toString());
                            xsw.writeEndElement();
                            xsw.writeStartElement("count");
                                xsw.writeCharacters(variantEntry.getValue().toString());
                            xsw.writeEndElement();
                        xsw.writeEndElement();
                        count++;
                    }
                    xsw.writeEndElement();
                    //count = 0;
                xsw.writeEndElement();
                xsw.writeEndDocument();
            }
            finally {
                if (fileOutputStream != null) {
                    try { fileOutputStream.close() ; } catch(IOException e) { /* ignore */ }      
                }
            }// end inner finally
        }
        finally {
            if (xsw != null) {
                try { xsw.close() ; } catch(XMLStreamException e) { /* ignore */ }    
            }
        }
    }
    
    public static void VKCandidatesToXml(
        List<VKCandidate> _candidatesList
            , String _filePath) throws IOException, XMLStreamException {
        
        XMLStreamWriter xsw = null;
        FileOutputStream fileOutputStream = new FileOutputStream(_filePath);
        
        try {
            
            try {
                
                //Integer count = 1;
                
                XMLOutputFactory xof = XMLOutputFactory.newInstance();
                // If you want pretty-printing, you can use:
                xsw =
                    new IndentingXMLStreamWriter(
                        xof.createXMLStreamWriter(
                            fileOutputStream,
                                "UTF-8"
                        )
                    );
                //xsw = xof.createXMLStreamWriter(out);
                xsw.writeStartDocument("utf-8", "1.0");
                    xsw.writeStartElement("candidates");

                        for(VKCandidate vKCandidate : _candidatesList) {
                            xsw.writeStartElement("candidate");
                                xsw.writeAttribute("id", vKCandidate.getUID().toString());
                                xsw.writeStartElement("fname");
                                    xsw.writeCharacters(
                                        vKCandidate.getFirstName()
                                    );
                                xsw.writeEndElement();
                                xsw.writeStartElement("lname");
                                    xsw.writeCharacters(
                                        vKCandidate.getLastName()
                                    );
                                xsw.writeEndElement();
                                xsw.writeStartElement("score");
                                    xsw.writeCharacters(vKCandidate.getScore().toString());
                                xsw.writeEndElement();
                            xsw.writeEndElement();
                            //count++;
                        }
                    xsw.writeEndElement();
                xsw.writeEndDocument();
            }
            finally {
                if (fileOutputStream != null) {
                    try { fileOutputStream.close() ; } catch(IOException e) { /* ignore */ }      
                }
            }// end inner finally
        }
        finally {
            if (xsw != null) {
                try { xsw.close() ; } catch(XMLStreamException e) { /* ignore */ }    
            }
        }
    }
}
