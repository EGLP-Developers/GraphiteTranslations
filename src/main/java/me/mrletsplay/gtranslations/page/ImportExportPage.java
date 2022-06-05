package me.mrletsplay.gtranslations.page;

import java.io.File;

import me.mrletsplay.gtranslations.GraphiteTranslations;
import me.mrletsplay.webinterfaceapi.page.Page;
import me.mrletsplay.webinterfaceapi.page.PageSection;
import me.mrletsplay.webinterfaceapi.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.page.action.value.ActionValue;
import me.mrletsplay.webinterfaceapi.page.element.Button;
import me.mrletsplay.webinterfaceapi.page.element.Select;
import me.mrletsplay.webinterfaceapi.page.element.VerticalSpacer;
import me.mrletsplay.webinterfaceapi.page.element.layout.DefaultLayoutOption;

public class ImportExportPage extends Page {
	
	public ImportExportPage() {
		super("Import/Export", "/t/locales", "gtranslate.import-export");
		setIcon("mdi:swap-vertical-bold");
		getContainerStyle().setProperty("max-width", "900px");
		
		PageSection s = new PageSection();
		s.addHeading("Import", 0);
		
		s.dynamic(els -> {
			Select sel = new Select();
			sel.addLayoutOptions(DefaultLayoutOption.FULL_NOT_LAST_COLUMN);
			File[] files = new File("import").listFiles();
			if(files != null) {
				for(File file : files) {
					if(file.isDirectory() || !file.getName().endsWith(".yml")) continue;
					sel.addOption(file.getName(), file.getName().substring(0, file.getName().length() - 4));
				}
			}
			els.add(sel);
			
			Button confirm = new Button("Import");
			confirm.setOnClickAction(SendJSAction.of("gtranslations", "importLocale", ActionValue.object().put("locale", ActionValue.elementValue(sel))));
			els.add(confirm);
		});
		
		s.addElement(new VerticalSpacer("30px"));
		s.addHeading("Export", 0);
		
		s.dynamic(els -> {
			Select sel = new Select();
			sel.addLayoutOptions(DefaultLayoutOption.FULL_NOT_LAST_COLUMN);
			for(String locale : GraphiteTranslations.LOCALES) {
				sel.addOption(locale, locale);
			}
			els.add(sel);
			
			Button confirm = new Button("Export");
			confirm.setOnClickAction(SendJSAction.of("gtranslations", "exportLocale", ActionValue.object().put("locale", ActionValue.elementValue(sel))));
			els.add(confirm);
		});
		
		addSection(s);
	}

}
