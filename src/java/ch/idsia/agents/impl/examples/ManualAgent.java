package ch.idsia.agents.impl.examples;

import ch.idsia.agents.AgentOptions;
import ch.idsia.agents.controllers.MarioHijackAIBase;
import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.options.FastOpts;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.agents.IAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;

import static will.neat.AbstractMarioFitnessFunction.DEFAULT_SIM_OPTIONS;


/**
 * Agent that sprints forward, jumps and shoots.
 * 
 * @author Jakub 'Jimmy' Gemrot, gemrot@gamedev.cuni.cz
 */
public class ManualAgent extends MarioHijackAIBase implements IAgent {

	private boolean shooting = false;
	
	@Override
	public void reset(AgentOptions options) {
		super.reset(options);
	}

	public MarioInput actionSelectionAI() {
//		System.out.println(info.distancePassedPhys);
//		System.out.println(info.distancePassedCells);
		return action;
	}

	public static void main(String[] args) {
		// IMPLEMENTS END-LESS RUNS
		while (true) {
			String options = DEFAULT_SIM_OPTIONS.replace(FastOpts.VIS_OFF, FastOpts.VIS_ON_2X);

			MarioSimulator simulator = new MarioSimulator(options);
			
			IAgent agent = new ManualAgent();
			
			EvaluationInfo info = simulator.run(agent);

			switch (info.marioStatus) {
			case Mario.STATUS_RUNNING:
				if (info.timeLeft <= 0) {
					System.out.println("LEVEL TIMED OUT!");
				} else {
					throw new RuntimeException("Invalid state...");
				}
				break;
			case Mario.STATUS_WIN:
				System.out.println("VICTORY");
				break;
			case Mario.STATUS_DEAD:
				System.out.println("MARIO KILLED");
				break;
			}
		}
		
		//System.exit(0);
	}
}