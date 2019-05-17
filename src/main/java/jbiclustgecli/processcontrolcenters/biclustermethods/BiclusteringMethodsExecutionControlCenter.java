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
package jbiclustgecli.processcontrolcenters.biclustermethods;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import jbiclustge.datatools.expressiondata.dataset.ExpressionData;
import jbiclustgecli.processcontrolcenters.AbstractBiclusteringProcessCLIControlCenter;
import jbiclustgecli.processcontrolcenters.tasks.BiclusteringMethodRunTask;
import pt.ornrocha.fileutils.MTUDirUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class BiclusteringMethodsExecutionControlCenter.
 */
public class BiclusteringMethodsExecutionControlCenter extends  AbstractBiclusteringProcessCLIControlCenter{

	/** The saveresultsindir. */
	protected String saveresultsindir;
	
	/** The saveheatmaps. */
	/*protected boolean saveheatmaps=false;
	
	*//** The saveparallelcoord. *//*
	protected boolean saveparallelcoord=false;*/
	


	/**
	 * Instantiates a new biclustering methods execution control center.
	 *
	 * @param data the data
	 * @param saveresultsindir the saveresultsindir
	 */
	public BiclusteringMethodsExecutionControlCenter(ExpressionData data, String saveresultsindir) {
		super(data);
		this.saveresultsindir=saveresultsindir;
	}
	
	/**
	 * Save bicluster heatmap.
	 *//*
	public void saveBiclusterHeatmap(){
		this.saveheatmaps=true;
	}
	
	*//**
	 * Save bicluster parallel coord.
	 *//*
	public void saveBiclusterParallelCoord(){
		this.saveparallelcoord=true;
	}*/
	
	
	


	/* (non-Javadoc)
	 * @see processcontrolcenters.AbstractBiclusteringProcessCLIControlCenter#getListProcessesToRun()
	 */
	@Override
	protected ArrayList<Runnable> getListProcessesToRun() {
		
		ArrayList<Runnable> methodtasks=new ArrayList<>();

		for (int i = 0; i < biclustmethods.size(); i++) {
			
			Integer numberprocs=null;
			if(runmap!=null && runmap.containsKey(biclustmethods.get(i)))
				numberprocs=runmap.get(biclustmethods.get(i));
					
			BiclusteringMethodRunTask runmethod=new BiclusteringMethodRunTask(biclustmethods.get(i), getFolderOfResults(i),numberprocs);
			runmethod.setProperties(props);
			/*if(saveheatmaps)
				runmethod.saveHeatmaps();
			if(saveparallelcoord)
				runmethod.saveParallelCoordinates();*/
			
			methodtasks.add(runmethod);
		}
		
		return methodtasks;
	}
	
	protected String getFolderOfResults(int index) {
		
		if(confignamelist!=null && index<=confignamelist.size()) {
			String configname=confignamelist.get(index);
			String folderpath=FilenameUtils.concat(saveresultsindir, configname);
			MTUDirUtils.checkDirectory(folderpath);
			return folderpath;
		}
		else
			return saveresultsindir;
	}

}
