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

import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import jbiclustge.methods.algorithms.AbstractBiclusteringAlgorithmCaller;
import jbiclustge.propertiesmodules.PropertyLabels;
import jbiclustge.results.biclusters.BiclusterOutputFormat;
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
	protected AbstractBiclusteringAlgorithmCaller method;
	
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
	
	protected int numberruns=1;
	
	protected double filterbyoverlap=-1;
	protected int filternumberbicsbyoverlap=-1;
	
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
	
	public BiclusteringMethodRunTask(AbstractBiclusteringAlgorithmCaller method, String saveresultstodir, Integer numberruns){
		this(method, saveresultstodir);
		if(numberruns!=null)
			this.numberruns=numberruns;
	}
	
	public void setProperties(Map<String, Object> props) {
		if(props!=null) {
			if(props.containsKey(PropertyLabels.OVERLAPFILTERING)) {
				filterbyoverlap=(double) props.get(PropertyLabels.OVERLAPFILTERING);
				
				if(props.containsKey(PropertyLabels.FILTEROVERLAPNUMBERBICS))
					filternumberbicsbyoverlap=(int) props.get(PropertyLabels.FILTEROVERLAPNUMBERBICS);
			}
			if(props.containsKey(PropertyLabels.MAKEPARALLELCOORD))
				makeparallelcoord=(boolean) props.get(PropertyLabels.MAKEPARALLELCOORD);
			if(props.containsKey(PropertyLabels.MAKEHEATMAP))
				makeheatmaps=(boolean) props.get(PropertyLabels.MAKEHEATMAP);
			
		}
	}
	

	
	public void filterByOverlap(double value) {
		this.filterbyoverlap=value;
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
	

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		for (int i = 0; i < numberruns; i++) {
	        executeBiclusteringAlgorithm();
	        method.reset();
		}
	}
	
	
	protected void executeBiclusteringAlgorithm() {

		method.run();
		results=method.getBiclusterResultList();

		if(saveindir!=null){
			resultsfilepath= MTUDirUtils.makeDirectoryWithUniqueIDAndDate(saveindir, method.getAlgorithmName());
		}
		else{
			resultsfilepath= MTUDirUtils.makeDirectoryWithUniqueIDAndDate(SystemFolderTools.checkAndSetJBiclusteGEWorkingDir(), method.getAlgorithmName());
		}

		try {
			if(results.size()>0) {
				results.writeBiclusterListToJBiclustGEOutputFormat(resultsfilepath);
				if(filterbyoverlap>=0.0 && filterbyoverlap<=1.0) {
					BiclusterList filtered=results.filterByOverlapTreshold(filternumberbicsbyoverlap, filterbyoverlap);
					filtered.writeBiclusterListToJBiclustGEOutputFormat(resultsfilepath, BiclusterOutputFormat.JBiclustGE_CSV.getName()+"_overlap_"+filterbyoverlap+"_filtered");
				}
			}
		} catch (Exception e) {
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
	
	
	

}
