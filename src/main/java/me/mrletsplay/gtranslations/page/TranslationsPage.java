package me.mrletsplay.gtranslations.page;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import me.mrletsplay.gtranslations.GraphiteTranslations;
import me.mrletsplay.gtranslations.TranslationData;
import me.mrletsplay.gtranslations.TranslationStatus;
import me.mrletsplay.gtranslations.element.TranslationInfo;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.page.Page;
import me.mrletsplay.webinterfaceapi.page.PageSection;
import me.mrletsplay.webinterfaceapi.page.action.RedirectAction;
import me.mrletsplay.webinterfaceapi.page.element.Button;
import me.mrletsplay.webinterfaceapi.page.element.Group;
import me.mrletsplay.webinterfaceapi.page.element.Text;
import me.mrletsplay.webinterfaceapi.page.element.builder.Align;
import me.mrletsplay.webinterfaceapi.page.element.layout.Grid;

public class TranslationsPage extends Page {
	
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
		
		PageSection s = new PageSection();
		s.setGrid(new Grid().setColumns("1fr").setGap("10px"));
		s.setSlimLayout(true);
		
		s.dynamic(els -> {
			HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
			String locale = ctx.getClientHeader().getPath().getQuery().getFirst("locale");
			if(locale == null) {
				for(String l : GraphiteTranslations.LOCALES) {
					Button b = new Button(l);
					b.setOnClickAction(RedirectAction.to("/t/translations?locale=" + l));
					els.add(b);
				}
				return;
			}
			
			List<TranslationData> dt = new ArrayList<>(GraphiteTranslations.getTranslationData().stream()
					.filter(d -> d.getLocaleIdentifier().equals(locale))
					.collect(Collectors.toList()));
			
			dt.sort(COMPARATOR);
			
			els.add(new TranslationInfo(dt));
		});
		
		addSection(s);
	}

}
