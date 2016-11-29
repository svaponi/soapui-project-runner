package it.infocert.soapui;

import org.apache.log4j.Logger;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.model.settings.Settings;

public class CustomSoapUICore extends DefaultSoapUICore {


	@Override
	public Settings getSettings() {
		if (log == null) {
			initLog();
		}
		return super.getSettings();
	}
	
	@Override
	protected void initLog() {
		// We keep on using the log of the server....
		log = Logger.getLogger(DefaultSoapUICore.class);
	}
}