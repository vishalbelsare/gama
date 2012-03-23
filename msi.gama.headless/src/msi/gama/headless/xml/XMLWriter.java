package msi.gama.headless.xml;

import java.io.IOException;

import msi.gama.headless.common.Fichier;
import msi.gama.headless.common.FichierException;
import msi.gama.headless.core.Simulation;



public class XMLWriter implements Writer{
	private Fichier file;
	
	public XMLWriter(String f)
	{
		try {
			this.file=new Fichier(f,Fichier.OUT);
		} catch (FichierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close() {
		String res="</Simulation>";
		try {
			this.file.ecrire(res);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void writeResultStep(int step, String[] names, Object[] values) {
		String res="\t<Step id='"+step+"' >\n";
		for(int i=0;i<values.length;i++)
			res=res+"\t\t<Variable name='"+names[i]+"' value='"+values[i].toString()+"'/>\n";
		res=res+"\t</Step>\n";
		try {
			this.file.ecrire(res);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeSimulationHeader(Simulation s) 
	{
		String res="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		res+="<Simulation id=\""+s.getExperimentID()+"\" >\n";
		try {
			this.file.ecrire(res);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
}
