package pulsar.lib.scheme;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import gnu.expr.Language;
import gnu.mapping.Environment;
import gnu.mapping.Procedure0;
import kawa.standard.Scheme;
import kawa.standard.load;
import pulsar.lib.CurrentObject;
import pulsar.lib.app.ApplicationComponent;
import pulsar.lib.thread.ThreadInitializer;
import pulsar.lib.thread.ThreadInitializerCollection;
import pulsar.lib.thread.ThreadInitializerContainer;

public class SchemeExecutor implements ThreadInitializerContainer<SchemeExecutor>, ApplicationComponent {
    static final Logger LOGGER = Logger.getLogger( MethodHandles.lookup().lookupClass().getName() );
    static void logError(String msg, Throwable e) { LOGGER.log(Level.SEVERE, msg, e); }
    static void logInfo(String msg)               { LOGGER.log(Level.INFO, msg);      } 
    static void logWarn(String msg)               { LOGGER.log(Level.WARNING, msg);   }
    
    public abstract interface Message<T> {
        public abstract void execute( Scheme resource );
    }
    
    Scheme scheme=null;
    public SchemeExecutor() {
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////
    //
    //////////////////////////////////////////////////////////////////////////////////////////
    
    private static final CurrentObject<SchemeExecutor> currentObject = new CurrentObject<>( SchemeExecutor.class );
    private final ThreadInitializer<SchemeExecutor> threadInitializer =
            ThreadInitializer.createMultipleThreadInitializer( "scheme", this,
                ThreadInitializer.createThreadInitializer( "current-scheme", currentObject, this ), new Runnable() {
                    @Override
                    public void run() {
                        initializeCurrentThread( SchemeExecutor.this.getScheme() );
                    }
                    @Override
                    public String toString() {
                        return "scheme-current-thread";
                    }
                });
    
    @Override
    public ThreadInitializer<SchemeExecutor> getThreadInitializer() {
        return threadInitializer;
    }
    public static SchemeExecutor getCurrent() {
        return currentObject.get();
    }

    private final ThreadInitializerCollection defaultInitializerCollection = new ThreadInitializerCollection( "default-scheme", this );
    {
        defaultInitializerCollection.addThreadInitializer( getThreadInitializer() );
    }
    public ThreadInitializerCollection getDefaultInitializerCollection() {
        return defaultInitializerCollection;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //
    //////////////////////////////////////////////////////////////////////////////////////////

    private ApplicationComponent parentApplicationComponent;
    @Override
    public ApplicationComponent getParentApplicationComponent() {
        return this.parentApplicationComponent;
    }
    @Override
    public void setParentApplicationComponent(ApplicationComponent parentApplicationComponent) {
        this.parentApplicationComponent = parentApplicationComponent;
    }

    @Override
    public void processInit() {
        this.newScheme();
    }
    @Override
    public void processQuit() {
    }
    
    public Scheme getScheme() {
        return this.scheme;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////
    // Thread Initializer
    //////////////////////////////////////////////////////////////////////////////////////////
    public static final void initializeCurrentThread( Scheme scheme ) {
        Language.setCurrentLanguage( scheme );
        Environment.setCurrent( scheme.getEnvironment() );
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // Scheme Initializer 
    //////////////////////////////////////////////////////////////////////////////////////////
    
    private List<Message> schemeInitializerList = new ArrayList<>();
    public List<Message> getSchemeInitializerList() {
        return schemeInitializerList;
    }

    /**
     * This method registers a specified initializer.
     * @see #invokeSchemeInitializers()
     */
    public void registerSchemeInitializer( Message message ) {
        this.getSchemeInitializerList().add( message  );
    }

    private void invokeSchemeInitializers() {
        for ( Message e : getSchemeInitializerList() ) {
            executeSecretarially( e );
        }
    }

    {
        registerSchemeInitializer( new Message() {
            @Override
            public void execute(Scheme scheme) {
                staticInitScheme( scheme );
            }
        });
        registerSchemeInitializer( new Message() {
            @Override
            public void execute(Scheme scheme) {
                // 3. This initializes Secretary Message Queue's thread.
                // // 4. (in most case ) this initializes main-thread
                initializeCurrentThread( scheme );
            }
        });
        registerSchemeInitializer( new Message() {
            @Override
            public void execute(Scheme scheme) {
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        // 5. This initializes AWT-EventQueue's thread.
                        initializeCurrentThread(scheme);
                    }
                });
            }
        });
        
        registerSchemeInitializer( new Message() {
            @Override
            public void execute(Scheme scheme) {
                initializeCurrentThread( scheme );
                //XXX This destroys the thread initialization for Environment().
                // (Thu, 15 Aug 2019 23:07:01 +0900)
//              SchemeUtils.defineVar( scheme, EmptyList.emptyList, "all-procedures" );
            }
        });
    }

    static class FinalizerEntry {
        Object parent;
        Message message;
        public FinalizerEntry(Object parent, Message message) {
            super();
            this.parent = parent;
            this.message = message;
        }
    }

    private void newSchemeProc() {
        logInfo( "SchemeSecretary#newScheme()" );
        this.scheme = new Scheme();
    }

    public void newScheme() {
        try {
            // 1. Create a new scheme object.
            newSchemeProc();
            
            // 2. Execute all the initializers.
            invokeSchemeInitializers();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    public static SchemeResult evaluateScheme( 
            SchemeExecutor schemeExecutor, Runnable threadInitializer, 
            String schemeScript, File currentDirectory, File schemeScriptFile, String schemeScriptURI ) 
    {
        return SchemeExecutor.evaluateScheme(
            schemeExecutor, threadInitializer,    
            new StringReader( schemeScript ), currentDirectory, schemeScriptFile, schemeScriptURI );
    }

    public static SchemeResult evaluateScheme( 
            SchemeExecutor schemeExecutor, Runnable threadInitializer, 
            Reader schemeScript, File currentDirectory, File schemeScriptFile, String schemeScriptURI ) 
    {
        return SchemeExecutorUtils.evaluateScheme(
            schemeExecutor.getScheme(), threadInitializer, schemeScript, currentDirectory, schemeScriptFile, schemeScriptURI );
    }
    
    public void executeSecretarially( Message message ) {
        message.execute( this.scheme );
    }

    /**
     * "loadRelative" was moved from 
     * {@link SchemeExecutorUtils#evaluateScheme(Scheme, Runnable, Reader, File, File, String)}  
     */
    public static void staticInitScheme( Scheme scheme ) {
        Environment env = scheme.getEnvironment();
        SchemeUtils.defineVar(env, load.loadRelative , "source" );
        SchemeUtils.defineVar(env, new Procedure0() {
            @Override
            public Object apply0() throws Throwable {
                return Language.getDefaultLanguage();
            }
        }, "current-scheme" );
        SchemeUtils.defineVar(env, new Procedure0() {
            @Override
            public Object apply0() throws Throwable {
                return Language.getDefaultLanguage();
            }
        }, "current-environment" );
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    
    private static final boolean DEBUG = false;
    private final class ScratchPadThread extends Thread {
        private final Runnable r;
        private ScratchPadThread(Runnable r) {
            this.r = r;
        }

        @Override
        public void run() {
            try {
                if ( DEBUG )
                    logInfo( "ScratchPadThreadManager:run" );
                r.run();
            } catch ( Throwable t ) {
                logError( "error occured in " + r , t );
            } finally {
                if ( DEBUG )
                    logInfo( "ScratchPadThreadManager:end" );
                removeThread( this );
            }
        }
        @Override
        public void interrupt() {
            logInfo("interrupted");
            super.interrupt();
        }
    }
    private ArrayDeque<Thread> threadList = new ArrayDeque<>();
    private void addThread( Thread t ) {
        synchronized ( threadList ) {
            threadList.add( t );
        }
    }
    private void removeThread( Thread t ) {
        synchronized ( threadList ) {
            threadList.remove( t );
        }
    }
    public void startThread( Runnable r ) {
        Thread t = new ScratchPadThread(r);
        addThread(t);
        t.start();
    }
    public void interruptAllThreads() {
        logInfo("interruptScratchPadThreads");
        synchronized ( threadList ) {
            for ( Thread t : threadList ) {
                logInfo( "interrupt start" );
                t.interrupt();
                logInfo( "interrpt end" );
            }
        }
    }
}
