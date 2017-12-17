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
package jbiclustgecli.processcontrolcenters.gsea;

import java.util.ArrayList;

import jbiclustge.datatools.expressiondata.dataset.ExpressionData;
import jbiclustge.propertiesmodules.components.GSEAInfoContainer;
import jbiclustgecli.processcontrolcenters.biclustermethods.BiclusteringMethodsExecutionControlCenter;
import jbiclustgecli.processcontrolcenters.tasks.BiclusteringMethodRunTask;
import jbiclustgecli.processcontrolcenters.tasks.BiclusteringMethodRunWithGSEATask;

// TODO: Auto-generated Javadoc
/**
 * The Class BiclusteringMethodsExecutionWithGSEAControlCenter.
 */
public class BiclusteringMethodsExecutionWithGSEAControlCenter extends BiclusteringMethodsExecutionControlCenter{

	
	/** The infogsea. */
	private GSEAInfoContainer infogsea;
	
	/**
	 * Instantiates a new biclustering methods execution with GSEA control center.
	 *
	 * @param data the data
	 * @param saveresultsindir the saveresultsindir
	 */
	public BiclusteringMethodsExecutionWithGSEAControlCenter(ExpressionData data, String saveresultsindir) {
		super(data, saveresultsindir);
	}
	
	
	/**
	 * Instantiates a new biclustering methods execution with GSEA control center.
	 *
	 * @param data the data
	 * @param infogsea the infogsea
	 * @param saveresultsindir the saveresultsindir
	 */
	public BiclusteringMethodsExecutionWithGSEAControlCenter(ExpressionData data,GSEAInfoContainer infogsea, String saveresultsindir) {
		super(data, saveresultsindir);
		this.infogsea=infogsea;
	}
	
	
	
	
	
	/* (non-Javadoc)
	 * @see processcontrolcenters.biclustermethods.BiclusteringMethodsExecutionControlCenter#getListProcessesToRun()
	 */
	@Override
	protected ArrayList<Runnable> getListProcessesToRun() {
		ArrayList<Runnable> methodtasks=new ArrayList<>();
		
		for (int i = 0; i < biclustmethods.size(); i++) {
			
			BiclusteringMethodRunTask runmethod=null;
			
			if(infogsea==null)
				runmethod=new BiclusteringMethodRunTask(biclustmethods.get(i), saveresultsindir);
			else
				runmethod=new BiclusteringMethodRunWithGSEATask(biclustmethods.get(i),infogsea, saveresultsindir);
			
			
			if(saveheatmaps)
				runmethod.saveHeatmaps();
			if(saveparallelcoord)
				runmethod.saveParallelCoordinates();
			
			methodtasks.add(runmethod);
		}
		return methodtasks;
	}
	
	
}
