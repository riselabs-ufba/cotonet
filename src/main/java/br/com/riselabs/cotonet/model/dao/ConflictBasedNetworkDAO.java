package br.com.riselabs.cotonet.model.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.riselabs.cotonet.model.beans.ConflictBasedNetwork;
import br.com.riselabs.cotonet.model.dao.validators.ConflictBasedNetworkValidator;
import br.com.riselabs.cotonet.model.db.DBConnection;
import br.com.riselabs.cotonet.model.db.DBManager;
import br.com.riselabs.cotonet.model.enums.NetworkType;

public class ConflictBasedNetworkDAO implements DAO<ConflictBasedNetwork>{

	private Connection conn;

	public ConflictBasedNetworkDAO() { }
	
	@Override
	public boolean save(ConflictBasedNetwork conet)
			throws IllegalArgumentException {
		ConflictBasedNetworkValidator validator =  new ConflictBasedNetworkValidator();
		boolean hasSaved = false;
		if (validator.validate(conet)) {
			PreparedStatement ps;
			try {
				conn = (conn==null || conn.isClosed())?DBConnection.getConnection():conn;
				ps = conn
						.prepareStatement("insert into `networks` (`merge_scenario_id`, `type`) values (?,?);");
				ps.setInt(1, conet.getMergeScenarioID());
				ps.setString(2, conet.getType().toString());
				hasSaved = DBManager.executeUpdate(ps);
					
			} catch (SQLException | ClassNotFoundException | IOException e) {
				DBConnection.closeConnection(conn);
				e.printStackTrace();
			}
		}else{
			throw new IllegalArgumentException("The conflict based network was invalid.");
		}
		if(conn!=null){ 
			DBConnection.closeConnection(conn);
		}
		return hasSaved;
	}

	@Override
	public void delete(ConflictBasedNetwork object)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ConflictBasedNetwork> list()
			throws IllegalArgumentException {
		List<ConflictBasedNetwork> result =  new ArrayList<ConflictBasedNetwork>();
		try {
			conn = (conn==null || conn.isClosed())?DBConnection.getConnection():conn;
			PreparedStatement ps;
				ps = conn.prepareStatement("select * from `networks`;");
			ResultSet rs = DBManager.executeQuery(ps);
			
			while (rs.next()){
				ConflictBasedNetwork connet =  new ConflictBasedNetwork();
				connet.setID(rs.getInt("id"));
				connet.setType(rs.getString("type").equals("C")?NetworkType.CHUNK_BASED:NetworkType.FILE_BASED);
				connet.setMergeScenarioID(rs.getInt("merge_scenario_id"));
				result.add(connet);
			}
			
		} catch (SQLException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		if(conn!=null){ 
			DBConnection.closeConnection(conn);
		}
		return result;
	}

	@Override
	public ConflictBasedNetwork get(ConflictBasedNetwork conet)
			throws IllegalArgumentException {
		try {
			conn = (conn==null || conn.isClosed())?DBConnection.getConnection():conn;
			PreparedStatement ps = conn.prepareStatement("select * from `networks` where (`type`=? and `merge_scenario_id`=?) or `id`=?;");
			ps.setString(1, conet.getType().toString());
			if (conet.getMergeScenarioID()==null){
				ps.setInt(2, Integer.MAX_VALUE);
			}else{
				ps.setInt(2, conet.getMergeScenarioID());
			}
			if (conet.getID()==null) {
				ps.setInt(3, Integer.MAX_VALUE);
			}else{
				ps.setInt(3, conet.getID());
			}
			ResultSet rs = DBManager.executeQuery(ps);
			if(rs.next()){
				Integer id = rs.getInt("id");
				Integer mergeScenarioID = rs.getInt("merge_scenario_id");
				NetworkType type = null;
				if(rs.getString("type").equals("F")){
					type = NetworkType.FILE_BASED;
				}else if (rs.getString("type").equals("C")) {
					type = NetworkType.CHUNK_BASED;
				}
				ConflictBasedNetwork retrieved =  new ConflictBasedNetwork(id, mergeScenarioID, type);
				DBConnection.closeConnection(conn);
				return retrieved;
			}
		} catch (SQLException | ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		if(conn!=null){ 
			DBConnection.closeConnection(conn);
		}
		return null;
	}

	@Override
	public List<ConflictBasedNetwork> search(ConflictBasedNetwork object)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}
	
}