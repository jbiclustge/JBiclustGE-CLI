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

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.commons.io.FilenameUtils;

import jbiclustge.methods.algorithms.AbstractBiclusteringAlgorithmCaller;
import jbiclustgecli.syntheticdatasets.components.DatasetPropertyRunInfoContainer;
import jbiclustgecli.syntheticdatasets.components.DatasetRunProperty;
import jbiclustgecli.syntheticdatasets.components.DefaultRecoveryRelevanceResultsContainer;
import pt.ornrocha.threadutils.MTUMultiThreadCallableExecutor;

// TODO: Auto-generated Javadoc
/**
 * The Class MultiThreadEvaluationByMethodExecutorRuntimeLoadData.
 */
public class MultiThreadEvaluationByMethodExecutorRuntimeLoadData {
	
	
	  /** The datasetstoanalyse. */
  	private LinkedHashMap<String, DatasetPropertyRunInfoContainer> datasetstoanalyse;
	  
  	/** The methodstoexecute. */
  	private ArrayList<AbstractBiclusteringAlgorithmCaller> methodstoexecute;
	  
  	/** The simultaneousprocesses. */
  	private int simultaneousprocesses=1;
	  
  	/** The tasks. */
  	private ArrayList<DefaultRecoveryRelevanceAnalysisTaskRuntimeLoadData> tasks;
	  
  	/** The results. */
  	private ArrayList<DefaultRecoveryRelevanceResultsContainer> results;
	  
  	/** The writereport. */
  	private boolean writereport=true;
	  
  	/** The usebiclusterssimple format. */
  	private boolean usebiclusterssimpleFormat=false;
	  
  	/** The writeonly. */
  	private boolean writeonly=false;
	  
  	/** The forcenumberbiclusters. */
  	private boolean forcenumberbiclusters=false;
	  
  	/** The saveresultsdir. */
  	private String saveresultsdir;
	  
	
	  /**
  	 * Instantiates a new multi thread evaluation by method executor runtime load data.
  	 *
  	 * @param methodstoexecute the methodstoexecute
  	 * @param datasetstoanalyse the datasetstoanalyse
  	 * @param simultaneousprocesses the simultaneousprocesses
  	 */
  	public MultiThreadEvaluationByMethodExecutorRuntimeLoadData(ArrayList<AbstractBiclusteringAlgorithmCaller> methodstoexecute, LinkedHashMap<String, DatasetPropertyRunInfoContainer> datasetstoanalyse, int simultaneousprocesses){
		  this.datasetstoanalyse=datasetstoanalyse;
		  this.methodstoexecute=methodstoexecute;
		  if(simultaneousprocesses==-1)
			  this.simultaneousprocesses=methodstoexecute.size();
		  else
			  this.simultaneousprocesses=simultaneousprocesses;
		  
	  }
	  
	  
	  /**
  	 * Use number biclusters to be found of config file.
  	 */
  	public void useNumberBiclustersToBeFoundOfConfigFile(){
		  forcenumberbiclusters=true;
	  }
	  
	  
	  
	  /**
  	 * Sets the save results todirectory.
  	 *
  	 * @param saveresultsdir the new save results todirectory
  	 */
  	public void setSaveResultsTodirectory(String saveresultsdir) {
		this.saveresultsdir = saveresultsdir;
	}


	/**
	 * Sets the number simultaneous analysis.
	 *
	 * @param value the new number simultaneous analysis
	 */
	public void setNumberSimultaneousAnalysis(int value){
		  this.simultaneousprocesses=value;
	  }
	  
	  
	  /**
  	 * Sets the use expected biclusters simple format.
  	 *
  	 * @param biclustformat the new use expected biclusters simple format
  	 */
  	public void setUseExpectedBiclustersSimpleFormat(boolean biclustformat){
		  this.usebiclusterssimpleFormat=biclustformat;
	  }
	  
	  /**
  	 * Sets the writeonly.
  	 *
  	 * @param writeonly the new writeonly
  	 */
  	public void setWriteonly(boolean writeonly) {
			this.writeonly = writeonly;
		  }
	  
	  /**
  	 * Configure tasks.
  	 */
  	private void configureTasks(){
		  
		  tasks=new ArrayList<>();
		  
		  for (int i = 0; i < methodstoexecute.size(); i++) {
			  AbstractBiclusteringAlgorithmCaller instance=methodstoexecute.get(i);
		      
			  for (String category : datasetstoanalyse.keySet()) {
				 
				  DatasetPropertyRunInfoContainer analysedatabycategory=datasetstoanalyse.get(category);
				  
				  ArrayList<DatasetRunProperty> datatoanalyse=analysedatabycategory.getSetstoanalyse();
				  System.out.println(datatoanalyse.size());
				  for (int j = 0; j < datatoanalyse.size(); j++) {
					
				      
					  LinkedHashMap<String, String> datasets2expected=datatoanalyse.get(j).getDatasetsToEvaluate2ExpectedResults();
					  
					  for (String expressionset : datasets2expected.keySet()) {
						  
						  DefaultRecoveryRelevanceAnalysisTaskRuntimeLoadData task=new DefaultRecoveryRelevanceAnalysisTaskRuntimeLoadData(expressionset, instance, datasets2expected.get(expressionset),usebiclusterssimpleFormat,writeonly,forcenumberbiclusters);
						  if(writereport){
							  
							   if(saveresultsdir!=null)
								   task.saveReportAtDirectory(saveresultsdir);
							   else{
								   String parentdir=FilenameUtils.getFullPathNoEndSeparator(expressionset);
								   task.saveReportAtDirectory(parentdir);
							   }
				    		   String datasetname=FilenameUtils.getBaseName(expressionset);
				    		   task.setAssociatedDatasetName(datasetname);
				    	   }
						  tasks.add(task);
					  }
				  }
			  }
		  }
	  }
	  

	  
	  /**
  	 * Execute.
  	 *
  	 * @throws Exception the exception
  	 */
  	public void execute() throws Exception{
		  
		  configureTasks();
		  results=(ArrayList<DefaultRecoveryRelevanceResultsContainer>) MTUMultiThreadCallableExecutor.run(tasks, simultaneousprocesses);
	  }
	  
	  
	  /**
  	 * Gets the results.
  	 *
  	 * @return the results
  	 */
  	public ArrayList<DefaultRecoveryRelevanceResultsContainer> getResults(){
		  return results;
	  }
	

}
