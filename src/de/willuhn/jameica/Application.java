/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/Attic/Application.java,v $
 * $Revision: 1.19 $
 * $Date: 2003/12/30 19:11:27 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.SplashScreen;
import de.willuhn.jameica.rmi.ServiceFactory;

/**
 * Basisklasse der Anwendung.
 * Diese Klasse ist sozusagen das Herzstueck. Sie enthaelt alle Komponenten,
 * initialsiert, startet und beendet diese.
 * @author willuhn
 */
public class Application {

  public static boolean DEBUG = true;
  public static boolean IDE   = true;
  private static boolean serverMode = false;

  private static boolean cleanShutdown = false;
  private static ShutdownHook shutdownHook = new ShutdownHook();
  
  // singleton
  private static Application app;
    private Logger log;
    private Config config;
    
  /**
   * ct.
   */
  private Application() {
  }

  /**
   * Erzeugt eine neue Instanz der Anwendung.
   * @param serverMode legt fest, ob die Anwendung im Server-Mode (also ohne GUI starten soll).
   * @param configFile optionaler Pfad zu einer Config-Datei.
   */
  public static void newInstance(boolean serverMode, String configFile) {

    Application.serverMode = serverMode;

    splash("starting jameica");

    // start application
    app = new Application();

    // init logger
		splash("init system logger");app.log = new Logger(null);

    Application.getLog().info("starting jameica in " + (serverMode ? "Server" : "GUI") + " mode");

    // init config
    try {
			splash("init system config");app.config = new Config(configFile);
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
      Application.getLog().error("config file not found. giving up.");
      Application.shutDown();
      return;
    }
    
    Application.DEBUG = app.config.debug();
    Application.IDE   = app.config.ide();

    // switch logger to defined log file
    try {
      Application.getLog().info("switching to defined log file " + app.config.getLogFile());
      app.log = new Logger(new FileOutputStream(app.config.getLogFile()));
      Application.getLog().info("  done");
    }
    catch (FileNotFoundException e)
    {
      if (Application.DEBUG)
        e.printStackTrace();
      Application.getLog().error("  failed");
      
    }
    // init service factory
		splash("init local and remote services"); ServiceFactory.init();

    // init plugins
		splash("loading plugins"); PluginLoader.init();

    // close splash screen
    if (!serverMode)
      SplashScreen.shutDown();

    // add shutdown hook for clean shutdown (also when pressing <CTRL><C>)
    Runtime.getRuntime().addShutdownHook(shutdownHook);

    // start loops
    if (serverMode) Server.init();
    else               GUI.init();

    // Das hier koennen wir uns jetzt erlauben, weil wir ja 'nen ShutdownHook haben ;)
    // Und da wir nicht wollen, dass Hinz und Kunz die Anwendung runterfahren lassen,
    // verlassen wir uns drauf, dass der Hook zuschlaegt, wenn wir System.exit(0) aufrufen.
    System.exit(0);

  }

  /**
   * Zeigt den Splash-Screen an und vergroessert den Fortschrittsbalken
   * bei jedem erneuten Aufruf um ein weiteres Stueck.
   * Wenn die Anwendung im Servermode laeuft, kehrt die Funktion
   * tatenlos zurueck ohne den Splash-Screen anzuzeigen.
   * @param text im Splashscreen anzuzeigender Text.
   */
  public static void splash(String text)
  {
    if (serverMode)
    	return;

		SplashScreen.setText(text);
    SplashScreen.add(10);
  }


  /**
   * Faehrt die gesamte Anwendung herunter.
   * Die Funktion ist synchronized, damit nicht mehrere gleichzeitig die Anwendung runterfahren ;).
   */
  public static synchronized void shutDown()
  {

    // Das Boolean wird nach dem erfolgreichen Shutdown auf True gesetzt.
    // Somit ist sichergestellt, dass er wirklich nur einmal ausgefuehrt wird.
    if (cleanShutdown) 
      return;

    Application.getLog().info("shutting down jameica");

    if (!serverMode)
      SplashScreen.shutDown();

               GUI.shutDown();     
      PluginLoader.shutDown();
    ServiceFactory.shutDown();

    Application.getLog().info("shutdown complete");
    Application.getLog().close();

    cleanShutdown = true;
  }


  /**
   * Liefert den System-Logger.
   * @return Logger.
   */
  public static Logger getLog()
  {
    return app.log;
  }
  
  /**
   * Liefert die System-Config.
   * @return Config.
   */
  public static Config getConfig()
  {
    return app.config;
  }

  /**
   * Preuft ob die Anwendung im Server-Mode (Also ohne GUI) laeuft.
   * @return true, wenn sie im Servermode laeuft.
   */
  public static boolean inServerMode()
  {
    return serverMode;
  }
  
}


/*********************************************************************
 * $Log: Application.java,v $
 * Revision 1.19  2003/12/30 19:11:27  willuhn
 * @N new splashscreen
 *
 * Revision 1.18  2003/12/22 21:00:34  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2003/12/22 16:25:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2003/12/22 15:07:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2003/12/21 20:59:00  willuhn
 * @N added internal SSH tunnel
 *
 * Revision 1.14  2003/12/18 21:47:12  willuhn
 * @N AbstractDBObjectNode
 *
 * Revision 1.13  2003/12/16 02:27:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2003/12/15 19:08:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.9  2003/11/24 11:51:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2003/11/20 03:48:41  willuhn
 * @N first dialogues
 *
 * Revision 1.7  2003/11/18 18:56:07  willuhn
 * @N added support for pluginmenus and plugin navigation
 *
 * Revision 1.6  2003/11/13 00:37:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/11/12 00:58:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/11/05 22:46:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/10/23 22:36:35  willuhn
 * @N added Menu
 *
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
