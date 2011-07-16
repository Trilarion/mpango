package net.sourceforge.mpango;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.mpango.actions.Command;
import net.sourceforge.mpango.dto.CellDTO;
import net.sourceforge.mpango.dto.CityDTO;
import net.sourceforge.mpango.dto.ConstructionDTO;
import net.sourceforge.mpango.dto.GameBoardDTO;
import net.sourceforge.mpango.dto.PlayerDTO;
import net.sourceforge.mpango.entity.Cell;
import net.sourceforge.mpango.entity.City;
import net.sourceforge.mpango.entity.Construction;
import net.sourceforge.mpango.entity.GameBoard;
import net.sourceforge.mpango.entity.Player;
import net.sourceforge.mpango.entity.Shield;
import net.sourceforge.mpango.entity.Technology;
import net.sourceforge.mpango.entity.Unit;
import net.sourceforge.mpango.entity.Weapon;
import net.sourceforge.mpango.enums.StateEnum;

public class TestUtils {
	public static final int ROW_SIZE = 20;
	public static final int COLUMN_SIZE = 15;

	public static CityDTO getCityDTO(Long id) {
		CityDTO dto = new CityDTO();
		dto.setId(id);
		dto.setAttackBonus(1.0f);
		dto.setConstructionTime(1);
		dto.setDefenseBonus(1f);
		dto.setHitPoints(1f);
		dto.setMaximumHitPoints(1f);
		return dto;
	}

	public static City getCity(Long id) {
		City city = new City();
		city.setIdentifier(id);
		city.setAttackBonus(1.0f);
		city.setDefenseBonus(1f);
		city.setHitPoints(1f);
		city.setMaximumHitPoints(1f);
		return city;
	}

	public static CellDTO getCellDTO(Long id) {
		CellDTO dto = new CellDTO();
		dto.setId(id);
		dto.setAttackBonus(1f);
		dto.setColumn(1);
		dto.setDefenseBonus(1f);
		dto.setRow(1);
		dto.setConstructions(new ArrayList<ConstructionDTO>());
		dto.getConstructions().add(getCityDTO(1L));
		return dto;
	}

	public static Cell getCell(Long id) {
		Cell cell = new Cell(1,1);
		cell.setIdentifier(id);
		cell.setAttackBonus(1f);
		cell.setColumn(1);
		cell.setDefenseBonus(1f);
		cell.setRow(1);
		cell.setConstructions(new ArrayList<Construction>());
		cell.getConstructions().add(getCity(1L));
		return cell;
	}

	public static GameBoardDTO getGameBoardDTO(Long id) {

		GameBoardDTO dto = new GameBoardDTO();
		dto.setId(id);
		dto.setColSize(COLUMN_SIZE);
		dto.setRowSize(ROW_SIZE);
		dto.setCells(new CellDTO[ROW_SIZE][COLUMN_SIZE]);

		for (Integer row = 0; row < ROW_SIZE; row++) {
			for (Integer col = 0; col < COLUMN_SIZE; col++) {
				dto.getCells()[row][col] = TestUtils.getCellDTO(new Long(row
						* col));
			}
		}

		return dto;

	}

	public static GameBoard getGameBoard(Long id) {
		GameBoard board = new GameBoard(ROW_SIZE, COLUMN_SIZE);
        board.setIdentifier(id);
        return board;
	}
	
	public static Player getPlayer() {
		Player player = new Player();
		player.setName("Bob");
		player.setState(StateEnum.CREATED);
		return player;
	}
	
	public static PlayerDTO getPlayerDTO() {
		PlayerDTO dto = new PlayerDTO();
		dto.setName("Bob");
		dto.setState(StateEnum.CREATED);
		return dto;
	}
	
	private static class TestUnit extends Unit {
		private TestUnit(List<Technology> technology, List<Command> command) {
			super(new City(), command, technology, 10f, 100f);
		}
		public static Unit instance() {
			List<Technology> tech = new ArrayList<Technology>();
			List<Command> com = new ArrayList<Command>();
			return new TestUnit(tech, com);
		}
	}
	
	public static Unit getUnit() {
		Unit unit = TestUnit.instance();
		unit.setWeapon(new Weapon(10f));
		unit.setShield(new Shield(10f));
		return unit;
	}	
	
	public static Technology getTechnology(Long id, int cost) {
		Technology tech = new Technology();
		tech.setIdentifier(id);
		tech.setTechnologyCost(cost);
		tech.setRequiredTechnologies((List<Technology>)new ArrayList<Technology>());
		return tech;
	}
	
}
