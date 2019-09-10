package kawapad;

import java.io.File;
import java.util.HashMap;

import javax.swing.SwingUtilities;

import pulsar.lib.scheme.SchemeUtils;
import pulsar.lib.scheme.SchemeUtils.ExecuteSchemeResult;
import pulsar.lib.scheme.scretary.SchemeSecretary;

public class KawapadEvaluator implements Runnable {
    Kawapad kawapad;
    String schemeScript;
    File schemeScriptFile;
    boolean insertText;
    boolean replaceText;
    boolean doReset;
    public KawapadEvaluator(Kawapad kawapad, String schemeScript, File schemeScriptFile, boolean insertText, boolean replaceText, boolean doReset ) {
        super();
        this.kawapad = kawapad;
        this.schemeScript = schemeScript;
        this.insertText = insertText;
        this.replaceText = replaceText;
        this.doReset = doReset;
    }
    @Override
    public void run() {
        Kawapad.logInfo( schemeScript );
        HashMap<String,Object> variables = new HashMap<>();
        kawapad.initVariables( variables );
        ExecuteSchemeResult result = SchemeSecretary.evaluateScheme( 
            kawapad.schemeSecretary, 
            kawapad.getThreadInitializerList(), variables, 
            schemeScript, schemeScriptFile, "scratchpad" );

        if ( insertText || ! result.succeeded() ) {
            if ( replaceText && result.succeeded() ) {
                if ( result.isDocument )  {
                    Kawapad.logWarn( "**KAWAPAD_PAGE**" );
                    SwingUtilities.invokeLater( new RunnableReplaceTextWithEntireBlockOnTextPane(
                        kawapad,
                        "(" + result.result.replaceFirst( "\n$", "" ) +" )",
                        false,
                        doReset
                        ) );
                } else {
                    SwingUtilities.invokeLater( new RunnableReplaceTextOnTextPane(
                        kawapad,
                        result.result,
                        doReset
                        ) );
                }
            } else {
                String resultString = SchemeUtils.formatResult( result.result ); 
                // We want to make sure the result string ends with "\n" to avoid to get an extra line.
                if ( ! schemeScript.endsWith( "\n" ) ) {
                    resultString = "\n" + SchemeUtils.formatResult( result.result ); 
                }
                Kawapad.logInfo( resultString );
                SwingUtilities.invokeLater( new RunnableInsertTextToTextPane( kawapad, resultString, true, doReset ) );
            }
        }
    }
}