package net.sourceforge.mpango.directory.builder;



import java.util.ArrayList;
import java.util.List;

import net.sourceforge.mpango.TestUtils;
import net.sourceforge.mpango.dto.CellDTO;
import net.sourceforge.mpango.entity.Cell;

import org.junit.Assert;
import org.junit.Test;

public class CellBuilderTest {

	@Test
	public void testBuildCell() {
		Cell cell = TestUtils.getCell(1L);
		CellDTO dto = CellBuilder.instance().build(cell);
		Assert.assertNotNull(dto);
		Assert.assertEquals(dto.getId().longValue(), 1L);
		Assert.assertEquals(dto.getConstructions().size(), 1);
		Assert.assertEquals(dto.getAttackBonus(), new Float(cell.getAttackBonus()));
		Assert.assertEquals(dto.getColumn(), new Integer(cell.getColumn()));
		Assert.assertEquals(dto.getDefenseBonus(), new Float(cell.getDefenseBonus()));
		Assert.assertEquals(dto.getRow(), new Integer(cell.getRow()));		
	}
	
	@Test
	public void testCellBuilderList() {
		List<Cell> cellList = new ArrayList<Cell>();
		cellList.add(TestUtils.getCell(1L));
		cellList.add(TestUtils.getCell(2L));
		cellList.add(TestUtils.getCell(3L));
		cellList.add(TestUtils.getCell(4L));
		cellList.add(TestUtils.getCell(5L));
		List<CellDTO> dtoList = CellBuilder.instance().buildList(cellList);
		Assert.assertNotNull(dtoList);
		Assert.assertEquals(dtoList.size(), 5);
	}

}