/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.modules.events;

import java.util.ArrayList;
import java.util.List;
import org.tyaa.comradfinder.modules.events.interfaces.UpdateCandidatesListener;

/**
 *
 * @author Юрий
 */
public class UpdateCandidatesGenerator
{
    private List<UpdateCandidatesListener> listeners = new ArrayList<>();

    public synchronized void addListener(UpdateCandidatesListener _toAddListener) {
        
        listeners.add(_toAddListener);
    }
    
    public synchronized void removeListener(UpdateCandidatesListener _toRemoveListener) {
        
        listeners.remove(_toRemoveListener);
    }
    
    public void fire(){
        
        UpdateCandidatesEvent e = new UpdateCandidatesEvent(this);
        // Notify everybody that may be interested.
        for (UpdateCandidatesListener l : listeners)
            l.handleUpdateCandidatesEvent(e);
    }
}
