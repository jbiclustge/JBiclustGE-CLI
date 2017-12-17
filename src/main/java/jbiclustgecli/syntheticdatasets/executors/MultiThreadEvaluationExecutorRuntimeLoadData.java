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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.commons.io.FilenameUtils;

import jbiclustge.methods.algorithms.AbstractBiclusteringAlgorithmCaller;
import jbiclustgecli.syntheticdatasets.components.DatasetPropertyRunInfoContainer;
import jbiclustgecli.syntheticdatasets.components.DatasetRunProperty;
import jbiclustgecli.syntheticdatasets.components.DefaultRecoveryRelevanceResultsContainer;
import pt.ornrocha.threadutils.MTUMultiThreadCallableExecutor;
import smile.imputation.MissingValueImputationException;

// TODO: Auto-generated Javadoc
/**
 * The Class MultiThreadEvaluationExecutorRuntimeLoadData.
 */
public class MultiThreadEvaluationExecutorRuntimeLoadData {
	
	
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
	  
	
	  /**
  	 * Instantiates a new multi thread evaluation executor runtime load data.
  	 *
  	 * @param methodstoexecute the methodstoexecute
  	 * @param datasetstoanalyse the datasetstoanalyse
  	 * @param simultaneousprocesses the simultaneousprocesses
  	 */
  	public MultiThreadEvaluationExecutorRuntimeLoadData(ArrayList<AbstractBiclusteringAlgorithmCaller> methodstoexecute, LinkedHashMap<String, DatasetPropertyRunInfoContainer> datasetstoanalyse, int simultaneousprocesses){
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
	 *
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParseException the parse exception
	 * @throws MissingValueImputationException the missing value imputation exception
	 */
	private void configureTasks() throws FileNotFoundException, IOException, ParseException, MissingValueImputationException{

		  tasks=new ArrayList<>();
		  
		   for (String category : datasetstoanalyse.keySet()) {
			   
			   DatasetPropertyRunInfoContainer analysedatabycategory=datasetstoanalyse.get(category);
			  
			   
			   ArrayList<DatasetRunProperty> datatoanalyse=analysedatabycategory.getSetstoanalyse();
			  
			   for (int i = 0; i < datatoanalyse.size(); i++) {
				  
				   LinkedHashMap<String, String> datasets2expected=datatoanalyse.get(i).getDatasetsToEvaluate2ExpectedResults();
				 
				   
				   for (String expressionset : datasets2expected.keySet()) {
					
				       //BiclusterList expected=BiclusterList.importBiclustersFromTxtFile(dataset, datasets2expected.get(expressionset), " ", true);
				   
				       for (int j = 0; j < methodstoexecute.size(); j++) {
						
				    	   AbstractBiclusteringAlgorithmCaller instance=methodstoexecute.get(j);
				    	  
				    	   DefaultRecoveryRelevanceAnalysisTaskRuntimeLoadData task=new DefaultRecoveryRelevanceAnalysisTaskRuntimeLoadData(expressionset, instance, datasets2expected.get(expressionset),usebiclusterssimpleFormat,writeonly,forcenumberbiclusters);
				    	   if(writereport){
				    		   String parentdir=FilenameUtils.getFullPathNoEndSeparator(expressionset);
				    		   task.saveReportAtDirectory(parentdir);
				    		   String datasetname=FilenameUtils.getBaseName(expressionset);
				    		   task.setAssociatedDatasetName(datasetname);
				    	   }
				    	   //instance.setExpressionData(dataset);
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
