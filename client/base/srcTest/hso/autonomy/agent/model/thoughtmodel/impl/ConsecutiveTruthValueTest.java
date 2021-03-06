package hso.autonomy.agent.model.thoughtmodel.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import hso.autonomy.agent.model.thoughtmodel.IThoughtModel;

public class ConsecutiveTruthValueTest
{
	private ConsecutiveTruthValue testee;

	@Before
	public void setUp() throws Exception
	{
		testee = new WrapperCTV(1, 3);
	}

	@Test
	public void testIsValid()
	{
		float time = 0.0f;
		assertFalse(testee.isValid());
		testee.setValidity(true, time++);
		assertTrue("Did not believe first perception", testee.isValid());

		testee.setValidity(false, time++);
		testee.setValidity(false, time++);
		assertTrue(testee.isValid());
		testee.setValidity(false, time++);
		assertFalse(testee.isValid());

		testee.setValidity(true, time++);
		assertTrue(testee.isValid());

		testee.setValidity(false, time++);
		testee.setValidity(true, time++);
		testee.setValidity(false, time++);
		testee.setValidity(false, time++);
		assertTrue(testee.isValid());
		testee.setValidity(false, time++);
		assertFalse(testee.isValid());
	}

	// just needed to create an instance
	class WrapperCTV extends ConsecutiveTruthValue
	{
		public WrapperCTV(int trueCount, int falseCount)
		{
			super(trueCount, falseCount);
		}

		@Override
		public void update(IThoughtModel thoughtModel)
		{
		}
	}
}
