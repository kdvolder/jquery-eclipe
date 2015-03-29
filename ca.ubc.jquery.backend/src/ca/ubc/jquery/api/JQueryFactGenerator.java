package ca.ubc.jquery.api;

public interface JQueryFactGenerator {
	abstract public String getName();

	abstract public void insert(String fact, Object[] args) throws JQueryException;

	abstract public void insertElementLocation(Object element, String fileName, int offset, int length) throws JQueryException;

	abstract public void insertName(Object element, String name) throws JQueryException;

	abstract public void insertChild(Object parent, Object child) throws JQueryException;

	abstract public void remove(String fact, Object[] args);

	abstract public void removeAll();

	abstract public int getUniqueID(String strRep);
}
