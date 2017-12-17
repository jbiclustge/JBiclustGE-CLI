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
package jbiclustgecli.processcontrolcenters;

import java.util.ArrayList;

import jbiclustge.datatools.expressiondata.dataset.ExpressionData;
import jbiclustge.execution.controlcenters.common.AbstractBiclusteringControlCenter;
import pt.ornrocha.threadutils.MTUMultiThreadRunneableExecutor;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractBiclusteringProcessCLIControlCenter.
 */
public abstract class AbstractBiclusteringProcessCLIControlCenter extends AbstractBiclusteringControlCenter{

	
	/** The simultaneousprocesses. */
	protected Integer simultaneousprocesses=null;

	
	/**
	 * Instantiates a new abstract biclustering process CLI control center.
	 *
	 * @param data the data
	 */
	public AbstractBiclusteringProcessCLIControlCenter(ExpressionData data) {
		super(data);
	}

	
	/**
	 * Sets the number concurrent processes.
	 *
	 * @param nproc the new number concurrent processes
	 */
	public void setNumberConcurrentProcesses(int nproc){
		this.simultaneousprocesses=nproc;
	}
	
	/**
	 * Gets the list processes to run.
	 *
	 * @return the list processes to run
	 */
	protected abstract ArrayList<Runnable> getListProcessesToRun();
	
	
	
	/**
	 * Execute.
	 *
	 * @throws Exception the exception
	 */
	public void execute() throws Exception{
		ArrayList<Runnable> processes=getListProcessesToRun();
		MTUMultiThreadRunneableExecutor.execute(processes, simultaneousprocesses);	
	}
	
	
	
}
