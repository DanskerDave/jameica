/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/ApplicationCallbackSWT.java,v $
 * $Revision: 1.16 $
 * $Date: 2007/11/05 13:01:13 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.system;

import java.security.cert.X509Certificate;
import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.SplashScreen;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.CertificateTrustDialog;
import de.willuhn.jameica.gui.dialogs.NewPasswordDialog;
import de.willuhn.jameica.gui.dialogs.PasswordDialog;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.gui.dialogs.TextDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.messaging.CheckTrustMessage;
import de.willuhn.logging.Logger;
import de.willuhn.security.Checksum;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * SWT-Implementierung des SSLCallbacks.
 */
public class ApplicationCallbackSWT extends AbstractApplicationCallback
{

	private SplashScreen monitor = null;
	private String password      = null;


  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#createPassword()
   */
  public String createPassword() throws Exception
  {
		this.password = Application.getStartupParams().getPassword();
		if (this.password != null)
		{
			Logger.info("master password given via commandline");
		}
		else
		{
	  	NewPasswordDialog p = new NewPasswordDialog(NewPasswordDialog.POSITION_CENTER);
	  	p.setText(Application.getI18n().tr(
				"Sie starten Jameica zum ersten Mal.\n\n" +
				"Bitte vergeben Sie ein Master-Passwort zum Schutz Ihrer pers�nlichen Daten.\n" +				"Es wird anschlie�end bei jedem Start von Jameica ben�tigt."));
			p.setTitle(Application.getI18n().tr("Jameica Master-Passwort"));

			try
			{
				this.password = (String) p.open();
			}
			catch (OperationCanceledException e)
			{
				throw new OperationCanceledException(Application.getI18n().tr("Passwort-Eingabe abgebrochen"),e);
			}
		}
		// Wir speichern eine Checksumme des neuen Passwortes.
		// Dann koennen wir spaeter checken, ob es ok ist.
		settings.setAttribute("jameica.system.callback.checksum",Checksum.md5(this.password.getBytes()));
		return this.password;
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#getPassword()
   */
  public String getPassword() throws Exception
  {
  	if (this.password != null)
  		return password;

		String checksum = settings.getString("jameica.system.callback.checksum","");
		this.password  	= Application.getStartupParams().getPassword();

		if (password != null)
		{
			Logger.info("master password given via commandline");
			if (checksum == null)
			{
				return password;
			}

			Logger.info("checksum found, testing");
			if (checksum.equals(Checksum.md5(password.getBytes())))
			{
				return password;
			}
			Logger.info("checksum test failed, asking for password in interactive mode");
		}

		PWD dialog = new PWD();
		
		String text = Application.getI18n().tr("Bitte geben Sie das Jameica Master-Passwort ein.");
		dialog.setText(text);
		dialog.setTitle(Application.getI18n().tr("Jameica Master-Passwort"));

		try
		{
			this.password = (String) dialog.open();
		}
		catch (OperationCanceledException e)
		{
			throw new OperationCanceledException(Application.getI18n().tr("Passwort-Eingabe abgebrochen"),e);
		}
		return this.password;
  }

	/**
	 * @see de.willuhn.jameica.system.ApplicationCallback#changePassword()
	 */
	public void changePassword() throws Exception
	{
		NewPasswordDialog p = new NewPasswordDialog(NewPasswordDialog.POSITION_CENTER);
		p.setText(Application.getI18n().tr(
			"Bitte geben Sie Ihr neues Master-Passwort zum Schutz Ihrer pers�nlichen Daten ein.\n" +
			"Es wird anschlie�end bei jedem Start von Jameica ben�tigt."));
		p.setTitle(Application.getI18n().tr("Neues Jameica Master-Passwort"));

		try
		{
			this.password = (String) p.open();
			// Wir speichern eine Checksumme des neuen Passwortes.
			// Dann koennen wir spaeter checken, ob es ok ist.
			settings.setAttribute("jameica.system.callback.checksum",Checksum.md5(this.password.getBytes()));
		}
		catch (OperationCanceledException e)
		{
			throw new OperationCanceledException(Application.getI18n().tr("Passwort-Eingabe abgebrochen"),e);
		}
	}

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#getStartupMonitor()
   */
  public ProgressMonitor getStartupMonitor()
  {
  	if (monitor != null)
  		return monitor;
  	monitor = new SplashScreen();
  	monitor.init();
  	return monitor;
  }


  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#startupError(java.lang.String, java.lang.Throwable)
   */
  public void startupError(String errorMessage, Throwable t)
  {
  	if (t != null && t instanceof OperationCanceledException)
  	{
  		Logger.error("it seems the user has forgotten the master password. not startup error dialog needed",t);
			return; // da hat der user wohl beim Login den Abbrechen-Knopf gedrueckt.
  	}
		Display d = Display.getCurrent();
		if (d == null)
			d = new Display();
		final Shell s = new Shell();
		s.setLayout(new GridLayout());
		s.setText(Application.getI18n().tr("Fehler beim Start von Jameica."));
		Label l = new Label(s,SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		l.setText(Application.getI18n().tr("Beim Start von Jameica ist ein Fehler aufgetreten:\n") + errorMessage);

		Button b = new Button(s,SWT.BORDER);
		b.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		b.setText("    OK    ");
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				s.close();
			}
		});
		s.pack();
		s.open();
		while (!s.isDisposed()) {
			if (!d.readAndDispatch()) d.sleep();
		}
		try {
			s.dispose();
			d.dispose();
		}
		catch (Exception e2) {
			// useless
		}
  }


  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#askUser(java.lang.String, java.lang.String)
   */
  public String askUser(String question, String labeltext) throws Exception
  {
  	TextDialog d = new TextDialog(TextDialog.POSITION_CENTER);
  	d.setText(question);
  	d.setTitle(labeltext);
  	d.setLabelText(labeltext);
  	return (String) d.open();
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#askUser(java.lang.String)
   */
  public boolean askUser(final String question) throws Exception
  {
    return askUser(question,(String[])null); 
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#askUser(java.lang.String, java.lang.String[])
   */
  public boolean askUser(final String question, String[] variables) throws Exception
  {
    if (question == null)
    {
      Logger.warn("<null> question!");
      return false;
    }

    // Wir schauen mal, ob wir fuer diese Frage schon eine Antwort haben
    String s = settings.getString(question,null);
    if (s != null)
      return s.equalsIgnoreCase("true");
    
    final String text = (variables == null || variables.length == 0) ? question : MessageFormat.format(question,(Object[])variables);

    AbstractDialog d = new AbstractDialog(AbstractDialog.POSITION_CENTER)
    {
      private Boolean choice = Boolean.FALSE;
      private CheckboxInput check = new CheckboxInput(false);
      protected Object getData() throws Exception
      {
        return choice;
      }

      protected void paint(Composite parent) throws Exception
      {
        LabelGroup g = new LabelGroup(parent,"");
        g.addText(text,true);
        g.addCheckbox(check,Application.getI18n().tr("Diese Frage k�nftig nicht mehr anzeigen"));
        ButtonArea buttons = new ButtonArea(parent,2);
        buttons.addButton("   " + i18n.tr("Ja") + "   ", new Action() {
          public void handleAction(Object context) throws ApplicationException
          {
            if (((Boolean)check.getValue()).booleanValue())
              settings.setAttribute(question,true);
            choice = Boolean.TRUE;
            close();
          }
        },null,true);
        buttons.addButton("   " + i18n.tr("Nein") + "   ", new Action() {
          public void handleAction(Object context) throws ApplicationException
          {
            if (((Boolean)check.getValue()).booleanValue())
              settings.setAttribute(question,false);
            choice = Boolean.FALSE;
            close();
          }
        });
      }
    };
    d.setTitle(Application.getI18n().tr("Jameica: Frage"));
    try
    {
      return ((Boolean)d.open()).booleanValue();
    }
    catch (Exception e)
    {
      Logger.error("error while asking user",e);
    }
    return false;
  }
  

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#lockExists(java.lang.String)
   */
  public boolean lockExists(String lockfile)
  {
    try
    {
      return askUser(Application.getI18n().tr("Jameica scheint bereits zu laufen. Wollen Sie den Startvorgang wirklich fortsetzen?"));
    }
    catch (Exception e)
    {
      Logger.error("error while asking user",e);
      return false;
    }
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#checkTrust(java.security.cert.X509Certificate)
   */
  public boolean checkTrust(X509Certificate cert) throws Exception
  {
    // Wir senden die Trust-Abrfrage vorher noch per Message
    CheckTrustMessage msg = new CheckTrustMessage(cert);
    Application.getMessagingFactory().sendSyncMessage(msg);
    if (msg.isTrusted())
    {
      Logger.info("cert: " + cert.getSubjectDN().getName() + ", trusted by: " + msg.getTrustedBy());
      return true;
    }

    CertificateTrustDialog d = new CertificateTrustDialog(CertificateTrustDialog.POSITION_CENTER,cert);

    Boolean b = (Boolean) d.open();
    return b.booleanValue();
  }


  /**
   * @see de.willuhn.jameica.system.ApplicationCallback#notifyUser(java.lang.String)
   */
  public void notifyUser(String text) throws Exception
  {
    SimpleDialog d = new SimpleDialog(SimpleDialog.POSITION_CENTER);
    d.setTitle(Application.getI18n().tr("Information"));
    d.setText(text);
    d.open();
  }



  /**
   * Innere Klasse fuer die Passwort-Eingabe.
   */
  private class PWD extends PasswordDialog
  {

    /**
     */
    public PWD()
    {
      super(PWD.POSITION_CENTER);
      setSize(400,SWT.DEFAULT);
    }

    /**
     * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#checkPassword(java.lang.String)
     */
    protected boolean checkPassword(String password)
    {
      if (password == null)
      {
        setErrorText(Application.getI18n().tr("Bitte geben Sie Ihr Master-Passwort ein.") + " " + getRetryString());
        return false;
      }
      try
      {
        String pw       = Checksum.md5(password.getBytes());
        String checksum = settings.getString("jameica.system.callback.checksum","");
        boolean b = checksum.equals(pw);
        if (!b)
        {
          setErrorText(Application.getI18n().tr("Passwort falsch.") + " " + getRetryString());
        }
        return b;
      }
      catch (Exception e)
      {
        Logger.error("error while checking password",e);
      }
      return false;
    }

    /**
     * Liefert einen locale String mit der Anzahl der Restversuche.
     * z.Bsp.: "Noch 2 Versuche.".
     * @return String mit den Restversuchen.
     */
    private String getRetryString()
    {
      String retries = getRemainingRetries() > 1 ? Application.getI18n().tr("Versuche") : Application.getI18n().tr("Versuch");
      return (Application.getI18n().tr("Noch") + " " + getRemainingRetries() + " " + retries + ".");
    }

  }
}


/**********************************************************************
 * $Log: ApplicationCallbackSWT.java,v $
 * Revision 1.16  2007/11/05 13:01:13  willuhn
 * @C Compiler-Warnings
 *
 * Revision 1.15  2007/08/31 10:00:10  willuhn
 * @N CheckTrustMessage synchron versenden, wenn Vertrauensstellung abgefragt wird
 *
 * Revision 1.14  2007/04/20 14:48:02  willuhn
 * @N Nachtraegliches Hinzuegen von Elementen in TablePart auch vor paint() moeglich
 * @N Zusaetzliche parametrisierbare askUser-Funktion
 *
 * Revision 1.13  2007/01/25 10:44:10  willuhn
 * @N autoanswer in ApplicationCallbackConsole
 *
 * Revision 1.12  2006/10/28 01:05:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2006/07/13 21:43:31  willuhn
 * @N Passwort-Dialog etwas kleiner gemacht
 *
 * Revision 1.10  2005/06/27 13:58:18  web0
 * @N auto answer in application callback
 *
 * Revision 1.9  2005/06/24 14:55:56  web0
 * *** empty log message ***
 *
 * Revision 1.8  2005/06/16 13:29:20  web0
 * *** empty log message ***
 *
 * Revision 1.7  2005/06/13 12:13:37  web0
 * @N Certificate-Code completed
 *
 * Revision 1.6  2005/06/09 23:07:47  web0
 * @N certificate checking activated
 *
 * Revision 1.5  2005/03/17 22:44:10  web0
 * @N added fallback if system is not able to determine hostname
 *
 * Revision 1.4  2005/03/01 22:56:48  web0
 * @N master password can now be changed
 *
 * Revision 1.3  2005/02/02 16:16:38  willuhn
 * @N Kommandozeilen-Parser auf jakarta-commons umgestellt
 *
 * Revision 1.2  2005/02/01 17:15:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/30 20:47:43  willuhn
 * *** empty log message ***
 *
 **********************************************************************/