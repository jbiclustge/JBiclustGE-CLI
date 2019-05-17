package jbiclustgecli.cli.installsupport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import org.apache.commons.io.FilenameUtils;
import org.beryx.textio.TextIO;
import org.beryx.textio.swing.SwingTextTerminal;

import jbiclustge.datatools.expressiondata.dataset.ExpressionData;
import jbiclustge.datatools.expressiondata.dataset.MissingValuesInDataException;
import jbiclustge.enrichmentanalysistools.clusterprofile.components.props.ClusterProfileEnrichGOPropsContainer;
import jbiclustge.enrichmentanalysistools.clusterprofile.components.props.ClusterProfileEnrichKeggModulePropsContainer;
import jbiclustge.enrichmentanalysistools.clusterprofile.components.props.ClusterProfileEnrichKeggPropsContainer;
import jbiclustge.enrichmentanalysistools.ontologizer.components.OntologizerPropertiesContainer;
import jbiclustge.enrichmentanalysistools.topgo.components.TopGoPropertiesContainer;
import jbiclustge.methods.algorithms.BiclusteringMethod;
import jbiclustge.propertiesmodules.PropertiesModules;
import jbiclustge.propertiesmodules.PropertyLabels;
import jbiclustge.propertiesmodules.templates.GSEATemplates;
import jbiclustge.results.biclusters.BiclusteringUtils;
import jbiclustge.utils.props.AlgorithmProperties;
import jbiclustgecli.cli.CommandLineInterfaceTextIO;
import pt.ornrocha.fileutils.MTUDirUtils;
import pt.ornrocha.fileutils.MTUFileUtils;
import pt.ornrocha.logutils.messagecomponents.LogMessageCenter;
import pt.ornrocha.propertyutils.EnhancedPropertiesWithSubGroups;
import pt.ornrocha.systemutils.OSystemUtils;

public class InitNewOperationWithTextIO {
	
	
	
	public static void createNewProfile() throws Exception {

		TextIO terminal = CommandLineInterfaceTextIO.getNewTerminal();
		if(terminal.getTextTerminal() instanceof SwingTextTerminal)
			((SwingTextTerminal)terminal.getTextTerminal()).setPaneTitle("JBiclustGE Profile Configuration");

		String saveprofiledir=getDirOrFileWithOptionalOpenJFileChooser(true, terminal,
				"Please, set the path of directory where the Profile will be saved",
				"Please, write the absolute path to the folder where the Profile will be saved");


		if(saveprofiledir!=null) {
			
			
			String profilename=terminal.newStringInputReader().read("\n\nGive a name to your Profile:");
			saveprofiledir=FilenameUtils.concat(saveprofiledir, profilename);

			File f=new File(saveprofiledir);
			if(!f.exists())
				f.mkdirs();

			EnhancedPropertiesWithSubGroups allprops=new EnhancedPropertiesWithSubGroups();
			allprops.appendProperties(getGeneExpressionFileProperties(terminal,saveprofiledir));
			allprops.appendProperties(getBiclusteringMethodsProperties(terminal,saveprofiledir));
			
			boolean rungsea=terminal.newBooleanInputReader().read("\nDo you want to perform the Gene Enrichment Analysis in Runtime?");
			if(rungsea) {
				EnhancedPropertiesWithSubGroups gseaprops=setupGSEAAnalysis(terminal,saveprofiledir);
				if(gseaprops!=null)
					allprops.appendProperties(gseaprops);
			}
			
			
			
			
			String filepath=FilenameUtils.concat(saveprofiledir, PropertyLabels.PROFILEFILENAME);
			allprops.store(new FileWriter(filepath), true);
			
			
			terminal.getTextTerminal().println("\n::::::::::::::::::::::::::::::::::        Your profile is now configured      :::::::::::::::::::::");
			terminal.getTextTerminal().println("\nEdit files in ["+saveprofiledir+"] to adjust your requirements ");
			terminal.getTextTerminal().println("\n::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
			
			if(terminal.getTextTerminal() instanceof SwingTextTerminal) {
				boolean closeterm=terminal.newBooleanInputReader().read("\n\nDo you want to close terminal?");
				if(closeterm)
					terminal.dispose();
			}
		}
		else {
			terminal.getTextTerminal().println("\nThe path of directory is invalid, operation was aborted\n");
			if(terminal.getTextTerminal() instanceof SwingTextTerminal) {
				boolean closeterm=terminal.newBooleanInputReader().read("\n\nDo you want to close terminal?");
				if(closeterm)
					terminal.dispose();
			}
		}

	}
	
	public static void createNewEnrichmentAnalysis() throws Exception {
		
		TextIO terminal = CommandLineInterfaceTextIO.getNewTerminal();
		if(terminal.getTextTerminal() instanceof SwingTextTerminal)
			((SwingTextTerminal)terminal.getTextTerminal()).setPaneTitle("JBiclustGE Enrichment Analysis Configuration");
		
		
		String savegseaconfigsdir=getDirOrFileWithOptionalOpenJFileChooser(true, terminal,
				"Please, set the path of directory where the the configuration files will be saved",
				"Please, write the absolute path to the folder where the configuration files will be saved");
		
		MTUDirUtils.checkDirectory(savegseaconfigsdir);
		
		EnhancedPropertiesWithSubGroups allprops=new EnhancedPropertiesWithSubGroups();
		
		
		String resultsrootdir=getDirOrFileWithOptionalOpenJFileChooser(true, terminal,
				"Please, set the path of directory where the results are stored.",
				"Please, write the absolute path to the folder where the results are stored.");
		

		allprops.addPropertyToGroupCategory("Folder of results",PropertyLabels.ANALYSERESULTINFOLDER ,  OSystemUtils.validatePath(resultsrootdir), "Directory where results are stored");
		
		String expressiondataset=getDirOrFileWithOptionalOpenJFileChooser(false, terminal,
				"Set gene expression dataset file path",
				"Please, write the absolute path to gene expression dataset file");
		

		allprops.addPropertyToGroupCategory("Gene Expression Dataset to Analyse", PropertyLabels.INPUTDATASETFILEPATH,  OSystemUtils.validatePath(expressiondataset), "");
		
		allprops.appendProperties(setupGSEAAnalysis(terminal,savegseaconfigsdir));
		

		String filepath=FilenameUtils.concat(savegseaconfigsdir, PropertyLabels.ENRICHMENTANALYSISCONF);
		allprops.store(new FileWriter(filepath), true);

		terminal.getTextTerminal().println("\n::::::::::::::::::::::::::::::::::        Your enrichment analysis is now configured      :::::::::::::::::::::");
		terminal.getTextTerminal().println("\nEdit files in ["+savegseaconfigsdir+"] to adjust your requirements ");
		terminal.getTextTerminal().println("\n::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
		
		if(terminal.getTextTerminal() instanceof SwingTextTerminal) {
			boolean closeterm=terminal.newBooleanInputReader().read("\n\nDo you want to close terminal?");
			if(closeterm)
				terminal.dispose();
		}
	}
	
	
	
	

	private static EnhancedPropertiesWithSubGroups getGeneExpressionFileProperties(TextIO terminal, String profiledir) throws IOException{
		
		EnhancedPropertiesWithSubGroups data=new EnhancedPropertiesWithSubGroups();
		
		String expressiondataset=getDirOrFileWithOptionalOpenJFileChooser(false, terminal,
				"Set gene expression dataset file path",
				"Please, write the absolute path to gene expression dataset file");
		
		
		MTUFileUtils.copyFile(expressiondataset, FilenameUtils.concat(profiledir, "dataset."+FilenameUtils.getExtension(expressiondataset)));
		
		boolean havemissingvalues=false;
		try {
			ExpressionData.loadDataset(expressiondataset, null);
		}
		catch (Exception e) {
			if(e instanceof MissingValuesInDataException)
				havemissingvalues=true;
			LogMessageCenter.getLogger().addCriticalErrorMessage(e);
		}
		
		if(havemissingvalues) {
			
			terminal.getTextTerminal().println("\n**** Dataset has missing values ****\n");
			
			boolean missimput=terminal.newBooleanInputReader().read("\nDo you want to choose a missing imputation method now?");
			
			if(missimput) {
				
				String AverageImputation="AverageImputation";
				String KMeansImputation="KMeansImputation";
				String KNNImputation="KNNImputation";
				String LLSImputation="LLSImputation";
				String SVDImputation="SVDImputation";
				String ZeroValueImputation="ZeroValueImputation";
				String None="None";
				
				String method=terminal.newStringInputReader()
								.withNumberedPossibleValues(AverageImputation,KMeansImputation,KNNImputation,
										LLSImputation,SVDImputation,ZeroValueImputation,None)
									.read("Which method (choose a number)?");
				
				String methodname=null;
				String methodtag=null;
				String methodsettings=null;
				
				switch (method) {
				case "AverageImputation":
					methodname=AverageImputation;
					break;
				case "KMeansImputation":	
					methodname=KMeansImputation;
					methodtag=PropertyLabels.KMeansImputation;
					int nc=terminal.newIntInputReader().withDefaultValue(4).withMinVal(2).read("Number of clusters:");
					methodsettings=String.valueOf(nc+";");
					int nr=terminal.newIntInputReader().withDefaultValue(4).read("Number of runs:");
					methodsettings=methodsettings+String.valueOf(nr);
					break;
				case "KNNImputation":	
					methodname=KNNImputation;
					methodtag=PropertyLabels.KNNImputation;
					int nn=terminal.newIntInputReader().withDefaultValue(4).withMinVal(1).read("number of neighbors used for imputation:");
					methodsettings=String.valueOf(nn);
					break;
				case "LLSImputation":	
					methodname=LLSImputation;
					methodtag=PropertyLabels.LLSImputation;
					int nei=terminal.newIntInputReader().withDefaultValue(4).withMinVal(1).read("number of nearest neighbors used for imputation:");
					methodsettings=String.valueOf(nei);	
					break;
				case "SVDImputation":	
					methodname=SVDImputation;
					methodtag=PropertyLabels.SVDImputation;
					int nev=terminal.newIntInputReader().withDefaultValue(3).withMinVal(1).read("number of of eigenvectors used for imputation:");
					methodsettings=String.valueOf(nev);	
					break;
				case "ZeroValueImputation":	
					methodname=ZeroValueImputation;
					break;
				default:
					break;
				}
				
				if(methodname!=null){
					data.addPropertyToSubGroupCategory("Data configuration", "Missing Value Imputation", PropertyLabels.MISSINGDATAIMPUTATIONMETHOD, methodname, "");
					if(methodtag!=null && methodsettings!=null)
						data.addPropertyToSubGroupCategory("Data configuration", "Imputation parameters", methodtag, methodsettings,"");
				}
				else
					data.addPropertyToSubGroupCategory("Data configuration", "Missing Value Imputation", PropertyLabels.MISSINGDATAIMPUTATIONMETHOD, "None", "");
			}
			else
				data.appendProperties(PropertiesModules.getInputExpressionDatasetFileModuleWithMissingvaluesImputation());

		}
			
		
		return data;
	}
	
	
	private static EnhancedPropertiesWithSubGroups getBiclusteringMethodsProperties(TextIO terminal, String profiledir) throws IOException{
		EnhancedPropertiesWithSubGroups data=new EnhancedPropertiesWithSubGroups();

		boolean choosemethods=terminal.newBooleanInputReader().read("\n\nDo you want to choose the biclustering methods?\n If (NO) all methods will be set up and you can choose later the ones you want to run");

		String dirmethods=FilenameUtils.concat(profiledir, PropertyLabels.ALGORITHMSCONFFOLDERNAME);
		MTUDirUtils.checkandsetDirectory(dirmethods);

		ArrayList<BiclusteringMethod> methodsselected=new ArrayList<>();

		if(choosemethods){
			for (BiclusteringMethod method : BiclusteringMethod.values()) {
				if(method.isSupported()){
					boolean rumethod=terminal.newBooleanInputReader().withDefaultValue(false).read("\nRun "+method.getAlgorithmName());
					if(rumethod){
						AlgorithmProperties.writeDefaultAlgorithmPropertiesToFile(dirmethods, method.getInstance(), true);
						methodsselected.add(method);
					}
				}
			}
		}
		else{

			BiclusteringUtils.writeBiclusteringMethodsConfigurationTemplate(dirmethods);
			methodsselected=BiclusteringMethod.getAllMethods();

			terminal.getTextTerminal().println("\n*****************************************************************\n");
			terminal.getTextTerminal().println("Configurations of the algorithms were stored in directory: "+dirmethods);
			terminal.getTextTerminal().println("\nNOTE:");
			terminal.getTextTerminal().println("You can delete configurations that you don't want to execute.");
			terminal.getTextTerminal().println("Change the parameters of the algorithms in the configuration files (if necessary).");
			terminal.getTextTerminal().println("*****************************************************************\n\n");
		}
        
		
		data.addPropertyToGroupCategory("Biclustering Algorithms", PropertyLabels.ALGORITHMSCONFTYPE,  PropertyLabels.ALGORITHMSCONFTYPESINGLE, "Define if each configuration for an algorithm can be executed in parallel (multithreading), options: single_run or multi_run");
		//data.addPropertyToGroupCategory("Biclustering Algorithms", PropertiesModules.ALGORITHMCONFIGURATIONSFOLDER,  OSystemUtils.validatePath(dirmethods), "Set the path of the folder that contains the configurations of the biclustering algorithms");

		
		boolean setnumbertimemethodsrun=terminal.newBooleanInputReader().read("\nDo you want to execute these methods more than one time?");

		if(setnumbertimemethodsrun) {
				for (BiclusteringMethod biclusteringMethod : methodsselected) {
					int n=terminal.newIntInputReader().withDefaultValue(1).withMinVal(0).read("\nNumber of runs for "+biclusteringMethod.getAlgorithmName()+": ");
					data.addPropertyToSubGroupCategory("Biclustering Algorithms", "Number of runs for each method configuration", BiclusteringMethod.getAlgorithmIDFromMethodInstance(biclusteringMethod.getInstance())+"_config_1", String.valueOf(n), "");
				}
		}
		else {
			for (BiclusteringMethod biclusteringMethod : methodsselected) {
				if(biclusteringMethod.isSupported())
					data.addPropertyToSubGroupCategory("Biclustering Algorithms", "Number of runs for each method configuration", BiclusteringMethod.getAlgorithmIDFromMethodInstance(biclusteringMethod.getInstance())+"_config_1", String.valueOf(1), "");
			}
		}
		
		
		
		

		if(methodsselected.size()>1){

			boolean methodsparallel=terminal.newBooleanInputReader().read("\nDo you want to execute biclustering methods simultaneously?");
			
			if(methodsparallel){
				int n=terminal.newIntInputReader().withDefaultValue(2).withMinVal(1).read("How many methods to execute simultaneously (please take care this could lead to instability of the system)");
				data.addPropertyToGroupCategory("Concurrent Processes", PropertyLabels.SIMULTANEOUSPROCESSES, String.valueOf(n), "Number of simultaneous processes running in parallel");
				if(n>1)
					data.addPropertyToGroupCategory("Biclustering Algorithms", PropertyLabels.ALGORITHMSCONFTYPE,  PropertyLabels.ALGORITHMSCONFTYPEMULTI, "Define if each configuration for an algorithm can be executed in parallel (multithreading), options: single_run or multi_run");
				else
					data.addPropertyToGroupCategory("Biclustering Algorithms", PropertyLabels.ALGORITHMSCONFTYPE,  PropertyLabels.ALGORITHMSCONFTYPESINGLE, "Define if each configuration for an algorithm can be executed in parallel (multithreading), options: single_run or multi_run");
			}
			else {
				data.addPropertyToGroupCategory("Concurrent Processes", PropertyLabels.SIMULTANEOUSPROCESSES, "1", "Number of simultaneous processes running in parallel");
				data.addPropertyToGroupCategory("Biclustering Algorithms", PropertyLabels.ALGORITHMSCONFTYPE,  PropertyLabels.ALGORITHMSCONFTYPESINGLE, "Define if each configuration for an algorithm can be executed in parallel (multithreading), options: single_run or multi_run");
			}
			
			
			boolean methodspriority=terminal.newBooleanInputReader().read("\nDo you want to prioritize the execution of algorithms?");
			
			if(methodspriority) {
				for (BiclusteringMethod biclusteringMethod : methodsselected) {
					int n=terminal.newIntInputReader().withDefaultValue(0).withMinVal(0).read("\nPriority for "+biclusteringMethod.getAlgorithmName()+" (lower value = low priority): ");
					data.addPropertyToSubGroupCategory("Biclustering Algorithms", "Priority for running each method configuration", "priority_"+BiclusteringMethod.getAlgorithmIDFromMethodInstance(biclusteringMethod.getInstance())+"_config_1", String.valueOf(n), "");
				}
			}

		}

		return data;
	}
	
	
	private static EnhancedPropertiesWithSubGroups setupGSEAAnalysis(TextIO terminal, String profiledir) throws IOException {
		
		EnhancedPropertiesWithSubGroups config=null;
		
		/*boolean rungsea=terminal.newBooleanInputReader().read("\nDo you want to perform the Gene Enrichment Analysis in Runtime?");
		
		if(rungsea) {*/
			
			String choosegseaeng = terminal.newStringInputReader().withDefaultValue(PropertyLabels.ONTOLOGIZER)
					.withNumberedPossibleValues(PropertyLabels.ONTOLOGIZER,
							PropertyLabels.TOPGO,
							PropertyLabels.CLUSTERPROFILERKEGG, 
							PropertyLabels.CLUSTERPROFILERKEGGMODULE,
							PropertyLabels.CLUSTERPROFILERGO)
					.read("\nChoose which one do you want to use:");
			
			config=GSEATemplates.getGSEABaseTemplateProperties();
			String gseaconfigfilepath=null;
			
			switch (choosegseaeng) {
			case PropertyLabels.TOPGO:
				config.setProperty(PropertyLabels.GSEAPROCESSOR, PropertyLabels.TOPGO);
				gseaconfigfilepath=FilenameUtils.concat(profiledir, PropertyLabels.GSEAPROCESSORTOPGOCONFIGNAME);
				if(!new File(gseaconfigfilepath).exists()){
					writeTopGoConfigurationFile(terminal,gseaconfigfilepath);
				}
				else{
					boolean ov=terminal.newBooleanInputReader().read("\ntopGO configuration file already exists, do you want to override this configuration:");
					if(ov)
						writeTopGoConfigurationFile(terminal,gseaconfigfilepath);

				}
				break;
			case PropertyLabels.CLUSTERPROFILERKEGG:
				config.setProperty(PropertyLabels.GSEAPROCESSOR, PropertyLabels.CLUSTERPROFILERKEGG);
				gseaconfigfilepath=FilenameUtils.concat(profiledir, PropertyLabels.GSEAPROCESSORCLUSTERPRLOFILERKEGGCONFIGNAME);
				if(!new File(gseaconfigfilepath).exists())
					ClusterProfileEnrichKeggPropsContainer.writeNewClusterProfileEnrichKeggProperties(gseaconfigfilepath);
				else{
					boolean ov=terminal.newBooleanInputReader().read("\nClusterprofiler Kegg configuration file already exists, do you want to override this configuration:");
					if(ov)
						ClusterProfileEnrichKeggPropsContainer.writeNewClusterProfileEnrichKeggProperties(gseaconfigfilepath);
				}

				break;
			case PropertyLabels.CLUSTERPROFILERKEGGMODULE:
				config.setProperty(PropertyLabels.GSEAPROCESSOR, PropertyLabels.CLUSTERPROFILERKEGGMODULE);
				gseaconfigfilepath=FilenameUtils.concat(profiledir, PropertyLabels.GSEAPROCESSORCLUSTERPRLOFILERKEGGMODULECONFIGNAME);
				if(!new File(gseaconfigfilepath).exists())
					ClusterProfileEnrichKeggModulePropsContainer.writeNewClusterProfileEnrichKeggModuleProperties(gseaconfigfilepath);
				else{
					boolean ov=terminal.newBooleanInputReader().read("\nClusterprofiler Kegg module configuration file already exists, do you want to override this configuration:");
					if(ov)
						ClusterProfileEnrichKeggModulePropsContainer.writeNewClusterProfileEnrichKeggModuleProperties(gseaconfigfilepath);
				}
                break;
			case PropertyLabels.CLUSTERPROFILERGO:
				config.setProperty(PropertyLabels.GSEAPROCESSOR, PropertyLabels.CLUSTERPROFILERGO);
				gseaconfigfilepath=FilenameUtils.concat(profiledir, PropertyLabels.GSEAPROCESSORCLUSTERPRLOFILERGOCONFIGNAME);
				if(!new File(gseaconfigfilepath).exists())
					ClusterProfileEnrichGOPropsContainer.writeNewClusterProfileEnrichGOPropsContainer(gseaconfigfilepath);
				else{
					boolean ov=terminal.newBooleanInputReader().read("\nClusterprofiler GO configuration file already exists, do you want to override this configuration:");
					if(ov)
						ClusterProfileEnrichGOPropsContainer.writeNewClusterProfileEnrichGOPropsContainer(gseaconfigfilepath);
				}
                break;
			default:
				config.setProperty(PropertyLabels.GSEAPROCESSOR, PropertyLabels.ONTOLOGIZER);
				gseaconfigfilepath=FilenameUtils.concat(profiledir, PropertyLabels.GSEAPROCESSORONTOLOGIZERCONFIGNAME);
				if(!new File(gseaconfigfilepath).exists())
					OntologizerPropertiesContainer.writeCompletePropertiesFileTemplate(gseaconfigfilepath);
				else{
					boolean ov=terminal.newBooleanInputReader().read("\nOntologizer configuration file already exists, do you want to override this configuration:");
					if(ov)
						OntologizerPropertiesContainer.writeCompletePropertiesFileTemplate(gseaconfigfilepath);
				}
				break;
			}
			
			
			boolean stres=terminal.newBooleanInputReader().read("\nYou want to define a folder to store Enrichment results?\n(otherwise will be saved in the same folder of the bicluster files):");
			if(stres){

				String gseadirpath=getDirOrFileWithOptionalOpenJFileChooser(true,terminal,
						"Choose folder where the Enrichment results will be stored",
						"Please set the path to the folder where the Enrichment results will be stored");
				
				config.addPropertyToGroupCategory("Report Results",PropertyLabels.OUTPUTDIRECTORY, gseadirpath, "Directory where results will be stored");
			}
			
			
			terminal.getTextTerminal().println("\n\n**** Change settings to perform the Gene Enrichment Analysis in the file below *****\n"+gseaconfigfilepath+"\n\n");
			config.setProperty(PropertyLabels.GSEACONFIGURATIONFILE, OSystemUtils.validatePath(gseaconfigfilepath));

		//}
		
		
		return config;
	}
	
	

	private static void writeTopGoConfigurationFile(TextIO terminal, String topgoconfigfile) throws IOException{
		

		String CUSTOMANNOT="Custom annotation";
		String DATABASEANNOT="Database annotation";
		
		String annottype=terminal.newStringInputReader().withDefaultValue(DATABASEANNOT)
		.withNumberedPossibleValues(DATABASEANNOT,CUSTOMANNOT)
		.read("\nWhich annotation that you will use with topGO:");

		
		if(annottype.equals(CUSTOMANNOT))
			TopGoPropertiesContainer.writePropertiesFileToAnnotationFile(topgoconfigfile);
		else
			TopGoPropertiesContainer.writePropertiesFileToAnnotationDatabase(topgoconfigfile);
	
		LogMessageCenter.getLogger().addInfoMessage("topGO configuration file was created in: "+topgoconfigfile);
	}
	

	
	

	private static String getDirOrFileWithOptionalOpenJFileChooser(boolean isdir,TextIO terminal, String chooseoptmsg,String askpathmsg) {
		
		String type="file";
		if(isdir)
			type="directory";
		
		String OPENLOCATIONWITHCHOOSER="Open "+type+" location using a file chooser";
		String WRITELOCALPATHLOCATION="Write absolute path of the "+type;

		String chooseopt = terminal.newStringInputReader()
				.withNumberedPossibleValues(OPENLOCATIONWITHCHOOSER,WRITELOCALPATHLOCATION)
				.read("\n"+chooseoptmsg+":");
				//.read("Please, set the path of directory where the Profile will be saved:");


		String path=null;

		if(chooseopt.equals(OPENLOCATIONWITHCHOOSER)) {

			try {
				JFileChooser chooser=new JFileChooser();
				if(isdir)
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				else
					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int tag=chooser.showOpenDialog(null);
				if(tag==JFileChooser.APPROVE_OPTION) {
					path=chooser.getSelectedFile().getAbsolutePath();			
				}
			}catch (Exception e) {
				terminal.getTextTerminal().println("*** Is not possible to open a FileChooser ***");
				path=terminal.newStringInputReader().withMinLength(1).read("\n"+askpathmsg);
				//saveprofiledir=terminal.newStringInputReader().withMinLength(1).read("\nPlease, write the absolute path to the folder where the Profile will be saved");
			}
		}
		else
			path=terminal.newStringInputReader().withMinLength(1).read("\n"+askpathmsg);

		
		if(path!=null && !path.isEmpty())
			path=OSystemUtils.validatePath(path);
		
		return path;

	}
	
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
