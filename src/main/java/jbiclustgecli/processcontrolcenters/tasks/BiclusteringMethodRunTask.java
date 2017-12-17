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
package jbiclustgecli.processcontrolcenters.tasks;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

import jbiclustge.methods.algorithms.AbstractBiclusteringAlgorithmCaller;
import jbiclustge.results.biclusters.containers.BiclusterList;
import jbiclustge.rtools.plotutils.BiclusterPlotUtils;
import jbiclustge.utils.osystem.SystemFolderTools;
import pt.ornrocha.fileutils.MTUDirUtils;
import pt.ornrocha.logutils.messagecomponents.LogMessageCenter;

// TODO: Auto-generated Javadoc
/**
 * The Class BiclusteringMethodRunTask.
 */
public class BiclusteringMethodRunTask implements Runnable{

	
	/** The method. */
	private AbstractBiclusteringAlgorithmCaller method;
	
	/** The saveindir. */
	protected String saveindir;
	
	/** The resultsfilepath. */
	protected String resultsfilepath;
	
	/** The results. */
	private BiclusterList results;
	
	/** The makeparallelcoord. */
	private boolean makeparallelcoord=false;
	
	/** The makeheatmaps. */
	private boolean makeheatmaps=false;
	
	/**
	 * Instantiates a new biclustering method run task.
	 *
	 * @param method the method
	 */
	public BiclusteringMethodRunTask(AbstractBiclusteringAlgorithmCaller method){
		this.method=method;
	}
	
	/**
	 * Instantiates a new biclustering method run task.
	 *
	 * @param method the method
	 * @param saveresultstodir the saveresultstodir
	 */
	public BiclusteringMethodRunTask(AbstractBiclusteringAlgorithmCaller method, String saveresultstodir){
		this.method=method;
		this.saveindir=saveresultstodir;
	}
	
	
	/**
	 * Save parallel coordinates.
	 */
	public void saveParallelCoordinates(){
		makeparallelcoord=true;
	}
	
	/**
	 * Save heatmaps.
	 */
	public void saveHeatmaps(){
		makeheatmaps=true;
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		method.run();
		results=method.getBiclusterResultList();
		
		if(saveindir!=null){
		    resultsfilepath= MTUDirUtils.makeDirectoryWithDate(saveindir, method.getAlgorithmName());
		}
		else{
			resultsfilepath= MTUDirUtils.makeDirectoryWithDate(SystemFolderTools.checkAndSetJBiclusteGEWorkingDir(), method.getAlgorithmName());
		}
		
		try {
			results.writeBiclusterListToJBiclustGEOutputFormat(resultsfilepath);
		} catch (IOException e) {
			LogMessageCenter.getLogger().addCriticalErrorMessage("Error saving results: ", e);
		}
		
		if(results!=null && results.size()>0){
			
			if(makeheatmaps){
				String dirheatmaps=FilenameUtils.concat(resultsfilepath, "heatmaps");
				MTUDirUtils.checkandsetDirectory(dirheatmaps);
				BiclusterPlotUtils.makeAndSaveListBiclustersPlotHeatmap(results, true, true, dirheatmaps);
			}
			
			if(makeparallelcoord){
				String dirparallelcoord=FilenameUtils.concat(resultsfilepath, "parallelcoordinates");
				MTUDirUtils.checkandsetDirectory(dirparallelcoord);
				BiclusterPlotUtils.makeAndSaveListBiclustersPlotParallelCoordinates(results, false, true,true, dirparallelcoord);
			}
			
		}
	}
	
	/**
	 * Gets the results file path.
	 *
	 * @return the results file path
	 */
	public String getResultsFilePath(){
		return resultsfilepath;
	}

	/**
	 * Gets the results.
	 *
	 * @return the results
	 */
	public BiclusterList getResults() {
		return results;
	}
	
	

}
