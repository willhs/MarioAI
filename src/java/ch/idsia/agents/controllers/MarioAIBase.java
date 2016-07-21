/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package ch.idsia.agents.controllers;

import ch.idsia.agents.AgentOptions;
import ch.idsia.agents.IAgent;
import ch.idsia.agents.controllers.modules.Entities;
import ch.idsia.benchmark.mario.engine.generalization.MarioEntity;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.environments.IEnvironment;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.agents.controllers.modules.Tiles;

/**
 * Abstract class that serves as a basis for implementing new Mario-AI agents. 
 * 
 * Created by IntelliJ IDEA. 
 * User: Sergey Karakovskiy 
 * Date: Apr 25, 2009 
 * Time: 12:30:41 AM 
 * Package: ch.ch.idsia.agents.controllers
 * 
 * @author Sergey Karakovskiy
 * @author Jakub 'Jimmy' Gemrot, gemrot@gamedev.cuni.cz
 */
public abstract class MarioAIBase extends MarioAgentBase {
	
	/**
	 * Information about Mario's body.
	 */
	protected MarioEntity mario;
	
	/**
	 * Actions an {@link IAgent} wants to perform.
	 */
	protected MarioInput action = new MarioInput();
	
	protected Entities entities = new Entities();
	
	protected Tiles tiles = new Tiles();

	protected EvaluationInfo info = new EvaluationInfo();

	protected int highestFitness;
	
	public MarioAIBase() {
		super("MarioAIBase");
		name = getClass().getSimpleName();
	}
	
	public MarioAIBase(String agentName) {
		super(agentName);
	}	
	
	@Override
	public void reset(AgentOptions options) {
		super.reset(options);
		action.reset();
		entities.reset(options);
		tiles.reset(options);
		info.reset();
		highestFitness = 0;
	}

	@Override
	public MarioInput actionSelection() {
		return action;
	}
	
	public void observe(IEnvironment environment) {
		mario = environment.getMario();
		tiles.tileField = environment.getTileField();
		entities.entityField = environment.getEntityField();
		entities.entities = environment.getEntities();
		info = environment.getEvaluationInfo();

		int fitness = info.computeBasicFitness();
		if (fitness > highestFitness) {
			highestFitness = fitness;
		}
	}

	@Override
	public boolean sucks(){
		return false;
	}

}
