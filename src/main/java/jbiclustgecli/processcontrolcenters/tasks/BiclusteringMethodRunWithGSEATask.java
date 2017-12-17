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

import org.apache.commons.io.FilenameUtils;

import jbiclustge.enrichmentanalysistools.common.EnrichmentAnalyserProcessor;
import jbiclustge.enrichmentanalysistools.common.EnrichmentAnalysisResultList;
import jbiclustge.methods.algorithms.AbstractBiclusteringAlgorithmCaller;
import jbiclustge.propertiesmodules.components.GSEAInfoContainer;
import jbiclustge.reporters.BiclusteringGSEATaskCSVReporter;
import jbiclustge.results.biclusters.containers.BiclusterList;
import pt.ornrocha.fileutils.MTUDirUtils;
import pt.ornrocha.logutils.messagecomponents.LogMessageCenter;

// TODO: Auto-generated Javadoc
/**
 * The Class BiclusteringMethodRunWithGSEATask.
 */
public class BiclusteringMethodRunWithGSEATask extends BiclusteringMethodRunTask{

	/** The infogsea. */
	private GSEAInfoContainer infogsea;
	
	
	/**
	 * Instantiates a new biclustering method run with GSEA task.
	 *
	 * @param method the method
	 * @param gseainfo the gseainfo
	 * @param saveresultstodir the saveresultstodir
	 */
	public BiclusteringMethodRunWithGSEATask(AbstractBiclusteringAlgorithmCaller method,GSEAInfoContainer gseainfo, String saveresultstodir) {
		super(method, saveresultstodir);
		this.infogsea=gseainfo;
	}
	
	/* (non-Javadoc)
	 * @see processcontrolcenters.tasks.BiclusteringMethodRunTask#saveParallelCoordinates()
	 */
	public void saveParallelCoordinates(){
		super.saveParallelCoordinates();
	}
	
	/* (non-Javadoc)
	 * @see processcontrolcenters.tasks.BiclusteringMethodRunTask#saveHeatmaps()
	 */
	public void saveHeatmaps(){
		super.saveHeatmaps();
	}
	

	/* (non-Javadoc)
	 * @see processcontrolcenters.tasks.BiclusteringMethodRunTask#run()
	 */
	@Override
	public void run() {
		super.run();
		if(infogsea.getEnrichmentanalyser()!=null){
			EnrichmentAnalyserProcessor currentprocessor=infogsea.getEnrichmentanalyser().copyWorkingInstance();
			BiclusterList results=super.getResults();
			currentprocessor.setBiclusteringResultsToAnalyse(results);
			try {
				currentprocessor.run();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			EnrichmentAnalysisResultList resultsgsea=currentprocessor.getEnrichmentAnalysisResults();
		  
			BiclusteringGSEATaskCSVReporter reporter=new BiclusteringGSEATaskCSVReporter(results, resultsgsea);
			reporter.setEnrichmentAnalysisPvalueTresholds(infogsea.getPvalues());
			reporter.useAdjustedpvalues(infogsea.isUseadjustedpvalues());
		
			String saveto=FilenameUtils.concat(resultsfilepath, "GSEA_Analysis");
			
			MTUDirUtils.checkandsetDirectory(saveto);
			try {
				reporter.writetodirectory(saveto);
			} catch (Exception e) {
				LogMessageCenter.getLogger().addCriticalErrorMessage("Error saving report files", e);
			}
		}
  		
	}

}
