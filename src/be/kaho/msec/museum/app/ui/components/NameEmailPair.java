package be.kaho.msec.museum.app.ui.components;

public class NameEmailPair implements Comparable<NameEmailPair> {
	private String name;
	private String email;
	
	public NameEmailPair(String name, String pair) {
		super();
		this.name = name;
		this.email = pair;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public int compareTo(NameEmailPair that) {
		return this.name.compareToIgnoreCase(that.name);
	}
	
}
