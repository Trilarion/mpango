package net.sourceforge.mpango.entity;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.sourceforge.mpango.entity.Cell;
import net.sourceforge.mpango.entity.City;
import net.sourceforge.mpango.entity.Construction;
import net.sourceforge.mpango.entity.Shield;
import net.sourceforge.mpango.entity.Technology;
import net.sourceforge.mpango.entity.Unit;
import net.sourceforge.mpango.entity.Weapon;
import net.sourceforge.mpango.entity.technology.ShieldTechnology;
import net.sourceforge.mpango.entity.technology.WeaponTechnology;
import net.sourceforge.mpango.exception.ConstructionAlreadyInPlaceException;
import net.sourceforge.mpango.exception.UnknownTechnologyException;

/**
 * This class is named after the entity Unit and for it's nature as a test.
 * @author etux
 *
 */
public class UnitTest extends TestCase {

	private static final Float SHIELD_HIT_POINTS = 5f;
	private static final Float WEAPON_ATTACK_POINTS = 3f;
	private static final Float UNIT_ATTACK_POINTS = 10f;
	private static final Float UNIT_HIT_POINTS = 15f;
	private static final Float MAXIMUM_SHIELD_HIT_POINTS = 5f; 
	private static final Float MAXIMUM_CITY_HIT_POINTS = 100f; 
	
	public void setUp() {
		
	}
	
	public void tearDown() {
	}
	
	public void testEffectiveAttackPoints() throws UnknownTechnologyException {
		Unit unit = new TestUnit(createTechnologies(), UNIT_ATTACK_POINTS, UNIT_HIT_POINTS);
		Float expectedAttackPoints = UNIT_ATTACK_POINTS * WEAPON_ATTACK_POINTS * unit.getHealth();
		assertEquals("The unit should have the expected attack points", expectedAttackPoints, unit.getEffectiveAttackPoints());
	}
	
	public void testSurviving() throws UnknownTechnologyException {
		Unit unit = new TestUnit(createTechnologies(), UNIT_ATTACK_POINTS, UNIT_HIT_POINTS);
		Float attackPoints = 19f;
		unit.receiveDamage(attackPoints);
		assertFalse(unit.isDead());
		assertEquals("The hit points left should be the expected", UNIT_HIT_POINTS + SHIELD_HIT_POINTS - attackPoints, unit.getHitPoints());
	}
	
	public void testKilling() throws UnknownTechnologyException {
		Unit unit = new TestUnit(createTechnologies(), UNIT_ATTACK_POINTS, UNIT_HIT_POINTS);
		Float attackPoints = 20f;
		unit.receiveDamage(attackPoints);
		assertTrue(unit.isDead());
		assertEquals("The hit points left should be the expected", UNIT_HIT_POINTS + SHIELD_HIT_POINTS - attackPoints, unit.getHitPoints());
	}
	
	public void testInvalidTechnology() {
		try {
			new TestUnit(createUnknownTechnologies(), UNIT_ATTACK_POINTS, UNIT_HIT_POINTS);
			fail("Expected exception not raised");
		} catch (UnknownTechnologyException e) {
			//Expected exception
		}
	}
	
	public void testWeapon() throws UnknownTechnologyException {
		Unit unit = new TestUnit(createTechnologies(), UNIT_ATTACK_POINTS, UNIT_HIT_POINTS);
		assertNotNull("Since we have provided a weapon technology during construction, the unit should have a weapon", unit.getWeapon());
	}
	
	public void testDamageShield() throws UnknownTechnologyException {
		Unit unit = new TestUnit(createTechnologies(), UNIT_ATTACK_POINTS, UNIT_HIT_POINTS);
		assertNotNull("Since we have provided a shield technology during construction, the unit should have a shield", unit.getShield());
		assertEquals("The shield should be initialized with the expected hit points", SHIELD_HIT_POINTS, unit.getShield().getRemainingHitPoints());
		unit.receiveDamage(1f);
		assertNotNull("The shield should have been capable of surviving the attack", unit.getShield());
		assertEquals("The should have the exact remaining hit points", MAXIMUM_SHIELD_HIT_POINTS - 1f, unit.getShield().getRemainingHitPoints());
	}
	
	public void testDestroyShield() throws UnknownTechnologyException {
		Unit unit = new TestUnit(createTechnologies(), UNIT_ATTACK_POINTS, UNIT_HIT_POINTS);
		assertNotNull("Since we have provided a shield technology during construction, the unit should have a shield", unit.getShield());
		assertEquals("The shield should be initialized with the expected hit points", SHIELD_HIT_POINTS, unit.getShield().getRemainingHitPoints());
		unit.receiveDamage(UNIT_HIT_POINTS);
		assertNull("The shield should have been destroyed during the attack", unit.getShield());
		
	}
	
	public void testSettle() throws ConstructionAlreadyInPlaceException, UnknownTechnologyException {
		Unit unit = new TestUnit(createTechnologies(), UNIT_ATTACK_POINTS, UNIT_HIT_POINTS);
		boolean flag = false;

		// create a city
		Cell cell = new Cell(0, 0);
		flag = unit.settle(cell, MAXIMUM_CITY_HIT_POINTS);
		System.out.println("initial: " + flag);
		List<Construction> constructions = cell.getConstructions();
		for (Construction construction: constructions) {
			if (construction instanceof City) {
				City city = (City) construction;
				assertEquals("The city should be initialized with the expected hit points", MAXIMUM_CITY_HIT_POINTS, city.getMaximumHitPoints());
				try {
					assertEquals("A city should be created successfully.", true, flag);
					assertEquals("The city should be initialized with the expected hit points", MAXIMUM_CITY_HIT_POINTS, city.getMaximumHitPoints());
				} catch (AssertionError e) {
					e.printStackTrace();
				}
			} 
		}
		
		// try creating a city in a cell that already has a city in it
		flag = unit.settle(cell, MAXIMUM_CITY_HIT_POINTS);
		System.out.println("existing: " + flag);
		try {
			assertEquals("A city should not be created since a cell already has a city in it.", false, flag);
		} catch (AssertionError e) {
			e.printStackTrace();
		}
		
		// create a city in a different cell
		cell = new Cell(0, 1);
		flag = unit.settle(cell, MAXIMUM_CITY_HIT_POINTS);
		try {
			assertEquals("A city should be created successfully.", true, flag);
		} catch (AssertionError e) {
			e.printStackTrace();
		}
	}
	
	private List<Technology> createTechnologies() {
		List<Technology> technologies = new ArrayList<Technology>();
		technologies.add(new TestShieldTechnology());
		technologies.add(new TestWeaponTechnology());
		return technologies;
	}
	private List<Technology> createUnknownTechnologies() {
		List<Technology> technologies = new ArrayList<Technology>();
		technologies.add(new Technology () {

			public List<Technology> getRequiredTechnologies() {
				// TODO Auto-generated method stub
				return null;
			}

			public Integer getTechnologyCost() {
				// TODO Auto-generated method stub
				return null;
			}
			
		});
		return technologies;
	}
	
	public class TestWeaponTechnology extends WeaponTechnology {
		public TestWeaponTechnology() {}
		public Weapon createWeapon() {
			return new Weapon(WEAPON_ATTACK_POINTS);
		}
		public List<Technology> getRequiredTechnologies() {
			// TODO Auto-generated method stub
			return null;
		}
		public Integer getTechnologyCost() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public class TestShieldTechnology extends ShieldTechnology {
		public TestShieldTechnology() {}
		public Shield createShield() {
			return new Shield(MAXIMUM_SHIELD_HIT_POINTS);
		}
		public List<Technology> getRequiredTechnologies() {
			// TODO Auto-generated method stub
			return null;
		}
		public Integer getTechnologyCost() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public class TestUnit extends Unit {
		
		public TestUnit(List<Technology> technologies, Float attackPoints, Float maximumHitPoints) throws UnknownTechnologyException {
			super(technologies, attackPoints, maximumHitPoints);
		}
		private static final long serialVersionUID = 1L;
		public float repair() {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
}