package net.sf.mpango.game.web.adapter;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import net.sf.mpango.game.core.entity.Weapon;
import net.sf.mpango.game.web.adapter.WeaponBuilder;
import net.sf.mpango.game.web.dto.WeaponDTO;


public class WeaponBuilderTest {
	
	@Test
	public void TestBuildWeapon() {
		Weapon weapon = new Weapon(1F);
		WeaponDTO dto = WeaponBuilder.instance().build(weapon);
		
		assertNotNull(dto);
		assertEquals(weapon.getIdentifier(), dto.getId());
		assertEquals(weapon.getAttackBonus(), dto.getAttackBonus());
	}
	
	@Test
	public void TestBuildList() {
		List<Weapon> weaponList = new ArrayList<Weapon>();
		weaponList.add(new Weapon(1F));
		weaponList.add(new Weapon(2F));
		weaponList.add(new Weapon(3F));
		weaponList.add(new Weapon(4F));
		weaponList.add(new Weapon(5F));
		List<WeaponDTO> dtoList = WeaponBuilder.instance().buildList(weaponList);
		assertNotNull(dtoList);
		assertEquals(5, dtoList.size());
	}

}