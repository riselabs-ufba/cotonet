/**
 * 
 */
package br.com.riselabs.connet.beans;

/**
 * @author alcemirsantos
 *
 */
public class ChunkBlame {

	private String commitRef;
	private CommandLineBlameResult result;

	public ChunkBlame(String ref){
		this(ref, null);
	}

	public ChunkBlame(String commit, CommandLineBlameResult result) {
		super();
		this.commitRef = commit;
		this.result = result;
	}

	public String getCommit() {
		return commitRef;
	}

	public void setCommit(String commit) {
		this.commitRef = commit;
	}

	public CommandLineBlameResult getResult() {
		return result;
	}

	public void setResult(CommandLineBlameResult result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "['"+result.getFilePath()+"' blame @ "
				+ "Commit ('"
				+ this.commitRef + "')]";
	}
}
