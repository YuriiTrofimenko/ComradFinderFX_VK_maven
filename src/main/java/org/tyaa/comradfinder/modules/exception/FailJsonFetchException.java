/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.modules.exception;

/**
 *
 * @author Yurii
 */
public class FailJsonFetchException extends Exception{

    @Override
    public String getMessage() {
        
        return "JSON fetching failed";
    }
}
