# Mario Bros AI
Code from Will Hardwick-Smith's Software Engineering honours project: Making intelligent Mario bros.

*Note this is still in progress and isn't set up to be usable by anyone other than myself*

## Algorithms
NEAT (NeuroEvolution of Augmenting Topologies (makes neural nets)), PSO (Particle Swarm Optimisation).

## Code origins
There was a Mario Bros AI tournament yearly during 2009-2012 [here](http://julian.togelius.com/mariocompetition2009/) and then [here](http://www.marioai.org/).
Entrants to the competition were given a Java implementation of Mario Bros called Mario Bros Infinite [gross-looking web port](http://games.keygames.com/infinity-mario/html5/)[github code](https://github.com/cflewis/Infinite-Mario-Bros)(originally made by Notch, the creator of Minecraft) which was designed to emulate Nintendo's Super Mario World, with added random level generation.
In addition to the game code, AI agent interfaces were added for entrants to use when implementing their agents. This original competition code can be found [here](https://code.google.com/archive/p/marioai/). 

Github user kefik extended this code by making readability improvements and adding quality-of-life improvements to the agent interfaces provided for the competition. This repo began as a fork of [that code](https://github.com/kefik/MarioAI).

## Dependencies (these are not included in repo)
Java 8, anji (another NEAT java implementation) lib, VUW PSO lib.
