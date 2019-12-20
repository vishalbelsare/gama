/*********************************************************************************************
 *
 * 'Converters.java, in plugin ummisco.gama.serialize, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package gama.extensions.serialize.factory;

import com.thoughtworks.xstream.converters.Converter;

import gama.extensions.serialize.gamaType.converters.ConverterScope;
import gama.extensions.serialize.gamaType.converters.GamaAgentConverter;
import gama.extensions.serialize.gamaType.converters.GamaAgentConverterNetwork;
import gama.extensions.serialize.gamaType.converters.GamaBasicTypeConverter;
import gama.extensions.serialize.gamaType.converters.GamaFileConverter;
import gama.extensions.serialize.gamaType.converters.GamaGraphConverter;
import gama.extensions.serialize.gamaType.converters.GamaListConverter;
import gama.extensions.serialize.gamaType.converters.GamaListConverterNetwork;
import gama.extensions.serialize.gamaType.converters.GamaMapConverter;
import gama.extensions.serialize.gamaType.converters.GamaMatrixConverter;
import gama.extensions.serialize.gamaType.converters.GamaPairConverter;
import gama.extensions.serialize.gamaType.converters.GamaPathConverter;
import gama.extensions.serialize.gamaType.converters.GamaPopulationConverter;
import gama.extensions.serialize.gamaType.converters.GamaSpeciesConverter;
import gama.extensions.serialize.gamaType.converters.LogConverter;
import gama.extensions.serialize.gamaType.converters.ReferenceAgentConverter;
import gama.extensions.serialize.gamaType.converters.SavedAgentConverter;

public abstract class Converters {

	private static Converter[] loadConverter(ConverterScope cs)
	{
		Converter[] converters= new Converter[14];
		converters[0]= new GamaBasicTypeConverter(cs);
		converters[1]=new GamaAgentConverter(cs);		
		converters[2]=new GamaListConverter(cs);
		converters[3]=new GamaMapConverter(cs);
		converters[4]=new GamaPairConverter();
		converters[5]=new GamaMatrixConverter(cs);
		converters[6]=new GamaGraphConverter(cs);		
		converters[7]=new GamaFileConverter(cs);

		converters[8]=new LogConverter();
		converters[9]=new SavedAgentConverter(cs);
		
		converters[10]= new GamaPopulationConverter(cs);
		converters[11]= new GamaSpeciesConverter(cs);	
		converters[12]= new ReferenceAgentConverter(cs);		
		converters[13]= new GamaPathConverter(cs);		
		
		//converters[12]= new ComplexMessageConverter(cs);		
		
		return converters;
	}
	
	
	public static Converter[] converterFactory(ConverterScope cs)
	{
		return loadConverter(cs);
	}

	
	
	// TODO Remove when possible
	private static Converter[] loadConverterNetwork(ConverterScope cs)
	{
		Converter[] converters= new Converter[12];
		converters[0]= new GamaBasicTypeConverter(cs);
		converters[1]=new GamaAgentConverterNetwork(cs);
		converters[2]=new GamaListConverterNetwork(cs);
		converters[3]=new GamaMapConverter(cs);
		converters[4]=new GamaPairConverter();
		converters[5]=new GamaMatrixConverter(cs);
		converters[6]=new GamaGraphConverter(cs);		
		converters[7]=new GamaFileConverter(cs);

		converters[8]=new LogConverter();
		converters[9]=new SavedAgentConverter(cs);
		
		converters[10]= new GamaPopulationConverter(cs);
		converters[11]= new GamaSpeciesConverter(cs);		
		//converters[12]= new ComplexMessageConverter(cs);		
		
		return converters;
	}
	
	public static Converter[] converterNetworkFactory(ConverterScope cs)
	{
		return loadConverterNetwork(cs);
	}	
	
	// END TODO
}