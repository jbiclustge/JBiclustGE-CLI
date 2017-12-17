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

import jbiclustge.datatools.expressiondata.dataset.ExpressionData;
import jbiclustge.methods.algorithms.AbstractBiclusteringAlgorithmCaller;
import jbiclustge.results.biclusters.containers.BiclusterList;
import jbiclustgecli.syntheticdatasets.components.IEvaluationResultsContainer;
import pt.ornrocha.logutils.messagecomponents.LogMessageCenter;


// TODO: Auto-generated Javadoc
/**
 * The Class MethodEvaluationExecutor.
 *
 * @param <T> the generic type
 */
public abstract class MethodEvaluationExecutor<T extends IEvaluationResultsContainer> implements Runnable{
	
	  /** The analysedata. */
  	protected ExpressionData analysedata;
	  
  	/** The method. */
  	protected AbstractBiclusteringAlgorithmCaller method;
	  
  	/** The expectedresults. */
  	protected BiclusterList expectedresults;
	  
  	/** The resultscontainer. */
  	protected T resultscontainer;
	  
  	/** The usenumberbicsofexpected. */
  	protected boolean usenumberbicsofexpected=true;
	
	
	  /**
  	 * Instantiates a new method evaluation executor.
  	 *
  	 * @param data the data
  	 * @param method the method
  	 * @param expectedresults the expectedresults
  	 */
  	public MethodEvaluationExecutor(ExpressionData data, AbstractBiclusteringAlgorithmCaller method, BiclusterList expectedresults){
		  this.analysedata=data;
		  this.method=method;
		  this.method.setExpressionData(analysedata);
		  this.expectedresults=expectedresults;
		  
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
		    if(usenumberbicsofexpected)
		    	method.changeNumberBiclusterTobeFound(expectedresults.size());
		    method.run();
		    BiclusterList methodresults=method.getBiclusterResultList();
		    try {
				this.resultscontainer=buildContainerResults(methodresults);
			} catch (Exception e) {
				LogMessageCenter.getLogger().toClass(getClass()).addCriticalErrorMessage("Error in executing "+method.getAlgorithmName(), e);
			}
			
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
