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
package jbiclustgecli.syntheticdatasets.executors;

import java.util.concurrent.Callable;

import jbiclustge.methods.algorithms.AbstractBiclusteringAlgorithmCaller;
import jbiclustgecli.syntheticdatasets.components.DefaultRecoveryRelevanceResultsContainer;
import jbiclustgecli.syntheticdatasets.evaluators.DefaultRecoveryRelevanceEvaluationExecutorRuntimeLoadDataset;



// TODO: Auto-generated Javadoc
/**
 * The Class DefaultRecoveryRelevanceAnalysisTaskRuntimeLoadData.
 */
public class DefaultRecoveryRelevanceAnalysisTaskRuntimeLoadData implements Callable<DefaultRecoveryRelevanceResultsContainer>{

	
	/** The thread. */
	private DefaultRecoveryRelevanceEvaluationExecutorRuntimeLoadDataset thread;
	
	
	/**
	 * Instantiates a new default recovery relevance analysis task runtime load data.
	 *
	 * @param expressiondata the expressiondata
	 * @param method the method
	 * @param expectedresults the expectedresults
	 * @param issimplebiclustformat the issimplebiclustformat
	 * @param writeonly the writeonly
	 * @param forcenumberbiclusterofconfig the forcenumberbiclusterofconfig
	 */
	public DefaultRecoveryRelevanceAnalysisTaskRuntimeLoadData(String expressiondata, AbstractBiclusteringAlgorithmCaller method,String expectedresults,boolean issimplebiclustformat, boolean writeonly, boolean forcenumberbiclusterofconfig){
		this.thread=new DefaultRecoveryRelevanceEvaluationExecutorRuntimeLoadDataset(expressiondata, method, expectedresults,issimplebiclustformat,writeonly);
		if(forcenumberbiclusterofconfig)
			thread.forceNumberOfBiclustersDefinedInConfiguration();
	
	}
	
	
	/**
	 * Save report at directory.
	 *
	 * @param dir the dir
	 */
	public void saveReportAtDirectory(String dir){
		thread.setFileToMakeResultsReport(dir);
	}
	
	
	/**
	 * Sets the associated dataset name.
	 *
	 * @param name the new associated dataset name
	 */
	public void setAssociatedDatasetName(String name){
		thread.setAssociatedDatasetName(name);
	}
	
	
	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public DefaultRecoveryRelevanceResultsContainer call() throws Exception {
		thread.run();
		return thread.getResults();
	}

}
