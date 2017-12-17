/************************************************************************** 
 * Copyright 2017
 *
 * University of Minho 
 * 
 * This is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This code is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Public License for more details. 
 * 
 * You should have received a copy of the GNU Public License 
 * along with this code. If not, see http://www.gnu.org/licenses/ 
 *  
 * Created by Orlando Rocha (ornrocha@gmail.com) inside BIOSYSTEMS Group (https://www.ceb.uminho.pt/BIOSYSTEMS)
 */
package jbiclustgecli.syntheticdatasets.components;

import jbiclustge.results.biclusters.containers.BiclusterList;

// TODO: Auto-generated Javadoc
/**
 * The Interface IEvaluationResultsContainer.
 */
public interface IEvaluationResultsContainer {

	
	/**
	 * Gets the recovery.
	 *
	 * @return the recovery
	 */
	double getRecovery();
	
	/**
	 * Gets the relevance.
	 *
	 * @return the relevance
	 */
	double getRelevance();
	
	/**
	 * Gets the running time.
	 *
	 * @return the running time
	 */
	String getRunningTime();
	
	/**
	 * Gets the results.
	 *
	 * @return the results
	 */
	BiclusterList getResults();
	
}
