package pulsar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import kawapad.Kawapad;
import kawapad.KawapadEvaluator;
import pulsar.lib.scheme.DescriptiveHelp;
import pulsar.lib.scheme.http.SchemeHttp;
import pulsar.lib.scheme.http.SchemeHttp.UserAuthentication;
import pulsar.lib.scheme.scretary.SchemeSecretary;

public class PulsarApplicationLibrary {
    public static SchemeSecretary createSchemeSecretary() {
        SchemeSecretary schemeSecretary = new SchemeSecretary();
        schemeSecretary.setDirectMeeting( true );
        return schemeSecretary;
    }

    public static SchemeHttp createPulsarHttpServer(
            SchemeSecretary schemeSecretary, int httpPort, UserAuthentication userAuthentication, Pulsar pulsar ) throws IOException {
        return new SchemeHttp( 
            httpPort, 
            userAuthentication, 
            schemeSecretary, 
            Arrays.asList( pulsar.threadInializer ));
    }
    public static Pulsar createPulsar( SchemeSecretary schemeSecretary ) {
        Pulsar pulsar = new Pulsar( schemeSecretary );
        DescriptiveHelp.registerGlobalSchemeInitializer( schemeSecretary );
        Pulsar.registerGlobalSchemeInitializers( schemeSecretary );
        Pulsar.registerLocalSchemeInitializers( schemeSecretary, pulsar );
//        Pulsar.registerFinalSchemeInitializers( schemeSecretary, pulsar );
        return pulsar;
    }

    public static PulsarFrame createPulsarGui( SchemeSecretary schemeSecretary, Pulsar pulsar, String ... urls ) {
        PulsarFrame pulsarFrame;
        PulsarFrame.registerGlobalSchemeInitializers( schemeSecretary );
        Kawapad.registerGlobalSchemeInitializer( schemeSecretary );
        KawapadEvaluator local = KawapadEvaluator.getLocal();
        ArrayList<KawapadEvaluator> evaluatorList = new ArrayList<>();
        evaluatorList.add( local );
        for ( String url : urls ) {
            evaluatorList.add( KawapadEvaluator.getRemote( url ) );
        }
        pulsarFrame = PulsarFrame.create( pulsar, local, evaluatorList, true , null );
        return pulsarFrame;
    }


    
}