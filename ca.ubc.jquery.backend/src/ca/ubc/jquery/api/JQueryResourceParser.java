package ca.ubc.jquery.api;

public abstract class JQueryResourceParser {
	private JQueryFactGenerator generator;

	private JQueryResourceManager manager;

	// TODO Remove manager?
	// Can I simply move the "dependencyFound" thing into the fact generator class?
	public JQueryResourceParser(JQueryResourceManager manager) {
		this.generator = null;
		this.manager = manager;
	}

	public abstract void parse();

	public abstract String getName();

	public abstract void initialize(JQueryFactGenerator generator);

	public JQueryFactGenerator getGenerator() {
		return generator;
	}

	protected final void setGenerator(JQueryFactGenerator generator) {
		this.generator = generator;
	}
}
