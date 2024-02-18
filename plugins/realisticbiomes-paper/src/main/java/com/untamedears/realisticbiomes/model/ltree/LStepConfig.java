package com.untamedears.realisticbiomes.model.ltree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class LStepConfig {

	private List<LStepConfigChance> results;
	private double directionWeight;
	private List<BlockTransformation> transformations;
	private boolean normalizeBeforeDirectionTransformation;
	private boolean transformInWorldCoordinates;
	private double chanceSum;
	private String id;

	public LStepConfig(String id, double directionWeight, List<BlockTransformation> transformations,
			boolean normalizeBeforeDirectionTransformation, boolean transformInWorldCoordinates) {
		this.directionWeight = directionWeight;
		this.transformations = transformations;
		this.normalizeBeforeDirectionTransformation = normalizeBeforeDirectionTransformation;
		this.transformInWorldCoordinates = transformInWorldCoordinates;
		this.results = new ArrayList<>();
		this.chanceSum = 0;
		this.id = id;
	}

	public String getID() {
		return id;
	}

	public void addNextStep(List<LStepConfig> step, double chance, List<Vector> directions) {
		this.results.add(new LStepConfigChance(step, chance, directions));
		chanceSum += chance;
	}

	public LStep toStep(Location loc, Vector direction, int depth) {
		return new LStep(this, loc, direction, depth);
	}

	public Vector getNewDirection(Vector oldDirection, Vector newDirection) {
		System.out.println("New before: " + newDirection);
		System.out.println("Old: " + oldDirection);
		if (!transformInWorldCoordinates) {
			newDirection = getInRelativeCoordinateSystem(oldDirection, newDirection);
		}
		System.out.println("New after: " + newDirection);
		return oldDirection.clone().multiply(1 - directionWeight).add(newDirection.clone().multiply(directionWeight));
	}

	/**
	 * Transform the toTransform vector into one relative to an ongoing directional
	 * movement targetDirection. This is done by assuming that targetDirection is
	 * the up vector of the target coordinate system, the normal of 0,1,0 and
	 * targetDirection is the x component and z is the cross product of the x
	 * component and targetDirection. If the target direction is already the up
	 * vector, the vector toTransform is left untouched as in this case our target
	 * coordinate system is the world coordinate system
	 * 
	 * @param targetDirection Direction based on which the target coordinate system
	 *                        is defined
	 * @param toTransform     Relative direction to transform
	 * @return Transformed vector in target coordinate system
	 */
	private Vector getInRelativeCoordinateSystem(Vector targetDirection, Vector toTransform) {
		if (Math.abs(targetDirection.getX()) < 1E-10 && Math.abs(targetDirection.getZ()) < 1E-10) {
			// standard up vector, meaning our default world coordinate system
			return toTransform;
		}
		// at this point we know the targetDirection vector is not the up vector, so the
		// cross product will give us a valid normal on the plane defined by
		// targetDirection and the up vector in world coordinates
		Vector xComponent = new Vector(0, 1, 0).crossProduct(targetDirection);
		Vector zComponent = xComponent.getCrossProduct(targetDirection);
		System.out.println("x: "+  xComponent);
		System.out.println("z: "+  zComponent);
		return dirtyMatrixMultiplication(xComponent, targetDirection, zComponent, toTransform);
	}
	
	private static Vector dirtyMatrixMultiplication(Vector matrixComp1, Vector matrixComp2, Vector matrixComp3, Vector vector) {
		double firstComp = matrixComp1.getX() * vector.getX() + matrixComp2.getX() * vector.getY() + matrixComp3.getX() * vector.getZ();
		double secondComp = matrixComp1.getY() * vector.getX() + matrixComp2.getY() * vector.getY() + matrixComp3.getY() * vector.getZ();
		double thirdComp = matrixComp1.getZ() * vector.getX() + matrixComp2.getZ() * vector.getY() + matrixComp3.getZ() * vector.getZ();
		return new Vector(firstComp, secondComp, thirdComp);
	}

	public List<LStep> progress(LStep step) {
		Location currentLocation = step.getLocation();
		Vector normalizedDirection = step.getDirection().clone().normalize();
		for (BlockTransformation transform : transformations) {
			transform.applyAt(currentLocation);
		}
		if (results.isEmpty()) {
			return Collections.emptyList();
		}
		List<LStepConfig> nextSteps = null;
		List<Vector> nextStepDirection = null;
		if (results.size() == 1) {
			nextSteps = results.get(0).step;
			nextStepDirection = results.get(0).directions;
		} else {
			double random = Math.random() * chanceSum;
			for (LStepConfigChance chance : results) {
				random -= chance.chance;
				if (random <= 0) {
					nextSteps = chance.step;
					nextStepDirection = chance.directions;
					break;
				}
			}
		}
		List<LStep> result = new ArrayList<>();
		Vector oldDirection = step.getDirection();
		if (normalizeBeforeDirectionTransformation) {
			oldDirection = normalizedDirection;
		}
		for (int i = 0; i < nextSteps.size(); i++) {
			LStepConfig subStep = nextSteps.get(i);
			Vector subDirection = nextStepDirection.get(i);
			Vector localDirection = subStep.getNewDirection(oldDirection, subDirection);
			Location updatedLocation = step.getLocation().clone().add(localDirection);
			result.add(subStep.toStep(updatedLocation, localDirection, step.getDepth() + 1));
		}
		return result;
	}

	public LStepConfig clone() {
		return new LStepConfig(id, directionWeight, transformations, normalizeBeforeDirectionTransformation, transformInWorldCoordinates);
	}

	private static final class LStepConfigChance {
		private final double chance;
		private final List<LStepConfig> step;
		private final List<Vector> directions;

		public LStepConfigChance(List<LStepConfig> step, double chance, List<Vector> directions) {
			this.step = step;
			this.chance = chance;
			this.directions = directions;
		}
	}

}
