package jbiclustgecli.cli;

import java.io.File;

import javax.swing.JFileChooser;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.console.ConsoleTextTerminalProvider;
import org.beryx.textio.jline.JLineTextTerminalProvider;
import org.beryx.textio.swing.SwingTextTerminalProvider;
import org.zeroturnaround.zip.ZipUtil;

import jbiclustge.propertiesmodules.PropertyLabels;
import jbiclustge.utils.props.JBiGePropertiesManager;
import jbiclustgecli.cli.executemodules.byfolder.RunBiclusteringMethodsByProfileFolder;
import jbiclustgecli.cli.installsupport.InitConfigurationWithTextIO;
import jbiclustgecli.cli.installsupport.InitNewOperationWithTextIO;
import pt.ornrocha.logutils.MTULogLevel;
import pt.ornrocha.logutils.messagecomponents.LogMessageCenter;
import pt.ornrocha.rtools.connectors.RConnector;
import pt.ornrocha.systemutils.OSystemUtils;

public class CommandLineInterfaceTextIO {
	
	

	/** The options. */
	private Options options;
	
	/** The inputargs. */
	private String[] inputargs;
	
	//private String R_PATH=null;
	//private String R_Libs_Path=null;
	
	
	/**
	 * Instantiates a new command line interface.
	 *
	 * @param args the args
	 */
	public CommandLineInterfaceTextIO(String[] args){
		this.inputargs=args;
		setupInputOptions();
	}
	
	
	/**
	 * Setup input options.
	 */
	private void setupInputOptions(){
		options=new Options();

		Option help= Option.builder("h").hasArg(false).longOpt("help").build();
		options.addOption(help);

		Option initconf=Option.builder("conf")
				.desc("Makes the initial configuration of JBiclustGE CLI (You will need to run once, at startup).\n")
				.longOpt("configure")
				.build();
		options.addOption(initconf);


		Option newprofile=Option.builder("newprofile")
				.desc("Interactive interface to create a configuration profile to perform the biclustering analysis.")
				.longOpt("new_profile")
				.build();
		options.addOption(newprofile);

		Option runbicprofile=Option.builder("run")
				.numberOfArgs(1)
				.hasArg(true)
				.argName("configuration filepath")
				.desc("Execute biclustering methods defined in a profile. Input arguments: path to the folder which contains a profile or if \"choosedir\" it is defined after \"run\" a help dialog will be opened to choose that folder.")
				.longOpt("run_profile")
				.build();
		options.addOption(runbicprofile);

		Option newgseaprofile=Option.builder("newgsea")
				.desc("Creates the configuration files to perform the Gene Set Enrichment Analysis.")
				.longOpt("new_enrichment_analysis")
				.build();
		options.addOption(newgseaprofile);

		Option rungseaprofile=Option.builder("rungsea")
				.numberOfArgs(1)
				.hasArg(true)
				.argName("path to folder which contains the configuration files")
				.desc("Execute the Gene Set Enrichment Analysis using the configuration files set in \"newgsea\". If \"choosedir\" it is  written after \"rungsea\" a help dialog will open to choose the folder required.")
				.longOpt("run_enrichment_analysis")
				.build();
		options.addOption(rungseaprofile);


		Option verbosity=Option.builder("v")
				.argName("option")
				.numberOfArgs(1)
				.hasArg(true)
				.desc("Verbosity level: off, warn, info, debug, trace ")
				.longOpt("verbosity")
				.build();
		options.addOption(verbosity);

		
		Option compress=Option.builder("z")
				.hasArg(false)
				.desc("Compress results and analysed data to a zip file")
				.longOpt("zip")
				.build();
		options.addOption(compress);
		
	}
	
	
	public void parse() throws Exception{
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd= parser.parse(options,inputargs);

			LogMessageCenter.getLogger().enableStackTrace();


			if(cmd.getOptions().length==0)
				help();
			else if (cmd.hasOption("h"))
				help();

			else{

				if(cmd.hasOption("v")){
					String param=cmd.getOptionValue("v");
					MTULogLevel level=MTULogLevel.getLevelFromStringName(param);
					LogMessageCenter.getLogger().setLogLevel(level);
				}
				else
					LogMessageCenter.getLogger().setLogLevel(MTULogLevel.INFO);

				if(cmd.hasOption("conf"))
					InitConfigurationWithTextIO.configureJbiclustGeEnvironment(null);
				else if(cmd.hasOption("run")){
					String filepath=cmd.getOptionValue("run");

					if(filepath.toLowerCase().equals("choosefolder")) {
						JFileChooser chooser=new JFileChooser();
						chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						int tag=chooser.showOpenDialog(null);
						if(tag==JFileChooser.APPROVE_OPTION) {
							filepath=chooser.getSelectedFile().getAbsolutePath();			
						}
						else
							System.exit(0);
					}

					if(filepath!=null && !filepath.isEmpty()){

						if(OSystemUtils.isLinux()) {
							String rcustom=(String) JBiGePropertiesManager.getManager().getKeyValue(PropertyLabels.RUSERPATH);
							if(rcustom!=null) {
								OSystemUtils.setEnvVariable("R_HOME", rcustom);
								System.out.println("Using custom R_HOME environment: "+rcustom);
							}
							else
								System.out.println("Using System R_HOME environment");
						}

						RunBiclusteringMethodsByProfileFolder exe=new RunBiclusteringMethodsByProfileFolder(OSystemUtils.validatePath(filepath));
						exe.execute();
						if(exe.needsToCloseRsession()) {
							RConnector.closeSession();
						}

						System.out.println("The execution of the biclustering methods was finished");
						if(cmd.hasOption("z")){
							String parentpath=new File(filepath).getParent();
							String basename=FilenameUtils.getBaseName(filepath);
							ZipUtil.pack(new File(filepath), new File(FilenameUtils.concat(parentpath, basename+"_results.zip")));
						}
					}
					else
						System.out.println("Incorrect number of input parameters");

				}


				else if(cmd.hasOption("newprofile")){
					InitNewOperationWithTextIO.createNewProfile();
				}
				else if(cmd.hasOption("newgsea")) {
					InitNewOperationWithTextIO.createNewEnrichmentAnalysis();
				}
			}

		} catch (ParseException e) {
			System.out.println(e);
			help();
		}
	}
	

	
	public static TextIO getNewTerminal() {

		TextTerminal<?> terminal=null;

		if(OSystemUtils.isWindows()) {
			terminal=new SwingTextTerminalProvider().getTextTerminal();
			if(terminal==null)
				terminal=new ConsoleTextTerminalProvider().getTextTerminal();	
		}
		else {

			terminal=new ConsoleTextTerminalProvider().getTextTerminal();
			if(terminal==null)
				terminal=new JLineTextTerminalProvider().getTextTerminal();
			if(terminal==null)
				terminal=new SwingTextTerminalProvider().getTextTerminal();
		}


		return new TextIO(terminal);
	}
	
	private void help() {
		HelpFormatter formater = new HelpFormatter();
		formater.setOptionComparator(null);
		formater.setWidth(200);
		
		if(OSystemUtils.isLinux()) {
			File check1=new File("/usr/local/bin/jbiclustge-cli");
			if(check1.exists())
				formater.printHelp("jbiclustge-cli -\"option\" [argument(s)]\n "
						+ "example: jbiclustge-cli -conf \"path of installation\"\n\n\n", options);
			else {
				formater.printHelp("jbiclustge-cli -\"option\" [argument(s)]\n "
						+ "example: jbiclustge-cli -conf \"path of installation\"\n"
						+ "or: java -jar jbiclustge-cli.jar -\"option\" [argument(s)]\n"
						+ "example: java -jar jbiclustge-cli.jar -conf \"path of installation\"\n\n\n"
						, options);
			}
		}
		else {
			formater.printHelp("jbiclustge-cli -\"option\" [argument(s)]\n "
					+ "example: jbiclustge-cli -conf \"path of installation\"\n"
					+ "or: java -jar jbiclustge-cli.jar -\"option\" [argument(s)]\n"
					+ "example: java -jar jbiclustge-cli.jar -conf \"path of installation\"\n\n\n"
					, options);
		}
	}

	public static void main(String[] args) throws Exception {
		new CommandLineInterfaceTextIO(args).parse();

	}

}
