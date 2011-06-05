package net.sourceforge.mpango.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import org.apache.commons.lang.math.RandomUtils;

public class GameContext {

	protected List<Player> players;
	protected GameBoard board;
	protected GameConfiguration configuration;
	
	private GameContext(BoardConfiguration boardConfiguration) {
		players = new ArrayList<Player>();
		board = new GameBoard(boardConfiguration);
	}
	
	public GameContext(GameConfiguration configuration) {
		this(configuration.getBoardConfiguration());
		this.configuration = configuration;
		
	}

	public void join(Player player) {
		player.setPosition(generateRandomPosition());
		player.setUnits(generateStartingUnits());
		players.add(player);
	}

	private List<Unit> generateStartingUnits() {
		List<Unit> units = null;
		return units;
	}

	private Position generateRandomPosition() {
		BoardConfiguration boardConfiguration = configuration.getBoardConfiguration();
		Position position = new Position(
				RandomUtils.nextInt(boardConfiguration.getColNumber()),
				RandomUtils.nextInt(boardConfiguration.getRowNumber()));
		return position;
	}

	public boolean containsPlayer(Player player) {
		return players.contains(player);
	}

	@Transient
	public int getNumberOfPlayers() {
		return players.size();
	}

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	public GameBoard getBoard() {
		return board;
	}

	public void setBoard(GameBoard board) {
		this.board = board;
	}

	public GameConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(GameConfiguration configuration) {
		this.configuration = configuration;
	}
}
