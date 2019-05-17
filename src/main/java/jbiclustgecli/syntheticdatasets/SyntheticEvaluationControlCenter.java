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
package jbiclustgecli.syntheticdatasets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import jbiclustge.methods.algorithms.AbstractBiclusteringAlgorithmCaller;
import jbiclustge.propertiesmodules.PropertiesModules;
import jbiclustge.propertiesmodules.PropertyLabels;
import jbiclustge.utils.props.AlgorithmProperties;
import jbiclustgecli.syntheticdatasets.components.DatasetPropertyRunInfoContainer;
import jbiclustgecli.syntheticdatasets.executors.MultiThreadEvaluationByMethodExecutorRuntimeLoadData;
import jbiclustgecli.syntheticdatasets.executors.MultiThreadEvaluationExecutorRuntimeLoadData;
import pt.ornrocha.fileutils.MTUDirUtils;
import pt.ornrocha.logutils.messagecomponents.LogMessageCenter;
import pt.ornrocha.propertyutils.PropertiesUtilities;


// TODO: Auto-generated Javadoc
/**
 * The Class SyntheticEvaluationControlCenter.
 */
public class SyntheticEvaluationControlCenter {
	
	   /** The runmethods. */
   	private ArrayList<AbstractBiclusteringAlgorithmCaller> runmethods;
	   
   	/** The datasetsdirpaths. */
   	private LinkedHashMap<String, ArrayList<String>> datasetsdirpaths;
	   
   	/** The datasetstoanalyse. */
   	private LinkedHashMap<String, DatasetPropertyRunInfoContainer> datasetstoanalyse;

	   /** The executebymethod. */
   	private boolean executebymethod=false;
	   
   	/** The simultaneousprocesses. */
   	private int simultaneousprocesses=1;
	   
   	/** The usebiclusters simple format. */
   	private boolean usebiclustersSimpleFormat=false;
	   
   	/** The writeonly. */
   	private boolean writeonly=false;
	   
   	/** The forcenumberbiclusters. */
   	private boolean forcenumberbiclusters=false;
	   
   	/** The props. */
   	private Properties props;

	
	   /**
   	 * Instantiates a new synthetic evaluation control center.
   	 *
   	 * @param datasetfolder the datasetfolder
   	 */
   	public SyntheticEvaluationControlCenter(String datasetfolder){
		   try {
			loadMethodsByConfiguration(FilenameUtils.concat(datasetfolder, "algorithms"));
			loadDatasetInformation(datasetfolder);
			File f=new File(FilenameUtils.concat(datasetfolder, "synthetic_run_config.conf"));
			if(f.exists())
				props=PropertiesUtilities.loadFileProperties(f.getAbsolutePath());
			
		} catch (IOException e) {
			LogMessageCenter.getLogger().toClass(getClass()).addCriticalErrorMessage("Error loading configuarions", e);
		}   
	   }
	
	
	   
	   /**
   	 * Load methods by configuration.
   	 *
   	 * @param dir the dir
   	 * @throws FileNotFoundException the file not found exception
   	 * @throws IOException Signals that an I/O exception has occurred.
   	 */
   	private void loadMethodsByConfiguration(String dir) throws FileNotFoundException, IOException{
		   this.runmethods=AlgorithmProperties.loadBiclusterMethodsFromConfigurationsInDirectory(dir);
	   }
	
	  /**
  	 * Sets the simultaneous processes.
  	 *
  	 * @param value the new simultaneous processes
  	 */
  	public void setSimultaneousProcesses(int value){
		  this.simultaneousprocesses=value;
	  }

	  
	  /**
  	 * Sets the execute by method.
  	 *
  	 * @param value the new execute by method
  	 */
  	public void setExecuteByMethod(boolean value){
		  this.executebymethod=value;
	  }
	  
	  /**
  	 * Sets the use expected biclusters simple format.
  	 *
  	 * @param biclustformat the new use expected biclusters simple format
  	 */
  	public void setUseExpectedBiclustersSimpleFormat(boolean biclustformat){
		  this.usebiclustersSimpleFormat=biclustformat;
	  }
	  
	  /**
  	 * Use number biclusters to be found of config file.
  	 */
  	public void useNumberBiclustersToBeFoundOfConfigFile(){
		  forcenumberbiclusters=true;
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
  	 * Load properties.
  	 */
  	private void loadProperties() {
		  simultaneousprocesses=Integer.parseInt(props.getProperty(PropertyLabels.SIMULTANEOUSPROCESSES));
		  if(props.containsKey("analysis_by_method")) {
			  String option=props.getProperty("analysis_by_method");
			  if(option.toLowerCase().equals("true"))
				  executebymethod=true;
		  }
	  }


	/**
	 * Load dataset information.
	 *
	 * @param datasetsfolder the datasetsfolder
	 */
	private void loadDatasetInformation(String datasetsfolder){
		   datasetsdirpaths=new LinkedHashMap<>();
		   
		   ArrayList<String> ignoredirname=new ArrayList<>();
		   ignoredirname.add("Results");
		   ignoredirname.add("algorithms");
		   
		   ArrayList<String> folders=MTUDirUtils.getLastDirectoryInTree(datasetsfolder,ignoredirname);

		   if(folders.size()>0){
			   
			   for (int i = 0; i < folders.size(); i++) {
				   String parentpath=FilenameUtils.getFullPathNoEndSeparator(folders.get(i));
				   String category=FilenameUtils.getBaseName(parentpath);
				   if(datasetsdirpaths.containsKey(category)){
					   datasetsdirpaths.get(category).add(folders.get(i));
				   }
				   else{
					   ArrayList<String> subfolders=new ArrayList<>();
					   subfolders.add(folders.get(i));
					   datasetsdirpaths.put(category, subfolders);
				   }
			   } 
		   }
		   else{
			   String parentpath=FilenameUtils.getFullPathNoEndSeparator(datasetsfolder);
			   String category=FilenameUtils.getBaseName(parentpath);
			   ArrayList<String> subfolders=new ArrayList<>();
			   subfolders.add(datasetsfolder);
			   datasetsdirpaths.put(category, subfolders);
		   }
			   
		  
		 System.out.println(datasetsdirpaths);
	  }
	  
	 
	  
	  
	  /**
  	 * Load datasets to execute.
  	 */
  	private void loadDatasetsToExecute(){
		  datasetstoanalyse=new LinkedHashMap<>();
		  for (String categoryid : datasetsdirpaths.keySet()) {
			  
			  ArrayList<String> dirs=datasetsdirpaths.get(categoryid); 
              
			  datasetstoanalyse.put(categoryid, new DatasetPropertyRunInfoContainer(dirs));
			  
		  }
		  
		  //datasetstoanalyse.get("constant").print();
	  }
	
	
	
	  /**
  	 * Run.
  	 *
  	 * @throws Exception the exception
  	 */
  	public void run() throws Exception{
		  
		  loadDatasetsToExecute();
		  if(props!=null)
			  loadProperties();
		  
		  
		 if(!executebymethod){

			  MultiThreadEvaluationExecutorRuntimeLoadData executor=new MultiThreadEvaluationExecutorRuntimeLoadData(runmethods, datasetstoanalyse, simultaneousprocesses);
			  executor.setUseExpectedBiclustersSimpleFormat(usebiclustersSimpleFormat);
			  if(forcenumberbiclusters)
				  executor.useNumberBiclustersToBeFoundOfConfigFile();
			  executor.setWriteonly(writeonly);
			  executor.execute();
		  }
		  else{

			  MultiThreadEvaluationByMethodExecutorRuntimeLoadData executor=new MultiThreadEvaluationByMethodExecutorRuntimeLoadData(runmethods, datasetstoanalyse, simultaneousprocesses);
			  if(forcenumberbiclusters)
				  executor.useNumberBiclustersToBeFoundOfConfigFile();
			  executor.setUseExpectedBiclustersSimpleFormat(usebiclustersSimpleFormat);
			  executor.setWriteonly(writeonly);
			  executor.execute();
		  }
 
	  }
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		
		
		/*EvaluationControlCenter center=new EvaluationControlCenter("/home/orocha/discodados/Biclustering/datasets/mytestes/new_confs", 
																	"/home/orocha/discodados/Biclustering/datasets/mytestes/constant-up", DatasetEvaluationType.constantup);*/
		//center.run();

	}

}
