/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinderfx_vk_maven.utils;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yurii
 */
public class BgLauncher {
    
    private static synchronized void doInBg(Callable<Void> _func) {
                
        Thread th = new Thread() {
            @Override
            public void run() {
                
                try {
                    _func.call();
                } catch (Exception ex) {
                    Logger.getLogger(BgLauncher.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        th.start();
    }
}
