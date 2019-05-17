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

package jbiclustgecli.cli.executemodules.byfolder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import jbiclustge.methods.algorithms.AbstractBiclusteringAlgorithmCaller;
import jbiclustge.methods.algorithms.r.RBiclustAlgorithmCaller;
import jbiclustge.propertiesmodules.readers.ConcurrentProcessesModuleloader;
import jbiclustge.propertiesmodules.readers.ExpressionDatasetModuleLoader;
import jbiclustge.propertiesmodules.templates.RunMethodsTemplates;
import jbiclustge.utils.props.AlgorithmProperties;
import jbiclustgecli.processcontrolcenters.biclustermethods.BiclusteringMethodsExecutionControlCenter;
import pt.ornrocha.fileutils.MTUDirUtils;
import pt.ornrocha.logutils.messagecomponents.LogMessageCenter;

// TODO: Auto-generated Javadoc
/**
 * The Class BiclusteringWithEnrichmentAnalysisExecuterByFolderConfigurations.
 */
public class BiclusteringWithEnrichmentAnalysisExecuterByFolderConfigurations extends RunBiclusteringMethodsByFolderConfigurations{
	

	/**
	 * Instantiates a new biclustering with enrichment analysis executer by folder configurations.
	 *
	 * @param dirconfigs the dirconfigs
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public BiclusteringWithEnrichmentAnalysisExecuterByFolderConfigurations(String dirconfigs) throws FileNotFoundException, IOException{
		super(dirconfigs);
	}
	
	

	/* (non-Javadoc)
	 * @see cli.executemodules.RunBiclusteringMethodsByFolderConfigurations#configure()
	 */
	@Override
	protected boolean configure() {

		try {
			
			String mainconfig=FilenameUtils.concat(dirconfs, RunMethodsTemplates.CONFIGFILENAME);
			Properties dataprops=AlgorithmProperties.loadProperties(mainconfig);
			
			ExpressionDatasetModuleLoader datasetloader=new ExpressionDatasetModuleLoader(dataprops);
			//ConcurrentProcessesPropertyModuleloader ccploader=new ConcurrentProcessesPropertyModuleloader(dataprops);
			
			String methodresultsdir=MTUDirUtils.checkEnumeratedDirAndIncrementIfExists(dirconfs, "Results_methods");
			controlcenter=new BiclusteringMethodsExecutionControlCenter(datasetloader.getExpressionDataset(), methodresultsdir);
			controlcenter.setNumberConcurrentProcesses(ConcurrentProcessesModuleloader.getNumberConcurrentProcessesFromLoader(dataprops));
			
			ArrayList<AbstractBiclusteringAlgorithmCaller> methods=AlgorithmProperties.loadBiclusterMethodsFromConfigurationsInDirectory(dirconfs);
			for (int i = 0; i < methods.size(); i++) {
				if(methods.get(i) instanceof RBiclustAlgorithmCaller)
					shutdownrenev=true;
				controlcenter.addBiclusteringMethod(methods.get(i));
			}

		} catch (Exception e) {
			LogMessageCenter.getLogger().addCriticalErrorMessage("Error loading methods to control center: ", e);
			return false;
		}
		return true;
	}



	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	

}
