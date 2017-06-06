/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinderfx_vk_maven.modules;

import static java.lang.System.out;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * Заготовка на будущее:
 * загрузчик разметки веб-страницы по указанному адресу
 * (для дальнейшего парсинга при помощи библиотеки Jsoup)
 * 
 * @author Юрий
 */
public class GroupUesersScanner
{        
    private Document getDocumentByUrl(String _urlString){
        
        Document document = null;
    
        try{

            document = Jsoup.connect("http://www.test/").get();
        }catch(IOException e){

            out.println("Error: group page has not loaded");
        }
        
        return document;
    }
}
