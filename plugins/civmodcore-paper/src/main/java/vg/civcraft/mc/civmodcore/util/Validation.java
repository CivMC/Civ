package vg.civcraft.mc.civmodcore.util;

/**
 * Interface to attach an isValid() method to classes. Allows for a more abstract validation flow.
 */
public interface Validation {

	/**
	 * Determines if this instance is valid.
	 *
	 * @return Returns true if the instance is valid.
	 */
	boolean isValid();

	/**
	 * Determines if a validation instance is valid.
	 *
	 * @param validation The extended validation class to test.
	 * @return Returns true if the given instance is valid.
	 */
	static boolean checkValidity(Validation validation) {
		return validation != null && validation.isValid();
	}

}
