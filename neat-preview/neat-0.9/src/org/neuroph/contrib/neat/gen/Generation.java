package org.neuroph.contrib.neat.gen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A <code>Generation</code> is a snapshot of the <code>Specie</code>s at the
 * end of each generation.
 * 
 * @author Aidan Morgan
 */
public class Generation implements Serializable {
	private static final long serialVersionUID = -8534071334022878312L;

	/**
	 * The generation.
	 */
	private int generationNumber;

	/**
	 * A <code>List</code> of the <code>Specie</code>s in the generation.
	 */
	private List<Specie> species;

	/**
	 * The last innovation id used for this <code>Generation</code>.
	 */
	private long lastInnovationId;

	/**
	 * Constructor.
	 * 
	 * @param g
	 *            the generation number.
	 * @param o
	 *            the <code>List</code> of <code>Specie</code>s in the
	 *            generation.
	 * @param l   the last innovation id generated by the owning <code>NeatParameters</code>. This value is used
	 * 		 	  to re-initialise the <code>NeatParameters</code> if the generation is reloaded from the 
	 * 			  <code>Persistence</code> instance.
	 */
	public Generation(int g, List<Specie> o, long l) {
		this.generationNumber = g;
		this.species = o;
		this.lastInnovationId = l;
	}

	/**
	 * Returns the number of this <code>Generation</code>.
	 * 
	 * @return the number of this <code>Generation</code>.
	 */
	public int getGenerationNumber() {
		return generationNumber;
	}

	/**
	 * Returns the <code>List</code> of <code>Specie</code>s that are in this
	 * <code>Generation</code>.
	 * 
	 * <b>Note:</b> these are a copy of the <code>Specie</code>s, not the actual
	 * ones under evolution.
	 * 
	 * @return the <code>List</code> of <code>Specie</code>s that are in this
	 *         <code>Generation</code>.
	 */
	public List<Specie> getSpecies() {
		return species;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + generationNumber;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Generation other = (Generation) obj;
		if (generationNumber != other.generationNumber)
			return false;
		return true;
	}

	/**
	 * Returns a <code>List</code> of all of the <code>Organism</code>s in the
	 * <code>Specie</code>s that are part of this generation.
	 * 
	 * @return a <code>List</code> of all of the <code>Organism</code>s in the
	 *         <code>Specie</code>s that are part of this generation.
	 */
	public List<Organism> getOrganisms() {
		List<Organism> orgs = new ArrayList<Organism>();

		for (Specie s : species) {
			orgs.addAll(s.getOrganisms());
		}

		return orgs;
	}

	/**
	 * Returns the last innovation id used. This value is used to re-initialise the 
	 * <code>NeatParameters</code> if the generation is reloaded from the 
	 * <code>Persistence</code> instance.
	 * 
	 * @return the last innovation id used.
	 */
	public long getLastInnovationId() {
		return lastInnovationId;
	}

}