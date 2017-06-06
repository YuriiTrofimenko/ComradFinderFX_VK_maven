/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.utils;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 *
 * @author Yurii
 */
public class BgLauncher {
    
    private static synchronized Task doInBg(Callable<Void> _func) {
                
        /*Thread th = new Thread() {
            @Override
            public void run() {
                
                try {
                    _func.call();
                } catch (Exception ex) {
                    Logger.getLogger(BgLauncher.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        th.start();*/
        
        return new Task() {
            @Override
            protected Object call() throws Exception {
              for (int i = 0; i < 10; i++) {
                Thread.sleep(2000);
                updateMessage("2000 milliseconds");
                updateProgress(i + 1, 10);
              }
              return true;
            }
        };
    }
}
