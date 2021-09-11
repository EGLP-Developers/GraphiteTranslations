package me.mrletsplay.gtranslations.page;

import java.io.File;
import java.util.Arrays;

import me.mrletsplay.gtranslations.GraphiteTranslations;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ElementValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceButton;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceSelect;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceVerticalSpacer;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.DefaultLayoutOption;

public class ImportExportPage extends WebinterfacePage {
	
	public ImportExportPage() {
		super("Import/Export", "/t/locales", "gtranslate.import-export");
		setIcon("mdi:swap-vertical-bold");
		getContainerStyle().setProperty("max-width", "900px");
		
		WebinterfacePageSection s = new WebinterfacePageSection();
		s.addHeading("Import", 0);
		
		s.addDynamicElements(() -> {
			WebinterfaceSelect sel = new WebinterfaceSelect();
			sel.addLayoutOptions(DefaultLayoutOption.FULL_NOT_LAST_COLUMN);
			File[] files = new File("import").listFiles();
			if(files != null) {
				for(File file : files) {
					if(file.isDirectory() || !file.getName().endsWith(".yml")) continue;
					sel.addOption(file.getName(), file.getName().substring(0, file.getName().length() - 4));
				}
			}
			
			WebinterfaceButton confirm = new WebinterfaceButton("Import");
			confirm.setOnClickAction(new SendJSAction("gtranslations", "importLocale", new ElementValue(sel)));
			return Arrays.asList(sel, confirm);
		});
		
		s.addElement(new WebinterfaceVerticalSpacer("30px"));
		s.addHeading("Export", 0);
		
		s.addDynamicElements(() -> {
			WebinterfaceSelect sel = new WebinterfaceSelect();
			sel.addLayoutOptions(DefaultLayoutOption.FULL_NOT_LAST_COLUMN);
			for(String locale : GraphiteTranslations.LOCALES) {
				sel.addOption(locale, locale);
			}
			
			WebinterfaceButton confirm = new WebinterfaceButton("Export");
			confirm.setOnClickAction(new SendJSAction("gtranslations", "exportLocale", new ElementValue(sel)));
			return Arrays.asList(sel, confirm);
		});
		
		addSection(s);
	}

}
