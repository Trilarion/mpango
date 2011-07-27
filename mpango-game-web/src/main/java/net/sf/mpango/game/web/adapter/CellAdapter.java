package net.sf.mpango.game.web.adapter;

import net.sf.mpango.common.adapter.BaseAdapter;

import net.sf.mpango.game.web.dto.CellDTO;
import net.sf.mpango.game.core.entity.Cell;

public class CellAdapter extends BaseAdapter<Cell, CellDTO> {
	
	private CellAdapter() {
		super();
	}

	/**
	 * Method that retuns an instance of the adapter.
	 * @return {@link CellAdapter}
	 */
	public static CellAdapter instance() {
		return new CellAdapter();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.mpango.directory.builder.BaseBuilder#build(java.lang.
	 * Object)
	 */
	@Override
	public CellDTO toDTO (Cell cell) {
		if (null == cell) {
			return null;
		}
		CellDTO dto = new CellDTO();
		dto.setId(cell.getIdentifier());
		dto.setAttackBonus(cell.getAttackBonus());
		dto.setColumn(cell.getColumn());
		dto.setConstructions(ConstructionAdapter.instance().toDTOList(cell.getConstructions()));
		dto.setDefenseBonus(cell.getDefenseBonus());
		dto.setRow(cell.getRow());
		return dto;
	}

	@Override
	public Cell fromDTO(CellDTO dto) {
		if (null == dto) {
			return null;
		}
		Cell cell = new Cell(dto.getRow(),dto.getColumn());
		cell.setColumn(dto.getColumn());
		cell.setRow(dto.getRow());
		cell.setIdentifier(dto.getId());
		cell.setAttackBonus(dto.getAttackBonus());
		cell.setConstructions(ConstructionAdapter.instance().fromDTOList(dto.getConstructions()));
		cell.setDefenseBonus(dto.getDefenseBonus());

		return cell;
	}
}