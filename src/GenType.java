
public enum GenType {
	CENTRAL("Central"), FIELD("Field");

	private String name;

	private GenType(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}
