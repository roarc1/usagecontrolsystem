package it.cnr.iit.ucs.builders;

import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.logging.Logger;

import it.cnr.iit.ucs.configuration.pip.PipProperties;
import it.cnr.iit.ucsinterface.pip.PIPBase;
import it.cnr.iit.utility.JsonUtility;

public class PIPBuilder {
    private static Logger LOGGER = Logger.getLogger( PIPBuilder.class.getName() );

    private static final String MSG_ERR_BUILD_PROP = "Error building PIPBase from properties : {0}";
    private static final String MSG_ERR_BUILD_STR = "Error building PIPBase from properties string";

    private PIPBuilder() {}

    public static Optional<PipProperties> getPropertiesFromString( String properties ) {
        Optional<PipProperties> obj = JsonUtility.loadObjectFromJsonString( properties, PipProperties.class );
        return obj;
    }

    public static Optional<PIPBase> buildFromString( String strProperties ) {
        Optional<PipProperties> properties = getPropertiesFromString( strProperties );
        if( !properties.isPresent() ) {
            LOGGER.severe( MSG_ERR_BUILD_STR );
            return Optional.empty();
        }
        return buildFromProperties( properties.get() );
    }

    public static Optional<PIPBase> buildFromProperties( PipProperties properties ) {
        Optional<PIPBase> obj;

        try {
            Class<?> clazz = Class.forName( properties.getClassName() );
            Constructor<?> constructor = clazz.getConstructor( PipProperties.class );
            PIPBase pip = (PIPBase) constructor.newInstance( properties );
            return Optional.of( pip );
        } catch( Exception e ) {
            LOGGER.severe( String.format( MSG_ERR_BUILD_PROP, e.getMessage() ) );
        }
        return Optional.empty();
    }
}