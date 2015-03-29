package ca.ubc.jquery.api.gluelog;

import org.eclipse.core.resources.IProject;

import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryFactGenerator;

/**
 * Some attempts at trying to get JTransformer to parse other types of files.
 */
public class GlueLogFactGenerator implements JQueryFactGenerator {

	public String getName() {
		return "gluelogfactgen";
	}

	public int getUniqueID(String strRep) {
		return 42;
	}

	public void insert(String fact, Object[] args) throws JQueryException {
		// TODO Auto-generated method stub
		
	}

	public void insertChild(Object parent, Object child) throws JQueryException {
		// TODO Auto-generated method stub
		
	}

	public void insertElementLocation(Object element, String fileName,
			int offset, int length) throws JQueryException {
		// TODO Auto-generated method stub
		
	}

	public void insertName(Object element, String name) throws JQueryException {
		// TODO Auto-generated method stub
		
	}

	public void remove(String fact, Object[] args) {
		// TODO Auto-generated method stub
		
	}

	public void removeAll() {
		// TODO Auto-generated method stub
		
	}
	
	
	//	public void factBaseUpdated(JTransformerProjectEvent e) {
//		try {
//			JTransformerNature nature = (JTransformerNature) e.getSource();
//
//			 Object[] x = nature.getOptions();
//			 for (int i = 0; i < x.length; i++) {
//			 System.out.println(((Option) x[i]).getLabel());
//			 }
//
//			IProject p = nature.getProject();
//			ISourceRegenerator rs = nature.getSourceRegenerator();
//
//			IAffectedFile[] f = rs.getAffectedFiles();
//			for (int i = 0; i < f.length; i++) {
//				System.out.println(f[i].getFilename());
//			}
//		} catch (Exception ex) {
//			System.out.println(ex);
//		}
//	}
//
//	public void appliedCt(JTransformerProjectEvent e) {
//	}
}
