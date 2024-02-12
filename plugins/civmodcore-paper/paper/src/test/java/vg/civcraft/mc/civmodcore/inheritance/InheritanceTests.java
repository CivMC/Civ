package vg.civcraft.mc.civmodcore.inheritance;

import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InheritanceTests {

	@Test
	public void testStaticInheritance() {
		final var method = MethodUtils.getMatchingAccessibleMethod(ChildClass.class, "create");
		Assertions.assertNotNull(method, "ChildClass did not inherit static method reflectively");
		final ParentClass result;
		try {
			result = (ParentClass) method.invoke(null);
		}
		catch (final InvocationTargetException | IllegalAccessException | ClassCastException exception) {
			Assertions.fail("Error occurred during the static method invocation: " + exception.getMessage());
			return;
		}
		Assertions.assertNotNull(result, "Method did not return a result");
	}

}
