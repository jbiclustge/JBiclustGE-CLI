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
 * The Class DefaultRecoveryRelevanceResultsContainer.
 */
public class DefaultRecoveryRelevanceResultsContainer implements IEvaluationResultsContainer{

	 /** The recovery. */
 	private double recovery;
	 
 	/** The relevance. */
 	private double relevance;
	 
 	/** The result. */
 	private BiclusterList result;
	
	 /**
 	 * Instantiates a new default recovery relevance results container.
 	 *
 	 * @param recovery the recovery
 	 * @param relevance the relevance
 	 * @param results the results
 	 */
 	public DefaultRecoveryRelevanceResultsContainer(double recovery, double relevance, BiclusterList results){
		 this.recovery=recovery;
		 this.relevance=relevance;
		 this.result=results;
	 }
	
	
	/* (non-Javadoc)
	 * @see jbiclustgecli.syntheticdatasets.components.IEvaluationResultsContainer#getRecovery()
	 */
	@Override
	public double getRecovery() {
		return recovery;
	}

	/* (non-Javadoc)
	 * @see jbiclustgecli.syntheticdatasets.components.IEvaluationResultsContainer#getRelevance()
	 */
	@Override
	public double getRelevance() {
		return relevance;
	}

	/* (non-Javadoc)
	 * @see jbiclustgecli.syntheticdatasets.components.IEvaluationResultsContainer#getRunningTime()
	 */
	@Override
	public String getRunningTime() {
		return result.getMethodRunningTime();
	}


	/* (non-Javadoc)
	 * @see jbiclustgecli.syntheticdatasets.components.IEvaluationResultsContainer#getResults()
	 */
	@Override
	public BiclusterList getResults() {
		return result;
	}
	
	
	

}
