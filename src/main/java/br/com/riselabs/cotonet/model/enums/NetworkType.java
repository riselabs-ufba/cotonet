package br.com.riselabs.cotonet.model.enums;

public enum NetworkType {
	CHUNK_BASED ("C"),
	FILE_BASED ("F");
	
	String description;

	NetworkType (String desc){
		this.description = desc;
	}
	
	@Override
	public String toString(){
		return description;
	}
}