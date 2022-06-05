package me.mrletsplay.gtranslations.page;

import java.util.List;
import java.util.stream.Collectors;

import me.mrletsplay.gtranslations.GraphiteTranslations;
import me.mrletsplay.gtranslations.TranslationData;
import me.mrletsplay.gtranslations.TranslationStatus;
import me.mrletsplay.gtranslations.element.TranslateArea;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;
import me.mrletsplay.webinterfaceapi.auth.Account;
import me.mrletsplay.webinterfaceapi.page.Page;
import me.mrletsplay.webinterfaceapi.page.PageSection;
import me.mrletsplay.webinterfaceapi.page.action.RedirectAction;
import me.mrletsplay.webinterfaceapi.page.action.ReloadPageAction;
import me.mrletsplay.webinterfaceapi.page.action.SendJSAction;
import me.mrletsplay.webinterfaceapi.page.action.value.ActionValue;
import me.mrletsplay.webinterfaceapi.page.action.value.ObjectValue;
import me.mrletsplay.webinterfaceapi.page.dynamic.DynamicMultiple;
import me.mrletsplay.webinterfaceapi.page.element.Button;
import me.mrletsplay.webinterfaceapi.page.element.PageElement;
import me.mrletsplay.webinterfaceapi.page.element.Text;
import me.mrletsplay.webinterfaceapi.page.element.TitleText;
import me.mrletsplay.webinterfaceapi.page.element.VerticalSpacer;
import me.mrletsplay.webinterfaceapi.page.element.builder.Align;
import me.mrletsplay.webinterfaceapi.page.element.layout.DefaultLayoutOption;
import me.mrletsplay.webinterfaceapi.session.Session;

public class TranslatePage extends Page {
	
	public TranslatePage() {
		super("Translate", "/t/translate", true);
		
		PageSection s = new PageSection();
		s.dynamic((DynamicMultiple<PageElement>) els -> {
			HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
			String locale = ctx.getClientHeader().getPath().getQuery().getFirst("locale");
			String path = ctx.getClientHeader().getPath().getQuery().getFirst("path");
			if(locale == null || path == null || !GraphiteTranslations.LOCALES.contains(locale)) return;
			
			TranslationData dt = GraphiteTranslations.getTranslationData().stream()
					.filter(t -> t.getLocaleIdentifier().equals(locale) && t.getPath().equals(path))
					.findFirst().orElse(null);
			if(dt == null) return;
			
			els.add(TitleText.builder()
					.text("Path")
					.leftboundText()
					.align(Align.TOP_LEFT)
					.create());
			
			els.add(Text.builder()
					.text(dt.getPath())
					.leftboundText()
					.withLayoutOptions(DefaultLayoutOption.SECOND_TO_LAST_COLUMN)
					.create());
			
			els.add(TitleText.builder()
					.text("Special Roles")
					.leftboundText()
					.align(Align.TOP_LEFT)
					.create());
			
			els.add(Text.builder()
					.text(dt.getMessageInfo().getSpecialRoles().isEmpty() ? "(none)" : dt.getMessageInfo().getSpecialRoles().stream().collect(Collectors.joining(", ")))
					.leftboundText()
					.withLayoutOptions(DefaultLayoutOption.SECOND_TO_LAST_COLUMN)
					.create());
			
			els.add(TitleText.builder()
					.text("Fallback")
					.leftboundText()
					.align(Align.TOP_LEFT)
					.create());
			
			els.add(Text.builder()
					.text(dt.getMessageInfo().getFallback())
					.leftboundText()
					.withLayoutOptions(DefaultLayoutOption.SECOND_TO_LAST_COLUMN)
					.create());
			
			els.add(TitleText.builder()
					.text("Placeholders")
					.leftboundText()
					.align(Align.TOP_LEFT)
					.create());
			
			els.add(Text.builder()
					.text(dt.getMessageInfo().getPlaceholders().isEmpty() ? "(none)" : dt.getMessageInfo().getPlaceholders().stream().collect(Collectors.joining(", ")))
					.leftboundText()
					.withLayoutOptions(DefaultLayoutOption.SECOND_TO_LAST_COLUMN)
					.create());
			
			els.add(TitleText.builder()
					.text("Current Translation")
					.leftboundText()
					.align(Align.TOP_LEFT)
					.create());
			
			els.add(Text.builder()
					.text(dt.getText() == null ? "(none)" : dt.getText())
					.leftboundText()
					.withLayoutOptions(DefaultLayoutOption.SECOND_TO_LAST_COLUMN)
					.create());
			
			List<String> errors = dt.checkErrors();
			if(!errors.isEmpty()) {
				els.add(TitleText.builder()
						.text("Possible Issues")
						.leftboundText()
						.align(Align.TOP_LEFT)
						.create());
				
				els.add(Text.builder()
						.text(errors.stream().collect(Collectors.joining(", ")))
						.leftboundText()
						.withLayoutOptions(DefaultLayoutOption.SECOND_TO_LAST_COLUMN)
						.create());
			}

			Account acc = Session.getCurrentSession().getAccount();
			
			if(dt.getStatus() != TranslationStatus.ACCEPTED) {
				els.add(new VerticalSpacer("30px"));
				
				TranslateArea input = new TranslateArea("Translation");
				input.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				els.add(input);
				
				Button confirm = new Button("Confirm & Next");
				confirm.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				ObjectValue v = ActionValue.object();
				v.put("locale", ActionValue.string(locale));
				v.put("path", ActionValue.string(path));
				v.put("translation", ActionValue.elementValue(input));
				confirm.setOnClickAction(SendJSAction.of("gtranslations", "submitTranslation", v).onSuccess(RedirectAction.to("/t/next?locale=" + locale)));
				els.add(confirm);
				
				if(acc.hasPermission("gtranslate.accept")) {
					Button confirmAccept = new Button("Confirm & Accept");
					confirmAccept.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
					ObjectValue v2 = ActionValue.object();
					v2.put("locale", ActionValue.string(locale));
					v2.put("path", ActionValue.string(path));
					v2.put("translation", ActionValue.elementValue(input));
					v2.put("accept", ActionValue.bool(true));
					confirmAccept.setOnClickAction(SendJSAction.of("gtranslations", "submitTranslation", v2).onSuccess(RedirectAction.to("/t/next?locale=" + locale + "&mode=t")));
					els.add(confirmAccept);
				}
			}
			
			if(dt.getStatus() == TranslationStatus.SUBMITTED && acc.hasPermission("gtranslate.accept")) {
				Button confirm = new Button("Accept");
				confirm.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				ObjectValue v = ActionValue.object();
				v.put("locale", ActionValue.string(locale));
				v.put("path", ActionValue.string(path));
				confirm.setOnClickAction(SendJSAction.of("gtranslations", "acceptTranslation", v).onSuccess(RedirectAction.to("/t/next?locale=" + locale + "&mode=t")));
				els.add(confirm);
			}
			
			if(dt.getStatus() == TranslationStatus.ACCEPTED && acc.hasPermission("gtranslate.accept")) {
				els.add(new VerticalSpacer("30px"));
				
				Button confirm = new Button("Unaccept");
				confirm.addLayoutOptions(DefaultLayoutOption.FULL_WIDTH);
				ObjectValue v = ActionValue.object();
				v.put("locale", ActionValue.string(locale));
				v.put("path", ActionValue.string(path));
				confirm.setOnClickAction(SendJSAction.of("gtranslations", "unacceptTranslation", v).onSuccess(ReloadPageAction.reload()));
				els.add(confirm);
			}
		});
		addSection(s);
	}

}
