/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/StatusBar.java,v $
 * $Revision: 1.53 $
 * $Date: 2007/05/14 11:18:09 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.gui;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;

/**
 * Bildet die Statusleiste der Anwendung ab.
 * @author willuhn
 */
public class StatusBar implements Part
{

  private ArrayList items = new ArrayList();
  
  private Composite status;

	private StackLayout progressStack;
		private Composite progressComp;
		private ProgressBar progress;
		private ProgressBar noProgress;
  
	/**
	 * ct.
	 */
	public StatusBar()
  {
    
  }
  
  /**
   * Fuegt der Statusbar ein neues Element hinzu.
   * @param item das hinzufuegende Element.
   */
  public void addItem(StatusBarItem item)
  {
    this.items.add(item);
  }
  
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    int height = 20;
    try
    {
      FontData font = Font.DEFAULT.getSWTFont().getFontData()[0];
      int h = SWTUtil.pt2px(font.getHeight());
      if (h > 0)
        height = h;
    }
    catch (Throwable t)
    {
      // ignore
    }
    
		this.status = new Composite(parent, SWT.NONE);
		GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = height + 12; // 12 Pixel fuer den Rand
		status.setLayoutData(data);

    GridLayout layout = new GridLayout(2,false);
    layout.marginHeight = 1;
    layout.marginWidth = 1;
    layout.horizontalSpacing = 1;
    layout.verticalSpacing = 1;
		status.setLayout(layout);

		progressComp = new Composite(status, SWT.NONE);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint = 60;
		gd.heightHint = height + 10; // hier nochmal 10 Pixel
		progressComp.setLayoutData(gd);
		progressStack = new StackLayout();
		progressComp.setLayout(progressStack);
		
		progress = new ProgressBar(progressComp, SWT.INDETERMINATE);
		progress.setToolTipText(Application.getI18n().tr("Vorgang wird bearbeitet..."));
		noProgress = new ProgressBar(progressComp, SWT.NONE);
		progressStack.topControl = noProgress;


    int size = this.items.size();

		Composite tComp = new Composite(status,SWT.BORDER);
		tComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout tgd = new GridLayout((2 * size) - 1,false);
		tgd.marginHeight = 0;
		tgd.marginWidth = 0;
		tgd.horizontalSpacing = 0;
		tgd.verticalSpacing = 0;
		tComp.setLayout(tgd);
    
    for (int i=0;i<size;++i)
    {
      StatusBarItem item = (StatusBarItem) this.items.get(i);
      item.paint(tComp);
      if (i < (size - 1))
      {
        final Label sep = GUI.getStyleFactory().createLabel(tComp, SWT.SEPARATOR | SWT.VERTICAL);
        final GridData sepgd = new GridData(GridData.FILL_VERTICAL);
        sepgd.widthHint = 5;
        sep.setLayoutData(sepgd);
      }
    }

	}
	
	/**
   * Schaltet den Progress-Balken ein.
   */
  public synchronized void startProgress()
	{
		GUI.getDisplay().syncExec(new Runnable() {
      public void run()
      {
        if (progressComp == null || progressComp.isDisposed())
          return;
        progressStack.topControl = progress;
        progressComp.layout();
      }
    });
	}

	/**
	 * Schaltet den Progress-Balken aus.
	 */
	public synchronized void stopProgress()
	{
		GUI.getDisplay().syncExec(new Runnable() {
      public void run()
      {
        if (progressComp == null || progressComp.isDisposed())
          return;
        progressStack.topControl = noProgress;
        progressComp.layout();
      }
    });
	}

  /**
   * Ersetzt den aktuellen Statustext rechts unten gegen den uebergebenen.
   * @param message anzuzeigender Text.
   * Nachrichten sollten direkt ueber die MessagingFactory mit
   * dem Nachrichtentyp StatusBarMessage gesendet werden.
   */
  public void setSuccessText(final String message)
  {
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(message,StatusBarMessage.TYPE_SUCCESS));
  }

  /**
   * Ersetzt den aktuellen Statustext rechts unten gegen den uebergebenen.
   * Formatiert die Anzeige hierbei aber rot als Fehler.
   * @param message anzuzeigender Text.
   * Nachrichten sollten direkt ueber die MessagingFactory mit
   * dem Nachrichtentyp StatusBarMessage gesendet werden.
   */
  public void setErrorText(final String message)
  {
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(message,StatusBarMessage.TYPE_ERROR));
  }
}


/*********************************************************************
 * $Log: StatusBar.java,v $
 * Revision 1.53  2007/05/14 11:18:09  willuhn
 * @N Hoehe der Statusleiste abhaengig von DPI-Zahl und Schriftgroesse
 * @N Default-Schrift konfigurierbar und Beruecksichtigung dieser an mehr Stellen
 *
 * Revision 1.52  2007/04/01 22:15:22  willuhn
 * @B Breite des Statusbarlabels
 * @B Redraw der Statusleiste
 *
 * Revision 1.51  2006/03/15 16:36:18  web0
 * @C changed border style
 *
 * Revision 1.50  2006/03/15 16:25:32  web0
 * @N Statusbar refactoring
 *
 * Revision 1.49  2006/03/07 23:00:55  web0
 * @C no border around log panel
 *
 * Revision 1.48  2006/03/07 22:43:14  web0
 * *** empty log message ***
 *
 * Revision 1.47  2006/03/07 18:24:04  web0
 * @N Statusbar and logview redesign
 *
 * Revision 1.46  2005/11/18 12:14:12  web0
 * @B dispose check
 *
 * Revision 1.45  2005/08/25 21:18:24  web0
 * @C changes accoring to findbugs eclipse plugin
 *
 * Revision 1.44  2005/07/26 22:58:34  web0
 * @N background task refactoring
 *
 * Revision 1.43  2005/07/11 08:31:24  web0
 * *** empty log message ***
 *
 * Revision 1.42  2005/06/27 15:35:52  web0
 * @N ability to store last table order
 *
 * Revision 1.41  2005/06/21 20:02:02  web0
 * @C cvs merge
 *
 * Revision 1.40  2005/06/10 22:13:09  web0
 * @N new TabGroup
 * @N extended Settings
 *
 * Revision 1.39  2005/06/03 17:14:41  web0
 * @N Livelog
 *
 * Revision 1.38  2004/12/31 19:33:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.37  2004/12/13 22:48:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.36  2004/11/17 19:02:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.35  2004/11/15 00:38:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.34  2004/11/12 18:23:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.33  2004/11/10 17:48:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.32  2004/10/08 00:19:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.31  2004/08/30 15:03:28  willuhn
 * @N neuer Security-Manager
 *
 * Revision 1.30  2004/08/27 17:46:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.29  2004/08/18 23:14:19  willuhn
 * @D Javadoc
 *
 * Revision 1.28  2004/08/11 23:37:21  willuhn
 * @N Navigation ist jetzt modular erweiterbar
 *
 * Revision 1.27  2004/07/23 15:51:20  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.26  2004/07/21 23:54:54  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.25  2004/06/30 20:58:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2004/06/17 22:07:12  willuhn
 * @C cleanup in tablePart and statusBar
 *
 * Revision 1.23  2004/06/10 20:56:53  willuhn
 * @D javadoc comments fixed
 *
 * Revision 1.22  2004/05/26 23:23:23  willuhn
 * @N Timeout fuer Messages in Statusbars
 *
 * Revision 1.21  2004/05/23 15:30:52  willuhn
 * @N new color/font management
 * @N new styleFactory
 *
 * Revision 1.20  2004/04/29 23:05:54  willuhn
 * @N new snapin feature
 *
 * Revision 1.19  2004/04/29 21:21:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/04/12 19:16:00  willuhn
 * @C refactoring
 * @N forms
 *
 * Revision 1.17  2004/03/30 22:08:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/03/24 00:46:03  willuhn
 * @C refactoring
 *
 * Revision 1.15  2004/03/05 00:40:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.13  2004/02/23 20:30:34  willuhn
 * @C refactoring in AbstractDialog
 *
 * Revision 1.12  2004/02/20 01:25:06  willuhn
 * @N nice dialog
 * @N busy indicator
 * @N new status bar
 *
 * Revision 1.11  2004/02/12 23:46:27  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.9  2004/01/25 18:39:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.6  2004/01/06 20:11:22  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2003/12/12 01:28:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.3  2003/11/13 00:37:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2003/10/29 00:41:26  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/23 21:49:46  willuhn
 * initial checkin
 *
 **********************************************************************/
