package me.mrletsplay.gtranslations.page;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import me.mrletsplay.gtranslations.GraphiteTranslations;
import me.mrletsplay.gtranslations.TranslationData;
import me.mrletsplay.gtranslations.TranslationStatus;
import me.mrletsplay.webinterfaceapi.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.RedirectAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceButton;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceElementGroup;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfacePageElement;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceText;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.DefaultLayoutOption;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.GridLayout;

public class TranslationsPage extends WebinterfacePage {
	
	public static final Comparator<TranslationData> COMPARATOR;
	
	static {
		Comparator<TranslationData> d = Comparator.comparingInt(t -> t.checkErrors().isEmpty() || t.getStatus() == TranslationStatus.ACCEPTED ? 1 : 0);
		d = d.thenComparing(t -> t.getStatus());
		d = d.thenComparing(t -> t.getPath());
		COMPARATOR = d;
	}
	
	public TranslationsPage() {
		super("Translations", "/t/translations");
		setIcon("mdi:translate");
		
		WebinterfacePageSection s = new WebinterfacePageSection();
		
		s.addDynamicElements(() -> {
			List<WebinterfacePageElement> els = new ArrayList<>();
			
			HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
			String locale = ctx.getClientHeader().getPath().getQueryParameterValue("locale");
			if(locale == null) {
				for(String l : GraphiteTranslations.LOCALES) {
					WebinterfaceButton b = new WebinterfaceButton(l);
					b.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
					b.setOnClickAction(new RedirectAction("/t/translations?locale=" + l));
					els.add(b);
				}
				return els;
			}
			
			List<TranslationData> dt = new ArrayList<>(GraphiteTranslations.getTranslationData().stream()
					.filter(d -> d.getLocaleIdentifier().equals(locale))
					.collect(Collectors.toList()));
			
			dt.sort(COMPARATOR);
			
			for(TranslationData t : dt) {
				String background = "pink";
				String text = "???";
				
				switch(t.getStatus()) {
					case ACCEPTED:
						background = "limegreen";
						text = "Accepted";
						break;
					case SUBMITTED:
						background = "cornflowerblue";
						text = "Submitted";
						break;
					case NOT_TRANSLATED:
						background = "orangered";
						text = "Not Translated";
						break;
				}
				
				if(!t.checkErrors().isEmpty() && t.getStatus() != TranslationStatus.ACCEPTED) {
					background = "orange";
					text = "Possibly Contains Errors";
				}
				
				WebinterfaceElementGroup grp = new WebinterfaceElementGroup();
				grp.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH, new GridLayout("3fr", "1fr", "200px"));
				grp.getStyle().setProperty("background-color", background);
				grp.getStyle().setProperty("border-radius", "5px");
				
				grp.addElement(WebinterfaceText.builder()
						.text(t.getPath())
						.leftbound()
						.centeredVertically()
						.create());
				
				grp.addElement(WebinterfaceText.builder()
						.text(text)
						.leftbound()
						.centeredVertically()
						.create());
				
				WebinterfaceButton b = new WebinterfaceButton("Translate");
				b.getStyle().setProperty("background-color", "rgba(0,0,0,0.25)");
				b.setOnClickAction(new RedirectAction("/t/translate?locale=" + locale + "&path=" + t.getPath()));
				grp.addElement(b);
				
				els.add(grp);
			}
			
			return els;
		});
		
		addSection(s);
	}

}
