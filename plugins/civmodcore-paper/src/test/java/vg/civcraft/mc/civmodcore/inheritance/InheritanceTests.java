package vg.civcraft.mc.civmodcore.inheritance;

import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.Assert;
import org.junit.Test;

public class InheritanceTests {

	@Test
	public void testStaticInheritance() {
		final var method = MethodUtils.getMatchingAccessibleMethod(ChildClass.class, "create");
		Assert.assertNotNull("ChildClass did not inherit static method reflectively", method);
		final ParentClass result;
		try {
			result = (ParentClass) method.invoke(null);
		}
		catch (final InvocationTargetException | IllegalAccessException | ClassCastException exception) {
			Assert.fail("Error occurred during the static method invocation: " + exception.getMessage());
			return;
		}
		Assert.assertNotNull("Method did not return a result", result);
	}

}
