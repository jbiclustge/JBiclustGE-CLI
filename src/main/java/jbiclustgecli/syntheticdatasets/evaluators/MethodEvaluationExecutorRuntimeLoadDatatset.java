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
package jbiclustgecli.syntheticdatasets.evaluators;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

import jbiclustge.datatools.expressiondata.dataset.ExpressionData;
import jbiclustge.methods.algorithms.AbstractBiclusteringAlgorithmCaller;
import jbiclustge.results.biclusters.containers.BiclusterList;
import jbiclustgecli.syntheticdatasets.components.IEvaluationResultsContainer;
import pt.ornrocha.ioutils.writers.MTUWriterUtils;
import pt.ornrocha.logutils.messagecomponents.LogMessageCenter;


// TODO: Auto-generated Javadoc
/**
 * The Class MethodEvaluationExecutorRuntimeLoadDatatset.
 *
 * @param <T> the generic type
 */
public abstract class MethodEvaluationExecutorRuntimeLoadDatatset<T extends IEvaluationResultsContainer> implements Runnable{
	
	  /** The analysedata. */
  	protected String analysedata;
	  
  	/** The method. */
  	protected AbstractBiclusteringAlgorithmCaller method;
	  
  	/** The expectedresultsfile. */
  	protected String expectedresultsfile;
	  
  	/** The expectedresults. */
  	protected BiclusterList expectedresults;
	  
  	/** The isbiclustsimple format. */
  	protected boolean isbiclustsimpleFormat=false;
	  
  	/** The resultscontainer. */
  	protected T resultscontainer;
	  
  	/** The usenumberbicsofexpected. */
  	protected boolean usenumberbicsofexpected=true;
	
	
	  /**
  	 * Instantiates a new method evaluation executor runtime load datatset.
  	 *
  	 * @param expressiondata the expressiondata
  	 * @param method the method
  	 * @param expectedresults the expectedresults
  	 * @param issimplebiclustformat the issimplebiclustformat
  	 */
  	public MethodEvaluationExecutorRuntimeLoadDatatset(String expressiondata, AbstractBiclusteringAlgorithmCaller method, String expectedresults, boolean issimplebiclustformat){
		  this.analysedata=expressiondata;
		  this.method=method.copyIntance();
		  this.expectedresultsfile=expectedresults;
		  this.isbiclustsimpleFormat=issimplebiclustformat;
		  
	  }
	  
	  /**
  	 * Force number of biclusters defined in configuration.
  	 */
  	public void forceNumberOfBiclustersDefinedInConfiguration(){
		  usenumberbicsofexpected=false;
	  }
	

	  /**
  	 * Builds the container results.
  	 *
  	 * @param obtainedresults the obtainedresults
  	 * @return the t
  	 * @throws Exception the exception
  	 */
  	protected abstract T buildContainerResults(BiclusterList obtainedresults)throws Exception;

	  
	  /* (non-Javadoc)
  	 * @see java.lang.Runnable#run()
  	 */
  	@Override
		public void run() {
		   
		  System.out.println("Running method: "+ method.getAlgorithmName()+"  Expression dataset: "+analysedata+"  expected results: "+expectedresultsfile);
		  
		  
		    
		   try {
		    ExpressionData data=ExpressionData.importFromTXTFormat(analysedata).load();
		    if(isbiclustsimpleFormat)
		    	expectedresults=BiclusterList.importBiclustersFromTxtFile(data, expectedresultsfile, " ", true);	
		    else
		    	expectedresults=BiclusterList.importBiclustersFromBiclustRPackageOutputFormat(expectedresultsfile,data);
		    
		    if(usenumberbicsofexpected)
		    	method.changeNumberBiclusterTobeFound(expectedresults.size());
		    method.setExpressionData(data);
		    method.run();
		    BiclusterList methodresults=method.getBiclusterResultList();
		   
				this.resultscontainer=buildContainerResults(methodresults);
			} catch (Exception e) {
				LogMessageCenter.getLogger().toClass(getClass()).addCriticalErrorMessage("Error in executing "+method.getAlgorithmName(), e);
			}
		   
		   String dataname=FilenameUtils.getBaseName(analysedata);
		   String parentpath=FilenameUtils.getFullPathNoEndSeparator(analysedata);
		   String dataanalysed=FilenameUtils.getBaseName(parentpath);
		   
		   String filenameanalysed=FilenameUtils.concat(parentpath, "AA_Executed_Methods_In_"+dataanalysed+".csv");
		   String analysed=method.getAlgorithmName()+"\t"+dataname+"\t"+FilenameUtils.getBaseName(expectedresultsfile)+"\n";
		   try {
			MTUWriterUtils.appendDataTofile(filenameanalysed, analysed);
		   } catch (IOException e) {}
			
	    }
	  
	  
	  /**
  	 * Gets the results.
  	 *
  	 * @return the results
  	 */
  	public T getResults(){
		  return resultscontainer;
	  }
}
