package me.mrletsplay.gtranslations.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import me.mrletsplay.gtranslations.GraphiteTranslations;
import me.mrletsplay.gtranslations.TranslationData;
import me.mrletsplay.gtranslations.TranslationStatus;
import me.mrletsplay.gtranslations.element.TranslateArea;
import me.mrletsplay.webinterfaceapi.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.webinterface.auth.WebinterfaceAccount;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePage;
import me.mrletsplay.webinterfaceapi.webinterface.page.WebinterfacePageSection;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.RedirectAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.ReloadPageAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ElementValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.ObjectValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.RawValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.value.StringValue;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceButton;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfacePageElement;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceText;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceTitleText;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.WebinterfaceVerticalSpacer;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.layout.DefaultLayoutOption;
import me.mrletsplay.webinterfaceapi.webinterface.session.WebinterfaceSession;

public class TranslatePage extends WebinterfacePage {
	
	public TranslatePage() {
		super("Translate", "/t/translate", true);
		
		WebinterfacePageSection s = new WebinterfacePageSection();
		s.addDynamicElements(() -> {
			HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
			String locale = ctx.getClientHeader().getPath().getQueryParameterValue("locale");
			String path = ctx.getClientHeader().getPath().getQueryParameterValue("path");
			if(locale == null || path == null || !GraphiteTranslations.LOCALES.contains(locale)) return Collections.emptyList();
			
			TranslationData dt = GraphiteTranslations.getTranslationData().stream()
					.filter(t -> t.getLocaleIdentifier().equals(locale) && t.getPath().equals(path))
					.findFirst().orElse(null);
			if(dt == null) return Collections.emptyList();
			
			List<WebinterfacePageElement> els = new ArrayList<>();
			
			els.add(WebinterfaceTitleText.builder()
					.text("Path")
					.leftbound()
					.centeredVertically()
					.create());
			
			els.add(WebinterfaceText.builder()
					.text(dt.getPath())
					.leftbound()
					.withLayoutOptions(DefaultLayoutOption.SECOND_TO_LAST_COLUMN)
					.centeredVertically()
					.create());
			
			els.add(WebinterfaceTitleText.builder()
					.text("Special Roles")
					.leftbound()
					.centeredVertically()
					.create());
			
			els.add(WebinterfaceText.builder()
					.text(dt.getMessageInfo().getSpecialRoles().isEmpty() ? "(none)" : dt.getMessageInfo().getSpecialRoles().stream().collect(Collectors.joining(", ")))
					.leftbound()
					.withLayoutOptions(DefaultLayoutOption.SECOND_TO_LAST_COLUMN)
					.centeredVertically()
					.create());
			
			els.add(WebinterfaceTitleText.builder()
					.text("Fallback")
					.leftbound()
					.centeredVertically()
					.create());
			
			els.add(WebinterfaceText.builder()
					.text(dt.getMessageInfo().getFallback())
					.leftbound()
					.withLayoutOptions(DefaultLayoutOption.SECOND_TO_LAST_COLUMN)
					.centeredVertically()
					.create());
			
			els.add(WebinterfaceTitleText.builder()
					.text("Placeholders")
					.leftbound()
					.centeredVertically()
					.create());
			
			els.add(WebinterfaceText.builder()
					.text(dt.getMessageInfo().getPlaceholders().isEmpty() ? "(none)" : dt.getMessageInfo().getPlaceholders().stream().collect(Collectors.joining(", ")))
					.leftbound()
					.withLayoutOptions(DefaultLayoutOption.SECOND_TO_LAST_COLUMN)
					.centeredVertically()
					.create());
			
			els.add(WebinterfaceTitleText.builder()
					.text("Current Translation")
					.leftbound()
					.centeredVertically()
					.create());
			
			els.add(WebinterfaceText.builder()
					.text(dt.getText() == null ? "(none)" : dt.getText())
					.leftbound()
					.withLayoutOptions(DefaultLayoutOption.SECOND_TO_LAST_COLUMN)
					.centeredVertically()
					.create());
			
			List<String> errors = dt.checkErrors();
			if(!errors.isEmpty()) {
				els.add(WebinterfaceTitleText.builder()
						.text("Possible Issues")
						.leftbound()
						.centeredVertically()
						.create());
				
				els.add(WebinterfaceText.builder()
						.text(errors.stream().collect(Collectors.joining(", ")))
						.leftbound()
						.withLayoutOptions(DefaultLayoutOption.SECOND_TO_LAST_COLUMN)
						.centeredVertically()
						.create());
			}

			WebinterfaceAccount acc = WebinterfaceSession.getCurrentSession().getAccount();
			
			if(dt.getStatus() != TranslationStatus.ACCEPTED) {
				els.add(new WebinterfaceVerticalSpacer("30px"));
				
				TranslateArea input = new TranslateArea("Translation");
				input.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				els.add(input);
				
				WebinterfaceButton confirm = new WebinterfaceButton("Confirm & Next");
				confirm.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				ObjectValue v = new ObjectValue();
				v.put("locale", new StringValue(locale));
				v.put("path", new StringValue(path));
				v.put("translation", new ElementValue(input));
				confirm.setOnClickAction(new SendJSAction("gtranslations", "submitTranslation", v).onSuccess(new RedirectAction("/t/next?locale=" + locale)));
				els.add(confirm);
				
				if(acc.hasPermission("gtranslate.accept")) {
					WebinterfaceButton confirmAccept = new WebinterfaceButton("Confirm & Accept");
					confirmAccept.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
					ObjectValue v2 = new ObjectValue();
					v2.put("locale", new StringValue(locale));
					v2.put("path", new StringValue(path));
					v2.put("translation", new ElementValue(input));
					v2.put("accept", new RawValue("true"));
					confirmAccept.setOnClickAction(new SendJSAction("gtranslations", "submitTranslation", v2).onSuccess(new RedirectAction("/t/next?locale=" + locale + "&mode=t")));
					els.add(confirmAccept);
				}
			}
			
			if(dt.getStatus() == TranslationStatus.SUBMITTED && acc.hasPermission("gtranslate.accept")) {
				WebinterfaceButton confirm = new WebinterfaceButton("Accept");
				confirm.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				ObjectValue v = new ObjectValue();
				v.put("locale", new StringValue(locale));
				v.put("path", new StringValue(path));
				confirm.setOnClickAction(new SendJSAction("gtranslations", "acceptTranslation", v).onSuccess(new RedirectAction("/t/next?locale=" + locale + "&mode=t")));
				els.add(confirm);
			}
			
			if(dt.getStatus() == TranslationStatus.ACCEPTED && acc.hasPermission("gtranslate.accept")) {
				els.add(new WebinterfaceVerticalSpacer("30px"));
				
				WebinterfaceButton confirm = new WebinterfaceButton("Unaccept");
				confirm.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				ObjectValue v = new ObjectValue();
				v.put("locale", new StringValue(locale));
				v.put("path", new StringValue(path));
				confirm.setOnClickAction(new SendJSAction("gtranslations", "unacceptTranslation", v).onSuccess(new ReloadPageAction()));
				els.add(confirm);
			}
			
			return els;
		});
		addSection(s);
	}

}
