/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tyaa.comradfinder.modules.interfaces;

import java.io.IOException;
import java.util.List;
import org.tyaa.comradfinder.model.VKCandidate;

/**
 *
 * Интерфейс сохранения результатов
 * (реализации можно осуществлять на базе различных технологий)
 * 
 * @author Юрий
 */
public interface IResultSaver {
    
    boolean saveCandidates(List<VKCandidate> candidatesList
        , String _filePath) throws IOException;
    
    boolean saveCandidates(List<VKCandidate> candidatesList) throws IOException;
}
