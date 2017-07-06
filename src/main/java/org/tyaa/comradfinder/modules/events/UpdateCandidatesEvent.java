/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.modules.events;

import java.util.EventObject;
import java.util.List;
import org.tyaa.comradfinder.model.VKCandidate;

/**
 *
 * @author Юрий
 */
public class UpdateCandidatesEvent extends EventObject
{
    public List<VKCandidate> vKCandidatesList;
    
    public UpdateCandidatesEvent(Object source)
    {
        super(source);
    }
    
}
