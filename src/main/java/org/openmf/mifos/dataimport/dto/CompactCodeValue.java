package org.openmf.mifos.dataimport.dto;

public class CompactCodeValue {

	private final Integer id;
	private final String name;

	public CompactCodeValue(Integer id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}
